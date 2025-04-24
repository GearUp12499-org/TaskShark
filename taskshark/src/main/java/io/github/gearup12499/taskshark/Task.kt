package io.github.gearup12499.taskshark

import io.github.gearup12499.taskshark.ITask.IllegalTransitionException
import io.github.gearup12499.taskshark.ITask.State

abstract class Task : ITask {
    @JvmField protected var state = State.NotStarted
    @JvmField protected var id = -1
    @JvmField protected var priority = 0

    final override fun getState() = state

    final override fun getId() = id

    @Throws(IllegalTransitionException::class)
    override fun transition(newState: State) {
        if (newState.order < state.order) throw IllegalTransitionException(this, state, newState)
        state = newState
    }

    override fun register(parent: Scheduler) {
        id = parent.register(this)
    }

    final override fun getPriority(): Int = priority

    final override fun canStart(): Boolean {
        TODO("Not yet implemented")
    }

    open fun extendGetDependents(result: MutableSet<ITask>) {}
    protected val depends: MutableSet<ITask> = mutableSetOf()

    final override fun getDependents() = depends.toMutableSet().also { extendGetDependents(it) }

    override fun stop(cancel: Boolean): Nothing {
        throw TaskStopException(cancel)
    }

    override fun <T : ITask> then(other: T): T {
        TODO("Not yet implemented")
    }
}