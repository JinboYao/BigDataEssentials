https://juejin.cn/post/7011365385098231816

# Hadoop基础

### Hadoop的运行模式

1. 单机版：不需要配置`hdfs-site.xml`或`mapred-site.xml`文件，因为Hadoop默认在本地文件系统而非HDFS上操作。
2. 伪分布模式： 所有的Hadoop进程以独立的java进程形式存在，类似于完全的分布式环境。（如 NameNode、DataNode、ResourceManager、NodeManager 等）
3. 完全分布模式：hadoop 运行在多台机器组成的集群上面。

### Hadoop集群中启动的进程

1. NameNode：hadoop中的主服务器，管理文件系统名称空间和对集群中存储的文件的访问，保存有metadate
2. SecondaryNameNode：提供周期检查点和清理任务。帮助NN合并editslog，减少NN启动时间。
3. DataNode：管理连接到节点的存储（一个集群中可以有多个节点）。每个存储数据的节点运行一个datanode守护进程。
4. ResourceManagerr（JobTracker）：JobTracker负责调度DataNode上的工作。每个DataNode有一个TaskTracker，它们执行实际工作。
5. NodeManager（TaskTracker）：执行任务
6. DFSZKFailoverController
7. JournalNode

### Hadoop序列化和反序列化

- 序列化：将对象序列化为字节流，以便通过网络传输或存储到 HDFS。

- 反序列化：将字节流转换回对象，以便在计算过程中使用。例如**MapReduce**分布式计算

Hadoop 使用 `Writable` 接口作为其序列化框架的核心。

`Writable` 接口定义了两个核心方法：

- `void write(DataOutput out)`：将对象的字段写入字节流。
- `void readFields(DataInput in)`：从字节流中读取字段并赋值给对象。

### ☆MapReduce跑得慢的原因

Mapreduce 程序效率的瓶颈在于两点：
1）硬件资源限制

- **磁盘I/O速度**
- **网络带宽**：在shuffle阶段需要较高的带宽
- **内存资源**

2）计算过程瓶颈

1. **数据倾斜** 数据分布不均匀，数据倾斜导致某个Reducer运行时间长
2. **小文件过多**  每个文件都需要启动一个Map，大量小文件导致很大map启动和文件读取开销
3. **Map和reduce数设置不合理**  Map数设置过小，单个节点资源未能充分利用，任务失败风险更大；Map数设置过大，资源碎片化，启动JVM进程的开销大
4. **大量的不可分块的超大文件** 会导致单个Map需要计算的数据量变大，运行时间变长
5. **spill次数过多** 频繁spill增加IO开销
6. **merge次数过多** shuffle阶段需要merge，频繁merge增加CPU、IO负担
7. **Map设置的运行时间太长，Reduce等待过久**  Reduce任务在开始处理之前需要等待所有Map任务完成

### ☆MapReduce优化方法

**数据输入**

1. 合并小文件 ：执行mr任务前小文件合并

2. 采用combinFileInputFormat来作为输入，解决输入端大量小文件场景

**map阶段**

1. 减少spill次数：增大触发spill的内存上限，减少spill次数，从而减少磁盘 IO
2. 减少merge次数：增大merge的文件数目，减少merge参数，缩短mr处理时间
3. 在map之后先进行combine处理，减少IO

**reduce阶段**

（1）合理设置map和reduce数

（2）设置map、reduce共存：

**IO传输**

（1）采用数据压缩的方式，减少网络IO的时间。安装Snappy和LZOP压缩编码器。
（2）使用SequenceFile二进制文件

**数据倾斜问题**

收集倾斜数据： 在reduce方法中加入记录map输出键的详细情况的功能。

（1）抽样和范围分区：可以通过对原始数据进行抽样得到的结果集来预设分区边界值。

（2）自定义分区：另一个抽样和范围分区的替代方案是基于输出键的背景知识进行自定义分区。例如，如果map输出键的单词来源于一本书。其中大部分必然是省略词（stopword）。那么就可以将自定义分区将这部分省略词发送给固定的一部分reduce实例。而将其他的都发送

