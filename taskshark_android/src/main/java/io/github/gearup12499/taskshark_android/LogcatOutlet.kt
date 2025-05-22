package io.github.gearup12499.taskshark_android

import android.util.Log
import io.github.gearup12499.taskshark.api.LogOutlet

class LogcatOutlet : LogOutlet() {
    override var level = Level.Trace /* filtering can be handled by the logcat consumer */

    private val dispatch: Map<Level, (tag: String?, msg: String?, thr: Throwable?) -> Any?> = mapOf(
        Level.Trace to Log::v,
        Level.Debug to Log::d,
        Level.Info to Log::i,
        Level.Warn to Log::w,
        Level.Error to Log::e,
        Level.Nothing to { _, _, _ -> throw IllegalArgumentException("attempt to log at level Nothing") },
    )

    override fun internalLog(
        message: String?,
        ex: Throwable?,
        level: Level
    ) {
        dispatch[level]!!("TaskShark", message, ex)
    }
}