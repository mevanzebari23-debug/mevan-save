package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService
import com.example.util.PdfRowData
import com.example.util.SummaryCardData

data class GenericExcelCellRow(
  val cellRef: String,
  val colA_Ref: String,
  val colB_Name: String,
  val colC_Category: String,
  val colD_Formula: String,
  val colE_Amount: Double,
  val colF_Status: String,
  val isPositiveHighlight: Boolean = true
)

enum class ExcelSortOption {
  NONE,
  REF_ASC,
  NAME_ASC,
  AMOUNT_DESC,
  AMOUNT_ASC
}

/**
 * Executive Portfolio Command Terminal (خشتەیێ گشتی یێ تێرمیناڵا دارایی)
 * Fullscreen Interactive Multi-Sheet Ledger with dark obsidian glass styling,
 * live formula evaluator, search & sort filters, and professional PDF generation.
 */
@Composable
fun ExecutiveExcelWorkbookDialog(
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean = true,
  initialSheetIndex: Int = 0,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var activeSheetIndex by remember { mutableStateOf(initialSheetIndex.coerceIn(0, 3)) }
  var searchQuery by remember { mutableStateOf("") }
  var sortOption by remember { mutableStateOf(ExcelSortOption.NONE) }
  var selectedRowIndex by remember { mutableStateOf(0) }

  val displayCurrency = uiState.displayCurrency
  val iqdRate = uiState.iqdRate

  // Sheet 0: Balance Sheet & Net Worth Data
  val sheet0Rows = remember(uiState) {
    val cashVault = uiState.pureCashBalance
    val carsNet = uiState.myActiveCarValuationShare
    val estateVal = uiState.myRealEstateValuation
    val mulukGoldVal = uiState.goldValuablesInvestmentsValuation
    val vaultGoldVal = uiState.totalGoldCurrentValueDisplay
    val loansGiven = uiState.totalCreditReceivable
    val privateCredits = uiState.totalPrivateAccountCreditsReceivable

    val debtsOwed = uiState.totalDebtOwed
    val myCarInstallmentsDebt = uiState.totalCarsMyRemainingMoney
    val estateDebts = uiState.totalIndependentRealEstateDebt

    listOf(
      GenericExcelCellRow("A1", "A1", if (isKurdish) "کاشێ بەردەست (سیولە)" else "Cash & Liquid Vault", "ASSET", "=VAULT(USD_IQD)", cashVault, "ACTIVE", true),
      GenericExcelCellRow("A2", "A2", if (isKurdish) "سەرمایێ سەیاران (تنێ پشکا من)" else "Active Cars (My Share Only)", "ASSET", "=SUM(CARS:ActiveMyShare)", carsNet, "ACTIVE", true),
      GenericExcelCellRow("A3", "A3", if (isKurdish) "عەقارات و ملوک" else "Real Estate & Properties", "ASSET", "=SUM(ESTATE:Valuation)", estateVal, "ACTIVE", true),
      GenericExcelCellRow("A4", "A4", if (isKurdish) "زێڕێ ملوک و وەبەرهێنانێ" else "Muluk Gold Valuables", "ASSET", "=SUM(ESTATE:Gold)", mulukGoldVal, "ACTIVE", true),
      GenericExcelCellRow("A5", "A5", if (isKurdish) "خەزینەیا زێڕ و زیڤی (Spot)" else "Spot Gold & Silver Vault", "ASSET", "=MARKET(GOLD_SPOT)", vaultGoldVal, "ACTIVE", true),
      GenericExcelCellRow("A6", "A6", if (isKurdish) "قەرزێن گشتی دەف خەلکی" else "Receivables (Lent Out)", "ASSET", "=SUM(DEBTS:Credits)", loansGiven, "ACTIVE", true),
      GenericExcelCellRow("A7", "A7", if (isKurdish) "قەرزێ تایبەتێ ملوکان دەف خەلکی" else "Muluk Private Credits", "ASSET", "=SUM(ESTATE:Credits)", privateCredits, "ACTIVE", true),
      GenericExcelCellRow("B1", "B1", if (isKurdish) "قەرزێن گشتی یێن لسەر من" else "Payables (Debts Owed)", "LIABILITY", "=SUM(DEBTS:Payables)", debtsOwed, "LIABILITY", false),
      GenericExcelCellRow("B2", "B2", if (isKurdish) "باقیێ قستێن سەیاران (تنێ پشکا من)" else "Car Installments Debt (My Share)", "LIABILITY", "=SUM(CARS:Installments)", myCarInstallmentsDebt, "LIABILITY", false),
      GenericExcelCellRow("B3", "B3", if (isKurdish) "قەرزێ ماى لسەر ملوکان" else "Muluk Outstanding Debt", "LIABILITY", "=SUM(ESTATE:Debt)", estateDebts, "LIABILITY", false)
    )
  }

  // Sheet 1: Debts & Credits Data
  val sheet1Rows = remember(uiState) {
    uiState.debtsCredits.mapIndexed { idx, d ->
      val isCredit = d.isCreditReceivable
      GenericExcelCellRow(
        cellRef = if (isCredit) "C${idx + 1}" else "D${idx + 1}",
        colA_Ref = if (isCredit) "CR-${idx + 1}" else "DB-${idx + 1}",
        colB_Name = d.personName,
        colC_Category = if (isCredit) (if (isKurdish) "دەف خەلکی" else "Receivable") else (if (isKurdish) "لسەر من" else "Payable"),
        colD_Formula = if (d.totalAmount > 0) "=REMAINING(${d.totalAmount} - ${d.amountPaid})" else "=0.0",
        colE_Amount = d.remainingAmount,
        colF_Status = if (d.isCompleted) (if (isKurdish) "تمامکری" else "SETTLED") else (if (d.isOverdue) (if (isKurdish) "دیرکەفتی" else "OVERDUE") else (if (isKurdish) "چالاک" else "ACTIVE")),
        isPositiveHighlight = isCredit
      )
    }
  }

  // Sheet 2: Vehicles & Fleet Data
  val sheet2Rows = remember(uiState) {
    uiState.carInvestments.mapIndexed { idx, car ->
      GenericExcelCellRow(
        cellRef = "V${idx + 1}",
        colA_Ref = "CAR-${idx + 1}",
        colB_Name = car.name,
        colC_Category = if (car.isCoOwned) (if (isKurdish) "شەریکاتی (${car.partnerName})" else "Co-Owned (${car.partnerName})") else (if (isKurdish) "ب تنێ" else "Solo"),
        colD_Formula = "=VALUATION(${car.currentValue})",
        colE_Amount = car.myInvestmentAmount.takeIf { it > 0 } ?: car.currentValue,
        colF_Status = if (car.isSold) (if (isKurdish) "فرۆتی" else "SOLD") else (if (isKurdish) "بەردەست" else "ACTIVE"),
        isPositiveHighlight = true
      )
    }
  }

  // Sheet 3: Assets & Gold Inventory Data
  val sheet3Rows = remember(uiState) {
    val list = mutableListOf<GenericExcelCellRow>()
    uiState.nonCarInvestments.forEachIndexed { idx, inv ->
      list.add(
        GenericExcelCellRow(
          cellRef = "R${idx + 1}",
          colA_Ref = "PROP-${idx + 1}",
          colB_Name = inv.name,
          colC_Category = inv.category,
          colD_Formula = "=INVEST(${inv.currentValue})",
          colE_Amount = inv.currentValue,
          colF_Status = if (inv.isSold) (if (isKurdish) "فرۆتی" else "SOLD") else (if (isKurdish) "چالاک" else "ACTIVE"),
          isPositiveHighlight = true
        )
      )
    }
    uiState.activeGoldHoldings.forEachIndexed { idx, gold ->
      list.add(
        GenericExcelCellRow(
          cellRef = "G${idx + 1}",
          colA_Ref = "GOLD-${idx + 1}",
          colB_Name = "${gold.title} (${"%.1f".format(gold.weightGrams)}g)",
          colC_Category = "Gold K${gold.karat}",
          colD_Formula = "=PRICE(${gold.weightGrams}g * ${gold.buyPricePerGram})",
          colE_Amount = gold.currentValue(80.0),
          colF_Status = if (isKurdish) "خەزینە" else "VAULT",
          isPositiveHighlight = true
        )
      )
    }
    list
  }

  val activeRows = when (activeSheetIndex) {
    0 -> sheet0Rows
    1 -> sheet1Rows
    2 -> sheet2Rows
    else -> sheet3Rows
  }

  val filteredSortedRows by remember(activeRows, searchQuery, sortOption) {
    derivedStateOf {
      var result = if (searchQuery.isBlank()) activeRows else {
        val q = searchQuery.trim().lowercase()
        activeRows.filter {
          it.colA_Ref.lowercase().contains(q) ||
            it.colB_Name.lowercase().contains(q) ||
            it.colC_Category.lowercase().contains(q) ||
            it.colD_Formula.lowercase().contains(q) ||
            it.colF_Status.lowercase().contains(q)
        }
      }

      result = when (sortOption) {
        ExcelSortOption.NONE -> result
        ExcelSortOption.REF_ASC -> result.sortedBy { it.colA_Ref }
        ExcelSortOption.NAME_ASC -> result.sortedBy { it.colB_Name.lowercase() }
        ExcelSortOption.AMOUNT_DESC -> result.sortedByDescending { it.colE_Amount }
        ExcelSortOption.AMOUNT_ASC -> result.sortedBy { it.colE_Amount }
      }
      result
    }
  }

  val selectedCell = remember(filteredSortedRows, selectedRowIndex) {
    filteredSortedRows.getOrNull(selectedRowIndex) ?: filteredSortedRows.firstOrNull()
  }

  val totalSheetAmount = remember(filteredSortedRows) {
    filteredSortedRows.sumOf { it.colE_Amount }
  }
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("excel_fullscreen_workbook"),
      color = TerminalDarkBg
    ) {
      Column(modifier = Modifier.fillMaxSize()) {

        // 1. TOP TERMINAL COMMAND HEADER (Obsidian & Electric Blue Glow)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                listOf(Color(0xFF071224), Color(0xFF0F1E38), Color(0xFF071224))
              )
            )
            .border(width = 1.dp, color = TerminalBorder)
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left: Terminal Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(9.dp),
                color = TerminalAccentBlue.copy(alpha = 0.15f),
                border = BorderStroke(1.2.dp, TerminalAccentBlue.copy(alpha = 0.7f))
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "⚡",
                    fontSize = 17.sp
                  )
                }
              }
              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = if (isKurdish) "تێرمیناڵا سەرەکی یا پۆرتفۆلیۆیێ" else "Executive Portfolio Terminal",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.5.sp,
                    color = Color.White
                  )
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldLight.copy(alpha = 0.15f),
                    border = BorderStroke(0.6.dp, EmeraldLight.copy(alpha = 0.6f))
                  ) {
                    Text(
                      text = "LIVE",
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Black,
                      color = EmeraldLight,
                      fontFamily = FontFamily.Monospace,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = if (isKurdish) "مۆدێلا دارایی یا ڕاستەوخۆ • هەڤدەمکرنا ئۆفلاین و ئۆنلاین" else "Real-Time Ledger Engine • Offline & Auto-Sync",
                  fontSize = 10.5.sp,
                  color = GoldAccent
                )
              }
            }

            // Right Actions: Export PDF + Currency Switch + Close
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // PDF Statement Export Button
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldLight.copy(alpha = 0.18f),
                border = BorderStroke(0.9.dp, EmeraldLight),
                modifier = Modifier
                  .clickable {
                    val sheetName = when (activeSheetIndex) {
                      0 -> if (isKurdish) "تەختا ميزانیەیێ (Balance Sheet)" else "Balance Sheet Statement"
                      1 -> if (isKurdish) "قەرز و دەین (Debts & Credits)" else "Debts & Receivables Statement"
                      2 -> if (isKurdish) "ترۆمبێل (Vehicles Fleet)" else "Vehicles Fleet Statement"
                      else -> if (isKurdish) "عەقارات و زێڕ (Assets & Muluk)" else "Muluk & Assets Statement"
                    }
                    val pdfRows = filteredSortedRows.map { r ->
                      PdfRowData(
                        cells = listOf(
                          r.colA_Ref,
                          r.colB_Name,
                          r.colC_Category,
                          r.colD_Formula,
                          "${if (r.isPositiveHighlight) "+" else "-"}${CurrencyService.formatShort(r.colE_Amount, displayCurrency)}",
                          r.colF_Status
                        ),
                        isPositive = r.isPositiveHighlight
                      )
                    }
                    val sumCards = listOf(
                      SummaryCardData(
                        label = if (isKurdish) "کۆیێ گشتیێ شیتێ" else "Total Sum",
                        value = "${if (totalSheetAmount >= 0) "+" else ""}${CurrencyService.formatShort(totalSheetAmount, displayCurrency)}",
                        color = if (totalSheetAmount >= 0) android.graphics.Color.rgb(4, 120, 87) else android.graphics.Color.rgb(185, 28, 28)
                      )
                    )
                    previewPdfResult = PdfExportService.generateGenericWorkbookPdf(
                      context = context,
                      sheetTitle = sheetName,
                      headers = listOf("Ref", "Account / Item", "Category", "Formula", "Amount", "Status"),
                      columnWidths = floatArrayOf(45f, 160f, 80f, 95f, 85f, 58f),
                      rows = pdfRows,
                      summaryCards = sumCards,
                      displayCurrency = displayCurrency,
                      isKurdish = isKurdish
                    )
                  }
                  .testTag("excel_workbook_export_pdf_btn")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = EmeraldLight, modifier = Modifier.size(15.dp))
                  Text(
                    text = "PDF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldLight
                  )
                }
              }

              // Currency Toggle Pill
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldAccent.copy(alpha = 0.16f),
                border = BorderStroke(0.9.dp, GoldAccent),
                modifier = Modifier.clickable { viewModel.toggleDisplayCurrency() }
              ) {
                Text(
                  text = if (displayCurrency == "IQD") "د.ع" else "USD",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoldAccent,
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                )
              }

              // Close Fullscreen Button
              IconButton(
                onClick = onDismiss,
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.10f))
                  .testTag("excel_close_button")
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
              }
            }
          }
        }

        // 2. FORMULA BAR (`fx` Formula Display & Row Reference)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF091426))
            .border(width = 0.8.dp, color = TerminalBorder)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("excel_formula_bar")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Active Cell Box
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF0F1E38),
              border = BorderStroke(0.8.dp, TerminalAccentBlue.copy(alpha = 0.6f))
            ) {
              Text(
                text = selectedCell?.cellRef ?: "A1",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = TerminalAccentBlue,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            // Function Symbol `fx`
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = TerminalAccentBlue.copy(alpha = 0.15f),
              modifier = Modifier.size(24.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Functions, contentDescription = "fx", tint = TerminalAccentBlue, modifier = Modifier.size(14.dp))
              }
            }

            // Formula Text
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0B172C))
                .border(0.6.dp, TerminalBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = selectedCell?.let { "${it.colD_Formula}  [${it.colB_Name}]" } ?: "=SUM(TOTAL)",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Amount Preview Pill
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldLight.copy(alpha = 0.15f)
            ) {
              Text(
                text = selectedCell?.let { CurrencyService.formatShort(it.colE_Amount, displayCurrency) } ?: "0.00",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp,
                color = EmeraldLight,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        // 3. SEARCH & COLUMN SORTING FILTER RIBBON
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(TerminalDarkBg)
            .padding(horizontal = 12.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Search Field
          Box(
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(TerminalCardBg)
              .border(0.7.dp, TerminalBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 6.dp)
              .testTag("excel_search_filter_input"),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Search, contentDescription = null, tint = TerminalAccentBlue, modifier = Modifier.size(15.dp))
              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = Color.White, fontSize = 11.5.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                  if (searchQuery.isEmpty()) {
                    Text(
                      text = if (isKurdish) "فلتەرکرن ل تەختێ تێرمیناڵێ..." else "Filter active terminal ledger...",
                      color = Color(0xFF64748B),
                      fontSize = 11.5.sp
                    )
                  }
                  innerTextField()
                }
              )
            }
          }

          // Sort Mode Toggle Chip
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (sortOption != ExcelSortOption.NONE) GoldAccent.copy(alpha = 0.22f) else TerminalCardBg,
            border = BorderStroke(
              0.8.dp,
              if (sortOption != ExcelSortOption.NONE) GoldAccent else TerminalBorder
            ),
            modifier = Modifier.clickable {
              sortOption = when (sortOption) {
                ExcelSortOption.NONE -> ExcelSortOption.AMOUNT_DESC
                ExcelSortOption.AMOUNT_DESC -> ExcelSortOption.AMOUNT_ASC
                ExcelSortOption.AMOUNT_ASC -> ExcelSortOption.NAME_ASC
                ExcelSortOption.NAME_ASC -> ExcelSortOption.REF_ASC
                ExcelSortOption.REF_ASC -> ExcelSortOption.NONE
              }
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Sort",
                tint = if (sortOption != ExcelSortOption.NONE) GoldAccent else Color.White,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = when (sortOption) {
                  ExcelSortOption.NONE -> if (isKurdish) "ڕیزبەندی" else "Sort"
                  ExcelSortOption.AMOUNT_DESC -> if (isKurdish) "بهایێ زێدە" else "Amount ↓"
                  ExcelSortOption.AMOUNT_ASC -> if (isKurdish) "بهایێ کێم" else "Amount ↑"
                  ExcelSortOption.NAME_ASC -> if (isKurdish) "ناڤ (A-Z)" else "Name (A-Z)"
                  ExcelSortOption.REF_ASC -> if (isKurdish) "کۆد" else "Ref (A-Z)"
                },
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (sortOption != ExcelSortOption.NONE) GoldAccent else Color.White
              )
            }
          }
        }

        // 4. MAIN TERMINAL GRID (Horizontal Panning Grid with Sticky Header)
        val horizontalTableScroll = rememberScrollState()

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(TerminalDarkBg)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .horizontalScroll(horizontalTableScroll)
          ) {
            // Sticky Grid Column Header (A, B, C, D, E, F)
            Row(
              modifier = Modifier
                .background(TerminalHeaderBg)
                .border(width = 0.8.dp, color = TerminalBorder)
                .padding(vertical = 8.dp)
            ) {
              // # / Row index column
              Box(modifier = Modifier.width(42.dp), contentAlignment = Alignment.Center) {
                Text(text = "#", fontWeight = FontWeight.Black, fontSize = 11.sp, color = GoldAccent, fontFamily = FontFamily.Monospace)
              }
              // Col A: Cell Ref
              Box(modifier = Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                Text(text = "A • REF", fontWeight = FontWeight.Black, fontSize = 11.sp, color = TerminalAccentBlue, fontFamily = FontFamily.Monospace)
              }
              // Col B: Account Name
              Box(modifier = Modifier.width(220.dp), contentAlignment = Alignment.CenterStart) {
                Text(text = if (isKurdish) "B • ناڤێ حیسابێ" else "B • ACCOUNT NAME", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
              }
              // Col C: Category
              Box(modifier = Modifier.width(130.dp), contentAlignment = Alignment.CenterStart) {
                Text(text = if (isKurdish) "C • پۆل" else "C • CATEGORY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
              }
              // Col D: Formula
              Box(modifier = Modifier.width(180.dp), contentAlignment = Alignment.CenterStart) {
                Text(text = if (isKurdish) "D • هاوکێشە (fx)" else "D • FORMULA", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
              }
              // Col E: Amount
              Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                Text(text = if (isKurdish) "E • بڕ ($displayCurrency)" else "E • AMOUNT", fontWeight = FontWeight.Black, fontSize = 11.sp, color = GoldAccent)
              }
              // Col F: Status
              Box(modifier = Modifier.width(100.dp).padding(end = 8.dp), contentAlignment = Alignment.Center) {
                Text(text = if (isKurdish) "F • دۆخ" else "F • STATUS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
              }
            }

            // Lazy Spreadsheet Rows
            LazyColumn(modifier = Modifier.weight(1f)) {
              itemsIndexed(filteredSortedRows) { index, rowItem ->
                val isSelected = selectedRowIndex == index
                val isEven = index % 2 == 0
                val rowBg = when {
                  isSelected -> Color(0xFF13223D)
                  isEven -> TerminalCardBg
                  else -> TerminalRowAltBg
                }

                Row(
                  modifier = Modifier
                    .background(rowBg)
                    .border(width = 0.5.dp, color = if (isSelected) TerminalAccentBlue else TerminalBorder.copy(alpha = 0.5f))
                    .clickable { selectedRowIndex = index }
                    .padding(vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Index column
                  Box(modifier = Modifier.width(42.dp), contentAlignment = Alignment.Center) {
                    Text(
                      text = "${index + 1}",
                      fontSize = 10.5.sp,
                      color = Color(0xFF64748B),
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  // Col A: Cell Ref
                  Box(modifier = Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                    Text(
                      text = rowItem.colA_Ref,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = TerminalAccentBlue,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  // Col B: Account Name
                  Box(modifier = Modifier.width(220.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                      text = rowItem.colB_Name,
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  // Col C: Category
                  Box(modifier = Modifier.width(130.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                      text = rowItem.colC_Category,
                      fontSize = 10.5.sp,
                      color = Color(0xFF94A3B8),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  // Col D: Formula
                  Box(modifier = Modifier.width(180.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                      text = rowItem.colD_Formula,
                      fontSize = 10.sp,
                      color = Color(0xFF64B5F6),
                      fontFamily = FontFamily.Monospace,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  // Col E: Amount
                  Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                    Text(
                      text = CurrencyService.formatShort(rowItem.colE_Amount, displayCurrency),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Black,
                      color = if (rowItem.isPositiveHighlight) EmeraldLight else LossRed,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  // Col F: Status
                  Box(modifier = Modifier.width(100.dp).padding(end = 8.dp), contentAlignment = Alignment.Center) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (rowItem.colF_Status.contains("ACT", true) || rowItem.colF_Status.contains("چالاک", true))
                        EmeraldLight.copy(alpha = 0.16f)
                      else if (rowItem.colF_Status.contains("SET", true) || rowItem.colF_Status.contains("تمام", true))
                        Color(0xFF38BDF8).copy(alpha = 0.16f)
                      else
                        LossRed.copy(alpha = 0.16f),
                      border = BorderStroke(
                        0.7.dp,
                        if (rowItem.colF_Status.contains("ACT", true) || rowItem.colF_Status.contains("چالاک", true))
                          EmeraldLight.copy(alpha = 0.6f)
                        else if (rowItem.colF_Status.contains("SET", true) || rowItem.colF_Status.contains("تمام", true))
                          Color(0xFF38BDF8).copy(alpha = 0.6f)
                        else
                          LossRed.copy(alpha = 0.6f)
                      )
                    ) {
                      Text(
                        text = rowItem.colF_Status,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rowItem.colF_Status.contains("ACT", true) || rowItem.colF_Status.contains("چالاک", true))
                          EmeraldLight
                        else if (rowItem.colF_Status.contains("SET", true) || rowItem.colF_Status.contains("تمام", true))
                          Color(0xFF38BDF8)
                        else
                          LossRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }

              // Sticky / Footer Summary Bar (Σ SUM)
              item {
                Row(
                  modifier = Modifier
                    .background(Color(0xFF091426))
                    .border(width = 1.dp, color = TerminalAccentBlue.copy(alpha = 0.6f))
                    .padding(vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(modifier = Modifier.width(118.dp), contentAlignment = Alignment.Center) {
                    Text(
                      text = if (isKurdish) "Σ کۆما گشتی" else "Σ TOTAL SUM",
                      fontWeight = FontWeight.Black,
                      fontSize = 11.5.sp,
                      color = GoldAccent
                    )
                  }
                  Box(modifier = Modifier.width(530.dp)) {
                    Text(
                      text = "${filteredSortedRows.size} ${if (isKurdish) "ڕێز هاتینە هەژمارتن" else "rows calculated"}",
                      fontSize = 10.5.sp,
                      color = Color.White.copy(alpha = 0.7f)
                    )
                  }
                  Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                    Text(
                      text = CurrencyService.format(totalSheetAmount, displayCurrency),
                      fontWeight = FontWeight.Black,
                      fontSize = 13.sp,
                      color = GoldAccent,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                  Box(modifier = Modifier.width(100.dp))
                }
              }
            }
          }
        }

        // 5. BOTTOM WORKBOOK SHEETS TABS (Executive Terminal Tabs with sheet counts)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081220))
            .border(width = 1.dp, color = TerminalBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val sheetTitles = listOf(
              Triple("📊", if (isKurdish) "هاوسەنگی" else "Balance Sheet", sheet0Rows.size),
              Triple("🤝", if (isKurdish) "قەرز و دەین" else "Debts", sheet1Rows.size),
              Triple("🚗", if (isKurdish) "سەیارە" else "Vehicles", sheet2Rows.size),
              Triple("🏛️", if (isKurdish) "موڵک و زێڕ" else "Assets", sheet3Rows.size)
            )

            sheetTitles.forEachIndexed { index, (icon, title, count) ->
              val isSheetActive = activeSheetIndex == index
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 40.dp)
                  .clickable {
                    activeSheetIndex = index
                    searchQuery = ""
                    selectedRowIndex = 0
                  }
                  .testTag("excel_sheet_tab_${index}"),
                shape = RoundedCornerShape(10.dp),
                color = if (isSheetActive) TerminalAccentBlue.copy(alpha = 0.22f) else TerminalCardBg,
                border = BorderStroke(
                  width = if (isSheetActive) 1.2.dp else 0.7.dp,
                  color = if (isSheetActive) TerminalAccentBlue else TerminalBorder
                )
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = icon,
                    fontSize = 12.sp
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (isSheetActive) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSheetActive) Color.White else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Surface(
                    shape = CircleShape,
                    color = if (isSheetActive) TerminalAccentBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
                  ) {
                    Text(
                      text = "$count",
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSheetActive) TerminalAccentBlue else Color(0xFF64748B),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (previewPdfResult != null) {
    PrintPreviewDialog(
      result = previewPdfResult!!,
      onDismiss = { previewPdfResult = null }
    )
  }
}
