plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("maven-publish")
}

group = "com.arkivanov.gradle"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    compileOnly("com.android.tools.build:gradle:7.1.0")
    compileOnly("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.3.1")
    compileOnly("org.jetbrains.kotlinx.binary-compatibility-validator:org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.8.0")
    compileOnly("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins.create(project.name) {
        id = "com.arkivanov.gradle.setup"
        implementationClass = "com.arkivanov.gradle.GradleSetupPlugin"
    }
}
