plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set(libs.versions.ktlint.get())
    android.set(false)
    ignoreFailures.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

detekt {
    config.setFrom(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}
