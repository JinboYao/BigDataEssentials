package 链表;

public class 两数相加 {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy=new ListNode(-1);
        ListNode cur=dummy;
        int carry=0;
        while(l1!=null||l2!=null||carry!=0){
            int x=l1!=null?l1.val:0;
            int y=l2!=null?l2.val:0;
            int n=(x+y+carry)%10;
            carry=(x+y+carry)/10;
            cur.next=new ListNode(n);
            cur=cur.next;
            if(l1!=null){l1=l1.next;}
            if(l2!=null){l2=l2.next;}
        }
        return dummy.next;
    }
}
