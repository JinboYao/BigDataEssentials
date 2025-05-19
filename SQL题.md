## 行转列

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

## 列转行

```sql
select uid,语文 as course，score from 表名
WHERE `语文` IS NOT NULL  
union
select uid,数学 as course，score from 表名
WHERE `数学` IS NOT NULL  
```

## 开窗

### *合并日期

获取前面的活动最大结束时间，如果比当前的开始时间晚，合并

```sql
select hall_id,
	max(end_time) as end_time,
	min(start_time) as start_time
from(
select *, 
	sum(if(start_time<=max_end,0,1) over(partition by hall_id order by start_time asc,end_time asc) as group_id
from(
    select *,
        max(end_date) over(partition by hall_id order by start_time asc,end_time asc rows between unbounded preceding and 1 preceding) as max_end
    from table
    )tt
)t
group by hall_id,group_id 
```

### *最近一笔有效订单

```sql
with tmp as(
    select *,
        case when last_ord is 'First' then ord_id else last_ord end as last_ord_0
    from(
        select *,
        lag(ord_id,1,'First') over(partiton by user_id order by ord_time asc) as last_ord
        from table
        where is_valid=1  
    )t
)

select 
	*
from(
    select *,
    	row_number() over(partition by ord_id,user_id order by ord_id asc) as rn
    from table  a
    left join tmp b
    on a.user_id=b.user_id
    where a.ord_time<=b.ord_time
)t
where rn=1
```

### 最高峰同时直播人数

```sql
with tmp as(
select user_id,start_time as time,1 as login_flag
from table
union all
select user_id,end_time as time,-1 as login_flag
from table
)

select max(sum) as max_cnt
from(
    select sum(login_flag) over(order by time asc) as sum
    from table
)t
```

### *每分钟最大直播人数

用爆炸函数制造每分钟一个time，分组 sum的时候算每分钟

```sql
with tmp as(
select user_id,start_time as time,1 as login_flag
from table
union all
select user_id,end_time as time,-1 as login_flag
from table
union all
    0 as user_id,
    form_unixtime(unix_timestamp('2024-04-29','yyyy-MM-dd')+item*60,'yyyy-MM-dd HH:mm:ss') as time,
    0 as login_flag
    from(select posexplode(split(space(24*60),' ') as item,value)t
)

select max(sum) as max_cnt,date_form(time,'yyyy-MM-DD HH:mm') as action_time
from(
    select time,sum(login_flag) over(order by time asc) as sum
    from tmp
)t
group by date_form(time,'yyyy-MM-DD HH:mm')
```

### 股票波峰波谷

```sql
select *,
case when last_close<close and close>next_close then 1 else 0 end as is_up,
case when last_close>close and close<next_close then 1 else 0 end as is_down,
from(
select *,
	lag(close,1) over(partiton by ts_code order by trade_date asc) as last_close,
	lead(close,1) over(partiton by ts_code order by trade_date asc) as next_close
from table
)
```

### *累加刚好超过各省GDP40%的地市名称

```sql
select t1.prov,
       t1.city
from t1_gdp t1
         left join
     (select prov,
             city,
             gdp_amt,
             total_gpd_amt,
             ord_sum_gdp_amt,
             round(gdp_amt / total_gpd_amt, 2)         as city_percnt,
             round(ord_sum_gdp_amt / total_gpd_amt, 2) as lj_city_percent
      from (select prov,
                   city,
                   gdp_amt,
                   sum(gdp_amt) over (partition by prov)                      as total_gpd_amt,
                   sum(gdp_amt) over (partition by prov order by gdp_amt asc) as ord_sum_gdp_amt
            from t1_gdp) t
      where round(ord_sum_gdp_amt / total_gpd_amt, 2) < 0.6) tt
     on t1.prov = tt.prov
         and t1.city = tt.city
where tt.city is null
```

### 连续段起始位置

