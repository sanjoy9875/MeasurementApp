package com.example.measurear.ui.measure.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

@Composable
internal fun MeasureTouchCaptureLayer(
    onScreenTap: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.pointerInput(onScreenTap) {
            detectTapGestures { offset ->
                onScreenTap(offset)
            }
        },
    )
}
