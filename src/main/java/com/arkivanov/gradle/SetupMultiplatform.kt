package com.arkivanov.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.setupMultiplatform(targets: List<Target>, sourceSetConfigurator: (SourceSetsScope.() -> Unit)?) {
    enabledTargets = targets

    doIfTargetEnabled<Target.Android> {
        setupAndroidTarget()

        extensions.with<BaseExtension> {
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
        setupJsTarget(mode = it.mode)
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
    when (leafSourceSet) {
        DefaultSourceSetNames.android -> isTargetEnabled<Target.Android>()
        DefaultSourceSetNames.jvm -> isTargetEnabled<Target.Jvm>()
        DefaultSourceSetNames.js -> isTargetEnabled<Target.Js>()
        DefaultSourceSetNames.linuxX64 -> isTargetEnabled<Target.Linux>()
        DefaultSourceSetNames.iosSimulatorArm64 -> getEnabledTarget<Target.Ios>()?.isAppleSiliconEnabled ?: false
        in DefaultSourceSetNames.iosSet -> isTargetEnabled<Target.Ios>()
        DefaultSourceSetNames.watchosSimulatorArm64 -> getEnabledTarget<Target.WatchOs>()?.isAppleSiliconEnabled ?: false
        in DefaultSourceSetNames.watchosSet -> isTargetEnabled<Target.WatchOs>()
        DefaultSourceSetNames.tvosSimulatorArm64 -> getEnabledTarget<Target.TvOs>()?.isAppleSiliconEnabled ?: false
        in DefaultSourceSetNames.tvosSet -> isTargetEnabled<Target.TvOs>()
        DefaultSourceSetNames.macosArm64 -> getEnabledTarget<Target.MacOs>()?.isAppleSiliconEnabled ?: false
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
        iosArm64 {
            disableCompilationsIfNeeded()
        }

        if (target.isAppleSiliconEnabled) {
            iosSimulatorArm64 {
                disableCompilationsIfNeeded()
            }
        }

        iosX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupWatchOsTarget(target: Target.WatchOs) {
    kotlin {
        watchosArm32 {
            disableCompilationsIfNeeded()
        }

        watchosArm64 {
            disableCompilationsIfNeeded()
        }

        if (target.isAppleSiliconEnabled) {
            watchosSimulatorArm64 {
                disableCompilationsIfNeeded()
            }
        }

        watchosX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupTvOsTarget(target: Target.TvOs) {
    kotlin {
        tvosArm64 {
            disableCompilationsIfNeeded()
        }

        if (target.isAppleSiliconEnabled) {
            tvosSimulatorArm64 {
                disableCompilationsIfNeeded()
            }
        }

        tvosX64 {
            disableCompilationsIfNeeded()
        }
    }
}

private fun Project.setupMacOsTarget(target: Target.MacOs) {
    kotlin {
        if (target.isAppleSiliconEnabled) {
            macosArm64 {
                disableCompilationsIfNeeded()
            }
        }

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
