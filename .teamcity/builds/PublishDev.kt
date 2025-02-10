/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import vcs.Github

object PublishDev : BuildType({
    name = "Publish Dev"

    vcs {
        root(Github)
    }

    features {
        perfmon { }
    }

    steps {
        gradle {
            name = "clean"
            tasks = "clean"
        }

        gradle {
            name = "Publish Locally"
            name = "publishLocally"
        }

        gradle {
            name = "Api Check"
            tasks = "apiCheck"
        }

        gradle {
            name = "Publish to Firework Repository"
            tasks = "publishAllPublicationsToFireworkRepository --no-configuration-cache"
        }

        gradle {
            name = "Publish to Sellmair Repository"
            tasks = "publishAllPublicationsToSellmairRepository --no-configuration-cache"
        }
    }
})
