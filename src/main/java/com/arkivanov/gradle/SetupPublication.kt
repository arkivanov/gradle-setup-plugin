package com.arkivanov.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

fun Project.setupPublication() {
    val config = requireDefaults<PublicationConfig>()
    when {
        hasExtension { KotlinMultiplatformExtension::class } -> setupPublicationMultiplatform(config)
        hasExtension { LibraryExtension::class } -> setupPublicationAndroidLibrary(config)
        hasExtension { GradlePluginDevelopmentExtension::class } -> setupPublicationGradlePlugin(config)
        hasExtension { JavaPluginExtension::class } -> setupPublicationJava(config)
        else -> error("Unsupported project type for publication")
    }
}

private fun Project.setupPublicationMultiplatform(config: PublicationConfig) {
    applyMavenPublishPlugin()

    group = config.group
    version = config.version

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            setupPublicationPom(project, config)
        }
    }

    setupPublicationRepository(config)

    if (Compilations.isGenericEnabled && multiplatformExtension.targets.any { it.platformType == KotlinPlatformType.androidJvm }) {
        multiplatformExtension.apply {
            androidTarget {
                publishLibraryVariants("release", "debug")
            }
        }
    }
}

private fun Project.setupPublicationAndroidLibrary(config: PublicationConfig) {
    if (!Compilations.isGenericEnabled) {
        return
    }

    plugins.apply("maven-publish")

    val androidExtension = extensions.getByType<LibraryExtension>()

    val sourceJarTask by tasks.creating(Jar::class) {
        from(androidExtension.sourceSets.getByName("main").java.srcDirs)
        archiveClassifier.set("source")
    }

    fun PublicationContainer.createMavenPublication(name: String, artifactIdSuffix: String) {
        create<MavenPublication>(name) {
            from(components[name])
            artifact(sourceJarTask)

            groupId = config.group
            version = config.version
            artifactId = "${project.name}$artifactIdSuffix"

            setupPublicationPom(project, config)
        }
    }

    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications {
                createMavenPublication(name = "debug", artifactIdSuffix = "-debug")
                createMavenPublication(name = "release", artifactIdSuffix = "")
            }
        }
    }

    setupPublicationRepository(config)
}

private fun Project.setupPublicationGradlePlugin(config: PublicationConfig) {
    applyMavenPublishPlugin()

    group = config.group
    version = config.version

    val gradlePluginExtension = extensions.getByType<GradlePluginDevelopmentExtension>()

    val sourceJarTask by tasks.creating(Jar::class) {
        from(gradlePluginExtension.pluginSourceSet.java.srcDirs)
        archiveClassifier.set("sources")
    }

    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                artifact(sourceJarTask)
                setupPublicationPom(project, config)
            }
        }
    }

    setupPublicationRepository(config)
}

private fun Project.setupPublicationJava(config: PublicationConfig) {
    applyMavenPublishPlugin()

    group = config.group
    version = config.version

    val javaPluginExtension = extensions.getByType<JavaPluginExtension>()

    val sourceJarTask by tasks.creating(Jar::class) {
        from(javaPluginExtension.sourceSets.getByName("main").java.srcDirs)
        archiveClassifier.set("sources")
    }

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            artifact(sourceJarTask)
            setupPublicationPom(project, config)
        }
    }

    setupPublicationRepository(config)
}

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

    extensions.configure<PublishingExtension> {
        if (isSigningEnabled) {
            extensions.configure<SigningExtension> {
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

private fun Project.applyMavenPublishPlugin() {
    plugins.apply("maven-publish")
}

private const val JAVADOC_JAR_TASK_NAME = "javadocJar"

private fun Project.ensureJavadocJarTask(): Task =
    tasks.findByName(JAVADOC_JAR_TASK_NAME) ?: createJavadocJarTask()

private fun Project.createJavadocJarTask(): Task =
    tasks.create<Jar>(JAVADOC_JAR_TASK_NAME).apply {
        archiveClassifier.set("javadoc")
    }
