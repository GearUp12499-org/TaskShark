package taskshark.buildsrc

plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "BuildLocal"
            url = uri(rootProject.layout.buildDirectory.dir("repo"))
        }
    }
}
