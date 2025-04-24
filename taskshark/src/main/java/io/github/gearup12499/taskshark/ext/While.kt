package io.github.gearup12499.taskshark.ext

import io.github.gearup12499.taskshark.legacy.SchedulerOld
import io.github.gearup12499.taskshark.legacy.TaskTemplate

class While(
    scheduler: SchedulerOld,
    val condition: () -> Boolean,
    val action: () -> Unit
) : TaskTemplate(scheduler) {
    constructor(scheduler: SchedulerOld, condition: () -> Boolean, action: Runnable) : this(
        scheduler,
        condition,
        action::run
    )

    override fun invokeIsCompleted(): Boolean = !(condition())

    override fun invokeOnTick() = action()
}