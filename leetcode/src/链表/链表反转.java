package 链表;

public class 链表反转 {
    public ListNode reverse(ListNode head) {
        ListNode pre = null;
        ListNode cur = head;
        while (cur != null) {
            ListNode next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;   //最后一个next是null，cur也是null
        }
        return pre;
    }
}
