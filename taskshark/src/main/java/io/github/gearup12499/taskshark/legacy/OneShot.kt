package io.github.gearup12499.taskshark.legacy

import java.lang.Runnable

@Deprecated("the legacy api is deprecated")
class OneShot(scheduler: SchedulerOld, val target: () -> Unit) : TaskTemplate(scheduler) {
    constructor(scheduler: SchedulerOld, target: Runnable) : this(scheduler, target::run)

    override fun invokeOnStart() {
        target()
    }

    override fun invokeIsCompleted() = true
}