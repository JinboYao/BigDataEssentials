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

## 排序开窗

```sql
select * from(
    select class,student,score,dense_rank() over(partition by student order by score desc) as rn 
    from table
)T
where rn=2
```

