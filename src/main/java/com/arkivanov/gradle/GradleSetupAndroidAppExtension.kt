package com.arkivanov.gradle

open class GradleSetupAndroidAppExtension : GradleSetupExtensionBase() {

    fun androidApp(
        applicationId: String,
        versionCode: Int,
        versionName: String,
    ) {
        project.setupAndroidApp(
            config = defaultAndroidConfig,
            applicationId = applicationId,
            versionCode = versionCode,
            versionName = versionName
        )
    }
}
