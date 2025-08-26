plugins {
    id("com.android.library")
    kotlin("android")
    id("taskshark.buildsrc.publish")
    alias(libs.plugins.dokka)
}

val groupId: String by project
group = groupId
val libraryVersion = run {
    val version: String by project
    version
}

kotlin {
    jvmToolchain(17)
}

//tasks.withType<KotlinCompile> {
//    compilerOptions {
//        freeCompilerArgs.add("-Xjvm-default=all")
//    }
//}

android {
    namespace = "io.github.gearup12499.taskshark_android"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {}
    }
}

publishing {
    publications {
        register<MavenPublication>("androidRelease") {
            artifactId = "taskshark-android"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    api(project(":taskshark"))
}