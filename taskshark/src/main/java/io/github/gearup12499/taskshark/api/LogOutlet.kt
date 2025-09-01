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
    abstract fun internalLog(message: String?, ex: Throwable?, level: Level)

    inline fun logDeferred(level: Level, ex: Throwable?, message: () -> String) {
        if (level.value < this.level.value) return
        internalLog(message(), ex, level)
    }

    fun log(level: Level, ex: Throwable?, message: String?) {
        if (level.value < this.level.value) return
        internalLog(message, ex, level)
    }

    open fun trace(message: String)
        = log(Level.Trace, null, message)
    inline fun trace(provider: () -> String)
        = logDeferred(Level.Trace, null, provider)
    open fun debug(message: String)
        = log(Level.Debug, null, message)
    inline fun debug(provider: () -> String)
        = logDeferred(Level.Debug, null, provider)
    open fun info(message: String)
        = log(Level.Info, null, message)
    inline fun info(provider: () -> String)
        = logDeferred(Level.Info, null, provider)
    open fun warn(message: String)
        = log(Level.Warn, null, message)
    inline fun warn(provider: () -> String)
        = logDeferred(Level.Warn, null, provider)
    open fun error(message: String)
        = log(Level.Error, null, message)
    inline fun error(provider: () -> String)
        = logDeferred(Level.Error, null, provider)
    open fun error(ex: Throwable, message: String)
        = log(Level.Error, ex, message)
    inline fun error(ex: Throwable, provider: () -> String)
        = logDeferred(Level.Error, ex, provider)
    open fun error(ex: Throwable)
        = log(Level.Error, ex, null)

    class DefaultLogOutlet : LogOutlet() {
        override var level = Level.Warn

        override fun internalLog(
            message: String?,
            ex: Throwable?,
            level: Level
        ) {
            println(buildString {
                this.append("[${level.name}] ")
                message?.let {
                    this.append(message)
                }
                if (message != null && ex != null)
                    this.append("\n")
                ex?.let {
                    this.append(ex.stackTraceToString())
                }
            })
        }

    }
}