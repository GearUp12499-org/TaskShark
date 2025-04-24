@file:Suppress("unused")

package io.github.gearup12499.taskshark.ext

import io.github.gearup12499.taskshark.legacy.Runnable
import io.github.gearup12499.taskshark.legacy.Task
import io.github.gearup12499.taskshark.legacy.TaskAction1
import io.github.gearup12499.taskshark.legacy.TaskAction2
import io.github.gearup12499.taskshark.legacy.TaskQuery1
import io.github.gearup12499.taskshark.legacy.TaskQuery2

fun Task.extendOnStart(other: Runnable) {
    val original = this.onStart
    onStart { a, b ->
        original(a, b)
        other()
    }
}
fun Task.extendOnStart(other: TaskAction1) {
    val original = this.onStart
    onStart { a, b ->
        original(a, b)
        other(a)
    }
}
fun Task.extendOnStart(other: TaskAction2) {
    val original = this.onStart
    onStart { a, b ->
        original(a, b)
        other(a, b)
    }
}

fun Task.isCompletedAnd(other: () -> Boolean) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) && other()
    }
}
fun Task.isCompletedAnd(other: TaskQuery1<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) && other(a)
    }
}
fun Task.isCompletedAnd(other: TaskQuery2<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) && other(a, b)
    }
}

fun Task.isCompletedOr(other: () -> Boolean) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) || other()
    }
}
fun Task.isCompletedOr(other: TaskQuery1<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) || other(a)
    }
}
fun Task.isCompletedOr(other: TaskQuery2<Boolean>) {
    val original = this.isCompleted
    isCompleted { a, b ->
        original(a, b) || other(a, b)
    }
}

fun Task.canStartAnd(other: () -> Boolean) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other()
    }
}
fun Task.canStartAnd(other: TaskQuery1<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a)
    }
}
fun Task.canStartAnd(other: TaskQuery2<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a, b)
    }
}

fun Task.canStartOr(other: () -> Boolean) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other()
    }
}
fun Task.canStartOr(other: TaskQuery1<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a)
    }
}
fun Task.canStartOr(other: TaskQuery2<Boolean>) {
    val original = this.canStart
    canStart { a, b ->
        original(a, b) || other(a, b)
    }
}
