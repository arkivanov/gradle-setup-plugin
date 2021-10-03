package com.arkivanov.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue

internal fun Project.setupAndroidLibraryPublications(config: PublicationConfig) {
    if (!isTargetCompilationAllowed<Target.Android>()) {
        return
    }

    plugins.apply("maven-publish")

    val androidExtension = extensions.getByType<LibraryExtension>()

    val sourceJarTask by tasks.creating(Jar::class) {
        from(androidExtension.sourceSets.getByName("main").java.srcDirs)
        archiveClassifier.set("source")
    }

    fun PublicationContainer.createMavenPublication(name: String) {
        create<MavenPublication>(name) {
            from(components[name])
            artifact(sourceJarTask)

            groupId = config.group
            version = config.version
            artifactId = "${project.name}-$name"

            setupPublicationPom(project, config)
        }
    }

    afterEvaluate {
        publishing {
            publications {
                createMavenPublication(name = "debug")
                createMavenPublication(name = "release")
            }
        }
    }

    setupPublicationRepository(config)
}
