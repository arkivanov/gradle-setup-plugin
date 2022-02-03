package com.arkivanov.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension

internal fun Project.setupJsApp() {
    extensions.with<KotlinJsProjectExtension> {
        js(IR) {
            browser()
            binaries.executable()

            disableCompilationsIfNeeded()
        }
    }
}
