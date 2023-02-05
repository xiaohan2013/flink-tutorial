package com.xiaozhu.etl.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.java.tuple.Tuple2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * 获取redis的java客户端Jedis
 */
public class RedisUtil {
    // 声明JedisPool连接池
    private static JedisPool jedisPool;
    public static Jedis getJedis(){
        if(jedisPool == null){
            initJedisPool();
        }
        System.out.println("获取Redis连接...");
        return jedisPool.getResource();
    }

    // 初始化连接池对象
    private static void initJedisPool() {
        // 连接池配置对象
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大连接数
        jedisPoolConfig.setMaxTotal(100);
        // 每次连接时是否进行ping pong测试
        jedisPoolConfig.setTestOnBorrow(true);
        // 连接耗尽是否等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 等待时间
        jedisPoolConfig.setMaxWaitMillis(2000);
        // 最小空闲连接数
        jedisPoolConfig.setMinIdle(5);
        // 最大空闲连接数
        jedisPoolConfig.setMaxTotal(5);

        jedisPool = new JedisPool(jedisPoolConfig,"hadoop101",6379,10000);
    }

    public static JSONObject getDimInfo(String tableName, Tuple2<String,String>... colNameAndValues){
        // 拼接查询维度的SQL
        StringBuilder selectDimSql = new StringBuilder("select * from " + tableName + " where ");
        // 拼接redis的key
        StringBuilder redisKey = new StringBuilder("dim:"+tableName.toLowerCase()+":");

        for (int i = 0; i < colNameAndValues.length; i++) {
            Tuple2<String, String> colNameAndValue = colNameAndValues[i];
            String colName = colNameAndValue.f0;
            String colValue = colNameAndValue.f1;

            selectDimSql.append(colName + "='" + colValue + "'");
            redisKey.append(colValue);

            if (i < colNameAndValues.length - 1){
                selectDimSql.append(" and ");
                redisKey.append("_");
            }
        }
        // 先根据key到redis中查询缓存的维度数据
        // 声明操作redis的客户端
        Jedis jedis = null;
        // 声明变量，用于接受从redis中查询出来的缓存数据
        String jsonStr = null;
        // 声明变量，用于处理返回的维度对象
        JSONObject dimInfoJsonObj = null;

        try {
            jedis = RedisUtil.getJedis();
            // 从redis中获取维度数据
            jsonStr = jedis.get(redisKey.toString());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("从redis中查询维度数据发生了异常...");
        }

        // 判断是否从redis中获取到了维度缓存数据
        if (jsonStr != null && jsonStr.length() > 0){
            // 从redis中查到了维度的缓存数据，将缓存的维度字符串转换为json对象
            dimInfoJsonObj = JSON.parseObject(jsonStr);
        } else {
            // 从redis中没有查到维度的缓存数据，发送请求到phoenix库中去查询
            System.out.println("查询sql的语句：" + selectDimSql);
            // 底层调用的还是之前封装的查询phoenix表数据的方法
            List<JSONObject> dimList = PhoenixUtil.queryList(selectDimSql.toString(), JSONObject.class);

            if (dimList != null && dimList.size() > 0){
                // 根据维度数据的主键去查询，所以只会返回一条数据
                dimInfoJsonObj = dimList.get(0);
                // 将从phoenix中查询出来的维度数据，写到redis缓存中
                if (jedis != null){
                    jedis.setex(redisKey.toString(),3600*24,dimInfoJsonObj.toJSONString());
                }
            } else {
                System.out.println("维度数据没找到：" + selectDimSql);
            }
        }

        if (jedis != null){
            jedis.close();
            System.out.println("关闭redis连接。");
        }

        return dimInfoJsonObj;
    }

    // 获取主键：
    public static JSONObject getDimInfo(String tableName, String id){
        return getDimInfo(tableName,Tuple2.of("ID",id));
    }



    public static void main(String[] args) {
        Jedis jedis = getJedis();
        String pong = jedis.ping();
        System.out.println(pong);
    }
}
