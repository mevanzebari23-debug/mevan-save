package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberObsidianCard
import com.example.ui.theme.CyberObsidianSurface
import com.example.ui.theme.CyberObsidianVoid

/**
 * Cyber-Luxury Tactile Category Button.
 * Features subtle neon glow, scale-up hover & press animations,
 * dynamic count pill, and high-contrast typography.
 */
@Composable
fun CyberNeonCategoryButton(
  label: String,
  accentColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  count: Int? = null,
  testTagKey: String? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isPressed by interactionSource.collectIsPressedAsState()

  val isElevated = isHovered || isPressed || isSelected

  // Subtle spring scale-up animation for cyber-luxury tactile feedback
  val scale by animateFloatAsState(
    targetValue = if (isHovered || isPressed) 1.08f else if (isSelected) 1.04f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "neonButtonScale"
  )

  // Neon glow alpha animation
  val glowAlpha by animateFloatAsState(
    targetValue = if (isHovered || isPressed) 0.95f else if (isSelected) 0.85f else 0.35f,
    label = "neonGlowAlpha"
  )

  val bgBrush = if (isSelected || isHovered) {
    Brush.linearGradient(
      listOf(
        accentColor.copy(alpha = if (isSelected) 0.30f else 0.22f),
        CyberObsidianSurface.copy(alpha = 0.95f),
        accentColor.copy(alpha = 0.12f)
      )
    )
  } else {
    Brush.linearGradient(
      listOf(
        CyberObsidianCard.copy(alpha = 0.95f),
        CyberObsidianVoid.copy(alpha = 0.98f)
      )
    )
  }

  val borderBrush = Brush.horizontalGradient(
    listOf(
      accentColor.copy(alpha = glowAlpha),
      accentColor.copy(alpha = (glowAlpha * 0.6f).coerceAtLeast(0.2f)),
      accentColor.copy(alpha = glowAlpha)
    )
  )

  Surface(
    modifier = modifier
      .scale(scale)
      .height(44.dp)
      .hoverable(interactionSource = interactionSource)
      .clip(RoundedCornerShape(14.dp))
      .clickable(interactionSource = interactionSource, indication = null) { onClick() }
      .then(if (testTagKey != null) Modifier.testTag(testTagKey) else Modifier),
    shape = RoundedCornerShape(14.dp),
    color = Color.Transparent,
    border = BorderStroke(if (isElevated) 1.4.dp else 0.8.dp, borderBrush),
    shadowElevation = if (isElevated) 8.dp else 0.dp
  ) {
    Box(
      modifier = Modifier
        .background(bgBrush)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isElevated) accentColor else Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
          text = label,
          fontSize = if (isSelected) 12.5.sp else 12.sp,
          fontWeight = if (isElevated) FontWeight.ExtraBold else FontWeight.SemiBold,
          color = if (isSelected) Color.White else if (isHovered) accentColor else Color(0xFFE2E8F0)
        )

        if (count != null && count > 0) {
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(
                if (isSelected) accentColor else accentColor.copy(alpha = 0.20f)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$count",
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = if (isSelected) CyberObsidianVoid else accentColor
            )
          }
        }
      }
    }
  }
}
