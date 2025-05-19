![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e0c96476a819418686b9b46baf7951c7~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

## Spark和MR并行计算对比

MapReduce 是阶段式、磁盘驱动的粗粒度并行计算；Spark 是内存驱动、流水线式的细粒度并行计算

|                                        | MapReduce                                                 | Spark                                           |
| -------------------------------------- | --------------------------------------------------------- | ----------------------------------------------- |
| 执行模型                               | 两阶段模型(Map-Shuffle-Reduce)                            | DAG图执行模型（划分stage）                      |
| 任务并行粒度                           | MapTask数量(Block块)<br />ReduceTask数量(setReduceNumber) | RDD Partition(一个partition对应一个Task)        |
| 执行并行度<br />(分批运行任务并行粒度) | 可同时跑的 Task 数（YARN container）                      | Executor 数 × 每个 Executor 的 core 数          |
| 任务调度                               | 每个Task启动独立的JVM                                     | Executor启动一次JVM, 多个task在Executor并行运行 |
| 执行机制                               | 每次Job提交立即执行                                       | 懒执行(遇到Action才执行)                        |
| 中间结果(**内存使用**)                 | 中间结果必须落盘                                          | 默认内存，必要时落盘(Shuffle)                   |
| IO性能                                 | IO 瓶颈明显，磁盘频繁读写                                 | 内存驱动，延迟低，性能高                        |
| 缓存                                   |                                                           | 支持 RDD 缓存、广播变量                         |

- 并行计算

  MR：一个任务划分成Map和Reduce阶段，一个Task启动独立的JVM执行task。基于磁盘刷写

  Spark： 基于内存驱动计算
  
  **创建执行计划(懒执行)**
  
  - 用户提交spark代码后，Driver创建一个Context对象(进程)，跟资源管理器申请Executor，创建RDD。
  - 遇到`Action`算子创建DAG无向图，发送给DAG scheduler划分stage和task
  
  **Executor并行运行**
  
  - `Executor`执行Task Scheduler分配的task，执行并行度是Executor 数 × 每个 Executor 的 core 数

## Spark SQL 为什么比 HIVE 快

1. 内存和磁盘刷写

   **HIVE 基于磁盘，Spark SQL 使用内存计算作为核心计算机引擎，极大减少磁盘IO**

   MR每个 map 和 reduce 阶段都要落地 → 产生**大量 HDFS IO**。

   - 数据需要经过环形缓冲区溢写，需要按key进行排序以便键相同的数据可以发送到同一个reduce。

   Spark **数据尽可能存在内存中**，避免频繁读写磁盘。

   - Spark的DAG可以减少Shuffle次数。如果计算不涉及其他节点数据交换，Spark可以在内存中一次性完成这些操作，只在最后结果落盘。如果涉及数据交换，Shuffle阶段数据还要写磁盘。

   - (Executor 中有一个 `BlockManager`存储模块：将内存和磁盘共同作为存储设备，多轮迭代计算时，中间结果直接存储到这里。减少IO开销。)

2. 进程和线程

   MR基于**进程**级别，每个任务运行在单独的进程。Mapper和Reducer任务执行过程中可能涉及多个线程处理输入数据,执行计算和IO操作

   Spark基于**线程**级别，由执行器（Executor）中的线程池处理。每个 Executor 可以运行多个任务，每个任务由一个或多个线程处理，共享 Executor 内的内存。

3. Spark DAG调度

   Spark的DAG可以减少Shuffle次数。如果计算不涉及其他节点数据交换，Spark可以在内存中一次性完成这些操作，只在最后结果落盘。

   MR 一个任务划分成Map和Reduce阶段，一个Task启动独立的JVM执行task

3. Spark Shuffle 优化

   MR每次Shuffle 都需要分区快排，Spill的多个临时文件归并排序，reduce拉取map的输出后归并排序成一个文件

   Spark Shuffle 基于Hash将相同的key写入同一个内存缓冲区/磁盘文件；或者预排序-批量处理-合并大文件+索引记录 ，不需要多次排序加快速度

3. Spark持久化缓存机制

   Spark将需要`复用的数据`缓存在内存中，下次再使用此RDD时直接从缓存获取，显著提高Spark运行的速度。

   MR每个Shuffle都需要写入磁盘，增加了读写延迟

4. 数据格式和序列化

   Spark使用数据序列化格式，例如Parquet

   MR默认使用文本格式，传输和解析开销大

## Sprak和MR的Shuffle的区别

**MR的shuffle**

- Map端shuffle：分区，放入环形缓冲区，排序，Spill溢出，合并几个阶段

- Reduce端shuffle：复制，合并，排序几个阶段

**问题：**MR在map端进行一次排序，在reduce端对map的结果会进行一次排序。最后多个溢写文件在最后merge成一个输出文件的时候还会排序一次。MR 的全局排序消耗资源较大

**Spark的shuffle**

- 基于Hash的shuffle

  Mapper根据Reduce数量创建相应的bucket，Mapper的结果`分区`到bucket

  Reduce启动的时候，从内存或者磁盘拉取相应的bucket

  Spark在Reduce端做聚合，不需要合并和排序。聚合是hashmap，将shuffle读取的key值插入到hashmap

  **问题：** 创建Map*Reduce个小文件。I/O开销和缓存开销大

- 基于sort的shuffle

  Mapper先把数据写入内存数据结构。聚合类使用map数据结构，边聚合边写入；非聚合类(join)使用array数据结构直接写入内存

  内存数据到达某个阈值就会溢写到磁盘，溢写之前，对key排序再分批写入

  多次磁盘溢写会有多个文件，最终合并成一个大数据文件和一个索引文件

## Spark是否可以完全取代Hadoop

**稳定性**

- **内存管理：** Spark依赖于内存存储中间数据。大量数据缓存在 RAM 中可能导致 Java 垃圾回收（GC）问题

```
GC问题：
* 频繁的垃圾回收：大量缓存在内存中，新生代和老年代就有可能很快被填满，JVM就需要频繁的垃圾回收，GC期间，大部分用户线程需要暂停等待
* 内存溢出： 垃圾回收无法有效释放足够的内存空间来满足新对象的分配需求，JVM 最终会内存溢出
```

- **代码质量处理：** 基于线程的task 资源隔离没有保证，代码执行过程复杂

**数据处理能力**

- **大数据集限制**：超过 RAM 大小的数据集，Spark 可能会遇到内存不足的问题，这在单个节点上尤为明显。如果内存不足，还是有磁盘I/O

## SPARK 架构

![img](https://uploadfiles.nowcoder.com/files/20240320/261038666_1710946095702/.jpg)

**Spark主从架构，一个Master(Driver)和若干woker(Executor)**

- Driver：
  - 用户提交spark代码后，Driver创建一个Context对象(进程)，跟资源管理器申请Executor。
  - 用户提交spark代码后，把代码转换成逻辑计划，DAG无向图。发送给DAG scheduler

- Cluster Manager ：节点管理器，把算子RDD发送给Worker Node
- Executor：一个JVM进程，用于计算任务Task
- HDFS,HBASE：存储数据

**Applicaiton、Job、Stage、Task之间的关系**

- Application 用户提交的一个完整 Spark 程序。`spark—submit xxx.py`
- Job   
  - 每调用一次 Action(结果返回存储系统)，就触发一个 Job
  - **流程：** `Transformation`算子懒执行，当遇到`Action`算子。Driver会：
    - 从当前RDD往上构建DAG
    - 然后根据shuffle划分Stage
    - 把这整个过程算一个job。然后遇到下一个action，执行下一个job
- stage  由 Job 中的 DAG 图被划分成几段 Stage 来决定数量，一个宽依赖划分一次stage
- task  由RDD分区数量决定，在stage中并行执行，真正运行在 Executor 上的最小单位

![Application组成](https://i-blog.csdnimg.cn/blog_migrate/54b32b3de899f513f172d2e14ff56d93.png)

## Spark 工作机制

1. **构建运行环境** 

   - 用户提交的一个完整 Spark 程序，`spark—submit main.py`

     ```shell
     --executor-cores
     --executor-memory
     --num-executors
     --driver-cores
     --drive-rmemory
     ```

   - Driver创建一个Context对象，负责跟资源管理器申请Executor。

   - Context注册Executor：**Content向资源管理器注册申请运行Executor进程**，Executor等待task，Executor运行情况随着心跳发送到资源管理器上。

2. **SparkContext构建DAG图(有向无环图)、划分`stage`并分配taskset至Executor**

   - `RDD`创建和转换：Context 会基于输入数据或已有数据集创建 RDD
   - `DAG`构建：Spark根据RDD依赖关系构建DAG
   - `DAG Scheduler`划分Stage：分析DAG将作业划分成多个Stage。每个stage包换了一组可以并行执行的任务集。
   - `Task Scheduler`任务调度：`DAG Scheduler`根据stage的划分把任务发送到Task Scheduler，`Task Scheduler`把任务调度到 executors上。

3. **数据分区**

   输入数据被划分成物理分区。RDD封装分区，在节点间并行处理

4. **任务执行**

   `Executor`执行Task Scheduler分配的任务，并行度由core决定

   - 一个`Executor`进程有很多Task线程，Task对应RDD的操作

5. **数据流动**

   - `Shuffle`：转换操作需要跨分区聚合数据

6. **任务完成返回结果到Driver**

7. **容错和恢复**

   ​	`RDD 的血统信息（lineage）` 提供容错能力，任何节点失败可以重新计算

## Spark 部署方式

- Local
- Standalone: Master-Slaver调度集群
- Yarn：Spark客户端直接连接Yarn。yan-client和yarn-cluster模型
- Mesos

## Spark Client vs Cluster 

**Spark Client**

Driver运行在提交命令的机器上，Driver掉线任务会直接失败 。

- 任务提交，submit
- 本地机器会开启driver进程,进行任务划分，资源申请
- Spark Yarn Client向YARN的ResourceManager申请启动Application Master，ResourceManager收到请求后，在集群中选择一个NodeManager，为该应用程序分配第一个Container，要求它在这个Container中启动应用程序的ApplicationMaster



![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e5f2d3bcf60040fc8d8b468070850475~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

**Spark Cluster**

Driver被分配到群集的container中，提交任务的终端掉了，Driver的container会继续工作

- 先把Driver作为ApplicationMaster在YARN集群启动
- ApplicationMaster创建应用程序，向资源管理器申请资源，启动executor执行task

![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/38f0602454b14d04bdd3b8b532dd5cf5~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

## Spark SQL解析流程

1. 输入SQL
2. 解析器：把SQL转换为抽象语法树AST
3. 语义分析器：分析AST 
4. 逻辑优化器：分析后的AST 转换成 逻辑计划
5. 物理优化器：逻辑计划 转换成 物理计划
6. 代码生成器：物理计划变成可以执行的java字节码
7. 执行器：执行器执行

## Spark 优势

1. **内存计算：**Spark使用内存计算作为核心计算引擎，减少磁盘IO
2. **数据存储格式：**Spark支持多种数据存储格式，Parquet、ORC 等。在列式存储和压缩方面有优势
3. **并行计算：**Spark将任务划分成多个并行任务，在多个节点上并行计算
4. MR只有 MAP和Reduce两种编程算子，Spark封装了多种Transformation和Action算子（map，reduce，groupByKey）

## partition和block

HDFS中，block是磁盘的最小存储单元。block是物理存储单位

Spark中，**partition** 是RDD的最小单元。partition是逻辑处理单位

每个RDD可以分成多个partition，每个partition就是一个数据集片段，partition可以分配到不同节点上面计算。

## RDD

![img](https://pic2.zhimg.com/80/v2-15aa361a456b5283a51632420dc7aa55_720w.webp

![f738dbe3df690bc0ba8f580a3e2d1112](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e5caa08f11304397a3a1164f5e74c739~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

RDD分布式弹性数据集本身不存储数据，作为数据访问的一种虚拟结构（物理角度看RDD存储的是block和node之间的映射）。所有算子都基于RDD执行，`不可变，可分区，里面的元素可以并行计算`。 

- 弹性：数据可以保存在内存或者磁盘，数据优先内存存储，计算节点内存不够时可以把数据刷到磁盘等外部存储。
- 分布式：对内部的元素进行分布式存储。RDD本质可以看成只读的，可分区的分布式数据集。
- 数据集：一个RDD就是一个分布式对象集合，本质上是一个只读的分区记录集合

特性：

- 分布式内存：RDDs是存储在分布式内存中的，可以在集群的多个节点上并行计算
- 不可变性：RDDs是不可变的数据结构，它们的数据只能通过转换操作创建
- 容错性：RDDs具有容错性， `血脉机制`保存RDD的依赖关系
- 懒加载：RDDs是惰性计算

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

**宽依赖：** 每个子RDD分区依赖父RDD的全部分区

**窄依赖：** 每个子RDD依赖父RDD的同一个分区

窄依赖允许子RDD的每个分区可以被并行处理，而且支持在一个节点上链式执行多条指令，无需等待其他父RDD的分区操作

![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5d8b5924f4654875b9bd589450a3f43a~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

### 算子的区别

#### groupByKey、reduceByKey、aggreageByKey、combineByKey 

- groupByKey  对数据集中的元素按键分组，但不进行任何聚合计算。它会生成一个（键，值序列）的数据集。
- reduceByKey  按键合并数据集的值。分区内先聚合，再shuffle
- combineByKey  先在本地进行规约计算，再到下一个节点聚合。给每个key一个累加器存储想要的数据结构(例如，value的值)，再聚合。
- aggreageByKey  分区内聚合+分区间相同的key聚合。 累加器必须初始化，不能变化

#### cache、presist

cache：默认将数据集存储在内存中

```scale
调用 persist(StorageLevel.MEMORY_ONLY)
```

persist：允许用户选择存储级别（例如，内存、磁盘或两者的组合）。默认为Momory

#### repartition、coalesce

repartition 重新分配数据，以改变RDD分区数量。一定会**全局shuffle**。调整并行度，调整数据的分区分布

```sql
调用了coalesce(..., shuffle = true) 
构建新的分区ShuffledRDD，指定新的分区器（比如 HashPartitioner），将原数据重新划分到目标分区数中。
```

coalesce  会将多个小分区合并为一个大分区，减少RDD数量

```sql
shuffle = false
把多个原分区合并为一个逻辑分区，不shuffle
```

#### map、flatMap

map 将函数用于RDD中每个元素，返回值构成新的RDD。例如，将每个数字乘以2。

flatMap 与 `map` 类似，但每个输入元素可以映射到0或多个输出元素。例如，将句子分解为单词

## RDD、DataFrame、DataSet

都是Spark的弹性分布式数据集

RDD：容错的、不可变的分布式数据集合。只记录了数据

DataFrame：在RDD基础之上，记录了数据结构。类似于二维表的格式

DataSet：是 DataFrame API 的一个扩展，存储了数据结构+**字段类型+严格的错误检查**

## Spark Shuffle

Spark Shuffle 是指当 Spark 执行**宽依赖**操作（如 `reduceByKey`、`groupByKey`、`join` 等）时，需要跨分区传输数据的过程。这通常涉及到在不同节点之间交换数据，以确保相同的键（key）聚集在一起进行计算。

**Hash Shuffle**

![img](https://ask.qcloudimg.com/http-save/yehe-2039230/8852487da9ed55cffc637ae65ab62b47.png)

- **Map阶段**

  每个task处理输入数据，执行map、filter等操作。对数据进行分组，存入Map的buffer和block

- **Shuffle Write**

  - 每个task处理的数据按照key进行hash算计，将相同的key写入同一个内存缓冲区/磁盘文件。

  - 磁盘文件数量：Task数量*Reduce任务数量(计算机结果的种类个数)


- **Shuffle Read**
- 上一个Stage计算结果相同的Key，从各个节点上拉到自己所在的节点上
  
- shuffle read task都有一个自己的buffer缓冲，每次只能拉取buffer能承载的数据(防止内存溢出)，在内存中聚合完再拉下一批


- **Reduce阶段**

  聚合、排序等处理

```
优化：
缓冲区阶段，Task复用Buffer缓冲区。不需要为executor每一个task都创建buffer
```

**Sort Shuffle**

![img](https://ask.qcloudimg.com/http-save/yehe-2039230/3a2d37c338fd287135746a6aca23718c.png)

- **Map阶段**
  - 数据写入内存数据结构：根据Shuffle算子，数据写入不同数据结构
    - 聚合操作(ReduceByKey)：Map结构，允许在写入数据时聚合
    - 非聚合操作(join): 使用Array结构，直接存入内存
  - **内存缓冲**：每写入一条数据到内存数据结构，判断是否到达阈值。如果到达阈值，就把内存数据结构的数据溢写到磁盘，再清空内存数据结构。
  
- **Shuffle**
  
  - **排序、分批溢写**：**溢写到磁盘之前，先根据key进行排序**。再以每批1万条数据形式分批写入
  - **合并磁盘文件：** 所有任务完成后，合并磁盘文件
  - **创建索引：** 根据下一个task 创建索引记录数据段的位置
  
- **Reduce阶段**

  根据索引获得数据，聚合、排序等处理

```
Bypass SortShuffle
当数据量比较小的时候，Shuffle输出的分区较少，跳过排序过程，直接溢写
```

## Spark 数据倾斜

**倾斜现象**

- Executor OOM，Shuffle过程出错，执行时间特别久
- Driver OOM
- 正常运行的任务突然失败

**数据倾斜原理**

- 在Shuffle操作时，例如聚合（groupByKey）或者连接（join）时，需要根据key重新分配到不太的节点，当某个key数量很大时。某几个task执行很慢

**如何定位**

- 定位方法：查看Spark UI中每个Stage的执行情况(某个task执行特别慢；某个task内存溢出)
- 数据倾斜一般发生在Shuffle过程中，常见的的数据倾斜算子：join，groupByKey，distinct等

**解决方案**

1. 过滤导致倾斜的key

2. 提高Shuffle并行度

   ```sql
   set spark.sql.shuffle.partitions
   repartition(n)
   ```

3. 两段聚合（仅适合聚合类）

   - **局部聚合+全局聚合**

   - 在执行 `reduceBykey`或 `groupBykey`等聚合操作时，先进行局部聚合，再进行全局聚合

   - 将原本相同的key加上随机前缀做局部聚合，再去掉前缀做全局聚合

4. 将Reduce Join转为Map join(大小表)

   对RDD进行join的时候，join操作中一个RDD或者表数据量比较小。

   **原理：**普通的join会进行shuffle，将相同的key拉到同一个shuffle read task中再进行join。如果一个RDD比较小，采用**广播小RDD全量数据+map算子**实现join

5. 采样倾斜key 并拆分join

   如果只是某几个key导致了倾斜，可以将少数几个key分拆成独立RDD，并附加随机前缀打散成n份去进行join

6. 使用随机前缀和扩容RDD 进行join

   找到那个造成数据倾斜的RDD/Hive表，给数据标上随机前缀（n以内）。

   给另一个RDD的数局扩容n倍，每个数标上1-n的前缀。在进行join
   
7. HIVE ETL 预处理数据

   HIVE 预先对数据按照key进行聚合，或者预先和其他表jion

   Spark执行预处理后的表

## Spark的优化策略

**延迟执行** Spark遇到转换操作时，会先记录下来，直到遇到行动操作时执行。可以把多个转换操作合并成一个任务

**分区执行** Spark把数据划分成很多分区，分配给集群的不同节点并行运行

**内存管理** Spark把数据优先存储在内存中，减少磁盘IO。同时还使用数据序列化和内存缓存提高内存利用率和传输效率

**任务调度**  Spark使用任务调度器把任务分配给集群中不同节点运行

**部分聚合** Spark可以在分区做部分聚合，再做全局聚合。例如Sort的shffule

**广播变量** 集群的所有节点共享一个较小的只读变量时，广播变量减少数据传输和复制开销

## Spark容错机制

**数据容错：RDD的血统** 信息可以重新计算丢失的数据分区

**任务容错：任务调度器容错** 可以重新调度发生错误的任务，并且分配给其他可用节点运行

**数据丢失容错：数据持久化** 把数据缓存到磁盘上，当节点发送故障，可以用持久化存储中恢复

**节点容错：主从架构**。主节点（Driver）协调任务执行，如果挂了，可以重启一个新的主节点容错

## Spark加载大数据量会不会失败

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

## Spark共享变量(广播变量/累加器)

**广播变量**

- 问题： task需要使用变量的时候，频繁拉取driver的变量。如果大便量，复制开销大
- 作用：数据从Driver发送到所有的Executor，存储一份备份。Executor的task共享变量。

**累加器**

- 作用：Driver端进行全局汇总的计算需求。
  - Driver端定义且赋初始值给累加器.
  - Executor更新，最终在Driver端读取最后的汇总值

## Spark的持久化&缓存机制

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

## Spark WordCount

**创建SparkContent**

**加载文本**  使用textfile函数加载文本，转换成RDD

**数据转换** `flatmap()` 算子把文本拆分成单词，`map()`算子计算key，value

**聚合** `groupByKey()` 算子聚合key-V，value相加

**输出** `collect`算子把结果收集到Driver

## Spark TopN

使用`textfile`加载数据，转换成RDD

**数据转换** 使用`map()` 或者`flatmap()` 计算 key-value

**按键分组 **使用`groupByKey()` 将数据按key进行分组

**计算TOPN ** 通过`mapValues`方法把前N个值取出来

**全局取TOP N** 通过`reduceByKey()` 合并分区结果

## Spark 分组排序

使用`spark.read` 读取数据

**分组排序** `groupBy` 分组，`orderBy`排序

## Join类型

**inner join** 内连接

**outer join** 返回关联和不关联的数据。不关联的数据置空。`full outer join` `left outer join` `right outer join`

**cross join** 笛卡尔积

**Semi Join ** 左表中有匹配右表的行的那部分数据

```sql
SELECT *
FROM table1
WHERE id IN (SELECT id FROM table2);
```

**Anti join ** 左表中没有在右表中找到匹配的行的数

```sql
SELECT *
FROM table1
WHERE id NOT IN (SELECT id FROM table2);
```

## Join实现

**Join原理：** join的两张表抽象为`流式遍历表`(大表)和`查找表`(小表)

**Sort Merge Join**

- 适用于大表join大表，默认join
- 步骤： 
  - 两张表根据key进行分区和排序，将可能join到一起的key放在同一个分区中
  - 在Join 的时候，对于遍历表的下一条数据，只需要从查找表的上一次查找结束的位置找

**Broadcast Hash Join**

- 适用于大表join小表
- 步骤
  - 把`查找表`**广播到每个计算节点**，然后查找表放到hash表中。Join的时候直接hash查找不用shuffle了

**Shuffle Hash Join**

- 适用于大表join小表，且每个分区的记录不能太大
- 步骤
  - 把大表和小表按照相同的key进行分区，让**hash值一样的数据**分发到同一个分区，每个分区进行局部的哈希连接。

**Broadcast Nested Loop Join**

- 非等值连接：没有任何连接条件的场景
- 将小表 广播到每个节点，然后每个节点执行嵌套循环连接，广播的表再与大表逐一对比

**Cartesian Join**

- join的时候没有 where条件 (on)，笛卡尔积 join

## Spark小文件问题

1、coalesce()或 repartition() 减少分区

```
val rdd2 = rdd1.coalesce(8, shuffle = true)
val rdd3 = rdd1.repartition(8)
```

2、调整 `spark.sql.shuffle.partitions`

设置shuffle之后的分区数，只对宽依赖有效

## Spark性能调优

**数据输入于存储**

- 文件格式 Parquet优于ORC
- 小文件 用repartition/coalesce合并输出

**Spark SQL优化**

- 慎用Select *
- 谓词下推
- 减少不必要的collect() ，collect会把数据拉回driver，oom

**Shuffle优化**

- 分区数

   `spark.sql.shuffle.partitions`

- Spill 过多

  提高内存的buffer

  `spark.reducer.maxSizeInFlight`

- 合并小文件

  `coalesce()`控制partition数量

- Shuffle压缩

  开启压缩参数 `spark.shuffle.compress=true`

**Join优化**

- 大表JOIN小表：小表声明为`broadcast`变量。把小表放到每个节点，再放到hash表。JOIN的时候直接查hash不需要shuffle了
- 大表JOIN： 通过HASH分区使两个RDD拥有相同的分区

**数据倾斜优化**

- 倾斜的key就几个情况
- 提高shuffle并行度
- 两段聚合
- 两个数据量都很大的情况
- join操作的RDD还是有大量key倾斜，加随机前缀大散，再给另一个RDD扩容n倍打上0-n的前缀。

**持久化&缓存优化**

频繁复用的数据要`cache()` / `persist(StorageLevel.MEMORY_AND_DISK)`

不用之后要`unpersist()`释放内存

**参数调优**

`spark.executor.memory`

`spark.executor.cores`

`spark.sql.shuffle.partitions`

`spark.driver.maxResultSize`

## Spark内存模型

![img](https://github.com/MoRan1607/BigDataGuide/raw/master/Spark/%E9%9D%A2%E8%AF%95%E9%A2%98/Spark%E7%9A%84%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B/202304081142714.png)

**堆内内存(Executor)**

JVM内存

- execution：计算过程中临时数据，例如 shuffle，join，sort

- storage：缓存数据，例如RDD缓存，广播等

- 用户内存：定义的变量和对象等

- 预留内存：防止OOM，默认300M

  ```
  –executor-memory 或 spark.executor.memory 设置
  Executor中Task共享JVM内存，当这些任务缓存在RDD或者广播时数据占用的内存被划分为storage内存，在执行shuffle操作时数据占用的内存被划分为execution内存
  ```

**堆外内存**

把内存对象分配在JVM的堆以外的内存

```
系统内存，开启需要参数
spark.memory.offHeap.enabled=true
spark.memory.offHeap.size=512m
```

**动态占用机制**

Storage内存和Execution内存共享内存。

```
spark.memory.fraction = 0.6   Executor 内存中用于 **统一内存池** 的比例
spark.memory.storageFraction = 0.5  统一内存池中分配给 Storage 的初始比例，剩下的是 Execution
```

`Execution 优先级高，Storage 优先级低`。Execution 可以抢回空间，Storage 不行。

- 双方的空间都不足时，则存储到硬盘；若己方空间不足而对方空余时，可借用对方的空间;
- Execution 的内存空间被 Storage 占了： Storage **把缓存数据写到磁盘（落盘）**，释放内存归还给Execution
- Storage 的内存空间被Execution 占了：无法抢占回来

## Reference

https://zhuanlan.zhihu.com/p/102544207
https://juejin.cn/post/7052321931625758757?searchId=20240506161817447254DE1438E4FCE872

https://www.cnblogs.com/liugp/p/16122904.html

https://xie.infoq.cn/article/71e6677d03b59ce7aa5eec22a

![c954b8ee79ab4bec9f5cb396d640193c.png](https://ucc.alicdn.com/pic/developer-ecology/o46gtualwok5w_9be992bb23ad4e9ab6160930dffd656d.png?x-oss-process=image%2Fresize%2Cw_1400%2Fformat%2Cwebp)
