plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "io.github.penguinencounter.task_conductor"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

sourceSets.main {
    java.srcDirs("src/main/java")
    kotlin.srcDirs("src/main/java")
}

sourceSets.test {
    java.srcDirs("src/test/java")
    kotlin.srcDirs("src/test/java")
}
