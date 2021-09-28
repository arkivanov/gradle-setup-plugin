package com.arkivanov.gradle

import kotlin.reflect.KClass

sealed class Target {

    object Android : Target()
    object Jvm : Target()
    object Linux : Target()
    object Ios : Target()
    object WatchOs : Target()
    object TvOs : Target()
    object MacOs : Target()

    class Js(val mode: Mode = Mode.BOTH) : Target() {
        enum class Mode {
            BOTH, IR, LEGACY
        }
    }

    companion object {
        internal val LINUX_SPLIT_CLASSES: List<KClass<out Target>> by lazy {
            listOf(
                Android::class,
                Jvm::class,
                Linux::class,
                Js::class,
            )
        }

        internal val MACOS_SPLIT_CLASSES: List<KClass<out Target>> by lazy {
            listOf(
                Ios::class,
                WatchOs::class,
                TvOs::class,
                MacOs::class,
            )
        }
    }
}
