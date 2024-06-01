package com.arkivanov.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.intellij.IntelliJPluginExtension
import org.jetbrains.intellij.tasks.BuildSearchableOptionsTask
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.setupIdeaPlugin(
    group: String,
    version: String,
    sinceBuild: String,
    intellijVersion: String,
) {
    this.group = group
    this.version = version

    extensions.configure<KotlinJvmProjectExtension> {
        disableCompilationsOfNeeded()

        tasks.withType<BuildSearchableOptionsTask>().configureEach {
            enabled = Compilations.isGenericEnabled
        }
    }

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    tasks.withType<PatchPluginXmlTask>().configureEach {
        this.sinceBuild.set(sinceBuild)
    }

    extensions.configure<IntelliJPluginExtension> {
        this.version.set(intellijVersion)
        this.updateSinceUntilBuild.set(false)
    }
}
