package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.bean.TableProcess;
import com.xiaozhu.etl.common.MySQLSource;
import com.xiaozhu.etl.function.DimSink;
import com.xiaozhu.etl.function.HbaseTableProcessFunction;
import com.xiaozhu.etl.function.MyDeserializationSchemaFunction;
import com.xiaozhu.etl.utils.KafkaUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class DWDDBApp {
    public static void main(String[] args) throws Exception {
        //TODO 1 基本环境准备
        //流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度
        env.setParallelism(4);

//        //TODO 2 检查点设置
//        //开启检查点
//        env.enableCheckpointing(5000L,CheckpointingMode.EXACTLY_ONCE);
//        // 设置检查点超时时间
//        env.getCheckpointConfig().setCheckpointTimeout(60000L);
//        // 设置重启策略
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
//        // 设置job取消后，检查点是否保留
//        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
//        // 设置状态后端 -- 基于内存 or 文件系统 or RocksDB
//        env.setStateBackend(new FsStateBackend("hdfs://hadoop101:8020/ck/gmall"));
//        // 指定操作HDFS的用户
//        System.setProperty("HADOOP_USER_NAME","hzy");

        //TODO 3 从kafka中读取数据
        //声明消费的主题以及消费者组
        String topic = "ods_base_db_m";
        String groupId = "base_db_app_group";
        // 获取消费者对象
        FlinkKafkaConsumer<String> kafkaSource = KafkaUtils.getKafkaSource(topic, groupId);
        // 读取数据，封装成流
        DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);

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
//        filterDS.print("<<<");

        //TODO 6 使用FlinkCDC读取配置表数据
        //获取dataSource
        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");
        SourceFunction<String> mySqlSourceFunction = MySQLSource.<String>builder()
                .hostname("hadoop101")
                .port(3306)
                .username("root")
                .password("123456")
                // 可配置多个库
                .databaseList("gmall2022_realtime")
                ///可选配置项,如果不指定该参数,则会读取上一个配置中指定的数据库下的所有表的数据
                //注意：指定的时候需要使用"db.table"的方式
                .tableList("gmall2022_realtime.table_process")
                .debeziumProperties(props)
                .deserializer(new MyDeserializationSchemaFunction())
                .build();

        // 读取数据封装流
        DataStreamSource<String> mySqlDS = env.addSource(mySqlSourceFunction);

        // 为了让每一个并行度上处理业务数据的时候，都能使用配置流的数据，需要将配置流广播下去
        // 想要使用广播状态，状态描述器只能是map，使用map状态存储
        MapStateDescriptor<String, TableProcess> mapStateDescriptor =
                new MapStateDescriptor<>("table_process", String.class, TableProcess.class);

        BroadcastStream<String> broadcastDS = mySqlDS.broadcast(mapStateDescriptor);

        // 调用非广播流的connect方法，将业务流与配置流进行连接
        BroadcastConnectedStream<JSONObject, String> connectDS = filterDS.connect(broadcastDS);

        //TODO 7 动态分流，将维度数据放到维度侧输出流，事实数据放到主流中
        //声明维度侧输出流的标记
        OutputTag<JSONObject> dimTag = new OutputTag<JSONObject>("dimTag") {};
        SingleOutputStreamOperator<JSONObject> realDS = connectDS.process(
                new HbaseTableProcessFunction(dimTag,mapStateDescriptor)
        );
        // 获取维度侧输出流
        DataStream<JSONObject> dimDS = realDS.getSideOutput(dimTag);
        realDS.print("事实数据：");
        dimDS.print("维度数据：");

        //TODO 8 将维度侧输出流的数据写到hbase(Phoenix)中
        dimDS.addSink(new DimSink());

        //TODO 9 将主流数据写回kafka的dwd层
        realDS.addSink(
                KafkaUtils.getKafkaSinkBySchema(new KafkaSerializationSchema<JSONObject>() {
                    @Override
                    public ProducerRecord<byte[], byte[]> serialize(JSONObject jsonObj, @Nullable Long timestamp) {
                        return new ProducerRecord<byte[], byte[]>(jsonObj.getString("sink_table"),
                                jsonObj.getJSONObject("data").toJSONString().getBytes());
                    }
                })
        );

        env.execute();
    }
}
