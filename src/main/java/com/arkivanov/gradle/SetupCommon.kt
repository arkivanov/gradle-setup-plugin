package com.arkivanov.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.lint.AndroidLintTask
import com.android.build.gradle.internal.lint.AndroidLintTextOutputTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withGroovyBuilder
import org.gradle.kotlin.dsl.withType

internal fun Project.setupAndroidCommon(config: AndroidConfig) {
    extensions.with<BaseExtension> {
        compileSdkVersion(config.compileSdkVersion)

        defaultConfig {
            minSdkVersion(config.minSdkVersion)
            targetSdkVersion(config.targetSdkVersion)
        }

        compileOptions {
            sourceCompatibility(JavaVersion.VERSION_1_8)
            targetCompatibility(JavaVersion.VERSION_1_8)
        }

        withGroovyBuilder {
            "kotlinOptions" {
                setProperty("jvmTarget", "1.8")
            }
        }
    }

    tasks.withType<AndroidLintTask> {
        enabled = isTargetCompilationAllowed<Target.Android>()
    }

    tasks.withType<AndroidLintTextOutputTask> {
        enabled = isTargetCompilationAllowed<Target.Android>()
    }
}
