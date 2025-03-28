## Spark vs MR 组件

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e0c96476a819418686b9b46baf7951c7~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

## Spark和MR并行计算

- 并行计算

  MR：MR的一个作业称为Job，Job 分为map task和reduce task。每个task在自己的进程中运行。

  Spark：Spark把任务成为application，一个application对应一个spark content，每触发一次action就会产生一个job。application中存在多个job，每个job有很多stage（stage是shuffle中DGASchaduler通过RDD间的依赖关系划分job而来的）每个stage里有多个task（taskset）。taskset由taskshedulaer分发到各个executor中执行。
  
- 内存使用

  MR过程中重复读写HDFS，大量IO操作

  Spark二点迭代计算在内存中进行，API提供了大量RDD操作（join，group by），DAG图实现良好容错性

## Spark SQL 为什么比 HIVE 快

1. 内存和磁盘刷写

   **HIVE 基于磁盘，Spark SQL 使用内存计算作为核心计算机引擎**

   MR的shuffle涉及大量数据传输，网络和磁盘I/O造成负担。数据需要经过环形缓冲区溢写，需要按key进行排序以便键相同的数据可以发送到同一个reduce。

   Spark的DAG可以减少Shuffle次数。如果计算不涉及其他节点数据交换，Spark可以在内存中一次性完成这些操作，只在最后结果落盘。如果涉及数据交换，Shuffle阶段数据还要写磁盘。(Executor 中有一个 `BlockManager`存储模块：将内存和磁盘共同作为存储设备，多轮迭代计算时，中间结果直接存储到这里。减少IO开销。)

2. 进程和线程

   MR基于**进程**级别，每个任务运行在单独的进程。Mapper和Reducer任务执行过程中可能涉及多个线程处理输入数据,执行计算和IO操作

   Spark基于**线程**级别，由执行器（Executor）中的线程池处理。每个 Executor 可以运行多个任务，每个任务由一个或多个线程处理，共享 Executor 内的内存。

3. Spark Shuffle 优化

   MR每次Shuffle 都需要分区快排，Spill的多个临时文件归并排序，reduce拉取map的输出后归并排序成一个文件

   Spark Shuffle 基于Hash将相同的key写入同一个内存缓冲区/磁盘文件；或者预排序-批量处理-合并大文件+索引记录 ，不需要多次排序加快速度

3. Spark持久化缓存机制

   Spark将需要`复用的数据`缓存在内存中，下次再使用此RDD时直接从缓存获取，显著提高Spark运行的速度。

   MR每个Shuffle都需要写入磁盘，增加了读写延迟

4. 数据格式和序列化

   Spark使用数据序列化格式，例如Parquet

   MR默认使用文本格式，传输和解析开销大

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

## Applicaiton、Job、Stage、Task之间的关系

