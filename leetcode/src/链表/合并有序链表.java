package 链表;

public class 合并有序链表 {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode prehead = new ListNode(-1);
        ListNode cur=prehead;
        while(list1!=null && list2!=null){
            if(list1.val>=list2.val){
                cur.next=list2;
                list2=list2.next;
            }else{
                cur.next=list1;
                list1=list1.next;
            }
            cur=cur.next;
        }
        while(list1!=null){
            cur.next=list1;
            list1=list1.next;
            cur=cur.next;
        }
        while(list2!=null){
            cur.next=list2;
            list2=list2.next;
            cur=cur.next;
        }
        return prehead.next;
    }
}
