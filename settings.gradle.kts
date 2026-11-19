pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Hanya untuk FreeReflection (com.github.tiann), dipakai oleh :Bcore
        maven {
            url = uri("https://www.jitpack.io")
            content { includeGroup("com.github.tiann") }
        }
    }
}

rootProject.name = "VLauncher"
include(":app")
include(":Bcore")
include(":black-reflection")
include(":compiler")
