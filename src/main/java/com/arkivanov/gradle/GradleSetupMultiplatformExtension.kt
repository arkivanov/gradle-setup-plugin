package com.arkivanov.gradle

open class GradleSetupMultiplatformExtension : GradleSetupExtensionBase() {

    fun targets(vararg targets: Target) {
        project.setupMultiplatform(
            targets = targets.takeUnless(Array<*>::isEmpty)?.toList() ?: defaultMultiplatformTargets,
            androidConfig = { defaultAndroidConfig },
            sourceSetConfigurator = defaultSourceSetConfigurator,
        )
    }

    fun publications() {
        project.setupMultiplatformPublications(defaultPublicationConfig)
    }

    fun binaryCompatibilityValidator() {
        project.setupBinaryCompatibilityValidatorMultiplatform()
    }
}
