package com.example.measurear.ar

import android.content.Context
import android.view.WindowManager
import com.google.ar.core.Session

internal object ArDisplayGeometry {

    fun displayRotation(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        return windowManager.defaultDisplay.rotation
    }

    fun updateSessionGeometry(
        session: Session,
        context: Context,
        width: Int,
        height: Int,
    ) {
        if (width <= 0 || height <= 0) return
        session.setDisplayGeometry(displayRotation(context), width, height)
    }
}
