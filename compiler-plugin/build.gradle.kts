import com.arkivanov.gradle.setupPublication
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("maven-publish")
    id("com.arkivanov.gradle.setup")
}

setupPublication()

dependencies {
    compileOnly(deps.kotlin.kotlinCompilerEmbeddable)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
