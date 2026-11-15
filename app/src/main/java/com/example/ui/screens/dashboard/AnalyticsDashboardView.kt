package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.NetWorthTrendLineChart
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Data models for charting
data class MonthlyCashFlowDataPoint(
  val monthKey: String, // e.g. "2026-09"
  val monthLabel: String, // e.g. "Sep"
  val fullLabel: String, // e.g. "Sep 2026"
  val inflow: Double,
  val outflow: Double,
  val net: Double,
  val timestamp: Long
)

data class InvestmentPLDataPoint(
  val id: Long,
  val name: String,
  val category: String,
  val initialCapital: Double,
  val currentValueOrSold: Double,
  val profitLoss: Double,
  val roiPercent: Double,
  val isSold: Boolean,
  val isWinning: Boolean
)

@Composable
fun AnalyticsDashboardView(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)
  val displayCurrency = uiState.displayCurrency

  // Tabs: 0 -> Overview (Both), 1 -> Cash Flow, 2 -> Investment P&L
  var selectedTab by remember { mutableIntStateOf(0) }
  // Horizon: 0 -> 3M, 1 -> 6M, 2 -> 1Y, 3 -> All
  var selectedHorizon by remember { mutableIntStateOf(1) } // Default 6 months

  // Calculate Monthly Cash Flow points from transactions
  val monthlyCashflowPoints = remember(uiState.transactions, uiState.displayCurrency, uiState.iqdRate, selectedHorizon) {
    calculateMonthlyCashFlow(
      transactions = uiState.transactions,
      displayCurrency = uiState.displayCurrency,
      iqdRate = uiState.iqdRate,
      horizonMonths = when (selectedHorizon) {
        0 -> 3
        1 -> 6
        2 -> 12
        else -> 24
      }
    )
  }

  // Calculate Investment P&L points
  val investmentPLPoints = remember(uiState.investments, uiState.carInvestments, uiState.displayCurrency, uiState.iqdRate) {
    calculateInvestmentPL(
      uiState = uiState
    )
  }

  val totalPeriodInflow = monthlyCashflowPoints.sumOf { it.inflow }
  val totalPeriodOutflow = monthlyCashflowPoints.sumOf { it.outflow }
  val netPeriodCashflow = totalPeriodInflow - totalPeriodOutflow

  val totalProfitGains = investmentPLPoints.filter { it.profitLoss > 0 }.sumOf { it.profitLoss }
  val totalLossGains = investmentPLPoints.filter { it.profitLoss < 0 }.sumOf { abs(it.profitLoss) }
  val netInvestPL = investmentPLPoints.sumOf { it.profitLoss }
  val winningDealsCount = investmentPLPoints.count { it.isWinning }
  val totalDealsCount = investmentPLPoints.size
  val winRatePercent = if (totalDealsCount > 0) ((winningDealsCount.toDouble() / totalDealsCount) * 100).toInt() else 0

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("analytics_dashboard_view"),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(top = 10.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("dashboard_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isKurdish) "داشبۆردێ چارتان و شیکاریێ" else "Visual Financial Dashboard",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isKurdish) "شیکاریا داهات، مەسروف، و قازانج و خوسارەتێ وەبەرهێنانێ" else "Interactive Cash Flow & Investment P&L Visualizer",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Currency Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (displayCurrency == "IQD") GoldAccent.copy(alpha = 0.2f) else EmeraldLight.copy(alpha = 0.2f))
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = displayCurrency,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp,
              color = if (displayCurrency == "IQD") GoldAccent else EmeraldLight
            )
          }
        }

        // Tab Row (Overview, Cashflow, Investment P&L)
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = EmeraldLight,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = EmeraldLight
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                text = if (isKurdish) "کورتیا گشتی" else "Combined",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                text = if (isKurdish) "کاش فلۆ (هەیڤانە)" else "Cash Flow",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = {
              Text(
                text = if (isKurdish) "قازانج و خوسارەت (P&L)" else "Investments P&L",
                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          )
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Time Horizon Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = "Time Horizon",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKurdish) "ماوێ چارتێ:" else "Horizon:",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val horizons = listOf(
              "3M" to if (isKurdish) "٣ هەیڤ" else "3 Mos",
              "6M" to if (isKurdish) "٦ هەیڤ" else "6 Mos",
              "1Y" to if (isKurdish) "١ ساڵ" else "1 Year",
              "ALL" to if (isKurdish) "هەمی" else "All"
            )
            horizons.forEachIndexed { index, pair ->
              val isSelected = selectedHorizon == index
              FilterChip(
                selected = isSelected,
                onClick = { selectedHorizon = index },
                label = { Text(pair.first, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldLight,
                  selectedLabelColor = Color(0xFF0F172A)
                ),
                modifier = Modifier.height(30.dp)
              )
            }
          }
        }
      }

      // 2. Executive KPI Header Summary Cards
      item {
        ExecutiveKpiHeader(
          totalInflow = totalPeriodInflow,
          totalOutflow = totalPeriodOutflow,
          netCashflow = netPeriodCashflow,
          netInvestPL = netInvestPL,
          winRatePercent = winRatePercent,
          displayCurrency = displayCurrency,
          isKurdish = isKurdish,
          selectedTab = selectedTab
        )
      }

      // 3. Main Chart Sections based on Tab
      if (selectedTab == 0) {
        item {
          NetWorthTrendLineChart(
            uiState = uiState,
            isKurdish = isKurdish,
            isMasked = uiState.isSensitiveDataMasked
          )
        }
      }

      if (selectedTab == 0 || selectedTab == 1) {
        // Section: Monthly Cash Flow Recharts-style Bar Chart
        item {
          MonthlyCashFlowBarChart(
            points = monthlyCashflowPoints,
            displayCurrency = displayCurrency,
            isKurdish = isKurdish
          )
        }
      }

      if (selectedTab == 0 || selectedTab == 2) {
        // Section: Investment Profits versus Losses Diverging Waterfall Chart
        item {
          InvestmentProfitLossWaterfallChart(
            points = investmentPLPoints,
            totalProfits = totalProfitGains,
            totalLosses = totalLossGains,
            displayCurrency = displayCurrency,
            isKurdish = isKurdish
          )
        }
      }

      // 4. Cumulative Trajectory Curve
      item {
        CumulativePerformanceCurve(
          cashflowPoints = monthlyCashflowPoints,
          investmentPoints = investmentPLPoints,
          displayCurrency = displayCurrency,
          isKurdish = isKurdish
        )
      }

      // 5. Itemized Performance Insights List
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "وردەکاریێن داتایێن وێنەکری" else "Visualized Performance Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${monthlyCashflowPoints.size} " + if (isKurdish) "هەیڤ" else "months",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recent Months Summary List
            monthlyCashflowPoints.takeLast(4).reversed().forEach { point ->
              MonthlyRowItem(point = point, displayCurrency = displayCurrency, isKurdish = isKurdish)
              Spacer(modifier = Modifier.height(6.dp))
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// EXECUTIVE KPI HEADER
// -------------------------------------------------------------
@Composable
private fun ExecutiveKpiHeader(
  totalInflow: Double,
  totalOutflow: Double,
  netCashflow: Double,
  netInvestPL: Double,
  winRatePercent: Int,
  displayCurrency: String,
  isKurdish: Boolean,
  selectedTab: Int
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Card 1: Net Cashflow
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.weight(1f)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isKurdish) "صافیێ کاش فلۆ" else "Net Cash Flow",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Icon(
            imageVector = if (netCashflow >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = if (netCashflow >= 0) EmeraldLight else LossRed,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = (if (netCashflow >= 0) "+ " else "") + CurrencyService.format(netCashflow, displayCurrency),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Black,
          color = if (netCashflow >= 0) EmeraldLight else LossRed
        )
        Text(
          text = "${if (isKurdish) "داهات" else "In"}: ${CurrencyService.formatShort(totalInflow, displayCurrency)} • ${if (isKurdish) "مەسروف" else "Out"}: ${CurrencyService.formatShort(totalOutflow, displayCurrency)}",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Card 2: Investment Net P&L & Win Rate
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.weight(1f)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isKurdish) "قازانج/خوسارەت P&L" else "Investments P&L",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Box(
            modifier = Modifier
              .background(
                if (winRatePercent >= 50) EmeraldLight.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f),
                RoundedCornerShape(6.dp)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$winRatePercent% Win",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (winRatePercent >= 50) EmeraldLight else LossRed
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = (if (netInvestPL >= 0) "+ " else "") + CurrencyService.format(netInvestPL, displayCurrency),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Black,
          color = if (netInvestPL >= 0) EmeraldLight else LossRed
        )
        Text(
          text = if (isKurdish) "رێژەیا سەرکەفتنێ: $winRatePercent%" else "Portfolio Success Rate",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

// -------------------------------------------------------------
// RECHARTS / D3-STYLE MONTHLY CASH FLOW BAR CHART
// -------------------------------------------------------------
@Composable
fun MonthlyCashFlowBarChart(
  points: List<MonthlyCashFlowDataPoint>,
  displayCurrency: String,
  isKurdish: Boolean,
  modifier: Modifier = Modifier
) {
  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(points) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(900))
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header & Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "چارتێ داهات و مەسروفێ هەیڤانە" else "Monthly Cash Flow (Recharts/D3)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isKurdish) "بەراوردکرنا داهات بەرامبەر مەسروفی بۆ هەر هەیڤەکێ" else "Grouped Inflow vs Outflow with Net Trend",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Legend Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          LegendDot(label = if (isKurdish) "داهات" else "In", color = EmeraldLight)
          LegendDot(label = if (isKurdish) "مەسروف" else "Out", color = LossRed)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (points.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isKurdish) "چ داتایێن تەحویلاتان بۆ ڤی ماوەی نینن" else "No transactions recorded in this timeframe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val maxVal = points.maxOf { max(it.inflow, it.outflow) }.coerceAtLeast(100.0)

        // Interactive Selected Tooltip Overlay
        val activePoint = selectedIndex?.let { points.getOrNull(it) }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
        ) {
          if (activePoint != null) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${activePoint.fullLabel}:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = "+${CurrencyService.formatShort(activePoint.inflow, displayCurrency)}",
                  color = EmeraldLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
                Text(
                  text = "-${CurrencyService.formatShort(activePoint.outflow, displayCurrency)}",
                  color = LossRed,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
                Text(
                  text = "Net: ${(if (activePoint.net >= 0) "+" else "") + CurrencyService.formatShort(activePoint.net, displayCurrency)}",
                  color = if (activePoint.net >= 0) EmeraldLight else LossRed,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp
                )
              }
            }
          } else {
            Text(
              text = if (isKurdish) "دەست ل سەر هەر کۆڵەکەکێ دانێ بۆ دیتنا ژمارەیان" else "Tap or scrub bars to view detailed monthly metrics",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              modifier = Modifier.align(Alignment.CenterStart)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Custom Canvas Chart with Touch Gestures
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(points) {
              detectTapGestures(
                onTap = { offset ->
                  val step = size.width / points.size
                  val clickedIndex = (offset.x / step).toInt().coerceIn(0, points.size - 1)
                  selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                }
              )
            }
        ) {
          val width = size.width
          val height = size.height
          val paddingBottom = 26f
          val chartHeight = height - paddingBottom
          val count = points.size
          val slotWidth = width / count

          // Draw horizontal grid lines (0%, 50%, 100%)
          val gridLineCount = 3
          for (i in 0..gridLineCount) {
            val y = chartHeight * (i.toFloat() / gridLineCount)
            drawLine(
              color = Color.Gray.copy(alpha = 0.15f),
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 1f
            )
          }

          val barWidth = min(slotWidth * 0.32f, 22f)
          val barGap = 4f

          points.forEachIndexed { index, point ->
            val centerX = (index * slotWidth) + (slotWidth / 2f)
            val isSelected = selectedIndex == index

            // Highlight background if selected
            if (isSelected) {
              drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                topLeft = Offset(index * slotWidth + 4f, 0f),
                size = Size(slotWidth - 8f, chartHeight),
                cornerRadius = CornerRadius(8f, 8f)
              )
            }

            // Inflow Bar (Green/Emerald)
            val inflowHeight = (point.inflow / maxVal).toFloat() * chartHeight * animationProgress.value
            val inTop = chartHeight - inflowHeight
            val inLeft = centerX - barWidth - (barGap / 2f)

            if (inflowHeight > 0f) {
              drawRoundRect(
                brush = Brush.verticalGradient(
                  colors = listOf(
                    EmeraldLight,
                    EmeraldLight.copy(alpha = 0.65f)
                  ),
                  startY = inTop,
                  endY = chartHeight
                ),
                topLeft = Offset(inLeft, inTop),
                size = Size(barWidth, inflowHeight),
                cornerRadius = CornerRadius(6f, 6f)
              )
            }

            // Outflow Bar (Rose/Red)
            val outflowHeight = (point.outflow / maxVal).toFloat() * chartHeight * animationProgress.value
            val outTop = chartHeight - outflowHeight
            val outLeft = centerX + (barGap / 2f)

            if (outflowHeight > 0f) {
              drawRoundRect(
                brush = Brush.verticalGradient(
                  colors = listOf(
                    LossRed,
                    LossRed.copy(alpha = 0.65f)
                  ),
                  startY = outTop,
                  endY = chartHeight
                ),
                topLeft = Offset(outLeft, outTop),
                size = Size(barWidth, outflowHeight),
                cornerRadius = CornerRadius(6f, 6f)
              )
            }

            // Baseline tick
            drawCircle(
              color = if (isSelected) EmeraldLight else Color.Gray.copy(alpha = 0.4f),
              radius = if (isSelected) 3.5f else 2f,
              center = Offset(centerX, chartHeight + 2f)
            )
          }
        }

        // X-Axis Labels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          points.forEachIndexed { idx, pt ->
            Text(
              text = pt.monthLabel,
              fontSize = 10.sp,
              fontWeight = if (selectedIndex == idx) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedIndex == idx) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.clickable { selectedIndex = if (selectedIndex == idx) null else idx }
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// D3 / RECHARTS-STYLE INVESTMENT PROFIT VS LOSS WATERFALL CHART
// -------------------------------------------------------------
@Composable
fun InvestmentProfitLossWaterfallChart(
  points: List<InvestmentPLDataPoint>,
  totalProfits: Double,
  totalLosses: Double,
  displayCurrency: String,
  isKurdish: Boolean,
  modifier: Modifier = Modifier
) {
  var selectedItem by remember { mutableStateOf<InvestmentPLDataPoint?>(null) }
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(points) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(900))
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "قازانج بەرامبەر خوسارەت (Investments P&L)" else "Investment Profits vs Losses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isKurdish) "بەراوردکرنا سەرکەفتن و خوسارەتا هەمی سەیارە و پرۆژەیان" else "Diverging Waterfall (+Gains / -Losses)",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Summary Badges
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .background(EmeraldLight.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "+${CurrencyService.formatShort(totalProfits, displayCurrency)}",
              color = EmeraldLight,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          Box(
            modifier = Modifier
              .background(LossRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "-${CurrencyService.formatShort(totalLosses, displayCurrency)}",
              color = LossRed,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (points.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isKurdish) "چ وەبەرهێنان یان سەیارە نەهاتینە تۆمارکرن" else "No vehicles or investments available to visualize",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Selected Item Tooltip
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
        ) {
          if (selectedItem != null) {
            val item = selectedItem!!
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${item.name} (${item.category}):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
              Text(
                text = "${if (item.profitLoss >= 0) "+" else ""}${CurrencyService.format(item.profitLoss, displayCurrency)} (${item.roiPercent.toInt()}%)",
                color = if (item.isWinning) EmeraldLight else LossRed,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
              )
            }
          } else {
            Text(
              text = if (isKurdish) "کلیک ل سەر هەر بارەکێ بکە بۆ دیتنا هوورکاریێن قازانج یان خوسارەتێ" else "Click on any investment bar to inspect gain/loss and ROI",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              modifier = Modifier.align(Alignment.CenterStart)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Diverging Bar Chart Canvas (Zero baseline in middle)
        val maxAbsVal = points.maxOf { abs(it.profitLoss) }.coerceAtLeast(100.0)

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(points) {
              detectTapGestures(
                onTap = { offset ->
                  val slotW = size.width / points.size
                  val clicked = (offset.x / slotW).toInt().coerceIn(0, points.size - 1)
                  selectedItem = if (selectedItem == points[clicked]) null else points[clicked]
                }
              )
            }
        ) {
          val width = size.width
          val height = size.height
          val count = points.size
          val slotWidth = width / count
          val zeroY = height / 2f // Zero baseline right in center

          // Draw Zero Baseline
          drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(0f, zeroY),
            end = Offset(width, zeroY),
            strokeWidth = 2f
          )

          // Upper and Lower subtle guide lines (+50%, -50%)
          drawLine(
            color = Color.Gray.copy(alpha = 0.12f),
            start = Offset(0f, zeroY / 2f),
            end = Offset(width, zeroY / 2f),
            strokeWidth = 1f
          )
          drawLine(
            color = Color.Gray.copy(alpha = 0.12f),
            start = Offset(0f, zeroY + (zeroY / 2f)),
            end = Offset(width, zeroY + (zeroY / 2f)),
            strokeWidth = 1f
          )

          val barWidth = min(slotWidth * 0.6f, 28f)

          points.forEachIndexed { index, pt ->
            val centerX = (index * slotWidth) + (slotWidth / 2f)
            val isSelected = selectedItem == pt

            // Highlight background if selected
            if (isSelected) {
              drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                topLeft = Offset(index * slotWidth + 2f, 0f),
                size = Size(slotWidth - 4f, height),
                cornerRadius = CornerRadius(6f, 6f)
              )
            }

            val pl = pt.profitLoss
            val barHeight = ((abs(pl) / maxAbsVal).toFloat() * (zeroY - 10f) * animationProgress.value).coerceAtLeast(4f)
            val barLeft = centerX - (barWidth / 2f)

            if (pl >= 0) {
              // Upward Green Bar (Profit)
              val barTop = zeroY - barHeight
              drawRoundRect(
                brush = Brush.verticalGradient(
                  colors = listOf(
                    EmeraldLight,
                    EmeraldLight.copy(alpha = 0.65f)
                  ),
                  startY = barTop,
                  endY = zeroY
                ),
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(5f, 5f)
              )
            } else {
              // Downward Red Bar (Loss)
              drawRoundRect(
                brush = Brush.verticalGradient(
                  colors = listOf(
                    LossRed.copy(alpha = 0.65f),
                    LossRed
                  ),
                  startY = zeroY,
                  endY = zeroY + barHeight
                ),
                topLeft = Offset(barLeft, zeroY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(5f, 5f)
              )
            }

            // Small dot on zero baseline
            drawCircle(
              color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.5f),
              radius = if (isSelected) 3.5f else 2f,
              center = Offset(centerX, zeroY)
            )
          }
        }

        // X-Axis Labels (Names truncated)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          points.forEachIndexed { idx, pt ->
            Text(
              text = pt.name.take(6),
              fontSize = 9.sp,
              fontWeight = if (selectedItem == pt) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedItem == pt) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              modifier = Modifier.clickable { selectedItem = if (selectedItem == pt) null else pt }
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CUMULATIVE PERFORMANCE BEZIER AREA CURVE
// -------------------------------------------------------------
@Composable
fun CumulativePerformanceCurve(
  cashflowPoints: List<MonthlyCashFlowDataPoint>,
  investmentPoints: List<InvestmentPLDataPoint>,
  displayCurrency: String,
  isKurdish: Boolean,
  modifier: Modifier = Modifier
) {
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(cashflowPoints) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(1000))
  }

  // Calculate cumulative net cashflow over time
  val cumulativeData = remember(cashflowPoints) {
    var runningTotal = 0.0
    cashflowPoints.map { pt ->
      runningTotal += pt.net
      pt.monthLabel to runningTotal
    }
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "ئاراستەیا کۆمبوونا کاشی (Cumulative Area)" else "Cumulative Growth Trajectory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isKurdish) "سەرجەمێ پێشکەفتنا دارایی ل دیف دەمی" else "Recharts Area Curve for Overall Trajectory",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        val finalTotal = cumulativeData.lastOrNull()?.second ?: 0.0
        val isPos = finalTotal >= 0
        Box(
          modifier = Modifier
            .background(
              if (isPos) EmeraldLight.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f),
              RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = (if (isPos) "+" else "") + CurrencyService.formatShort(finalTotal, displayCurrency),
            color = if (isPos) EmeraldLight else LossRed,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (cumulativeData.size < 2) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isKurdish) "پێویستە پتر ژ هەیڤەکێ داتایان هەبیت بۆ کێشانا ئاراستەیێ" else "Need multiple periods to generate trajectory curve",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val minVal = cumulativeData.minOf { it.second }.coerceAtMost(0.0)
        val maxVal = cumulativeData.maxOf { it.second }.coerceAtLeast(100.0)
        val range = (maxVal - minVal).coerceAtLeast(1.0)

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
        ) {
          val width = size.width
          val height = size.height
          val padX = 14f
          val padY = 16f
          val chartW = width - (padX * 2)
          val chartH = height - (padY * 2)
          val stepX = chartW / (cumulativeData.size - 1)

          val zeroY = padY + chartH * (1f - ((0.0 - minVal) / range).toFloat())

          // Zero Guide Line
          drawLine(
            color = Color.Gray.copy(alpha = 0.25f),
            start = Offset(padX, zeroY),
            end = Offset(width - padX, zeroY),
            strokeWidth = 1.5f
          )

          val linePath = Path()
          val fillPath = Path()
          val nodePoints = mutableListOf<Offset>()

          cumulativeData.forEachIndexed { i, (_, value) ->
            val x = padX + (i * stepX)
            val normalized = ((value - minVal) / range).toFloat()
            val y = padY + chartH * (1f - (normalized * animationProgress.value))
            nodePoints.add(Offset(x, y))

            if (i == 0) {
              linePath.moveTo(x, y)
              fillPath.moveTo(x, zeroY)
              fillPath.lineTo(x, y)
            } else {
              val prev = nodePoints[i - 1]
              val cx = (prev.x + x) / 2f
              linePath.cubicTo(cx, prev.y, cx, y, x, y)
              fillPath.cubicTo(cx, prev.y, cx, y, x, y)
            }
          }

          fillPath.lineTo(nodePoints.last().x, zeroY)
          fillPath.close()

          // Draw Gradient Area
          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(
                EmeraldLight.copy(alpha = 0.35f),
                EmeraldLight.copy(alpha = 0.05f),
                Color.Transparent
              )
            )
          )

          // Draw Stroke
          drawPath(
            path = linePath,
            color = EmeraldLight,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
          )

          // Draw Nodes
          nodePoints.forEach { pt ->
            drawCircle(color = Color(0xFF0F172A), radius = 6.5f, center = pt)
            drawCircle(color = EmeraldLight, radius = 4f, center = pt)
          }
        }

        // Labels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          cumulativeData.forEach { (lbl, _) ->
            Text(
              text = lbl,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// ROW ITEM HELPER
// -------------------------------------------------------------
@Composable
private fun MonthlyRowItem(
  point: MonthlyCashFlowDataPoint,
  displayCurrency: String,
  isKurdish: Boolean
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(if (point.net >= 0) EmeraldLight else LossRed, CircleShape)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = point.fullLabel,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "+ ${CurrencyService.formatShort(point.inflow, displayCurrency)}",
        color = EmeraldLight,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = "- ${CurrencyService.formatShort(point.outflow, displayCurrency)}",
        color = LossRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = (if (point.net >= 0) "+" else "") + CurrencyService.formatShort(point.net, displayCurrency),
        color = if (point.net >= 0) EmeraldLight else LossRed,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold
      )
    }
  }
}

