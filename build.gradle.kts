import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    compileOnly("com.android.tools.build:gradle:8.7.3")
    compileOnly("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.17.3")
    compileOnly("org.jetbrains.kotlinx:binary-compatibility-validator:0.16.3")
    compileOnly("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins.create(project.name) {
        id = "com.arkivanov.gradle.setup"
        implementationClass = "com.arkivanov.gradle.GradleSetupPlugin"
    }
}
