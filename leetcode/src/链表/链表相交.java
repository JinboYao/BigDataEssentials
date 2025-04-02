package 链表;

public class 链表相交 {
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        HashMap<ListNode,Integer> map=new HashMap<>();
        ListNode cur=headA;
        while(cur!=null){
            map.put(cur,1);
            cur=cur.next;
        }
        cur=headB;
        while(cur!=null){
            if(map.containsKey(cur)){
                return cur;
            }
            cur=cur.next;
        }
        return null;
    }
}
