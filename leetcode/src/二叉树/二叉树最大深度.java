package 二叉树;

public class 二叉树最大深度 {
    public int maxDepth(TreeNode root) {
        if(root ==null){return 0;}
        else{
            int left=maxDepth(root.left);
            int right=maxDepth(root.right);
            return Math.max(left,right)+1;
        }
    }
}
//class TreeNode{
//    int value;
//    TreeNode left;
//    TreeNode right;
//    TreeNode(int value){
//        this.value=value;
//        this.left=null;
//        this.right=null;
//    }
//}