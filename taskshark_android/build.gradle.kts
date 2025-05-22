plugins {
    id("com.android.library")
    kotlin("android")
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
}

dependencies {
    implementation(project(":taskshark"))
}