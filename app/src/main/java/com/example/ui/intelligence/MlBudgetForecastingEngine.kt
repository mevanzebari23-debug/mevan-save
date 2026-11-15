package com.example.ui.intelligence

import com.example.data.currency.CurrencyService
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class MlMonthlyProjectionPoint(
  val monthOffset: Int,
  val monthLabelKu: String,
  val monthLabelEn: String,
  val projectedCashExpenseDisplay: Double,
  val projectedUnifiedSchoolExpenseDisplay: Double,
  val totalProjectedBudgetDisplay: Double,
  val lowerConfidenceDisplay: Double,
  val upperConfidenceDisplay: Double,
  val seasonalityWeight: Double
) {
  fun monthLabel(isKurdish: Boolean): String = if (isKurdish) monthLabelKu else monthLabelEn
}

data class MlCategoryForecast(
  val category: String,
  val labelKu: String,
  val labelEn: String,
  val isUnifiedSchool: Boolean,
  val currentMonthSpentDisplay: Double,
  val historicalMonthlyAvgDisplay: Double,
  val mlProjectedMonthlyDisplay: Double,
  val recommendedBudgetCapDisplay: Double,
  val trendSlopePercent: Double,
  val confidencePercent: Int,
  val existingBudgetId: Long? = null,
  val existingLimitDisplay: Double? = null
) {
  fun label(isKurdish: Boolean): String = if (isKurdish) labelKu else labelEn

  val isAlreadyAligned: Boolean
    get() = existingLimitDisplay != null &&
      abs(existingLimitDisplay - recommendedBudgetCapDisplay) <= max(5.0, recommendedBudgetCapDisplay * 0.05)

  fun toBudgetRecommendation(): BudgetRecommendation {
    val trendSign = if (trendSlopePercent >= 0) "+" else ""
    val trendFormatted = "$trendSign${"%.1f".format(trendSlopePercent)}%"
    return BudgetRecommendation(
      category = category,
      categoryLabelKu = labelKu,
      categoryLabelEn = labelEn,
      isSchoolCategory = isUnifiedSchool,
      currentMonthSpentDisplay = currentMonthSpentDisplay,
      historicalMonthlyAvgDisplay = historicalMonthlyAvgDisplay,
      recommendedMonthlyLimitDisplay = recommendedBudgetCapDisplay,
      existingBudgetId = existingBudgetId,
      existingLimitDisplay = existingLimitDisplay,
      potentialMonthlySavingsDisplay = (mlProjectedMonthlyDisplay - recommendedBudgetCapDisplay).coerceAtLeast(0.0),
      reasonKu = "پێشبینیا ML (ئاراستە $trendFormatted • دڵنیایی $confidencePercent%) بۆ کۆنترۆلکرنا بودجەیێ.",
      reasonEn = "ML Ensemble Projection (trend $trendFormatted • $confidencePercent% confidence) optimal cap."
    )
  }
}

data class MlEnsembleForecastReport(
  val modelConfidencePercent: Int = 86,
  val cashTrendPercent: Double = 0.0,
  val schoolTrendPercent: Double = 0.0,
  val combinedTrendPercent: Double = 0.0,
  val nextMonthCashForecastDisplay: Double = 0.0,
  val nextMonthUnifiedSchoolForecastDisplay: Double = 0.0,
  val nextMonthTotalBudgetDisplay: Double = 0.0,
  val monthlyProjections: List<MlMonthlyProjectionPoint> = emptyList(),
  val categoryForecasts: List<MlCategoryForecast> = emptyList(),
  val modelInsightKu: String = "",
  val modelInsightEn: String = ""
) {
  fun projectionsForHorizon(months: Int): List<MlMonthlyProjectionPoint> =
    monthlyProjections.take(months.coerceIn(1, 6))

  fun horizonTotalBudget(months: Int): Double =
    projectionsForHorizon(months).sumOf { it.totalProjectedBudgetDisplay }

  fun horizonMonthlyAvgBudget(months: Int): Double {
    val slice = projectionsForHorizon(months)
    return if (slice.isEmpty()) 0.0 else slice.sumOf { it.totalProjectedBudgetDisplay } / slice.size.toDouble()
  }

  fun horizonCashBudget(months: Int): Double =
    projectionsForHorizon(months).sumOf { it.projectedCashExpenseDisplay }

  fun horizonUnifiedSchoolBudget(months: Int): Double =
    projectionsForHorizon(months).sumOf { it.projectedUnifiedSchoolExpenseDisplay }

  fun horizonLowerBound(months: Int): Double =
    projectionsForHorizon(months).sumOf { it.lowerConfidenceDisplay }

  fun horizonUpperBound(months: Int): Double =
    projectionsForHorizon(months).sumOf { it.upperConfidenceDisplay }
}

