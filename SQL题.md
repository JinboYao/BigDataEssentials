## SQL题

### 一张device表

| device_id | country uid | extend                          | date_time |
| --------- | ----------- | ------------------------------- | --------- |
|           |             | {spu_code click pay_id time pv} |           |

1、当前spu在某些国家下的曝光情况 汇总
2、14天内某些spu点击 曝光 购买趋势  top10  汇总
3、当前u_ID 是否活跃用户（连续7天有曝光行为）

```sql
with device2 as(
select  
device_id
,country
,uid
,date_time
,get_json(extend,spu_code) as spu_code
,get_json(extend,click) as click
,get_json(extend,pay_id) as pay_id
,get_json(extend,time) as time
,get_json(extend,pv) as  pv
from  device
)
1.
select  country,spu_code,sum(pv)
from  device2 
group by country,spu_code

2.
select spu_code
from(
select spu_code,
        rank() over(partition by spu_code order by sum_click) as click_top,
        rank() over(partition by spu_code order by sum_pv) as pv_top,
        rank() over(partition by spu_code order by sum_paid) as paid_top,
from(
select spu_code,sum(click),sum(pv),sum(paid_id)
from  device2 
where date_time>=date_sub(date_time,14)
group by spu_code
)t
)t2
where click_top>=10 or pv_top>=10 or paid_top>=10

3.
select u_id,if(count(time_num)>=7,1,0) as if_create
from(
select uid,date_sub(date_time,rn) as time_num
from(
    select  date_time,uid,row_number() over(partition by u_id order by date_time) as rn
    from device2
)t
)t2
```

### sql算法

**分析聚类**

已知一张表table_a记录了用户浏览信息,包含字段user_id(用户ID)和page_id(页面ID)以及对应的访问时间，例如
user_id     page_id     view_time
0001             A        2025-01-02 10:01:02
0001             B          2025-01-02 11:02:03
0002             A        2025-01-01 10:02:03
0002             A        2025-01-01 10:02:05
0002             C        2025-01-01 10:03:03
0002             D        2025-01-01 10:07:03

求出当天连续访问A->B->C页面的用户信息

```
lead（）获取下一行数据
```

##### 连续N天登录的用户

```sql
select 
 id
 ,result_date
 ,count(result_date) as num
from(
select 
 id
 ,date_sub(date,interval cum day) as result_date
from(
 select 
     id
     ,date(date) as date
     ,row_number() OVER (PARTITION BY id ORDER BY date(date)) as cum 
  from test.demo
  group by id,date(date)  
)t
)t1
group by id
 ,result_date
having count(result_date)>=3
```

##### 最长的连续登录N天数-可间断

```sql
SELECT user_id,
       MAX(log_days) AS max_log_days
FROM (
    SELECT user_id,
           GROUP_ID,
           DATEDIFF(MAX(login_date), MIN(login_date)) + 1 AS log_days
    FROM (
        SELECT user_id,
               login_date,
               -- 创建一个组ID，如果日期差大于2则开始一个新组
               SUM(CASE WHEN date_diff > 2 THEN 1 ELSE 0 END) OVER (PARTITION BY user_id ORDER BY login_date) AS group_id
        FROM (
            SELECT user_id,
                   login_date,
                   -- 计算当前登录日期与上一登录日期的差
                   DATEDIFF(login_date, LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date)) AS date_diff
            FROM (
                SELECT user_id,
                       TO_DATE(login_datetime) AS login_date
                FROM t_login_events
                GROUP BY user_id, TO_DATE(login_datetime)
            ) AS dates
        ) AS date_diffs
    ) AS grouped_dates
    GROUP BY user_id, group_id
) AS max_days
GROUP BY user_id;
```

##### 行转列

```sql
SELECT uid,  
   sum(if(course='语文', score, NULL)) as `语文`,  
   sum(if(course='数学', score, NULL)) as `数学`, 
   sum(if(course='英语', score, NULL)) as `英语`,  
   sum(if(course='物理', score, NULL)) as `物理`,  
   sum(if(course='化学', score, NULL)) as `化学`  
FROM scoreLong  
GROUP BY uid 
```

