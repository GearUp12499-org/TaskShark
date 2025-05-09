package io.github.gearup12499.taskshark.test

import io.github.gearup12499.taskshark.FastScheduler
import io.github.gearup12499.taskshark.api.LogOutlet
import io.github.gearup12499.taskshark.prefabs.Wait
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.markNow

object TestWait {
    const val DURATION = 10 // ms
    val durationType = DURATION.milliseconds

    @Test
    fun `test wait at least ms`() {
        val fs = FastScheduler()
        var end: TimeSource.Monotonic.ValueTimeMark? = null
        var start: TimeSource.Monotonic.ValueTimeMark? = null
        testing(fs) {
            fs.add(Wait.ms(DURATION))
                .then(RequireExecution())

            start = markNow()
            runToCompletion(fs)
            end = markNow()
        }
        assert((end!! - start!! - durationType).isPositive()) {
            "Too fast: actually took ${end - start} to complete; expected $durationType"
        }
    }

    const val MIN_SPIN_PER_SECOND = 5000 // Hz; minimum

    @Test
    fun `test spin perf`() {
        val fs = FastScheduler()
        var start: TimeSource.Monotonic.ValueTimeMark? = null
        var end: TimeSource.Monotonic.ValueTimeMark? = null
        testing(fs) {
            fs.add(Wait.ms(100))
                .then(RequireExecution())
            start = markNow()
            runToCompletion(fs, 1_000_000_000) // run max 1B steps
            end = markNow()
        }
        val duration = end!! - start!!
        val spinRate = fs.getTickCount() / duration.toDouble(DurationUnit.SECONDS)
        assert(spinRate >= MIN_SPIN_PER_SECOND) {
            ("FastScheduler isn't performant enough" +
                    " - expected at least ${MIN_SPIN_PER_SECOND}Hz, actually %.2fHz").format(spinRate)
        }
        LogOutlet.currentLogger.info {
            "Benchmark results: %.2fHz (minimum: ${MIN_SPIN_PER_SECOND}Hz)".format(spinRate)
        }
    }
}