package worktree.windson.git_worktree_manage.ui

import com.intellij.openapi.project.Project
import com.intellij.vcs.log.impl.VcsProjectLog
import java.util.function.Predicate


class GitWorkTreeTabPredicated:Predicate<Project> {
    override fun test(project: Project): Boolean {
        val predicateResult = VcsProjectLog.getLogProviders(project).isNotEmpty()
        return predicateResult
    }
}