##### 列转行

```sql
select uid,语文 as course，score from 表名
WHERE `语文` IS NOT NULL  
union
select uid,数学 as course，score from 表名
WHERE `数学` IS NOT NULL  
```

##### 七日留存

```sql
select
a.date as date
, sum((case when datediff(b.date,a.date)= 1 then 1 else 0))/count(a.uid) as '次日留存率'
, sum((case when datediff(b.date,a.date)= 3 then 1 else 0))/count(a.uid) as '三日留存率'
, sum((case when datediff(b.date,a.date)= 7 then 1 else 0))/count(a.uid) as '七日留存率'
from
   （
   select uid
      ,substr(datetime,0,10) as date
   from 
       table
   group by uid ,substr(datetime,0,10)
）a
left join 
   (
   select uid
      ,substr(datetime,0,10) as date
   from 
       table
   group by uid ,substr(datetime,0,10)
)b on a.uid=b.uid and a.date < b.date ---加上了第二个条件可以将join后的表记录数减少一半
group by a.date
```

##### 相互关注

```sql
select 
    t1.to_user,
    t1.from_user,
    if(t2.from_user is not null,1,0) as is_friend
from fans t1
left join fans2 t2
on t1.to_user=t2.from_user 
and t1.from_user=t2.to_uwer
```

##### 天/月gmv

要求使用SQL统计出每个用户的累积访问次数

```sql
select
   count,
   sum(count) over(parition by user_id order by day) as sum_count
from table
```

## collect按照顺序拼接

```sql
select regexp_replace(
    concat_ws(',',sort_array(collect_list(concat_ws(':',lpad(id,5,0),val)))),
    '\\d+\:','')
from table
```

## 行为路径分析--无法开窗解决

```sql
with tmp as
         (select user_id,
                 dt,
                 regexp_replace(concat_ws(',', sort_array(collect_list(op_str))),
                                '(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\|)', '') as op_sort
          from (select user_id,
                       op_id,
                       to_date(op_time)               as dt,
                       op_time,
                       concat_ws('|', op_time, op_id) as op_str
                from t10_act_log) t
          group by user_id, dt)
select user_id,
       dt,
       op_sort
from tmp
where op_sort like '%A%B%D%'
  and op_sort not like '%A%B%C%D%'
```

## 连续N天登录

```sql
with tmp as(
    select id,login_data 
    row_number() over(partition by id order by login_date) as rn
    from table
)

select id 
from(
    select id,login_date,DATE_SUB(login_date,rn) as flag from tmp
)T
group by id,flag
having count(1)>=N
```

## 连续签到领金币（找出group）

示例：可以领多少金币

- 用户签到获得1金币；

- 如果用户连续签到3天则第三天获得2金币，如果用户连续签到7天则第7天获得5金币；

- 连续签到7天后连续天数重置，每月签到天数重置；

  +----------+--------------+----------+
  | user_id  | signin_date  | is_sign  |
  +----------+--------------+----------+
  | 001      | 2024-01-01   | 1        |
  | 001      | 2024-01-02   | 1        |
  | 001      | 2024-01-03   | 1        |
  | 001      | 2024-01-04   | 0        |
  | 001      | 2024-01-05   | 1        |
  ———————————————

```sql
select *,
	case when cnt=7 then 1
		 when cnt=3 then 2
	else 1 end as coiun_num
from(
    select user_id,sign_group,month,
        mod(count(sign_group) over (partition by user_id,sign_group,month order by signin_date asc),7) as cnt
    from(
        select user_id,
            signin_date,
            substr(signin_date,0,7) as month
            sum(if(is_sign=1,0,1)) over(partition by user_id,substr(signin_date,0,7) order by signin_date ASC) as sign_group 
        from table
    )t
)t1
```

## 连续5天以上未登录的用户及最近一次登录时间(lag函数)

+---------------------+------------------------+--+
| user_id  | login_date  |
+---------------------+------------------------+--+
| 1                   | 2022-01-01             |
| 1                   | 2022-01-02             |
| 1                   | 2022-01-03             |
| 1                   | 2022-01-05             |

