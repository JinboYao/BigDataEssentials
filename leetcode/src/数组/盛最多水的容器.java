package 数组;

public class 盛最多水的容器 {
        public int maxArea(int[] height) {
            int res=0,n=height.length;
            int left=0,right=n-1;
            while(right>=left){
                int x=right-left;
                int y=Math.min(height[left],height[right]);
                res=Math.max(x*y,res);
                if(height[right]>=height[left]){
                    left++;
                }else{
                    right--;
                }
            }
            return res;
        }
}
