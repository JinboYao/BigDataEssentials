package 二叉树;

public class 搜索二叉树搜索第k小 {
    public int kthSmallest(TreeNode root, int k) {
        ArrayList<Integer> list=new ArrayList<Integer>();
        midOrder(root,list);
        return list.get(k-1);
    }
    public void midOrder(TreeNode node,ArrayList<Integer> list){
        if(node ==null) return;
        midOrder(node.left,list);
        list.add(node.val);
        midOrder(node.right,list);
    }
}
