package com.example.ui.screens.debts

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
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
import com.example.data.local.DebtCreditEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.CyberNeonCategoryButton
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.EntryExchangeRateCard
import com.example.ui.components.LiveInstantSearchBar
import com.example.ui.components.SectionQuickHistoryStrip
import com.example.ui.history.ActionSection
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodCardSurface
import com.example.ui.theme.WoodInlayGold
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtsCreditsScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  modifier: Modifier = Modifier
) {
  var activeSubTab by remember { mutableStateOf("ALL_ACTIVE") } // "ALL_ACTIVE", "CREDIT" (They Owe Me), "DEBT" (I Owe), "SETTLED"
  var searchQuery by remember { mutableStateOf("") }
  var showAddDialog by remember { mutableStateOf(false) }
  var dialogInitialType by remember { mutableStateOf("CREDIT") }
  var recordingPaymentFor by remember { mutableStateOf<DebtCreditEntity?>(null) }
  var addingMoneyToDebt by remember { mutableStateOf<DebtCreditEntity?>(null) }
  var editingDebtCredit by remember { mutableStateOf<DebtCreditEntity?>(null) }
  var selectedDebtForDetails by remember { mutableStateOf<DebtCreditEntity?>(null) }

  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  LaunchedEffect(uiState.focusedEntityType, uiState.focusedEntityId, uiState.debtsCredits) {
    if (uiState.focusedEntityType == "DEBT" && uiState.focusedEntityId != null) {
      val target = uiState.debtsCredits.find { it.id == uiState.focusedEntityId }
      if (target != null) {
        activeSubTab = if (target.isCompleted) "SETTLED" else target.type
        selectedDebtForDetails = target
        viewModel.clearFocusedSearchResult()
      }
    }
  }

  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

  val filteredItems = remember(uiState.debtsCredits, activeSubTab, searchQuery) {
    val tabFiltered = when (activeSubTab) {
      "ALL_ACTIVE" -> uiState.debtsCredits.filter { !it.isCompleted && !it.isMulukDebt }
      "GENERAL" -> uiState.debtsCredits.filter { !it.isCompleted && !it.isMulukDebt }
      "CREDIT" -> uiState.debtsCredits.filter { it.type == "CREDIT" && !it.isCompleted && !it.isMulukDebt }
      "DEBT" -> uiState.debtsCredits.filter { it.type == "DEBT" && !it.isCompleted && !it.isMulukDebt }
      "MULUK", "PRIVATE" -> uiState.debtsCredits.filter { !it.isCompleted && it.isMulukDebt }
      "SETTLED" -> uiState.debtsCredits.filter { it.isCompleted }
      else -> uiState.debtsCredits.filter { !it.isCompleted && !it.isMulukDebt }
    }
    val q = searchQuery.trim()
    if (q.isEmpty()) {
      tabFiltered
    } else {
      tabFiltered.filter {
        it.personName.contains(q, ignoreCase = true) ||
          it.contactInfo.contains(q, ignoreCase = true) ||
          it.notes.contains(q, ignoreCase = true) ||
          it.totalAmount.toString().contains(q) ||
          it.currency.contains(q, ignoreCase = true)
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
      contentPadding = PaddingValues(top = 10.dp, bottom = 92.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 0. Instant Live Search Bar at the Very Top of Debts Screen
      item {
        LiveInstantSearchBar(
          query = searchQuery,
          onQueryChange = { searchQuery = it },
          placeholder = if (isKurdish) "گەڕیانا ئێکسەر ل قەرزان (ناڤ، پیت، ژمارە، تێبینی)..." else "Instant live search debts by name, letter...",
          matchCount = filteredItems.size,
          isKurdish = isKurdish,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("debts_top_live_search_bar")
        )
      }

      // Unified Net Capital Balance & Debt/Credit Offset Hero Card
      item {
        val summary = uiState.unifiedCapitalDebtSummary
        val netDebtOffset = summary.netDebtCreditOffset
        val combinedVolume = (summary.totalOwedByOthers + summary.totalDebtsToCreditors).coerceAtLeast(1.0)
        val creditShareRatio = if (summary.totalOwedByOthers == 0.0 && summary.totalDebtsToCreditors == 0.0) {
          0.5f
        } else {
          (summary.totalOwedByOthers / combinedVolume).toFloat().coerceIn(0f, 1f)
        }

        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkGlassCardSurface, RoundedCornerShape(20.dp))
            .border(
              width = 1.2.dp,
              brush = Brush.horizontalGradient(
                colors = listOf(WoodInlayGold.copy(alpha = 0.65f), EmeraldLight.copy(alpha = 0.45f), WoodBorder)
              ),
              shape = RoundedCornerShape(20.dp)
            )
            .testTag("unified_debt_credit_capital_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(GoldAccent.copy(alpha = 0.16f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = if (isKurdish) "تۆمارا ئێکگرتی یا قەرزان و سەرمایەی" else "UNIFIED RECEIVABLES, DEBTS & CAPITAL",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.8.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.ExtraBold
                  )
                  Text(
                    text = if (isKurdish) "نوێکرنا ئۆتۆماتیکی یا سەرمایێ صافی و صافی سەروەتی" else "Auto-updates Net Capital Balance & Clean Net Worth",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Automatic Net Capital Balance Banner
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                  RoundedCornerShape(14.dp)
                )
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (isKurdish) "سەرمایێ صافی (کاش + قەرز ل ئێک کۆژی)" else "Net Capital Balance (Auto-Updated)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = CurrencyService.format(summary.netCapitalBalance, uiState.displayCurrency),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Black,
                  color = if (summary.netCapitalBalance >= 0.0) GoldAccent else LossRed,
                  modifier = Modifier.testTag("debts_screen_net_capital_balance")
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = if (isKurdish) "کاش و زێڕ پێش قەرزان" else "Base Cash & Gold",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = CurrencyService.format(summary.baseCapitalBeforeDebtCredit, uiState.displayCurrency),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = if (isKurdish) "دەف خەلکی (قەرزدەر)" else "Owed by Others (Credits)",
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldLight,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "+${CurrencyService.format(uiState.totalCreditReceivable, uiState.displayCurrency)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldLight,
                  modifier = Modifier.testTag("total_owed_by_others_value")
                )
                Text(
                  text = if (isKurdish) "واصلبووی: ${CurrencyService.formatShort(summary.totalCreditRepaymentsCollected, uiState.displayCurrency)}"
                  else "Collected: ${CurrencyService.formatShort(summary.totalCreditRepaymentsCollected, uiState.displayCurrency)}",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = if (isKurdish) "لسەر من (بۆ قەرزدەران)" else "Debts to Creditors (Debts)",
                  style = MaterialTheme.typography.labelSmall,
                  color = LossRed,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "−${CurrencyService.format(uiState.totalDebtOwed, uiState.displayCurrency)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = LossRed,
                  modifier = Modifier.testTag("total_debts_to_creditors_value")
                )
                Text(
                  text = if (isKurdish) "دراوە: ${CurrencyService.formatShort(summary.totalDebtRepaymentsPaid, uiState.displayCurrency)}"
                  else "Paid back: ${CurrencyService.formatShort(summary.totalDebtRepaymentsPaid, uiState.displayCurrency)}",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual proportion bar of Owed by Others vs Debts to Creditors
            LinearProgressIndicator(
              progress = { creditShareRatio },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = EmeraldLight,
              trackColor = LossRed.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  if (netDebtOffset >= 0.0) EmeraldLight.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f),
                  RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "⚖️ ل ئێک کۆژیا قەرزان (کاریگەری لسەر سەرمایەی):" else "⚖️ Net Offset Impact on Capital:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${if (netDebtOffset >= 0.0) "+" else ""}${CurrencyService.format(netDebtOffset, uiState.displayCurrency)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (netDebtOffset >= 0.0) EmeraldLight else LossRed,
                modifier = Modifier.testTag("net_debt_credit_offset_value")
              )
            }
          }
        }
      }

      // Quick Action Buttons: "+ Lend Money" & "+ Borrow Money"
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkGlassCardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, WoodBorder, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "کردارێن لەزگین" else "QUICK MONEY ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (isKurdish) "تۆمارا نوی" else "Add new record",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldLight
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Add Money (Lend / Credit)
              Button(
                onClick = {
                  dialogInitialType = "CREDIT"
                  showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldLight,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .testTag("quick_add_money_lend_button")
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowOutward,
                  contentDescription = null,
                  tint = Color(0xFF0F172A),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = if (isKurdish) "+ قەرز دان" else "+ Lend Money",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    text = if (isKurdish) "دەف خەلکی (+سەرمایە)" else "Owed by Others (+)",
                    fontSize = 10.sp,
                    color = Color(0xFF0F172A).copy(alpha = 0.8f)
                  )
                }
              }

              // Add Money (Borrow / Debt)
              Button(
                onClick = {
                  dialogInitialType = "DEBT"
                  showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldAccent,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .testTag("quick_add_money_borrow_button")
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = Color(0xFF0F172A),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = if (isKurdish) "+ قەرز وەرگرتن" else "+ Borrow Money",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    text = if (isKurdish) "لسەر من (−سەرمایە)" else "Owed to Creditor (−)",
                    fontSize = 10.sp,
                    color = Color(0xFF0F172A).copy(alpha = 0.8f)
                  )
                }
              }
            }
          }
        }
      }

      // Sub-Tabs Filter Chips: General Debts, Receivables, Payables, Isolated Muluk Debts, Settled
      item {
        val mulukCount = uiState.debtsCredits.count { !it.isCompleted && it.isMulukDebt }
        val generalCount = uiState.debtsCredits.count { !it.isCompleted && !it.isMulukDebt }
        val generalCreditCount = uiState.debtsCredits.count { !it.isCompleted && !it.isMulukDebt && it.type == "CREDIT" }
        val generalDebtCount = uiState.debtsCredits.count { !it.isCompleted && !it.isMulukDebt && it.type == "DEBT" }
        val settledCount = uiState.debtsCredits.count { it.isCompleted }
        val scrollState = rememberScrollState()

        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("active_debt_credit_loans_section")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(scrollState)
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // 1. General Active Loans
            CyberNeonCategoryButton(
              label = if (isKurdish) "🤝 قەرزێن گشتی" else "🤝 General Debts",
              accentColor = Color(0xFF38BDF8),
              isSelected = activeSubTab == "ALL_ACTIVE" || activeSubTab == "GENERAL",
              count = generalCount.takeIf { it > 0 },
              onClick = { activeSubTab = "ALL_ACTIVE" },
              testTagKey = "tab_all_active_loans"
            )

            // 2. They Owe Me (CREDIT)
            CyberNeonCategoryButton(
              label = if (isKurdish) "📥 دەف خەلکی" else "📥 Receivables",
              accentColor = EmeraldLight,
              isSelected = activeSubTab == "CREDIT",
              count = generalCreditCount.takeIf { it > 0 },
              onClick = { activeSubTab = "CREDIT" },
              testTagKey = "tab_credit_loans"
            )

            // 3. I Owe (DEBT)
            CyberNeonCategoryButton(
              label = if (isKurdish) "📤 لسەر من" else "📤 Payables",
              accentColor = LossRed,
              isSelected = activeSubTab == "DEBT",
              count = generalDebtCount.takeIf { it > 0 },
              onClick = { activeSubTab = "DEBT" },
              testTagKey = "tab_debt_loans"
            )

            // 4. Dedicated Isolated Muluk Real Estate Debts
            CyberNeonCategoryButton(
              label = if (isKurdish) "🏢 قەرزێن ملوک" else "🏢 Muluk Debts",
              accentColor = GoldAccent,
              isSelected = activeSubTab == "MULUK" || activeSubTab == "PRIVATE",
              count = mulukCount.takeIf { it > 0 },
              icon = Icons.Default.Apartment,
              onClick = { activeSubTab = "MULUK" },
              testTagKey = "tab_muluk_isolated_loans"
            )

            // 5. Settled
            CyberNeonCategoryButton(
              label = if (isKurdish) "✅ واصل" else "✅ Settled",
              accentColor = Color(0xFF00FFA3),
              isSelected = activeSubTab == "SETTLED",
              count = settledCount.takeIf { it > 0 },
              onClick = { activeSubTab = "SETTLED" },
              testTagKey = "tab_settled_loans"
            )
          }
        }
      }

      // Dedicated Isolated Muluk Banner when viewing Muluk Debts
      if (activeSubTab == "MULUK" || activeSubTab == "PRIVATE") {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF06281D)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldAccent),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("muluk_debts_isolated_banner")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Apartment,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(24.dp)
                )
                Column {
                  Text(
                    text = if (isKurdish) "حسابا سەربەخۆ یا قەرزێن عەقارات و ملوکان" else "Isolated Muluk Real Estate Debts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                  )
                  Text(
                    text = if (isKurdish) "ئەڤ قەرزە ب تەمامی جودانە و دگەل قەرزێن دی یێن گشتی تێکەل نابن" else "Strictly isolated from general personal and car debt pools",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA7F3D0)
                  )
                }
              }
            }
          }
        }
      }

      // Reminder Alerts Banner
      val remindersForTab = filteredItems.filter { it.reminderEnabled && !it.isCompleted }
      if (remindersForTab.isNotEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkGlassCardSurface, RoundedCornerShape(12.dp))
              .border(1.dp, WoodBorder.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AlarmOn,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "${remindersForTab.size} payment reminder(s) active for ${if (activeSubTab == "CREDIT") "receivable loans" else "your debts"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // Recorded Installment Repayments Banner (with 1-Click Delete All)
      if (uiState.debtPayments.isNotEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.16f)),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, LossRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .clickable { viewModel.clearAllDebtPayments() }
              .testTag("debts_screen_clear_repayments_banner")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = null,
                  tint = LossRed,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isKurdish) {
                    "ژێبرنا هەمی دانەوەیێن قستان (${uiState.debtPayments.size} قست)"
                  } else {
                    "Delete All Recorded Repayments (${uiState.debtPayments.size})"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = LossRed
                )
              }
              Text(
                text = if (isKurdish) "ژێببە ✕" else "Delete All ✕",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LossRed
              )
            }
          }
        }
      }

      if (filteredItems.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkGlassCardSurface, RoundedCornerShape(14.dp))
              .border(
                1.dp,
                Brush.horizontalGradient(listOf(WoodInlayGold.copy(alpha = 0.4f), WoodBorder)),
                RoundedCornerShape(14.dp)
              )
          ) {
            Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
              Text(
                text = if (activeSubTab == "CREDIT") "No loans given out yet. Tap '+' to record money lent to someone."
                else "No debts logged. Tap '+' to track money you borrowed from someone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(filteredItems, key = { it.id }) { item ->
          val convertedTotal = CurrencyService.convert(item.totalAmount, item.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = item.exchangeRateAtEntry)
          val convertedRemaining = CurrencyService.convert(item.remainingAmount, item.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = item.exchangeRateAtEntry)
          val convertedPaid = CurrencyService.convert(item.amountPaid, item.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = item.exchangeRateAtEntry)

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkGlassCardSurface, RoundedCornerShape(16.dp))
              .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                  colors = if (item.isCompleted) {
                    listOf(EmeraldLight.copy(alpha = 0.65f), WoodInlayGold.copy(alpha = 0.35f), WoodBorder)
                  } else if (item.type == "DEBT") {
                    listOf(WoodInlayGold.copy(alpha = 0.6f), LossRed.copy(alpha = 0.45f), WoodBorder)
                  } else {
                    listOf(WoodInlayGold.copy(alpha = 0.6f), EmeraldLight.copy(alpha = 0.45f), WoodBorder)
                  }
                ),
                shape = RoundedCornerShape(16.dp)
              )
              .clickable { selectedDebtForDetails = item }
              .testTag("debt_card_${item.id}")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .background(
                        if (item.isCompleted) EmeraldLight.copy(alpha = 0.2f)
                        else if (item.isOverdue) LossRed.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.Person,
                      contentDescription = null,
                      tint = if (item.isCompleted) EmeraldLight else if (item.isOverdue) LossRed else MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(22.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = item.lenderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                          .weight(1f, fill = false)
                          .testTag("loan_lender_name_${item.id}")
                      )
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .background(
                            if (item.type == "DEBT") LossRed.copy(alpha = 0.15f) else EmeraldLight.copy(alpha = 0.15f)
                          )
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = if (item.type == "DEBT") {
                            if (isKurdish) "قەرزدەر (لسەر من)" else "Lender (Debt)"
                          } else {
                            if (isKurdish) "قەرزدار (دەف خەلکی)" else "Borrower (Credit)"
                          },
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (item.type == "DEBT") LossRed else EmeraldLight,
                          maxLines = 1
                        )
                      }
                    }
                    Text(
                      text = if (item.type == "DEBT") {
                        if (isKurdish) "ناڤێ قەرزدەری: ${item.lenderName}" else "Lender Name: ${item.lenderName}"
                      } else {
                        if (isKurdish) "وەرگر: ${item.borrowerName} • قەرزدەر: تۆ" else "Borrower: ${item.borrowerName} • Lender: You"
                      },
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    if (item.contactInfo.isNotBlank()) {
                      Text(
                        text = item.contactInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = { viewModel.openQuickEditForDebt(item) },
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("edit_loan_button_${item.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit & Preview",
                      tint = GoldAccent.copy(alpha = 0.85f),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.deleteDebtCredit(item.id) },
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("delete_loan_button_${item.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = "Delete",
                      tint = LossRed.copy(alpha = 0.85f),
                      modifier = Modifier.size(19.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Automatic Net Capital Balance Impact Pill
              val signedCapitalImpact = CurrencyService.convert(
                item.signedNetCapitalImpact,
                item.currency,
                uiState.displayCurrency,
                uiState.iqdRate,
                entryIqdRate = item.exchangeRateAtEntry
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(
                    if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    else if (item.type == "CREDIT") EmeraldLight.copy(alpha = 0.1f)
                    else LossRed.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                  )
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isKurdish) "کاریگەری لسەر سەرمایێ صافی:" else "Net Capital Balance Impact:",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (item.isCompleted) {
                    if (isKurdish) "تەواوبووی (٠)" else "Settled ($0)"
                  } else {
                    "${if (signedCapitalImpact >= 0.0) "+" else ""}${CurrencyService.format(signedCapitalImpact, uiState.displayCurrency)}"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (item.isCompleted) EmeraldLight else if (signedCapitalImpact >= 0.0) EmeraldLight else LossRed,
                  modifier = Modifier.testTag("loan_capital_impact_${item.id}")
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              // 1-Click Cash Balance Toggle Pill (ژ کاشی کێم ببیت یان زێدە ببیت یان نە)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (item.affectCashBalance) {
                      if (item.type == "CREDIT") LossRed.copy(alpha = 0.14f) else EmeraldLight.copy(alpha = 0.14f)
                    } else {
                      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    }
                  )
                  .border(
                    width = 0.8.dp,
                    color = if (item.affectCashBalance) {
                      if (item.type == "CREDIT") LossRed.copy(alpha = 0.45f) else EmeraldLight.copy(alpha = 0.45f)
                    } else {
                      WoodBorder
                    },
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { viewModel.toggleDebtCreditCashImpact(item.id) }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("toggle_loan_cash_impact_${item.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isKurdish) {
                    if (item.type == "CREDIT") "💵 کاریگەری لسەر کاشی (قەرزێ دای):" else "💵 کاریگەری لسەر کاشی (قەرزێ وەرگرتی):"
                  } else {
                    "💵 Cash Balance Impact (Tap to toggle):"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (item.affectCashBalance) {
                    if (item.type == "CREDIT") {
                      if (isKurdish) "✓ ژ کاشی کێمبوویە (−)" else "✓ Deducted from Cash (−)"
                    } else {
                      if (isKurdish) "✓ ل کاشی زێدەبوویە (+)" else "✓ Added to Cash (+)"
                    }
                  } else {
                    if (isKurdish) "⚪ بێی کاش (کلیک بکە)" else "⚪ No Cash Change (Tap)"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (item.affectCashBalance) {
                    if (item.type == "CREDIT") LossRed else EmeraldLight
                  } else {
                    GoldAccent
                  }
                )
              }

              if (item.currency == "IQD" && item.exchangeRateAtEntry > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (isKurdish) "🔒 نرخی دیناری یێ جێگیرکری (ل دەمێ داخلکرنێ):" else "🔒 Locked IQD Rate at Entry:",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontSize = 10.sp
                  )
                  Text(
                    text = "1 USD = ${item.exchangeRateAtEntry.toLong()} IQD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent,
                    fontSize = 10.5.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Amount stats
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = "Total Loan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = CurrencyService.format(convertedTotal, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "Repaid So Far",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = CurrencyService.format(convertedPaid, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = if (isKurdish) "باقیێ مایی (Remaining)" else "Remaining Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = CurrencyService.format(convertedRemaining, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isCompleted) EmeraldLight else if (item.type == "DEBT") LossRed else GoldAccent,
                    modifier = Modifier.testTag("loan_remaining_balance_${item.id}")
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Repayment Progress Bar & Percentage
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (isKurdish) "ڕێژەیا دانەوەی: ${item.repaymentProgressPercent}%" else "Repayment Progress: ${item.repaymentProgressPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.repaymentProgressPercent >= 100) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  val itemPaymentsCount = uiState.debtPayments.count { it.debtId == item.id }
                  if (itemPaymentsCount > 0) {
                    Text(
                      text = if (isKurdish) "$itemPaymentsCount دفعاتێن واصلبووی" else "$itemPaymentsCount installment(s) logged",
                      style = MaterialTheme.typography.labelSmall,
                      color = EmeraldLight,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                LinearProgressIndicator(
                  progress = { item.repaymentProgressRatio },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = if (item.type == "CREDIT") EmeraldLight else GoldAccent,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Due date and reminder badge
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (item.isOverdue) Icons.Default.Warning else Icons.Default.Alarm,
                    contentDescription = null,
                    tint = if (item.isOverdue) LossRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (item.isCompleted) "Completed"
                    else if (item.isOverdue) "OVERDUE (Due: ${dateFormat.format(Date(item.dueDateMillis))})"
                    else "Due: ${dateFormat.format(Date(item.dueDateMillis))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isOverdue) LossRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (item.isOverdue) FontWeight.Bold else FontWeight.Normal
                  )
                }

                if (item.reminderEnabled) {
                  Box(
                    modifier = Modifier
                      .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "Reminder Active",
                      fontSize = 10.sp,
                      color = GoldAccent,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Note: ${item.notes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Action buttons: Record Repayment & Add Money to Loan
              if (!item.isCompleted) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Button: Record Payment
                  Button(
                    onClick = { recordingPaymentFor = item },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = EmeraldLight.copy(alpha = 0.16f),
                      contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 42.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Payment,
                      contentDescription = null,
                      tint = EmeraldLight,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isKurdish) {
                        if (item.type == "DEBT") "+ دانەوە" else "+ وەرگرتن"
                      } else {
                        "+ Payment"
                      },
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  // Button: Add Money to Loan / Debt
                  Button(
                    onClick = { addingMoneyToDebt = item },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 42.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isKurdish) "+ پارێ زێدە" else "+ Add Money",
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  // Button: 1-Click Settle Full Balance
                  Button(
                    onClick = {
                      viewModel.settleDebtCreditInFull(
                        debtId = item.id,
                        notes = if (isKurdish) "تەسفییەکرنا تەمامی یا قەرزی" else "Full Balance Settled"
                      )
                    },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = GoldAccent.copy(alpha = 0.18f),
                      contentColor = GoldAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                      .heightIn(min = 42.dp)
                      .testTag("settle_full_loan_button_${item.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DoneAll,
                      contentDescription = null,
                      tint = GoldAccent,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isKurdish) "تەسفییە" else "Settle",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    ExtendedFloatingActionButton(
      onClick = {
        dialogInitialType = if (activeSubTab == "DEBT") "DEBT" else "CREDIT"
        showAddDialog = true
      },
      containerColor = if (activeSubTab == "DEBT") GoldAccent else EmeraldLight,
      contentColor = Color(0xFF0F172A),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_loan_debt_fab")
    ) {
      Icon(
        imageVector = Icons.Default.AttachMoney,
        contentDescription = null,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (isKurdish) {
          if (activeSubTab == "DEBT") "+ زێدەکرنا قەرزی (لسەر من)" else "+ زێدەکرنا قەرزی"
        } else {
          if (activeSubTab == "DEBT") "+ Add Money (Borrow)" else "+ Add Receivable / Debt"
        },
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }
  }

  // Add Debt / Credit Dialog
  if (showAddDialog) {
    AddDebtCreditDialog(
      initialType = dialogInitialType,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddDialog = false },
      onConfirmWithRate = { type, name, contact, total, paid, cur, dueMillis, notes, reminder, affectCash, customRate ->
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
        showAddDialog = false
      }
    )
  }

  // Edit Debt / Credit Dialog
  if (editingDebtCredit != null) {
    val itemToEdit = editingDebtCredit!!
    AddDebtCreditDialog(
      initialType = itemToEdit.type,
      displayCurrency = itemToEdit.currency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      existingEntity = itemToEdit,
      onDismiss = { editingDebtCredit = null },
      onConfirmWithRate = { type, name, contact, total, paid, cur, dueMillis, notes, reminder, affectCash, customRate ->
        viewModel.updateDebtCredit(
          id = itemToEdit.id,
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
        editingDebtCredit = null
      }
    )
  }

  // Record Repayment Dialog
  if (recordingPaymentFor != null) {
    val debt = recordingPaymentFor!!
    RecordPaymentDialog(
      debt = debt,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { recordingPaymentFor = null },
      onConfirm = { paymentAmt, notes, affectCash, customRate ->
        viewModel.recordLoanPayment(debt.id, paymentAmt, notes, affectCash, customRate)
        recordingPaymentFor = null
      }
    )
  }

  // Add Money to Loan / Debt Dialog
  if (addingMoneyToDebt != null) {
    val debt = addingMoneyToDebt!!
    AddMoneyToLoanDialog(
      debt = debt,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { addingMoneyToDebt = null },
      onConfirm = { additionalAmount, notes, affectCash, customRate ->
        viewModel.addMoneyToLoan(debt.id, additionalAmount, notes, affectCash, customRate)
        addingMoneyToDebt = null
      }
    )
  }

  // Direct Person Details Dialog (Opened on 1-click from Global Search or tapping person card)
  if (selectedDebtForDetails != null) {
    val liveDebt = uiState.debtsCredits.find { it.id == selectedDebtForDetails!!.id } ?: selectedDebtForDetails!!
    PersonDebtDetailsDialog(
      debt = liveDebt,
      uiState = uiState,
      isKurdish = isKurdish,
      dateFormat = dateFormat,
      onRecordPayment = {
        selectedDebtForDetails = null
        recordingPaymentFor = liveDebt
      },
      onAddMoney = {
        selectedDebtForDetails = null
        addingMoneyToDebt = liveDebt
      },
      onEditDebt = {
        selectedDebtForDetails = null
        viewModel.openQuickEditForDebt(liveDebt)
      },
      onSettleFull = {
        viewModel.settleDebtCreditInFull(
          debtId = liveDebt.id,
          notes = if (isKurdish) "تەسفییەکرنا تەمامی یا قەرزی" else "Full Balance Settled"
        )
        selectedDebtForDetails = null
      },
      onDeletePayment = { paymentId ->
        viewModel.deleteDebtPayment(paymentId)
      },
      onDelete = {
        viewModel.deleteDebtCredit(liveDebt.id)
        selectedDebtForDetails = null
      },
      onDismiss = { selectedDebtForDetails = null }
    )
  }
}

@Composable
fun PersonDebtDetailsDialog(
  debt: DebtCreditEntity,
  uiState: FinanceUiState,
  isKurdish: Boolean,
  dateFormat: SimpleDateFormat,
  onRecordPayment: () -> Unit,
  onAddMoney: () -> Unit,
  onEditDebt: () -> Unit = {},
  onSettleFull: () -> Unit = {},
  onDeletePayment: (Long) -> Unit = {},
  onDelete: () -> Unit,
  onDismiss: () -> Unit
) {
  val isCredit = debt.type == "CREDIT"
  val convertedTotal = CurrencyService.convert(debt.totalAmount, debt.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = debt.exchangeRateAtEntry)
  val convertedPaid = CurrencyService.convert(debt.amountPaid, debt.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = debt.exchangeRateAtEntry)
  val convertedRemaining = CurrencyService.convert(debt.remainingAmount, debt.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = debt.exchangeRateAtEntry)

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WoodCardSurface,
    modifier = Modifier
      .border(1.2.dp, WoodInlayGold.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
      .testTag("person_debt_details_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(
              if (isCredit) EmeraldLight.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.2f),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = if (isCredit) EmeraldLight else GoldAccent,
            modifier = Modifier.size(22.dp)
          )
        }
        Column {
          Text(
            text = debt.personName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = if (isCredit) {
              if (isKurdish) "قەرزێ دای (دەف خەلکی)" else "Money Lent (They Owe Me)"
            } else {
              if (isKurdish) "قەرزێ وەرگرتی (لسەر من)" else "Money Borrowed (I Owe)"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isCredit) EmeraldLight else GoldAccent,
            fontWeight = FontWeight.Bold
          )
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (debt.contactInfo.isNotBlank()) {
          Text(
            text = "${if (isKurdish) "پەیوەندی: " else "Contact: "}${debt.contactInfo}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isKurdish) "کۆیێ گشتی یێ قەرزی:" else "Total Loan:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyService.format(convertedTotal, uiState.displayCurrency),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isKurdish) "واصلبووی تا نوکە:" else "Paid So Far:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyService.format(convertedPaid, uiState.displayCurrency),
                fontWeight = FontWeight.Bold,
                color = EmeraldLight,
                style = MaterialTheme.typography.bodyMedium
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isKurdish) "باقیێ مای:" else "Remaining Balance:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = CurrencyService.format(convertedRemaining, uiState.displayCurrency),
                fontWeight = FontWeight.ExtraBold,
                color = if (debt.isCompleted) EmeraldLight else if (isCredit) GoldAccent else LossRed,
                style = MaterialTheme.typography.titleMedium
              )
            }
          }
        }

        Text(
          text = "${if (isKurdish) "مێژوویا دانەوەی: " else "Due Date: "}${dateFormat.format(Date(debt.dueDateMillis))}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (debt.notes.isNotBlank()) {
          Text(
            text = "${if (isKurdish) "تێبینی: " else "Notes: "}${debt.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        val debtPaymentsList = uiState.debtPayments.filter { it.debtId == debt.id }
        if (debtPaymentsList.isNotEmpty()) {
          Text(
            text = if (isKurdish) "دانەوەیێن قستان (${debtPaymentsList.size}):" else "Recorded Repayments (${debtPaymentsList.size}):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = EmeraldLight
          )
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            debtPaymentsList.forEach { payment ->
              val payRate = if (payment.exchangeRateAtEntry > 0.0) payment.exchangeRateAtEntry else debt.exchangeRateAtEntry
              val payConverted = CurrencyService.convert(payment.amount, debt.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = payRate)
              CyberRecordButton(
                title = CurrencyService.format(payConverted, uiState.displayCurrency),
                subtitle = payment.notes.ifBlank { if (isKurdish) "قست / دانەڤە" else "Installment Payment" },
                dateOrMeta = dateFormat.format(Date(payment.dateMillis)),
                primaryValue = CurrencyService.formatShort(payment.amount, debt.currency),
                accentColor = EmeraldLight,
                icon = Icons.Default.Payment,
                actionHintLabel = if (isKurdish) "ژێبرن ✕" else "Delete ✕",
                quickActionLabel = if (isKurdish) "ژێبرن" else "Delete",
                quickActionIcon = Icons.Default.DeleteOutline,
                quickActionColor = LossRed,
                onQuickActionClick = { onDeletePayment(payment.id) },
                onClick = { onDeletePayment(payment.id) },
                testTagKey = "delete_debt_payment_${payment.id}"
              )
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (!debt.isCompleted) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onRecordPayment,
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldLight,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isKurdish) "+ قست / واصل" else "+ Payment",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }

              Button(
                onClick = onAddMoney,
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldAccent,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isKurdish) "+ پارێ زێدە" else "+ Add Money",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
            Button(
              onClick = onSettleFull,
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent.copy(alpha = 0.22f),
                contentColor = GoldAccent
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "تەسفییەکرنا تەمامی" else "Settle in Full",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
              )
            }
          }

          Button(
            onClick = onEditDebt,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF38BDF8).copy(alpha = 0.18f),
              contentColor = Color(0xFF38BDF8)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKurdish) "دەستکاری و گوهۆڕین" else "Edit Debt",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
      ) {
        Text(if (isKurdish) "داخستن" else "Close")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDelete) {
        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isKurdish) "ژێبرن" else "Delete", color = LossRed)
      }
    }
  )
}

