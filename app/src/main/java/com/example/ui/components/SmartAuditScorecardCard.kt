package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class AuditPillarScore(
  val titleKu: String,
  val titleEn: String,
  val score: Int,
  val maxScore: Int = 25,
  val icon: ImageVector,
  val statusNoteKu: String,
  val statusNoteEn: String,
  val color: Color
)

@Composable
fun SmartAuditScorecardCard(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onOpenCopilot: () -> Unit,
  modifier: Modifier = Modifier
) {
  // 1. Compute Liquidity Health (0 - 25)
  // Evaluates cash buffer against typical monthly obligations
  val pureCash = uiState.pureCashBalance
  val totalOut = max(1.0, uiState.totalMoneyOut)
  val liquidityScore = when {
    pureCash >= 20000.0 -> 25
    pureCash >= 8000.0 -> 21
    pureCash >= 2500.0 -> 16
    pureCash >= 500.0 -> 10
    else -> 5
  }

  // 2. Compute Debt Safety Ratio (0 - 25)
  // Safe leverage: Total debt vs total wealth assets
  val totalDebt = uiState.totalDebtOwed
  val totalAssets = max(1.0, uiState.totalCapitalAssets)
  val debtRatio = totalDebt / totalAssets
  val debtSafetyScore = when {
    totalDebt == 0.0 -> 25
    debtRatio <= 0.12 -> 24
    debtRatio <= 0.25 -> 20
    debtRatio <= 0.45 -> 14
    debtRatio <= 0.70 -> 8
    else -> 4
  }

  // 3. Compute Asset Diversification Index (0 - 25)
  var activeClasses = 0
  if (pureCash > 500.0) activeClasses++
  if (uiState.carInvestments.any { !it.isSold }) activeClasses++
  if (uiState.nonCarInvestments.any { !it.isSold }) activeClasses++
  if (uiState.goldHoldings.any { !it.isSold }) activeClasses++
  val diversificationScore = when (activeClasses) {
    4 -> 25
    3 -> 21
    2 -> 16
    1 -> 10
    else -> 5
  }

  // 4. Compute Collection Velocity & Credit Balance (0 - 25)
  val totalCredit = uiState.totalCreditReceivable
  val creditScore = when {
    totalDebt == 0.0 && totalCredit == 0.0 -> 25
    totalCredit >= totalDebt -> 25
    totalDebt > 0 && totalCredit / totalDebt >= 0.75 -> 20
    totalDebt > 0 && totalCredit / totalDebt >= 0.40 -> 14
    else -> 8
  }

  val totalScore = (liquidityScore + debtSafetyScore + diversificationScore + creditScore).coerceIn(0, 100)

  val animatedScore = remember { Animatable(0f) }
  LaunchedEffect(totalScore) {
    animatedScore.animateTo(
      targetValue = totalScore.toFloat(),
      animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
    )
  }

  val gradeColor = when {
    totalScore >= 80 -> EmeraldLight
    totalScore >= 60 -> GoldAccent
    else -> Color(0xFFF59E0B)
  }

  val gradeLabelKu = when {
    totalScore >= 85 -> "💎 سەروەتێ شاهانە و پاراستی (Sovereign Wealth)"
    totalScore >= 70 -> "🌟 بالانسا بهێز و د پاراستیە (Strong & Stable)"
    totalScore >= 55 -> "⚖️ سەنگین لێ پێدڤی ب چاودێریێ (Moderate Balance)"
    else -> "⚠️ پێدڤی ب بالانسکرنا بلەزە (Rebalancing Needed)"
  }

  val gradeLabelEn = when {
    totalScore >= 85 -> "💎 Sovereign Wealth Grade (Secure & Thriving)"
    totalScore >= 70 -> "🌟 Strong Financial Health & Balance"
    totalScore >= 55 -> "⚖️ Moderate Risk • Optimization Recommended"
    else -> "⚠️ High Leverage Alert • Immediate Action Advised"
  }

  val executiveSynthesisKu = when {
    totalScore >= 80 -> "سەرمایەیێ تە د ناڤبەرا کاش، ملوک، سەیارە و زێڕی دا ب ئاوایەکێ زۆر زیرەکانە دابەش بوویە و قەرزێن تە ژ ژێر کۆنتڕۆڵێ نە."
    totalScore >= 60 -> "پێکهاتەیا سەرمایەیێ تە باشە، لێ زێدەکرنا نەختینەیێ دێ مەترسیا بازاڕی کێم کەت و ڕێژەیا قازانجان زێدە کەت."
    else -> "ڕێژەیا قەرزان یان کەمییا کاشێ بەردەست پێدڤی ب ئەولەویەتدانێ یە دا کو بارێ دارایی سڤک بیت."
  }

  val pillars = listOf(
    AuditPillarScore(
      titleKu = "نەختینە و بەردەوامی",
      titleEn = "Cash Runway & Liquidity",
      score = liquidityScore,
      icon = Icons.Default.AccountBalance,
      statusNoteKu = if (liquidityScore >= 20) "کاشێ تەناهیێ بەردەستە" else "کاش زێدەتر بکە",
      statusNoteEn = if (liquidityScore >= 20) "Healthy Cash Buffer" else "Buffer Enhancement Advised",
      color = EmeraldLight
    ),
    AuditPillarScore(
      titleKu = "سەلامەتیا قەرزان",
      titleEn = "Debt-to-Asset Shield",
      score = debtSafetyScore,
      icon = Icons.Default.Shield,
      statusNoteKu = if (debtSafetyScore >= 20) "قەرز د ڕێژەیا ئاسایی دانە" else "قەرزان کێم بکە",
      statusNoteEn = if (debtSafetyScore >= 20) "Low Leverage Risk" else "High Debt Pressure",
      color = GoldAccent
    ),
    AuditPillarScore(
      titleKu = "جۆراوجۆریا سەرمایەی",
      titleEn = "Asset Diversification",
      score = diversificationScore,
      icon = Icons.Default.DirectionsCar,
      statusNoteKu = "$activeClasses ژ 4 پۆلان چالاکن",
      statusNoteEn = "$activeClasses of 4 Asset Classes",
      color = Color(0xFF38BDF8)
    ),
    AuditPillarScore(
      titleKu = "وەرگرتنا مافان",
      titleEn = "Receivables Velocity",
      score = creditScore,
      icon = Icons.Default.Insights,
      statusNoteKu = if (creditScore >= 20) "دەینێن تە د پاراستینە" else "دەینان وەربگرە",
      statusNoteEn = if (creditScore >= 20) "Favorable Credit Net" else "Collect Outstanding Sums",
      color = Color(0xFFA78BFA)
    )
  )

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .fillMaxWidth()
      .background(DarkGlassCardSurface, RoundedCornerShape(22.dp))
      .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
          colors = listOf(
            GoldAccent.copy(alpha = 0.70f),
            EmeraldLight.copy(alpha = 0.40f),
            GoldAccent.copy(alpha = 0.55f)
          )
        ),
        shape = RoundedCornerShape(22.dp)
      )
      .testTag("smart_audit_scorecard_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header: Brand Title & Live Audit Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    GoldAccent.copy(alpha = 0.35f),
                    EmeraldContainerDark
                  )
                )
              )
              .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = if (isKurdish) "پشکنینا ژیر یا سەرمایەیێ شاهانە" else "Royal Executive Wealth Audit",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp,
              color = Color.White
            )
            Text(
              text = if (isKurdish) "هەلسەنگاندنا ١٠٠ خالیا تەندروستیا دارایی" else "100-Point Financial Health Scorecard",
              fontSize = 11.sp,
              color = GoldAccent.copy(alpha = 0.9f)
            )
          }
        }

        // Live badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldLight.copy(alpha = 0.16f))
            .border(0.8.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(EmeraldLight)
            )
            Text(
              text = if (isKurdish) "پشکنیا زندی" else "LIVE AUDIT",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
          }
        }
      }

      // Middle Row: Circular Gauge + Health Status Grade
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF070B14).copy(alpha = 0.65f))
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Circular Gauge
        Box(
          modifier = Modifier
            .size(86.dp)
            .testTag("smart_audit_score_gauge"),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.size(86.dp)) {
            val strokeWidth = 8.dp.toPx()
            // Track background arc
            drawArc(
              color = Color.White.copy(alpha = 0.10f),
              startAngle = 140f,
              sweepAngle = 260f,
              useCenter = false,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progress arc
            val sweep = (animatedScore.value / 100f) * 260f
            drawArc(
              brush = Brush.sweepGradient(
                colors = listOf(
                  EmeraldLight,
                  GoldAccent,
                  gradeColor
                )
              ),
              startAngle = 140f,
              sweepAngle = sweep,
              useCenter = false,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${animatedScore.value.roundToInt()}",
              fontWeight = FontWeight.Black,
              fontSize = 22.sp,
              color = gradeColor
            )
            Text(
              text = "/ 100",
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.6f)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Grade label & description
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = if (isKurdish) gradeLabelKu else gradeLabelEn,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = gradeColor
          )
          Text(
            text = if (isKurdish) executiveSynthesisKu else "Your assets across vehicles, real estate, gold, and cash are analyzed with zero leverage compromise.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.82f),
            lineHeight = 16.sp
          )
        }
      }

      // 4 Metric Pillars Grid (2x2)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PillarItemCard(pillar = pillars[0], isKurdish = isKurdish, modifier = Modifier.weight(1f))
          PillarItemCard(pillar = pillars[1], isKurdish = isKurdish, modifier = Modifier.weight(1f))
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PillarItemCard(pillar = pillars[2], isKurdish = isKurdish, modifier = Modifier.weight(1f))
          PillarItemCard(pillar = pillars[3], isKurdish = isKurdish, modifier = Modifier.weight(1f))
        }
      }

      // Action CTA: Open Interactive Gemini Financial Copilot
      Button(
        onClick = onOpenCopilot,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                GoldAccent,
                EmeraldLight,
                GoldAccent
              )
            )
          )
          .testTag("smart_audit_copilot_cta_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF070B14),
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = if (isKurdish) "ڕاوێژکارێ داراییێ ژیر (Gemini Copilot) ڤەکە" else "Consult Gemini AI Financial Copilot",
            color = Color(0xFF070B14),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
private fun PillarItemCard(
  pillar: AuditPillarScore,
  isKurdish: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF0A0F1A).copy(alpha = 0.7f))
      .border(0.8.dp, pillar.color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
      .padding(10.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Icon(
            imageVector = pillar.icon,
            contentDescription = null,
            tint = pillar.color,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = if (isKurdish) pillar.titleKu else pillar.titleEn,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Text(
          text = "${pillar.score}/25",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = pillar.color
        )
      }

      LinearProgressIndicator(
        progress = { (pillar.score / 25f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = pillar.color,
        trackColor = Color.White.copy(alpha = 0.12f)
      )

      Text(
        text = if (isKurdish) pillar.statusNoteKu else pillar.statusNoteEn,
        fontSize = 10.sp,
        color = Color.White.copy(alpha = 0.70f)
      )
    }
  }
}
