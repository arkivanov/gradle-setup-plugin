package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.setupMultiplatformPublications(config: PublicationConfig) {
    plugins.apply("maven-publish")

    this.group = config.group
    this.version = config.version

    publishing {
        publications.withType<MavenPublication>().forEach {
            it.setupPublicationPom(
                project = this@setupMultiplatformPublications,
                projectName = config.projectName,
                projectDescription = config.projectDescription,
                projectUrl = config.projectUrl,
                scmUrl = config.scmUrl,
                licenseName = config.licenseName,
                licenseUrl = config.licenseUrl,
                developerId = config.developerId,
                developerName = config.developerName,
                developerEmail = config.developerEmail,
            )
        }
    }

    setupPublicationRepository(
        signingKey = config.signingKey,
        signingPassword = config.signingPassword,
        repositoryUrl = config.repositoryUrl,
        repositoryUserName = config.repositoryUserName,
        repositoryPassword = config.repositoryPassword,
    )

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
    val isMetadataOnly: Boolean? = System.getProperty("metadata_only")?.let(String::toBoolean)
    val targets = extensions.getByType<KotlinMultiplatformExtension>().targets

    tasks.withType<AbstractPublishToMaven>().configureEach {
        val publicationName = publication?.name

        enabled =
            when {
                publicationName == "kotlinMultiplatform" -> isMetadataOnly != false

                publicationName != null -> {
                    val target = targets.find { it.name.startsWith(publicationName) }
                    checkNotNull(target) { "Target not found for publication $publicationName" }
                    (isMetadataOnly != true) && target.isCompilationAllowed
                }

                else -> {
                    val target = targets.find { name.contains(other = it.name, ignoreCase = true) }
                    checkNotNull(target) { "Target not found for publication $this" }
                    (isMetadataOnly != true) && target.isCompilationAllowed
                }
            }

        println("Publication $this enabled=$enabled")
    }
}

private fun Project.setupPublicationRepository(
    signingKey: String?,
    signingPassword: String?,
    repositoryUrl: String,
    repositoryUserName: String?,
    repositoryPassword: String?,
) {
    val isSigningEnabled = signingKey != null

    if (isSigningEnabled) {
        plugins.apply("signing")
    }

    publishing {
        if (isSigningEnabled) {
            extensions.with<SigningExtension> {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(publications)
            }
        }

        repositories {
            maven {
                setUrl(repositoryUrl)

                credentials {
                    username = repositoryUserName
                    password = repositoryPassword
                }
            }
        }
    }
}

private fun Project.publishing(block: PublishingExtension.() -> Unit) {
    extensions.with(block)
}
