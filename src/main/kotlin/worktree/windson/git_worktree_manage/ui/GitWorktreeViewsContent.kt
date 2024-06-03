package worktree.windson.git_worktree_manage.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentProvider
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import worktree.windson.git_worktree_manage.bundle.GitWorkTreeBundle
import worktree.windson.git_worktree_manage.util.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.tree.DefaultMutableTreeNode


class GitWorktreeViewsContent(private val project: Project) : ChangesViewContentProvider {
    private lateinit var rootPanel: JPanel
    override fun initContent(): JComponent {
        rootPanel = JPanel()
        updateContent()
        return rootPanel
    }

    private fun updateContent() {
        rootPanel.removeAll()
        renderTree()
        rootPanel.validate()
        rootPanel.repaint()
    }

    private fun renderTree() {
        // tree root
        val root = DefaultMutableTreeNode()

        // new 1st root
        val branchRoot = DefaultMutableTreeNode(GitWorkTreeBundle.message("worktree.tab.tree.branches"))
        val remoteBranchRoot = DefaultMutableTreeNode(GitWorkTreeBundle.message("worktree.tab.tree.remotes"))
        val worktreeRoot = DefaultMutableTreeNode(GitWorkTreeBundle.message("worktree.tab.tree.worktrees"))

        // add branches and worktree to 2nd root
        getLocalBranches(project, branchRoot)
        getRemoteBranches(project, remoteBranchRoot)
        getWorkTree(project, worktreeRoot)

        // add node to root
        root.add(branchRoot)
        root.add(remoteBranchRoot)
        root.add(worktreeRoot)

        // new tree instance
        val worktreeTree = Tree(root)

        // do not show root
        worktreeTree.isRootVisible = false

        // expand all by default
        for (i in 0 until worktreeTree.rowCount + 1) {
            worktreeTree.expandRow(i)
        }
        // context menu
        setContextMenu(worktreeTree)

        val scrollPane = JBScrollPane(worktreeTree)
        rootPanel.setLayout(GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1))
        rootPanel.add(
            scrollPane,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null,
                0,
                false
            )
        )
    }

    private fun setContextMenu(tree: Tree) {
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                // 如果是右键点击
                if (MouseEvent.BUTTON3 == e.button) {
                    val row: Int = (e.source as Tree).selectionRows?.get(0) ?: return
                    if (row != -1) {
                        val selectedPath = tree.getPathForRow(row).toString()
                        val selectedPathWithoutRoot =
                            selectedPath.substring(IntRange(2, selectedPath.length - 2)).split(",").map { it.trim() }
                        if (selectedPathWithoutRoot.size == 2) {
                            // 显示右键菜单
                            val popup = JPopupMenu()
                            val parent = selectedPathWithoutRoot[0]
                            val name = selectedPathWithoutRoot[1]
                            when (parent) {
                                GitWorkTreeBundle.message("worktree.tab.tree.branches") -> setBranchesPopup(popup, name)
                                GitWorkTreeBundle.message("worktree.tab.tree.remotes") -> setRemoteBranchesPopup(popup, name)
                                GitWorkTreeBundle.message("worktree.tab.tree.worktrees") -> setWorktreesPopup(popup, name)
                            }
                            popup.show(tree, e.x, e.y)
                        }
                    }
                }
            }
        })
    }

    private fun setBranchesPopup(popup: JPopupMenu, branch: String) {
        val menu1 = JMenuItem(GitWorkTreeBundle.message("worktree.tab.tree.new_worktree"))
        menu1.addActionListener {
            newLocalWorkTree(project, branch)
            updateContent()
        }
        popup.add(menu1)
    }

    private fun setRemoteBranchesPopup(popup: JPopupMenu, branch: String) {
        val menu1 = JMenuItem(GitWorkTreeBundle.message("worktree.tab.tree.checkout_new_worktree"))
        menu1.addActionListener {
            newRemoteWorkTree(project, branch)
            updateContent()
        }
        popup.add(menu1)
    }

    private fun setWorktreesPopup(popup: JPopupMenu, branch: String) {
        val menu1 = JMenuItem(GitWorkTreeBundle.message("worktree.tab.tree.open_worktree"))
        menu1.addActionListener {
            openWorkTree(project, branch)
            updateContent()
        }
        val menu2 = JMenuItem(GitWorkTreeBundle.message("worktree.tab.tree.delete_worktree"))
        menu2.addActionListener {
            removeWorkTree(project, branch)
            updateContent()
        }
        popup.add(menu1)
        popup.add(menu2)
    }

}