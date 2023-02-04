package com.tutorial.table;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Main {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        String createOdsTablSql = "CREATE TABLE IF NOT EXISTS ods_app_log ...";
        String createDimTableSql = "CREATE TABLE IF NOT EXISTS dim_app ...";
        String createDwsResultSql = "CREATE TABLE IF NOT EXISTS dws_company_vist ...";
        String insertIntoSql = "insert into xxx as  select ...";

        // 基于流创建一张表
        
    }
}
