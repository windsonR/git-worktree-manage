package worktree.windson.git_worktree_manage

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangesViewManager.DisplayNameSupplier
import org.jetbrains.annotations.Nls

class GitWorktreeViewContentDisplayName(project: Project) : DisplayNameSupplier(project) {
    override fun get():@Nls String{
        return "Worktree"
    }
}