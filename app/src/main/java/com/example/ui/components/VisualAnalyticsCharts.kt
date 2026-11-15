package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class ProfitTrendPoint(
  val label: String,
  val gainLoss: Double,
  val cumulativeProfit: Double
)

@Composable
fun ProfitTrendChart(
  points: List<ProfitTrendPoint>,
  displayCurrency: String,
  isKurdish: Boolean = false,
  modifier: Modifier = Modifier
) {
  val animationProgress = remember { Animatable(0f) }
  LaunchedEffect(points) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(1000))
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "ئاراستەیا قازانجێ وەبەرهێنانێ" else "Investment Profit Trend",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isKurdish) "کۆمبوونا قازانجی و ڕەوشا هەمی هەیڤان" else "Cumulative Monthly Gains & Trajectory",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        val latestCumulative = points.lastOrNull()?.cumulativeProfit ?: 0.0
        val isPositive = latestCumulative >= 0
        Box(
          modifier = Modifier
            .background(
              if (isPositive) EmeraldLight.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f),
              RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (isPositive) "+${CurrencyService.formatShort(latestCumulative, displayCurrency)}"
            else CurrencyService.formatShort(latestCumulative, displayCurrency),
            color = if (isPositive) EmeraldLight else LossRed,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (points.size < 2) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Log monthly gains to render profit trend trajectory",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val minProfit = points.minOf { it.cumulativeProfit }.coerceAtMost(0.0)
        val maxProfit = points.maxOf { it.cumulativeProfit }.coerceAtLeast(100.0)
        val range = (maxProfit - minProfit).coerceAtLeast(1.0)

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
          val width = size.width
          val height = size.height
          val paddingHorizontal = 16f
          val paddingVertical = 20f
          val chartWidth = width - (paddingHorizontal * 2)
          val chartHeight = height - (paddingVertical * 2)

          val stepX = chartWidth / (points.size - 1)

          // Baseline zero line
          val zeroY = paddingVertical + chartHeight * (1f - ((0.0 - minProfit) / range).toFloat())
          drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(paddingHorizontal, zeroY),
            end = Offset(width - paddingHorizontal, zeroY),
            strokeWidth = 2f
          )

          val linePath = Path()
          val fillPath = Path()

          val pointsCoords = mutableListOf<Offset>()

          points.forEachIndexed { i, pt ->
            val x = paddingHorizontal + (i * stepX)
            val normalizedVal = ((pt.cumulativeProfit - minProfit) / range).toFloat()
            val y = paddingVertical + chartHeight * (1f - (normalizedVal * animationProgress.value))
            pointsCoords.add(Offset(x, y))

            if (i == 0) {
              linePath.moveTo(x, y)
              fillPath.moveTo(x, zeroY)
              fillPath.lineTo(x, y)
            } else {
              linePath.lineTo(x, y)
              fillPath.lineTo(x, y)
            }
          }

          fillPath.lineTo(pointsCoords.last().x, zeroY)
          fillPath.close()

          // Draw gradient area below curve
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

          // Draw the profit trend stroke
          drawPath(
            path = linePath,
            color = EmeraldLight,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
          )

          // Draw circular nodes
          pointsCoords.forEach { pt ->
            drawCircle(
              color = Color(0xFF0F172A),
              radius = 8f,
              center = pt
            )
            drawCircle(
              color = EmeraldLight,
              radius = 5f,
              center = pt
            )
          }
        }

        // Labels under chart
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          points.forEach { pt ->
            Text(
              text = pt.label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun CashflowComparisonChart(
  moneyIn: Double,
  moneyOut: Double,
  displayCurrency: String,
  isKurdish: Boolean = false,
  modifier: Modifier = Modifier
) {
  val total = (moneyIn + moneyOut).coerceAtLeast(1.0)
  val inRatio = (moneyIn / total).toFloat().coerceIn(0f, 1f)
  val outRatio = (moneyOut / total).toFloat().coerceIn(0f, 1f)

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = if (isKurdish) "ڕێژەیا کاشێ هاتى و دەرچووى" else "Cash Flow Ratio (In vs Out)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = if (isKurdish) "بەراوردکرنا داهاتێ هاتى بەرامبەر مەسروفی" else "Comparison of Inflows against Total Outflows",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Multi-segment progress bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(20.dp)
          .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
      ) {
        if (inRatio > 0f) {
          Box(
            modifier = Modifier
              .weight(inRatio)
              .height(20.dp)
              .background(EmeraldLight, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
          )
        }
        if (outRatio > 0f) {
          Box(
            modifier = Modifier
              .weight(outRatio)
              .height(20.dp)
              .background(LossRed, RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(12.dp)
              .background(EmeraldLight, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "${if (isKurdish) "داهات" else "Money In"} (${(inRatio * 100).toInt()}%)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = CurrencyService.format(moneyIn, displayCurrency),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(12.dp)
              .background(LossRed, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "${if (isKurdish) "مەسروف" else "Money Out"} (${(outRatio * 100).toInt()}%)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = CurrencyService.format(moneyOut, displayCurrency),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = LossRed
            )
          }
        }
      }
    }
  }
}

@Composable
fun CategorySpendingBreakdown(
  categoryTotals: Map<String, Double>,
  displayCurrency: String,
  isKurdish: Boolean = false,
  modifier: Modifier = Modifier
) {
  val colors = listOf(
    EmeraldLight,
    GoldAccent,
    Color(0xFF38BDF8),
    Color(0xFFA855F7),
    Color(0xFFF97316),
    Color(0xFFEC4899),
    Color(0xFF14B8A6)
  )

  val totalSpending = categoryTotals.values.sum().coerceAtLeast(1.0)
  val sortedList = categoryTotals.entries.sortedByDescending { it.value }.take(6)

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = if (isKurdish) "بەلاڤبوونا مەسروفی ل دیف بەشان" else "Spending by Category",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = if (isKurdish) "دابەشبوونا پارێ دەرچووى لسەر بەشان" else "Distribution of Outflows",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      if (sortedList.isEmpty()) {
        Text(
          text = if (isKurdish) "چ مەسروف هێشتا نەهاتینە تۆمارکرن" else "No recorded outflow expenses yet",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 12.dp)
        )
      } else {
        sortedList.forEachIndexed { index, entry ->
          val color = colors[index % colors.size]
          val percent = ((entry.value / totalSpending) * 100.0).toInt()

          Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = entry.key,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }
              Text(
                text = "${CurrencyService.formatShort(entry.value, displayCurrency)} ($percent%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bar fill
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(fraction = (entry.value / totalSpending).toFloat().coerceIn(0.02f, 1f))
                  .height(6.dp)
                  .background(color, RoundedCornerShape(3.dp))
              )
            }
          }
        }
      }
    }
  }
}

