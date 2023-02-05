package com.xiaozhu.etl.app;

import com.xiaozhu.etl.utils.KafkaUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DWSApp extends FlinkBase{
    public static void main(String[] args) throws Exception {
        FlinkBase base = new DWSApp();
        base.entry();
    }

    @Override
    public void executeBiz(StreamExecutionEnvironment env) {
        env.addSource(KafkaUtils.getKafkaSource("",""));
    }
}
