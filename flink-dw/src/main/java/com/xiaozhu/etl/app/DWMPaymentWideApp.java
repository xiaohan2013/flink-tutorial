package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.xiaozhu.etl.bean.OrderWide;
import com.xiaozhu.etl.bean.PaymentInfo;
import com.xiaozhu.etl.utils.DateTimeUtil;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.time.Duration;

public class DWMPaymentWideApp {
    public static void main(String[] args) throws Exception {
        //TODO 1.基本环境准备
        //1.1 流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2.检查点相关设置(略)

        //TODO 3.从Kafka中读取数据
        //3.1 声明消费的主题以及消费者组
        String paymentInfoSourceTopic = "dwd_payment_info";
        String orderWideSourceTopic = "dwm_order_wide";
        String groupId = "payment_wide_app_group";
        //3.2 获取kafka消费者
        FlinkKafkaConsumer<String> paymentInfoSource = KafkaUtils.getKafkaSource(paymentInfoSourceTopic, groupId);
        FlinkKafkaConsumer<String> orderWideSource = KafkaUtils.getKafkaSource(orderWideSourceTopic, groupId);
        //3.3 读取数据  封装为流
        DataStreamSource<String> paymentInfoStrDS = env.addSource(paymentInfoSource);
        DataStreamSource<String> orderWideStrDS = env.addSource(orderWideSource);

        //TODO 4.对流中数据类型进行转换    String ->实体对象
        //支付
        SingleOutputStreamOperator<PaymentInfo> paymentInfoDS = paymentInfoStrDS.map(jsonStr -> JSON.parseObject(jsonStr, PaymentInfo.class));
        //订单宽表
        SingleOutputStreamOperator<OrderWide> orderWideInfoDS = orderWideStrDS.map(jsonStr -> JSON.parseObject(jsonStr, OrderWide.class));

        paymentInfoDS.print("支付数据：");
        orderWideInfoDS.print("订单宽表：");

        //TODO 5.指定Watermark并提取事件时间字段
        //支付
        SingleOutputStreamOperator<PaymentInfo> paymentInfoWithWatermarkDS = paymentInfoDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<PaymentInfo>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<PaymentInfo>() {
                                    @Override
                                    public long extractTimestamp(PaymentInfo paymentInfo, long recordTimestamp) {
//                                        String callbackTimeStr = paymentInfo.getCallback_time();
//                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                        Long ts = null;
//                                        try {
//                                            ts = sdf.parse(callbackTimeStr).getTime();
//                                        } catch (ParseException e) {
//                                            e.printStackTrace();
//                                        }
//                                        return ts;
                                        return DateTimeUtil.toTs(paymentInfo.getCallback_time());
                                    }
                                }
                        )
        );
        // 订单宽表
        SingleOutputStreamOperator<OrderWide> orderWideWithWatermarkDS = orderWideInfoDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<OrderWide>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<OrderWide>() {
                                    @Override
                                    public long extractTimestamp(OrderWide orderWide, long recordTimestamp) {
//                                        String createTimeStr = orderWide.getCreate_time();
//                                        // 每调用一次创建一个对象，不好
//                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-HH-dd HH:mm:ss");
//                                        Long ts = null;
//                                        try {
//                                            ts = sdf.parse(createTimeStr).getTime();
//                                        } catch (ParseException e) {
//                                            e.printStackTrace();
//                                        }
//                                        return ts;
                                        return DateTimeUtil.toTs(orderWide.getCreate_time());
                                    }
                                }
                        )
        );

//        // TODO 7 支付和订单宽表的双流join
//        SingleOutputStreamOperator<PaymentWide> paymentWideDS = paymentInfoKeyedDS
//                .intervalJoin(orderWideKeyedDS)
//                .between(Time.minutes(-30), Time.minutes(0))
//                .process(
//                        new ProcessJoinFunction<PaymentInfo, OrderWide, PaymentWide>() {
//                            @Override
//                            public void processElement(PaymentInfo paymentInfo, OrderWide orderWide, Context ctx, Collector<PaymentWide> out) throws Exception {
//                                out.collect(new PaymentWide(paymentInfo, orderWide));
//                            }
//                        }
//                );
//
//        paymentWideDS.print(">>>>");

        // TODO 8 将支付宽表数据写到kafka的dwm_payment_wide
//        paymentWideDS
//                .map(paymentWide -> JSON.toJSONString(paymentWide))
//                .addSink(KafkaUtils.getKafkaSink("dwm_payment_wide"));

        env.execute();
    }
}
