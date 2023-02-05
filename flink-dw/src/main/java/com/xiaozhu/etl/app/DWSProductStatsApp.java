package com.xiaozhu.etl.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaozhu.etl.annotations.TransientSink;
import com.xiaozhu.etl.bean.OrderWide;
import com.xiaozhu.etl.bean.PaymentWide;
import com.xiaozhu.etl.bean.ProductStats;
import com.xiaozhu.etl.common.Constant;
import com.xiaozhu.etl.function.DimAsyncFunction;
import com.xiaozhu.etl.utils.ClickhouseUtil;
import com.xiaozhu.etl.utils.DateTimeUtil;
import com.xiaozhu.etl.utils.KafkaUtils;
import lombok.Builder;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import javax.naming.Context;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 商品主题统计DWS
 */
public class DWSProductStatsApp {
    public static void main(String[] args) throws Exception {
        // TODO 1 基本环境准备
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        // TODO 2 检查点相关配置（略）

        // TODO 3 从kafka主题中读取数据
        // 3.1 声明消费主题及消费者组
        String groupId = "product_stats_app";
        String pageViewSourceTopic = "dwd_page_log";
        String orderWideSourceTopic = "dwm_order_wide";
        String paymentWideSourceTopic = "dwm_payment_wide";
        String cartInfoSourceTopic = "dwd_cart_info";
        String favorInfoSourceTopic = "dwd_favor_info";
        String refundInfoSourceTopic = "dwd_order_refund_info";
        String commentInfoSourceTopic = "dwd_comment_info";
        // 3.2 创建消费者对象
        FlinkKafkaConsumer<String> pageViewSource  = KafkaUtils.getKafkaSource(pageViewSourceTopic,groupId);
        FlinkKafkaConsumer<String> orderWideSource  = KafkaUtils.getKafkaSource(orderWideSourceTopic,groupId);
        FlinkKafkaConsumer<String> paymentWideSource  = KafkaUtils.getKafkaSource(paymentWideSourceTopic,groupId);
        FlinkKafkaConsumer<String> favorInfoSourceSouce  = KafkaUtils.getKafkaSource(favorInfoSourceTopic,groupId);
        FlinkKafkaConsumer<String> cartInfoSource  = KafkaUtils.getKafkaSource(cartInfoSourceTopic,groupId);
        FlinkKafkaConsumer<String> refundInfoSource  = KafkaUtils.getKafkaSource(refundInfoSourceTopic,groupId);
        FlinkKafkaConsumer<String> commentInfoSource  = KafkaUtils.getKafkaSource(commentInfoSourceTopic,groupId);
        // 3.3 读取数据，封装为流
        DataStreamSource<String> pageViewStrDS = env.addSource(pageViewSource);
        DataStreamSource<String> favorInfoStrDS = env.addSource(favorInfoSourceSouce);
        DataStreamSource<String> orderWideStrDS= env.addSource(orderWideSource);
        DataStreamSource<String> paymentWideStrDS= env.addSource(paymentWideSource);
        DataStreamSource<String> cartInfoStrDS= env.addSource(cartInfoSource);
        DataStreamSource<String> refundInfoStrDS= env.addSource(refundInfoSource);
        DataStreamSource<String> commentInfoStrDS= env.addSource(commentInfoSource);

        //
        // TODO 4 对流中的数据进行类型转换 jsonStr -> ProductStats
        // 4.1  转换点击以及曝光流数据
        // 转换点击以及曝光流数据
        SingleOutputStreamOperator<ProductStats> clickAndDisplayStatsDS = pageViewStrDS.process(
                new ProcessFunction<String, ProductStats>() {
                    @Override
                    public void processElement(String jsonStr, Context ctx, Collector<ProductStats> out) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        Long ts = jsonObj.getLong("ts");
                        // 判断是否为点击行为
                        JSONObject pageJsonObj = jsonObj.getJSONObject("page");
                        String pageId = pageJsonObj.getString("page_id");
                        if (pageId.equals("good_detail")) {
                            // 如果当前日志记录的页面是商品的详情页，认为这条日志记录的是点击行为
                            Long skuId = pageJsonObj.getLong("item");
                            // 下面代码等同于 new 外部类.内部类().build();
                            ProductStats productStats = ProductStats.builder()
                                    .sku_id(skuId)
                                    .click_ct(1L)
                                    .ts(ts)
                                    .build();
                            out.collect(productStats);
                        }

                        // 判断是否为曝光行为
                        JSONArray displays = jsonObj.getJSONArray("displays");
                        if (displays != null && displays.size() > 0) {
                            // 如果displays数组不为空，说明页面上有曝光行为，对所有曝光行为进行遍历
                            for (int i = 0; i < displays.size(); i++) {
                                JSONObject displaysJsonObj = displays.getJSONObject(i);
                                // 判断曝光的是否为商品
                                if (displaysJsonObj.getString("item_type").equals("sku_id")) {
                                    Long skuId = displaysJsonObj.getLong("item");
                                    ProductStats productStats1 = ProductStats.builder()
                                            .sku_id(skuId)
                                            .display_ct(1L)
                                            .ts(ts)
                                            .build();
                                    out.collect(productStats1);
                                }
                            }
                        }
                    }
                }
        );

