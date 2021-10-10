package com.arkivanov.gradle

import kotlinx.validation.ApiCompareCompareTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import kotlin.reflect.KClass

internal fun Project.setupBinaryCompatibilityValidatorMultiplatform() {
    plugins.apply("org.jetbrains.kotlinx.binary-compatibility-validator")

    afterEvaluate {
        tasks.withType<ApiCompareCompareTask> {
            val targetName = name.removeSuffix("ApiCheck")
            if (targetName != name) {
                enabled = isTargetCompilationAllowed(getTargetClassForTaskName(taskName = targetName))
                println("API check $this enabled=$enabled")
            }
        }
    }
}

private fun getTargetClassForTaskName(taskName: String): KClass<out Target> =
    when (taskName) {
        "android" -> Target.Android::class
        "jvm" -> Target.Jvm::class
        else -> error("Unsupported API check task name: $taskName")
    }
