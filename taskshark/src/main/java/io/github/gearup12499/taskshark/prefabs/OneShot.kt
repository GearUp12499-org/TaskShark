package io.github.gearup12499.taskshark.prefabs

import io.github.gearup12499.taskshark.Task

open class OneShot(val action: Action) : Task<OneShot>() {
    fun interface Action {
        fun run()
    }

    override fun onStart() {
        action.run()
        finish()
    }

    override fun onTick() = true
}