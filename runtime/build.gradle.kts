import com.arkivanov.gradle.bundle
import com.arkivanov.gradle.dependsOn
import com.arkivanov.gradle.setupMultiplatform
import com.arkivanov.gradle.setupPublication
import com.arkivanov.gradle.setupSourceSets

plugins {
    id("kotlin-multiplatform")
    id("maven-publish")
    id("com.arkivanov.gradle.setup")
}

setupMultiplatform()
setupPublication()

kotlin {
    setupSourceSets {
        val darwin by bundle()
        darwin dependsOn common
        darwinSet dependsOn darwin
    }
}
