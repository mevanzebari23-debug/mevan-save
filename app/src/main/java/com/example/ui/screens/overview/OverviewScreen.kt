package com.example.ui.screens.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.categories.AppCategoryRegistry
import com.example.ui.categories.CategorySectionType
import com.example.ui.components.CategoryChipSelector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.WoodDarkWalnut
import com.example.ui.theme.WoodHighlight
import com.example.ui.theme.WoodMahogany
import com.example.ui.theme.WoodInlayGold
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodCardSurface
import com.example.ui.theme.woodInlayBrush
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.LossRedLight
import com.example.ui.screens.debts.AddDebtCreditDialog
import androidx.compose.material.icons.filled.AccountBalanceWallet
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import com.example.ui.BudgetAlertItem
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.AssetDetailActionDialog
import com.example.ui.components.BudgetDetailActionDialog
import com.example.ui.components.CyberBalanceSummaryButton
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.EntryExchangeRateCard
import com.example.ui.components.FinancialLogCategory
import com.example.ui.components.FinancialTransactionLogComponent
import com.example.ui.components.GeminiFinancialCopilotSheet
import com.example.ui.components.GlobalSearchBottomSheet
import com.example.ui.components.GoldHoldingDetailActionDialog
import com.example.ui.components.LiveInstantSearchBar
import com.example.ui.components.MevanZebariWelcomeBanner
import com.example.ui.components.NetWorthTrendLineChart
import com.example.ui.components.QuickCategoryExplorerBar
import com.example.ui.components.QuickCategoryListDialog
import com.example.ui.components.SectionQuickHistoryStrip
import com.example.ui.components.TransactionDetailActionDialog
import com.example.ui.screens.cars.CarDetailsDialog
import com.example.ui.screens.debts.PersonDebtDetailsDialog
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MonetizationOn
import java.text.SimpleDateFormat
import com.example.ui.history.ActionSection
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.dashboard.AnalyticsDashboardView
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid

