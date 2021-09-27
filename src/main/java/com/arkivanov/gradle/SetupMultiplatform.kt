package com.arkivanov.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.reflect.KClass

internal fun Project.setupMultiplatform(targets: List<Target>, sourceSetConfigurator: (SourceSetsScope.() -> Unit)?) {
    enabledTargets = targets

    setupSourceSets(sourceSetConfigurator)

    doIfTargetEnabled<Target.Android> {
        setupAndroidTarget()

        extensions.with<LibraryExtension> {
            setupAndroid()
        }
    }

    doIfTargetEnabled<Target.Jvm> {
        setupJvmTarget()
    }

    doIfTargetEnabled<Target.Linux> {
        setupLinuxTarget()
    }

    doIfTargetEnabled<Target.Ios> {
        setupIosTarget()
    }

    doIfTargetEnabled<Target.WatchOs> {
        setupWatchOsTarget()
    }

    doIfTargetEnabled<Target.MacOs> {
        setupMacOsTarget()
    }

    doIfTargetEnabled<Target.Js> {
        setupJsTarget(mode = it.mode)
    }

    println("Enabled targets for $this:")
    extensions.getByType<KotlinMultiplatformExtension>().targets.forEach {
        println("$it, compilation allowed: ${it.isCompilationAllowed}")
    }
}

private fun Project.setupSourceSets(configurator: (SourceSetsScope.() -> Unit)?) {
    kotlin {
        sourceSets {
            getByName("commonMain") {
                dependencies {
                    implementation(kotlin("stdlib"))
                }
            }

            getByName("commonTest") {
                dependencies {
                    implementation(kotlin("test"))
                }
            }

            if (configurator != null) {
                configureSourceSets(configurator)
            }
        }
    }
}

private fun Project.configureSourceSets(configurator: SourceSetsScope.() -> Unit) {
    val scope = DefaultSourceSetsScope()
    scope.configurator()
    configureSourceSets(scope)
}

private fun Project.configureSourceSets(scope: DefaultSourceSetsScope) {
    val kotlinSourceSets = extensions.getByType<KotlinMultiplatformExtension>().sourceSets

    scope.connections.forEach { (child, parent) ->
        val parentLeaves = scope.findLeafSourceSets(parent = parent)

        // We can't create an unconnected source set, should be fixed in Kotlin 1.6.0

        if (parentLeaves.any(::isLeafSourceSetAllowed)) {
            val parentMain = kotlinSourceSets.maybeCreate(parent.main)
            val parentTest = kotlinSourceSets.maybeCreate(parent.test)

            val childLeaves = scope.findLeafSourceSets(parent = child)
            if (childLeaves.any(::isLeafSourceSetAllowed)) {
                kotlinSourceSets.maybeCreate(child.main).dependsOn(parentMain)
                kotlinSourceSets.maybeCreate(child.test).dependsOn(parentTest)
            }
        }
    }
}

private fun DefaultSourceSetsScope.findLeafSourceSets(parent: SourceSetName): Set<SourceSetName> {
    val set = HashSet<SourceSetName>()

    connections.forEach { (a, b) ->
        if (b == parent) {
            val leaves = findLeafSourceSets(parent = a)

            if (leaves.isEmpty()) {
                set += a
            } else {
                set += leaves
            }
        }
    }

    if (set.isEmpty()) {
        set += parent
    }

    return set
}

private fun Project.isLeafSourceSetAllowed(leafSourceSet: SourceSetName): Boolean =
    isTargetEnabled(getTargetClassForLeafSourceSet(leafSourceSet = leafSourceSet))

private fun getTargetClassForLeafSourceSet(leafSourceSet: SourceSetName): KClass<out Target> =
    when (leafSourceSet) {
        DefaultSourceSetNames.android -> Target.Android::class
        DefaultSourceSetNames.jvm -> Target.Jvm::class
        DefaultSourceSetNames.js -> Target.Js::class
        DefaultSourceSetNames.linuxX64 -> Target.Linux::class
        in DefaultSourceSetNames.iosSet -> Target.Ios::class
        in DefaultSourceSetNames.watchosSet -> Target.WatchOs::class
        in DefaultSourceSetNames.macosSet -> Target.MacOs::class
        else -> error("No Target class found for leaf source set $leafSourceSet")
    }

private fun Project.setupAndroidTarget() {
    kotlin {
        android {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupJvmTarget() {
    kotlin {
        jvm {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupLinuxTarget() {
    kotlin {
        linuxX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupIosTarget() {
    kotlin {
        iosArm64 {
            disableCompilationsIfNeeded()
        }

        iosX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupWatchOsTarget() {
    kotlin {
        watchosArm32 {
            disableCompilationsIfNeeded()
        }

        watchosArm64 {
            disableCompilationsIfNeeded()
        }

        watchosX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupMacOsTarget() {
    kotlin {
        macosX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupJsTarget(mode: Target.Js.Mode) {
    kotlin {
        js(
            when (mode) {
                Target.Js.Mode.BOTH -> BOTH
                Target.Js.Mode.IR -> IR
                Target.Js.Mode.LEGACY -> LEGACY
            }
        ) {
            browser()
            nodejs()

            disableCompilationsIfNeeded()
        }
    }
}
