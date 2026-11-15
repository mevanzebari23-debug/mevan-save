package com.example.ui.screens.school

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddSchoolExpenseDialog
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.LedgerItem
import com.example.ui.components.LedgerTile
import com.example.ui.components.TransactionDetailActionDialog
import com.example.ui.components.extractSchoolSubCategoryLabel
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.LedgerFormattingHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SchoolCyan = Color(0xFF38BDF8)
private val SchoolCyanDark = Color(0xFF0369A1)
private val SchoolNavyBg = Color(0xFF04121A)
private val SchoolCardBg = Color(0xF2071E2D)
private val SchoolBorderCyan = Color(0x6638BDF8)

/**
 * Dedicated, first-class Screen for School & Education (جهێ تایبەتێ قوتابخانێ).
 * Provides a unified standalone vault for tuition, uniforms, bus, books, and courses.
 */
@Composable
fun SchoolScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  modifier: Modifier = Modifier
) {
  val isKurdish = AppStrings.isKurdish(uiState.settings.language)
  val schoolTxs = uiState.schoolTransactions

  var showAddDialog by remember { mutableStateOf(false) }
  var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
  var selectedSchoolTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
  var selectedDeductionFilter by remember { mutableStateOf<Boolean?>(null) } // true: deducted, false: not deducted, null: all

  val keyboardController = LocalSoftwareKeyboardController.current

  // Filtered list
  val filteredTransactions by remember(schoolTxs, searchQuery, selectedCategoryFilter, selectedDeductionFilter, isKurdish) {
    derivedStateOf {
      schoolTxs.filter { tx ->
        val matchesQuery = if (searchQuery.isBlank()) true else {
          val query = searchQuery.trim().lowercase(Locale.ROOT)
          tx.category.lowercase(Locale.ROOT).contains(query) ||
            tx.notes.lowercase(Locale.ROOT).contains(query)
        }
        val subCat = extractSchoolSubCategoryLabel(tx.category, isKurdish)
        val matchesCat = selectedCategoryFilter == null || subCat == selectedCategoryFilter
        val matchesDeduction = selectedDeductionFilter == null || tx.affectsCapital == selectedDeductionFilter
        matchesQuery && matchesCat && matchesDeduction
      }
    }
  }

  // Breakdown by category
  val categorySpendingMap = remember(schoolTxs, uiState.displayCurrency, uiState.iqdRate, isKurdish) {
    schoolTxs.groupBy { tx ->
      extractSchoolSubCategoryLabel(tx.category, isKurdish)
    }.mapValues { (_, list) ->
      list.sumOf {
        CurrencyService.convert(
          it.amount,
          it.currency,
          uiState.displayCurrency,
          uiState.iqdRate,
          entryIqdRate = it.exchangeRateAtEntry
        )
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(SchoolNavyBg)
      .testTag("school_dedicated_screen")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
      // 1. Executive Top Hero Header
      item {
        SchoolHeroHeader(
          isKurdish = isKurdish,
          displayCurrency = uiState.displayCurrency,
          onToggleCurrency = { viewModel.toggleDisplayCurrency() },
          onOpenAddExpense = { showAddDialog = true }
        )
      }

      // 2. Grand Total Obsidian Card
      item {
        SchoolGrandTotalVaultCard(
          uiState = uiState,
          isKurdish = isKurdish,
          onQuickAdd = { showAddDialog = true }
        )
      }

      // 3. Category Breakdown Ribbon
      if (categorySpendingMap.isNotEmpty()) {
        item {
          SchoolCategoryBreakdownSection(
            categorySpendingMap = categorySpendingMap,
            selectedFilter = selectedCategoryFilter,
            displayCurrency = uiState.displayCurrency,
            isKurdish = isKurdish,
            onSelectFilter = { cat ->
              selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
            }
          )
        }
      }

      // 4. Search and Deduction Filter Bar
      item {
        SchoolSearchAndFilterBar(
          searchQuery = searchQuery,
          onSearchChange = { searchQuery = it },
          selectedDeductionFilter = selectedDeductionFilter,
          onSelectDeductionFilter = { selectedDeductionFilter = it },
          selectedCategoryFilter = selectedCategoryFilter,
          onClearCategoryFilter = { selectedCategoryFilter = null },
          totalCount = filteredTransactions.size,
          isKurdish = isKurdish,
          onClearSearch = {
            searchQuery = ""
            keyboardController?.hide()
          }
        )
      }

      // 5. School Transactions Ledger Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = null,
              tint = SchoolCyan,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = if (isKurdish) {
                "سجلا مەسروفێن قوتابخانێ (${filteredTransactions.size})"
              } else {
                "School Records Ledger (${filteredTransactions.size})"
              },
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = Color.White
            )
          }

          if (selectedCategoryFilter != null || selectedDeductionFilter != null || searchQuery.isNotBlank()) {
            Text(
              text = if (isKurdish) "پاقژکرنا فلتەران ↺" else "Reset Filters ↺",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = SchoolCyan,
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  selectedCategoryFilter = null
                  selectedDeductionFilter = null
                  searchQuery = ""
                }
                .padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // 6. Ledger Items List
      if (filteredTransactions.isEmpty()) {
        item {
          SchoolEmptyStateCard(
            isKurdish = isKurdish,
            hasActiveFilter = selectedCategoryFilter != null || selectedDeductionFilter != null || searchQuery.isNotBlank(),
            onAddFirstExpense = { showAddDialog = true }
          )
        }
      } else {
        items(
          items = filteredTransactions,
          key = { it.id }
        ) { tx ->
          SchoolLedgerItemCard(
            transaction = tx,
            uiState = uiState,
            isKurdish = isKurdish,
            onClick = { selectedSchoolTxForDetail = tx },
            onEdit = { viewModel.openQuickEditForTransaction(tx) },
            onDelete = { txToDelete = tx }
          )
        }
      }
    }
  }

  if (selectedSchoolTxForDetail != null) {
    TransactionDetailActionDialog(
      transaction = selectedSchoolTxForDetail!!,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedSchoolTxForDetail = null }
    )
  }

  // Add School Expense Dialog
  if (showAddDialog) {
    AddSchoolExpenseDialog(
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddDialog = false },
      onConfirm = { amount, currency, subCat, notes, deductCash, customRate ->
        viewModel.addSchoolExpense(
          amount = amount,
          currency = currency,
          subCategory = subCat,
          notes = notes,
          deductFromCash = deductCash,
          customIqdRate = customRate
        )
        showAddDialog = false
      }
    )
  }

  // Delete Confirmation Dialog
  if (txToDelete != null) {
    val tx = txToDelete!!
    AlertDialog(
      onDismissRequest = { txToDelete = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed)
          Text(
            text = if (isKurdish) "ژێبرنا مەسروفا قوتابخانێ؟" else "Delete School Record?",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      },
      text = {
        Text(
          text = if (isKurdish) {
            "ئەرێ تۆ پشتڕاستی تە دڤێت ئەڤ تۆمارە ب بڕێ ${CurrencyService.format(tx.amount, tx.currency)} (${tx.category}) ژێببەی؟"
          } else {
            "Are you sure you want to delete this school expense of ${CurrencyService.format(tx.amount, tx.currency)} (${tx.category})?"
          },
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteTransaction(tx.id)
            txToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = LossRed)
        ) {
          Text(if (isKurdish) "بەڵێ، ژێببە" else "Yes, Delete", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { txToDelete = null }) {
          Text(if (isKurdish) "پەشیمانبوون" else "Cancel")
        }
      }
    )
  }
}

