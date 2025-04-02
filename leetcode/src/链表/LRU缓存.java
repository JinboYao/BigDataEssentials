package 链表;
import java.util.HashMap;

public class LRU缓存 {
    public int capacity;
    public HashMap<Integer,DLinkedNode> map;
    public DLinkedNode head,tail;

    public LRUCache(int capacity){//设置LRU容量
        this.capacity=capacity;
        this.map=new HashMap<>();
        this.head=new DLinkedNode(0,0);
        this.tail=new DLinkedNode(0,0);
        head.next=tail;
        tail.prev=head;
    }
    public int get(int key){//链表位置靠前
        if(map.containsKey(key)){
            DLinkedNode node=map.get(key);
            remove(node);
            toHead(node);
            return node.value;
        }
        return -1;
    }
    public void put(int key,int value){
        if(map.containsKey(key)){
            DLinkedNode node=map.get(key);
            remove(node);
            node.value=value;
            toHead(node);
        }else{
            if(map.size()==capacity){
                DLinkedNode re=tail.prev;
                remove(re);
                map.remove(re.key);
            }
            DLinkedNode node=new DLinkedNode(key,value);
            toHead(node);
            map.put(key,node);
        }
    }
    public void toHead(DLinkedNode node){
        DLinkedNode HeadNext=head.next;
        node.prev=head;
        node.next=HeadNext;
        HeadNext.prev=node;
        head.next=node;
    }
    public void remove(DLinkedNode node){
        DLinkedNode prevNode=node.prev;
        DLinkedNode nextNode=node.next;
        prevNode.next=nextNode;
        nextNode.prev=prevNode;
    }

    class DLinkedNode{
        int key;
        int value;
        DLinkedNode next;
        DLinkedNode prev;
        DLinkedNode(int key, int value){
            this.key = key;
            this.value = value;
        }
    }
}
