package 数组;

import java.util.ArrayList;

public class 全排列 {
    public List<List<Integer>> permute(int[] nums) {
        int n=nums.length;
        List<List<Integer>> lists=new ArrayList<>();
        List<Integer> list=new ArrayList<>();
        fullSort(nums,0,list,lists);
        return lists;
    }
    public void fullSort(int[] nums,int flag,List<Integer> list,List<List<Integer>> lists){
        if(flag==nums.length){
            lists.add(new ArrayList<>(list));
        }
        for(int i=0;i<nums.length;i++){
            if(!list.contains(nums[i])){
                list.add(nums[i]);
                fullSort(nums,flag+1,list,lists);
                list.remove(list.size()-1);
            }
        }
    }
}



//模板
//void backtracking(参数) {
//    if (终止条件) {
//        存放结果;
//        return;
//    }
//    for (选择 : 本层集合中的元素) {
//        处理节点;
//        backtracking(路径, 选择列表); // 递归
//        撤销处理; // 回溯
//    }
//}