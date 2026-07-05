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
     */
    fun stopUsing(lock: Lock) {
        val correlatedJob = locks[lock]
        if (correlatedJob != null) {
            cancel(correlatedJob)
        }
    }

    /**
     * checks each lock associated with a task and if the lock is already held will throw DoubleAcquire error
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
     */
    private fun checkLocks(task: Task){
       task.dependedLocks().forEach{
            stopUsing(it)
       }
    }

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

    fun cancel(job: Job){
        val correlatedTask = activeTasks2[job]
        scope.launch{
            correlatedTask?.onFinish(false)
        }
        job.cancel()
        runTaskFinalizers(correlatedTask!!, true)
    }

    fun add(task: Task): Task {
        queuedTasks.add(task)
        return task
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
    fun tick(){
        LogOutlet.currentLogger.trace{
            "($this) --- TICK #$tickCount END ----"
        }
        tickCount++
        processWaiting()
        dispatcher.runTasks()
    }
}