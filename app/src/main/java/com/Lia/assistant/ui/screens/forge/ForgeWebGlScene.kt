package com.Lia.assistant.ui.screens.forge

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.Lia.assistant.forge.ForgeStage

class ForgeJsBridge(
    private val onReady: () -> Unit,
    private val onRevealFinished: () -> Unit,
    private val onWebGLFailed: () -> Unit
) {
    @JavascriptInterface
    fun onReady() {
        onReady.invoke()
    }

    @JavascriptInterface
    fun onRevealFinished() {
        onRevealFinished.invoke()
    }

    @JavascriptInterface
    fun onWebGLFailed() {
        onWebGLFailed.invoke()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ForgeWebGlScene(
    stage: ForgeStage,
    progress: Float,
    isRevealing: Boolean,
    onRevealFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef: WebView? = remember { null }

    val bridge = remember {
        ForgeJsBridge(
            onReady = {},
            onRevealFinished = onRevealFinished,
            onWebGLFailed = {}
        )
    }

    val assetLoader = remember {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    LaunchedEffect(stage, progress) {
        val js = "window.Forge && window.Forge.setStage && window.Forge.setStage(${stage.ordinal}, '${stage.label}'); window.Forge.setProgress(${progress});"
        webViewRef?.evaluateJavascript(js, null)
    }

    LaunchedEffect(isRevealing) {
        if (isRevealing) {
            webViewRef?.evaluateJavascript("window.Forge && window.Forge.reveal && window.Forge.reveal();", null)
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#06050E"))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false

                addJavascriptInterface(bridge, "ForgeBridge")

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        return true
                    }
                }

                loadUrl("https://appassets.androidplatform.net/assets/forge/forge.html")
                webViewRef = this
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.evaluateJavascript("window.Forge && window.Forge.dispose && window.Forge.dispose();", null)
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}
