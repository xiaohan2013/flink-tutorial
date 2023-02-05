package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

public class DWDBaseDBApp {
    public static void main(String[] args) throws Exception {
        //TODO 1 基本环境准备
        //流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度
        env.setParallelism(1);

        //TODO 2 检查点设置
        //开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        // 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        // 设置重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
        // 设置job取消后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 设置状态后端 -- 基于内存 or 文件系统 or RocksDB
        env.setStateBackend(new FsStateBackend("hdfs://hadoop101:8020/ck/gmall"));
        // 指定操作HDFS的用户
        System.setProperty("HADOOP_USER_NAME","hzy");

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
        filterDS.print("<<<");

        //TODO 6 动态分流

        //TODO 7 将维度侧输出流的数据写到Hbase中

        //TODO 8 将主流数据写回kafka的dwd层

        env.execute();
    }
}