/**
 * Top Hero Header for School Screen
 */
@Composable
private fun SchoolHeroHeader(
  isKurdish: Boolean,
  displayCurrency: String,
  onToggleCurrency: () -> Unit,
  onOpenAddExpense: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(
          Brush.linearGradient(
            listOf(SchoolCyan.copy(alpha = 0.28f), SchoolCyanDark.copy(alpha = 0.15f))
          )
        )
        .border(1.dp, SchoolCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.School,
          contentDescription = "School Vault",
          tint = SchoolCyan,
          modifier = Modifier.size(24.dp)
        )
      }

      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = if (isKurdish) "جهێ تایبەتێ قوتابخانێ" else "Dedicated School Vault",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(SchoolCyan.copy(alpha = 0.2f))
              .border(0.6.dp, SchoolCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "STANDALONE",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Black,
              color = SchoolCyan
            )
          }
        }
        Text(
          text = if (isKurdish) {
            "هەمی قست و خەرجیێن قوتابخانێ د یەک حسابا سەربەخۆ دا"
          } else {
            "All tuition, uniforms & educational spending in one ledger"
          },
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Quick Currency Pill
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(Color.White.copy(alpha = 0.08f))
        .border(0.8.dp, SchoolCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        .clickable { onToggleCurrency() }
        .padding(horizontal = 10.dp, vertical = 6.dp)
        .testTag("school_currency_toggle_pill"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = if (displayCurrency == "IQD") "ع.د" else "USD",
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        color = SchoolCyan
      )
    }
  }
}

