package com.example.measurear.ar

import android.opengl.Matrix
import com.example.measurear.domain.model.ScreenPoint
import com.example.measurear.domain.model.WorldPoint
import com.google.ar.core.Frame
import com.google.ar.core.Pose

object ArProjectionUtils {

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val worldPointHomogeneous = FloatArray(4)

    fun projectToScreen(
        frame: Frame,
        worldPoint: WorldPoint,
        viewWidth: Int,
        viewHeight: Int,
    ): ScreenPoint? {
        if (viewWidth <= 0 || viewHeight <= 0) return null

        val camera = frame.camera
        camera.getViewMatrix(viewMatrix, 0)
        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
        Matrix.multiplyMM(
            modelViewProjectionMatrix,
            0,
            projectionMatrix,
            0,
            viewMatrix,
            0,
        )

        worldPointHomogeneous[0] = worldPoint.x
        worldPointHomogeneous[1] = worldPoint.y
        worldPointHomogeneous[2] = worldPoint.z
        worldPointHomogeneous[3] = 1f

        Matrix.multiplyMV(worldPointHomogeneous, 0, modelViewProjectionMatrix, 0, worldPointHomogeneous, 0)

        val clipW = worldPointHomogeneous[3]
        if (clipW <= 0f) return null

        val normalizedX = worldPointHomogeneous[0] / clipW
        val normalizedY = worldPointHomogeneous[1] / clipW

        val screenX = ((normalizedX + 1f) * 0.5f) * viewWidth
        val screenY = ((1f - normalizedY) * 0.5f) * viewHeight

        return ScreenPoint(screenX, screenY)
    }

    fun poseToWorldPoint(pose: Pose): WorldPoint {
        return WorldPoint(
            x = pose.tx(),
            y = pose.ty(),
            z = pose.tz(),
        )
    }
}
