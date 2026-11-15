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
import com.example.data.local.DebtCreditEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Executive Financial Terminal for Debts & Credits Ledger
 * (تەختا پێشکەفتی یا قەرز و مافێن دەف خەلکی)
 */
@Composable
fun ExcelDebtsTableView(
  debts: List<DebtCreditEntity>,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onSelectDebt: (DebtCreditEntity) -> Unit,
  onSettleDebt: ((DebtCreditEntity) -> Unit)? = null,
  onOpenFullscreenWorkbook: (() -> Unit)? = null,
  isMulukOnly: Boolean = false,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var tableSearchQuery by remember { mutableStateOf("") }
  var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "CREDIT", "DEBT", "OVERDUE"
  val horizontalScrollState = rememberScrollState()
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }

  val creditCount = remember(debts) { debts.count { it.type == "CREDIT" } }
  val debtCount = remember(debts) { debts.count { it.type == "DEBT" } }
  val overdueCount = remember(debts) { debts.count { it.isOverdue && !it.isCompleted } }

  val filteredRows by remember(debts, tableSearchQuery, selectedTypeFilter) {
    derivedStateOf {
      debts.filter { debt ->
        val matchesType = when (selectedTypeFilter) {
          "CREDIT" -> debt.type == "CREDIT"
          "DEBT" -> debt.type == "DEBT"
          "OVERDUE" -> debt.isOverdue && !debt.isCompleted
          else -> true
        }
        val matchesSearch = if (tableSearchQuery.isBlank()) true else {
          val q = tableSearchQuery.trim().lowercase()
          val dueStr = if (debt.dueDateMillis > 0L) dateFormat.format(Date(debt.dueDateMillis)).lowercase() else ""
          val typeLabel = if (debt.type == "CREDIT") (if (isKurdish) "دەف خەلکی" else "credit") else (if (isKurdish) "لسەر من" else "debt")
          debt.personName.lowercase().contains(q) ||
            debt.notes.lowercase().contains(q) ||
            debt.currency.lowercase().contains(q) ||
            debt.remainingBalance.toString().contains(q) ||
            dueStr.contains(q) ||
            typeLabel.contains(q)
        }
        matchesType && matchesSearch
      }
    }
  }

  val totalCredits = remember(filteredRows, displayCurrency, currentIqdRate) {
    filteredRows.filter { it.type == "CREDIT" }.sumOf {
      CurrencyService.convert(it.remainingBalance, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  }
  val totalDebts = remember(filteredRows, displayCurrency, currentIqdRate) {
    filteredRows.filter { it.type == "DEBT" }.sumOf {
      CurrencyService.convert(it.remainingBalance, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  }
  val netPosition = totalCredits - totalDebts

  val terminalTitle = if (isMulukOnly) {
    if (isKurdish) "تەختا قەرزێن تایبەت یێن ملوکان (Muluk Credits Terminal)" else "Muluk Isolated Credits Terminal"
  } else {
    if (isKurdish) "تەختا قەرز و مافان (Debts & Receivables Terminal)" else "Executive Debts & Receivables Terminal"
  }

  val terminalSubtitle = if (isKurdish) {
    "مافێن تە: ${CurrencyService.formatShort(totalCredits, displayCurrency)} • قەرزێن تە: ${CurrencyService.formatShort(totalDebts, displayCurrency)}"
  } else {
    "Receivables: ${CurrencyService.formatShort(totalCredits, displayCurrency)} • Payables: ${CurrencyService.formatShort(totalDebts, displayCurrency)}"
  }

  TerminalContainer(
    title = terminalTitle,
    subtitle = terminalSubtitle,
    isLive = true,
    liveLabel = "DEBTS AUDIT",
    searchQuery = tableSearchQuery,
    onSearchChange = { tableSearchQuery = it },
    searchPlaceholder = if (isKurdish) "لێگەڕیان ب ناڤ، تێبینی یان پارەی..." else "Search person, notes, amount...",
    onOpenFullscreen = onOpenFullscreenWorkbook,
    onExportPdf = {
      previewPdfResult = PdfExportService.generateDebtsPdf(
        context = context,
        debts = filteredRows,
        displayCurrency = displayCurrency,
        currentIqdRate = currentIqdRate,
        isKurdish = isKurdish,
        isMulukOnly = isMulukOnly
      )
    },
    pdfButtonTag = "export_debts_pdf_button",
    testTag = "excel_debts_table_container",
    modifier = modifier
  ) {
    // 1. KPI Metric Strip
    TerminalKpiStrip(
      items = listOf(
        TerminalKpiItem(
          label = if (isKurdish) "مەوقەفێ صافی یێ قەرزان" else "Net Position",
          value = "${if (netPosition >= 0) "+" else ""}${CurrencyService.formatShort(netPosition, displayCurrency)}",
          isPositive = netPosition >= 0,
          customColor = if (netPosition >= 0) EmeraldLight else LossRed
        ),
        TerminalKpiItem(
          label = if (isKurdish) "ماف (دەف خەلکی)" else "Receivables",
          value = "+${CurrencyService.formatShort(totalCredits, displayCurrency)}",
          isPositive = true
        ),
        TerminalKpiItem(
          label = if (isKurdish) "قەرزێن لسەر من" else "Payables",
          value = "-${CurrencyService.formatShort(totalDebts, displayCurrency)}",
          isPositive = false
        ),
        TerminalKpiItem(
          label = if (isKurdish) "ژمارەیا تۆماران" else "Records",
          value = "${filteredRows.size}",
          customColor = Color(0xFF38BDF8)
        )
      )
    )

    // 2. Filter Pills
    TerminalFilterPills(
      options = listOf(
        TerminalFilterOption("ALL", if (isKurdish) "هەمی قەرز" else "All Records", debts.size),
        TerminalFilterOption("CREDIT", if (isKurdish) "دەف خەلکی (ماف)" else "Receivables", creditCount, EmeraldLight),
        TerminalFilterOption("DEBT", if (isKurdish) "لسەر من (دەین)" else "Payables", debtCount, LossRed),
        TerminalFilterOption("OVERDUE", if (isKurdish) "بسەرڤەچوو" else "Overdue", overdueCount, Color(0xFFF97316))
      ),
      selectedId = selectedTypeFilter,
      onSelect = { selectedTypeFilter = it }
    )

    // 3. Terminal Ledger Table
    if (filteredRows.isEmpty()) {
      TerminalEmptyState(
        message = if (isKurdish) "چ قەرز ل گۆرەی ڤی فلتەری نەهاتنە دیتن" else "No debt records match your search or filter"
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
            TerminalHeaderCell(if (isKurdish) "کەس / ئالیێ پەیوەندیدار" else "Person / Entity", 185.dp)
            TerminalHeaderCell(if (isKurdish) "جۆرێ قەرزی" else "Direction", 110.dp, TextAlign.Center)
            TerminalHeaderCell(if (isKurdish) "بڕێ ماى" else "Remaining", 130.dp, TextAlign.End)
            TerminalHeaderCell(if (isKurdish) "بهایێ گۆڕڕاو" else "Converted Eqv", 130.dp, TextAlign.End)
            TerminalHeaderCell(if (isKurdish) "دەمێ دیارکری" else "Due Date", 115.dp, TextAlign.Center)
            TerminalHeaderCell(if (isKurdish) "تێبینی و کریار" else "Notes / Action", 160.dp)
          }

          // Table Rows
          filteredRows.forEachIndexed { index, debt ->
            val isCredit = debt.type == "CREDIT"
            val rowBg = if (index % 2 == 0) TerminalDarkBg else TerminalRowAltBg
            val convertedAmount = CurrencyService.convert(
              debt.remainingBalance,
              debt.currency,
              displayCurrency,
              currentIqdRate,
              entryIqdRate = debt.exchangeRateAtEntry
            )
            val amountColor = if (isCredit) EmeraldLight else LossRed
            val dueStr = if (debt.dueDateMillis > 0L) dateFormat.format(Date(debt.dueDateMillis)) else "-"

            Row(
              modifier = Modifier
                .background(rowBg)
                .clickable { onSelectDebt(debt) }
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

              // Person Name & Contact
              Column(
                modifier = Modifier
                  .width(185.dp)
                  .padding(horizontal = 6.dp)
              ) {
                Text(
                  text = debt.personName,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (debt.contactInfo.isNotBlank()) {
                  Text(
                    text = debt.contactInfo,
                    fontSize = 9.5.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              // Type Badge (Credit vs Debt)
              Box(modifier = Modifier.width(110.dp), contentAlignment = Alignment.Center) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isCredit) EmeraldLight.copy(alpha = 0.16f) else LossRed.copy(alpha = 0.16f),
                  border = BorderStroke(0.7.dp, if (isCredit) EmeraldLight.copy(alpha = 0.6f) else LossRed.copy(alpha = 0.6f))
                ) {
                  Text(
                    text = if (isCredit) (if (isKurdish) "دەف خەلکی" else "RECEIVABLE") else (if (isKurdish) "لسەر من" else "PAYABLE"),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCredit) EmeraldLight else LossRed,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                  )
                }
              }

              // Native Remaining Balance
              Text(
                text = "${CurrencyService.formatShort(debt.remainingBalance, debt.currency)}",
                modifier = Modifier
                  .width(130.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
              )

              // Converted Balance
              Text(
                text = "${if (isCredit) "+" else "-"}${CurrencyService.formatShort(convertedAmount, displayCurrency)}",
                modifier = Modifier
                  .width(130.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = amountColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
              )

              // Due Date with Overdue Indicator
              Column(
                modifier = Modifier
                  .width(115.dp)
                  .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = dueStr,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (debt.isOverdue && !debt.isCompleted) Color(0xFFF97316) else Color(0xFFCBD5E1),
                  fontFamily = FontFamily.Monospace
                )
                if (debt.isOverdue && !debt.isCompleted) {
                  Surface(shape = RoundedCornerShape(3.dp), color = Color(0xFFF97316).copy(alpha = 0.2f)) {
                    Text(
                      text = if (isKurdish) "بسەرڤەچوو" else "OVERDUE",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFFF97316),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
              }

              // Notes & Action
              Row(
                modifier = Modifier
                  .width(160.dp)
                  .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = debt.notes.ifBlank { "-" },
                  fontSize = 10.5.sp,
                  color = Color(0xFF94A3B8),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f).padding(end = 4.dp)
                )

                if (onSettleDebt != null && !debt.isCompleted) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldLight.copy(alpha = 0.18f),
                    border = BorderStroke(0.7.dp, EmeraldLight),
                    modifier = Modifier.clickable { onSettleDebt(debt) }
                  ) {
                    Text(
                      text = if (isKurdish) "دان" else "Pay",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldLight,
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                  }
                }
              }
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
              text = if (isKurdish) "سەرجەمێ صافی یێ دەین و قەرزان" else "Net Position Total",
              modifier = Modifier.width(185.dp).padding(horizontal = 6.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black,
              color = GoldAccent
            )
            Box(modifier = Modifier.width(110.dp), contentAlignment = Alignment.Center) {
              Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E2D4A)) {
                Text(text = "NET TOTAL", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TerminalAccentBlue, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
              }
            }
            Text(text = "-", modifier = Modifier.width(130.dp), textAlign = TextAlign.Center, color = Color(0xFF64748B))
            val netColor = if (netPosition >= 0) EmeraldLight else LossRed
            Text(
              text = "${if (netPosition >= 0) "+" else ""}${CurrencyService.formatShort(netPosition, displayCurrency)}",
              modifier = Modifier.width(130.dp).padding(horizontal = 6.dp),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = netColor,
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.End
            )
            Text(text = "${filteredRows.size} Items", modifier = Modifier.width(115.dp), fontSize = 10.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
            Text(text = displayCurrency, modifier = Modifier.width(160.dp).padding(horizontal = 6.dp), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
