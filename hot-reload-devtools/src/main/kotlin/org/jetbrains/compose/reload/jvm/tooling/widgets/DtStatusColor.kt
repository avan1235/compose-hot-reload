/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.compose.reload.jvm.tooling.widgets

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.sellmair.evas.compose.composeFlow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.reload.jvm.tooling.states.ReloadState
import org.jetbrains.compose.reload.jvm.tooling.theme.DtColors
import kotlin.time.Duration.Companion.seconds

@Composable
fun animateReloadStatusColor(
    idleColor: Color = Color.LightGray,
    reloadingColor: Color = DtColors.statusColorOrange2,
    okColor: Color = DtColors.statusColorOk,
    errorColor: Color = DtColors.statusColorError,
): State<Color> {
    val color = remember { Animatable(idleColor) }
    val state = ReloadState.composeFlow()

    LaunchedEffect(idleColor, reloadingColor, okColor, errorColor) {
        state.collectLatest { state ->
            when (state) {
                is ReloadState.Reloading -> {
                    color.animateTo(reloadingColor)
                }

                is ReloadState.Failed -> {
                    color.animateTo(errorColor)
                }

                is ReloadState.Ok -> {
                    color.animateTo(okColor)
                    delay(1.seconds)
                    color.animateTo(idleColor)
                }
            }
        }
    }

    return color.asState()
}

@Composable
fun animatedReloadStatusBrush(
    idleColor: Color = Color.Transparent,
    okColor: Color = DtColors.statusColorOk,
    errorColor: Color = DtColors.statusColorError,
): Brush {
    val none = SolidColor(Color.Transparent)
    val brush = remember { mutableStateOf<Brush>(none) }

    val solidColor = remember { Animatable(idleColor) }
    val movingColorA = remember { Animatable(idleColor) }
    val movingColorB = remember { Animatable(idleColor) }

    val movingTransition = rememberInfiniteTransition()
    val movingGradientShift by movingTransition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing))
    )

    var showSolid by remember { mutableStateOf(true) }


    val state = ReloadState.composeFlow()
    LaunchedEffect(Unit) {
        state.collectLatest { state ->
            when (state) {
                is ReloadState.Ok -> {
                    coroutineScope {
                        launch { solidColor.animateTo(okColor) }
                        launch { movingColorA.animateTo(okColor) }
                        launch { movingColorB.animateTo(okColor) }
                    }

                    delay(1.seconds)
                    launch { solidColor.animateTo(idleColor) }
                    launch { movingColorA.animateTo(idleColor) }
                    launch { movingColorB.animateTo(idleColor) }
                    showSolid = true
                }

                is ReloadState.Failed -> {
                    coroutineScope {
                        launch { solidColor.animateTo(errorColor) }
                        launch { movingColorA.animateTo(errorColor) }
                        launch { movingColorB.animateTo(errorColor) }
                    }
                    brush.value = SolidColor(solidColor.value)
                    showSolid = true
                }

                is ReloadState.Reloading -> {
                    coroutineScope {
                        launch { solidColor.animateTo(DtColors.statusColorOrange2) }
                        launch { movingColorA.animateTo(DtColors.statusColorOrange1) }
                        launch { movingColorB.animateTo(DtColors.statusColorOrange2) }
                    }
                    showSolid = false
                }

            }
        }
    }

    return if (showSolid) SolidColor(solidColor.value)
    else Brush.linearGradient(
        colors = listOf(movingColorA.value, movingColorB.value),
        start = Offset(0f, movingGradientShift),
        end = Offset(0f, movingGradientShift + 400),
        tileMode = TileMode.Mirror,
    )
}

@Composable
fun Modifier.animateReloadStatusBackground(idleColor: Color): Modifier {
    val reloadStateColor by animateReloadStatusColor(idleColor = idleColor)
    return this.background(reloadStateColor.copy(alpha = 0.075f))
}

@Composable
fun Modifier.animatedReloadStatusBorder(
    width: Dp = 1.dp, shape: Shape = RoundedCornerShape(8.dp),
    idleColor: Color = Color.LightGray,
): Modifier {
    return border(
        width = width,
        brush = animatedReloadStatusBrush(
            idleColor = idleColor,
        ),
        shape = shape
    )
}
