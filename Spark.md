## Spark vs Hadoop(HIVE)

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e0c96476a819418686b9b46baf7951c7~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

## SPARK Architecture

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/eb8634a7be9545f6a69475410a1d589b~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a66bb7a6aaba462097ea327f0c6eacff~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

## SPARK 架构师设计

![img](https://uploadfiles.nowcoder.com/files/20240320/261038666_1710946095702/.jpg)



主从架构，一个Master(Driver)和若干woker

Driver：资源申请，任务分配

Cluster Manager:

Executor：一个JVM进程，用于计算

HDFS,HBASE：存储数据

## Spark比Hadoop的优势

1. Executor 多线程执行任务：Executor开启一个JVM进程，多线程执行task。MR是多进程模型
2. Executor 中有一个`BlockManager`存储模块：将内存和磁盘共同作为存储设备，多轮迭代计算时，中间结果直接存储到这里。减少IO开销。

## spark作业提交流程

## Spark宽窄依赖&血缘

## spark的持久化&缓存机制

## Sprak和MR的Shuffle的区别

## Spark vs Hive







```sql
列转行
select name,'语文' as subject，语文 as score
from table
union all
select name,数学 as subject
from table
union all
select name,物理 as subject
from table

列转行
select name
    max(case subject when '语文' then score end) as 语文，
    max(case subject when '数学' then score end) as 语文，
    max(case subject when '屋里' then score end) as 语文，
from table
group by name;

```

