package com.example.data.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

data class CurrencyRateResult(
  val success: Boolean,
  val iqdPerUsd: Double,
  val eurPerUsd: Double,
  val message: String,
  val timestamp: Long = System.currentTimeMillis()
)

object CurrencyService {
  const val CURRENCY_USD = "USD"
  const val CURRENCY_IQD = "IQD"
  const val CURRENCY_EUR = "EUR"

  // Standard baseline rates
  const val DEFAULT_IQD_PER_USD = 1310.0 // Official Central Bank rate baseline
  const val DEFAULT_EUR_PER_USD = 0.92

  suspend fun fetchRealtimeRates(): CurrencyRateResult = withContext(Dispatchers.IO) {
    try {
      val url = URL("https://open.er-api.com/v6/latest/USD")
      val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 6000
        readTimeout = 6000
        requestMethod = "GET"
        setRequestProperty("Accept", "application/json")
      }

      val responseCode = connection.responseCode
      if (responseCode == 200) {
        val stream = connection.inputStream
        val jsonStr = stream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonStr)
        val rates = root.optJSONObject("rates")
        if (rates != null) {
          val iqd = rates.optDouble("IQD", DEFAULT_IQD_PER_USD)
          val eur = rates.optDouble("EUR", DEFAULT_EUR_PER_USD)
          return@withContext CurrencyRateResult(
            success = true,
            iqdPerUsd = if (iqd > 500) iqd else DEFAULT_IQD_PER_USD,
            eurPerUsd = if (eur > 0) eur else DEFAULT_EUR_PER_USD,
            message = "Rates updated successfully from live market"
          )
        }
      }
      connection.disconnect()
      CurrencyRateResult(
        success = false,
        iqdPerUsd = DEFAULT_IQD_PER_USD,
        eurPerUsd = DEFAULT_EUR_PER_USD,
        message = "Using cached/official rates (offline mode)"
      )
    } catch (e: Exception) {
      CurrencyRateResult(
        success = false,
        iqdPerUsd = DEFAULT_IQD_PER_USD,
        eurPerUsd = DEFAULT_EUR_PER_USD,
        message = "Offline: using cached rates ($DEFAULT_IQD_PER_USD IQD/USD)"
      )
    }
  }

  fun normalizeIqdRatePerUsd(rawInput: Double, fallback: Double = DEFAULT_IQD_PER_USD): Double {
    if (rawInput <= 0.0) return fallback
    return when {
      rawInput >= 50000.0 -> rawInput / 100.0
      rawInput in 100.0..300.0 -> rawInput * 10.0
      else -> rawInput
    }
  }

  fun convert(
    amount: Double,
    from: String,
    to: String,
    iqdRate: Double,
    eurRate: Double = DEFAULT_EUR_PER_USD,
    entryIqdRate: Double = 0.0
  ): Double {
    if (from == to) return amount
    val effectiveFromIqdRate = if (entryIqdRate > 0.0) entryIqdRate else iqdRate
    // First normalize to USD using the locked historical rate when available
    val amountInUsd = when (from) {
      CURRENCY_USD -> amount
      CURRENCY_IQD -> if (effectiveFromIqdRate > 0) amount / effectiveFromIqdRate else amount
      CURRENCY_EUR -> if (eurRate > 0) amount / eurRate else amount
      else -> amount
    }

    // Then convert from USD to target
    return when (to) {
      CURRENCY_USD -> amountInUsd
      CURRENCY_IQD -> amountInUsd * iqdRate
      CURRENCY_EUR -> amountInUsd * eurRate
      else -> amountInUsd
    }
  }

  fun format(amount: Double, currency: String): String {
    return when (currency) {
      CURRENCY_IQD -> {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          maximumFractionDigits = 0
        }
        "${nf.format(amount)} IQD (ع.د)"
      }
      CURRENCY_EUR -> {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          minimumFractionDigits = 2
          maximumFractionDigits = 2
        }
        "€${nf.format(amount)}"
      }
      else -> {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          minimumFractionDigits = 2
          maximumFractionDigits = 2
        }
        "$${nf.format(amount)}"
      }
    }
  }

  fun formatShort(amount: Double, currency: String): String {
    return when (currency) {
      CURRENCY_IQD -> {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          maximumFractionDigits = 0
        }
        "${nf.format(amount)} ع.د"
      }
      CURRENCY_EUR -> {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          maximumFractionDigits = 2
        }
        "€${nf.format(amount)}"
      }
      else -> {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
          maximumFractionDigits = 2
        }
        "$${nf.format(amount)}"
      }
    }
  }
}
