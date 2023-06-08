package com.arkivanov.gradle

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.konan.target.Family

fun Project.setupDetekt() {
    checkIsRootProject()

    allprojects {
        plugins.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<DetektExtension> {
            parallel = true
            buildUponDefaultConfig = true
            config = files("$rootDir/detekt.yml")
        }

        tasks.register("detektAll") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.withType<Detekt>())
        }

        tasks.configureEach {
            if (name == "build") {
                dependsOn("detektAll")
            } else if (name.startsWith("detekt")) {
                enabled = getFamily()?.isCompilationAllowed() ?: true
                logger.info("Detekt $this, enabled: $enabled")
            }
        }
    }
}

private fun Task.getFamily(): Family? =
    Family.values().firstOrNull { family ->
        name.contains(other = family.name, ignoreCase = true)
    }
