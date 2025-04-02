package 链表;

import java.util.HashSet;

public class 回文链表 {
    public boolean isPalindrome(ListNode head) {
        ListNode cur=head;
        Stack<Integer> stack=new Stack<>();
        while(cur!=null){
            stack.push(cur.val);
            cur=cur.next;
        }

        while(head!=null){
            if(stack.pop()!=head.val){
                return false;
            }
            head=head.next;
        }
        return true;
    }
}
