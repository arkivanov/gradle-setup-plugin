package com.arkivanov.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.Family
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

fun Project.setupMultiplatform(
    targets: MultiplatformConfigurator = requireDefaults(),
) {
    configureAndroidLintTasks()

    multiplatformExtension.apply {
        with(targets) { invoke() }

        setupSourceSets {
            common.main.dependencies {
                implementation(kotlin("stdlib"))
            }

            common.test.dependencies {
                implementation(kotlin("test"))
            }
        }

        this.targets.configureEach {
            when (this) {
                is KotlinJvmTarget ->
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }

                is KotlinMultiplatformAndroidLibraryTarget ->
                    configureAndroid(requireDefaults())
            }

            compilations.configureEach {
                compileTaskProvider.configure {
                    compilerOptions {
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                    }
                }
            }
        }

        disableCompilationsOfNeeded()
    }

    configureExtension<JavaPluginExtension> {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }
}

private fun KotlinMultiplatformAndroidLibraryTarget.configureAndroid(config: AndroidConfig) {
    compileSdk {
        version = release(config.compileSdkVersion)
    }

    minSdk = config.minSdkVersion

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }

    if (compilations.none { it is KotlinMultiplatformAndroidHostTestCompilation }) {
        withHostTest {}
    }
}

fun KotlinMultiplatformExtension.setupSourceSets(block: MultiplatformSourceSets.() -> Unit) {
    DefaultMultiplatformSourceSets(targets, sourceSets).block()
}

fun interface MultiplatformConfigurator {
    operator fun KotlinMultiplatformExtension.invoke()
}

interface MultiplatformSourceSets : NamedDomainObjectContainer<KotlinSourceSet> {

    val common: SourceSetBundle
    val allSet: Set<SourceSetBundle>
    val javaSet: Set<SourceSetBundle>
    val nativeSet: Set<SourceSetBundle>
    val linuxSet: Set<SourceSetBundle>
    val darwinSet: Set<SourceSetBundle>
    val iosSet: Set<SourceSetBundle>
    val watchosSet: Set<SourceSetBundle>
    val tvosSet: Set<SourceSetBundle>
    val macosSet: Set<SourceSetBundle>
}

private class DefaultMultiplatformSourceSets(
    private val targets: NamedDomainObjectCollection<KotlinTarget>,
    private val sourceSets: NamedDomainObjectContainer<KotlinSourceSet>,
) : MultiplatformSourceSets, NamedDomainObjectContainer<KotlinSourceSet> by sourceSets {

    override val common: SourceSetBundle by bundle()

    override val allSet: Set<SourceSetBundle> =
        targets.toSourceSetBundles()

    override val javaSet: Set<SourceSetBundle> =
        targets
            .filter { it.platformType in setOf(KotlinPlatformType.androidJvm, KotlinPlatformType.jvm) }
            .toSourceSetBundles()

    override val nativeSet: Set<SourceSetBundle> = nativeSourceSets()
    override val linuxSet: Set<SourceSetBundle> = nativeSourceSets(Family.LINUX)
    override val darwinSet: Set<SourceSetBundle> = nativeSourceSets(Family.IOS, Family.OSX, Family.WATCHOS, Family.TVOS)
    override val iosSet: Set<SourceSetBundle> = nativeSourceSets(Family.IOS)
    override val watchosSet: Set<SourceSetBundle> = nativeSourceSets(Family.WATCHOS)
    override val tvosSet: Set<SourceSetBundle> = nativeSourceSets(Family.TVOS)
    override val macosSet: Set<SourceSetBundle> = nativeSourceSets(Family.OSX)

    private fun nativeSourceSets(vararg families: Family = Family.values()): Set<SourceSetBundle> =
        targets
            .filterIsInstance<KotlinNativeTarget>()
            .filter { it.konanTarget.family in families }
            .toSourceSetBundles()

    private fun Iterable<KotlinTarget>.toSourceSetBundles(): Set<SourceSetBundle> =
        filter { it.platformType != KotlinPlatformType.common }
            .map { it.getSourceSetBundle() }
            .toSet()

    private fun KotlinTarget.getSourceSetBundle(): SourceSetBundle {
        val mainCompilation = compilations.findByName("main")
        val testCompilation = compilations.findByName("test") ?: compilations.findByName("hostTest")

        return if ((mainCompilation != null) && (testCompilation != null)) {
            SourceSetBundle(
                main = mainCompilation.defaultSourceSet,
                test = testCompilation.defaultSourceSet,
            )
        } else {
            bundle(name)
        }
    }
}

