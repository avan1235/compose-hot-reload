/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package builds

import builds.conventions.PublishDevPrivilege
import builds.conventions.publishDevVersion
import jetbrains.buildServer.configs.kotlin.BuildType

object PublishDev : BuildType({
    name = "Publish Dev"

    steps {
        publishDevVersion()
    }
}), PublishDevPrivilege
