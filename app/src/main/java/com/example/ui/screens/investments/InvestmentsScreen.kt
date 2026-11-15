package com.example.ui.screens.investments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.ActiveDebtCreditLoan
import com.example.data.local.InvestmentEntity
import com.example.data.local.validatePartnershipShares
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.AssetDetailActionDialog
import com.example.ui.components.CyberNeonCategoryButton
import com.example.ui.components.EntryExchangeRateCard
import com.example.ui.components.ProfitTrendChart
import com.example.ui.components.ProfitTrendPoint
import com.example.ui.components.SectionQuickHistoryStrip
import com.example.ui.history.ActionSection
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvestmentsScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onNavigateToCars: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  var showAddInvestmentDialog by remember { mutableStateOf(false) }
  var dialogPresetCategory by remember { mutableStateOf("Real Estate") }
  var dialogPresetName by remember { mutableStateOf("") }
  var dialogPresetNotes by remember { mutableStateOf("") }
  var dialogPresetIsCoOwned by remember { mutableStateOf(false) }
  var dialogPresetInitialCap by remember { mutableStateOf("") }
  var dialogCustomTitle by remember { mutableStateOf<String?>(null) }
  var selectedCategoryFilter by remember { mutableStateOf("ALL") }
  var selectedInvestmentForGainLoss by remember { mutableStateOf<InvestmentEntity?>(null) }
  var selectedInvestmentForSale by remember { mutableStateOf<InvestmentEntity?>(null) }
  var showAddRealEstateDebtDialog by remember { mutableStateOf(false) }
  var realEstateDebtPresetType by remember { mutableStateOf("CREDIT") }
  var selectedRealEstateLoanForAction by remember { mutableStateOf<ActiveDebtCreditLoan?>(null) }
  var selectedInvestmentForDetail by remember { mutableStateOf<InvestmentEntity?>(null) }

  LaunchedEffect(uiState.focusedEntityType, uiState.focusedEntityId, uiState.nonCarInvestments) {
    if (uiState.focusedEntityType == "INVESTMENT" && uiState.focusedEntityId != null) {
      val targetInv = uiState.nonCarInvestments.find { it.id == uiState.focusedEntityId }
      if (targetInv != null) {
        selectedCategoryFilter = "ALL"
        selectedInvestmentForDetail = targetInv
        viewModel.clearFocusedSearchResult()
      }
    }
  }

  // Build profit trend points from monthly records & investments
  val trendPoints = remember(uiState.monthlyRecords, uiState.investments, uiState.displayCurrency, uiState.iqdRate) {
    if (uiState.monthlyRecords.isNotEmpty()) {
      val groupedByMonth = uiState.monthlyRecords.groupBy { it.yearMonth }.toSortedMap()
      var cumulative = 0.0
      groupedByMonth.map { (month, records) ->
        val monthGain = records.sumOf {
          CurrencyService.convert(it.gainLossAmount, it.currency, uiState.displayCurrency, uiState.iqdRate)
        }
        cumulative += monthGain
        ProfitTrendPoint(
          label = month.takeLast(5), // e.g. "08" or "09"
          gainLoss = monthGain,
          cumulativeProfit = cumulative
        )
      }
    } else {
      // Fallback: estimate from investments
      var cumulative = 0.0
      val baseProfit = uiState.totalInvestmentProfit
      listOf(
        ProfitTrendPoint("Month 1", baseProfit * 0.2, baseProfit * 0.2),
        ProfitTrendPoint("Month 2", baseProfit * 0.3, baseProfit * 0.5),
        ProfitTrendPoint("Month 3", baseProfit * 0.25, baseProfit * 0.75),
        ProfitTrendPoint("Current", baseProfit * 0.25, baseProfit)
      )
    }
  }

  Box(
    modifier = modifier.fillMaxSize()
  ) {
    val filteredInvestments = remember(uiState.nonCarInvestments, selectedCategoryFilter) {
      when (selectedCategoryFilter) {
        "HOUSES" -> uiState.nonCarInvestments.filter { it.isHouse }
        "LANDS" -> uiState.nonCarInvestments.filter { it.isLand }
        "COMMERCIAL" -> uiState.nonCarInvestments.filter { it.isCommercial }
        "GOLD" -> uiState.nonCarInvestments.filter { it.isGoldValuable }
        "VENTURE" -> uiState.nonCarInvestments.filter { !it.isRealEstate && !it.isGoldValuable }
        else -> uiState.nonCarInvestments
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Global Master Capital Connection Banner (Dark Glass)
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38CBD5E1)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.linearGradient(
                  colors = listOf(
                    Color(0xF60F172A),
                    Color(0xEE0A0F1A)
                  )
                )
              )
              .padding(18.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Hub,
                      contentDescription = "Global Capital Hub",
                      tint = Color(0xFF38BDF8),
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = if (isKurdish) "سەرمایەیا گشتی یا مەزن" else "GLOBAL MASTER CAPITAL",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF38BDF8),
                      letterSpacing = 1.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = if (isKurdish) "گرێدای ب هەمی پرۆژە و وەبەرهێنانان ڤە" else "Connected to All Investment Projects",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White.copy(alpha = 0.85f)
                    )
                  }
                }

                Text(
                  text = CurrencyService.format(uiState.masterCapital, uiState.displayCurrency),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Capital Allocation Breakdown: Real Estate vs Ventures vs Available
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF141F36), RoundedCornerShape(12.dp))
                  .border(0.8.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(if (isKurdish) "🏢 ملوک (بەشێ من)" else "🏢 Real Estate (My Share)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8))
                  Text(
                    text = CurrencyService.formatShort(uiState.totalInvestedInRealEstate, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }

                Column {
                  Text(if (isKurdish) "💼 پرۆژە و کار" else "💼 Ventures", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                  Text(
                    text = CurrencyService.formatShort(uiState.totalInvestedInVentures, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(if (isKurdish) "💼 پارێ نەبەلاڤکری" else "💼 Unallocated Cash", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                  Text(
                    text = CurrencyService.formatShort(uiState.liquidAvailableCapital, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }
          }
        }
      }

      // 2. Master Independent Assets Portfolio Hub (Real Estate & Gold) - Dark Smoky Glass
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.65f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color(0xF60F172A),
                    Color(0xEE090E17)
                  )
                )
              )
              .padding(20.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = if (isKurdish) "💎 سەروەتێ ملوک، زێڕ و قەرزێن تایبەت" else "💎 PROPERTIES, GOLD & PRIVATE CREDITS",
                      style = MaterialTheme.typography.labelSmall,
                      color = GoldAccent,
                      letterSpacing = 0.8.sp,
                      fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = if (isKurdish) "تنێ بەشێ من (سەربەخۆ)" else "MY EQUITY ONLY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = CurrencyService.format(uiState.totalIndependentWealth, uiState.displayCurrency),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }

                Box(
                  modifier = Modifier
                    .background(
                      Color(0xFF38BDF8).copy(alpha = 0.2f),
                      RoundedCornerShape(10.dp)
                    )
                    .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = if (isKurdish) "بەشێ من تنێ" else "Personal Share",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Clean Breakdown: My Real Estate + My Gold + Credits Owed to Me + Partner Share (Informational)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF141F36), RoundedCornerShape(12.dp))
                  .border(0.8.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "🏢 ملوک (بەشێ من)" else "🏢 Properties (My Share)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8)
                  )
                  Text(
                    text = CurrencyService.formatShort(uiState.myRealEstateValuation, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }

                Column {
                  Text(
                    text = if (isKurdish) "🪙 زێڕ (عالمي)" else "🪙 Spot Gold",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent
                  )
                  Text(
                    text = CurrencyService.formatShort(uiState.goldValuablesInvestmentsValuation, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                  )
                }

                Column {
                  Text(
                    text = if (isKurdish) "📥 قەرزێ ل دەڤ خەلکی (تایبەت)" else "📥 Private Credits Owed",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldLight
                  )
                  Text(
                    text = "+${CurrencyService.formatShort(uiState.totalPrivateAccountCreditsReceivable, uiState.displayCurrency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                  )
                }

                if (uiState.totalPartnerShareInRealEstate > 0.0) {
                  Column {
                    Text(
                      text = if (isKurdish) "🤝 پارێ شەریکان" else "🤝 Partner Share",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                      text = CurrencyService.formatShort(uiState.totalPartnerShareInRealEstate, uiState.displayCurrency),
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White.copy(alpha = 0.75f)
                    )
                  }
                }

                if (uiState.totalRealEstateDebtsPayable > 0.0) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = if (isKurdish) "📤 قەرزێ لسەر ملوکان" else "📤 Debt on Asset",
                      style = MaterialTheme.typography.labelSmall,
                      color = LossRed
                    )
                    Text(
                      text = "−${CurrencyService.formatShort(uiState.totalRealEstateDebtsPayable, uiState.displayCurrency)}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = LossRed
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 3. Quick Action Buttons: Sub-categories (Houses, Lands, Commercial, Gold Valuables)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38CBD5E1)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkGlassCardSurface)
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "زێدەکرنا لەزگین یا سامانێن سەربەخۆ" else "QUICK ADD INDEPENDENT ASSETS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = GoldAccent
              )
              Text(
                text = if (isKurdish) "عەقارات و ملوک" else "Real Estate & Properties",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldLight
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // 1. Add House / Apartment
              Button(
                onClick = {
                  dialogPresetCategory = "Real Estate - House"
                  dialogPresetName = "خانوو / شوقە"
                  dialogPresetNotes = "Residential property"
                  dialogCustomTitle = if (isKurdish) "زێدەکرنا خانوو یان شوقەیێ" else "Add House or Apartment"
                  showAddInvestmentDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).height(46.dp)
              ) {
                Text(if (isKurdish) "🏡 خانوو" else "🏡 House", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
              }

              // 2. Add Land / Plot
              Button(
                onClick = {
                  dialogPresetCategory = "Real Estate - Land"
                  dialogPresetName = "ئەرد / زەوی"
                  dialogPresetNotes = "Land plot investment"
                  dialogCustomTitle = if (isKurdish) "زێدەکرنا ئەرد و زەڤیێ" else "Add Land Plot"
                  showAddInvestmentDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).height(46.dp)
              ) {
                Text(if (isKurdish) "🗺️ ئەرد" else "🗺️ Land", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
              }

              // 3. Add Commercial Building
              Button(
                onClick = {
                  dialogPresetCategory = "Real Estate - Commercial"
                  dialogPresetName = "بینا / جهێ بازرگانی"
                  dialogPresetNotes = "Commercial building / plaza"
                  dialogCustomTitle = if (isKurdish) "زێدەکرنا بینایێ بازرگانی" else "Add Commercial Building"
                  showAddInvestmentDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).height(46.dp)
              ) {
                Text(if (isKurdish) "🏢 بازرگانی" else "🏢 Commercial", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
              }

              // 4. Add Gold (World Spot)
              Button(
                onClick = {
                  dialogPresetCategory = "Real Estate - Gold"
                  dialogPresetName = if (isKurdish) "زێڕێ حسابا تایبەت" else "Private Account Gold"
                  dialogPresetNotes = "Live spot gold holding"
                  dialogCustomTitle = if (isKurdish) "زێدەکرنا زێڕ (بهایێ عالمي یێ نوکە)" else "Add Gold (Live World Spot Rate)"
                  showAddInvestmentDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f).height(46.dp)
              ) {
                Text(if (isKurdish) "🪙 زێڕ" else "🪙 Gold", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
              }
            }
          }
        }
      }

      // 3B. Dedicated Standalone Real Estate Debt & Credit Ledger (حسابا قەرز و دەینێن عەقاری)
      item {
        RealEstateDebtLedgerCard(
          uiState = uiState,
          isKurdish = isKurdish,
          onAddNewLoan = { type ->
            realEstateDebtPresetType = type
            showAddRealEstateDebtDialog = true
          },
          onRecordPayment = { loan ->
            selectedRealEstateLoanForAction = loan
          },
          onSettleLoan = { loan ->
            viewModel.settleDebtCreditInFull(loan.id, notes = "Real estate full settlement", affectCashBalance = false)
          },
          onDeleteLoan = { loan ->
            viewModel.deleteDebtCredit(loan.id)
          },
          onToggleClassification = { loan ->
            viewModel.toggleDebtRealEstateClassification(loan.id)
          }
        )
      }

      // Profit Trends Visualization Curve
      item {
        ProfitTrendChart(
          points = trendPoints,
          displayCurrency = uiState.displayCurrency,
          isKurdish = isKurdish
        )
      }

      // Sub-category Filter Chips (Horizontally Scrollable)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) "سامانێن سەربەخۆ و پرۆژە (${filteredInvestments.size})" else "Tracked Independent Assets (${filteredInvestments.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isKurdish) "لێ بدە بۆ نویژەنکرنا بهای" else "Tap to update valuation",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf(
              Triple("ALL", if (isKurdish) "🏢 هەمی ملوک" else "🏢 All Assets", filteredInvestments.size),
              Triple("HOUSES", if (isKurdish) "🏡 خانوو" else "🏡 Houses", uiState.realEstateHousesInvestments.size),
              Triple("LANDS", if (isKurdish) "🗺️ ئەرد و زەوی" else "🗺️ Lands", uiState.realEstateLandsInvestments.size),
              Triple("COMMERCIAL", if (isKurdish) "🏢 بازرگانی" else "🏢 Commercial", uiState.realEstateCommercialInvestments.size),
              Triple("GOLD", if (isKurdish) "🪙 زێڕ" else "🪙 Gold", uiState.goldValuablesInvestments.size),
              Triple("VENTURE", if (isKurdish) "💼 پرۆژە" else "💼 Ventures", uiState.nonCarInvestments.count { !it.isRealEstate && !it.isGoldValuable })
            ).forEach { (key, label, count) ->
              val isSelected = selectedCategoryFilter == key
              CyberNeonCategoryButton(
                label = label,
                accentColor = when (key) {
                  "ALL" -> Color(0xFF38BDF8)
                  "HOUSES", "LANDS" -> Color(0xFF00FFA3)
                  "COMMERCIAL" -> Color(0xFF8B5CF6)
                  "GOLD" -> Color(0xFFFFD700)
                  else -> Color(0xFF00F2FE)
                },
                isSelected = isSelected,
                count = count.takeIf { it > 0 },
                onClick = { selectedCategoryFilter = key },
                testTagKey = "investments_filter_button_${key.lowercase()}"
              )
            }
          }
        }
      }

      if (filteredInvestments.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38CBD5E1)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkGlassCardSurface)
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isKurdish) "چ سامان د ڤی بەشی دا نینن. ل دوگمەیێن سەری بدە بۆ تۆمارکرنا خانوو، ئەرد یان بینایا بازرگانی." else "No assets tracked in this category. Tap the buttons above to add houses, lands, or commercial properties.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
              )
            }
          }
        }
      } else {
        items(filteredInvestments, key = { it.id }) { inv ->
          val isGold = inv.isGoldValuable
          val convertedInitial = CurrencyService.convert(inv.initialCapital, inv.currency, uiState.displayCurrency, uiState.iqdRate)
          val convertedCurrent = if (isGold) {
            val liveUsd = inv.goldLiveValuationUsd(uiState.effectiveGoldPricePerGram24K)
            CurrencyService.convert(liveUsd, "USD", uiState.displayCurrency, uiState.iqdRate)
          } else {
            CurrencyService.convert(inv.currentValue, inv.currency, uiState.displayCurrency, uiState.iqdRate)
          }
          val convertedGain = convertedCurrent - convertedInitial
          val roi = if (convertedInitial > 0.0) (convertedGain / convertedInitial) * 100.0 else 0.0
          val isRealEstate = inv.isRealEstate

          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x38CBD5E1)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedInvestmentForDetail = inv }
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkGlassCardSurface)
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  val catIcon = when {
                    inv.isHouse -> Icons.Default.Home
                    inv.isLand -> Icons.Default.Place
                    inv.isCommercial -> Icons.Default.Apartment
                    inv.isGoldValuable -> Icons.Default.Star
                    inv.isRealEstate -> Icons.Default.Apartment
                    roi >= 0 -> Icons.Default.TrendingUp
                    else -> Icons.Default.TrendingDown
                  }
                  val catColor = when {
                    inv.isHouse -> Color(0xFF38BDF8)
                    inv.isLand -> EmeraldLight
                    inv.isCommercial -> Color(0xFFF97316)
                    inv.isGoldValuable -> GoldAccent
                    inv.isRealEstate -> Color(0xFF38BDF8)
                    roi >= 0 -> EmeraldLight
                    else -> LossRed
                  }

                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .background(catColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = catIcon,
                      contentDescription = null,
                      tint = catColor,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      Text(
                        text = inv.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                      )

                      val subCategoryBadge = when {
                        inv.isHouse -> if (isKurdish) "🏡 خانوو" else "🏡 House"
                        inv.isLand -> if (isKurdish) "🗺️ ئەرد" else "🗺️ Land"
                        inv.isCommercial -> if (isKurdish) "🏢 بازرگانی" else "🏢 Commercial"
                        inv.isGoldValuable -> if (isKurdish) "🪙 زێڕ" else "🪙 Gold"
                        inv.isRealEstate -> if (isKurdish) "🏢 ملوک" else "🏢 Property"
                        else -> null
                      }
                      if (subCategoryBadge != null) {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = subCategoryBadge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = catColor,
                            maxLines = 1
                          )
                        }
                      }

                      if (inv.isSold) {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldLight.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = if (isKurdish) "+ کاش (فرۆتی)" else "SOLD (+Cash)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldLight,
                            maxLines = 1
                          )
                        }
                      } else if (inv.deductedFromCash) {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = if (isKurdish) "− ژ کاشی" else "− From Cash",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent,
                            maxLines = 1
                          )
                        }
                      }
                    }
                    Text(
                      text = if (inv.isGoldValuable) {
                        val effW = inv.goldEffectiveWeightGrams(uiState.effectiveGoldPricePerGram24K)
                        val mVal = if (inv.goldWeightMithqal > 0.0) inv.goldWeightMithqal else (effW / 5.0)
                        "🪙 ${"%.2f".format(mVal)} مسقال (${"%.1f".format(effW)}g) • عیار ${inv.goldKarat}K • بهایێ عالمي"
                      } else {
                        "${inv.category} • Orig: ${CurrencyService.formatShort(inv.initialCapital, inv.currency)}"
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = if (inv.isGoldValuable) GoldAccent else Color.White.copy(alpha = 0.7f),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  IconButton(
                    onClick = { viewModel.openQuickEditForInvestment(inv) },
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(GoldAccent.copy(alpha = 0.14f))
                      .testTag("quick_edit_investment_button_${inv.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit & Preview",
                      tint = GoldAccent,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.deleteInvestment(inv.id) },
                    modifier = Modifier
                      .size(34.dp)
                      .testTag("delete_investment_button_${inv.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = "Delete",
                      tint = LossRed.copy(alpha = 0.85f),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }

              if (inv.notes.isNotBlank() && !inv.notes.startsWith("[DEBT:")) {
                val cleanNotes = inv.notes
                  .replace(Regex("""\[(?:DEBT|INDEPENDENT_DEBT|قەرز):[0-9.]*\]"""), "")
                  .replace(Regex("""\[GOLD_VALUABLE:[0-9.]+:[0-9]+:[A-Z_]+\]"""), "")
                  .trim()
                if (cleanNotes.isNotBlank()) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = cleanNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f)
                  )
                }
              }

              if (inv.isGoldValuable) {
                val gramPriceUsd = inv.goldPricePerGramUsd(uiState.effectiveGoldPricePerGram24K)
                val mithqalPriceUsd = inv.goldPricePerMithqalUsd(uiState.effectiveGoldPricePerGram24K)
                val gramPriceFormatted = if (uiState.displayCurrency == "IQD") {
                  CurrencyService.format(CurrencyService.convert(gramPriceUsd, "USD", "IQD", uiState.iqdRate), "IQD")
                } else {
                  "$${"%.2f".format(gramPriceUsd)}"
                }
                val mithqalPriceFormatted = if (uiState.displayCurrency == "IQD") {
                  CurrencyService.format(CurrencyService.convert(mithqalPriceUsd, "USD", "IQD", uiState.iqdRate), "IQD")
                } else {
                  "$${"%.1f".format(mithqalPriceUsd)}"
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldAccent.copy(alpha = 0.12f))
                    .border(0.8.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                      text = "⚡",
                      fontSize = 11.sp
                    )
                    Text(
                      text = if (isKurdish) "بهایێ بۆرسا دەولی:" else "Live Spot Rate:",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldAccent
                    )
                  }
                  Text(
                    text = "$gramPriceFormatted/g • $mithqalPriceFormatted/مسقال",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = if (inv.isGoldValuable) {
                      if (isKurdish) "🌍 بهایێ عالمي یێ نوکە" else "🌍 Live World Spot Value"
                    } else {
                      if (isKurdish) "بهایێ نوکە" else "Current Value"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (inv.isGoldValuable) GoldAccent else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (inv.isGoldValuable) FontWeight.Bold else FontWeight.Normal
                  )
                  Text(
                    text = CurrencyService.format(convertedCurrent, uiState.displayCurrency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = if (inv.isGoldValuable) {
                      if (isKurdish) "گوهۆڕینا عالمي / قازانج" else "Spot Trend / ROI"
                    } else {
                      if (isKurdish) "قازانج / زەرەر" else "Profit / Loss"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                  )
                  Text(
                    text = "${if (convertedGain >= 0) "▲ +" else "▼ "}${CurrencyService.format(if (convertedGain < 0) -convertedGain else convertedGain, uiState.displayCurrency)} (${if (roi >= 0) "+" else ""}${"%.1f".format(roi)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (convertedGain >= 0) EmeraldLight else LossRed
                  )
                }
              }

              if (inv.isCoOwned) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "You Paid: ${CurrencyService.formatShort(inv.myEffectiveCapital, inv.currency)}",
                    fontSize = 11.sp,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${inv.partnerName} Paid: ${CurrencyService.formatShort(inv.partnerEffectiveCapital, inv.currency)}",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Action buttons: Update Valuation & Sell Asset (+ Cash Wealth)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = { selectedInvestmentForGainLoss = inv },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                  ),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                  modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (isKurdish) "نویکرنا بهایی" else "Update Value",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                if (!inv.isSold) {
                  Button(
                    onClick = { selectedInvestmentForSale = inv },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = EmeraldLight,
                      contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 42.dp)
                      .testTag("sell_investment_button_${inv.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Sell,
                      contentDescription = null,
                      tint = Color(0xFF0F172A),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (isKurdish) "فرۆتن (+ کاش)" else "Sell (+ Cash)",
                      color = Color(0xFF0F172A),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                } else {
                  OutlinedButton(
                    onClick = { viewModel.reopenInvestmentAsActive(inv.id) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 42.dp)
                  ) {
                    Text(
                      text = if (isKurdish) "زڤڕاندن بۆ چالاک" else "Reopen Active",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Quick Real Estate button
      Button(
        onClick = {
          dialogPresetCategory = "Real Estate"
          dialogPresetName = ""
          dialogPresetNotes = ""
          dialogCustomTitle = if (isKurdish) "زێدەکرنا وەبەرهێنانا ملوک / عەقار" else "Add Real Estate Investment"
          dialogPresetIsCoOwned = false
          dialogPresetInitialCap = ""
          showAddInvestmentDialog = true
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = EmeraldLight,
          contentColor = Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = Modifier
          .heightIn(min = 48.dp)
          .testTag("fab_add_real_estate_button")
      ) {
        Icon(
          imageVector = Icons.Default.Apartment,
          contentDescription = null,
          tint = Color(0xFF0F172A),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isKurdish) "+ ملوک / عەقار" else "+ Real Estate",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }

      // Standard Add button for investments
      FloatingActionButton(
        onClick = {
          dialogPresetCategory = "Real Estate"
          dialogPresetName = ""
          dialogPresetNotes = ""
          dialogCustomTitle = null
          dialogPresetIsCoOwned = false
          dialogPresetInitialCap = ""
          showAddInvestmentDialog = true
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("add_investment_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = if (isKurdish) "زێدەکرنا پرۆژەی" else "Add Project")
      }
    }
  }

  // Add Investment Dialog
  if (showAddInvestmentDialog) {
    AddInvestmentDialog(
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      effectiveGoldPricePerGram24K = uiState.effectiveGoldPricePerGram24K,
      presetCategory = dialogPresetCategory,
      presetName = dialogPresetName,
      presetNotes = dialogPresetNotes,
      presetIsCoOwned = dialogPresetIsCoOwned,
      presetInitialCap = dialogPresetInitialCap,
      customTitle = dialogCustomTitle,
      isKurdish = isKurdish,
      onDismiss = {
        showAddInvestmentDialog = false
        dialogPresetIsCoOwned = false
        dialogPresetInitialCap = ""
      },
      onConfirm = { name, category, initialCap, currentVal, currency, notes, isCoOwned, partnerName, myAmt, partnerAmt, deductFromCash, customRate ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        viewModel.addInvestment(
          name = name,
          category = category,
          initialCapital = initialCap,
          currentValue = currentVal,
          currency = currency,
          notes = notes,
          isCoOwned = isCoOwned,
          partnerName = partnerName,
          myInvestmentAmount = myAmt,
          partnerInvestmentAmount = partnerAmt,
          deductedFromCash = deductFromCash
        )
        showAddInvestmentDialog = false
        dialogPresetIsCoOwned = false
        dialogPresetInitialCap = ""
      }
    )
  }

  // Add Real Estate Debt Dialog (حسابا قەرز و دەینێن عەقاری)
  if (showAddRealEstateDebtDialog) {
    AddRealEstateDebtDialog(
      defaultType = realEstateDebtPresetType,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddRealEstateDebtDialog = false },
      onConfirm = { type, personName, amount, currency, notes ->
        viewModel.addRealEstateDebtCredit(
          type = type,
          personName = personName,
          amount = amount,
          currency = currency,
          notes = notes
        )
        showAddRealEstateDebtDialog = false
      }
    )
  }

  // Record Real Estate Payment / Settle Dialog
  selectedRealEstateLoanForAction?.let { loan ->
    RecordRealEstatePaymentDialog(
      loan = loan,
      isKurdish = isKurdish,
      onDismiss = { selectedRealEstateLoanForAction = null },
      onConfirmPayment = { amount, notes ->
        viewModel.recordLoanPayment(
          debtId = loan.id,
          paymentAmount = amount,
          notes = if (notes.isNotBlank()) notes else "Real estate debt payment",
          affectCashBalance = false
        )
        selectedRealEstateLoanForAction = null
      },
      onSettleInFull = {
        viewModel.settleDebtCreditInFull(
          debtId = loan.id,
          notes = "Real estate full settlement",
          affectCashBalance = false
        )
        selectedRealEstateLoanForAction = null
      },
      onDeleteLoan = {
        viewModel.deleteDebtCredit(loan.id)
        selectedRealEstateLoanForAction = null
      }
    )
  }

  // Sell Real Estate / Investment Dialog (Adds sale proceeds directly to Cash Wealth)
  if (selectedInvestmentForSale != null) {
    val targetInv = selectedInvestmentForSale!!
    SellInvestmentDialog(
      investment = targetInv,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { selectedInvestmentForSale = null },
      onConfirm = { soldPrice, notes, customRate, addToCash ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        viewModel.sellInvestment(
          investmentId = targetInv.id,
          soldPrice = soldPrice,
          notes = notes,
          customIqdRate = customRate,
          addToCashWealth = addToCash
        )
        selectedInvestmentForSale = null
      }
    )
  }

  // Log Monthly Gain / Loss Dialog
  if (selectedInvestmentForGainLoss != null) {
    val targetInv = selectedInvestmentForGainLoss!!
    LogMonthlyGainDialog(
      investment = targetInv,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { selectedInvestmentForGainLoss = null },
      onConfirm = { yearMonth, gainLoss, newValuation, customRate ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        viewModel.recordMonthlyGainLoss(
          investmentId = targetInv.id,
          yearMonth = yearMonth,
          gainLossAmount = gainLoss,
          newTotalValuation = newValuation
        )
        selectedInvestmentForGainLoss = null
      }
    )
  }

  // Detail & Action Dialog on tapping any investment card button
  if (selectedInvestmentForDetail != null) {
    AssetDetailActionDialog(
      investment = selectedInvestmentForDetail!!,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedInvestmentForDetail = null }
    )
  }
}

@Composable
fun AddInvestmentDialog(
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  effectiveGoldPricePerGram24K: Double = 0.0,
  presetCategory: String = "Real Estate",
  presetName: String = "",
  presetNotes: String = "",
  presetIsCoOwned: Boolean = false,
  presetInitialCap: String = "",
  customTitle: String? = null,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (
    name: String,
    category: String,
    initialCap: Double,
    currentVal: Double,
    currency: String,
    notes: String,
    isCoOwned: Boolean,
    partnerName: String,
    myInvestmentAmount: Double,
    partnerInvestmentAmount: Double,
    deductedFromCash: Boolean,
    customIqdRate: Double?
  ) -> Unit
) {
  var nameInput by remember { mutableStateOf(presetName) }
  var category by remember { mutableStateOf(presetCategory) }
  var initialCapInput by remember { mutableStateOf(presetInitialCap) }
  var currentValInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var iqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  var notesInput by remember { mutableStateOf(presetNotes) }
  var deductFromCash by remember {
    mutableStateOf(!presetCategory.startsWith("Real Estate"))
  }

  var isCoOwned by remember {
    mutableStateOf(presetIsCoOwned || (presetCategory == "Cars & Vehicles" && presetName.contains("Partner", ignoreCase = true)))
  }
  var partnerNameInput by remember { mutableStateOf(if (isKurdish) "شەریک" else "Partner") }
  var myInvestmentInput by remember { mutableStateOf("") }
  var partnerInvestmentInput by remember { mutableStateOf("") }
  var showShareValidationError by remember { mutableStateOf(false) }

  val fmtCleanNum: (Double) -> String = { v ->
    val clean = v.coerceAtLeast(0.0)
    if (clean % 1.0 == 0.0) clean.toLong().toString() else "%.2f".format(clean)
  }

  val parsedTotalCap = initialCapInput.toDoubleOrNull() ?: 0.0
  val parsedMyShare = myInvestmentInput.toDoubleOrNull() ?: 0.0
  val parsedPartnerShare = partnerInvestmentInput.toDoubleOrNull() ?: 0.0
  val shareValidation = validatePartnershipShares(parsedTotalCap, parsedMyShare, parsedPartnerShare, currency)
  val hasTypedAnyShare = myInvestmentInput.isNotBlank() || partnerInvestmentInput.isNotBlank()
  val isShareMismatchVisible = isCoOwned && !shareValidation.isValid && ((parsedTotalCap > 0.0 && hasTypedAnyShare) || showShareValidationError)

  val isGoldCategory = category.contains("Gold", ignoreCase = true) || category.contains("زێڕ", ignoreCase = true) || category.contains("زێر", ignoreCase = true)
  var selectedKarat by remember { mutableStateOf(21) }
  var isMithqal by remember { mutableStateOf(true) }
  var goldWeightInput by remember { mutableStateOf("") }

  val availableCategories = listOf(
    "Real Estate - House" to (if (isKurdish) "🏡 خانوو" else "🏡 House"),
    "Real Estate - Land" to (if (isKurdish) "🗺️ ئەرد" else "🗺️ Land"),
    "Real Estate - Commercial" to (if (isKurdish) "🏢 بازرگانی" else "🏢 Commercial"),
    "Real Estate - Gold" to (if (isKurdish) "🪙 زێڕ (عالمي)" else "🪙 Gold (Spot)"),
    "Business Venture" to (if (isKurdish) "💼 کاروبار" else "💼 Venture"),
    "Stocks & ETFs" to (if (isKurdish) "📈 پشک" else "📈 Stocks")
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Text(
        text = customTitle ?: (if (isKurdish) "زێدەکرنا وەبەرهێنانێ یان پڕۆژەی" else "Add Investment / Project Deal"),
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Category selection pills in 2 compact rows
        Text(
          text = if (isKurdish) "جۆرێ وەبەرهێنانێ / سامانێ سەربەخۆ:" else "Asset / Independent Category:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          // Row 1: Real Estate Sub-categories (Houses, Lands, Commercial, Gold)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            availableCategories.take(4).forEach { (catKey, catLabel) ->
              val isSelected = category.contains(catKey, ignoreCase = true) ||
                (catKey.contains("House") && category.contains("House", ignoreCase = true)) ||
                (catKey.contains("Land") && category.contains("Land", ignoreCase = true)) ||
                (catKey.contains("Commercial") && category.contains("Commercial", ignoreCase = true)) ||
                (catKey.contains("Gold") && (category.contains("Gold", ignoreCase = true) || category.contains("زێڕ") || category.contains("زێر")))
              Button(
                onClick = {
                  category = catKey
                  if (catKey.startsWith("Real Estate")) {
                    deductFromCash = false
                  }
                  if (catKey.contains("Gold") && nameInput.isBlank()) {
                    nameInput = if (isKurdish) "زێڕێ حسابا تایبەت" else "Private Account Gold"
                  }
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) (if (catKey.contains("Gold")) GoldAccent else EmeraldLight) else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = catLabel,
                  color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Row 2: Ventures, Stocks
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            availableCategories.drop(4).forEach { (catKey, catLabel) ->
              val isSelected = category.contains(catKey, ignoreCase = true)
              Button(
                onClick = {
                  category = catKey
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = catLabel,
                  color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // Live Spot Gold Calculation Helper Card
        if (isGoldCategory) {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = if (isKurdish) "🪙 تایبەتمەندیێن زێڕی (بهایێ دەولی یێ نوکە)" else "🪙 Gold Specifications (Live World Spot)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )

              // Karat Selector
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf(18, 21, 22, 24).forEach { k ->
                  val isKaratSel = selectedKarat == k
                  Button(
                    onClick = {
                      selectedKarat = k
                      val parsedW = goldWeightInput.toDoubleOrNull() ?: 0.0
                      if (parsedW > 0.0) {
                        val g = if (isMithqal) parsedW * 5.0 else parsedW
                        val purity = when (k) {
                          24 -> 1.0
                          22 -> 22.0 / 24.0
                          21 -> 21.0 / 24.0
                          18 -> 18.0 / 24.0
                          else -> 21.0 / 24.0
                        }
                        val effectivePrice = if (effectiveGoldPricePerGram24K > 0.0) effectiveGoldPricePerGram24K else 85.0
                        val spotValUsd = g * effectivePrice * purity
                        val convertedVal = CurrencyService.convert(spotValUsd, "USD", currency, currentIqdRate)
                        val formatted = "%.0f".format(convertedVal)
                        currentValInput = formatted
                        if (initialCapInput.isBlank()) initialCapInput = formatted
                      }
                    },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isKaratSel) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(
                      text = "${k}K",
                      color = if (isKaratSel) Color(0xFF0F172A) else Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                  }
                }
              }

              // Weight Unit Toggle (Mithqal / Grams)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Button(
                  onClick = {
                    isMithqal = true
                    val parsedW = goldWeightInput.toDoubleOrNull() ?: 0.0
                    if (parsedW > 0.0) {
                      val g = parsedW * 5.0
                      val purity = when (selectedKarat) {
                        24 -> 1.0
                        22 -> 22.0 / 24.0
                        21 -> 21.0 / 24.0
                        18 -> 18.0 / 24.0
                        else -> 21.0 / 24.0
                      }
                      val effectivePrice = if (effectiveGoldPricePerGram24K > 0.0) effectiveGoldPricePerGram24K else 85.0
                      val spotValUsd = g * effectivePrice * purity
                      val convertedVal = CurrencyService.convert(spotValUsd, "USD", currency, currentIqdRate)
                      val formatted = "%.0f".format(convertedVal)
                      currentValInput = formatted
                      if (initialCapInput.isBlank()) initialCapInput = formatted
                    }
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMithqal) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
                  ),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = if (isKurdish) "مسقال (٥ گرام)" else "Mithqal (5g)",
                    color = if (isMithqal) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Button(
                  onClick = {
                    isMithqal = false
                    val parsedW = goldWeightInput.toDoubleOrNull() ?: 0.0
                    if (parsedW > 0.0) {
                      val g = parsedW
                      val purity = when (selectedKarat) {
                        24 -> 1.0
                        22 -> 22.0 / 24.0
                        21 -> 21.0 / 24.0
                        18 -> 18.0 / 24.0
                        else -> 21.0 / 24.0
                      }
                      val effectivePrice = if (effectiveGoldPricePerGram24K > 0.0) effectiveGoldPricePerGram24K else 85.0
                      val spotValUsd = g * effectivePrice * purity
                      val convertedVal = CurrencyService.convert(spotValUsd, "USD", currency, currentIqdRate)
                      val formatted = "%.0f".format(convertedVal)
                      currentValInput = formatted
                      if (initialCapInput.isBlank()) initialCapInput = formatted
                    }
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isMithqal) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
                  ),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = if (isKurdish) "گرام (g)" else "Grams (g)",
                    color = if (!isMithqal) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Weight input
              OutlinedTextField(
                value = goldWeightInput,
                onValueChange = {
                  goldWeightInput = it
                  val parsedW = it.toDoubleOrNull() ?: 0.0
                  if (parsedW > 0.0) {
                    val g = if (isMithqal) parsedW * 5.0 else parsedW
                    val purity = when (selectedKarat) {
                      24 -> 1.0
                      22 -> 22.0 / 24.0
                      21 -> 21.0 / 24.0
                      18 -> 18.0 / 24.0
                      else -> 21.0 / 24.0
                    }
                    val effectivePrice = if (effectiveGoldPricePerGram24K > 0.0) effectiveGoldPricePerGram24K else 85.0
                    val spotValUsd = g * effectivePrice * purity
                    val convertedVal = CurrencyService.convert(spotValUsd, "USD", currency, currentIqdRate)
                    val formatted = "%.0f".format(convertedVal)
                    currentValInput = formatted
                    if (initialCapInput.isBlank()) initialCapInput = formatted
                  }
                },
                label = {
                  Text(if (isMithqal) (if (isKurdish) "کێش ب مسقالان" else "Weight in Mithqals") else (if (isKurdish) "کێش ب گرامان" else "Weight in Grams"))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              // Live spot calculation display
              val parsedWeight = goldWeightInput.toDoubleOrNull() ?: 0.0
              if (parsedWeight > 0.0) {
                val totalGrams = if (isMithqal) parsedWeight * 5.0 else parsedWeight
                val purity = when (selectedKarat) {
                  24 -> 1.0
                  22 -> 22.0 / 24.0
                  21 -> 21.0 / 24.0
                  18 -> 18.0 / 24.0
                  else -> 21.0 / 24.0
                }
                val effectivePrice = if (effectiveGoldPricePerGram24K > 0.0) effectiveGoldPricePerGram24K else 85.0
                val spotValUsd = totalGrams * effectivePrice * purity
                val convertedSpot = CurrencyService.convert(spotValUsd, "USD", currency, currentIqdRate)
                val liveGramRate = effectivePrice * purity
                val liveMithqalRate = liveGramRate * 5.0
                val liveGramFormatted = if (currency == "IQD") {
                  CurrencyService.format(CurrencyService.convert(liveGramRate, "USD", "IQD", currentIqdRate), "IQD")
                } else {
                  "$${"%.2f".format(liveGramRate)}"
                }
                val liveMithqalFormatted = if (currency == "IQD") {
                  CurrencyService.format(CurrencyService.convert(liveMithqalRate, "USD", "IQD", currentIqdRate), "IQD")
                } else {
                  "$${"%.1f".format(liveMithqalRate)}"
                }
                Text(
                  text = "⚡ ${if (isKurdish) "نرخێ عالمي نوکە:" else "Live Spot Rate:"} $liveGramFormatted/g • $liveMithqalFormatted/مسقال",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
                Text(
                  text = "📊 ${if (isKurdish) "بهایێ دەولی یێ کۆم:" else "World Spot Total:"} ${CurrencyService.format(convertedSpot, currency)} (${"%.1f".format(totalGrams)}g • عیار ${selectedKarat}K)",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text(if (isKurdish) "ناڤێ پڕۆژەی / عەقاری" else "Asset / Property Name") },
          placeholder = { Text(if (isKurdish) "بۆ نموونە شوقە، پارچە عەرد، بازاڕ" else "e.g. Apartment, Land Plot, Plaza") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_name_input")
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
                text = cur,
                color = if (currency == cur) Color.Black else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        OutlinedTextField(
          value = initialCapInput,
          onValueChange = {
            initialCapInput = it
            if (currentValInput.isEmpty()) currentValInput = it
            showShareValidationError = false
          },
          label = { Text("${if (isKurdish) "کۆیێ سەرمایێ کڕینێ" else "Total Purchase Price / Capital"} ($currency)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_capital_input")
        )

        OutlinedTextField(
          value = currentValInput,
          onValueChange = { currentValInput = it },
          label = { Text("${if (isKurdish) "بهایێ نوکە یێ گشتی" else "Current Total Valuation"} ($currency)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = parsedTotalCap,
          selectedCurrency = if (currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )

        // Co-Ownership Section (Partner Purchase)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCoOwned) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Handshake,
                  contentDescription = null,
                  tint = if (isCoOwned) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = if (isKurdish) "کڕین ب هەڤپشکی (شەریکاتی)" else "Bought with Partner (Co-Owned)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isCoOwned) GoldAccent else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (isKurdish) "بۆ نموونە: ٢٠,٠٠٠$ ⟵ ١٠,٠٠٠$ تۆ / ١٠,٠٠٠$ شەریک" else "e.g. Total $20,000 → You $10,000 / Partner $10,000",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = isCoOwned,
                onCheckedChange = { checked ->
                  isCoOwned = checked
                  showShareValidationError = false
                  val tot = initialCapInput.toDoubleOrNull() ?: 0.0
                  if (checked && tot > 0 && myInvestmentInput.isBlank() && partnerInvestmentInput.isBlank()) {
                    val half = fmtCleanNum(tot / 2.0)
                    myInvestmentInput = half
                    partnerInvestmentInput = half
                  }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent)
              )
            }

            if (isCoOwned) {
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = partnerNameInput,
                onValueChange = { partnerNameInput = it },
                label = { Text(if (isKurdish) "ناڤێ شەریکی" else "Partner Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = myInvestmentInput,
                  onValueChange = {
                    myInvestmentInput = it
                    showShareValidationError = false
                  },
                  isError = isShareMismatchVisible,
                  label = { Text("${if (isKurdish) "پشکا من" else "My Share"} ($currency)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                  value = partnerInvestmentInput,
                  onValueChange = {
                    partnerInvestmentInput = it
                    showShareValidationError = false
                  },
                  isError = isShareMismatchVisible,
                  label = { Text("${if (isKurdish) "شەریک" else "Partner"} ($currency)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
              }

              // Quick 50/50 Split & Complete Remaining Buttons
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Button(
                  onClick = {
                    val tot = initialCapInput.toDoubleOrNull() ?: 0.0
                    val half = fmtCleanNum(tot / 2.0)
                    myInvestmentInput = half
                    partnerInvestmentInput = half
                    showShareValidationError = false
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GoldAccent.copy(alpha = 0.25f)),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(if (isKurdish) "⚖️ بەشکرنا ٥٠/٥٠" else "⚖️ 50/50 Split", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                }

                if (!shareValidation.isValid && parsedTotalCap > 0.0) {
                  Button(
                    onClick = {
                      if (myInvestmentInput.isNotBlank() && parsedMyShare <= parsedTotalCap) {
                        partnerInvestmentInput = fmtCleanNum(parsedTotalCap - parsedMyShare)
                      } else if (partnerInvestmentInput.isNotBlank() && parsedPartnerShare <= parsedTotalCap) {
                        myInvestmentInput = fmtCleanNum(parsedTotalCap - parsedPartnerShare)
                      } else {
                        val half = fmtCleanNum(parsedTotalCap / 2.0)
                        myInvestmentInput = half
                        partnerInvestmentInput = half
                      }
                      showShareValidationError = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(if (isKurdish) "⚡ کاملکرنا مەبلەغی" else "⚡ Complete Amount", fontSize = 10.sp, color = EmeraldLight, fontWeight = FontWeight.ExtraBold)
                  }
                }
              }

              if (isShareMismatchVisible) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.16f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LossRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                ) {
                  Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Text(
                      text = if (isKurdish) "⚠️ خەلەتە، مەبلەغی کامل بکە!" else "⚠️ Wrong Amount! Complete the Full Price",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = LossRed
                    )
                    Text(
                      text = if (isKurdish) shareValidation.errorMessageKu else shareValidation.errorMessageEn,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              } else if (shareValidation.isValid) {
                val myPct = if (parsedTotalCap > 0) (parsedMyShare / parsedTotalCap) * 100.0 else 50.0
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = if (isKurdish) "✅ هەردوو پشک تەمامن (${"%.0f".format(myPct)}% تۆ / ${"%.0f".format(100.0 - myPct)}% شەریک)" else "✅ Shares Complete (${"%.0f".format(myPct)}% You / ${"%.0f".format(100.0 - myPct)}% Partner)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldLight
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی (جهـ، بارودۆخ، سال، و هتد)" else "Notes (Model, Year, Condition, Location, etc.)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Deduct from Cash Wealth Toggle (بشتی ئەڤرۆ کێمکرن ژ سەروەتێ کاشی)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (deductFromCash) EmeraldLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isKurdish) "💵 کێمکرن ژ سەروەتێ کاشی" else "💵 Deduct from Cash Wealth",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (deductFromCash) EmeraldLight else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isKurdish) "سەرمایێ کڕینێ ڕاستەوخۆ ژ کاشی دهێتە کێمکرن" else "Automatically deduct purchase cost from Cash Wealth",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = deductFromCash,
              onCheckedChange = { deductFromCash = it },
              colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val initAmt = initialCapInput.toDoubleOrNull() ?: 0.0
          val curAmt = currentValInput.toDoubleOrNull() ?: initAmt
          if (nameInput.isNotBlank() && initAmt > 0) {
            val myAmt = if (isCoOwned) (myInvestmentInput.toDoubleOrNull() ?: 0.0) else initAmt
            val partnerAmt = if (isCoOwned) (partnerInvestmentInput.toDoubleOrNull() ?: 0.0) else 0.0
            if (isCoOwned) {
              val validation = validatePartnershipShares(initAmt, myAmt, partnerAmt, currency)
              if (!validation.isValid) {
                showShareValidationError = true
                return@Button
              }
            }
            val customRate = iqdRateInput.toDoubleOrNull() ?: currentIqdRate
            val cleanNotes = notesInput.replace(Regex("""\[(?:DEBT|INDEPENDENT_DEBT|قەرز):[0-9.]*\]"""), "").trim()
            val finalNotes = if (isGoldCategory && goldWeightInput.isNotBlank()) {
              val parsedW = goldWeightInput.toDoubleOrNull() ?: 0.0
              if (cleanNotes.isNotBlank()) "$cleanNotes [GOLD_VALUABLE:$parsedW:$selectedKarat:${if (isMithqal) "MITHQAL" else "GRAM"}]"
              else "[GOLD_VALUABLE:$parsedW:$selectedKarat:${if (isMithqal) "MITHQAL" else "GRAM"}]"
            } else {
              cleanNotes
            }

            onConfirm(
              nameInput,
              category,
              initAmt,
              curAmt,
              currency,
              finalNotes,
              isCoOwned,
              partnerNameInput.ifBlank { "Partner" },
              myAmt,
              partnerAmt,
              deductFromCash,
              customRate
            )
          } else if (isCoOwned) {
            showShareValidationError = true
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isCoOwned) GoldAccent else EmeraldLight
        ),
        modifier = Modifier.testTag("confirm_add_investment_button")
      ) {
        Text(
          text = if (isKurdish) {
            if (isCoOwned) "تۆمارکرنا پڕۆژەیێ شەریکی" else "تۆمارکرنا وەبەرهێنانێ"
          } else {
            if (isCoOwned) "Save Partner Deal" else "Add Project Deal"
          },
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

@Composable
fun LogMonthlyGainDialog(
  investment: InvestmentEntity,
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (yearMonth: String, gainLoss: Double, newValuation: Double, customIqdRate: Double?) -> Unit
) {
  val currentYearMonth = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }
  var monthInput by remember { mutableStateOf(currentYearMonth) }
  var gainLossInput by remember { mutableStateOf("") }
  var newValuationInput by remember { mutableStateOf(investment.currentValue.toString()) }
  var iqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.imePadding(),
    title = { Text(if (isKurdish) "تۆمارکرنا قازانج/زەرەرا هەیڤانە" else "Log Monthly Performance", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "${if (isKurdish) "سەرمایە: " else "Asset: "}${investment.name}",
          fontWeight = FontWeight.Bold,
          color = EmeraldLight
        )
        Text(
          text = "${if (isKurdish) "بهایێ نوکە: " else "Current Value: "}${CurrencyService.format(investment.currentValue, investment.currency)}",
          style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
          value = monthInput,
          onValueChange = { monthInput = it },
          label = { Text(if (isKurdish) "هەیڤ (بۆ نموونە 2026-09)" else "Month (e.g. 2026-09)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = gainLossInput,
          onValueChange = {
            gainLossInput = it
            val gain = it.toDoubleOrNull()
            if (gain != null) {
              newValuationInput = (investment.currentValue + gain).toString()
            }
          },
          label = { Text("${if (isKurdish) "قازانج یان زەرەرا هەیڤانە" else "Monthly Gain or Loss (+/-)"} (${investment.currency})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gain_loss_amount_input")
        )

        OutlinedTextField(
          value = newValuationInput,
          onValueChange = { newValuationInput = it },
          label = { Text("${if (isKurdish) "بهایێ نوی یێ پڕۆژەی" else "New Updated Valuation"} (${investment.currency})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = newValuationInput.toDoubleOrNull() ?: investment.currentValue,
          selectedCurrency = if (investment.currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val gain = gainLossInput.toDoubleOrNull() ?: 0.0
          val newVal = newValuationInput.toDoubleOrNull() ?: (investment.currentValue + gain)
          val customRate = iqdRateInput.toDoubleOrNull() ?: currentIqdRate
          onConfirm(monthInput, gain, newVal, customRate)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
        modifier = Modifier.testTag("confirm_monthly_record_button")
      ) {
        Text(if (isKurdish) "تۆمارکرن" else "Save Record")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

@Composable
fun SellInvestmentDialog(
  investment: InvestmentEntity,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (soldPrice: Double, notes: String, customIqdRate: Double?, addToCash: Boolean) -> Unit
) {
  var soldPriceInput by remember { mutableStateOf(investment.currentValue.toString()) }
  var notesInput by remember { mutableStateOf("") }
  var iqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  var linkToCashWealth by remember(investment.id) { mutableStateOf(investment.deductedFromCash) }
  val parsedSale = soldPriceInput.toDoubleOrNull() ?: 0.0
  val mySaleShare = if (investment.isCoOwned) parsedSale * (investment.mySharePercent / 100.0) else parsedSale
  val profitLoss = parsedSale - investment.initialCapital
  val isGain = profitLoss >= 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.imePadding(),
    title = {
      Text(
        text = if (isKurdish) "💵 فرۆتنا عەقار / پرۆژەی" else "💵 Sell Property / Asset",
        fontWeight = FontWeight.ExtraBold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "${investment.name} (${investment.category})",
          fontWeight = FontWeight.Bold,
          color = EmeraldLight
        )
        Text(
          text = if (isKurdish) {
            "دیار بکە کا دێ پارێ فرۆتنێ چیتە سەر سەروەتێ کاشی یان نە."
          } else {
            "Specify whether the sale proceeds should link to Cash Wealth or stay standalone."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = soldPriceInput,
          onValueChange = { soldPriceInput = it },
          label = { Text("${if (isKurdish) "بهایێ فرۆتنێ" else "Total Sale Price"} (${investment.currency})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_sold_price_input")
        )
        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = parsedSale,
          selectedCurrency = if (investment.currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )

        // Standalone or Add to Cash Wealth Option
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (linkToCashWealth) EmeraldLight.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
              Text(
                text = if (isKurdish) "💵 ل سەروەتێ کاشی زێدە ببیت؟" else "💵 Add proceeds to Cash Wealth?",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (linkToCashWealth) EmeraldLight else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (linkToCashWealth) {
                  if (isKurdish) "بهایێ فرۆتنێ دچیتە سەر سەروەتێ کاشی" else "Sale proceeds will be added to Cash Wealth"
                } else {
                  if (isKurdish) "عەقار ب سەربەخۆیی تنێ دمینیت (ناچیتە سەروەتێ کاش)" else "Proceeds stay standalone (Not added to Cash Wealth)"
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = linkToCashWealth,
              onCheckedChange = { linkToCashWealth = it },
              modifier = Modifier.testTag("toggle_sell_add_to_cash")
            )
          }
        }

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = (if (isGain) EmeraldLight else LossRed).copy(alpha = 0.14f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "زێدەبوون بۆ سەروەتێ کاش:" else "Added to Cash Wealth:", fontSize = 11.sp)
              Text(
                text = if (linkToCashWealth) "+${CurrencyService.format(mySaleShare, investment.currency)}" else if (isKurdish) "سەربەخۆ (تنێ)" else "Standalone",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (linkToCashWealth) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "قازانج / زەرەر:" else "Net Profit / Loss:", fontSize = 11.sp)
              Text(
                text = "${if (isGain) "+" else ""}${CurrencyService.format(profitLoss, investment.currency)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGain) EmeraldLight else LossRed
              )
            }
          }
        }
        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینیێن فرۆتنێ" else "Sale Notes") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (parsedSale > 0.0) {
            val customRate = iqdRateInput.toDoubleOrNull() ?: currentIqdRate
            onConfirm(parsedSale, notesInput, customRate, linkToCashWealth)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = EmeraldLight,
          contentColor = Color(0xFF0F172A)
        ),
        modifier = Modifier.testTag("confirm_sell_investment_button")
      ) {
        Text(
          text = if (isKurdish) "پشتڕاستکرنا فرۆتنێ" else "Confirm Sale",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

