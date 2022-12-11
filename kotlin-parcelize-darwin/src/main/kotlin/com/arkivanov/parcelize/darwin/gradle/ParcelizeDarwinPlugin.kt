package com.arkivanov.parcelize.darwin.gradle

import com.arkivanov.parcelize.darwin.kotlin_parcelize_darwin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

class ParcelizeDarwinPlugin : KotlinCompilerPluginSupportPlugin {

    private val pluginVersion = BuildConfig.VERSION

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }

    override fun getCompilerPluginId(): String =
        "com.arkivanov.parcelize.darwin"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "com.arkivanov.parcelize.darwin",
            artifactId = "kotlin-parcelize-darwin-compiler",
            version = pluginVersion,
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        (kotlinCompilation.target as? KotlinNativeTarget)
                ?.konanTarget?.family?.isAppleFamily == true

    override fun apply(target: Project) {
        val ext = target.kotlinExtension as KotlinMultiplatformExtension
        ext.targets.configureEach { trg ->
            if ((trg is KotlinNativeTarget) && trg.konanTarget.family.isAppleFamily) {
                trg.compilations.configureEach { compilation ->
                    compilation.dependencies {
                        implementation("com.arkivanov.parcelize.darwin:kotlin-parcelize-darwin-runtime:$pluginVersion")
                    }
                }
            }
        }
    }
}
