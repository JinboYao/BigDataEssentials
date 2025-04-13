package 二叉树;

public class 验证二叉搜索树 {
    public boolean isValidBST(TreeNode root) {
        return isSearch(root,Long.MIN_VALUE,Long.MAX_VALUE);
    }
    public boolean isSearch(TreeNode node,long lower,long upper){
        if(node==null) return true;
        if(node.val<=lower || node.val>=upper){
            return false;
        }
        return true&&isSearch(node.left,lower,node.val)&&isSearch(node.right,node.val,upper);
    }
}
