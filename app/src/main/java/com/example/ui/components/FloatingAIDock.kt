package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberHoloSweep
import com.example.ui.theme.CyberObsidianCard
import com.example.ui.theme.CyberObsidianSurface
import com.example.ui.theme.HoloNeonCyan
import com.example.ui.theme.HoloNeonEmerald
import com.example.ui.theme.HoloNeonGold
import com.example.ui.theme.HoloNeonMagenta
import com.example.ui.theme.HoloNeonViolet

data class DockNavItem(
  val label: String,
  val icon: ImageVector,
  val accentColor: Color,
  val badgeCount: Int? = null
)

/**
 * Ultra-Luxury Floating AI Action Dock pinned to the bottom of the viewport.
 * Features an animated pulsing holographic AI neural core with perfectly balanced
 * primary navigation tabs, without speech or web search clutter.
 */
@Composable
fun FloatingAIDock(
  selectedTab: Int,
  navItems: List<DockNavItem>,
  isKurdish: Boolean,
  onSelectTab: (Int) -> Unit,
  onOpenCopilot: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Infinite Pulse Animation for Holographic AI Core
  val infiniteTransition = rememberInfiniteTransition(label = "FloatingAIDockPulse")

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "aiCoreScale"
  )

  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "aiCoreAlpha"
  )

  val haloRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "haloRotation"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag("floating_ai_dock"),
    contentAlignment = Alignment.BottomCenter
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 700.dp),
      shape = RoundedCornerShape(28.dp),
      color = CyberObsidianCard.copy(alpha = 0.94f),
      border = BorderStroke(
        width = 1.2.dp,
        brush = Brush.horizontalGradient(
          listOf(
            HoloNeonCyan.copy(alpha = 0.70f),
            HoloNeonEmerald.copy(alpha = 0.60f),
            HoloNeonViolet.copy(alpha = 0.60f),
            HoloNeonGold.copy(alpha = 0.70f)
          )
        )
      ),
      shadowElevation = 18.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left Navigation Group (First 3 items: e.g. Command, Vehicles, Debts)
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          navItems.take(3).forEachIndexed { i, item ->
            val index = i
            DockTabItem(
              item = item,
              isSelected = selectedTab == index,
              onClick = { onSelectTab(index) }
            )
          }
        }

        // Center: Elevated Pulsing Holographic AI Core Button (Gemini 2.5)
        Box(
          modifier = Modifier
            .size(56.dp)
            .padding(horizontal = 2.dp)
            .clickable { onOpenCopilot() }
            .testTag("gemini_pulse_core_button"),
          contentAlignment = Alignment.Center
        ) {
          // Pulsing Outer Glowing Ring
          Box(
            modifier = Modifier
              .size(54.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .border(
                width = 1.6.dp,
                brush = Brush.sweepGradient(
                  listOf(
                    HoloNeonCyan.copy(alpha = pulseAlpha),
                    HoloNeonViolet.copy(alpha = pulseAlpha),
                    HoloNeonMagenta.copy(alpha = pulseAlpha),
                    HoloNeonGold.copy(alpha = pulseAlpha),
                    HoloNeonCyan.copy(alpha = pulseAlpha)
                  )
                ),
                shape = CircleShape
              )
          )

          // Inner Rotating Holographic Halo
          Box(
            modifier = Modifier
              .size(46.dp)
              .rotate(haloRotation)
              .clip(CircleShape)
              .border(
                width = 1.dp,
                brush = CyberHoloSweep,
                shape = CircleShape
              )
          )

          // Core Holographic Neural Avatar
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(CyberObsidianSurface),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_gemini_ai_core),
              contentDescription = "Gemini AI Core",
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          }
        }

        // Right Navigation Group (Remaining 3 items: e.g. Assets, School, Reports)
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          navItems.drop(3).forEachIndexed { i, item ->
            val index = i + 3
            DockTabItem(
              item = item,
              isSelected = selectedTab == index,
              onClick = { onSelectTab(index) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DockTabItem(
  item: DockNavItem,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val bgBrush = if (isSelected) {
    Brush.verticalGradient(
      listOf(
        item.accentColor.copy(alpha = 0.25f),
        item.accentColor.copy(alpha = 0.08f)
      )
    )
  } else {
    Brush.verticalGradient(
      listOf(
        Color.White.copy(alpha = 0.03f),
        Color.Transparent
      )
    )
  }

  Box(
    modifier = Modifier
      .height(48.dp)
      .widthIn(min = 44.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(bgBrush)
      .border(
        width = if (isSelected) 1.2.dp else 0.5.dp,
        brush = if (isSelected) {
          Brush.verticalGradient(
            listOf(
              item.accentColor.copy(alpha = 0.90f),
              item.accentColor.copy(alpha = 0.35f)
            )
          )
        } else {
          Brush.verticalGradient(
            listOf(
              Color.White.copy(alpha = 0.08f),
              Color.Transparent
            )
          )
        },
        shape = RoundedCornerShape(14.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 4.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(contentAlignment = Alignment.TopEnd) {
        Icon(
          imageVector = item.icon,
          contentDescription = item.label,
          tint = if (isSelected) item.accentColor else Color(0xFF94A3B8),
          modifier = Modifier.size(if (isSelected) 20.dp else 18.dp)
        )
        if (item.badgeCount != null && item.badgeCount > 0) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(item.accentColor, CircleShape)
          )
        }
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = item.label,
        fontSize = if (isSelected) 10.sp else 9.sp,
        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
        color = if (isSelected) item.accentColor else Color(0xFF94A3B8),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
