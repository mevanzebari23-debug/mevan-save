package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * AnimatedNashraStrip (نەشرەیا زێرینی یا گەڕۆک)
 *
 * A luxurious, festive flowing LED light garland with warm golden and amber glowing beads
 * and a traveling luminous shimmer wave that moves back and forth smoothly across the screen.
 *
 * @param modifier Custom modifier for sizing and padding
 * @param height Height of the light strip
 * @param isTop True if placed on the top bar, false if on the bottom navigation dock
 */
@Composable
fun AnimatedNashraStrip(
  modifier: Modifier = Modifier,
  height: Dp = 7.dp,
  isTop: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "NashraStripTransition")

  // Traveling beam sweep that moves back and forth across the screen ("بێت و بچێت")
  val sweepFraction by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sweepFraction"
  )

  // Gentle rhythmic breathing / pulsing of the LED bulbs
  val pulseFraction by infiniteTransition.animateFloat(
    initialValue = 0.65f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseFraction"
  )

  // Continuous traveling phase wave for the individual bulb micro-sparkles
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhase"
  )

  val density = LocalDensity.current
  val bulbSpacingPx = with(density) { 18.dp.toPx() }
  val baseRadiusPx = with(density) { 1.8.dp.toPx() }

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
  ) {
    val width = size.width
    val canvasHeight = size.height
    if (width <= 0f || canvasHeight <= 0f) return@Canvas

    val centerY = canvasHeight / 2f
    val sweepCenterX = width * sweepFraction
    val beamWidth = (width * 0.38f).coerceAtLeast(160f)
    val beamStart = sweepCenterX - beamWidth / 2f
    val beamEnd = sweepCenterX + beamWidth / 2f

    // 1. Festive Wire / Underlying Golden Ribbon Track
    drawLine(
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color(0x358C6418),
          Color(0x75FFD700),
          Color(0x95FFDF73),
          Color(0x75FFD700),
          Color(0x358C6418)
        )
      ),
      start = Offset(0f, centerY),
      end = Offset(width, centerY),
      strokeWidth = 1.4f
    )

    // 2. Traveling Luminous Shimmer Wave ("وەکی نەشرێ بیت و بچیت")
    drawRect(
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color.Transparent,
          Color(0x18FFB800),
          Color(0x55FFDF73),
          Color(0xCCFFF7DB), // Brilliant champagne spark highlight at center
          Color(0x55FFDF73),
          Color(0x18FFB800),
          Color.Transparent
        ),
        startX = beamStart,
        endX = beamEnd
      ),
      topLeft = Offset(0f, 0f),
      size = Size(width, canvasHeight)
    )

    // 3. Festive Glowing LED Bulb Beads along the Garland ("گەوهەر و گلۆپێن نەشرێ")
    val bulbCount = (width / bulbSpacingPx).toInt() + 1
    for (i in 0..bulbCount) {
      val bulbX = i * bulbSpacingPx
      if (bulbX > width + bulbSpacingPx) break

      // Proximity boost when the sweeping beam moves over this bulb
      val distFromSweep = abs(bulbX - sweepCenterX)
      val sweepProximity = (1f - (distFromSweep / (beamWidth / 2f))).coerceIn(0f, 1f)

      // Individual twinkling wave effect
      val rad = (wavePhase + i * 26f) * (Math.PI / 180.0)
      val waveBoost = ((sin(rad).toFloat() + 1f) / 2f) * 0.35f + 0.65f

      val currentIntensity = (0.4f + 0.6f * sweepProximity) * pulseFraction * waveBoost

      // Festive luxury color sequence: 24K Gold, Vibrant Amber, Champagne Diamond, Warm Sunset Gold
      val bulbColor = when (i % 4) {
        0 -> Color(0xFFFFD700) // Pure 24K Gold
        1 -> Color(0xFFFFB300) // Radiant Warm Amber
        2 -> Color(0xFFFFF9E6) // Sparkling Champagne Diamond
        else -> Color(0xFFFFA000) // Deep Sunset Amber Gold
      }

      // Outer luminous halo
      drawCircle(
        color = bulbColor.copy(alpha = (0.28f * currentIntensity).coerceIn(0f, 1f)),
        radius = baseRadiusPx * 3.2f * (0.8f + 0.35f * currentIntensity),
        center = Offset(bulbX, centerY)
      )

      // Mid-ring glow
      drawCircle(
        color = bulbColor.copy(alpha = (0.75f * currentIntensity).coerceIn(0f, 1f)),
        radius = baseRadiusPx * 1.9f * (0.85f + 0.25f * currentIntensity),
        center = Offset(bulbX, centerY)
      )

      // Bright inner sparkling core
      drawCircle(
        color = Color(0xFFFFFFFF).copy(alpha = (0.92f * currentIntensity).coerceIn(0f, 1f)),
        radius = baseRadiusPx * 0.95f,
        center = Offset(bulbX, centerY)
      )
    }

    // 4. Subtle ambient glow edge bleeding into the navigation bar or content
    val edgeGradientBrush = if (isTop) {
      Brush.verticalGradient(
        colors = listOf(
          Color.Transparent,
          Color(0x22FFD700)
        )
      )
    } else {
      Brush.verticalGradient(
        colors = listOf(
          Color(0x22FFD700),
          Color.Transparent
        )
      )
    }
    drawRect(
      brush = edgeGradientBrush,
      topLeft = Offset(0f, if (isTop) canvasHeight - 2f else 0f),
      size = Size(width, 2f)
    )
  }
}
