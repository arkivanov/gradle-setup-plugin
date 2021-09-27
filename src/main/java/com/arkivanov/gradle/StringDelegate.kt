package com.arkivanov.gradle

import kotlin.reflect.KProperty

internal operator fun StringDelegate.getValue(receiver: Any?, property: KProperty<*>): String = property.name

internal fun string(): StringDelegate = StringDelegate()

internal class StringDelegate
