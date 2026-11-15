package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed

// Deep obsidian surfaces for executive terminal
val TerminalDarkBg = Color(0xFF070E1A)
val TerminalCardBg = Color(0xFF0B1426)
val TerminalBorder = Color(0xFF1E2D4A)
val TerminalAccentBlue = Color(0xFF38BDF8)
val TerminalRowAltBg = Color(0xFF0E1A30)
val TerminalHeaderBg = Color(0xFF11203C)

/**
 * Executive Financial Terminal Card Container with glowing gradient header,
 * live status indicator chip, dynamic search input, and action triggers.
 */
@Composable
fun TerminalContainer(
  title: String,
  subtitle: String? = null,
  isLive: Boolean = true,
  liveLabel: String = "TERMINAL ACTIVE",
  searchQuery: String = "",
  onSearchChange: (String) -> Unit = {},
  searchPlaceholder: String = "Search ledger...",
  isExpanded: Boolean = true,
  onToggleExpand: (() -> Unit)? = null,
  onOpenFullscreen: (() -> Unit)? = null,
  onExportPdf: (() -> Unit)? = null,
  pdfButtonTag: String = "terminal_export_pdf_btn",
  testTag: String = "financial_terminal_container",
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = TerminalCardBg),
    border = BorderStroke(
      1.2.dp,
      Brush.linearGradient(
        listOf(
          GoldAccent.copy(alpha = 0.5f),
          TerminalAccentBlue.copy(alpha = 0.35f),
          EmeraldLight.copy(alpha = 0.3f),
          TerminalBorder
        )
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(TerminalDarkBg)
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Terminal Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f).padding(end = 6.dp)
        ) {
          // Terminal Logo / Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = TerminalAccentBlue.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, TerminalAccentBlue.copy(alpha = 0.5f)),
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "⚡",
                fontSize = 16.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.5.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (isLive) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = EmeraldLight.copy(alpha = 0.15f),
                  border = BorderStroke(0.7.dp, EmeraldLight.copy(alpha = 0.6f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(EmeraldLight)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = liveLabel,
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Black,
                      color = EmeraldLight,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }
            }

            if (!subtitle.isNullOrBlank()) {
              Text(
                text = subtitle,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = GoldAccent.copy(alpha = 0.9f)
              )
            }
          }
        }

        // Action Buttons: PDF Export + Fullscreen Toggle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (onExportPdf != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldLight.copy(alpha = 0.18f),
              border = BorderStroke(0.9.dp, EmeraldLight),
              modifier = Modifier
                .clickable { onExportPdf() }
                .testTag(pdfButtonTag)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PictureAsPdf,
                  contentDescription = "Export PDF",
                  tint = EmeraldLight,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "PDF",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldLight
                )
              }
            }
          }

          if (onOpenFullscreen != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = TerminalAccentBlue.copy(alpha = 0.16f),
              border = BorderStroke(0.9.dp, TerminalAccentBlue.copy(alpha = 0.7f)),
              modifier = Modifier
                .clickable { onOpenFullscreen() }
                .testTag("terminal_fullscreen_btn")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Fullscreen,
                  contentDescription = "Fullscreen",
                  tint = TerminalAccentBlue,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "EXPAND",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  color = TerminalAccentBlue,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }

      // 2. Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 42.dp)
          .testTag("terminal_search_input"),
        placeholder = {
          Text(
            text = searchPlaceholder,
            fontSize = 11.5.sp,
            color = Color(0xFF64748B)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TerminalAccentBlue,
            modifier = Modifier.size(16.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchChange("") },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF091426),
          unfocusedContainerColor = Color(0xFF091426),
          focusedBorderColor = TerminalAccentBlue,
          unfocusedBorderColor = TerminalBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        )
      )

      // 3. Body Content
      content()
    }
  }
}

/**
 * Filter Pill item model
 */
data class TerminalFilterOption(
  val id: String,
  val label: String,
  val count: Int? = null,
  val highlightColor: Color? = null
)

/**
 * Interactive Horizontal Quick-Filter Pill Row
 */
@Composable
fun TerminalFilterPills(
  options: List<TerminalFilterOption>,
  selectedId: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    options.forEach { opt ->
      val isSelected = opt.id == selectedId
      val activeColor = opt.highlightColor ?: GoldAccent
      val bg = if (isSelected) activeColor.copy(alpha = 0.22f) else Color(0xFF0E1A30)
      val border = if (isSelected) activeColor else TerminalBorder.copy(alpha = 0.6f)

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(if (isSelected) 1.2.dp else 0.8.dp, border),
        modifier = Modifier
          .clickable { onSelect(opt.id) }
          .testTag("filter_pill_${opt.id}")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = opt.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
          )
          if (opt.count != null) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isSelected) activeColor.copy(alpha = 0.4f) else Color(0xFF1E293B)
            ) {
              Text(
                text = "${opt.count}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * KPI Metric Item Model
 */
data class TerminalKpiItem(
  val label: String,
  val value: String,
  val isPositive: Boolean? = null,
  val customColor: Color? = null
)

/**
 * Executive KPI Strip displaying key summaries
 */
@Composable
fun TerminalKpiStrip(
  items: List<TerminalKpiItem>,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    items.forEach { item ->
      val valueColor = item.customColor ?: when (item.isPositive) {
        true -> EmeraldLight
        false -> LossRed
        null -> GoldAccent
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF091426),
        border = BorderStroke(0.8.dp, TerminalBorder),
        modifier = Modifier.padding(vertical = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = item.label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF94A3B8)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = item.value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

/**
 * Modern Table Column Header Cell
 */
@Composable
fun TerminalHeaderCell(
  text: String,
  width: Dp,
  align: TextAlign = TextAlign.Start,
  color: Color = Color(0xFF38BDF8)
) {
  Text(
    text = text,
    modifier = Modifier
      .width(width)
      .padding(horizontal = 6.dp),
    fontSize = 11.sp,
    fontWeight = FontWeight.Black,
    color = color,
    fontFamily = FontFamily.Monospace,
    textAlign = align,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
  )
}

/**
 * Empty State for Terminal
 */
@Composable
fun TerminalEmptyState(
  message: String = "No matching records found",
  icon: ImageVector = Icons.Default.Inbox
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color(0xFF475569),
        modifier = Modifier.size(32.dp)
      )
      Text(
        text = message,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF64748B)
      )
    }
  }
}