@Composable
fun AddDebtCreditDialog(
  initialType: String,
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  existingEntity: DebtCreditEntity? = null,
  onDismiss: () -> Unit,
  onConfirm: (
    type: String,
    name: String,
    contact: String,
    total: Double,
    paid: Double,
    currency: String,
    dueDateMillis: Long,
    notes: String,
    reminder: Boolean,
    affectCashBalance: Boolean
  ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
  onConfirmWithRate: ((
    type: String,
    name: String,
    contact: String,
    total: Double,
    paid: Double,
    currency: String,
    dueDateMillis: Long,
    notes: String,
    reminder: Boolean,
    affectCashBalance: Boolean,
    customRate: Double?
  ) -> Unit)? = null
) {
  var type by remember { mutableStateOf(existingEntity?.type ?: initialType) }
  var nameInput by remember { mutableStateOf(existingEntity?.personName ?: "") }
  var contactInput by remember { mutableStateOf(existingEntity?.contactInfo ?: "") }
  var totalAmountInput by remember { mutableStateOf(existingEntity?.totalAmount?.toString() ?: "") }
  var paidAmountInput by remember { mutableStateOf(existingEntity?.amountPaid?.toString() ?: "0") }
  var currency by remember { mutableStateOf(existingEntity?.currency ?: displayCurrency) }
  val initialRateVal = existingEntity?.exchangeRateAtEntry?.takeIf { it > 0.0 } ?: currentIqdRate
  var iqdRateInput by remember { mutableStateOf(initialRateVal.toLong().toString()) }
  var daysUntilDueInput by remember {
    val defaultDays = if (existingEntity != null) {
      ((existingEntity.dueDateMillis - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1L).toString()
    } else "30"
    mutableStateOf(defaultDays)
  }
  var notesInput by remember { mutableStateOf(existingEntity?.notes ?: "") }
  var reminderEnabled by remember { mutableStateOf(existingEntity?.reminderEnabled ?: true) }
  var affectCashBalance by remember { mutableStateOf(existingEntity?.affectCashBalance ?: true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WoodCardSurface,
    modifier = Modifier
      .border(1.2.dp, WoodInlayGold.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
      .testTag("add_debt_credit_dialog"),
    title = {
      Text(
        text = if (existingEntity != null) {
          if (isKurdish) "دەستکاریکرنا تۆمارا قەرزی" else "Edit Receivable / Debt Record"
        } else if (type == "CREDIT") {
          if (isKurdish) "تۆمارکرنا قەرزی (دەف خەلکی یە)" else "Record Money Lent (They Owe Me)"
        } else {
          if (isKurdish) "تۆمارکرنا قەرزی (لسەر منە)" else "Record Debt (I Owe Someone)"
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
        // Toggle type
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { type = "CREDIT" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (type == "CREDIT") EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 44.dp)
          ) {
            Text(
              text = if (isKurdish) "دەف خەلکی یە" else "They Owe Me",
              color = if (type == "CREDIT") Color.Black else MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Button(
            onClick = { type = "DEBT" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (type == "DEBT") GoldAccent else MaterialTheme.colorScheme.surfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 44.dp)
          ) {
            Text(
              text = if (isKurdish) "لسەر منە" else "I Owe Someone",
              color = if (type == "DEBT") Color.Black else MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = {
            Text(
              if (type == "DEBT") {
                if (isKurdish) "ناڤێ قەرزدەری / کۆمپانیایێ (Lender Name)" else "Lender Name / Institution"
              } else {
                if (isKurdish) "ناڤێ وەرگرێ قەرزی (Borrower / Person Name)" else "Borrower / Person Name (Lender: You)"
              }
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_person_input")
        )

        OutlinedTextField(
          value = contactInput,
          onValueChange = { contactInput = it },
          label = { Text(if (isKurdish) "ژمارا مۆبایلێ / پەیوەندی (ئارەزوومەندانە)" else "Contact Info / Phone (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            Button(
              onClick = { currency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (currency == cur) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                cur,
                color = if (currency == cur) Color.Black else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = totalAmountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = currency,
          isKurdish = isKurdish,
          testTag = "loan_iqd_rate_input"
        )

        OutlinedTextField(
          value = totalAmountInput,
          onValueChange = { totalAmountInput = it },
          label = { Text("${if (isKurdish) "کۆیێ گشتی یێ قەرزی" else "Total Loan Amount"} ($currency)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_amount_input")
        )

        // 1-Tap Quick Loan Amount Chips (بۆ داخلکرنا ب ساناهی ب ئێک کلیک)
        val quickLoanPresets = if (currency == "USD") {
          listOf(100.0, 500.0, 1000.0, 5000.0)
        } else {
          listOf(100000.0, 250000.0, 500000.0, 1000000.0)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          quickLoanPresets.forEach { preset ->
            val label = if (currency == "USD") "+$${preset.toLong()}" else "+${preset.toLong() / 1000}K"
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(GoldAccent.copy(alpha = 0.16f))
                .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .clickable {
                  val current = totalAmountInput.toDoubleOrNull() ?: 0.0
                  val next = current + preset
                  totalAmountInput = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
              )
            }
          }
        }

        OutlinedTextField(
          value = paidAmountInput,
          onValueChange = { paidAmountInput = it },
          label = { Text("${if (isKurdish) "بڕێ واصلبووی یێ دەستپێکێ" else "Initial Amount Repaid"} ($currency)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_paid_input")
        )

        val previewTotal = totalAmountInput.toDoubleOrNull() ?: 0.0
        val previewPaid = paidAmountInput.toDoubleOrNull() ?: 0.0
        val previewRemaining = (previewTotal - previewPaid).coerceAtLeast(0.0)
        if (previewTotal > 0.0) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("loan_remaining_balance_preview"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) "باقیێ مایی (Remaining Balance):" else "Remaining Balance:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = CurrencyService.format(previewRemaining, currency),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.ExtraBold,
              color = if (type == "DEBT") LossRed else EmeraldLight
            )
          }
        }

        OutlinedTextField(
          value = daysUntilDueInput,
          onValueChange = { daysUntilDueInput = it },
          label = { Text(if (isKurdish) "دەمێ دانەوەی (چەند رۆژێن دی)" else "Due In (Days from today)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // 1-Tap Quick Due Date Presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "7" to (if (isKurdish) "٧ ڕۆژ" else "7d"),
            "15" to (if (isKurdish) "١٥ ڕۆژ" else "15d"),
            "30" to (if (isKurdish) "١ هەیڤ" else "30d"),
            "90" to (if (isKurdish) "٣ هەیڤ" else "90d")
          ).forEach { (daysVal, label) ->
            val isSelectedDays = daysUntilDueInput == daysVal
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelectedDays) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { daysUntilDueInput = daysVal }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelectedDays) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Cash Balance Impact Selector (وەختێ قەرزەکی دەم یان وەرگرم ژ کاشی کێم ببیت یان زێدە ببیت یان نە)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (affectCashBalance) {
                if (type == "CREDIT") LossRed.copy(alpha = 0.12f) else EmeraldLight.copy(alpha = 0.12f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              }
            )
            .border(
              width = 1.dp,
              color = if (affectCashBalance) {
                if (type == "CREDIT") LossRed.copy(alpha = 0.45f) else EmeraldLight.copy(alpha = 0.45f)
              } else {
                WoodBorder
              },
              shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (type == "CREDIT") {
                  if (isKurdish) "💵 ژ سەروەتێ کاشی کێم ببیت؟" else "💵 Deduct Loan from Cash Balance?"
                } else {
                  if (isKurdish) "💵 ل سەروەتێ کاشی زێدە ببیت؟" else "💵 Add Borrowed Loan to Cash Balance?"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (affectCashBalance) {
                  if (type == "CREDIT") LossRed else EmeraldLight
                } else {
                  MaterialTheme.colorScheme.onSurface
                }
              )
              Text(
                text = if (type == "CREDIT") {
                  if (isKurdish) "دەمێ قەرزەکی ددەی، ژ کاشێ تە بهێتە کێمکرن یان نە" else "Choose if lending this money reduces your Cash"
                } else {
                  if (isKurdish) "دەمێ قەرزەکی وەردگری، ل کاشێ تە بهێتە زێدەکرن یان نە" else "Choose if borrowing this money increases your Cash"
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = affectCashBalance,
              onCheckedChange = { affectCashBalance = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = if (type == "CREDIT") LossRed else EmeraldLight
              ),
              modifier = Modifier.testTag("loan_affect_cash_switch")
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { affectCashBalance = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (affectCashBalance) {
                  if (type == "CREDIT") LossRed else EmeraldLight
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                }
              ),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 38.dp)
                .testTag("loan_affect_cash_yes_button")
            ) {
              Text(
                text = if (type == "CREDIT") {
                  if (isKurdish) "بەلێ — ژ کاشی کێم ببیت" else "Yes — Deduct Cash"
                } else {
                  if (isKurdish) "بەلێ — ل کاشی زێدە ببیت" else "Yes — Add to Cash"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (affectCashBalance) {
                  if (type == "CREDIT") Color.White else Color(0xFF0F172A)
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Button(
              onClick = { affectCashBalance = false },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (!affectCashBalance) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
              ),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 38.dp)
                .testTag("loan_affect_cash_no_button")
            ) {
              Text(
                text = if (isKurdish) "نە — بێی کاش" else "No — Keep Cash",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!affectCashBalance) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isKurdish) "ئاگادارکەرەوەیا دانەوەیێ" else "Enable Payment Reminders",
            style = MaterialTheme.typography.bodyMedium
          )
          Switch(
            checked = reminderEnabled,
            onCheckedChange = { reminderEnabled = it },
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight)
          )
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی / رێککەفتن" else "Notes / Loan Agreement Details") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val total = totalAmountInput.toDoubleOrNull() ?: 0.0
          val paid = paidAmountInput.toDoubleOrNull() ?: 0.0
          val days = daysUntilDueInput.toLongOrNull() ?: 30L
          val dueMillis = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
          val customRate = CurrencyService.normalizeIqdRatePerUsd(
            iqdRateInput.toDoubleOrNull() ?: currentIqdRate,
            currentIqdRate
          )

          if (nameInput.isNotBlank() && total > 0) {
            if (onConfirmWithRate != null) {
              onConfirmWithRate(type, nameInput, contactInput, total, paid, currency, dueMillis, notesInput, reminderEnabled, affectCashBalance, customRate)
            } else {
              onConfirm(type, nameInput, contactInput, total, paid, currency, dueMillis, notesInput, reminderEnabled, affectCashBalance)
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
        modifier = Modifier.testTag("confirm_add_loan_button")
      ) {
        Text(if (isKurdish) "تۆمارکرنا قەرزی" else "Save Loan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

@Composable
fun RecordPaymentDialog(
  debt: DebtCreditEntity,
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, notes: String, affectCashBalance: Boolean, customIqdRate: Double) -> Unit
) {
  var paymentInput by remember { mutableStateOf(debt.remainingAmount.toString()) }
  var notesInput by remember { mutableStateOf(if (isKurdish) "پارێ هاتیەدان" else "Installment payment") }
  var affectCashBalance by remember { mutableStateOf(debt.affectCashBalance) }
  val initialRate = if (debt.exchangeRateAtEntry > 0.0) debt.exchangeRateAtEntry else currentIqdRate
  var entryIqdRateInput by remember(initialRate) { mutableStateOf(initialRate.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WoodCardSurface,
    modifier = Modifier
      .border(1.2.dp, WoodInlayGold.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
      .testTag("record_payment_dialog"),
    title = {
      Text(
        text = if (debt.type == "CREDIT") {
          if (isKurdish) "وەرگرتنا پارەی ژ ${debt.personName}" else "Collect Payment from ${debt.personName}"
        } else {
          if (isKurdish) "دانەوەیا قەرزی بۆ ${debt.personName}" else "Repay Debt to ${debt.personName}"
        },
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "${if (isKurdish) "باقیێ مای: " else "Remaining Balance: "}${CurrencyService.format(debt.remainingAmount, debt.currency)}",
          fontWeight = FontWeight.Bold,
          color = EmeraldLight
        )

        OutlinedTextField(
          value = paymentInput,
          onValueChange = { paymentInput = it },
          label = { Text("${if (isKurdish) "بڕێ پارێ وەرگرتی / دراوە" else "Payment Amount"} (${debt.currency})") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_amount_input")
        )

        // 1-Tap Quick Repayment Percentage & Amount Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            0.25 to "25%",
            0.50 to (if (isKurdish) "50% نیڤی" else "50% Half"),
            0.75 to "75%",
            1.00 to (if (isKurdish) "100% تەمام" else "100% Full")
          ).forEach { (ratio, label) ->
            val presetAmt = debt.remainingAmount * ratio
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldLight.copy(alpha = 0.16f))
                .border(0.8.dp, EmeraldLight.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .clickable {
                  paymentInput = if (presetAmt % 1.0 == 0.0) presetAmt.toLong().toString() else "%.2f".format(Locale.US, presetAmt)
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLight
              )
            }
          }
        }

        val parsedPayment = paymentInput.toDoubleOrNull() ?: 0.0
        if (parsedPayment > 0.0) {
          val newRemaining = (debt.remainingAmount - parsedPayment).coerceAtLeast(0.0)
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isKurdish) "باقیێ نوێ دێ مینیت:" else "New Remaining Balance:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyService.format(newRemaining, debt.currency),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
              )
            }
            if (affectCashBalance) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isKurdish) "بەس ئەڤ بڕە دێ کار کەتە سەر کاشی:" else "Incremental Cash Impact Only:",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${if (debt.type == "CREDIT") "+" else "−"}${CurrencyService.format(parsedPayment, debt.currency)}",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (debt.type == "CREDIT") EmeraldLight else LossRed
                )
              }
            }
          }
        }

        // Cash Balance Impact Toggle for this Payment
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (affectCashBalance) {
                if (debt.type == "CREDIT") EmeraldLight.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (debt.type == "CREDIT") {
                if (isKurdish) "💵 ئەڤ پارێ وەرگرتی ل کاشی زێدە ببیت؟" else "💵 Add Collected Amount to Cash?"
              } else {
                if (isKurdish) "💵 ئەڤ پارێ دراوە ژ کاشی کێم ببیت؟" else "💵 Deduct Repaid Amount from Cash?"
              },
              fontSize = 11.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (affectCashBalance) {
                if (debt.type == "CREDIT") EmeraldLight else LossRed
              } else {
                MaterialTheme.colorScheme.onSurface
              }
            )
            Text(
              text = if (affectCashBalance) {
                if (isKurdish) "بەس ئەڤ بڕە دهێتە گوهۆڕین ل کاشی (نە کۆیێ گشتی)" else "Only this payment amount changes your Cash"
              } else {
                if (isKurdish) "بێی دەستکاریا کاشی" else "No change to Cash balance"
              },
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = affectCashBalance,
            onCheckedChange = { affectCashBalance = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = if (debt.type == "CREDIT") EmeraldLight else LossRed
            )
          )
        }

        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = initialRate,
          enteredAmount = paymentInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = debt.currency,
          isKurdish = isKurdish,
          testTag = "repayment_entry_iqd_rate_input"
        )

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی / ژمارا پسوولێ" else "Payment Notes / Receipt") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = paymentInput.toDoubleOrNull() ?: 0.0
          val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
            entryIqdRateInput.toDoubleOrNull() ?: initialRate,
            initialRate
          )
          if (amt > 0) {
            onConfirm(amt, notesInput, affectCashBalance, lockedRate)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
        modifier = Modifier.testTag("confirm_repayment_button")
      ) {
        Text(if (isKurdish) "تەسجیل بکە" else "Confirm Payment")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

@Composable
fun AddMoneyToLoanDialog(
  debt: DebtCreditEntity,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, notes: String, affectCashBalance: Boolean, customIqdRate: Double) -> Unit
) {
  var amountInput by remember { mutableStateOf("") }
  var notesInput by remember { mutableStateOf(if (isKurdish) "پارێ زێدەکرنێ" else "Additional amount") }
  var affectCashBalance by remember { mutableStateOf(debt.affectCashBalance) }
  val initialRate = if (debt.exchangeRateAtEntry > 0.0) debt.exchangeRateAtEntry else currentIqdRate
  var entryIqdRateInput by remember(initialRate) { mutableStateOf(initialRate.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WoodCardSurface,
    modifier = Modifier
      .border(1.2.dp, WoodInlayGold.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
      .testTag("add_money_to_debt_dialog"),
    title = {
      Text(
        text = if (debt.type == "CREDIT") {
          if (isKurdish) "زێدەکرنا پارەی لسەر قەرزی (${debt.personName})" else "Add Money to Loan (${debt.personName})"
        } else {
          if (isKurdish) "زێدەکرنا پارەی لسەر دەینی (${debt.personName})" else "Add Money to Debt (${debt.personName})"
        },
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = if (debt.type == "CREDIT") {
            if (isKurdish) "پارێ زێدە هاتە دان بۆ ${debt.personName}" else "Lending additional money to ${debt.personName}"
          } else {
            if (isKurdish) "قەرزێ زێدە هاتە وەرگرتن ژ ${debt.personName}" else "Borrowing additional money from ${debt.personName}"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "${if (isKurdish) "کۆیێ نوکە: " else "Current Total: "}${CurrencyService.format(debt.totalAmount, debt.currency)} (${if (isKurdish) "نەدایە: " else "Unpaid: "}${CurrencyService.format(debt.remainingAmount, debt.currency)})",
          fontWeight = FontWeight.Bold,
          color = if (debt.type == "CREDIT") EmeraldLight else GoldAccent
        )

        OutlinedTextField(
          value = amountInput,
          onValueChange = { amountInput = it },
          label = { Text("${if (isKurdish) "بڕێ پارێ زێدەکری" else "Additional Amount"} (${debt.currency})") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_money_amount_input")
        )

        // 1-Tap Quick Incremental Presets (+100, +250, +500, +1000)
        val addPresets = if (debt.currency == "USD") {
          listOf(100.0, 250.0, 500.0, 1000.0)
        } else {
          listOf(50000.0, 100000.0, 250000.0, 500000.0)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          addPresets.forEach { preset ->
            val label = if (debt.currency == "USD") "+$${preset.toLong()}" else "+${preset.toLong() / 1000}K"
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(GoldAccent.copy(alpha = 0.16f))
                .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .clickable {
                  val current = amountInput.toDoubleOrNull() ?: 0.0
                  val next = current + preset
                  amountInput = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
              )
            }
          }
        }

        val addedAmt = amountInput.toDoubleOrNull() ?: 0.0
        if (addedAmt > 0.0) {
          val newTotalLoan = debt.totalAmount + addedAmt
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isKurdish) "کۆیێ نوێ یێ قەرزی:" else "New Total Loan:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyService.format(newTotalLoan, debt.currency),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
              )
            }
            if (affectCashBalance) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isKurdish) "گوهۆڕینا کاشی (بەس ئەڤ بڕێ زێدە):" else "Cash Change (Only Incremental Amount):",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${if (debt.type == "CREDIT") "−" else "+"}${CurrencyService.format(addedAmt, debt.currency)}",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (debt.type == "CREDIT") LossRed else EmeraldLight
                )
              }
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = initialRate,
          enteredAmount = amountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = debt.currency,
          isKurdish = isKurdish,
          testTag = "add_loan_money_iqd_rate_input"
        )

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "ئەگەر / تێبینی" else "Reason / Note") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Cash Balance Impact Toggle for Additional Loan Amount
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (affectCashBalance) {
                if (debt.type == "CREDIT") LossRed.copy(alpha = 0.12f) else EmeraldLight.copy(alpha = 0.12f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (debt.type == "CREDIT") {
                if (isKurdish) "💵 بەس ئەڤ بڕە ژ کاشی کێم ببیت؟" else "💵 Deduct Only This Added Amount from Cash?"
              } else {
                if (isKurdish) "💵 بەس ئەڤ بڕە ل کاشی زێدە ببیت؟" else "💵 Add Only This Added Amount to Cash?"
              },
              fontSize = 11.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (affectCashBalance) {
                if (debt.type == "CREDIT") LossRed else EmeraldLight
              } else {
                MaterialTheme.colorScheme.onSurface
              }
            )
            Text(
              text = if (affectCashBalance) {
                if (debt.type == "CREDIT") {
                  if (isKurdish) "بەلێ — بەس ئەڤ بڕێ نوێ ژ کاشی کێم دبیت (نە کۆیێ گشتی)" else "Yes — Only this added amount deducts from Cash"
                } else {
                  if (isKurdish) "بەلێ — بەس ئەڤ بڕێ نوێ ل کاشی زێدە دبیت (نە کۆیێ گشتی)" else "Yes — Only this added amount adds to Cash"
                }
              } else {
                if (isKurdish) "نە — بێی دەستکاریا کاشی" else "No — Does not change Cash balance"
              },
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = affectCashBalance,
            onCheckedChange = { affectCashBalance = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = if (debt.type == "CREDIT") LossRed else EmeraldLight
            )
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
            entryIqdRateInput.toDoubleOrNull() ?: initialRate,
            initialRate
          )
          if (amt > 0) {
            onConfirm(amt, notesInput, affectCashBalance, lockedRate)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (debt.type == "CREDIT") EmeraldLight else GoldAccent
        ),
        modifier = Modifier.testTag("confirm_add_money_button")
      ) {
        Text(
          text = if (isKurdish) "+ زێدە بکە" else "+ Add Money",
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}
