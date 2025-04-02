package 堆栈队列;

import java.util.PriorityQueue;

public class 第k大元素 {
    public int TopK(int[] nums, int k){
        PriorityQueue<Integer> maxHeap=new PriorityQueue<>((a,b)->b-a);
        for(int x:nums){
            maxHeap.add(x);
        }
        for (int i = 0; i < k - 1; i++) {
            maxHeap.poll();
        }
        return maxHeap.peek();
    }
}
