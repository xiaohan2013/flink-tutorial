package com.tutorial.kafka;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(1000);
        // 周期性地生成水位线
        // 实际中用的多，结合时间或累计条数两个维度
        env.getConfig().setAutoWatermarkInterval(200);

        KafkaSource<AccessLog> kafka_source = KafkaSource.<AccessLog>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("")
                .setGroupId("test")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new CustomSerialSchema())
                .build();

        env.fromSource(kafka_source,
                WatermarkStrategy.<AccessLog>noWatermarks().withIdleness(Duration.ofMinutes(5)),
                "KafkaSource")
                .assignTimestampsAndWatermarks(new WatermarkStrategy() {
                    @Override
                    public TimestampAssigner<AccessLog> createTimestampAssigner(
                            TimestampAssignerSupplier.Context context) {
                        return new TimestampAssigner<AccessLog>() {
                            @Override
                            public long extractTimestamp(AccessLog element, long recordTimestamp) {
                                // 时间格式需要转换，基于
                                return Long.parseLong(element.getTime());
                            }
                        };
                    }

                    @Override
                    public WatermarkGenerator createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
                        return new WatermarkGenerator<AccessLog>() {
                            @Override
                            public void onEvent(AccessLog event, long eventTimestamp, WatermarkOutput output) {
                                // 基于事件
//                                output.emitWatermark(new Watermark());
                            }

                            @Override
                            public void onPeriodicEmit(WatermarkOutput output) {
                                // 在数据源定期生成Watermark
                            }
                        };
                    }
                });
    }
}
