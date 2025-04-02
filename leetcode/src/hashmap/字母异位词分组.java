package hashmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class 字母异位词分组 {
    public List<List<String>> groupAnagrams(String[] strs) {
        HashMap<String,ArrayList<String>> map=new HashMap<>();
        for(int i=0;i<strs.length;i++){
            String str=strs[i];
            char[] ch=str.toCharArray();
            Arrays.sort(ch);
            String key= new String(ch);
            ArrayList<String> list= map.getOrDefault(key,new ArrayList<String>());
            list.add(str);
            map.put(key,list);
        }
        return new ArrayList<List<String>>(map.values());
    }
}
