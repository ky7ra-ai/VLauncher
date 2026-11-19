pluginManagement {
    repositories {
        maven { url = uri("https://www.jitpack.io") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://www.jitpack.io") }
        google()
        mavenCentral()
    }
}

rootProject.name = "VLauncher"
include(":app")
include(":Bcore")
include(":black-reflection")
include(":compiler")
