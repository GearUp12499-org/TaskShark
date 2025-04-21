package dev.aether.tcapi.ext

import dev.aether.tcapi.legacy.SchedulerOld
import dev.aether.tcapi.legacy.TaskTemplate

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