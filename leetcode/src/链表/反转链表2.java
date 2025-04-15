package 链表;
public class 反转链表2 {
    public ListNode reverse(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(-1);
        dummy.next = head;

        ListNode pre = dummy;
        for (int i = 1; i < left; i++) {
            pre = pre.next;
        }

        ListNode cur = pre.next;
        ListNode prev = null;
        for (int i = left; i <= right; i++) {
            ListNode next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }

        pre.next.next = cur; // 原来的 left 节点现在在尾部，它的 next 指向 right+1
        pre.next = prev;     // pre 的 next 指向反转后的头节点

        return dummy.next;
    }

}
