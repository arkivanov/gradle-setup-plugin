package com.arkivanov.gradle

import org.gradle.kotlin.dsl.provideDelegate
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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
    val iosX64: SourceSetName
    val iosSet: Set<SourceSetName>

    val watchosArm32: SourceSetName
    val watchosArm64: SourceSetName
    val watchosX64: SourceSetName
    val watchosSet: Set<SourceSetName>

    val macosX64: SourceSetName
    val macosSet: Set<SourceSetName>
}

fun SourceSetNames.named(name: String): SourceSetName = SourceSetName(name = name)

fun SourceSetNames.named(): ReadOnlyProperty<Any?, SourceSetName> = SourceSetNameDelegate()

internal class SourceSetNameDelegate : ReadOnlyProperty<Any?, SourceSetName> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): SourceSetName =
        SourceSetName(name = property.name)
}

internal object DefaultSourceSetNames : SourceSetNames {

    override val common: SourceSetName by named()
    override val android: SourceSetName by named()
    override val jvm: SourceSetName by named()
    override val js: SourceSetName by named()
    override val linuxX64: SourceSetName by named()

    override val iosArm64: SourceSetName by named()
    override val iosX64: SourceSetName by named()
    override val iosSet: Set<SourceSetName> get() = setOf(iosArm64, iosX64)

    override val watchosArm32: SourceSetName by named()
    override val watchosArm64: SourceSetName by named()
    override val watchosX64: SourceSetName by named()
    override val watchosSet: Set<SourceSetName> get() = setOf(watchosArm32, watchosArm64, watchosX64)

    override val macosX64: SourceSetName by named()
    override val macosSet: Set<SourceSetName> get() = setOf(macosX64)
}

interface SourceSetsScope : SourceSetNames {

    infix fun SourceSetName.dependsOn(other: SourceSetName)

    infix fun Set<SourceSetName>.dependsOn(other: SourceSetName)
}

internal class DefaultSourceSetsScope : SourceSetsScope, SourceSetNames by DefaultSourceSetNames {

    private val _connections = ArrayList<Pair<SourceSetName, SourceSetName>>()
    val connections: List<Pair<SourceSetName, SourceSetName>> = _connections

    override fun SourceSetName.dependsOn(other: SourceSetName) {
        _connections += this to other
    }

    override fun Set<SourceSetName>.dependsOn(other: SourceSetName) {
        forEach {
            _connections += it to other
        }
    }
}
