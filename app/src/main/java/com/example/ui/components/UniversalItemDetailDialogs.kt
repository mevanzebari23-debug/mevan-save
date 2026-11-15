package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.BudgetEntity
import com.example.data.local.CarPaymentEntity
import com.example.data.local.CarServiceEntity
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.history.ReversibleAction
import com.example.ui.theme.CyberObsidianCard
import com.example.ui.theme.CyberObsidianSurface
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
import kotlin.math.abs

/**
 * Detail & Action Dialog for any Cashflow or School Transaction.
 * Actions: Toggle Cash Deduction, Edit (QuickEdit Modal), Delete.
 */
@Composable
fun TransactionDetailActionDialog(
  transaction: TransactionEntity,
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean,
  onDismiss: () -> Unit
) {
  val tx = uiState.transactions.find { it.id == transaction.id } ?: transaction
  val isIncome = tx.type.equals("IN", ignoreCase = true)
  val isSchool = uiState.isSchoolTransaction(tx)
  val accent = when {
    isIncome -> EmeraldLight
    isSchool -> HoloNeonViolet
    else -> LossRed
  }
  val converted = CurrencyService.convert(
    amount = tx.amount,
    from = tx.currency,
    to = uiState.displayCurrency,
    iqdRate = uiState.iqdRate,
    entryIqdRate = tx.exchangeRateAtEntry
  )
  val otherCurrency = if (uiState.displayCurrency == "USD") "IQD" else "USD"
  val secondaryConverted = CurrencyService.convert(
    amount = tx.amount,
    from = tx.currency,
    to = otherCurrency,
    iqdRate = uiState.iqdRate,
    entryIqdRate = tx.exchangeRateAtEntry
  )
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.US) }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor = CyberObsidianSurface,
    modifier = Modifier
      .fillMaxWidth(0.94f)
      .imePadding(),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = accent.copy(alpha = 0.18f),
          border = BorderStroke(1.dp, accent),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isSchool) Icons.Default.School else Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = accent,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = tx.category.ifBlank { if (isIncome) (if (isKurdish) "داهات" else "Income") else (if (isKurdish) "مەسروف" else "Expense") },
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = Color.White
          )
          Text(
            text = dateFormat.format(Date(tx.dateMillis)),
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontFamily = FontFamily.Monospace
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CyberObsidianCard,
          border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "${if (isIncome) "+" else "-"}${CurrencyService.format(converted, uiState.displayCurrency)}",
              fontSize = 22.sp,
              fontWeight = FontWeight.Black,
              color = accent,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "≈ ${CurrencyService.format(secondaryConverted, otherCurrency)} (${if (isKurdish) "ئەسلی" else "Orig"}: ${CurrencyService.format(tx.amount, tx.currency)})",
              fontSize = 11.5.sp,
              color = Color(0xFF94A3B8),
              fontFamily = FontFamily.Monospace
            )
            if (tx.exchangeRateAtEntry > 0.0) {
              Text(
                text = "${if (isKurdish) "نرخێ دۆلاری ل دەمێ تۆمارێ" else "Entry Rate"}: ${"%.0f".format(Locale.US, tx.exchangeRateAtEntry)} IQD",
                fontSize = 10.5.sp,
                color = HoloNeonGold
              )
            }
          }
        }

        if (tx.notes.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "${if (isKurdish) "تێبینی:" else "Notes:"} ${tx.notes}",
              fontSize = 12.sp,
              color = Color(0xFFE2E8F0),
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        if (!isIncome) {
          val badgeColor = if (tx.affectsCapital) LossRed else EmeraldLight
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = badgeColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f)),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable { viewModel.toggleTransactionCashDeduction(tx) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (tx.affectsCapital) Icons.Default.CheckCircle else Icons.Default.DoNotDisturbOn,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = if (tx.affectsCapital) {
                  if (isKurdish) "ژ کاشی هاتیە کێمکرن — کلیک بکە بۆ گوهۆڕینێ" else "Deducted from Cash — Tap to toggle"
                } else {
                  if (isKurdish) "ژ کاشی نەهاتیە کێمکرن — کلیک بکە بۆ گوهۆڕینێ" else "Not Deducted from Cash — Tap to toggle"
                },
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              onDismiss()
              viewModel.openQuickEditForTransaction(tx)
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF041016)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "دەستکاری" else "Edit", fontWeight = FontWeight.Black)
          }
          OutlinedButton(
            onClick = {
              viewModel.deleteTransaction(tx.id)
              onDismiss()
            },
            border = BorderStroke(1.dp, LossRed.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "ژێبرن" else "Delete", color = LossRed, fontWeight = FontWeight.Bold)
          }
        }
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
        ) {
          Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFFCBD5E1))
        }
      }
    }
  )
}

