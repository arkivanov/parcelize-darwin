package com.arkivanov.parcelize.darwin.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class ParcelizeDarwinPlugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }

    override fun getCompilerPluginId(): String = "com.arkivanov.parcelize.darwin.kotlin-parcelize-darwin-compiler"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "com.arkivanov.parcelize.darwin",
            artifactId = "kotlin-parcelize-darwin-compiler-j",
            version = "0.1.0"
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getPluginArtifactForNative(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "com.arkivanov.parcelize.darwin",
            artifactId = "kotlin-parcelize-darwin-compiler",
            version = "0.1.0"
        )
}
