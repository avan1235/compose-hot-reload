/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.compose.reload.tests.gradle

import org.jetbrains.compose.reload.orchestration.OrchestrationClientRole
import org.jetbrains.compose.reload.orchestration.OrchestrationMessage
import org.jetbrains.compose.reload.orchestration.OrchestrationMessage.UIRendered
import org.jetbrains.compose.reload.test.gradle.HotReloadTest
import org.jetbrains.compose.reload.test.gradle.HotReloadTestFixture

import org.jetbrains.compose.reload.test.gradle.initialSourceCode
import org.jetbrains.compose.reload.utils.QuickTest
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.jvm.optionals.getOrNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class PidFileTest {

    @HotReloadTest
    @QuickTest
    fun `test - pid file`(fixture: HotReloadTestFixture) = fixture.runTest {
        val pidFile = fixture.projectDir.resolve("build/run/jvmMain/jvmRunHot.pid")

        fixture.runTransaction {
            fixture initialSourceCode """
                import org.jetbrains.compose.reload.test.*
                
                fun main() {
                    screenshotTestApplication {
                    }
                }
            """.trimIndent()

            skipToMessage<UIRendered>()
            assertTrue(pidFile.exists(), "Expected .pid file to exist: ${pidFile.toUri()}")
            pidFile
        }

        val properties = Properties()
        properties.load(pidFile.readText().reader())


        val pid = properties.getProperty("pid")
        if (pid.toLongOrNull() == null) {
            fail("Invalid pid value: $pid")
        }

        val orchestrationPort = properties.getProperty("orchestration.port")
        if (orchestrationPort.toIntOrNull() == null) {
            fail("Invalid orchestration port value: $orchestrationPort")
        }
        assertEquals(fixture.orchestration.port, orchestrationPort.toInt())
        val processHandle = ProcessHandle.of(pid.toLong()).getOrNull() ?: fail("Process with pid=$pid not found")

        fixture.sendMessage(OrchestrationMessage.ShutdownRequest("Explicitly requested by the test")) {
            skipToMessage<OrchestrationMessage.ClientDisconnected> { message ->
                message.clientRole == OrchestrationClientRole.Application
            }
        }

        if (processHandle.onExit().get(1, TimeUnit.MINUTES) == null) {
            fail("Timeout while waiting for the process to exit: ${processHandle.pid()}")
        }

        if (pidFile.exists()) fail(
            "Expected .pid file to be deleted: ${pidFile.toUri()}"
        )
    }
}
