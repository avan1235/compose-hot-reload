import builds.BumpBootstrap
import builds.BumpDev
import builds.Nightly
import builds.PublishDev
import builds.Tests
import jetbrains.buildServer.configs.kotlin.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.sequential
import vcs.Github

/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

object ComposeHotReloadProject : Project({
    vcsRoot(Github)
    buildType(Tests)
    buildType(PublishDev)
    buildType(Nightly)
    buildType(BumpDev)
    buildType(BumpBootstrap)

    sequential {
        buildType(Tests)
        buildType(BumpDev)
        buildType(PublishDev)
        buildType(BumpBootstrap)
        buildType(Nightly)
    }

    params {
        password(
            "env.ORG_GRADLE_PROJECT_signing.key",
            "credentialsJSON:a8763adb-f827-47c7-a463-344294cd4850",
            display = ParameterDisplay.HIDDEN,
        )

        password(
            "env.ORG_GRADLE_PROJECT_signing.key.password",
            "credentialsJSON:55dbddf8-050d-4139-8a8c-82ede4c58523",
            display = ParameterDisplay.HIDDEN,
        )
    }
})
