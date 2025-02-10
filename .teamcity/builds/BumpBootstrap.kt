/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package builds

import builds.conventions.PushPrivilege
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object BumpBootstrap : BuildType({
    name = "Bump: bootstrap version"
    description = "Bumps bootstrap version"

    steps {
        script {
            name = "Setup Git"
            scriptContent = """
                git config user.email "compose-team@jetbrains.com"
                git config user.name "JetBrains Compose Team"
            """.trimIndent()
        }

        gradle {
            workingDir = "repository-tools"
            name = "Bump Build Number"
            tasks = "bumpBootstrapVersion"
        }
    }
}), PushPrivilege
