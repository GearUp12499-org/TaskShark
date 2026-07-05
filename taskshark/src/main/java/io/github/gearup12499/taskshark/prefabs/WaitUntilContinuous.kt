package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task
import io.github.gearup12499.taskshark.systemPackages


class WaitUntilContinuous @JvmOverloads constructor(private val duration: Double, private val max: Double = -1.0, private val cond: Condition) : Task() {
    companion object {
        init {
            systemPackages.add(WaitUntilContinuous::class.qualifiedName!!)
        }
    }

    fun interface Condition {
        fun check(): Boolean
    }

    private var isMatching = false
    private var startedMatching = 0L
    private var started = 0L

    override fun onStart() {
        started = System.nanoTime()
    }

    override fun onTick(): Boolean {
        if (cond.check()) {
            val now = System.nanoTime()
            if (!isMatching) {
                isMatching = true
                startedMatching = now
            }
            if ((now - startedMatching) / 1.0e9 > duration) {
                return true
            }
            if (max > 0 && (now - started) / 1e9 > max) return true
        } else {
            if (isMatching) isMatching = false
        }
        return false
    }

    override fun onFinish(completedNormally: Boolean) {

    }
}