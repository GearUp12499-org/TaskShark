package io.github.gearup12499.taskshark

import io.github.gearup12499.taskshark.ITask.IllegalTransitionException
import io.github.gearup12499.taskshark.ITask.State
import io.github.gearup12499.taskshark.api.LogOutlet

abstract class Task<Self: Task<Self>> : ITask<Self> {
    /**
     * Helper type for anonymous extenders of Task that can't name themselves.
     */
    abstract class Anonymous : Task<Anonymous>()

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
        LogOutlet.currentLogger.debug {
            buildString {
                append("(${this@Task}) transition: $state -> $newState")
                scheduler?.let {
                    append(" (t = ${it.getTickCount()})")
                }
            }
        }
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

    open fun extendGetDependents(result: MutableSet<ITask<*>>) {}

    @JvmField protected val lockDependencies: MutableSet<Lock> = mutableSetOf()
    @JvmField protected val taskDependencies: MutableSet<ITask<*>> = mutableSetOf()

    override fun require(lock: Lock): Self {
        lockDependencies.add(lock)
        @Suppress("UNCHECKED_CAST")
        return this as Self
    }

    final override fun dependedLocks(): Set<Lock> = lockDependencies
    final override fun dependedTasks(): Set<ITask<*>> = taskDependencies

    @JvmField protected val dependents: MutableSet<ITask<*>> = mutableSetOf()
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
            // this really needs to run, it's the only thing saving us from
            // deadlocking if [onFinish] blows up
            scheduler?.runTaskFinalizers(this)
        }
    }

    override fun <T : ITask<T>> then(other: T): T {
        if (!other.isVirtual()) dependents.add(other)
        other.require(this)
        scheduler?.add(other)
        scheduler?.resurvey(other)
        return other
    }

    override fun require(before: ITask<*>): Self {
        taskDependencies.add(before)
        @Suppress("UNCHECKED_CAST")
        return this as Self
    }

    override fun toString() = "${this::class.simpleName ?: this::class.qualifiedName ?: "<?:Task>"}#$id"
}