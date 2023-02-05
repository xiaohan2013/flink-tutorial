package com.xiaozhu.etl.common;

import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Properties;

public class MySQLSource<T> implements SourceFunction<T> {
    @Override
    public void run(SourceContext ctx) throws Exception {

    }

    @Override
    public void cancel() {

    }


    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String hostname;

        private int port;

        private String username;
        private String password;

        private String databaseList;

        private String tableList;

        private Properties debeziumProperties;

        private DebeziumDeserializationSchema deserializer;

        public Builder hostname(String hostname){
            this.hostname = hostname;
            return this;
        }

        public Builder port(int port){
            this.port = port;
            return this;
        }

        public Builder username(String username){
            this.username = username;
            return this;
        }

        public Builder password(String password){
            this.password = password;
            return this;
        }

        public Builder databaseList(String databaseList){
            this.databaseList = databaseList;
            return this;
        }


        public Builder tableList(String tableList){
            this.tableList = tableList;
            return this;
        }

        public Builder debeziumProperties(Properties debeziumProperties){
            this.debeziumProperties = debeziumProperties;
            return this;
        }

        public Builder deserializer(DebeziumDeserializationSchema deserializer){
            this.deserializer = deserializer;
            return this;
        }


        public MySQLSource build(){
            return new MySQLSource();
        }
    }
}
