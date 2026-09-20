plugins {
    id("com.android.application")
}

android {
    namespace = "com.scifigalaxy.hindibookmakerpdf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.scifigalaxy.hindibookmakerpdf"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
}
