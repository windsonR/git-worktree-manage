package worktree.windson.git_worktree_manage.util

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import worktree.windson.git_worktree_manage.bundle.GitWorkTreeBundle
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Paths
import java.util.*

fun exec(cmdr: String, project: Project): List<String> {
    val runtime = Runtime.getRuntime()
    val workPath = Paths.get(project.basePath!!)
    var result: List<String> = listOf()
    try {
        val exec: Process
        val os = System.getProperty("os.name")
        exec = if (os.lowercase(Locale.getDefault()).startsWith("win")) {
            runtime.exec(cmdr, null, workPath.toFile())
        } else {
            val cmd = arrayOf( GitWorkTreeBundle.message("worktree.no.windows.cmd.exec"),  GitWorkTreeBundle.message("worktree.no.windows.cmd.exec.c"), cmdr)
            runtime.exec(cmd, null, workPath.toFile())
        }
        exec.waitFor()
        val br = BufferedReader(InputStreamReader(exec.inputStream))
        val output = StringBuilder()
        var inline: String?
        while (null != br.readLine().also { inline = it }) {
            output.append(inline).append("\n")
        }
        exec.destroy()
        br.close()
        result = output.split("\n")
    } catch (e: IOException) {
        // LOG.error(e)
    } catch (e: InterruptedException) {
        // LOG.error(e)
    }
    return result
}

fun getWorktreePathMap(project: Project): MutableMap<String, String> {
    val cmdr =  GitWorkTreeBundle.message("git.worktree.list")
    val output = exec(cmdr, project)
    val pathMap = mutableMapOf<String, String>()
    output.forEach {
        val list = it.split(" ")
        val path = list[0]
        val branch = list[list.size - 1].replace("[", "").replace("]", "")
        if (branch.isNotEmpty() && path.isNotEmpty()) {
            pathMap.plusAssign(branch to path)
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
    ProjectUtil.openOrImport(path, null, false)
}

fun removeWorkTree(project: Project, branch: String) {
    val pathMap = getWorktreePathMap(project)
    val path = pathMap[branch]!!
    val cmdr = GitWorkTreeBundle.message("worktree.remove.path.cmdr", path)
    val bo = MessageDialogBuilder.yesNo(GitWorkTreeBundle.message("worktree.remove.message", branch),"")
    if (bo.ask(project)) {
        exec(cmdr, project)
    }
}

fun newLocalWorkTree(project: Project, branch: String) {
    var basePath = project.basePath!!

    val projectName = project.name

    val realProjectName = projectName.split('@')[0]
    if (basePath.indexOf("worktree") > 0) {
        basePath = basePath.substring(0, basePath.indexOf(realProjectName) + realProjectName.length)
    }

    // add local worktree
    val path = GitWorkTreeBundle.message("worktree.add.local.worktree.path",basePath,realProjectName,branch)

    val cmdr = GitWorkTreeBundle.message("worktree.add.local.worktree.cmdr", path,branch)

    exec(cmdr, project)

    ProjectUtil.openOrImport(path, null, true)
}

