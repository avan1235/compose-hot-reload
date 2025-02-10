import builds.Nightly
import builds.PublishDev
import builds.Tests
import jetbrains.buildServer.configs.kotlin.Project
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
})
