package 二叉树;

public class 对称二叉树 {
    public boolean isSymmetric(TreeNode root) {
        if(root==null) return true;
        return isSame(root.left,root.right);
    }
    public boolean isSame(TreeNode left,TreeNode right){
        if(left ==null&& right==null) return true;
        if(left ==null|| right==null) return false;
        return (left.val ==right.val && isSame(left.left,right.right) &&isSame(right.left,left.right));
    }
}
