package com.arkivanov.gradle

class BinaryCompatibilityValidatorConfig(
    val nonPublicMarkers: List<String> = emptyList(),
    val klib: Boolean = false,
)
