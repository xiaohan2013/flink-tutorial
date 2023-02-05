package com.xiaozhu.etl.app;

import com.ververica.cdc.debezium.StringDebeziumDeserializationSchema;
import com.xiaozhu.etl.common.MySQLSource;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Properties;

/**
 * 通过FlinkCDC动态读取MySQL表中的数据 -- DataStreamAPI
 */
public class FlinkCDC01_DS {
    public static void main(String[] args) throws Exception {
        //TODO 1 准备流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //TODO 2 开启检查点   Flink-CDC将读取binlog的位置信息以状态的方式保存在CK,如果想要做到断点续传,
        // 需要从Checkpoint或者Savepoint启动程序
        // 开启Checkpoint,每隔5秒钟做一次CK,并指定CK的一致性语义
        // 指定操作HDFS用户
        // 设置状态后端 基于内存还是文件系统还是RocksDB
        // 设置Job取消后，检查点是否保存
        // 设置重启策略
        env.enableCheckpointing(5000L, CheckpointingMode.EXACTLY_ONCE);
        // 设置超时时间为1分钟
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        // 指定从CK自动重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2,2000L));
        // 设置任务关闭的时候保留最后一次CK数据
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 设置状态后端
        env.setStateBackend(new FsStateBackend("hdfs://hadoop101:8020/ck/flinkCDC"));
        // 设置访问HDFS的用户名
        System.setProperty("HADOOP_USER_NAME", "hzy");

        //TODO 3 创建Flink-MySQL-CDC的Source
        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");
        SourceFunction<String> sourceFunction = MySQLSource.<String>builder()
                .hostname("hadoop101")
                .port(3306)
                .username("root")
                .password("123456")
                // 可配置多个库
                .databaseList("gmall2022_realtime")
                ///可选配置项,如果不指定该参数,则会读取上一个配置中指定的数据库下的所有表的数据
                //注意：指定的时候需要使用"db.table"的方式
                .tableList("gmall2022_realtime.t_user")
                .debeziumProperties(props)
                .deserializer(new StringDebeziumDeserializationSchema())
                .build();

        //TODO 4 使用CDC Source从MySQL读取数据
        DataStreamSource<String> mysqlDS = env.addSource(sourceFunction);

        //TODO 5 打印输出
        mysqlDS.print();

        //TODO 6 执行任务
        env.execute();
    }
}

