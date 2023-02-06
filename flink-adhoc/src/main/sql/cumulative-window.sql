-- 需求描述
-- 求每天，从0:00开始计算。统计当天的销售额度。需要将滚动窗口内的数据进行累加
-- CUMULATE 参数
-- 表名
-- 时间字段
-- 累计步长
-- 累计时间：如果写1 day，那么就是从今天的0：00到今晚24：00 进行统计，明天开始，又会从新的0：00开始统计。如果写2，或者更多，意味着从前俩天开始一直统计。在0：00的时候，会继续进行累加。

SELECT
    cast(PROCTIME() as timestamp_ltz) as window_end_time,
    manufacturer_name,
    event_id,
    case when state is null then -1 else state end ,
    cast(sum(agg)as string ) as agg
FROM TABLE(CUMULATE(
       TABLE dm_cumulate
       , DESCRIPTOR(ts1)
       , INTERVAL '5' MINUTES
       , INTERVAL '1' DAY(9)))
GROUP BY
          window_end
         ,window_start
         ,manufacturer_name
         ,event_id
         ,case when state is null then -1 else state end


