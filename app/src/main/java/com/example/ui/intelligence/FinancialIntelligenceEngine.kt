package com.example.ui.intelligence

import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.ui.FinanceUiState
import com.example.ui.history.ActionSection
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

enum class InsightSeverity {
  CRITICAL,
  WARNING,
  OPPORTUNITY,
  INFO
}

enum class DebtPayoffStrategy(
  val labelKu: String,
  val labelEn: String,
  val subtitleKu: String,
  val subtitleEn: String
) {
  AVALANCHE(
    labelKu = "🔥 قەرزێن مەزن و ب مەترسی (Avalanche)",
    labelEn = "🔥 Highest Impact First (Avalanche)",
    subtitleKu = "ئەولەویەت بۆ قەرزێن درەنگکەفتی و کوژمێن هەرە مەزن بۆ کێمکرنا مەترسیێ",
    subtitleEn = "Prioritizes overdue and largest obligations first to minimize risk exposure"
  ),
  SNOWBALL(
    labelKu = "⚡ خەلاسکرنا بلەز (Snowball)",
    labelEn = "⚡ Quick Wins First (Snowball)",
    subtitleKu = "ئەولەویەت بۆ قەرزێن بچووک دا کو زوو بهێنە داخستن و بارێ تە سڤک ببیت",
    subtitleEn = "Clears smallest balances first for immediate account closures and momentum"
  )
}

data class AnomalyAlert(
  val id: String,
  val severity: InsightSeverity,
  val section: ActionSection,
  val titleKu: String,
  val titleEn: String,
  val descriptionKu: String,
  val descriptionEn: String,
  val metricBadge: String,
  val targetTab: Int,
  val entityType: String? = null,
  val entityId: Long? = null,
  val actionLabelKu: String = "پێشبینین و چارەسەرکرن",
  val actionLabelEn: String = "Inspect & Resolve"
) {
  fun title(isKurdish: Boolean): String = if (isKurdish) titleKu else titleEn
  fun description(isKurdish: Boolean): String = if (isKurdish) descriptionKu else descriptionEn
  fun actionLabel(isKurdish: Boolean): String = if (isKurdish) actionLabelKu else actionLabelEn
}

data class Forecast30Day(
  val currentPureCash: Double = 0.0,
  val dailyInflowAvg: Double = 0.0,
  val dailyGeneralBurnAvg: Double = 0.0,
  val dailySchoolBurnAvg: Double = 0.0,
  val projected30DayIncome: Double = 0.0,
  val projected30DayGeneralExpense: Double = 0.0,
  val projected30DaySchoolExpense: Double = 0.0,
  val scheduledCarInstallmentsIn: Double = 0.0,
  val projectedMonthEndCash: Double = 0.0,
  val net30DayProjectedFlow: Double = 0.0,
  val cashRunwayDays: Int = 999,
  val savingsRatePercent: Double = 0.0,
  val summaryKu: String = "",
  val summaryEn: String = ""
)

data class BudgetRecommendation(
  val category: String,
  val categoryLabelKu: String,
  val categoryLabelEn: String,
  val isSchoolCategory: Boolean,
  val currentMonthSpentDisplay: Double,
  val historicalMonthlyAvgDisplay: Double,
  val recommendedMonthlyLimitDisplay: Double,
  val existingBudgetId: Long? = null,
  val existingLimitDisplay: Double? = null,
  val potentialMonthlySavingsDisplay: Double = 0.0,
  val reasonKu: String,
  val reasonEn: String
) {
  fun categoryLabel(isKurdish: Boolean): String = if (isKurdish) categoryLabelKu else categoryLabelEn
  fun reason(isKurdish: Boolean): String = if (isKurdish) reasonKu else reasonEn
  val isAlreadyAligned: Boolean
    get() = existingLimitDisplay != null &&
      abs(existingLimitDisplay - recommendedMonthlyLimitDisplay) <= max(5.0, recommendedMonthlyLimitDisplay * 0.05)
}

data class DebtPayoffStep(
  val rank: Int,
  val debtEntity: DebtCreditEntity,
  val personName: String,
  val isCreditReceivable: Boolean,
  val remainingDisplay: Double,
  val recommendedPaymentDisplay: Double,
  val remainingAfterPaymentDisplay: Double,
  val willSettleCompletely: Boolean,
  val isOverdue: Boolean,
  val daysUntilDue: Int,
  val estimatedMonthsToClear: Int,
  val reasonKu: String,
  val reasonEn: String
) {
  fun reason(isKurdish: Boolean): String = if (isKurdish) reasonKu else reasonEn
}

data class DebtPayoffPlan(
  val strategy: DebtPayoffStrategy = DebtPayoffStrategy.AVALANCHE,
  val currentPureCashDisplay: Double = 0.0,
  val safeCashReserveDisplay: Double = 0.0,
  val allocatableCashNowDisplay: Double = 0.0,
  val totalActiveDebtDisplay: Double = 0.0,
  val totalActiveCreditDisplay: Double = 0.0,
  val prioritizedDebts: List<DebtPayoffStep> = emptyList(),
  val prioritizedCredits: List<DebtPayoffStep> = emptyList(),
  val debtsClearableImmediatelyCount: Int = 0,
  val strategySummaryKu: String = "",
  val strategySummaryEn: String = ""
)

