package com.arkivanov.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.logging.Logger
import org.gradle.kotlin.dsl.extra

private const val PROPERTY_NAME = "com.arkivanov.gradle.disableTasks"

internal fun Project.ensureUnreachableTasksDisabled() {
    require(rootProject == this) { "Must be called on a root project" }

    if (extra.has(PROPERTY_NAME)) {
        return
    }

    extra.set(PROPERTY_NAME, true)

    gradle.taskGraph.whenReady {
        allTasks
            .filterNot { it.enabled }
            .forEach { disableChildren(it, logger) }
    }
}

private fun TaskExecutionGraph.disableChildren(task: Task, logger: Logger) {
    getDependencies(task).forEach { child ->
        if (child.enabled) {
            if (!isTaskAccessible(child)) {
                child.enabled = false
                logger.info("Task disabled: ${child.path}")
                disableChildren(child, logger)
            } else {
                logger.info("Task accessible: ${child.path}")
            }
        } else {
            logger.info("Task already disabled: ${child.path}")
            disableChildren(child, logger)
        }
    }
}

private fun TaskExecutionGraph.isTaskAccessible(task: Task): Boolean =
    allTasks.any { (it != task) && isPathExists(source = it, destination = task) }

private fun TaskExecutionGraph.isPathExists(source: Task, destination: Task): Boolean =
    when {
        !source.enabled -> false
        source == destination -> true
        else -> getDependencies(source).any { isPathExists(source = it, destination = destination) }
    }
