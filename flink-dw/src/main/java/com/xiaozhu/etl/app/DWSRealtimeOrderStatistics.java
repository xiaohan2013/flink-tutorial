package com.xiaozhu.etl.app;

import com.xiaozhu.etl.bean.Order;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.flink.table.api.Expressions.$;

/**
 * 每隔5秒统计最近5秒的每个用户的订单总数、订单的最大金额、订单的最小金额
 */
public class DWSRealtimeOrderStatistics {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        DataStreamSource<Order> source = env.addSource(new SourceFunction<Order>() {
            private Boolean isRunning = true;
            @Override
            public void run(SourceContext<Order> ctx) throws Exception {
                while (isRunning) {
                    Random random = new Random();
                    Order order = new Order(
                            UUID.randomUUID().toString(),
                            random.nextInt(3),
                            random.nextInt(100),
                            System.currentTimeMillis()
                    );
                    TimeUnit.SECONDS.sleep(1);
                    ctx.collect(order);
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        });

        SingleOutputStreamOperator<Order> watermarks = source.assignTimestampsAndWatermarks(
                WatermarkStrategy.<Order>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner((event, timestamp) -> event.getCreateTime())
        );

        String sql = "select " +
                "userId," +
                "count(money) as totalCount," +
                "max(money) as maxMoney," +
                "min(money) as minMoney " +
                "from t_order " +
                "group by userId," +
                "tumble(createTime, interval '5' second)";

        tableEnv.createTemporaryView("t_order", watermarks, $("id"), $("userId"), $("money"), $("createTime").rowtime());

        Table result = tableEnv.sqlQuery(sql);

        // 将表转换为回撤流：add and retract messages.
        DataStream<Tuple2<Boolean, Row>> orderResult = tableEnv.toRetractStream(result, Row.class);

        orderResult.print();
        env.execute();
    }
}
