package io.github.gearup12499.taskshark

abstract class Scheduler {
    val tasks = mutableMapOf<Int, ITask>()

    abstract fun register(task: ITask): Int

    open fun <T: ITask> add(task: T): T {
        task.register(this)
        return task
    }
}