package worktree.windson.git_worktree_manage

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContext
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentProvider
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.Nls
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel

class GitWorktreeViewsContent(private val project: Project) : ChangesViewContentProvider {
    override fun initContent(): JComponent {
        val result = JPanel()
        val subPanel = JPanel(GridLayout(3, 1))
        subPanel.size = Dimension(200, 100)
        result.add(subPanel)
        addComponents(subPanel)
        return result
    }
    private fun addComponents(panel: JPanel){
        val refreshPanel = panel {
            row {
                button("refresh"){
                    panel.removeAll()
                    addComponents(panel)
                    panel.revalidate()
                    panel.updateUI()
                    panel.isVisible = true
                }
            }
        }
        refreshPanel.size = Dimension(100, 30)
        panel.add(refreshPanel)
        val branchListPanel = getBranchList(this.project)
        branchListPanel.size = Dimension(100, 30)
        panel.add(branchListPanel)
        val worktreeListPanel = getWorktreeList(this.project)
        worktreeListPanel.size = Dimension(100, 30)
        panel.add(worktreeListPanel)
    }
}