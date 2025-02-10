/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

fun main() {
    val newVersion = readGradleProperties("bootstrap.version")
    command("git", "add", ".")
    command("git", "commit", "-m", "v$newVersion")
    command("git", "push")

    command("git", "tag", "v$newVersion")
    command("git", "push", "origin", "tag", "v$newVersion")
}
