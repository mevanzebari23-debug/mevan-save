package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.AuditActionDetailDialog
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.LiveInstantSearchBar
import com.example.ui.history.ActionOperationType
import com.example.ui.history.ReversibleAction
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.budgets.BudgetsAndToolsScreen
import com.example.ui.screens.cashflow.CashFlowScreen
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.WoodBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Consolidated Hub for Reports, Cashflow, and Tools (راپۆرت و چالاکی)
 * Contains Cashflow (0), Comprehensive Reports & Undo History (1), and Tools & Settings (2)
 * with instant live search at the top.
 */
@Composable
fun ReportsAndToolsHubScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onLockApp: () -> Unit,
  onTestBiometric: () -> Unit = {},
  onOpenFirebaseAuthDialog: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  // 5: Tools & Vault, 0: Cash & Ledger, 1: Cars Report, 2: Debts Report, 3: Assets & Gold, 4: History & Undo
  var activeHubTab by remember { mutableIntStateOf(5) }
  var instantSearchQuery by remember { mutableStateOf("") }

  LaunchedEffect(uiState.focusedEntityType) {
    when (uiState.focusedEntityType) {
      "TRANSACTION" -> activeHubTab = 0
      "CAR" -> activeHubTab = 1
      "DEBT" -> activeHubTab = 2
      "INVESTMENT", "GOLD" -> activeHubTab = 3
      "BUDGET" -> activeHubTab = 5
    }
  }

  val reportCategories = listOf(
    Triple(5, if (isKurdish) "⚙️ ئامراز و بەڕێوەبردن" else "⚙️ Tools & Vault", null),
    Triple(0, if (isKurdish) "📑 کاش و سجل" else "📑 Cash & Ledger", uiState.transactions.size.takeIf { it > 0 }),
    Triple(1, if (isKurdish) "🚗 راپۆرتا سەیاران" else "🚗 Cars Report", uiState.carInvestments.size.takeIf { it > 0 }),
    Triple(2, if (isKurdish) "🤝 راپۆرتا قەرزان" else "🤝 Debts Report", uiState.activeDebtCreditLoans.size.takeIf { it > 0 }),
    Triple(3, if (isKurdish) "🏢 ملوک و زێڕ" else "🏢 Assets & Gold", (uiState.nonCarInvestments.size + uiState.activeGoldHoldings.size).takeIf { it > 0 }),
    Triple(4, if (isKurdish) "📜 مێژوو و ئەنجام" else "📜 Audit & Undo", uiState.actionHistory.size.takeIf { it > 0 })
  )

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    // 1. Instant Live Search Bar at the very top of Reports Hub
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
      LiveInstantSearchBar(
        query = instantSearchQuery,
        onQueryChange = { instantSearchQuery = it },
        placeholder = if (isKurdish) "گەڕیانا بلەز د هەمی راپۆرتان دا..." else "Search all reports instantly...",
        matchCount = null,
        isKurdish = isKurdish
      )
    }

    // 2. Hub Sub-Tabs Strip (Horizontal Scrollable for clean, beautiful spacing)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      reportCategories.forEach { (tabIndex, title, count) ->
        val isSelected = activeHubTab == tabIndex
        val accent = when (tabIndex) {
          0 -> EmeraldLight
          1 -> Color(0xFF38BDF8)
          2 -> Color(0xFFF97316)
          3 -> GoldAccent
          4 -> Color(0xFFA855F7)
          else -> Color(0xFF94A3B8)
        }

        Box(
          modifier = Modifier
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
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("reports_hub_tab_$tabIndex"),
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
            if (count != null && count > 0) {
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
        0 -> CashFlowScreen(
          viewModel = viewModel,
          uiState = uiState,
          modifier = Modifier.fillMaxSize()
        )
        1 -> CarFleetReportView(
          uiState = uiState,
          viewModel = viewModel,
          searchQuery = instantSearchQuery,
          isKurdish = isKurdish,
          modifier = Modifier.fillMaxSize()
        )
        2 -> DebtsReportView(
          uiState = uiState,
          viewModel = viewModel,
          searchQuery = instantSearchQuery,
          isKurdish = isKurdish,
          modifier = Modifier.fillMaxSize()
        )
        3 -> AssetsReportView(
          uiState = uiState,
          viewModel = viewModel,
          searchQuery = instantSearchQuery,
          isKurdish = isKurdish,
          modifier = Modifier.fillMaxSize()
        )
        4 -> ReportsAndHistoryView(
          viewModel = viewModel,
          uiState = uiState,
          searchQuery = instantSearchQuery,
          isKurdish = isKurdish
        )
        5 -> BudgetsAndToolsScreen(
          viewModel = viewModel,
          uiState = uiState,
          onLockApp = onLockApp,
          onTestBiometric = onTestBiometric,
          onOpenFirebaseAuthDialog = onOpenFirebaseAuthDialog,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun ReportsAndHistoryView(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  searchQuery: String,
  isKurdish: Boolean
) {
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US) }
  var selectedActionForDetail by remember { mutableStateOf<ReversibleAction?>(null) }
  val filteredHistory = remember(uiState.actionHistory, searchQuery) {
    if (searchQuery.isBlank()) {
      uiState.actionHistory
    } else {
      uiState.actionHistory.filter {
        it.titleKu.contains(searchQuery, ignoreCase = true) ||
          it.titleEn.contains(searchQuery, ignoreCase = true) ||
          it.subtitleKu.contains(searchQuery, ignoreCase = true) ||
          it.subtitleEn.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Executive Summary Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkGlassCardSurface, RoundedCornerShape(16.dp))
          .border(1.dp, WoodBorder, RoundedCornerShape(16.dp))
          .padding(top = 8.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) "خوولاسەیا سەروەت و داراییا گشتی" else "Comprehensive Executive Summary",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
            Icon(
              imageVector = Icons.Default.Assessment,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isKurdish) "سەروەتێ سافی" else "Net Worth",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyService.format(uiState.netWorth, uiState.displayCurrency),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = EmeraldLight
              )
            }
            Column {
              Text(
                text = if (isKurdish) "سەرمایێ دەستپێکێ" else "Initial Capital",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyService.format(uiState.settings.startingCapital, uiState.displayCurrency),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // Action History Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isKurdish) "مێژوویا کریاران و زڤڕاندن (Undo Log)" else "Action History & Undo Log",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (uiState.actionHistory.isNotEmpty()) {
          Text(
            text = "${filteredHistory.size} / ${uiState.actionHistory.size}",
            fontSize = 11.sp,
            color = GoldAccent
          )
        }
      }
    }

    if (filteredHistory.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (searchQuery.isNotBlank()) {
              if (isKurdish) "چ ئەنجام نەهاتنە دیتن بۆ ($searchQuery)" else "No results found for ($searchQuery)"
            } else {
              if (isKurdish) "هیچ کریارەک نەهاتیە ئەنجامدان هێشتا" else "No recent actions recorded yet"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(filteredHistory, key = { it.id }) { action ->
        val opAccent = when (action.operationType) {
          ActionOperationType.ADD -> EmeraldLight
          ActionOperationType.EDIT -> GoldAccent
          ActionOperationType.DELETE -> LossRed
          ActionOperationType.STATUS_CHANGE -> Color(0xFF38BDF8)
        }
        val impactValue = when {
          kotlin.math.abs(action.cashDeltaDisplay) >= 0.01 ->
            "${if (action.cashDeltaDisplay > 0) "+" else "-"}${CurrencyService.formatShort(kotlin.math.abs(action.cashDeltaDisplay), uiState.displayCurrency)}"
          kotlin.math.abs(action.netWorthDeltaDisplay) >= 0.01 ->
            "${if (action.netWorthDeltaDisplay > 0) "+" else "-"}${CurrencyService.formatShort(kotlin.math.abs(action.netWorthDeltaDisplay), uiState.displayCurrency)}"
          else -> dateFormat.format(Date(action.timestampMillis))
        }
        CyberRecordButton(
          title = action.title(isKurdish),
          subtitle = action.subtitle(isKurdish).ifBlank { dateFormat.format(Date(action.timestampMillis)) },
          primaryValue = impactValue,
          dateOrMeta = dateFormat.format(Date(action.timestampMillis)),
          accentColor = opAccent,
          icon = Icons.Default.History,
          statusBadge = if (isKurdish) action.section.labelKu else action.section.labelEn,
          statusBadgeColor = Color(0xFFA855F7),
          actionHintLabel = if (isKurdish) "هوردەکاری" else "Details",
          quickActionLabel = if (isKurdish) "زڤڕاندن" else "Undo",
          quickActionIcon = Icons.AutoMirrored.Filled.Undo,
          quickActionColor = EmeraldLight,
          onQuickActionClick = { viewModel.undoAction(action.id) },
          onClick = { selectedActionForDetail = action },
          testTagKey = "audit_history_btn_${action.id}"
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }

  val activeAction = selectedActionForDetail
  if (activeAction != null) {
    AuditActionDetailDialog(
      action = activeAction,
      displayCurrency = uiState.displayCurrency,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedActionForDetail = null }
    )
  }
}
