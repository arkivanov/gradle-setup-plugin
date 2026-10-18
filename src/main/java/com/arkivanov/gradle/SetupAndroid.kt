package com.arkivanov.gradle

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.lint.AndroidLintTask
import com.android.build.gradle.internal.lint.AndroidLintTextOutputTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.setupAndroidLibrary() {
    val defaults = requireDefaults<AndroidConfig>()

    extensions.configure<LibraryExtension> {
        defaultConfig.apply {
            lint {
                targetSdk = defaults.targetSdkVersion
            }
        }
    }

    setupAndroid(defaults)
}

fun Project.setupAndroidApp(
    applicationId: String,
    versionCode: Int,
    versionName: String,
) {
    val defaults = requireDefaults<AndroidConfig>()

    extensions.configure<ApplicationExtension> {
        defaultConfig.apply {
            targetSdk = defaults.targetSdkVersion
            this.applicationId = applicationId
            this.versionCode = versionCode
            this.versionName = versionName
        }
    }

    setupAndroid(defaults)
}

private fun Project.setupAndroid(config: AndroidConfig) {
    setupAndroidCommon(config)

    tasks.withType<KotlinCompile> {
        enabled = Compilations.isGenericEnabled

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

private fun Project.setupAndroidCommon(config: AndroidConfig) {
    extensions.configure<CommonExtension> {
        compileSdk {
            version = release(config.compileSdkVersion)
        }

        defaultConfig.apply {
            minSdk = config.minSdkVersion
        }

        compileOptions.apply {
            sourceCompatibility(JavaVersion.VERSION_11)
            targetCompatibility(JavaVersion.VERSION_11)
        }
    }

    configureAndroidLintTasks()
}

internal fun Project.configureAndroidLintTasks() {
    tasks.withType<AndroidLintTask> {
        enabled = Compilations.isGenericEnabled
    }

    tasks.withType<AndroidLintTextOutputTask> {
        enabled = Compilations.isGenericEnabled
    }
}
