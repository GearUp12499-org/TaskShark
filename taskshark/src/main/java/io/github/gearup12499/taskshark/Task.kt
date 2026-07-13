package io.github.gearup12499.taskshark

/**
 * base task use to create other tasks
 */
abstract class Task() {

    @JvmField protected var scheduler: Scheduler? = null

    abstract fun onStart()



    abstract fun onTick():Boolean



    abstract fun onFinish(completedNormally: Boolean)

    @JvmField
    protected val lockDependencies: MutableSet<Lock> = mutableSetOf()

    fun require(lock: Lock): Task{
        lockDependencies.add(lock)
        @Suppress("UNCHECKED_CAST")
        return this as Task
    }

    open fun dependedLocks(): Set<Lock> = lockDependencies

    @JvmField protected val taskDependencies: MutableSet<Task> = mutableSetOf()


    //TODO: make it so you can only call then after the add and not after the task
    //TODO: ex: you have to do sch.add(task()).then(task2()) and not sch.add(task().then(task2()))
    fun then(other: Task): Task{
        this.require(other)
        return other
    }

    fun require(before: Task): Task{
        taskDependencies.add(before)
        @Suppress("UNCHECKED_CAST")
        return this as Task
    }

    open fun dependedTasks(): Set<Task> = taskDependencies



}