### 维度建模
主要根据业务过程和指标计算设计表结构。根据不同类型的业务和指标的计算表的设计会有些区别。
* 事实表
* 维度表
* 宽表
* 拉链表

### 模型维护
* 字段增加
* 字段类型
* 字段属性
* 缓慢维度变化
* 大表和小表

### 模型分层
* ODS
* DWD
* DWS/DWM， 主要以计算指标为目标


### 部署
---项目打包 、部署到服务器

1.修改realtime项目中的并行度（资源足够则不用修改），并打jar包
-BaseLogApp
-KeywordStatsApp


2.修改flink-conf.yml（注意冒号后面有一个“空格”）
taskmanager.memory.process.size: 2000m
taskmanager.numberOfTaskSlots: 8

3.启动zk、kf、clickhouse、flink本地集群(bin/start-cluster.sh)、logger.sh

4.启动BaseLog、KeywordStatsApp
-独立分窗口启动
bin/flink run -m hadoop101:8081 -c com.hzy.gmall.realtime.app.dwd.BaseLogApp ./gmall2022-realtime-1.O-SNAPSHOT-jar-with-dependencies.jar


bin/flink run -m hadoop101:8081 -c com.hzy.gmall.realtime.app.dws.KeywordStatsApp ./gmall2022-realtime-1.O-SNAPSHOT-jar-with-dependencies.jar

	
	-编写realtime.sh脚本
		echo "========BaseLogApp==============="
		/opt/module/flink-local/bin/flink run -m hadoop101:8081 -c com.hzy.gmall.realtime.app.dwd.BaseLogApp /opt/module/flink-local/gmall2022-realtime-1.O-SNAPSHOT-jar-with-dependencies.jar
>/dev/null 2>&1  &

		echo "========KeywordStatsApp==============="
		/opt/module/flink-local/bin/flink run -m hadoop101:8081 -c com.hzy.gmall.realtime.app.dws.KeywordStatsApp /opt/module/flink-local/gmall2022-realtime-1.O-SNAPSHOT-jar-with-dependencies.jar
>/dev/null 2>&1  &

5.打包publisher并上传运行

6.花生壳添加hadoop上的publisher地址映射
hadoop101:8070/api/sugar/keyword/
aliyun服务器直接访问公网地址即可

7.sugar修改空间映射

8.运行模拟生成日志的jar包，查看效果


9.常见问题排查
-启动flink集群,不能访问webUI（logger使用的8081，flink同样使用8081，造成冲突）
查看日志，端口冲突  lsof -i:8081

	-集群启动之后，应用不能启动
		bin/flink run -m hadoop101:8081 -c com.hzy.gmall.realtime.app.dwd.BaseLogApp ./gmall2022-realtime-1.O-SNAPSHOT-jar-with-dependencies.jar


		*phoenix驱动不识别，需要加Class.forName指定
		*找不到hadoop和hbase等相关的jar
			原因：NoClassDefoundError：这个错误编译期间不会报，运行期间才会包。原因是运行期间找不到这个类或无法加载，这个比较复杂。我的做法是把类所在jar包放在flink lib下重启集群就不会出现这个问题。

			解决：
				>在my.env环境变量中添加
					export HADOOP_CLASSPATH=`hadoop classpath`

				>在flink的lib目录下创建执行hbase的lib的软连接
					ln -s /opt/module/hbase/lib/ ./

		*和官方jar包冲突
			Caused by: java.lang.ClassCastException: org.codehaus.janino.CompilerFactory cannot be cast to org.codehaus.commons.compiler.ICompilerFactory
			将程序中flink、hadoop相关以及三个日志包的scope调整为provided，<scope>provided</scope>
			注意：不包含connector相关的
