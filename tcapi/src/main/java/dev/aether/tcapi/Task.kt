package dev.aether.tcapi

import dev.aether.tcapi.ITask.IllegalTransitionException
import dev.aether.tcapi.ITask.State

abstract class Task : ITask {
    protected var state = State.NotStarted
    protected var id = -1
    protected var priority = 0

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