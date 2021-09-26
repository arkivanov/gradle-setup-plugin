package com.arkivanov.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.setupMultiplatform(targets: List<Target>) {
    enabledTargets = targets

    setupCommon()

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

private fun Project.setupCommon() {
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

            if (isTargetEnabled(Target::isJava) || isTargetEnabled<Target.Js>()) {
                create("jvmJsMain").dependsOn(getByName("commonMain"))
                create("jvmJsTest").dependsOn(getByName("commonTest"))
            }

            if (isTargetEnabled<Target.Js>() || isTargetEnabled(Target::isNative)) {
                create("jsNativeMain").dependsOn(getByName("commonMain"))
                create("jsNativeTest").dependsOn(getByName("commonTest"))
            }

            if (isTargetEnabled(Target::isJava) || isTargetEnabled(Target::isNative)) {
                create("jvmNativeMain").dependsOn(getByName("commonMain"))
                create("jvmNativeTest").dependsOn(getByName("commonTest"))
            }

            if (isTargetEnabled(Target::isJava)) {
                create("javaMain") {
                    dependsOn(getByName("jvmJsMain"))
                    dependsOn(getByName("jvmNativeMain"))
                }

                create("javaTest") {
                    dependsOn(getByName("jvmJsTest"))
                    dependsOn(getByName("jvmNativeTest"))
                }
            }

            if (isTargetEnabled(Target::isNative)) {
                create("nativeMain") {
                    dependsOn(getByName("jsNativeMain"))
                    dependsOn(getByName("jvmNativeMain"))
                }

                create("nativeTest") {
                    dependsOn(getByName("jsNativeTest"))
                    dependsOn(getByName("jvmNativeTest"))
                }
            }

            if (isTargetEnabled(Target::isDarwin)) {
                create("darwinMain").dependsOn(getByName("nativeMain"))
                create("darwinTest").dependsOn(getByName("nativeTest"))
            }
        }
    }
}

private fun Project.setupAndroidTarget() {
    kotlin {
        android {
            disableCompilationsIfNeeded()
        }

        sourceSets {
            getByName("androidMain").dependsOn(getByName("javaMain"))
            getByName("androidTest").dependsOn(getByName("javaTest"))
        }
    }
}

private fun Project.setupJvmTarget() {
    kotlin {
        jvm {
            disableCompilationsIfNeeded()
        }

        sourceSets {
            getByName("jvmMain").dependsOn(getByName("javaMain"))
            getByName("jvmTest").dependsOn(getByName("javaTest"))
        }
    }
}

private fun Project.setupLinuxTarget() {
    kotlin {
        linuxX64 {
            disableCompilationsIfNeeded()
        }

        sourceSets {
            getByName("linuxX64Main").dependsOn(getByName("nativeMain"))
            getByName("linuxX64Test").dependsOn(getByName("nativeTest"))
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

        sourceSets {
            getByName("iosArm64Main").dependsOn(getByName("darwinMain"))
            getByName("iosArm64Test").dependsOn(getByName("darwinTest"))

            getByName("iosX64Main").dependsOn(getByName("darwinMain"))
            getByName("iosX64Test").dependsOn(getByName("darwinTest"))
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

        sourceSets {
            getByName("watchosArm32Main").dependsOn(getByName("darwinMain"))
            getByName("watchosArm32Test").dependsOn(getByName("darwinTest"))

            getByName("watchosArm64Main").dependsOn(getByName("darwinMain"))
            getByName("watchosArm64Test").dependsOn(getByName("darwinTest"))

            getByName("watchosX64Main").dependsOn(getByName("darwinMain"))
            getByName("watchosX64Test").dependsOn(getByName("darwinTest"))
        }
    }
}

private fun Project.setupMacOsTarget() {
    kotlin {
        macosX64 {
            disableCompilationsIfNeeded()
        }

        sourceSets {
            getByName("macosX64Main").dependsOn(getByName("darwinMain"))
            getByName("macosX64Test").dependsOn(getByName("darwinTest"))
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

        sourceSets {
            getByName("jsMain") {
                dependsOn(getByName("jvmJsMain"))
                dependsOn(getByName("jsNativeMain"))
            }

            getByName("jsTest") {
                dependsOn(getByName("jvmJsTest"))
                dependsOn(getByName("jsNativeTest"))
            }
        }
    }
}