/**
 * Detail & Action Dialog for Real Estate / Commercial / Ventures / Private Gold Investments.
 * Actions: Edit, Inline Quick Value Update, Sell / Reopen Active, Delete.
 */
@Composable
fun AssetDetailActionDialog(
  investment: InvestmentEntity,
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean,
  onDismiss: () -> Unit
) {
  val inv = uiState.investments.find { it.id == investment.id } ?: investment
  val accent = when {
    inv.isSold -> EmeraldLight
    inv.isGoldValuable -> HoloNeonGold
    inv.isRealEstate -> HoloNeonEmerald
    else -> HoloNeonCyan
  }
  val displayVal = CurrencyService.convert(inv.currentValue, inv.currency, uiState.displayCurrency, uiState.iqdRate, inv.exchangeRateAtEntry)
  val displayCost = CurrencyService.convert(inv.initialCapital, inv.currency, uiState.displayCurrency, uiState.iqdRate, inv.exchangeRateAtEntry)
  val otherCurrency = if (uiState.displayCurrency == "USD") "IQD" else "USD"
  val secondaryVal = CurrencyService.convert(inv.currentValue, inv.currency, otherCurrency, uiState.iqdRate, inv.exchangeRateAtEntry)

  var showSellForm by remember { mutableStateOf(false) }
  var sellPriceInput by remember { mutableStateOf(inv.currentValue.toLong().toString()) }
  var sellNotesInput by remember { mutableStateOf("") }
  var addToCash by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor = CyberObsidianSurface,
    modifier = Modifier
      .fillMaxWidth(0.94f)
      .imePadding(),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = accent.copy(alpha = 0.18f),
          border = BorderStroke(1.dp, accent),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (inv.isCar) Icons.Default.DirectionsCar else Icons.Default.Apartment,
              contentDescription = null,
              tint = accent,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(inv.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
          Text(
            text = "${inv.category}${if (inv.isCoOwned && inv.partnerName.isNotBlank()) " • 🤝 ${inv.partnerName}" else ""}",
            fontSize = 11.sp,
            color = accent
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CyberObsidianCard,
          border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = if (inv.isSold) {
                if (isKurdish) "فرۆتی ب:" else "Sold For:"
              } else {
                if (isKurdish) "بهایێ نوکە:" else "Current Valuation:"
              },
              fontSize = 10.5.sp,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = CurrencyService.format(displayVal, uiState.displayCurrency),
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = accent,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "≈ ${CurrencyService.format(secondaryVal, otherCurrency)} • ${if (isKurdish) "تێچوو" else "Cost"}: ${CurrencyService.format(displayCost, uiState.displayCurrency)}",
              fontSize = 11.sp,
              color = Color(0xFFCBD5E1),
              fontFamily = FontFamily.Monospace
            )
            val roi = inv.returnOnInvestmentPercent
            Text(
              text = "ROI: ${if (roi >= 0) "+" else ""}${"%.1f".format(roi)}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (roi >= 0) EmeraldLight else LossRed
            )
          }
        }

        if (inv.isCoOwned) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = HoloNeonBlue.copy(alpha = 0.12f),
            border = BorderStroke(0.8.dp, HoloNeonBlue.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "🤝 ${if (isKurdish) "شەریکاتی:" else "Partnership:"} ${inv.partnerName}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = HoloNeonBlue
              )
              Text(
                text = "${if (isKurdish) "پشکا من" else "My Share"} (${"%.0f".format(inv.mySharePercent)}%): ${CurrencyService.format(inv.myCurrentValuation, inv.currency)}",
                fontSize = 11.sp,
                color = EmeraldLight
              )
            }
          }
        }

        if (inv.notes.isNotBlank()) {
          Text(
            text = "${if (isKurdish) "تێبینی:" else "Notes:"} ${inv.notes}",
            fontSize = 11.5.sp,
            color = Color(0xFF94A3B8)
          )
        }

        if (showSellForm && !inv.isSold) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldAccent.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = if (isKurdish) "بهایێ فرۆتنێ (${inv.currency})" else "Sale Price (${inv.currency})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GoldAccent
              )
              OutlinedTextField(
                value = sellPriceInput,
                onValueChange = { sellPriceInput = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text(if (isKurdish) "مەبلەغ" else "Amount") },
                modifier = Modifier.fillMaxWidth()
              )
              OutlinedTextField(
                value = sellNotesInput,
                onValueChange = { sellNotesInput = it },
                singleLine = true,
                label = { Text(if (isKurdish) "تێبینی" else "Notes") },
                modifier = Modifier.fillMaxWidth()
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { addToCash = !addToCash }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = if (addToCash) Icons.Default.CheckCircle else Icons.Default.DoNotDisturbOn,
                  contentDescription = null,
                  tint = if (addToCash) EmeraldLight else Color(0xFF94A3B8),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = if (isKurdish) "پارە بچیتە سەر کاشی" else "Add sale proceeds to Cash",
                  fontSize = 11.5.sp,
                  color = Color.White
                )
              }
              Button(
                onClick = {
                  val price = sellPriceInput.toDoubleOrNull() ?: inv.currentValue
                  viewModel.sellInvestment(inv.id, price, sellNotesInput, addToCashWealth = addToCash)
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041016)),
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 48.dp)
              ) {
                Text(if (isKurdish) "پشتڕاستکرنا فرۆتنێ" else "Confirm Sale", fontWeight = FontWeight.Black)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              onDismiss()
              viewModel.openQuickEditForInvestment(inv)
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF041016)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "دەستکاری" else "Edit", fontWeight = FontWeight.Black)
          }
          if (!inv.isSold) {
            Button(
              onClick = { showSellForm = !showSellForm },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041016)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isKurdish) "فرۆتن" else "Sell", fontWeight = FontWeight.Black)
            }
          } else {
            Button(
              onClick = {
                viewModel.reopenInvestmentAsActive(inv.id)
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = HoloNeonCyan, contentColor = Color(0xFF041016)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isKurdish) "زڤڕاندن" else "Reopen", fontWeight = FontWeight.Black)
            }
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              viewModel.deleteInvestment(inv.id)
              onDismiss()
            },
            border = BorderStroke(1.dp, LossRed.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 46.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "ژێبرن" else "Delete", color = LossRed, fontWeight = FontWeight.Bold)
          }
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 46.dp)
          ) {
            Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFFCBD5E1))
          }
        }
      }
    }
  )
}

