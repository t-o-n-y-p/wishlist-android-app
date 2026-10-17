plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.openapi) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.ktfmt) apply false
}

buildscript {
    dependencies {
        classpath(libs.secrets.gradle.plugin)
        classpath(libs.ktfmt)
    }
}
