package io.github.gearup12499.taskshark_android

import io.github.gearup12499.taskshark.api.LogOutlet
import io.github.gearup12499.taskshark.systemPackages

object TaskSharkAndroid {
    @JvmStatic fun setup() {
        LogOutlet.currentLogger = LogcatOutlet()
        systemPackages.add("android.")
    }
}