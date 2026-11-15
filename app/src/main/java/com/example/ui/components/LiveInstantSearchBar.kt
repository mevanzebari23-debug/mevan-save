package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodInlayGold

/**
 * Reusable, reactive live instant search bar with Badini Kurdish support,
 * luxury gold & obsidian crystal glass styling, and instant match counter.
 */
@Composable
fun LiveInstantSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  matchCount: Int? = null,
  isKurdish: Boolean = true,
  onClear: () -> Unit = { onQueryChange("") }
) {
  val isFocusedOrActive = query.isNotEmpty()
  val borderBrush = if (isFocusedOrActive) {
    Brush.horizontalGradient(
      listOf(GoldAccent, EmeraldLight, GoldAccent)
    )
  } else {
    Brush.horizontalGradient(
      listOf(WoodInlayGold.copy(alpha = 0.5f), WoodBorder.copy(alpha = 0.6f))
    )
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(DarkGlassCardBg)
      .background(DarkGlassCardSurface)
      .border(
        width = if (isFocusedOrActive) 1.2.dp else 1.dp,
        brush = borderBrush,
        shape = RoundedCornerShape(16.dp)
      )
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(
              if (isFocusedOrActive) GoldAccent.copy(alpha = 0.22f)
              else Color.White.copy(alpha = 0.08f),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = if (isFocusedOrActive) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterStart
        ) {
          if (query.isEmpty()) {
            Text(
              text = placeholder,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
              fontSize = 13.sp,
              maxLines = 1
            )
          }

          BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = TextStyle(
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(GoldAccent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("live_instant_search_input")
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Match counter badge when user has typed anything
        if (isFocusedOrActive && matchCount != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(GoldAccent.copy(alpha = 0.18f))
              .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
              .padding(horizontal = 7.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (isKurdish) "$matchCount ئەنجام" else "$matchCount found",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
          }
        }

        AnimatedVisibility(
          visible = query.isNotEmpty(),
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.12f))
              .clickable { onClear() }
              .testTag("clear_live_search_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(15.dp)
            )
          }
        }
      }
    }
  }
}
