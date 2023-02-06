-- 需求
-- 已知一个表STG.ORDER，有如下字段:Date，Order_id，User_id，amount。
-- 数据样例:2017-01-01,10029028,1000003251,33.57。
--
-- 请给出sql进行统计:
-- (1) 给出 2017年每个月的订单数、用户数、总成交金额。
-- (2)给出2017年11月的新客数(指在11月才有第一笔订单)

-- (2) 先按用户进行分组，然后求最早的订单日期
select user_id, min(Date) over(partition by user_id order by Date) as first_date from t where first_date = 11