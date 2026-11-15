package com.example.ui.screens.investments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.LiveInstantSearchBar
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.gold.GoldScreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodCardSurface

/**
 * Consolidated Hub for Assets & Investments (ملوک و وەبەرهێنان)
 * Contains Real Estate & Private Account (0) and Gold & Silver Vault (1)
 * with instant live search at the top.
 */
@Composable
fun AssetsAndInvestmentsHubScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onOpenGoogleSearchGrounding: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  // 0 = Real Estate & Private Account, 1 = Cars Fleet, 2 = Gold & Silver
  var activeHubTab by remember { mutableIntStateOf(0) }
  var instantSearchQuery by remember { mutableStateOf("") }

  LaunchedEffect(uiState.focusedEntityType) {
    when (uiState.focusedEntityType) {
      "GOLD" -> activeHubTab = 1
      "INVESTMENT" -> activeHubTab = 0
    }
  }

  val hubCategories = listOf(
    Triple(
      0,
      if (isKurdish) "🏢 عەقارات و ملوک" else "🏢 Real Estate & Properties",
      uiState.nonCarInvestments.size
    ),
    Triple(
      1,
      if (isKurdish) "🥇 زێڕ و زیڤ" else "🥇 Gold & Silver",
      uiState.goldHoldings.size
    )
  )

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    // 1. Instant Live Search Bar at the very top of Assets & Investments Hub
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      LiveInstantSearchBar(
        query = instantSearchQuery,
        onQueryChange = { instantSearchQuery = it },
        placeholder = if (isKurdish) "گەڕیانا بلەز ل ملوک، عەقارات، زێڕ و زیڤ..." else "Live search properties, real estate, gold & silver...",
        matchCount = null,
        isKurdish = isKurdish
      )
    }

    // 2. Hub Sub-Tabs Strip (Equally accessible, high elegance)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      hubCategories.forEach { (tabIndex, title, count) ->
        val isSelected = activeHubTab == tabIndex
        val accent = when (tabIndex) {
          0 -> EmeraldLight
          else -> GoldAccent
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (isSelected) accent.copy(alpha = 0.22f)
              else Color.White.copy(alpha = 0.05f)
            )
            .border(
              width = if (isSelected) 1.2.dp else 0.8.dp,
              color = if (isSelected) accent else WoodBorder.copy(alpha = 0.5f),
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { activeHubTab = tabIndex }
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("assets_hub_tab_$tabIndex"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = title,
              fontSize = 11.5.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
              color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
            if (count > 0) {
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(accent.copy(alpha = if (isSelected) 0.35f else 0.15f))
                .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "$count",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 3. Active Screen Content
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      when (activeHubTab) {
        0 -> InvestmentsScreen(
          viewModel = viewModel,
          uiState = uiState,
          onNavigateToCars = {},
          modifier = Modifier.fillMaxSize()
        )
        else -> GoldScreen(
          viewModel = viewModel,
          uiState = uiState,
          onOpenGoogleSearchGrounding = onOpenGoogleSearchGrounding,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
