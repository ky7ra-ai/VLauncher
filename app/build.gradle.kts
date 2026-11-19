plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.vlauncher"
    compileSdk = 35
    ndkVersion = "29.0.13846066"

    defaultConfig {
        applicationId = "com.example.vlauncher"
        minSdk = 21
        // Sengaja 28: mesin virtualisasi (Bcore) butuh target rendah agar hook
        // framework tidak terkena pembatasan target SDK baru. Konsekuensinya app
        // ini tidak bisa dipublikasikan ke Google Play, hanya sideload.
        targetSdk = 28
        versionCode = 2
        versionName = "0.2.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(project(":Bcore"))
}
