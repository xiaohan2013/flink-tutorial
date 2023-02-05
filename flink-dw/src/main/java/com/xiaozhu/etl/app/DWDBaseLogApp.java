package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

/**
 * 对日志数据进行分流操作
 *  启动、曝光、页面
 *    启动日志放到启动侧输出流中
 *    曝光日志放到曝光侧输出流中
 *    页面日志放到主流中
 *  将不同流的数据写回到kafka的dwd主题中
 */

public class DWDBaseLogApp {
    public static void main(String[] args) throws Exception {
        // TODO 1 基本环境准备
        // 流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度
        env.setParallelism(4);
        // TODO 2 检查点相关设置
        // 开启检查点
        // 每5S中开启一次检查点，检查点模式为EXACTLY_ONCE
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        //   设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        //   设置重启策略
        // 重启三次，每次间隔3s钟
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
        //   设置job取消后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //   设置状态后端 -- 基于内存 or 文件系统 or RocksDB
        //   内存：状态存在TaskManager内存中，检查点存在JobManager内存中
        //   文件系统：状态存在TaskManager内存中，检查点存在指定的文件系统路径中
        //   RocksDB：看做和Redis类似的数据库，状态存在TaskManager内存中，检查点存在JobManager内存和本地磁盘上
        //   hadoop中nm的地址
        env.setStateBackend(new FsStateBackend("hdfs://hadoop101:8020/ck/gmall"));
        //   指定操作HDFS的用户
        System.setProperty("HADOOP_USER_NAME","hzy");
        // TODO 3 从kafka读取数据
        // 声明消费的主题和消费者组
        String topic = "ods_base_log";
        String groupId = "base_log_app_group";

        // 获取kafka消费者
        FlinkKafkaConsumer<String> kafkaSource = KafkaUtils.getKafkaSource(topic, groupId);
        // 读取数据，封装为流
        DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);
        // TODO 4 对读取的数据进行结构的转换 jsonStr -> jsonObj
//        // 匿名内部类实现
//        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.map(
//                new MapFunction<String, JSONObject>() {
//                    @Override
//                    public JSONObject map(String jsonStr) throws Exception {
//                        return JSON.parseObject(jsonStr);
//                    }
//                }
//        );
//        // lambda表达式实现
//        kafkaDS.map(
//                jsonStr -> JSON.parse(jsonStr)
//        );

        // 方法的默认调用，注意导入的是alibaba JSON包
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.map(JSON::parseObject);

        jsonObjDS.print(">>>");

        // TODO 5 修复新老访客状态

        // TODO 6 按照日志类型对日志进行分流

        // TODO 7 将不同流的数据写到kafka的dwd不同主题中


        env.execute();
    }
}
