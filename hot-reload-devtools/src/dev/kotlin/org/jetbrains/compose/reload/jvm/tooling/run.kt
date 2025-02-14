/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package org.jetbrains.compose.reload.jvm.tooling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.sellmair.evas.compose.installEvas
import io.sellmair.evas.eventsOrThrow
import io.sellmair.evas.statesOrThrow
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import org.jetbrains.compose.reload.jvm.tooling.sidecar.DtSidecarWindowContent

@DevelopmentEntryPoint
@Composable
fun DevToolingSidecarEntryPoint() {
    LaunchedEffect(Unit) {
        applicationScope.launchApplicationStates()
    }

    installEvas(applicationScope.coroutineContext.eventsOrThrow, applicationScope.coroutineContext.statesOrThrow) {
        var isExpanded by remember { mutableStateOf(true) }
        DtSidecarWindowContent(isExpanded, isExpandedChanged = { isExpanded = it })
    }
}
