![](https://oss-emcsprod-public.modb.pro/wechatSpider/modb_20210421_fbfc26d2-a27e-11eb-b313-38f9d3cd240d.png)



## [HashMap](https://juejin.cn/post/7122091070862655501?searchId=202404211656195D7D995AC104EEA0E966)

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/258233c98ca440248e0ca0fea1e30848~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

**结构：**数组+链表/红黑树 （超过7变成红黑树）

数组元素包括 Key，Value，Hashcode和Entry节点。

通过key计算hashcode，hashcode%数组长度=数组下标。不同key计算的hashcode可能相同=>数组下标可能相同=>**哈希冲突**。

**get：**

1. 计算key的hashcode，数组下标，找到对应的Node。
2. 如果Node第一个节点命中，直接返回
3. 如果存在冲突，key.equals(k) 查找对应的entry

**put：**

1. 对key的hashCode()做hash，然后再计算index;

2. 如果存在冲突

   - 查看是否存在相同的key，存在相同的key跳出循环，覆盖key的value

   - 如果不存在相同的key，在链表末尾插入新的Node.如果链表节点过长，转换为树。

3. Node容量满了，Resize() 



**resize():**

元素个数超过设定的阈值，例如0.75的容量 。会新建一个数组，重新计算node。

### HashMap 线程不安全

**数据覆盖问题：**

如果两个线程并发执行put操作，并且hash值冲突。可能出现数据覆盖问题。

A判断hash值位置为null，还没写入数据时挂起。B正常插入数据，然后A获得时间片，写入A的值，把B的数据覆盖掉。

**环形链表问题：**

HashMap扩容时，两个线程同时操作一个链表时，引起指针混乱，形成环形链条。

### ConcurrentHashMap 分段锁的原理

HashMap 出现并发问题的核心在于多个线程同时操作同一个链表，而 ConcurrentHashMap 在操作链表前会用 synchronized 对链表的首个元素加锁，从而避免并发问题。
