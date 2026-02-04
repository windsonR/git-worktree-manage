package worktree.windson.git_worktree_manage.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

enum class DefaultExpandedNode {
    ALL,
    BRANCHES,
    REMOTES,
    WORKTREES,
    NONE
}

@State(
    name = "GitWorktreeSettings",
    storages = [Storage("GitWorktreeSettings.xml")]
)
class GitWorktreeSettings : PersistentStateComponent<GitWorktreeSettings.State> {

    data class State(
        var defaultExpandedNode: DefaultExpandedNode = DefaultExpandedNode.ALL
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var defaultExpandedNode: DefaultExpandedNode
        get() = myState.defaultExpandedNode
        set(value) {
            myState.defaultExpandedNode = value
        }

    companion object {
        fun getInstance(): GitWorktreeSettings {
            return ApplicationManager.getApplication().getService(GitWorktreeSettings::class.java)
        }
    }
}
