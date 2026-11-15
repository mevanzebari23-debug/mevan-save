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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.history.ActionSection
import com.example.ui.intelligence.AnomalyAlert
import com.example.ui.intelligence.BudgetRecommendation
import com.example.ui.intelligence.DebtPayoffStep
import com.example.ui.intelligence.DebtPayoffStrategy
import com.example.ui.intelligence.InsightSeverity
import com.example.ui.intelligence.MlCategoryForecast
import com.example.ui.intelligence.MlMonthlyProjectionPoint
import kotlin.math.max

@Composable
fun SectionQuickHistoryStrip(
  section: ActionSection,
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val isKurdish = uiState.settings.language == "KU"
  val latestForSection = remember(uiState.actionHistory, section) {
    uiState.actionHistory.firstOrNull { it.section == section } ?: uiState.actionHistory.firstOrNull()
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { viewModel.openHistorySheet() }
      .border(
        width = 1.2.dp,
        color = Color(0xFF38BDF8).copy(alpha = 0.50f),
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("section_quick_history_${section.name.lowercase()}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1726))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = null,
          tint = Color(0xFF38BDF8),
          modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (latestForSection != null) {
              latestForSection.title(isKurdish)
            } else {
              if (isKurdish) "مێژوویا گوهۆڕینان و پێشبینینا بلەز" else "Quick Preview & Change History"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (latestForSection != null) {
              latestForSection.subtitle(isKurdish)
            } else {
              if (isKurdish) "هەر گوهۆڕینەکێ ب ئێک کلیک بزڤڕینە یان دەستکاری بکە" else "Undo or preview any change in 1 tap"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (latestForSection != null) {
          if (latestForSection.canQuickEdit) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF59E0B).copy(alpha = 0.16f),
              modifier = Modifier.clickable {
                viewModel.openQuickEditFromAction(latestForSection)
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = if (isKurdish) "دەستکاری" else "Edit",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFDE68A)
                )
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF10B981).copy(alpha = 0.2f),
            modifier = Modifier.clickable {
              viewModel.undoAction(latestForSection.id)
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = if (isKurdish) "زڤڕاندن" else "Undo",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
              )
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF38BDF8).copy(alpha = 0.16f),
          modifier = Modifier.clickable { viewModel.openHistorySheet() }
        ) {
          Text(
            text = if (isKurdish) "مێژوو (${uiState.actionHistory.size})" else "History (${uiState.actionHistory.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
          )
        }
      }
    }
  }
}

