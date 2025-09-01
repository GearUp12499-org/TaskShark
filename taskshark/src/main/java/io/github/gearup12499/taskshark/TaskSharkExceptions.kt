package io.github.gearup12499.taskshark

/**
 * Optimized exception class for Exception-as-control-flow
 */
open class FastException : RuntimeException("FastException <control flow>") {
    override fun fillInStackTrace(): Throwable? {
        return null
    }
}

open class TaskSharkInternalException : RuntimeException {
    companion object {
        val ADDON = """
            !  TaskShark internal error. If you aren't messing with TaskShark's internals, this is a bug.
            !  Please report this error: https://github.com/GearUp12499-org/TaskShark/issues
            !  
        """.trimMargin("!")

        fun formatMessage(msg: String) = msg + "\n" + ADDON
    }

    constructor(message: String) : super(formatMessage(message))
    constructor(message: String, cause: Throwable) : super(formatMessage(message), cause)
    constructor(cause: Throwable) : super(ADDON, cause)
}

class TaskStopException() : FastException()

