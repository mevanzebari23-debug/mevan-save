package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SearchCategory {
  ALL,
  TRANSACTIONS,
  CARS,
  DEBTS,
  INVESTMENTS,
  GOLD,
  BUDGETS
}

data class SearchResultItem(
  val id: String,
  val entityType: String,
  val entityId: Long,
  val title: String,
  val subtitle: String,
  val amountFormatted: String? = null,
  val dateFormatted: String? = null,
  val category: SearchCategory,
  val icon: ImageVector,
  val iconTint: Color,
  val targetTab: Int,
  val badge: String? = null,
  val extraKeywords: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchBottomSheet(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onNavigateToTab: (Int) -> Unit,
  onSelectSearchResult: ((SearchResultItem) -> Unit)? = null,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var query by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    delay(120)
    runCatching { focusRequester.requestFocus() }
  }

  val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

  fun openItemDirectly(item: SearchResultItem) {
    if (onSelectSearchResult != null) {
      onSelectSearchResult(item)
    } else {
      onNavigateToTab(item.targetTab)
    }
    onDismiss()
  }

  // Index all data items across the app
  val allItems = remember(uiState, isKurdish) {
    val items = mutableListOf<SearchResultItem>()

    // 1. Debts & Credits (People names - highest priority for name searches)
    uiState.debtsCredits.forEach { debt ->
      val isCredit = debt.type == "CREDIT"
      val remaining = debt.remainingAmount
      val isSettled = debt.isCompleted || remaining <= 0
      val subtitleParts = mutableListOf<String>()
      if (debt.contactInfo.isNotBlank()) subtitleParts.add(debt.contactInfo)
      if (debt.notes.isNotBlank()) subtitleParts.add(debt.notes)
      if (subtitleParts.isEmpty()) {
        subtitleParts.add(if (isCredit) "Money Lent / قەرزێ دای" else "Borrowed Money / قەرزێ وەرگرتی")
      }

      items.add(
        SearchResultItem(
          id = "debt_${debt.id}",
          entityType = "DEBT",
          entityId = debt.id,
          title = debt.personName,
          subtitle = subtitleParts.joinToString(" • "),
          amountFormatted = CurrencyService.formatShort(remaining, debt.currency),
          dateFormatted = dateFormat.format(Date(debt.createdAtMillis)),
          category = SearchCategory.DEBTS,
          icon = Icons.Default.Handshake,
          iconTint = if (isCredit) EmeraldLight else GoldAccent,
          targetTab = 1,
          badge = when {
            isSettled -> if (isKurdish) "واصل بوو" else "Settled"
            isCredit -> if (isKurdish) "دەف خەلکی" else "They Owe Me"
            else -> if (isKurdish) "لسەر من" else "I Owe"
          },
          extraKeywords = "${debt.personName} ${debt.contactInfo} ${debt.notes}"
        )
      )
    }

    // 2. Cars & Fleet
    uiState.carInvestments.forEach { car ->
      val remainingMoney = uiState.getCarRemainingMoney(car)
      val subtitleParts = mutableListOf<String>()
      if (car.notes.isNotBlank()) subtitleParts.add(car.notes)
      if (car.isCoOwned) subtitleParts.add(if (isKurdish) "شەریکاتی: ${car.partnerName}" else "Co-owned: ${car.partnerName}")
      if (car.isSold) subtitleParts.add(if (isKurdish) "فرۆتی ب ${car.soldPrice} ${car.currency}" else "Sold for ${car.soldPrice} ${car.currency}")
      if (remainingMoney > 0) subtitleParts.add(if (isKurdish) "قستێ مایی: $remainingMoney ${car.currency}" else "Remaining: $remainingMoney ${car.currency}")

      items.add(
        SearchResultItem(
          id = "car_${car.id}",
          entityType = "CAR",
          entityId = car.id,
          title = car.name,
          subtitle = subtitleParts.joinToString(" • ").ifBlank { if (isKurdish) "سەیارەیا بەردەست" else "Active Vehicle" },
          amountFormatted = CurrencyService.formatShort(car.currentValue, car.currency),
          dateFormatted = dateFormat.format(Date(car.purchaseDateMillis)),
          category = SearchCategory.CARS,
          icon = Icons.Default.DirectionsCar,
          iconTint = Color(0xFF38BDF8),
          targetTab = 2,
          badge = when {
            car.isSold -> if (isKurdish) "فرۆتی" else "Sold"
            car.isCoOwned -> if (isKurdish) "شەریک" else "Partner"
            car.soldPrice > 0 -> if (isKurdish) "قست" else "Installment"
            else -> if (isKurdish) "کاش" else "Cash"
          },
          extraKeywords = "${car.name} ${car.partnerName} ${car.notes}"
        )
      )
    }

    // 3. Non-Car Investments (Real estate, ventures, etc.)
    uiState.nonCarInvestments.forEach { inv ->
      val partnerInfo = if (inv.isCoOwned && inv.partnerName.isNotBlank()) " • ${inv.partnerName}" else ""
      items.add(
        SearchResultItem(
          id = "inv_${inv.id}",
          entityType = "INVESTMENT",
          entityId = inv.id,
          title = inv.name,
          subtitle = "${inv.category}$partnerInfo${if (inv.notes.isNotBlank()) " • ${inv.notes}" else ""}",
          amountFormatted = CurrencyService.formatShort(inv.currentValue, inv.currency),
          dateFormatted = dateFormat.format(Date(inv.purchaseDateMillis)),
          category = SearchCategory.INVESTMENTS,
          icon = if (inv.category.contains("Real", ignoreCase = true)) Icons.Default.Apartment else Icons.Default.Assessment,
          iconTint = EmeraldLight,
          targetTab = 2,
          badge = inv.category,
          extraKeywords = "${inv.name} ${inv.partnerName} ${inv.category} ${inv.notes}"
        )
      )
    }

    // 4. Gold & Silver Holdings
    uiState.goldHoldings.forEach { gold ->
      val liveValUsd = gold.currentValue(uiState.effectiveGoldPricePerGram24K, uiState.effectiveSilverPricePerGram9999)
      val badgeLabel = gold.getDisplayPurityBadge(isKurdish)
      val subLabel = if (gold.isSilver) {
        "${gold.weightGrams}g • ${gold.purityLabel}${if (gold.notes.isNotBlank()) " • ${gold.notes}" else ""}"
      } else {
        "${gold.weightGrams}g • ${gold.karat}K${if (gold.notes.isNotBlank()) " • ${gold.notes}" else ""}"
      }
      items.add(
        SearchResultItem(
          id = "gold_${gold.id}",
          entityType = "GOLD",
          entityId = gold.id,
          title = gold.title,
          subtitle = subLabel,
          amountFormatted = "$${"%.1f".format(liveValUsd)}",
          dateFormatted = dateFormat.format(Date(gold.purchaseDateMillis)),
          category = SearchCategory.GOLD,
          icon = Icons.Default.AutoGraph,
          iconTint = if (gold.isSilver) Color(0xFF94A3B8) else GoldAccent,
          targetTab = 2,
          badge = badgeLabel,
          extraKeywords = "${gold.title} ${gold.notes} ${gold.purityLabel} ${gold.karat} زێڕ زیڤ gold silver 9999"
        )
      )
    }

    // 5. Transactions (Cashflow & School)
    uiState.transactions.forEach { tx ->
      val isIncome = tx.type == "IN"
      val isSchool = uiState.isSchoolTransaction(tx)
      items.add(
        SearchResultItem(
          id = "tx_${tx.id}",
          entityType = "TRANSACTION",
          entityId = tx.id,
          title = tx.category.ifBlank { if (isIncome) "Income" else "Expense" },
          subtitle = if (tx.notes.isNotBlank()) tx.notes else (if (isSchool) "School Expense / مەسروفا قوتابخانێ" else if (isIncome) "Money In / داهات" else "Money Out / مەسروف"),
          amountFormatted = "${if (isIncome) "+" else "-"}${CurrencyService.formatShort(tx.amount, tx.currency)}",
          dateFormatted = dateFormat.format(Date(tx.dateMillis)),
          category = SearchCategory.TRANSACTIONS,
          icon = if (isSchool) Icons.Default.School else Icons.Default.ReceiptLong,
          iconTint = if (isSchool) Color(0xFF38BDF8) else if (isIncome) EmeraldLight else LossRed,
          targetTab = if (isSchool) 4 else 5,
          badge = if (isSchool) (if (isKurdish) "🎓 قوتابخانە" else "🎓 School") else if (isIncome) (if (isKurdish) "داهات" else "Income") else (if (isKurdish) "مەسروف" else "Expense"),
          extraKeywords = "${tx.category} ${tx.notes} قوتابخانە school"
        )
      )
    }

    // 6. Budgets
    uiState.budgets.forEach { budget ->
      items.add(
        SearchResultItem(
          id = "budget_${budget.id}",
          entityType = "BUDGET",
          entityId = budget.id,
          title = budget.category,
          subtitle = if (isKurdish) "سنوورێ هەیڤانە یێ دیارکری" else "Monthly Limit Alert",
          amountFormatted = CurrencyService.formatShort(budget.monthlyLimit, budget.currency),
          category = SearchCategory.BUDGETS,
          icon = Icons.Default.Tune,
          iconTint = EmeraldLight,
          targetTab = 3,
          badge = if (isKurdish) "بودجە" else "Budget",
          extraKeywords = budget.category
        )
      )
    }

    items
  }

  // Filtered and ranked items based on search query & selected category
  val filteredResults by remember(allItems, query, selectedCategory) {
    derivedStateOf {
      val trimmed = query.trim()
      val matched = allItems.filter { item ->
        val matchesCategory = selectedCategory == SearchCategory.ALL || item.category == selectedCategory
        val matchesQuery = if (trimmed.isEmpty()) {
          true
        } else {
          item.title.contains(trimmed, ignoreCase = true) ||
            item.subtitle.contains(trimmed, ignoreCase = true) ||
            item.extraKeywords.contains(trimmed, ignoreCase = true) ||
            (item.badge?.contains(trimmed, ignoreCase = true) == true) ||
            (item.amountFormatted?.contains(trimmed, ignoreCase = true) == true)
        }
        matchesCategory && matchesQuery
      }
      if (trimmed.isEmpty()) {
        matched
      } else {
        matched.sortedWith(
          compareByDescending<SearchResultItem> { it.title.equals(trimmed, ignoreCase = true) }
            .thenByDescending { it.title.startsWith(trimmed, ignoreCase = true) }
            .thenByDescending { it.title.contains(trimmed, ignoreCase = true) }
        )
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier
      .fillMaxHeight(0.92f)
      .imePadding(),
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Top Title Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .background(EmeraldLight.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = EmeraldLight,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isKurdish) "لێکگەڕیانا سەرتاسەری (ڤەکرن ب ١ کلیک)" else "Global Search (1-Click Open)",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            Text(
              text = if (isKurdish) "ناڤێ کەسی یان سەیارێ بنڤیسە و ب ١ کلیک ڕاستەوخۆ ڤەکە" else "Type any name and open it directly with 1 click",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Clear, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Bar TextField with Auto-Focus & IME Search 1-Click Open
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester)
          .testTag("global_search_input"),
        placeholder = {
          Text(
            if (isKurdish) "ناڤێ کەسی، سەیارە، پڕۆژە، زێڕ یان مەسروف بنڤیسە..."
            else "Type name, car, person, project, gold..."
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldLight)
        },
        trailingIcon = {
          if (query.isNotEmpty()) {
            IconButton(onClick = { query = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
          onSearch = {
            filteredResults.firstOrNull()?.let { topMatch ->
              openItemDirectly(topMatch)
            }
          }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldLight,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
      )

      // Instant 1-Click Direct Open Banner for the Best Matching Name when user types
      val topResult = if (query.trim().isNotEmpty()) filteredResults.firstOrNull() else null
      if (topResult != null) {
        Spacer(modifier = Modifier.height(10.dp))
        CyberRecordButton(
          title = if (isKurdish) "١ کلیک: ${topResult.title}" else "1-Click Open: ${topResult.title}",
          subtitle = topResult.subtitle,
          primaryValue = topResult.amountFormatted ?: (if (isKurdish) "ڤەکرن" else "Open"),
          dateOrMeta = topResult.dateFormatted,
          accentColor = EmeraldLight,
          icon = topResult.icon,
          statusBadge = topResult.badge ?: (if (isKurdish) "باشترین ئەنجام" else "Top Match"),
          statusBadgeColor = EmeraldLight,
          actionHintLabel = if (isKurdish) "ڤەکرن" else "Open",
          isHighlighted = true,
          onClick = { openItemDirectly(topResult) },
          testTagKey = "search_top_match_one_click_card"
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Category filter pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val categories = listOf(
          SearchCategory.ALL to (if (isKurdish) "هەمی" else "All"),
          SearchCategory.DEBTS to (if (isKurdish) "قەرز و ناڤ" else "Debts & Names"),
          SearchCategory.CARS to (if (isKurdish) "سەیارە" else "Cars"),
          SearchCategory.INVESTMENTS to (if (isKurdish) "وەبەرهێنان" else "Invest"),
          SearchCategory.GOLD to (if (isKurdish) "زێڕ" else "Gold"),
          SearchCategory.TRANSACTIONS to (if (isKurdish) "داهات و مەسروف" else "Cashflow"),
          SearchCategory.BUDGETS to (if (isKurdish) "بودجە" else "Budgets")
        )

        categories.forEach { (cat, label) ->
          val isSelected = selectedCategory == cat
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSelected) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant)
              .clickable { selectedCategory = cat }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Count label
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isKurdish) "ئەنجامێن دیتنێ: ${filteredResults.size} (کلیک بکە بۆ ڤەکرنا ڕاستەوخۆ)" else "Results Found: ${filteredResults.size} (Tap any item to open directly)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold
        )
        if (query.isNotEmpty()) {
          Text(
            text = if (isKurdish) "پاقژکرن" else "Clear Search",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldLight,
            modifier = Modifier.clickable { query = "" },
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Search results list
      if (filteredResults.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (isKurdish) "چ ئەنجام نەهاتنە دیتن بۆ \"$query\"" else "No results found for \"$query\"",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = if (isKurdish) "پەیڤەکا دی بنڤیسە یان فلتەرێ پۆلان بگوهۆڕە" else "Try searching a different word or change category filter",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          items(filteredResults, key = { it.id }) { item ->
            CyberRecordButton(
              title = item.title,
              subtitle = item.subtitle,
              primaryValue = item.amountFormatted ?: (if (isKurdish) "ڤەکرن" else "Open"),
              dateOrMeta = item.dateFormatted,
              accentColor = item.iconTint,
              icon = item.icon,
              statusBadge = item.badge,
              statusBadgeColor = item.iconTint,
              actionHintLabel = if (isKurdish) "ڤەکرن" else "Open",
              onClick = { openItemDirectly(item) },
              testTagKey = "search_result_${item.id}"
            )
          }
        }
      }
    }
  }
}