/**
 * Grand Total Obsidian Vault Card
 */
@Composable
private fun SchoolGrandTotalVaultCard(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onQuickAdd: () -> Unit
) {
  val totalDisplay = uiState.totalSchoolSpendingDisplay
  val deductedDisplay = uiState.schoolDeductedFromCashDisplay
  val notDeductedDisplay = uiState.schoolNotDeductedFromCashDisplay
  val schoolTxs = uiState.schoolTransactions

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SchoolCardBg),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.2.dp, SchoolBorderCyan, RoundedCornerShape(20.dp))
      .testTag("school_grand_total_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isKurdish) "کۆما گشتی یا هەمی مەسروفێن قوتابخانێ" else "GRAND TOTAL SPENT ON SCHOOL",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SchoolCyan
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = CurrencyService.format(totalDisplay, uiState.displayCurrency),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.testTag("school_grand_total_display")
          )

          // Compact Iraqi Dinar shorthand
          val iqdEquivalent = if (uiState.displayCurrency == "IQD") {
            totalDisplay
          } else {
            CurrencyService.convert(totalDisplay, "USD", "IQD", uiState.iqdRate)
          }
          Text(
            text = "≈ ${LedgerFormattingHelper.formatIqdCompact(iqdEquivalent, isKurdish)}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = GoldAccent
          )
        }

        // Quick Add Button
        Button(
          onClick = onQuickAdd,
          colors = ButtonDefaults.buttonColors(
            containerColor = SchoolCyan
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          modifier = Modifier.testTag("add_school_expense_main_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
            Text(
              text = if (isKurdish) "+ زێدەکرن" else "+ Add",
              color = Color(0xFF0F172A),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp
            )
          }
        }
      }

      // Dual Currency details row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color.White.copy(alpha = 0.05f))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "USD: ${CurrencyService.format(uiState.totalSchoolSpendingUsd, "USD")}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldLight
        )
        Text(
          text = "IQD: ${CurrencyService.format(uiState.totalSchoolSpendingIqd, "IQD")}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = GoldAccent
        )
        Text(
          text = "${schoolTxs.size} ${if (isKurdish) "تۆمار" else "items"}",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Deduction Breakdown Split Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val deductedCount = schoolTxs.count { it.affectsCapital }
        val notDeductedCount = schoolTxs.count { !it.affectsCapital }

        // Deducted from Cash Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(LossRed.copy(alpha = 0.12f))
            .border(0.8.dp, LossRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LossRed, modifier = Modifier.size(12.dp))
              Text(
                text = if (isKurdish) "ژ کاشی کێمکری ($deductedCount)" else "Deducted ($deductedCount)",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = LossRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = CurrencyService.format(deductedDisplay, uiState.displayCurrency),
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }

        // Standalone (Not Deducted) Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(EmeraldLight.copy(alpha = 0.12f))
            .border(0.8.dp, EmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
              Text(
                text = if (isKurdish) "سەربەخۆ ($notDeductedCount)" else "Standalone ($notDeductedCount)",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = CurrencyService.format(notDeductedDisplay, uiState.displayCurrency),
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

/**
 * Category Breakdown Ribbon
 */
@Composable
private fun SchoolCategoryBreakdownSection(
  categorySpendingMap: Map<String, Double>,
  selectedFilter: String?,
  displayCurrency: String,
  isKurdish: Boolean,
  onSelectFilter: (String) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = if (isKurdish) "پوختەیا بەشێن قوتابخانێ:" else "Breakdown by Category:",
      fontSize = 12.sp,
      fontWeight = FontWeight.ExtraBold,
      color = SchoolCyan
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categorySpendingMap.forEach { (catName, totalSpent) ->
        val isSelected = selectedFilter == catName
        val icon = resolveSchoolCategoryIcon(catName)

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SchoolCyan.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f))
            .border(
              width = if (isSelected) 1.2.dp else 0.7.dp,
              color = if (isSelected) SchoolCyan else Color.White.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelectFilter(catName) }
            .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = if (isSelected) SchoolCyan else Color.White.copy(alpha = 0.75f),
              modifier = Modifier.size(16.dp)
            )
            Column {
              Text(
                text = catName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) SchoolCyan else Color.White
              )
              Text(
                text = CurrencyService.format(totalSpent, displayCurrency),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.White else GoldAccent
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Search and Filter Bar
 */
@Composable
private fun SchoolSearchAndFilterBar(
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedDeductionFilter: Boolean?,
  onSelectDeductionFilter: (Boolean?) -> Unit,
  selectedCategoryFilter: String?,
  onClearCategoryFilter: () -> Unit,
  totalCount: Int,
  isKurdish: Boolean,
  onClearSearch: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Search OutlinedTextField
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("school_search_field"),
      placeholder = {
        Text(
          text = if (isKurdish) "لێگەڕیان ل ناڤ قوتابخانێ (ناڤێ قوتابی، قست، جلوبەرگ)..." else "Search school records by student or item...",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = SchoolCyan, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = onClearSearch, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SchoolCyan,
        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
        focusedContainerColor = Color(0xD0071724),
        unfocusedContainerColor = Color(0xB0071724)
      ),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )

    // Quick Deduction Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // All
      DeductionChip(
        label = if (isKurdish) "هەمی" else "All",
        isSelected = selectedDeductionFilter == null,
        accentColor = SchoolCyan,
        onClick = { onSelectDeductionFilter(null) }
      )

      // Deducted
      DeductionChip(
        label = if (isKurdish) "✅ ژ کاشی کێمکری" else "✅ Deducted",
        isSelected = selectedDeductionFilter == true,
        accentColor = LossRed,
        onClick = { onSelectDeductionFilter(if (selectedDeductionFilter == true) null else true) }
      )

      // Standalone
      DeductionChip(
        label = if (isKurdish) "🛡️ سەربەخۆ" else "🛡️ Standalone",
        isSelected = selectedDeductionFilter == false,
        accentColor = EmeraldLight,
        onClick = { onSelectDeductionFilter(if (selectedDeductionFilter == false) null else false) }
      )

      if (selectedCategoryFilter != null) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GoldAccent.copy(alpha = 0.2f))
            .border(0.8.dp, GoldAccent, RoundedCornerShape(8.dp))
            .clickable { onClearCategoryFilter() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "$selectedCategoryFilter ✕",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DeductionChip(
  label: String,
  isSelected: Boolean,
  accentColor: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
      .border(
        width = if (isSelected) 1.dp else 0.6.dp,
        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 9.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
      color = if (isSelected) accentColor else Color.White.copy(alpha = 0.75f)
    )
  }
}

/**
 * School Ledger Card using Dark Glass Aesthetic
 */
@Composable
private fun SchoolLedgerItemCard(
  transaction: TransactionEntity,
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onClick: () -> Unit = {},
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val subCat = extractSchoolSubCategoryLabel(transaction.category, isKurdish)
  val icon = resolveSchoolCategoryIcon(subCat)
  val isDeducted = transaction.affectsCapital
  val formattedDate = remember(transaction.dateMillis) {
    val df = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    df.format(Date(transaction.dateMillis))
  }
  val iqdEquivalent = if (transaction.currency == "IQD") {
    transaction.amount
  } else {
    CurrencyService.convert(
      transaction.amount,
      transaction.currency,
      "IQD",
      uiState.iqdRate,
      entryIqdRate = transaction.exchangeRateAtEntry
    )
  }

  CyberRecordButton(
    title = subCat,
    subtitle = transaction.notes.ifBlank { null },
    dateOrMeta = formattedDate,
    primaryValue = CurrencyService.format(transaction.amount, transaction.currency),
    secondaryValue = LedgerFormattingHelper.formatIqdCompact(iqdEquivalent, isKurdish),
    accentColor = SchoolCyan,
    icon = icon,
    statusBadge = if (isDeducted) {
      if (isKurdish) "✅ ژ کاشی کێم بوویە" else "✅ Deducted"
    } else {
      if (isKurdish) "🛡️ سەربەخۆ" else "🛡️ Standalone"
    },
    statusBadgeColor = if (isDeducted) LossRed else EmeraldLight,
    actionHintLabel = if (isKurdish) "دەستکاری / ژێبرن ▸" else "Edit / Delete ▸",
    quickActionLabel = if (isKurdish) "دەستکاری" else "Edit",
    quickActionIcon = Icons.Default.Edit,
    quickActionColor = GoldAccent,
    onQuickActionClick = onEdit,
    onClick = onClick,
    testTagKey = "school_ledger_item_${transaction.id}"
  )
}

/**
 * Empty State Card
 */
@Composable
private fun SchoolEmptyStateCard(
  isKurdish: Boolean,
  hasActiveFilter: Boolean,
  onAddFirstExpense: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SchoolCardBg),
    modifier = Modifier
      .fillMaxWidth()
      .border(0.8.dp, SchoolCyan.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
      .padding(vertical = 12.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(SchoolCyan.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.School,
          contentDescription = null,
          tint = SchoolCyan,
          modifier = Modifier.size(30.dp)
        )
      }

      Text(
        text = if (hasActiveFilter) {
          if (isKurdish) "چ تۆمار ب ڤان فلتەران نەهاتنە دیتن" else "No records match active filters"
        } else {
          if (isKurdish) "هێشتا چ مەسروفێن قوتابخانێ نەهاتینە تۆمارکرن" else "No school expenses recorded yet"
        },
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Text(
        text = if (isKurdish) {
          "ل ڤێرێ دشێی هەمی قستێن خواندنێ، جلوبەرگ، پاس، پەرتووک، و کەرەستێن قوتابخانێ ب سەربەخۆیی تۆمار بکەی."
        } else {
          "Record all your tuition fees, school uniforms, bus transport, and educational supplies in this dedicated vault."
        },
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Button(
        onClick = onAddFirstExpense,
        colors = ButtonDefaults.buttonColors(containerColor = SchoolCyan),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isKurdish) "زێدەکرنا ئێکەم مەسروفا قوتابخانێ" else "Record First School Expense",
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Black,
          fontSize = 12.5.sp
        )
      }
    }
  }
}

private fun resolveSchoolCategoryIcon(cat: String): ImageVector {
  val lower = cat.lowercase(Locale.ROOT)
  return when {
    lower.contains("قست") || lower.contains("tuition") || lower.contains("fee") -> Icons.Default.Payment
    lower.contains("جل") || lower.contains("uniform") || lower.contains("cloth") -> Icons.Default.LocalMall
    lower.contains("پاس") || lower.contains("bus") || lower.contains("transport") -> Icons.Default.DirectionsBus
    lower.contains("پەرتووک") || lower.contains("book") || lower.contains("supply") || lower.contains("stationery") -> Icons.Default.MenuBook
    else -> Icons.Default.School
  }
}
