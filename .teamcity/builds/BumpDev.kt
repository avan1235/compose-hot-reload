/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package builds

import builds.conventions.PushPrivilege
import builds.conventions.publishDevVersion
import builds.conventions.setupGit
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object BumpDev : BuildType({
    name = "Bump: dev version"

    steps {
        setupGit()

        gradle {
            workingDir = "repository-tools"
            name = "Bump Dev Version"
            tasks = "bumpDevVersion"
        }

        publishDevVersion()

        gradle {
            workingDir = "repository-tools"
            name = "Push Dev Version"
            tasks = "pushDevVersion"
        }
    }
}), PushPrivilege
