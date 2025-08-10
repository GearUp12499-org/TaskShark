package io.github.gearup12499.taskshark

import java.lang.Thread.currentThread


/**
 * List of packages to exclude when considering what to name tasks by default.
 */
val systemPackages = mutableSetOf(
    PackageIdentifier::class.java.`package`.name,
    "java.",
    "javax.",
)

fun autoName(): String? {
    val stack = currentThread().stackTrace
    var targetFrame = stack[0] ?: return null
    for (frame in stack) {
        val className = frame.className
        if (systemPackages.any { className.startsWith(it) }) continue
        targetFrame = frame
        break
    }
    return buildString {
        append(targetFrame.fileName)
        append(" line ")
        append(targetFrame.lineNumber)
    }
}