```sql
select *,
	date_diff(login_date,last_time) as cnt
from(
    select user_id,
        login_date,
        lag(login_date) over(partition by user_id order by login_date asc) as last_time
    from table 
) t
where date_diff(login_date,last_time)>=5
```



## 占据好友封面个数

有两个表，朋友关系表user_friend，用户步数表user_steps。朋友关系表包含两个字段，用户id，用户好友的id；用户步数表包含两个字段，用户id，用户的步数

查询：占据多少个好友的封面（在好友的列表中排行第一，且必须超过好友的步数）

朋友关系表user_friend

+----------+------------+
| user_id  | friend_id  |
+----------+------------+
| 1        | 2          |
| 1        | 3          |
| 2        | 1          |
| 2        | 3          |
| 2        | 4          |
| 2        | 5          |
| 3        | 1          |
| 3        | 4          |
| 3        | 5          |
| 4        | 2          |
| 4        | 3          |
| 4        | 5          |
| 5        | 2          |
| 5        | 3          |
| 5        | 4          |

用户步数表user_steps

+---------------------+-------------------+
| user_steps.user_id  | user_steps.steps  |
+---------------------+-------------------+
| 1                   | 100               |
| 2                   | 95                |
| 3                   | 90                |
| 4                   | 80                |
| 5                   | 10                |

```sql
select
    ttt1.user_id,
    count(user_step) as fm_cnt
from
    user_friend ttt1
left join(
    select
        user_id,
        friend_id,
        user_step,        
        friend_step
        row_number() over(partition by friend_id order by user_step desc) as rn
    from(
        select 
            user_id,
            friend_id,
            user_step,
            friend_step
        from(
            select 
                user_id,
                friend_id,
                b.setp as user_step,
                c.step as friend_step
            from user_friend as a
            left join user_steps as b on a.user_id=b.user_id
            left join user_steps as c on a.friend_id=c.user_id
        )t where user_step>friend_step
    ) tt
    where rn=1
)ttt on ttt1.user_id=ttt.user_id and ttt1.friend_id=ttt.friend_id
group by ttt1.user_id;
```



## 已购买，已收藏，未购买人数（case when）

购买记录表t_order,包含自增id:id,用户ID:user_id，商品ID:goods_id,订单时间：order_time,商品类别：goods_type;

+-----+----------+-----------+-------------------+-------------+
| id  | user_id  | goods_id  |    order_time     | goods_type  |
+-----+----------+-----------+-------------------+-------------+
| 1   | 1        | 201       | 2020/11/14 10:00  | 1           |
| 2   | 2        | 203       | 2020/11/15 12:00  | 2           |
| 3   | 3        | 203       | 2020/11/16 10:00  | 1           |
| 4   | 4        | 203       | 2020/11/17 10:00  | 1           |
| 5   | 5        | 203       | 2020/11/18 10:00  | 1           |
| 6   | 6        | 203       | 2020/11/18 11:00  | 1           |
| 7   | 7        | 204       | 2020/11/18 12:00  | 1           |
| 8   | 8        | 205       | 2020/11/18 11:30  | 1           |
| 9   | 9        | 206       | 2020/12/1 10:00   | 1           |
| 10  | 4        | 207       | 2020/12/2 10:00   | 3           |
| 11  | 5        | 208       | 2020/12/3 10:00   | 1           |
| 12  | 6        | 209       | 2020/12/4 8:00    | 2           |
| 13  | 7        | 203       | 2020/12/5 10:00   | 2           |
| 14  | 8        | 203       | 2020/12/6 10:00   | 3           |
| 15  | 9        | 203       | 2020/12/7 15:00   | 4           |
| 16  | 1        | 204       | 2020/12/8 10:00   | 5           |
| 17  | 2        | 204       | 2020/12/9 10:00   | 5           |
| 18  | 3        | 206       | 2020/12/10 10:00  | 5           |
| 19  | 4        | 208       | 2020/12/11 10:00  | 5           |
| 20  | 5        | 209       | 2020/12/12 19:00  | 5           |
+-----+----------+-----------+-------------------+-------------+
用户收藏记录表t_collect_log,包含自增id，用户ID: user_id，商品ID：goods_id，收藏时间 collect_time

