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
    kapt(deps.google.autoService)
    compileOnly(deps.google.autoServiceAnnotations)
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
