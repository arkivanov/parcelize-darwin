dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("deps") {
            from(files("deps.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.toString() == "com.arkivanov.gradle.setup") {
                useModule("com.github.arkivanov:gradle-setup-plugin:1f7ac3c058")
            }
        }
    }

    plugins {
        id("com.arkivanov.gradle.setup")
    }
}

include(":gradle-plugin")
include(":compiler-plugin")
include(":runtime")