data class SmartIntelligenceSummary(
  val healthScore: Int = 88,
  val healthStatusKu: String = "باشە",
  val healthStatusEn: String = "Healthy",
  val anomalyAlerts: List<AnomalyAlert> = emptyList(),
  val forecast30Day: Forecast30Day = Forecast30Day(),
  val mlForecastReport: MlEnsembleForecastReport = MlEnsembleForecastReport(),
  val budgetRecommendations: List<BudgetRecommendation> = emptyList(),
  val debtPayoffPlanAvalanche: DebtPayoffPlan = DebtPayoffPlan(strategy = DebtPayoffStrategy.AVALANCHE),
  val debtPayoffPlanSnowball: DebtPayoffPlan = DebtPayoffPlan(strategy = DebtPayoffStrategy.SNOWBALL)
) {
  fun debtPlanFor(strategy: DebtPayoffStrategy): DebtPayoffPlan =
    if (strategy == DebtPayoffStrategy.SNOWBALL) debtPayoffPlanSnowball else debtPayoffPlanAvalanche
}

object FinancialIntelligenceEngine {

  private const val DAY_MILLIS = 86_400_000L

  fun analyze(uiState: FinanceUiState): SmartIntelligenceSummary {
    val now = System.currentTimeMillis()
    val displayCurrency = uiState.displayCurrency
    val iqdRate = uiState.iqdRate

    val forecast = build30DayForecast(uiState, now, displayCurrency, iqdRate)
    val mlForecast = MlBudgetForecastingEngine.buildEnsembleForecast(uiState, forecast, now)
    val anomalies = detectAnomaliesAndInsights(uiState, forecast, now, displayCurrency, iqdRate)
    val budgetRecs = generateBudgetRecommendations(uiState, now, displayCurrency, iqdRate)
    val avalanchePlan = buildDebtPayoffPlan(uiState, DebtPayoffStrategy.AVALANCHE, now, displayCurrency, iqdRate)
    val snowballPlan = buildDebtPayoffPlan(uiState, DebtPayoffStrategy.SNOWBALL, now, displayCurrency, iqdRate)

    val healthScore = calculateFinancialHealthScore(uiState, forecast, anomalies)
    val (statusKu, statusEn) = when {
      healthScore >= 85 -> "زۆر باش و جێگیر" to "Excellent & Resilient"
      healthScore >= 70 -> "باش و هاوسەنگ" to "Strong & Balanced"
      healthScore >= 50 -> "پێدڤی ب چاڤدێریێ" to "Needs Attention"
      else -> "مەترسیا سیولە و قەرزان" to "High Liquidity Risk"
    }

    return SmartIntelligenceSummary(
      healthScore = healthScore,
      healthStatusKu = statusKu,
      healthStatusEn = statusEn,
      anomalyAlerts = anomalies,
      forecast30Day = forecast,
      mlForecastReport = mlForecast,
      budgetRecommendations = budgetRecs,
      debtPayoffPlanAvalanche = avalanchePlan,
      debtPayoffPlanSnowball = snowballPlan
    )
  }

