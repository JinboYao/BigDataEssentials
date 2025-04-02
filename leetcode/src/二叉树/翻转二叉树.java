package 二叉树;

public class 翻转二叉树 {
    public TreeNode invertTree(TreeNode root) {
        if(root==null) return root;
        TreeNode left=root.left;
        TreeNode right=root.right;
        root.left=invertTree(right);
        root.right=invertTree(left);
        return root;
    }
}
