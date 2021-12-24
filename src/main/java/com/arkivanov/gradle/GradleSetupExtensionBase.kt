package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

open class GradleSetupExtensionBase {

    internal lateinit var project: Project

    protected val defaultPublicationConfig: PublicationConfig by lazy {
        project.findDefaultConfig(
            errorMessage = "Default publication config not set",
            extract = GradleSetupAllProjectsExtension::publicationConfig,
        )
    }

    protected val defaultMultiplatformTargets: List<Target> by lazy {
        project.findDefaultConfig(
            errorMessage = "Default multiplatform targets not set",
            extract = GradleSetupAllProjectsExtension::multiplatformTargets,
        )
    }

    protected val defaultAndroidConfig: AndroidConfig by lazy {
        project.findDefaultConfig(
            errorMessage = "Default Android config not set",
            extract = GradleSetupAllProjectsExtension::androidConfig,
        )
    }

    protected val defaultSourceSetConfigurator: (SourceSetsScope.() -> Unit)? by lazy {
        project.findDefaultConfig(GradleSetupAllProjectsExtension::multiplatformSourceSetConfigurator)
    }

    private companion object {
        private fun <T : Any> Project.findDefaultConfig(errorMessage: String, extract: (GradleSetupAllProjectsExtension) -> T?): T =
            findDefaultConfig(extract)
                ?: error(errorMessage)

        private fun <T : Any> Project.findDefaultConfig(extract: (GradleSetupAllProjectsExtension) -> T?): T? =
            project.rootProject.extensions.findByType<GradleSetupAllProjectsExtension>()?.let(extract)
    }
}
