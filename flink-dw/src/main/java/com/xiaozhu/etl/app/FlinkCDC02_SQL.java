package com.xiaozhu.etl.app;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class FlinkCDC02_SQL {
        public static void main(String[] args) throws Exception {
            //TODO 1.准备环境
            //1.1流处理环境
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);
            //1.2 表执行环境
            StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

            //TODO 2.创建动态表
            tableEnv.executeSql("CREATE TABLE user_info (" +
                    "  id INT," +
                    "  name STRING," +
                    "  age INT" +
                    ") WITH (" +
                    "  'connector' = 'mysql-cdc'," +
                    "  'hostname' = 'hadoop101'," +
                    "  'port' = '3306'," +
                    "  'username' = 'root'," +
                    "  'password' = '123456'," +
                    "  'database-name' = 'gmall2022_realtime'," +
                    "  'table-name' = 't_user'" +
                    ")");

            tableEnv.executeSql("select * from user_info").print();

            //TODO 6.执行任务
            env.execute();
        }
}
