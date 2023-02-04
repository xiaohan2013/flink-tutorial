package com.tutorial.async;

import org.apache.commons.lang3.SystemUtils;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Checkpoint的位置
        env.setStateBackend(new HashMapStateBackend());
        if (SystemUtils.IS_OS_WINDOWS) {
            env.getCheckpointConfig().setCheckpointStorage(new FileSystemCheckpointStorage("file:///D:\\data\\ckp"));
        } else {
            env.getCheckpointConfig().setCheckpointStorage(new FileSystemCheckpointStorage("hdfs://node1:8020/flink-checkpoint/checkpoint"));
        }

        FutureTask task = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });

        ExecutorService executors = Executors.newSingleThreadExecutor();
        try{
            executors.execute(task);
            task.get(30, TimeUnit.MINUTES);
        }finally {
            executors.shutdown();
        }

    }
}
