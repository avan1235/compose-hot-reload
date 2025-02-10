/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.CheckoutMode
import jetbrains.buildServer.configs.kotlin.buildFeatures.sshAgent
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import vcs.Github

object BumpDev : BuildType({
    name = "Bump: dev version"

    features {
        sshAgent {
            teamcitySshKey = "compose-hot-reload-deploy-id_rsa"
        }
    }


    vcs {
        checkoutMode = CheckoutMode.ON_AGENT
        root(Github)
    }

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
            tasks = "bumpBuildNumber"
        }
    }
})
