package 二叉树;

public class 二叉树的直径 {
    public int ans;
    public int diameterOfBinaryTree(TreeNode root) {
        ans=0;
        maxDepth(root);
        return ans;
    }
    public int maxDepth(TreeNode node){
        if(node==null) return 0;
        int left=maxDepth(node.left);
        int right=maxDepth(node.right);
        ans=Math.max(left+right,ans);
        int depth=Math.max(left,right)+1;
        return depth;
    }
}
