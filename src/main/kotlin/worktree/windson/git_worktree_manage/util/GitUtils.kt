package worktree.windson.git_worktree_manage.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import git4idea.GitUtil
import git4idea.branch.GitBrancher
import git4idea.repo.GitRepository
import worktree.windson.git_worktree_manage.getWorktreeNames
import worktree.windson.git_worktree_manage.getWorktreePathMap
import worktree.windson.git_worktree_manage.newLocalWorkTree
import javax.swing.tree.DefaultMutableTreeNode

fun getRepository(project: Project): GitRepository {
    val repos = GitUtil.getRepositoryManager(project).repositories
    val repo = repos[0]
    return repo
}

fun getBranchesCommon(project: Project, root: DefaultMutableTreeNode, isRemote: Boolean){
    val repo = getRepository(project)
    val branches = repo.branches
    val branchesLocalOrRemote = if (!isRemote) {
        branches.localBranches
    } else {
        branches.remoteBranches
    }
    val existsWorktrees = getWorktreeNames(project)
    branchesLocalOrRemote.filter { it.name.isNotEmpty() }.filter { !existsWorktrees.contains(it.name) }.forEach {
        val node = DefaultMutableTreeNode(it.name)
        root.add(node)
    }
}

fun getLocalBranches(project: Project, branchRoot: DefaultMutableTreeNode) {
    getBranchesCommon(project, branchRoot, false)
}

fun getRemoteBranches(project: Project, remoteBranchRoot: DefaultMutableTreeNode) {
    getBranchesCommon(project, remoteBranchRoot, true)
}

fun getWorkTree(project: Project, root: DefaultMutableTreeNode){
    val worktrees = getWorktreePathMap(project)
    worktrees.keys.toList().filter { it.isNotEmpty() }.forEach {
        val node = DefaultMutableTreeNode(it)
        root.add(node)
    }
}

fun newRemoteWorkTree(project: Project, branch: String){
    val repo = getRepository(project)
    ProjectManager.getInstance().run {
        GitBrancher.getInstance(project).checkoutNewBranch(branch, listOf(repo))
        newLocalWorkTree(project, branch)
    }
}