@Composable
fun SmartFinancialIntelligenceHubCard(
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  initialTab: Int = 0,
  modifier: Modifier = Modifier
) {
  val isKurdish = uiState.settings.language == "KU"
  val summary = uiState.smartIntelligence
  var selectedSubTab by remember { mutableIntStateOf(initialTab) }

  val scoreColor = when {
    summary.healthScore >= 85 -> Color(0xFF10B981)
    summary.healthScore >= 70 -> Color(0xFF38BDF8)
    summary.healthScore >= 50 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        brush = Brush.horizontalGradient(
          listOf(
            Color(0xFFF59E0B).copy(alpha = 0.6f),
            Color(0xFF10B981).copy(alpha = 0.5f),
            Color(0xFF38BDF8).copy(alpha = 0.6f)
          )
        ),
        shape = RoundedCornerShape(20.dp)
      )
      .testTag("smart_intelligence_hub_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF111826))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Top Title & Financial Health Score Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFFF59E0B).copy(alpha = 0.25f), Color(0xFF10B981).copy(alpha = 0.25f))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(22.dp)
            )
          }
          Column {
            Text(
              text = if (isKurdish) "🧠 ناڤەندا زیرەکیا دارایی و پێشبینین" else "🧠 Smart Wealth Intelligence & Optimizer",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = if (isKurdish)
                "ئاگەهداریێن زیرەک • پێشبینیا ٣٠ ڕۆژان • بودجەیا ئۆتۆماتیکی • پلانا قەرزان"
              else
                "Anomaly Alerts • 30-Day Forecast • 1-Tap Auto-Budgets • Debt Payoff Plan",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = scoreColor.copy(alpha = 0.16f),
          modifier = Modifier.border(1.dp, scoreColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "${summary.healthScore}/100",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              color = scoreColor
            )
            Text(
              text = if (isKurdish) summary.healthStatusKu else summary.healthStatusEn,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = scoreColor
            )
          }
        }
      }

      // 4 Interactive Sub-Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val tabs = listOf(
          0 to (if (isKurdish) "🚨 ئاگەهداری (${summary.anomalyAlerts.size})" else "🚨 Insights (${summary.anomalyAlerts.size})"),
          1 to (if (isKurdish) "🤖 پێشبینیا ML (١-٦ هەیڤ)" else "🤖 ML Forecast (1-6M)"),
          2 to (if (isKurdish) "🎯 بودجەیا زیرەک (${summary.budgetRecommendations.size})" else "🎯 Auto-Budgets (${summary.budgetRecommendations.size})"),
          3 to (if (isKurdish) "🤝 ئۆپتیمایزەرێ قەرزان" else "🤝 Debt Optimizer")
        )
        tabs.forEach { (idx, label) ->
          val isSelected = selectedSubTab == idx
          FilterChip(
            selected = isSelected,
            onClick = { selectedSubTab = idx },
            label = {
              Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 12.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.22f),
              selectedLabelColor = Color(0xFFFDE68A)
            ),
            modifier = Modifier.testTag("smart_hub_tab_$idx")
          )
        }
      }

      HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

      when (selectedSubTab) {
        0 -> AnomalyAlertsSection(
          alerts = summary.anomalyAlerts,
          isKurdish = isKurdish,
          onSelectAlert = { alert ->
            if (alert.entityType != null && alert.entityId != null) {
              viewModel.openSearchResultItem(alert.targetTab, alert.entityType, alert.entityId)
            } else {
              viewModel.setTab(alert.targetTab)
            }
          }
        )
        1 -> Forecast30DaySection(
          uiState = uiState,
          viewModel = viewModel,
          isKurdish = isKurdish
        )
        2 -> AutoBudgetRecommendationsSection(
          recommendations = summary.budgetRecommendations,
          isKurdish = isKurdish,
          displayCurrency = uiState.displayCurrency,
          onApplySingle = { viewModel.applyRecommendedBudget(it) },
          onApplyAll = { viewModel.applyAllRecommendedBudgets() }
        )
        3 -> DebtPayoffOptimizerSection(
          uiState = uiState,
          viewModel = viewModel,
          isKurdish = isKurdish
        )
      }
    }
  }
}

