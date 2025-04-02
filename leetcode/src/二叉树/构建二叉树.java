package 二叉树;

import sun.reflect.generics.tree.Tree;

import java.util.LinkedList;
import java.util.Queue;

public class 构建二叉树 {
    public static void main(String[] args) {
        BinaryTree0 bt = new BinaryTree0();
        bt.insert(1);
        bt.insert(2);
        bt.insert(3);
        bt.insert(4);
        bt.insert(5);
        bt.trans();
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
class BinaryTree0{
    TreeNode root;

    public void insert(int value){
        TreeNode newNode=new TreeNode(value);
        if(root==null){
            root=newNode;
            return;
        }
        Queue<TreeNode> queue=new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()){
            TreeNode currentNode=queue.poll();
            if(currentNode.left!=null){
                queue.add(currentNode.left);
            }else{
                currentNode.left=newNode;
                break;
            }

            if(currentNode.right!=null){
                queue.add(currentNode.right);
            }else{
                currentNode.right=newNode;
                break;
            }
        }
    }
    public void trans(){
        preOrder(root);
    }
    public void preOrder(TreeNode node){//前序遍历
        if(node ==null) return;
        System.out.println(node.value);
        preOrder(node.left);
        preOrder(node.right);
    }
}
