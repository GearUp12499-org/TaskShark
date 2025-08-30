package io.github.gearup12499.taskshark.api

import io.github.gearup12499.taskshark.ITask

interface SupportsAdd {
    fun <T: ITask<*>> add(task: T): T
}