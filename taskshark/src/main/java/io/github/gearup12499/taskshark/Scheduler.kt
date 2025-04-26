package io.github.gearup12499.taskshark

abstract class Scheduler {
    companion object {
        private var nextID = 0;
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return (super.hashCode() shl 8) + id
    }

    @JvmField var errorOnLockDoubleAcquire = true
    @JvmField var errorOnLockDoubleFree = true
    @JvmField var errorOnTaskDoubleFinalize = true
    @JvmField var errorOnNeverFinalized = true

    @JvmField val id = nextID++
    @JvmField val tasks = mutableMapOf<Int, ITask>()
    @JvmField protected val evalStack = ArrayDeque<ITask>()

    /**
     * Add the provided [task] to this [Scheduler].
     *
     * **Do not** call [Task.register] in implementations; this will cause an infinite loop!
     *
     * @return the task's ID number in context.
     */
    abstract fun register(task: ITask): Int

    /**
     * Hints to schedulers that preconditions and other configuration
     * options on this [ITask] may have changed.
     */
    open fun refresh(task: ITask) {}

    open fun <T: ITask> add(task: T): T {
        task.register(this)
        return task
    }

    open fun getOpenEvaluations(): ArrayDeque<ITask> = evalStack
    open fun getCurrentEvaluation(): ITask? = evalStack.lastOrNull()

    /**
     * Clean up after the provided task has finished. The task should have already been moved to one of the finish
     * states ([ITask.State.Finished] or [ITask.State.Cancelled]) before this method is called.
     *
     * This method is part of the public API because it is necessary for [ITask.stop] and other outside-of-scheduler
     * state operations.
     *
     * **Only call this method once on each task!**
     */
    abstract fun runTaskFinalizers(task: ITask)

    protected inline fun <T> using(t: ITask, block: () -> T): T? = using(t, block) { null }

    protected inline fun <T> using(t: ITask, block: () -> T, onStopped: () -> T?): T? {
        evalStack.addLast(t);
        return try {
            block()
        } catch (_: TaskStopException) {
            /* control flow jump target */
            onStopped()
        } finally {
            if (evalStack.removeLast() !== t) throw TaskSharkInternalException(
                "Evaluation stack failed: expected to remove $t, but got something else instead"
            )
        }
    }

    abstract fun tick()
}