@Composable
private fun AnomalyAlertsSection(
  alerts: List<AnomalyAlert>,
  isKurdish: Boolean,
  onSelectAlert: (AnomalyAlert) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    alerts.take(5).forEach { alert ->
      val (accentColor, icon) = when (alert.severity) {
        InsightSeverity.CRITICAL -> Color(0xFFEF4444) to Icons.Default.Warning
        InsightSeverity.WARNING -> Color(0xFFF59E0B) to Icons.Default.NotificationsActive
        InsightSeverity.OPPORTUNITY -> Color(0xFF10B981) to Icons.Default.TrendingUp
        InsightSeverity.INFO -> Color(0xFF38BDF8) to Icons.Default.Info
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectAlert(alert) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF182235))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = alert.title(isKurdish),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = accentColor.copy(alpha = 0.16f)
            ) {
              Text(
                text = alert.metricBadge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = alert.description(isKurdish),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) alert.section.labelKu else alert.section.labelEn,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = "${alert.actionLabel(isKurdish)} →",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = accentColor
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Forecast30DaySection(
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean
) {
  val forecast = uiState.smartIntelligence.forecast30Day
  val mlReport = uiState.smartIntelligence.mlForecastReport
  val cur = uiState.displayCurrency
  val isNetPos = forecast.net30DayProjectedFlow >= 0.0
  var selectedHorizonMonths by remember { mutableIntStateOf(3) }

  val horizonTotalBudget = mlReport.horizonTotalBudget(selectedHorizonMonths)
  val horizonMonthlyAvg = mlReport.horizonMonthlyAvgBudget(selectedHorizonMonths)
  val horizonCashTotal = mlReport.horizonCashBudget(selectedHorizonMonths)
  val horizonSchoolTotal = mlReport.horizonUnifiedSchoolBudget(selectedHorizonMonths)
  val horizonLower = mlReport.horizonLowerBound(selectedHorizonMonths)
  val horizonUpper = mlReport.horizonUpperBound(selectedHorizonMonths)

  val trendSign = if (mlReport.combinedTrendPercent >= 0.0) "+" else ""
  val trendColor = when {
    mlReport.combinedTrendPercent > 8.0 -> Color(0xFFF59E0B)
    mlReport.combinedTrendPercent < -3.0 -> Color(0xFF10B981)
    else -> Color(0xFF38BDF8)
  }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // 1. Horizon Selector Bar (1M / 3M / 6M) + ML Confidence Badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val horizons = listOf(
          1 to (if (isKurdish) "١ هەیڤ" else "1 Month"),
          3 to (if (isKurdish) "٣ هەیڤ" else "3 Months"),
          6 to (if (isKurdish) "٦ هەیڤ" else "6 Months")
        )
        horizons.forEach { (m, label) ->
          val active = selectedHorizonMonths == m
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (active) Color(0xFF38BDF8).copy(alpha = 0.22f) else Color(0xFF172033),
            modifier = Modifier
              .border(
                width = if (active) 1.2.dp else 0.8.dp,
                color = if (active) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
              )
              .clickable { selectedHorizonMonths = m }
              .testTag("ml_forecast_horizon_$m")
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
              color = if (active) Color(0xFF38BDF8) else Color(0xFF94A3B8),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF10B981).copy(alpha = 0.16f),
        modifier = Modifier
          .border(0.8.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
          .testTag("ml_forecast_confidence_badge")
      ) {
        Text(
          text = if (isKurdish) {
            "دڵنیایی ML: ${mlReport.modelConfidencePercent}%"
          } else {
            "ML Confidence: ${mlReport.modelConfidencePercent}%"
          },
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF10B981),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
      }
    }

    // 2. ML Ensemble Budget Projection Hero Card (OLS Regression + Holt Smoothing)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          brush = Brush.horizontalGradient(
            listOf(
              Color(0xFF38BDF8).copy(alpha = 0.5f),
              Color(0xFFF59E0B).copy(alpha = 0.5f)
            )
          ),
          shape = RoundedCornerShape(16.dp)
        )
        .testTag("ml_forecast_hero_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF162135))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isKurdish) {
                "🤖 پێدڤیا بودجەیا پێشبینیکری ($selectedHorizonMonths هەیڤ)"
              } else {
                "🤖 Projected Budget Requirement (${selectedHorizonMonths}M)"
              },
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8),
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = CurrencyService.format(horizonTotalBudget, cur),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = Color.White,
              modifier = Modifier.testTag("ml_forecast_horizon_total_text")
            )
            Text(
              text = if (isKurdish) {
                "تێکڕایا هەیڤانە: ${CurrencyService.format(horizonMonthlyAvg, cur)}/هەیڤ"
              } else {
                "Monthly Avg: ${CurrencyService.format(horizonMonthlyAvg, cur)}/mo"
              },
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFFDE68A),
              fontWeight = FontWeight.Bold
            )
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = trendColor.copy(alpha = 0.16f)
          ) {
            Text(
              text = if (isKurdish) {
                "ئاراستە: $trendSign${"%.1f".format(mlReport.combinedTrendPercent)}%"
              } else {
                "Trend: $trendSign${"%.1f".format(mlReport.combinedTrendPercent)}%"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = trendColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
          }
        }

        // Split Pill: Cash General Spending vs Unified School Spending
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
              .border(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
              .padding(10.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = if (isKurdish) "💸 بودجەیا مەسروفاتێن کاشی" else "💸 Cash Spending Forecast",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDE68A)
              )
              Text(
                text = CurrencyService.format(horizonCashTotal, cur),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Text(
                text = if (isKurdish) {
                  "هەیڤا بهێت: ${CurrencyService.format(mlReport.nextMonthCashForecastDisplay, cur)}"
                } else {
                  "Next mo: ${CurrencyService.format(mlReport.nextMonthCashForecastDisplay, cur)}"
                },
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF38BDF8).copy(alpha = 0.14f))
              .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
              .padding(10.dp)
              .testTag("ml_forecast_unified_school_box")
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = if (isKurdish) "🎓 قوتابخانە (ئێکگرتی)" else "🎓 Unified School Forecast",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
              )
              Text(
                text = CurrencyService.format(horizonSchoolTotal, cur),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Text(
                text = if (isKurdish) {
                  "هەیڤا بهێت: ${CurrencyService.format(mlReport.nextMonthUnifiedSchoolForecastDisplay, cur)}"
                } else {
                  "Next mo: ${CurrencyService.format(mlReport.nextMonthUnifiedSchoolForecastDisplay, cur)}"
                },
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }
        }

        // Confidence Band & ML Algorithm Footnote
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isKurdish) "مەودایێ دڵنیاییێ (٨٥%):" else "85% Confidence Interval:",
            fontSize = 10.5.sp,
            color = Color(0xFF94A3B8)
          )
          Text(
            text = "${CurrencyService.formatShort(horizonLower, cur)} – ${CurrencyService.formatShort(horizonUpper, cur)}",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFCBD5E1)
          )
        }

        Text(
          text = if (isKurdish) mlReport.modelInsightKu else mlReport.modelInsightEn,
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.5.sp,
          color = Color(0xFFCBD5E1)
        )
      }
    }

    // 3. Multi-Month Trajectory Breakdown (1 to Selected Horizon Months)
    val visibleProjections = mlReport.projectionsForHorizon(selectedHorizonMonths)
    if (visibleProjections.isNotEmpty()) {
      val maxMonthlyTotal = max(visibleProjections.maxOf { it.totalProjectedBudgetDisplay }, 1.0)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ml_forecast_trajectory_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF172234))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) {
                "📊 نەخشەیا هەیڤانە یا بودجەیێ (کاش + قوتابخانە)"
              } else {
                "📊 Monthly Budget Trajectory (Cash + Unified School)"
              },
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = if (isKurdish) "OLS + Holt ML" else "OLS + Holt ML",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }

          visibleProjections.forEach { point ->
            MlMonthlyTrajectoryRow(
              point = point,
              maxTotal = maxMonthlyTotal,
              displayCurrency = cur,
              isKurdish = isKurdish
            )
          }
        }
      }
    }

    // 4. Category-Level ML Budget Projections with 1-Tap Apply
    if (mlReport.categoryForecasts.isNotEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ml_forecast_categories_section"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF172234))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val unalignedMlCats = mlReport.categoryForecasts.filter { !it.isAlreadyAligned }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isKurdish) {
                  "🎯 پێشبینیا بودجەیا پۆلان ب فێربوونا ئامێری"
                } else {
                  "🎯 ML Category & Unified School Budget Targets"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Text(
                text = if (isKurdish) {
                  "ب ئێک کلیک بودجەیا هەیڤا بهێت ل سەر بنەمایێ پێشبینیا ML دابنێ"
                } else {
                  "1-tap apply ML-projected monthly limits to your active budgets"
                },
                fontSize = 10.5.sp,
                color = Color(0xFF94A3B8)
              )
            }

            if (unalignedMlCats.isNotEmpty()) {
              Button(
                onClick = {
                  unalignedMlCats.forEach { catForecast ->
                    viewModel.applyRecommendedBudget(catForecast.toBudgetRecommendation())
                  }
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF38BDF8),
                  contentColor = Color(0xFF041322)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                modifier = Modifier
                  .height(32.dp)
                  .testTag("ml_forecast_apply_all_budgets_btn")
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isKurdish) "دانانا هەمیان (${unalignedMlCats.size})" else "Apply All (${unalignedMlCats.size})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }

          mlReport.categoryForecasts.forEach { catForecast ->
            MlCategoryForecastRow(
              catForecast = catForecast,
              displayCurrency = cur,
              isKurdish = isKurdish,
              onApply = {
                viewModel.applyRecommendedBudget(catForecast.toBudgetRecommendation())
              }
            )
          }
        }
      }
    }

    // 5. 30-Day Pure Cash Trajectory & Liquidity Runway Summary
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF172234))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isKurdish) "پێشبینیا کاشێ صافی پاشی ٣٠ ڕۆژان" else "Projected Pure Cash in 30 Days",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = CurrencyService.format(forecast.projectedMonthEndCash, cur),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = if (forecast.projectedMonthEndCash >= 0.0) Color(0xFF10B981) else Color(0xFFEF4444)
            )
          }
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = (if (isNetPos) Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.16f)
          ) {
            Text(
              text = "${if (isNetPos) "+" else ""}${CurrencyService.format(forecast.net30DayProjectedFlow, cur)}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = if (isNetPos) Color(0xFF10B981) else Color(0xFFEF4444),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }

        Text(
          text = if (isKurdish) forecast.summaryKu else forecast.summaryEn,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFCBD5E1)
        )
      }
    }

    // 4-Metric Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ForecastMetricTile(
        title = if (isKurdish) "پێشبینیا داهاتی (٣٠ ڕۆژ)" else "30d Projected Inflow",
        value = CurrencyService.format(forecast.projected30DayIncome, cur),
        sub = if (isKurdish) "ڕۆژانە: ${CurrencyService.format(forecast.dailyInflowAvg, cur)}" else "Daily: ${CurrencyService.format(forecast.dailyInflowAvg, cur)}",
        accentColor = Color(0xFF10B981),
        modifier = Modifier.weight(1f)
      )
      ForecastMetricTile(
        title = if (isKurdish) "پێشبینیا مەسروفاتان" else "30d General Burn",
        value = CurrencyService.format(forecast.projected30DayGeneralExpense, cur),
        sub = if (isKurdish) "ڕۆژانە: ${CurrencyService.format(forecast.dailyGeneralBurnAvg, cur)}" else "Daily: ${CurrencyService.format(forecast.dailyGeneralBurnAvg, cur)}",
        accentColor = Color(0xFFEF4444),
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ForecastMetricTile(
        title = if (isKurdish) "🎓 پێشبینیا قوتابخانێ" else "🎓 30d School Forecast",
        value = CurrencyService.format(forecast.projected30DaySchoolExpense, cur),
        sub = if (isKurdish) "ڕۆژانە: ${CurrencyService.format(forecast.dailySchoolBurnAvg, cur)}" else "Daily: ${CurrencyService.format(forecast.dailySchoolBurnAvg, cur)}",
        accentColor = Color(0xFF38BDF8),
        modifier = Modifier.weight(1f)
      )
      ForecastMetricTile(
        title = if (isKurdish) "🛡️ بەرگەگرتنا کاشی (Runway)" else "🛡️ Cash Runway",
        value = if (forecast.cashRunwayDays >= 365) (if (isKurdish) "+٣٦٥ ڕۆژ" else "365+ Days") else (if (isKurdish) "${forecast.cashRunwayDays} ڕۆژ" else "${forecast.cashRunwayDays} Days"),
        sub = if (isKurdish) "ڕێژەیا پاشەکەفتێ: ${forecast.savingsRatePercent.toInt()}%" else "Savings rate: ${forecast.savingsRatePercent.toInt()}%",
        accentColor = Color(0xFFF59E0B),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun MlMonthlyTrajectoryRow(
  point: MlMonthlyProjectionPoint,
  maxTotal: Double,
  displayCurrency: String,
  isKurdish: Boolean
) {
  val totalRatio = (point.totalProjectedBudgetDisplay / maxTotal).toFloat().coerceIn(0.08f, 1f)
  val schoolShare = if (point.totalProjectedBudgetDisplay > 0.0) {
    (point.projectedUnifiedSchoolExpenseDisplay / point.totalProjectedBudgetDisplay).toFloat().coerceIn(0f, 1f)
  } else 0f
  val cashShare = (1f - schoolShare).coerceIn(0f, 1f)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF111927))
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = point.monthLabel(isKurdish),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isKurdish) {
            "کاش: ${CurrencyService.formatShort(point.projectedCashExpenseDisplay, displayCurrency)} • قوتابخانە: ${CurrencyService.formatShort(point.projectedUnifiedSchoolExpenseDisplay, displayCurrency)}"
          } else {
            "Cash: ${CurrencyService.formatShort(point.projectedCashExpenseDisplay, displayCurrency)} • School: ${CurrencyService.formatShort(point.projectedUnifiedSchoolExpenseDisplay, displayCurrency)}"
          },
          fontSize = 10.sp,
          color = Color(0xFF94A3B8)
        )
        Text(
          text = CurrencyService.format(point.totalProjectedBudgetDisplay, displayCurrency),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Black,
          color = Color(0xFFFDE68A)
        )
      }
    }

    // Stacked Cash + Unified School Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color.White.copy(alpha = 0.06f))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth(totalRatio)
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
      ) {
        if (cashShare > 0.01f) {
          Box(
            modifier = Modifier
              .weight(cashShare)
              .height(8.dp)
              .background(Color(0xFFF59E0B))
          )
        }
        if (schoolShare > 0.01f) {
          Box(
            modifier = Modifier
              .weight(schoolShare)
              .height(8.dp)
              .background(Color(0xFF38BDF8))
          )
        }
      }
    }
  }
}

