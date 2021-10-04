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
        DisableTasks(graph = this, logger = logger)
            .disableTasks()
    }
}

private class DisableTasks(
    private val graph: TaskExecutionGraph,
    private val logger: Logger,
) {
    private val results = HashMap<Pair<Task, Task>, Boolean>()

    fun disableTasks() {
        graph
            .allTasks
            .filterNot { it.enabled }
            .forEach { disableChildren(it) }
    }

    private fun disableChildren(task: Task) {
        graph.getDependencies(task).forEach { child ->
            if (child.enabled) {
                if (!isTaskAccessible(task = child)) {
                    child.enabled = false
                    logger.info("Task disabled: ${child.path}")
                    disableChildren(task = child)
                } else {
                    logger.info("Task accessible: ${child.path}")
                }
            } else {
                logger.info("Task already disabled: ${child.path}")
                disableChildren(task = child)
            }
        }
    }

    private fun isTaskAccessible(task: Task): Boolean =
        graph.allTasks.any { (it != task) && isPathExists(source = it, destination = task) }

    private fun isPathExists(source: Task, destination: Task): Boolean =
        results.getOrPut(source to destination) {
            when {
                !source.enabled -> false
                source == destination -> true
                else -> graph.getDependencies(source).any { isPathExists(source = it, destination = destination) }
            }
        }
}
