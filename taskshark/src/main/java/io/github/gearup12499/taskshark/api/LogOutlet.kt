package io.github.gearup12499.taskshark.api

import org.intellij.lang.annotations.PrintFormat

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

    fun escape(rawMessage: String) = rawMessage.replace("%", "%%")

    fun log(level: Level, ex: Throwable?, @PrintFormat fmt: String?, vararg parameters: Any?) {
        if (level.value < this.level.value) return
        internalLog(fmt?.format(*parameters), ex, level)
    }

    open fun trace(message: String)
        = log(Level.Trace, null, escape(message))
    open fun trace(@PrintFormat fmt: String, first: Any?, vararg rest: Any?)
        = log(Level.Trace, null, fmt, *rest)
    open fun debug(message: String)
        = log(Level.Debug, null, escape(message))
    open fun debug(@PrintFormat fmt: String, first: Any?, vararg rest: Any?)
        = log(Level.Debug, null, fmt, *rest)
    open fun info(message: String)
        = log(Level.Info, null, escape(message))
    open fun info(@PrintFormat fmt: String, first: Any?, vararg rest: Any?)
        = log(Level.Info, null, fmt, *rest)
    open fun warn(message: String)
        = log(Level.Warn, null, escape(message))
    open fun warn(@PrintFormat fmt: String, first: Any?, vararg rest: Any?)
        = log(Level.Warn, null, fmt, *rest)
    open fun error(message: String)
        = log(Level.Error, null, escape(message))
    open fun error(@PrintFormat fmt: String, first: Any?, vararg rest: Any?)
        = log(Level.Error, null, fmt, *rest)
    open fun error(ex: Throwable, message: String)
        = log(Level.Error, ex, escape(message))
    open fun error(ex: Throwable, @PrintFormat fmt: String, first: Any?, vararg rest: Any?)
        = log(Level.Error, ex, fmt, *rest)
    open fun error(ex: Throwable)
        = log(Level.Error, ex, null)

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