package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.bean.OrderDetail;
import com.xiaozhu.etl.bean.OrderInfo;
import com.xiaozhu.etl.bean.OrderWide;
import com.xiaozhu.etl.function.DimAsyncFunction;
import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 订单宽表的准备
 * A.intervalJoin(B)
 *  .between(下界,上界)
 *  .process()
 */
public class DWMOrderWideApp {
    public static void main(String[] args) throws Exception {
        //TODO 1 基本环境准备
        //1.1 流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //1.2 设置并行度
        env.setParallelism(4);

        //TODO 2 检查点设置（略）

        //TODO 3 从kafka中读取数据
        //3.1 声明消费主题以及消费者组
        String orderInfoSourceTopic = "dwd_order_info";
        String orderDetailSourceTopic = "dwd_order_detail";
        String groupId = "order_wide_app_group";

        //3.2 获取kafka消费者对象
        // 订单
        FlinkKafkaConsumer<String> orderInfoKafkaSource = KafkaUtils.getKafkaSource(orderInfoSourceTopic, groupId);
        // 订单明细
        FlinkKafkaConsumer<String> orderDetailKafkaSource = KafkaUtils.getKafkaSource(orderDetailSourceTopic, groupId);
        //3.3 读取数据，封装为流
        // 订单流
        DataStreamSource<String> orderInfoStrDS = env.addSource(orderInfoKafkaSource);
        // 订单明细流
        DataStreamSource<String> orderDetailStrDS = env.addSource(orderDetailKafkaSource);

        //TODO 4 对流中数据类型进行转换 String -> 实体对象
        //订单
        SingleOutputStreamOperator<OrderInfo> orderInfoDS = orderInfoStrDS.map(
                new RichMapFunction<String, OrderInfo>() {
                    private SimpleDateFormat sdf;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }

                    @Override
                    public OrderInfo map(String jsonStr) throws Exception {
                        OrderInfo orderInfo = JSON.parseObject(jsonStr, OrderInfo.class);
                        orderInfo.setCreate_ts(sdf.parse(orderInfo.getCreate_time()).getTime());
                        return orderInfo;
                    }
                }
        );
        // 订单明细
        SingleOutputStreamOperator<OrderDetail> orderDetailDS = orderDetailStrDS.map(
                new RichMapFunction<String, OrderDetail>() {
                    private SimpleDateFormat sdf;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }

                    @Override
                    public OrderDetail map(String jsonStr) throws Exception {
                        OrderDetail orderDetail = JSON.parseObject(jsonStr, OrderDetail.class);
                        orderDetail.setCreate_ts(sdf.parse(orderDetail.getCreate_time()).getTime());
                        return orderDetail;
                    }
                }
        );

        orderInfoDS.print("订单信息：");
        orderDetailDS.print("订单明细：");

        // join
        // 基于窗口的join，join和coGroup
        // 基于状态缓存(temporal table join)：interval join
        // TODO 5 指定Watermark并提取事件时间字段
        //订单
        SingleOutputStreamOperator<OrderInfo> orderInfoWithWatermarkDS = orderInfoDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<OrderInfo>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<OrderInfo>() {
                                    @Override
                                    public long extractTimestamp(OrderInfo orderInfo, long recordTimestamp) {
                                        return orderInfo.getCreate_ts();
                                    }
                                }
                        )
        );
        // 订单明细
        SingleOutputStreamOperator<OrderDetail> orderDetailWithWatermarkDS = orderDetailDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<OrderDetail>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<OrderDetail>() {
                                    @Override
                                    public long extractTimestamp(OrderDetail orderDetail, long recordTimestamp) {
                                        return orderDetail.getCreate_ts();
                                    }
                                }
                        )
        );


        // TODO 6 通过分组指定两流的关联字段 -- order_id
        // 订单
        KeyedStream<OrderInfo, Long> orderInfoKeyedDS = orderInfoWithWatermarkDS.keyBy(OrderInfo::getId);
        // 订单明细
        KeyedStream<OrderDetail, Long> orderDetailkeyedDS = orderDetailWithWatermarkDS.keyBy(OrderDetail::getOrder_id);

        // TODO 7 双流join，使用intervalJoin
        // 用订单（一）join订单明细（多）
        SingleOutputStreamOperator<OrderWide> orderWideDS = orderInfoKeyedDS
                .intervalJoin(orderDetailkeyedDS)
                .between(Time.seconds(-5), Time.seconds(5))
                .process(
                        new ProcessJoinFunction<OrderInfo, OrderDetail, OrderWide>() {
                            @Override
                            public void processElement(OrderInfo orderInfo, OrderDetail orderDetail, Context ctx, Collector<OrderWide> out) throws Exception {
                                out.collect(new OrderWide(orderInfo, orderDetail));
                            }
                        }
                );
        orderWideDS.print(">>>");

        // TODO 8 和用户维度进行关联
        // 以下方式（同步）实现效率低
