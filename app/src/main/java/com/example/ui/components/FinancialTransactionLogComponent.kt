package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FinancialLogCategory {
  ALL,
  CAPITAL_AND_LOANS,
  CAPITAL_ADDITION,
  LOAN_TAKEN,
  LOAN_REPAYMENT,
  EXPENSE_OUTFLOW
}

data class FinancialLogEntry(
  val id: String,
  val sourceEntityType: String, // "TRANSACTION", "DEBT", "DEBT_PAYMENT", "CAPITAL_BASE"
  val sourceEntityId: Long,
  val targetTab: Int,
  val category: FinancialLogCategory,
  val title: String,
  val subtitle: String,
  val badgeLabel: String,
  val amount: Double,
  val currency: String,
  val dateMillis: Long,
  val isPositiveInflow: Boolean,
  val remainingLoanBalance: Double? = null,
  val paymentEntityId: Long? = null,
  val exchangeRateAtEntry: Double = 0.0
)

/**
 * Builds the unified chronological list of financial entries tracking:
 * - Additions to Capital (Capital injections, baseline capital, and income inflows)
 * - New Loans Taken (Borrowed debt records from lenders & credit records)
 * - Loan Installment Repayments
 * - Cashflow Outflows / Expenses
 */
fun buildFinancialTransactionLog(
  uiState: FinanceUiState,
  isKurdish: Boolean
): List<FinancialLogEntry> {
  val entries = mutableListOf<FinancialLogEntry>()

  // 1. Baseline Master Capital entry (if > 0)
  if (uiState.settings.startingCapital > 0.0) {
    entries.add(
      FinancialLogEntry(
        id = "capital_base_1",
        sourceEntityType = "CAPITAL_BASE",
        sourceEntityId = 1L,
        targetTab = 0,
        category = FinancialLogCategory.CAPITAL_ADDITION,
        title = if (isKurdish) "سەرمایێ سەرەکی یێ بنەڕەت" else "Baseline Master Capital",
        subtitle = if (isKurdish) "سەرمایەیا دەستپێکێ یا تۆمارکری د سیستەمی دا" else "Initial starting capital allocation",
        badgeLabel = if (isKurdish) "+ سەرمایە" else "+ CAPITAL",
        amount = uiState.settings.startingCapital,
        currency = uiState.settings.capitalCurrency,
        dateMillis = uiState.settings.lastRateUpdateMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
        isPositiveInflow = true,
        exchangeRateAtEntry = uiState.settings.capitalExchangeRateAtEntry
      )
    )
  }

  // 2. Transactions (Additions to Capital / Inflows & Expense Outflows)
  uiState.transactions.forEach { tx ->
    val isInflow = tx.type == "IN"
    val isExplicitCapital = isInflow && (
      tx.category.contains("Capital", ignoreCase = true) ||
        tx.category.contains("سەرمایە", ignoreCase = true) ||
        tx.notes.contains("Capital", ignoreCase = true) ||
        tx.notes.contains("سەرمایە", ignoreCase = true)
      )

    val badge = when {
      isExplicitCapital -> if (isKurdish) "+ زێدەکرنا سەرمایەی" else "+ CAPITAL ADDITION"
      isInflow -> if (isKurdish) "+ داهات بۆ سەرمایەی" else "+ CAPITAL INFLOW"
      else -> if (isKurdish) "− مەسروف" else "− EXPENSE"
    }

    entries.add(
      FinancialLogEntry(
        id = "tx_${tx.id}",
        sourceEntityType = "TRANSACTION",
        sourceEntityId = tx.id,
        targetTab = 3,
        category = if (isInflow) FinancialLogCategory.CAPITAL_ADDITION else FinancialLogCategory.EXPENSE_OUTFLOW,
        title = tx.category.ifBlank {
          if (isInflow) (if (isKurdish) "زێدەکرنا سەرمایەی" else "Capital Addition")
          else (if (isKurdish) "مەسروف" else "Expense")
        },
        subtitle = tx.notes.ifBlank {
          if (isInflow) (if (isKurdish) "زێدەبوونا پارەی لسەر سەرمایەی" else "Added to liquid capital")
          else (if (isKurdish) "کێمبوون ژ کاشێ بەردەست" else "Deducted from liquid cash")
        },
        badgeLabel = badge,
        amount = tx.amount,
        currency = tx.currency,
        dateMillis = tx.dateMillis,
        isPositiveInflow = isInflow,
        exchangeRateAtEntry = tx.exchangeRateAtEntry
      )
    )
  }

  // 3. New Loans Taken (Debts borrowed from Lenders & Credits given)
  uiState.debtsCredits.forEach { debt ->
    val isDebtTaken = debt.type == "DEBT"
    val title = if (isDebtTaken) {
      if (isKurdish) "قەرزێ وەرگرتی ژ: ${debt.lenderName}" else "New Loan Taken • ${debt.lenderName}"
    } else {
      if (isKurdish) "قەرزێ دای بۆ: ${debt.borrowerName}" else "Credit Loan Given • ${debt.borrowerName}"
    }

    val subParts = mutableListOf<String>()
    if (isDebtTaken) {
      subParts.add(
        if (isKurdish) "قەرزدەر: ${debt.lenderName}" else "Lender: ${debt.lenderName}"
      )
    } else {
      subParts.add(
        if (isKurdish) "قەرزدار: ${debt.borrowerName}" else "Borrower: ${debt.borrowerName}"
      )
    }
    if (debt.notes.isNotBlank()) {
      subParts.add(debt.notes)
    }

    entries.add(
      FinancialLogEntry(
        id = "loan_${debt.id}",
        sourceEntityType = "DEBT",
        sourceEntityId = debt.id,
        targetTab = 1,
        category = FinancialLogCategory.LOAN_TAKEN,
        title = title,
        subtitle = subParts.joinToString(" • "),
        badgeLabel = if (isDebtTaken) {
          if (isKurdish) "قەرزێ نوێ (لسەر من)" else "NEW LOAN TAKEN"
        } else {
          if (isKurdish) "قەرزێ دای (دەف خەلکی)" else "CREDIT LOAN"
        },
        amount = debt.totalAmount,
        currency = debt.currency,
        dateMillis = debt.createdAtMillis,
        isPositiveInflow = isDebtTaken,
        remainingLoanBalance = debt.remainingBalance,
        exchangeRateAtEntry = debt.exchangeRateAtEntry
      )
    )
  }

  // 4. Loan Repayments (Installments paid on loans)
  val debtMap = uiState.debtsCredits.associateBy { it.id }
  uiState.debtPayments.forEach { payment ->
    val parentLoan = debtMap[payment.debtId]
    val personName = parentLoan?.lenderName ?: (if (isKurdish) "قەرز" else "Loan")
    val currency = parentLoan?.currency ?: uiState.displayCurrency
    val isDebtRepayment = parentLoan?.type == "DEBT"

    entries.add(
      FinancialLogEntry(
        id = "loan_pay_${payment.id}",
        sourceEntityType = "DEBT",
        sourceEntityId = payment.debtId,
        targetTab = 1,
        category = FinancialLogCategory.LOAN_REPAYMENT,
        title = if (isKurdish) "دانەوەیا قستی • $personName" else "Loan Repayment • $personName",
        subtitle = payment.notes.ifBlank {
          if (isKurdish) "تۆمارا پارێ واصلبووی" else "Recorded loan installment"
        },
        badgeLabel = if (isKurdish) "قستێ واصلبووی" else "LOAN REPAYMENT",
        amount = payment.amount,
        currency = currency,
        dateMillis = payment.dateMillis,
        isPositiveInflow = !isDebtRepayment,
        remainingLoanBalance = parentLoan?.remainingBalance,
        paymentEntityId = payment.id,
        exchangeRateAtEntry = if (payment.exchangeRateAtEntry > 0.0) payment.exchangeRateAtEntry else (parentLoan?.exchangeRateAtEntry ?: 0.0)
      )
    )
  }

  // 5. Car Installments Received (قستێ سەیاران -> راست دچیتە صافی سەروەت و داهاتی)
  val carMap = uiState.carInvestments.associateBy { it.id }
  uiState.carPayments.forEach { carPay ->
    val car = carMap[carPay.carId]
    val carName = car?.name ?: (if (isKurdish) "سەیارە" else "Car")
    entries.add(
      FinancialLogEntry(
        id = "car_pay_${carPay.id}",
        sourceEntityType = "CAR_PAYMENT",
        sourceEntityId = carPay.carId,
        targetTab = 2,
        category = FinancialLogCategory.CAPITAL_ADDITION,
        title = if (isKurdish) "قستێ هاتی یێ سەیارێ • $carName" else "Car Installment Received • $carName",
        subtitle = carPay.notes.ifBlank {
          if (isKurdish) "راست زێدەبوو د صافی سەروەت و داهاتی دا" else "Directly added to Clean Net Worth & Income"
        },
        badgeLabel = if (isKurdish) "+ قستێ سەیارێ" else "+ CAR INSTALLMENT",
        amount = carPay.effectiveMyShare,
        currency = carPay.currency,
        dateMillis = carPay.paymentDateMillis,
        isPositiveInflow = true,
        paymentEntityId = carPay.id,
        exchangeRateAtEntry = carPay.exchangeRateAtEntry
      )
    )
  }

  // 6. Sold Cars (فرۆتنا سەیاران -> راست دچیتە صافی سەروەت و داهاتی)
  val carsWithPayments = uiState.carPayments.map { it.carId }.toSet()
  uiState.soldCarInvestments.forEach { soldCar ->
    val saleAmount = if (soldCar.soldPrice > 0.0) soldCar.soldPrice else soldCar.currentValue
    val myShareAmount = if (soldCar.isCoOwned) saleAmount * (soldCar.mySharePercent / 100.0) else saleAmount
    if (soldCar.id !in carsWithPayments && myShareAmount > 0.0) {
      entries.add(
        FinancialLogEntry(
          id = "car_sold_${soldCar.id}",
          sourceEntityType = "INVESTMENT",
          sourceEntityId = soldCar.id,
          targetTab = 2,
          category = FinancialLogCategory.CAPITAL_ADDITION,
          title = if (isKurdish) "فرۆتنا سەیارێ • ${soldCar.name}" else "Car Sold • ${soldCar.name}",
          subtitle = if (isKurdish) "راست زێدەبوو د صافی سەروەت و داهاتی دا" else "Directly added to Clean Net Worth & Income",
          badgeLabel = if (isKurdish) "+ فرۆتنا سەیارێ" else "+ CAR SALE",
          amount = myShareAmount,
          currency = soldCar.currency,
          dateMillis = soldCar.purchaseDateMillis,
          isPositiveInflow = true
        )
      )
    }
  }

  // 7. Gold & Silver Cash Wealth Holdings (زێڕ و زیڤ وەکی سەروەتێ کاش)
  uiState.goldHoldings.forEach { metal ->
    val currentValUsd = metal.currentValue(
      uiState.effectiveGoldPricePerGram24K,
      uiState.effectiveSilverPricePerGram9999
    )
    entries.add(
      FinancialLogEntry(
        id = "metal_${metal.id}",
        sourceEntityType = "GOLD",
        sourceEntityId = metal.id,
        targetTab = 2,
        category = FinancialLogCategory.CAPITAL_ADDITION,
        title = if (isKurdish) "سەروەتێ کاش (زێڕ/زیڤ) • ${metal.title}" else "Cash Wealth (Gold/Silver) • ${metal.title}",
        subtitle = if (isKurdish) "${metal.weightGrams} گرام • وەکی کاش د صافی سەروەت دا" else "${metal.weightGrams}g • Treated as Cash in Clean Net Worth",
        badgeLabel = if (isKurdish) "+ زێڕ و زیڤ (کاش)" else "+ GOLD/SILVER CASH",
        amount = currentValUsd,
        currency = "USD",
        dateMillis = metal.purchaseDateMillis,
        isPositiveInflow = true
      )
    )
  }

  return entries.sortedByDescending { it.dateMillis }
}

