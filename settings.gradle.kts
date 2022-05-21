    dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()

        maven { url = uri("https://jitpack.io") }
        jcenter()
        maven {
            url = uri("https://github.com/QuickBlox/quickblox-android-sdk-releases/raw/master/")
        }
    }
}

rootProject.name = "i69"
include(":app")