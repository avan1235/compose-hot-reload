/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package vcs

import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

object Github : GitVcsRoot({
    name = "https://github.com/JetBrains/compose-hot-reload#refs/heads/master"
    url = "https://github.com/JetBrains/compose-hot-reload"
    branch = "refs/heads/master"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "sellmair"
        password = "credentialsJSON:da04c3b1-7589-4c6f-87ee-83cf8ab7827d"
    }
})
