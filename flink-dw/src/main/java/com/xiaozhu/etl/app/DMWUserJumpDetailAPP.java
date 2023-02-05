package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternFlatTimeoutFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;

import static org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.getExecutionEnvironment;

/**
 * 用户跳出明细统计
 */
public class DMWUserJumpDetailAPP {
    public static void main(String[] args) throws Exception {
        //TODO 1 基本环境准备
        //1.1 流处理环境
        StreamExecutionEnvironment env = getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);
        //TODO 2 检查点设置（略）

        //TODO 3 从kafka中读取数据
        //3.1 声明消费主题以及消费者组
        String topic = "dwd_page_log";
        String groupId = "user_jump_detail_app_group";
        //3.2 获取kafka消费者对象
        FlinkKafkaConsumer<String> kafkaSource = KafkaUtils.getKafkaSource(topic, groupId);
        //3.3 读取数据封装流
        // DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);
        //3.3 读取数据封装流
        //DataStreamSource<String> kafkaDS = env.addSource(kafkaSource);
        DataStream<String> kafkaDS = env
                .fromElements(
                        "{\"common\":{\"mid\":\"101\"},\"page\":{\"page_id\":\"home\"},\"ts\":10000} ",
                        "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"home\"},\"ts\":12000}",
                        "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"good_list\",\"last_page_id\":" +
                                "\"home\"},\"ts\":15000} ",
                        "{\"common\":{\"mid\":\"102\"},\"page\":{\"page_id\":\"good_list\",\"last_page_id\":" +
                                "\"detail\"},\"ts\":30000} "
                );

        //TODO 4 对读取的数据进行类型转换 String -> JSONObject
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.map(JSON::parseObject);
        jsonObjDS.print(">>>");
        // TODO 5.指定watermark以及提取事件时间字段
        SingleOutputStreamOperator<JSONObject> jsonObjWithWatermark = jsonObjDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<JSONObject>forMonotonousTimestamps()
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<JSONObject>() {
                                    @Override
                                    public long extractTimestamp(JSONObject jsonObj, long recordTimestamp) {
                                        return jsonObj.getLong("ts");
                                    }
                                }
                        )
        );

        // TODO 6 按照mid进行分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWatermark.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"));

        // TODO 7 定义pattern
        // 跳出 or 跳转的3个条件
        // 必须是一个新会话，lastPageId为空 访问了其他页面
        // 不能超过一定时间
        Pattern<JSONObject, JSONObject> pattern = Pattern.<JSONObject>begin("first").where(
                        // 条件1：开启一个新的会话访问
                        new SimpleCondition<JSONObject>() {
                            @Override
                            public boolean filter(JSONObject jsonObj) throws Exception {
                                String lastPageId = jsonObj.getJSONObject("page").getString("last_page_id");
                                if (lastPageId == null || lastPageId.length() == 0) {
                                    return true;
                                }
                                return false;
                            }
                        }
                ).next("second").where(
                        // 条件2：访问了网站的其他页面
                        new SimpleCondition<JSONObject>() {
                            @Override
                            public boolean filter(JSONObject jsonObj) throws Exception {
                                String pageId = jsonObj.getJSONObject("page").getString("page_id");
                                if (pageId != null && pageId.length() > 0) {
                                    return true;
                                }
                                return false;
                            }
                        }
                )
                // within方法：定文匹配模式的事件序列出现的最大时间间隔。
                // 如果未完成的事件序列超过了这个事件，就会被丢弃:
                .within(Time.seconds(10));

        // TODO 8 将pattern应用到流上
        PatternStream<JSONObject> patternDS = CEP.pattern(keyedDS, pattern);

        // TODO 9 从流中提取数据
        //9.1 定义侧输出流标记，FlinkCDC会将超时数据匹配放到侧数据流中
        OutputTag<String> timeoutTag = new OutputTag<>("timeoutTag");
        // 9.2 提取数据
//        patternDS.select(
//                timeoutTag,
//                new PatternTimeoutFunction<JSONObject, String>() {
//                    @Override
//                    public String timeout(Map<String, List<JSONObject>> pattern, long timestamp) throws Exception {
//                        return null;
//                    }
//                },
//                new PatternSelectFunction<JSONObject, String>() {
//                    @Override
//                    public String select(Map<String, List<JSONObject>> map) throws Exception {
//                        return null;
//                    }
//                }
//        );

        SingleOutputStreamOperator<String> resDS = patternDS.flatSelect(
                timeoutTag,
                // 处理超时数据 -- 跳出，需要进行统计
                new PatternFlatTimeoutFunction<JSONObject, String>() {
                    @Override
                    public void timeout(Map<String, List<JSONObject>> pattern, long timestamp, Collector<String> out) throws Exception {
                        List<JSONObject> jsonObjectList = pattern.get("first");
                        for (JSONObject jsonObj : jsonObjectList) {
                            out.collect(jsonObj.toJSONString());
                        }
                    }
                },
                // 处理完全匹配的数据 -- 跳转，不在此需求统计范围之内
                new PatternFlatSelectFunction<JSONObject, String>() {
                    @Override
                    public void flatSelect(Map<String, List<JSONObject>> map, Collector<String> collector) throws Exception {

                    }
                }
        );

        // 9.3 从侧输出流中获取超时数据（跳出）
        DataStream<String> jumpDS = resDS.getSideOutput(timeoutTag);

        // TODO 10 将跳出明细写到kafka的dwm层主题
        jumpDS.addSink(KafkaUtils.getKafkaSink("dwm_user_jump_detail"));
        env.execute();
    }

}