+-----+----------+-----------+-------------------+
| id  | user_id  | goods_id  |   collect_time    |
+-----+----------+-----------+-------------------+
| 1   | 1        | 203       | 2020/11/14 12:00  |
| 2   | 9        | 203       | 2020/11/15 10:00  |
| 3   | 4        | 203       | 2020/11/16 10:00  |
| 4   | 5        | 203       | 2020/11/17 10:00  |
| 5   | 6        | 203       | 2020/11/17 11:00  |
| 6   | 7        | 204       | 2020/11/17 12:00  |
| 7   | 8        | 205       | 2020/11/18 11:30  |
| 8   | 9        | 212       | 2020/12/1 10:00   |
| 9   | 4        | 207       | 2020/12/2 10:00   |
| 10  | 5        | 213       | 2020/12/3 10:00   |
| 11  | 6        | 209       | 2020/12/4 8:00    |
| 12  | 7        | 203       | 2020/12/5 10:00   |
| 13  | 8        | 203       | 2020/12/6 10:00   |
| 14  | 9        | 203       | 2020/12/7 15:00   |
| 15  | 1        | 203       | 2020/12/8 10:00   |
| 16  | 2        | 204       | 2020/12/9 10:00   |
| 17  | 3        | 205       | 2020/12/10 8:00   |
| 18  | 4        | 208       | 2020/12/11 10:00  |
| 19  | 5        | 209       | 2020/12/10 19:00  |
| 20  | 7        | 201       | 2020/12/11 19:00  |
+-----+----------+-----------+-------------------+
请用一句 sql 语句得出以下查询结果，得到所有用户的商品行为特征，其中用户行为分类为4种：是否已购买、购买未收藏、收藏未购买、收藏且购买。

样例结果

+----------+-----------+---------+------------------+------------------+------------------+
| user_id  | goods_id  | is_buy  | buy_not_collect  | collect_not_buy  | buy_and_collect  |
+----------+-----------+---------+------------------+------------------+------------------+
| 1        | 201       | 1       | 1                | 0                | 0                |
| 1        | 203       | 0       | 0                | 1                | 0                |
| 1        | 204       | 1       | 1                | 0                | 0                |
| 2        | 203       | 1       | 1                | 0                | 0                |
| 2        | 204       | 1       | 0                | 0                | 1                |
| 3        | 203       | 1       | 1                | 0                | 0                |
| 3        | 205       | 0       | 0                | 1                | 0                |
| 3        | 206       | 1       | 1                | 0                | 0                |
| 4        | 203       | 1       | 0                | 0                | 1                |
| 4        | 207       | 1       | 0                | 0                | 1                |
| 4        | 208       | 1       | 0                | 0                | 1                |
| 5        | 203       | 1       | 0                | 0                | 1                |
| 5        | 208       | 1       | 1                | 0                | 0                |
| 5        | 209       | 1       | 0                | 0                | 1                |
| 5        | 213       | 0       | 0                | 1                | 0                |
| 6        | 203       | 1       | 0                | 0                | 1                |
| 6        | 209       | 1       | 0                | 0                | 1                |
| 7        | 201       | 0       | 0                | 1                | 0                |
| 7        | 203       | 1       | 0                | 0                | 1                |
| 7        | 204       | 1       | 0                | 0                | 1                |
| 8        | 203       | 1       | 0                | 0                | 1                |
| 8        | 205       | 1       | 0                | 0                | 1                |
| 9        | 203       | 1       | 0                | 0                | 1                |
| 9        | 206       | 1       | 1                | 0                | 0                |
| 9        | 212       | 0       | 0                | 1                | 0                |
+----------+-----------+---------+------------------+------------------+------------------+

```sql
select
	coalesce(a.user_id, b.user_id) as user_id,
	coalesce(a.goods_id, b.goods_id) as goods_id,
	case when a.order_time is not null then 1 else 0 end as is_buy,
	case when a.order_time is not null and b.collect_time is not null then 1 else 0 end as buy_and_col,
	case when a.order_time is not null and b.collect_time is null then 1 else 0 end as buy_not_collect,
	case when a.order_time is null and b.collect_time is not null then 1 else 0 end as collect_not_buy
from (
	select *
	from t_order
) a
full join (
    select *
    from t_collect_log
) b on a.user_id = b.user_id and a.goods_id = b.goods_id;

```