![Application组成](https://i-blog.csdnimg.cn/blog_migrate/54b32b3de899f513f172d2e14ff56d93.png)

`applicaiton` 完整的Spark应用程序

`job`一个Applicaiton会调用多个Job，Job间串行执行

`stage`DAG scheduler划分Stage，会把Job作业划分成多个stage。依据是shuffle依赖。一个Stage包含很多task，依据是最后一个分区数。

`task` 并行执行，负责对一个分区进行计算操作

## Spark 工作机制

1. **构建运行环境** 

   - Client提交Spark任务，Driver创建一个Context对象，负责与Cluster Manager通信以及资源申请、任务分配和监控。

   - Executor注册：**Content向资源管理器注册申请运行Executor进程**，Executor运行情况随着心跳发送到资源管理器上。

2. **任务划分和任务调度**

   - `RDD`创建和转换：Context 会基于输入数据或已有数据集创建 RDD
   - `DAG`构建：Spark根据RDD依赖关系构建DAG，
   - `DAG Scheduler`划分Stage：Spark通过分析DAG将作业划分成多个Stage。每个stage包换了一组可以并行执行的任务集。
   - `Task Scheduler`任务调度：`DAG Scheduler`根据stage的划分把任务发送到Task Scheduler，`Task Scheduler`把任务调度到 executors上。

3. **数据分区**

   输入数据被划分成物理分区。RDD封装分区，在节点间并行处理

4. **任务执行**

   `Executor`执行Task Scheduler分配的任务

   - 一个`Executor`进程有很多Task线程，Task对应RDD的操作

5. **数据流动**

   - `Shuffle`：转换操作需要跨分区聚合数据

6. **任务完成返回结果到Driver**

7. **容错和恢复**

   ​	`RDD 的血统信息（lineage）` 提供容错能力，任何节点失败可以重新计算

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

RDD分布式弹性数据集本身不存储数据，作为数据访问的一种虚拟结构。所有算子都基于RDD执行，`不可变，可分区，里面的元素可以并行计算`。RDD执行过程中会生成DAG图。 （物理角度看RDD存储的是block和node之间的映射）

- 弹性：数据可以保存在内存或者磁盘，数据优先内存存储，计算节点内存不够时可以把数据刷到磁盘等外部存储。
- 分布式：对内部的元素进行分布式存储。RDD本质可以看成只读的，可分区的分布式数据集。
- 数据集：一个RDD就是一个分布式对象集合，本质上是一个只读的分区记录集合
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

**宽依赖：** 每个子RDD分区依赖父RDD的全部分区

![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171657817-1513169131.png)

**窄依赖：** 每个子RDD依赖父RDD的同一个分区

窄依赖允许子RDD的每个分区可以被并行处理，而且支持在一个节点上链式执行多条指令，无需等待其他父RDD的分区操作

![img](https://img2022.cnblogs.com/blog/1601821/202204/1601821-20220416171650540-1125656506.png)

### Mapper和Reducer相当于什么算子

Map() 和 reduceByKey()

### 算子的区别

#### groupByKey、reduceByKey、aggreageByKey

- groupByKey  对数据集中的元素按键分组，但不进行任何聚合计算。它会生成一个（键，值序列）的数据集。
- reduceByKey 按键合并数据集的值。对partition中数据根据不同key进行aggregate，再shuffle
- aggreageByKey 

#### cache、presist

cache：默认将数据集存储在内存中

persist：允许用户选择存储级别（例如，内存、磁盘或两者的组合）。

#### repartition、coalesce

repartition 重新分配数据，以改变RDD分区数量。需要全局shuffle。调整并行度，调整数据的分区分布

coalesce  减少RDD的分区数，它尽可能避免进行全局数据洗牌，尽量在本地合并分区，可能会不均匀

#### map、flatMap

map 将函数用于RDD中每个元素，返回值构成新的RDD。例如，将每个数字乘以2。

flatMap 与 `map` 类似，但每个输入元素可以映射到0或多个输出元素。例如，将句子分解为单词

## RDD、DataFrame、DataSet

都是Spark的弹性分布式数据集

RDD：容错的、不可变的分布式数据集合

DataFrame：在RDD基础之上的高级抽象。数据表格形式组长

DataSet：是 DataFrame API 的一个扩展

## Spark Shuffle

Spark Shuffle 是指当 Spark 执行**宽依赖**操作（如 `reduceByKey`、`groupByKey`、`join` 等）时，需要跨分区传输数据的过程。这通常涉及到在不同节点之间交换数据，以确保相同的键（key）聚集在一起进行计算。

### Hash Shuffle

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

### Sort Shuffle

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

## Spark的优化策略

**延迟执行** Spark遇到转换操作时，会先记录下来，直到遇到行动操作时执行。可以把多个转换操作合并成一个任务

**分区执行** Spark把数据划分成很多分区，分配给集群的不同节点并行运行

**内存管理** Spark把数据优先存储在内存中，减少磁盘IO。同时还使用数据序列化和内存缓存提高内存利用率和传输效率

**任务调度**  Spark使用任务调度器把任务分配给集群中不同节点运行

**部分聚合** Spark可以在分区做部分聚合，再做全局聚合。例如Sort的shffule

**广播变量** 集群的所有节点共享一个较小的只读变量时，广播变量减少数据传输和复制开销

## Spark容错机制

**数据容错：RDD的血统** 信息可以重新吉首丢失的数据分区

**任务容错：任务调度器容错** 可以重新调度发生错误的任务，并且分配给其他可用节点运行

**数据丢失容错：数据持久化** 把数据缓存到磁盘上，当节点发送故障，可以用持久化存储中恢复

**节点容错：主从架构**。主节点（Driver）协调任务执行，如果挂了，可以重启一个新的主节点容错

## Parquet文件存储

列式存储，把同一列的数据存储在一起。查询时只需要读取需要的列

同一列的数据存储在一起，有利于压缩算法和编码。减少存储空间降低磁盘IO

支持多种压缩算法和编码方式

存储了数据的模式信息，列名、数据类型等。Spark在查询时可以自动推断数据的模式

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

## Spark的持久化(persist&缓存机制(cache)

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

## Spark调优

Shuffle优化

Join优化

- 大表JOIN小表：小表声明为broadcast变量。把小表放到每个节点，再放到hash表。JOIN的时候直接查hash不需要shuffle了
- 大表JOIN： 通过HASH分区使两个RDD拥有相同的分区

数据倾斜优化

- 倾斜的key就几个情况
- 提高shuffle并行度
- 两段聚合
- 两个数据量都很大的情况
- join操作的RDD还是有大量key倾斜，加随机前缀大散，再给另一个RDD扩容n倍打上0-n的前缀。

****

### SPARK Architecture

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

## Reference

https://zhuanlan.zhihu.com/p/102544207
https://juejin.cn/post/7052321931625758757?searchId=20240506161817447254DE1438E4FCE872

https://www.cnblogs.com/liugp/p/16122904.html

https://xie.infoq.cn/article/71e6677d03b59ce7aa5eec22a