```sql
select min(id) as start_id,max(id) as end_id
from(
    select id,sum(if(diff=1,0,1)) over(order by id asc) as group_id
    from(
        select id,lead(id) over(order by id asc)-id as diff
        from table
    )t
)tt
group by gourp_id
```

### 合并连续支付订单

```sql
select user_id,merchant_id,max(pay_time),min(pay_time),sum(pay_amount)
from(    
    select *,
        sum(if(merchant_id=last_merchant,0,1)) over(partition by user_id order by pay_time asc) as group_id
    from(
        select *,
        lag(merchant_id) over(partition by user_id order by pay_time asc) as last_merchant
        from table
    )t
)tt
group by user_id,merchant_id,gourp_id
```

### 连续5天涨幅超过5%的股票

```sql
with tmp as(
select *,
	if(closing_price/lag(closing_price) over(partition by stock_code order by trade_date asc)-1>0.05,1,0) as is_up
from table
)
tmp2 as(
    select *,
        row_number() over(partition by stock_code order by trade_date asc)-
        row_number() over(partition by stock_code,is_up order by trade_date asc) as diff
)

select stock_code,max(trade_date),min(trade_date),count(1)
from tmp2
where flag=1
group by sotck_code,diff
haiving count(1)>=5
```

### 连续登陆超过N天用户

```sql
select user_id
from (select user_id,
             diff,
             count(1) as login_days
      from (select user_id,
                   login_date,
                   row_number() over (partition by user_id order by login_date asc) -
                   datediff(from_unixtime(unix_timestamp(login_date, 'yyyyMMdd'), 'yyyy-MM-dd'), '2022-01-01') as diff
            from t5_login_log) t
      group by user_id, diff) tt
where login_days >= 4
group by user_id
```

### 连续签到领金币

```sql
select user_id,month,sum(coin_num) as sum_n
from(
    select user_id,month,
        case when day=3 then 2 
            when day=7 then 5
            else 1 end as coin_num
    from(
        select user_id,month,
            if(mod(count(sign_date) over(partition by user_id,month,group_id order by signin_date asc),7)=0,
               7,
               mod(count(sign_date) over(partition by user_id,month,group_id order by signin_date asc)) as day
        from(
            select month(signin_date) as month,*,
                sum(if(is_sign=1,0,1)) over(partition by user_id,month order by signin_date asc) as group_id
            from table
        )t
        where is_sign=1
    )tt
)ttt
group by user_id,month
```

### 品牌营销活动天数(合并日期)

```
select brand,
	date_diff(max(end_date),min(start_date)) as diff
from(
	select *,sum(if(start_date<=max_end,0,1)) over(partition by brand order by start_date asc,end_date asc) as group_id
    form(
        select *,
            max(end_date) over(partition by brand order by start_date asc,end_date asc rows beetween unbounded proceding and 1 proceding) as max_end 
        from table
    )t
)tt
group by brand,group_id
```

### *截止目前登陆用户数及登陆用户列表

```sql
select log_date,
       user_cnt,
       user_list
from (select log_date,
             user_id,
             size(collect_set(user_id) over (order by log_date asc))          as user_cnt,
             sort_array(collect_set(user_id) over (order by log_date asc)) as user_list
      from t2_user_login) t
group by log_date, user_cnt, user_list
```

### 占据好友封面个数

```sql
with tmp as(
    select user_id,friend_id,user_step,friend_step
    from user_friend a 
    left join step b on a.user_id=b.user_id
    left join step c on a.friend_id= c.user_id
) t

select uesr_id,sum(case when user_id=top_user then 1 esle 0) end as num
from(
    select *,
        first_value(user_id) over(partition by friend_id order by user_step desc) as top_user
    from tmp
    where user_step>friend_step
)t
group by uesr_id
```

### 截止目前登陆用户数及登陆用户列表

```sql
select log_date,
       user_cnt,
       user_list
from (select log_date,
             user_id,
             size(collect_set(user_id) over (order by log_date asc))          as user_cnt,
             sort_array(collect_set(user_id) over (order by log_date asc)) as user_list
      from t2_user_login) t
group by log_date, user_cnt, user_list
```

