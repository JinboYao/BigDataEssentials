https://juejin.cn/post/7011365385098231816

# Hadoop基础

## HDFS(分布式文件存储系统)

### HDFS架构

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210228145111743.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDMxODgzMA==,size_16,color_FFFFFF,t_70)

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
3、datanode收到读请求后，通过HDFS的FSinoutstream将数据读取到本地，然后进行下一个数据块的读取，知道文件的所有block读取完成。

### 文件管理的容错机制

HDFS写入时，把文件分隔成block，每个block的3个副本存储在三个集群机器上。

```
■ 第一副本：放置在上传文件的 DataNode上;如果是集群外提交，则随机挑选一台磁盘不太慢、CPU不太忙的节点
■ 第二副本：放置在与第一个副本不同的机架的节点上
■ 第三副本：与第二个副本相同机架的不同节点上。
```

## MapReduce(分布式计算架构)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201210185505552.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01heV9KX09sZGh1,size_16,color_FFFFFF,t_70#pic_center#pic_center)

![1713443818276.png](https://img2.imgtp.com/2024/04/18/cjmeVp92.png)

1. split 输入分片

   ```
   输入文件，InputFormat数据读取
   split分片：根据HDFS的block块大小分片。假设HDFS大小为128M，文件是128*10M，Mapreduce就会分为10个MapTask。
   ```
2. Map

   ```
   map处理每一行的内容，分别计算 key，value的list
   ```

   （溢写之前）优化：combiner 合并每个map task的重复key值
3. shuffing

   ```
   1、MapTask收集map（）方法输出的<k,v>list，放入环形缓冲区（默认大小100M）.
   2、环形缓冲区到达一定阈值（80%），数据就会溢出到本地磁盘文件，多个溢写形成大文件。
   3溢写之前(分区+快排)：合并过程中，分区算法（HASH算法）和对key进行快速排序
   （在map阶段环形缓冲区的数据写入到磁盘的时候会根据reducetask的数量生成对应的分区，然后根据对应数据的哈希对分区数取模写入，然后会根据key值对分区中的数据使用快速排序算法进行排序，所以每个分区内的数据是有序的）
   4、合成大文件后，map端shuffle的过程也就结束了，后面进入reduce端shuffle的过程。
   5、Reduce会拉取同一个分区内的各个MapTask结果放在内存，放不下就溢写到磁盘
   6、对内存和磁盘的数据进行merge归并排序（这样就可以满足将key相同的数据聚在一起）
   ```
4. reduce

   reduce从合并的文件中取出一个一个的键值对group，调用用户自定义的reduce方法（），生成最终的输出文件。完成后output到HDFS中

### MapReduce两次排序

第一次在Map从环形缓冲区写入磁盘时，会根据Reduce的数量进行分区，然后根据数据的Hash取模写入。之后根据快排进行排序，数据有序

第二次是reduce阶段，reduceTask去Maptask节点上对应分区拉取数据，采用归并排序对拉取的key值排序。

### MapReduce中的Combine是干嘛的?有什么好外?

对MapTask的输出做一个重复key值的合并操作（{key，[V1,V2]}）,减少网络传输。

## HIVE

### HIVE底层原理

![在这里插入图片描述](https://img-blog.csdnimg.cn/5a5255259afc4096be4894b1dec1ccc1.png)

***Meta store：***hive借用关系型数据库（MySQL）存储hive中表的元数据信息。表结构，分区信息，列类型

**HIVE查询流程**：

1. HIVE CLI/WEB 提交HIVEQL
2. 解释器：转化成MR任务。这些任务用于在Hadoop集群上处理数据。
3. 编译器：根据元数据信息生成查询计划。提交给YARN分配资源
4. Hadoop执行生成的任务，计算HDFS的数据，结果写回HDFS

### UDF、UDAF、UDTF的区别

UDF 对单个输入值返回单个结果

UDAF 处理多行数据返回单个聚合值，类似 sum()，avg()

UDTF 可以从单个输入行生成多个输出行的函数。可以返回包含多列和多行的表格结果。例如爆炸函数

```
SELECT product_id, explode(features) as feature
FROM products;
```

### HIVE join的MR实现

![img](https://imgconvert.csdnimg.cn/aHR0cDovL2ltZy5seHcxMjM0LmNvbS8wNjI1LTEuanBn?x-oss-process=image/format,png#pic_center)

**一张表为小表** map join 进行聚合

**都为大表** 将join on公共字段相同的数据划分到同一个分区->传递到同一个reduce，实现聚合

### 分区表、分桶表、内部表、外部表

分区表：所有文件存储在一个HDFS文件夹里面，根据字段分区（PARTITIONED BY ）。每个分区是一个文件系统的目录

分桶表：按照hash值把数据分散到多个文件（桶）中。（数据加载到桶表时，会对字段取hash值，然后与桶的数量取模。把数据放到对应的文件中。物理上，每个桶就是表(或分区）目录里的一个文件，一个作业产生的桶(输出文件)和reduce任务个数相同。）

内部表：创建表时，会将数据移动到数据仓库指向的路径

外部表(EXTERNAL_TABLE)：创建表在指定的目录位置，hive存储元数据。

### Sort By，Order By，Cluster By，Distrbute By

Sort by 全局排序，只有一个reducer

order by 不是全局排序，数据在进入reduce之前完成排序

distrbute by 按照指定的字段对数据进行划分输出到不同的reduce

cluster by  数据划分到不同reduce中

### split、coalesce及collect_list函数

split 把字符串划分成数组

coalesce（T1,T2）返回第一个非空值

collect_list（）将一组值合并成一个数组

### Hive获取json

**get_json_object**

提取单个HIVE列中json字符串

```
SELECT get_json_object('{"a":{"b":1}}', '$.a.b') AS value;
-- 输出: "1"

```

**json_tuple**

提取多个字段

```
SELECT json_tuple('{"name": "Alice", "age": 30}', 'name', 'age') AS (name, age);
-- 输出: Alice, 30
```

### Hive元数据管理

### 数据倾斜

**原因：**

1. key值分布不均匀,导致某些reduce需要处理的数据量大，有些处理的数据量小，分布不均匀。可能是mapreduce中某些键值对出现频率非常高。触发**Shuffle**动作，**所有相同key的值就会拉到一个或几个节点上，就容易发生单个节点处理数据量爆增的情况。**
2. 业务数据本身分布不均
3. 建表时考虑不周
4. 某些SQL语句倾斜

**定位：** 任务执行过程中 卡在99%

**解决方法：**

1. 参数调节

   ```sql
   设置hive.map.aggr=true  开启map端的部分聚合供你，把key相同的聚合在一起
   设置hive.groupby.skewindata=true 负载均衡
   ```
2. SQL语句调节

   （1）小表join大表（map join）

   ```sql
   1、数据量小、key值分布均匀的表在左边
   2、map join ：让小的维度表（1000条以下的记录条数）先进内存。在map端完成reduce
   ```

   （2）大表join大表

   ```
   空key值过滤
   ```

   （3）group by 造成倾斜

   默认情况下，Map阶段同一Key数据分发给一个reduce，当一个key数据过大时就倾斜了

   ```
   1、map端进行聚合  hive.map.aggr = true
   2、数据倾斜的时候进行负载均衡（默认是false） hive.groupby.skewindata = true
   ```

   （4）count distinct

   ```
   distinct 把map的所有输出放在一个reduce task上面计算。
   ```

   优化：使用先GROUP BY再COUNT的方式替换

   （5）行列过滤

   ```sql
   select时使用分区过滤；join时先where过滤再关联
   ```

   （6）避免笛卡尔积

   ```
   笛卡尔积在一个reduce上面完成
   ```

   （7）参数设置并发执行

### HIVE 小文件调优

**原因:**

数据源上传时包含很多小文件

reduce数量越多，包含的小文件就越多

**影响：**

1、小文件启动多个map，一个map需要开启一个jvm

2、HDFS中，一个小文件对象占150byte，小文件过多占用大量内存

**解决方案：**

1、Hadoop archive命令：小文件归档

2、参数设置：减少reduce数量

3、使用Sequencefile作为表存储格式在一定程度上可以减少小文件数量

## Reference

https://juejin.cn/post/7011365385098231816

https://cloud.tencent.com/developer/article/1431491

https://zhuanlan.zhihu.com/p/482548135

MR JOIN

```
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
