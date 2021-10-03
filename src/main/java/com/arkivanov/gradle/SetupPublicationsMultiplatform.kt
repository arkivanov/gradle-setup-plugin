package com.arkivanov.gradle

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal fun Project.setupMultiplatformPublications(config: PublicationConfig) {
    plugins.apply("maven-publish")

    group = config.group
    version = config.version

    publishing {
        publications.withType<MavenPublication>().forEach {
            it.setupPublicationPom(project, config)
        }
    }

    setupPublicationRepository(config)

    doIfTargetEnabled<Target.Android> {
        kotlin {
            android {
                publishLibraryVariants("release", "debug")
            }
        }
    }

    enablePublicationTasks()
}

private fun Project.enablePublicationTasks() {
    val targets = extensions.getByType<KotlinMultiplatformExtension>().targets

    tasks.withType<AbstractPublishToMaven>().configureEach {
        val isAllowed = isAllowed(targets)
        enableSubtree(isEnabled = isAllowed)
        println("Publication $this enabled=$isAllowed")
    }
}

private fun AbstractPublishToMaven.isAllowed(targets: NamedDomainObjectCollection<KotlinTarget>): Boolean {
    val isMetadataOnly = System.getProperty("metadata_only") != null
    val publicationName = publication?.name

    return when {
        publicationName == "kotlinMultiplatform" -> isMetadataOnly

        publicationName != null -> {
            val target = targets.find { it.name.startsWith(publicationName) }
            checkNotNull(target) { "Target not found for publication $publicationName" }
            !isMetadataOnly && target.isCompilationAllowed
        }

        else -> {
            val target = targets.find { name.contains(other = it.name, ignoreCase = true) }
            checkNotNull(target) { "Target not found for publication $this" }
            !isMetadataOnly && target.isCompilationAllowed
        }
    }
}


