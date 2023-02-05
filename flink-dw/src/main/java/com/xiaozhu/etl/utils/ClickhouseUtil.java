package com.xiaozhu.etl.utils;

import com.xiaozhu.etl.annotations.TransientSink;
import com.xiaozhu.etl.common.Config;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 操作ClickHouse的工具类
 */
public class ClickhouseUtil {
    public static <T> SinkFunction<T> getJdbcSink(String sql){
        // "insert into visitor_stats_2022 values(?,?,?,?,?,?,?,?,?,?,?,?)"
        SinkFunction<T> sinkFunction = JdbcSink.<T>sink(
                sql,
                new JdbcStatementBuilder<T>() {
                    // 参数 T obj：就是流中的一条数据
                    @Override
                    public void accept(PreparedStatement ps, T obj) throws SQLException {
                        // 获取流中obj属性值，赋值给问号
                        // 获取流中对象所属类的属性
                        Field[] fields = obj.getClass().getDeclaredFields();
                        int skipNum = 0;
                        // 对属性数组进行遍历
                        for (int i = 0; i < fields.length; i++) {
                            // 获取每一个属性对象
                            Field field = fields[i];
                            // 判断该属性是否有@trannsient注解修饰
                            TransientSink transientSink = field.getAnnotation(TransientSink.class);
                            if (transientSink != null){
                                skipNum++;
                                continue;
                            }
                            // 设置私有属性的访问权限
                            field.setAccessible(true);
                            try {
                                // 获取对象的属性值
                                Object fieldValue = field.get(obj);
                                // 将属性的值赋值给问号占位符
                                // JDBC相关操作和查询结果集的列从1开始
                                ps.setObject(i+1-skipNum,fieldValue);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                // 构造者设计模式
                new JdbcExecutionOptions.Builder()
                        // 5条数据为一批，一批处理一次
                        // 4个并行度，每一个slot数量到5，才会保存到ClickHouse
                        .withBatchSize(5)
//                        // 2s后直接保存到ClickHouse
//                        .withBatchIntervalMs(2000)
//                        // 最大重试次数为3
//                        .withMaxRetries(3)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withDriverName("ru.yandex.clickhouse.ClickHouseDriver")
                        .withUrl(Config.CLICKHOUSE_URL)
                        .build()
        );
        return sinkFunction;
    }
}

