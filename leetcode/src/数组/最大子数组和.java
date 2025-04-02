package 数组;

public class 最大子数组和 {
    public static void main(String[] args){
        int[] arr= {-2,1,-3,4,-1,2,1,-5,4};
        maxSubArray(arr);
    }
    public static int maxSubArray(int[] nums) {
        int flag=nums[0], res=nums[0];
        for(int i=1;i<nums.length;i++){
            flag=Math.max(nums[i],flag+nums[i]);
            res=Math.max(res,flag);
        }
        return res;
    }
}