@Composable
private fun MlCategoryForecastRow(
  catForecast: MlCategoryForecast,
  displayCurrency: String,
  isKurdish: Boolean,
  onApply: () -> Unit
) {
  val accentColor = if (catForecast.isUnifiedSchool) Color(0xFF38BDF8) else Color(0xFFF59E0B)
  val trendSign = if (catForecast.trendSlopePercent >= 0) "+" else ""

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF111927))
      .border(0.8.dp, accentColor.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
      .padding(10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = catForecast.label(isKurdish),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = accentColor.copy(alpha = 0.16f)
        ) {
          Text(
            text = "$trendSign${"%.1f".format(catForecast.trendSlopePercent)}%",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Text(
        text = if (isKurdish) {
          "پێشبینیا ML: ${CurrencyService.format(catForecast.mlProjectedMonthlyDisplay, displayCurrency)} • سنوورێ پێشنیارکری: ${CurrencyService.format(catForecast.recommendedBudgetCapDisplay, displayCurrency)}"
        } else {
          "ML Forecast: ${CurrencyService.format(catForecast.mlProjectedMonthlyDisplay, displayCurrency)} • Target Cap: ${CurrencyService.format(catForecast.recommendedBudgetCapDisplay, displayCurrency)}"
        },
        fontSize = 10.5.sp,
        color = Color(0xFFCBD5E1)
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    if (catForecast.isAlreadyAligned) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF10B981).copy(alpha = 0.16f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = if (isKurdish) "چالاکە" else "Aligned",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF10B981)
          )
        }
      }
    } else {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.2f),
        modifier = Modifier
          .clickable { onApply() }
          .testTag("ml_forecast_apply_cat_${catForecast.category}")
      ) {
        Text(
          text = if (isKurdish) "دانانا سنووری" else "Set ML Cap",
          fontSize = 10.5.sp,
          fontWeight = FontWeight.ExtraBold,
          color = accentColor,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun ForecastMetricTile(
  title: String,
  value: String,
  sub: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF172033))
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF94A3B8),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = accentColor
      )
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = Color(0xFFCBD5E1)
      )
    }
  }
}