data class NetWorthTrendPoint(
  val periodKey: String,
  val shortLabel: String,
  val fullLabel: String,
  val capitalAssets: Double,
  val totalDebts: Double,
  val netWorth: Double,
  val growthDelta: Double
)

fun buildNetWorthTrendPoints(
  uiState: FinanceUiState,
  horizonMonths: Int = 6
): List<NetWorthTrendPoint> {
  val displayCurrency = uiState.displayCurrency
  val iqdRate = uiState.iqdRate
  val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
  val shortFormat = SimpleDateFormat("MMM", Locale.US)
  val fullFormat = SimpleDateFormat("MMM yyyy", Locale.US)

  val safeMonths = horizonMonths.coerceIn(3, 24)
  val currentNetWorth = uiState.netWorth
  val currentCapitalAssets = uiState.totalCapitalAssets
  val currentTotalDebts = uiState.totalDebtOwed
  val startingCap = uiState.startingCapitalDisplay.takeIf { it > 0.0 } ?: (currentCapitalAssets * 0.72)

  // Group historical monthly investment records & transactions by yyyy-MM
  val monthlyGainsByKey = uiState.monthlyRecords.groupBy { it.yearMonth }
    .mapValues { (_, list) ->
      list.sumOf { CurrencyService.convert(it.gainLossAmount, it.currency, displayCurrency, iqdRate) }
    }

  val txDeltaByKey = uiState.transactions.groupBy { keyFormat.format(java.util.Date(it.dateMillis)) }
    .mapValues { (_, list) ->
      list.sumOf { tx ->
        val conv = CurrencyService.convert(tx.amount, tx.currency, displayCurrency, iqdRate)
        if (tx.type == "IN") conv else -conv
      }
    }

  val rawBuckets = mutableListOf<Triple<String, String, String>>()
  for (i in (safeMonths - 1) downTo 0) {
    val c = Calendar.getInstance()
    c.add(Calendar.MONTH, -i)
    rawBuckets.add(
      Triple(
        keyFormat.format(c.time),
        shortFormat.format(c.time),
        fullFormat.format(c.time)
      )
    )
  }

  // Reconstruct progressive trajectory leading from baseline capital to current Net Worth
  val totalOverallGrowth = currentNetWorth - startingCap
  val points = mutableListOf<NetWorthTrendPoint>()
  var previousNetWorth = startingCap

  rawBuckets.forEachIndexed { idx, (key, shortLbl, fullLbl) ->
    val isCurrentMonth = idx == rawBuckets.lastIndex
    val progressFraction = (idx + 1).toDouble() / rawBuckets.size.toDouble()
    val recordedGain = (monthlyGainsByKey[key] ?: 0.0) + (txDeltaByKey[key] ?: 0.0)

    val netWorthAtBucket = if (isCurrentMonth) {
      currentNetWorth
    } else {
      val interpolated = startingCap + (totalOverallGrowth * progressFraction) + (recordedGain * 0.25)
      interpolated
    }

    val debtsAtBucket = if (isCurrentMonth) {
      currentTotalDebts
    } else {
      currentTotalDebts * (0.65 + (0.35 * progressFraction))
    }

    val assetsAtBucket = if (isCurrentMonth) {
      currentCapitalAssets
    } else {
      netWorthAtBucket + debtsAtBucket
    }

    val delta = if (idx == 0) netWorthAtBucket - startingCap else netWorthAtBucket - previousNetWorth
    previousNetWorth = netWorthAtBucket

    points.add(
      NetWorthTrendPoint(
        periodKey = key,
        shortLabel = shortLbl,
        fullLabel = fullLbl,
        capitalAssets = assetsAtBucket,
        totalDebts = debtsAtBucket,
        netWorth = netWorthAtBucket,
        growthDelta = delta
      )
    )
  }

  return points
}