fun NamedDomainObjectContainer<out KotlinSourceSet>.bundle(name: String): SourceSetBundle =
    SourceSetBundle(
        main = maybeCreate("${name}Main"),
        test = maybeCreate(if (name == "android") "${name}HostTest" else "${name}Test"),
    )

fun NamedDomainObjectContainer<out KotlinSourceSet>.bundle(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SourceSetBundle>> =
    PropertyDelegateProvider { _, property ->
        val bundle = bundle(property.name)
        ReadOnlyProperty { _, _ -> bundle }
    }

data class SourceSetBundle(
    val main: KotlinSourceSet,
    val test: KotlinSourceSet,
)

operator fun SourceSetBundle.plus(other: SourceSetBundle): Set<SourceSetBundle> =
    this + setOf(other)

operator fun SourceSetBundle.plus(other: Set<SourceSetBundle>): Set<SourceSetBundle> =
    setOf(this) + other

infix fun SourceSetBundle.dependsOn(other: SourceSetBundle) {
    main.dependsOn(other.main)
    test.dependsOn(other.test)
}

infix fun Iterable<SourceSetBundle>.dependsOn(other: Iterable<SourceSetBundle>) {
    forEach { left ->
        other.forEach { right ->
            left.dependsOn(right)
        }
    }
}

infix fun SourceSetBundle.dependsOn(other: Iterable<SourceSetBundle>) {
    listOf(this) dependsOn other
}

infix fun Iterable<SourceSetBundle>.dependsOn(other: SourceSetBundle) {
    this dependsOn listOf(other)
}

fun KotlinMultiplatformExtension.iosCompat(
    x64: String? = DEFAULT_TARGET_NAME,
    arm64: String? = DEFAULT_TARGET_NAME,
    simulatorArm64: String? = DEFAULT_TARGET_NAME,
) {
    enableTarget(name = x64, enableDefault = { iosX64() }, enableNamed = { iosX64(it) })
    enableTarget(name = arm64, enableDefault = { iosArm64() }, enableNamed = { iosArm64(it) })
    enableTarget(name = simulatorArm64, enableDefault = { iosSimulatorArm64() }, enableNamed = { iosSimulatorArm64(it) })
}

fun KotlinMultiplatformExtension.watchosCompat(
    arm32: String? = DEFAULT_TARGET_NAME,
    arm64: String? = DEFAULT_TARGET_NAME,
    simulatorArm64: String? = DEFAULT_TARGET_NAME,
) {
    enableTarget(name = arm32, enableDefault = { watchosArm32() }, enableNamed = { watchosArm32(it) })
    enableTarget(name = arm64, enableDefault = { watchosArm64() }, enableNamed = { watchosArm64(it) })
    enableTarget(name = simulatorArm64, enableDefault = { watchosSimulatorArm64() }, enableNamed = { watchosSimulatorArm64(it) })
}

fun KotlinMultiplatformExtension.tvosCompat(
    arm64: String? = DEFAULT_TARGET_NAME,
    simulatorArm64: String? = DEFAULT_TARGET_NAME,
) {
    enableTarget(name = arm64, enableDefault = { tvosArm64() }, enableNamed = { tvosArm64(it) })
    enableTarget(name = simulatorArm64, enableDefault = { tvosSimulatorArm64() }, enableNamed = { tvosSimulatorArm64(it) })
}

fun KotlinMultiplatformExtension.macosCompat(
    arm64: String? = DEFAULT_TARGET_NAME,
) {
    enableTarget(name = arm64, enableDefault = { macosArm64() }, enableNamed = { macosArm64(it) })
}

private fun KotlinMultiplatformExtension.enableTarget(
    name: String?,
    enableDefault: KotlinMultiplatformExtension.() -> Unit,
    enableNamed: KotlinMultiplatformExtension.(String) -> Unit,
) {
    if (name != null) {
        if (name == DEFAULT_TARGET_NAME) {
            enableDefault()
        } else {
            enableNamed(name)
        }
    }
}

private const val DEFAULT_TARGET_NAME = "DEFAULT_TARGET_NAME"
