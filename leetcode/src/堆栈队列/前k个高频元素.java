package 堆栈队列;

import java.util.HashMap;
import java.util.PriorityQueue;

public class 前k个高频元素 {
    public int[] topKFrequent(int[] nums, int k) {
        HashMap<Integer,Integer> map=new HashMap<>();
        for(int x:nums){
            map.put(x,map.getOrDefault(x,0)+1);
        }
        PriorityQueue<Integer> queue=new PriorityQueue<>(
                (a,b)->map.get(b)-map.get(a));
        queue.addAll(map.keySet());
        int[] ans=new int[k];
        for(int i=0;i<k;i++){
            ans[i]=queue.poll();
        }
        return ans;
    }
}
