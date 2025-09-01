package io.github.gearup12499.taskshark.virtual

import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Lock
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.TaskSharkInternalException
import io.github.gearup12499.taskshark.api.LogOutlet

/**
 * This **internal** class is a Scheduler that doesn't do any scheduling things; instead
 * it only serves to keep track of its members for future reparenting.
 */
internal class TaskBin : Scheduler() {
    val registered: MutableList<ITask<*>> = mutableListOf()

    override fun register(task: ITask<*>): Int {
        registered += task
        return registered.size
    }

    /**
     * Reassign all tasks to a different scheduler.
     */
    fun retcon(actualScheduler: Scheduler) {
        for (task in registered) {
            actualScheduler.add(task)
        }
        registered.clear()
    }

    private fun stub(methodName: String): Nothing = throw TaskSharkInternalException(
        "tried to use '$methodName' on fake Scheduler (TaskBin); a different scheduler should have been assigned before we got here"
    )

    override fun getLockOwner(lock: Lock): Nothing = stub("getLockOwner")

    override fun runTaskFinalizers(task: ITask<*>): Nothing = stub("runTaskFinalizers")

    override fun getTickCount(): Int {
        LogOutlet.currentLogger.error("tried to use 'getTickCount' on fake Scheduler (TaskBin); a different scheduler should have been assigned before we got here")
        return -1
    }

    override fun tick(): Nothing = stub("tick")
}