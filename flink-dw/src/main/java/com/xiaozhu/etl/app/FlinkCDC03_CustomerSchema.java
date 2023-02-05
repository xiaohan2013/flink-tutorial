package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.xiaozhu.etl.bean.TableProcess;
import com.xiaozhu.etl.common.MySQLSource;
import com.xiaozhu.etl.function.TableProcessFunction;
import com.xiaozhu.etl.utils.KafkaUtils;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Properties;

/**
 * 通过FlinkCDC动态读取MySQL表中的数据 -- DataStreamAPI
 *
 * 自定义反序列化器
 */
public class FlinkCDC03_CustomerSchema {
    public static void main(String[] args) throws Exception {
        //TODO 1 准备流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //TODO 3 从kafka中读取数据
        //声明消费的主题以及消费者组
        String topic = "ods_base_db_m";
        String groupId = "base_db_app_group";
        // 获取消费者对象
        FlinkKafkaConsumer<String> kafkaSource = KafkaUtils.getKafkaSource(topic, groupId);
        // 读取数据，封装成流
        DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);

        //TODO 3 创建Flink-MySQL-CDC的Source
        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");
        SourceFunction<String> sourceFunction = MySQLSource.<String>builder()
                .hostname("hadoop101")
                .port(3306)
                .username("root")
                .password("123456")
                // 可配置多个库
                .databaseList("gmall2022_realtime")
                ///可选配置项,如果不指定该参数,则会读取上一个配置中指定的数据库下的所有表的数据
                //注意：指定的时候需要使用"db.table"的方式
                .tableList("gmall2022_realtime.t_user")
                .debeziumProperties(props)
                .deserializer(new MySchema())
                .build();

        //TODO 4 使用CDC Source从MySQL读取数据
        DataStreamSource<String> mysqlDS = env.addSource(sourceFunction);

        // 为了让每一个并行度上处理业务数据的时候，都能使用配置流的数据，需要将配置流广播下去
        // 想要使用广播状态，状态描述器只能是map
        MapStateDescriptor<String, TableProcess> mapStateDescriptor =
                new MapStateDescriptor<>("table_process", String.class, TableProcess.class);

        BroadcastStream<String> broadcastDS = mysqlDS.broadcast(mapStateDescriptor);

        //TODO 4 对数据类型进行转换 String -> JSONObject
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.map(JSON::parseObject);

        //TODO 5 简单的ETL
        SingleOutputStreamOperator<JSONObject> filterDS = jsonObjDS.filter(
                new FilterFunction<JSONObject>() {
                    @Override
                    public boolean filter(JSONObject jsonobj) throws Exception {
                        boolean flag =
                                jsonobj.getString("table") != null &&
                                        jsonobj.getString("table").length() > 0 &&
                                        jsonobj.getJSONObject("data") != null &&
                                        jsonobj.getString("data").length() > 3;
                        return flag;
                    }
                }
        );
        // 调用非广播流的connect方法，将业务流与配置流进行连接
        BroadcastConnectedStream<JSONObject, String> connectDS = filterDS.connect(broadcastDS);

        //TODO 7 动态分流，将维度数据放到维度侧输出流，事实数据放到主流中
        //声明维度侧输出流的标记
        OutputTag<JSONObject> dimTag = new OutputTag<JSONObject>("dimTag") {};
        SingleOutputStreamOperator<JSONObject> realDS = connectDS.process(
                new TableProcessFunction(dimTag,mapStateDescriptor)
        );
        // 获取维度侧输出流
        DataStream<JSONObject> dimDS = realDS.getSideOutput(dimTag);
        realDS.print("事实数据：");
        dimDS.print("维度数据：");

        //TODO 5 打印输出
        mysqlDS.print();

        //TODO 6 执行任务
        env.execute();
    }
}

class MySchema implements DebeziumDeserializationSchema<String> {

    /**
     * ConnectRecord{
     *     value=Struct{
     *        after=Struct{id=1,name=zhangsan,age=18},
     *        source=Struct{
     *           db=gmall2022_realtime,
     *           table=t_user
     *                },
     *        op=c
     *     },
     * }
     */
    // 反序列化
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
        collector.collect(jsonObj.toJSONString());
    }

    // 指定类型
    @Override
    public TypeInformation<String> getProducedType() {
        return TypeInformation.of(String.class);
    }
}