@Composable
private fun LegendDot(label: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(9.dp)
        .background(color, CircleShape)
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

// -------------------------------------------------------------
// DATA AGGREGATION CALCULATIONS
// -------------------------------------------------------------
private fun calculateMonthlyCashFlow(
  transactions: List<com.example.data.local.TransactionEntity>,
  displayCurrency: String,
  iqdRate: Double,
  horizonMonths: Int
): List<MonthlyCashFlowDataPoint> {
  val cal = Calendar.getInstance()
  val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
  val labelFormat = SimpleDateFormat("MMM", Locale.US)
  val fullFormat = SimpleDateFormat("MMM yyyy", Locale.US)

  // Generate sequence of past N months in chronological order
  val monthsList = mutableListOf<Triple<String, String, String>>()
  for (i in (horizonMonths - 1) downTo 0) {
    val tempCal = Calendar.getInstance()
    tempCal.add(Calendar.MONTH, -i)
    val key = keyFormat.format(tempCal.time)
    val lbl = labelFormat.format(tempCal.time)
    val full = fullFormat.format(tempCal.time)
    monthsList.add(Triple(key, lbl, full))
  }

  // Group transactions by "yyyy-MM"
  val grouped = transactions.groupBy { keyFormat.format(Date(it.dateMillis)) }

  return monthsList.map { (key, label, full) ->
    val list = grouped[key].orEmpty()
    val inflow = list.filter { it.type == "IN" }
      .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate) }
    val outflow = list.filter { it.type == "OUT" }
      .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate) }
    val net = inflow - outflow

    MonthlyCashFlowDataPoint(
      monthKey = key,
      monthLabel = label,
      fullLabel = full,
      inflow = inflow,
      outflow = outflow,
      net = net,
      timestamp = System.currentTimeMillis()
    )
  }
}

