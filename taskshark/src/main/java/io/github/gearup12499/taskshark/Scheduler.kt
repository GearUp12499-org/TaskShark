package io.github.gearup12499.taskshark

/**
 * An object that handles the various actions related to running [Tasks][ITask].
 *
 * #### **Documentation in this class is intended for *implementers* (people creating custom Schedulers).**
 *
 * If you're just looking to use the library, check out the [FastScheduler] documentation.
 */
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

    /**
     * Whether the scheduler may throw an exception for *double acquires*.
     *
     * A *double acquire* is when the scheduler has already committed to starting a task,
     * but one of the locks that the task requires has been taken by another task between the
     * time the lock status was checked and the time the lock was to be acquired.
     *
     * A *double require* error will usually result in a subsequent *double free* error; see [errorOnLockDoubleFree] to
     * control that behavior.
     */
    @JvmField var errorOnLockDoubleAcquire = true

    /**
     * Whether the scheduler may throw an exception for *double frees*.
     *
     * A *double free* is when the scheduler realizes that a lock that a task claims to depend on
     * was either acquired by another task somehow or never acquired by the task in question.
     * While this does not represent a complete failure in the locking system the way a double acquire does,
     * it still represents a logical failure somewhere in the system
     *
     * A common cause for this error is updating dependencies after the task has already started.
     */
    @JvmField var errorOnLockDoubleFree = true

    /**
     * Whether the scheduler may throw an exception for *double finalizations*.
     *
     * This occurs when [runTaskFinalizers] is called multiple times on the same Task.
     * If disabled, the operation will fail silently.
     */
    @JvmField var errorOnTaskDoubleFinalize = true
    @JvmField var errorOnNeverFinalized = true

    @JvmField val id = nextID++
    @JvmField val tasks = mutableMapOf<Int, ITask>()
    @JvmField protected val evalStack = ArrayDeque<ITask>()

    /**
     * Add the provided [task] to this [Scheduler] and assigns an ID. This is the second phase of registration,
     * the first phase being [io.github.gearup12499.taskshark.register].
     *
     * ## Users: do not call directly. Instead, use [add] to add a task.
     *
     * For implementers of [Scheduler]:
     *
     * **Do not** call [Task.register] in implementations; this will cause an infinite loop!
     *
     * @return the task's ID number in context.
     */
    abstract fun register(task: ITask): Int

    /**
     * Hints to schedulers that dependencies for this task have changed.
     */
    open fun resurvey(task: ITask) {}

    /**
     * Tries to start this Task if it is startable.
     */
    open fun refresh(task: ITask) {}

    /**
     * Adds an [ITask] to this scheduler.
     *
     * Internally, calls [io.github.gearup12499.taskshark.register].
     *
     * @return the passed task, for chaining
     */
    open fun <T: ITask> add(task: T): T {
        task.register(this)
        return task
    }

    fun addAll(vararg tasks: ITask) {
        for (t in tasks) add(t)
    }

    /**
     * Returns the current owner of a [Lock], or null if there is no current owner (i.e. it is released.)
     */
    abstract fun getLockOwner(lock: Lock): ITask?

    /**
     * Retrieve the list of "open evaluations" - a "mini call stack" containing the stack of
     * tasks that are actively running user code ([ITask.onStart], [ITask.onTick], [ITask.onFinish])
     *
     * [getCurrentEvaluation] returns the top (last) item of this "stack".
     */
    open fun getOpenEvaluations(): List<ITask> = evalStack.toList()

    /**
     * Retrieve the current "open evaluations" - the last task to start running user code ([ITask.onStart], [ITask.onTick], [ITask.onFinish]) in this context.
     *
     * [getOpenEvaluations] returns the entire stack of tasks that are actively running user code.
     */
    open fun getCurrentEvaluation(): ITask? = evalStack.lastOrNull()

    /**
     * Clean up after the provided task has finished. The task should have already been moved to one of the finish
     * states ([ITask.State.Finished] or [ITask.State.Cancelled]) before this method is called.
     *
     * This method is part of the public API because it is necessary for [ITask.stop] and other outside-of-scheduler
     * state operations.
     *
     * ### __Only call this method once on each task!__
     * Do not call directly except in custom [ITask] implementations; [Task] (the class) already handles this for you.
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

    abstract fun getTickCount(): Int
    abstract fun tick()
}