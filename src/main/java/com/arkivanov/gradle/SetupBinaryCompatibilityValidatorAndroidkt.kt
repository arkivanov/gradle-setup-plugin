package com.arkivanov.gradle

import org.gradle.api.Project

internal fun Project.setupBinaryCompatibilityValidatorAndroid() {
    if (isTargetCompilationAllowed<Target.Android>()) {
        plugins.apply("org.jetbrains.kotlinx.binary-compatibility-validator")
    }
}