@Composable
private fun AutoBudgetRecommendationsSection(
  recommendations: List<BudgetRecommendation>,
  isKurdish: Boolean,
  displayCurrency: String,
  onApplySingle: (BudgetRecommendation) -> Unit,
  onApplyAll: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isKurdish) "پێشنیارێن بودجەیا هەیڤانە یا زیرەک" else "Automated Monthly Budget Targets",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White
        )
        Text(
          text = if (isKurdish)
            "لدویڤ تێکڕایا مەسروفاتێن تە هاتینە حسابکرن بۆ کۆنترۆلکرنا خەرجییان"
          else
            "Calculated from your historical spending velocity to optimize cash retention",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF94A3B8)
        )
      }

      val unalignedCount = recommendations.count { !it.isAlreadyAligned }
      if (unalignedCount > 0) {
        Button(
          onClick = onApplyAll,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF59E0B),
            contentColor = Color(0xFF140E05)
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("apply_all_smart_budgets_btn")
        ) {
          Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isKurdish) "دانانا هەمیان ($unalignedCount)" else "Apply All ($unalignedCount)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }

    recommendations.forEach { rec ->
      val catColor = if (rec.isSchoolCategory) Color(0xFF38BDF8) else Color(0xFF10B981)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF182235))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              if (rec.isSchoolCategory) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(16.dp)
                )
              }
              Text(
                text = rec.categoryLabel(isKurdish),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
            }

            if (rec.isAlreadyAligned) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.16f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = if (isKurdish) "چالاکە" else "Active",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                  )
                }
              }
            } else {
              Button(
                onClick = { onApplySingle(rec) },
                colors = ButtonDefaults.buttonColors(
                  containerColor = catColor.copy(alpha = 0.2f),
                  contentColor = catColor
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text(
                  text = if (isKurdish) "جێبەجێکرن ⚡" else "Apply Target ⚡",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "${if (isKurdish) "خەرجیا ئەڤ هەیڤە" else "This Month"}: ${CurrencyService.format(rec.currentMonthSpentDisplay, displayCurrency)}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = "${if (isKurdish) "ئارمانجا پێشنیارکری" else "Recommended"}: ${CurrencyService.format(rec.recommendedMonthlyLimitDisplay, displayCurrency)}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = catColor
            )
          }

          Text(
            text = rec.reason(isKurdish),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1)
          )
        }
      }
    }
  }
}

