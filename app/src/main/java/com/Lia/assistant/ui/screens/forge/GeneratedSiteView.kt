package com.Lia.assistant.ui.screens.forge

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GeneratedSiteView(
    html: String,
    modifier: Modifier = Modifier
) {
    var webViewRef: WebView? = remember { null }

    LaunchedEffect(html) {
        if (html.isNotBlank()) {
            webViewRef?.loadDataWithBaseURL(
                "https://localhost/",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            if (!url.startsWith("https://localhost/")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    ctx.startActivity(intent)
                                    return true
                                } catch (_: Exception) {}
                            }
                        }
                        return false
                    }
                }

                if (html.isNotBlank()) {
                    loadDataWithBaseURL(
                        "https://localhost/",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
                webViewRef = this
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}
