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
        mavenLocal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.toString() == "com.arkivanov.gradle.setup") {
//                useModule("com.github.arkivanov:gradle-setup-plugin:60ac46054c")
            }
        }
    }

    plugins {
        id("com.arkivanov.gradle.setup").version("0.0.1")
    }
}

include(":gradle-plugin")
include(":compiler-plugin")
include(":runtime")
