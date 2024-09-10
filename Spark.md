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

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a66bb7a6aaba462097ea327f0c6eacff~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

## SPARK 架构

![img](https://uploadfiles.nowcoder.com/files/20240320/261038666_1710946095702/.jpg)

主从架构，一个Master(Driver)和若干woker

<<<<<<< HEAD
- Driver：资源申请，任务分配，SparkContext主导应用执行
- Cluster Manager ：节点管理器，把算子RDD发送给Worker Node
- Executor：一个JVM进程，用于计算，接任务Task
=======
- Driver：资源申请，任务分配
- Cluster Manager
- Executor：一个JVM进程，用于计算
>>>>>>> 73683d5a821b472b1fc3f0c02c6860a6f3b8a950
- HDFS,HBASE：存储数据

#### Spark 核心组件

![img](https://static001.geekbang.org/infoq/37/37ffe974627175391384fcf34dbccc35.png)

#### 各部分功能

![img](https://static001.geekbang.org/infoq/9f/9fdc3427eaf284a2b6d3e439ada9dbdd.png)

- Driver注册了一些Executor之后，可以正式执行Spark应用程序。第一步创建RDD，读取数据源
- HDFS文件被读取到多个Worker节点，形成内存中的分布式数据集，也就是初始RDD
- Driver对RDD的定义的操作，提交Task到Executor
- Task对RDD的partiiton数据执行指定的算子操作，形成新的RDD的partiton

## spark 架构运行的特点

- 每个application 获取专属的executer进程，该进程在application期间一直驻留以多线程方式运行task
- Spark与资源管理器无关，只有能够获取executor进程
- 提交SparkContext的Client应该靠近Worker节点。因为Spark Application运行过程中SparkContext和Executor之间有大量的信息交换；如果想在远程集群中运行，最好使用RPC将SparkContext提交给集群，不要远离Worker运行SparkContext。

## Spark工作流程

- 客户端提交任务，创建`Driver`进程并且初始化SparkContext
- `SparkContent`向 `Cluster Manager`申请资源
-  `Cluster Manager`选择合适的`Worker`节点创建executor进程
- `executor`向Driver 端注册，并且等待分配task任务
- SparkContent 构建DAG图，划分stage并且分配task给Executor
- Executor启动task线程执行具体任务

## spark作业提交流程

1. **运行环境构建**
   - 当一个spark应用被提交时，**Driver创建一个Context对象，负责与Cluster Manager 通信以及资源申请、任务分配和监控。**
   - **Content向资源管理器注册申请运行Executor进程** Executor运行情况随着心跳发送到资源管理器上。SparkContext 可以看成是应用程序连接集群的通道.
2. **资源管理器为Executor分配资源，启动Executor进程**
   - Executor运行情况将随着心跳发送到资源管理器上。一个Executor进程又很多Task线程
3. Spark content 根据RDD 依赖关系构建DAG图
   - DAG图交给DAG 调度器（**DAGScheduler**）进行解析。DAG调度器分解成多个阶段 `Stage`（任务集），计算出各个阶段的依赖关系。
   - 把任务集交给底层的任务调度器（**TaskScheduler**）进行处理。Executor向Context申请任务（`Task`）
4. 任务调度器（**TaskScheduler**）将任务分发给 Executor 运行，同时，SparkContext 将应用程序代码发放给 Executor。
5. 任务在Executor运行，完成后写入数据在存储然后释放所有资源.

![img](https://images2018.cnblogs.com/blog/1228818/201804/1228818-20180425172026316-1086206534.png)

## Spark 优势

1. 内存存储中间计算结果，减少磁盘IO
2. MR只有 MAP和Reduce两种编程算子，Spark封装了多种Transformation和Action算子（map，reduce，groupByKey）
3. Spark引进RDD（弹性分布式数据集），是spark基础数据单元，和mysql数据库中view类似

## Spark比Hadoop的优势

1. Executor 多线程执行任务：Executor开启一个JVM进程，多线程执行task。MR是多进程模型
2. Executor 中有一个 `BlockManager`存储模块：将内存和磁盘共同作为存储设备，多轮迭代计算时，中间结果直接存储到这里。减少IO开销。

## RDD是什么，有什么特点

![img](https://pic2.zhimg.com/80/v2-15aa361a456b5283a51632420dc7aa55_720w.webp)

![f738dbe3df690bc0ba8f580a3e2d1112](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e5caa08f11304397a3a1164f5e74c739~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

Resilient Distributed Datasets(弹性分布式数据集合) 是分布式内存的一个抽象概念。RDD提供了一种高度受限的共享内存模型，即RDD是只读的记录分区的集合。

类似于于视图，本身不存储数据，作为数据访问的一种虚拟结构。不可变，可分区，里面的元素可以并行计算。Spark通过RDD的相互转换完成整个计算过程。

- resilient `弹性`：数据可以保存在内存或者磁盘，数据优先内存存储，计算节点内存不够时可以把数据刷到磁盘等外部存储。
- distributed `分布式`：对内部的元素进行分布式存储。RDD本质可以看成只读的，可分区的分布式数据集。
- datasets：存储数据集合
- 容错性：RDD 的 `血脉机制`保存RDD的依赖关系。Checkpoint机制当RDD结构更新或者数据丢失时对RDD进行重建

特性：

<<<<<<< HEAD
- RDD有一组分片，即数据集的基本组成单位。
- 每个分片都会被一个计算任务处理，并且决定并行计算的粒度
- RDD直接存储依赖关系
=======
```
A list of partitions
A function for computing each split 
A list of dependencies on other RDDs
Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned) 
Optionally, a list of preferred locations to compute each split on (e.g. block locations for an HDFS file)
```
>>>>>>> 73683d5a821b472b1fc3f0c02c6860a6f3b8a950

## Spark宽窄依赖&血缘

#### 1、DAG

有向无环图。原始RDD通过一系列转化形成DAG

![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171707366-810064490.png)

#### 2、窄依赖和宽依赖

Spark中RDD的 `血脉机制`，当RDD数据丢失时，可以根据记录的血脉依赖关系重新计算，DAG调度中的stage，划分的依据也是RDD的依赖关系

**宽依赖：** 父RDD每个分区被多个子RDD分区使用

<<<<<<< HEAD
![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171657817-1513169131.png)

**窄依赖：**父RDD每个分区被子RDD的一个分区使用
=======
**窄依赖：** 父RDD每个分区被子RDD的一个分区使用
>>>>>>> 73683d5a821b472b1fc3f0c02c6860a6f3b8a950

窄依赖允许子RDD的每个分区可以被并行处理，而且支持在一个节点上链式执行多条指令，无需等待其他父RDD的分区操作

<<<<<<< HEAD
![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171650540-1125656506.png)

## wordcount

![wc执行流程图](https://p1-jj.byteimg.com/tos-cn-i-t2oaga2asx/gold-user-assets/2019/4/20/16a3898802df75c3~tplv-t2oaga2asx-jj-mark:3024:0:0:0:q75.png)
=======
## spark的持久化&缓存机制
>>>>>>> 73683d5a821b472b1fc3f0c02c6860a6f3b8a950

## Sprak和MR的Shuffle的区别

## Spark 数据倾斜

## spark的持久化&缓存机制

## 用Spark遇到了哪些问题

## Spark join的有几种实现

## 背压机制应用场景 底层实现

## Reference

https://juejin.cn/post/7052321931625758757?searchId=20240506161817447254DE1438E4FCE872

https://www.cnblogs.com/liugp/p/16122904.html

https://xie.infoq.cn/article/71e6677d03b59ce7aa5eec22a
