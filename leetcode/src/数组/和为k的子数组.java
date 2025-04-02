package 数组;

import java.util.HashMap;
// sum[9]-sum[6]在前缀和中存在，则可以获得k
public class 和为k的子数组 {
    public int subarraySum(int[] nums,int k){
        int count=0;
        int currentSum=0;
        HashMap<Integer,Integer> map=new HashMap<>();
        map.put(0,1);
        for (int i=0;i<nums.length;i++){
            currentSum=currentSum+nums[i];
            if(map.containsKey(currentSum-k)){
                count+= map.get(currentSum-k);
            }
            map.put(currentSum,map.getOrDefault(currentSum,0)+1);
        }
        return count;
    }
}
