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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberObsidianCard
import com.example.ui.theme.CyberObsidianSurface
import com.example.ui.theme.CyberObsidianVoid

/**
 * Full-width Cyber-Luxury tactile record button used across all lists, logs, and reports.
 * Tapping opens the contextual Detail & Action Dialog (Edit / Pay / Settle / Sell / Delete).
 */
@Composable
fun CyberRecordButton(
  title: String,
  primaryValue: String,
  accentColor: Color,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  secondaryValue: String? = null,
  statusBadge: String? = null,
  statusBadgeColor: Color = accentColor,
  dateOrMeta: String? = null,
  progressRatio: Float? = null,
  actionHintLabel: String = "کردار / دەستکاری",
  quickActionLabel: String? = null,
  quickActionIcon: ImageVector? = null,
  quickActionColor: Color = accentColor,
  onQuickActionClick: (() -> Unit)? = null,
  isHighlighted: Boolean = false,
  testTagKey: String? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isPressed by interactionSource.collectIsPressedAsState()
  val isActive = isHovered || isPressed || isHighlighted

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.985f else if (isHovered) 1.015f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "cyberRecordButtonScale"
  )

  val glowAlpha by animateFloatAsState(
    targetValue = if (isActive) 0.92f else 0.45f,
    label = "cyberRecordButtonGlow"
  )

  val bgBrush = Brush.linearGradient(
    colors = listOf(
      if (isActive) accentColor.copy(alpha = 0.22f) else accentColor.copy(alpha = 0.08f),
      CyberObsidianCard.copy(alpha = 0.96f),
      CyberObsidianVoid.copy(alpha = 0.99f)
    )
  )

  val borderBrush = Brush.horizontalGradient(
    colors = listOf(
      accentColor.copy(alpha = glowAlpha),
      accentColor.copy(alpha = (glowAlpha * 0.45f).coerceAtLeast(0.18f)),
      accentColor.copy(alpha = glowAlpha)
    )
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .scale(scale)
      .heightIn(min = 58.dp)
      .hoverable(interactionSource = interactionSource)
      .clip(RoundedCornerShape(16.dp))
      .clickable(interactionSource = interactionSource, indication = null) { onClick() }
      .then(if (testTagKey != null) Modifier.testTag(testTagKey) else Modifier),
    shape = RoundedCornerShape(16.dp),
    color = Color.Transparent,
    border = BorderStroke(if (isActive) 1.5.dp else 1.0.dp, borderBrush),
    shadowElevation = if (isActive) 8.dp else 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(bgBrush)
        .padding(horizontal = 13.dp, vertical = 11.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left: Neon Icon Badge + Title & Subtitle
        Row(
          modifier = Modifier
            .weight(1f)
            .padding(end = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.65f)),
            modifier = Modifier.size(40.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = title,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
              Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
            if (!dateOrMeta.isNullOrBlank()) {
              Text(
                text = dateOrMeta,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // Right: Primary & Secondary Value
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            text = primaryValue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            maxLines = 1
          )
          if (!secondaryValue.isNullOrBlank()) {
            Text(
              text = secondaryValue,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF94A3B8),
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.End,
              maxLines = 1
            )
          }
        }
      }

      if (progressRatio != null) {
        LinearProgressIndicator(
          progress = { progressRatio.coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = accentColor,
          trackColor = Color(0xFF1E293B)
        )
      }

      // Bottom Tactile Action Strip: Status Pill + Quick Action + Detail Trigger Hint
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!statusBadge.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusBadgeColor.copy(alpha = 0.16f),
            border = BorderStroke(0.8.dp, statusBadgeColor.copy(alpha = 0.55f))
          ) {
            Text(
              text = statusBadge,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = statusBadgeColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (onQuickActionClick != null && !quickActionLabel.isNullOrBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = quickActionColor.copy(alpha = 0.20f),
              border = BorderStroke(0.8.dp, quickActionColor.copy(alpha = 0.7f)),
              modifier = Modifier
                .heightIn(min = 32.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onQuickActionClick() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                if (quickActionIcon != null) {
                  Icon(
                    imageVector = quickActionIcon,
                    contentDescription = quickActionLabel,
                    tint = quickActionColor,
                    modifier = Modifier.size(13.dp)
                  )
                }
                Text(
                  text = quickActionLabel,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = quickActionColor
                )
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.14f),
            border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.45f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = actionHintLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2E8F0)
              )
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Tactile Matrix Tile Button for Overview Balance Sheet (replaces ExcelBalanceSheetTableView).
 */
@Composable
fun CyberBalanceSummaryButton(
  title: String,
  primaryAmount: String,
  accentColor: Color,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  secondaryAmount: String? = null,
  countLabel: String? = null,
  testTagKey: String? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isPressed by interactionSource.collectIsPressedAsState()
  val isActive = isHovered || isPressed

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.97f else if (isHovered) 1.02f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "balanceSummaryBtnScale"
  )

  Surface(
    modifier = modifier
      .scale(scale)
      .heightIn(min = 78.dp)
      .hoverable(interactionSource = interactionSource)
      .clip(RoundedCornerShape(16.dp))
      .clickable(interactionSource = interactionSource, indication = null) { onClick() }
      .then(if (testTagKey != null) Modifier.testTag(testTagKey) else Modifier),
    shape = RoundedCornerShape(16.dp),
    color = Color.Transparent,
    border = BorderStroke(
      width = if (isActive) 1.5.dp else 1.0.dp,
      brush = Brush.linearGradient(
        listOf(
          accentColor.copy(alpha = if (isActive) 0.95f else 0.55f),
          accentColor.copy(alpha = 0.22f)
        )
      )
    ),
    shadowElevation = if (isActive) 6.dp else 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            listOf(
              accentColor.copy(alpha = if (isActive) 0.22f else 0.11f),
              CyberObsidianSurface.copy(alpha = 0.96f),
              CyberObsidianVoid
            )
          )
        )
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.18f),
            border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.6f)),
            modifier = Modifier.size(28.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
              )
            }
          }
          Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE2E8F0),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        if (!countLabel.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.18f)
          ) {
            Text(
              text = countLabel,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = accentColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Text(
        text = primaryAmount,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.Black,
        color = accentColor,
        fontFamily = FontFamily.Monospace,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      if (!secondaryAmount.isNullOrBlank()) {
        Text(
          text = secondaryAmount,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF94A3B8),
          fontFamily = FontFamily.Monospace,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
