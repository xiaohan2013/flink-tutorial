package org.xiaozhu;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class K8SMain {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.addSource(new SourceFunction<String>() {
            private volatile boolean isRunning = true;
            @Override
            public void run(SourceContext<String> ctx) throws Exception {
                while (isRunning) {
                    ctx.collect(UUID.randomUUID().toString());
                    TimeUnit.SECONDS.sleep(1);
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        });

        env.execute();
    }
}