private fun calculateInvestmentPL(
  uiState: FinanceUiState
): List<InvestmentPLDataPoint> {
  val displayCurrency = uiState.displayCurrency
  val iqdRate = uiState.iqdRate

  val list = mutableListOf<InvestmentPLDataPoint>()

  // 1. Vehicles (Both sold and active)
  uiState.carInvestments.forEach { car ->
    val initialCap = CurrencyService.convert(car.initialCapital, car.currency, displayCurrency, iqdRate)
    val totalCost = uiState.getCarTotalCost(car)
    val totalRevenue = uiState.getCarTotalRevenue(car)
    val pl = uiState.getCarNetProfitLoss(car)
    val roi = if (totalCost > 0) (pl / totalCost) * 100.0 else 0.0

    list.add(
      InvestmentPLDataPoint(
        id = car.id,
        name = car.name,
        category = "Vehicle",
        initialCapital = totalCost,
        currentValueOrSold = totalRevenue,
        profitLoss = pl,
        roiPercent = roi,
        isSold = car.isSold,
        isWinning = pl >= 0
      )
    )
  }

  // 2. Other Investments (Real Estate, Ventures, etc.)
  uiState.nonCarInvestments.forEach { inv ->
    val initialCap = CurrencyService.convert(inv.initialCapital, inv.currency, displayCurrency, iqdRate)
    val currentVal = CurrencyService.convert(inv.currentValue, inv.currency, displayCurrency, iqdRate)
    val pl = currentVal - initialCap
    val roi = if (initialCap > 0) (pl / initialCap) * 100.0 else 0.0

    list.add(
      InvestmentPLDataPoint(
        id = inv.id,
        name = inv.name,
        category = inv.category,
        initialCapital = initialCap,
        currentValueOrSold = currentVal,
        profitLoss = pl,
        roiPercent = roi,
        isSold = inv.isSold,
        isWinning = pl >= 0
      )
    )
  }

  return list.sortedByDescending { abs(it.profitLoss) }
}
