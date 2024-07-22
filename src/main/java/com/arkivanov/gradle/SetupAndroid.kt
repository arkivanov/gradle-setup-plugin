package com.arkivanov.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.lint.AndroidLintTask
import com.android.build.gradle.internal.lint.AndroidLintTextOutputTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.setupAndroidLibrary() {
    setupAndroid(requireDefaults())
}

fun Project.setupAndroidApp(
    applicationId: String,
    versionCode: Int,
    versionName: String,
) {
    setupAndroid(requireDefaults())

    extensions.configure<BaseAppModuleExtension> {
        defaultConfig {
            this.applicationId = applicationId
            this.versionCode = versionCode
            this.versionName = versionName
        }
    }
}

private fun Project.setupAndroid(config: AndroidConfig) {
    setupAndroidCommon(config)

    tasks.withType<KotlinCompile> {
        enabled = Compilations.isGenericEnabled

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
}

internal fun Project.setupAndroidCommon(config: AndroidConfig) {
    extensions.configure<BaseExtension> {
        compileSdkVersion(config.compileSdkVersion)

        defaultConfig {
            minSdk = config.minSdkVersion
            targetSdk = config.targetSdkVersion
        }

        compileOptions {
            sourceCompatibility(JavaVersion.VERSION_1_8)
            targetCompatibility(JavaVersion.VERSION_1_8)
        }
    }

    tasks.withType<AndroidLintTask> {
        enabled = Compilations.isGenericEnabled
    }

    tasks.withType<AndroidLintTextOutputTask> {
        enabled = Compilations.isGenericEnabled
    }
}
