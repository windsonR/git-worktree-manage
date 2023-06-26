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
            val cmd = arrayOf<String>("/bin/sh", "-c", cmdr)
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
            val cmd = arrayOf<String>("/bin/sh", "-c", cmdr)
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
//        LOG.error(e)
    } catch (e: InterruptedException) {
//        LOG.error(e)
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
        row("可用worktree") {
            val cb = comboBox(pathMap.keys.toList().filter { it.isNotEmpty() })
            button("打开") {
                val branch = cb.component.selectedItem
                val path = pathMap[branch]!!
                ProjectUtil.openOrImport(path, null, true)
            }
            button("移除") {
                val branch = cb.component.selectedItem
                val path = pathMap[branch]!!
                val cmdr = "git worktree remove $path"
                val workPath = Paths.get(project.basePath!!)
                val bo = MessageDialogBuilder.yesNo("是否删除worktree?", "")
                if (bo.ask(project)) {
                    execCmd(cmdr, workPath)
                }
            }
        }
    }
    return result
}

@Suppress("DuplicatedCode")
fun getBranchList(project: Project): JPanel {
    val runtime = Runtime.getRuntime()
    var originBranchs = listOf<String>()
    try {
        val exec: Process
        val os = System.getProperty("os.name")
        val cmdr = "git branch -r"
        val workPath = Paths.get(project.basePath!!)
        exec = if (os.lowercase(Locale.getDefault()).startsWith("win")) {
            runtime.exec(cmdr, null, workPath.toFile())
        } else {
            val cmd = arrayOf<String>("/bin/sh", "-c", cmdr)
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
        originBranchs = mavenOutput.split("\n").filter { it.trim().length > 0 }.map { it.replace("origin/", "") }
    } catch (e: IOException) {
        //
    } catch (e: InterruptedException) {
        //
    }

    val basePath = project.basePath!!
    val projectName = project.name
    val rBranchs = originBranchs

    val result = panel {
        row("可用分支") {
            val cb = comboBox(rBranchs)
            button("新建") {
                val branch = cb.component.selectedItem
                val cmdr = "git worktree add ../$projectName.worktree/$projectName.$branch $branch"
                val workPath = Paths.get(basePath)
                execCmd(cmdr, workPath)

                val path = "$basePath.worktree/$projectName.$branch"
                ProjectUtil.openOrImport(path, null, true)
            }
        }
    }
    return result
}