/**
 * Detail & Action Dialog for Gold & Silver Bullion Holdings.
 */
@Composable
fun GoldHoldingDetailActionDialog(
  holding: GoldHoldingEntity,
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean,
  onDismiss: () -> Unit
) {
  val item = uiState.goldHoldings.find { it.id == holding.id } ?: holding
  val accent = if (item.isSilver) Color(0xFF94A3B8) else HoloNeonGold
  val liveUsd = item.currentValue(uiState.effectiveGoldPricePerGram24K, uiState.effectiveSilverPricePerGram9999)
  val liveDisplay = CurrencyService.convert(liveUsd, "USD", uiState.displayCurrency, uiState.iqdRate)
  val otherCurrency = if (uiState.displayCurrency == "USD") "IQD" else "USD"
  val secondaryDisplay = CurrencyService.convert(liveUsd, "USD", otherCurrency, uiState.iqdRate)
  val pnlUsd = item.profitLoss(uiState.effectiveGoldPricePerGram24K, uiState.effectiveSilverPricePerGram9999)

  var showSellForm by remember { mutableStateOf(false) }
  var soldPriceInput by remember { mutableStateOf("%.2f".format(Locale.US, liveUsd)) }
  var sellNotesInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor = CyberObsidianSurface,
    modifier = Modifier
      .fillMaxWidth(0.94f)
      .imePadding(),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = accent.copy(alpha = 0.18f),
          border = BorderStroke(1.dp, accent),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(item.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
          Text(
            text = "${item.getDisplayPurityBadge(isKurdish)} • ${"%.2f".format(item.weightGrams)}g (${"%.2f".format(item.weightMithqal)} ${if (isKurdish) "مسقال" else "Mithqal"})",
            fontSize = 11.sp,
            color = accent
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CyberObsidianCard,
          border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = if (item.isSold) (if (isKurdish) "فرۆتی ب:" else "Sold Price:") else (if (isKurdish) "بهایێ ڕاستەوخۆ:" else "Live Valuation:"),
              fontSize = 10.5.sp,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = CurrencyService.format(liveDisplay, uiState.displayCurrency),
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = accent,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "≈ ${CurrencyService.format(secondaryDisplay, otherCurrency)}",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8),
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "${if (isKurdish) "قازانج/زەرەر" else "P&L"}: ${if (pnlUsd >= 0) "+" else ""}${CurrencyService.format(pnlUsd, "USD")}",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (pnlUsd >= 0) EmeraldLight else LossRed
            )
          }
        }

        if (showSellForm && !item.isSold) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldAccent.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = soldPriceInput,
                onValueChange = { soldPriceInput = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text(if (isKurdish) "بهایێ فرۆتنێ (USD)" else "Sale Price (USD)") },
                modifier = Modifier.fillMaxWidth()
              )
              OutlinedTextField(
                value = sellNotesInput,
                onValueChange = { sellNotesInput = it },
                singleLine = true,
                label = { Text(if (isKurdish) "تێبینی" else "Notes") },
                modifier = Modifier.fillMaxWidth()
              )
              Button(
                onClick = {
                  val price = soldPriceInput.toDoubleOrNull() ?: liveUsd
                  viewModel.sellGoldHolding(item.id, price, sellNotesInput)
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041016)),
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 48.dp)
              ) {
                Text(if (isKurdish) "پشتڕاستکرنا فرۆتنێ" else "Confirm Sale", fontWeight = FontWeight.Black)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              onDismiss()
              viewModel.openQuickEditForGold(item)
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF041016)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "دەستکاری" else "Edit", fontWeight = FontWeight.Black)
          }
          if (!item.isSold) {
            Button(
              onClick = { showSellForm = !showSellForm },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041016)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isKurdish) "فرۆتن" else "Sell", fontWeight = FontWeight.Black)
            }
          } else {
            Button(
              onClick = {
                viewModel.reopenGoldHoldingAsActive(item.id)
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = HoloNeonCyan, contentColor = Color(0xFF041016)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isKurdish) "زڤڕاندن" else "Reopen", fontWeight = FontWeight.Black)
            }
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              viewModel.deleteGoldHolding(item.id)
              onDismiss()
            },
            border = BorderStroke(1.dp, LossRed.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 46.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "ژێبرن" else "Delete", color = LossRed, fontWeight = FontWeight.Bold)
          }
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 46.dp)
          ) {
            Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFFCBD5E1))
          }
        }
      }
    }
  )
}