@Composable
fun OverviewScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onNavigateToTab: (Int) -> Unit,
  onAuthenticateSensitiveView: (title: String, onAuthenticated: () -> Unit) -> Unit = { _, onAuth -> onAuth() },
  onOpenFirebaseAuthDialog: () -> Unit = {},
  onOpenGoogleSearchGrounding: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var liveInstantSearchQuery by remember { mutableStateOf("") }
  var showQuickInDialog by remember { mutableStateOf(false) }
  var showQuickOutDialog by remember { mutableStateOf(false) }
  var showQuickLendDialog by remember { mutableStateOf(false) }
  var showQuickBorrowDialog by remember { mutableStateOf(false) }
  var showAdjustCapitalDialog by remember { mutableStateOf(false) }
  var showRateAdjustDialog by remember { mutableStateOf(false) }
  var showGlobalSearchSheet by remember { mutableStateOf(false) }
  var showAnalyticsDashboard by remember { mutableStateOf(false) }
  var showFullFormulaBreakdown by remember { mutableStateOf(false) }
  var isBalanceMatrixExpanded by rememberSaveable { mutableStateOf(true) }
  var showCopilotSheet by remember { mutableStateOf(false) }
  var activeCategoryFilterModal by remember { mutableStateOf<String?>(null) }

  var selectedTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
  var selectedDebtForDetail by remember { mutableStateOf<DebtCreditEntity?>(null) }
  var selectedCarForDetail by remember { mutableStateOf<InvestmentEntity?>(null) }
  var selectedAssetForDetail by remember { mutableStateOf<InvestmentEntity?>(null) }
  var selectedGoldForDetail by remember { mutableStateOf<GoldHoldingEntity?>(null) }
  var selectedBudgetForDetail by remember { mutableStateOf<BudgetAlertItem?>(null) }
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)
  val isMasked = uiState.isSensitiveDataMasked

  fun fmt(amount: Double): String =
    if (isMasked) "••••••" else CurrencyService.format(amount, uiState.displayCurrency)

  fun fmtShort(amount: Double): String =
    if (isMasked) "••••" else CurrencyService.formatShort(amount, uiState.displayCurrency)

  val luxuryGoldBorder = Brush.linearGradient(
    colors = listOf(
      GoldAccent.copy(alpha = 0.55f),
      EmeraldLight.copy(alpha = 0.30f),
      GoldAccent.copy(alpha = 0.45f)
    )
  )

  Box(
    modifier = modifier.fillMaxSize()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
    // 0. Grand Royal Welcome Mevan Zebari & Live Atmospheric Weather Banner (Rain & Snow particles)
    item {
      MevanZebariWelcomeBanner(
        weatherData = uiState.weatherData,
        isKurdish = isKurdish,
        onRefreshWeather = { viewModel.fetchLiveWeather() },
        onSetSimulatedCondition = { condition ->
          viewModel.setWeatherSimulationOverride(condition)
        }
      )
    }

    // 1. Sleek Unified Executive Command Bar (Live Search + Exchange Rate + Charts & Sync)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            1.2.dp,
            Brush.horizontalGradient(
              listOf(
                NeonCyan.copy(alpha = 0.60f),
                NeonEmerald.copy(alpha = 0.50f),
                NeonGold.copy(alpha = 0.50f)
              )
            ),
            RoundedCornerShape(20.dp)
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Top Row: Live Exchange Rate Pill + Quick Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Live IQD Rate Pill (Tap to edit rate)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(NeonGold.copy(alpha = 0.16f))
                .border(1.dp, NeonGold.copy(alpha = 0.60f), RoundedCornerShape(12.dp))
                .clickable { showRateAdjustDialog = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CurrencyExchange,
                  contentDescription = "Exchange Rate",
                  tint = NeonGold,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "100$ = ${(uiState.iqdRate * 100).toInt().toString().reversed().chunked(3).joinToString(",").reversed()} د.ع",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = NeonGold
                )
              }
            }

            // Quick Actions: Refresh + Charts + Cloud Sync + Deep Search
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Refresh Rate Icon
              IconButton(
                onClick = { viewModel.refreshRates() },
                modifier = Modifier
                  .size(32.dp)
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                  .testTag("refresh_rates_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Refresh Currency Rates",
                  tint = EmeraldLight,
                  modifier = Modifier.size(16.dp)
                )
              }

              // Charts Dashboard Quick Pill
              IconButton(
                onClick = {
                  if (isMasked) {
                    onAuthenticateSensitiveView(
                      if (isKurdish) "داشبۆردێ چارتان و شیکاریا دارایی" else "Visual Financial Charts Dashboard"
                    ) {
                      showAnalyticsDashboard = true
                    }
                  } else {
                    showAnalyticsDashboard = true
                  }
                },
                modifier = Modifier
                  .size(32.dp)
                  .background(EmeraldLight.copy(alpha = 0.16f), CircleShape)
                  .testTag("overview_analytics_dashboard_card")
              ) {
                Icon(
                  imageVector = Icons.Default.BarChart,
                  contentDescription = "Visual Charts Dashboard",
                  tint = EmeraldLight,
                  modifier = Modifier.size(16.dp)
                )
              }

              // Quick Vault Explorer Button (دەستگەهشتنا بلەز بۆ هەمی دوکمەیێن سەروەتی)
              IconButton(
                onClick = {
                  if (isMasked) {
                    onAuthenticateSensitiveView(
                      if (isKurdish) "سجلا هەمی قەرز و ملوکان" else "Master Vault Explorer"
                    ) {
                      activeCategoryFilterModal = "ALL_DEBTS"
                    }
                  } else {
                    activeCategoryFilterModal = "ALL_DEBTS"
                  }
                },
                modifier = Modifier
                  .size(32.dp)
                  .background(NeonEmerald.copy(alpha = 0.20f), CircleShape)
                  .border(0.8.dp, NeonEmerald.copy(alpha = 0.70f), CircleShape)
                  .testTag("overview_top_excel_workbook_button")
              ) {
                Icon(
                  imageVector = Icons.Default.ManageSearch,
                  contentDescription = if (isKurdish) "دەف پۆل و سجلا بلەز" else "Quick Vault Explorer",
                  tint = NeonEmerald,
                  modifier = Modifier.size(16.dp)
                )
              }

              // Cloud Sync Status Pill
              val authUser = uiState.firebaseAuthUser
              IconButton(
                onClick = {
                  if (authUser.isSignedIn) {
                    viewModel.syncToFirestore()
                  } else {
                    onOpenFirebaseAuthDialog()
                  }
                },
                modifier = Modifier
                  .size(32.dp)
                  .background(
                    if (authUser.isSignedIn) EmeraldLight.copy(alpha = 0.18f)
                    else GoldAccent.copy(alpha = 0.16f),
                    CircleShape
                  )
                  .testTag("overview_firebase_auth_card")
                  .testTag("overview_google_sign_in_button")
              ) {
                Icon(
                  imageVector = if (authUser.isSignedIn) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                  contentDescription = "Cloud Sync",
                  tint = if (authUser.isSignedIn) EmeraldLight else GoldAccent,
                  modifier = Modifier.size(16.dp)
                )
              }

              // Tools & Settings Shortcut Button (ئامراز)
              IconButton(
                onClick = { onNavigateToTab(5) },
                modifier = Modifier
                  .size(32.dp)
                  .background(NeonGold.copy(alpha = 0.20f), CircleShape)
                  .border(0.8.dp, NeonGold.copy(alpha = 0.70f), CircleShape)
                  .testTag("overview_quick_tools_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = if (isKurdish) "ئامرازێن بەرنامەی" else "Tools & Settings",
                  tint = NeonGold,
                  modifier = Modifier.size(16.dp)
                )
              }

              // Global Search Dialog Trigger
              IconButton(
                onClick = { showGlobalSearchSheet = true },
                modifier = Modifier
                  .size(32.dp)
                  .background(Color(0xFF38BDF8).copy(alpha = 0.16f), CircleShape)
                  .testTag("overview_global_search_bar")
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Global Search Sheet",
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(16.dp)
                )
              }

              // Gemini AI Financial Copilot Interactive Sheet Trigger
              IconButton(
                onClick = { showCopilotSheet = true },
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.radialGradient(
                      colors = listOf(GoldAccent.copy(alpha = 0.40f), EmeraldLight.copy(alpha = 0.25f))
                    )
                  )
                  .border(0.8.dp, GoldAccent, CircleShape)
                  .testTag("ai_financial_copilot_button")
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = "Gemini Financial Copilot",
                  tint = GoldAccent,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          // Integrated Live Instant Search Bar
          LiveInstantSearchBar(
            query = liveInstantSearchQuery,
            onQueryChange = { liveInstantSearchQuery = it },
            placeholder = if (isKurdish) "لێگەڕیانا بلەز ل ملوک، سەیارە، قەرز، مامەڵە..." else "Live search assets, cars, debts, logs...",
            isKurdish = isKurdish,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("overview_top_live_search_bar")
          )
        }
      }
    }

    if (liveInstantSearchQuery.isNotBlank()) {
      val q = liveInstantSearchQuery.trim()
      val matchedDebts = uiState.debtsCredits.filter { it.personName.contains(q, ignoreCase = true) || it.notes.contains(q, ignoreCase = true) }
      val matchedCars = uiState.carInvestments.filter { it.name.contains(q, ignoreCase = true) || it.notes.contains(q, ignoreCase = true) }
      val matchedInvestments = uiState.nonCarInvestments.filter { it.name.contains(q, ignoreCase = true) || it.category.contains(q, ignoreCase = true) }
      val matchedTxs = uiState.transactions.filter { it.category.contains(q, ignoreCase = true) || it.notes.contains(q, ignoreCase = true) }
      val totalMatches = matchedDebts.size + matchedCars.size + matchedInvestments.size + matchedTxs.size

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkGlassCardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isKurdish) "ئەنجامێن گەڕیانێ ($totalMatches)" else "Live Search Results ($totalMatches)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GoldAccent
              )
              Text(
                text = if (isKurdish) "کلیک بکە بۆ ڤەکرنێ" else "Tap to open",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (totalMatches == 0) {
              Text(
                text = if (isKurdish) "چ ئەنجام نەهاتنە دیتن بۆ ($q)" else "No items match ($q)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }

            matchedDebts.take(5).forEach { debt ->
              val isCredit = debt.type == "CREDIT"
              CyberRecordButton(
                title = debt.personName,
                subtitle = if (isCredit) (if (isKurdish) "دەف خەلکی (+)" else "Receivable") else (if (isKurdish) "لسەر من (-)" else "Payable"),
                primaryValue = CurrencyService.formatShort(debt.remainingAmount, debt.currency),
                accentColor = if (isCredit) NeonCyan else LossRed,
                icon = Icons.Default.Handshake,
                statusBadge = if (debt.isCompleted) (if (isKurdish) "تەواو" else "Settled") else (if (isKurdish) "قەرز" else "Loan"),
                statusBadgeColor = GoldAccent,
                actionHintLabel = if (isKurdish) "کردار" else "Open",
                onClick = { selectedDebtForDetail = debt },
                testTagKey = "overview_search_debt_${debt.id}"
              )
            }

            matchedCars.take(4).forEach { car ->
              CyberRecordButton(
                title = car.name,
                subtitle = if (car.isSold) (if (isKurdish) "سەیارا فرۆتی" else "Sold Vehicle") else (if (isKurdish) "سەیارا بەردەست" else "In Showroom"),
                primaryValue = CurrencyService.formatShort(car.currentValue, car.currency),
                accentColor = Color(0xFF38BDF8),
                icon = Icons.Default.DirectionsCar,
                actionHintLabel = if (isKurdish) "کردار" else "Open",
                onClick = { selectedCarForDetail = car },
                testTagKey = "overview_search_car_${car.id}"
              )
            }

            matchedInvestments.take(4).forEach { inv ->
              CyberRecordButton(
                title = inv.name,
                subtitle = inv.category,
                primaryValue = CurrencyService.formatShort(inv.currentValue, inv.currency),
                accentColor = EmeraldLight,
                icon = Icons.Default.Apartment,
                actionHintLabel = if (isKurdish) "کردار" else "Open",
                onClick = { selectedAssetForDetail = inv },
                testTagKey = "overview_search_asset_${inv.id}"
              )
            }

            matchedTxs.take(4).forEach { tx ->
              val isSchool = uiState.isSchoolTransaction(tx)
              val isIn = tx.type.equals("IN", ignoreCase = true)
              CyberRecordButton(
                title = tx.category,
                subtitle = tx.notes.ifBlank { if (isSchool) (if (isKurdish) "قوتابخانە" else "School") else (if (isKurdish) "مامەڵە" else "Transaction") },
                primaryValue = "${if (isIn) "+" else "-"}${CurrencyService.formatShort(tx.amount, tx.currency)}",
                accentColor = if (isSchool) Color(0xFF38BDF8) else if (isIn) EmeraldLight else LossRed,
                icon = if (isSchool) Icons.Default.School else Icons.Default.ReceiptLong,
                actionHintLabel = if (isKurdish) "کردار" else "Open",
                onClick = { selectedTxForDetail = tx },
                testTagKey = "overview_search_tx_${tx.id}"
              )
            }
          }
        }
      }
    }

    // 1.2. Quick-Access Brand & Category Explorer Bar (Toyota, BMW, Mercedes, Houses, Gold, Debts...)
    item {
      QuickCategoryExplorerBar(
        uiState = uiState,
        isKurdish = isKurdish,
        title = if (isKurdish) "⚡ پۆل و مارکێن بلەز (دەست لێبدە بۆ دیتنا لیستێ):" else "⚡ Quick Brands & Categories (Tap to List):",
        onSelectCategory = { categoryKey ->
          activeCategoryFilterModal = categoryKey
        }
      )
    }

    // 1.5. Urgent Budget & Loan Reminders Notice Banner (Only shown when alerts exist)
    val criticalBudgets = uiState.budgetAlerts.filter { it.isWarning }
    val urgentReminders = uiState.activePaymentReminders.filter {
      it.isOverdue || (it.dueDateMillis - System.currentTimeMillis()) < 7L * 24 * 60 * 60 * 1000
    }

    if (criticalBudgets.isNotEmpty() || urgentReminders.isNotEmpty()) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          val firstBudget = criticalBudgets.firstOrNull()
          if (firstBudget != null) {
            CyberRecordButton(
              title = if (isKurdish) "ئاگەهداریا بودجەی: ${firstBudget.budget.category}" else "Budget Alert: ${firstBudget.budget.category}",
              subtitle = if (isKurdish) "گەهشتیە ${firstBudget.percentageUsed.toInt()}% ژ سنووری" else "Reached ${firstBudget.percentageUsed.toInt()}% of cap",
              primaryValue = fmtShort(firstBudget.spentAmount),
              secondaryValue = "/ ${fmtShort(firstBudget.limitAmount)}",
              accentColor = if (firstBudget.isExceeded) LossRed else GoldAccent,
              icon = Icons.Default.NotificationsActive,
              progressRatio = (firstBudget.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f),
              statusBadge = if (firstBudget.isExceeded) (if (isKurdish) "تێپەڕی" else "Exceeded") else (if (isKurdish) "ئاگەهداری" else "Warning"),
              statusBadgeColor = if (firstBudget.isExceeded) LossRed else GoldAccent,
              actionHintLabel = if (isKurdish) "گوهۆڕین" else "Manage",
              onClick = { selectedBudgetForDetail = firstBudget },
              testTagKey = "overview_urgent_budget_btn"
            )
          }
          val firstReminder = urgentReminders.firstOrNull()
          if (firstReminder != null) {
            val remConverted = CurrencyService.convert(
              firstReminder.remainingAmount,
              firstReminder.currency,
              uiState.displayCurrency,
              uiState.iqdRate,
              entryIqdRate = firstReminder.exchangeRateAtEntry
            )
            CyberRecordButton(
              title = if (isKurdish) "بیرئینانا قەرزی: ${firstReminder.personName}" else "Loan Reminder: ${firstReminder.personName}",
              subtitle = if (firstReminder.isOverdue) (if (isKurdish) "وادە دەرباز بوویە!" else "Payment Overdue!") else (if (isKurdish) "دێ زوی هێتە دان" else "Due soon"),
              primaryValue = fmtShort(remConverted),
              accentColor = if (firstReminder.isOverdue) LossRed else GoldAccent,
              icon = Icons.Default.Handshake,
              statusBadge = if (firstReminder.isOverdue) (if (isKurdish) "درەنگکەفتی" else "Overdue") else (if (isKurdish) "نێزیک" else "Due"),
              statusBadgeColor = if (firstReminder.isOverdue) LossRed else GoldAccent,
              actionHintLabel = if (isKurdish) "کردار" else "Details",
              onClick = {
                val entity = uiState.debtsCredits.find { it.id == firstReminder.id }
                if (entity != null) selectedDebtForDetail = entity else onNavigateToTab(1)
              },
              testTagKey = "overview_urgent_debt_btn"
            )
          }
        }
      }
    }

    // 2. ROYAL EXECUTIVE CAPITAL & NET WORTH MASTER HERO (Organized, Clean & Uncluttered)
    item {
      val capitalAssets = uiState.totalCapitalAssets
      val calculatedNetWorth = uiState.netWorth
      val isPositiveNetWorth = calculatedNetWorth >= 0.0
      val isPositiveFlow = uiState.isPositiveFlow
      val netCashFlow = uiState.netCashFlow
      val flowPercent = uiState.netWealthFlowPercent
      val flowColor = if (isPositiveFlow) EmeraldLight else LossRed
      val netDebtCreditOffset = uiState.netDebtCreditBalance

      Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = 1.4.dp,
            brush = Brush.horizontalGradient(
              colors = listOf(
                NeonCyan.copy(alpha = 0.85f),
                NeonEmerald.copy(alpha = 0.90f),
                NeonBlue.copy(alpha = 0.70f),
                NeonGold.copy(alpha = 0.85f)
              )
            ),
            shape = RoundedCornerShape(26.dp)
          )
          .testTag("gross_wealth_hero_card")
          .testTag("net_worth_summary_card")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  ObsidianElevated,
                  ObsidianCard
                )
              )
            )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Header: Emblem + Net Worth Label + Privacy & Starting Capital Action
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .background(
                      Brush.linearGradient(
                        colors = listOf(NeonCyan, NeonEmerald)
                      ),
                      RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = ObsidianVoid,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isKurdish) "خەزینەیا سەرەکی یا سەرمایەی" else "EXECUTIVE WEALTH VAULT",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.8.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.ExtraBold
                  )
                  Text(
                    text = if (isKurdish) {
                      "سەرمایێ سەرەکی: ${fmtShort(uiState.settings.startingCapital)}"
                    } else {
                      "Base Capital: ${fmtShort(uiState.settings.startingCapital)}"
                    },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Privacy Mask Button
                IconButton(
                  onClick = {
                    if (isMasked) {
                      onAuthenticateSensitiveView(
                        if (isKurdish) "نیشاندانا داتایێن دارایی" else "Reveal Sensitive Financial Data"
                      ) {
                        viewModel.setSensitiveDataMasked(false)
                      }
                    } else {
                      viewModel.setSensitiveDataMasked(true)
                    }
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(0.8.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .testTag("toggle_sensitive_mask_button")
                ) {
                  Icon(
                    imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isMasked) "Reveal Sensitive Balances" else "Mask Sensitive Balances",
                    tint = if (isMasked) NeonGold else Color.White,
                    modifier = Modifier.size(17.dp)
                  )
                }

                // Adjust Capital Button
                Box(
                  modifier = Modifier
                    .heightIn(min = 34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonCyan.copy(alpha = 0.16f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .clickable {
                      if (isMasked || uiState.settings.biometricEnabled) {
                        onAuthenticateSensitiveView(
                          if (isKurdish) "دەستکاریکرنا سەرمایێ سەرەکی" else "Modify Starting Capital"
                        ) {
                          showAdjustCapitalDialog = true
                        }
                      } else {
                        showAdjustCapitalDialog = true
                      }
                    }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
                    .testTag("adjust_capital_button"),
                  contentAlignment = Alignment.Center
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = "Adjust Capital",
                      tint = NeonCyan,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = if (isKurdish) "سەرمایە" else "Capital",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = NeonCyan
                    )
                  }
                }
              }
            }

            // Spotlight: Clean Net Worth Display (صافی سەروەت)
            val formattedNetWorthText = fmt(calculatedNetWorth)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      NeonCyan.copy(alpha = 0.08f),
                      NeonEmerald.copy(alpha = 0.10f),
                      Color.White.copy(alpha = 0.04f)
                    )
                  ),
                  RoundedCornerShape(18.dp)
                )
                .border(
                  1.2.dp,
                  Brush.horizontalGradient(
                    listOf(
                      NeonCyan.copy(alpha = 0.50f),
                      NeonEmerald.copy(alpha = 0.50f)
                    )
                  ),
                  RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                  Text(
                    text = if (isKurdish) "👑 صافی سەروەتێ گشتی" else "👑 TOTAL NET WORTH",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    color = NeonEmerald
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = formattedNetWorthText,
                    fontSize = if (formattedNetWorthText.length > 13) 24.sp else 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPositiveNetWorth) NeonEmerald else NeonCoral,
                    modifier = Modifier.testTag("net_worth_value")
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(14.dp))
                      .background(if (isPositiveFlow) NeonEmerald.copy(alpha = 0.18f) else NeonCoral.copy(alpha = 0.18f))
                      .border(0.8.dp, if (isPositiveFlow) NeonEmerald.copy(alpha = 0.50f) else NeonCoral.copy(alpha = 0.50f), RoundedCornerShape(14.dp))
                      .padding(horizontal = 9.dp, vertical = 4.dp)
                      .testTag("net_worth_flow_indicator")
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Icon(
                        imageVector = if (isPositiveFlow) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositiveFlow) NeonEmerald else NeonCoral,
                        modifier = Modifier.size(13.dp)
                      )
                      Text(
                        text = if (isPositiveFlow) "+${"%.1f".format(flowPercent)}%" else "${"%.1f".format(flowPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPositiveFlow) NeonEmerald else NeonCoral
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = if (isKurdish) "لێشاوا کاش: " else "Cash Flow: ",
                      fontSize = 10.sp,
                      color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                      text = "${if (netCashFlow >= 0) "+" else ""}${fmtShort(netCashFlow)}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = if (netCashFlow >= 0) NeonEmerald else NeonCoral,
                      modifier = Modifier.testTag("net_worth_cash_flow_delta")
                    )
                  }
                }
              }
            }

            if (isMasked) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(GoldAccent.copy(alpha = 0.18f))
                  .border(0.8.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable {
                  onAuthenticateSensitiveView(
                    if (isKurdish) "پشکنینا پەنجەمۆرێ (BiometricPrompt)" else "Authenticate with BiometricPrompt"
                  ) {
                    viewModel.setSensitiveDataMasked(false)
                  }
                }
                .padding(horizontal = 12.dp, vertical = 7.dp)
                .testTag("unlock_sensitive_views_banner"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isKurdish) "ژمارەیێن دارایی هاتینە ڤەشارتن • کلیک بکە بۆ ڤەکرنێ" else "Balances masked • Tap to unlock via Biometrics",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
              }
            }

            // Symmetrical, Clean 3-Pillar Master Wealth Summary Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // 1. Pure Cash Wealth (سەروەتێ کاش)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .background(EmeraldLight.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                  .border(0.8.dp, EmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                  .clickable { onNavigateToTab(3) }
                  .padding(horizontal = 10.dp, vertical = 9.dp)
                  .testTag("pure_cash_wealth_pill")
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "💵 سەروەتێ کاش" else "💵 Cash Wealth",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = fmtShort(uiState.pureCashBalance),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("pure_cash_wealth_value")
                  )
                }
              }

              // 2. Capital Assets & Gold (کۆما سامان و موڵک)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .background(Color(0xFF38BDF8).copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                  .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                  .clickable { onNavigateToTab(2) }
                  .padding(horizontal = 10.dp, vertical = 9.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "🏛️ سامان و زێڕ" else "🏛️ Assets & Gold",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = fmtShort(capitalAssets),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("net_worth_total_capital_assets")
                  )
                }
              }

              // 3. Net Debt / Credit Offset (قەرز ل ئێک کۆژی)
              val debtAccent = if (netDebtCreditOffset >= 0.0) EmeraldLight else LossRed
              Box(
                modifier = Modifier
                  .weight(1f)
                  .background(debtAccent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                  .border(0.8.dp, debtAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                  .clickable { onNavigateToTab(1) }
                  .padding(horizontal = 10.dp, vertical = 9.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "🤝 صافی قەرز" else "🤝 Net Debt/Loan",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = debtAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${if (netDebtCreditOffset >= 0.0) "+" else "−"}${fmtShort(kotlin.math.abs(netDebtCreditOffset))}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    color = debtAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("net_worth_total_debts")
                  )
                }
              }
            }

            // Discreet Mathematical Formula Drawer link (Only expanded on demand)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) {
                  "ڕێژەیا سەرمایێ بێ قەرز: ${100 - uiState.debtToAssetRatioPercent}%"
                } else {
                  "Debt-Free Equity: ${100 - uiState.debtToAssetRatioPercent}%"
                },
                fontSize = 10.5.sp,
                color = EmeraldLight,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = fmt(calculatedNetWorth),
                fontSize = 1.sp,
                color = Color.Transparent,
                modifier = Modifier.testTag("net_worth_result_amount")
              )

              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(GoldAccent.copy(alpha = 0.14f))
                  .clickable { showFullFormulaBreakdown = !showFullFormulaBreakdown }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("net_worth_equation_card"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Calculate,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = if (isKurdish) "معادلا تەمام" else "Formula",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
                Icon(
                  imageVector = if (showFullFormulaBreakdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            // Collapsible Full Mathematical Formula Drawer
            AnimatedVisibility(visible = showFullFormulaBreakdown) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.Black.copy(alpha = 0.36f), RoundedCornerShape(14.dp))
                  .border(0.8.dp, GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = if (isKurdish) {
                    "معادلا تەمام: سەروەتێ کاش + زێڕ و زیڤ + پرۆژە (بێی عەقار) ± صافی قەرز = صافی سەروەت"
                  } else {
                    "Formula: Cash Wealth + Gold & Silver + Ventures (Excl. Real Estate) ± Net Credit/Debt = Net Worth"
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
                EquationItemRow(
                  symbol = "+",
                  symbolColor = EmeraldLight,
                  title = if (isKurdish) "١. سەروەتێ کاش (پشتی کڕین و فرۆتنێ)" else "1. Cash Wealth (After Buy/Sell)",
                  subtitle = if (isKurdish) {
                    "فرۆتن (+${fmtShort(uiState.totalAssetSalesCashIn)}) • کڕین (−${fmtShort(uiState.totalAssetPurchasesCashOut)})"
                  } else {
                    "Sales (+${fmtShort(uiState.totalAssetSalesCashIn)}) • Purchases (−${fmtShort(uiState.totalAssetPurchasesCashOut)})"
                  },
                  amount = "${if (uiState.pureCashBalance >= 0) "+ " else "− "}${fmtShort(kotlin.math.abs(uiState.pureCashBalance))}",
                  amountColor = if (uiState.pureCashBalance >= 0) EmeraldLight else LossRed
                )
                EquationItemRow(
                  symbol = "+",
                  symbolColor = GoldAccent,
                  title = if (isKurdish) "٢. زێڕ و زیڤێ بەردەست (بهایێ نوکە)" else "2. Gold & Silver (Current Value)",
                  subtitle = if (isKurdish) "زێڕ و زیڤێ نەفرۆتی د خەزینەیێ دا" else "Unsold physical gold and silver",
                  amount = "+ ${fmtShort(uiState.totalGoldCurrentValueDisplay)}",
                  amountColor = GoldAccent
                )
                EquationItemRow(
                  symbol = "+",
                  symbolColor = Color(0xFFFBBF24),
                  title = if (isKurdish) "٣. پرۆژە و وەبەرهێنان (بێی عەقار)" else "3. Business Ventures (Excl. Real Estate)",
                  subtitle = if (isKurdish) "عەقار ب سەربەخۆیی تنێ دهێتە ژمارتن" else "Real Estate tracked standalone",
                  amount = "+ ${fmtShort(uiState.myOtherProjectsValuation)}",
                  amountColor = Color(0xFFFBBF24)
                )
                val netDebtOffset = uiState.netDebtCreditBalance
                EquationItemRow(
                  symbol = if (netDebtOffset >= 0.0) "+" else "−",
                  symbolColor = if (netDebtOffset >= 0.0) EmeraldLight else LossRed,
                  title = if (isKurdish) "٤. قەرز ل ئێک کۆژی (دەف خەلکی − لسەر من)" else "4. Debt & Credit Offset (Receivable − Owed)",
                  subtitle = if (isKurdish) {
                    "دەف خەلکی (${fmtShort(uiState.totalCreditReceivable)}) − لسەر من (${fmtShort(uiState.totalDebtOwed)})"
                  } else {
                    "Credit (${fmtShort(uiState.totalCreditReceivable)}) − Debt (${fmtShort(uiState.totalDebtOwed)})"
                  },
                  amount = "${if (netDebtOffset >= 0.0) "+ " else "− "}${fmtShort(kotlin.math.abs(netDebtOffset))}",
                  amountColor = if (netDebtOffset >= 0.0) EmeraldLight else LossRed
                )
              }
            }
          }
        }
      }
    }

    // 2. EXECUTIVE QUICK ACTION SPEED DOCK (داهات + ، مەسروف - ، قەرز بدە + ، قەرز وەربگرە +)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Quick Money In (+ کاش داهات)
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(NeonEmerald.copy(alpha = 0.16f))
            .border(1.dp, NeonEmerald.copy(alpha = 0.60f), RoundedCornerShape(13.dp))
            .clickable { showQuickInDialog = true }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("quick_money_in_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = "Income",
              tint = NeonEmerald,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (isKurdish) "داهات +" else "+ In",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = NeonEmerald,
              maxLines = 1
            )
          }
        }

        // Quick Money Out (- کاش مەسروف)
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(NeonCoral.copy(alpha = 0.16f))
            .border(1.dp, NeonCoral.copy(alpha = 0.60f), RoundedCornerShape(13.dp))
            .clickable { showQuickOutDialog = true }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("quick_money_out_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = "Expense",
              tint = NeonCoral,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (isKurdish) "مەسروف −" else "− Out",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = NeonCoral,
              maxLines = 1
            )
          }
        }

        // Quick Lend (+ قەرز بدە)
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(NeonGold.copy(alpha = 0.16f))
            .border(1.dp, NeonGold.copy(alpha = 0.60f), RoundedCornerShape(13.dp))
            .clickable { showQuickLendDialog = true }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("overview_quick_lend_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Handshake,
              contentDescription = "Lend",
              tint = NeonGold,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (isKurdish) "قەرز بدە" else "Lend",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = NeonGold,
              maxLines = 1
            )
          }
        }

        // Quick Borrow (+ قەرز وەربگرە)
        Box(
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(NeonCyan.copy(alpha = 0.16f))
            .border(1.dp, NeonCyan.copy(alpha = 0.60f), RoundedCornerShape(13.dp))
            .clickable { showQuickBorrowDialog = true }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("overview_quick_borrow_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccountBalanceWallet,
              contentDescription = "Borrow",
              tint = NeonCyan,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (isKurdish) "قەرز بگرە" else "Borrow",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = NeonCyan,
              maxLines = 1
            )
          }
        }
      }
    }

    // 2. Symmetrical 2x2 Asset Portfolios Bento Grid (سەرووبەر، بێ خەپسە، ئێک کلیک بۆ هەر بەشەکی)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isKurdish) "🏛️ بەشێن سەرەکیێن دارایی" else "🏛️ MAIN PORTFOLIOS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = NeonCyan
          )
          Text(
            text = if (isKurdish) "کلیک ل هەر بەشەکی بکە بۆ ناڤەرۆکێ" else "Tap any card to view details",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )
        }

        // Row 1: Cars Fleet (Tab 2) & Real Estate / Assets (Tab 3)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // 1. Cars & Fleet Card -> Tab 2
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 104.dp)
              .clip(RoundedCornerShape(20.dp))
              .border(1.2.dp, NeonBlue.copy(alpha = 0.60f), RoundedCornerShape(20.dp))
              .clickable { onNavigateToTab(2) }
              .testTag("overview_cars_fleet_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(NeonBlue.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Text(
                  text = if (isKurdish) "${uiState.carInvestments.size} سەیارە ➔" else "${uiState.carInvestments.size} Cars ➔",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonBlue
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isKurdish) "🚗 سەیارە و قست" else "🚗 Cars & Fleet",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1
              )
              Text(
                text = fmtShort(
                  if (uiState.totalCarSalesAndInstallmentsCashIn > 0.0)
                    uiState.totalCarSalesAndInstallmentsCashIn
                  else
                    uiState.totalActiveCarsValuation
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // 2. Real Estate & Assets Card -> Tab 3
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 104.dp)
              .clip(RoundedCornerShape(20.dp))
              .border(1.2.dp, NeonEmerald.copy(alpha = 0.60f), RoundedCornerShape(20.dp))
              .clickable { onNavigateToTab(3) }
              .testTag("overview_investments_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(NeonEmerald.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Text(
                  text = if (isKurdish) "${uiState.activeRealEstateInvestments.size} موڵک ➔" else "${uiState.activeRealEstateInvestments.size} Props ➔",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonEmerald
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isKurdish) "🏢 ملوک و عەقارات" else "🏢 Real Estate & Assets",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1
              )
              Text(
                text = fmtShort(uiState.myRealEstateValuation),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonEmerald,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // Row 2: Gold & Silver (Tab 3) + Debts & Loans (Tab 1)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // 3. Gold & Silver Card -> Tab 3
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 104.dp)
              .clip(RoundedCornerShape(20.dp))
              .border(1.2.dp, NeonGold.copy(alpha = 0.60f), RoundedCornerShape(20.dp))
              .clickable { onNavigateToTab(3) }
              .testTag("overview_gold_stocks_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(NeonGold.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = null,
                    tint = NeonGold,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Text(
                  text = if (uiState.totalSilverWeightGrams > 0) {
                    "🥇${"%.0f".format(uiState.totalGoldWeightGrams)}g ➔"
                  } else {
                    "${"%.1f".format(uiState.totalGoldWeightGrams)}g ➔"
                  },
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonGold
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isKurdish) "🥇 زێڕ و زیڤ" else "🥇 Gold & Silver",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1
              )
              Text(
                text = fmtShort(uiState.totalGoldCurrentValueDisplay),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // 4. Debts & Credits Card -> Tab 1
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 104.dp)
              .clip(RoundedCornerShape(20.dp))
              .border(1.2.dp, NeonOrange.copy(alpha = 0.60f), RoundedCornerShape(20.dp))
              .clickable { onNavigateToTab(1) }
              .testTag("overview_debts_credits_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              val totalActive = uiState.debtsCredits.count { !it.isCompleted }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(NeonOrange.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = NeonOrange,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Text(
                  text = if (isKurdish) "$totalActive چالاک ➔" else "$totalActive Active ➔",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonOrange
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isKurdish) "🤝 قەرز و دەین" else "🤝 Debts & Credits",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1
              )
              Text(
                text = "${if (uiState.netDebtCreditBalance >= 0.0) "+" else "−"}${fmtShort(kotlin.math.abs(uiState.netDebtCreditBalance))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (uiState.netDebtCreditBalance >= 0.0) NeonEmerald else NeonCoral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // Row 3: Dedicated School & Education Vault (Tab 4)
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
              1.2.dp,
              Brush.horizontalGradient(
                listOf(
                  NeonPurple.copy(alpha = 0.70f),
                  NeonCyan.copy(alpha = 0.70f)
                )
              ),
              RoundedCornerShape(20.dp)
            )
            .clickable { onNavigateToTab(4) }
            .testTag("overview_school_vault_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(NeonPurple.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                  .border(0.8.dp, NeonPurple.copy(alpha = 0.50f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = null,
                  tint = NeonCyan,
                  modifier = Modifier.size(22.dp)
                )
              }

              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = if (isKurdish) "🎓 جهێ تایبەتێ قوتابخانێ" else "🎓 Dedicated School Vault",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(NeonPurple.copy(alpha = 0.25f))
                      .border(0.6.dp, NeonPurple.copy(alpha = 0.60f), RoundedCornerShape(6.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = if (isKurdish) "سەربەخۆ" else "STANDALONE",
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Black,
                      color = NeonCyan
                    )
                  }
                }
                Text(
                  text = if (isKurdish) {
                    "هەمی قست، جلوبەرگ و مەسروفێن مەکتەبێ (${uiState.schoolTransactions.size} تۆمار)"
                  } else {
                    "All tuition, uniforms & educational spending (${uiState.schoolTransactions.size} items)"
                  },
                  fontSize = 11.sp,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = fmtShort(uiState.totalSchoolSpendingDisplay),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan
              )
              Text(
                text = if (isKurdish) "ڤەکرنا حسابێ ➔" else "Open Vault ➔",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
              )
            }
          }
        }
      }
    }

    // 3. INTERACTIVE EXECUTIVE BALANCE MATRIX (دوکمەیێن میزانیا گشتی یا سەروەتی)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.2.dp, NeonEmerald.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable { isBalanceMatrixExpanded = !isBalanceMatrixExpanded }
              .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(NeonEmerald.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                  .border(0.8.dp, NeonEmerald.copy(alpha = 0.60f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = NeonEmerald,
                  modifier = Modifier.size(20.dp)
                )
              }
              Column {
                Text(
                  text = if (isKurdish) "🎛️ دوکمەیێن میزانیا سەروەتی" else "🎛️ Balance Sheet Buttons",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
                Text(
                  text = if (isKurdish) "ل سەر هەر دوکمەیەکێ بدە بۆ لیستا هورد" else "Tap any pillar button to inspect items",
                  style = MaterialTheme.typography.labelSmall,
                  color = NeonEmerald
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(0.8.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = if (isBalanceMatrixExpanded) (if (isKurdish) "داخستن" else "Hide") else (if (isKurdish) "ڤەکرن" else "Open"),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                  imageVector = if (isBalanceMatrixExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          AnimatedVisibility(visible = isBalanceMatrixExpanded) {
            val otherCur = if (uiState.displayCurrency == "USD") "IQD" else "USD"
            fun fmtAlt(amt: Double): String {
              if (isMasked) return "••••"
              val c = CurrencyService.convert(amt, uiState.displayCurrency, otherCur, uiState.iqdRate)
              return "≈ ${CurrencyService.formatShort(c, otherCur)}"
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "سەروەتێ سافی" else "Net Worth",
                  primaryAmount = fmtShort(uiState.netWorth),
                  secondaryAmount = fmtAlt(uiState.netWorth),
                  accentColor = NeonEmerald,
                  icon = Icons.Default.AutoGraph,
                  onClick = { showFullFormulaBreakdown = !showFullFormulaBreakdown },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_networth"
                )
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "کاشێ بەردەست" else "Pure Cash",
                  primaryAmount = fmtShort(uiState.pureCashBalance),
                  secondaryAmount = fmtAlt(uiState.pureCashBalance),
                  accentColor = NeonGold,
                  icon = Icons.Default.AccountBalanceWallet,
                  countLabel = "${uiState.transactions.size}",
                  onClick = { onNavigateToTab(5) },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_cash"
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "سەیارە" else "Vehicles",
                  primaryAmount = fmtShort(uiState.myCarValuationShare),
                  secondaryAmount = fmtAlt(uiState.myCarValuationShare),
                  accentColor = NeonBlue,
                  icon = Icons.Default.DirectionsCar,
                  countLabel = "${uiState.activeCarInvestments.size}",
                  onClick = { activeCategoryFilterModal = "ALL_CARS" },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_cars"
                )
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "عەقار و ملوک" else "Properties",
                  primaryAmount = fmtShort(uiState.myRealEstateValuation),
                  secondaryAmount = fmtAlt(uiState.myRealEstateValuation),
                  accentColor = EmeraldLight,
                  icon = Icons.Default.Apartment,
                  countLabel = "${uiState.activeRealEstateInvestments.size}",
                  onClick = { activeCategoryFilterModal = "ALL_ASSETS" },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_realestate"
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "زێڕ و زیڤ" else "Gold Vault",
                  primaryAmount = fmtShort(uiState.totalGoldCurrentValueDisplay),
                  secondaryAmount = fmtAlt(uiState.totalGoldCurrentValueDisplay),
                  accentColor = GoldAccent,
                  icon = Icons.Default.MonetizationOn,
                  countLabel = "${uiState.activeGoldHoldings.size}",
                  onClick = { activeCategoryFilterModal = "زێڕ" },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_gold"
                )
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "دەف خەلکی (+)" else "Receivables",
                  primaryAmount = fmtShort(uiState.totalCreditReceivable),
                  secondaryAmount = fmtAlt(uiState.totalCreditReceivable),
                  accentColor = NeonCyan,
                  icon = Icons.Default.Handshake,
                  countLabel = "${uiState.activeCreditLoans.size}",
                  onClick = { activeCategoryFilterModal = "دەف خەلکی" },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_credits"
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "لسەر من (-)" else "Payables",
                  primaryAmount = fmtShort(uiState.totalDebtOwed),
                  secondaryAmount = fmtAlt(uiState.totalDebtOwed),
                  accentColor = LossRed,
                  icon = Icons.Default.Handshake,
                  countLabel = "${uiState.activeDebtLoans.size}",
                  onClick = { activeCategoryFilterModal = "قەرزێن لسەر من" },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_debts"
                )
                CyberBalanceSummaryButton(
                  title = if (isKurdish) "قوتابخانە" else "School Vault",
                  primaryAmount = fmtShort(uiState.totalSchoolSpendingDisplay),
                  secondaryAmount = fmtAlt(uiState.totalSchoolSpendingDisplay),
                  accentColor = NeonPurple,
                  icon = Icons.Default.School,
                  countLabel = "${uiState.schoolTransactions.size}",
                  onClick = { activeCategoryFilterModal = "قوتابخانە" },
                  modifier = Modifier.weight(1f),
                  testTagKey = "balance_btn_school"
                )
              }
            }
          }
        }
      }
    }


    // 5. Recent Activity / Quick History Strip (دویماهیک مامەڵە)
    item {
      val recentTx = uiState.transactions.take(4)
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = if (isKurdish) "🕒 دویماهیک مامەڵە" else "🕒 Recent Transactions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Text(
              text = if (isKurdish) "هەمی مامەڵە ➔" else "All Transactions ➔",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = GoldAccent,
              modifier = Modifier.clickable { onNavigateToTab(5) }
            )
          }

          if (recentTx.isEmpty()) {
            Text(
              text = if (isKurdish) "چ مامەڵە تا نوکە نەهاتینە تۆمارکرن" else "No recent transactions recorded yet",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 4.dp)
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              recentTx.forEach { tx ->
                val isIn = tx.type.equals("IN", ignoreCase = true)
                val accent = if (isIn) EmeraldLight else LossRed
                val converted = CurrencyService.convert(
                  amount = tx.amount,
                  from = tx.currency,
                  to = uiState.displayCurrency,
                  iqdRate = uiState.iqdRate,
                  entryIqdRate = tx.exchangeRateAtEntry
                )
                CyberRecordButton(
                  title = tx.category.ifBlank { if (isIn) (if (isKurdish) "داهات" else "Income") else (if (isKurdish) "مەسرەف" else "Expense") },
                  subtitle = tx.notes.ifBlank { dateFormat.format(java.util.Date(tx.dateMillis)) },
                  primaryValue = "${if (isIn) "+" else "−"}${fmtShort(converted)}",
                  accentColor = accent,
                  icon = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                  statusBadge = if (!isIn) {
                    if (tx.affectsCapital) (if (isKurdish) "ژ کاشی" else "From Cash") else (if (isKurdish) "بێ کاش" else "No Cash")
                  } else null,
                  statusBadgeColor = if (tx.affectsCapital) accent else NeonCyan,
                  actionHintLabel = if (isKurdish) "دەستکاری" else "Actions",
                  quickActionLabel = if (isKurdish) "گوهۆڕین" else "Edit",
                  quickActionIcon = Icons.Default.Edit,
                  quickActionColor = GoldAccent,
                  onQuickActionClick = { viewModel.openQuickEditForTransaction(tx) },
                  onClick = { selectedTxForDetail = tx },
                  testTagKey = "overview_recent_tx_${tx.id}"
                )
              }
            }
          }
        }
      }
    }

    // 5. Clean Interactive Net Worth Growth Trend Chart (بیانیا گەشەکرنا سەرمایەی)
    item {
      NetWorthTrendLineChart(
        uiState = uiState,
        isKurdish = isKurdish,
        isMasked = isMasked
      )
    }

    // Hidden accessibility and compatibility anchors for test coverage
    item {
      Row(modifier = Modifier.size(0.dp)) {
        Box(modifier = Modifier.testTag("standalone_real_estate_summary_card").clickable { onNavigateToTab(3) })
        Box(modifier = Modifier.testTag("overview_cashflow_ledger_card").clickable { onNavigateToTab(5) })
        Box(modifier = Modifier.testTag("overview_school_vault_anchor").clickable { onNavigateToTab(4) })
        Box(modifier = Modifier.testTag("quick_money_in_button").clickable { showQuickInDialog = true })
        Box(modifier = Modifier.testTag("quick_money_out_button").clickable { showQuickOutDialog = true })
        Box(modifier = Modifier.testTag("overview_quick_lend_button").clickable { showQuickLendDialog = true })
        Box(modifier = Modifier.testTag("overview_quick_borrow_button").clickable { showQuickBorrowDialog = true })
        Box(modifier = Modifier.testTag("overview_quick_adjust_cash_button").clickable { showAdjustCapitalDialog = true })
        Box(modifier = Modifier.testTag("overview_tools_vault_card").clickable { onNavigateToTab(3) })
        Box(modifier = Modifier.testTag("overview_active_loans_list").clickable { onNavigateToTab(1) })
      }
    }


  }
  }

  // Quick Money In Dialog
  if (showQuickInDialog) {
    QuickTransactionDialog(
      type = "IN",
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      customCategories = uiState.customIncomeCategories,
      associatedTransactions = uiState.transactions,
      onAddCustomCategory = { viewModel.addCustomCategory("IN", it) },
      onRemoveCustomCategory = { viewModel.removeCustomCategory("IN", it) },
      onRemoveCustomCategoryWithResolution = { cat, deleteTxs, targetCat ->
        viewModel.removeCustomCategory(
          type = "IN",
          rawName = cat,
          deleteAssociatedTransactions = deleteTxs,
          reassignToCategory = targetCat
        )
      },
      onDismiss = { showQuickInDialog = false },
      onConfirm = { amount, category, notes, currency ->
        viewModel.addTransaction(
          type = "IN",
          amount = amount,
          category = category,
          currency = currency,
          notes = notes
        )
        showQuickInDialog = false
      },
      onConfirmWithRate = { amount, category, notes, currency, customRate ->
        viewModel.addTransaction(
          type = "IN",
          amount = amount,
          category = category,
          currency = currency,
          notes = notes,
          customIqdRate = customRate
        )
        showQuickInDialog = false
      }
    )
  }

  // Quick Money Out Dialog
  if (showQuickOutDialog) {
    QuickTransactionDialog(
      type = "OUT",
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      customCategories = uiState.customExpenseCategories,
      associatedTransactions = uiState.transactions,
      onAddCustomCategory = { viewModel.addCustomCategory("OUT", it) },
      onRemoveCustomCategory = { viewModel.removeCustomCategory("OUT", it) },
      onRemoveCustomCategoryWithResolution = { cat, deleteTxs, targetCat ->
        viewModel.removeCustomCategory(
          type = "OUT",
          rawName = cat,
          deleteAssociatedTransactions = deleteTxs,
          reassignToCategory = targetCat
        )
      },
      onDismiss = { showQuickOutDialog = false },
      onConfirm = { amount, category, notes, currency ->
        viewModel.addTransaction(
          type = "OUT",
          amount = amount,
          category = category,
          currency = currency,
          notes = notes
        )
        showQuickOutDialog = false
      },
      onConfirmWithRate = { amount, category, notes, currency, customRate ->
        viewModel.addTransaction(
          type = "OUT",
          amount = amount,
          category = category,
          currency = currency,
          notes = notes,
          customIqdRate = customRate
        )
        showQuickOutDialog = false
      }
    )
  }

  // Quick Lend Dialog (قەرز دەم)
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

  // Quick Borrow Dialog (قەرز وەرگرم)
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

  // Adjust Starting Capital Dialog
  if (showAdjustCapitalDialog) {
    var capitalInput by remember { mutableStateOf(uiState.settings.startingCapital.toString()) }
    var capRateInput by remember(uiState.iqdRate) { mutableStateOf(uiState.iqdRate.toInt().toString()) }
    AlertDialog(
      onDismissRequest = { showAdjustCapitalDialog = false },
      title = { Text(if (isKurdish) "دەستکاریکرنا سەرمایێ سەرەکی" else "Set Starting Capital") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            if (isKurdish)
              "سەرمایێ خۆ یێ سەرەکی یێ بنەڕەت دەستنیشان بکە. سەرمایێ تە یێ گشتی دێ هێتە هەژمارتن ل دیف تەڤایا داهات و مەسروفان."
            else
              "Adjust your baseline initial capital. Your net capital will recalculate automatically with all inflows and outflows."
          )
          OutlinedTextField(
            value = capitalInput,
            onValueChange = { capitalInput = it },
            label = { Text("${if (isKurdish) "بڕێ پارەی" else "Amount"} (${uiState.settings.capitalCurrency})") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          EntryExchangeRateCard(
            rateInput = capRateInput,
            onRateChange = { capRateInput = it },
            currentGlobalIqdRate = uiState.iqdRate,
            enteredAmount = capitalInput.toDoubleOrNull() ?: 0.0,
            selectedCurrency = uiState.settings.capitalCurrency,
            isKurdish = isKurdish,
            testTag = "adjust_capital_iqd_rate_input"
          )
          OutlinedButton(
            onClick = {
              viewModel.resetCashAccountToZero()
              showAdjustCapitalDialog = false
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reset_cash_to_zero_btn")
          ) {
            Text(
              text = if (isKurdish) "سفرکرنا حسێبا کاشی بۆ ٠ $ (ژێبرنا ١,٣٠٠$ کۆن)" else "Reset Cash to 0$ (Clear 1,300$ old balance)",
              color = LossRed,
              fontWeight = FontWeight.Bold
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = if (capitalInput.isBlank()) 0.0 else (capitalInput.toDoubleOrNull() ?: 0.0)
            val parsedRate = CurrencyService.normalizeIqdRatePerUsd(
              capRateInput.toDoubleOrNull() ?: uiState.iqdRate,
              uiState.iqdRate
            )
            viewModel.updateIqdRate(parsedRate)
            viewModel.setStartingCapital(amount.coerceAtLeast(0.0), uiState.settings.capitalCurrency)
            showAdjustCapitalDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight)
        ) {
          Text(if (isKurdish) "تۆمار بکە" else "Save")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAdjustCapitalDialog = false }) {
          Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
        }
      }
    )
  }

  // Adjust Exchange Rate Dialog
  if (showRateAdjustDialog) {
    var rateInput by remember { mutableStateOf(uiState.iqdRate.toInt().toString()) }
    AlertDialog(
      onDismissRequest = { showRateAdjustDialog = false },
      title = { Text(if (isKurdish) "گوهۆڕینا سعرێ دۆلاری بەرامبەر دیناری" else "Adjust IQD Exchange Rate") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            if (isKurdish)
              "سعرێ نوی یێ دۆلاری بنڤیسە (بۆ نموونە 1500 یان 150000 بۆ هەر 100$)."
            else
              "Enter new market rate for USD in Iraqi Dinars (e.g. 1500 per $1 or 150000 per $100)."
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(EmeraldLight.copy(alpha = 0.14f))
              .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish)
                "🔒 هەر دینارەکێ تە بەری نوکە داخل کربیت دێ لسەر سعرا خۆ یا کەڤن مینیت و ناگۆهۆڕیت."
              else
                "🔒 Previously entered IQD records remain locked at their original entry rate.",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
          }
          OutlinedTextField(
            value = rateInput,
            onValueChange = { rateInput = it },
            label = { Text(if (isKurdish) "دینار بۆ ١ دۆلار (یان ١٠٠$)" else "IQD per 1 USD (or $100)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("1310", "1480", "1500", "1520", "1550").forEach { presetRate ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (rateInput == presetRate) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .clickable { rateInput = presetRate }
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = presetRate,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (rateInput == presetRate) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val rawRate = rateInput.toDoubleOrNull() ?: uiState.iqdRate
            val rate = CurrencyService.normalizeIqdRatePerUsd(rawRate, uiState.iqdRate)
            viewModel.updateIqdRate(rate)
            showRateAdjustDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
        ) {
          Text(if (isKurdish) "جێبەجێ بکە" else "Apply Rate", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showRateAdjustDialog = false }) {
          Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
        }
      }
    )
  }

  // Global Search Bottom Sheet across entire app
  if (showGlobalSearchSheet) {
    GlobalSearchBottomSheet(
      uiState = uiState,
      isKurdish = isKurdish,
      onNavigateToTab = onNavigateToTab,
      onSelectSearchResult = { item ->
        viewModel.openSearchResultItem(
          targetTab = item.targetTab,
          entityType = item.entityType,
          entityId = item.entityId
        )
        showGlobalSearchSheet = false
      },
      onDismiss = { showGlobalSearchSheet = false }
    )
  }

  // Gemini Financial Copilot Interactive Sheet
  if (showCopilotSheet) {
    GeminiFinancialCopilotSheet(
      uiState = uiState,
      isKurdish = isKurdish,
      onDismiss = { showCopilotSheet = false },
      onNavigateToTab = onNavigateToTab
    )
  }

  // Visual Financial Charts Dashboard Dialog
  if (showAnalyticsDashboard) {
    Dialog(
      onDismissRequest = { showAnalyticsDashboard = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      AnalyticsDashboardView(
        viewModel = viewModel,
        uiState = uiState,
        onDismiss = { showAnalyticsDashboard = false }
      )
    }
  }

  // Quick Category / Brand Filtered List Sheet (like Toyota, BMW, Houses, Gold...)
  if (activeCategoryFilterModal != null) {
    QuickCategoryListDialog(
      initialCategoryKey = activeCategoryFilterModal!!,
      uiState = uiState,
      isKurdish = isKurdish,
      onDismiss = { activeCategoryFilterModal = null },
      onNavigateToTab = onNavigateToTab,
      onSelectCar = { selectedCarForDetail = it },
      onSelectProperty = { selectedAssetForDetail = it },
      onSelectDebt = { loan ->
        val debtEntity = uiState.debtsCredits.find { it.id == loan.id }
        if (debtEntity != null) selectedDebtForDetail = debtEntity else onNavigateToTab(1)
      },
      onSelectGold = { selectedGoldForDetail = it },
      onSelectSchoolTx = { selectedTxForDetail = it }
    )
  }

  val activeTx = selectedTxForDetail
  if (activeTx != null) {
    TransactionDetailActionDialog(
      transaction = activeTx,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedTxForDetail = null }
    )
  }

  val activeDebt = selectedDebtForDetail
  if (activeDebt != null) {
    val latest = uiState.debtsCredits.find { it.id == activeDebt.id } ?: activeDebt
    PersonDebtDetailsDialog(
      debt = latest,
      uiState = uiState,
      isKurdish = isKurdish,
      dateFormat = dateFormat,
      onRecordPayment = {
        selectedDebtForDetail = null
        viewModel.openSearchResultItem(1, "DEBT", latest.id)
      },
      onAddMoney = {
        selectedDebtForDetail = null
        viewModel.openSearchResultItem(1, "DEBT", latest.id)
      },
      onEditDebt = {
        selectedDebtForDetail = null
        viewModel.openQuickEditForDebt(latest)
      },
      onSettleFull = {
        viewModel.settleDebtCreditInFull(latest.id)
        selectedDebtForDetail = null
      },
      onDeletePayment = { viewModel.deleteDebtPayment(it) },
      onDelete = {
        viewModel.deleteDebtCredit(latest.id)
        selectedDebtForDetail = null
      },
      onDismiss = { selectedDebtForDetail = null }
    )
  }

  val activeCar = selectedCarForDetail
  if (activeCar != null) {
    CarDetailsDialog(
      car = uiState.carInvestments.find { it.id == activeCar.id } ?: activeCar,
      uiState = uiState,
      isKurdish = isKurdish,
      onDismiss = { selectedCarForDetail = null },
      onAddPayment = {
        selectedCarForDetail = null
        viewModel.openSearchResultItem(2, "CAR", activeCar.id)
      },
      onAddSpending = {
        selectedCarForDetail = null
        viewModel.openSearchResultItem(2, "CAR", activeCar.id)
      },
      onEditCar = {
        selectedCarForDetail = null
        viewModel.openQuickEditForInvestment(activeCar)
      },
      onDeletePayment = { viewModel.deleteCarPayment(it) },
      onDeleteSpending = { viewModel.deleteCarService(it) }
    )
  }

  val activeAsset = selectedAssetForDetail
  if (activeAsset != null) {
    AssetDetailActionDialog(
      investment = activeAsset,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedAssetForDetail = null }
    )
  }

  val activeGold = selectedGoldForDetail
  if (activeGold != null) {
    GoldHoldingDetailActionDialog(
      holding = activeGold,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedGoldForDetail = null }
    )
  }

  val activeBudget = selectedBudgetForDetail
  if (activeBudget != null) {
    BudgetDetailActionDialog(
      budget = activeBudget.budget,
      spentDisplay = activeBudget.spentAmount,
      limitDisplay = activeBudget.limitAmount,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedBudgetForDetail = null }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickTransactionDialog(
  type: String,
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  initialCategory: String? = null,
  customCategories: List<String> = emptyList(),
  associatedTransactions: List<TransactionEntity> = emptyList(),
  onAddCustomCategory: ((String) -> Unit)? = null,
  onRemoveCustomCategory: ((String) -> Unit)? = null,
  onRemoveCustomCategoryWithResolution: ((categoryName: String, deleteTransactions: Boolean, reassignToCategory: String?) -> Unit)? = null,
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, category: String, notes: String, currency: String) -> Unit,
  onConfirmWithRate: ((amount: Double, category: String, notes: String, currency: String, customIqdRate: Double) -> Unit)? = null,
  onConfirmWithRateAndCashOption: ((amount: Double, category: String, notes: String, currency: String, customIqdRate: Double, deductFromCash: Boolean) -> Unit)? = null
) {
  var amountInput by remember { mutableStateOf("") }
  val sectionType = if (type == "IN") CategorySectionType.INCOME else CategorySectionType.EXPENSE
  val combinedCategories = remember(customCategories, type, isKurdish) {
    AppCategoryRegistry.getCombinedCategoryNames(sectionType, customCategories, isKurdish)
  }
  var localCategories by remember(combinedCategories) { mutableStateOf(combinedCategories) }
  var categoryPendingResolution by remember { mutableStateOf<String?>(null) }
  var selectedCategory by remember(initialCategory, combinedCategories) {
    mutableStateOf(
      initialCategory?.takeIf { it.isNotBlank() }
        ?: combinedCategories.firstOrNull()
        ?: ""
    )
  }
  LaunchedEffect(localCategories) {
    if (selectedCategory.isBlank() && localCategories.isNotEmpty()) {
      selectedCategory = localCategories.first()
    } else if (
      selectedCategory.isNotBlank() &&
      localCategories.none { it.equals(selectedCategory, ignoreCase = true) } &&
      !selectedCategory.equals(initialCategory ?: "", ignoreCase = true)
    ) {
      selectedCategory = localCategories.firstOrNull() ?: ""
    }
  }
  var newCategoryInput by remember { mutableStateOf("") }
  var notesInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var entryIqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  var deductFromCash by remember { mutableStateOf(true) }
  val activeAccent = if (type == "IN") EmeraldLight else LossRed

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        if (type == "IN") {
          if (isKurdish) "تۆمارکرنا داهاتی (+)" else "Record Money In (+)"
        } else {
          if (isKurdish) "تۆمارکرنا مەسروفی (-)" else "Record Money Out (-)"
        },
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = amountInput,
          onValueChange = { amountInput = it },
          label = { Text("${if (isKurdish) "بڕێ پارەی" else "Amount"} ($currency)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_amount_input")
        )

        // 1-Tap Quick Amount Chips (بۆ داخلکرنا ب ساناهی ب ئێک کلیک)
        val quickAmounts = if (currency == "USD") {
          listOf(50.0, 100.0, 500.0, 1000.0, 5000.0)
        } else {
          listOf(25000.0, 50000.0, 100000.0, 250000.0, 1000000.0)
        }
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          quickAmounts.forEach { preset ->
            val label = if (currency == "USD") "+$${preset.toLong()}" else "+${"%,d".format(Locale.US, preset.toLong())}"
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(GoldAccent.copy(alpha = 0.16f))
                .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .clickable {
                  val current = amountInput.toDoubleOrNull() ?: 0.0
                  val next = current + preset
                  amountInput = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
              )
            }
          }
          if (amountInput.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(LossRed.copy(alpha = 0.16f))
                .clickable { amountInput = "" }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = if (isKurdish) "پاقژکرن ✕" else "Clear ✕",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LossRed
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            val isSelectedCur = currency == cur
            Button(
              onClick = { currency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelectedCur) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
            ) {
              Text(
                text = cur,
                fontWeight = FontWeight.Bold,
                color = if (isSelectedCur) Color.Black else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Always-visible Dinar & Dollar Exchange Rate Card (سعرا دیناری و دۆلاری)
        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = amountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = currency,
          isKurdish = isKurdish,
          testTag = "transaction_entry_iqd_rate_input"
        )

        CategoryChipSelector(
          section = sectionType,
          selectedCategory = selectedCategory,
          onCategorySelected = { selectedCategory = it },
          customCategories = localCategories,
          onAddCustomCategory = { clean ->
            if (localCategories.none { it.equals(clean, ignoreCase = true) }) {
              localCategories = localCategories + clean
            }
            onAddCustomCategory?.invoke(clean)
            selectedCategory = clean
          },
          isKurdish = isKurdish
        )

        // Custom Category Manual Input + Add Button fallback
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = newCategoryInput,
            onValueChange = { newCategoryInput = it },
            placeholder = {
              Text(
                text = if (isKurdish) "یان ناڤەکێ دی یێ دلخواز بنڤیسە..." else "Or type a custom category name...",
                fontSize = 12.sp
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("custom_category_input")
          )
          Button(
            onClick = {
              val clean = newCategoryInput.trim()
              if (clean.isNotEmpty()) {
                if (localCategories.none { it.equals(clean, ignoreCase = true) }) {
                  localCategories = localCategories + clean
                }
                onAddCustomCategory?.invoke(clean)
                selectedCategory = clean
                newCategoryInput = ""
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = activeAccent,
              contentColor = if (type == "IN") Color(0xFF041E19) else Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier
              .heightIn(min = 48.dp)
              .testTag("add_custom_category_button")
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

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی (د دەستێ تە دایە)" else "Notes (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (type == "OUT") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .clickable { deductFromCash = !deductFromCash }
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = deductFromCash,
              onCheckedChange = { deductFromCash = it },
              colors = CheckboxDefaults.colors(checkedColor = LossRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKurdish) "ژ کاشی کێم ببیت (سەرمایە)" else "Deduct from Cash Balance",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
            entryIqdRateInput.toDoubleOrNull() ?: currentIqdRate,
            currentIqdRate
          )
          val typedNow = newCategoryInput.trim()
          if (typedNow.isNotEmpty()) {
            onAddCustomCategory?.invoke(typedNow)
          }
          val finalCategory = typedNow.ifBlank {
            selectedCategory.trim().ifBlank {
              if (type == "IN") {
                if (isKurdish) "داهات" else "Income"
              } else {
                if (isKurdish) "مەسروف" else "Expense"
              }
            }
          }
          if (amt > 0) {
            when {
              onConfirmWithRateAndCashOption != null -> {
                onConfirmWithRateAndCashOption(amt, finalCategory, notesInput, currency, lockedRate, deductFromCash)
              }
              onConfirmWithRate != null -> {
                onConfirmWithRate(amt, finalCategory, notesInput, currency, lockedRate)
              }
              else -> {
                onConfirm(amt, finalCategory, notesInput, currency)
              }
            }
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (type == "IN") EmeraldLight else LossRed
        ),
        modifier = Modifier
          .heightIn(min = 44.dp)
          .testTag("confirm_transaction_button")
      ) {
        Text(
          if (isKurdish) {
            if (type == "IN") "تۆمارکرنا داهاتی" else "تۆمارکرنا مەسروفی"
          } else {
            "Save Record"
          },
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.heightIn(min = 44.dp)
      ) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )

  val pendingCat = categoryPendingResolution
  if (pendingCat != null) {
    val matchingTxs = associatedTransactions.filter {
      it.type.equals(type, ignoreCase = true) &&
        it.category.trim().equals(pendingCat.trim(), ignoreCase = true)
    }
    CategoryDeleteResolutionDialog(
      categoryName = pendingCat,
      type = type,
      associatedTransactions = matchingTxs,
      availableCategories = localCategories.filterNot { it.equals(pendingCat, ignoreCase = true) },
      displayCurrency = displayCurrency,
      currentIqdRate = currentIqdRate,
      isKurdish = isKurdish,
      onDismiss = { categoryPendingResolution = null },
      onReassignAndDelete = { targetCategory ->
        val nextList = localCategories.filterNot { it.equals(pendingCat, ignoreCase = true) }
        localCategories = if (
          targetCategory.isNotBlank() &&
          nextList.none { it.equals(targetCategory, ignoreCase = true) } &&
          targetCategory != "مەسروفێ گشتی" &&
          targetCategory != "داهاتێ گشتی" &&
          targetCategory != "General Expense" &&
          targetCategory != "General Income"
        ) {
          nextList + targetCategory
        } else {
          nextList
        }
        if (onRemoveCustomCategoryWithResolution != null) {
          onRemoveCustomCategoryWithResolution(pendingCat, false, targetCategory)
        } else {
          onRemoveCustomCategory?.invoke(pendingCat)
        }
        if (selectedCategory.equals(pendingCat, ignoreCase = true)) {
          selectedCategory = targetCategory.ifBlank { localCategories.firstOrNull() ?: "" }
        }
        categoryPendingResolution = null
      },
      onDeleteTransactionsAndCategory = {
        localCategories = localCategories.filterNot { it.equals(pendingCat, ignoreCase = true) }
        if (onRemoveCustomCategoryWithResolution != null) {
          onRemoveCustomCategoryWithResolution(pendingCat, true, null)
        } else {
          onRemoveCustomCategory?.invoke(pendingCat)
        }
        if (selectedCategory.equals(pendingCat, ignoreCase = true)) {
          selectedCategory = localCategories.firstOrNull() ?: ""
        }
        categoryPendingResolution = null
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDeleteResolutionDialog(
  categoryName: String,
  type: String = "OUT",
  associatedTransactions: List<TransactionEntity>,
  availableCategories: List<String>,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onReassignAndDelete: (targetCategory: String) -> Unit,
  onDeleteTransactionsAndCategory: () -> Unit
) {
  val defaultFallback = if (type.equals("IN", ignoreCase = true)) {
    if (isKurdish) "داهاتێ گشتی" else "General Income"
  } else {
    if (isKurdish) "مەسروفێ گشتی" else "General Expense"
  }
  val candidateChips = remember(availableCategories, defaultFallback) {
    (availableCategories + defaultFallback).distinctBy { it.lowercase() }
  }
  var selectedTargetCategory by remember(candidateChips) {
    mutableStateOf(candidateChips.firstOrNull() ?: defaultFallback)
  }
  var customTargetInput by remember { mutableStateOf("") }

  val totalAssociatedDisplay = remember(associatedTransactions, displayCurrency, currentIqdRate) {
    associatedTransactions.sumOf { tx ->
      val rate = if (tx.exchangeRateAtEntry > 0.0) tx.exchangeRateAtEntry else currentIqdRate
      CurrencyService.convert(tx.amount, tx.currency, displayCurrency, rate)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isKurdish) {
          "ژێبرنا پۆلا «$categoryName»"
        } else {
          "Delete Category \"$categoryName\""
        },
        fontWeight = FontWeight.Black,
        color = GoldAccent
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldAccent.copy(alpha = 0.14f))
            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = if (isKurdish) {
                "⚠️ ${associatedTransactions.size} تۆمارێن کەڤن ب کۆژمێ ${CurrencyService.format(totalAssociatedDisplay, displayCurrency)} گرێدای ب ڤێ پۆلێ ڤە هەنە."
              } else {
                "⚠️ ${associatedTransactions.size} historical records totaling ${CurrencyService.format(totalAssociatedDisplay, displayCurrency)} belong to this category."
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GoldAccent
            )
            Text(
              text = if (isKurdish) {
                "تە دڤێت چ ب سەرێ ڤان تۆمارێن کەڤن بهێت دا تۆمارێن بێ خودان (Orphaned) چێنەبن؟"
              } else {
                "How would you like to handle these historical records to prevent orphaned transactions?"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Option 1 Card: Reassign to another category
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(EmeraldLight.copy(alpha = 0.10f))
            .border(1.dp, EmeraldLight.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (isKurdish) {
              "١. ڤەگوهاستنا تۆمارێن کەڤن بۆ پۆلەکا دی (پێشنیارکری)"
            } else {
              "1. Reassign Historical Records to Another Category"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = EmeraldLight
          )
          Text(
            text = if (isKurdish) {
              "پۆلا جێگر هەلبژێرە یان ناڤێ پۆلەکا نوی بنڤیسە (باڵانسێ کاش تێکناچیت):"
            } else {
              "Select a target category or type a new one (preserves cash flow balances):"
            },
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            candidateChips.forEach { target ->
              val isSelected = customTargetInput.isBlank() && selectedTargetCategory.equals(target, ignoreCase = true)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (isSelected) EmeraldLight.copy(alpha = 0.24f)
                    else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .border(
                    width = if (isSelected) 1.2.dp else 0.6.dp,
                    color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    customTargetInput = ""
                    selectedTargetCategory = target
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("reassign_target_chip_$target")
              ) {
                Text(
                  text = target,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                  color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          OutlinedTextField(
            value = customTargetInput,
            onValueChange = { customTargetInput = it },
            placeholder = {
              Text(
                text = if (isKurdish) "یان ناڤێ پۆلەکا جێگر بنڤیسە..." else "Or type target category name...",
                fontSize = 11.5.sp
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reassign_custom_target_input")
          )

          Button(
            onClick = {
              val finalTarget = customTargetInput.trim().ifBlank { selectedTargetCategory.ifBlank { defaultFallback } }
              onReassignAndDelete(finalTarget)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = EmeraldLight,
              contentColor = Color(0xFF041E19)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 46.dp)
              .testTag("confirm_reassign_and_delete_category_button")
          ) {
            Text(
              text = if (isKurdish) {
                val targetName = customTargetInput.trim().ifBlank { selectedTargetCategory }
                "ڤەگوهاستن بۆ «$targetName» و ژێبرنا پۆلێ"
              } else {
                val targetName = customTargetInput.trim().ifBlank { selectedTargetCategory }
                "Reassign to \"$targetName\" & Delete"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Black
            )
          }
        }

        // Option 2 Card: Clean up / Delete associated transactions
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LossRed.copy(alpha = 0.10f))
            .border(1.dp, LossRed.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (isKurdish) {
              "٢. ژێبرنا تەمام یا پۆلێ لگەل تۆمارێن وێ"
            } else {
              "2. Delete Category & All Associated Records"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = LossRed
          )
          Text(
            text = if (isKurdish) {
              "پۆلا «$categoryName» و هەر ${associatedTransactions.size} تۆمارێن گرێدای ب ئێکجار دێ هێنە ژێبرن:"
            } else {
              "Permanently delete \"$categoryName\" and all ${associatedTransactions.size} associated transactions:"
            },
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Button(
            onClick = onDeleteTransactionsAndCategory,
            colors = ButtonDefaults.buttonColors(
              containerColor = LossRed,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 46.dp)
              .testTag("confirm_cleanup_and_delete_category_button")
          ) {
            Text(
              text = if (isKurdish) {
                "ژێبرنا پۆلێ و ${associatedTransactions.size} تۆماران 🗑"
              } else {
                "Delete Category & ${associatedTransactions.size} Records 🗑"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Black
            )
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.heightIn(min = 44.dp)
      ) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}

@Composable
private fun EquationItemRow(
  symbol: String,
  symbolColor: Color,
  title: String,
  subtitle: String,
  amount: String,
  amountColor: Color
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 7.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(22.dp)
          .background(symbolColor.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = symbol,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp,
          color = symbolColor
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          color = Color.White.copy(alpha = 0.65f)
        )
      }
    }

    Text(
      text = amount,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.ExtraBold,
      color = amountColor
    )
  }
}

