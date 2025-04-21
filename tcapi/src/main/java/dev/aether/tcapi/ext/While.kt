package dev.aether.tcapi.ext

import dev.aether.tcapi.legacy.SchedulerOld
import dev.aether.tcapi.legacy.TaskTemplate

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