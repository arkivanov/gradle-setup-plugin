import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("maven-publish")
}

group = "com.arkivanov.gradle"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    compileOnly("com.android.tools.build:gradle:9.1.1")
    compileOnly("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.17.3")
    compileOnly("org.jetbrains.kotlinx:binary-compatibility-validator:0.18.2")
    compileOnly("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins.create(project.name) {
        id = "com.arkivanov.gradle.setup"
        implementationClass = "com.arkivanov.gradle.GradleSetupPlugin"
    }
}
