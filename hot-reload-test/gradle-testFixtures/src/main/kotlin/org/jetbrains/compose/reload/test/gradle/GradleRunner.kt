/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.compose.reload.test.gradle

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import org.jetbrains.compose.reload.core.destroyWithDescendants
import org.jetbrains.compose.reload.test.core.InternalHotReloadTestApi
import org.jetbrains.compose.reload.test.gradle.GradleRunner.ExitCode
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText
import kotlin.test.fail

private val logger = LoggerFactory.getLogger("Gradle")

public data class GradleRunner @InternalHotReloadTestApi constructor(
    val projectRoot: Path,
    val gradleVersion: String,
    val arguments: List<String> = emptyList(),
    val gradleHome: Path = Path("build/gradleHome"),
) {
    @JvmInline
    public value class ExitCode internal constructor(public val value: Int) {
        public companion object {
            public val success: ExitCode = ExitCode(0)
        }
    }
}

public fun ExitCode?.assertSuccess() {
    if (this == null) fail("Expected successful execution; No exit code received")
    if (value != 0) fail("Expected successful execution; Exit code: $value")
}

public suspend fun GradleRunner.build(vararg args: String): ExitCode? {
    gradleHome.createDirectories()
    createWrapperPropertiesFile()
    copyGradleWrapper()

    val gradleScriptCommand = if ("win" in System.getProperty("os.name").lowercase()) {
        arrayOf("cmd", "/c", "gradlew.bat")
    } else {
        arrayOf("./gradlew")
    }

    val processBuilder = ProcessBuilder().directory(projectRoot.toFile()).command(
        *gradleScriptCommand,
        "-i", "-s",
        "--console=plain",
        "--configuration-cache",
        "--configuration-cache-problems=warn",
        *arguments.toTypedArray(),
        *args,
    )

    val exitCode = CompletableDeferred<ExitCode?>()
    val scopeJob = currentCoroutineContext().job

    val thread = thread(name = "Gradle Runner") {
        Thread.currentThread().setUncaughtExceptionHandler { _, e ->
            logger.error("Uncaught exception in Gradle runner thread", e)
            exitCode.complete(null)
        }

        logger.info("Starting Gradle runner: ${processBuilder.command().joinToString("\n")}")
        val process = processBuilder.start()

        scopeJob.invokeOnCompletion {
            if (process.isAlive) {
                logger.info("Killing Gradle invocation at '${process.pid()}'")
                process.destroy()
            }
        }

        thread(name = "Gradle Runner Output Reader") {
            process.inputStream.bufferedReader().use { reader ->
                while (true) {
                    logger.debug(reader.readLine() ?: break)
                }
            }
        }

        thread(name = "Gradle Runner Error Reader") {
            process.errorStream.bufferedReader().use { reader ->
                while (true) {
                    logger.debug(reader.readLine() ?: break)
                }
            }
        }

        val shutdownHook = Thread {
            process.destroyWithDescendants()
        }

        Runtime.getRuntime().addShutdownHook(shutdownHook)

        try {
            exitCode.complete(ExitCode(process.waitFor()))
        } catch (_: InterruptedException) {
            exitCode.complete(null)
            process.destroy()
        }

        runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }
    }

    scopeJob.invokeOnCompletion {
        if (!exitCode.isActive) {
            logger.error("Sending 'Shutdown' signal to Gradle runner thread")
            thread.interrupt()
        }
    }

    return exitCode.await()
}

private fun GradleRunner.createWrapperPropertiesFile() {
    projectRoot.resolve("gradle/wrapper/gradle-wrapper.properties").createParentDirectories().writeText(
        """
        distributionBase=GRADLE_USER_HOME
        distributionPath=wrapper/dists
        distributionUrl=https\://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip
        networkTimeout=10000
        validateDistributionUrl=true
        zipStoreBase=GRADLE_USER_HOME
        zipStorePath=wrapper/dists
    """.trimIndent()
    )
}


private fun GradleRunner.copyGradleWrapper() {
    projectRoot.createDirectories()

    gradleWrapperFile().copyTo(projectRoot.resolve("gradlew"), overwrite = true)
    gradleWrapperBatFile().copyTo(projectRoot.resolve("gradlew.bat"), overwrite = true)
    gradleWrapperJarFile().copyTo(
        projectRoot.resolve("gradle/wrapper/gradle-wrapper.jar").createParentDirectories(),
        overwrite = true
    )
}