/**
 * Detail & Action Dialog for Monthly Budgets.
 */
@Composable
fun BudgetDetailActionDialog(
  budget: BudgetEntity,
  spentDisplay: Double,
  limitDisplay: Double,
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean,
  onDismiss: () -> Unit
) {
  val b = uiState.budgets.find { it.id == budget.id } ?: budget
  var limitInput by remember(b.id) { mutableStateOf(b.monthlyLimit.toLong().toString()) }
  var thresholdInput by remember(b.id) { mutableStateOf(b.alertThresholdPercent.toString()) }
  val ratio = if (limitDisplay > 0.0) (spentDisplay / limitDisplay).toFloat().coerceIn(0f, 1f) else 0f
  val accent = when {
    ratio >= 1f -> LossRed
    ratio >= b.alertThresholdPercent / 100f -> GoldAccent
    else -> EmeraldLight
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor = CyberObsidianSurface,
    modifier = Modifier
      .fillMaxWidth(0.92f)
      .imePadding(),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
          shape = CircleShape,
          color = accent.copy(alpha = 0.18f),
          border = BorderStroke(1.dp, accent),
          modifier = Modifier.size(38.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
          }
        }
        Column {
          Text(b.category, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
          Text(
            text = "${CurrencyService.format(spentDisplay, uiState.displayCurrency)} / ${CurrencyService.format(limitDisplay, uiState.displayCurrency)}",
            fontSize = 11.5.sp,
            color = accent,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LinearProgressIndicator(
          progress = { ratio },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = accent,
          trackColor = Color(0xFF1E293B)
        )
        OutlinedTextField(
          value = limitInput,
          onValueChange = { limitInput = it },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          label = { Text(if (isKurdish) "سنوورێ هەیڤانە (${b.currency})" else "Monthly Limit (${b.currency})") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = thresholdInput,
          onValueChange = { thresholdInput = it },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          label = { Text(if (isKurdish) "ئاگەهداری ل ٪" else "Alert Threshold (%)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              val nextLimit = limitInput.toDoubleOrNull()?.takeIf { it > 0.0 } ?: b.monthlyLimit
              val nextThreshold = thresholdInput.toIntOrNull()?.coerceIn(10, 100) ?: b.alertThresholdPercent
              viewModel.updateBudget(b.copy(monthlyLimit = nextLimit, alertThresholdPercent = nextThreshold))
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041016)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "تۆمارکرنا گوهۆڕینێ" else "Save", fontWeight = FontWeight.Black)
          }
          OutlinedButton(
            onClick = {
              viewModel.deleteBudget(b.id)
              onDismiss()
            },
            border = BorderStroke(1.dp, LossRed.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "ژێبرن" else "Delete", color = LossRed, fontWeight = FontWeight.Bold)
          }
        }
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
        ) {
          Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFFCBD5E1))
        }
      }
    }
  )
}

