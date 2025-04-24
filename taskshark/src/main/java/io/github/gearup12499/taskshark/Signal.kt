package io.github.gearup12499.taskshark

open class Signal {
    @get:JvmName("isActive") var isActive = false; private set

    internal fun tryActivate() {
        isActive = true
    }

    @Throws(IllegalStateException::class)
    internal fun activate() {
        if (isActive) throw IllegalStateException("This signal is already open")
        tryActivate()
    }

    internal fun tryDeactivate() {
        isActive = false
    }

    @Throws(IllegalStateException::class)
    internal fun deactivate() {
        if (!isActive) throw IllegalStateException("This signal is already closed")
        tryDeactivate()
    }
}