@Composable
fun DebtPayoffOptimizerSection(
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  isKurdish: Boolean
) {
  val strategy = uiState.debtPayoffStrategy
  val plan = uiState.smartIntelligence.debtPlanFor(strategy)
  val cur = uiState.displayCurrency

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // Strategy Selector
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      DebtPayoffStrategy.entries.forEach { strat ->
        val selected = strategy == strat
        FilterChip(
          selected = selected,
          onClick = { viewModel.setDebtPayoffStrategy(strat) },
          label = {
            Text(
              text = if (isKurdish) strat.labelKu else strat.labelEn,
              fontSize = 11.sp,
              fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            )
          },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Safe Cash Reserve & Allocatable Cash Summary
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF172234))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = if (isKurdish) plan.strategySummaryKu else plan.strategySummaryEn,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFCBD5E1)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = if (isKurdish) "🛡️ یەدەگێ پاراستی (١٥ ڕۆژ)" else "🛡️ Safe 15d Reserve",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = CurrencyService.format(plan.safeCashReserveDisplay, cur),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = if (isKurdish) "⚡ کاشێ ئامادە بۆ دانا قەرزان" else "⚡ Ready for Debt Payoff",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = CurrencyService.format(plan.allocatableCashNowDisplay, cur),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF10B981)
            )
          }
        }
      }
    }

    if (plan.prioritizedDebts.isEmpty() && plan.prioritizedCredits.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (isKurdish) "✅ هیچ قەرزەکێ چالاک نینە — بارودۆخێ تە یێ قەرزان سەداسەد پاقژە!" else "✅ Zero active debts or credits — your ledger is 100% clear!",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF10B981),
          fontWeight = FontWeight.Bold
        )
      }
    } else {
      if (plan.prioritizedDebts.isNotEmpty()) {
        Text(
          text = if (isKurdish) "🔻 ڕیزبەندیا دانا قەرزێن لسەر من (DEBT Payoff Order)" else "🔻 Prioritized Debt Payoff Queue (DEBT)",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFFF59E0B)
        )
        plan.prioritizedDebts.take(4).forEach { step ->
          DebtPayoffStepCard(
            step = step,
            isKurdish = isKurdish,
            displayCurrency = cur,
            onExecuteStep = { viewModel.applyDebtPayoffStep(step, affectCashBalance = true) }
          )
        }
      }

      if (plan.prioritizedCredits.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (isKurdish) "🔺 ڕیزبەندیا وەرگرتنەڤا قەرزێن دەف خەلکی (CREDIT Collection)" else "🔺 Priority Receivables to Collect (CREDIT)",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF10B981)
        )
        plan.prioritizedCredits.take(3).forEach { step ->
          DebtPayoffStepCard(
            step = step,
            isKurdish = isKurdish,
            displayCurrency = cur,
            onExecuteStep = { viewModel.applyDebtPayoffStep(step, affectCashBalance = true) }
          )
        }
      }
    }
  }
}

