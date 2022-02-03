package com.arkivanov.gradle

import kotlin.reflect.KClass

sealed class Target {

    object Android : Target()
    object Jvm : Target()
    object Linux : Target()
    class Ios(val isAppleSiliconEnabled: Boolean = true) : Target()
    class WatchOs(val isAppleSiliconEnabled: Boolean = true) : Target()
    class TvOs(val isAppleSiliconEnabled: Boolean = true) : Target()
    class MacOs(val isAppleSiliconEnabled: Boolean = true) : Target()

    class Js(
        val mode: Mode = Mode.BOTH,
        val environments: Set<Environment> = Environment.values().toSet(),
        val binary: Binary = Binary.NONE,
    ) : Target() {
        enum class Mode {
            BOTH, IR, LEGACY
        }

        enum class Environment {
            BROWSER, NODE_JS
        }

        enum class Binary {
            NONE, EXECUTABLE, LIBRARY
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
