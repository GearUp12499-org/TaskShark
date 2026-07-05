package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task

class SentinelTask: Task() {

    var canStart: Boolean = false

    override fun onStart() {

    }

    override fun onTick(): Boolean {
        return canStart
    }

    override fun onFinish(completedNormally: Boolean) {

    }
}