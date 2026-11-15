package com.example.util

import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Result model representing a calculated remaining debt balance with real-time currency conversion.
 */
data class DebtBalanceResult(
  val originalTotal: Double,
  val totalPaid: Double,
  val remainingNominal: Double,
  val remainingConverted: Double,
  val debtCurrency: String,
  val displayCurrency: String,
  val repaymentRatio: Float,
  val repaymentPercent: Int,
  val isCompleted: Boolean,
  val isOverdue: Boolean,
  val formattedRemainingOriginal: String,
  val formattedRemainingDisplay: String,
  val formattedOriginalTotal: String
)

/**
 * Helper utility for calculating dynamic remaining balances for debt and credit records
 * and formatting currency outputs with specialized compact Iraqi Dinar (IQD) Kurdish notation.
 */
object LedgerFormattingHelper {

  /**
   * Formats an amount in Iraqi Dinar (IQD) with clean comma separators and optional compact shorthand.
   * Examples:
   * - 1,500,000 -> "1.5M د.ع" (or "1,500,000 د.ع" if shorthand disabled)
   * - 250,000   -> "250,000 د.ع"
   * - 75,000    -> "75,000 د.ع"
   * - 10,000,000 -> "10M د.ع"
   *
   * @param amount The numerical monetary value in IQD.
   * @param isKurdish If true, uses Kurdish Badini suffix "د.ع", otherwise "IQD".
   * @param useMillionShorthand If true, amounts >= 1,000,000 use compact "M" suffix.
   */
  fun formatIqdCompact(
    amount: Double,
    isKurdish: Boolean = true,
    useMillionShorthand: Boolean = true
  ): String {
    val suffix = if (isKurdish) "د.ع" else "IQD"
    val absAmount = abs(amount)
    val sign = if (amount < 0) "−" else ""

    if (useMillionShorthand && absAmount >= 1_000_000.0) {
      val millions = absAmount / 1_000_000.0
      val formattedMillions = if (millions % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", millions)
      } else {
        // Strip trailing zero if e.g. 1.50 -> 1.5
        String.format(Locale.US, "%.1f", millions).trimEnd('0').trimEnd('.')
      }
      return "$sign${formattedMillions}M $suffix"
    }

    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = 0
    }
    return "$sign${nf.format(absAmount)} $suffix"
  }

  /**
   * Automatically formats any currency amount with optimal notation for ledger tiles.
   * Routes IQD to [formatIqdCompact], and USD/EUR to clean localized currency strings.
   */
  fun formatCurrencyAuto(
    amount: Double,
    currency: String,
    isKurdish: Boolean = true,
    useCompactIqd: Boolean = true
  ): String {
    return when (currency.uppercase()) {
      CurrencyService.CURRENCY_IQD, "IQD" -> {
        formatIqdCompact(amount, isKurdish = isKurdish, useMillionShorthand = useCompactIqd)
      }
      CurrencyService.CURRENCY_EUR, "EUR" -> {
        val sign = if (amount < 0) "−" else ""
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          minimumFractionDigits = if (abs(amount) % 1.0 == 0.0) 0 else 2
          maximumFractionDigits = 2
        }
        "${sign}€${nf.format(abs(amount))}"
      }
      else -> {
        val sign = if (amount < 0) "−" else ""
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          minimumFractionDigits = if (abs(amount) % 1.0 == 0.0) 0 else 2
          maximumFractionDigits = 2
        }
        "${sign}$${nf.format(abs(amount))}"
      }
    }
  }

  /**
   * Calculates the real-time dynamic remaining balance for a debt record, including
   * repayment ratio, completion state, overdue status, and currency conversion to the display currency.
   *
   * @param debt The [DebtCreditEntity] to calculate for.
   * @param targetDisplayCurrency The active UI currency ("USD", "IQD", etc.).
   * @param currentIqdRate The real-time IQD exchange rate per 1 USD.
   * @param isKurdish Whether Kurdish Badini localization is enabled.
   */
  fun calculateDebtRemainingBalance(
    debt: DebtCreditEntity,
    targetDisplayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean = true
  ): DebtBalanceResult {
    val total = debt.totalAmount.coerceAtLeast(0.0)
    val paid = debt.amountPaid.coerceAtLeast(0.0)
    val remainingNominal = (total - paid).coerceAtLeast(0.0)
    val isCompleted = paid >= total && total > 0.0
    val isOverdue = !isCompleted && remainingNominal > 0.0 && debt.dueDateMillis > 0L && System.currentTimeMillis() > debt.dueDateMillis

    val ratio = if (total > 0.0) (paid / total).coerceIn(0.0, 1.0).toFloat() else 0f
    val percent = (ratio * 100f).toInt().coerceIn(0, 100)

    val remainingConverted = CurrencyService.convert(
      amount = remainingNominal,
      from = debt.currency,
      to = targetDisplayCurrency,
      iqdRate = currentIqdRate,
      entryIqdRate = debt.exchangeRateAtEntry
    )

    val formattedRemainingOriginal = formatCurrencyAuto(
      amount = remainingNominal,
      currency = debt.currency,
      isKurdish = isKurdish,
      useCompactIqd = remainingNominal >= 1_000_000.0
    )

    val formattedRemainingDisplay = formatCurrencyAuto(
      amount = remainingConverted,
      currency = targetDisplayCurrency,
      isKurdish = isKurdish,
      useCompactIqd = remainingConverted >= 1_000_000.0
    )

    val formattedOriginalTotal = formatCurrencyAuto(
      amount = total,
      currency = debt.currency,
      isKurdish = isKurdish,
      useCompactIqd = total >= 1_000_000.0
    )

    return DebtBalanceResult(
      originalTotal = total,
      totalPaid = paid,
      remainingNominal = remainingNominal,
      remainingConverted = remainingConverted,
      debtCurrency = debt.currency,
      displayCurrency = targetDisplayCurrency,
      repaymentRatio = ratio,
      repaymentPercent = percent,
      isCompleted = isCompleted,
      isOverdue = isOverdue,
      formattedRemainingOriginal = formattedRemainingOriginal,
      formattedRemainingDisplay = formattedRemainingDisplay,
      formattedOriginalTotal = formattedOriginalTotal
    )
  }
}