object MlBudgetForecastingEngine {

  private const val DAY_MILLIS = 86_400_000L
  private const val HOLT_ALPHA = 0.62
  private const val HOLT_BETA = 0.28
  private const val HOLT_DAMPING_PHI = 0.86

  fun buildEnsembleForecast(
    uiState: FinanceUiState,
    forecast30Day: Forecast30Day,
    now: Long
  ): MlEnsembleForecastReport {
    val displayCurrency = uiState.displayCurrency
    val iqdRate = uiState.iqdRate

    fun toDisplay(amount: Double, currency: String, entryRate: Double): Double =
      CurrencyService.convert(amount, currency, displayCurrency, iqdRate, entryIqdRate = entryRate)

    val cashOutTxs = uiState.generalOutTransactions
    // Unified School: combine ALL school expenses (both cash-deducted and tracked separately)
    val unifiedSchoolTxs = uiState.schoolTransactions

    val cashSeries = extractMonthlySeries(
      txs = cashOutTxs,
      fallbackMonthlyBaseline = forecast30Day.projected30DayGeneralExpense,
      now = now,
      toDisplay = ::toDisplay
    )

    val schoolSeries = extractMonthlySeries(
      txs = unifiedSchoolTxs,
      fallbackMonthlyBaseline = max(
        forecast30Day.projected30DaySchoolExpense,
        uiState.totalSchoolSpendingDisplay * 0.35
      ),
      now = now,
      toDisplay = ::toDisplay
    )

    val cashFit = fitEnsembleSeries(cashSeries)
    val schoolFit = fitEnsembleSeries(schoolSeries)

    val monthFormatEn = SimpleDateFormat("MMM", Locale.US)
    val kurdishMonthNames = arrayOf(
      "کانوونا دووێ", "شوبات", "ئادار", "نیسان", "گولان", "حوزەیران",
      "تیرمەهـ", "تەباخ", "ئەیلول", "چلیا ئێکێ", "چلیا دووێ", "کانوونا ئێکێ"
    )

    val projections = (1..6).map { horizon ->
      val cal = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.MONTH, horizon)
      }
      val calMonth = cal.get(Calendar.MONTH)
      val mEn = monthFormatEn.format(cal.time)
      val mKu = kurdishMonthNames[calMonth]

      val cashSeasonality = cashSeasonalityFactor(calMonth)
      val schoolSeasonality = schoolAcademicSeasonalityFactor(calMonth)

      val rawCashProj = cashFit.predictAt(horizon) * cashSeasonality
      val rawSchoolProj = schoolFit.predictAt(horizon) * schoolSeasonality
      val totalProj = (rawCashProj + rawSchoolProj).coerceAtLeast(0.0)

      val combinedSigma = sqrt(cashFit.residualStdDev.pow(2) + schoolFit.residualStdDev.pow(2))
      val horizonSpread = max(totalProj * (0.07 + horizon * 0.025), combinedSigma * sqrt(horizon.toDouble()) * 0.65)
      val lowerBound = (totalProj - horizonSpread).coerceAtLeast(totalProj * 0.65)
      val upperBound = totalProj + horizonSpread

