package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable luxury ultra-clean frosted glass and crystal glow design components.
 * Completely replaces heavy opaque wood textures with sheer crystal clarity and luminous inlays.
 */

/**
 * Modifier to draw subtle frosted glass specular highlight and translucent crystal shimmer.
 */
fun Modifier.woodGrainSurface(
  baseColor: Color = Color(0x200F172A),
  grainColor: Color = Color(0x38FFFFFF),
  highlightColor: Color = Color(0x5538BDF8)
): Modifier = this.drawBehind {
  drawRect(color = baseColor)

  // Top specular crystal hairline
  drawLine(
    color = highlightColor,
    start = Offset(0f, 0f),
    end = Offset(size.width, 0f),
    strokeWidth = 1.2.dp.toPx()
  )
}

/**
 * Ultra-Clean Frosted Glass Card Container with crystal glow, sheer translucency, and luminous border.
 * Financial data and controls float directly over the background scenery.
 */
@Composable
fun WoodCardBox(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  borderWidth: Dp = 1.dp,
  borderColor: Color = Color(0x38FFFFFF),
  darkWalnutTone: Boolean = true,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0x2E101B2E),
            Color(0x220A101C),
            Color(0x34080D18)
          )
        )
      )
      .border(borderWidth, borderColor, shape)
  ) {
    // Sheer frosted crystal ambient glow
    Box(
      modifier = Modifier
        .matchParentSize()
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color(0x1838BDF8),
              Color(0x08FFD700),
              Color.Transparent
            ),
            center = Offset(300f, 0f),
            radius = 800f
          )
        )
    )

    content()
  }
}

/**
 * Crystal & 24K Gold Inlay Gradient for luminous trims, dividers, and card borders.
 */
fun woodInlayBrush(alpha: Float = 0.85f): Brush = Brush.horizontalGradient(
  colors = listOf(
    Color(0x3338BDF8).copy(alpha = alpha * 0.4f),
    Color(0x99FFD700).copy(alpha = alpha),
    Color(0xEEFFFFFF).copy(alpha = alpha),
    Color(0x99FFD700).copy(alpha = alpha),
    Color(0x3338BDF8).copy(alpha = alpha * 0.4f)
  )
)
