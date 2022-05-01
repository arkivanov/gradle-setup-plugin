package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.setupMultiplatform(
    targets: List<Target>,
    androidConfig: () -> AndroidConfig,
    sourceSetConfigurator: (SourceSetsScope.() -> Unit)?
) {
    enabledTargets = targets

    doIfTargetEnabled<Target.Android> {
        setupAndroidTarget()
        setupAndroidCommon(androidConfig())
    }

    doIfTargetEnabled<Target.Jvm> {
        setupJvmTarget()
    }

    doIfTargetEnabled<Target.Linux> {
        setupLinuxTarget()
    }

    doIfTargetEnabled<Target.Ios> {
        setupIosTarget(it)
    }

    doIfTargetEnabled<Target.WatchOs> {
        setupWatchOsTarget(it)
    }

    doIfTargetEnabled<Target.TvOs> {
        setupTvOsTarget(it)
    }

    doIfTargetEnabled<Target.MacOs> {
        setupMacOsTarget(it)
    }

    doIfTargetEnabled<Target.Js> {
        setupJsTarget(it)
    }

    setupSourceSets(sourceSetConfigurator)

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
        val parentMain = kotlinSourceSets.maybeCreate(parent.main)
        val parentTest = kotlinSourceSets.maybeCreate(parent.test)
        kotlinSourceSets.maybeCreate(child.main).dependsOn(parentMain)
        kotlinSourceSets.maybeCreate(child.test).dependsOn(parentTest)
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
    when (leafSourceSet) {
        DefaultSourceSetNames.android -> isTargetEnabled<Target.Android>()
        DefaultSourceSetNames.jvm -> isTargetEnabled<Target.Jvm>()
        DefaultSourceSetNames.js -> isTargetEnabled<Target.Js>()
        DefaultSourceSetNames.linuxX64 -> isTargetEnabled<Target.Linux>()
        DefaultSourceSetNames.iosX64 -> getEnabledTarget<Target.Ios>()?.x64 ?: false
        DefaultSourceSetNames.iosArm64 -> getEnabledTarget<Target.Ios>()?.arm64 ?: false
        DefaultSourceSetNames.iosSimulatorArm64 -> getEnabledTarget<Target.Ios>()?.simulatorArm64 ?: false
        in DefaultSourceSetNames.iosSet -> isTargetEnabled<Target.Ios>()
        DefaultSourceSetNames.watchosX64 -> getEnabledTarget<Target.WatchOs>()?.x64 ?: false
        DefaultSourceSetNames.watchosArm32 -> getEnabledTarget<Target.WatchOs>()?.arm32 ?: false
        DefaultSourceSetNames.watchosArm64 -> getEnabledTarget<Target.WatchOs>()?.arm64 ?: false
        DefaultSourceSetNames.watchosSimulatorArm64 -> getEnabledTarget<Target.WatchOs>()?.simulatorArm64 ?: false
        in DefaultSourceSetNames.watchosSet -> isTargetEnabled<Target.WatchOs>()
        DefaultSourceSetNames.tvosX64 -> getEnabledTarget<Target.TvOs>()?.x64 ?: false
        DefaultSourceSetNames.tvosArm64 -> getEnabledTarget<Target.TvOs>()?.arm64 ?: false
        DefaultSourceSetNames.tvosSimulatorArm64 -> getEnabledTarget<Target.TvOs>()?.simulatorArm64 ?: false
        in DefaultSourceSetNames.tvosSet -> isTargetEnabled<Target.TvOs>()
        DefaultSourceSetNames.macosX64 -> getEnabledTarget<Target.MacOs>()?.x64 ?: false
        DefaultSourceSetNames.macosArm64 -> getEnabledTarget<Target.MacOs>()?.arm64 ?: false
        in DefaultSourceSetNames.macosSet -> isTargetEnabled<Target.MacOs>()
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

private fun Project.setupIosTarget(target: Target.Ios) {
    kotlin {
        if (target.arm64) {
            iosArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.simulatorArm64) {
            iosSimulatorArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.x64) {
            iosX64 {
                disableCompilationsIfNeeded()
            }
        }
    }
}

private fun Project.setupWatchOsTarget(target: Target.WatchOs) {
    kotlin {
        if (target.arm32) {
            watchosArm32 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.arm64) {
            watchosArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.simulatorArm64) {
            watchosSimulatorArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.x64) {
            watchosX64 {
                disableCompilationsIfNeeded()
            }
        }
    }
}

private fun Project.setupTvOsTarget(target: Target.TvOs) {
    kotlin {
        if (target.arm64) {
            tvosArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.simulatorArm64) {
            tvosSimulatorArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.x64) {
            tvosX64 {
                disableCompilationsIfNeeded()
            }
        }
    }
}

private fun Project.setupMacOsTarget(target: Target.MacOs) {
    kotlin {
        if (target.arm64) {
            macosArm64 {
                disableCompilationsIfNeeded()
            }
        }

        if (target.x64) {
            macosX64 {
                disableCompilationsIfNeeded()
            }
        }
    }
}

private fun Project.setupJsTarget(target: Target.Js) {
    kotlin {
        js(
            when (target.mode) {
                Target.Js.Mode.BOTH -> BOTH
                Target.Js.Mode.IR -> IR
                Target.Js.Mode.LEGACY -> LEGACY
            }
        ) {
            target.environments.forEach {
                when (it) {
                    Target.Js.Environment.BROWSER -> browser()
                    Target.Js.Environment.NODE_JS -> nodejs()
                }.let {}
            }

            when (target.binary) {
                Target.Js.Binary.NONE -> Unit
                Target.Js.Binary.EXECUTABLE -> binaries.executable()
                Target.Js.Binary.LIBRARY -> binaries.library()
            }

            disableCompilationsIfNeeded()
        }
    }
}
