package io.github.gearup12499.taskshark.api

abstract class LogOutlet {
    companion object {
        @JvmStatic var currentLogger: LogOutlet = DefaultLogOutlet()
    }

    enum class Level(val value: Int) {
        Trace(0),
        Debug(1),
        Info(2),
        Warn(3),
        Error(4),
        Nothing(5);
    }

    abstract var level: Level
    protected abstract fun internalLog(message: String?, ex: Throwable?, level: Level)

    fun log(message: String?, ex: Throwable?, level: Level) {
        if (level.value < this.level.value) return
        internalLog(message, ex, level)
    }

    open fun trace(message: String) = log(message, null, Level.Trace)
    open fun debug(message: String) = log(message, null, Level.Debug)
    open fun info(message: String) = log(message, null, Level.Info)
    open fun warn(message: String) = log(message, null, Level.Warn)
    open fun error(message: String) = log(message, null, Level.Error)
    open fun error(message: String, ex: Throwable) = log(message, ex, Level.Error)
    open fun error(ex: Throwable) = log(null, ex, Level.Error)

    class DefaultLogOutlet : LogOutlet() {
        override var level = Level.Nothing

        override fun internalLog(
            message: String?,
            ex: Throwable?,
            level: Level
        ) {
            println(buildString {
                this.append("[${level.name}] ")
                message?.let {
                    this.append(message)
                    this.append("\n")
                }
                ex?.let {
                    this.append(ex.stackTraceToString())
                }
            })
        }

    }
}