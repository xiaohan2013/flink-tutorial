-- 连续登录天数和最大登录天数
-- 活跃表
/*
 CREATE TABLE `user_active` {
    `uid` string,
    `active_time` string
 }
 ROW FORMAT delimited
 fields terminated by '\t'

*/
-- 连续登录天数,在统计周期内最长的连续登陆天数，连续登陆时间越长，说明用户的粘性越高
-- （1）用户组内排序 使用ROW_NUMBER窗口函数，按uid分组，按day升序排列
-- （2）判断是否连续 升序排序后：用day - sort如果连续则做差后值一样，然后再用uid，day分组算出每个uid下连续的天数。如果连续那么登录和排序序号同增长
-- （3）以uid分组，取最大连续登陆天数
select uid, max(continuous_days) as continuous_days
from
(
select
    uid,
    date_sub(day, sort) as active_group,
    min(day) as start_date,
    max(day) as end_date,
    count(1) as continuous_days
    from (
        -- 首先根据用户分组，活跃时间排序，结果按照活跃时间升序排列
        select
            uid,
            day,
            row_number() OVER(PARTITION BY uid order by day) as sort
        from user_active
    ) a
    group by uid, date_sub(day, sort)
) b
group by uid
;

# 查询结果
# "uid"  "continuous_days"
"0ab14054cc500e62cf1ecb09e7930d07"  "6"
"15b3048ca106e794a51986718827c87c"  "16"
"5b86f65517bebc97a1393a9abac42bf5"  "4"
"9b5f08191302b9b808e643a75badbb82"  "2"

-- 方法一
-- 第一步:用户登录日期去重
select distinct date(date) as 日期,id from tb_user;
-- 第二步:用row_number()计数
select *,row_number() over(PARTITION by id order by 日期) as cum from (select DISTINCT date(date) as 日期,id from tb_user)a;
-- 第三步:日期减去计数值得到结果
select *,date(日期)-cum as 结果 from (select *,row_number() over(PARTITION by id order by 日期) as cum from (select DISTINCT date(date) as 日期,id from tb_user)a)b;
-- 第四步:根据id和结果分组并计算总和,大于等于7的即为连续登录7天的用户
select id,count(*) from (select *,date(日期)-cum as 结果 from (select *,row_number() over(PARTITION by id order by 日期) as cum from (select DISTINCT date(date) as 日期,id from tb_user)a)b)c GROUP BY id,结果 having count(*)>=7;
select * from tb_user;

-- 方法二
select id,count(date2) as 连续天数
from (select *,date_sub(date1,interval r day) date2
			from(select distinct id,date(date) date1,
					 dense_rank()over(partition by id ORDER BY date(date)) as r
					 from tb_user) v ) w
group by id,date2
having count(date2) >= 7;

-- 方法三
select id,max(h) '登录天数'from(
select id ,count(e) h from (
select *,a-b as e from (
select *,row_number() over(order by id) b from(
select *,date_format(date,'%Y%m%d') a from (
select distinct id,date(date) date from tb_user order by id,date(date)) as c
 )as d) as f)as g
group by e,id)as i group by id having max(h) >= 7;

-- 方法五
select id,max(sort1) as 最多登录天数
from(
select *,
dense_rank()over(partition by id,datesub order by id,date) as sort1
from
(select *,
date_sub(date,interval sort day) as datesub
from
(select id,date(date) date,
dense_rank()over(partition by id order by id,date(date)) as sort
from tb_user) a  # 按id,date 不跳越排序
) b   #计算 date - sort  日期差
) c group by id having max(sort1) >= 7;   #再次排序 按id和 日期差

-- 方法六
SELECT id,count(date-t2) `连续登录天数`
from(SELECT DISTINCT date(date) date,id,row_number() over(PARTITION by id)t2
			from tb_user GROUP BY id,date(date)) a
group by id,date-t2 having count(date-t2) >= 7;

-- 方法七
-- 排序+去重
select distinct id,date(date) date from tb_user order by id,date(date);
-- 给上表中添加row_number num 和 日期-num n
select id,date,row_number()over(partition by id) num,day(date) - row_number()over(partition by id) n from (select distinct id,date(date) date from tb_user order by id,date(date)) a;
-- 相同id且相同n的记录数超过7即可
select id,count(n) from (select id,date,row_number()over(partition by id) num,day(date)-row_number()over(partition by id) n from (select distinct id,date(date) date from tb_user order by id,date(date)) a) a group by id,n having count(n) >= 7;

-- 方法八
select distinct a.id
from (select distinct id,day(date) day1,dense_rank()over(partition by id order by day(date)) dayno from tb_user) a
join (select distinct id,day(date) day1,dense_rank()over(partition by id order by day(date)) dayno from tb_user) b
on a.id = b.id
where a.day1 + 6 = b.day1 and a.dayno + 6 = b.dayno
group by a.id,a.day1;