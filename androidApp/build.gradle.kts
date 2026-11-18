plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "dev.eyeoverthink.bellgame.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.eyeoverthink.bellgame"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(project(":composeApp"))
}

ktlint {
    version.set(libs.versions.ktlint.get())
    android.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}
