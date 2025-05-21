package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * ## Are you sure this is what you want?
 * This waits for a fixed number of *scheduler ticks*, not a real duration; for that, use [Wait].
 */
open class WaitTicks(val duration: Int) : Task() {
    private var endsAt: Int = 0

    override fun onStart() {
       endsAt = scheduler!!.getTickCount() + duration
    }

    override fun onTick(): Boolean {
        return scheduler!!.getTickCount() >= endsAt
    }
}