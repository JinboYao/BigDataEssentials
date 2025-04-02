package 二叉树;

import javax.swing.tree.TreeNode;
public class 有序数组变成二叉搜索树 {
    public TreeNode sortedArrayToBST(int[] nums) {
        return insert(nums,0,nums.length-1);
    }
    public TreeNode insert(int[] nums,int left,int right){
        if(left>right) return null;
        int mid=(left+right)/2;

        TreeNode root= new TreeNode(nums[mid]);
        root.left= insert(nums,left,mid-1);
        root.right=insert(nums,mid+1,right);
        return root;
    }
}