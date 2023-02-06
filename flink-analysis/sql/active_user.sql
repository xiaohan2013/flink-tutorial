-- 活跃用户
-- 活跃：连续两天都有访问记录的用户
-- HIVE(SPARKSQL)
SELECT * FROM test JOIN (SELECT min(dt), usr, age FROM test GROUP BY usr)


-- SPARKSQL
-- FLINKSQL


