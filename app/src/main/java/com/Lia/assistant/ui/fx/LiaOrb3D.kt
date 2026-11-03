package com.Lia.assistant.ui.fx

import android.content.Context
import android.graphics.RuntimeShader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.voice.NovaOrbState

private const val ORB_SHADER_SRC = """
uniform float2 uResolution;
uniform float uTime;
uniform float uAmplitude;
uniform float2 uTilt;
uniform float3 uColorCore;
uniform float3 uColorRim;
uniform float3 uColorHalo;

half4 main(float2 fragCoord) {
    float2 uv = (fragCoord - 0.5 * uResolution) / min(uResolution.x, uResolution.y);
    float dist = length(uv);
    float radius = 0.38 + uAmplitude * 0.08;

    if (dist < radius) {
        // Compute 3D sphere normal
        float z = sqrt(max(0.0, radius * radius - dist * dist));
        float3 normal = normalize(float3(uv.x, uv.y, z));

        // Light direction modulated by device tilt
        float3 lightDir = normalize(float3(-uTilt.x * 0.6 + 0.3, -uTilt.y * 0.6 - 0.5, 0.8));
        float diff = max(dot(normal, lightDir), 0.0);

        // Specular highlight
        float3 viewDir = float3(0.0, 0.0, 1.0);
        float3 halfDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(normal, halfDir), 0.0), 32.0);

        // Fresnel edge rim
        float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 2.5);

        // Energy swirl noise
        float swirl = sin(dist * 20.0 - uTime * 3.0) * 0.5 + 0.5;

        float3 col = mix(uColorCore, uColorRim, fresnel * 0.85);
        col += uColorRim * swirl * 0.25;
        col += float3(1.0, 1.0, 1.0) * spec * 0.6;
        col *= (diff * 0.7 + 0.3);

        return half4(col, 1.0);
    } else {
        // Soft outer glowing halo
        float haloDist = (dist - radius) / 0.25;
        float haloAlpha = exp(-haloDist * 4.0) * (0.6 + uAmplitude * 0.4);
        return half4(uColorHalo * haloAlpha, haloAlpha);
    }
}
"""

@Composable
fun LiaOrb3D(
    state: NovaOrbState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    style: NovaOrbStyle = NovaOrbStyle.NOVA,
    assistantName: String = "Lia"
) {
    val context = LocalContext.current
    val colors = LocalNovaColors.current
    val reducedMotion = LocalNovaReducedMotion.current

    // Fallback to NovaOrb on API < 33
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        NovaOrb(
            state = state,
            amplitude = amplitude,
            modifier = modifier,
            size = size,
            style = style,
            assistantName = assistantName
        )
        return
    }

    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(reducedMotion) {
        if (reducedMotion) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.size >= 2) {
                    val rawX = event.values[0] / 9.81f
                    val rawY = event.values[1] / 9.81f
                    // Low pass filter
                    tiltX = tiltX * 0.85f + rawX * 0.15f
                    tiltY = tiltY * 0.85f + rawY * 0.15f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shaderTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    NovaOrbState.THINKING -> 1800
                    NovaOrbState.CONNECTING -> 1200
                    else -> 4000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    val (coreCol, rimCol, haloCol) = when (state) {
        NovaOrbState.IDLE -> Triple(
            floatArrayOf(0.12f, 0.10f, 0.30f),
            floatArrayOf(1.0f, 0.43f, 0.68f),
            floatArrayOf(0.55f, 0.36f, 0.96f)
        )
        NovaOrbState.LISTENING -> Triple(
            floatArrayOf(0.05f, 0.20f, 0.25f),
            floatArrayOf(0.25f, 0.88f, 0.82f),
            floatArrayOf(0.25f, 0.88f, 0.82f)
        )
        NovaOrbState.THINKING -> Triple(
            floatArrayOf(0.35f, 0.15f, 0.35f),
            floatArrayOf(1.0f, 0.71f, 0.28f),
            floatArrayOf(1.0f, 0.54f, 0.24f)
        )
        NovaOrbState.SPEAKING -> Triple(
            floatArrayOf(0.85f, 0.45f, 0.15f),
            floatArrayOf(1.0f, 0.71f, 0.28f),
            floatArrayOf(1.0f, 0.43f, 0.68f)
        )
        NovaOrbState.CONNECTING -> Triple(
            floatArrayOf(0.40f, 0.30f, 0.05f),
            floatArrayOf(0.98f, 0.75f, 0.14f),
            floatArrayOf(0.98f, 0.75f, 0.14f)
        )
        NovaOrbState.ERROR -> Triple(
            floatArrayOf(0.50f, 0.05f, 0.05f),
            floatArrayOf(1.0f, 0.48f, 0.52f),
            floatArrayOf(0.9f, 0.15f, 0.15f)
        )
    }

    val shader = remember {
        try {
            RuntimeShader(ORB_SHADER_SRC)
        } catch (_: Exception) {
            null
        }
    }

    if (shader == null) {
        NovaOrb(state, amplitude, modifier, size, style, assistantName)
        return
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "$assistantName status: ${state.name}" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            shader.setFloatUniform("uResolution", this.size.width, this.size.height)
            shader.setFloatUniform("uTime", if (reducedMotion) 0f else time)
            shader.setFloatUniform("uAmplitude", amplitude)
            shader.setFloatUniform("uTilt", if (reducedMotion) 0f else tiltX, if (reducedMotion) 0f else tiltY)
            shader.setFloatUniform("uColorCore", coreCol[0], coreCol[1], coreCol[2])
            shader.setFloatUniform("uColorRim", rimCol[0], rimCol[1], rimCol[2])
            shader.setFloatUniform("uColorHalo", haloCol[0], haloCol[1], haloCol[2])

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
