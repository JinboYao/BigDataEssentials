package 二叉树;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class 层序遍历 {
    public static void main(String[] args) {
        BinaryTree2 bt = new BinaryTree2();
        bt.insert(1);
        bt.insert(2);
        bt.insert(3);
        bt.insert(4);
        bt.insert(5);
        bt.insert(6);
        bt.insert(7);
        bt.order();
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
class BinaryTree2{
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

//层序遍历思想
    public void order(){
        Queue<TreeNode> queue=new LinkedList<>();
        ArrayList<Integer> array=new ArrayList<>();
        if(root!=null){
            queue.add(root);
        }
        while(!queue.isEmpty()){
            TreeNode currentNode=queue.poll();
            array.add(currentNode.value);
            if (currentNode.left !=null){
                queue.add(currentNode.left);
            }
            if (currentNode.right !=null) {
                queue.add(currentNode.right);
            }
        }
        System.out.println(Arrays.toString(array.toArray()));
    }
}
