package com.xiaozhu.etl.common;

public class Config {
    public static final String HBASE_SCHEMA = "GMALL2022_REALTIME";
    public static final String PHOENIX_SERVER = "jdbc:phoenix:hadoop101,hadoop102,hadoop103:2181";
    public static final String CLICKHOUSE_URL = "jdbc:clickhouse://hadoop101:8123/default";
}