## 用户中两人一定认识的组合数(一张表自关联)

有某城市网吧上网记录表，包含字段：网吧id，访客id（身份证号），上线时间，下线时间。

规则1：如果两个用户在同一个网吧上线时间或者下线时间间隔在10分钟以内，则两个用户可能认识；
规则2：如果两个用户在三家以上的网吧出现过【规则1】可能认识的情况，则两人一定认识；
请计算该市中两人一定认识的组合数。

样例数据

+---------+----------+----------------------+----------------------+
| bar_id  | user_id  |      login_time      |     logoff_time      |
+---------+----------+----------------------+----------------------+
| 1       | 001      | 2023-08-01 09:00:00  | 2023-08-01 10:00:00  |
| 1       | 003      | 2023-08-01 09:04:00  | 2023-08-01 11:00:00  |
| 2       | 004      | 2023-08-01 10:00:00  | 2023-08-01 12:02:00  |
| 1       | 006      | 2023-08-01 10:00:00  | 2023-08-01 12:00:00  |
| 2       | 005      | 2023-08-01 10:10:00  | 2023-08-01 11:00:00  |
| 2       | 001      | 2023-08-01 11:01:00  | 2023-08-01 12:00:00  |
| 2       | 002      | 2023-08-01 11:03:00  | 2023-08-01 14:00:00  |
| 3       | 002      | 2023-08-02 15:00:00  | 2023-08-02 17:06:00  |
| 3       | 001      | 2023-08-02 16:01:00  | 2023-08-02 17:07:00  |
| 3       | 004      | 2023-08-02 16:02:00  | 2023-08-02 18:00:00  |
| 3       | 003      | 2023-08-02 20:00:00  | 2023-08-02 22:00:00  |
| 4       | 001      | 2023-08-03 17:00:00  | 2023-08-03 19:00:00  |
| 4       | 002      | 2023-08-03 18:00:00  | 2023-08-03 21:00:00  |
| 4       | 003      | 2023-08-03 18:05:00  | 2023-08-03 22:00:00  |
| 4       | 004      | 2023-08-03 19:00:00  | 2023-08-03 18:58:00  |
+---------+----------+----------------------+----------------------+

````sql
select
	t.a_user_id,
	t.b_user_id,
	count(distinct t.bar_id) as num
from (
    select
        a.user_id as a_user_id,
        b.user_id as b_user_id,
        a.bar_id as bar_id,
        a.login_time as a_login_time,
        b.login_time as b_login_time,
        a.logoff_time as a_logoff_time,
        b.logoff_time as b_logoff_time
    from table a
    left join table b 
        on a.bar_id = b.bar_id 
        and a.user_id < b.user_id  -- 防止关联循环
        and (
            abs(unix_timestamp(a.login_time) - unix_timestamp(b.login_time)) < 600
            or
            abs(unix_timestamp(a.logoff_time) - unix_timestamp(b.logoff_time)) < 600
        )
) t
group by t.a_user_id, t.b_user_id
having count(distinct t.bar_id) > 3;
````

## 截止目前登陆用户数及登陆用户列表

```sql
select 
	log_date,
	user_id,
	count(distinct user_id) over(order by log_date asc)
	collect_set(user_id) over(order by log_date asc)
from table 
group by log_date
```



## 取出累计值与1000差值最小的记录

```sql
解法一
SELECT id, money, mon_cnt - 1000 AS diff
FROM (
    SELECT id, money,
           SUM(money) OVER (ORDER BY id ASC) AS mon_cnt
    FROM t_cost_detail
) t
ORDER BY ABS(mon_cnt - 1000) ASC
LIMIT 1;

解法二
SELECT
    id,
    money
FROM (
    SELECT
        id,
        money,
        row_number() OVER (ORDER BY abs_diff) AS rn
    FROM (
        SELECT
            id,
            money,
            SUM(money) OVER (ORDER BY id) AS sum_money,
            ABS(SUM(money) OVER (ORDER BY id) - 1000) AS abs_diff
        FROM t_cost_detail
    ) t
) tt
WHERE rn = 1;
```

