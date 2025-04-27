package io.github.gearup12499.taskshark

import io.github.gearup12499.taskshark.ITask.IllegalTransitionException
import io.github.gearup12499.taskshark.ITask.State
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

abstract class Task : ITask {
    @JvmField protected var state = State.NotStarted
    @JvmField protected var id = -1
    @JvmField protected var priority = 0
    @JvmField protected var scheduler: Scheduler? = null

    final override fun getState() = state

    final override fun getId() = id

    override fun hashCode(): Int {
        return (scheduler?.hashCode() ?: 0) * 31 + id
    }

    override fun equals(other: Any?) = this === other

    @Throws(IllegalTransitionException::class)
    override fun transition(newState: State) {
        if (newState.order < state.order) throw IllegalTransitionException(this, state, newState)
        state = newState
    }

    override fun register(parent: Scheduler) {
        id = parent.register(this)
        scheduler = parent
    }

    final override fun getPriority(): Int = priority

    override fun getTags(): Set<String> = emptySet()

    override fun canStart(): Boolean = true
    override fun onStart() {}
    override fun onFinish(completedNormally: Boolean) {}

    open fun extendGetDependents(result: MutableSet<ITask>) {}

    @JvmField protected val lockDependencies: MutableSet<Lock> = mutableSetOf()
    @JvmField protected val taskDependencies: MutableSet<ITask> = mutableSetOf()

    override fun require(lock: Lock) {
        lockDependencies.add(lock)
    }

    final override fun dependedLocks(): Set<Lock> = lockDependencies
    final override fun dependedTasks(): Set<ITask> = taskDependencies

    @JvmField protected val dependents: MutableSet<ITask> = mutableSetOf()
    final override fun getDependents() = dependents.toMutableSet().also { extendGetDependents(it) }

    override fun stop(cancel: Boolean) {
        try {
            when (state) {
                State.Starting, State.Ticking -> {
                    transition(State.Finishing)
                    onFinish(!cancel)
                }
                else -> {}
            }
            transition(if (cancel) State.Cancelled else State.Finished)
            if (scheduler?.getCurrentEvaluation() === this)
                throw TaskStopException()
        } finally {
            // this really needs to run, it's the only thing saving us from deadlocking
            // even if [onFinish] blows up
            scheduler?.runTaskFinalizers(this)
        }
    }

    override fun <T : ITask> then(other: T): T {
        dependents.add(other)
        return other
    }

    override fun require(before: ITask) {
        taskDependencies.add(before)
    }
}