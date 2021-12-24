package com.arkivanov.gradle

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal fun Project.setupDetekt() {
    allprojects {
        plugins.apply("io.gitlab.arturbosch.detekt")

        extensions.with<DetektExtension> {
            parallel = true
            buildUponDefaultConfig = true
            config = files("$rootDir/detekt.yml")
            source = files(file("src").listFiles()?.find { it.isDirectory } ?: emptyArray<Any>())
        }

        tasks.register("detektAll") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.withType<Detekt>())
        }

        tasks.configureEach {
            if (name == "build") {
                dependsOn("detektAll")
            }
        }
    }
}
