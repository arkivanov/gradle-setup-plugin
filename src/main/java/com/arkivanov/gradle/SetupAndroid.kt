package com.arkivanov.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.setupAndroidLibrary(config: AndroidConfig) {
    setupAndroid<LibraryExtension>(config)
}

internal fun Project.setupAndroidApp(
    config: AndroidConfig,
    applicationId: String,
    versionCode: Int,
    versionName: String,
) {
    setupAndroid<BaseAppModuleExtension>(config)

    project.extensions.with<BaseAppModuleExtension> {
        defaultConfig {
            this.applicationId = applicationId
            this.versionCode = versionCode
            this.versionName = versionName
        }
    }
}

private inline fun <reified T : BaseExtension> Project.setupAndroid(config: AndroidConfig) {
    setupAndroidCommon(config)

    project.tasks.withType<KotlinCompile> {
        enabled = isTargetCompilationAllowed<Target.Android>()
    }
}
