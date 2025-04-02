package 二叉树;
import sun.reflect.generics.tree.Tree;
import java.util.LinkedList;
import java.util.Queue;

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
public class 前中后序遍历 {
    public static void main(String[] args){
        BinaryTree bt=new BinaryTree();
        bt.insert(1);
        bt.insert(2);
        bt.insert(3);
        bt.insert(4);
        bt.insert(5);
        bt.trans();
    }
}
class BinaryTree{
    TreeNode root;
    public void insert(int value){
        TreeNode newnode=new TreeNode(value);
        if(root==null){
            root=newnode;
            return;
        }
        Queue<TreeNode> queue=new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()){
            TreeNode node=queue.poll();
            if(node.left==null){
                node.left=newnode;
                break;
            }else{
                queue.add(node.left);
            }

            if(node.right==null){
                node.right=newnode;
                break;
            }else{
                queue.add(node.right);
            }
        }
    }
    public void trans(){
        preOrder(root);
        midOrder(root);
        proOrder(root);
    }
    public void preOrder(TreeNode node){
        if(node==null) return;
        System.out.println(node.value);
        preOrder(node.left);
        preOrder(node.right);
    }
    public void midOrder(TreeNode node){
        if(node==null) return;
        midOrder(node.left);
        System.out.println(node.value);
        midOrder(node.right);
    }
    public void proOrder(TreeNode node){
        if(node==null) return;
        proOrder(node.left);
        proOrder(node.right);
        System.out.println(node.value);
    }
}