//        orderWideDS.map(
//                new MapFunction<OrderWide, OrderWide>() {
//                    @Override
//                    public OrderWide map(OrderWide orderWide) throws Exception {
//                        Long user_id = orderWide.getUser_id();
//                        JSONObject userDimInfo = DimUtil.getDimInfo("dim_user_info", user_id.toString());
//                        String gender = userDimInfo.getString("GENDER");
//                        orderWide.setUser_gender(gender);
//                        return null;
//                    }
//                }
//        );
        // 异步操作，和用户表关联
        SingleOutputStreamOperator<OrderWide> orderWideWithUserDS = AsyncDataStream.unorderedWait(
                orderWideDS,
                // 动态绑定
                new DimAsyncFunction<OrderWide>("DIM_USER_INFO") {
                    @Override
                    public void join(OrderWide orderWide, JSONObject dimJsonObj) throws Exception {
                        String gender = dimJsonObj.getString("GENDER");
                        orderWide.setUser_gender(gender);

                        // 2000-10-02
                        String birthday = dimJsonObj.getString("BIRTHDAY");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date birthdayDate = sdf.parse(birthday);
                        long diffMs = System.currentTimeMillis() - birthdayDate.getTime();
                        long ageLong = diffMs / 1000L / 60L / 60L / 24L / 365L;
                        orderWide.setUser_age((int) ageLong);
                    }

                    @Override
                    public String getKey(OrderWide orderWide) {
                        return orderWide.getUser_id().toString();
                    }

                },
                60,
                TimeUnit.SECONDS
        );

        orderWideWithUserDS.print(">>>");

        // TODO 9 和地区维度进行关联
        SingleOutputStreamOperator<OrderWide> orderWideWithProvinceDS = AsyncDataStream.unorderedWait(
                orderWideWithUserDS,
                new DimAsyncFunction<OrderWide>("DIM_BASE_PROVINCE") {
                    // name
                    // area_code
                    // iso_code
                    // iso_3166_2
                    @Override
                    public void join(OrderWide orderWide, JSONObject dimJsonObj) throws Exception {
                        orderWide.setProvince_name(dimJsonObj.getString("NAME"));
                        orderWide.setProvince_area_code("AREA_CODE");
                        orderWide.setProvince_iso_code("ISO_CODE");
                        orderWide.setProvince_3166_2_code("ISO_3166_2");
                    }

                    @Override
                    public String getKey(OrderWide orderWide) {
                        return orderWide.getProvince_id().toString();
                    }
                },
                60,
                TimeUnit.SECONDS
        );

        orderWideWithProvinceDS.print();

        // TODO 10 和sku维度进行关联
        SingleOutputStreamOperator<OrderWide> orderWideWithSkuDS = AsyncDataStream.unorderedWait(
                orderWideWithProvinceDS,
                new DimAsyncFunction<OrderWide>("DIM_SKU_INFO") {
                    @Override
                    public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                        orderWide.setSku_name(jsonObject.getString("SKU_NAME"));
                        orderWide.setCategory3_id(jsonObject.getLong("CATEGORY3_ID"));
                        orderWide.setSpu_id(jsonObject.getLong("SPU_ID"));
                        orderWide.setTm_id(jsonObject.getLong("TM_ID"));
                    }

                    @Override
                    public String getKey(OrderWide orderWide) {
                        return String.valueOf(orderWide.getSku_id());
                    }
                }, 60, TimeUnit.SECONDS);


        // 关联SPU维度
        SingleOutputStreamOperator<OrderWide> orderWideWithSpuDS = AsyncDataStream.unorderedWait(
                orderWideWithSkuDS,
                new DimAsyncFunction<OrderWide>("DIM_SPU_INFO") {
                    @Override
                    public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                        orderWide.setSpu_name(jsonObject.getString("SPU_NAME"));
                    }

                    @Override
                    public String getKey(OrderWide orderWide) {
                        return String.valueOf(orderWide.getSpu_id());
                    }
                }, 60, TimeUnit.SECONDS);

        //  关联品类维度
        SingleOutputStreamOperator<OrderWide> orderWideWithCategory3DS = AsyncDataStream.unorderedWait(
                orderWideWithSpuDS,
                new DimAsyncFunction<OrderWide>("DIM_BASE_CATEGORY3") {
                    @Override
                    public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                        orderWide.setCategory3_name(jsonObject.getString("NAME"));
                    }

                    @Override
                    public String getKey(OrderWide orderWide) {
                        return String.valueOf(orderWide.getCategory3_id());
                    }
                }, 60, TimeUnit.SECONDS);

        // 关联品牌维度
        SingleOutputStreamOperator<OrderWide> orderWideWithTmDS = AsyncDataStream.unorderedWait(
                orderWideWithCategory3DS, new DimAsyncFunction<OrderWide>("DIM_BASE_TRADEMARK") {
                    @Override
                    public void join(OrderWide orderWide, JSONObject jsonObject) throws Exception {
                        orderWide.setTm_name(jsonObject.getString("TM_NAME"));
                    }

                    @Override
                    public String getKey(OrderWide orderWide) {
                        return String.valueOf(orderWide.getTm_id());
                    }
                }, 60, TimeUnit.SECONDS);

        //
        // TODO 14 将订单宽表数据写回到kafka的dwd_order_wide主题中
        // JSON.parseObject(jsonStr) : 将json格式的字符串转换为JSON对象
        // JSON.parseObject(jsonStr,类型) : 将json格式的字符串转换为指定格式对象
        // JSON.toJSONString(orderWide) : 将对象转换为JSON格式字符串
        orderWideWithTmDS
                // 将OrderWide对象转换为JSON格式字符串
                .map(orderWide -> JSON.toJSONString(orderWide))
                .addSink(
                        // 放到同一个主题中
                        KafkaUtils.getKafkaSink("dwm_order_wide")
                );

        env.execute();
    }
}
