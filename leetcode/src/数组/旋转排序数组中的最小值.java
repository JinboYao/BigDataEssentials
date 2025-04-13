package 数组;

public class 旋转排序数组中的最小值 {
    public int findMin(int[] nums) {
        int n=nums.length;
        int left=0,right=n-1;
        while (left<=right){
            int mid=left+(right-left)/2;
            if(nums[mid]>nums[right]){//左边有序
                left=mid+1;
            }else{
                right=mid;
            }
        }
        return nums[left];
    }
}
