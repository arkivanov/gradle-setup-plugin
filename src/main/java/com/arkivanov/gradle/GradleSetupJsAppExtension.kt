package com.arkivanov.gradle

open class GradleSetupJsAppExtension : GradleSetupExtensionBase() {

    fun jsApp(block: (isCompilationAllowed: Boolean) -> Unit = {}) {
        project.setupJsApp()
    }
}
