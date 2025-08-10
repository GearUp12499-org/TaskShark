package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task

/**
 * Simple task that repeatedly checks if the [condition] returns `true`.
 */
class WaitUntil(val condition: Condition) : Task<WaitUntil>() {
    override fun onTick(): Boolean = condition.check()

    fun interface Condition {
        fun check(): Boolean
    }
}