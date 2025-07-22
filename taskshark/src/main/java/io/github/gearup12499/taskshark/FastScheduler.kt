package io.github.gearup12499.taskshark

import io.github.gearup12499.taskshark.api.LogOutlet

/**
 * FastScheduler doesn't care about your "priority". Who needs that anyway?
 */
open class FastScheduler() : Scheduler() {
    @JvmField
    protected var nextId = 0
    protected val activeWaiting: MutableList<ITask> = mutableListOf()
    protected val activeTicking: MutableList<ITask> = mutableListOf()
    protected val locks: MutableMap<Lock, ITask> = mutableMapOf()
    protected val lockReleaseNotify: MutableMap<Lock, MutableList<ITask>> = mutableMapOf()
    private val disposed: MutableSet<ITask> = mutableSetOf()

    protected val taskDependencies: MutableMap<ITask, MutableSet<ITask>> = mutableMapOf()

    protected open fun surveyTaskPreconditions(task: ITask): Boolean {
        val requires = task.dependedTasks().filter {
            val state = it.getState()
            when (state) {
                ITask.State.Finished -> false
                ITask.State.Cancelled -> {
                    // this task can't start anyway...!
                    task.stop(true)
                    return@surveyTaskPreconditions false
                }

                else -> true
            }
        }.toMutableSet()
        taskDependencies[task] = requires
        return requires.isEmpty()
    }

    private fun acquireAllLocks(task: ITask) {
        task.dependedLocks().forEach {
            if (errorOnLockDoubleAcquire) assert(locks[it] == null || locks[it] === task) {
                "Trying to acquire lock $it, but it's already owned by a different task: ${locks[it]}"
            }
            locks[it] = task
            LogOutlet.currentLogger.debug {
                "($this) acquired lock ${it.getFriendlyName()} for $task"
            }
        }
    }

    private fun releaseAllLocks(task: ITask) {
        task.dependedLocks().forEach {
            if (errorOnLockDoubleFree) assert(locks[it] === task) {
                val currentOwner = locks[it]
                if (currentOwner == null) "Trying to release lock $it, but it's already free!"
                else "Trying to release lock $it, but it's already held by $currentOwner"
            }
            // release
            locks.remove(it)
            LogOutlet.currentLogger.debug {
                "($this) released lock ${it.getFriendlyName()} for $task"
            }
        }
    }

    private fun notifyAllLocks(task: ITask) {
        task.dependedLocks().forEach {
            // Skip already owned locks because there's no way we need to notify again for those
            if (locks[it] != null) return@forEach

            // Notify dependents...
            val notifyListIter = lockReleaseNotify[it]?.sortedWith(ITask.COMPARE_PRIORITY)
            if (notifyListIter == null) return@forEach
            processTasks@ for (task in notifyListIter) {
                when (refreshInternal(task)) {
                    RefreshResult.Died, RefreshResult.Started -> {
                        // if Died, then the lock has already been notified a second time
                        // then we'd be liable to try to refresh it after it's started and that's bad
                        // if Started, then this lock is already taken, so that's that
                        break@processTasks
                    }

                    RefreshResult.NotStarted, RefreshResult.AlreadyStarted -> {}
                }
            }
        }
    }

    private fun notifyDependents(task: ITask) {
        val state = task.getState()
        task.getDependents().forEach {
            val deps = taskDependencies[it]
                ?: throw TaskSharkInternalException("Scheduler failed to set up dependency tree for $it (dependent) before $task finished")
            when (state) {
                ITask.State.Finished -> {
                    deps.remove(task)
                    LogOutlet.currentLogger.debug {
                        "($this) ($task finalization) notify dependent: $it; ${deps.size} left"
                    }
                    if (deps.isEmpty()) refreshInternal(it)
                }

                ITask.State.Cancelled -> task.stop()
                else -> throw IllegalStateException(
                    "Task $task isn't in finished or cancelled state during finalization process, " +
                            "needed to determine what to do with dependent tasks"
                )
            }
        }
    }

    fun getTaskMissingLocks(task: ITask): Set<Lock> {
        return task.dependedLocks().filter {
            val currentState = locks[it]
            currentState != null && currentState != task
        }.toMutableSet()
    }

    override fun runTaskFinalizers(task: ITask) {
        LogOutlet.currentLogger.debug {
            "($this) Finalizing task: $task"
        }
        if (disposed.contains(task)) {
            if (errorOnTaskDoubleFinalize) assert(false) {
                "Attempt to finalize a task that's already been finalized: $task"
            }
            return
        }
        disposed.add(task)
        releaseAllLocks(task)
        notifyDependents(task)
        notifyAllLocks(task)
        LogOutlet.currentLogger.debug {
            "($this) Finalize task completed: $task"
        }
    }

