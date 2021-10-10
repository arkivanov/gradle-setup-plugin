package com.arkivanov.gradle

open class GradleSetupAndroidLibraryExtension : GradleSetupExtensionBase() {

    fun androidLibrary() {
        project.setupAndroidLibrary(defaultAndroidConfig)
    }

    fun publications() {
        project.setupAndroidLibraryPublications(defaultPublicationConfig)
    }

    fun binaryCompatibilityValidator() {
        project.setupBinaryCompatibilityValidatorAndroid()
    }
}
