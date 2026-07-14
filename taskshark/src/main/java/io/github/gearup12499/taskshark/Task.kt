package io.github.gearup12499.taskshark

/**
 * base task use to create other tasks
 */
abstract class Task {

    @JvmField protected var scheduler: Scheduler? = null

    /**
     * code that runs once when the task starts
     */

    abstract fun onStart()


    /**
     * code that runs every tick or loop until returns true
     */

    abstract fun onTick():Boolean

    /**
     * code that runs once when the task is finalizing, has completed normally to tell if it was canceled or not
     * true = finished normally
     * false = canceled
     * completed normally doesn't really have a function besides debugging
     */

    abstract fun onFinish(completedNormally: Boolean)

    /**
     * set of locks that the task requires
     * don't call lockDependencies because this is the one attached to the task
     */

    @JvmField
    protected val lockDependencies: MutableSet<Lock> = mutableSetOf()

    /**
     * add a lock the task will require
     */

    fun require(lock: Lock): Task{
        lockDependencies.add(lock)
        @Suppress("UNCHECKED_CAST")
        return this as Task
    }

    /**
     * returns the locks that this task depends on
     */

    open fun dependedLocks(): Set<Lock> = lockDependencies

    /**
     * set of task that this task requires before it can run
     * don't call taskDependencies because this is the one attached to the task
     */

    @JvmField protected val taskDependencies: MutableSet<Task> = mutableSetOf()

    /**
     * called to make task passed in wait for previous running task
     * then has to attach to the scheduler and not the task though
     * make sure to call sch.add(task).then(task2) and not sch.add(task.then(task2))
     */

    //TODO: make it so you can only call then after the add and not after the task
    //TODO: ex: you have to do sch.add(task()).then(task2()) and not sch.add(task().then(task2()))
    fun then(other: Task): Task{
        this.require(other)
        return other
    }

    /**
     * adds task to this task's dependencies
     * so this task won't run until passed in task is finished
     */

    fun require(before: Task): Task{
        taskDependencies.add(before)
        @Suppress("UNCHECKED_CAST")
        return this as Task
    }

    /**
     * returns the tasks that this task depends on
     */

    open fun dependedTasks(): Set<Task> = taskDependencies



}