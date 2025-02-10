/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.gradleCache
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import vcs.Github

object BumpBootstrap : BuildType({
    name = "Bump: bootstrap version"
    description = "Bumps bootstrap version"

    vcs {
        root(Github)
    }

    features {
        perfmon { }
        gradleCache {  }
    }

    steps {
        gradle {
            workingDir = "repository-tools"
            name = "Bump Build Number"
            tasks = "bumpBootstrapVersion"
        }
    }
})
