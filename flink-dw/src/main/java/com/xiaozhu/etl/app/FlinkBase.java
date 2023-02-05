package com.xiaozhu.etl.app;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public abstract class FlinkBase {
    public void entry() throws Exception {
        //TODO 1 基本环境准备
        //流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度
        env.setParallelism(4);

        //TODO 2 检查点设置
        //开启检查点
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        // 设置检查点超时时间
        env.getCheckpointConfig().setCheckpointTimeout(60000L);
        // 设置重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000L));
        // 设置job取消后，检查点是否保留
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 设置状态后端 -- 基于内存 or 文件系统 or RocksDB
        env.setStateBackend(new FsStateBackend("hdfs://hadoop101:8020/ck/gmall"));
        // 指定操作HDFS的用户
        System.setProperty("HADOOP_USER_NAME","hzy");

        executeBiz(env);

        env.execute();
    }

    public abstract void executeBiz(StreamExecutionEnvironment env);
}

