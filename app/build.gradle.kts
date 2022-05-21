import Others.exoplayer
import com.android.build.gradle.tasks.generatePrefabPackages

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("com.onesignal.androidsdk.onesignal-gradle-plugin")
    id ("com.apollographql.apollo3")
}

android {
    compileSdk = Android.compileSdk
    buildToolsVersion = Android.buildTools

    signingConfigs {
        create("release") {
            storeFile = file("/Users/smimranahmed/Desktop/i69_keystore.jks")
            storePassword = "Tr@Yv*BJ46L"
            keyAlias = "i69sasu"
            keyPassword = "Tr@Yv*BJ46L"
        }
        /*getByName("debug"){
            storeFile = file("C:\\Users\\sures\\Desktop\\i69_keystore.jks")
            storePassword = "Tr@Yv*BJ46L"
            keyAlias = "i69sasu"
            keyPassword = "Tr@Yv*BJ46L"
        }*/
    }

    defaultConfig {
        applicationId = Android.appId
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            /*isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )*/


            signingConfig = signingConfigs.getByName("release")
            isDebuggable = true


//            buildConfigField("String", "BASE_URL", "\"https://api.i69app.com/\"")
            buildConfigField("String", "BASE_URL", "\"https://admin.chatadmin-mod.click/\"")
//            buildConfigField("String", "BASE_URL", "\"http://95.216.208.1:8000/\"")
//                        buildConfigField("String", "BASE_URL", "\"https://sandbox-api.i69app.com/\"")


        }
        debug {
//            buildConfigField("String", "BASE_URL", "\"https://api.i69app.com/\"")

            buildConfigField("String", "BASE_URL", "\"https://admin.chatadmin-mod.click/\"")
//            buildConfigField("String", "BASE_URL", "\"http://95.216.208.1:8000/\"")
//            buildConfigField("String", "BASE_URL", "\"https://sandbox-api.i69app.com/\"")


        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
    }

    packagingOptions {
        resources.excludes += "META-INF/library_release.kotlin_module"
    }

    kapt {
        correctErrorTypes = true
    }

    lint {
        //isAbortOnError = true
        //isExplainIssues = true
    }

}

dependencies {
    // Kotlin
    implementation(Kotlin.coreKtx)
    implementation(Kotlin.kotlinStdLib)
    implementation(Kotlin.coroutinesCore)
    implementation(Kotlin.coroutinesAndroid)
    implementation(Kotlin.coroutinesPlayServices)

    // UI
    implementation(UI.appCompat)
    implementation(UI.activity)
    implementation(UI.fragment)
    implementation(UI.constraintLayout)
    implementation(UI.material)
    implementation(UI.legacySupport)
    implementation(UI.recyclerView)
    implementation(UI.datastorePref)

    // Navigation
    implementation(UI.navigationFragment)
    implementation(UI.navigationUI)

    // Lifecycle
    implementation(Lifecycle.runtime)
    implementation(Lifecycle.viewModel)
    implementation(Lifecycle.liveData)
    implementation(Lifecycle.common)
    implementation(Lifecycle.process)

    // Firebase
    implementation(platform(Firebase.firebaseBom))
    implementation(Firebase.analytics)
    implementation(Firebase.crashlytics)

    // Login
    implementation(Login.google)
    implementation(Login.facebook)

    // Retrofit
    implementation(Others.retrofit)
    implementation(Others.gsonConverter)
    implementation(Others.scalarsCibverter)
    implementation(Others.loggingInterceptor)

//    // Firebase Cloud Messaging (Kotlin)
    implementation(Others.firebasecrashanalytics)
    implementation(Others.firebaseanalytic)
    implementation(Others.firebasemsg)

    // Glide
    implementation(Others.glide)
    kapt(Others.glideCompiler)
    implementation(Others.glideAnnotation)
    implementation(Others.glideHttp)


    // Quickblox
    implementation(Quickblox.messages)
    implementation(Quickblox.chat)
    implementation(Quickblox.content)

    // Lottie
    implementation(Others.lottie)

    // Timber
    implementation(Others.timber)

    // Hilt
    implementation(Hilt.android)
    kapt(Hilt.compiler)

    // Google Libraries
    implementation(Google.location)
    implementation(Google.billing)
    implementation(Google.playCore)

    // Expandable Layout
    implementation(Others.expandableLayout)

    // Chat UI
    implementation(Others.chatKit)
    implementation(Others.pixImagePicker)
    implementation(Others.carousel)
    implementation(Others.permissions)

    // One Signal
    implementation(Others.oneSignal)

    // Worker
    implementation(Others.runtimeWorker)

    // Local Unit Tests
    testImplementation(Testing.junit)
    testImplementation(Testing.truth)
    testImplementation(Testing.archCore)
    testImplementation(HiltTest.hiltAndroidTesting)
    kaptTest(Hilt.compiler)

    // Instrumentation Tests
    androidTestImplementation(Testing.junitAndroid)
    androidTestImplementation(Testing.espresso)
    androidTestImplementation(Testing.truth)
    androidTestImplementation(Testing.archCore)
    androidTestImplementation(HiltTest.hiltAndroidTesting)
    kaptAndroidTest(Hilt.compiler)

    // Room
    implementation(Kotlin.room)
    implementation(Kotlin.roomCoroutines)
    kapt(Kotlin.roomCompiler)

    implementation(exoplayer)




    implementation ("com.nshmura:recyclertablayout:1.5.0")


    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    // The core runtime dependencies
    implementation("com.apollographql.apollo3:apollo-runtime:3.2.2")
    implementation("com.apollographql.apollo3:apollo-api:3.2.2")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    //sdp
    implementation ("com.intuit.sdp:sdp-android:1.0.6")

    debugImplementation("com.amitshekhar.android:debug-db:1.0.6")
}

apollo {
    mapScalarToUpload("Upload")
    packageName.set("com.i69app")
    generateKotlinModels.set(true)

}