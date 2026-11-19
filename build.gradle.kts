plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

// Dipakai oleh modul :Bcore (mesin virtualisasi) lewat rootProject.ext
extra["compileSdkVersion"] = 35
extra["minSdk"] = 21
