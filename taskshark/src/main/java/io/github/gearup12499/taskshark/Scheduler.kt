package io.github.gearup12499.taskshark

import io.github.gearup12499.taskshark.api.LogOutlet
import kotlinx.coroutines.*
import kotlin.collections.forEach

/**
Object used to handle the scheduling of various tasks
 */

open class Scheduler {

    protected val queuedTasks = mutableListOf<Task>()

    val activeTasks: MutableMap<Task, Job> = mutableMapOf()

    val activeTasks2: MutableMap<Job, Task> = mutableMapOf()
    protected val locks: MutableMap<Lock, Job> =  mutableMapOf()

    protected val dispatcher = Dispatcher()
    protected val scope = CoroutineScope(dispatcher)

    @JvmField var errorOnLockDoubleAcquire = true
    @JvmField var errorOnLockDoubleFree= true

    /**
     * whatever task is using the given lock will be canceled
     * lock is the Lock that you want to cancel
     */
    fun stopUsing(lock: Lock) {
        val correlatedJob = locks[lock]
        if (correlatedJob != null) {
            cancel(correlatedJob)
        }
    }

    /**
     * checks each lock associated with a task and if the lock is already held will throw DoubleAcquire error
     * if all locks are free, task will hold all the locks it requires
     * task is the Task that is checked
     */
    private fun acquireAllLocks(task: Task){
        task.dependedLocks().forEach{
            if (errorOnLockDoubleAcquire) assert(locks[it] == null || locks[it] === task) {
                "Trying to acquire lock $it, but it's already owned by a different task: ${locks[it]}"
            }
            locks[it] = activeTasks[task]!!
            LogOutlet.currentLogger.debug {
                "($this) acquired lock ${it.getFriendlyName()} for $task"
            }
        }
    }

    /**
     * auto cancels whatever tasks are already holding locks that given task needs
     * task is Task that will override the other task currently holding lock
     */
    private fun checkLocks(task: Task){
       task.dependedLocks().forEach{
            stopUsing(it)
       }
    }

    /**
     * for each lock associated with this task, the task will let go of the lock
     * if lock is alr free or another task is holding the lock, will throw a double free error
     * task is Task that you want to let go of locks
     */

    private fun releaseAllLocks(task: Task) {
        val job = activeTasks[task]
        task.dependedLocks().forEach {
            if (errorOnLockDoubleFree) assert(locks[it] === job) {
                val currentOwner = locks[it]
                if (currentOwner == null) "Trying to release lock ${it.getFriendlyName()}, but it's already free!"
                else "Trying to release lock ${it.getFriendlyName()}, but it's already held by $currentOwner"
            }
            // release
            locks.remove(it)
            LogOutlet.currentLogger.debug {
                "($this) released lock ${it.getFriendlyName()} for $task"
            }
        }
    }

    /**
     * notifies whatever tasks that depend on the passed in task that it can start running after passed in task is finished
     * task is Task that is waiting for the depended on task
     */
    fun notifyDependents(task: Task){
        task.dependedTasks().forEach {
            val job = activeTasks[task]
            scope.launch{
                job?.join()
                add(it)
            }
        }
    }

    /**
     * finishes the task by releasing all it's locks and removing task from active task list
     */
    fun runTaskFinalizers(task: Task, wasRunning:Boolean){
        LogOutlet.currentLogger.debug {
            "($this) Finalizing task: $task${if (!wasRunning) " [not running]" else ""}"
        }
        val correlatedJob = activeTasks[task]
        if(wasRunning) releaseAllLocks(task)
        activeTasks.remove(task)
        activeTasks2.remove(correlatedJob)
        LogOutlet.currentLogger.debug {
            "($this) Finalize task completed: $task"
        }
    }

    /**
     * force cancels the task given
     * job is the wrapped coroutine associated with the task
     */
    fun cancel(job: Job){
        val correlatedTask = activeTasks2[job]
        scope.launch{
            correlatedTask?.onFinish(false)
        }
        job.cancel()
        runTaskFinalizers(correlatedTask!!, true)
    }

    /**
     * adds current task to queue of Tasks
     */

    fun add(task: Task): Task {
        queuedTasks.add(task)
        println(task)
        return task
    }

    /**
     * returns the task that owns the lock
     */

    fun getLockOwner(lock: Lock): Job?{
        return locks[lock]
    }

    /**
     * wraps the passed in task into a coroutine that yields every loop until onTick is true
     */
    protected open fun register(task: Task): Job {
        return scope.launch {
            task.onStart()

            while (!task.onTick()) {
                yield()
            }


            task.onFinish(true)
            runTaskFinalizers(task, true)
        }
    }

    /**
     * adds tasks that can be started into the active ticking list
     * also checks and acquires all the locks it needs
     */

    protected fun processWaiting(){
        for (task in queuedTasks.toList()){
                val job = register(task)
                queuedTasks.remove(task)
                activeTasks[task] = job
                activeTasks2[job] = task
                checkLocks(task)
                acquireAllLocks(task)
                if(!task.dependedTasks().isEmpty()){
                    notifyDependents(task)
            }
        }
    }

    open var tickCount = 0

    /**
     * processes the waiting tasks
     * tells dispatcher to cycle through each coroutine again
     */
    fun tick(){
        LogOutlet.currentLogger.trace{
            "($this) --- TICK #$tickCount END ----"
        }
        tickCount++
        processWaiting()
        dispatcher.runTasks()
    }
}