package 数组;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class 合并区间 {
    public int[][] merge(int[][] arr) {
        if(arr.length==1 || arr.length==0){
            return arr;
        }
        Arrays.sort(arr,(a,b)->a[0]-b[0]);
        ArrayList<int[]> list=new ArrayList<>();
        for(int i=0;i<arr.length-1;i++){
            if(arr[i][1]<arr[i+1][0]){
                list.add(new int[]{arr[i][0],arr[i+1][1]});
            }
        }
        list.size()
    }
}