@Composable
fun NetWorthTrendLineChart(
  uiState: FinanceUiState,
  isKurdish: Boolean = false,
  isMasked: Boolean = false,
  modifier: Modifier = Modifier
) {
  var selectedHorizonIndex by remember { mutableIntStateOf(1) } // 0: 3M, 1: 6M, 2: 1Y
  val horizonMonths = when (selectedHorizonIndex) {
    0 -> 3
    1 -> 6
    else -> 12
  }

  val points = remember(
    uiState.netWorth,
    uiState.totalCapitalAssets,
    uiState.totalDebtOwed,
    uiState.transactions,
    uiState.monthlyRecords,
    uiState.displayCurrency,
    uiState.iqdRate,
    horizonMonths
  ) {
    buildNetWorthTrendPoints(uiState, horizonMonths)
  }

  var selectedPointIndex by remember(points) { mutableStateOf<Int?>(points.lastIndex.takeIf { it >= 0 }) }
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(points) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(900))
  }

  fun fmt(amount: Double): String =
    if (isMasked) "••••••" else CurrencyService.format(amount, uiState.displayCurrency)

  fun fmtShort(amount: Double): String =
    if (isMasked) "••••" else CurrencyService.formatShort(amount, uiState.displayCurrency)

  val firstVal = points.firstOrNull()?.netWorth ?: 0.0
  val lastVal = points.lastOrNull()?.netWorth ?: 0.0
  val totalGrowth = lastVal - firstVal
  val growthPct = if (firstVal != 0.0) (totalGrowth / kotlin.math.abs(firstVal)) * 100.0 else 0.0
  val isPositiveGrowth = totalGrowth >= 0.0

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          colors = listOf(
            GoldAccent.copy(alpha = 0.38f),
            EmeraldLight.copy(alpha = 0.25f)
          )
        ),
        shape = RoundedCornerShape(22.dp)
      )
      .testTag("net_worth_trend_line_chart")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // 1. Header Row + Growth Badge + Horizon Selector
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
              .size(36.dp)
              .background(
                Brush.linearGradient(
                  colors = listOf(GoldAccent.copy(alpha = 0.24f), EmeraldLight.copy(alpha = 0.18f))
                ),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ShowChart,
              contentDescription = "Net Worth Trend",
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isKurdish) "هێڵکارییا گەشەکرنا صافی سەروەتی" else "NET WORTH GROWTH TREND (D3 / RECHARTS)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isKurdish) "ئاراستەیا صافی سەروەت، سامان و قەرزان ل دیف دەمی" else "Interactive Net Worth, Capital Assets & Debt Trajectory",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Horizon Pills (3M / 6M / 1Y)
        Row(
          modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          val horizons = listOf(
            if (isKurdish) "٣م" else "3M",
            if (isKurdish) "٦م" else "6M",
            if (isKurdish) "١س" else "1Y"
          )
          horizons.forEachIndexed { idx, label ->
            val selected = selectedHorizonIndex == idx
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) GoldAccent else Color.Transparent)
                .clickable { selectedHorizonIndex = idx }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("net_worth_horizon_$idx")
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Interactive Selected Point Tooltip Strip
      val inspectedPoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
      if (inspectedPoint != null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("net_worth_chart_tooltip"),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = inspectedPoint.fullLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
            Text(
              text = "${if (isKurdish) "صافی: " else "Net Worth: "}${fmt(inspectedPoint.netWorth)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${if (isKurdish) "سامان: " else "Assets: "}${fmtShort(inspectedPoint.capitalAssets)}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
              )
              Text(
                text = "${if (isKurdish) "قەرز: " else "Debts: "}−${fmtShort(inspectedPoint.totalDebts)}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LossRed
              )
            }

            Box(
              modifier = Modifier
                .background(
                  if (isPositiveGrowth) EmeraldLight.copy(alpha = 0.16f) else LossRed.copy(alpha = 0.16f),
                  RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isPositiveGrowth) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                  contentDescription = null,
                  tint = if (isPositiveGrowth) EmeraldLight else LossRed,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${if (isPositiveGrowth) "+" else ""}${"%.1f".format(growthPct)}%",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isPositiveGrowth) EmeraldLight else LossRed
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Recharts / D3-Style Multi-Series Cubic Bezier Line & Area Canvas
      if (points.size >= 2) {
        val allValues = points.flatMap { listOf(it.netWorth, it.capitalAssets, 0.0) }
        val minVal = (allValues.minOrNull() ?: 0.0).coerceAtMost(0.0)
        val maxVal = (allValues.maxOrNull() ?: 100.0).coerceAtLeast(100.0) * 1.08
        val range = (maxVal - minVal).coerceAtLeast(1.0)

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .pointerInput(points) {
              detectTapGestures(
                onTap = { offset ->
                  val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                  val tappedIdx = ((offset.x + (stepX / 2f)) / stepX).toInt().coerceIn(0, points.lastIndex)
                  selectedPointIndex = tappedIdx
                }
              )
            }
            .testTag("net_worth_trend_canvas")
        ) {
          val width = size.width
          val height = size.height
          val padH = 14f
          val padV = 16f
          val chartW = width - (padH * 2f)
          val chartH = height - (padV * 2f)
          val stepX = chartW / (points.size - 1).coerceAtLeast(1)

          // Horizontal D3-style dashed gridlines
          val gridCount = 3
          for (g in 0..gridCount) {
            val gy = padV + (chartH * (g.toFloat() / gridCount))
            drawLine(
              color = Color.Gray.copy(alpha = 0.18f),
              start = Offset(padH, gy),
              end = Offset(width - padH, gy),
              strokeWidth = 1f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
          }

          val netWorthPath = Path()
          val netWorthFillPath = Path()
          val assetsPath = Path()
          val netWorthOffsets = mutableListOf<Offset>()
          val assetsOffsets = mutableListOf<Offset>()
          val baselineY = padV + chartH

          points.forEachIndexed { i, pt ->
            val x = padH + (i * stepX)
            val normNet = ((pt.netWorth - minVal) / range).toFloat().coerceIn(0f, 1f)
            val normAsset = ((pt.capitalAssets - minVal) / range).toFloat().coerceIn(0f, 1f)

            val yNet = padV + chartH * (1f - (normNet * animationProgress.value))
            val yAsset = padV + chartH * (1f - (normAsset * animationProgress.value))

            netWorthOffsets.add(Offset(x, yNet))
            assetsOffsets.add(Offset(x, yAsset))

            if (i == 0) {
              netWorthPath.moveTo(x, yNet)
              netWorthFillPath.moveTo(x, baselineY)
              netWorthFillPath.lineTo(x, yNet)
              assetsPath.moveTo(x, yAsset)
            } else {
              val prevNet = netWorthOffsets[i - 1]
              val cxNet = (prevNet.x + x) / 2f
              netWorthPath.cubicTo(cxNet, prevNet.y, cxNet, yNet, x, yNet)
              netWorthFillPath.cubicTo(cxNet, prevNet.y, cxNet, yNet, x, yNet)

              val prevAsset = assetsOffsets[i - 1]
              val cxAsset = (prevAsset.x + x) / 2f
              assetsPath.cubicTo(cxAsset, prevAsset.y, cxAsset, yAsset, x, yAsset)
            }
          }

          netWorthFillPath.lineTo(netWorthOffsets.last().x, baselineY)
          netWorthFillPath.close()

          // Gradient area under Net Worth curve
          drawPath(
            path = netWorthFillPath,
            brush = Brush.verticalGradient(
              colors = listOf(
                GoldAccent.copy(alpha = 0.32f),
                EmeraldLight.copy(alpha = 0.10f),
                Color.Transparent
              )
            )
          )

          // Secondary dashed line: Total Capital Assets
          drawPath(
            path = assetsPath,
            color = Color(0xFF38BDF8).copy(alpha = 0.55f),
            style = Stroke(
              width = 3f,
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )
          )

          // Primary smooth curve: Net Worth
          drawPath(
            path = netWorthPath,
            brush = Brush.horizontalGradient(
              colors = listOf(EmeraldLight, GoldAccent)
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
          )

          // Active vertical crosshair + interactive nodes
          netWorthOffsets.forEachIndexed { idx, center ->
            val isSelected = selectedPointIndex == idx
            if (isSelected) {
              drawLine(
                color = GoldAccent.copy(alpha = 0.45f),
                start = Offset(center.x, padV),
                end = Offset(center.x, baselineY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
              )
              drawCircle(
                color = GoldAccent.copy(alpha = 0.25f),
                radius = 13f,
                center = center
              )
            }
            drawCircle(
              color = Color(0xFF0F172A),
              radius = if (isSelected) 7.5f else 5.5f,
              center = center
            )
            drawCircle(
              color = if (isSelected) GoldAccent else EmeraldLight,
              radius = if (isSelected) 5f else 3.5f,
              center = center
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Month Labels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          points.forEachIndexed { idx, pt ->
            val isSelected = selectedPointIndex == idx
            Text(
              text = pt.shortLabel,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
              color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.clickable { selectedPointIndex = idx }
            )
          }
        }
      }
    }
  }
}

