package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

open class Wait(val duration: Duration) : Task() {
    companion object {
        @JvmStatic fun s(seconds: Number) = Wait(seconds.toDouble().seconds)
        @JvmStatic fun ms(millis: Double) = Wait(millis.milliseconds)
        @JvmStatic fun ms(millis: Long) = Wait(millis.milliseconds)
        @JvmStatic fun ms(millis: Int) = Wait(millis.milliseconds)
    }

    private var endsAt: TimeSource.Monotonic.ValueTimeMark? = null

    override fun onStart() {
       endsAt = TimeSource.Monotonic.markNow() + duration
    }

    override fun onTick(): Boolean {
        return TimeSource.Monotonic.markNow() >= endsAt!!
    }
}