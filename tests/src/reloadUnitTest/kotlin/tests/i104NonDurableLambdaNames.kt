/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.jetbrains.compose.reload.test.HotReloadUnitTest
import org.jetbrains.compose.reload.test.compileAndReload
import utils.readSource

@OptIn(ExperimentalTestApi::class)
@HotReloadUnitTest
fun `test - #104 -remembered composable lambdas produce non-durable class names`() = runComposeUiTest {
    setContent {
        I104NonDurableLambdaNames.render()
    }

    var source = readSource("i104NonDurableLambdaNames.object.kt")
    source = source
        .replace("import androidx.compose.material3.Surface", "import androidx.compose.material3.Card")
        .replace("Surface {", "Card {")

    compileAndReload(source)
}
