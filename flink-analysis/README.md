SQL功力提升
---
1. 造数据。
```sql
create table xxxx insert into
create temporary view xxx as values(SPARKSQL支持)
CREATE TABLE test_sql.test2 (
user_id string, shop string )
ROW format delimited FIELDS TERMINATED BY '\t';
INSERT INTO TABLE test_sql.test2 VALUES ( 'u1', 'a' ),
```
2. 先画结果，包括结果字段有哪些，数据量也画几条。这是分析要什么。从源表到结果表，一路可能要多个步骤，其实就是多几个子查询，过程多就用with as重构提高可读性
3. 由简单到复杂。先写简单select * from table，每个步骤都打印结果，看是否符合预期，根据中间结果，进一步调整SQL，在执行调整
4. 数据量要小，工具快，如果用Hive。这是set hive.exec.mode.local.auto=true, 如果是SPARKSQL，就设置合适Shuffle并行度set spark.sql.shuffle.partitions=4