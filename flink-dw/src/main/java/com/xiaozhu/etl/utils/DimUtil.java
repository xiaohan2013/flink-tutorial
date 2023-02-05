package com.xiaozhu.etl.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.common.Config;
import org.apache.flink.api.java.tuple.Tuple2;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.util.List;

/**
 * 查询维度数据的工具类
 */
public class DimUtil {
    // 从phoenix表中查询维度数据
    // 返回值类型：{"ID":"1","TM_NAME":"zhangsan"}
    // 参数类型："select * from dim_base_trademark where id = '1' and TM_NAME = 'AAA' ", JSONObject.class
    public static JSONObject getDimInfoNoCache(String tableName, Tuple2<String,String> ... colNameAndValues){
        // 拼接查询维度的SQL
        StringBuilder selectDimSql = new StringBuilder("select * from " + tableName + " where 1=1 ");
        for (int i = 0; i < colNameAndValues.length; i++) {
            // 去掉1 = 1，则第一个条件前不加或最后一个条件后不加
//            if(i >= 1){
//                selectDimSql.append(" and ");
//            }
            Tuple2<String, String> colNameAndValue = colNameAndValues[i];
            String colName = colNameAndValue.f0;
            String colValue = colNameAndValue.f1;
            selectDimSql.append("and " + colName + "='" + colValue + "'");
            // 最后一个条件后不加
//            if(i < colNameAndValues.length - 1){
//                selectDimSql.append(" and ");
//            }
        }
        System.out.println("查询sql的语句：" + selectDimSql);
        // 底层调用的还是之前封装的查询phoenix表数据的方法
        List<JSONObject> dimList = PhoenixUtil.queryList(selectDimSql.toString(), JSONObject.class);
        JSONObject dimInfoJsonObj = null;
        if (dimList != null && dimList.size() > 0){
            // 根据维度数据的主键去查询，所以只会返回一条数据
            dimInfoJsonObj = dimList.get(0);
        } else {
            System.out.println("维度数据没找到：" + selectDimSql);
        }
        return dimInfoJsonObj;
    }

    // 根据redis中的key删除redis中的记录
    public static void deleteCached(String tableName, String id) {
        String redisKey = "dim:"+tableName.toLowerCase()+":"+ id;
        try {
            Jedis jedis = RedisUtil.getJedis();
            jedis.del(redisKey);
            jedis.close();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("删除redis缓存发生了异常");
        }
    }

    public static JSONObject getDimInfo(String tableName, Tuple2<String,String> ... colNameAndValues){
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

    public static JSONObject getDimInfo(String tableName, String id){
        return getDimInfo(tableName,Tuple2.of("ID",id));
    }

    public static JSONObject queryDimInfo(Connection connection,
                                            String tableName,
                                            String id ) throws Exception {
        // 查询redis之前，先查询redis
        // 存jsonStr 用string   不用hash  （1）用户维度数据量大；（2）需要设置过期时间
        Jedis jedis = RedisUtil.getJedis();
        String redisKey = "DIM:" + tableName + ":" +id;
        String dimInfoJsonStr = jedis.get(redisKey);
        if(dimInfoJsonStr != null){
            // 重置过期时间
            jedis.expire(redisKey,24 * 3600);
            jedis.close();
            return JSONObject.parseObject(dimInfoJsonStr);
        }

        String querySql = "select * from " + Config.HBASE_SCHEMA  + "." + tableName + " where id ='" + id +"'";

        List<JSONObject> jsonObjects = JdbcUtil.queryList(connection, querySql, JSONObject.class, false);

        // 一秒处理200条数据（单并行度）
        JSONObject dimInfoJsonObject = jsonObjects.get(0);
        // 将数据写入到redis
        jedis.set(redisKey,dimInfoJsonObject.toJSONString());
        // 设置过期时间
        jedis.expire(redisKey,24 * 3600);
        jedis.close();

        //返回数据
        return dimInfoJsonObject;
    }

    public static void delRedisData(String tableName,String id){
        Jedis jedis = RedisUtil.getJedis();
        String redisKey = "DIM:" + tableName + ":" +id;
        jedis.del(redisKey);
        jedis.close();
    }

    public static void main(String[] args) {
        JSONObject dimInfo = DimUtil.getDimInfoNoCache("dim_base_trademark", Tuple2.of("id", "13"));
        System.out.println(dimInfo);
    }
}
