package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create

private const val JAVADOC_JAR_TASK_NAME = "javadocJar"

internal fun MavenPublication.setupPublicationPom(
    project: Project,
    projectName: String,
    projectDescription: String,
    projectUrl: String,
    scmUrl: String,
    licenseName: String,
    licenseUrl: String,
    developerId: String,
    developerName: String,
    developerEmail: String,
) {
    artifact(project.ensureJavadocJarTask())

    pom {
        name.set(projectName)
        description.set(projectDescription)
        url.set(projectUrl)

        licenses {
            license {
                name.set(licenseName)
                url.set(licenseUrl)
            }
        }

        developers {
            developer {
                id.set(developerId)
                name.set(developerName)
                email.set(developerEmail)
            }
        }

        scm {
            url.set(projectUrl)
            connection.set(scmUrl)
            developerConnection.set(scmUrl)
        }
    }
}

private fun Project.ensureJavadocJarTask(): Task =
    tasks.findByName(JAVADOC_JAR_TASK_NAME) ?: createJavadocJarTask()

private fun Project.createJavadocJarTask(): Task =
    tasks.create<Jar>(JAVADOC_JAR_TASK_NAME).apply {
        archiveClassifier.set("javadoc")
    }
