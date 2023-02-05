动态分流测试执行流程

需要启动的进程：zookeeper，kafka，maxwell，hdfs，hbase，BaseDBApp

当业务数据发生变化，maxwell会采集变化数据到ods层

BaseDBApp从ods层读取到变化数据，作为业务主流

BaseDBApp在启动时，会通过FlinkCDC读取配置表，作为广播流

业务流和广播流通过connect进行连接

对连接之后的数据通过process进行处理

processElement
processBroadcastElement
具体执行流程见一 2（3）总结

将维度侧输出流的数据写到Hbase中 – DimSink

拼接upsert
执行sql（手动提交事务）
将主流数据写回到kafka的dwd层

重写获取FlinkKafkaProducer的方法，自定义序列化的过程
将主流的数据写到kafka不同的主题中，并且保存精准一次性


## 执行流程
业务数据生成**->Maxwell同步->Kafka的ods_base_db_m主题->BaseDBApp分流写回kafka->dwd_order_info和dwd_order_detail->**OrderWideApp从kafka的dwd层读数据，打印输出

