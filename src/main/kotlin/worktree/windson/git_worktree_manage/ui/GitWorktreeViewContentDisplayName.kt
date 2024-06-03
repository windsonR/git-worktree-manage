package worktree.windson.git_worktree_manage.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangesViewManager.DisplayNameSupplier
import org.jetbrains.annotations.Nls
import worktree.windson.git_worktree_manage.bundle.GitWorkTreeBundle

class GitWorktreeViewContentDisplayName(project: Project) : DisplayNameSupplier(project) {
    override fun get():@Nls String{
        return GitWorkTreeBundle.message("worktree.tab.name")
    }
}