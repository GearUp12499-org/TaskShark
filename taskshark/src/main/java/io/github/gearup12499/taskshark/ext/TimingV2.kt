package io.github.gearup12499.taskshark.ext

import io.github.gearup12499.taskshark.legacy.SchedulerOld
import io.github.gearup12499.taskshark.legacy.TaskTemplate

class Pause(scheduler: SchedulerOld, val seconds: Double) : TaskTemplate(scheduler) {
    private var startTime: Long = 0
    override fun invokeOnStart() {
        startTime = System.nanoTime()
    }

    override fun invokeIsCompleted(): Boolean {
        val now = System.nanoTime()
        return (now - startTime) / 1e9 >= seconds
    }
}