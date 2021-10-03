package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

open class GradleSetupExtension {

    internal lateinit var project: Project

    private val defaultPublicationConfig: PublicationConfig by lazy {
        project.findDefaultConfig(
            errorMessage = "Default publication config not set",
            extract = GradleSetupDefaultsExtension::publicationConfig,
        )
    }

    private val defaultMultiplatformTargets: List<Target> by lazy {
        project.findDefaultConfig(
            errorMessage = "Default multiplatform targets not set",
            extract = GradleSetupDefaultsExtension::multiplatformTargets,
        )
    }

    private val defaultAndroidConfig: AndroidConfig by lazy {
        project.findDefaultConfig(
            errorMessage = "Default Android config not set",
            extract = GradleSetupDefaultsExtension::androidConfig,
        )
    }

    private val defaultSourceSetConfigurator: (SourceSetsScope.() -> Unit)? by lazy {
        project.findDefaultConfig(GradleSetupDefaultsExtension::multiplatformSourceSetConfigurator)
    }

    fun multiplatform(vararg targets: Target) {
        project.setupMultiplatform(
            targets = targets.takeUnless(Array<*>::isEmpty)?.toList() ?: defaultMultiplatformTargets,
            androidConfig = { defaultAndroidConfig },
            sourceSetConfigurator = defaultSourceSetConfigurator,
        )
    }

    fun multiplatformPublications() {
        project.setupMultiplatformPublications(defaultPublicationConfig)
    }

    fun androidLibraryPublications() {
        project.setupAndroidLibraryPublications(defaultPublicationConfig)
    }

    fun androidLibrary(block: (isCompilationAllowed: Boolean) -> Unit = {}) {
        project.setupAndroidLibrary(defaultAndroidConfig)
        block(isTargetCompilationAllowed<Target.Android>())
    }

    fun androidApp(
        applicationId: String,
        versionCode: Int,
        versionName: String,
        block: (isCompilationAllowed: Boolean) -> Unit = {}
    ) {
        project.setupAndroidApp(
            config = defaultAndroidConfig,
            applicationId = applicationId,
            versionCode = versionCode,
            versionName = versionName
        )

        block(isTargetCompilationAllowed<Target.Android>())
    }

    fun jsApp(block: (isCompilationAllowed: Boolean) -> Unit = {}) {
        project.setupJsApp()
        block(isTargetCompilationAllowed<Target.Js>())
    }

    fun ideaPlugin(
        group: String,
        version: String,
        sinceBuild: String,
        intellijVersion: String,
        block: (isCompilationAllowed: Boolean) -> Unit = {}
    ) {
        project.setupIdeaPlugin(
            group = group,
            version = version,
            sinceBuild = sinceBuild,
            intellijVersion = intellijVersion
        )
        block(isTargetCompilationAllowed<Target.Jvm>())
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
