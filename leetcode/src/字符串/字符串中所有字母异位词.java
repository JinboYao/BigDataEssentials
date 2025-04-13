package 字符串;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class 字符串中所有字母异位词 {
    public List<Integer> findAnagrams(String s, String p) {
        int n=s.length(),m=p.length();
        char[] chp=p.toCharArray();
        Arrays.sort(chp);
        List<Integer> list=new ArrayList<>();
        for(int i=0;i<n-m+1;i++){
            int j=i+m;
            char[] chs=s.substring(i,j).toCharArray();
            Arrays.sort(chs);
            if(Arrays.equals(chp,chs)){
                list.add(i);
            }
        }
        return list;
    }
}
