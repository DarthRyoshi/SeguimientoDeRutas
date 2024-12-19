// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
buildscript {
    repositories {
        google() // Repositorio de Google
        mavenCentral() // Repositorio Maven Central
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1") // Gradle para Android
        classpath("com.google.gms:google-services:4.4.0") // Plugin de Google Services
    }
}

