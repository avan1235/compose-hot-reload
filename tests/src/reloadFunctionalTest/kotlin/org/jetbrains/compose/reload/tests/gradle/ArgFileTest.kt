/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.compose.reload.tests.gradle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.jetbrains.compose.reload.core.createLogger
import org.jetbrains.compose.reload.core.destroyWithDescendants
import org.jetbrains.compose.reload.orchestration.OrchestrationClientRole.Application
import org.jetbrains.compose.reload.orchestration.OrchestrationMessage
import org.jetbrains.compose.reload.orchestration.OrchestrationMessage.ClientConnected
import org.jetbrains.compose.reload.orchestration.OrchestrationMessage.ShutdownRequest
import org.jetbrains.compose.reload.test.gradle.GradleRunner
import org.jetbrains.compose.reload.test.gradle.HotReloadTest
import org.jetbrains.compose.reload.test.gradle.HotReloadTestFixture
import org.jetbrains.compose.reload.test.gradle.ProjectMode
import org.jetbrains.compose.reload.test.gradle.build
import org.jetbrains.compose.reload.test.gradle.getDefaultMainKtSourceFile
import org.jetbrains.compose.reload.utils.GradleIntegrationTest
import org.jetbrains.compose.reload.utils.HostIntegrationTest
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.fail

class ArgFileTest {
    private val logger = createLogger()

    @HotReloadTest
    @HostIntegrationTest
    @GradleIntegrationTest
    fun `test - launch application using argfile`(fixture: HotReloadTestFixture) = fixture.runTest {
        val testJob = currentCoroutineContext().job

        fixture.projectDir.resolve(fixture.getDefaultMainKtSourceFile()).createParentDirectories().writeText(
            """
            import org.jetbrains.compose.reload.test.*
            fun main() {
                screenshotTestApplication {
                }
            }
            """.trimIndent()
        )

        val runTaskName = when (projectMode) {
            ProjectMode.Kmp -> "jvmRunHot"
            ProjectMode.Jvm -> "runHot"
        }

        val compilationClassifier = when (projectMode) {
            ProjectMode.Kmp -> "jvmMain"
            ProjectMode.Jvm -> "main"
        }

        assertEquals(GradleRunner.ExitCode.success, fixture.gradleRunner.build("${runTaskName}Argfile"))

        val (applicationProcess, applicationConnectedMessage) = fixture.runTransaction {
            val applicationProcess = ProcessBuilder(
                ProcessHandle.current().info().command().orElseThrow(),
                "@${fixture.projectDir.resolve("build/run/$compilationClassifier/$runTaskName.argfile")}",
                "MainKt"
            ).start()

            testJob.invokeOnCompletion {
                applicationProcess.destroyWithDescendants()
            }

            fixture.launchTestDaemon(Dispatchers.IO) {
                launch {
                    currentCoroutineContext().job.invokeOnCompletion {
                        applicationProcess.errorStream.close()
                    }
                }
                applicationProcess.errorStream.bufferedReader().forEachLine {
                    logger.error("Application stderr: $it")
                }
            }

            fixture.launchTestDaemon(Dispatchers.IO) {
                launch {
                    currentCoroutineContext().job.invokeOnCompletion {
                        applicationProcess.inputStream.close()
                    }
                }
                applicationProcess.inputStream.bufferedReader().forEachLine {
                    logger.info("Application stdout: $it")
                }
            }


            val applicationConnectedMessage = select {
                /* If the process dies before we get the 'connected' message, we can fail the test */
                applicationProcess.onExit().asDeferred().onAwait {
                    fail("Application process exited with ${applicationProcess.exitValue()}")
                }

                async { skipToMessage<ClientConnected> { it.clientRole == Application } }.onAwait { it }
            }

            applicationProcess to applicationConnectedMessage
        }

        assertEquals(applicationProcess.pid(), applicationConnectedMessage.clientPid)

        runTransaction { skipToMessage<OrchestrationMessage.UIRendered>() }
        orchestration.sendMessage(ShutdownRequest("Requested by test")).get()
        applicationProcess.onExit().asDeferred().await()

        if (applicationProcess.exitValue() !in listOf(0, 134)) fail(
            "Expected the test application to exit with 0 or 134 (SIGTERM), " +
                "but it exited with ${applicationProcess.exitValue()}"
        )

        if (applicationProcess.isAlive) fail("Expected the test application to be terminated")
    }
}