## 用户浏览记录合并

```sql
select 
	user_id,
    sum(is_60) over(partition by user_id order by access_time) as group_id
from(
    select user_id,
        case when abs(time-last_time) <=60 then 1 else 0 as is_60,
    from(
        select 
            user_id,
            unix_timestamp(access_time,'yyyy-MM-dd HH:mm;ss') as time
            lag(time) over(partition by user_id order by time asc) as last_time
        from table
    )t 
)tt
```

## 合并日期重叠的活动

```sql
select 
	group_id,
	hall_id,
	max(end_time) as end_time,
	min(start_time) as start_time
(
    select
        hall_id,
        sum(if(is_combine==1),0,1) over(partition by hall_id order by start_date) as gropu_id
    from(
        select hall_id,
            case when sart_date<= last_end then 1 else 0 end as is_combine 
        from(
            select 
                hall_id,
                lag(start_date,1,end_date) over(partition by hall_id order by start_date) as last_start,
                lag(end_date) over(partition by hall_id order by start_date) as last_end
            from table
        )t
    )TT
)ttt
group by group_id,hall_id
```

## 查询最近一笔有效订单

```sql

```



## 共同使用ip用户

```sql
select sum(ip) over(partition by a.user_id,b.user_id) as cnt
from(
    select a.user_id,b.user_id,a.IP
    from table a
    left join table b
    on a.ip=b.ip
    where a.user_id<b.user_id
    group by a.user_id,b.user_id,a.IP
)t
```



## 向用户推荐好友喜欢的音乐

现有三张表分别为：

用户关注表 t_follow(user_id,follower_id)记录用户ID及其关注的人ID，请给用户1 推荐他关注的用户喜欢的音乐名称

+----------+--------------+
| user_id  | follower_id  |
+----------+--------------+
| 1        | 2            |
| 1        | 4            |
| 1        | 5            |
用户喜欢的音乐t_music_likes(user_id,music_id)

+----------+-----------+
| user_id  | music_id  |
+----------+-----------+
| 1        | 10        |
| 2        | 20        |
| 2        | 30        |
| 3        | 20        |
| 3        | 30        |
| 4        | 40        |
| 4        | 50        |
音乐名字表t_music(music_id,music_name)

+-----------+-------------+
| music_id  | music_name  |
+-----------+-------------+
| 10        | a           |
| 20        | b           |
| 30        | c           |
| 40        | d           |
| 50        | e           |

```sql
select user_id,concat_ws(',',collect_set(musci_name)) as name
from(
    select *
    from t_follow a
    left join t_music_likes b on a.follower_id=b.user_id
    left join t_music c on b.music_id=c.music_id
)t
group by user_id 
```

## 累加刚好超过各省GDP40%的地市名称

```sql
select 
	prov,
    city,
    gdp_amt,
    round(cnt/cntt,2) as rate 
(
    select
        prov,
        city,
        gdp_amt,
        sum(gdp_amt) over(partition by prov order by gdp_amt desc) as cnt
        sum(gdp_amt) over(partition by prov) as cntt
    from table
)t
where round(cnt/cntt,2)
```

## 股票波峰波谷

```sql
select *，
    case when close>last_close and close>next_close then 1 else 0 as is_波峰
    case when close<last_close and close<next_close then 1 else 0 as is_波谷
from(
    select *,
        lag(close,1) over(partition by ts_code order by trade_date asc) as last_close 
        lead(close,1) over(partition by ts_code order by trade_date asc) as next_close 
    from table
)t
```



## 每年成绩都提升的学生

```sql
select *,
	row_number() over(partition by year,subject order by score desc) as rn
from table
having rn=1


select
	student,
	sum(is_up)/count(1)
from(
    select *,
        if((score-lag(sum_score) over(partition by student order by year))>0,1,0) as is_up
    from(
        select sum(score) as sum_score,year,student
        from table 
        group by year,student
    )t
)tt
group by student
having sum(is_up)=count(1)
```

