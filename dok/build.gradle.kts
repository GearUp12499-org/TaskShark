plugins {
    kotlin("jvm") apply false
    alias(libs.plugins.dokka)
}

val libraryVersion = run {
    val version: String by project
    version
}

dependencies {
    // Live projects
    dokka(project(":taskshark"))
    dokka(project(":taskshark_android"))

    // Historical versions
    dokkaHtmlPlugin(libs.dokkaVersioning)
}

val previousVersions = rootProject.layout.buildDirectory.dir("docVersionsActive")

dokka {
    moduleName = "TaskShark"

    pluginsConfiguration {
        versioning {
            version = libraryVersion
            val dir = previousVersions.get()
            if (dir.asFile.exists()) {
                olderVersionsDir = dir
            }
        }
    }
}