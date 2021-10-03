package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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
    val isMetadataOnly: Boolean = System.getProperty("metadata_only") != null
    val targets = extensions.getByType<KotlinMultiplatformExtension>().targets

    tasks.withType<AbstractPublishToMaven>().configureEach {
        val publicationName = publication?.name

        enabled =
            when {
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

        println("Publication $this enabled=$enabled")
    }
}
