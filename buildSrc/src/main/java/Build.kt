object Build {
    private const val androidBuildToolsVersion = "7.1.1"
    private const val oneSignalPluginVersion = "0.13.4"
    private const val googleGmsVersion = "4.3.10"
    private const val firebaseCrashlyticsVersion = "2.7.1"


    const val androidBuildTools = "com.android.tools.build:gradle:$androidBuildToolsVersion"

    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}"

    const val googleGmsPlugin = "com.google.gms:google-services:${googleGmsVersion}"

    const val firebaseCrashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:${firebaseCrashlyticsVersion}"

    const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${UI.navigationVersion}"

    const val oneSignalPlugin = "gradle.plugin.com.onesignal:onesignal-gradle-plugin:${oneSignalPluginVersion}"

    const val hiltAndroidPlugin = "com.google.dagger:hilt-android-gradle-plugin:${Hilt.version}"

}