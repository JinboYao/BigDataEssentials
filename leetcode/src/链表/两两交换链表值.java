package 链表;
public class 两两交换链表值 {
    public ListNode swapPairs(ListNode head) {
        ListNode dummy=new ListNode(-1);
        dummy.next=head;
        ListNode cur=dummy;
        while(cur.next!=null&&cur.next.next!=null){
            ListNode first=cur.next;
            ListNode second=cur.next.next;
            cur.next=second;
            first.next=second.next;
            second.next=first;
            cur=first;
        }
        return dummy.next;
    }
}
