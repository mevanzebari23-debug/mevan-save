package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceUiState
import com.example.ui.theme.CyberObsidianCard
import com.example.ui.theme.HoloNeonCyan
import com.example.ui.theme.HoloNeonEmerald
import com.example.ui.theme.HoloNeonGold

/**
 * Cyber-Luxury Dynamic Quick Category Explorer Bar.
 * Fulfills the user request: "برنامێ خڕی بکە دوکمە هەر تشت داخل بکەم پشتی هنگی وەکی دوکمەی ڕیز ببیتن"
 * (Make the whole app buttons: whatever I enter, after that it lines up as buttons!)
 *
 * Dynamically lines up all car brands (Toyota, Land Cruiser, BMW, Mercedes, Nissan, Lexus, etc.),
 * all properties (Houses, Lands, Apartments), debts (Receivables, Payables, Contacts),
 * and gold into cyber-neon glowing buttons with tactile hover/press scaling.
 */
@Composable
fun QuickCategoryExplorerBar(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  selectedCategory: String? = null,
  onSelectCategory: (String) -> Unit,
  modifier: Modifier = Modifier,
  title: String? = null
) {
  // 0 = All Buttons ("هەمی"), 1 = Cars ("سەیارە"), 2 = Real Estate ("عەقارات"), 3 = Debts ("قەرز"), 4 = Gold & School ("زێڕ و قوتابخانە")
  var activeSectionIndex by remember { mutableIntStateOf(0) }

  // Dynamically extract all buttons from user's live data
  val allQuickButtons = remember(uiState, isKurdish) {
    DynamicCategoryExtractor.extractAllQuickItems(uiState, isKurdish)
  }
  val carButtons = remember(uiState.carInvestments, isKurdish) {
    DynamicCategoryExtractor.extractCarBrandButtons(uiState.carInvestments, isKurdish)
  }
  val realEstateButtons = remember(uiState.nonCarInvestments, isKurdish) {
    DynamicCategoryExtractor.extractRealEstateButtons(uiState.nonCarInvestments, isKurdish)
  }
  val debtButtons = remember(uiState.debtsCredits, isKurdish) {
    DynamicCategoryExtractor.extractDebtButtons(uiState, isKurdish)
  }

  val displayedButtons = when (activeSectionIndex) {
    1 -> carButtons
    2 -> realEstateButtons
    3 -> debtButtons
    4 -> allQuickButtons.filter { it.key == "زێڕ" || it.key == "قوتابخانە" }
    else -> allQuickButtons
  }

  val sectionTabs = listOf(
    0 to if (isKurdish) "✨ هەمی دوکمە (${allQuickButtons.size})" else "✨ All Buttons (${allQuickButtons.size})",
    1 to if (isKurdish) "🚗 سەیارە (${carButtons.size})" else "🚗 Cars (${carButtons.size})",
    2 to if (isKurdish) "🏢 عەقارات (${realEstateButtons.size})" else "🏢 Properties (${realEstateButtons.size})",
    3 to if (isKurdish) "🤝 قەرز (${debtButtons.size})" else "🤝 Debts (${debtButtons.size})"
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Header & Section Quick Switcher
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title ?: if (isKurdish) "⚡ دوکمەیێن بلەز (دەست لێبدە بۆ دیتنا لیستێ):" else "⚡ Quick Category Buttons (Tap to List):",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = HoloNeonGold,
        modifier = Modifier.padding(horizontal = 4.dp)
      )

      // Section Filter Pills
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        sectionTabs.forEach { (index, tabLabel) ->
          val isTabSelected = activeSectionIndex == index
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isTabSelected) HoloNeonCyan.copy(alpha = 0.20f)
                else Color.White.copy(alpha = 0.04f)
              )
              .border(
                0.8.dp,
                if (isTabSelected) HoloNeonCyan else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(8.dp)
              )
              .clickable { activeSectionIndex = index }
              .padding(horizontal = 7.dp, vertical = 3.dp)
          ) {
            Text(
              text = tabLabel,
              fontSize = 10.5.sp,
              fontWeight = if (isTabSelected) FontWeight.ExtraBold else FontWeight.Medium,
              color = if (isTabSelected) HoloNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Dynamic Scrolling Lineup of Tactile Neon Category Buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      displayedButtons.forEach { item ->
        val isSelected = selectedCategory?.equals(item.key, ignoreCase = true) == true
        CyberNeonCategoryButton(
          label = item.label,
          accentColor = item.accentColor,
          isSelected = isSelected,
          icon = item.icon,
          count = item.count,
          onClick = { onSelectCategory(item.key) },
          testTagKey = "category_button_${item.key.lowercase().replace(" ", "_")}"
        )
      }
    }
  }
}
