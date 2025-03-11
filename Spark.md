## Spark vs MR

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e0c96476a819418686b9b46baf7951c7~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

Spark和MR都是基于MR并行计算

- 计算

  MR：MR的一个作业称为job，job 分为map task和reduce task。每个task在自己的进程中运行。

  Spark：spark把任务成为application，一个application对应一个spark content，每触发一次action就会产生一个job。application中存在多个job，每个job有很多stage（stage是shuffle中DGASchaduler通过RDD间的依赖关系划分job而来的）每个stage里有多个task（taskset）。taskset由taskshedulaer分发到各个executor中执行。
  
- 存储

  MR过程中重复读写HDFS，大量IO操作

  Spark二点迭代计算在内存中进行，API提供了大量RDD操作（join，group by），DAG图实现良好容错性

### Spark 为什么比HIVE快

1. 内存和磁盘刷写

   MR 在shuffle阶段，数据需要经过环形缓冲区溢写，需要按案件进行排序以便键相同的数据可以发送到同一个reduce。期间涉及大量数据传输，网络和磁盘I/O造成负担

   Spark的DAG可以减少Shuffle次数。如果计算不涉及其他节点数据交换，Spark可以在内存中一次性完成这些操作，只在最后结果落盘。如果涉及数据交换，Shuffle阶段数据还要写磁盘。(Executor 中有一个 `BlockManager`存储模块：将内存和磁盘共同作为存储设备，多轮迭代计算时，中间结果直接存储到这里。减少IO开销。)

2. 进程和线程

   MR基于**进程**，MR任务基于进程级别，每个任务运行在单独的进程。Mapper和Reducer任务执行过程中可能涉及多个线程处理输入数据,执行计算和IO操作

   Spark基于**线程**，任务是线程级别的，由执行器（Executor）中的线程池处理。每个 Executor 可以运行多个任务，每个任务由一个或多个线程处理，共享 Executor 内的内存。

3. Spark持久化缓存机制

   Spark将需要复用的数据存储在内存中，显著提高Spark运行的速度。

   MR每个阶段之间都需要把数据写入分布式文件系统，多次复制移动

4. 数据格式和序列化

   Spark使用数据序列化格式，例如Parquet

   MR默认使用文本格式，传输和解析开销

## SPARK 架构

