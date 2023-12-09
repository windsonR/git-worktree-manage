@file:Suppress("DuplicatedCode")

package worktree.windson.git_worktree_manage

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.ui.dsl.builder.panel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

fun execCmd(cmdr: String, workPath: Path) {
    val runtime = Runtime.getRuntime()

    try {
        val exec: Process
        val os = System.getProperty("os.name")
        exec = if (os.lowercase(Locale.getDefault()).startsWith("win")) {
            runtime.exec(cmdr, null, workPath.toFile())
        } else {
            val cmd = arrayOf("/bin/sh", "-c", cmdr)
            runtime.exec(cmd, null, workPath.toFile())
        }
        exec.waitFor(20, TimeUnit.SECONDS)
        var br = BufferedReader(InputStreamReader(exec.inputStream))
        val mavenOutput = StringBuilder()
        var inline: String?
        while (null != br.readLine().also { inline = it }) {
            mavenOutput.append(inline).append("\n")
        }
        br = BufferedReader(InputStreamReader(exec.errorStream))
        val stdErrOut = StringBuilder()
        while (null != br.readLine().also { inline = it }) {
            stdErrOut.append(inline).append("\n")
        }
        exec.destroy()
        br.close()
    } catch (e: IOException) {
        //
    } catch (e: InterruptedException) {
        //
    }
}

fun getWorktreePathMap(project: Project): MutableMap<String, String> {
    val runtime = Runtime.getRuntime()
    val cmdr = "git worktree list"
    val workPath = Paths.get(project.basePath!!)
    var gitWorktreeList: List<String> = listOf()
    try {
        val exec: Process
        val os = System.getProperty("os.name")
        exec = if (os.lowercase(Locale.getDefault()).startsWith("win")) {
            runtime.exec(cmdr, null, workPath.toFile())
        } else {
            val cmd = arrayOf("/bin/sh", "-c", cmdr)
            runtime.exec(cmd, null, workPath.toFile())
        }
        exec.waitFor(5, TimeUnit.SECONDS)
        var br = BufferedReader(InputStreamReader(exec.inputStream))
        val mavenOutput = StringBuilder()
        var inline: String?
        while (null != br.readLine().also { inline = it }) {
            mavenOutput.append(inline).append("\n")
        }
        br = BufferedReader(InputStreamReader(exec.errorStream))
        val stdErrOut = StringBuilder()
        while (null != br.readLine().also { inline = it }) {
            stdErrOut.append(inline).append("\n")
        }
        exec.destroy()
        br.close()
        gitWorktreeList = mavenOutput.split("\n")
    } catch (e: IOException) {
        // LOG.error(e)
    } catch (e: InterruptedException) {
        // LOG.error(e)
    }
    val pathMap = mutableMapOf<String, String>()
    gitWorktreeList.forEach {
        val list = it.split(" ")
        val path = list[0]
        val branch = list[list.size - 1].replace("[", "").replace("]", "")
        pathMap.plusAssign(branch to path)
    }
    return pathMap
}

fun getWorktreeList(project: Project): JPanel {
    val pathMap = getWorktreePathMap(project)
    val result = panel {
        row("Available worktrees") {
            val cb = comboBox(pathMap.keys.toList().filter { it.isNotEmpty() })
            button("Open") {
                val branch = cb.component.selectedItem?.toString()?.trim()
                val path = pathMap[branch]!!
                ProjectUtil.openOrImport(path, null, true)
            }
            button("Remove") {
                val branch = cb.component.selectedItem?.toString()?.trim()
                val path = pathMap[branch]!!
                val cmdr = "git worktree remove $path"
                val workPath = Paths.get(project.basePath!!)
                val bo = MessageDialogBuilder.yesNo("remove worktree?", "")
                if (bo.ask(project)) {
                    execCmd(cmdr, workPath)
                }
            }
        }
    }
    return result
}

fun getBranchList(project: Project): JPanel {
    val runtime = Runtime.getRuntime()
    var originBranches = listOf<String>()
    val alreadyExistBranches = getWorktreePathMap(project).keys
    try {
        val exec: Process
        val os = System.getProperty("os.name")
        val cmdr = "git branch -a"
        val workPath = Paths.get(project.basePath!!)
        exec = if (os.lowercase(Locale.getDefault()).startsWith("win")) {
            runtime.exec(cmdr, null, workPath.toFile())
        } else {
            val cmd = arrayOf("/bin/sh", "-c", cmdr)
            runtime.exec(cmd, null, workPath.toFile())
        }
        exec.waitFor(20, TimeUnit.SECONDS)
        var br = BufferedReader(InputStreamReader(exec.inputStream))
        val mavenOutput = StringBuilder()
        var inline: String?
        while (null != br.readLine().also { inline = it }) {
            mavenOutput.append(inline).append("\n")
        }
        br = BufferedReader(InputStreamReader(exec.errorStream))
        val stdErrOut = StringBuilder()
        while (null != br.readLine().also { inline = it }) {
            stdErrOut.append(inline).append("\n")
        }
        exec.destroy()
        br.close()
        originBranches = mavenOutput.split("\n").filter { it.trim().isNotEmpty() }.map { it.replace("origin/", "") }
            .map { it.trim() }.filter { it.indexOf("+") < 0 && it.indexOf("*") < 0 }
    } catch (e: IOException) {
        //
    } catch (e: InterruptedException) {
        //
    }

    var basePath = project.basePath!!

    val projectName = project.name
    val rBranches = originBranches.filter {
        !alreadyExistBranches.contains(it.trim())
    }.filter {
        !alreadyExistBranches.contains(it.trim().replace("remotes/", ""))
    }

    val result = panel {
        row("Local branches") {
            val cb = comboBox(rBranches)
            button("New") {
                var branch = cb.component.selectedItem?.toString()?.trim()
                val realProjectName = projectName.split('@')[0]
                if (basePath.indexOf("worktree") > 0) {
                    basePath = basePath.substring(0, basePath.indexOf(realProjectName) + realProjectName.length)
                }
                val path = "$basePath.worktree/$realProjectName@$branch"

                val workPath = Paths.get(basePath)

                if (branch?.indexOf("remotes/") == 0) {
                    branch = branch.replace("remotes/", "")
                    val newBranchCmdr = "git branch $branch origin/$branch"
                    execCmd(newBranchCmdr, workPath)
                }

                val cmdr = "git worktree add $path $branch"

                execCmd(cmdr, workPath)

                ProjectUtil.openOrImport(path, null, true)
            }
        }
    }
    return result
}
