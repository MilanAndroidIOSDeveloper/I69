object Kotlin {
    const val version = "1.5.31"
    private const val coreKtxVersion = "1.6.0"
    private const val coroutines = "1.5.2"
    private const val roomVersion = "2.4.1"


    /// Kotlin
    const val coreKtx = "androidx.core:core-ktx:$coreKtxVersion"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib:$version"

    /// Coroutines
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    const val coroutinesPlayServices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines"

    /// Room
    const val room = "androidx.room:room-runtime:$roomVersion"
    const val roomCompiler = "androidx.room:room-compiler:$roomVersion"
    const val roomCoroutines = "androidx.room:room-ktx:$roomVersion"
}