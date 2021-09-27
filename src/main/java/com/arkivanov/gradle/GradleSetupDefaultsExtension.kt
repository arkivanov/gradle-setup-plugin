package com.arkivanov.gradle

open class GradleSetupDefaultsExtension {

    internal var publicationConfig: PublicationConfig? = null
        private set

    internal var multiplatformTargets: List<Target>? = null
        private set

    internal var multiplatformSourceSetConfigurator: (SourceSetsScope.() -> Unit)? = null
        private set

    fun publicationConfig(config: PublicationConfig) {
        publicationConfig = config
    }

    fun multiplatformTargets(vararg targets: Target) {
        multiplatformTargets = targets.toList()
    }

    fun multiplatformTargets(targets: List<Target>) {
        multiplatformTargets = targets
    }

    fun multiplatformSourceSets(block: SourceSetsScope.() -> Unit) {
        multiplatformSourceSetConfigurator = block
    }
}
