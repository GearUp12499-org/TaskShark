plugins {
    id("taskshark.buildsrc.kotlin-jvm")
    alias(libs.plugins.dokka)
}

val groupId: String by project
group = groupId
val libraryVersion = run {
    val version: String by project
    version
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    dokkaHtmlPlugin(libs.dokkaVersioning)
}

publishing {
    publications {
        create<MavenPublication>("taskshark") {
            artifactId = "taskshark"
            version = libraryVersion
            from(components["java"])
        }
    }
}

sourceSets.main {
    java.srcDirs("src/main/java")
    kotlin.srcDirs("src/main/java")
}

sourceSets.test {
    java.srcDirs("src/test/java")
    kotlin.srcDirs("src/test/java")
}
