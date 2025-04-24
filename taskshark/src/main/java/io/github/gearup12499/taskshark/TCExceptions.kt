package io.github.gearup12499.taskshark

/**
 * Optimized exception class for Exception-as-control-flow
 */
open class FastException : RuntimeException("FastException <control flow>") {
    override fun fillInStackTrace(): Throwable? {
        return null
    }
}

internal class TaskStopException(val isCancellation: Boolean) : FastException()

