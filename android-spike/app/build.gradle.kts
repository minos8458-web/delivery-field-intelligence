plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.dfi.spike"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dfi.spike"
        // minSdk 26: TYPE_APPLICATION_OVERLAY floor (O). Below O the spike's
        // overlay window type does not exist.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-c4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    // Deliberately minimal: no androidx, no Hilt, no Room, no WorkManager,
    // no CameraX, no networking. C4 validates interaction feasibility only.
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}
