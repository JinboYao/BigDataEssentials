package 数组;

public class 搜索二维矩阵 {
    public boolean searchMatrix(int[][] matrix, int target) {
        int n=matrix.length,m=matrix[0].length;
        for(int i=0;i<n;i++){
            if(target>=matrix[i][0]&&target<=matrix[i][m-1]){
                int left=0,right=m-1;
                while (left<=right){
                    int mid=(left+right)/2;
                    if(matrix[i][mid]<target){
                        left=mid+1;
                    } else if (matrix[i][mid]>target) {
                        right=mid-1;
                    }else {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
