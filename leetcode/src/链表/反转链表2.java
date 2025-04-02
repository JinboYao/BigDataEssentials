package 链表;
public class 反转链表2 {
    public ListNode reverse(ListNode head,int left,int right){
        ListNode dammpy= new ListNode(-1);
        dammpy.next=head;
        ListNode pre=null;
        ListNode cur=head;
        for (int i=1;i<=right;i++){
            if(i==left){
                ListNode next=cur.next;
                cur.next=pre;
                pre=cur;
                cur=next;
            }else{
                cur=cur.next;
            }
        }
    }
}
