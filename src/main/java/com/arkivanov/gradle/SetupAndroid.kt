package com.arkivanov.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.setupAndroidLibrary() {
    setupAndroid<LibraryExtension>()
}

internal fun Project.setupAndroidApp(
    applicationId: String,
    versionCode: Int,
    versionName: String,
) {
    setupAndroid<BaseAppModuleExtension>()

    project.extensions.with<BaseAppModuleExtension> {
        defaultConfig {
            this.applicationId = applicationId
            this.versionCode = versionCode
            this.versionName = versionName
        }
    }
}

private inline fun <reified T : BaseExtension> Project.setupAndroid() {
    project.extensions.getByType<T>().setupAndroid()

    project.tasks.withType<KotlinCompile> {
        enabled = isTargetCompilationAllowed<Target.Android>()
    }
}
