package 链表;
public class 删除倒数n个节点 {
    public ListNode remove(ListNode head,int n){
        ListNode cur=new ListNode(0);
        cur.next=head;
        ListNode fast=cur;
        ListNode slow=cur;
        for(int i=0;i<=n;i++){
            fast=fast.next;
        }
        while (fast!=null){
            fast=fast.next;
            slow=slow.next;
        }
        slow.next=slow.next.next;
        return cur.next;
    }
}
