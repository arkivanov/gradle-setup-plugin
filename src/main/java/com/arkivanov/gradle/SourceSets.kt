package com.arkivanov.gradle

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

data class SourceSetName(val name: String)

internal val SourceSetName.main: String get() = "${name}Main"
internal val SourceSetName.test: String get() = "${name}Test"

interface SourceSetNames {

    val common: SourceSetName

    val android: SourceSetName
    val jvm: SourceSetName
    val js: SourceSetName

    val linuxX64: SourceSetName

    val iosArm64: SourceSetName
    val iosSimulatorArm64: SourceSetName
    val iosX64: SourceSetName

    val watchosArm32: SourceSetName
    val watchosArm64: SourceSetName
    val watchosSimulatorArm64: SourceSetName
    val watchosX64: SourceSetName

    val tvosArm64: SourceSetName
    val tvosSimulatorArm64: SourceSetName
    val tvosX64: SourceSetName

    val macosArm64: SourceSetName
    val macosX64: SourceSetName
}

val SourceSetNames.javaSet: Set<SourceSetName>
    get() = setOf(android, jvm)

val SourceSetNames.iosSet: Set<SourceSetName>
    get() = setOf(iosArm64, iosSimulatorArm64, iosX64)

val SourceSetNames.watchosSet: Set<SourceSetName>
    get() = setOf(watchosArm32, watchosArm64, watchosSimulatorArm64, watchosX64)

val SourceSetNames.tvosSet: Set<SourceSetName>
    get() = setOf(tvosArm64, tvosSimulatorArm64, tvosX64)

val SourceSetNames.macosSet: Set<SourceSetName>
    get() = setOf(macosX64, macosArm64)

val SourceSetNames.darwinSet: Set<SourceSetName>
    get() = listOf(iosSet, watchosSet, tvosSet, macosSet).flatten().toSet()

val SourceSetNames.nativeSet: Set<SourceSetName>
    get() = (darwinSet + linuxX64).toSet()

internal object DefaultSourceSetNames : SourceSetNames {

    override val common: SourceSetName by named()
    override val android: SourceSetName by named()
    override val jvm: SourceSetName by named()
    override val js: SourceSetName by named()
    override val linuxX64: SourceSetName by named()

    override val iosArm64: SourceSetName by named()
    override val iosSimulatorArm64: SourceSetName by named()
    override val iosX64: SourceSetName by named()

    override val watchosArm32: SourceSetName by named()
    override val watchosArm64: SourceSetName by named()
    override val watchosSimulatorArm64: SourceSetName by named()
    override val watchosX64: SourceSetName by named()

    override val tvosArm64: SourceSetName by named()
    override val tvosSimulatorArm64: SourceSetName by named()
    override val tvosX64: SourceSetName by named()

    override val macosX64: SourceSetName by named()
    override val macosArm64: SourceSetName by named()

    private fun named(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SourceSetName>> =
        PropertyDelegateProvider { _, property ->
            val name = SourceSetName(name = property.name)
            ReadOnlyProperty { _, _ -> name }
        }
}

interface SourceSetsScope : SourceSetNames {

    fun SourceSetName.dependsOn(vararg parents: SourceSetName)

    fun SourceSetName.dependsOn(parents: Iterable<SourceSetName>)

    fun Iterable<SourceSetName>.dependsOn(vararg parents: SourceSetName)

    fun Iterable<SourceSetName>.dependsOn(parents: Iterable<SourceSetName>)
}

internal fun SourceSetsScope.named(name: String, vararg parents: SourceSetName): SourceSetName {
    val child = SourceSetName(name = name)
    child.dependsOn(parents.asIterable())

    return child
}

fun SourceSetsScope.named(vararg parents: SourceSetName): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SourceSetName>> =
    PropertyDelegateProvider { _, property ->
        val name = named(name = property.name, parents = parents)
        ReadOnlyProperty { _, _ -> name }
    }

internal class DefaultSourceSetsScope : SourceSetsScope, SourceSetNames by DefaultSourceSetNames {

    private val _connections = ArrayList<Pair<SourceSetName, SourceSetName>>()
    val connections: List<Pair<SourceSetName, SourceSetName>> = _connections

    override fun SourceSetName.dependsOn(vararg parents: SourceSetName) {
        dependsOn(parents.asIterable())
    }

    override fun SourceSetName.dependsOn(parents: Iterable<SourceSetName>) {
        parents.forEach { parent ->
            _connections += this to parent
        }
    }

    override fun Iterable<SourceSetName>.dependsOn(vararg parents: SourceSetName) {
        dependsOn(parents.asIterable())
    }

    override fun Iterable<SourceSetName>.dependsOn(parents: Iterable<SourceSetName>) {
        forEach { child ->
            parents.forEach { parent ->
                _connections += child to parent
            }
        }
    }
}