### 最长的连续登录N天数-可间断

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

## JOIN

### 两人一定认识的组合数

```sql
with tmp as(
	select *
    from table a 
    join table b
    on a.bar_id=b.bar_id
    where a.user_id<b.uer_id
    and (
          abs(unix_timestamp(a.login_time, 'yyyy-MM-dd HH:mm:ss') -
              unix_timestamp(b.login_time, 'yyyy-MM-dd HH:mm:ss')) < 600
              or
          abs(unix_timestamp(a.logoff_time, 'yyyy-MM-dd HH:mm:ss') -
              unix_timestamp(b.logoff_time, 'yyyy-MM-dd HH:mm:ss')) < 600
          )
)

select a.user_id,b.user_id,count(a.bar)
from tmp
group by a.user_id,b.user_id
```

### 共同使用ip用户

```sql
with table as(
    select user_id,ip
    from table
    group by user_id,ip
)
with tmp as(
    select a.user_id as a,b.user_id as b,ip
    from table a
    join table b
    on a.ip=b.ip 
    where a.user_id<b.user_id
)
select a,b,count(IP)
from tmp
group by a,b
having count(IP)>3
```

### 向用户推荐好友喜欢的音乐

```sql
select a.user_id,concat(',',collect_list(c.music)) as names
from 用户关注表 a
left join 用户喜欢的音乐 b on a.follower_id=b.user_id 
left join 音乐名字表 c on b.music_id=c.music_id
group by a.user_id
```

### 受欢迎程度(自排序)

```sql
with tmp as(
    select user1_id,user2_id
    from table
    union all
    select user2_id as user1_id,user1_id as user2_id
    from table
)

select user1_id,count(user2_id)/count(distinct user1_id) over() as res
from tmp
group by user1_id
```

## Case When 计算

### 用户商品购买收藏行为特征加工

```sql
select
    coalesce(t_ord.user_id,t_collect.user_id) as user_id,
    coalesce(t_ord.goods_id,t_collect.goods_id) as goods_id,
    if(t_ord.goods_id is not null,1,0) as is_buy,
    if(t_ord.goods_id is not null and t_collect.goods_id is null,1,0) as buy_not_collect,
    if(t_ord.goods_id is null and t_collect.goods_id is not null,1,0) as collect_not_buy,
    if(t_ord.goods_id is not null and t_collect.goods_id is not null,1,0) as buy_and_collect
from
    (
    --订单表数据
    select
        user_id,
        goods_id
    from t2_order
    group by
        user_id,
        goods_id
    ) t_ord
    full join
    (
    select
        user_id,
        goods_id
    from t2_collect_log
    group by
        user_id,
        goods_id
    ) t_collect
        on t_ord.user_id = t_collect.user_id
        and t_ord.goods_id = t_collect.goods_id
```

## 有序拼接

```sql
select regexp_replace(
    concat_ws(',',sort_array(collect_list(concat_ws(':',lpad(id,5,0),val)))),
    '\\d+\:','')
from table
```

### 行为路径分析--无法开窗解决

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

### 用户行为轨迹

```sql
with tmp as(
select user_id,station_id,in_time as time
from table
where out_time is Null
union all
select user_id,station_id,out_time as time
from table
where in_time is Null
union all
select user_id,market as station_id,check_time as time
from table2
)

select 
```

### 合并数据

```sql
select max_id
	concat("|",collect_list(name)) as name,
from(
	select *,
		max(id) over(partition by name) as max_id,
    	min(id) over (partition by name) as ord_id --按id排序
	from table
)t
group by max_id，ord_id

--- group_id
select max(id) as id,concat('|',collect_list(name)) as name
from(
    select *,
        row_number() over(partition by name order by id)-id as group_id
    from table
)t
group by group_id
```

## 留存

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

## 相互关注

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

