package 数组;

import java.util.ArrayList;

public class 螺旋矩阵 {
    public ArrayList<Integer> spiralOrder(int[][] matrix) {
        int n=matrix.length;//行数
        int m=matrix[0].length;//列数
        int left=0,right=m-1,top=0,down=n-1;
        ArrayList<Integer> list=new ArrayList<>();
        while(top<=down&&left<=right){
            for(int i=left;i<=right;i++){
                list.add(matrix[top][i]);
            }
            for (int j=top+1;j<=down;j++){
                list.add(matrix[j][right]);
            }
            if(top<down &&left<right){
                for (int j=right-1;j>left;j--){
                    list.add(matrix[down][j]);
                }
                for (int j=down;j>top;j--){
                    list.add(matrix[j][left]);
                }
            }
            left++;
            right--;
            top++;
            down--;
        }
        return list;
    }
}
