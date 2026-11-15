package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.categories.AppCategoryRegistry

@Composable
fun CategoryFilterBar(
  categories: List<String>,
  selectedCategory: String?,
  onSelectCategory: (String?) -> Unit,
  isKurdish: Boolean = true,
  categoryCountMap: Map<String, Int> = emptyMap(),
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // 1. "All" (هەمی) Chip
    val isAllSelected = selectedCategory == null
    Box(
      modifier = Modifier
        .heightIn(min = 34.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(
          if (isAllSelected) Color(0xFF38BDF8).copy(alpha = 0.25f)
          else Color(0xFF1E293B).copy(alpha = 0.6f)
        )
        .border(
          width = if (isAllSelected) 1.2.dp else 0.8.dp,
          color = if (isAllSelected) Color(0xFF38BDF8) else Color(0xFF38BDF8).copy(alpha = 0.3f),
          shape = RoundedCornerShape(10.dp)
        )
        .clickable { onSelectCategory(null) }
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("category_filter_all"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = if (isKurdish) "هەمی" else "All",
        fontSize = 11.5.sp,
        fontWeight = if (isAllSelected) FontWeight.ExtraBold else FontWeight.Medium,
        color = if (isAllSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
      )
    }

    // 2. Individual Category Chips
    categories.forEach { cat ->
      val isSelected = selectedCategory.equals(cat, ignoreCase = true)
      val catColor = AppCategoryRegistry.findCategoryColor(cat)
      val count = categoryCountMap[cat]

      Box(
        modifier = Modifier
          .heightIn(min = 34.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            if (isSelected) catColor.copy(alpha = 0.25f)
            else Color(0xFF1E293B).copy(alpha = 0.6f)
          )
          .border(
            width = if (isSelected) 1.3.dp else 0.8.dp,
            color = if (isSelected) catColor else catColor.copy(alpha = 0.35f),
            shape = RoundedCornerShape(10.dp)
          )
          .clickable {
            if (isSelected) onSelectCategory(null) else onSelectCategory(cat)
          }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("category_filter_${cat.replace(" ", "_")}"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = cat,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
          )
          if (count != null && count > 0) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(catColor.copy(alpha = 0.25f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$count",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = catColor
              )
            }
          }
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear",
              tint = catColor,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }
    }
  }
}