## 连续点击

```sql
select *,
	sum(if(is_flag=1,1,0) over (order by click_time asc) as group_id
from(
   select *,
        case when user_id=last_user then 0 else 1 end as is_flag
    from(
        select *,
            lag(user_id) over(order by click_time) as last_user
        from table
    )t 
)tt
```

## 去掉最大最小值的部门平均薪水

```sql
select depart_id,
       avg(salary) as avg_salary
from(
    select *,
        row_number() over(partition by depart_id order by salary asc) as asc,
        row_number() over(partition by depart_id order by salary desc) as desc
    from table
)t
where asc>1 and desc>1
group by depart_id
```

## 当前活跃用户连续活跃天数

找出上一次登录时间

再用当前时间去减

```sql
select 
	a.user_id,
	count(1) as num
from(
    select *
	from table
)a left join(
    select user_id,max(login_date) as last_login
    from table
    where is_login=0
    group by user_id
)b on a.user_id=b.user_id
where a.login_date>coalesce（b.last_login,'1970-01-01')
group by a.user_id
```

## 最长的连续登录天数-可间断

```sql
select user_id,group_id,count(*) as cnt
from(
    select *,
        sum(if(diff<=2,0,1)) over(partition by user_id order by login_datetime asc) as group_id
    from(
        select user_id,
            datediff(login_datetime,lag(login_datetime) over(partition by user_id order by login_datetime asc)) as diff
        from table
    )t    
)tt
group by user_id,group_id
```



## 销售额连续3天增长的商户

```sql
select shop_id,group_id,count(*) as cnt
from(
    select *,sum(if(diff>0,0,1)) over(partition by shop_id order by order_time asc) as group_id
    from(
        select *,
        order_amt-lag(order_amt) over(partition by shop_id order by order_time asc) as diff
    )t
)tt
where diff>0
group by shop_id,group_id
having count(*)>3
order by cnt desc
limit 1
```

## 不及格课程大于2门的学生

```sql
select sid,
	fail,
	dense_rank() over(order by avgscore desc) as rn
(
select sid,
	sum(case when score<60 then 1 else 0 end) as fail,
	avg(score) as avgscore
from table
group by sid
) t
where fail>2
```

## 所有考试科目的成绩都大于对应学科平均成绩的学生

```
select sid,
	count(case when score>avg_score then 1 else null end) as cnt,
	count(1) as sum
from(
    select *,avg(score) over(partition by cid) as avg_score
    from table
)t
group by sid
having count(case when score>avg_score then 1 else null end)=count(1)
```

## 查询每个学科第三名的学生的学科成绩总成绩及总排名

```sql
select * from(
select *,row_number() over(order by sum desc) as rn2
from(
    select *，
        row_number() over(partition by subject order by score desc) as rn
        sum(score) over(partition by student) as sum
    from table
)t
)tt
where rn=3
```

## 奖金瓜分问题

```sql
select user_id,
       score,
       power(0.5, rn) * 10000 as prize
from (select user_id,
             score,
             row_number() over (order by score desc) as rn
      from t15_user_score) t
where power(0.5, rn - 1) * 10000 >=250
```

## max_by处理缺失值

## 统计每个人的tag数

​              给定     id           tags

​                        1001        2，3，4

​                        1002        1，2，3

​                        1001        1，2，3

​                        1003        2，5，6

类似这样，要求输出比如 1001   1，2，3，4

```sql
select id,concat_set(tag) as tags
from(
	select id,explode(split(tags,',')) as tag
	from table
)
group by id 
```

## 相互关注

```sql
WITH exploded AS (
    SELECT user_id, follower_id
    FROM your_table
    LATERAL VIEW explode(split(follower_ids, ',')) t AS follower_id
)

SELECT a.user_id,b.user_id
FROM exploded a
JOIN exploded b ON a.follower_id = b.user_id AND b.follower_id = a.user_id
WHERE a.user_id < b.user_id;
```



## reference

https://www.dwsql.com/basic/consecutive
