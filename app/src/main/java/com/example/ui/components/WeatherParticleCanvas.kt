package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.example.data.weather.WeatherConditionType
import kotlin.math.sin
import kotlin.random.Random

private class InternalRainDrop(
  var x: Float,
  var y: Float,
  var length: Float,
  var speed: Float,
  var alpha: Float,
  var strokeWidth: Float,
  var windTilt: Float
)

private class InternalSnowflake(
  var x: Float,
  var y: Float,
  var radius: Float,
  var speed: Float,
  var alpha: Float,
  var swayPhase: Float,
  var swaySpeed: Float,
  var swayAmplitude: Float
)

private class InternalSunParticle(
  var x: Float,
  var y: Float,
  var radius: Float,
  var speed: Float,
  var alpha: Float,
  var phase: Float
)

@Composable
fun WeatherParticleCanvas(
  condition: WeatherConditionType,
  modifier: Modifier = Modifier,
  densityMultiplier: Float = 1.0f
) {
  var frameCount by remember { mutableFloatStateOf(0f) }

  // Rain Drops pool
  val rainDrops = remember {
    List((50 * densityMultiplier).toInt().coerceAtLeast(35)) {
      InternalRainDrop(
        x = Random.nextFloat(),
        y = Random.nextFloat(),
        length = Random.nextFloat() * 18f + 14f,
        speed = Random.nextFloat() * 0.022f + 0.018f,
        alpha = Random.nextFloat() * 0.45f + 0.35f,
        strokeWidth = Random.nextFloat() * 1.5f + 1.2f,
        windTilt = Random.nextFloat() * 0.003f + 0.002f
      )
    }
  }

  // Snowflakes pool
  val snowflakes = remember {
    List((40 * densityMultiplier).toInt().coerceAtLeast(28)) {
      InternalSnowflake(
        x = Random.nextFloat(),
        y = Random.nextFloat(),
        radius = Random.nextFloat() * 2.5f + 1.5f,
        speed = Random.nextFloat() * 0.004f + 0.0025f,
        alpha = Random.nextFloat() * 0.50f + 0.45f,
        swayPhase = Random.nextFloat() * 6.28f,
        swaySpeed = Random.nextFloat() * 0.04f + 0.02f,
        swayAmplitude = Random.nextFloat() * 0.004f + 0.002f
      )
    }
  }

  // Sun / Clear Shimmer dust pool
  val sunParticles = remember {
    List(20) {
      InternalSunParticle(
        x = Random.nextFloat(),
        y = Random.nextFloat(),
        radius = Random.nextFloat() * 2.5f + 1.2f,
        speed = Random.nextFloat() * 0.0015f + 0.0008f,
        alpha = Random.nextFloat() * 0.35f + 0.20f,
        phase = Random.nextFloat() * 6.28f
      )
    }
  }

  // Continuous smooth frame ticker
  LaunchedEffect(condition) {
    while (true) {
      withFrameNanos { _ ->
        frameCount += 1f
      }
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@Canvas

    when (condition) {
      WeatherConditionType.RAIN, WeatherConditionType.THUNDERSTORM -> {
        // Draw Falling Rain Streaks
        val isThunder = condition == WeatherConditionType.THUNDERSTORM
        val flashAlpha = if (isThunder && (frameCount % 180f) in 0f..4f) 0.15f else 0f
        if (flashAlpha > 0f) {
          drawRect(color = Color.White.copy(alpha = flashAlpha))
        }

        for (drop in rainDrops) {
          drop.y += drop.speed
          drop.x += drop.windTilt
          if (drop.y > 1.0f) {
            drop.y = -0.05f
            drop.x = Random.nextFloat()
          }
          if (drop.x > 1.0f) drop.x = 0f

          val startX = drop.x * w
          val startY = drop.y * h
          val endX = startX + (drop.windTilt * w * 4f)
          val endY = startY + drop.length

          drawLine(
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0x80B3E5FC),
                Color(0xFFE1F5FE).copy(alpha = drop.alpha)
              ),
              startY = startY,
              endY = endY
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = drop.strokeWidth,
            cap = StrokeCap.Round
          )

          // Splash hint near ground
          if (drop.y in 0.90f..0.98f && drop.strokeWidth > 1.8f) {
            drawCircle(
              color = Color(0x6080D8FF),
              radius = 2.5f,
              center = Offset(endX, endY)
            )
          }
        }
      }

      WeatherConditionType.SNOW -> {
        // Draw Falling Snowflakes with graceful wave sway
        for (flake in snowflakes) {
          flake.y += flake.speed
          flake.swayPhase += flake.swaySpeed
          val waveOffset = sin(flake.swayPhase) * flake.swayAmplitude
          flake.x += waveOffset

          if (flake.y > 1.05f) {
            flake.y = -0.05f
            flake.x = Random.nextFloat()
          }
          if (flake.x > 1.0f) flake.x = 0f
          if (flake.x < 0f) flake.x = 1.0f

          val posX = flake.x * w
          val posY = flake.y * h

          // Soft outer crystal halo
          drawCircle(
            color = Color(0x35E0F7FA),
            radius = flake.radius * 1.8f,
            center = Offset(posX, posY)
          )
          // Crisp core snowflake
          drawCircle(
            color = Color.White.copy(alpha = flake.alpha),
            radius = flake.radius,
            center = Offset(posX, posY)
          )
        }
      }

      WeatherConditionType.CLEAR -> {
        // Floating ambient golden shimmer
        for (p in sunParticles) {
          p.y -= p.speed
          p.phase += 0.03f
          val sway = sin(p.phase) * 0.001f
          p.x += sway
          if (p.y < -0.05f) {
            p.y = 1.05f
            p.x = Random.nextFloat()
          }

          val posX = p.x * w
          val posY = p.y * h
          val pulsatingAlpha = (p.alpha * (0.7f + 0.3f * sin(p.phase))).coerceIn(0.1f, 0.6f)

          drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = pulsatingAlpha),
            radius = p.radius,
            center = Offset(posX, posY)
          )
        }
      }

      WeatherConditionType.CLOUDY, WeatherConditionType.PARTLY_CLOUDY -> {
        // Soft ambient drifting cloud mists
        for (p in sunParticles.take(12)) {
          p.x += p.speed * 0.8f
          if (p.x > 1.05f) p.x = -0.05f
          val posX = p.x * w
          val posY = (0.2f + p.y * 0.6f) * h
          drawCircle(
            color = Color(0x18FFFFFF),
            radius = p.radius * 7f,
            center = Offset(posX, posY)
          )
        }
      }
    }
  }
}
