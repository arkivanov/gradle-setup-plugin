package com.arkivanov.gradle

open class GradleSetupIdeaPluginExtension : GradleSetupExtensionBase() {

    fun ideaPlugin(
        group: String,
        version: String,
        sinceBuild: String,
        intellijVersion: String,
        block: (isCompilationAllowed: Boolean) -> Unit = {}
    ) {
        project.setupIdeaPlugin(
            group = group,
            version = version,
            sinceBuild = sinceBuild,
            intellijVersion = intellijVersion
        )
        block(isTargetCompilationAllowed<Target.Jvm>())
    }
}
