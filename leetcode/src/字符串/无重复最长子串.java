package 字符串;

import java.util.HashMap;

public class 无重复最长子串 {
    public int lengthOfLongestSubstring(String s) {
        int left=0,len=0;
        HashMap<Character,Integer> map=new HashMap<>();
        for(int i=1;i<s.length();i++){
            if(map.containsKey(s.charAt(i))){
                int x=map.get(s.charAt(i));
                left =Math.max(left,x+1);
            }
            map.put(s.charAt(i),i);
            len=Math.max(len,i-left+1);
        }
        return len;
    }
}
