package com.tutorial.state;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(3000);
        // 使用内嵌RocksDB
        env.setStateBackend(new EmbeddedRocksDBStateBackend());
        DataStreamSource data = env.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<Tuple2<Long, Long>> maped = data.map(new MapFunction<String, Tuple2<Long, Long>>() {
            @Override
            public Tuple2<Long, Long> map(String value) throws Exception {
                String[] split = value.split(",");
                return new Tuple2<Long, Long>(Long.valueOf(split[0]), Long.valueOf(split[1]));
            }
        });

        KeyedStream<Tuple2<Long,Long>, Long> keyed = maped.keyBy(value -> value.f0);

//        SingleOutputStreamOperator<Tuple2<Long, Long>> flatMaped = keyed.flatMap(new FlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>>() {
//            @Override
//            public void flatMap(Tuple2<Long, Long> value, Collector<Tuple2<Long, Long>> out) throws Exception {
//
//            }
//        });

//        MapStateDescriptor<Void, MyPattern> bcStateDescriptor = new MapStateDescriptor<> ("patterns", Types.VOID, Types.POJO(MyPattern.class));
//        BroadcastStream<MyPattern> broadcastPatterns = patterns.broadcast(bcStateDescriptor);

        SingleOutputStreamOperator<Tuple2<Long, Long>> flatMaped = keyed.flatMap(new RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>>() {
            ValueState<Tuple2<Long, Long>> sumState;
            @Override
            public void open(Configuration conf) throws Exception {
                ValueStateDescriptor<Tuple2<Long, Long>> descriptor = new ValueStateDescriptor<Tuple2<Long, Long>>(
                        "avg",
                        TypeInformation.of(new TypeHint<Tuple2<Long, Long>>(){})
                );

                sumState = getRuntimeContext().getState(descriptor);
                super.open(conf);
            }
            @Override
            public void flatMap(Tuple2<Long, Long> value, Collector<Tuple2<Long, Long>> out) throws Exception {
                Tuple2<Long, Long> currentSum = sumState.value();

                currentSum.f0 += 1;
                currentSum.f1 += value.f1;
                sumState.update(currentSum);

                if(currentSum.f0 == 2) {
                    long avg = currentSum.f1 / currentSum.f0;
                    out.collect(new Tuple2<>(value.f0, avg));
                    sumState.clear();
                }
            }
        });

        flatMaped.print();
        env.execute();

    }
}
