import com.arkivanov.gradle.PublicationConfig
import com.arkivanov.gradle.iosCompat
import com.arkivanov.gradle.macosCompat
import com.arkivanov.gradle.setupDefaults
import com.arkivanov.gradle.tvosCompat
import com.arkivanov.gradle.watchosCompat

buildscript {
    extra.set("kotlinVersion", deps.versions.kotlin.get())
    extra.set("pluginVersion", deps.versions.plugin.get())

    repositories {
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(deps.kotlin.kotlinGradlePlug)
        classpath(deps.gmazzo.buildConfigPlug)
    }
}

plugins {
    id("com.arkivanov.gradle.setup")
}

setupDefaults(
    multiplatformConfigurator = {
        iosCompat()
        watchosCompat()
        tvosCompat()
        macosCompat()
    },
    publicationConfig = PublicationConfig(
        group = "com.arkivanov.parcelize.darwin",
        version = deps.versions.plugin.get(),
        projectName = "Parcelize Darwin",
        projectDescription = "Parcelize Gradle plugin for Darwin targets",
        projectUrl = "https://github.com/arkivanov/parcelize-darwin",
        scmUrl = "scm:git:git://github.com/arkivanov/parcelize-darwin.git",
        licenseName = "The Apache License, Version 2.0",
        licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt",
        developerId = "arkivanov",
        developerName = "Arkadii Ivanov",
        developerEmail = "arkann1985@gmail.com",
        signingKey = System.getenv("SIGNING_KEY"),
        signingPassword = System.getenv("SIGNING_PASSWORD"),
        repositoryUrl = "https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${System.getenv("SONATYPE_REPOSITORY_ID")}",
        repositoryUserName = "arkivanov",
        repositoryPassword = System.getenv("SONATYPE_PASSWORD"),
    ),
)

allprojects {
    repositories {
        mavenCentral()
    }
}
