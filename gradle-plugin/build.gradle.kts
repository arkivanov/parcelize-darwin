import com.arkivanov.gradle.setupPublication
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("com.github.gmazzo.buildconfig")
    id("com.arkivanov.gradle.setup")
}

setupPublication()

gradlePlugin {
    plugins {
        create("kotlinParcelizeDarwin") {
            id = "com.arkivanov.parcelize.darwin"
            implementationClass = "com.arkivanov.parcelize.darwin.gradle.ParcelizeDarwinPlugin"
        }
    }
}

dependencies {
    implementation(deps.kotlin.kotlinGradlePluginApi)
    compileOnly(deps.kotlin.kotlinGradlePlug)
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

buildConfig {
    buildConfigField("String", "VERSION", "\"${deps.versions.plugin.get()}\"")
}
