rootProject.name = "BellGame"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_PROJECT lets the Kotlin/Wasm plugin add the Node.js distribution
    // repository. The app itself never calls the network at runtime.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

include(":composeApp")
include(":androidApp")
