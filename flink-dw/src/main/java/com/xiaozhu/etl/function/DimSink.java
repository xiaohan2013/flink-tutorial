package com.xiaozhu.etl.function;

import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.common.Config;
import com.xiaozhu.etl.utils.DimUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 将维度侧输出流的数据写到hbase(Phoenix)中
 */
public class DimSink extends RichSinkFunction<JSONObject> {
    private Connection conn;


    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn = DriverManager.getConnection(Config.PHOENIX_SERVER);
    }

    @Override
    public void invoke(JSONObject jsonObj, Context context) throws Exception {
        // 上游传递过来的数据格式如下：
        // {"database":"gmall2022",
        // "data":{"tm_name":"a","id":13},
        // "commit":true,
        // "sink_table":"dim_base_trademark",
        // "type":"insert",
        // "table":"base_trademark","
        // ts":1670131087}

        // 获取维度表表名
        String tableName = jsonObj.getString("sink_table");
        // 获取数据
        JSONObject dataJsonObj = jsonObj.getJSONObject("data");
        // 拼接插入语句 upsert into 表空间.表 (a,b,c) values(aa,bb,cc);
        String upsertSql = genUpsertSql(tableName,dataJsonObj);

        System.out.println("向phoenix维度表中插入数据的sql：" + upsertSql);

        PreparedStatement ps = null;
        try {
            // 创建数据库操作对象
            ps = conn.prepareStatement(upsertSql);
            // 执行sql语句
            ps.executeUpdate();
            // 手动提交事务，phoenix的连接实现类不是自动提交事务
            conn.commit();
        }catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException("向phoenix维度表中插入数据失败了");
        } finally {
            // 释放资源
            if (ps != null){
                ps.close();
            }
        }

        // 如果当前维度数据进行删除或者修改，清空redis缓存中的数据
        if (jsonObj.getString("type").equals("update") || jsonObj.getString("type").equals("delete")){
            DimUtil.deleteCached(tableName,dataJsonObj.getString("id"));
        }

    }

    // 拼接插入语句
    private String genUpsertSql(String tableName, JSONObject dataJsonObj) {
        // id 10
        // tm_name zs
        String upsertSql = "upsert into " + Config.HBASE_SCHEMA + "."+ tableName +
                " ("+ StringUtils.join(dataJsonObj.keySet(),",") +
                ") values('"+ StringUtils.join(dataJsonObj.values(),"','")+"')";
        return upsertSql;
    }
}
