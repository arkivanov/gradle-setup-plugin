package com.arkivanov.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.withGroovyBuilder

internal fun BaseExtension.setupAndroid() {
    compileSdkVersion(31)

    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(31)
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    withGroovyBuilder {
        "kotlinOptions" {
            setProperty("jvmTarget", "1.8")
        }
    }
}