    protected open fun lifecycleFinishTask(task: ITask) {
        try {
            LogOutlet.currentLogger.debug {
                "($this) lifecycleFinishTask: $task"
            }
            activeTicking.remove(task)
            task.transition(ITask.State.Finishing)
            using(task, { task.onFinish(true) }, { return@lifecycleFinishTask })
            task.transition(ITask.State.Finished)
        } finally {
            if (!disposed.contains(task)) runTaskFinalizers(task)
        }
    }

    protected open fun lifecycleTickTask(task: ITask) {
        if (using(task, { task.onTick() }, { return@lifecycleTickTask }) ?: false) {
            lifecycleFinishTask(task)
        }
    }

    protected open fun lifecycleBeginTask(task: ITask) {
        assert(task.getState() == ITask.State.NotStarted) {
            "Can't begin a task that has already begun"
        }
        LogOutlet.currentLogger.debug {
            "($this) lifecycleBeginTask: $task"
        }
        task.transition(ITask.State.Starting)
        acquireAllLocks(task)
        using(task, { task.onStart() }, { return@lifecycleBeginTask })
        // do one tick now, potentially cascading into a finish / notify / begin...
        LogOutlet.currentLogger.debug {
            "($this) preempting first tick for $task..."
        }
        task.transition(ITask.State.Ticking)
        activeTicking.add(task)
        lifecycleTickTask(task)
    }

    protected enum class RefreshResult {
        AlreadyStarted,
        NotStarted,
        Started,
        Died,
    }

    protected fun refreshInternal(task: ITask): RefreshResult {
        if (task.getState() != ITask.State.NotStarted) return RefreshResult.AlreadyStarted
        LogOutlet.currentLogger.debug {
            "($this) Checking if $task is startable..."
        }
        // remove it from the active waiting list, if possible (we're going to put them back if they're eligible)
        activeWaiting.remove(task)
        // do a quick survey of the task conditions...
        if (!surveyTaskPreconditions(task)) return RefreshResult.NotStarted
        val locks = getTaskMissingLocks(task)
        if (locks.isEmpty()) {
            // The task might be able to start now
            return when (using(task) { task.canStart() }) {
                true -> {
                    lifecycleBeginTask(task)
                    when (task.getState()) {
                        ITask.State.Finished, ITask.State.Cancelled -> {
                            if (errorOnNeverFinalized && !disposed.contains(task)) assert(false) {
                                "Expected a 'finished' or 'completed' task $task to be finalized, but it wasn't"
                            }
                            RefreshResult.Died
                        }

                        else -> RefreshResult.Started
                    }
                }

                false -> {
                    // Hmm, maybe later
                    activeWaiting.add(task)
                    RefreshResult.NotStarted
                }

                null -> RefreshResult.Died
            }
        } else {
            // we don't need to pay attention :P
            // we need **all** of these, so just pick the first one and queue it up
            LogOutlet.currentLogger.debug {
                "($this) $task is now sleeping (waiting for locks)"
            }
            val lock = locks.first()
            lockReleaseNotify.putIfAbsent(lock, mutableListOf())
            val notifyList = lockReleaseNotify[lock]!!
            notifyList.add(task)
            return RefreshResult.NotStarted
        }
    }

    override fun refresh(task: ITask) {
        refreshInternal(task)
    }

    override fun resurvey(task: ITask) {
        if (tasks.containsValue(task)) surveyTaskPreconditions(task)
        else if (!task.isVirtual()) throw IllegalArgumentException("The provided task ($task) is not registered in this scheduler ($this).")
    }

    override fun getLockOwner(lock: Lock) = locks[lock]

    /**
     * @suppress
     */
    override fun register(task: ITask): Int {
        val id = nextId++
        tasks.put(id, task)
        // note: putting [refresh] in here violates some expectations about side effects during the construction phase
        // instead, slap it on the queue (even if for only one tick) and make the dependency tree
        surveyTaskPreconditions(task)
        activeWaiting.add(task)
        return id
    }

    protected open fun processWaiting() {
        // make a copy to avoid CMEs; this might be a bit expensive unfortunately
        for (task in activeWaiting.toList()) {
            when (using(task) { task.canStart() }) {
                true -> {
                    refreshInternal(task)
                } // do a full check
                false -> {}
                null -> activeWaiting.remove(task)
            }
        }
    }

    protected open fun processTicking() {
        for (task in activeTicking.toList()) {
            lifecycleTickTask(task)
        }
    }

    private var tickCount = 0
    override fun tick() {
        LogOutlet.currentLogger.trace {
            "($this) ---- TICK #$tickCount BEGIN ----"
        }
        processWaiting()
        processTicking()
        LogOutlet.currentLogger.trace {
            "($this) ----  TICK #$tickCount END  ----"
        }
        tickCount++
    }

    override fun getTickCount() = tickCount

    override fun toString(): String = "FastScheduler#$id"
}