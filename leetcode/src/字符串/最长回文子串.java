package 字符串;

public class 最长回文子串 {
    public String longestPalindrome(String s) {
        int start=0,end=0;
        for(int i=0;i<s.length();i++){
            int len= Math.max(expand(i,i,s),expand(i,i+1,s));
            if(len>end-start){
                start=i-(len-1)/2;
                end=i+len/2;
            }
        }
        return s.substring(start,end+1);
    }
    public int expand(int left,int right,String s){
        while (left>=0 && right<s.length()){
            if(s.charAt(left)==s.charAt(right)){
                left--;
                right++;
            }else{
                return right-left-1;
            }
        }
        return right-left-1;
    }
}