@Composable
fun FinancialTransactionLogComponent(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  isMasked: Boolean = false,
  onAddCapitalEntry: (amount: Double, currency: String, category: String, notes: String) -> Unit,
  onAddNewLoanTaken: (lenderName: String, contactInfo: String, amount: Double, currency: String, dueDays: Long, notes: String, affectCashBalance: Boolean) -> Unit,
  onOpenEntry: (FinancialLogEntry) -> Unit,
  onDeleteEntry: (FinancialLogEntry) -> Unit = {},
  onClearAllLoanRepayments: () -> Unit = {},
  onAddCapitalEntryWithRate: ((amount: Double, currency: String, category: String, notes: String, customIqdRate: Double) -> Unit)? = null,
  onAddNewLoanTakenWithRate: ((lenderName: String, contactInfo: String, amount: Double, currency: String, dueDays: Long, notes: String, affectCashBalance: Boolean, customIqdRate: Double) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf(FinancialLogCategory.ALL) }
  var searchQuery by remember { mutableStateOf("") }
  var showAddCapitalDialog by remember { mutableStateOf(false) }
  var showNewLoanDialog by remember { mutableStateOf(false) }
  var showFullLogDialog by remember { mutableStateOf(false) }

  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US) }

  val allEntries = remember(
    uiState.transactions,
    uiState.debtsCredits,
    uiState.debtPayments,
    uiState.carPayments,
    uiState.investments,
    uiState.goldHoldings,
    uiState.effectiveGoldPricePerGram24K,
    uiState.effectiveSilverPricePerGram9999,
    uiState.settings.startingCapital,
    uiState.settings.capitalCurrency,
    isKurdish
  ) {
    buildFinancialTransactionLog(uiState, isKurdish)
  }

  val filteredEntries by remember(allEntries, selectedFilter, searchQuery) {
    derivedStateOf {
      val trimmed = searchQuery.trim()
      allEntries.filter { entry ->
        val matchesFilter = when (selectedFilter) {
          FinancialLogCategory.ALL -> true
          FinancialLogCategory.CAPITAL_AND_LOANS ->
            entry.category == FinancialLogCategory.CAPITAL_ADDITION ||
              entry.category == FinancialLogCategory.LOAN_TAKEN
          else -> entry.category == selectedFilter
        }
        val matchesSearch = if (trimmed.isEmpty()) {
          true
        } else {
          entry.title.contains(trimmed, ignoreCase = true) ||
            entry.subtitle.contains(trimmed, ignoreCase = true) ||
            entry.badgeLabel.contains(trimmed, ignoreCase = true)
        }
        matchesFilter && matchesSearch
      }
    }
  }

  val totalCapitalAddedDisplay = remember(allEntries, uiState.displayCurrency, uiState.iqdRate) {
    allEntries
      .filter { it.category == FinancialLogCategory.CAPITAL_ADDITION }
      .sumOf { CurrencyService.convert(it.amount, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  }

  val totalLoansTakenDisplay = remember(uiState.debtsCredits, uiState.displayCurrency, uiState.iqdRate) {
    uiState.debtsCredits
      .filter { it.type == "DEBT" }
      .sumOf { CurrencyService.convert(it.totalAmount, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  }

  fun fmt(amount: Double): String =
    if (isMasked) "••••••" else CurrencyService.format(amount, uiState.displayCurrency)

  fun fmtShort(amount: Double): String =
    if (isMasked) "••••" else CurrencyService.formatShort(amount, uiState.displayCurrency)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
          colors = listOf(
            GoldAccent.copy(alpha = 0.32f),
            EmeraldLight.copy(alpha = 0.24f)
          )
        ),
        shape = RoundedCornerShape(20.dp)
      )
      .testTag("financial_transaction_log_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // 1. Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(EmeraldLight.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "Transaction Log",
              tint = EmeraldLight,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isKurdish) "تۆمارا زێدەکرنا سەرمایەی و قەرزێن نوێ" else "CAPITAL & LOAN TRANSACTION LOG",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isKurdish) {
                "مێژوویا هەمی زێدەکرنێن سەرمایەی، قەرزێن وەرگرتی و تەحویلاتان (${filteredEntries.size})"
              } else {
                "Scrollable history of capital additions, new loans & entries (${filteredEntries.size})"
              },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = { showFullLogDialog = true },
          modifier = Modifier
            .size(34.dp)
            .testTag("expand_transaction_log_button")
        ) {
          Icon(
            imageVector = Icons.Default.OpenInFull,
            contentDescription = if (isKurdish) "ڤەکرنا تۆمارا تەمامی" else "Expand Full Log",
            tint = EmeraldLight,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Summary Totals Banner (Capital Additions vs New Loans Taken)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.TrendingUp,
              contentDescription = null,
              tint = EmeraldLight,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "کۆیێ زێدەکرنا سەرمایەی" else "Total Capital Additions",
              style = MaterialTheme.typography.labelSmall,
              color = EmeraldLight,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "+${fmt(totalCapitalAddedDisplay)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldLight,
            modifier = Modifier.testTag("log_total_capital_additions")
          )
        }

        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.End
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Handshake,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "قەرزێن وەرگرتی (Loans)" else "New Loans Taken",
              style = MaterialTheme.typography.labelSmall,
              color = GoldAccent,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = fmt(totalLoansTakenDisplay),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccent,
            modifier = Modifier.testTag("log_total_loans_taken")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Quick Log Action Buttons: "+ Add to Capital" & "+ Log New Loan Taken"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { showAddCapitalDialog = true },
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldLight,
            contentColor = Color(0xFF0F172A)
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("log_add_capital_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isKurdish) "+ زێدەکرنا سەرمایەی" else "+ Add to Capital",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp
          )
        }

        Button(
          onClick = { showNewLoanDialog = true },
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldAccent,
            contentColor = Color(0xFF0F172A)
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("log_new_loan_taken_button")
        ) {
          Icon(
            imageVector = Icons.Default.Handshake,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isKurdish) "+ قەرزێ نوێ" else "+ New Loan Taken",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Category Filter Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val filters = listOf(
          FinancialLogCategory.ALL to (if (isKurdish) "هەمی (${allEntries.size})" else "All (${allEntries.size})"),
          FinancialLogCategory.CAPITAL_AND_LOANS to (if (isKurdish) "سەرمایە و قەرز" else "Capital & Loans"),
          FinancialLogCategory.CAPITAL_ADDITION to (if (isKurdish) "+ سەرمایە" else "+ Capital Added"),
          FinancialLogCategory.LOAN_TAKEN to (if (isKurdish) "قەرزێن نوێ" else "Loans Taken"),
          FinancialLogCategory.LOAN_REPAYMENT to (if (isKurdish) "قستێن واصل" else "Repayments"),
          FinancialLogCategory.EXPENSE_OUTFLOW to (if (isKurdish) "مەسروف" else "Expenses")
        )

        filters.forEach { (cat, label) ->
          val isSelected = selectedFilter == cat
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(18.dp))
              .background(
                if (isSelected) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
              )
              .clickable { selectedFilter = cat }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("log_filter_${cat.name.lowercase()}")
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
              color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (uiState.debtPayments.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LossRed.copy(alpha = 0.14f))
            .border(0.8.dp, LossRed.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable { onClearAllLoanRepayments() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("clear_all_repayments_button"),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete All Repayments",
              tint = LossRed,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKurdish) {
                "ژێبرنا هەمی دانەوەیێن قستان (${uiState.debtPayments.size})"
              } else {
                "Delete All Loan Repayments (${uiState.debtPayments.size})"
              },
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = LossRed
            )
          }
          Text(
            text = if (isKurdish) "ژێببە ✕" else "Clear All ✕",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LossRed
          )
        }
      }

      // 5. Scrollable History List of Financial Entries
      if (filteredEntries.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isKurdish) {
              "چ تۆمار نەهاتنە دیتن. کلیک ل '+ زێدەکرنا سەرمایەی' یان '+ قەرزێ نوێ' بکە."
            } else {
              "No financial entries logged for this filter yet. Tap '+ Add to Capital' or '+ New Loan Taken'."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val visibleEntries = remember(filteredEntries) { filteredEntries.take(12) }
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("financial_transaction_log_scroll_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          visibleEntries.forEach { entry ->
            FinancialLogEntryRow(
              entry = entry,
              uiState = uiState,
              isKurdish = isKurdish,
              isMasked = isMasked,
              dateFormat = dateFormat,
              onClick = { onOpenEntry(entry) },
              onDelete = { onDeleteEntry(entry) }
            )
          }
          if (filteredEntries.size > 12) {
            OutlinedButton(
              onClick = { showFullLogDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
            ) {
              Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) {
                  "نیشاندانا هەمی تۆماران (${filteredEntries.size})"
                } else {
                  "View All (${filteredEntries.size}) Entries"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }

  // Add Capital Addition Dialog
  if (showAddCapitalDialog) {
    AddCapitalAdditionDialog(
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddCapitalDialog = false },
      onConfirm = { amount, currency, category, notes, customRate ->
        if (onAddCapitalEntryWithRate != null) {
          onAddCapitalEntryWithRate(amount, currency, category, notes, customRate)
        } else {
          onAddCapitalEntry(amount, currency, category, notes)
        }
        showAddCapitalDialog = false
      }
    )
  }

  // Log New Loan Taken Dialog
  if (showNewLoanDialog) {
    LogNewLoanTakenDialog(
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showNewLoanDialog = false },
      onConfirm = { lenderName, contact, amount, currency, dueDays, notes, affectCash, customRate ->
        if (onAddNewLoanTakenWithRate != null) {
          onAddNewLoanTakenWithRate(lenderName, contact, amount, currency, dueDays, notes, affectCash, customRate)
        } else {
          onAddNewLoanTaken(lenderName, contact, amount, currency, dueDays, notes, affectCash)
        }
        showNewLoanDialog = false
      }
    )
  }

  // Full-Screen Scrollable History Dialog
  if (showFullLogDialog) {
    Dialog(
      onDismissRequest = { showFullLogDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp)
          .testTag("full_financial_transaction_log_dialog")
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (isKurdish) "تۆمارا تەمامی یا سەرمایە و قەرزان" else "Full Financial Transaction Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "${filteredEntries.size} ${if (isKurdish) "تۆمارێن دارایی" else "entries recorded"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            IconButton(onClick = { showFullLogDialog = false }) {
              Icon(Icons.Default.Clear, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                if (isKurdish) "لێگەڕیان د تۆمارا سەرمایە و قەرزان دا..."
                else "Filter by lender name, capital source, notes..."
              )
            },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldLight)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
          ) {
            items(filteredEntries, key = { "full_${it.id}" }) { entry ->
              FinancialLogEntryRow(
                entry = entry,
                uiState = uiState,
                isKurdish = isKurdish,
                isMasked = isMasked,
                dateFormat = dateFormat,
                onClick = {
                  showFullLogDialog = false
                  onOpenEntry(entry)
                },
                onDelete = { onDeleteEntry(entry) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun FinancialLogEntryRow(
  entry: FinancialLogEntry,
  uiState: FinanceUiState,
  isKurdish: Boolean,
  isMasked: Boolean,
  dateFormat: SimpleDateFormat,
  onClick: () -> Unit,
  onDelete: (() -> Unit)? = null
) {
  LedgerTile(
    entry = entry,
    displayCurrency = uiState.displayCurrency,
    currentIqdRate = uiState.iqdRate,
    isKurdish = isKurdish,
    onClick = onClick,
    onDetailsClick = onClick,
    onSettleClick = if (entry.category == FinancialLogCategory.LOAN_TAKEN && (entry.remainingLoanBalance ?: 0.0) > 0.0) onClick else null,
    modifier = Modifier.testTag("financial_log_entry_${entry.id}")
  )
}

@Composable
fun AddCapitalAdditionDialog(
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (amount: Double, currency: String, category: String, notes: String, customIqdRate: Double) -> Unit
) {
  var amountInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var categoryInput by remember {
    mutableStateOf(if (isKurdish) "زێدەکرنا سەرمایەی (Capital Injection)" else "Capital Addition")
  }
  var notesInput by remember { mutableStateOf("") }
  var entryIqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isKurdish) "زێدەکرنا پارەی لسەر سەرمایەی" else "Log Addition to Capital",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = amountInput,
          onValueChange = { amountInput = it },
          label = { Text("${if (isKurdish) "بڕێ سەرمایێ زێدەکری" else "Capital Amount"} ($currency)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("capital_addition_amount_input")
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
                color = if (currency == cur) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = amountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = currency,
          isKurdish = isKurdish,
          testTag = "capital_addition_iqd_rate_input"
        )

        OutlinedTextField(
          value = categoryInput,
          onValueChange = { categoryInput = it },
          label = { Text(if (isKurdish) "سەرچاوە / جۆرێ سەرمایەی" else "Capital Source / Label") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی" else "Notes (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
            entryIqdRateInput.toDoubleOrNull() ?: currentIqdRate,
            currentIqdRate
          )
          if (amt > 0.0) {
            onConfirm(amt, currency, categoryInput.ifBlank { "Capital Addition" }, notesInput, lockedRate)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
        modifier = Modifier.testTag("confirm_capital_addition_button")
      ) {
        Text(if (isKurdish) "تۆمارکرن" else "Save Capital Entry", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}

@Composable
fun LogNewLoanTakenDialog(
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (lenderName: String, contactInfo: String, amount: Double, currency: String, dueDays: Long, notes: String, affectCashBalance: Boolean, customIqdRate: Double) -> Unit
) {
  var lenderNameInput by remember { mutableStateOf("") }
  var contactInput by remember { mutableStateOf("") }
  var amountInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var dueDaysInput by remember { mutableStateOf("30") }
  var notesInput by remember { mutableStateOf("") }
  var affectCashBalance by remember { mutableStateOf(true) }
  var entryIqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isKurdish) "تۆمارکرنا قەرزێ نوێ (وەرگرتی)" else "Log New Loan Taken",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = lenderNameInput,
          onValueChange = { lenderNameInput = it },
          label = { Text(if (isKurdish) "ناڤێ قەرزدەری (Lender Name)" else "Lender Name / Institution") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_loan_lender_name_input")
        )

        OutlinedTextField(
          value = amountInput,
          onValueChange = { amountInput = it },
          label = { Text("${if (isKurdish) "بڕێ قەرزێ وەرگرتی" else "Loan Principal Amount"} ($currency)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_loan_amount_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            Button(
              onClick = { currency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (currency == cur) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = cur,
                color = if (currency == cur) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = amountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = currency,
          isKurdish = isKurdish,
          testTag = "new_loan_iqd_rate_input"
        )

        OutlinedTextField(
          value = contactInput,
          onValueChange = { contactInput = it },
          label = { Text(if (isKurdish) "ژمارا تەلەفۆنێ / پەیوەندی" else "Contact Info (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = dueDaysInput,
          onValueChange = { dueDaysInput = it },
          label = { Text(if (isKurdish) "ماوێ دانەوەی (ب ڕۆژ)" else "Due In (Days)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی" else "Loan Notes") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (affectCashBalance) EmeraldLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isKurdish) "💵 ل سەروەتێ کاشی زێدە ببیت؟" else "💵 Add to Cash Balance?",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (affectCashBalance) EmeraldLight else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (affectCashBalance) {
                if (isKurdish) "بەلێ — قەرزێ وەرگرتی ل کاشی زێدە دکەت" else "Yes — Increases your Cash balance"
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
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          val days = dueDaysInput.toLongOrNull() ?: 30L
          val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
            entryIqdRateInput.toDoubleOrNull() ?: currentIqdRate,
            currentIqdRate
          )
          if (lenderNameInput.isNotBlank() && amt > 0.0) {
            onConfirm(lenderNameInput.trim(), contactInput.trim(), amt, currency, days, notesInput.trim(), affectCashBalance, lockedRate)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0F172A)),
        modifier = Modifier.testTag("confirm_new_loan_taken_button")
      ) {
        Text(if (isKurdish) "تۆمارکرنا قەرزی" else "Save Loan Entry", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}
