package worktree.windson.git_worktree_manage

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

class ToolWindowFactory: com.intellij.openapi.wm.ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(createToolWindow(project), "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createToolWindow(project: Project): JPanel{
        val result = JPanel()
        val subPanel = JPanel(GridLayout(3, 1))
        subPanel.size = Dimension(200, 100)
        result.add(subPanel)
        addComponents(project, subPanel)
        return result
    }
    private fun addComponents(project: Project,panel: JPanel){
        val refreshPanel = panel {
            row {
                button("刷新"){
                    panel.removeAll()
                    addComponents(project, panel)
                    panel.revalidate()
                    panel.updateUI()
                    panel.isVisible = true
                }
            }
        }
        refreshPanel.size = Dimension(100, 30)
        panel.add(refreshPanel)
        val branchListPanel = getBranchList(project)
        branchListPanel.size = Dimension(100, 30)
        panel.add(branchListPanel)
        val worktreeListPanel = getWorktreeList(project)
        worktreeListPanel.size = Dimension(100, 30)
        panel.add(worktreeListPanel)
    }
}