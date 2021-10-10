package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

open class GradleSetupExtensionBase {

    internal lateinit var project: Project

    protected val defaultPublicationConfig: PublicationConfig by lazy {
        project.findDefaultConfig(
            errorMessage = "Default publication config not set",
            extract = GradleSetupDefaultsExtension::publicationConfig,
        )
    }

    protected val defaultMultiplatformTargets: List<Target> by lazy {
        project.findDefaultConfig(
            errorMessage = "Default multiplatform targets not set",
            extract = GradleSetupDefaultsExtension::multiplatformTargets,
        )
    }

    protected val defaultAndroidConfig: AndroidConfig by lazy {
        project.findDefaultConfig(
            errorMessage = "Default Android config not set",
            extract = GradleSetupDefaultsExtension::androidConfig,
        )
    }

    protected val defaultSourceSetConfigurator: (SourceSetsScope.() -> Unit)? by lazy {
        project.findDefaultConfig(GradleSetupDefaultsExtension::multiplatformSourceSetConfigurator)
    }

    private companion object {
        private fun <T : Any> Project.findDefaultConfig(errorMessage: String, extract: (GradleSetupDefaultsExtension) -> T?): T =
            findDefaultConfig(extract)
                ?: error(errorMessage)

        private fun <T : Any> Project.findDefaultConfig(extract: (GradleSetupDefaultsExtension) -> T?): T? =
            project.extensions.findByType<GradleSetupDefaultsExtension>()?.let(extract)
                ?: project.rootProject.extensions.findByType<GradleSetupDefaultsExtension>()?.let(extract)
    }
}
