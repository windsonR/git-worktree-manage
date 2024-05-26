package worktree.windson.git_worktree_manage.ui

import com.intellij.packageDependencies.ui.TreeModel
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class GitWorktreeTree {
    fun initComponent():Tree{
        val tree =Tree()
        val root = DefaultMutableTreeNode("Worktrees")
        val branches = DefaultMutableTreeNode("Branches")
        val worktrees = DefaultMutableTreeNode("Exists Worktrees")
        root.add(branches)
        root.add(worktrees)
        tree.model = DefaultTreeModel(root)
        return tree
    }
}