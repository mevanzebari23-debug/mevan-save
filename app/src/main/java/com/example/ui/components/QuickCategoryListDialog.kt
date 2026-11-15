package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.ActiveDebtCreditLoan
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.theme.CyberObsidianCard
import com.example.ui.theme.CyberObsidianSurface
import com.example.ui.theme.CyberObsidianVoid
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HoloNeonBlue
import com.example.ui.theme.HoloNeonCyan
import com.example.ui.theme.HoloNeonEmerald
import com.example.ui.theme.HoloNeonGold
import com.example.ui.theme.HoloNeonViolet
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Universal Quick Category Explorer Dialog.
 * When the user taps a category button (e.g. Toyota), this dialog instantly lists
 * all matching items across cars, properties, debts, or school fees with full details.
 */
@Composable
fun QuickCategoryListDialog(
  initialCategoryKey: String,
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onNavigateToTab: (Int) -> Unit,
  onOpenCarDetails: (InvestmentEntity) -> Unit = {},
  onSelectCar: ((InvestmentEntity) -> Unit)? = null,
  onSelectProperty: ((InvestmentEntity) -> Unit)? = null,
  onSelectDebt: ((ActiveDebtCreditLoan) -> Unit)? = null,
  onSelectGold: ((GoldHoldingEntity) -> Unit)? = null,
  onSelectSchoolTx: ((TransactionEntity) -> Unit)? = null
) {
  var activeCategory by remember { mutableStateOf(initialCategoryKey) }
  val displayCurrency = uiState.displayCurrency
  val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

  // Quick Preset Categories for fast switching inside the dialog
  val categoryPresets = listOf(
    "Toyota" to ("تۆیۆتا • Toyota"),
    "BMW" to ("بی ئێم • BMW"),
    "Mercedes" to ("مێرسیدس • Mercedes"),
    "Nissan" to ("نیسان • Nissan"),
    "Hyundai" to ("هیۆندای • Hyundai"),
    "Land Cruiser" to ("لاندکرۆزەر • Land Cruiser"),
    "خانوو" to ("خانوو و ڤێلا • Houses"),
    "شوقە" to ("شوقە • Apartments"),
    "زەوی" to ("زەوی • Lands"),
    "زێڕ" to ("زێڕ و زیڤ • Gold"),
    "قەرزێن لسەر من" to ("قەرزێن لسەر من • Payables"),
    "دەف خەلکی" to ("قەرزێن دەف خەلکی • Receivables"),
    "قوتابخانە" to ("قوتابخانە • School")
  )

  // Filter Matching Items based on activeCategory
  val isExplicitPersonQuery = activeCategory.startsWith("PERSON:")
  val cleanCategoryTitle = when {
    isExplicitPersonQuery -> activeCategory.removePrefix("PERSON:").trim()
    activeCategory == "ALL_CARS" -> if (isKurdish) "هەمی سەیارە" else "All Vehicles"
    activeCategory == "ALL_ASSETS" -> if (isKurdish) "هەمی عەقارات و ملوک" else "All Properties"
    activeCategory == "ALL_DEBTS" -> if (isKurdish) "هەمی قەرز" else "All Debts & Credits"
    else -> activeCategory
  }

  val isAllCars = activeCategory == "ALL_CARS"
  val isAllAssets = activeCategory == "ALL_ASSETS"
  val isAllDebts = activeCategory == "ALL_DEBTS"

  val isCarQuery = !isExplicitPersonQuery && (isAllCars || listOf("toyota", "bmw", "mercedes", "nissan", "hyundai", "land cruiser", "kia", "ford", "lexus", "chevrolet", "gmc", "dodge", "jeep", "cadillac", "porsche", "audi", "range rover", "سەیارە", "car").any {
    activeCategory.contains(it, ignoreCase = true)
  })
  val isRealEstateQuery = !isExplicitPersonQuery && (isAllAssets || listOf("خانوو", "شوقە", "زەوی", "عەقار", "بازرگانی", "ڤێلا", "مەزرەعە", "house", "apartment", "land", "property", "villa").any {
    activeCategory.contains(it, ignoreCase = true)
  })
  val isGoldQuery = !isExplicitPersonQuery && (activeCategory.contains("زێڕ", ignoreCase = true) || activeCategory.contains("gold", ignoreCase = true) || activeCategory.contains("زیڤ", ignoreCase = true))
  val isDebtQuery = isExplicitPersonQuery || isAllDebts || activeCategory.contains("قەرز", ignoreCase = true) || activeCategory.contains("debt", ignoreCase = true) || activeCategory.contains("خەلکی", ignoreCase = true) || activeCategory.contains("لسەر من", ignoreCase = true) || activeCategory.contains("درەنگکەفتی", ignoreCase = true) || activeCategory.contains("تەواوکری", ignoreCase = true) || activeCategory.contains("settled", ignoreCase = true)
  val isSchoolQuery = !isExplicitPersonQuery && (activeCategory.contains("قوتابخانە", ignoreCase = true) || activeCategory.contains("school", ignoreCase = true))

  val matchedCars: List<InvestmentEntity> = remember(activeCategory, uiState.carInvestments) {
    when {
      isExplicitPersonQuery -> emptyList()
      isAllCars -> uiState.carInvestments
      isCarQuery -> uiState.carInvestments.filter { car ->
        car.name.contains(activeCategory, ignoreCase = true) ||
          car.category.contains(activeCategory, ignoreCase = true) ||
          car.notes.contains(activeCategory, ignoreCase = true)
      }
      !isDebtQuery && !isGoldQuery && !isSchoolQuery && !isRealEstateQuery -> {
        uiState.carInvestments.filter { it.name.contains(activeCategory, ignoreCase = true) }
      }
      else -> emptyList()
    }
  }

  val matchedRealEstate: List<InvestmentEntity> = remember(activeCategory, uiState.nonCarInvestments) {
    when {
      isExplicitPersonQuery -> emptyList()
      isAllAssets -> uiState.nonCarInvestments
      isRealEstateQuery -> uiState.nonCarInvestments.filter { inv ->
        inv.name.contains(activeCategory, ignoreCase = true) ||
          inv.category.contains(activeCategory, ignoreCase = true) ||
          inv.notes.contains(activeCategory, ignoreCase = true) ||
          (activeCategory.contains("خانوو", ignoreCase = true) && inv.isHouse) ||
          (activeCategory.contains("زەوی", ignoreCase = true) && inv.isLand) ||
          (activeCategory.contains("شوقە", ignoreCase = true) && (inv.name.contains("شوقە", ignoreCase = true) || inv.category.contains("Apartment", ignoreCase = true))) ||
          (activeCategory.contains("بازرگانی", ignoreCase = true) && inv.isCommercial)
      }
      !isDebtQuery && !isGoldQuery && !isSchoolQuery && !isCarQuery -> {
        uiState.nonCarInvestments.filter { it.name.contains(activeCategory, ignoreCase = true) || it.category.contains(activeCategory, ignoreCase = true) }
      }
      else -> emptyList()
    }
  }

  val matchedGold: List<GoldHoldingEntity> = remember(activeCategory, uiState.activeGoldHoldings) {
    if (isGoldQuery) uiState.activeGoldHoldings else emptyList()
  }

  val matchedDebts: List<ActiveDebtCreditLoan> = remember(activeCategory, uiState.activeDebtCreditLoans, uiState.allDebtCreditLoanModels) {
    when {
      isExplicitPersonQuery -> {
        val exactPersonName = cleanCategoryTitle
        val exactMatches = uiState.activeDebtCreditLoans.filter {
          it.personName.trim().equals(exactPersonName, ignoreCase = true)
        }
        if (exactMatches.isNotEmpty()) exactMatches
        else uiState.activeDebtCreditLoans.filter {
          it.personName.contains(exactPersonName, ignoreCase = true)
        }
      }
      isAllDebts -> uiState.activeDebtCreditLoans
      isDebtQuery -> {
        if (activeCategory.contains("تەواوکری", ignoreCase = true) || activeCategory.contains("settled", ignoreCase = true)) {
          uiState.allDebtCreditLoanModels.filter { it.isCompleted }
        } else {
          uiState.activeDebtCreditLoans.filter { debt ->
            if (activeCategory.contains("لسەر من", ignoreCase = true) || activeCategory.contains("payables", ignoreCase = true) || activeCategory.contains("debt_on_me", ignoreCase = true)) {
              debt.type == "DEBT" || debt.type == "PAYABLE" || debt.type == "DEBT_ON_ME"
            } else if (activeCategory.contains("دەف خەلکی", ignoreCase = true) || activeCategory.contains("receivables", ignoreCase = true) || activeCategory.contains("credit", ignoreCase = true)) {
              debt.type == "CREDIT" || debt.type == "RECEIVABLE" || debt.type == "LOAN_TO_OTHER"
            } else if (activeCategory.contains("درەنگکەفتی", ignoreCase = true) || activeCategory.contains("overdue", ignoreCase = true)) {
              debt.dueDateMillis > 0 && debt.dueDateMillis < System.currentTimeMillis()
            } else {
              debt.personName.contains(activeCategory, ignoreCase = true) || debt.notes.contains(activeCategory, ignoreCase = true)
            }
          }
        }
      }
      else -> {
        // Exact person match first so a person's name stays strictly with that person
        val trimmed = activeCategory.trim()
        val exactByPerson = uiState.activeDebtCreditLoans.filter { it.personName.trim().equals(trimmed, ignoreCase = true) }
        if (exactByPerson.isNotEmpty()) exactByPerson
        else uiState.activeDebtCreditLoans.filter { it.personName.contains(trimmed, ignoreCase = true) }
      }
    }
  }

  val matchedSchool: List<TransactionEntity> = remember(activeCategory, uiState.schoolTransactions) {
    if (isSchoolQuery) {
      uiState.schoolTransactions
    } else emptyList()
  }

  val hasCars = matchedCars.isNotEmpty()
  val hasRealEstate = matchedRealEstate.isNotEmpty()
  val hasGold = matchedGold.isNotEmpty()
  val hasDebts = matchedDebts.isNotEmpty()
  val hasSchool = matchedSchool.isNotEmpty()

  val targetTab = when {
    hasCars -> 2 // Cars
    hasRealEstate || hasGold || isGoldQuery -> 3 // Assets & Gold
    hasSchool -> 4 // School
    hasDebts -> 1 // Debts
    else -> 0
  }

  // Convert EVERY item properly to displayCurrency (USD/IQD) via CurrencyService so IQD is NEVER counted 1:1 as USD!
  val debtTotalsBreakdown = remember(matchedDebts, displayCurrency, uiState.iqdRate, uiState.effectiveGoldPricePerGram24K) {
    val usdRaw = matchedDebts.filter { !it.isGoldDebt && it.currency.equals("USD", ignoreCase = true) }.sumOf { it.remainingBalance }
    val iqdRaw = matchedDebts.filter { !it.isGoldDebt && it.currency.equals("IQD", ignoreCase = true) }.sumOf { it.remainingBalance }
    val convertedTotal = matchedDebts.sumOf { debt ->
      if (debt.isGoldDebt) {
        val usdVal = debt.goldLiveValueUsd(uiState.effectiveGoldPricePerGram24K)
        CurrencyService.convert(usdVal, "USD", displayCurrency, uiState.iqdRate)
      } else {
        CurrencyService.convert(debt.remainingBalance, debt.currency, displayCurrency, uiState.iqdRate)
      }
    }
    Triple(usdRaw, iqdRaw, convertedTotal)
  }

  val totalValuation: Double = when {
    hasCars -> matchedCars.sumOf { car ->
      val raw = if (car.isSold && car.soldPrice > 0.0) car.soldPrice else car.currentValue
      CurrencyService.convert(raw, car.currency, displayCurrency, uiState.iqdRate, entryIqdRate = car.exchangeRateAtEntry)
    }
    hasRealEstate -> matchedRealEstate.sumOf { prop ->
      CurrencyService.convert(prop.currentValue, prop.currency, displayCurrency, uiState.iqdRate, entryIqdRate = prop.exchangeRateAtEntry)
    }
    hasGold -> matchedGold.sumOf { gold ->
      val usd = gold.currentValue(uiState.effectiveGoldPricePerGram24K, uiState.effectiveSilverPricePerGram9999)
      CurrencyService.convert(usd, "USD", displayCurrency, uiState.iqdRate)
    }
    hasDebts -> debtTotalsBreakdown.third
    hasSchool -> matchedSchool.sumOf { tx ->
      CurrencyService.convert(tx.amount, tx.currency, displayCurrency, uiState.iqdRate, entryIqdRate = tx.exchangeRateAtEntry)
    }
    else -> 0.0
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.88f)
        .clip(RoundedCornerShape(26.dp))
        .border(
          1.2.dp,
          Brush.horizontalGradient(listOf(HoloNeonCyan, HoloNeonEmerald, HoloNeonGold)),
          RoundedCornerShape(26.dp)
        )
        .testTag("quick_category_list_dialog"),
      color = CyberObsidianVoid
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(CyberObsidianSurface)
      ) {
        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(CyberObsidianCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(HoloNeonCyan.copy(alpha = 0.20f))
                .border(1.dp, HoloNeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when {
                  isCarQuery -> Icons.Default.DirectionsCar
                  isRealEstateQuery -> Icons.Default.Apartment
                  isDebtQuery -> Icons.Default.Handshake
                  isSchoolQuery -> Icons.Default.School
                  else -> Icons.Default.ReceiptLong
                },
                contentDescription = null,
                tint = HoloNeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = cleanCategoryTitle,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloNeonEmerald.copy(alpha = 0.20f))
                    .border(0.6.dp, HoloNeonEmerald, RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${matchedCars.size + matchedRealEstate.size + matchedGold.size + matchedDebts.size + matchedSchool.size} بڕگە",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = HoloNeonEmerald
                  )
                }
              }
              Text(
                text = "${if (isKurdish) "سەرجەمێ بهایێ گشتی:" else "Total Valuation:"} ${CurrencyService.format(totalValuation, displayCurrency)}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = HoloNeonGold
              )
              if (hasDebts && debtTotalsBreakdown.second > 0.0) {
                val iqdAsUsd = CurrencyService.convert(debtTotalsBreakdown.second, "IQD", "USD", uiState.iqdRate)
                Text(
                  text = if (isKurdish) {
                    "دینار: ${CurrencyService.format(debtTotalsBreakdown.second, "IQD")} = ${CurrencyService.format(iqdAsUsd, "USD")}"
                  } else {
                    "IQD: ${CurrencyService.format(debtTotalsBreakdown.second, "IQD")} = ${CurrencyService.format(iqdAsUsd, "USD")}"
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = HoloNeonCyan
                )
              }
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(34.dp)
              .background(Color.White.copy(alpha = 0.08f), CircleShape)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }

        // Quick Category Switcher Strip (Horizontal Scroll)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(CyberObsidianCard.copy(alpha = 0.70f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          categoryPresets.forEach { (catKey, catLabel) ->
            val isCurrent = activeCategory.equals(catKey, ignoreCase = true)
            CyberNeonCategoryButton(
              label = catLabel,
              accentColor = when {
                catKey.contains("Toyota", ignoreCase = true) || catKey.contains("BMW", ignoreCase = true) || catKey.contains("Mercedes", ignoreCase = true) -> HoloNeonBlue
                catKey.contains("خانوو", ignoreCase = true) || catKey.contains("شوقە", ignoreCase = true) -> HoloNeonEmerald
                catKey.contains("زێڕ", ignoreCase = true) -> HoloNeonGold
                catKey.contains("قەرز", ignoreCase = true) -> Color(0xFFF97316)
                else -> HoloNeonViolet
              },
              isSelected = isCurrent,
              onClick = { activeCategory = catKey }
            )
          }
        }

        // Content Area: List of Matched Items
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Cars Section
          if (hasCars || (isCarQuery && !hasRealEstate && !hasDebts && !hasSchool)) {
            if (matchedCars.isEmpty()) {
              item {
                EmptyCategoryNotice(
                  title = if (isKurdish) "چ سەیارەیێن سەر ب پۆلا '$activeCategory' نەهاتنە دیتن" else "No vehicles found for '$activeCategory'",
                  tip = if (isKurdish) "دشێی سەیارەکا نوی ب ناڤێ $activeCategory زێدە بکەی ل بەشێ پێشانگەها سەیاران." else "You can add a vehicle named $activeCategory in the Vehicles Fleet."
                )
              }
            } else {
              items(matchedCars, key = { "car_${it.id}" }) { car ->
                CarListItemCard(
                  car = car,
                  displayCurrency = displayCurrency,
                  isKurdish = isKurdish,
                  onClick = {
                    if (onSelectCar != null) {
                      onDismiss()
                      onSelectCar(car)
                    } else {
                      onOpenCarDetails(car)
                      onDismiss()
                      onNavigateToTab(2)
                    }
                  }
                )
              }
            }
          }

          // Real Estate Section
          if (hasRealEstate || (isRealEstateQuery && !hasCars && !hasDebts && !hasSchool)) {
            if (matchedRealEstate.isEmpty()) {
              item {
                EmptyCategoryNotice(
                  title = if (isKurdish) "چ ملوکێن سەر ب پۆلا '$activeCategory' نەهاتنە دیتن" else "No assets found for '$activeCategory'",
                  tip = if (isKurdish) "دشێی ملوکەکێ نوی ل بەشێ عەقارات و زێڕی تۆمار بکەی." else "You can log properties in Assets & Gold."
                )
              }
            } else {
              items(matchedRealEstate, key = { "prop_${it.id}" }) { prop ->
                PropertyListItemCard(
                  property = prop,
                  displayCurrency = displayCurrency,
                  isKurdish = isKurdish,
                  onClick = {
                    onDismiss()
                    if (onSelectProperty != null) onSelectProperty(prop) else onNavigateToTab(3)
                  }
                )
              }
            }
          }

          // Debts Section
          if (hasDebts || (isDebtQuery && !hasCars && !hasRealEstate && !hasGold && !hasSchool)) {
            if (matchedDebts.isEmpty()) {
              item {
                EmptyCategoryNotice(
                  title = if (isKurdish) "چ قەرزێن سەر ب ڤێ پۆلێ نەهاتنە دیتن" else "No debts found for '$cleanCategoryTitle'",
                  tip = if (isKurdish) "دشێی قەرزەکێ نوی ل سجلا قەرزان تۆمار بکەی." else "You can record loans in Debts & Credits."
                )
              }
            } else {
              items(matchedDebts, key = { "debt_${it.id}" }) { debt ->
                DebtListItemCard(
                  debt = debt,
                  displayCurrency = displayCurrency,
                  iqdRate = uiState.iqdRate,
                  effectiveGoldPricePerGram24K = uiState.effectiveGoldPricePerGram24K,
                  isKurdish = isKurdish,
                  onClick = {
                    onDismiss()
                    if (onSelectDebt != null) onSelectDebt(debt) else onNavigateToTab(1)
                  }
                )
              }
            }
          }

          // Gold & Silver Section
          if (hasGold || (isGoldQuery && !hasCars && !hasRealEstate && !hasDebts && !hasSchool)) {
            if (matchedGold.isEmpty()) {
              item {
                EmptyCategoryNotice(
                  title = if (isKurdish) "چ زێڕ یان زیڤ نەهاتنە دیتن" else "No gold or silver holdings found",
                  tip = if (isKurdish) "دشێی زێڕ یان زیڤەکێ نوی ل بەشێ عەقارات و زێڕی تۆمار بکەی." else "You can log gold & silver in Assets & Gold."
                )
              }
            } else {
              items(matchedGold, key = { "gold_${it.id}" }) { gold ->
                val valUsd = gold.currentValue(uiState.effectiveGoldPricePerGram24K, uiState.effectiveSilverPricePerGram9999)
                val valDisplay = CurrencyService.convert(valUsd, "USD", displayCurrency, uiState.iqdRate)
                val accent = if (gold.isSilver) Color(0xFF94A3B8) else HoloNeonGold
                CyberRecordButton(
                  title = gold.title,
                  subtitle = "${gold.getDisplayPurityBadge(isKurdish)} • ${String.format(Locale.US, "%.2f", gold.weightGrams)}g",
                  primaryValue = CurrencyService.format(valDisplay, displayCurrency),
                  accentColor = accent,
                  icon = Icons.Default.MonetizationOn,
                  statusBadge = if (gold.isSilver) (if (isKurdish) "زیڤ" else "Silver") else (if (isKurdish) "زێڕ" else "Gold"),
                  statusBadgeColor = accent,
                  actionHintLabel = if (isKurdish) "هوردەکاری" else "Open",
                  onClick = {
                    onDismiss()
                    if (onSelectGold != null) onSelectGold(gold) else onNavigateToTab(3)
                  },
                  testTagKey = "quick_cat_gold_${gold.id}"
                )
              }
            }
          }

          // School Section
          if (hasSchool || (isSchoolQuery && !hasCars && !hasRealEstate && !hasGold && !hasDebts)) {
            if (matchedSchool.isEmpty()) {
              item {
                EmptyCategoryNotice(
                  title = if (isKurdish) "چ مەسرۆفێن قوتابخانێ نەهاتنە دیتن" else "No school expenses found",
                  tip = if (isKurdish) "دشێی مەسرۆفەکێ نوی ل بەشێ قوتابخانێ تۆمار بکەی." else "Record fees in School Vault."
                )
              }
            } else {
              items(matchedSchool, key = { "school_${it.id}" }) { tx ->
                SchoolListItemCard(
                  tx = tx,
                  displayCurrency = displayCurrency,
                  isKurdish = isKurdish,
                  onClick = {
                    onDismiss()
                    if (onSelectSchoolTx != null) onSelectSchoolTx(tx) else onNavigateToTab(4)
                  }
                )
              }
            }
          }

          if (!hasCars && !hasRealEstate && !hasGold && !hasDebts && !hasSchool && !isCarQuery && !isRealEstateQuery && !isGoldQuery && !isDebtQuery && !isSchoolQuery) {
            item {
              EmptyCategoryNotice(
                title = if (isKurdish) "چ ئەنجام نەهاتنە دیتن بۆ '$cleanCategoryTitle'" else "No items found for '$cleanCategoryTitle'",
                tip = if (isKurdish) "دشێی بڕگەکێ نوی ب ڤی ناڤی زێدە بکەی یان ناڤەکێ دی بکاربینی." else "No records currently match this category."
              )
            }
          }
        }

        // Bottom Action: Open Full Vault
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .background(CyberObsidianCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
          color = Color.Transparent
        ) {
          Button(
            onClick = {
              onDismiss()
              onNavigateToTab(targetTab)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("open_full_vault_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = HoloNeonCyan,
              contentColor = CyberObsidianVoid
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = if (isKurdish) "گشتی ل بەشێ سەرەکی پیشان بدە" else "Open in Full Dedicated Vault",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
              )
              Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CarListItemCard(
  car: InvestmentEntity,
  displayCurrency: String,
  isKurdish: Boolean,
  onClick: () -> Unit
) {
  val accent = if (car.isSold) EmeraldLight else HoloNeonBlue
  val valAmount = if (car.isSold && car.soldPrice > 0) car.soldPrice else car.currentValue
  CyberRecordButton(
    title = car.name,
    subtitle = buildString {
      append(if (isKurdish) "کڕین: ${CurrencyService.format(car.initialCapital, car.currency)}" else "Cost: ${CurrencyService.format(car.initialCapital, car.currency)}")
      if (car.isCoOwned && car.partnerName.isNotBlank()) append(" • 🤝 ${car.partnerName}")
    },
    primaryValue = CurrencyService.format(valAmount, car.currency),
    accentColor = accent,
    icon = Icons.Default.DirectionsCar,
    statusBadge = if (car.isSold) (if (isKurdish) "فرۆتییە" else "Sold") else (if (isKurdish) "ل پێشانگەهێ یە" else "In Showroom"),
    statusBadgeColor = accent,
    actionHintLabel = if (isKurdish) "ڤەکرن" else "Open",
    onClick = onClick,
    testTagKey = "quick_cat_car_${car.id}"
  )
}

@Composable
private fun PropertyListItemCard(
  property: InvestmentEntity,
  displayCurrency: String,
  isKurdish: Boolean,
  onClick: () -> Unit
) {
  CyberRecordButton(
    title = property.name,
    subtitle = property.notes.ifBlank { property.category },
    primaryValue = CurrencyService.format(property.currentValue, property.currency),
    accentColor = HoloNeonEmerald,
    icon = Icons.Default.Apartment,
    statusBadge = if (property.isSold) (if (isKurdish) "فرۆتی" else "Sold") else (if (isKurdish) "چالاک" else "Active"),
    statusBadgeColor = HoloNeonEmerald,
    actionHintLabel = if (isKurdish) "ڤەکرن" else "Open",
    onClick = onClick,
    testTagKey = "quick_cat_prop_${property.id}"
  )
}

@Composable
private fun DebtListItemCard(
  debt: ActiveDebtCreditLoan,
  displayCurrency: String,
  iqdRate: Double,
  effectiveGoldPricePerGram24K: Double,
  isKurdish: Boolean,
  onClick: () -> Unit
) {
  val isMyDebt = debt.type == "DEBT" || debt.type == "PAYABLE" || debt.type == "DEBT_ON_ME"
  val convertedDisplay = if (debt.isGoldDebt) {
    val usdVal = debt.goldLiveValueUsd(effectiveGoldPricePerGram24K)
    CurrencyService.convert(usdVal, "USD", displayCurrency, iqdRate)
  } else {
    CurrencyService.convert(debt.remainingBalance, debt.currency, displayCurrency, iqdRate)
  }
  val showConvertedSubtitle = debt.isGoldDebt || !debt.currency.equals(displayCurrency, ignoreCase = true)
  val accent = if (isMyDebt) LossRed else HoloNeonCyan
  val primaryStr = if (debt.isGoldDebt) {
    "${String.format(Locale.US, "%.2f", debt.goldRemainingMithqal)} ${if (isKurdish) "مسقال" else "Mithqal"}"
  } else {
    CurrencyService.format(debt.remainingBalance, debt.currency)
  }
  CyberRecordButton(
    title = debt.personName,
    subtitle = if (isMyDebt) (if (isKurdish) "📤 قەرزێ لسەر منە (Payable)" else "📤 I Owe (Payable)") else (if (isKurdish) "📥 قەرزێ دەف خەلکی یە (Receivable)" else "📥 They Owe Me (Receivable)"),
    primaryValue = primaryStr,
    secondaryValue = if (showConvertedSubtitle) "≈ ${CurrencyService.format(convertedDisplay, displayCurrency)}" else null,
    accentColor = accent,
    icon = Icons.Default.Handshake,
    statusBadge = if (isMyDebt) (if (isKurdish) "لسەر من" else "Payable") else (if (isKurdish) "دەف خەلکی" else "Receivable"),
    statusBadgeColor = accent,
    actionHintLabel = if (isKurdish) "کردار" else "Details",
    onClick = onClick,
    testTagKey = "quick_cat_debt_${debt.id}"
  )
}

@Composable
private fun SchoolListItemCard(
  tx: TransactionEntity,
  displayCurrency: String,
  isKurdish: Boolean,
  onClick: () -> Unit
) {
  CyberRecordButton(
    title = tx.category,
    subtitle = tx.notes.ifBlank { if (isKurdish) "تۆمارا قوتابخانێ" else "School Expense" },
    primaryValue = CurrencyService.format(tx.amount, tx.currency),
    accentColor = HoloNeonViolet,
    icon = Icons.Default.School,
    statusBadge = if (tx.affectsCapital) (if (isKurdish) "ژ کاشی" else "From Cash") else (if (isKurdish) "جودا" else "Separate"),
    statusBadgeColor = HoloNeonViolet,
    actionHintLabel = if (isKurdish) "هوردەکاری" else "Open",
    onClick = onClick,
    testTagKey = "quick_cat_school_${tx.id}"
  )
}

@Composable
private fun EmptyCategoryNotice(title: String, tip: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White.copy(alpha = 0.04f))
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
      Text(text = tip, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
  }
}
