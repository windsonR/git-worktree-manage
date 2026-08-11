package worktree.windson.git_worktree_manage.util

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.vcs.LocalFilePath
import git4idea.commands.Git
import worktree.windson.git_worktree_manage.bundle.GitWorkTreeBundle


fun getWorktreePathMap(project: Project): MutableMap<String, String> {
    val repo = getRepository(project)
    val wth = repo.workingTreeHolder
    val workingTrees = wth.getWorkingTrees()
    val pathMap = mutableMapOf<String, String>()
    workingTrees.forEach { workingTree ->
        val path = workingTree.path
        val branch = workingTree.currentBranch?.name ?: ""
        if (branch.isNotEmpty() && path.ioFile.exists()) {
            pathMap.plusAssign(branch to path.path)
        }
    }
    return pathMap
}

fun getWorktreeNames(project: Project): List<String> {
    return getWorktreePathMap(project).keys.toList().filter { it.isNotEmpty() }
}

fun openWorkTree(project: Project, branch: String) {
    val pathMap = getWorktreePathMap(project)
    val path = pathMap[branch]!!
    ProjectUtil.openOrImport(path, null, true)
}

fun removeWorkTree(project: Project, branch: String) {
    val pathMap = getWorktreePathMap(project)
    val path = pathMap[branch]!!
    val repo = getRepository(project)
    val workingTree = repo.workingTreeHolder.getWorkingTrees()
        .firstOrNull { it.path.path == path } ?: return
    val bo = MessageDialogBuilder.yesNo(GitWorkTreeBundle.message("worktree.remove.message", branch), "")
    if (bo.ask(project)) {
        Git.getInstance().deleteWorkingTree(project, workingTree)
    }
}

fun newLocalWorkTree(project: Project, branch: String) {
    // get real branch name
    // demo:
    // origin/dev => dev
    // dev => dev
    val rBranch = if (branch.indexOf("/") >= 0) {
        branch.split("/")[1]
    } else {
        branch
    }
    var basePath = project.basePath!!

    val projectName = project.name

    ProjectManager.getInstance().run {
        // create local worktree
        val realProjectName = projectName.split('@')[0]
        if (basePath.indexOf("worktree") > 0) {
            basePath = basePath.substring(0, basePath.indexOf(realProjectName) + realProjectName.length)
        }

        // add local worktree
        val path = GitWorkTreeBundle.message("worktree.add.local.worktree.path", basePath, realProjectName, rBranch)

        // git 操作放到后台线程执行，避免阻塞 EDT
        ApplicationManager.getApplication().executeOnPooledThread {
            val repo = getRepository(project)
            val git = Git.getInstance()

            // 本地无此分支且存在远程分支时，fetch 创建同名本地分支并设置 upstream
            val localBranch = repo.branches.findLocalBranch(rBranch)
            if (localBranch == null && repo.branches.findRemoteBranch(branch) != null) {
                val remote = repo.remotes.firstOrNull { it.name == "origin" }
                if (remote != null) {
                    // git fetch origin {0}:{0}
                    val fetchResult = git.fetch(repo, remote, emptyList(), "$rBranch:$rBranch")
                    if (fetchResult.success()) {
                        // git branch --set-upstream-to=origin/{0} {0}
                        git.setUpstream(repo, rBranch, branch)
                    }
                }
            }

            // git worktree add {0} {1}
            repo.branches.findLocalBranch(rBranch)?.let { branchForWorktree ->
                git.createWorkingTree(repo, LocalFilePath(path, false), branchForWorktree, null)
            }

            // 回到 EDT 打开新项目
            ApplicationManager.getApplication().invokeLater {
                ProjectUtil.openOrImport(path, null, true)
            }
        }
    }

}
