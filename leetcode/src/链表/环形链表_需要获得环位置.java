package 链表;

public class 环形链表_需要获得环位置 {
    public ListNode detectCycle(ListNode head) {
        ListNode cur=head;
        Set<ListNode> set=new HashSet<>();
        while(cur!=null){
            if(set.contains(cur)){
                return cur;
            }
            set.add(cur);
            cur=cur.next;
        }
        return null;
    }
}