@Composable
private fun DebtPayoffStepCard(
  step: DebtPayoffStep,
  isKurdish: Boolean,
  displayCurrency: String,
  onExecuteStep: () -> Unit
) {
  val accent = if (step.isCreditReceivable) Color(0xFF10B981) else if (step.isOverdue) Color(0xFFEF4444) else Color(0xFFF59E0B)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF182235))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "#${step.rank}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = accent
            )
          }
          Column {
            Text(
              text = step.personName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = step.reason(isKurdish),
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF94A3B8)
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = CurrencyService.format(step.remainingDisplay, displayCurrency),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accent
          )
          Text(
            text = if (step.willSettleCompletely) {
              if (isKurdish) "تەمام دئێتە داخستن ✅" else "Clears 100% ✅"
            } else {
              if (isKurdish) "~${step.estimatedMonthsToClear} هەیڤ" else "~${step.estimatedMonthsToClear} mo"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${if (isKurdish) "پێشنیار بۆ نوکە" else "Recommended Now"}: ${CurrencyService.format(step.recommendedPaymentDisplay, displayCurrency)}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF38BDF8)
        )

        Button(
          onClick = onExecuteStep,
          colors = ButtonDefaults.buttonColors(
            containerColor = accent.copy(alpha = 0.2f),
            contentColor = accent
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.height(30.dp)
        ) {
          Text(
            text = if (step.isCreditReceivable) {
              if (isKurdish) "وەرگرتن 📥" else "Collect 📥"
            } else {
              if (isKurdish) "قەرز دانەڤە ⚡" else "Pay Step ⚡"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }
  }
}
