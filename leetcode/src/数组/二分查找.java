package 数组;

public class 二分查找 {
    public int searchInsert(int[] nums, int target) {
        int n=nums.length;
        int left=0,right=n-1;
        while(left<=right){
            int mid=(left+right)/2;
            if(nums[mid]>target){
                right=mid-1;
            }else if(nums[mid]<target){
                left=mid+1;
            }else{
                return mid;
            }
        }
        return left;
    }
}
