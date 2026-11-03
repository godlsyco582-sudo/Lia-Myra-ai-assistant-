package com.Lia.assistant.ui.fx

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

object OverlayEdgeGlowController {
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        if (overlayView != null) return

        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            val composeView = ComposeView(context).apply {
                setContent {
                    ListeningEdgeGlow()
                }
            }

            wm.addView(composeView, params)
            windowManager = wm
            overlayView = composeView
        } catch (_: Exception) {}
    }

    fun hide() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
            }
        } catch (_: Exception) {}
        overlayView = null
        windowManager = null
    }
}
