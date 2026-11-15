package com.example

import com.example.data.local.validatePartnershipShares
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun validatePartnershipShares_detectsMissingAmountAndShowsKurdishError() {
    val result = validatePartnershipShares(
      totalPrice = 6.0,
      myShare = 2.0,
      partnerShare = 3.0,
      currencySymbolOrCode = "USD"
    )
    assertFalse(result.isValid)
    assertEquals(5.0, result.combinedShares, 0.001)
    assertEquals(1.0, result.difference, 0.001)
    assertTrue(result.errorMessageKu.contains("خەلەتە، مەبلەغی کامل بکە!"))
    assertTrue(result.errorMessageKu.contains("1 USD یا کێمە"))

    val validResult = validatePartnershipShares(
      totalPrice = 6.0,
      myShare = 2.0,
      partnerShare = 4.0,
      currencySymbolOrCode = "USD"
    )
    assertTrue(validResult.isValid)
    assertEquals(0.0, validResult.difference, 0.001)
  }

  @Test
  fun weatherService_mapsCodesToCorrectConditions() {
    val rainCondition = com.example.data.weather.WeatherService.mapWeatherCodeToCondition(61, rainMm = 2.0)
    assertEquals(com.example.data.weather.WeatherConditionType.RAIN, rainCondition)

    val snowCondition = com.example.data.weather.WeatherService.mapWeatherCodeToCondition(71, snowfallCm = 1.5)
    assertEquals(com.example.data.weather.WeatherConditionType.SNOW, snowCondition)

    val thunderCondition = com.example.data.weather.WeatherService.mapWeatherCodeToCondition(95)
    assertEquals(com.example.data.weather.WeatherConditionType.THUNDERSTORM, thunderCondition)

    val clearCondition = com.example.data.weather.WeatherService.mapWeatherCodeToCondition(0)
    assertEquals(com.example.data.weather.WeatherConditionType.CLEAR, clearCondition)
  }

  @Test
  fun locationHelper_resolvesKurdistanCities() {
    val (duhokEn, duhokKu) = com.example.data.weather.LocationHelper.resolveCityName(36.8665, 42.9880)
    assertEquals("Duhok", duhokEn)
    assertEquals("دهۆک", duhokKu)

    val (erbilEn, erbilKu) = com.example.data.weather.LocationHelper.resolveCityName(36.1911, 44.0091)
    assertEquals("Erbil", erbilEn)
    assertEquals("هەولێر", erbilKu)
  }

  @Test
  fun liveWeatherData_simulationOverrideWorksCorrectly() {
    val weather = com.example.data.weather.LiveWeatherData(
      condition = com.example.data.weather.WeatherConditionType.CLEAR,
      simulatedOverride = com.example.data.weather.WeatherConditionType.RAIN
    )
    assertTrue(weather.isRaining)
    assertFalse(weather.isSnowing)
    assertEquals("🌧️", weather.displayIcon)

    val snowWeather = weather.copy(simulatedOverride = com.example.data.weather.WeatherConditionType.SNOW)
    assertTrue(snowWeather.isSnowing)
    assertFalse(snowWeather.isRaining)
    assertEquals("❄️", snowWeather.displayIcon)
  }

  @Test
  fun pdfExportService_rowDataModelAndFormatting() {
    val row = com.example.util.PdfRowData(
      cells = listOf("1", "Cash Vault", "ASSET", "$5,000", "USD"),
      isPositive = true
    )
    assertEquals(5, row.cells.size)
    assertEquals("Cash Vault", row.cells[1])
    assertTrue(row.isPositive)

    val negativeRow = com.example.util.PdfRowData(
      cells = listOf("2", "Debts Owed", "LIABILITY", "-$1,200", "USD"),
      isPositive = false
    )
    assertFalse(negativeRow.isPositive)
  }

  @Test
  fun ledgerFormattingHelper_formatIqdCompact_formatsMillionsAndThousands() {
    val millionsExact = com.example.util.LedgerFormattingHelper.formatIqdCompact(2_000_000.0, isKurdish = true)
    assertEquals("2M د.ع", millionsExact)

    val millionsDecimal = com.example.util.LedgerFormattingHelper.formatIqdCompact(1_500_000.0, isKurdish = true)
    assertEquals("1.5M د.ع", millionsDecimal)

    val thousands = com.example.util.LedgerFormattingHelper.formatIqdCompact(250_000.0, isKurdish = true)
    assertEquals("250,000 د.ع", thousands)

    val englishSuffix = com.example.util.LedgerFormattingHelper.formatIqdCompact(1_500_000.0, isKurdish = false)
    assertEquals("1.5M IQD", englishSuffix)

    val autoFormatted = com.example.util.LedgerFormattingHelper.formatCurrencyAuto(75_000.0, "IQD", isKurdish = true)
    assertEquals("75,000 د.ع", autoFormatted)
  }

  @Test
  fun ledgerFormattingHelper_calculateDebtRemainingBalance_computesDynamicBalanceAndConversions() {
    val debt = com.example.data.local.DebtCreditEntity(
      id = 1L,
      type = "DEBT",
      personName = "Ali",
      totalAmount = 5000.0,
      amountPaid = 2000.0,
      currency = "USD",
      dueDateMillis = System.currentTimeMillis() + 100000L
    )

    val balanceResult = com.example.util.LedgerFormattingHelper.calculateDebtRemainingBalance(
      debt = debt,
      targetDisplayCurrency = "IQD",
      currentIqdRate = 1500.0,
      isKurdish = true
    )

    assertEquals(5000.0, balanceResult.originalTotal, 0.001)
    assertEquals(2000.0, balanceResult.totalPaid, 0.001)
    assertEquals(3000.0, balanceResult.remainingNominal, 0.001)
    assertEquals(4_500_000.0, balanceResult.remainingConverted, 0.001)
    assertEquals(0.4f, balanceResult.repaymentRatio, 0.01f)
    assertEquals(40, balanceResult.repaymentPercent)
    assertFalse(balanceResult.isCompleted)
    assertFalse(balanceResult.isOverdue)
    assertTrue(balanceResult.formattedRemainingDisplay.contains("4.5M د.ع"))
  }
}
