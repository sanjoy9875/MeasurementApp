package com.example.measurear.ui.measure

import androidx.compose.ui.geometry.Offset
import com.example.measurear.ar.ArMeasureSurfaceView

internal object ArTapCoordinateMapper {

    fun mapToSurfaceView(
        localTapOffset: Offset,
        overlayOriginInWindow: Offset,
        surfaceView: ArMeasureSurfaceView,
    ): Offset {
        val surfaceLocationOnScreen = IntArray(2)
        surfaceView.getLocationOnScreen(surfaceLocationOnScreen)

        val windowX = localTapOffset.x + overlayOriginInWindow.x
        val windowY = localTapOffset.y + overlayOriginInWindow.y

        val surfaceX = windowX - surfaceLocationOnScreen[0]
        val surfaceY = windowY - surfaceLocationOnScreen[1]
        return Offset(surfaceX, surfaceY)
    }
}
