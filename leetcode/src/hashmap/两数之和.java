package hashmap;

import java.util.HashMap;

public class 两数之和 {
    public static void main(String[] args){
        int[] a={5,6,7,8,1,2};
        int sum=7;
        HashMap<Integer,Integer> map=new HashMap<Integer, Integer>();
        for(int i=0;i<a.length;i++){
            if(map.containsKey(sum-a[i])){
                System.out.println("{"+a[i]+","+(sum-a[i])+"}");
            }
            map.put(a[i],i);
        }
    }
}
