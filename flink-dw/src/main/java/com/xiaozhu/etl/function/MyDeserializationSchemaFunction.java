package com.xiaozhu.etl.function;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

public class MyDeserializationSchemaFunction implements DebeziumDeserializationSchema<String> {
    // // 反序列化，对Kafka中的数据进行反序列化化
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {
        // 导入的是org.apache.kafka.connnect.data包
        Struct valueStruct = (Struct) sourceRecord.value();
        // 获取数据的来源
        Struct afterStruct = valueStruct.getStruct("after");
        // 获取数据库和表名的来源
        Struct sourceStruct = valueStruct.getStruct("source");
        // 获取数据库
        String database = sourceStruct.getString("db");
        // 获取表名
        String table = sourceStruct.getString("table");
        // 获取操作类型
//        String op = valueStruct.getString("op");
        String type = Envelope.operationFor(sourceRecord).toString().toLowerCase();
        if(type.equals("create")){
            type = "insert";
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("database",database);
        jsonObj.put("table",table);
        jsonObj.put("type",type);
        // 获取影响的数据
        // 删除时，afterStruct为空
        JSONObject dataJsonObj = new JSONObject();
        if (afterStruct != null){
            // schema获取源数据的格式,fields获取里面的各个元素
            for (Field field : afterStruct.schema().fields()) {
                String fieldName = field.name();
                Object fieldValue = afterStruct.get(field);
                dataJsonObj.put(fieldName,fieldValue);
            }
        }
        // 删除操作会使得data属性不为空，但size为0
        jsonObj.put("data",dataJsonObj);

        // 向下游发送数据
        collector.collect(jsonObj.toJSONString()) ;
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return TypeInformation.of(String.class);
    }
}
