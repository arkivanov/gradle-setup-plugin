package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal val Project.multiplatformExtension: KotlinMultiplatformExtension
    get() = kotlinExtension as KotlinMultiplatformExtension

internal inline fun <reified T : Any> Project.hasExtension(): Boolean =
    try {
        extensions.findByType(T::class) != null
    } catch (e: NoClassDefFoundError) {
        false
    }

internal inline fun <reified T : Any> Project.configureExtension(crossinline configure: T.() -> Unit) {
    if (hasExtension<T>()) {
        extensions.configure<T> {
            configure()
        }
    }
}

internal fun Project.checkIsRootProject() {
    check(rootProject == this) { "Must be called on a root project" }
}