（3）Combine：使用Combine可以大量地减小数据频率倾斜和数据大小倾斜。给剩余的reduce实例。

### ☆HDFS小文件优化方法

**原因:**

数据源上传时包含很多小文件

reduce数量越多，包含的小文件就越多。（每个 Reduce 任务一个输出文件）

**HDFS小文件弊端**

1. 小文件启动多个map，一个map需要开启一个jvm

2. HDFS上每个文件都要在NameNode上建立一个索引，索引大小大约150byte.当小文件多的时候，就会产生很多的索引文件，一方面会大量占用namenode的内存空间，另一方面就是索引文件过大使的索引速度变慢

**解决方案**

（1）Hadoop Archive
    是一个高效地将小文件放入HDFS块中的文件存档工具，它能够将多个小文件打包成一个HAR文件，这样在减少namenode内存使用的同时。
（2）Sequence file
    sequence file由一系列的二进制key/value组成，如果为key小文件名，value为文件内容，则可以将大批小文件合并成一个大文件。

（3） `setNumReduceTasks()`合理设置Reduce数量

（4）CombineFileInputFormat
    CombineFileInputFormat是一种新的inputformat，用于将多个文件合并成一个单独的split，另外，它会考虑数据的存储位置

## HDFS(分布式文件存储系统)

### HDFS架构