![img](https://uploadfiles.nowcoder.com/files/20240320/261038666_1710946095702/.jpg)

主从架构，一个Master(Driver)和若干woker

- Driver：资源申请，任务分配，SparkContext主导应用执行
- Cluster Manager ：节点管理器，把算子RDD发送给Worker Node
- Executor：一个JVM进程，用于计算，接任务Task
- Driver：资源申请，任务分配
- Cluster Manager
- Executor：一个JVM进程，用于计算

- HDFS,HBASE：存储数据

## Spark作业提交流程

1. **运行环境构建**
   - 客户端提交Spark任务（Spark SQL通过抽象语法树解析、生成逻辑计划、 查询优化器优化、生成物理计划），**Driver创建一个Context对象，负责与Cluster Manager(资源管理器) 通信以及资源申请、任务分配和监控。**
   - **Content向资源管理器注册申请运行Executor进程** Executor运行情况随着心跳发送到资源管理器上。SparkContext 可以看成是应用程序连接集群的通道.
   - **RDD 创建**：在任务提交的初期，Context 会基于输入数据或已有数据集创建 RDD
2. **资源管理器为Executor分配资源，启动Executor进程**
   - Executor运行情况将随着心跳发送到资源管理器上。一个Executor进程又很多Task线程，Task对应RDD的操作
   - **RDD 分区**：RDD 被分割成多个分区，并且这些分区被分配给不同的 Executor。
3. Spark content 根据RDD 依赖关系构建DAG图
   - DAG图交给DAG 调度器（**DAGScheduler**）进行解析。DAG调度器分解成多个阶段 `Stage`（任务集），计算出各个阶段的依赖关系。
   - 把任务集交给底层的任务调度器（**TaskScheduler**）进行处理。Executor向Context申请任务（`Task`）
   - **RDD 的血统信息（lineage）**：DAGScheduler 会根据 RDD 之间的依赖关系生成 RDD 的血统信息，用来追踪每个 RDD 的转换历史。这样，即使某个 Task 失败了，Spark 也能通过血统信息重新计算丢失的分区。
4. 任务调度器（**TaskScheduler**）将任务分发给 Executor 运行，同时，SparkContext 将应用程序代码发放给 Executor。
   - **RDD 执行过程**：当任务分配到 Executor 时，Executor 会开始执行与 RDD 相关的操作。例如，`map` 或 `filter` 等操作将在 Executor 内部针对 RDD 的分区并行执行。
   - **数据传输与 Shuffle**：如果需要跨分区进行数据聚合（如 `reduceByKey`），Executor 会进行数据的 Shuffle 操作。
5. 任务在Executor运行，完成后写入数据在存储然后释放所有资源.
   - **RDD 计算**：Executor 执行任务并计算 RDD。
   - **持久化和缓存**：如果 RDD 被持久化（`persist` 或 `cache`）或中间结果需要被复用，Executor 会将计算结果存储在内存中，避免重复计算。
   - **完成任务后释放资源**：任务完成后，Executor 将结果写入外部存储系统（如 HDFS、S3 等）。随后，Executor 会通过心跳向资源管理器报告任务执行状态。如果没有更多任务，Executor 会释放资源。

![img](https://images2018.cnblogs.com/blog/1228818/201804/1228818-20180425172026316-1086206534.png)

## Spark 优势

1. 内存存储中间计算结果，减少磁盘IO
2. MR只有 MAP和Reduce两种编程算子，Spark封装了多种Transformation和Action算子（map，reduce，groupByKey）
3. Spark引进RDD（弹性分布式数据集），是spark基础数据单元，和mysql数据库中view类似

## RDD

![img](https://pic2.zhimg.com/80/v2-15aa361a456b5283a51632420dc7aa55_720w.webp

![f738dbe3df690bc0ba8f580a3e2d1112](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e5caa08f11304397a3a1164f5e74c739~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

 rdd分布式弹性数据集本身不存储数据，作为数据访问的一种虚拟结构。所有算子都基于RDD执行，不可变，可分区，里面的元素可以并行计算。RDD执行过程中会生成DAG图。 （物理角度看RDD存储的是block和node之间的映射）

- 弹性：数据可以保存在内存或者磁盘，数据优先内存存储，计算节点内存不够时可以把数据刷到磁盘等外部存储。
- 分布式：对内部的元素进行分布式存储。RDD本质可以看成只读的，可分区的分布式数据集。
- 数据集：存储数据集合
- 容错性：RDD 的 `血脉机制`保存RDD的依赖关系。Checkpoint机制当RDD结构更新或者数据丢失时对RDD进行重建

特性：

- RDD有一组分片，即数据集的基本组成单位。
- 每个分片都会被一个计算任务处理，并且决定并行计算的粒度
- RDD直接存储依赖关系

### RDD算子

Transformation（转化）算子 

- 对RDD操作，返回一个新的RDD

- **`map()`**：对 RDD 中的每个元素应用一个函数，返回新的 RDD。

  **`flatMap()`**：与 `map` 类似，但每个输入元素可能映射成多个输出元素。例如，把文本行映射成单词，最终得到的是一个包含所有单词的 RDD

  **`filter()`**：对 RDD 中的元素进行过滤，返回符合条件的元素。

  **`reduceByKey()`**：在键值对的 RDD 上进行聚合，按键进行聚合计算。

  **`groupByKey()`**：按键将值聚合到一起，返回一个 RDD，其中包含键和聚合后的值集合。

  **`distinct()`**：去重，返回一个新的 RDD，包含原 RDD 中唯一的元素。

Action（执行）算子。

- 返回结果输出到外部存储系统

- **`collect()`**：将 RDD 中的所有数据收集到 Driver 程序中。

  **`count()`**：返回 RDD 中的元素数量。

  **`reduce()`**：通过给定的函数聚合 RDD 中的元素，通常用于求和、乘积等聚合操作。

  **`first()`**：返回 RDD 中的第一个元素。

  **`saveAsTextFile()`**：将 RDD 的数据写入到外部存储系统（如 HDFS、S3、文件系统等）。

### Spark宽窄依赖&血缘

窄依赖和宽依赖

Spark中RDD的 `血脉机制`，当RDD数据丢失时，可以根据记录的血脉依赖关系重新计算，DAG调度中的stage，划分的依据也是RDD的依赖关系

**宽依赖：** 父RDD每个分区被多个子RDD分区使用

![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171657817-1513169131.png)

**窄依赖：** 父RDD每个分区被子RDD的一个分区使用

窄依赖允许子RDD的每个分区可以被并行处理，而且支持在一个节点上链式执行多条指令，无需等待其他父RDD的分区操作

![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171650540-1125656506.png)

## Spark Shuffle

Spark Shuffle 是指当 Spark 执行**宽依赖**操作（如 `reduceByKey`、`groupByKey`、`join` 等）时，需要跨分区传输数据的过程。这通常涉及到在不同节点之间交换数据，以确保相同的键（key）聚集在一起进行计算。

- 执行阶段：当一个Stage执行完需要与另一个stage进行数据交换时，Spark会开始Shuffle。
- 数据存储：
  - **内存溢写** Spark首先会在内存中存储Shuffle中间结果，如果内存不足，会溢写到磁盘，因为磁盘 I/O 速度较慢，可能会导致性能瓶颈。
  - **排序，批量写入** 溢写数据之前，Spark 会先对内存中的数据进行**排序**。排序后的数据会被分批写入磁盘。默认情况下**每批 10000 条数据**。
  - **合并文件** 当一个 Task 完成数据写入后，会产生多个临时文件。所有临时文件的数据会被读取出来合并，并按顺序写入最终的磁盘文件中。
  - **索引文件** Spark会为每个磁盘文件生成一个索引
- 数据传输：
  - 数据传输通常使用 **网络协议**进行通信，例如 HTTP 协议或基于 TCP 的协议。Spark 在 Shuffle 过程中会通过网络将一个节点的中间数据发送到另一个节点。

### Sprak和MR的Shuffle的区别

| 特性     | **Spark Shuffle**                                            | MapReduce Shuffle                                            |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 定义     | Spark Shuffle 是指当 Spark 执行宽依赖操作（如 `reduceByKey`、`groupByKey`、`join` 等）时，需要跨分区传输数据的过程。 | MapReduce Shuffle 是指 Map 阶段和 Reduce 阶段之间的数据传输过程。在 Map 阶段处理完数据后，数据根据键（key）被分组并排序，然后传输到 Reduce 阶段进行进一步的计算。 |
| 内存     | 优先存储在内存中，不足时溢写到磁盘                           | 由于磁盘 I/O 和网络传输，性能较低                            |
| 任务调度 | 基于 DAG 调度，智能任务分配和动态调度                        | 基于 Map 和 Reduce 阶段调度，较为简单                        |
| 容错机制 | 通过 RDD 血统信息重新计算丢失的数据                          | 基于任务重试                                                 |

## Spark 数据倾斜

**倾斜现象**

- 任务执行不均：Spark UI 监控各个stage执行时间，查看哪些task的执行时间长于其他task 
- 内存溢出： Spark作业内存溢出

**数据倾斜原理**

- 在Shuffle操作时，例如聚合（groupByKey）或者连接（join）时，需要根据key重新分配到不太的节点，当某个key数量很大时。某几个task执行很慢

**如何定位**

- 定位方法：查看Spark UI中每个Stage的执行情况，查看task的运行时间
- 数据倾斜一般发生在Shuffle过程中，常见的的数据倾斜算子：join，groupByKey，distinct等

**解决方案**

1. 过滤导致倾斜的key

2. 提高Shuffle并行度

   增加 `spark.sql.shuffle.partitions` 或者使用 `repartition()` 方法调整分区数。

3. 两段聚合（仅适合聚合类）

   在执行 `reduceBykey`或 `groupBykey`等聚合操作时，先进行局部聚合，再进行全局聚合

   将原本相同的key加上随机前缀做局部聚合，再去掉前缀做全局聚合

4. 将Reduce Join转为Map join(大小表)

   对RDD进行join的时候，join操作中一个RDD或者表数据量比较小。

   **原理：**普通的join会进行shuffle，将相同的key拉到同一个shuffle read task中再进行join。如果一个RDD比较小，采用广播小RDD全量数据+map算子实现join

5. 采样倾斜key 并拆分join

   如果只是某几个key导致了倾斜，可以将少数几个key分拆成独立RDD，并附加随机前缀打散成n份去进行join

6. 使用随机前缀和扩容RDD 进行join

   找到那个造成数据倾斜的RDD/Hive表，给数据标上随机前缀（n以内）。

   给另一个RDD的数局扩容n倍，每个数标上1-n的前缀。在进行join

## spark加载大数据量会不会失败

1. 内存溢出（OOM）

   **数据量超过内存限制：**在默认情况下，Spark 会将数据加载到内存中进行处理，如果内存不足，可能会触发 **溢写到磁盘** 或 **内存溢出**。

   **缓存或者持久化过多：** 进行计算或者迭代的时候，缓存了大量数据，导致内存消耗过多

2. Shuffle性能问题/数据倾斜

   当数据量较大，尤其是需要跨多个节点进行数据处理时，**Shuffle** 过程可能成为性能瓶颈。Shuffle导致大量磁盘IO和网络IO

   原因：

   - **大量的 Shuffle 操作**：Spark 中某些宽依赖操作（如 `reduceByKey`、`groupByKey`、`join` 等）会导致大量的 Shuffle 操作，这会引起数据的重分布、排序和网络传输，增加了作业的执行时间。

   - **数据倾斜**：在进行 `groupByKey` 或 `join` 等操作时，某些 `key` 可能会集中在少数几个节点上，导致 **数据倾斜**，某些节点负载过重。

3. 磁盘IO/网络IO

   因为内存不足导致溢写到磁盘和Shuffle操作中网络传输

4. 连接外部存储系统的性能问题

   Spark加载外部存储系统时导致I/O 性能瓶颈

## Spark join的有几种实现

## spark的持久化(persist&缓存机制(cache)

**持久化**

Spark默认数据存储在内存，RDD容错机制，需要根据血统计算处理，如果没有对父RDD持久化或者缓存化就需要重新做

**`persist`** 更灵活，允许用户选择不同的存储级别，适合更复杂的场景，尤其是数据较大时需要更多控制。

场景：

- 某个计算步骤十分耗时
- 计算链条十分长
- checkpoint所在的rdd要持久化persist。checkpoint前，要持久化，写个rdd.cache或者rdd.persist，将结果保存起来，再写checkpoint操作，这样执行起来会非常快，不需要重新计算rdd链条了。checkpoint之前一定会进行persist。
- Shuffle之后要persist，shuffle要进行网络传输风险很大，数据丢失重来恢复代价很大
- shuffle之前进行persist，框架默认将数据持久化到磁盘，这个是框架自动做的。

**缓存**

`cache` 是 **`persist`** 的一种简化形式，默认情况下，`cache` 会使用 **`MEMORY_AND_DISK`** 存储级别。也就是说，`cache` 将数据优先存储在内存中，如果内存不足，它会将数据溢写到磁盘中。

`cache` 主要用于那些需要多次访问的数据，以减少重复计算的开销。

## SPARK Architecture

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a66bb7a6aaba462097ea327f0c6eacff~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

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

## Reference

https://zhuanlan.zhihu.com/p/102544207
https://juejin.cn/post/7052321931625758757?searchId=20240506161817447254DE1438E4FCE872

https://www.cnblogs.com/liugp/p/16122904.html

https://xie.infoq.cn/article/71e6677d03b59ce7aa5eec22a
