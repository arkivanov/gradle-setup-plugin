package com.arkivanov.gradle

import com.arkivanov.gradle.GradleSetupPublicationConfigExtension.*
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

open class GradleSetupExtension {

    internal lateinit var project: Project

    private val publicationConfig by lazy {
        project.extensions.findByType<GradleSetupPublicationConfigExtension>()?.config
            ?: project.rootProject.extensions.findByType<GradleSetupPublicationConfigExtension>()?.config
            ?: error("Publication config is not set")
    }

    fun multiplatform(vararg targets: Target) {
        project.setupMultiplatform(targets.takeUnless(Array<*>::isEmpty)?.toList() ?: Target.ALL_DEFAULT)
    }

    fun multiplatformPublications() {
        project.setupMultiplatformPublications(config = publicationConfig)
    }

    fun androidLibrary(block: (isCompilationAllowed: Boolean) -> Unit = {}) {
        project.setupAndroidLibrary()
        block(isTargetCompilationAllowed<Target.Android>())
    }

    fun androidApp(
        applicationId: String,
        versionCode: Int,
        versionName: String,
        block: (isCompilationAllowed: Boolean) -> Unit = {}
    ) {
        project.setupAndroidApp(applicationId = applicationId, versionCode = versionCode, versionName = versionName)
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
}
