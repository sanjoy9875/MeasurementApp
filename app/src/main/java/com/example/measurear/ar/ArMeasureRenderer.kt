package com.example.measurear.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.core.content.ContextCompat
import com.example.measurear.domain.MeasurementCalculator
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.SessionPausedException
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class ArMeasureRenderer(
    private val context: Context,
    private val callbacks: ArMeasureCallbacks,
) : GLSurfaceView.Renderer {

    private val backgroundRenderer = BackgroundRenderer()
    private val pendingTap = AtomicReference<TapEvent?>(null)
    private val placedAnchors = mutableListOf<Anchor>()
    private var referencePlane: Plane? = null
    private var referenceHitDistanceMeters: Float? = null
    private var fixedLengthMeters: Float? = null

    private var viewportWidth = 0
    private var viewportHeight = 0
    private var lastPublishedTrackingState: TrackingState? = null
    private var notifiedPlacementFailedForCurrentTap = false

    var session: Session? = null
        private set

    @Volatile
    var isSessionResumed: Boolean = false
        private set

    @Volatile
    var isPlacementEnabled: Boolean = false

    fun markSessionResumed() {
        isSessionResumed = true
    }

    fun markSessionPaused() {
        isSessionResumed = false
    }

    fun enqueueTap(x: Float, y: Float) {
        notifiedPlacementFailedForCurrentTap = false
        pendingTap.set(TapEvent(x, y))
    }

    fun clearAnchors() {
        placedAnchors.forEach { anchor -> anchor.detach() }
        placedAnchors.clear()
        referencePlane = null
        referenceHitDistanceMeters = null
        fixedLengthMeters = null
        pendingTap.set(null)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        backgroundRenderer.createOnGlThread(context)
    }

    fun createSessionOnUiThread(): Session? {
        if (session != null) return session
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        return try {
            Session(context).apply {
                configure(buildSessionConfig(this))
            }.also { createdSession ->
                session = createdSession
                if (viewportWidth > 0 && viewportHeight > 0) {
                    ArDisplayGeometry.updateSessionGeometry(
                        session = createdSession,
                        context = context,
                        width = viewportWidth,
                        height = viewportHeight,
                    )
                }
            }
        } catch (_: SecurityException) {
            null
        }
    }

    private fun buildSessionConfig(session: Session): Config {
        return Config(session).apply {
            focusMode = Config.FocusMode.AUTO
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            depthMode = Config.DepthMode.DISABLED
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        GLES20.glViewport(0, 0, width, height)
        session?.let { activeSession ->
            ArDisplayGeometry.updateSessionGeometry(
                session = activeSession,
                context = context,
                width = width,
                height = height,
            )
        }
    }

    fun updateDisplayGeometryIfNeeded() {
        session?.let { activeSession ->
            if (viewportWidth > 0 && viewportHeight > 0) {
                ArDisplayGeometry.updateSessionGeometry(
                    session = activeSession,
                    context = context,
                    width = viewportWidth,
                    height = viewportHeight,
                )
            }
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!isSessionResumed || session == null) {
            return
        }

        val currentSession = session ?: return

        try {
            currentSession.setCameraTextureName(backgroundRenderer.getTextureId())
            val frame = currentSession.update()
            if (frame.timestamp == 0L) {
                return
            }

            backgroundRenderer.draw(frame)
            publishTrackingState(frame.camera.trackingState)
            processPendingTap(frame)

            if (placedAnchors.isNotEmpty()) {
                publishFrameState(frame)
            }
        } catch (_: SessionPausedException) {
            publishTrackingState(TrackingState.PAUSED)
        } catch (_: CameraNotAvailableException) {
            callbacks.onCameraUnavailable()
        }
    }

    private fun processPendingTap(frame: Frame) {
        val tap = pendingTap.get() ?: return
        if (!isPlacementEnabled) return
        if (placedAnchors.size >= MAX_ANCHORS) {
            pendingTap.set(null)
            return
        }

        val cameraTracking = frame.camera.trackingState == TrackingState.TRACKING
        val hitResult = ArHitTester.findMeasurementHit(
            frame = frame,
            x = tap.x,
            y = tap.y,
            preferredPlane = referencePlane,
            referenceDistanceMeters = referenceHitDistanceMeters,
            allowInstantPlacement = cameraTracking,
        ) ?: run {
            if (!notifiedPlacementFailedForCurrentTap) {
                notifiedPlacementFailedForCurrentTap = true
                callbacks.onPlacementFailed()
            }
            return
        }

        pendingTap.set(null)
        notifiedPlacementFailedForCurrentTap = false

        val anchor = hitResult.createAnchor()
        placedAnchors.add(anchor)
        if (referenceHitDistanceMeters == null) {
            referenceHitDistanceMeters = hitResult.distance
        }
        if (referencePlane == null) {
            referencePlane = hitResult.trackable as? Plane
        }

        if (placedAnchors.size >= MAX_ANCHORS) {
            fixedLengthMeters = measureAnchorSpanMeters()
        }

        callbacks.onPointPlaced(placedAnchors.size)
        publishFrameState(frame)
    }

    private fun measureAnchorSpanMeters(): Float {
        if (placedAnchors.size < MAX_ANCHORS) return 0f
        val start = ArProjectionUtils.poseToWorldPoint(placedAnchors[0].pose)
        val end = ArProjectionUtils.poseToWorldPoint(placedAnchors[1].pose)
        return MeasurementCalculator.distanceMeters(start, end)
    }

    private fun publishTrackingState(trackingState: TrackingState) {
        if (trackingState == lastPublishedTrackingState) return
        lastPublishedTrackingState = trackingState
        callbacks.onTrackingStateChanged(trackingState)
    }

    private fun publishFrameState(frame: Frame) {
        val worldPoints = placedAnchors.map { anchor ->
            ArProjectionUtils.poseToWorldPoint(anchor.pose)
        }
        val projectedPoints = worldPoints.mapNotNull { worldPoint ->
            ArProjectionUtils.projectToScreen(
                frame = frame,
                worldPoint = worldPoint,
                viewWidth = viewportWidth,
                viewHeight = viewportHeight,
            )
        }
        callbacks.onFrameUpdated(
            projectedPoints = projectedPoints,
            lengthMeters = fixedLengthMeters,
        )
    }

    private data class TapEvent(val x: Float, val y: Float)

    companion object {
        const val MAX_ANCHORS = 2
    }
}

interface ArMeasureCallbacks {
    fun onTapQueued(x: Float, y: Float)
    fun onPlacementFailed()
    fun onPointPlaced(anchorCount: Int)
    fun onFrameUpdated(
        projectedPoints: List<com.example.measurear.domain.model.ScreenPoint>,
        lengthMeters: Float?,
    )
    fun onTrackingStateChanged(trackingState: TrackingState)
    fun onCameraUnavailable()
}
