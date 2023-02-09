# 实时数仓
### 解决的问题和主要需求
- 时间要求
- 业务指标需求
- 秒级和分钟级，低延迟
- 稳定性，


### 技术
- 建模技术主要以维度建模
- ETL计算以Flink SQL为主
- 指标存储由Doris, ClickHouse, MySQL
- 维度存储由HBase，Redis, 
- Checkpoint存储由HDFS
- 缓存由Redis
- 数据流由Kafka实现
- 调度由Dolphine, Azakaban

### 业务场景
- BI报表
- 运营
- 广告
- 数据产品
- 数据服务API
- 可视化


### 常见问题
