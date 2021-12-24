package com.arkivanov.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class GradleSetupPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.rootProject.ensureUnreachableTasksDisabled()
        target.extensions.create<GradleSetupAllProjectsExtension>("setupAllProjects").project = target
        target.extensions.create<GradleSetupMultiplatformExtension>("setupMultiplatform").project = target
        target.extensions.create<GradleSetupAndroidLibraryExtension>("setupAndroidLibrary").project = target
        target.extensions.create<GradleSetupAndroidAppExtension>("setupAndroidApp").project = target
        target.extensions.create<GradleSetupJsAppExtension>("setupJsApp").project = target
        target.extensions.create<GradleSetupIdeaPluginExtension>("setupIdeaPlugin").project = target
    }
}
