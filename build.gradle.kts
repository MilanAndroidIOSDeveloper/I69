buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()

        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath(Build.androidBuildTools)
        classpath(Build.kotlinPlugin)
        classpath(Build.googleGmsPlugin)
        classpath(Build.firebaseCrashlyticsPlugin)
        classpath(Build.navigationSafeArgs)
        classpath(Build.hiltAndroidPlugin)
        classpath(Build.oneSignalPlugin)
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

plugins {
    id("com.apollographql.apollo3").version("3.2.2").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}