        // 4.2 转化收藏流数据 转化收藏流数据
        SingleOutputStreamOperator<ProductStats> favorStatsDS = favorInfoStrDS.map(
                new MapFunction<String, ProductStats>() {
                    @Override
                    public ProductStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        ProductStats productStats = ProductStats.builder()
                                .sku_id(jsonObj.getLong("sku_id"))
                                .favor_ct(1L)
                                .ts(DateTimeUtil.toTs(jsonObj.getString("create_time")))
                                .build();
                        return productStats;
                    }
                }
        );


        // 转换加购流数据
        // 4.3 转换加购流数据
        SingleOutputStreamOperator<ProductStats> cartStatsDS = cartInfoStrDS.map(
                new MapFunction<String, ProductStats>() {
                    @Override
                    public ProductStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        ProductStats productStats = ProductStats.builder()
                                .sku_id(jsonObj.getLong("sku_id"))
                                .cart_ct(1L)
                                .ts(DateTimeUtil.toTs(jsonObj.getString("create_time")))
                                .build();
                        return productStats;
                    }
                }
        );

        // 转换退款流数据
        // 4.4 转换退款流数据
        SingleOutputStreamOperator<ProductStats> refundStatsDS = refundInfoStrDS.map(
                new MapFunction<String, ProductStats>() {
                    @Override
                    public ProductStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);

                        ProductStats productStats = ProductStats.builder()
                                .sku_id(jsonObj.getLong("sku_id"))
                                .refundOrderIdSet(
                                        new HashSet(Collections.singleton(jsonObj.getLong("order_id")))
                                )
                                .refund_amount(jsonObj.getBigDecimal("refund_amout"))
                                .ts(DateTimeUtil.toTs(jsonObj.getString("create_time")))
                                .build();
                        return productStats;
                    }
                }
        );

        // 需要统计总评论数、好评数。
        // 4.5 转换评价流数据
        SingleOutputStreamOperator<ProductStats> commentStatsDS = commentInfoStrDS.map(
                new MapFunction<String, ProductStats>() {
                    @Override
                    public ProductStats map(String jsonStr) throws Exception {
                        JSONObject jsonObj = JSON.parseObject(jsonStr);
                        long goodct = Constant.APPRAISE_GOOD.equals(jsonObj.getString("appraise")) ? 1L : 0L;
                        ProductStats productStats = ProductStats.builder()
                                .sku_id(jsonObj.getLong("sku_id"))
                                .ts(DateTimeUtil.toTs(jsonObj.getString("create_time")))
                                .comment_ct(1L)
                                .good_comment_ct(goodct)
                                .build();
                        return productStats;
                    }
                }
        );

        // 4.6 转换订单宽表流数据
        SingleOutputStreamOperator<ProductStats> orderWideStatsDS = orderWideStrDS.map(
                new MapFunction<String, ProductStats>() {
                    @Override
                    public ProductStats map(String jsonStr) throws Exception {
                        OrderWide orderWide = JSON.parseObject(jsonStr, OrderWide.class);
                        ProductStats productStats = ProductStats.builder()
                                .sku_id(orderWide.getSku_id())
                                .order_sku_num(orderWide.getSku_num())
                                .order_amount(orderWide.getSplit_total_amount())
                                .ts(DateTimeUtil.toTs(orderWide.getCreate_time()))
                                .orderIdSet(new HashSet(Collections.singleton(orderWide.getOrder_id())))
                                .build();
                        return productStats;
                    }
                }
        );

        // 4.7 转换支付宽表流数据
        SingleOutputStreamOperator<ProductStats> paymentWideStatsDS = paymentWideStrDS.map(
                new MapFunction<String, ProductStats>() {
                    @Override
                    public ProductStats map(String jsonStr) throws Exception {
                        PaymentWide paymentWide = JSON.parseObject(jsonStr, PaymentWide.class);
                        ProductStats productStats = ProductStats.builder()
                                .sku_id(paymentWide.getSku_id())
                                .payment_amount(paymentWide.getSplit_total_amount())
                                .paidOrderIdSet(new HashSet(Collections.singleton(paymentWide.getOrder_id())))
                                .ts(DateTimeUtil.toTs(paymentWide.getCallback_time()))
                                .build();
                        return productStats;
                    }
                }
        );

        // TODO 5 将不同流的数据通过union合并到一起
        DataStream<ProductStats> unionDS = clickAndDisplayStatsDS.union(
                favorStatsDS,
                cartStatsDS,
                refundStatsDS,
                commentStatsDS,
                orderWideStatsDS,
                paymentWideStatsDS
        );

        unionDS.print(">>>");

        // TODO 6 指定watermark以及提取时间时间字段
        SingleOutputStreamOperator<ProductStats> productStatsWithWatermarkDS = unionDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<ProductStats>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(
                                new SerializableTimestampAssigner<ProductStats>() {
                                    @Override
                                    public long extractTimestamp(ProductStats productStats, long recordTimestamp) {
                                        return productStats.getTs();
                                    }
                                }
                        )
        );

        // TODO 7 分组 -- 按照商品id分组
        KeyedStream<ProductStats, Long> keyedDS = productStatsWithWatermarkDS.keyBy(ProductStats::getSku_id);

        // TODO 8 开窗
        WindowedStream<ProductStats, Long, TimeWindow> windowDS = keyedDS.window(TumblingEventTimeWindows.of(Time.seconds(10)));

        // TODO 9 聚合计算
        SingleOutputStreamOperator<ProductStats> reduceDS = windowDS.reduce(
                new ReduceFunction<ProductStats>() {
                    @Override
                    public ProductStats reduce(ProductStats stats1, ProductStats stats2) throws Exception {
                        stats1.setDisplay_ct(stats1.getDisplay_ct() + stats2.getDisplay_ct());
                        stats1.setClick_ct(stats1.getClick_ct() + stats2.getClick_ct());
                        stats1.setCart_ct(stats1.getCart_ct() + stats2.getCart_ct());
                        stats1.setFavor_ct(stats1.getFavor_ct() + stats2.getFavor_ct());
                        stats1.setOrder_amount(stats1.getOrder_amount().add(stats2.getOrder_amount()));

                        stats1.getOrderIdSet().addAll(stats2.getOrderIdSet());
                        stats1.setOrder_ct(stats1.getOrderIdSet().size() + 0L);

                        stats1.setOrder_sku_num(stats1.getOrder_sku_num() + stats2.getOrder_sku_num());
                        stats1.setPayment_amount(stats1.getPayment_amount().add(stats2.getPayment_amount()));

                        stats1.getRefundOrderIdSet().addAll(stats2.getRefundOrderIdSet());
                        stats1.setRefund_order_ct(stats1.getRefundOrderIdSet().size() + 0L);
                        stats1.setRefund_amount(stats1.getRefund_amount().add(stats2.getRefund_amount()));

                        stats1.getPaidOrderIdSet().addAll(stats2.getPaidOrderIdSet());
                        stats1.setPaid_order_ct(stats1.getPaidOrderIdSet().size() + 0L);

                        stats1.setComment_ct(stats1.getComment_ct() + stats2.getComment_ct());
                        stats1.setGood_comment_ct(stats1.getGood_comment_ct() + stats2.getGood_comment_ct());

                        return stats1;
                    }
                },
                new ProcessWindowFunction<ProductStats, ProductStats, Long, TimeWindow>() {
                    @Override
                    public void process(Long aLong, Context context, Iterable<ProductStats> elements, Collector<ProductStats> out) throws Exception {
                        for (ProductStats productStats : elements) {
                            productStats.setStt(DateTimeUtil.toYMDHMS(new Date(context.window().getStart())));
                            productStats.setEdt(DateTimeUtil.toYMDHMS(new Date(context.window().getEnd())));
                            productStats.setTs(new Date().getTime());
                            out.collect(productStats);
                        }
                    }
                }
        );

        // 补充商品维度信息
        // 因为除了下单操作之外，其它操作，只获取到了商品的id，其它维度信息是没有的。
        SingleOutputStreamOperator<ProductStats> productStatsWithSkuDS = AsyncDataStream.unorderedWait(
                reduceDS,
                new DimAsyncFunction<ProductStats>("DIM_SKU_INFO") {

                    @Override
                    public void join(ProductStats productStats, JSONObject dimJsonObj) throws Exception {
                        productStats.setSku_name(dimJsonObj.getString("SKU_NAME"));
                        productStats.setSku_price(dimJsonObj.getBigDecimal("PRICE"));
                        productStats.setCategory3_id(dimJsonObj.getLong("CATEGORY3_ID"));
                        productStats.setSpu_id(dimJsonObj.getLong("SPU_ID"));
                        productStats.setTm_id(dimJsonObj.getLong("TM_ID"));
                    }

                    @Override
                    public String getKey(ProductStats productStats) {
                        return productStats.getSku_id().toString();
                    }
                },
                60, TimeUnit.SECONDS
        );

        // 关联SPU维度
        SingleOutputStreamOperator<ProductStats> productStatsWithSpuDS =
                AsyncDataStream.unorderedWait(productStatsWithSkuDS,
                        new DimAsyncFunction<ProductStats>("DIM_SPU_INFO") {
                            @Override
                            public void join(ProductStats productStats, JSONObject jsonObject) throws Exception {
                                productStats.setSpu_name(jsonObject.getString("SPU_NAME"));
                            }
                            @Override
                            public String getKey(ProductStats productStats) {
                                return String.valueOf(productStats.getSpu_id());
                            }
                        }, 60, TimeUnit.SECONDS
                );

        // 关联品类维度
        SingleOutputStreamOperator<ProductStats> productStatsWithCategory3DS =
                AsyncDataStream.unorderedWait(productStatsWithSpuDS,
                        new DimAsyncFunction<ProductStats>("DIM_BASE_CATEGORY3") {
                            @Override
                            public void join(ProductStats productStats, JSONObject jsonObject) throws Exception {
                                productStats.setCategory3_name(jsonObject.getString("NAME"));
                            }
                            @Override
                            public String getKey(ProductStats productStats) {
                                return String.valueOf(productStats.getCategory3_id());
                            }
                        }, 60, TimeUnit.SECONDS
                );

        // 关联品牌维度
        SingleOutputStreamOperator<ProductStats> productStatsWithTmDS =
                AsyncDataStream.unorderedWait(productStatsWithCategory3DS,
                        new DimAsyncFunction<ProductStats>("DIM_BASE_TRADEMARK") {
                            @Override
                            public void join(ProductStats productStats, JSONObject jsonObject) throws Exception {
                                productStats.setTm_name(jsonObject.getString("TM_NAME"));
                            }
                            @Override
                            public String getKey(ProductStats productStats) {
                                return String.valueOf(productStats.getTm_id());
                            }
                        }, 60, TimeUnit.SECONDS
                );

        productStatsWithTmDS.print(">>>");

        // TODO 11 将结果写入到ClickHouse
        productStatsWithTmDS.addSink(
                ClickhouseUtil.getJdbcSink(
                        "insert into product_stats_2022 values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
        );

        env.execute();
    }

    @Builder.Default
    @TransientSink
    Set refundOrderIdSet = new HashSet(); //用于退款支付订单数
}
