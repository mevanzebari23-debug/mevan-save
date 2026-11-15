package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent

/**
 * Reusable Dinar & Dollar (IQD / USD) exchange rate input card shown inside every
 * data-entry dialog across the app (Cashflow, Cars, Debts, Gold & Silver, Real Estate,
 * Capital, and Budgets). Locks the chosen rate onto the specific entry so future market
 * rate changes never alter historical records.
 */
@Composable
fun EntryExchangeRateCard(
  rateInput: String,
  onRateChange: (String) -> Unit,
  currentGlobalIqdRate: Double,
  enteredAmount: Double = 0.0,
  selectedCurrency: String = "USD",
  isKurdish: Boolean = false,
  testTag: String = "entry_iqd_rate_input",
  modifier: Modifier = Modifier
) {
  val parsedEntryRate = CurrencyService.normalizeIqdRatePerUsd(
    rateInput.toDoubleOrNull() ?: currentGlobalIqdRate,
    currentGlobalIqdRate
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(GoldAccent.copy(alpha = 0.11f))
      .border(1.dp, GoldAccent.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isKurdish)
          "💱 سعرا دیناری و دۆلاری (١$ = چەند دینار)"
        else
          "💱 Dinar & Dollar Rate (1$ = ? IQD)",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GoldAccent
      )
      Text(
        text = "1$ = ${parsedEntryRate.toInt()} IQD",
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = EmeraldLight
      )
    }

    Text(
      text = if (isKurdish)
        "🔒 ئەڤ سعرە جێگیر دمینیت بۆ ڤی تۆماری خۆ ئەگەر سعرا بازاڕی بگۆهۆڕیت"
      else
        "🔒 Locked for this entry even if the market exchange rate changes later",
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
      value = rateInput,
      onValueChange = onRateChange,
      label = {
        Text(
          if (isKurdish)
            "سعرا دۆلاری و دیناری (بۆ نموونە 1500 یان 150000)"
          else
            "Exchange Rate (e.g. 1500 per $1 or 150000 per $100)"
        )
      },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag(testTag)
    )

    // Quick 1-Tap Exchange Rate Presets
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      listOf("1310", "1450", "1480", "1500", "1520", "1550").forEach { preset ->
        val isSelected = rateInput.trim() == preset
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(7.dp))
            .background(
              if (isSelected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onRateChange(preset) }
            .padding(vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = preset,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    if (enteredAmount > 0.0 && parsedEntryRate > 0.0) {
      if (selectedCurrency.equals("IQD", ignoreCase = true)) {
        val usdEquivalent = enteredAmount / parsedEntryRate
        Text(
          text = if (isKurdish)
            "💵 بەرامبەر دۆلاری ب ڤی سعری: ${CurrencyService.format(usdEquivalent, "USD")} (@ ${parsedEntryRate.toInt()} د.ع/$)"
          else
            "💵 Locked USD Equivalent: ${CurrencyService.format(usdEquivalent, "USD")} (@ ${parsedEntryRate.toInt()} IQD/$)",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = EmeraldLight
        )
      } else {
        val iqdEquivalent = enteredAmount * parsedEntryRate
        Text(
          text = if (isKurdish)
            "💴 بەرامبەر دیناری ب ڤی سعری: ${CurrencyService.format(iqdEquivalent, "IQD")} (@ ${parsedEntryRate.toInt()} د.ع/$)"
          else
            "💴 Locked IQD Equivalent: ${CurrencyService.format(iqdEquivalent, "IQD")} (@ ${parsedEntryRate.toInt()} IQD/$)",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = EmeraldLight
        )
      }
    }
  }
}
