package 堆栈队列;

import java.util.Stack;

public class 有效括号 {
    public boolean isValid(String s){
        Stack<Character> stack=new Stack<>();
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);
            if((c==')'||c==']'||c=='}')&&!stack.isEmpty()){
                char flag=stack.pop();
                if(c==')' && flag!='(') return false;
                if(c==']' && flag!='[') return false;
                if(c=='}' && flag!='{') return false;
            }else{
                stack.push(c);
            }
        }
        if(!stack.isEmpty()) return false;
        return true;
    }
}
