package 数组;

import java.util.ArrayList;
import java.util.List;

public class 子集 {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> lists=new ArrayList<>();
        List<Integer> list=new ArrayList<>();
        fullSort(lists,list,nums,0);
        return lists;
    }
    public void fullSort(List<List<Integer>> lists,List<Integer> list,int[] nums,int flag){
        lists.add(new ArrayList<>(list));
        for(int i=flag;i<nums.length;i++){
            list.add(nums[i]);
            fullSort(lists,list,nums,i+1);
            list.remove(list.size()-1);
        }
    }
}