![在这里插入图片描述](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7614738a4ec5464eabc8535b244df28a~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

HDFS是主从架构

`NAMENODE`  维护HDFS树，存储文件系统树中所有的文件和文件路径的元数据信息（文件名，命令空间，文件属性，文件数据块和DATANODE的映射关系）

`secondaryNameNode`  1、备份数据镜像 2、定期合并日志与镜像

`client`  文件切分；和NAMENODE 交互，获取文件位置；和datanode交互，存储数据

`DATANODE`  本地系统存储文件的块数据

### 写流程

![在这里插入图片描述](https://img-blog.csdnimg.cn/714f20edc4eb4ee299a8ef470790383f.png)

1、客户端向namenode发起请求，获取元数据信息（块命名空间，映射信息，DATANODE的位置）

2、客户端获取元数据信息，在DATANODE 写数据

3、client对文件进行切分成block。请求第一块block，namenode收到后返回三台存放数据副本的datanode服务器。client接收到后根据网络拓扑原理（就近原则）找到其中一台进行传输通道建立，然后再与其他两台datanode建立通道串行连接，节约client的io压力

4、datanode定期向namenode发送心跳信息，报告自身状态

### 读流程

![在这里插入图片描述](https://img-blog.csdnimg.cn/24d99c6da208466e9af71cb0c7b1a1e2.png)

1、client向namenode发送读请求，namenode收到请求后进行请求路径和用户权限校验。校验完成后返回目标文件的元数据信息，包含datanode位置信息与文件的数据块
2、客户端根据元数据信息根据网络拓扑原理和就近原则，发送读请求给datanode
3、datanode收到读请求后，通过HDFS的FSinoutstream将数据读取到本地，然后进行下一个数据块的读取，直到文件的所有block读取完成。

### Secondary NN 工作机制

**NN启动**

客户端对元数据有增删改操作

NN记录操作日志，更新滚动日志

NN在内存中对数据进行增删改

**SNN 工作**

- SNN请求执行Checkpoint

- NN滚动正在写的edit日志

- 拷贝日志和镜像文件到SNN

- SNN 把日志和镜像文件放入内存进行合并

- 生产新的镜像文件fsimage.checkpoint

- 拷贝fsimage.checkpoint到NN，NN重新命名为fsimage

### 文件管理的容错机制

HDFS写入时，把文件分隔成block，每个block的3个副本存储在三个集群机器上。

```
■ 第一副本：放置在上传文件的 DataNode上;如果是集群外提交，则随机挑选一台磁盘不太慢、CPU不太忙的节点
■ 第二副本：放置在与第一个副本不同的机架的节点上
■ 第三副本：与第二个副本相同机架的不同节点上。
```

### HDFS的数据压缩算法

**bzip2、gzip、lzo、snappy**

### HDFS block大小,过大或者过小的影响

Block块默认128M，依据是`最佳传输损耗理论` 寻址时间占总传输时间1%

一般磁盘寻址时间10ms，传输速率100m/s。

```
10ms* 100 * 100m/s =100M
```

- Block设置过大

  磁盘的传输时间会更明显大于寻址时间，程序处理这块数据时会很慢

  MR的map任务一次只处理一个块的数据，运行时间很慢

- Block设置过小

  大量小文件增加NameNode的元数据存储

  可能增加碎片化

  寻址时间增加

## MapReduce(分布式计算架构)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201210185505552.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01heV9KX09sZGh1,size_16,color_FFFFFF,t_70#pic_center#pic_center)

1. Map阶段

   ```
   输入文件，InputFormat数据读取
   split分片：根据HDFS的block块大小分片，输出<key-键值对，value-每一行的内容>形式。假设HDFS大小为128M，文件是128*10M，Mapreduce就会分为10个MapTask。
   map处理每一行的内容，分别计算 key，value的list
   ```

   (优化) `combine`合并：combiner 合并每个map task的重复key值

2. shuffing

   **Map端Shuffle**

   `Partition`：map（）函数输出的<k,v>list，先根据reducetask的数量使用**HASH算法**生成对应的分区

   `buffer`：写入入环形缓冲区（默认大小100M）

   `Spill`溢写

   - `Sort`(分区内排序)：根据key值对分区中的数据使用快速排序算法进行排序，每个分区内的数据是有序的
   - (选择)`combiner`合并：combiner 重复key值，value相加
   - 生成溢写文件：环形缓冲区到达一定阈值（80%），数据就会溢出到本地磁盘文件，生成一个临时文件

   `merge`合并：多个临时文件`归并排序`生成最终的一个**分区且已排序**的大文件(排序+合并)

   **Reduce 端 Shuffle**

   `Copy`复制：Reduce会拉取同一个分区内的各个MapTask结果放在内存，放不下就溢写到磁盘

   `merge`合并：在远程拷贝数据的同时，ReduceTask启动了两个后台线程对内存和磁盘上的文件进行合并

   `Sort`排序：对所有数据进行一次`merge归并排序`（这样就可以满足将key相同的数据聚在一起）

3. Reduce

   ```
   reduce从合并的文件中取出一个一个的键值对group，调用定义的reduce方法（），生成最终的输出文件。完成后output到HDFS中
   ```

### MapReduce三次排序

Map端`spill`之前：根据Reduce的数量进行分区，然后根据数据的Hash取模写入。每个分区内进行`快排`

Map端`merge`: 		 多个临时文件`归并排序`生成最终的一个**分区且已排序**的大文件

Reduce端`merge`：  Map端的所有数据进行`归并排序`，将key值进行排序

### MapReduce的Combine

对MapTask的输出做一个重复key值的合并操作（{key，[V1,V2]}）,局部汇总减少网络传输。

### MapTask和ReduceTask的数量

1. map数量：数据切分成的block块数量决定

   ```
   如果需要调整分片大小，可以通过以下参数：
   mapred ，uce.input.fileinputformat.split.maxsize：设置最大分片大小。
   mapreduce.input.fileinputformat.split.minsize：设置最小分片大小。
   ```

2. reduce 数量：Reduce任务的数量。job.setNumReduceTasks(int)设置，默认为1

3. mapTask数量：一个job的map阶段MapTask并行度（个数），由客户端提交job时的切片个数决定

4. reduceTask 数量：实际运行的 Reduce 任务的数量。如果集群资源不足，ReduceTask 可能会分批运行。（如果 Reduce 数量为 4，但集群只能同时运行 2 个 ReduceTask，则 ReduceTask 会分两批运行）

### MapReduce容错

1）MRAppMaster容错性
  一旦运行失败，由YARN的ResourceManager负责重新启动，最多重启次数可由用户设置，默认是2次。一旦超过最高重启次数，则作业运行失败。

2）Map Task/Reduce Task 

   Task周期性向MRAppMaster汇报心跳；一旦Task挂掉，则MRAppMaster将为之重新申请资源，并运行之。最多重新运行次数可由用户设置，默认4次。

## YARN

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/f39beb0b7a0ed71a88faf203f459d507.png)

ResourceManager：整个集群所有资源的管理者。`作用`:处理客户端请求、监控NodeManager、启动或监控ApplicationMaster、资源的分配与调度。

NodeManager：单个节点服务器资源管理者。`作用：`管理单个节点上的资源、处理来自ResourceManager的命令、处理来自ApplicationMaster的命令。

ApplicationMaster：单个任务运行的老大，任务在Container内运行客户端提交一个job，就会产生一个ApplicationMaster。`作用：`为应用程序申请资源并分配给内部的任务、任务的监控与容错。

Container：对任务运行环境的抽象，虚拟化技术容器，相当于一台独立的服务器，里面封装了任务运行所需要的资源，如内存、cpu、磁盘、网络等。`好处：`任务运行完直接释放。

### Hadoop中的主要调度器

1. **FIFO调度器（First In First Out Scheduler）**

   按照作业提交的顺序来调度作业，适用于作业大小相对一致且不需要多级队列或优先级调度的简单环境。

2. 容量调度器

   可以将集群的容量分割成多个队列，每个队列有一定的容量保证。

   适合多部门或多项目共享集群资源的场景，可以保证资源按需公平分配。

3. 公平调度器

   以公平的方式分配资源，确保所有运行的作业获得相等的资源

   适合需要高度公平性，保证无作业饥饿的环境

## HIVE

### HIVE底层原理

![在这里插入图片描述](https://img-blog.csdnimg.cn/5a5255259afc4096be4894b1dec1ccc1.png)

***Meta store：***hive借用关系型数据库（MySQL）存储hive中表的元数据信息。表结构，分区信息，列类型

**HIVE查询流程**：

1. HIVE CLI/WEB 提交HIVEQL
2. 解释器：转化成MR任务。这些任务用于在Hadoop集群上处理数据。
3. 编译器：根据元数据信息生成查询计划。提交给YARN分配资源
4. Hadoop执行生成的任务，计算HDFS的数据，结果写回HDFS

### HIVE SQL编译原理

1. 词法、语法解析：根据SQL 的语法规则，完成 SQL 词法，语法解析，将 SQL 转化为抽象语法树 AST Tree；
2. 语义解析：遍历 AST Tree，抽象出查询的基本组成单元 QueryBlock；
3. 生成逻辑执行计划：遍历 QueryBlock，翻译为执行操作树 OperatorTree；
4. 优化逻辑执行计划：逻辑层`优化器`进行 OperatorTree 变换，合并 Operator，达到减少 MapReduce Job，减少数据传输及 shuffle 数据量；
5. 生成物理执行计划：遍历 OperatorTree，翻译为 MapReduce 任务；
6. 优化物理执行计划：物理层优化器进行 MapReduce 任务的变换，生成最终的执行计划

### UDF、UDAF、UDTF

UDF 对单个输入值返回单个结果

UDAF 处理多行数据返回单个聚合值，类似 sum()，avg()

UDTF 可以从单个输入行生成多个输出行的函数。可以返回包含多列和多行的表格结果。例如爆炸函数

**执行阶段**

| 自定义函数 |                                                              | 执行阶段                      |
| ---------- | ------------------------------------------------------------ | ----------------------------- |
| UDF        | 单个输入值返回单个结果，例如Upper()                          | map() 端 转换功能             |
| UDAF       | 处理多行数据返回单个聚合值，类似 sum()，avg()                | reduce阶段或者 reduce&map阶段 |
| UDTF       | 单个输入行生成多个输出行的函数。<br />可以返回包含多列和多行的表格结果。例如爆炸函数 | map端                         |

**如何自定义**

UDF

```
* 继承org.apache.hadoop.hive.ql.UDF函数；
* 重写evaluate方法，evaluate方法支持重载。
```

UDAF

```
* 继承org.apache.hadoop.hive.ql.exec.UDAF类，并实现一个内部的UDAFEvaluator接口
* 实现 Evaluator 方法
    init():   类似于构造函数，用于UDAF的初始化
    iterate():接收传入的参数，并进行内部的轮转，返回boolean
    terminatePartial():无参数，其为iterate函数轮转结束后，返回轮转数据，类似于hadoop的Combiner
    merge():  接收terminatePartial的返回结果，进行数据merge操作，其返回类型为boolean
    terminate():返回最终的聚集函数结果
```

UDTF

```
* 继承org.apache.hadoop.hive.ql.udf.generic.GenericUDTF类
* 重写initialize(), process() 和 close() 方法
    initialize() 返回UDTF的返回行的信息（返回个数，类型）
    process() 处理函数
    close() 清理
```

### HIVE group by的MR实现

**MAP ** ：将group by的字段组合作为一个key，如果group by单个字段，那么key就一个。将group by之后要进行的聚合操作字段作为value

- 如果要进行count，则value是赋1

- 如要sum另一个字段，那么value就是该字段。

**Shuffle 分区与排序** ：根据Key被分配到不同的Reducer。

**Reduce **数据聚合

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/3cacabf4336e0c9004822dfa02991da4.png)

### HIVE distinct的MR实现

**Map**

* **键值对生成** ：`整行数据/指定的字段`  作为Key，Value为常量或者空值。
* **局部去重** ：可以选择性地进行一个局部聚合过程（Combine过程），仅保留具有相同键的唯一记录。

**Shuffle** 把key相同的值聚集在一起

**Reduce**

* **全局去重** ：Reducer接收到分组好的键值对后，每个组只需要输出一次键值对（因为相同键的所有值已经在Shuffle阶段被聚集到一起）。

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/cbdfe1d159f2639b874b6c3d1d968669.png)

### HIVE join的MR实现

map：map阶段生成键值对。key是join的列，value是 <表tag，数据>

reduce：根据key值完成join操作，通过tag值识别不同表数据组合一下

![img](https://imgconvert.csdnimg.cn/aHR0cDovL2ltZy5seHcxMjM0LmNvbS8wNjI1LTEuanBn?x-oss-process=image/format,png#pic_center)

### Map join原理

**场景：** 小表join大表

**参数：** `hive.auto.convert.join =true`

**执行流程：**

1. 通过MaoReduce  Local Task 将小表存入内存，生成HashTable 文件，上传到cache中
2. MR Job在Map阶段，每个Map从Cache中读取HashTableFile到内存，在Map阶段直接Join然后传递给下一个MR任务。

### TOP N的MR实现

Map：维护一个TopN的记录，Key 是需要查看的键，value是需要排序的数据

Reduce：收到所有的局部TopN记录，Reducer 再从这些记录中计算最终的全局 Top N。

### HIVE动态分区和静态分区

**动态分区：**Hive根据插入数据的值自动创建所需的分区目录

```sql
SET hive.exec.dynamic.partition=true;
SET hive.exec.dynamic.partition.mode=nonstrict;
```

**静态分区：**用户在插入数据时需要明确指定每个分区的路径和名称

使用：

```
对于频繁变更分区键值的表，优先考虑动态分区。对于分区键值稳定，数据更新不频繁的情况，可以使用静态分区以提高数据管理的效率。
```

### 分区表、分桶表、内部表、外部表

分区表：所有文件存储在一个HDFS文件夹里面，根据字段分区（PARTITIONED BY ）。每个分区是一个文件系统的目录

分桶表：按照hash值把数据分散到多个文件（桶）中。（数据加载到桶表时，会对字段取hash值，然后与桶的数量取模。把数据放到对应的文件中。物理上，每个桶就是表(或分区）目录里的一个文件，一个作业产生的桶(输出文件)和reduce任务个数相同。）

内部表：创建表时，会将数据移动到数据仓库指向的路径

外部表(EXTERNAL_TABLE)：创建表在指定的目录位置，hive存储元数据。

### HIVE的数据存储格式

| 存储格式         | 描述                           | 优点                             | 缺点                         |
| ---------------- | ------------------------------ | -------------------------------- | ---------------------------- |
| **TextFile**     | 默认的纯文本格式。             | 易于使用和理解。                 | 效率低，不支持压缩。         |
| **SequenceFile** | 二进制格式，支持分块和压缩。   | 更紧凑，提高 I/O 性能。          | 不易直接阅读。               |
| **RCFile**       | 列存储格式，优化列查询。       | 提高列查询效率，支持压缩。       | 文件查看不便。               |
| **ORC**          | 优化的行列式存储格式。         | 高效压缩，查询快，支持ACID事务。 | 创建和维护成本相对较高。     |
| **Parquet**      | 流行的列式存储格式。           | 与多种数据处理工具兼容性好。     | Hive集成程度低于ORC。        |
| **Avro**         | 支持丰富数据结构的序列化格式。 | 适用于数据交换，支持schema变更。 | 性能可能不如专门的Hive格式。 |

### Sort By，Order By，Cluster By，Distrbute By

Sort by 全局排序，只有一个reducer

order by 不是全局排序，数据在进入reduce之前完成排序

distrbute by 按照指定的字段对数据进行划分输出到不同的reduce

cluster by  数据划分到不同reduce中

### Fetch抓取和本地模式

- Fetch抓取是指，Hive中对某些情况的查询可以不必使用MapReduce计算。例如：SELECT * FROM employees;在这种情况下，Hive可以简单地读取employee对应的存储目录下的文件，然后输出查询结果到控制台。

- 查询触发执行任务时消耗可能会比实际job的执行时间要多的多，Hive可以通过本地模式在单台机器上处理所有的任务。

### ☆Hive数据倾斜

**原因：**

1. key值分布不均匀,导致某些reduce需要处理的数据量大，有些处理的数据量小，分布不均匀。可能是mapreduce中某些键值对出现频率非常高。触发**Shuffle**动作，**所有相同key的值就会拉到一个或几个节点上，就容易发生单个节点处理数据量爆增的情况。**
2. 业务数据本身分布不均
3. 建表时考虑不周，
4. 某些SQL语句倾斜，笛卡尔积或者小文件过多

**定位：** 任务执行过程中 卡在99%

**解决方法：**

**SQL语句调节**

1.空值导致

- null值在shuffle阶段进入一个reduce中，产生数据倾斜
- 解决方案：1、去掉null值 2、对null值进行随机赋值

2.表关联主键不唯一笛卡尔积

- 开窗函数取唯一主键，或者去重复

3.不同数据类型

- 表关联时主键数据类型不一致，导致数据倾斜。例如，int类型和string类型关联，默认的hash按照int的id分配，所有的string就会分配给同一个id，进入同一个reduce。
- 解决方案：int类型转换成string类型。

4.表关联（大小表）

- 数据量小、key值分布均匀的表在左边
- map join ：让小的维度表（1000条以下的记录条数）先进内存。在map端完成reduce

5.group by时数据分布不均（聚合场景）

    两段聚合（join情况下不适合）：

- 局部聚合时，给每个key加上随机前缀，可以在多个task上面聚合
- 去除前缀做全局聚合

![](https://i-blog.csdnimg.cn/blog_migrate/81d82770952cdeb64c9dc4be68af47b4.png)

6.count distinct去重时数据分布不均

- 空值过多的情况，把空值与非空值拆分单独计算
- 使用 sum group by替换

7.行列过滤

```sql
   select时使用分区过滤；join时先where过滤再关联
```

**参数调节**

```
设置hive.map.aggr=true  开启map端的部分聚合供你，把key相同的聚合在一起
设置hive.groupby.skewindata=true 负载均衡
```

有数据倾斜的时候进行负载均衡，当选项设定位true,生成的查询计划会有两个MR Job。

第一个MR Job(分散数据)，Map的输出结果集合会随机分布到Reduce中，每个Reduce做部分聚合操作并输出结果，将倾斜的键分散到多个Reducer中，从而达到负载均衡的目的；

第二个MR Job（最终聚合）再根据预处理的数据结果按照Group By Key 分布到 Reduce 中（这个过程可以保证相同的 Group By Key 被分布到同一个Reduce中），最后完成最终的聚合操作。

### SQL运行很慢的原因

1、数据倾斜

2、MR作业开销高

```
Hive底层依赖MapReduce来执行查询。当查询较复杂时，生成的MapReduce任务数量会很多，而MapReduce的启动和任务调度存在较高的开销，尤其是在小规模数据集上的查询，也会影响执行速度。
```

3、未使用分区

4、未使用合适的分桶

```
分桶可以将数据按照某个字段进一步细分，提高JOIN和GROUP BY操作的效率。如果查询涉及的字段没有进行分桶设计，查询时可能会需要对全表数据进行扫描。

```

5、小文件占用开销

6、未使用合适的文件格式

```
Hive支持多种文件格式，如TextFile、ORC、Parquet等。使用不适合的文件格式（如TextFile）会增加I/O开销，导致查询变慢。使用ORC或Parquet等压缩格式，可以大幅减少存储空间并提高查询性能。
```

### ☆HIVE 调优

**表设计优化**

- 分区  常用过滤字段做分区
- 分桶  JOIN/Group by多的字段，支持bucket map join
- 存储格式 ORC/Parquet 优于txt
- 小文件 控制小文件（输入前手动合并小文件）

**SQL优化**

- 分区裁剪和列裁剪：减少数据范围
- 大小表关联：map join
- 大表关联：设计分桶策略，避免数据倾斜
- 关联条件统一：字段类型统一，尽量减少笛卡尔积
- group by 代替dinstinct
- sort by代替order by
- 避免子查询
- union all 替代union（去重需要遍历、排序）

**作业执行层优化**

- 合理设置Map/Reduce个数

  `set mapreduce.job.reduces=200;`

- 小文件合并

  `set hive.merge.mapfiles=true;`

- 动态分区

  ```
  hive.exec.dynamic.partition.mode
  ```

   `nonstrict` 支持动态分区

**数据倾斜处理**

- 随机前缀
- Map Join
- 热点数据单独运行

## 手撕MapReduce程序

### 统计单词的个数

```java
public class WcMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		String[] words = line.split(" ");
		for (String word : words) {
			context.write(new Text(word), new IntWritable(1));
		}
	}
}
public class WcReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
	@Override
	protected void reduce(Text key, Iterable<IntWritable> values,
			Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
		int count = 0;
		for (IntWritable value : values) {
			count += value.get();
		}
		context.write(key, new IntWritable(count));
		
	}
}
```

### JOIN

```java
public class Job_JoinDriver {
  	// mapper
    static class Job_JoinMapper extends Mapper<LongWritable, Text, Text, Text> {
        Text k = new Text();
        Text v = new Text();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // 通过切片获取到当前读取文件的文件名
            InputSplit inputSplit = context.getInputSplit();
            FileSplit fileSplit = (FileSplit) inputSplit;
            String path = fileSplit.getPath().getName();
            // 定义 sid 用于存放获取的 学生ID
            String sid;
            String[] split = value.toString().split("\\s+");
            // 判断文件名
            if (path.startsWith("student")) {
                // 学生表的 ID 在第一位
                sid = split[0];
                // 将整条数据作为 vlaue，并添加 Stu 的标识
                v.set("Stu" + value);
            } else {
                // 成绩表的 ID 在第二位
                sid = split[1];
                // 将整条数据作为 vlaue，并添加 Sco 的标识
                v.set("Sco" + value);
            }
            k.set(sid);
            context.write(k, v);
        }
    }
	// reducer
    static class Job_JoinReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // 用于存放获取到的学生信息
            String stuContext = "";
            // 用于存放学生的各科成绩
            LinkedList<String> scoContext = new LinkedList<>();
            for (Text value : values) {
                String res = value.toString();
                // 根据添加的标识，来区分学生信息和成绩
                if (res.startsWith("Stu")){
                    stuContext = res.substring(3);
                } else {
                    scoContext.add(res.substring(3));
                }
            }
            for (String score : scoContext) {
                // 将学生成绩与学生信息拼接
                Text v = new Text(stuContext + "  " + score);
                context.write(key, v);
            }
        }
    }
}

```

## Reference

https://juejin.cn/post/7011365385098231816

https://cloud.tencent.com/developer/article/1431491

https://zhuanlan.zhihu.com/p/482548135
