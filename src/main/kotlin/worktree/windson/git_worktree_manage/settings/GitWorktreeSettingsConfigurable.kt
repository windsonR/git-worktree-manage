package worktree.windson.git_worktree_manage.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.bindItem
import worktree.windson.git_worktree_manage.bundle.GitWorkTreeBundle
import javax.swing.JComponent

class GitWorktreeSettingsConfigurable : Configurable {

    private var selectedNode: DefaultExpandedNode = GitWorktreeSettings.getInstance().defaultExpandedNode
    private var myPanel: DialogPanel? = null

    override fun getDisplayName(): String = GitWorkTreeBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        val panel = panel {
            row(GitWorkTreeBundle.message("settings.default.expanded.node")) {
                comboBox(
                    DefaultExpandedNode.values().toList(),
                    SimpleListCellRenderer.create("") { node ->
                        node.name.lowercase().replaceFirstChar { it.uppercase() }
                    }
                ).bindItem(
                    getter = { selectedNode },
                    setter = { selectedNode = it ?: DefaultExpandedNode.ALL }
                )
            }
        }
        myPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        myPanel?.apply()
        return selectedNode != GitWorktreeSettings.getInstance().defaultExpandedNode
    }

    override fun apply() {
        myPanel?.apply()
        GitWorktreeSettings.getInstance().defaultExpandedNode = selectedNode
    }

    override fun reset() {
        selectedNode = GitWorktreeSettings.getInstance().defaultExpandedNode
        myPanel?.reset()
    }
}
