## Spark vs MR

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e0c96476a819418686b9b46baf7951c7~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

Spark和MR都是基于MR并行计算

- 计算

  MR：MR的一个作业称为job，job 分为map task和reduce task。每个task在自己的进程中运行。

  Spark：spark把任务成为application，一个application对应一个spark content，每触发一次action就会产生一个job。application中存在多个job，每个job有很多stage（stage是shuffle中DGASchaduler通过RDD间的依赖关系划分job而来的）每个stage里有多个task（taskset）。taskset由taskshedulaer分发到各个executor中执行。

- 存储

  MR过程中重复读写HDFS，大量IO操作

  Spark二点迭代计算在内存中进行，API提供了大量RDD操作（join，group by），DAG图实现良好容错性

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

## Spark 优势

1. 速度快，基于内存进行计算
2. 基于RDD计算模型，容易理解和开发
3. 强通用性，应用层有Spark SQL,Spark MLib，Spark Graph，Spark Streaming
4. 集成Hadoop。继承Hadoop

## Spark比Hadoop的优势

1. Executor 多线程执行任务：Executor开启一个JVM进程，多线程执行task。MR是多进程模型
2. Executor 中有一个`BlockManager`存储模块：将内存和磁盘共同作为存储设备，多轮迭代计算时，中间结果直接存储到这里。减少IO开销。

## spark作业提交流程

1. 运行环境构建：当一个spark应用被提交时，**Driver创建一个Context对象，负责与Cluster Manager 通信以及资源申请、任务分配和监控。Content向资源管理器注册申请运行Executor进程****，**SparkContext 可以看成是应用程序连接集群的通道。
2. **资源管理器为Executor分配资源，启动Executor进程**，Executor运行情况将随着心跳发送到资源管理器上。一个Executor进程又很多Task线程
3. Spark content 根据RDD 依赖关系构建DAG图，交给DAG 调度器（**DAGScheduler**）进行解析。DAG调度器分解成多个阶段（任务集），计算出各个阶段的依赖关系。把任务集交给底层的任务调度器（**TaskScheduler**）进行处理。Executor向Context申请任务，任务调度器将任务分发给 Executor 运行，同时，SparkContext 将应用程序代码发放给 Executor。
4. 任务在Executor运行，完成后写入数据在存储然后释放所有资源.

![img](https://uploadfiles.nowcoder.com/files/20240320/261038666_1710946095710/.jpg)

## RDD是什么，有什么特点

![img](https://pic2.zhimg.com/80/v2-15aa361a456b5283a51632420dc7aa55_720w.webp)

Resilient Distributed Datasets(弹性分布式数据集合)。数据抽象，类属于视图。不可变，可分区，里面的元素可以并行计算。

- resilient：弹性。数据可以保存在内存或者磁盘

- distributed：对内部的元素进行分布式存储

- datasets：存储数据集合

特性：

```
A list of partitions
A function for computing each split 
A list of dependencies on other RDDs
Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned) 
Optionally, a list of preferred locations to compute each split on (e.g. block locations for an HDFS file)
```



## Spark宽窄依赖&血缘

#### 1、DAG

有向无环图。原始RDD通过一系列转化形成DAG

#### 2、窄依赖和宽依赖

**宽依赖：**RDD子分区依赖于所有的父EDD分区

**窄依赖：**子RDD分区依赖常数个父分区

![f5-rdd-narrow-and-wide-dependencies.png](https://img2.imgtp.com/2024/05/10/Vsp2nHCX.png)

## spark的持久化&缓存机制

## Sprak和MR的Shuffle的区别

## Spark 数据倾斜

## 用Spark遇到了哪些问题

## Spark join的有几种实现

## 背压机制应用场景 底层实现

## Reference

https://juejin.cn/post/7052321931625758757?searchId=20240506161817447254DE1438E4FCE872
