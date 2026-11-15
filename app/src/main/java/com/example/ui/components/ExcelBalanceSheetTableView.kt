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
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService

data class BalanceSheetExcelRow(
  val cellRef: String,
  val accountNameKu: String,
  val accountNameEn: String,
  val accountType: String, // "ASSET", "LIABILITY", "EQUITY"
  val formula: String,
  val amount: Double,
  val isTotalRow: Boolean = false
)

/**
 * Executive Financial Terminal for Master Balance Sheet & Net Worth Ledger
 * (تەختا پێشکەفتی یا میزانیەیێ و سەروەتێ گشتی)
 */
@Composable
fun ExcelBalanceSheetTableView(
  uiState: FinanceUiState,
  isKurdish: Boolean = true,
  onOpenFullscreenWorkbook: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val displayCurrency = uiState.displayCurrency
  val horizontalScrollState = rememberScrollState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ASSET", "LIABILITY"
  var selectedCellDetail by remember { mutableStateOf<BalanceSheetExcelRow?>(null) }
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }

  // Build Balance Sheet Line Items
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

  val totalAssets = cashVault + carsNet + estateVal + mulukGoldVal + vaultGoldVal + loansGiven + privateCredits
  val totalLiabilities = debtsOwed + myCarInstallmentsDebt + estateDebts
  val netWorth = totalAssets - totalLiabilities

  val rows = remember(uiState) {
    val list = mutableListOf(
      BalanceSheetExcelRow("A1", "کاشێ بەردەست (سیولە)", "Cash & Bank Vault", "ASSET", "=VAULT(USD_IQD)", cashVault),
      BalanceSheetExcelRow("A2", "سەرمایێ سەیارێن بەردەست (تنێ پشکا من)", "Active Cars Valuation (My Share Only)", "ASSET", "=SUM(CARS:ActiveMyShare)", carsNet),
      BalanceSheetExcelRow("A3", "عەقارات و ملوک", "Real Estate Properties", "ASSET", "=SUM(ESTATE:Valuation)", estateVal),
      BalanceSheetExcelRow("A4", "زێڕێ ملوک و وەبەرهێنانێ", "Muluk Gold Valuables", "ASSET", "=SUM(ESTATE:Gold)", mulukGoldVal),
      BalanceSheetExcelRow("A5", "خەزینەیا زێڕ و زیڤی (جیهانی)", "Spot Gold & Silver Vault", "ASSET", "=MARKET(GOLD_SPOT)", vaultGoldVal),
      BalanceSheetExcelRow("A6", "قەرزێن گشتی دەف خەلکی", "Receivables (Loans Given)", "ASSET", "=SUM(DEBTS:Credits)", loansGiven)
    )
    if (privateCredits > 0.0) {
      list.add(BalanceSheetExcelRow("A7", "قەرزێ تایبەتێ ملوکان دەف خەلکی", "Muluk Private Credits", "ASSET", "=SUM(ESTATE:Credits)", privateCredits))
    }
    list.add(BalanceSheetExcelRow("B1", "قەرزێن گشتی یێن لسەر من", "Payables (Debts Owed)", "LIABILITY", "=SUM(DEBTS:Payables)", debtsOwed))
    list.add(BalanceSheetExcelRow("B2", "باقیێ قستێن سەیاران (تنێ پشکا من)", "Car Installments Debt (My Share Only)", "LIABILITY", "=SUM(CARS:MyInstallments)", myCarInstallmentsDebt))
    list.add(BalanceSheetExcelRow("B3", "قەرزێ ماى لسەر ملوکان", "Debt Remaining on Muluk", "LIABILITY", "=SUM(ESTATE:Debt)", estateDebts))
    list
  }

  val assetCount = remember(rows) { rows.count { it.accountType == "ASSET" } }
  val liabCount = remember(rows) { rows.count { it.accountType == "LIABILITY" } }

  val filteredRows by remember(rows, searchQuery, selectedFilter) {
    derivedStateOf {
      rows.filter { item ->
        val matchesCategory = when (selectedFilter) {
          "ASSET" -> item.accountType == "ASSET"
          "LIABILITY" -> item.accountType == "LIABILITY"
          else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
          val q = searchQuery.trim().lowercase()
          item.cellRef.lowercase().contains(q) ||
            item.accountNameKu.lowercase().contains(q) ||
            item.accountNameEn.lowercase().contains(q) ||
            item.accountType.lowercase().contains(q) ||
            item.formula.lowercase().contains(q)
        }
        matchesCategory && matchesSearch
      }
    }
  }

  TerminalContainer(
    title = if (isKurdish) "تەختا سەرەکی یا میزانیەیێ (Balance Sheet Terminal)" else "Executive Balance Sheet Terminal",
    subtitle = if (isKurdish) "سەروەتێ صافی: ${CurrencyService.formatShort(netWorth, displayCurrency)}"
               else "Net Worth: ${CurrencyService.formatShort(netWorth, displayCurrency)}",
    isLive = true,
    liveLabel = "LIVE AUDIT",
    searchQuery = searchQuery,
    onSearchChange = { searchQuery = it },
    searchPlaceholder = if (isKurdish) "لێگەڕیان د میزانیەیێ دا..." else "Search ledger accounts...",
    onOpenFullscreen = onOpenFullscreenWorkbook,
    onExportPdf = {
      previewPdfResult = PdfExportService.generateBalanceSheetPdf(
        context = context,
        rows = rows,
        totalAssets = totalAssets,
        totalLiabilities = totalLiabilities,
        netWorth = netWorth,
        displayCurrency = displayCurrency,
        isKurdish = isKurdish
      )
    },
    pdfButtonTag = "excel_balancesheet_export_pdf_button",
    testTag = "excel_balancesheet_table_container",
    modifier = modifier
  ) {
    // 1. Executive KPI Summary Strip
    TerminalKpiStrip(
      items = listOf(
        TerminalKpiItem(
          label = if (isKurdish) "سەروەتێ صافی" else "Net Worth",
          value = "${if (netWorth >= 0) "+" else ""}${CurrencyService.formatShort(netWorth, displayCurrency)}",
          isPositive = netWorth >= 0,
          customColor = GoldAccent
        ),
        TerminalKpiItem(
          label = if (isKurdish) "کۆیێ سامانان" else "Total Assets",
          value = "+${CurrencyService.formatShort(totalAssets, displayCurrency)}",
          isPositive = true
        ),
        TerminalKpiItem(
          label = if (isKurdish) "کۆیێ قەرزان" else "Total Liabilities",
          value = "-${CurrencyService.formatShort(totalLiabilities, displayCurrency)}",
          isPositive = false
        ),
        TerminalKpiItem(
          label = if (isKurdish) "رێژەیا سامانان" else "Equity Ratio",
          value = if (totalLiabilities > 0) "%.1fx".format(totalAssets / totalLiabilities) else "100%",
          customColor = Color(0xFF38BDF8)
        )
      )
    )

    // 2. Interactive Filter Pills
    TerminalFilterPills(
      options = listOf(
        TerminalFilterOption("ALL", if (isKurdish) "هەمی" else "All Accounts", rows.size),
        TerminalFilterOption("ASSET", if (isKurdish) "سامانێن بەردەست" else "Assets", assetCount, EmeraldLight),
        TerminalFilterOption("LIABILITY", if (isKurdish) "قەرز و ئیلتزام" else "Liabilities", liabCount, LossRed)
      ),
      selectedId = selectedFilter,
      onSelect = { selectedFilter = it }
    )

    // 3. Selected Row Audit Inspector Pill
    if (selectedCellDetail != null) {
      val item = selectedCellDetail!!
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F1E36),
        border = BorderStroke(1.dp, TerminalAccentBlue.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().clickable { selectedCellDetail = null }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "AUDIT [${item.cellRef}]:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TerminalAccentBlue, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = item.formula, fontSize = 10.5.sp, color = Color.White, fontFamily = FontFamily.Monospace)
          }
          Text(text = "✕", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        }
      }
    }

    // 4. Terminal Ledger Table
    if (filteredRows.isEmpty()) {
      TerminalEmptyState(
        message = if (isKurdish) "چ بڕگەیێن میزانیەیێ نەهاتنە دیتن" else "No ledger accounts match your filter"
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
            TerminalHeaderCell(if (isKurdish) "بڕگە و ناڤێ حسێبێ" else "Account & Line Item", 220.dp)
            TerminalHeaderCell(if (isKurdish) "جۆر" else "Type", 100.dp, TextAlign.Center)
            TerminalHeaderCell(if (isKurdish) "هاوکێشە / سەرچاوە" else "Formula / Source", 160.dp)
            TerminalHeaderCell(if (isKurdish) "بڕێ کۆتایی" else "Balance Valuation", 140.dp, TextAlign.End)
            TerminalHeaderCell(if (isKurdish) "دراڤ" else "Cur", 65.dp, TextAlign.Center)
          }

          // Table Rows
          filteredRows.forEachIndexed { index, row ->
            val isAsset = row.accountType == "ASSET"
            val rowBg = if (index % 2 == 0) TerminalDarkBg else TerminalRowAltBg
            val amountColor = if (isAsset) EmeraldLight else LossRed

            Row(
              modifier = Modifier
                .background(rowBg)
                .clickable { selectedCellDetail = row }
                .padding(vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Cell Ref / ID
              Text(
                text = row.cellRef,
                modifier = Modifier.width(45.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
              )

              // Account Name
              Column(
                modifier = Modifier
                  .width(220.dp)
                  .padding(horizontal = 6.dp)
              ) {
                Text(
                  text = if (isKurdish) row.accountNameKu else row.accountNameEn,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = if (isKurdish) row.accountNameEn else row.accountNameKu,
                  fontSize = 9.5.sp,
                  color = Color(0xFF94A3B8),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              // Account Type Badge
              Box(
                modifier = Modifier.width(100.dp),
                contentAlignment = Alignment.Center
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isAsset) EmeraldLight.copy(alpha = 0.16f) else LossRed.copy(alpha = 0.16f),
                  border = BorderStroke(0.7.dp, if (isAsset) EmeraldLight.copy(alpha = 0.6f) else LossRed.copy(alpha = 0.6f))
                ) {
                  Text(
                    text = if (isAsset) (if (isKurdish) "سامان" else "ASSET") else (if (isKurdish) "قەرز" else "LIABILITY"),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isAsset) EmeraldLight else LossRed,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                  )
                }
              }

              // Formula / Audit Source
              Text(
                text = row.formula,
                modifier = Modifier
                  .width(160.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 10.5.sp,
                color = TerminalAccentBlue,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )

              // Valuation Amount
              Text(
                text = "${if (isAsset) "+" else "-"}${CurrencyService.formatShort(row.amount, displayCurrency)}",
                modifier = Modifier
                  .width(140.dp)
                  .padding(horizontal = 6.dp),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = amountColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
              )

              // Currency
              Text(
                text = displayCurrency,
                modifier = Modifier.width(65.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
              )
            }
          }

          // Terminal AutoSum Summary Footer Row
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
              text = if (isKurdish) "سەروەتێ صافی یێ باوەڕپێکری (Net Worth Equity)" else "Net Worth Equity Total",
              modifier = Modifier.width(220.dp).padding(horizontal = 6.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black,
              color = GoldAccent
            )
            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
              Surface(shape = RoundedCornerShape(4.dp), color = GoldAccent.copy(alpha = 0.2f)) {
                Text(text = "EQUITY", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GoldAccent, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
              }
            }
            Text(text = "=ASSETS - LIABILITIES", modifier = Modifier.width(160.dp).padding(horizontal = 6.dp), fontSize = 10.sp, color = TerminalAccentBlue, fontFamily = FontFamily.Monospace)
            val netColor = if (netWorth >= 0) EmeraldLight else LossRed
            Text(
              text = "${if (netWorth >= 0) "+" else ""}${CurrencyService.formatShort(netWorth, displayCurrency)}",
              modifier = Modifier.width(140.dp).padding(horizontal = 6.dp),
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Black,
              color = netColor,
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.End
            )
            Text(text = displayCurrency, modifier = Modifier.width(65.dp), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
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