      MlMonthlyProjectionPoint(
        monthOffset = horizon,
        monthLabelKu = "M+$horizon ($mKu)",
        monthLabelEn = "M+$horizon ($mEn)",
        projectedCashExpenseDisplay = rawCashProj,
        projectedUnifiedSchoolExpenseDisplay = rawSchoolProj,
        totalProjectedBudgetDisplay = totalProj,
        lowerConfidenceDisplay = lowerBound,
        upperConfidenceDisplay = upperBound,
        seasonalityWeight = (cashSeasonality + schoolSeasonality) / 2.0
      )
    }

    val nextMonth = projections.first()
    val totalSampleCount = cashOutTxs.size + unifiedSchoolTxs.size
    val avgVolatility = ((cashFit.volatilityRatio + schoolFit.volatilityRatio) / 2.0).coerceIn(0.0, 1.0)
    val modelConfidence = calculateModelConfidence(totalSampleCount, avgVolatility)

    val combinedCurrentAvg = (cashFit.historicalAvg + schoolFit.historicalAvg).coerceAtLeast(1.0)
    val combinedTrendPct = (((nextMonth.totalProjectedBudgetDisplay - combinedCurrentAvg) / combinedCurrentAvg) * 100.0)
      .coerceIn(-65.0, 120.0)

    val categoryForecasts = buildCategoryMlForecasts(
      uiState = uiState,
      cashOutTxs = cashOutTxs,
      unifiedSchoolTxs = unifiedSchoolTxs,
      schoolFit = schoolFit,
      nextMonthSchoolProjected = nextMonth.projectedUnifiedSchoolExpenseDisplay,
      modelConfidence = modelConfidence,
      now = now,
      toDisplay = ::toDisplay
    )

    val formattedNextTotal = CurrencyService.format(nextMonth.totalProjectedBudgetDisplay, displayCurrency)
    val formattedCash = CurrencyService.format(nextMonth.projectedCashExpenseDisplay, displayCurrency)
    val formattedSchool = CurrencyService.format(nextMonth.projectedUnifiedSchoolExpenseDisplay, displayCurrency)

    val insightKu = "مۆدێلا فێربوونا ئامێری (OLS + Holt Smoothing) پێشبینی دکەت بودجەیا پێدڤی بۆ هەیڤا بهێت $formattedNextTotal بیت ($formattedCash بۆ کاشێ گشتی + $formattedSchool بۆ کۆما گشتی یا قوتابخانێ)."
    val insightEn = "Ensemble ML (OLS Regression + Holt Smoothing) projects a $formattedNextTotal monthly budget requirement next month ($formattedCash Cash + $formattedSchool Unified School)."

    return MlEnsembleForecastReport(
      modelConfidencePercent = modelConfidence,
      cashTrendPercent = cashFit.trendPercent,
      schoolTrendPercent = schoolFit.trendPercent,
      combinedTrendPercent = combinedTrendPct,
      nextMonthCashForecastDisplay = nextMonth.projectedCashExpenseDisplay,
      nextMonthUnifiedSchoolForecastDisplay = nextMonth.projectedUnifiedSchoolExpenseDisplay,
      nextMonthTotalBudgetDisplay = nextMonth.totalProjectedBudgetDisplay,
      monthlyProjections = projections,
      categoryForecasts = categoryForecasts,
      modelInsightKu = insightKu,
      modelInsightEn = insightEn
    )
  }

  private data class EnsembleSeriesFit(
    val historicalAvg: Double,
    val level: Double,
    val trendSlope: Double,
    val olsIntercept: Double,
    val olsSlope: Double,
    val residualStdDev: Double,
    val volatilityRatio: Double,
    val trendPercent: Double,
    val n: Int
  ) {
    fun predictAt(horizonMonths: Int): Double {
      if (historicalAvg <= 0.0 && level <= 0.0) return 0.0
      val h = horizonMonths.toDouble()
      // Damped Holt projection
      val dampedSum = (1..horizonMonths).sumOf { HOLT_DAMPING_PHI.pow(it.toDouble()) }
      val holtPred = (level + dampedSum * trendSlope).coerceAtLeast(0.0)

      // Damped OLS linear regression projection
      val olsPred = (olsIntercept + olsSlope * ((n - 1) + h * 0.65)).coerceAtLeast(0.0)

      // Ensemble blend: 55% Damped Holt + 45% OLS Linear Regression, bounded reasonably around baseline
      val rawBlend = 0.55 * holtPred + 0.45 * olsPred
      val floorVal = historicalAvg * 0.45
      val ceilingVal = max(historicalAvg * 2.15, level * 1.85)
      return rawBlend.coerceIn(floorVal, ceilingVal)
    }
  }

  private fun extractMonthlySeries(
    txs: List<TransactionEntity>,
    fallbackMonthlyBaseline: Double,
    now: Long,
    toDisplay: (Double, String, Double) -> Double
  ): List<Double> {
    if (txs.isEmpty()) {
      val base = fallbackMonthlyBaseline.coerceAtLeast(0.0)
      return listOf(base * 0.94, base * 0.97, base, base * 1.02)
    }

    // Group transactions into monthly buckets over the last 6 months (oldest to newest)
    val calNow = Calendar.getInstance().apply { timeInMillis = now }
    val currentYear = calNow.get(Calendar.YEAR)
    val currentMonth = calNow.get(Calendar.MONTH)

    val bucketMap = mutableMapOf<Int, Double>()
    for (tx in txs) {
      val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
      val monthDiff = (currentYear - c.get(Calendar.YEAR)) * 12 + (currentMonth - c.get(Calendar.MONTH))
      if (monthDiff in 0..5) {
        val amt = toDisplay(tx.amount, tx.currency, tx.exchangeRateAtEntry)
        bucketMap[monthDiff] = (bucketMap[monthDiff] ?: 0.0) + amt
      }
    }

    val nonZeroMonths = bucketMap.filterValues { it > 0.0 }
    if (nonZeroMonths.size >= 2) {
      val maxAge = nonZeroMonths.keys.maxOrNull() ?: 1
      return (maxAge downTo 0).map { offset ->
        bucketMap[offset] ?: (nonZeroMonths.values.average() * 0.85)
      }
    }

    // If all transactions are within the current month, construct 4 weekly-scaled monthly equivalents
    val earliestMillis = txs.minOfOrNull { it.dateMillis } ?: now
    val spanDays = ((now - earliestMillis).coerceAtLeast(7 * DAY_MILLIS) / DAY_MILLIS.toDouble()).coerceIn(7.0, 30.0)
    val totalVal = txs.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }
    val monthlyEquivalent = max((totalVal / spanDays) * 30.0, fallbackMonthlyBaseline)

    val sortedTxs = txs.sortedBy { it.dateMillis }
    val midIdx = sortedTxs.size / 2
    val firstHalfAvg = if (midIdx > 0) {
      sortedTxs.take(midIdx).sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) } / midIdx
    } else monthlyEquivalent / 4.0
    val secondHalfAvg = if (sortedTxs.size - midIdx > 0) {
      sortedTxs.drop(midIdx).sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) } / (sortedTxs.size - midIdx)
    } else firstHalfAvg

    val momentumRatio = if (firstHalfAvg > 0.0) {
      (secondHalfAvg / firstHalfAvg).coerceIn(0.82, 1.22)
    } else 1.0

    return listOf(
      monthlyEquivalent * (1.0 / momentumRatio).coerceIn(0.85, 1.15),
      monthlyEquivalent * 0.96,
      monthlyEquivalent,
      monthlyEquivalent * momentumRatio
    )
  }

  private fun fitEnsembleSeries(series: List<Double>): EnsembleSeriesFit {
    val clean = series.ifEmpty { listOf(0.0, 0.0) }
    val n = clean.size
    val avg = clean.average()
    if (avg <= 0.001) {
      return EnsembleSeriesFit(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, n)
    }

    // 1. Ordinary Least Squares (OLS) Linear Regression: y = a + b*t
    val tMean = (n - 1) / 2.0
    var num = 0.0
    var den = 0.0
    for (i in 0 until n) {
      val dt = i - tMean
      val dy = clean[i] - avg
      num += dt * dy
      den += dt * dt
    }
    val rawOlsSlope = if (den > 0.0) num / den else 0.0
    val clampedOlsSlope = rawOlsSlope.coerceIn(-avg * 0.25, avg * 0.25)
    val olsIntercept = avg - clampedOlsSlope * tMean

    // 2. Holt's Double Exponential Smoothing (level L_t, trend B_t)
    var level = clean[0]
    var trend = if (n > 1) (clean[1] - clean[0]).coerceIn(-avg * 0.25, avg * 0.25) else 0.0
    val residuals = mutableListOf<Double>()

    for (i in 1 until n) {
      val obs = clean[i]
      val oneStepPred = level + trend
      residuals.add(obs - oneStepPred)
      val prevLevel = level
      level = HOLT_ALPHA * obs + (1.0 - HOLT_ALPHA) * (level + trend)
      trend = HOLT_BETA * (level - prevLevel) + (1.0 - HOLT_BETA) * trend
    }
    trend = trend.coerceIn(-avg * 0.25, avg * 0.25)

    val variance = if (residuals.isNotEmpty()) {
      residuals.sumOf { it * it } / residuals.size.toDouble()
    } else {
      (avg * 0.12).pow(2)
    }
    val stdDev = sqrt(variance).coerceAtLeast(avg * 0.06)
    val volatilityRatio = (stdDev / avg).coerceIn(0.04, 0.65)

    val nextStepBlend = 0.55 * (level + trend * HOLT_DAMPING_PHI) + 0.45 * (olsIntercept + clampedOlsSlope * n)
    val trendPct = (((nextStepBlend - avg) / avg) * 100.0).coerceIn(-55.0, 85.0)

    return EnsembleSeriesFit(
      historicalAvg = avg,
      level = level,
      trendSlope = trend,
      olsIntercept = olsIntercept,
      olsSlope = clampedOlsSlope,
      residualStdDev = stdDev,
      volatilityRatio = volatilityRatio,
      trendPercent = trendPct,
      n = n
    )
  }

  private fun buildCategoryMlForecasts(
    uiState: FinanceUiState,
    cashOutTxs: List<TransactionEntity>,
    unifiedSchoolTxs: List<TransactionEntity>,
    schoolFit: EnsembleSeriesFit,
    nextMonthSchoolProjected: Double,
    modelConfidence: Int,
    now: Long,
    toDisplay: (Double, String, Double) -> Double
  ): List<MlCategoryForecast> {
    val displayCurrency = uiState.displayCurrency
    val step = if (displayCurrency == "IQD") 25_000.0 else 25.0
    val calNow = Calendar.getInstance().apply { timeInMillis = now }
    val curMonth = calNow.get(Calendar.MONTH)
    val curYear = calNow.get(Calendar.YEAR)

    val results = mutableListOf<MlCategoryForecast>()

    // 1. Unified School Forecast Entry (Always combines all school transactions into one unified forecast)
    val currentMonthSchoolSpent = unifiedSchoolTxs.filter { tx ->
      val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
      c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
    }.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }

    val schoolProjected = max(nextMonthSchoolProjected, schoolFit.historicalAvg)
    if (schoolProjected > 0.0 || unifiedSchoolTxs.isNotEmpty()) {
      val recommendedSchoolCap = (ceil((schoolProjected * 0.95) / step) * step).coerceAtLeast(step)
      val existingSchoolBudget = uiState.budgets.find {
        it.category.contains("قوتابخانە") || it.category.contains("School", ignoreCase = true)
      }
      val existingLimit = existingSchoolBudget?.let {
        toDisplay(it.monthlyLimit, it.currency, it.exchangeRateAtEntry)
      }

      results.add(
        MlCategoryForecast(
          category = "قوتابخانە",
          labelKu = "🎓 حسابا گشتی یا قوتابخانێ (ئێکگرتی)",
          labelEn = "🎓 Unified School Account (All Combined)",
          isUnifiedSchool = true,
          currentMonthSpentDisplay = currentMonthSchoolSpent,
          historicalMonthlyAvgDisplay = schoolFit.historicalAvg,
          mlProjectedMonthlyDisplay = schoolProjected,
          recommendedBudgetCapDisplay = recommendedSchoolCap,
          trendSlopePercent = schoolFit.trendPercent,
          confidencePercent = modelConfidence,
          existingBudgetId = existingSchoolBudget?.id,
          existingLimitDisplay = existingLimit
        )
      )
    }

    // 2. Top Cash Spending Categories
    val groupedCash = cashOutTxs.groupBy { it.category.ifBlank { "خەرجیا گشتی" } }
    for ((cat, list) in groupedCash) {
      val series = extractMonthlySeries(
        txs = list,
        fallbackMonthlyBaseline = 0.0,
        now = now,
        toDisplay = toDisplay
      )
      val fit = fitEnsembleSeries(series)
      val projected = fit.predictAt(1)
      if (projected <= 0.0) continue

      val currentMonthCatSpent = list.filter { tx ->
        val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
        c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
      }.sumOf { toDisplay(it.amount, it.currency, it.exchangeRateAtEntry) }

      val recommendedCap = (ceil((projected * 0.92) / step) * step).coerceAtLeast(step)
      val existingBudget = uiState.budgets.find { it.category.equals(cat, ignoreCase = true) }
      val existingLimit = existingBudget?.let {
        toDisplay(it.monthlyLimit, it.currency, it.exchangeRateAtEntry)
      }

      results.add(
        MlCategoryForecast(
          category = cat,
          labelKu = "💸 $cat (کاش)",
          labelEn = "💸 $cat (Cash)",
          isUnifiedSchool = false,
          currentMonthSpentDisplay = currentMonthCatSpent,
          historicalMonthlyAvgDisplay = fit.historicalAvg,
          mlProjectedMonthlyDisplay = projected,
          recommendedBudgetCapDisplay = recommendedCap,
          trendSlopePercent = fit.trendPercent,
          confidencePercent = (modelConfidence - 2).coerceIn(72, 95),
          existingBudgetId = existingBudget?.id,
          existingLimitDisplay = existingLimit
        )
      )
    }

    return results.sortedWith(
      compareByDescending<MlCategoryForecast> { it.isUnifiedSchool }
        .thenByDescending { it.mlProjectedMonthlyDisplay }
    ).take(6)
  }

  private fun schoolAcademicSeasonalityFactor(calendarMonth: Int): Double = when (calendarMonth) {
    Calendar.SEPTEMBER, Calendar.OCTOBER -> 1.12 // Academic year start / supplies / tuition
    Calendar.JANUARY, Calendar.FEBRUARY -> 1.07  // Second semester start
    Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> 0.88 // Summer break lower routine burn
    else -> 1.00
  }

  private fun cashSeasonalityFactor(calendarMonth: Int): Double = when (calendarMonth) {
    Calendar.MARCH, Calendar.APRIL -> 1.06 // Spring / Newroz / seasonal activity
    Calendar.DECEMBER, Calendar.JANUARY -> 1.05
    else -> 1.00
  }

  private fun calculateModelConfidence(sampleCount: Int, avgVolatility: Double): Int {
    val sampleBonus = min(sampleCount * 2, 18)
    val volatilityPenalty = (avgVolatility * 22.0).roundToInt()
    return (78 + sampleBonus - volatilityPenalty).coerceIn(74, 96)
  }
}
