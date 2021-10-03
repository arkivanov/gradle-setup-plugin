package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.plugins.signing.SigningExtension

private const val JAVADOC_JAR_TASK_NAME = "javadocJar"

internal fun MavenPublication.setupPublicationPom(
    project: Project,
    config: PublicationConfig,
) {
    artifact(project.ensureJavadocJarTask())

    pom {
        name.set(config.projectName)
        description.set(config.projectDescription)
        url.set(config.projectUrl)

        licenses {
            license {
                name.set(config.licenseName)
                url.set(config.licenseUrl)
            }
        }

        developers {
            developer {
                id.set(config.developerId)
                name.set(config.developerName)
                email.set(config.developerEmail)
            }
        }

        scm {
            url.set(config.projectUrl)
            connection.set(config.scmUrl)
            developerConnection.set(config.scmUrl)
        }
    }
}

internal fun Project.setupPublicationRepository(config: PublicationConfig) {
    val isSigningEnabled = config.signingKey != null

    if (isSigningEnabled) {
        plugins.apply("signing")
    }

    publishing {
        if (isSigningEnabled) {
            extensions.with<SigningExtension> {
                useInMemoryPgpKeys(config.signingKey, config.signingPassword)
                sign(publications)
            }
        }

        repositories {
            maven {
                setUrl(config.repositoryUrl)

                credentials {
                    username = config.repositoryUserName
                    password = config.repositoryPassword
                }
            }
        }
    }
}

internal fun Project.publishing(block: PublishingExtension.() -> Unit) {
    extensions.with(block)
}

private fun Project.ensureJavadocJarTask(): Task =
    tasks.findByName(JAVADOC_JAR_TASK_NAME) ?: createJavadocJarTask()

private fun Project.createJavadocJarTask(): Task =
    tasks.create<Jar>(JAVADOC_JAR_TASK_NAME).apply {
        archiveClassifier.set("javadoc")
    }
