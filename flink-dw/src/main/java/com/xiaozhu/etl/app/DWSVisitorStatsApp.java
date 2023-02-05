package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.bean.VisitorStats;
import com.xiaozhu.etl.utils.ClickhouseUtil;
import com.xiaozhu.etl.utils.DateTimeUtil;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Date;

/**
 * 访客主题统计dws
 */
public class DWSVisitorStatsApp {
    public static void main(String[] args) throws Exception {
        // TODO 1 基本环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        // TODO 2 从kafka中读取数据
        // 2.1 声明读取的主题和消费者组
        String pageViewSourceTopic = "dwd_page_log";
        String uniqueVisitSourceTopic = "dwm_unique_visitor";
        String userJumpDetailSourceTopic = "dwm_user_jump_detail";
        String groupId = "visitor_stats_app";

        // 2.2 获取kafka消费者
        FlinkKafkaConsumer<String> pvSource = KafkaUtils.getKafkaSource(pageViewSourceTopic, groupId);
        FlinkKafkaConsumer<String> uvSource = KafkaUtils.getKafkaSource(uniqueVisitSourceTopic, groupId);
        FlinkKafkaConsumer<String> ujdSource = KafkaUtils.getKafkaSource(userJumpDetailSourceTopic, groupId);

        // 2.3 读取数据，封装成流
        DataStreamSource<String> pvStrDS = env.addSource(pvSource);
        DataStreamSource<String> uvStrDS = env.addSource(uvSource);
        DataStreamSource<String> ujdStrDS = env.addSource(ujdSource);

        pvStrDS.print("1111");
        uvStrDS.print("2222");
        ujdStrDS.print("3333");

        // TODO 3 对流中的数据进行类型转换 jsonStr -> VisitorStats
        // 3.1 dwd_page_loge流中数据的转化
        SingleOutputStreamOperator<VisitorStats> pvStatsDS = pvStrDS.map(
                new MapFunction<String, VisitorStats>() {
                    @Override
                    public VisitorStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                        JSONObject pageJsonObj = jsonObj.getJSONObject("page");
                        VisitorStats visitorStats = new VisitorStats(
                                "",
                                "",
                                commonJsonObj.getString("vc"),
                                commonJsonObj.getString("ch"),
                                commonJsonObj.getString("ar"),
                                commonJsonObj.getString("is_new"),
                                0L,
                                1L,
                                0L,
                                0L,
                                pageJsonObj.getLong("during_time"),
                                jsonObj.getLong("ts")
                        );
                        // 判断是否为新的会话，是则sessionViewCount + 1
                        String lastPageId = pageJsonObj.getString("last_page_id");
                        if (lastPageId == null || lastPageId.length() == 0) {
                            visitorStats.setSv_ct(1L);
                        }
                        return visitorStats;
                    }
                }
        );
        // 3.2 dwm_unique_visitor流中数据的转化
        SingleOutputStreamOperator<VisitorStats> uvStatsDS = uvStrDS.map(
                new MapFunction<String, VisitorStats>() {
                    @Override
                    public VisitorStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                        VisitorStats visitorStats = new VisitorStats(
                                "",
                                "",
                                commonJsonObj.getString("vc"),
                                commonJsonObj.getString("ch"),
                                commonJsonObj.getString("ar"),
                                commonJsonObj.getString("is_new"),
                                1L,
                                0L,
                                0L,
                                0L,
                                0L,
                                jsonObj.getLong("ts")
                        );
                        return visitorStats;
                    }
                }
        );
        // 3.3 dwm_user_jump_detail流中数据的转化
        SingleOutputStreamOperator<VisitorStats> ujdStatsDS = ujdStrDS.map(
                new MapFunction<String, VisitorStats>() {
                    @Override
                    public VisitorStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        JSONObject commonJsonObj = jsonObj.getJSONObject("common");
                        VisitorStats visitorStats = new VisitorStats(
                                "",
                                "",
                                commonJsonObj.getString("vc"),
                                commonJsonObj.getString("ch"),
                                commonJsonObj.getString("ar"),
                                commonJsonObj.getString("is_new"),
                                0L,
                                0L,
                                0L,
                                1L,
                                0L,
                                jsonObj.getLong("ts")
                        );
                        return visitorStats;
                    }
                }
        );
        // TODO 4 将三条流转换后的数据进行合并
        DataStream<VisitorStats> unionDS = pvStatsDS.union(uvStatsDS, ujdStatsDS);

        unionDS.print(">>>");
        // TODO 5 指定Watermark以及提取事件时间字段
        SingleOutputStreamOperator<VisitorStats> visitorStatsWithWatermarkDS = unionDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<VisitorStats>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<VisitorStats>() {
                                    @Override
                                    public long extractTimestamp(VisitorStats visitorStats, long recordTimestamp) {
                                        return visitorStats.getTs();
                                    }
                                }
                        )
        );

        // TODO 6 按照维度对流中的数据进行分组
        //维度有：版本，渠道，地区，新老访客 定义分组的key为Tuple4类型
        KeyedStream<VisitorStats, Tuple4<String, String, String, String>> keyedDS = visitorStatsWithWatermarkDS.keyBy(
                new KeySelector<VisitorStats, Tuple4<String, String, String, String>>() {
                    @Override
                    public Tuple4<String, String, String, String> getKey(VisitorStats visitorStats) throws Exception {
                        return Tuple4.of(
                                visitorStats.getVc(),
                                visitorStats.getCh(),
                                visitorStats.getAr(),
                                visitorStats.getIs_new()
                        );
                    }
                }
        );

        // TODO 7 对分组之后的数据，进行开窗处理
        // 每个分组是独立的窗口，分组之间互不影响
        WindowedStream<VisitorStats, Tuple4<String, String, String, String>, TimeWindow> windowDS = keyedDS.window(TumblingEventTimeWindows.of(Time.seconds(10)));

        // TODO 8 聚合计算
        // 对窗口中的数据进行两两聚合计算
        SingleOutputStreamOperator<VisitorStats> reduceDS = windowDS.reduce(
                new ReduceFunction<VisitorStats>() {
                    @Override
                    public VisitorStats reduce(VisitorStats stats1, VisitorStats stats2) throws Exception {
                        // 度量值进行两两相加
                        stats1.setPv_ct(stats1.getPv_ct() + stats2.getPv_ct());
                        stats1.setUv_ct(stats1.getUv_ct() + stats2.getUv_ct());
                        stats1.setSv_ct(stats1.getSv_ct() + stats2.getSv_ct());
                        stats1.setDur_sum(stats1.getDur_sum() + stats2.getDur_sum());
                        stats1.setUj_ct(stats1.getUj_ct() + stats2.getUj_ct());
                        return stats1;
                    }
                },
                new ProcessWindowFunction<VisitorStats, VisitorStats, Tuple4<String, String, String, String>, TimeWindow>() {
                    @Override
                    public void process(Tuple4<String, String, String, String> Tuple4, Context context, Iterable<VisitorStats> elements, Collector<VisitorStats> out) throws Exception {
                        // 补全时间字段的值
                        for (VisitorStats visitorStats : elements) {
                            visitorStats.setStt(DateTimeUtil.toYMDHMS(new Date(context.window().getStart())));
                            visitorStats.setEdt(DateTimeUtil.toYMDHMS(new Date(context.window().getEnd())));
                            // 操作时间为当前系统时间
                            visitorStats.setTs(System.currentTimeMillis());
                            // 将处理之后的数据向下游发送
                            out.collect(visitorStats);
                        }
                    }
                }
        );

        reduceDS.print(">>>");
        // TODO 9 将聚合统计之后的数据写到ClickHouse
        reduceDS.addSink(
                ClickhouseUtil.getJdbcSink("insert into visitor_stats_2022 values(?,?,?,?,?,?,?,?,?,?,?,?)")
        );

        env.execute();
    }
}
