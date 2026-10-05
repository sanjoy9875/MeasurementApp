package com.example.measurear.ui.measure.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.measurear.domain.model.ScreenPoint

@Composable
internal fun MeasurementOverlayCanvas(
    projectedPoints: List<ScreenPoint>,
    tapFeedbackPosition: Offset?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val pointRadius = 10.dp.toPx()
        val strokeWidth = 4.dp.toPx()

        tapFeedbackPosition?.let { tapPosition ->
            drawCircle(
                color = Color(0x80FFFFFF),
                radius = 18.dp.toPx(),
                center = tapPosition,
            )
        }

        projectedPoints.forEach { point ->
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = pointRadius,
                center = Offset(point.x, point.y),
            )
            drawCircle(
                color = Color.White,
                radius = pointRadius * 0.35f,
                center = Offset(point.x, point.y),
            )
        }

        if (projectedPoints.size >= 2) {
            val start = projectedPoints.first()
            val end = projectedPoints.last()
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(start.x, start.y),
                end = Offset(end.x, end.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
