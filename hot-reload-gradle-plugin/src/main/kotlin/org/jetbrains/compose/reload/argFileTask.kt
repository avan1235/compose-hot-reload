/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.compose.reload

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.reload.gradle.files
import java.io.File
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream

internal fun Project.setupArgfileTasks() {
    afterEvaluate {
        afterEvaluate {
            createArgfileTasks()
        }
    }
}

private fun Project.createArgfileTasks() {
    val runTasks = tasks.withType<AbstractComposeHotRun>()
    runTasks.names.forEach { runTaskName ->
        val runTask = runTasks.named(runTaskName)
        createArgfileTask(runTask)
    }
}

private fun Project.createArgfileTask(runTask: TaskProvider<AbstractComposeHotRun>) {
    val argfileTaskName = runTask.name + "Argfile"
    val createArgsTask = tasks.register(argfileTaskName, ComposeHotReloadArgfileTask::class.java) { task ->
        task.runTaskName.set(runTask.name)
        task.argFile.set(provider { runTask.get().compilation.get().runBuildFile("${runTask.name}.argfile").get() })
        task.arguments.addAll(provider { runTask.get().allJvmArgs })
        task.classpath.from(project.files { runTask.get().classpath })
    }

    runTask.configure { task ->
        task.dependsOn(createArgsTask)
        task.argFile.set(provider { createArgsTask.get().argFile.get() })
        task.argFileTaskName.set(argfileTaskName)
    }
}

internal open class ComposeHotReloadArgfileTask : DefaultTask() {
    @get:Input
    internal val arguments = project.objects.listProperty(String::class.java)

    @get:Classpath
    internal val classpath = project.objects.fileCollection()

    @get:Input
    internal val runTaskName = project.objects.property(String::class.java)

    @get:OutputFile
    internal val argFile = project.objects.fileProperty()

    @TaskAction
    internal fun createArgfile() {
        val argFile = this@ComposeHotReloadArgfileTask.argFile.get().asFile.toPath()
        argFile.createParentDirectories()
        argFile.outputStream().bufferedWriter().use { writer ->
            arguments.get().forEach { arg -> writer.appendLine(arg) }
            writer.appendLine("-cp \"${classpath.joinToString(separator = "${File.pathSeparator}\\\n")}\"")
        }
    }
}
