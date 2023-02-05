package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.text.SimpleDateFormat;

public class DWMUniqueVisitApp {
    public static void main(String[] args) throws Exception {
        //TODO 1 基本环境准备
        //1.1 流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2 检查点设置
//        //2.1 开启检查点
//        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
//        //2.2 设置检查点超时时间
//        env.getCheckpointConfig().setCheckpointTimeout(60000L);
//        //2.3 设置重启策略
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
//        //2.4 设置job取消后，检查点是否保留
//        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
//        //2.5 设置状态后端 -- 基于内存 or 文件系统 or RocksDB
//        env.setStateBackend(new FsStateBackend("hdfs://hadoop101:8020/ck/gmall"));
//        //2.6 指定操作HDFS的用户
//        System.setProperty("HADOOP_USER_NAME","hzy");

        //TODO 3 从kafka中读取数据
        //3.1 声明消费主题以及消费者组
        String topic = "dwd_page_log";
        String groupId = "union_visitor_app_group";
        //3.2 获取kafka消费者对象
        FlinkKafkaConsumer<String> kafkaSource = KafkaUtils.getKafkaSource(topic, groupId);
        //3.3 读取数据封装流
        DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);

        //TODO 4 对读取的数据进行类型转换 String -> JSONObject
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.map(JSON::parseObject);

        jsonObjDS.print(">>>");

        //TODO 5 按照设备id对数据进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(
                jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        //TODO 6 实现过滤
        //实现目的：如有一个用户在6月访问一次，11月访问一次，6-11月共访问两次，
        // 如果一直保留其6月的访问状态，直到11月才去更新，会消耗很多资源，
        // 所以需要将其访问时间放入状态中，定时进行更新。
        SingleOutputStreamOperator<JSONObject> filterDS = keyedDS.filter(
                new RichFilterFunction<JSONObject>() {
                    // 声明状态变量，用于存放上次访问日期
                    private ValueState<String> lastVistDateState;
                    // 声明日期格式工具类
                    private SimpleDateFormat sdf;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        sdf = new SimpleDateFormat("yyyyMMdd");
                        ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<>("lastVistDateState", String.class);
                        // 注意：UV可以延伸为日活统计，其状态值主要用于筛选当天是否访问过
                        // 那么状态超过今天就没有存在的意义
                        // 所以设置状态的失效时间为1天
                        // 粒度为天，不记录时分秒
                        StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.days(1))
                                // 默认值，当状态创建或者写入的时候会更新状态失效时间
//                                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                // 默认值，状态过期后，如果还没有被清理，是否返回给状态调用者
//                                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                                .build();
                        valueStateDescriptor.enableTimeToLive(ttlConfig);
                        lastVistDateState = getRuntimeContext().getState(valueStateDescriptor);
                    }

                    @Override
                    public boolean filter(JSONObject jsonObj) throws Exception {
                        // 如果从其他页面跳转过来，直接过滤掉
                        String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                        if (lastPageId != null && lastPageId.length() > 0) {
                            return false;
                        }
                        // 获取状态中的上次访问日期
                        String lastVisitDate = lastVistDateState.value();
                        String curVisitDate = sdf.format(jsonObj.getLong("ts"));
                        if (lastVisitDate != null && lastVisitDate.length() > 1 && lastVisitDate.equals(curVisitDate)) {
                            // 今天已经访问过
                            return false;
                        } else {
                            // 今天还没访问过
                            lastVistDateState.update(curVisitDate);
                            return true;
                        }
                    }
                }
        );

        filterDS.print(">>>");

        env.execute();
    }
}
