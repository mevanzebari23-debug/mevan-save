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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Sell
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
import com.example.data.local.InvestmentEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService

/**
 * Executive Financial Terminal for Cars & Fleet Assets
 * (تەختا پێشکەفتی یا سەیاران و فلیتێ وەبەرهێنانێ)
 */
@Composable
fun ExcelCarsTableView(
  cars: List<InvestmentEntity>,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onSelectCar: (InvestmentEntity) -> Unit,
  onSellCar: ((InvestmentEntity) -> Unit)? = null,
  onOpenFullscreenWorkbook: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var tableSearchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "SOLD", "CO_OWNED"
  val horizontalScrollState = rememberScrollState()
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }

  val activeCarsCount = remember(cars) { cars.count { !it.isSold } }
  val soldCarsCount = remember(cars) { cars.count { it.isSold } }
  val coOwnedCarsCount = remember(cars) { cars.count { it.isCoOwned } }

  val filteredRows by remember(cars, tableSearchQuery, selectedFilter) {
    derivedStateOf {
      cars.filter { car ->
        val matchesFilter = when (selectedFilter) {
          "ACTIVE" -> !car.isSold
          "SOLD" -> car.isSold
          "CO_OWNED" -> car.isCoOwned
          else -> true
        }
        val matchesSearch = if (tableSearchQuery.isBlank()) true else {
          val q = tableSearchQuery.trim().lowercase()
          car.name.lowercase().contains(q) ||
            car.partnerName.lowercase().contains(q) ||
            car.notes.lowercase().contains(q) ||
            (car.isSold && (if (isKurdish) "فرۆتی" else "sold").contains(q)) ||
            (!car.isSold && (if (isKurdish) "بەردەست" else "active").contains(q))
        }
        matchesFilter && matchesSearch
      }
    }
  }

  // Summary calculations
  val totalPurchaseCost by remember(filteredRows, displayCurrency, currentIqdRate) {
    derivedStateOf {
      filteredRows.sumOf { car ->
        CurrencyService.convert(car.initialCapital, car.currency, displayCurrency, currentIqdRate, entryIqdRate = car.exchangeRateAtEntry)
      }
    }
  }

  val totalCurrentValuation by remember(filteredRows, displayCurrency, currentIqdRate) {
    derivedStateOf {
      filteredRows.sumOf { car ->
        val amount = if (car.isSold && car.soldPrice > 0.0) car.soldPrice else car.currentValue
        CurrencyService.convert(amount, car.currency, displayCurrency, currentIqdRate, entryIqdRate = car.exchangeRateAtEntry)
      }
    }
  }

  val totalProfitLoss by remember(filteredRows, displayCurrency, currentIqdRate) {
    derivedStateOf {
      filteredRows.sumOf { car ->
        val profitInCarCurr = if (car.isSold && car.soldPrice > 0.0) car.soldPrice - car.initialCapital else car.currentValue - car.initialCapital
        CurrencyService.convert(profitInCarCurr, car.currency, displayCurrency, currentIqdRate, entryIqdRate = car.exchangeRateAtEntry)
      }
    }
  }

  val terminalTitle = if (isKurdish) "تەختا سەیاران و فلیت (Fleet Asset Terminal)" else "Executive Fleet & Car Terminal"
  val terminalSubtitle = if (isKurdish) {
    "سەرمایێ فلیتی: ${CurrencyService.formatShort(totalPurchaseCost, displayCurrency)} • قازانج: ${if (totalProfitLoss >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitLoss, displayCurrency)}"
  } else {
    "Fleet Capital: ${CurrencyService.formatShort(totalPurchaseCost, displayCurrency)} • P&L: ${if (totalProfitLoss >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitLoss, displayCurrency)}"
  }

  TerminalContainer(
    title = terminalTitle,
    subtitle = terminalSubtitle,
    isLive = true,
    liveLabel = "FLEET AUDIT",
    searchQuery = tableSearchQuery,
    onSearchChange = { tableSearchQuery = it },
    searchPlaceholder = if (isKurdish) "لێگەڕیان ب ناڤێ سەیارێ، شەریک، تێبینی..." else "Search car model, partner, notes...",
    onOpenFullscreen = onOpenFullscreenWorkbook,
    onExportPdf = {
      previewPdfResult = PdfExportService.generateCarsPdf(
        context = context,
        cars = filteredRows,
        displayCurrency = displayCurrency,
        currentIqdRate = currentIqdRate,
        isKurdish = isKurdish
      )
    },
    pdfButtonTag = "excel_cars_export_pdf_button",
    testTag = "excel_cars_table_container",
    modifier = modifier
  ) {
    // 1. KPI Metric Strip
    TerminalKpiStrip(
      items = listOf(
        TerminalKpiItem(
          label = if (isKurdish) "سەرمایێ کڕینێ" else "Capital Cost",
          value = CurrencyService.formatShort(totalPurchaseCost, displayCurrency),
          customColor = Color.White
        ),
        TerminalKpiItem(
          label = if (isKurdish) "هەڵسەنگاندن/فرۆتن" else "Valuation / Sales",
          value = CurrencyService.formatShort(totalCurrentValuation, displayCurrency),
          customColor = TerminalAccentBlue
        ),
        TerminalKpiItem(
          label = if (isKurdish) "قازانج / زەرەر" else "Net P&L",
          value = "${if (totalProfitLoss >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitLoss, displayCurrency)}",
          customColor = if (totalProfitLoss >= 0) EmeraldLight else LossRed
        ),
        TerminalKpiItem(
          label = if (isKurdish) "ژمارا سەیاران" else "Fleet Units",
          value = "${filteredRows.size}",
          customColor = GoldAccent
        )
      )
    )

    // 2. Interactive Quick-Filter Pills
    TerminalFilterPills(
      options = listOf(
        TerminalFilterOption(
          id = "ALL",
          label = if (isKurdish) "هەمی سەیارە" else "All Fleet",
          count = cars.size,
          highlightColor = GoldAccent
        ),
        TerminalFilterOption(
          id = "ACTIVE",
          label = if (isKurdish) "بەردەست" else "Active Fleet",
          count = activeCarsCount,
          highlightColor = EmeraldLight
        ),
        TerminalFilterOption(
          id = "SOLD",
          label = if (isKurdish) "فرۆتی" else "Sold",
          count = soldCarsCount,
          highlightColor = TerminalAccentBlue
        ),
        TerminalFilterOption(
          id = "CO_OWNED",
          label = if (isKurdish) "شەریکاتی" else "Co-Owned",
          count = coOwnedCarsCount,
          highlightColor = GoldAccent
        )
      ),
      selectedId = selectedFilter,
      onSelect = { selectedFilter = it }
    )

    // 3. Horizontal Scrollable Terminal Grid
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
        .background(TerminalDarkBg)
        .horizontalScroll(horizontalScrollState)
    ) {
      // Header Cells
      Row(
        modifier = Modifier
          .background(TerminalHeaderBg)
          .border(0.5.dp, TerminalBorder)
          .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        TerminalHeaderCell("#", 45.dp)
        TerminalHeaderCell(if (isKurdish) "سەیارە (Car Model)" else "Car Model", 150.dp)
        TerminalHeaderCell(if (isKurdish) "کڕین (Cost)" else "Cost", 115.dp)
        TerminalHeaderCell(if (isKurdish) "نوکە/فرۆتن" else "Valuation", 115.dp)
        TerminalHeaderCell(if (isKurdish) "قازانج/زەرەر" else "P&L", 110.dp)
        TerminalHeaderCell(if (isKurdish) "حاڵەت" else "Status", 95.dp)
        TerminalHeaderCell(if (isKurdish) "شەریکاتی" else "Partner", 125.dp)
        TerminalHeaderCell(if (isKurdish) "پشکا من" else "Share %", 85.dp)
        TerminalHeaderCell(if (isKurdish) "تێبینی" else "Notes", 140.dp)
      }

      // Empty State
      if (filteredRows.isEmpty()) {
        TerminalEmptyState(
          message = if (isKurdish) "چ سەیارە ل دووڤ فلتەری نەهاتنە دیتن" else "No vehicles found matching current filter"
        )
      } else {
        filteredRows.forEachIndexed { index, car ->
          val isEven = index % 2 == 0
          val rowBg = if (isEven) TerminalCardBg else TerminalRowAltBg
          val profit = if (car.isSold && car.soldPrice > 0.0) car.soldPrice - car.initialCapital else car.currentValue - car.initialCapital
          val isProfitPositive = profit >= 0.0

          Row(
            modifier = Modifier
              .background(rowBg)
              .border(0.3.dp, TerminalBorder.copy(alpha = 0.5f))
              .clickable { onSelectCar(car) }
              .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Col 1: Index
            Text(
              text = "${index + 1}",
              modifier = Modifier.width(45.dp),
              fontSize = 11.sp,
              color = Color(0xFF64748B),
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.Center
            )

            // Col 2: Car Name
            Row(
              modifier = Modifier.width(150.dp).padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🚗 ${car.name}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Col 3: Cost
            Text(
              text = "${car.currency} ${CurrencyService.formatShort(car.initialCapital, car.currency)}",
              modifier = Modifier.width(115.dp).padding(horizontal = 8.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFCBD5E1),
              fontFamily = FontFamily.Monospace
            )

            // Col 4: Valuation / Sold Price
            val valAmt = if (car.isSold && car.soldPrice > 0.0) car.soldPrice else car.currentValue
            Text(
              text = "${car.currency} ${CurrencyService.formatShort(valAmt, car.currency)}",
              modifier = Modifier.width(115.dp).padding(horizontal = 8.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (car.isSold) EmeraldLight else TerminalAccentBlue,
              fontFamily = FontFamily.Monospace
            )

            // Col 5: P&L
            val pnlColor = if (isProfitPositive) EmeraldLight else LossRed
            Text(
              text = "${if (isProfitPositive) "+" else ""}${CurrencyService.formatShort(profit, car.currency)}",
              modifier = Modifier.width(110.dp).padding(horizontal = 8.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = pnlColor,
              fontFamily = FontFamily.Monospace
            )

            // Col 6: Status Chip
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (car.isSold) EmeraldLight.copy(alpha = 0.15f) else TerminalAccentBlue.copy(alpha = 0.15f),
              border = BorderStroke(
                0.8.dp,
                if (car.isSold) EmeraldLight.copy(alpha = 0.7f) else TerminalAccentBlue.copy(alpha = 0.7f)
              ),
              modifier = Modifier.width(90.dp).padding(horizontal = 4.dp)
            ) {
              Text(
                text = if (car.isSold) (if (isKurdish) "فرۆتی" else "Sold") else (if (isKurdish) "بەردەست" else "Active"),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (car.isSold) EmeraldLight else TerminalAccentBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 3.dp)
              )
            }

            // Col 7: Partner
            Text(
              text = if (car.isCoOwned) car.partnerName.ifBlank { "شەریک" } else (if (isKurdish) "ب تنێ" else "Solo"),
              modifier = Modifier.width(125.dp).padding(horizontal = 8.dp),
              fontSize = 11.sp,
              color = if (car.isCoOwned) GoldAccent else Color(0xFF94A3B8),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            // Col 8: Share %
            Text(
              text = if (car.isCoOwned) "${car.mySharePercent}%" else "100%",
              modifier = Modifier.width(85.dp).padding(horizontal = 8.dp),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (car.isCoOwned) GoldAccent else EmeraldLight,
              textAlign = TextAlign.Center
            )

            // Col 9: Notes
            Text(
              text = car.notes.ifBlank { "-" },
              modifier = Modifier.width(140.dp).padding(horizontal = 8.dp),
              fontSize = 11.sp,
              color = Color(0xFF94A3B8),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      // Terminal Summary Footer
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF0A1526))
          .border(0.8.dp, TerminalAccentBlue.copy(alpha = 0.4f))
          .padding(vertical = 10.dp, horizontal = 6.dp),
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
          text = if (isKurdish) "سەرجەمێ فلیتی (Fleet Terminal Total)" else "Fleet Terminal Total",
          modifier = Modifier.width(150.dp).padding(horizontal = 6.dp),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Black,
          color = GoldAccent
        )
        Text(
          text = "کڕین: ${CurrencyService.formatShort(totalPurchaseCost, displayCurrency)}",
          modifier = Modifier.width(170.dp).padding(horizontal = 6.dp),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "نوکە: ${CurrencyService.formatShort(totalCurrentValuation, displayCurrency)}",
          modifier = Modifier.width(160.dp).padding(horizontal = 6.dp),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = TerminalAccentBlue
        )
        val netPnlColor = if (totalProfitLoss >= 0) EmeraldLight else LossRed
        Text(
          text = "قازانج: ${if (totalProfitLoss >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitLoss, displayCurrency)}",
          modifier = Modifier.width(180.dp).padding(horizontal = 6.dp),
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = netPnlColor
        )
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
