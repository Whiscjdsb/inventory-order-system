package com.example.inventorypractice.dsa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 二叉树算法复写练习（无提示复写）
 * 目标：从内存复写层序遍历 + 原地翻转，验证普通树/单节点/空树。
 */
public class BinaryTreePractice {

    /** 标准二叉树节点，left/right 是自行定义的引用字段，不是 Java API。 */
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode(int val) { this.val = val; }
    }

    /**
     * 层序遍历：返回 List<List<Integer>>，每一层一个子列表。
     * 提示坑（你的高频错误 #15）：循环里必须用取出的 current 节点的 left/right，
     * 不能一直用固定的 root.left/root.right。
     */
    public static List<List<Integer>> levelOrder(TreeNode root) {
        // TODO 你来写
        return null;
    }

    /**
     * 原地翻转二叉树：交换每个节点的 left 和 right，返回原根。
     * 提示坑（你的高频错误 #16）：原地翻转交换的是引用位置，原始子树不会丢；
     * 只有"建新树"才必须接住递归返回值并连接。
     */
    public static TreeNode invertTree(TreeNode root) {
        // TODO 你来写
        return null;
    }

    // ===== 测试台，不要改 =====
    public static void main(String[] args) {
        //         1
        //        / \
        //       2   3
        //      / \   \
        //     4   5   6
        TreeNode t = new TreeNode(1);
        t.left = new TreeNode(2);
        t.right = new TreeNode(3);
        t.left.left = new TreeNode(4);
        t.left.right = new TreeNode(5);
        t.right.right = new TreeNode(6);

        System.out.println("== 层序遍历 ==");
        System.out.println("普通树: " + levelOrder(t));                  // 期望 [[1],[2,3],[4,5,6]]
        System.out.println("单节点: " + levelOrder(new TreeNode(7)));   // 期望 [[7]]
        System.out.println("空树  : " + levelOrder(null));               // 期望 []

        System.out.println("== 翻转二叉树 ==");
        TreeNode inverted = invertTree(t);
        System.out.println("翻转后层序: " + levelOrder(inverted));                       // 期望 [[1],[3,2],[6,5,4]]
        System.out.println("单节点翻转: " + levelOrder(invertTree(new TreeNode(7))));    // 期望 [[7]]
        System.out.println("空树翻转  : " + levelOrder(invertTree(null)));              // 期望 []
    }
}
