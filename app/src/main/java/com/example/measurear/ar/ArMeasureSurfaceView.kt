package com.example.measurear.ar

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException

class ArMeasureSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), LifecycleEventObserver {

    private var callbacks: ArMeasureCallbacks? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val renderer: ArMeasureRenderer

    init {
        preserveEGLContextOnPause = true
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        renderer = ArMeasureRenderer(
            context = context,
            callbacks = object : ArMeasureCallbacks {
                override fun onTapQueued(x: Float, y: Float) {
                    mainHandler.post { callbacks?.onTapQueued(x, y) }
                }

                override fun onPlacementFailed() {
                    mainHandler.post { callbacks?.onPlacementFailed() }
                }

                override fun onPointPlaced(anchorCount: Int) {
                    mainHandler.post { callbacks?.onPointPlaced(anchorCount) }
                }

                override fun onFrameUpdated(
                    projectedPoints: List<com.example.measurear.domain.model.ScreenPoint>,
                    lengthMeters: Float?,
                ) {
                    mainHandler.post {
                        callbacks?.onFrameUpdated(projectedPoints, lengthMeters)
                    }
                }

                override fun onTrackingStateChanged(trackingState: TrackingState) {
                    mainHandler.post { callbacks?.onTrackingStateChanged(trackingState) }
                }

                override fun onCameraUnavailable() {
                    mainHandler.post { callbacks?.onCameraUnavailable() }
                }
            },
        )
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setArCallbacks(arCallbacks: ArMeasureCallbacks) {
        callbacks = arCallbacks
    }

    fun setPlacementEnabled(enabled: Boolean) {
        renderer.isPlacementEnabled = enabled
    }

    fun enqueueTap(x: Float, y: Float) {
        if (width <= 0 || height <= 0) return
        val clampedX = x.coerceIn(0f, width.toFloat())
        val clampedY = y.coerceIn(0f, height.toFloat())
        renderer.enqueueTap(clampedX, clampedY)
        requestRender()
    }

    fun clearAnchors() {
        queueEvent { renderer.clearAnchors() }
    }

    private fun resumeArSession() {
        val session = renderer.createSessionOnUiThread() ?: return
        try {
            session.resume()
            renderer.markSessionResumed()
        } catch (_: CameraNotAvailableException) {
            renderer.markSessionPaused()
            mainHandler.post { callbacks?.onCameraUnavailable() }
        } catch (_: Exception) {
            renderer.markSessionPaused()
        }
    }

    private fun pauseArSession() {
        renderer.markSessionPaused()
        renderer.session?.pause()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.let { owner ->
            lifecycleOwner = owner
            owner.lifecycle.addObserver(this)
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                onResume()
            }
        }
    }

    override fun onDetachedFromWindow() {
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null
        onPause()
        super.onDetachedFromWindow()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            else -> Unit
        }
    }

    public override fun onResume() {
        resumeArSession()
        super.onResume()
        renderer.updateDisplayGeometryIfNeeded()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        renderer.updateDisplayGeometryIfNeeded()
    }

    public override fun onPause() {
        pauseArSession()
        super.onPause()
    }
}
