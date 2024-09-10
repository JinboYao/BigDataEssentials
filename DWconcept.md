## 数据仓库是什么

目的：面向分析和决策

***意义：数据仓库的意义就是对企业所有的数据进行汇总，为各个部门提供统一的、规范数据出口***

- 稳定性：来源于历史数据，不存在更新删除
- 集合性：数据来自多个数据源，需要整合与清洗
- 大数据量：数据仓库存储的数据量大，需要支持大量数据分析
- 面向主题：数据围绕业务主题，把与主题相关的数据整合起来形成一个整体。

## 离线数仓架构

![img](https://uploadfiles.nowcoder.com/files/20231026/261038666_1698323104392/.jpg)

数据源层：包含接入的原始数据，包括客户端日志、服务端日志、业务库、集团数据、外部数据等。

数据加工层：使用 Spark、Hive 构建离线数仓、使用 Storm、 Flink 实时数仓。在数仓之上针对服务对象建设各种数据集市，比如：面向总部使用的总部数据集市面向行为数据的流量数据集市面向线下城市团队的城市团队集市面向广告的广告集市面向算法的算法特征

数据服务层：主要包括存储介质的使用和数据服务的方式。存储：主要使用开源组件，如 Mysql, HDFS, HBase, Kylin, Doris, Druid, ES, Tair 等数据服务：对外数据查询、接口以及报表服务

数据应用层：主要包括主题报表、自助取数工具、增值产品、数据分析等支撑业务开展，同时依赖公司平台提供的一些工具建设整体数据应用。

## 数仓建模

| 层级           | 作用                                                                                 |                                                    |
| -------------- | ------------------------------------------------------------------------------------ | -------------------------------------------------- |
| ODS 原始数据层 | 存放原始数据，后端数据快照                                                           |                                                    |
| DWD 数据明细层 | 业务过程驱动。根据每个业务构建最细粒度的明细事实表。宽表化处理，维度属性字段冗余     | 事务性事实表，周期性快照事实宽表，累计快照事实宽表 |
| DWS 数据汇总层 | 分析的主题对象为驱动，构建公共粒度的汇总指标事实表。构建命名规范和口径统一的统计指标 | 公共汇总宽表                                       |
| DIM 维度层     | 建立一致性维度                                                                       | 降低数据计算口径和算法不统一风险。                 |
| ADS 应用层     | 个性化指标业务                                                                       |                                                    |

![img](https://help-static-aliyun-doc.aliyuncs.com/assets/img/zh-CN/8325932951/p44636.png)

分层的好处：

- 数据解耦：根据不同的主题或者数据域存储，减少错误和冗余
- 数据血缘追踪：通过明确的层次结构，可以追踪数据的流向
- 性能优化：不同层次的数据可以针对特定的查询和分析任务优化；把数据查询和处理操作分散到不同的层级，减少单一负载

## 数仓VS数据库

|          | 数据库                                                         | 数据仓库                                                                                                                                |
| -------- | -------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| 数据结构 | 规范化的数据结构，二维表的关系型数据库                         | 非规范化或部分规范化的数据结构，如星形模式（star schema）和雪花模式（snowflake schema），这些结构优化了数据读取速度，便于进行多维分析。 |
| 目的     | 处理日常业务,On line Transaction Processing.支持高并发快速响应 | 存储历史数据，支持复杂查询，报表分析，On line Analytical Processing                                                                     |
| 数据更新 | 增删改查，经常发生                                             | 批量进行，日，周，月更新。没有修改和删除                                                                                                |
| 数据源   | 单一数据源，例如上课时间记录表                                 | 多数据源                                                                                                                                |

## 数仓建模

### ER模型

实体-关系模型

优点：保证数据的一致性和冗余少，应用于OLTP系统（操作型处理）中

### 维度模型

 1、星型模型
 以事实表为中心，所有维度表直接连在事实表上面。数据组织直观、执行效率高
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/d1f8c4c435554f01a4f09787e91a4938.png)
 2、雪花模型
 维度表可以有其他的维度表，维护成本高
![在这里插入图片描述](https://img-blog.csdnimg.cn/8cb988075cc64e68be527d35d6c82033.png)
 3、星座模型
基于多张事实表，建设一致性维度表，多张事实表共享维度信息。
![在这里插入图片描述](https://img-blog.csdnimg.cn/72d32693cd6a40bc99d56b23d42654f3.png)

### Data Value 模型

### Anchor模型

## 三大范式

| 范式 | 概念                                                                         |
| ---- | ---------------------------------------------------------------------------- |
| 1NF  | 原子性，数据库中每一列都不可分                                               |
| 2NF  | 所有建依赖于主键（可能是混合主键）                                           |
| 3NF  | 每个属性与主键有直接关系，消除传递依赖。（eg：学生表与专业表完全无传值依赖） |

***范式建模***

3NF设计规范，符合实体-关系模型

结合业务系统，构建实体数据-实体关系的数据表，一份数据只保存在一个地方，没有数据冗余

## 维度建模

概念：Kimball建模，把数据抽取为维度+事实表，数据冗余不能保证数据口径一致

***维度建模设计***

1、选择业务过程

教务系统业务过程：学员购买正价课－助教加微信触达－到课完课／退费退课

2、声明粒度

确定事实表的主键，学员，课程，小节，助教

比如直播间一次学习记录表，一行数据就是一个直播间中用户的学习记录

3、标识维度

通过维度，可以确定度量和事实。例如小节维度，对于直播间，课程属性的需求

4、标识事实：确认事实的指标

比如课程商品的订单活动，相关度量就是销售金额和销售数量

## 事实表设计

1、根据业务过程确定事实表：接到业务需求后，需要分析业务的整个过程与生命周期，拆解出其中的关键步骤，从而建立事务事实表。例如教育履约的业务过程：购买正价课-添加助教-企微聊天-参课完课-退费退课、复购

2、声明粒度：粒度是事实表中非常重要的一步，出现了问题大概率就会出现重复计算导致指标错误的情况。因此需要根据业务的过程，选择最细级别的原子力度，以保证后期上卷统计有更好的灵活性。例如产品要求统计到订单粒度，但实际业务过程最细粒度是子订单，那么在创建事实表的过程中中应该围绕子订单来进行统计。

3、确定维度：声明了最细粒度，也就意味着确定了主键，其相对应的维度属性也就可以确定。

4、确定事实：事实应该选择与业务过程相关的所有选项，并且粒度与声明的最细粒度一致。

5、关联维度：这一步是针对大数据环境下维度建模的特别步骤，主要为了统计和下游使用的便捷性，适当冗余部分维度，虽然破坏了星型模型的规则，但提高了灵活性。

### 事实表设计分几种，每一种都是如何在业务中使用

1. 单事务事实表：一个阶段的业务过程设计一个事实表，教育履约小节直播明细表
2. 多事务事实表：不同阶段的业务过程汇总在一张事实表，教育全链路大款表
3. 周期快照事实表：在确定的时间间隔内，对指标项统计，获得周期性度量值，期数到课完课表（对每周的指标进行一次汇总（完课人数、到课人数、退课人数、加微量、触达人数、退费数量）、助教服务表 当月的各种指标）。
4. 累计快照事实表：不确定的时间间隔内，对指标项进行统计。例如淘宝的下单-支付-确认收货-评价时间

## 退化维度

没有对应的维度表，存储在事实表中。如说订单编号，你可以获得这个订单里面包含哪些商品，对应的商家是谁，下单的用户是谁；分析商品，商家的进行分组统计订单数。

## 拉链表（缓慢变化维）

缓慢变化维：维度的属性可能随时间流逝变化。使用代理键做维度表的主键。

1. 直接覆盖原值

   ![img](https://img-blog.csdnimg.cn/img_convert/e1bd30254a92928b27c9a4915429a903.png)
2. 拉链表

   增加**开链时间**，**关链时间**和行标识

   ![](https://img-blog.csdnimg.cn/img_convert/c3d0a73aa36ef44db1604087f63ad11e.png)
3. 增加属性列

   存储旧标识和新标识

   ![](https://img-blog.csdnimg.cn/img_convert/776905e38e4e75d457a37ea5e33afd99.png)

### 增量表、全量表、快照表和拉链表

1、全量表：对所有的数据每个分区存一份

2、增量表：只存储当前分区的增量数据

3、快照表：存储的是历史到当前时间的数据。

4、拉链表：对于表中部分字段会被update、查看某一时间段的历史快照、变化比例和频率不是很大的数。
例如，物权表，用户的物权变化是缓慢变化维，采用加行的方式。

## 维度表和事实表的区别?

1、维度表：保存这个维度的元数据，例如dim_edu_course_section_base_pt，小节维度表，包含了小节的id、标题、开始结束时间、小节属性(有效、解锁)、小节所属课程以及课程标题、小节所属的直播间id、类型。
2、事实表：每行数据代表一个业务事件。包括业务事件的度量值，事实表 = 主键 + 度量 + 相关维度ID和退化维度。例如dws_edu_pref_user_section_learn_progress_wide_pd（教育履约用户小节学习进度宽表），需要dim表的维度信息计算，例如（学习进度）

### 教务系统项目建模过程

![在这里插入图片描述](https://img-blog.csdnimg.cn/19cd9e2827a547f0970c043176710045.png)

原子指标也称为基本指标，是直接从数据源中提取的指标，未经过任何计算或变换。

派生指标是基于一个或多个原子指标经过数学运算或逻辑运算得到的指标。

### 例子

假设一个电商网站关注以下指标：

* **原子指标** ：每日用户访问量、每日总销售额、每日广告费用。
* **派生指标** ：平均订单价值（每日总销售额 / 订单数量）、广告投资回报率（ROI）（每日总销售额 / 广告费用）。

## 指标、维度、度量

**指标：** 衡量数据，例如转化率

**维度：** 数据对象的特征和属性，例如时间，地点

**度量：** 事实表和维度表的交叉点，基于数据聚合计算的结果

**粒度：**数据细节级别或者汇总级别

区别：

**维度和度量**

维度为度量提供了分组基础。例如，你可能会按产品类别（维度）计算总销售额（度量），或者按月份（维度）查看平均销售量（度量）。

**指标和度量**

在数据仓库中，指标通常是基于维度进行计算的度量，如计算在特定时间或地点的销售总额（指标）。

总结：

* **维度**用于分类和上下文化数据；
* **度量**是基于这些分类进行的计算结果；
* **指标**是特定于业务的、用于衡量业务性能的一种度量。

## 代码调优的经验

1. 小表join大表
2. 不用discount
3. where放在join里面

## 数据建模考虑的点是什么，针对一个业务场景，数据模型大致怎么设计？

**了解业务需求** ：与业务团队沟通，了解业务的核心需求和关键性能指标（KPIs）。

**
    识别数据源** ：确定数据从哪些源头获取，例如销售系统、客户关系管理（CRM）系统等。

**定义事实和维度** ：

**
    事实表** ：通常包含业务过程中的量化数据。

**
    维度表** ：提供事实表中度量的上下文信息。

**选择模型类型** ：

**
    星型模式** ：简单、性能好，适用于大多数业务分析场景。

**
    雪花模式** ：维度表规范化，适合维度较多的复杂场景。

**建立模型** ：根据业务需求构建事实表和维度表。

**数据整合** ：制定ETL（抽取、转换、加载）流程，整合来自不同源的数据。

**验证与迭代** ：通过与业务用户的反馈迭代优化数据模型。

## 怎么保证数据质量

1. **模型建设**

* **确保高内聚低耦合**

```
（1）将业务相近或者相关、粒度相同的数据设计为一个逻辑或者物理模型
（2）将高概率同时访问的数据放在一起，将低概率同时访问的数据分开存储。
```

* **提高模型的复用性和扩展性** ：构建可重用的数据模型以应对多变的业务需求，同时保证模型容易扩展，能够适应数据量的增长和新的数据类型。
* **监控模型的稳定性** ：定期检查是否存在数据倾斜或运行时间异常，确保数据处理和查询性能稳定。

2. **数据成本与性能管理**

* **有效管理数据生命周期** ：通过设定数据的存储周期和访问频率，管理数据的存储成本和性能。
* **优化数据倾斜** ：通过合理的数据分区和负载均衡策略，减少数据倾斜问题，提高查询和处理的效率。
* **控制数据冗余和复制** ：确保数据冗余和复制的策略既能满足性能需求，又不会造成过度的成本负担。

3. **模型数据质量**

* **确保一致性** ：对于存储在不同表中的相同数据，通过定期的数据质量检查，确保没有差异。
* **完整性验证** ：检查数据集是否完整，确保所有必需的数据项都被正确采集和存储。
* **保持数据准确性** ：通过数据校验、清洗和修正流程，确保数据的准确性。
* **维护数据唯一性** ：通过唯一性校验，确保数据表中没有重复的记录，避免错误的数据汇总。
* **提高数据及时性** ：通过优化数据处理流程，确保数据可以及时更新，满足业务需求的实时性或近实时性。
* **规范数据管理** ：统一数据命名和格式规范，确保各个系统和数据表之间的一致性，便于管理和使用。

4. **持续监控和改进**

* **建立数据质量监控（DQC）** ：监控关键数据质量指标，如一致性、完整性、及时性等。
* **定期审查和优化** ：根据数据质量监控结果，不断调整数据处理流程和数据模型，以适应新的业务需求和技术发展。

## 怎么判断一个数仓的质量

1. 模型建设方面：

   高内聚低耦合

   ```
   （1）将业务相近或者相关、粒度相同的数据设计为一个逻辑或者物理模型
   （2）将高概率同时访问的数据放在一起，将低概率同时访问的数据分开存储。
   ```

   模型复用性，扩展性

   模型建设的完善程度

   模型稳定性，是否数据倾斜，运行时长是否稳定
2. 数据成本与性能：

   表的生命周期管理；数据倾斜任务；运行时长管理

   适当的数据冗余可换取查询和刷新性能，不宜过度冗余与数据复制、
3. 模型数据质量：

   `一致性`：相同数据在不同数据表中是否有gap

   `完整性`：数据集中是否包含所有的数据项

   `准确性`：

   `唯一性`：表中不存在重复数据。例如dwd层存在重复数据，dws层的汇总指标计算可能有误

   `及时性`：数据及时更新，满足业务的需求。例如，数据运行时间过长导致当天数据不能及时计算。

   `规范性`：表的命名，数据的存储格式规范。字段的命名在不同表中一致

![img](https://img-blog.csdnimg.cn/b5fb91f864664c738f2b6ff21c20940b.png)

## 元数据

描述数据的数据

1. 描述性元数据（Descriptive Metadata） 描述性元数据主要是用来描述数据的内容、形式和结构等方面的信息。比如，它可以包括关于数据的标题、作者、主题、摘要、关键字、日期等信息，以帮助用户快速了解和找到所需的数据。
2. 技术元数据（Technical Metadata） 技术元数据主要是用来描述数据的技术特性和属性的信息。比如，它可以包括数据的文件格式、编码方式、大小、分辨率、数据源、数据格式等信息，以帮助系统和应用程序理解和处理数据。
3. 行政元数据（Administrative Metadata） 行政元数据主要是用来管理和维护数据的信息。比如，它可以包括数据的创建时间、访问权限、版本号、维护人员、存储位置等信息，以帮助管理和维护数据的使用和安全。

## 开窗函数

- 聚合函数：sum() ,avg(),max(),min()

  ```sql
  SELECT SUM(Sales) OVER (ORDER BY Date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS Cumulative_Sales FROM sales_data;
  ```
- 排名函数：row_number(),rank(),dense_rank()

  ````sql
  100,200,200,300
  rn   1,2,3,4
  rank 1,2,2,4
  dr   1,2,2,3         SELECT  DENSE_RANK() OVER (ORDER BY Score DESC) AS Rank  FROM student_scores;(从大到小)
  ````
- 分析函数：lead(),lag(),FIRST_VALUE(),LAST_VALUE()

  ```sql
  lead(参数)            查看下一行数据      SELECT lead(name) OVER (ORDER BY id) FROM employees;
  lag(参数)             查看上一行数据      SELECT lag(name) OVER (ORDER BY id) FROM employees;
  FIRST_VALUE(参数)     查看第一行数据      SELECT FIRST_VALUE(name) OVER (ORDER BY hire_date) FROM employees;
  LAST_VALUE(参数)      查看最后一行数据    SELECT LAST_VALUE(name) OVER (ORDER BY hire_date ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) FROM employees;（必须要指定rows）
  ```

**语法：**

```sql
SELECT ROW_NUMBER(COLUMN) OVER(
    PARTITION BY COLUMN4 
    ORDER BY COLUMN5
    [ROWS|RANGE BETWEEN A AND B]
) AS colunm_name
FROM TABLE
WHERE CONDITION
```

`PARTITION BY` 用于定义窗口内数据分组的依据

`ORDER BY` 用于数据排序（**ASC `默认`：**由小到大  **DESC**:由大到小 ）

`ROWS|RANGE` 定义了窗口的前后范围

rows&range：

```sql
1、完整分区窗口
ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING: 包括整个分区的所有行

2、到当前分区末尾的窗口
ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING: 从当前行到分区的最后一行。

3、动态调整窗口
ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING: 包括当前行、前两行和后两行，总共五行，适用于需要考虑邻近数据的情况，如移动平均等。

4、固定行数的窗口
ROWS BETWEEN 1 PRECEDING AND CURRENT ROW: 这个窗口包括当前行和它之前的一行，用于计算这两行的数据。
ROWS BETWEEN 3 PRECEDING AND 1 FOLLOWING: 包括当前行、前面三行和后面一行，合计五行数据。

5、固定距离的窗口（基于逻辑值如日期）
RANGE BETWEEN INTERVAL 1 DAY PRECEDING AND CURRENT ROW: 对于日期字段，这个窗口包括从当前日期向前数一天内的所有行。
RANGE BETWEEN INTERVAL 1 HOUR PRECEDING AND INTERVAL 1 HOUR FOLLOWING: 包括从当前时间前后各一小时内的所有行。
```

**SQL执行顺序：**

- FROM
- JOIN
- WHERE
- GROUP BY
- 聚合函数
- HAVING
- SELECT
- DISTINCT
- ORDER BY
- LIMIT/OFFSET

## SQL题

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

##### 连续N天登录的用户

```sql
select 
 id
 ,result_date
 ,count(result_date) as num
from(
select 
 id
 ,date_sub(date,interval cum day) as result_date
from(
 select 
     id
     ,date(date) as date
     ,row_number() OVER (PARTITION BY id ORDER BY date(date)) as cum 
  from test.demo
  group by id,date(date)  
)t
)t1
group by id
 ,result_date
having count(result_date)>=3
```

##### 行转列

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

##### 列转行

```sql
select uid,语文 as course，score from 表名
WHERE `语文` IS NOT NULL  
union
select uid,数学 as course，score from 表名
WHERE `数学` IS NOT NULL  
```

##### 七日留存

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

##### 相互关注

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

##### 天/月gmv

要求使用SQL统计出每个用户的累积访问次数

```sql
select
   count,
   sum(count) over(parition by user_id order by day) as sum_count
from table
```

##### 炸裂函数

|              |                                                                   |
| ------------ | ----------------------------------------------------------------- |
| explode()    |                                                                   |
| posexplode() | position+explode                                                  |
| Lateral View | lateral view udtf(expression) 虚拟表别名 as col1 [,col2,col3……] |

SELECT id, key, value
FROM example_data
LATERAL VIEW EXPLODE(attributes) t AS key, value;

```sql
select tf1.*, tf2.*
from (select 0) t
lateral view explode(map('A',10,'B',20,'C',30)) tf1 as key,value
lateral view explode(map('A',10,'B',20,'C',30)) tf2 as key2,value2
where tf1.key = tf2.key2;
```

##### 集合函数

|                           |                  |
| ------------------------- | ---------------- |
| concat()                  | 有null则null     |
| concat_ws(分隔符，列，列) | 分隔符不能为null |
| concat_set()              | 去重             |
| concat_list()             | 排               |

```sql
select username, collect_list(video_name)[0] 
from 表名
group by username;
```

### 一个复杂的SQL中发生了数据倾斜，你怎么确定是哪个group by还是join发生

1. 查看执行计划：通过查看SQL的执行计划，可以获得SQL的具体执行流程，包括group by和join操作的执行顺序和数据分布情况。如果执行计划显示某个操作的数据分布不均匀，那么很可能是该操作导致了数据倾斜。
2. 分析数据分布情况：可以通过查看相关表的数据分布情况来初步判断是哪个操作导致了数据倾斜。例如，查看group by字段或者join字段的值的分布情况，看是否存在某些值的数量远远超过其他值。
3. 使用统计信息：数据库系统通常会提供统计信息，包括表的行数、列的基数等。可以通过查看统计信息，比较group by字段和join字段的基数，来判断哪个字段的数据分布更倾斜。
4. 使用日志或监控工具：可以通过分析数据库的日志或使用监控工具来获取SQL的执行情况和性能指标。通过查看相关指标，如数据读取量、CPU使用率等，可以初步判断是哪个操作导致了数据倾斜。