/**
 * Detail & Action Dialog for Car Maintenance Service or Monthly Installment Payment records.
 */
@Composable
fun CarSubItemDetailActionDialog(
  title: String,
  subtitle: String,
  amountFormatted: String,
  secondaryAmountFormatted: String?,
  dateFormatted: String,
  paidByLabel: String,
  notes: String,
  accentColor: Color,
  isKurdish: Boolean,
  onOpenCar: (() -> Unit)? = null,
  onDelete: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor = CyberObsidianSurface,
    modifier = Modifier.fillMaxWidth(0.92f),
    title = {
      Column {
        Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
        Text(subtitle, fontSize = 11.5.sp, color = accentColor)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CyberObsidianCard,
          border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = amountFormatted,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = accentColor,
              fontFamily = FontFamily.Monospace
            )
            if (!secondaryAmountFormatted.isNullOrBlank()) {
              Text(
                text = secondaryAmountFormatted,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
              )
            }
            Text("$dateFormatted • $paidByLabel", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            if (notes.isNotBlank()) {
              Text("${if (isKurdish) "تێبینی:" else "Notes:"} $notes", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
          }
        }
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (onOpenCar != null) {
            Button(
              onClick = {
                onDismiss()
                onOpenCar()
              },
              colors = ButtonDefaults.buttonColors(containerColor = HoloNeonBlue, contentColor = Color(0xFF041016)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isKurdish) "دەف سەیارێ" else "Open Car", fontWeight = FontWeight.Black)
            }
          }
          OutlinedButton(
            onClick = {
              onDelete()
              onDismiss()
            },
            border = BorderStroke(1.dp, LossRed.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "ژێبرن" else "Delete", color = LossRed, fontWeight = FontWeight.Bold)
          }
        }
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
        ) {
          Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFFCBD5E1))
        }
      }
    }
  )
}

/**
 * Detail & Action Dialog for ReversibleAction history log items.
 */
@Composable
fun AuditActionDetailDialog(
  action: ReversibleAction,
  displayCurrency: String,
  viewModel: FinanceViewModel,
  isKurdish: Boolean,
  onDismiss: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US) }
  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor = CyberObsidianSurface,
    modifier = Modifier.fillMaxWidth(0.92f),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
          shape = CircleShape,
          color = HoloNeonViolet.copy(alpha = 0.18f),
          border = BorderStroke(1.dp, HoloNeonViolet),
          modifier = Modifier.size(38.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.History, contentDescription = null, tint = HoloNeonViolet, modifier = Modifier.size(18.dp))
          }
        }
        Column {
          Text(action.title(isKurdish), fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
          Text(
            text = "${if (isKurdish) action.section.labelKu else action.section.labelEn} • ${dateFormat.format(Date(action.timestampMillis))}",
            fontSize = 10.5.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val sub = action.subtitle(isKurdish)
        if (sub.isNotBlank()) {
          Text(sub, fontSize = 13.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
        }
        if (abs(action.cashDeltaDisplay) >= 0.01) {
          val sign = if (action.cashDeltaDisplay > 0) "+" else "-"
          Text(
            text = "${if (isKurdish) "کاریگەریا کاشی" else "Cash Impact"}: $sign${CurrencyService.format(abs(action.cashDeltaDisplay), displayCurrency)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (action.cashDeltaDisplay > 0) EmeraldLight else LossRed
          )
        }
        if (abs(action.netWorthDeltaDisplay) >= 0.01) {
          val sign = if (action.netWorthDeltaDisplay > 0) "+" else "-"
          Text(
            text = "${if (isKurdish) "کاریگەریا سەروەتی" else "Net Worth Impact"}: $sign${CurrencyService.format(abs(action.netWorthDeltaDisplay), displayCurrency)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = HoloNeonCyan
          )
        }
      }
    },
    confirmButton = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (action.canQuickEdit) {
            Button(
              onClick = {
                onDismiss()
                viewModel.openQuickEditFromAction(action)
              },
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF041016)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isKurdish) "دەستکاری" else "Quick Edit", fontWeight = FontWeight.Black)
            }
          }
          Button(
            onClick = {
              viewModel.undoAction(action.id)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041016)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isKurdish) "زڤڕاندن" else "Undo", fontWeight = FontWeight.Black)
          }
        }
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
        ) {
          Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFFCBD5E1))
        }
      }
    }
  )
}
