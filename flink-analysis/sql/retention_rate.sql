-- 留存率：次日、3日、7日
-- 业务含义
-- 第N日活跃用户留存率：以基准日的活跃用户数为主，第N日依然活跃的用户占基准活跃用户的比例
-- 第1日留存率（次日留存），（以基准日当天活跃的用户中，基准日之后的第1天还活跃的用户数）/基准日当天总活跃用户数；
-- 第3日留存率：（以基准日当天活跃的用户中，基准日之后的第3天还活跃的用户数）/基准日当天总活跃用户数；
-- 第7日留存率：（以基准日当天活跃的用户中，基准日之后的第7天还活跃的用户数）/基准日当天总活跃用户数；
-- 第30日留存率：（以基准日当天活跃的用户中，基准日之后的第30天还活跃的用户数）/基准日当天总活跃用户数；
-- 基准日：
-- 活跃：将用户登录APP定义为活跃，也可以将用户使用某功能定义为活跃

-- 通用解法
-- 1. 表自关联，刷选出右表大于左表的数据
-- 2. 计算每日活跃用户数
-- 3. 计算留存用户数
-- 4. 计算用户留存率

-- 3.留存用户数计算
CREATE VIEW user_remain_view AS SELECT
a.dates,
count( DISTINCT a.user_id ) AS user_count,
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 1, a.user_id, NULL ) ) ) AS remain1,
-- 1日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 2, a.user_id, NULL ) ) ) AS remain2,
-- 2日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 3, a.user_id, NULL ) ) ) AS remain3,
-- 3日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 4, a.user_id, NULL ) ) ) AS remain4,
-- 4日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 5, a.user_id, NULL ) ) ) AS remain5,
-- 5日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 6, a.user_id, NULL ) ) ) AS remain6,
-- 6日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 7, a.user_id, NULL ) ) ) AS remain7,
-- 7日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 15, a.user_id, NULL ) ) ) AS remain15,
-- 15日留存数
count( DISTINCT ( IF ( DATEDIFF( b.dates, a.dates ) = 30, a.user_id, NULL ) ) ) AS remain30
-- 30日留存数

FROM
	( SELECT user_id, dates FROM temp_trade GROUP BY user_id, dates ) a
	LEFT JOIN ( SELECT user_id, dates FROM temp_trade GROUP BY user_id, dates ) b ON a.user_id = b.user_id
WHERE
	b.dates >= a.dates
GROUP BY
	a.dates;


SELECT
	dates,
	user_count,
	concat( round( remain1 / user_count * 100, 2 ), '%' ) AS day1,
-- 1日留存率
	concat( round( remain2 / user_count * 100, 2 ), '%' ) AS day2,
-- 2日留存率
	concat( round( remain3 / user_count * 100, 2 ), '%' ) AS day3,
-- 3日留存率
	concat( round( remain4 / user_count * 100, 2 ), '%' ) AS day4,
-- 4日留存率
	concat( round( remain5 / user_count * 100, 2 ), '%' ) AS day5,
-- 5日留存率
	concat( round( remain6 / user_count * 100, 2 ), '%' ) AS day6,
-- 6日留存率
	concat( round( remain7 / user_count * 100, 2 ), '%' ) AS day7,
-- 7日留存率
	concat( round( remain15 / user_count * 100, 2 ), '%' ) AS day15,
-- 15日留存率
	concat( round( remain30 / user_count * 100, 2 ), '%' ) AS day30
-- 30日留存率
-- concat(cast(remain1/user_count*100 as DECIMAL(10,2)),'%') -- 使用另一个函数cast()
FROM
	user_remain_view;

