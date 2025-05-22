package io.github.gearup12499.taskshark_android

import io.github.gearup12499.taskshark.api.LogOutlet

object TaskSharkAndroid {
    @JvmStatic fun setupLogging() {
        LogOutlet.currentLogger = LogcatOutlet()
    }
}