  private fun build30DayForecast(
    uiState: FinanceUiState,
    now: Long,
    displayCurrency: String,
    iqdRate: Double
  ): Forecast30Day {
    val thirtyDaysAgo = now - 30L * DAY_MILLIS
    val sixtyDaysAgo = now - 60L * DAY_MILLIS

    // Convert helper
    fun toDisplay(amount: Double, currency: String, entryRate: Double): Double =
      CurrencyService.convert(amount, currency, displayCurrency, iqdRate, entryIqdRate = entryRate)

    val recentInTxs = uiState.transactions.filter { it.type == "IN" && it.dateMillis >= thirtyDaysAgo }
    val recentGenOutTxs = uiState.generalOutTransactions.filter { it.dateMillis >= thirtyDaysAgo && it.affectsCapital }
    val recentSchoolOutTxs = uiState.schoolTransactions.filter { it.dateMillis >= thirtyDaysAgo }
    val recentSchoolCashOutTxs = recentSchoolOutTxs.filter { it.affectsCapital }

    // Determine effective observation window in days (at least 7 days to avoid extreme spikes on day 1)
    val earliestTxMillis = uiState.transactions.minOfOrNull { it.dateMillis } ?: now
    val activeSpanDays = ((now - earliestTxMillis).coerceAtLeast(7 * DAY_MILLIS) / DAY_MILLIS.toDouble())
      .coerceIn(7.0, 30.0)

    val recentInSum = if (recentInTxs.isNotEmpty()) {
      recentInTxs.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
    } else {
      uiState.transactions.filter { it.type == "IN" && it.dateMillis >= sixtyDaysAgo }
        .sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) } / 2.0
    }

    val recentGenOutSum = if (recentGenOutTxs.isNotEmpty()) {
      recentGenOutTxs.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
    } else {
      uiState.generalExpensesOutDisplay * 0.35
    }

    val recentSchoolSum = if (recentSchoolOutTxs.isNotEmpty()) {
      recentSchoolOutTxs.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
    } else {
      uiState.totalSchoolSpendingDisplay * 0.35
    }

    val recentSchoolCashSum = if (recentSchoolCashOutTxs.isNotEmpty()) {
      recentSchoolCashOutTxs.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
    } else {
      uiState.schoolDeductedFromCashDisplay * 0.35
    }

    val dailyInflow = recentInSum / activeSpanDays
    val dailyGenBurn = recentGenOutSum / activeSpanDays
    val dailySchoolBurn = recentSchoolSum / activeSpanDays
    val dailySchoolCashBurn = recentSchoolCashSum / activeSpanDays

    // Expected car installment inflow over next 30 days (roughly 15% of active remaining car installments)
    val expectedCarInstallmentIn = uiState.totalCarsRemainingMoney * 0.15

    val projected30In = (dailyInflow * 30.0) + expectedCarInstallmentIn
    val projected30GenOut = dailyGenBurn * 30.0
    val projected30SchoolOut = dailySchoolBurn * 30.0
    val projected30CashOut = projected30GenOut + (dailySchoolCashBurn * 30.0)

    val net30Flow = projected30In - projected30CashOut
    val projectedEndCash = uiState.pureCashBalance + net30Flow

    val totalDailyCashBurn = dailyGenBurn + dailySchoolCashBurn
    val runwayDays = when {
      uiState.pureCashBalance <= 0.0 -> 0
      totalDailyCashBurn <= 0.01 -> 999
      else -> (uiState.pureCashBalance / totalDailyCashBurn).roundToInt().coerceIn(0, 999)
    }

    val totalInReference = max(projected30In, uiState.totalMoneyIn)
    val totalOutReference = projected30CashOut
    val savingsRate = if (totalInReference > 0.0) {
      (((totalInReference - totalOutReference) / totalInReference) * 100.0).coerceIn(-100.0, 100.0)
    } else {
      0.0
    }

    val formattedEndCash = CurrencyService.format(projectedEndCash, displayCurrency)
    val summaryKu = if (net30Flow >= 0) {
      "ب گوێرەی ڕێژەیا خەرجیا ڕۆژانە، کاشێ سافی د ٣٠ ڕۆژێن بهێت دا دێ گەهیتە $formattedEndCash (+${CurrencyService.format(net30Flow, displayCurrency)})."
    } else {
      "ئاگەهداری: ب ڤێ خێزیا مەسروفاتان، د ٣٠ ڕۆژێن بهێت دا کاشێ سافی ب بڕێ ${CurrencyService.format(abs(net30Flow), displayCurrency)} کێم دبیت و دگەهیتە $formattedEndCash."
    }
    val summaryEn = if (net30Flow >= 0) {
      "At your current velocity, Pure Cash is projected to reach $formattedEndCash in 30 days (+${CurrencyService.format(net30Flow, displayCurrency)})."
    } else {
      "Heads up: At current burn rate, Pure Cash is projected to decrease by ${CurrencyService.format(abs(net30Flow), displayCurrency)} to $formattedEndCash over 30 days."
    }

    return Forecast30Day(
      currentPureCash = uiState.pureCashBalance,
      dailyInflowAvg = dailyInflow,
      dailyGeneralBurnAvg = dailyGenBurn,
      dailySchoolBurnAvg = dailySchoolBurn,
      projected30DayIncome = projected30In,
      projected30DayGeneralExpense = projected30GenOut,
      projected30DaySchoolExpense = projected30SchoolOut,
      scheduledCarInstallmentsIn = expectedCarInstallmentIn,
      projectedMonthEndCash = projectedEndCash,
      net30DayProjectedFlow = net30Flow,
      cashRunwayDays = runwayDays,
      savingsRatePercent = savingsRate,
      summaryKu = summaryKu,
      summaryEn = summaryEn
    )
  }

  private fun detectAnomaliesAndInsights(
    uiState: FinanceUiState,
    forecast: Forecast30Day,
    now: Long,
    displayCurrency: String,
    iqdRate: Double
  ): List<AnomalyAlert> {
    val alerts = mutableListOf<AnomalyAlert>()

    fun toDisplay(amount: Double, currency: String, entryRate: Double): Double =
      CurrencyService.convert(amount, currency, displayCurrency, iqdRate, entryIqdRate = entryRate)

    // 1. Check Overdue or Imminent Debts (I owe)
    val activeDebts = uiState.debtsCredits.filter { it.type == "DEBT" && !it.isCompleted && it.remainingAmount > 0 }
    val overdueDebts = activeDebts.filter { it.dueDateMillis in 1 until now }
    if (overdueDebts.isNotEmpty()) {
      val topOverdue = overdueDebts.maxByOrNull { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }!!
      val totalOverdueVal = overdueDebts.sumOf { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }
      alerts.add(
        AnomalyAlert(
          id = "overdue_debt_${topOverdue.id}",
          severity = InsightSeverity.CRITICAL,
          section = ActionSection.DEBT,
          titleKu = "🚨 ${overdueDebts.size} قەرزێن لسەر تە دەمێ وان بووریە",
          titleEn = "🚨 ${overdueDebts.size} Overdue Debt Obligation(s)",
          descriptionKu = "پێشینە: ${topOverdue.personName} (${CurrencyService.format(totalOverdueVal, displayCurrency)} ماوە). پلانا دانا قەرزان ڤەکە بۆ چارەسەرکرنێ.",
          descriptionEn = "Top priority: ${topOverdue.personName} (${CurrencyService.format(totalOverdueVal, displayCurrency)} overdue). Use Debt Optimizer to settle safely.",
          metricBadge = CurrencyService.format(totalOverdueVal, displayCurrency),
          targetTab = 1,
          entityType = "DEBT",
          entityId = topOverdue.id,
          actionLabelKu = "چوونا قەرزان",
          actionLabelEn = "Open Debt"
        )
      )
    }

    // 2. Check Overdue or High-Value Credits Receivable (People owe me)
    val activeCredits = uiState.debtsCredits.filter { it.type == "CREDIT" && !it.isCompleted && it.remainingAmount > 0 }
    if (activeCredits.isNotEmpty()) {
      val overdueCredits = activeCredits.filter { it.dueDateMillis in 1 until now }
      val targetList = overdueCredits.ifEmpty { activeCredits }
      val topCredit = targetList.maxByOrNull { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }!!
      val totalCreditVal = targetList.sumOf { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }
      alerts.add(
        AnomalyAlert(
          id = "credit_collection_${topCredit.id}",
          severity = if (overdueCredits.isNotEmpty()) InsightSeverity.WARNING else InsightSeverity.OPPORTUNITY,
          section = ActionSection.DEBT,
          titleKu = if (overdueCredits.isNotEmpty()) {
            "⏳ دەمێ وەرگرتنا ${overdueCredits.size} قەرزێن دەف خەلکی هاتیە"
          } else {
            "💰 دەرفەتا زێدەکرنا کاشی ژ قەرزێن دەف خەلکی"
          },
          titleEn = if (overdueCredits.isNotEmpty()) {
            "⏳ ${overdueCredits.size} Receivable Loan(s) Past Due"
          } else {
            "💰 Cash Boost Opportunity from Receivables"
          },
          descriptionKu = "وەرگرتنا قەرزێ دەف ${topCredit.personName} کاشێ تە یێ بەردەست ب بڕێ ${CurrencyService.format(totalCreditVal, displayCurrency)} زێدە دکەت.",
          descriptionEn = "Collecting from ${topCredit.personName} and active debtors adds ${CurrencyService.format(totalCreditVal, displayCurrency)} directly to your Pure Cash.",
          metricBadge = "+${CurrencyService.format(totalCreditVal, displayCurrency)}",
          targetTab = 1,
          entityType = "DEBT",
          entityId = topCredit.id,
          actionLabelKu = "تۆمارکرنا وەرگرتنێ",
          actionLabelEn = "Collect Now"
        )
      )
    }

    // 3. Unusual Spending Anomaly Detection (General Expenses & School Account)
    val outTxs = uiState.transactions.filter { it.type == "OUT" }
    if (outTxs.size >= 2) {
      val byCategory = outTxs.groupBy { if (uiState.isSchoolTransaction(it)) "قوتابخانە" else it.category }
      for ((cat, list) in byCategory) {
        if (list.size >= 2) {
          val convertedList = list.map { it to toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
          val avg = convertedList.sumOf { it.second } / convertedList.size.toDouble()
          val maxPair = convertedList.maxByOrNull { it.second }
          if (maxPair != null && avg > 0 && maxPair.second >= avg * 1.75 && maxPair.second >= 25.0) {
            val spikeTx = maxPair.first
            val ratio = (maxPair.second / avg)
            val isSchool = uiState.isSchoolTransaction(spikeTx)
            alerts.add(
              AnomalyAlert(
                id = "anomaly_tx_${spikeTx.id}",
                severity = InsightSeverity.WARNING,
                section = if (isSchool) ActionSection.SCHOOL else ActionSection.CASH,
                titleKu = if (isSchool) {
                  "🎓 خەرجیەکا نەئاسایی د حسابا قوتابخانێ دا"
                } else {
                  "⚡ بەرزبوونا نەئاسایی یا مەسروفێ ($cat)"
                },
                titleEn = if (isSchool) {
                  "🎓 Unusual Spike in School Account"
                } else {
                  "⚡ Spending Anomaly Detected in $cat"
                },
                descriptionKu = "تۆمارا «${spikeTx.notes.ifBlank { spikeTx.category }}» ب بڕێ ${CurrencyService.format(maxPair.second, displayCurrency)} (${"%.1f".format(ratio)}x پتر ژ ناڤەنجیا ئاسایی ${CurrencyService.format(avg, displayCurrency)}).",
                descriptionEn = "Entry '${spikeTx.notes.ifBlank { spikeTx.category }}' of ${CurrencyService.format(maxPair.second, displayCurrency)} is ${"%.1f".format(ratio)}x above your ${CurrencyService.format(avg, displayCurrency)} category average.",
                metricBadge = "${"%.1f".format(ratio)}x Avg",
                targetTab = 3,
                entityType = "TRANSACTION",
                entityId = spikeTx.id,
                actionLabelKu = "پێشبینین و دەستکاری",
                actionLabelEn = "Preview & Edit"
              )
            )
          }
        }
      }
    }

    // 4. Dedicated School Account Summary Insight
    if (uiState.schoolTransactions.isNotEmpty()) {
      val totalSchool = uiState.totalSchoolSpendingDisplay
      val cashDeducted = uiState.schoolDeductedFromCashDisplay
      val nonCash = uiState.schoolNotDeductedFromCashDisplay
      alerts.add(
        AnomalyAlert(
          id = "school_account_intelligence",
          severity = InsightSeverity.INFO,
          section = ActionSection.SCHOOL,
          titleKu = "🎓 شیکاریا حسابا تایبەت یا قوتابخانێ (${uiState.schoolTransactions.size} تۆمار)",
          titleEn = "🎓 Dedicated School Account Intelligence (${uiState.schoolTransactions.size} items)",
          descriptionKu = "کۆما گشتی: ${CurrencyService.format(totalSchool, displayCurrency)} • ژ کاشی هاتیە کێمکرن: ${CurrencyService.format(cashDeducted, displayCurrency)} • بێی کێمکرن ژ کاشی: ${CurrencyService.format(nonCash, displayCurrency)}.",
          descriptionEn = "Total School: ${CurrencyService.format(totalSchool, displayCurrency)} • Deducted from Cash: ${CurrencyService.format(cashDeducted, displayCurrency)} • Tracked separately: ${CurrencyService.format(nonCash, displayCurrency)}.",
          metricBadge = CurrencyService.format(totalSchool, displayCurrency),
          targetTab = 3,
          actionLabelKu = "حسابا قوتابخانێ",
          actionLabelEn = "School Vault"
        )
      )
    }

    // 5. Cash Runway / Liquidity Warning
    if (uiState.pureCashBalance < 0.0 || (forecast.cashRunwayDays in 1..45)) {
      alerts.add(
        AnomalyAlert(
          id = "liquidity_runway_warning",
          severity = InsightSeverity.CRITICAL,
          section = ActionSection.CASH,
          titleKu = if (uiState.pureCashBalance < 0.0) {
            "⚠️ کاشێ سافی ب سالب دەرکەفتیە"
          } else {
            "⚠️ کاشێ بەردەست تەنێ بۆ ${forecast.cashRunwayDays} ڕۆژان تێرا دکەت"
          },
          titleEn = if (uiState.pureCashBalance < 0.0) {
            "⚠️ Pure Cash Balance is Negative"
          } else {
            "⚠️ Low Cash Runway: ${forecast.cashRunwayDays} Days Remaining"
          },
          descriptionKu = "پێشنیار: قستێن سەیاران یان قەرزێن دەف خەلکی وەربگرە، یان مەسروفاتێن نەپێدڤی ژ کاشی لادە.",
          descriptionEn = "Recommendation: Collect pending car installments/receivables or toggle non-cash items to preserve liquidity.",
          metricBadge = CurrencyService.format(uiState.pureCashBalance, displayCurrency),
          targetTab = 3,
          actionLabelKu = "ڕێکخستنا کاشی",
          actionLabelEn = "Manage Cash"
        )
      )
    }

    // 6. Cars Installment Collection & Fleet Profitability Insight
    if (uiState.totalCarsRemainingMoney > 0.0) {
      val topCar = uiState.carInvestments.maxByOrNull { uiState.getCarRemainingMoney(it) }
      alerts.add(
        AnomalyAlert(
          id = "car_installments_remaining",
          severity = InsightSeverity.OPPORTUNITY,
          section = ActionSection.CAR,
          titleKu = "🚗 ${CurrencyService.format(uiState.totalCarsRemainingMoney, displayCurrency)} قستێن سەیاران ماینە د لایێ کریاران دا",
          titleEn = "🚗 ${CurrencyService.format(uiState.totalCarsRemainingMoney, displayCurrency)} Pending Car Installments",
          descriptionKu = "تۆمارکرنا هەر قستەکا نوو ڕاستەوخۆ کاشێ سافی و صافی سەروەتێ تە زێدە دکەت.",
          descriptionEn = "Recording incoming vehicle installments directly increases your Pure Cash and Clean Net Worth.",
          metricBadge = "+${CurrencyService.format(uiState.totalCarsRemainingMoney, displayCurrency)}",
          targetTab = 2,
          entityType = "CAR",
          entityId = topCar?.id,
          actionLabelKu = "پشکنینا سەیاران",
          actionLabelEn = "View Fleet"
        )
      )
    }

    // 7. Gold & Silver Portfolio Appreciation Insight
    if (uiState.activeGoldHoldings.isNotEmpty() && abs(uiState.totalGoldProfitLossDisplay) > 1.0) {
      val isGain = uiState.totalGoldProfitLossDisplay >= 0.0
      alerts.add(
        AnomalyAlert(
          id = "gold_portfolio_insight",
          severity = if (isGain) InsightSeverity.OPPORTUNITY else InsightSeverity.INFO,
          section = ActionSection.GOLD,
          titleKu = if (isGain) {
            "🥇 خەزینەیا زێڕ و زیڤی د قازانجێ دایە (+${"%.1f".format(uiState.totalGoldRoiPercent)}%)"
          } else {
            "🥇 گوهۆڕینا بهایێ زێڕ و زیڤی (${"%.1f".format(uiState.totalGoldRoiPercent)}%)"
          },
          titleEn = if (isGain) {
            "🥇 Precious Metals Vault in Profit (+${"%.1f".format(uiState.totalGoldRoiPercent)}%)"
          } else {
            "🥇 Precious Metals Market Fluctuation (${"%.1f".format(uiState.totalGoldRoiPercent)}%)"
          },
          descriptionKu = "بهایێ نوکە: ${CurrencyService.format(uiState.totalGoldCurrentValueDisplay, displayCurrency)} (${if (isGain) "+" else ""}${CurrencyService.format(uiState.totalGoldProfitLossDisplay, displayCurrency)} جیاوازی لگەل کڕینێ).",
          descriptionEn = "Current valuation: ${CurrencyService.format(uiState.totalGoldCurrentValueDisplay, displayCurrency)} (${if (isGain) "+" else ""}${CurrencyService.format(uiState.totalGoldProfitLossDisplay, displayCurrency)} vs purchase cost).",
          metricBadge = "${if (isGain) "+" else ""}${"%.1f".format(uiState.totalGoldRoiPercent)}%",
          targetTab = 2,
          actionLabelKu = "خەزینەیا زێڕی",
          actionLabelEn = "Open Vault"
        )
      )
    }

    // Ensure there's always at least one constructive executive summary insight if ledger is clean
    if (alerts.isEmpty()) {
      alerts.add(
        AnomalyAlert(
          id = "executive_all_clear",
          severity = InsightSeverity.OPPORTUNITY,
          section = ActionSection.CAPITAL,
          titleKu = "✅ سەروەت و کاشێ تە ب تەمامی ڕێکخستی و هاوسەنگە",
          titleEn = "✅ Your Wealth & Cash Ledger is Fully Balanced",
          descriptionKu = "چ خەرجیێن نەئاسایی یان قەرزێن درەنگکەفتی نەهاتنە دیتن. صافی سەروەت: ${CurrencyService.format(uiState.netWorth, displayCurrency)}.",
          descriptionEn = "No unusual spending spikes or overdue debts detected. Clean Net Worth: ${CurrencyService.format(uiState.netWorth, displayCurrency)}.",
          metricBadge = CurrencyService.format(uiState.netWorth, displayCurrency),
          targetTab = 0,
          actionLabelKu = "پێشبینین",
          actionLabelEn = "Overview"
        )
      )
    }

    return alerts.sortedBy { it.severity.ordinal }
  }

  private fun generateBudgetRecommendations(
    uiState: FinanceUiState,
    now: Long,
    displayCurrency: String,
    iqdRate: Double
  ): List<BudgetRecommendation> {
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    fun toDisplay(amount: Double, currency: String, entryRate: Double): Double =
      CurrencyService.convert(amount, currency, displayCurrency, iqdRate, entryIqdRate = entryRate)

    val outTxs = uiState.transactions.filter { it.type == "OUT" }
    val recommendations = mutableListOf<BudgetRecommendation>()

    // Group into canonical categories (separating School into its own dedicated recommendation)
    val grouped = outTxs.groupBy { tx ->
      if (uiState.isSchoolTransaction(tx)) "قوتابخانە" else tx.category.ifBlank { "خەرجیا گشتی" }
    }

    // Calculate how many distinct months exist in history (minimum 1)
    val distinctMonths = outTxs.map { tx ->
      val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
      "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}"
    }.distinct().size.coerceAtLeast(1)

    for ((category, txs) in grouped) {
      val totalHistorical = txs.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
      val monthlyAvg = totalHistorical / distinctMonths.toDouble()

      val currentMonthSpent = txs.filter { tx ->
        val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
        c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
      }.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }

      val referenceSpend = max(currentMonthSpent, monthlyAvg)
      if (referenceSpend <= 0.0) continue

      // Recommend an optimal budget target (90% of reference spend to save 10%, rounded cleanly)
      val rawRecommended = if (currentMonthSpent > monthlyAvg * 1.15 && monthlyAvg > 0) {
        monthlyAvg
      } else {
        referenceSpend * 0.90
      }
      val step = if (displayCurrency == "IQD") 25_000.0 else 25.0
      val roundedRecommended = (ceil(rawRecommended / step) * step).coerceAtLeast(step)

      val existingBudget = uiState.budgets.find {
        it.category.equals(category, ignoreCase = true) ||
          (category == "قوتابخانە" && (it.category.contains("قوتابخانە") || it.category.contains("School", ignoreCase = true)))
      }
      val existingLimit = existingBudget?.let { toDisplay(it.monthlyLimit, it.currency, it.exchangeRateAtEntry) }
      val potentialSavings = (referenceSpend - roundedRecommended).coerceAtLeast(0.0)

      val isSchool = category == "قوتابخانە"
      recommendations.add(
        BudgetRecommendation(
          category = category,
          categoryLabelKu = if (isSchool) "🎓 حسابا قوتابخانێ (قوتابخانە)" else category,
          categoryLabelEn = if (isSchool) "🎓 School & Education (قوتابخانە)" else category,
          isSchoolCategory = isSchool,
          currentMonthSpentDisplay = currentMonthSpent,
          historicalMonthlyAvgDisplay = monthlyAvg,
          recommendedMonthlyLimitDisplay = roundedRecommended,
          existingBudgetId = existingBudget?.id,
          existingLimitDisplay = existingLimit,
          potentialMonthlySavingsDisplay = potentialSavings,
          reasonKu = if (existingLimit == null) {
            "چ سنور نەهاتینە دانان • پێشنیار: ${CurrencyService.format(roundedRecommended, displayCurrency)} د هەیڤێ دا بۆ پاشەکەفتکرنا 10%."
          } else {
            "بودجەیا نوکە: ${CurrencyService.format(existingLimit, displayCurrency)} • سنورێ نموونەیی: ${CurrencyService.format(roundedRecommended, displayCurrency)}."
          },
          reasonEn = if (existingLimit == null) {
            "No cap set • Recommended ${CurrencyService.format(roundedRecommended, displayCurrency)}/mo to capture a 10% efficiency buffer."
          } else {
            "Current cap: ${CurrencyService.format(existingLimit, displayCurrency)} • Optimal target: ${CurrencyService.format(roundedRecommended, displayCurrency)}."
          }
        )
      )
    }

    // If user has no expense categories yet, provide 3 starter smart budget templates
    if (recommendations.isEmpty()) {
      val baseCap = max(uiState.pureCashBalance, 1000.0)
      val step = if (displayCurrency == "IQD") 50_000.0 else 50.0
      val monthlyOverall = (ceil((baseCap * 0.15) / step) * step).coerceAtLeast(step * 4)
      val schoolStarter = (ceil((baseCap * 0.05) / step) * step).coerceAtLeast(step * 2)

      recommendations.add(
        BudgetRecommendation(
          category = "Monthly Spending",
          categoryLabelKu = "📊 خەرجیا هەیڤانە یا گشتی",
          categoryLabelEn = "📊 Total Monthly Spending",
          isSchoolCategory = false,
          currentMonthSpentDisplay = 0.0,
          historicalMonthlyAvgDisplay = 0.0,
          recommendedMonthlyLimitDisplay = monthlyOverall,
          existingBudgetId = uiState.budgets.find { it.category == "Monthly Spending" }?.id,
          existingLimitDisplay = uiState.budgets.find { it.category == "Monthly Spending" }?.let {
            toDisplay(it.monthlyLimit, it.currency, it.exchangeRateAtEntry)
          },
          potentialMonthlySavingsDisplay = monthlyOverall * 0.10,
          reasonKu = "پێشنیارا زیرەک (15% ژ سیولەیا بەردەست) بۆ پاراستنا سەرمایێ کاش.",
          reasonEn = "Smart baseline cap (15% of liquid cash) to protect core capital."
        )
      )
      recommendations.add(
        BudgetRecommendation(
          category = "قوتابخانە",
          categoryLabelKu = "🎓 حسابا تایبەت یا قوتابخانێ",
          categoryLabelEn = "🎓 Dedicated School Account",
          isSchoolCategory = true,
          currentMonthSpentDisplay = uiState.totalSchoolSpendingDisplay,
          historicalMonthlyAvgDisplay = uiState.totalSchoolSpendingDisplay,
          recommendedMonthlyLimitDisplay = schoolStarter,
          existingBudgetId = uiState.budgets.find { it.category.contains("قوتابخانە") }?.id,
          existingLimitDisplay = uiState.budgets.find { it.category.contains("قوتابخانە") }?.let {
            toDisplay(it.monthlyLimit, it.currency, it.exchangeRateAtEntry)
          },
          potentialMonthlySavingsDisplay = 0.0,
          reasonKu = "دیارکرنا بودجەیا تایبەت بۆ خەرجیێن قوتابخانێ ب جودا ژ مەسروفاتان.",
          reasonEn = "Dedicated monthly allocation for school & education expenses."
        )
      )
    }

    return recommendations.sortedByDescending { it.currentMonthSpentDisplay + it.recommendedMonthlyLimitDisplay }
  }

  private fun buildDebtPayoffPlan(
    uiState: FinanceUiState,
    strategy: DebtPayoffStrategy,
    now: Long,
    displayCurrency: String,
    iqdRate: Double
  ): DebtPayoffPlan {
    fun toDisplay(amount: Double, currency: String, entryRate: Double): Double =
      CurrencyService.convert(amount, currency, displayCurrency, iqdRate, entryIqdRate = entryRate)

    val pureCash = uiState.pureCashBalance.coerceAtLeast(0.0)
    // Reserve 20% of pure cash as safety liquidity buffer
    val safeReserve = pureCash * 0.20
    val allocatableCash = (pureCash - safeReserve).coerceAtLeast(0.0)

    val activeDebts = uiState.debtsCredits
      .filter { it.type == "DEBT" && !it.isCompleted && it.remainingAmount > 0.0 }

    val sortedDebts = when (strategy) {
      DebtPayoffStrategy.AVALANCHE -> {
        // Overdue first, then largest remaining amount in display currency
        activeDebts.sortedWith(
          compareByDescending<DebtCreditEntity> { it.dueDateMillis in 1 until now }
            .thenByDescending { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }
        )
      }
      DebtPayoffStrategy.SNOWBALL -> {
        // Smallest remaining amount first for quick closure wins
        activeDebts.sortedWith(
          compareBy<DebtCreditEntity> { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }
            .thenBy { if (it.dueDateMillis > 0) it.dueDateMillis else Long.MAX_VALUE }
        )
      }
    }

    var remainingPool = allocatableCash
    var clearableNowCount = 0

    val debtSteps = sortedDebts.mapIndexed { idx, debt ->
      val remDisplay = toDisplay(debt.remainingAmount, debt.currency, debt.exchangeRateAtEntry)
      val recommendedPay = when {
        remainingPool >= remDisplay -> remDisplay
        remainingPool > 0.0 -> remainingPool
        else -> (remDisplay * 0.25).coerceAtLeast(minOf(remDisplay, if (displayCurrency == "IQD") 50_000.0 else 50.0))
      }
      if (remainingPool >= remDisplay && remDisplay > 0.0) {
        clearableNowCount++
      }
      remainingPool = (remainingPool - recommendedPay).coerceAtLeast(0.0)

      val remAfter = (remDisplay - recommendedPay).coerceAtLeast(0.0)
      val isOverdue = debt.dueDateMillis in 1 until now
      val daysUntilDue = if (debt.dueDateMillis > 0) {
        ((debt.dueDateMillis - now) / DAY_MILLIS).toInt()
      } else 30

      val monthlyPace = max(recommendedPay, remDisplay * 0.25)
      val estMonths = if (remAfter <= 0.01) 1 else ceil(remDisplay / monthlyPace).toInt().coerceIn(1, 36)

      val reasonKu = when {
        isOverdue -> "🚨 دەمێ وێ بووریە • پێدڤیە د رێزا ئێکێ دا بهێتە دان"
        strategy == DebtPayoffStrategy.AVALANCHE -> "🔥 مەزنترین بارێ قەرزی • کێمکرنا مەترسیا سەرمایەی"
        else -> "⚡ قەرزێ بچووک • ب ئێک پێنگاڤ ب تەمامی خەلاس دکەت"
      }
      val reasonEn = when {
        isOverdue -> "🚨 Past due date • Immediate settlement priority"
        strategy == DebtPayoffStrategy.AVALANCHE -> "🔥 Largest capital liability • Reduces debt-to-asset ratio fastest"
        else -> "⚡ Quickest account closure • Eliminates an obligation immediately"
      }

      DebtPayoffStep(
        rank = idx + 1,
        debtEntity = debt,
        personName = debt.personName,
        isCreditReceivable = false,
        remainingDisplay = remDisplay,
        recommendedPaymentDisplay = recommendedPay,
        remainingAfterPaymentDisplay = remAfter,
        willSettleCompletely = remAfter <= 0.01,
        isOverdue = isOverdue,
        daysUntilDue = daysUntilDue,
        estimatedMonthsToClear = estMonths,
        reasonKu = reasonKu,
        reasonEn = reasonEn
      )
    }

    val activeCredits = uiState.debtsCredits
      .filter { it.type == "CREDIT" && !it.isCompleted && it.remainingAmount > 0.0 }
      .sortedWith(
        compareByDescending<DebtCreditEntity> { it.dueDateMillis in 1 until now }
          .thenByDescending { toDisplay(it.remainingAmount, it.currency, it.exchangeRateAtEntry) }
      )

    val creditSteps = activeCredits.mapIndexed { idx, credit ->
      val remDisplay = toDisplay(credit.remainingAmount, credit.currency, credit.exchangeRateAtEntry)
      val isOverdue = credit.dueDateMillis in 1 until now
      val daysUntilDue = if (credit.dueDateMillis > 0) {
        ((credit.dueDateMillis - now) / DAY_MILLIS).toInt()
      } else 14

      DebtPayoffStep(
        rank = idx + 1,
        debtEntity = credit,
        personName = credit.personName,
        isCreditReceivable = true,
        remainingDisplay = remDisplay,
        recommendedPaymentDisplay = remDisplay,
        remainingAfterPaymentDisplay = 0.0,
        willSettleCompletely = true,
        isOverdue = isOverdue,
        daysUntilDue = daysUntilDue,
        estimatedMonthsToClear = 1,
        reasonKu = if (isOverdue) "⏳ ژ وادەیێ خۆ بووریە • وەربگرە بۆ زێدەکرنا کاشی" else "💵 وەرگرتنا ڤی قەرزی کاشێ سافی زێدە دکەت",
        reasonEn = if (isOverdue) "⏳ Past due • Collect now to boost Pure Cash" else "💵 Collecting boosts liquid Pure Cash"
      )
    }

    val totalDebtVal = uiState.totalDebtOwed
    val totalCreditVal = uiState.totalCreditReceivable

    val summaryKu = when {
      activeDebts.isEmpty() -> "✅ چ قەرز لسەر تە نینن! هەمی کاشێ تە یێ ئازادە."
      clearableNowCount > 0 -> "ب هێلانا 20% یەدەگێ کاشی (${CurrencyService.format(safeReserve, displayCurrency)})، دشێی نوکە $clearableNowCount قەرزان ب تەمامی خەلاس بکەی."
      else -> "بڕێ ${CurrencyService.format(allocatableCash, displayCurrency)} ژ کاشێ تە یێ بەردەستە بۆ کێمکرنا قەرزان بێی تێکدانا سیولەیێ."
    }
    val summaryEn = when {
      activeDebts.isEmpty() -> "✅ Zero active debt obligations! Your liquid cash is 100% unencumbered."
      clearableNowCount > 0 -> "While keeping a 20% cash reserve (${CurrencyService.format(safeReserve, displayCurrency)}), you can completely settle $clearableNowCount debt(s) today."
      else -> "You can safely allocate ${CurrencyService.format(allocatableCash, displayCurrency)} toward debt reduction while preserving your 20% reserve."
    }

    return DebtPayoffPlan(
      strategy = strategy,
      currentPureCashDisplay = pureCash,
      safeCashReserveDisplay = safeReserve,
      allocatableCashNowDisplay = allocatableCash,
      totalActiveDebtDisplay = totalDebtVal,
      totalActiveCreditDisplay = totalCreditVal,
      prioritizedDebts = debtSteps,
      prioritizedCredits = creditSteps,
      debtsClearableImmediatelyCount = clearableNowCount,
      strategySummaryKu = summaryKu,
      strategySummaryEn = summaryEn
    )
  }

  private fun calculateFinancialHealthScore(
    uiState: FinanceUiState,
    forecast: Forecast30Day,
    anomalies: List<AnomalyAlert>
  ): Int {
    var score = 85

    // Liquidity factor
    if (uiState.pureCashBalance > 0) score += 5 else score -= 20
    if (forecast.cashRunwayDays >= 90) score += 5 else if (forecast.cashRunwayDays < 30) score -= 10

    // Debt-to-asset factor
    val debtRatio = uiState.debtToAssetRatioPercent
    when {
      debtRatio == 0 -> score += 5
      debtRatio <= 25 -> score += 2
      debtRatio in 50..75 -> score -= 8
      debtRatio > 75 -> score -= 15
    }

    // Critical alerts penalty
    val criticalCount = anomalies.count { it.severity == InsightSeverity.CRITICAL }
    score -= (criticalCount * 8)

    // Budget exceed penalty
    val exceededBudgets = uiState.budgetAlerts.count { it.isExceeded }
    score -= (exceededBudgets * 5)

    return score.coerceIn(18, 99)
  }
}
