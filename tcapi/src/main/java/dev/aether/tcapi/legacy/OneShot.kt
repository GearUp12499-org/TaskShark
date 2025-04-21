package dev.aether.tcapi.legacy

import java.lang.Runnable

class OneShot(scheduler: SchedulerOld, val target: () -> Unit) : TaskTemplate(scheduler) {
    constructor(scheduler: SchedulerOld, target: Runnable) : this(scheduler, target::run)

    override fun invokeOnStart() {
        target()
    }

    override fun invokeIsCompleted() = true
}