```sql
SELECT
    from_user,
    to_user,
    SUM(1) OVER(PARTITION BY concat_users) AS flag
FROM
    (
        SELECT
            from_user,
            to_user,
            IF(from_user > to_user, 
               CONCAT(from_user, '-', to_user), 
               CONCAT(to_user, '-', from_user)) AS concat_users
        FROM 
            follow
    ) t;
```



### 连续签到领金币（找出group）

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

### 连续5天以上未登录的用户及最近一次登录时间(lag函数)

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

```sql
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

## N指标--累计去重

```
-- table
time_id          user_id
2018-01-01 10:00:00    001
2018-01-01 11:03:00    002
2018-01-01 13:18:00    001
2018-01-02 08:34:00    004
2018-01-02 10:08:00    002
2018-01-02 10:40:00    003
2018-01-02 14:21:00    002
2018-01-02 15:39:00    004
2018-01-03 08:34:00    005
2018-01-03 10:08:00    003
2018-01-03 10:40:00    001
2018-01-03 14:21:00    005
```

```
日期       当日活跃人数     月累计活跃人数_截至当日
date_id   user_cnt_act    user_cnt_act_month
2018-01-01      2                2
2018-01-02      3                4
2018-01-03      3                5
```

SQL实现

```sql
-- 去重
with t0 as(
    SELECT
        DATE(time_id) AS date_id,
        user_id
    FROM table
    GROUP BY DATE(time_id), user_id
)
-- 计算 当日活跃人数
,t1 as(
    select 
    	count(user_id) as user_cnt_act,
    	date_id
    from t0
    group by date_id
)
-- 计算 月累计活跃人数_截至当日
,t2 as(
    select
    	a.date_id,
    	count(distinct user_id) as user_cnt_act_month
    from t0 as a
    join t0 as b
    on date_format(a.date_id,'yyyy-MM')=date_format(a.date_id,'yyyy-MM')
    and a.date_id>=b.date_id
    group by a.date_id
)
select 
	t2.date_id,
	t2.user_cnt_act_month,
	t1.user_cnt_act
from t2
join t1
on t2.date_id=t1.date_id
order by t2.date_id
```

## 非等值连接--最近匹配

笛卡尔积

a表和b表join实现：

```
a    b
1    2
2    2
4    3
5    3
5    7
8    7
10   11
```

SQL

```sql
select a,b
from(
    select
        a,
        b,
        abs(a-b) as abs,
        min(abs(a-b)) over(partition by a) as min_abs,
    from a 
    cross join b
)t
where abs=min_abs
```

## 非等值连接--范围匹配

```sql
SELECT 
    t1.date_id,
    t1.p_id,
    t2.p_value
FROM table1 t1
LEFT JOIN table2 t2
    ON t1.p_id = t2.p_id
    AND t1.date_id BETWEEN t2.d_start AND t2.d_end
```

## 时间序列--构造连续日期

```sql
select 
	date_add(start_time,pos) as day_time
from table
labteral view posexplode(split(space(datediff(end_time,staer_time)),'')) as pos,val
```

## 时间序列--补全数据

```sql
select 
	first_value(a) over(partition by group_id order by date_id asc) as a,
	date_id
from(
    select
        a,
        date_id,
        count(a) over(order by date_id) as group_id
    from table
)t
```

所有科目成绩都大于某一学科平均成绩的学生

```sql
with avg as( 
    select subject,avg(score) as avg
    from table
    group by subject
)

select uid,
	sum(flag) as avg,
	count(uid) as cnt
from(
    select if(a.score-b.avg>1,1,0) as flag,a.*
    from table a
    left join avg b on a.subject=b.subject
)t
group by uid
having sum(flag)=count(uid)
```



## reference

https://www.dwsql.com/basic/consecutive



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

##### 



```sql
select *
from table a
join table b on a.bar_id=b.bar_id
where t1.user_id<t2.user_id
and (abs(a.login_time,b.login_time)<=600 or abs(a.logoff_time,b.logoff_time)<=600)
```



