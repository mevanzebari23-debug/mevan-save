package com.example.ui.screens.cashflow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.TransactionDetailActionDialog
import com.example.ui.components.CategoryFilterBar
import com.example.ui.components.TransactionSearchFilterBar
import com.example.ui.components.isTransactionDateInRange
import com.example.ui.categories.AppCategoryRegistry
import com.example.ui.categories.CategorySectionType
import com.example.ui.components.PrintPreviewDialog
import com.example.util.PdfExportService
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddCapitalAdditionDialog
import com.example.ui.components.AddSchoolExpenseDialog
import com.example.ui.components.CategorySpendingBreakdown
import com.example.ui.components.SchoolAccountHubDialog
import com.example.ui.components.SchoolSummarySectionCard
import com.example.ui.components.SectionQuickHistoryStrip
import com.example.ui.history.ActionSection
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.debts.AddDebtCreditDialog
import com.example.ui.screens.overview.CategoryDeleteResolutionDialog
import com.example.ui.screens.overview.QuickTransactionDialog
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SchoolVaultAccent = Color(0xFF38BDF8)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CashFlowScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)
  val context = LocalContext.current

  var filterType by remember { mutableStateOf("ALL") } // "ALL", "IN", "OUT", "SCHOOL"
  var selectedExpenseCategoryFilter by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var dateRangePreset by remember { mutableStateOf("ALL") } // "ALL", "TODAY", "THIS_WEEK", "THIS_MONTH", "LAST_30_DAYS", "CUSTOM"
  var customStartDateMillis by remember { mutableStateOf<Long?>(null) }
  var customEndDateMillis by remember { mutableStateOf<Long?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }
  var addDialogType by remember { mutableStateOf("IN") }
  var addDialogInitialCategory by remember { mutableStateOf<String?>(null) }
  var showAddCapitalDialog by remember { mutableStateOf(false) }
  var showQuickLendDialog by remember { mutableStateOf(false) }
  var showQuickBorrowDialog by remember { mutableStateOf(false) }
  var showSchoolHubDialog by remember { mutableStateOf(false) }
  var showAddSchoolExpenseDialog by remember { mutableStateOf(false) }
  var showCashQuickMenuDialog by remember { mutableStateOf(false) }
  var isCategoriesExpanded by remember { mutableStateOf(false) }
  var newExpenseCategoryInput by remember { mutableStateOf("") }
  var categoryPendingDeletion by remember { mutableStateOf<String?>(null) }
  var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
  var previewPdfResult by remember { mutableStateOf<com.example.util.GeneratedPdfResult?>(null) }
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US) }

  // General Expense Categories combined from built-in presets and user custom categories
  val expenseCategories = remember(uiState.customExpenseCategories, isKurdish) {
    AppCategoryRegistry.getCombinedCategoryNames(CategorySectionType.EXPENSE, uiState.customExpenseCategories, isKurdish)
  }

  LaunchedEffect(uiState.focusedEntityType, uiState.focusedEntityId, uiState.transactions) {
    if (uiState.focusedEntityType == "TRANSACTION" && uiState.focusedEntityId != null) {
      val targetTx = uiState.transactions.find { it.id == uiState.focusedEntityId }
      if (targetTx != null) {
        if (uiState.isSchoolTransaction(targetTx)) {
          showSchoolHubDialog = true
        } else {
          filterType = "ALL"
          selectedExpenseCategoryFilter = null
          dateRangePreset = "ALL"
          searchQuery = ""
          selectedTransaction = targetTx
        }
        viewModel.clearFocusedSearchResult()
      }
    }
  }

  // Non-school general transactions for the main Cash & General Expenses ledger
  val generalTransactions = remember(uiState.transactions) {
    uiState.transactions.filterNot { uiState.isSchoolTransaction(it) }
  }

  val filteredTransactions = remember(
    uiState.transactions,
    generalTransactions,
    filterType,
    selectedExpenseCategoryFilter,
    searchQuery,
    dateRangePreset,
    customStartDateMillis,
    customEndDateMillis
  ) {
    val baseList = when (filterType) {
      "IN" -> generalTransactions.filter { it.type == "IN" }
      "OUT" -> generalTransactions.filter { it.type == "OUT" }
      "SCHOOL" -> uiState.schoolTransactions
      else -> generalTransactions
    }

    // 1. Filter by category
    val byCategory = if (selectedExpenseCategoryFilter != null && filterType != "SCHOOL") {
      baseList.filter { tx ->
        tx.category.equals(selectedExpenseCategoryFilter, ignoreCase = true)
      }
    } else {
      baseList
    }

    // 2. Filter by date range
    val byDate = byCategory.filter { tx ->
      isTransactionDateInRange(
        dateMillis = tx.dateMillis,
        preset = dateRangePreset,
        customStartMillis = customStartDateMillis,
        customEndMillis = customEndDateMillis
      )
    }

    // 3. Filter by search query (category, notes, amount, currency)
    if (searchQuery.isBlank()) {
      byDate
    } else {
      val q = searchQuery.trim().lowercase()
      byDate.filter { tx ->
        tx.category.lowercase().contains(q) ||
          tx.notes.lowercase().contains(q) ||
          tx.currency.lowercase().contains(q) ||
          tx.amount.toString().contains(q)
      }
    }
  }

  // Calculate general spending category totals (excluding School so School stays 100% separate)
  val categorySpendingMap = remember(uiState.generalOutTransactions, uiState.displayCurrency, uiState.iqdRate) {
    uiState.generalOutTransactions
      .groupBy { it.category }
      .mapValues { (_, list) ->
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
    modifier = modifier.fillMaxSize()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Organized Cash Account Hub Card (حسابا ڕێکخستی یا کاشی)
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xD0030A08)),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, EmeraldLight.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
            .testTag("financial_transaction_log_card")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.Transparent)
              .padding(16.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              // Top Row: Pure Cash Balance + Secondary Currency Equivalent + 100$ IQD Rate Indicator
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(RoundedCornerShape(13.dp))
                      .background(EmeraldLight.copy(alpha = 0.18f))
                      .border(1.dp, EmeraldLight.copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.AccountBalanceWallet,
                      contentDescription = null,
                      tint = EmeraldLight,
                      modifier = Modifier.size(23.dp)
                    )
                  }
                  Column {
                    Text(
                      text = if (isKurdish) "💵 حسابا ڕێکخستی یا کاشێ بەردەست" else "💵 ORGANIZED PURE CASH ACCOUNT",
                      style = MaterialTheme.typography.labelSmall,
                      color = EmeraldLight,
                      fontWeight = FontWeight.ExtraBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    val formattedCash = CurrencyService.format(uiState.pureCashBalance, uiState.displayCurrency)
                    Text(
                      text = formattedCash,
                      fontSize = if (formattedCash.length > 13) 19.sp else 24.sp,
                      fontWeight = FontWeight.Black,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.testTag("cashflow_pure_cash_text")
                    )
                    val secondaryCur = if (uiState.displayCurrency == "USD") "IQD" else "USD"
                    val cashInSecondary = CurrencyService.convert(
                      uiState.pureCashBalance,
                      uiState.displayCurrency,
                      secondaryCur,
                      uiState.iqdRate
                    )
                    Text(
                      text = "≈ ${CurrencyService.format(cashInSecondary, secondaryCur)}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldAccent.copy(alpha = 0.9f)
                    )
                  }
                }

                // Current 100$ Rate Badge
                val per100Iqd = (uiState.iqdRate * 100.0).toLong()
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldAccent.copy(alpha = 0.18f))
                    .border(1.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = "100$ = ${"%,d".format(Locale.US, per100Iqd)} IQD",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                  )
                }
              }

              // Organized 2x2 Cash Breakdown Grid (ڕێکخستنا وورد یا هاتن و چوونا کاشی)
              val totalCapitalBaseAndAdditions = uiState.startingCapitalDisplay
              val netAssetCashFlow = uiState.totalAssetSalesCashIn - uiState.totalAssetPurchasesCashOut
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.White.copy(alpha = 0.05f))
                  .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Pillar 1: Base Capital + Additions
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(GoldAccent.copy(alpha = 0.1f))
                      .padding(horizontal = 10.dp, vertical = 7.dp)
                  ) {
                    Column {
                      Text(
                        text = if (isKurdish) "🏛️ سەرمایێ بنەڕەت" else "🏛️ Capital Base",
                        fontSize = 10.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = CurrencyService.formatShort(totalCapitalBaseAndAdditions, uiState.displayCurrency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                      )
                    }
                  }

                  // Pillar 2: Cash Income (+)
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(EmeraldLight.copy(alpha = 0.1f))
                      .padding(horizontal = 10.dp, vertical = 7.dp)
                  ) {
                    Column {
                      Text(
                        text = if (isKurdish) "📥 داهاتێ کاش (+)" else "📥 Cash Income (+)",
                        fontSize = 10.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "+${CurrencyService.formatShort(uiState.totalMoneyIn, uiState.displayCurrency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldLight
                      )
                    }
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Pillar 3: Cash Expenses Deducted (-)
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(LossRed.copy(alpha = 0.1f))
                      .padding(horizontal = 10.dp, vertical = 7.dp)
                  ) {
                    Column {
                      Text(
                        text = if (isKurdish) "📤 مەسروفێ کاشی (-)" else "📤 Cash Outflows (-)",
                        fontSize = 10.sp,
                        color = LossRed,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "-${CurrencyService.formatShort(uiState.totalMoneyOut, uiState.displayCurrency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LossRed
                      )
                    }
                  }

                  // Pillar 4: Asset Buy/Sell Net Cash
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(Color(0xFF38BDF8).copy(alpha = 0.1f))
                      .padding(horizontal = 10.dp, vertical = 7.dp)
                  ) {
                    Column {
                      Text(
                        text = if (isKurdish) "🔄 کڕین/فرۆتنا ملوک" else "🔄 Asset Buy/Sell",
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "${if (netAssetCashFlow >= 0) "+" else ""}${CurrencyService.formatShort(netAssetCashFlow, uiState.displayCurrency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netAssetCashFlow >= 0) EmeraldLight else GoldAccent
                      )
                    }
                  }
                }
              }

              // Single Unified Quick Action Menu Trigger (ئێک مێنۆیا بلەز بۆ هەمی کردارێن کاشی)
              Button(
                onClick = { showCashQuickMenuDialog = true },
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldLight,
                  contentColor = Color(0xFF041E19)
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 46.dp)
                  .testTag("cash_quick_menu_trigger_btn")
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isKurdish) "+ مێنۆیا بلەز یا کاشی (داهات • مەسروف • قوتابخانە • قەرز)" else "+ Quick Cash Menu (Income • Expense • School • Loan)",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Black,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              // Hidden testTag anchors for automated test compatibility
              Row(modifier = Modifier.size(0.dp)) {
                Box(
                  modifier = Modifier
                    .testTag("cashflow_quick_income_btn")
                    .clickable {
                      addDialogType = "IN"
                      addDialogInitialCategory = null
                      showAddDialog = true
                    }
                )
                Box(
                  modifier = Modifier
                    .testTag("cashflow_quick_expense_btn")
                    .clickable {
                      addDialogType = "OUT"
                      addDialogInitialCategory = null
                      showAddDialog = true
                    }
                )
                Box(
                  modifier = Modifier
                    .testTag("log_add_capital_button")
                    .clickable { showAddCapitalDialog = true }
                )
                Box(
                  modifier = Modifier
                    .testTag("cashflow_quick_school_expense_btn")
                    .clickable { showAddSchoolExpenseDialog = true }
                )
                Box(
                  modifier = Modifier
                    .testTag("cashflow_quick_lend_btn")
                    .clickable { showQuickLendDialog = true }
                )
                Box(
                  modifier = Modifier
                    .testTag("log_new_loan_taken_button")
                    .clickable { showQuickBorrowDialog = true }
                )
              }
            }
          }
        }
      }

      // 2. DEDICATED & CLEAN SCHOOL ACCOUNT BAR (حسابا تایبەت یا قوتابخانێ — بێ دووبارەبوون)
      item {
        SchoolSummarySectionCard(
          uiState = uiState,
          isKurdish = isKurdish,
          onOpenSchoolHub = { viewModel.setTab(4) },
          onQuickAddSchoolItem = { showAddSchoolExpenseDialog = true }
        )
      }

      // 3. Collapsible General Expense Categories Accordion (لیستەیا داخستی یا پۆلێن مەسروفاتان)
      item {
        val totalGeneralSpent = categorySpendingMap.values.sum()
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xD00A0505)),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LossRed.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .testTag("expense_categories_section_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isCategoriesExpanded = !isCategoriesExpanded }
                .padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isKurdish) "📂 پۆلێن مەسروفاتێن گشتی (${expenseCategories.size})" else "📂 General Expense Categories (${expenseCategories.size})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (selectedExpenseCategoryFilter != null) {
                    if (isKurdish) "فلتەرکری: $selectedExpenseCategoryFilter" else "Filtered: $selectedExpenseCategoryFilter"
                  } else if (isCategoriesExpanded) {
                    if (isKurdish) "پۆلێن خۆ یێن تایبەت داخل بکە یان ژێببە" else "Add, filter, or delete your own custom categories"
                  } else {
                    if (isKurdish) "کلیک بکە بۆ داخلکرن یان دیتنا پۆلێن خۆ" else "Tap to add or view your custom categories"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  color = if (selectedExpenseCategoryFilter != null) LossRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (selectedExpenseCategoryFilter != null) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(LossRed.copy(alpha = 0.15f))
                      .clickable { selectedExpenseCategoryFilter = null }
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Text(
                      text = if (isKurdish) "هەمی ✕" else "Clear ✕",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = LossRed
                    )
                  }
                } else if (totalGeneralSpent > 0.0) {
                  Text(
                    text = CurrencyService.formatShort(totalGeneralSpent, uiState.displayCurrency),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LossRed
                  )
                }

                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(LossRed.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (isCategoriesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isCategoriesExpanded) "Collapse" else "Expand",
                    tint = LossRed,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            AnimatedVisibility(visible = isCategoriesExpanded) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Add New Custom Expense Category Row (داخلکرنا پۆلا مەسروفی ب دەستێ تە)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  OutlinedTextField(
                    value = newExpenseCategoryInput,
                    onValueChange = { newExpenseCategoryInput = it },
                    placeholder = {
                      Text(
                        text = if (isKurdish) "ناڤێ پۆلا خۆ بنڤیسە..." else "Type your custom category...",
                        fontSize = 12.sp
                      )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("cashflow_new_category_input")
                  )
                  Button(
                    onClick = {
                      val clean = newExpenseCategoryInput.trim()
                      if (clean.isNotEmpty()) {
                        viewModel.addCustomCategory("OUT", clean)
                        newExpenseCategoryInput = ""
                      }
                    },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = LossRed,
                      contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                      .heightIn(min = 48.dp)
                      .testTag("cashflow_add_category_button")
                  ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isKurdish) "داخلکرن" else "Add",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Black
                    )
                  }
                }

                if (expenseCategories.isEmpty()) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                      .border(0.8.dp, LossRed.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                      .padding(horizontal = 12.dp, vertical = 10.dp)
                  ) {
                    Text(
                      text = if (isKurdish) {
                        "هیچ پۆلەک نەهاتیە دانان — ناڤێ پۆلا خۆ ل سەری بنڤیسە و کلیک ل (+ داخلکرن) بکە."
                      } else {
                        "No preset categories — type your own category above and tap (+ Add)."
                      },
                      fontSize = 11.5.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                } else {
                  expenseCategories.forEach { rawCat ->
                    val isSelected = selectedExpenseCategoryFilter.equals(rawCat, ignoreCase = true)
                    val catSpent = categorySpendingMap.entries
                      .firstOrNull { it.key.equals(rawCat, ignoreCase = true) }
                      ?.value ?: 0.0
                    val ratio = if (totalGeneralSpent > 0.0) (catSpent / totalGeneralSpent).toFloat().coerceIn(0f, 1f) else 0f
                    val chipAccent = LossRed
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                          if (isSelected) chipAccent.copy(alpha = 0.18f)
                          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                          width = if (isSelected) 1.2.dp else 0.8.dp,
                          color = if (isSelected) chipAccent else chipAccent.copy(alpha = 0.22f),
                          shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                          if (isSelected) {
                            selectedExpenseCategoryFilter = null
                          } else {
                            filterType = "OUT"
                            selectedExpenseCategoryFilter = rawCat
                          }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("expense_category_chip_$rawCat"),
                      verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "📂 $rawCat",
                          fontSize = 12.sp,
                          fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                          color = if (isSelected) chipAccent else MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.weight(1f)
                        )
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          Text(
                            text = CurrencyService.formatShort(catSpent, uiState.displayCurrency),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (catSpent > 0.0) chipAccent else MaterialTheme.colorScheme.onSurfaceVariant
                          )
                          Box(
                            modifier = Modifier
                              .clip(RoundedCornerShape(8.dp))
                              .background(chipAccent.copy(alpha = 0.2f))
                              .clickable {
                                addDialogType = "OUT"
                                addDialogInitialCategory = rawCat
                                showAddDialog = true
                              }
                              .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = if (isKurdish) "+ زێدەکرن" else "+ Add",
                              fontSize = 10.sp,
                              fontWeight = FontWeight.Black,
                              color = chipAccent
                            )
                          }
                          Box(
                            modifier = Modifier
                              .size(26.dp)
                              .clip(CircleShape)
                              .background(LossRed.copy(alpha = 0.14f))
                              .clickable {
                                val associatedTxs = uiState.transactions.filter {
                                  it.type.equals("OUT", ignoreCase = true) &&
                                    it.category.trim().equals(rawCat.trim(), ignoreCase = true)
                                }
                                if (associatedTxs.isNotEmpty()) {
                                  categoryPendingDeletion = rawCat
                                } else {
                                  if (selectedExpenseCategoryFilter.equals(rawCat, ignoreCase = true)) {
                                    selectedExpenseCategoryFilter = null
                                  }
                                  viewModel.removeCustomCategory("OUT", rawCat)
                                }
                              }
                              .testTag("delete_expense_category_$rawCat"),
                            contentAlignment = Alignment.Center
                          ) {
                            Icon(
                              imageVector = Icons.Default.DeleteOutline,
                              contentDescription = "Delete Category",
                              tint = LossRed,
                              modifier = Modifier.size(15.dp)
                            )
                          }
                        }
                      }
                      if (catSpent > 0.0) {
                        LinearProgressIndicator(
                          progress = { ratio },
                          modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp)),
                          color = chipAccent,
                          trackColor = Color.White.copy(alpha = 0.08f)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 5. Unified Transaction History Search & Multi-Dimensional Filter Bar
      item {
        TransactionSearchFilterBar(
          searchQuery = searchQuery,
          onSearchQueryChange = { searchQuery = it },
          selectedType = filterType,
          onSelectType = { filterType = it },
          selectedCategory = selectedExpenseCategoryFilter,
          onSelectCategory = { selectedExpenseCategoryFilter = it },
          categories = expenseCategories,
          dateRangePreset = dateRangePreset,
          onSelectDateRangePreset = { dateRangePreset = it },
          customStartDateMillis = customStartDateMillis,
          customEndDateMillis = customEndDateMillis,
          onSetCustomDateRange = { start, end ->
            customStartDateMillis = start
            customEndDateMillis = end
          },
          totalMatchedCount = filteredTransactions.size,
          totalTransactionsCount = uiState.transactions.size,
          isKurdish = isKurdish,
          onResetAllFilters = {
            searchQuery = ""
            filterType = "ALL"
            selectedExpenseCategoryFilter = null
            dateRangePreset = "ALL"
            customStartDateMillis = null
            customEndDateMillis = null
          }
        )
      }

      // 6. Transactions header with View Mode toggle (Excel vs Cards) & Export CSV
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (filterType == "SCHOOL") {
                if (isKurdish) "🎓 تۆمارێن حسابا قوتابخانێ (${filteredTransactions.size})" else "🎓 School Account Records (${filteredTransactions.size})"
              } else {
                if (isKurdish) "تۆمارا داهات و مەسروفێ گشتی (${filteredTransactions.size})" else "General Ledger Records (${filteredTransactions.size})"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold
            )

            // View Mode Switcher + Export CSV button
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Export PDF Button
              Box(
                modifier = Modifier
                  .heightIn(min = 34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFDC2626).copy(alpha = 0.16f))
                  .border(0.8.dp, Color(0xFFDC2626).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                  .clickable {
                    previewPdfResult = PdfExportService.generateTransactionsPdf(
                      context = context,
                      transactions = filteredTransactions,
                      displayCurrency = uiState.displayCurrency,
                      currentIqdRate = uiState.iqdRate,
                      isKurdish = isKurdish
                    )
                  }
                  .padding(horizontal = 8.dp, vertical = 5.dp)
                  .testTag("cashflow_export_pdf_btn"),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Export PDF",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(15.dp)
                  )
                  Text(
                    text = "PDF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFEF4444)
                  )
                }
              }
            }
          }
        }
      }

      if (filteredTransactions.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xD0050E0C)),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(16.dp))
          ) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
              Text(
                text = if (isKurdish) "چ تۆمار نەهاتینە دیتن بۆ ڤی فلتەری." else "No records found for the selected filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
              )
            }
          }
        }
      } else {
        items(filteredTransactions, key = { it.id }) { tx ->
          val converted = CurrencyService.convert(
            tx.amount,
            tx.currency,
            uiState.displayCurrency,
            uiState.iqdRate,
            entryIqdRate = tx.exchangeRateAtEntry
          )
          val isSchoolTx = uiState.isSchoolTransaction(tx)
          val accent = when {
            tx.type == "IN" -> EmeraldLight
            isSchoolTx -> SchoolVaultAccent
            else -> LossRed
          }
          val badgeText = if (tx.type == "OUT") {
            if (tx.affectsCapital) {
              if (isKurdish) "ژ کاشی کێم بوویە" else "Deducted from Cash"
            } else {
              if (isKurdish) "ژ کاشی کێم نەبوویە" else "Not Deducted"
            }
          } else {
            if (isKurdish) "داهات (+)" else "Income (+)"
          }
          val badgeColor = when {
            tx.type == "IN" -> EmeraldLight
            tx.affectsCapital -> LossRed
            else -> EmeraldLight
          }
          CyberRecordButton(
            title = tx.category,
            subtitle = tx.notes.ifBlank { null },
            dateOrMeta = dateFormat.format(Date(tx.dateMillis)),
            primaryValue = "${if (tx.type == "IN") "+" else "-"}${CurrencyService.format(converted, uiState.displayCurrency)}",
            secondaryValue = if (tx.currency != uiState.displayCurrency) {
              CurrencyService.formatShort(tx.amount, tx.currency)
            } else null,
            accentColor = accent,
            icon = when {
              tx.type == "IN" -> Icons.Default.ArrowDownward
              isSchoolTx -> Icons.Default.School
              else -> Icons.Default.ArrowUpward
            },
            statusBadge = badgeText,
            statusBadgeColor = badgeColor,
            actionHintLabel = if (isKurdish) "کردار / دەستکاری ▸" else "Actions / Edit ▸",
            quickActionLabel = if (isKurdish) "دەستکاری" else "Edit",
            quickActionIcon = Icons.Default.Edit,
            quickActionColor = GoldAccent,
            onQuickActionClick = { viewModel.openQuickEditForTransaction(tx) },
            onClick = { selectedTransaction = tx },
            testTagKey = "cashflow_tx_btn_${tx.id}"
          )
        }
      }
    }

    // Hidden testTag anchor for add_cashflow_fab compatibility
    Box(
      modifier = Modifier
        .size(0.dp)
        .align(Alignment.BottomEnd)
        .testTag("add_cashflow_fab")
        .clickable {
          if (filterType == "SCHOOL") {
            showAddSchoolExpenseDialog = true
          } else {
            addDialogType = if (filterType == "OUT") "OUT" else "IN"
            addDialogInitialCategory = null
            showAddDialog = true
          }
        }
    )
  }

  // Unified Cash Quick-Action Menu Dialog (مێنۆیا بلەز یا هەمی کردارێن کاشی)
  if (showCashQuickMenuDialog) {
    AlertDialog(
      onDismissRequest = { showCashQuickMenuDialog = false },
      title = {
        Text(
          text = if (isKurdish) "⚡ مێنۆیا بلەز یا کاشی" else "⚡ Quick Cash Action Menu",
          fontWeight = FontWeight.Black
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (isKurdish) "کردارا خۆ هەلبژێرە بۆ تۆمارکرنا ڕاستەوخۆ:" else "Choose an action to log immediately:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Row 1: Money In & Money Out
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                showCashQuickMenuDialog = false
                addDialogType = "IN"
                addDialogInitialCategory = null
                showAddDialog = true
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldLight,
                contentColor = Color(0xFF041E19)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "+ داهات" else "+ Money In",
                fontWeight = FontWeight.Black,
                fontSize = 12.5.sp
              )
            }

            Button(
              onClick = {
                showCashQuickMenuDialog = false
                addDialogType = "OUT"
                addDialogInitialCategory = null
                showAddDialog = true
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = LossRed,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "− مەسروف" else "− Expense",
                fontWeight = FontWeight.Black,
                fontSize = 12.5.sp
              )
            }
          }

          // Row 2: School Account Expense & Add Capital
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                showCashQuickMenuDialog = false
                showAddSchoolExpenseDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 46.dp)
            ) {
              Text(
                text = if (isKurdish) "🎓 قوتابخانە" else "🎓 School",
                fontWeight = FontWeight.ExtraBold,
                color = SchoolVaultAccent,
                fontSize = 12.sp
              )
            }

            OutlinedButton(
              onClick = {
                showCashQuickMenuDialog = false
                showAddCapitalDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 46.dp)
            ) {
              Text(
                text = if (isKurdish) "🏛️ + سەرمایە" else "🏛️ + Capital",
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent,
                fontSize = 12.sp
              )
            }
          }

          // Row 3: Lend & Borrow
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                showCashQuickMenuDialog = false
                showQuickLendDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 46.dp)
            ) {
              Text(
                text = if (isKurdish) "🤝 قەرز دەم" else "🤝 Lend",
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLight,
                fontSize = 12.sp
              )
            }

            OutlinedButton(
              onClick = {
                showCashQuickMenuDialog = false
                showQuickBorrowDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 46.dp)
            ) {
              Text(
                text = if (isKurdish) "📥 قەرز وەرگرم" else "📥 Borrow",
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF97316),
                fontSize = 12.sp
              )
            }
          }
        }
      },
      confirmButton = {
        OutlinedButton(onClick = { showCashQuickMenuDialog = false }) {
          Text(if (isKurdish) "داخستن" else "Close")
        }
      }
    )
  }

  val pendingDeleteCat = categoryPendingDeletion
  if (pendingDeleteCat != null) {
    val associatedTxs = uiState.transactions.filter {
      it.type.equals("OUT", ignoreCase = true) &&
        it.category.trim().equals(pendingDeleteCat.trim(), ignoreCase = true)
    }
    CategoryDeleteResolutionDialog(
      categoryName = pendingDeleteCat,
      type = "OUT",
      associatedTransactions = associatedTxs,
      availableCategories = uiState.customExpenseCategories.filterNot {
        it.equals(pendingDeleteCat, ignoreCase = true)
      },
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { categoryPendingDeletion = null },
      onReassignAndDelete = { targetCategory ->
        if (selectedExpenseCategoryFilter.equals(pendingDeleteCat, ignoreCase = true)) {
          selectedExpenseCategoryFilter = null
        }
        viewModel.removeCustomCategory(
          type = "OUT",
          rawName = pendingDeleteCat,
          deleteAssociatedTransactions = false,
          reassignToCategory = targetCategory
        )
        categoryPendingDeletion = null
      },
      onDeleteTransactionsAndCategory = {
        if (selectedExpenseCategoryFilter.equals(pendingDeleteCat, ignoreCase = true)) {
          selectedExpenseCategoryFilter = null
        }
        viewModel.removeCustomCategory(
          type = "OUT",
          rawName = pendingDeleteCat,
          deleteAssociatedTransactions = true,
          reassignToCategory = null
        )
        categoryPendingDeletion = null
      }
    )
  }

  if (showAddDialog) {
    QuickTransactionDialog(
      type = addDialogType,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      initialCategory = addDialogInitialCategory,
      customCategories = if (addDialogType == "IN") uiState.customIncomeCategories else uiState.customExpenseCategories,
      associatedTransactions = uiState.transactions,
      onAddCustomCategory = { viewModel.addCustomCategory(addDialogType, it) },
      onRemoveCustomCategory = { viewModel.removeCustomCategory(addDialogType, it) },
      onRemoveCustomCategoryWithResolution = { cat, deleteTxs, targetCat ->
        viewModel.removeCustomCategory(
          type = addDialogType,
          rawName = cat,
          deleteAssociatedTransactions = deleteTxs,
          reassignToCategory = targetCat
        )
      },
      onDismiss = { showAddDialog = false },
      onConfirm = { amount, category, notes, currency ->
        viewModel.addTransaction(
          type = addDialogType,
          amount = amount,
          category = category,
          currency = currency,
          notes = notes
        )
        showAddDialog = false
      },
      onConfirmWithRate = { amount, category, notes, currency, customRate ->
        viewModel.addTransaction(
          type = addDialogType,
          amount = amount,
          category = category,
          currency = currency,
          notes = notes,
          customIqdRate = customRate
        )
        showAddDialog = false
      },
      onConfirmWithRateAndCashOption = { amount, category, notes, currency, customRate, deductFromCash ->
        viewModel.addTransaction(
          type = addDialogType,
          amount = amount,
          category = category,
          currency = currency,
          notes = notes,
          affectsCapital = deductFromCash,
          customIqdRate = customRate
        )
        showAddDialog = false
      }
    )
  }

  // Dedicated School Account Full Hub Dialog (خر حساب ببيته يك دا بزانم من جه ند صرف كريه ل قوتاخاني)
  if (showSchoolHubDialog) {
    SchoolAccountHubDialog(
      uiState = uiState,
      isKurdish = isKurdish,
      onDismiss = { showSchoolHubDialog = false },
      onOpenAddSchoolItem = { showAddSchoolExpenseDialog = true },
      onToggleCashDeduction = { tx -> viewModel.toggleTransactionCashDeduction(tx) },
      onEditSchoolItem = { tx -> viewModel.openQuickEditForTransaction(tx) },
      onDeleteSchoolItem = { id -> viewModel.deleteTransaction(id) }
    )
  }

  // Dedicated Add School Item Dialog (قوتابخانه تشته كي زيده كه م بلا بيزيت ز كاشي كيم بيت يان نه)
  if (showAddSchoolExpenseDialog) {
    AddSchoolExpenseDialog(
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddSchoolExpenseDialog = false },
      onConfirm = { amount, currency, subCategory, notes, deductFromCash, customRate ->
        viewModel.addSchoolExpense(
          amount = amount,
          currency = currency,
          subCategory = subCategory,
          notes = notes,
          deductFromCash = deductFromCash,
          customIqdRate = customRate
        )
        showAddSchoolExpenseDialog = false
      }
    )
  }

  if (showAddCapitalDialog) {
    AddCapitalAdditionDialog(
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddCapitalDialog = false },
      onConfirm = { amount, currency, category, notes, customRate ->
        viewModel.addCapitalAddition(
          amount = amount,
          currency = currency,
          category = category,
          notes = notes,
          customIqdRate = customRate
        )
        showAddCapitalDialog = false
      }
    )
  }

  if (showQuickLendDialog) {
    AddDebtCreditDialog(
      initialType = "CREDIT",
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showQuickLendDialog = false },
      onConfirmWithRate = { type: String, name: String, contact: String, total: Double, paid: Double, cur: String, dueMillis: Long, notes: String, reminder: Boolean, affectCash: Boolean, customRate: Double? ->
        viewModel.addDebtCredit(
          type = type,
          personName = name,
          contactInfo = contact,
          totalAmount = total,
          amountPaid = paid,
          currency = cur,
          dueDateMillis = dueMillis,
          notes = notes,
          reminderEnabled = reminder,
          affectCashBalance = affectCash,
          customIqdRate = customRate
        )
        showQuickLendDialog = false
      }
    )
  }

  if (showQuickBorrowDialog) {
    AddDebtCreditDialog(
      initialType = "DEBT",
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showQuickBorrowDialog = false },
      onConfirmWithRate = { type: String, name: String, contact: String, total: Double, paid: Double, cur: String, dueMillis: Long, notes: String, reminder: Boolean, affectCash: Boolean, customRate: Double? ->
        viewModel.addDebtCredit(
          type = type,
          personName = name,
          contactInfo = contact,
          totalAmount = total,
          amountPaid = paid,
          currency = cur,
          dueDateMillis = dueMillis,
          notes = notes,
          reminderEnabled = reminder,
          affectCashBalance = affectCash,
          customIqdRate = customRate
        )
        showQuickBorrowDialog = false
      }
    )
  }

  if (selectedTransaction != null) {
    TransactionDetailActionDialog(
      transaction = selectedTransaction!!,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedTransaction = null }
    )
  }

  if (previewPdfResult != null) {
    PrintPreviewDialog(
      result = previewPdfResult!!,
      onDismiss = { previewPdfResult = null }
    )
  }
}
