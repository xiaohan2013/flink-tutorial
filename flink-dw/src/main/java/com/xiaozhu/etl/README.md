# 启动zookeeper
# 启动kafka
# 启动采集服务
logger.sh start
# 启动nm以将检查点保存到hdfs上
start-dfs.sh
# 等待安全模式关闭，启动主程序，如果出现权限问题，可以将权限放开
hdfs dfs -chmod -R 777 /
# 或者增加以下代码到主程序中
System.setProperty("HADOOP_USER_NAME","hzy");
# 程序运行起来后，启动模拟生成日志数据jar包，在主程序中可以接收到数据


# 主要流程
···
基本环境准备
读取主流业务数据
使用CDC读取配置表中数据
将读取的配置数据流转换为广播流
主流和广播流进行连接
对连接之后的流进行处理（动态分流 – TableProcessFunction）
处理广播流的方法 – processBroadcastElement
获取状态
从广播流中获取配置信息，将配置信息放到状态中
处理主流的方法 – processElement
获取状态
从主流中获取表以及操作类型，根据表和操作类型封装key
通过key到状态中获取配置对象
根据配置对象的sinkType进行分流

### 
