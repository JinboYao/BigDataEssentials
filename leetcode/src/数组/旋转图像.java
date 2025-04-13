package 数组;

public class 旋转图像 {
    public void rotate(int[][] matrix) {
        int n=matrix.length;
        int m=n-1;
        for(int i=0;i<n/2;i++){
            for(int j=0;j<(n + 1) / 2;j++){
                int tmp=matrix[i][j];
                matrix[i][j]=matrix[m-j][i];
                matrix[m-j][i]=matrix[m-i][m-j];
                matrix[m-i][m-j]=matrix[j][m-i];
                matrix[j][m-i]=tmp;
            }
        }
    }
}
