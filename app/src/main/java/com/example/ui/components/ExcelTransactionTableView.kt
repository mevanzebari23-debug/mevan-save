package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.data.local.TransactionEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Executive Financial Terminal for Cash Flow & Transactions Ledger
 * (تەختا پێشکەفتی یا تەحویلات و خەرجیان)
 */
@Composable
fun ExcelTransactionTableView(
  transactions: List<TransactionEntity>,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onSelectTransaction: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var tableSearchQuery by remember { mutableStateOf("") }
  var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "IN", "OUT"
  val horizontalScrollState = rememberScrollState()
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US) }
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }

  val inCount = remember(transactions) { transactions.count { it.type == "IN" } }
  val outCount = remember(transactions) { transactions.count { it.type == "OUT" } }

  val filteredRows by remember(transactions, tableSearchQuery, selectedTypeFilter) {
    derivedStateOf {
      transactions.filter { tx ->
        val matchesType = when (selectedTypeFilter) {
          "IN" -> tx.type == "IN"
          "OUT" -> tx.type == "OUT"
          else -> true
        }
        val matchesSearch = if (tableSearchQuery.isBlank()) true else {
          val q = tableSearchQuery.trim().lowercase()
          val dateStr = dateFormat.format(Date(tx.dateMillis)).lowercase()
          val amountStr = tx.amount.toString()
          val typeLabel = if (tx.type == "IN") (if (isKurdish) "داهات" else "income") else (if (isKurdish) "مەسروف" else "expense")
          dateStr.contains(q) ||
            tx.category.lowercase().contains(q) ||
            tx.notes.lowercase().contains(q) ||
            tx.currency.lowercase().contains(q) ||
            amountStr.contains(q) ||
            typeLabel.contains(q)
        }
        matchesType && matchesSearch
      }
    }
  }

  val totalInflow = remember(filteredRows, displayCurrency, currentIqdRate) {
    filteredRows.filter { it.type == "IN" }.sumOf {
      CurrencyService.convert(it.amount, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  }
  val totalOutflow = remember(filteredRows, displayCurrency, currentIqdRate) {
    filteredRows.filter { it.type == "OUT" }.sumOf {
      CurrencyService.convert(it.amount, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  }
  val netBalance = totalInflow - totalOutflow

  TerminalContainer(
    title = if (isKurdish) "تەختا تەحویلات و خەرجیان (CashFlow Terminal)" else "Executive CashFlow Terminal",
    subtitle = if (isKurdish) {
      "داهات: +${CurrencyService.formatShort(totalInflow, displayCurrency)} • مەسروف: -${CurrencyService.formatShort(totalOutflow, displayCurrency)}"
    } else {
      "Inflow: +${CurrencyService.formatShort(totalInflow, displayCurrency)} • Outflow: -${CurrencyService.formatShort(totalOutflow, displayCurrency)}"
    },
    isLive = true,
    liveLabel = "CASH AUDIT",
    searchQuery = tableSearchQuery,
    onSearchChange = { tableSearchQuery = it },
    searchPlaceholder = if (isKurdish) "لێگەڕیان د تەحویلاتان دا..." else "Search transactions...",
    onExportPdf = {
      previewPdfResult = PdfExportService.generateTransactionsPdf(
        context = context,
        transactions = filteredRows,
        displayCurrency = displayCurrency,
        currentIqdRate = currentIqdRate,
        isKurdish = isKurdish
      )
    },
    pdfButtonTag = "export_pdf_button",
    testTag = "excel_transactions_table_container",
    modifier = modifier
  ) {
    // 1. KPI Metric Strip
    TerminalKpiStrip(
      items = listOf(
        TerminalKpiItem(
          label = if (isKurdish) "مەوقەفێ صافی" else "Net Cash Flow",
          value = "${if (netBalance >= 0) "+" else ""}${CurrencyService.formatShort(netBalance, displayCurrency)}",
          isPositive = netBalance >= 0,
          customColor = if (netBalance >= 0) EmeraldLight else LossRed
        ),
        TerminalKpiItem(
          label = if (isKurdish) "کۆیێ داهاتی (IN)" else "Total Inflow",
          value = "+${CurrencyService.formatShort(totalInflow, displayCurrency)}",
          isPositive = true
        ),
        TerminalKpiItem(
          label = if (isKurdish) "کۆیێ مەسروفی (OUT)" else "Total Outflow",
          value = "-${CurrencyService.formatShort(totalOutflow, displayCurrency)}",
          isPositive = false
        ),
        TerminalKpiItem(
          label = if (isKurdish) "ژمارەیا تەحویلاتان" else "Entries",
          value = "${filteredRows.size}",
          customColor = Color(0xFF38BDF8)
        )
      )
    )

    // 2. Filter Pills
    TerminalFilterPills(
      options = listOf(
        TerminalFilterOption("ALL", if (isKurdish) "هەمی تەحویلات" else "All Transactions", transactions.size),
        TerminalFilterOption("IN", if (isKurdish) "داهات (پارێ هاتی)" else "Inflow", inCount, EmeraldLight),
        TerminalFilterOption("OUT", if (isKurdish) "مەسروف (پارێ چووی)" else "Outflow", outCount, LossRed)
      ),
      selectedId = selectedTypeFilter,
      onSelect = { selectedTypeFilter = it }
    )

    // 3. Terminal Ledger Table
    if (filteredRows.isEmpty()) {
      TerminalEmptyState(
        message = if (isKurdish) "چ تەحویلات نەهاتنە دیتن" else "No transactions match your search"
      )
    } else {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = TerminalCardBg,
        border = BorderStroke(1.dp, TerminalBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
        ) {
          // Table Header
          Row(
            modifier = Modifier
              .background(TerminalHeaderBg)
              .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TerminalHeaderCell("#", 45.dp, TextAlign.Center, Color(0xFF64748B))
            TerminalHeaderCell(if (isKurdish) "بەروار و دەمژمێر" else "Date & Time", 125.dp)
            TerminalHeaderCell(if (isKurdish) "جۆر" else "Flow", 90.dp, TextAlign.Center)
            TerminalHeaderCell(if (isKurdish) "پۆلێن (Category)" else "Category", 140.dp)
            TerminalHeaderCell(if (isKurdish) "بڕێ کۆتایی" else "Converted Amount", 130.dp, TextAlign.End)
            TerminalHeaderCell(if (isKurdish) "بڕێ بنەڕەتی" else "Original Amount", 110.dp, TextAlign.End)
            TerminalHeaderCell(if (isKurdish) "تێبینی" else "Notes", 170.dp)
          }

          // Table Rows
          filteredRows.forEachIndexed { index, tx ->
            val isIn = tx.type == "IN"
            val rowBg = if (index % 2 == 0) TerminalDarkBg else TerminalRowAltBg
            val convertedAmount = CurrencyService.convert(
              tx.amount,
              tx.currency,
              displayCurrency,
              currentIqdRate,
              entryIqdRate = tx.exchangeRateAtEntry
            )
            val amountColor = if (isIn) EmeraldLight else LossRed
            val dateStr = dateFormat.format(Date(tx.dateMillis))

            Row(
              modifier = Modifier
                .background(rowBg)
                .clickable { onSelectTransaction(tx) }
                .padding(vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Row Index
              Text(
                text = "${index + 1}",
                modifier = Modifier.width(45.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
              )

              // Date
              Text(
                text = dateStr,
                modifier = Modifier
                  .width(125.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 10.5.sp,
                color = Color(0xFFCBD5E1),
                fontFamily = FontFamily.Monospace,
                maxLines = 1
              )

              // Flow Badge
              Box(modifier = Modifier.width(90.dp), contentAlignment = Alignment.Center) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isIn) EmeraldLight.copy(alpha = 0.16f) else LossRed.copy(alpha = 0.16f),
                  border = BorderStroke(0.7.dp, if (isIn) EmeraldLight.copy(alpha = 0.6f) else LossRed.copy(alpha = 0.6f))
                ) {
                  Text(
                    text = if (isIn) (if (isKurdish) "داهات" else "INFLOW") else (if (isKurdish) "مەسروف" else "OUTFLOW"),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIn) EmeraldLight else LossRed,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              // Category
              Text(
                text = tx.category,
                modifier = Modifier
                  .width(140.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )

              // Converted Amount
              Text(
                text = "${if (isIn) "+" else "-"}${CurrencyService.formatShort(convertedAmount, displayCurrency)}",
                modifier = Modifier
                  .width(130.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = amountColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
              )

              // Original Amount
              Text(
                text = "${CurrencyService.formatShort(tx.amount, tx.currency)}",
                modifier = Modifier
                  .width(110.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
              )

              // Notes
              Text(
                text = tx.notes.ifBlank { "-" },
                modifier = Modifier
                  .width(170.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 10.5.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Terminal Summary AutoSum Row
          Row(
            modifier = Modifier
              .background(Color(0xFF0F1E38))
              .border(0.8.dp, GoldAccent.copy(alpha = 0.5f))
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "∑",
              modifier = Modifier.width(45.dp),
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = GoldAccent,
              textAlign = TextAlign.Center
            )
            Text(
              text = if (isKurdish) "سەرجەمێ صافی یێ تەحویلاتان" else "Net Cash Flow Total",
              modifier = Modifier.width(215.dp).padding(horizontal = 6.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black,
              color = GoldAccent
            )
            Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.Center) {
              Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E2D4A)) {
                Text(text = "NET VELOCITY", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TerminalAccentBlue, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
              }
            }
            val netColor = if (netBalance >= 0) EmeraldLight else LossRed
            Text(
              text = "${if (netBalance >= 0) "+" else ""}${CurrencyService.formatShort(netBalance, displayCurrency)}",
              modifier = Modifier.width(130.dp).padding(horizontal = 6.dp),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = netColor,
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.End
            )
            Text(text = "${filteredRows.size} Items", modifier = Modifier.width(110.dp), fontSize = 10.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.End)
            Text(text = displayCurrency, modifier = Modifier.width(170.dp).padding(horizontal = 6.dp), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }

  val currentPreview = previewPdfResult
  if (currentPreview != null) {
    PrintPreviewDialog(
      result = currentPreview,
      onDismiss = { previewPdfResult = null }
    )
  }
}
