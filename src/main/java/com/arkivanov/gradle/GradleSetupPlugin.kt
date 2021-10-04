package com.arkivanov.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class GradleSetupPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.rootProject.ensureUnreachableTasksDisabled()
        target.extensions.create<GradleSetupExtension>("setup").project = target
        target.extensions.create<GradleSetupDefaultsExtension>("setupDefaults")
    }
}
