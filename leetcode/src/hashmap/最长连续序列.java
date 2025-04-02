package hashmap;

import java.util.HashSet;
import java.util.Set;

public class 最长连续序列 {
    public int longestConsecutive(int[] nums) {
        Set<Integer> map=new HashSet<>();
        for(int i=0;i<nums.length;i++){
            map.add(nums[i]);
        }
        int ans=0;
        for(int x:map){
            if(!map.contains(x-1)){
                int cnt=1;
                while (map.contains(x+1)){
                    cnt++;
                    x++;
                }
                ans=Math.max(ans,cnt);
            }
        }
        return ans;
    }
}
