-- Flink中的表
CREATE TABLE dim.ad (
    ID STRING,
) WITH (
    'connector' = 'hbase',
    'table-name'
);

INSERT INTO dim.ad
SELECT * FROM


