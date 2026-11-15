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
import com.example.data.local.InvestmentEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService

/**
 * Executive Financial Terminal for Real Estate & Portfolio Assets
 * (تەختا پێشکەفتی یا عەقارات و سامانێن وەبەرهێنانێ)
 */
@Composable
fun ExcelAssetsTableView(
  assets: List<InvestmentEntity>,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onSelectAsset: (InvestmentEntity) -> Unit,
  onOpenFullscreenWorkbook: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var tableSearchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "ESTATE", "GOLD", "ACTIVE"
  val horizontalScrollState = rememberScrollState()
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }

  val estateCount = remember(assets) { assets.count { it.category.contains("ESTATE", ignoreCase = true) || it.category.contains("REAL", ignoreCase = true) } }
  val goldCount = remember(assets) { assets.count { it.category.contains("GOLD", ignoreCase = true) } }
  val activeCount = remember(assets) { assets.count { !it.isSold } }

  val filteredRows by remember(assets, tableSearchQuery, selectedCategoryFilter) {
    derivedStateOf {
      assets.filter { asset ->
        val matchesCategory = when (selectedCategoryFilter) {
          "ESTATE" -> asset.category.contains("ESTATE", ignoreCase = true) || asset.category.contains("REAL", ignoreCase = true)
          "GOLD" -> asset.category.contains("GOLD", ignoreCase = true)
          "ACTIVE" -> !asset.isSold
          else -> true
        }
        val matchesSearch = if (tableSearchQuery.isBlank()) true else {
          val q = tableSearchQuery.trim().lowercase()
          asset.name.lowercase().contains(q) ||
            asset.category.lowercase().contains(q) ||
            asset.partnerName.lowercase().contains(q) ||
            asset.notes.lowercase().contains(q)
        }
        matchesCategory && matchesSearch
      }
    }
  }

  // AutoSum calculations
  val totalInitialCapital by remember(filteredRows, displayCurrency, currentIqdRate) {
    derivedStateOf {
      filteredRows.sumOf {
        CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
      }
    }
  }

  val totalCurrentValuation by remember(filteredRows, displayCurrency, currentIqdRate) {
    derivedStateOf {
      filteredRows.sumOf {
        val amt = if (it.isSold && it.soldPrice > 0.0) it.soldPrice else it.currentValue
        CurrencyService.convert(amt, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
      }
    }
  }

  val totalProfitGain by remember(filteredRows, displayCurrency, currentIqdRate) {
    derivedStateOf {
      filteredRows.sumOf {
        val profit = if (it.isSold && it.soldPrice > 0.0) it.soldPrice - it.initialCapital else it.currentValue - it.initialCapital
        CurrencyService.convert(profit, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
      }
    }
  }

  val terminalTitle = if (isKurdish) "تەختا موڵک و سامانان (Real Estate & Assets Terminal)" else "Executive Real Estate & Assets Terminal"
  val terminalSubtitle = if (isKurdish) {
    "نرخاندنا گشتی: ${CurrencyService.formatShort(totalCurrentValuation, displayCurrency)} • قازانج: ${if (totalProfitGain >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitGain, displayCurrency)}"
  } else {
    "Total Valuation: ${CurrencyService.formatShort(totalCurrentValuation, displayCurrency)} • Gain: ${if (totalProfitGain >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitGain, displayCurrency)}"
  }

  TerminalContainer(
    title = terminalTitle,
    subtitle = terminalSubtitle,
    isLive = true,
    liveLabel = "PORTFOLIO AUDIT",
    searchQuery = tableSearchQuery,
    onSearchChange = { tableSearchQuery = it },
    searchPlaceholder = if (isKurdish) "لێگەڕیان ب ناڤێ موڵکی، جور، شەریک..." else "Search asset name, category, partner...",
    onOpenFullscreen = onOpenFullscreenWorkbook,
    onExportPdf = {
      previewPdfResult = PdfExportService.generateAssetsPdf(
        context = context,
        assets = filteredRows,
        displayCurrency = displayCurrency,
        currentIqdRate = currentIqdRate,
        isKurdish = isKurdish
      )
    },
    pdfButtonTag = "excel_assets_export_pdf_button",
    testTag = "excel_assets_table_container",
    modifier = modifier
  ) {
    // 1. KPI Metric Strip
    TerminalKpiStrip(
      items = listOf(
        TerminalKpiItem(
          label = if (isKurdish) "سەرمایێ دەستپێکێ" else "Initial Capital",
          value = CurrencyService.formatShort(totalInitialCapital, displayCurrency),
          customColor = Color.White
        ),
        TerminalKpiItem(
          label = if (isKurdish) "نرخاندنا نوکە" else "Current Value",
          value = CurrencyService.formatShort(totalCurrentValuation, displayCurrency),
          customColor = TerminalAccentBlue
        ),
        TerminalKpiItem(
          label = if (isKurdish) "زێدەهی / قازانج" else "Capital Gain",
          value = "${if (totalProfitGain >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitGain, displayCurrency)}",
          customColor = if (totalProfitGain >= 0) EmeraldLight else LossRed
        ),
        TerminalKpiItem(
          label = if (isKurdish) "ژمارا موڵکان" else "Holdings Count",
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
          label = if (isKurdish) "هەمی سامان" else "All Assets",
          count = assets.size,
          highlightColor = GoldAccent
        ),
        TerminalFilterOption(
          id = "ESTATE",
          label = if (isKurdish) "عەقارات" else "Real Estate",
          count = estateCount,
          highlightColor = TerminalAccentBlue
        ),
        TerminalFilterOption(
          id = "GOLD",
          label = if (isKurdish) "زێڕ و ملوک" else "Gold & Valuables",
          count = goldCount,
          highlightColor = GoldAccent
        ),
        TerminalFilterOption(
          id = "ACTIVE",
          label = if (isKurdish) "چالاک" else "Active",
          count = activeCount,
          highlightColor = EmeraldLight
        )
      ),
      selectedId = selectedCategoryFilter,
      onSelect = { selectedCategoryFilter = it }
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
        TerminalHeaderCell(if (isKurdish) "ناڤێ موڵکی (Asset)" else "Asset Name", 155.dp)
        TerminalHeaderCell(if (isKurdish) "جور (Category)" else "Category", 110.dp)
        TerminalHeaderCell(if (isKurdish) "سەرمایە (Cost)" else "Cost", 115.dp)
        TerminalHeaderCell(if (isKurdish) "نرخاندن (Valuation)" else "Valuation", 120.dp)
        TerminalHeaderCell(if (isKurdish) "زێدەهی (Gain)" else "Net Gain", 110.dp)
        TerminalHeaderCell(if (isKurdish) "شەریکاتی" else "Partner", 125.dp)
        TerminalHeaderCell(if (isKurdish) "پشکا من" else "Share %", 85.dp)
        TerminalHeaderCell(if (isKurdish) "تێبینی" else "Notes", 140.dp)
      }

      // Empty State
      if (filteredRows.isEmpty()) {
        TerminalEmptyState(
          message = if (isKurdish) "چ موڵک ل دووڤ فلتەری نەهاتنە دیتن" else "No assets found matching current filter"
        )
      } else {
        filteredRows.forEachIndexed { index, asset ->
          val isEven = index % 2 == 0
          val rowBg = if (isEven) TerminalCardBg else TerminalRowAltBg
          val profit = if (asset.isSold && asset.soldPrice > 0.0) asset.soldPrice - asset.initialCapital else asset.currentValue - asset.initialCapital
          val isGainPositive = profit >= 0.0

          Row(
            modifier = Modifier
              .background(rowBg)
              .border(0.3.dp, TerminalBorder.copy(alpha = 0.5f))
              .clickable { onSelectAsset(asset) }
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

            // Col 2: Name
            Row(
              modifier = Modifier.width(155.dp).padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val iconPrefix = if (asset.category.contains("GOLD", ignoreCase = true)) "🪙" else "🏢"
              Text(
                text = "$iconPrefix ${asset.name}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Col 3: Category Chip
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = TerminalAccentBlue.copy(alpha = 0.12f),
              border = BorderStroke(0.7.dp, TerminalAccentBlue.copy(alpha = 0.5f)),
              modifier = Modifier.width(105.dp).padding(horizontal = 4.dp)
            ) {
              Text(
                text = asset.category.ifBlank { "ASSET" },
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = TerminalAccentBlue,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 3.dp, horizontal = 4.dp)
              )
            }

            // Col 4: Cost
            Text(
              text = "${asset.currency} ${CurrencyService.formatShort(asset.initialCapital, asset.currency)}",
              modifier = Modifier.width(115.dp).padding(horizontal = 8.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFCBD5E1),
              fontFamily = FontFamily.Monospace
            )

            // Col 5: Valuation
            val valAmt = if (asset.isSold && asset.soldPrice > 0.0) asset.soldPrice else asset.currentValue
            Text(
              text = "${asset.currency} ${CurrencyService.formatShort(valAmt, asset.currency)}",
              modifier = Modifier.width(120.dp).padding(horizontal = 8.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (asset.isSold) EmeraldLight else TerminalAccentBlue,
              fontFamily = FontFamily.Monospace
            )

            // Col 6: Net Gain
            val gainColor = if (isGainPositive) EmeraldLight else LossRed
            Text(
              text = "${if (isGainPositive) "+" else ""}${CurrencyService.formatShort(profit, asset.currency)}",
              modifier = Modifier.width(110.dp).padding(horizontal = 8.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = gainColor,
              fontFamily = FontFamily.Monospace
            )

            // Col 7: Partner
            Text(
              text = if (asset.isCoOwned) asset.partnerName.ifBlank { "شەریک" } else (if (isKurdish) "ب تنێ" else "Solo"),
              modifier = Modifier.width(125.dp).padding(horizontal = 8.dp),
              fontSize = 11.sp,
              color = if (asset.isCoOwned) GoldAccent else Color(0xFF94A3B8),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            // Col 8: Share %
            Text(
              text = if (asset.isCoOwned) "${asset.mySharePercent}%" else "100%",
              modifier = Modifier.width(85.dp).padding(horizontal = 8.dp),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (asset.isCoOwned) GoldAccent else EmeraldLight,
              textAlign = TextAlign.Center
            )

            // Col 9: Notes
            Text(
              text = asset.notes.ifBlank { "-" },
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
          text = if (isKurdish) "سەرجەمێ موڵکان (Assets Total)" else "Assets Terminal Total",
          modifier = Modifier.width(155.dp).padding(horizontal = 6.dp),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Black,
          color = GoldAccent
        )
        Text(
          text = "سەرمایە: ${CurrencyService.formatShort(totalInitialCapital, displayCurrency)}",
          modifier = Modifier.width(170.dp).padding(horizontal = 6.dp),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "نرخاندن: ${CurrencyService.formatShort(totalCurrentValuation, displayCurrency)}",
          modifier = Modifier.width(160.dp).padding(horizontal = 6.dp),
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = TerminalAccentBlue
        )
        val netGainColor = if (totalProfitGain >= 0) EmeraldLight else LossRed
        Text(
          text = "زێدەهی: ${if (totalProfitGain >= 0) "+" else ""}${CurrencyService.formatShort(totalProfitGain, displayCurrency)}",
          modifier = Modifier.width(180.dp).padding(horizontal = 6.dp),
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = netGainColor
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
