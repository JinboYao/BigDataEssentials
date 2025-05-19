![](https://oss-emcsprod-public.modb.pro/wechatSpider/modb_20210421_fbfc26d2-a27e-11eb-b313-38f9d3cd240d.png)



# java基础

## 静态变量和实例变量

静态变量 ：static关键字修饰，所有由这类生成的对象共享变量，类装载时就分配存储空间。调用：类名.方法名。

实例变量：独立于方法之外的变量。类变成对象时，才分配存储空间，能通过对象的引用访问实例变量。多线程中，线程共享，同步访问时可能由问题。

局部变量：方法中的定义的变量。在多线程中，每个线程复制一份局部变量，防止同步问题发生

## 面向对象的特征

1. 封装：封装对象的属性和实现细节，对外提供公共访问
  
2. 继承：扩展一个已有的类，继承该类的属性和行为，创建一个新的类
  
3. 多态 多态性是指允许不同子类型的对象对同一消息作出不同的响应。有运行时多态与编译时多态。

  重载：运行时多态

  重写：编译时多态

  向上转型：将子类的引用赋给父类的对象


## 值传递&引用传递

值传递：基本值直接存储在栈中，基本数据类型

引用类型：存储对象的引用，指向对象在内存中的地址。String

原因：

- 栈存取速度快
  
- 堆可以动态分配内存，Java垃圾回收器自动回收垃圾对象。
  

![](https://img-blog.csdnimg.cn/img_convert/1173f9aaf7fee2d1ec103d6e5189250f.webp?x-oss-process=image/format,png)

## JDK JRE JVM

JDK包括JRE，JRE包括JVM

JDK：编译java代码的工具

JRE：编译JAVA时所需的基础类库，虚拟环境（bin，lib）

JVM：java虚拟机，java语言实现跨平台

## 反射

可以获得类的构造函数，字段，方法，父类等信息

- 类的实例对象的getClass（）方法
  
- 类名.class
  
- Class.forName（）
  

## 基本类型

基本类型数据

| 整型   | byte    | 1    |
| ------ | ------- | ---- |
| 整型   | short   | 2    |
| 整型   | int     | 4    |
| 整型   | long    | 8    |
| 浮点型 | float   | 4    |
| 浮点型 | double  | 8    |
| 字符   | char    | 2    |
| 布尔   | boolean | 1    |

引用型数据类型

1. 数组
2. 类
3. 数组
4. String


## 访问修饰符 public,private,protected，default

public :当前类，同一个包，子类，其他类都可以调用

protected：对当前类，同一个包还有子类都可以调用

default：当前类与同包

private：当前类

## String

引用类型。String类被final修饰，不能被继承，其他成员方法默认为final，字符串一旦创建不能修改

字符串实例的值是通过char数组实现存储的

```
public final class String {
    private final char value[];
}
```

### '+'连接符

StringBuulder(s).append.toString()

## 字符串常量池

- 本质上是map，用来存放字符串的引用
  
- new一个字符串的时候,创建string实例：
  
  Stirng s =new String（“xyz”）
  

## ==和equal

基本数据类型时，==比较的是值；引用数据类型时，==比较的是地址

equal只能比较引用数据类型，若重写object类，比较对象的属性。

## int&integer

int是基本数据类型，默认值为0；

integer是对象的引用，integer变量必须实例化后才可以引用，默认值为null

## [堆](https://www.cnblogs.com/chengxiao/p/6129630.html)

基本思想是：将待排序序列构造成一个大顶堆，此时，整个序列的最大值就是堆顶的根节点。将其与末尾元素进行交换，此时末尾就为最大值。然后将剩余n-1个元素重新构造成一个堆，这样会得到n个元素的次小值。如此反复执行，便能得到一个有序序列了

**优先队列 PriorityQueue**

## 接口&抽象类

抽象类：用abstract关键字声明，extends继承。一个类只能继承一个抽象类

接口：用interface关键字声明，implement实现。一个类可以实现多个接口

## 重载重写

重载：发生在本类，方法名想同，参数列表一定不同。返回类型可以相同可以不同

重写：发生在父子继承，方法名和参数相同，实现不同。

```
public  class father{}
public class child extands father{}
```

## 常见的java异常介绍几种？

- NullPointerException：空指针异常
- OutOfMemoryError ：内存溢出错误（不是异常）
- ClassNotFoundException：指定类不存在
- NumberFormatException：字符串转换为数字异常
- IndexOutOfBoundsException：数组下标越界异常
- IllegalArgumentException：方法的参数错误
- ClassCastException：数据类型转换异常
- FileNotFoundException：文件未找到异常

## 内存泄漏&内存溢出

内存泄漏：程序中已经分配的堆内存没有由于某些原因无法释放，造成系统浪费

内存溢出：一次申请的内存超过了主机的内存

## OOM

- 堆溢出
  
  可能是对象较大；可能是内存泄漏，分配的堆内存无法释放
  
- 栈溢出
  
  创建了大量线程
  

## Java String/StringBuilder/StringBuffer区别

string不可变，每次操作会生成新的对象。

\- string类被final修饰，不能继承
\- 底层是char[]数组/byte[]数组
\- String对象一旦创建就不能修改，底层维护了一个线程常量池可共享。

StringBuilder 线程不安全，单线程操作

StringBuffer 线程安全，多线程操作,方法使用了synchronized锁

每个StringBuilder，StringBuffer对象都有一定的缓冲区容量，当字符串大小没有超过容量时，不会分配新的容量，当字符串大小超过容量时，会自动增加容量。

## 锁机制

synchronized

volatile

Lock

# JAVA集合类

- 集合实际上是一个容器，存储其他类型的数据

- 一次性可以存储多个对象

- 不能直接存储基本数据类型，也不能直接存储java对象，集合中存储的是java对象的内存地址（时引用）

![在这里插入图片描述](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b62f677b4af54f7e8dd4af73e1a4a605~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

Collection 接口的接口 对象的集合（单列集合） 

## List 接口

元素按进入先后有序保存，可重复 

LinkedList 接口实现类， 底层数据结构是链表， 插入删除， 没有同步， 线程不安全 
ArrayList 接口实现类， 底层数据结构是数组， 随机访问， 没有同步， 线程不安全 
Vector 接口实现类 底层数据结构是数组， 同步， 线程安全 
Stack 是Vector类的实现类 

### ArrayList

1. ArrayList内部是Object[]数组，不同的构造器，初始化容量不同，空参或者空集合添加第一个元素时为0，有参构造方法添加第一个元素时，根据实际情况进行初始化，初始化容量较小。

2. 添加元素会先进行计算目标容量，如果容量不足进行扩容，每次新的扩容后的数组长度是原数组长度的1.5倍。

3. ArrayList适合查询多，添加删除少的场景。

4. ArrayList集合不是线程安全的

### LinkedList

1. LinkedList 是一个双向链表。它也可以被当作堆栈、队列或双端队列进行操作。
2. LinkedList增删快，查找慢。
3. LinkedList 是非同步的。

## Set 接口

仅接收一次，不可重复，并做内部排序 

### HashSet  

使用底层使用hash表（数组）存储元素 （无序）

- LinkedHashSet 链表维护元素的插入次序 （FIFO，由链表保证元素有序，哈希表保证元素唯一）

### TreeSet  

底层实现为二叉树（红黑树），元素排好序（根据比较的返回值是否是0来决定元素是否唯一）

## Map 接口

键值对的集合 （双列集合） 

- HashMap 接口实现类 ，没有同步， 线程不安全-

  - LinkedHashMap 双向链表和哈希表实现 

  -  WeakHashMap 

- Hashtable 接口实现类， 同步， 线程安全 
- TreeMap 红黑树对所有的key进行排序 **（按照键的顺序对这些键值对进行排序）**

- LinkedHashMap:元素将按照它们最初被插入到映射中的顺序出现

### [HashMap](https://juejin.cn/post/7122091070862655501?searchId=202404211656195D7D995AC104EEA0E966)

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/258233c98ca440248e0ca0fea1e30848~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

**结构：**`数组`+`链表/红黑树` （超过7变成红黑树）

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

Hash冲突

通过key计算hashcode，hashcode%数组长度=数组下标。

不同key计算的hashcode可能相同=>数组下标可能相同=>**哈希冲突**。

**存在的问题：**

多个节点对应一个桶bucket，拉成一个链条。**Hash**冲突的极端情况下,某一个桶**bucket**后面挂着的链表会特别长,导致遍历的效率很低

**解决方法：**

- 当**HashMap**中存储的**Entry**较多时,需要对**HashMap**扩容来增加桶**bucket**的数量
- 这样对后续要存储的**Entry**来讲,就会大大缓解**Hash**冲突

### HashMap 线程不安全

**数据覆盖问题：**

- 如果两个线程并发执行put操作，并且hash值冲突。可能出现数据覆盖问题。

- A判断hash值位置为null，还没写入数据时挂起。B正常插入数据，然后A获得时间片，写入A的值，把B的数据覆盖掉。

**环形链表问题：**

- HashMap扩容时，两个线程同时操作一个链表时，引起指针混乱，形成环形链条。

### HashTable

- 线程安全：通过**synchronized**对关键方法加锁（比如 put、get、remove 等）
- 整表加锁

### ConcurrentHashMap 分段锁的原理

- CAS: 桶为空时的快速插入
  - 不需要加锁，避免线程阻塞
- Synchornized：Hash冲突时，锁住桶头节点（非整表）确保`数据一致性`

### TreeMap

### 红黑树

1. 二叉搜索树

   性质：

   - 节点的左子树只包含小于当前节点的数。
   - 节点的右子树只包含大于当前节点的数。
   - 左右子树也分别必须是二叉搜索树

   问题：依赖插入和删除的顺序，可能左右数据的差距很大

2. 平衡二叉树（自平衡的二叉搜索树）

   性质：

   * 任何节点的两个子树的高度最大差别为一
   * AVL树可能会在插入或删除节点后进行一些平衡操作，包括：
     - 单旋转（左旋或右旋）
     - 双旋转（左右旋或右左旋

   问题：频繁的树调整操作

3. 红黑树

   红黑树是基于平衡二叉树的改进，而平衡二叉树是为了解决二叉搜索树在特殊情况下，退化成链表，查找、插入效率退化成 O(n)，规定左右子树高度差不超过1，但是插入、删除节点的时候，所做的平衡操作比较复杂。 

   **红黑树维持了以下性质**：

   - 节点颜色：每个节点要么是红色的，要么是黑色的。
   - 根节点属性：**红黑树的根节点是黑色的**。
   - 叶子节点属性：所有的叶子节点（叶子节点是指树结束的空（null）节点）都是黑色的。
   - 红色节点属性：**如果一个节点是红色的，那么它的两个子节点都是黑色的。**换句话说，红色节点不能相邻，即所谓的“不能有两个连续的红色节点”。
   - 黑色高度属性：从任一节点到其每个叶子节点的所有路径上的黑色节点数量相同。

# JUC

### Synchronized关键字

**线程同步机制**，本质是锁定当前变量，其他线程会被阻塞

在对象上加锁实现`线程同步`。当一个线程进入syn块或者方法时，会获取该对象的锁。如果已经被其他线程持有，该线程就阻塞.

变量，方法，代码块都可以使用

### Volatile关键字

volatile本质是变量每次从`主内存`中读取最新值

volatile修饰的变量对所有线程可见。解决多个线程之间变量的可见性

# JVM

## java类加载的过程

![图片](https://img-blog.csdnimg.cn/img_convert/d41477f21fc75d5213d44335e43ba0e1.png)

1. 加载：把类的class文件中的二进制数据读入内存（运行进程），将它放在方法区（存储加载过的类）中。堆创建一个java.lang.class对象，封装类在方法区内的数据结构，最终在堆中创建一个**class对象**。
  
2. 链接：把二进制数据合并到jre中，
   验证：验证被加载的类有没有正确的内部结构，与其他类是否协调
   准备：为类的静态变量分配内存，这些内存在方法区进行分配
   解析：把符号引用直接解析为实际引用（对于一个方法的调用，编译器会解析一个包含目标方法所在的类、方法名、返回类型等的符号引用，指代要调用的方法）
  
3. 初始化 ：为类的静态变量赋予正确的初始值
  
4. 使用：new对象在程序中使用
  
5. 卸载：垃圾回收


## java类加载机制

全盘负责：当一个类加载某个class文件时，class所依赖和引用的其他class也由该类加载器载入。

父亲委托：先让父类加载器试图加载该类，无法加载时从自己的路径中加载。

缓存机制：缓存区保存所有加载过的class，程序需要使用某个class时，类加载器从缓冲区寻找该class。

## 双亲委派

在加载一个类时，由底向上检查类是否加载过。最上层是Object。

## java内存模型

![](https://img-blog.csdnimg.cn/img_convert/9c430c0c25780904cc722719bbe9fdc4.webp?x-oss-process=image/format,png)

方法区：存储`类结构`、`常量`、`静态变量`，`方法信息`。

- 永久代，很少垃圾回收
- 方法区里有一个运行时的常量池
- 类是模板，多个对象会共用。统一放在方法区，线程共享和避免重复浪费

堆：存放**对象实例**

- 所有线程共享的内存，在虚拟机启动的时候创建。几乎所有的对象实例都在这里创建。经常发生**垃圾回收**操作

桟：每个线程的方法；方法的局部变量、操作数栈、返回地址

- 每个方法执行的时候创建一个栈，用于存储方法的局部变量、操作数栈、返回地址
- 每个线程用自己的栈，**线程私有**

本地方法桟：与栈相似，存放native方法

pc计数器：计算字节码，计算当前所在的位置

## 堆内存模型

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e8ee3924f8574c3798c21bcf4a1d6859~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp?)

新生代

- new一个对象，就会放入eden区，当第一次CG时，eden区的存活对象放入s区，s0与s1不断复制与清空，计算对象的年龄

老年代：

- 大于年龄阈值就放入老生代。
- 大对象一般之间放入老生代
- 老生代满了就会清除

永久代：方法区

## 垃圾回收(CG)算法

引用计算法

- 维护一个引用计数器

复制

- 适用新生代
- 对象创建在Eden区，当Eden满了，把存活的对象复制到`S0`或者`S1`
- 清空Eden和另一个s区，之后两个S区交替使用

标记-清除

- 适用于老年代
- 标记需要清除的对象，之后统一回收标记的对象。
- 对象标记->对象清除->内存碎片

标记-整理

- 适用于老年代
- 标记需要清除的对象，让所有的存活对象压缩到堆的一端，清理端边界的对象。
- 对象整理->清除->空间消耗大

分代回收

- 适用于新生代，老生代

- 根据年代的特点清除对象。

## Minor GC 和Full GC

**Minor GC** 

- 针对 **年轻代** 的垃圾回收
- 触发条件： Eden区满了

**Full GC** 

- 对JVM堆空间(年轻代+老年代+元空间) 进行完全回收
- 触发条件：老年代/元空间满了

## 垃圾回收器

- Serial收集器，串行收集器是最古老，最稳定以及效率高的收集器，可能会产生较长的停顿，只使用一个线程去回收。
- ParNew收集器，ParNew收集器其实就是Serial收集器的多线程版本。
- Parallel收集器，Parallel Scavenge收集器类似ParNew收集器，Parallel收集器更关注系统的吞吐量。
- Parallel Old 收集器，Parallel Old是Parallel Scavenge收集器的老年代版本，使用多线程和“标记－整理”算法
- CMS收集器，CMS（Concurrent Mark Sweep）收集器是一种以获取最短回收停顿时间为目标的收集器。
- G1收集器，G1 (Garbage-First)是一款面向服务器的垃圾收集器,主要针对配备多颗处理器及大容量内存的机器. 以极高概率满足GC停顿时间要求的同时,还具备高吞吐量性能特征

# 多线程

一个进程包含多个线程，线程共享内存空间。

**实现方式**

1. 继承Thread类

**不能继承别的类**

```java
extends Thread{
	public void run() {
        System.out.println("我是线程逻辑");
    }
}
new MyThread().start(); // 启动线程
```

2. 实现Runnable接口

```java
implements Runnable {
    public void run() {
            System.out.println("我是线程逻辑");
    }
}
new Thread(new MyRunnable()).start();
```

**必须重写 run 方法**，**启动的时候调用的也是调用线程对象的start()方法来启动该线程**

```
run()方法只是写了一个线程的函数。start()方法才会执行多线程
```

## 线程同步

同步机制保证按预期顺序访问共享资源，防止多线程访问共享数据时，同时修改共享数据，导致数据不一致问题

线程同步确保线程安全

**同步方法**

synchronized

```
当一个线程调用synchronized关键字的方法时，它锁定该方法所属对象，其他线程无法同时调用该方法
```

volatile关键字

```
*修饰变量
*确保该变量在多线程下可见。当一个线程修改了被 volatile 修饰的变量的值时，其他线程能够立即看到这个改变，而不是使用自己的缓存值。
```

线程间通信

- 共享内存：共享变量可以是全局变量、实例变量或静态变量等。
- 消息队列

## 线程池

避免大量线程的创建和销毁的资源损耗，提高响应速度和线程重复利用

![](https://img-blog.csdnimg.cn/img_convert/201014c6ba9a8c431b62054672a3b279.webp?x-oss-process=image/format,png)

# 设计模式

### 单例模式

目的: 系统中只存在一个实例，不能随便new

```java
//饿汉式.类加载的时候就实例化。
public class Single{
    private static Single instance=new Single();//创建私有对象，外部类无法调用
    private Single（）{}//构造器私有化
    public static Single getInstance(){return instance;}//提供外部调用方法。调用方法 Single.getInstance();
}
Single类的构造方法使用private修饰，其他方法无法创建instance 对象实例，只能调用geiInstance方法。牺牲空间换取时间
```

```java
//懒汉式
//线程不安全：若一个线程进入了if (instance == null)判断语句块，还未来得及往下执行，
另一个线程也通过了这个判断语句，这时便会产生多个实例。
在if (instance == null)前面加synchronized锁

public class Single{
    private static Single instance=null;
    private Single(){}
    public static Single getInstance{//调用该方法时，创建instance
        if(instance==null){
            instance =new Single();
        }
        return instance;
    }
}
```

```
双重锁机制 DCL模式
public class Single{
    private static Single instance=null;
    private static Single(){}
    public  static Single getInstance(){
        if(intance==null){
           synchronized(Single.class){
              if(instance==null){同步块内检验：可能会有多个线程一起进入同步块外的 if，如果在同步块内不进行二次检验的话就会生成多个实例
                   instance=new Single();
              }
         }
     }
          return instance;
   }
}
```

jvm操作：

1. 给 instance 分配内存
  
2. 调用 Singleton 的构造函数来初始化成员变量
  
3. 将instance对象指向分配的内存空间（执行完这步 instance 就为非 null 了）

  在jvm中执行时可能会按照1-3-2步骤执行。假设A刚好执行到第三步，instance依旧是null。切换B线程，B线程执行getInstance时发现isntance！=null，可能发生空指针异常。

# Reference

https://github.com/lvminghui/Java-Notes/blob/master/docs/%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%9D%A2%E8%AF%95%E9%A2%98.md![linux](C:\Users\admin\OneDrive\桌面\linux.png)
