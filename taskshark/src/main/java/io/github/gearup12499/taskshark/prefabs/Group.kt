package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.ITask
import io.github.gearup12499.taskshark.Scheduler
import io.github.gearup12499.taskshark.Task

class Group(conf: ConfigureFn) : Task() {
    fun interface ConfigureFn {
        fun conf(inner: Scheduler)
    }

    internal class GroupScheduler : FastScheduler() {
        fun isAllCompleted() = tasks.all { (_, it) ->
            val state = it.getState()
            when (state) {
                ITask.State.Finished, ITask.State.Cancelled -> true
                else -> it.getTags().contains("daemon")
            }
        }
    }

    private val inner = GroupScheduler()

    init {
        conf.conf(inner)
    }

    fun getScheduler() = inner as Scheduler

    override fun onTick(): Boolean {
        inner.tick()
        return inner.isAllCompleted()
    }
}