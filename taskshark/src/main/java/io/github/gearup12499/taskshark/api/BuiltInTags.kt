package io.github.gearup12499.taskshark.api
import io.github.gearup12499.taskshark.ITask

object BuiltInTags {
    /**
     * Daemon tasks aren't required to complete a group.
     */
    const val DAEMON = "daemon"

    /**
     * Virtual tasks aren't subject to strict contract requirements.
     *
     * This tag doesn't do anything on its own; see [ITask.isVirtual]
     */
    const val VIRTUAL = "virtual"
}