package worktree.windson.git_worktree_manage

import com.intellij.codeInsight.codeVision.CodeVisionState.NotReady.result
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import javax.swing.JButton
import javax.swing.JPanel

class ToolWindowFactory: com.intellij.openapi.wm.ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(createToolWindow(project), "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createToolWindow(project: Project): JPanel{
        val result = JPanel()
        addComponents(project, result)
        return result
    }
    private fun addComponents(project: Project,panel: JPanel){
        panel.add(panel {
            row {
                button("刷新"){
                    panel.removeAll()
                    addComponents(project, panel)
                    panel.revalidate()
                    panel.updateUI()
                    panel.isVisible = true
                }
            }
        })
        panel.add(getWorktreeList(project))
        panel.add(getBranchList(project))
    }
}