package com.example.ui.screens.gold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.data.currency.GoldCandlePoint
import com.example.data.local.GoldHoldingEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.EntryExchangeRateCard
import com.example.ui.components.GoldHoldingDetailActionDialog
import com.example.ui.components.SectionQuickHistoryStrip
import com.example.ui.history.ActionSection
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════════════════════
// PHYSICAL 24K GOLD BULLION LUXURY SYSTEM
// ═══════════════════════════════════════════════════════════════════════════════
private val PhysicalGoldLuminous = Color(0xFFFFD54F)
private val PhysicalGoldReflective = Color(0xFFFFE082)
private val PhysicalGoldHighlight = Color(0xFFFFF9C4)
private val PhysicalGoldBorderWarm = Color(0xFFC59B27)
private val SilverAccent = Color(0xFFCBD5E1)
private val SilverAccentBright = Color(0xFFE2E8F0)

// Transparent Background allowing the luxury jewelry, gold bullion, and silver background image to shine through
private val PhysicalGoldScreenBackground = Brush.verticalGradient(
  colors = listOf(
    Color.Transparent,
    Color.Transparent
  )
)

// Reflective Ingot 24K Gold Border Brush
private val PhysicalGoldBorderBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFFFFF7C2),
    Color(0xFFF3C74D),
    Color(0xFFB8860B),
    Color(0xFFFFDF73),
    Color(0xFFD4AF37),
    Color(0xFF8B6214),
    Color(0xFFFFF7C2)
  )
)

// Fine 24K Gold Inlay Border (for Silver Platinum Cards)
private val FineGoldInlayBorderBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFFFFEEA8),
    Color(0xFFE5B83B),
    Color(0xFFB8860B),
    Color(0xFFFFDF73),
    Color(0xFFFFEEA8)
  )
)

// Dark Glassmorphic 24K Gold Bullion Card Surface (Deep obsidian glass with rich warm gold tint)
private val PhysicalGoldIngotCardBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xF6130E06),
    Color(0xEE1E1508),
    Color(0xF6271C0B),
    Color(0xEE1E1508),
    Color(0xF6130E06)
  ),
  start = Offset(0f, 0f),
  end = Offset(1000f, 1000f)
)

// Dark Glassmorphic Platinum Silver Ingot Card Surface (Deep obsidian glass with refined slate-silver tint)
private val SilverPlatinumIngotCardBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xF60C111A),
    Color(0xEE121926),
    Color(0xF6162030),
    Color(0xEE121926),
    Color(0xF60C111A)
  ),
  start = Offset(0f, 0f),
  end = Offset(1000f, 1000f)
)

// Subtle Luminous Gold Sheen Sweeping Across Bullion Surfaces
private fun Modifier.drawBullionSheen(progress: Float): Modifier = this.drawBehind {
  val w = size.width
  val h = size.height
  if (w <= 0f || h <= 0f) return@drawBehind

  val totalSpan = w + h
  val currentPos = progress * totalSpan
  val beamWidth = 130.dp.toPx()

  val startX = currentPos - beamWidth
  val startY = 0f
  val endX = currentPos + beamWidth
  val endY = h

  drawRect(
    brush = Brush.linearGradient(
      colors = listOf(
        Color.Transparent,
        Color(0x18FFD54F),
        Color(0x3BFFF9C4),
        Color(0x55FFE082),
        Color(0x3BFFF9C4),
        Color(0x18FFD54F),
        Color.Transparent
      ),
      start = Offset(startX, startY),
      end = Offset(endX, endY)
    )
  )
}

@Composable
private fun PhysicalGoldAtmosphereCanvas(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@Canvas

    // 1. Warm radial molten gold glow at top center (like light reflecting off physical 24K bullion gold)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x38FFD54F),
          Color(0x20E5B83B),
          Color(0x0CB8860B),
          Color.Transparent
        ),
        center = Offset(w * 0.5f, h * 0.16f),
        radius = w * 0.75f
      ),
      center = Offset(w * 0.5f, h * 0.16f),
      radius = w * 0.75f
    )

    // 2. Secondary metallic luster reflection in bottom third
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x22FFD54F),
          Color(0x10B8860B),
          Color.Transparent
        ),
        center = Offset(w * 0.75f, h * 0.70f),
        radius = w * 0.6f
      ),
      center = Offset(w * 0.75f, h * 0.70f),
      radius = w * 0.6f
    )

    // 3. Fine metallic bullion brushed texture lines
    val stepY = 24.dp.toPx()
    var currY = 0f
    while (currY < h) {
      drawLine(
        color = Color(0x0CFFDF73),
        start = Offset(0f, currY),
        end = Offset(w, currY),
        strokeWidth = 1.dp.toPx()
      )
      currY += stepY
    }
  }
}

@Composable
fun GoldScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onOpenGoogleSearchGrounding: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  // 0 = 🥇 Gold Vault, 1 = 🥈 Silver 9999 Vault, 2 = 📊 Combined Vault
  var selectedVaultTab by remember { mutableIntStateOf(0) }
  var showAddDialog by remember { mutableStateOf(false) }
  var defaultDialogMetal by remember { mutableStateOf("GOLD") }
  var editingHolding by remember { mutableStateOf<GoldHoldingEntity?>(null) }
  var sellingHolding by remember { mutableStateOf<GoldHoldingEntity?>(null) }
  var showChartAndSimulator by remember { mutableStateOf(false) }
  var showSilverKgPriceDialog by remember { mutableStateOf(false) }
  var selectedHoldingForDetail by remember { mutableStateOf<GoldHoldingEntity?>(null) }

  LaunchedEffect(uiState.focusedEntityType, uiState.focusedEntityId, uiState.goldHoldings) {
    if (uiState.focusedEntityType == "GOLD" && uiState.focusedEntityId != null) {
      val targetItem = uiState.goldHoldings.find { it.id == uiState.focusedEntityId }
      if (targetItem != null) {
        selectedVaultTab = if (targetItem.isSilver) 1 else 0
        selectedHoldingForDetail = targetItem
        viewModel.clearFocusedSearchResult()
      }
    }
  }

  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
  val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

  val quote = uiState.goldQuote
  val currentPrice24K = uiState.effectiveGoldPricePerGram24K
  val currentSilver9999 = uiState.effectiveSilverPricePerGram9999

  val goldHoldingsList = uiState.goldHoldings.filter { it.isGold }
  val silverHoldingsList = uiState.goldHoldings.filter { it.isSilver }
  val displayedHoldings = when (selectedVaultTab) {
    0 -> goldHoldingsList
    1 -> silverHoldingsList
    else -> uiState.goldHoldings
  }

  val isSilverTab = selectedVaultTab == 1
  val activeAccent = if (isSilverTab) SilverAccent else PhysicalGoldLuminous

  val infiniteTransition = rememberInfiniteTransition(label = "goldSheen")
  val sheenProgress by infiniteTransition.animateFloat(
    initialValue = -0.4f,
    targetValue = 1.4f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sheenProgress"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(PhysicalGoldScreenBackground)
  ) {
    PhysicalGoldAtmosphereCanvas(modifier = Modifier.fillMaxSize())

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Top Segmented Vault Switcher (🥇 زێڕ | 🥈 زیڤ 9999 | 📊 گشتی) + Live Sync Bar
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .background(PhysicalGoldIngotCardBrush, RoundedCornerShape(22.dp))
            .border(1.2.dp, PhysicalGoldBorderBrush, RoundedCornerShape(22.dp))
            .drawBullionSheen(sheenProgress)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                      if (uiState.simulatedGoldPricePerGram != null || uiState.simulatedSilverPricePerGram != null)
                        Color(0xFFF59E0B)
                      else PhysicalGoldLuminous
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = if (isKurdish) "خەزینەیا زێڕ و زیڤی • عەیار ٢٤" else "24K PURE GOLD BULLION VAULT",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Black,
                      color = PhysicalGoldReflective
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "AU 999.9",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color(0xFF160E03),
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PhysicalGoldLuminous)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                  Text(
                    text = if (isKurdish) {
                      "نویکرنا ڕاستەوخۆ یا بۆرسێ • ${timeFormat.format(Date(quote.timestamp))}"
                    } else {
                      "Official Spot Feed • ${timeFormat.format(Date(quote.timestamp))}"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFFD4B982)
                  )
                }
              }

              if (uiState.isFetchingGold) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                  color = PhysicalGoldLuminous
                )
              } else {
                IconButton(
                  onClick = { viewModel.fetchLiveGoldQuote() },
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(0.8.dp, PhysicalGoldBorderBrush, CircleShape)
                    .testTag("refresh_gold_price_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = if (isKurdish) "نویکرنا بهایێ ڕاستەوخۆ" else "Refresh Live Price",
                    tint = PhysicalGoldLuminous,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Tab Segmented Selector
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .border(0.8.dp, PhysicalGoldBorderWarm.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              VaultSegmentTab(
                title = if (isKurdish) "🥇 زێڕ (${goldHoldingsList.size})" else "🥇 Gold (${goldHoldingsList.size})",
                selected = selectedVaultTab == 0,
                activeColor = PhysicalGoldLuminous,
                onClick = { selectedVaultTab = 0 },
                modifier = Modifier
                  .weight(1f)
                  .testTag("vault_tab_gold")
              )
              VaultSegmentTab(
                title = if (isKurdish) "🥈 زیڤ 9999 (${silverHoldingsList.size})" else "🥈 Silver (${silverHoldingsList.size})",
                selected = selectedVaultTab == 1,
                activeColor = SilverAccentBright,
                onClick = { selectedVaultTab = 1 },
                modifier = Modifier
                  .weight(1f)
                  .testTag("vault_tab_silver")
              )
              VaultSegmentTab(
                title = if (isKurdish) "📊 گشتی (${uiState.goldHoldings.size})" else "📊 All (${uiState.goldHoldings.size})",
                selected = selectedVaultTab == 2,
                activeColor = PhysicalGoldHighlight,
                onClick = { selectedVaultTab = 2 },
                modifier = Modifier
                  .weight(1f)
                  .testTag("vault_tab_all")
              )
            }
          }
        }
      }

      // 2. Executive Vault Hero Summary Card (Buy Price vs Current Valuation vs Profit/Loss)
      item {
        val totalCost = when (selectedVaultTab) {
          0 -> uiState.totalGoldOnlyCostDisplay
          1 -> uiState.totalSilverCostDisplay
          else -> uiState.totalGoldCostDisplay
        }
        val currentValue = when (selectedVaultTab) {
          0 -> uiState.totalGoldOnlyValueDisplay
          1 -> uiState.totalSilverValueDisplay
          else -> uiState.totalGoldCurrentValueDisplay
        }
        val profitLoss = when (selectedVaultTab) {
          0 -> uiState.totalGoldOnlyProfitLossDisplay
          1 -> uiState.totalSilverProfitLossDisplay
          else -> uiState.totalGoldProfitLossDisplay
        }
        val roi = when (selectedVaultTab) {
          0 -> uiState.totalGoldOnlyRoiPercent
          1 -> uiState.totalSilverRoiPercent
          else -> uiState.totalGoldRoiPercent
        }
        val isGain = profitLoss >= 0

        val heroCardBrush = if (isSilverTab) SilverPlatinumIngotCardBrush else PhysicalGoldIngotCardBrush
        val heroBorderBrush = if (isSilverTab) FineGoldInlayBorderBrush else PhysicalGoldBorderBrush

        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .background(heroCardBrush, RoundedCornerShape(22.dp))
            .border(1.3.dp, heroBorderBrush, RoundedCornerShape(22.dp))
            .drawBullionSheen(sheenProgress)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            // Header + Total Weight Pill
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = when (selectedVaultTab) {
                    0 -> if (isKurdish) "سەرمایەیا زێڕێ ٢٤ عەیار" else "24K GOLD BULLION PORTFOLIO"
                    1 -> if (isKurdish) "خەزینەیا زیڤێ خالس 9999" else "FINE SILVER 999.9 PLATINUM INLAY"
                    else -> if (isKurdish) "کۆژمێ گشتی یێ خەزینەیا زێڕ و زیڤی" else "TOTAL 24K GOLD & SILVER VAULT"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.8.sp,
                  color = PhysicalGoldReflective
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.45f))
                  .border(0.8.dp, PhysicalGoldBorderBrush, RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = when (selectedVaultTab) {
                    0 -> "🥇 ${"%.1f".format(uiState.totalGoldWeightGrams)}g (${"%.1f".format(uiState.totalGoldWeightGrams / 5.0)} ${if (isKurdish) "مسقال" else "Mithqal"})"
                    1 -> "🥈 ${"%.1f".format(uiState.totalSilverWeightGrams)}g (${"%.2f".format(uiState.totalSilverWeightGrams / 1000.0)} ${if (isKurdish) "کیلۆ" else "Kg"})"
                    else -> "🥇 ${"%.0f".format(uiState.totalGoldWeightGrams)}g • 🥈 ${"%.0f".format(uiState.totalSilverWeightGrams)}g"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = PhysicalGoldReflective
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Box Executive Summary Row (سعرا کڕینێ | بهایێ نوکە | فایدە یان خسارە)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Box 1: Buy Cost
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.Black.copy(alpha = 0.40f))
                  .border(0.7.dp, PhysicalGoldBorderWarm.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "سعرا کڕینێ" else "Total Buy Cost",
                    fontSize = 10.sp,
                    color = Color(0xFFD4B982),
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = CurrencyService.format(totalCost, uiState.displayCurrency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              // Box 2: Current Value
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color(0xFF422C0A).copy(alpha = 0.55f))
                  .border(1.dp, PhysicalGoldBorderBrush, RoundedCornerShape(14.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "بهایێ نوکە" else "Current Value",
                    fontSize = 10.sp,
                    color = PhysicalGoldReflective,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = CurrencyService.format(currentValue, uiState.displayCurrency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = PhysicalGoldLuminous,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              // Box 3: Profit or Loss
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(14.dp))
                  .background((if (isGain) EmeraldLight else LossRed).copy(alpha = 0.18f))
                  .border(
                    1.dp,
                    (if (isGain) EmeraldLight else LossRed).copy(alpha = 0.45f),
                    RoundedCornerShape(14.dp)
                  )
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) {
                      if (isGain) "🟢 فایدە (+${"%.1f".format(roi)}%)" else "🔴 خسارە (${"%.1f".format(roi)}%)"
                    } else {
                      if (isGain) "🟢 Profit (+${"%.1f".format(roi)}%)" else "🔴 Loss (${"%.1f".format(roi)}%)"
                    },
                    fontSize = 10.sp,
                    color = if (isGain) EmeraldLight else LossRed,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "${if (isGain) "+" else ""}${CurrencyService.format(profitLoss, uiState.displayCurrency)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isGain) EmeraldLight else LossRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }

            // If on Combined Tab (2), show side-by-side Gold vs Silver breakdown mini-row
            if (selectedVaultTab == 2) {
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldAccent.copy(alpha = 0.1f))
                    .clickable { selectedVaultTab = 0 }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (isKurdish) "🥇 زێڕ (${"%.0f".format(uiState.totalGoldWeightGrams)}g)" else "🥇 Gold",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldAccent
                    )
                    Text(
                      text = CurrencyService.formatShort(uiState.totalGoldOnlyValueDisplay, uiState.displayCurrency),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SilverAccent.copy(alpha = 0.12f))
                    .clickable { selectedVaultTab = 1 }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = if (isKurdish) "🥈 زیڤ (${"%.0f".format(uiState.totalSilverWeightGrams)}g)" else "🥈 Silver",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = SilverAccent
                    )
                    Text(
                      text = CurrencyService.formatShort(uiState.totalSilverValueDisplay, uiState.displayCurrency),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 3. Clean 2x2 Global Market Rates & Purity Grid (Organized by selected tab)
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .background(PhysicalGoldIngotCardBrush, RoundedCornerShape(22.dp))
            .border(1.2.dp, PhysicalGoldBorderBrush, RoundedCornerShape(22.dp))
            .drawBullionSheen(sheenProgress)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = when (selectedVaultTab) {
                  0 -> if (isKurdish) "سعرا عالمی و عەیارێن زێڕێ ٢٤ عەیار (${uiState.displayCurrency})" else "24K GOLD KARAT MARKET MATRIX (${uiState.displayCurrency})"
                  1 -> if (isKurdish) "سعرا عالمی یا زیڤی 9999 (${uiState.displayCurrency})" else "LIVE SILVER 9999 RATES (${uiState.displayCurrency})"
                  else -> if (isKurdish) "سعرا ڕاستەوخۆ یا زێڕ و زیڤی (${uiState.displayCurrency})" else "LIVE GOLD & SILVER SPOT (${uiState.displayCurrency})"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = PhysicalGoldReflective
              )

              val isRising = if (selectedVaultTab == 1) quote.isSilverRising else quote.isRising
              val pct = if (selectedVaultTab == 1) quote.silverChangePercent24h else quote.priceChangePercent24h
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background((if (isRising) EmeraldLight else LossRed).copy(alpha = 0.20f))
                  .border(0.7.dp, (if (isRising) EmeraldLight else LossRed).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "${if (isRising) "▲ +" else "▼ "}${"%.2f".format(pct)}% (24h)",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isRising) EmeraldLight else LossRed
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedVaultTab == 0) {
              // Gold 2x2 Spacious Grid
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MarketRateGridTile(
                  badge = "🥇 24K AU",
                  title = if (isKurdish) "زێڕێ عەیار ٢٤ (خالس)" else "24K Pure Gold",
                  usdAmount = currentPrice24K,
                  subtitle = if (isKurdish) {
                    "بۆ هەر گرامەکێ • مسقال: ${CurrencyService.formatShort(CurrencyService.convert(currentPrice24K * 5.0, "USD", uiState.displayCurrency, uiState.iqdRate), uiState.displayCurrency)}"
                  } else {
                    "Per Gram • 5g: ${CurrencyService.formatShort(CurrencyService.convert(currentPrice24K * 5.0, "USD", uiState.displayCurrency, uiState.iqdRate), uiState.displayCurrency)}"
                  },
                  uiState = uiState,
                  accentColor = PhysicalGoldLuminous,
                  isHighlighted = true,
                  modifier = Modifier.weight(1f)
                )
                MarketRateGridTile(
                  badge = "⚖️ 21K مسقال",
                  title = if (isKurdish) "مسقالا عەیار ٢١ (٥ گرام)" else "21K Mithqal (5g)",
                  usdAmount = currentPrice24K * (21.0 / 24.0) * 5.0,
                  subtitle = if (isKurdish) {
                    "١ گرام عەیار ٢١: ${CurrencyService.formatShort(CurrencyService.convert(currentPrice24K * (21.0 / 24.0), "USD", uiState.displayCurrency, uiState.iqdRate), uiState.displayCurrency)}"
                  } else {
                    "1g 21K: ${CurrencyService.formatShort(CurrencyService.convert(currentPrice24K * (21.0 / 24.0), "USD", uiState.displayCurrency, uiState.iqdRate), uiState.displayCurrency)}"
                  },
                  uiState = uiState,
                  accentColor = PhysicalGoldLuminous,
                  isHighlighted = true,
                  modifier = Modifier.weight(1f)
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MarketRateGridTile(
                  badge = "🪙 22K لیرە",
                  title = if (isKurdish) "زێڕێ عەیار ٢٢ (لیرە)" else "22K Coin Gold",
                  usdAmount = currentPrice24K * (22.0 / 24.0),
                  subtitle = if (isKurdish) "بۆ هەر ١ گرامەکێ" else "Per 1 Gram",
                  uiState = uiState,
                  accentColor = PhysicalGoldReflective,
                  isHighlighted = false,
                  modifier = Modifier.weight(1f)
                )
                MarketRateGridTile(
                  badge = "💎 18K",
                  title = if (isKurdish) "زێڕێ عەیار ١٨ (ئیتاڵی)" else "18K Italian Gold",
                  usdAmount = currentPrice24K * (18.0 / 24.0),
                  subtitle = if (isKurdish) "بۆ هەر ١ گرامەکێ" else "Per 1 Gram",
                  uiState = uiState,
                  accentColor = PhysicalGoldReflective,
                  isHighlighted = false,
                  modifier = Modifier.weight(1f)
                )
              }
            } else if (selectedVaultTab == 1) {
              // Silver 9999 2x2 Spacious Grid (1 Kg Global Bar first and prominent!)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MarketRateGridTile(
                  badge = "🧱 1 Kg (1000g)",
                  title = if (isKurdish) "١ کیلۆ زیڤێ خالس 9999" else "1 Kg Silver 9999 Bar",
                  usdAmount = currentSilver9999 * 1000.0,
                  subtitle = if (isKurdish) "سعرا عالمی (32.15 Oz) • کلیک بۆ گوهۆڕین" else "Global 1Kg Bar • Tap to calibrate",
                  uiState = uiState,
                  accentColor = SilverAccentBright,
                  isHighlighted = true,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { showSilverKgPriceDialog = true }
                )
                MarketRateGridTile(
                  badge = "🥈 9999 (1g)",
                  title = if (isKurdish) "١ گرام زیڤێ خالس 9999" else "1g Fine Silver 9999",
                  usdAmount = currentSilver9999,
                  subtitle = if (isKurdish) "بهایێ ١ گرام ب دۆلار: $${"%.2f".format(currentSilver9999)}" else "Spot / Gram: $${"%.2f".format(currentSilver9999)}",
                  uiState = uiState,
                  accentColor = SilverAccentBright,
                  isHighlighted = true,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { showSilverKgPriceDialog = true }
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MarketRateGridTile(
                  badge = "🌐 Troy Oz (XAG)",
                  title = if (isKurdish) "ئۆنسا عالمی یا زیڤی" else "Silver Troy Ounce",
                  usdAmount = currentSilver9999 * 31.1034768,
                  subtitle = if (isKurdish) "٣١.١٠ گرام (XAG/USD)" else "31.10g Spot Ounce",
                  uiState = uiState,
                  accentColor = SilverAccent,
                  isHighlighted = false,
                  modifier = Modifier.weight(1f)
                )
                MarketRateGridTile(
                  badge = "💍 925 (1 Kg)",
                  title = if (isKurdish) "١ کیلۆ زیڤێ عەیار 925" else "1 Kg 925 Sterling",
                  usdAmount = currentSilver9999 * 0.925 * 1000.0,
                  subtitle = if (isKurdish) "١ گرام 925: $${"%.2f".format(currentSilver9999 * 0.925)}" else "1g 925: $${"%.2f".format(currentSilver9999 * 0.925)}",
                  uiState = uiState,
                  accentColor = SilverAccent,
                  isHighlighted = false,
                  modifier = Modifier.weight(1f)
                )
              }
            } else {
              // Combined Tab: Side-by-Side Gold vs 1 Kg Silver Global Spot Cards
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MarketRateGridTile(
                  badge = "🥇 24K Gold (1g)",
                  title = if (isKurdish) "١ گرام زێڕێ عەیار ٢٤" else "1g 24K Pure Gold",
                  usdAmount = currentPrice24K,
                  subtitle = if (isKurdish) {
                    "مسقال 21K: ${CurrencyService.formatShort(CurrencyService.convert(currentPrice24K * (21.0 / 24.0) * 5.0, "USD", uiState.displayCurrency, uiState.iqdRate), uiState.displayCurrency)}"
                  } else {
                    "21K Mithqal: ${CurrencyService.formatShort(CurrencyService.convert(currentPrice24K * (21.0 / 24.0) * 5.0, "USD", uiState.displayCurrency, uiState.iqdRate), uiState.displayCurrency)}"
                  },
                  uiState = uiState,
                  accentColor = PhysicalGoldLuminous,
                  isHighlighted = true,
                  modifier = Modifier.weight(1f)
                )
                MarketRateGridTile(
                  badge = "🧱 1 Kg Silver 9999",
                  title = if (isKurdish) "١ کیلۆ زیڤێ خالس 9999" else "1 Kg Fine Silver 9999",
                  usdAmount = currentSilver9999 * 1000.0,
                  subtitle = if (isKurdish) {
                    "١ گرام: $${"%.2f".format(currentSilver9999)} • ئۆنس: $${"%.2f".format(currentSilver9999 * 31.1034768)}"
                  } else {
                    "1g: $${"%.2f".format(currentSilver9999)} • Oz: $${"%.2f".format(currentSilver9999 * 31.1034768)}"
                  },
                  uiState = uiState,
                  accentColor = SilverAccentBright,
                  isHighlighted = true,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { showSilverKgPriceDialog = true }
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated 1 Kg Global Silver Live Bar with Fine Gold Border
            val liveSilverKgUsd = currentSilver9999 * 1000.0
            val liveSilverKgDisplay = CurrencyService.convert(liveSilverKgUsd, "USD", uiState.displayCurrency, uiState.iqdRate)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.dp, FineGoldInlayBorderBrush, RoundedCornerShape(12.dp))
                .clickable { showSilverKgPriceDialog = true }
                .padding(horizontal = 12.dp, vertical = 9.dp)
                .testTag("silver_1kg_global_rate_bar"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isKurdish) {
                    "🥈 بهایێ ١ کیلۆ زیڤێ نوکە (عالمی 9999): ${CurrencyService.format(liveSilverKgDisplay, uiState.displayCurrency)}"
                  } else {
                    "🥈 Live 1 Kg Silver 9999 Global Price: ${CurrencyService.format(liveSilverKgDisplay, uiState.displayCurrency)}"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = SilverAccentBright
                )
                Text(
                  text = if (isKurdish) {
                    "١ گرام: $${"%.2f".format(currentSilver9999)} • ئۆنس: $${"%.2f".format(currentSilver9999 * 31.1034768)} USD${if (uiState.simulatedSilverPricePerGram != null) " (دەستکاریکری)" else " (بۆرسا جیهانی)"}"
                  } else {
                    "1g: $${"%.2f".format(currentSilver9999)} • Troy Oz: $${"%.2f".format(currentSilver9999 * 31.1034768)} USD"
                  },
                  fontSize = 10.sp,
                  color = Color(0xFFD4B982)
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF38250A))
                  .border(0.7.dp, PhysicalGoldBorderBrush, RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = PhysicalGoldLuminous,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isKurdish) "سعرا ١کغ" else "1Kg Rate",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PhysicalGoldLuminous
                  )
                }
              }
            }
          }
        }
      }

      // 4. Holdings Section Header + Quick Add Buttons for Gold & Silver
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = when (selectedVaultTab) {
              0 -> if (isKurdish) "تۆمارێن زێڕێ ٢٤ عەیار (${goldHoldingsList.size})" else "24K GOLD BULLION HOLDINGS (${goldHoldingsList.size})"
              1 -> if (isKurdish) "تۆمارێن زیڤێ خالس 9999 (${silverHoldingsList.size})" else "FINE SILVER 999.9 HOLDINGS (${silverHoldingsList.size})"
              else -> if (isKurdish) "هەمی خەزینەیا زێڕ و زیڤی (${uiState.goldHoldings.size})" else "ALL BULLION HOLDINGS (${uiState.goldHoldings.size})"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = PhysicalGoldReflective
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedVaultTab != 1) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF452E0B))
                  .border(0.8.dp, PhysicalGoldBorderBrush, RoundedCornerShape(8.dp))
                  .clickable {
                    defaultDialogMetal = "GOLD"
                    showAddDialog = true
                  }
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = if (isKurdish) "+ زێڕ" else "+ Gold",
                  fontSize = 11.sp,
                  color = PhysicalGoldLuminous,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
            if (selectedVaultTab != 0) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF26303D))
                  .border(0.8.dp, FineGoldInlayBorderBrush, RoundedCornerShape(8.dp))
                  .clickable {
                    defaultDialogMetal = "SILVER"
                    showAddDialog = true
                  }
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = if (isKurdish) "+ زیڤ 9999" else "+ Silver 9999",
                  fontSize = 11.sp,
                  color = SilverAccentBright,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }
        }
      }

      // 5. Holdings List Items (Placed right in main view!)
      if (displayedHoldings.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
              .fillMaxWidth()
              .background(PhysicalGoldIngotCardBrush, RoundedCornerShape(20.dp))
              .border(1.2.dp, PhysicalGoldBorderBrush, RoundedCornerShape(20.dp))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = when (selectedVaultTab) {
                  1 -> if (isKurdish) {
                    "چ زیڤ د ڤێ خەزینەیێ دا نەهاتیە تۆمارکرن. بۆ زێدەکرنا زیڤێ خۆ یێ عەیار 9999 ب سعرا کڕینێ و بهایێ نوکە ل دوگمەیا خوارێ بدە."
                  } else {
                    "No Silver 9999 holdings recorded yet. Tap below to add your Silver 9999 with buy price & live profit/loss."
                  }
                  else -> if (isKurdish) {
                    "چ زێڕ د ڤێ خەزینەیێ دا نینە. ل دوگمەیا زێدەکرنا زێڕی بدە دا قاڵبێن خۆ یێن ٢٤ عەیار تۆمار بکەی."
                  } else {
                    "No gold bullion holdings recorded yet. Tap below to add your 24K physical gold bullion."
                  }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD4B982)
              )
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = {
                  defaultDialogMetal = if (selectedVaultTab == 1) "SILVER" else "GOLD"
                  showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = PhysicalGoldLuminous,
                  contentColor = Color(0xFF160E03)
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = if (selectedVaultTab == 1) {
                    if (isKurdish) "+ زێدەکرنا زیڤی (عەیار 9999)" else "+ Add Silver 9999"
                  } else {
                    if (isKurdish) "+ زێدەکرنا زێڕێ ٢٤ عەیار" else "+ Add 24K Gold Bullion"
                  },
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }
      } else {
        items(displayedHoldings, key = { it.id }) { holding ->
          GoldHoldingCard(
            holding = holding,
            marketPrice24K = currentPrice24K,
            marketSilverPrice9999 = currentSilver9999,
            displayCurrency = uiState.displayCurrency,
            iqdRate = uiState.iqdRate,
            dateFormat = dateFormat,
            isKurdish = isKurdish,
            sheenProgress = sheenProgress,
            onEdit = { viewModel.openQuickEditForGold(holding) },
            onSell = { sellingHolding = holding },
            onReopen = { viewModel.reopenGoldHoldingAsActive(holding.id) },
            onDelete = { viewModel.deleteGoldHolding(holding.id) },
            onClick = { selectedHoldingForDetail = holding }
          )
        }
      }

      // 6. Collapsible 24-Hour Market Chart & Price Simulator Card (Organized at bottom)
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .background(PhysicalGoldIngotCardBrush, RoundedCornerShape(22.dp))
            .border(1.2.dp, PhysicalGoldBorderBrush, RoundedCornerShape(22.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showChartAndSimulator = !showChartAndSimulator },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ShowChart,
                  contentDescription = null,
                  tint = PhysicalGoldLuminous,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = if (isKurdish) "خشتەیا ٢٤ دەمژمێرین و تاقیکرنا سعرا" else "24-HOUR CHART & PRICE SIMULATOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = PhysicalGoldReflective
                  )
                  Text(
                    text = if (isKurdish) "سنورێ ٢٤ دەمژمێرین: $${"%.1f".format(quote.low24hPerGram)} - $${"%.1f".format(quote.high24hPerGram)} / g" else "24h Range: $${"%.1f".format(quote.low24hPerGram)} - $${"%.1f".format(quote.high24hPerGram)} / g",
                    fontSize = 10.sp,
                    color = Color(0xFFD4B982)
                  )
                }
              }

              Icon(
                imageVector = if (showChartAndSimulator) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = PhysicalGoldLuminous
              )
            }

            AnimatedVisibility(visible = showChartAndSimulator) {
              Column(modifier = Modifier.padding(top = 14.dp)) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.40f))
                    .border(0.8.dp, PhysicalGoldBorderWarm.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(8.dp)
                ) {
                  GoldChartCanvas(
                    candles = quote.candles,
                    isRising = quote.isRising,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(130.dp)
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                  text = if (isKurdish) "تاقیکرنا بهایێ زێڕێ خاو (USD / g):" else "Simulate Gold Spot Price (USD / g):",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = PhysicalGoldReflective
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Button(
                    onClick = { viewModel.setSimulatedGoldPrice(120.0) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (uiState.simulatedGoldPricePerGram == 120.0) LossRed else Color.Black.copy(alpha = 0.40f),
                      contentColor = if (uiState.simulatedGoldPricePerGram == 120.0) Color.White else PhysicalGoldReflective
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicalGoldBorderWarm.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 38.dp)
                  ) {
                    Text("$120/g", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                  }

                  Button(
                    onClick = { viewModel.setSimulatedGoldPrice(127.0) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (uiState.simulatedGoldPricePerGram == 127.0) PhysicalGoldLuminous else Color.Black.copy(alpha = 0.40f),
                      contentColor = if (uiState.simulatedGoldPricePerGram == 127.0) Color(0xFF160E03) else PhysicalGoldReflective
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicalGoldBorderWarm.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 38.dp)
                  ) {
                    Text("$127/g", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                  }

                  Button(
                    onClick = { viewModel.setSimulatedGoldPrice(135.0) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (uiState.simulatedGoldPricePerGram == 135.0) EmeraldLight else Color.Black.copy(alpha = 0.40f),
                      contentColor = if (uiState.simulatedGoldPricePerGram == 135.0) Color(0xFF160E03) else PhysicalGoldReflective
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicalGoldBorderWarm.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 38.dp)
                  ) {
                    Text("$135/g", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                  }

                  Button(
                    onClick = {
                      viewModel.setSimulatedGoldPrice(null)
                      viewModel.setSimulatedSilverPrice(null)
                      viewModel.fetchLiveGoldQuote()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.40f)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicalGoldBorderWarm.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 38.dp)
                  ) {
                    Text(if (isKurdish) "ڕاستەوخۆ" else "Live API", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = PhysicalGoldReflective)
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                  text = if (isKurdish) "تاقیکرن یان دیارکرنا بهایێ ١ کیلۆ زیڤ (USD / 1 Kg):" else "Simulate / Set 1 Kg Silver Price (USD / Kg):",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = SilverAccentBright
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  listOf(1960.0 to "$1,960", 2006.0 to "$2,006", 2070.0 to "$2,070").forEach { (kgVal, label) ->
                    val isSel = uiState.simulatedSilverPricePerGram != null &&
                      kotlin.math.abs((uiState.simulatedSilverPricePerGram * 1000.0) - kgVal) < 1.0
                    Button(
                      onClick = { viewModel.setCustomSilverPricePerKg(kgVal) },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) SilverAccent else Color.Black.copy(alpha = 0.40f),
                        contentColor = if (isSel) Color(0xFF160E03) else SilverAccentBright
                      ),
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, FineGoldInlayBorderBrush),
                      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                      modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp)
                    ) {
                      Text("$label/Kg", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                  }

                  Button(
                    onClick = { showSilverKgPriceDialog = true },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = Color(0xFF38250A),
                      contentColor = PhysicalGoldLuminous
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PhysicalGoldBorderBrush),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                      .weight(1f)
                      .heightIn(min = 38.dp)
                  ) {
                    Text(if (isKurdish) "✏️ دیارکرن" else "✏️ Custom", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Minted Coin 24K Gold Floating Action Button
    ExtendedFloatingActionButton(
      onClick = {
        defaultDialogMetal = if (selectedVaultTab == 1) "SILVER" else "GOLD"
        showAddDialog = true
      },
      containerColor = PhysicalGoldLuminous,
      contentColor = Color(0xFF160E03),
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .border(
          width = 1.5.dp,
          brush = Brush.linearGradient(
            listOf(Color(0xFFFFF9C4), Color(0xFFC59B27), Color(0xFFFFD54F))
          ),
          shape = RoundedCornerShape(16.dp)
        )
        .testTag("add_gold_stock_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF160E03), modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (selectedVaultTab == 1) {
          if (isKurdish) "+ زێدەکرنا زیڤی 9999" else "+ Add Silver 9999"
        } else {
          if (isKurdish) "+ زێدەکرنا زێڕی" else "+ Add Gold Stock"
        },
        fontWeight = FontWeight.Black,
        color = Color(0xFF160E03),
        fontSize = 13.sp
      )
    }
  }

  // Add Gold / Silver Dialog
  if (showAddDialog) {
    AddEditGoldDialog(
      holding = null,
      initialMetalType = defaultDialogMetal,
      defaultGoldPrice24K = currentPrice24K,
      defaultSilverPrice9999 = currentSilver9999,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddDialog = false },
      onConfirm = { metalType, title, grams, pricePerGram, karatOrPurity, notes, deductFromCash ->
        viewModel.addPreciousMetalHolding(
          metalType = metalType,
          title = title,
          weightGrams = grams,
          buyPricePerGram = pricePerGram,
          karatOrPurity = karatOrPurity,
          purityLabel = if (metalType == "SILVER") "$karatOrPurity" else "${karatOrPurity}K",
          notes = notes,
          deductedFromCash = deductFromCash
        )
        showAddDialog = false
      }
    )
  }

  // Edit Gold / Silver Dialog
  if (editingHolding != null) {
    AddEditGoldDialog(
      holding = editingHolding,
      initialMetalType = editingHolding!!.metalType,
      defaultGoldPrice24K = currentPrice24K,
      defaultSilverPrice9999 = currentSilver9999,
      displayCurrency = uiState.displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { editingHolding = null },
      onConfirm = { metalType, title, grams, pricePerGram, karatOrPurity, notes, deductFromCash ->
        viewModel.updateGoldHolding(
          editingHolding!!.copy(
            metalType = metalType,
            title = title,
            weightGrams = grams,
            buyPricePerGram = pricePerGram,
            karat = karatOrPurity,
            purityLabel = if (metalType == "SILVER") "$karatOrPurity" else "${karatOrPurity}K",
            notes = notes,
            deductedFromCash = deductFromCash
          )
        )
        editingHolding = null
      }
    )
  }

  // Sell Gold / Silver Dialog (Directly adds proceeds to Cash Wealth)
  if (sellingHolding != null) {
    val targetHolding = sellingHolding!!
    SellGoldSilverDialog(
      holding = targetHolding,
      marketPrice24K = currentPrice24K,
      marketSilverPrice9999 = currentSilver9999,
      displayCurrency = uiState.displayCurrency,
      iqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { sellingHolding = null },
      onConfirm = { soldPriceUsd, notes ->
        viewModel.sellGoldHolding(
          holdingId = targetHolding.id,
          soldPriceUsd = soldPriceUsd,
          notes = notes
        )
        sellingHolding = null
      }
    )
  }

  // Calibrate / Live Global 1 Kg Silver Price Dialog
  if (showSilverKgPriceDialog) {
    SilverKgGlobalPriceDialog(
      currentSilverGram9999 = currentSilver9999,
      liveQuoteSilverGram9999 = quote.silverPricePerGram9999,
      isCustomSimulated = uiState.simulatedSilverPricePerGram != null,
      displayCurrency = uiState.displayCurrency,
      iqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showSilverKgPriceDialog = false },
      onUseLiveGlobal = {
        viewModel.setSimulatedSilverPrice(null)
        viewModel.fetchLiveGoldQuote()
        showSilverKgPriceDialog = false
      },
      onSaveCustomKgPrice = { customKgUsd ->
        viewModel.setCustomSilverPricePerKg(customKgUsd)
        showSilverKgPriceDialog = false
      }
    )
  }

  if (selectedHoldingForDetail != null) {
    GoldHoldingDetailActionDialog(
      holding = selectedHoldingForDetail!!,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedHoldingForDetail = null }
    )
  }
}

@Composable
private fun VaultSegmentTab(
  title: String,
  selected: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .heightIn(min = 38.dp)
      .clip(RoundedCornerShape(10.dp))
      .then(
        if (selected) {
          Modifier.background(
            Brush.linearGradient(
              listOf(Color(0xFF6B4B13), Color(0xFF8A6218), Color(0xFF5A3E0F))
            ),
            RoundedCornerShape(10.dp)
          )
        } else {
          Modifier.background(Color.Transparent, RoundedCornerShape(10.dp))
        }
      )
      .border(
        width = if (selected) 1.dp else 0.dp,
        brush = if (selected) PhysicalGoldBorderBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
      color = if (selected) PhysicalGoldHighlight else Color(0xFFD4B982),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun MarketRateGridTile(
  badge: String,
  title: String,
  usdAmount: Double,
  subtitle: String,
  uiState: FinanceUiState,
  accentColor: Color,
  isHighlighted: Boolean,
  modifier: Modifier = Modifier
) {
  val converted = CurrencyService.convert(usdAmount, "USD", uiState.displayCurrency, uiState.iqdRate)
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(
        if (isHighlighted) Brush.linearGradient(listOf(Color(0xFF4A320C), Color(0xFF6E4A12), Color(0xFF48300A)))
        else Brush.linearGradient(listOf(Color(0xFF2A1C06), Color(0xFF382508)))
      )
      .border(
        width = 1.1.dp,
        brush = if (isHighlighted) PhysicalGoldBorderBrush else FineGoldInlayBorderBrush,
        shape = RoundedCornerShape(14.dp)
      )
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = badge,
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = PhysicalGoldReflective
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFD4B982),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = CurrencyService.format(converted, uiState.displayCurrency),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = PhysicalGoldLuminous,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = Color(0xFFC0A670),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun GoldChartCanvas(
  candles: List<GoldCandlePoint>,
  isRising: Boolean,
  modifier: Modifier = Modifier
) {
  val strokeColor = if (isRising) EmeraldLight else LossRed
  val fillColor = strokeColor.copy(alpha = 0.15f)

  Canvas(modifier = modifier) {
    if (candles.isEmpty()) return@Canvas

    val width = size.width
    val height = size.height
    val minPrice = candles.minOf { it.lowPerGram } * 0.998
    val maxPrice = candles.maxOf { it.highPerGram } * 1.002
    val range = if (maxPrice > minPrice) maxPrice - minPrice else 1.0

    val stepX = width / (candles.size - 1).coerceAtLeast(1)

    val path = Path()
    val fillPath = Path()

    candles.forEachIndexed { index, candle ->
      val x = index * stepX
      val y = height - (((candle.closePerGram - minPrice) / range) * height).toFloat()

      if (index == 0) {
        path.moveTo(x, y)
        fillPath.moveTo(x, height)
        fillPath.lineTo(x, y)
      } else {
        path.lineTo(x, y)
        fillPath.lineTo(x, y)
      }

      if (index == candles.size - 1) {
        fillPath.lineTo(x, height)
        fillPath.close()
      }
    }

    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(fillColor, Color.Transparent),
        startY = 0f,
        endY = height
      )
    )

    drawPath(
      path = path,
      color = strokeColor,
      style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    val lastCandle = candles.last()
    val lastX = (candles.size - 1) * stepX
    val lastY = height - (((lastCandle.closePerGram - minPrice) / range) * height).toFloat()
    drawCircle(
      color = strokeColor,
      radius = 5.dp.toPx(),
      center = Offset(lastX, lastY)
    )
    drawCircle(
      color = Color.White,
      radius = 2.5.dp.toPx(),
      center = Offset(lastX, lastY)
    )
  }
}

@Composable
fun GoldHoldingCard(
  holding: GoldHoldingEntity,
  marketPrice24K: Double,
  marketSilverPrice9999: Double = 1.05,
  displayCurrency: String,
  iqdRate: Double,
  dateFormat: SimpleDateFormat,
  isKurdish: Boolean = false,
  sheenProgress: Float = 0f,
  onEdit: () -> Unit,
  onSell: () -> Unit = {},
  onReopen: () -> Unit = {},
  onDelete: () -> Unit,
  onClick: () -> Unit = {}
) {
  val isSilver = holding.isSilver
  val metalAccent = if (isSilver) SilverAccentBright else PhysicalGoldLuminous
  val holdingCardBrush = if (isSilver) SilverPlatinumIngotCardBrush else PhysicalGoldIngotCardBrush
  val holdingBorderBrush = if (isSilver) FineGoldInlayBorderBrush else PhysicalGoldBorderBrush

  val currentValueUsd = if (holding.isSold && holding.soldPriceUsd > 0.0) {
    holding.soldPriceUsd
  } else {
    holding.currentValue(marketPrice24K, marketSilverPrice9999)
  }
  val totalCostUsd = holding.totalCost
  val profitLossUsd = currentValueUsd - totalCostUsd
  val roi = if (totalCostUsd > 0) (profitLossUsd / totalCostUsd) * 100.0 else 0.0
  val isGain = profitLossUsd >= 0

  val currentValueDisplay = CurrencyService.convert(currentValueUsd, "USD", displayCurrency, iqdRate)
  val totalCostDisplay = CurrencyService.convert(totalCostUsd, "USD", displayCurrency, iqdRate)
  val profitLossDisplay = CurrencyService.convert(profitLossUsd, "USD", displayCurrency, iqdRate)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    modifier = Modifier
      .fillMaxWidth()
      .background(holdingCardBrush, RoundedCornerShape(20.dp))
      .border(1.3.dp, holdingBorderBrush, RoundedCornerShape(20.dp))
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .drawBullionSheen(sheenProgress)
      .testTag("gold_holding_card_${holding.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top Row: Badge + Title + Weight + Profit/Loss Chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                  if (isSilver) Color(0xFF283548) else Color(0xFF452E0B)
                )
                .border(
                  0.8.dp,
                  if (isSilver) FineGoldInlayBorderBrush else PhysicalGoldBorderBrush,
                  RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (isSilver) {
                  "🥈 ${holding.getDisplayPurityBadge(isKurdish)}"
                } else {
                  "🥇 ${holding.getDisplayPurityBadge(isKurdish)}"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = metalAccent
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = holding.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (holding.isSold) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(EmeraldLight.copy(alpha = 0.2f))
                  .border(0.8.dp, EmeraldLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (isKurdish) "+ کاش (فرۆتی)" else "SOLD (+Cash)",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldLight
                )
              }
            } else if (holding.deductedFromCash) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF38250A))
                  .border(0.8.dp, PhysicalGoldBorderBrush, RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (isKurdish) "− ژ کاشی" else "− From Cash",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = PhysicalGoldLuminous
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (isSilver) {
              "${holding.weightGrams}${if (isKurdish) " گرام" else "g"} (${"%.2f".format(holding.weightKilograms)} ${if (isKurdish) "کیلۆ" else "Kg"}) • ${dateFormat.format(Date(holding.purchaseDateMillis))}"
            } else {
              "${holding.weightGrams}${if (isKurdish) " گرام" else "g"} (${"%.1f".format(holding.weightMithqal)} ${if (isKurdish) "مسقال" else "Mithqal"}) • ${dateFormat.format(Date(holding.purchaseDateMillis))}"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFD4B982)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background((if (isGain) EmeraldLight else LossRed).copy(alpha = 0.20f))
            .border(0.8.dp, (if (isGain) EmeraldLight else LossRed).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${if (isGain) (if (isKurdish) "🟢 فایدە +" else "▲ +") else (if (isKurdish) "🔴 خسارە " else "▼ ")}${"%.2f".format(roi)}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isGain) EmeraldLight else LossRed
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Organized 3-Column Comparison Grid (سعرا کڕینێ | بهایێ نوکە | فایدە یان خسارە)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 1. Buy Price Box
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.40f))
            .border(0.7.dp, PhysicalGoldBorderWarm.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "سعرا کڕینێ" else "Buy Price",
              fontSize = 10.sp,
              color = Color(0xFFD4B982),
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (isSilver) {
                "$${"%,.2f".format(holding.buyPricePerKg)}/Kg"
              } else {
                "$${"%.2f".format(holding.effectiveBuyPricePerGram)}/g"
              },
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = CurrencyService.format(totalCostDisplay, displayCurrency),
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFC0A670),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // 2. Current Market Value Box
        val actualMarketGram = (if (isSilver) marketSilverPrice9999 else marketPrice24K) * holding.getPurityFactor()
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF452E0B).copy(alpha = 0.60f))
            .border(1.dp, PhysicalGoldBorderBrush, RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "بهایێ نوکە" else "Market Now",
              fontSize = 10.sp,
              color = PhysicalGoldReflective,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (isSilver) {
                "$${"%,.2f".format(actualMarketGram * 1000.0)}/Kg"
              } else {
                "$${"%.2f".format(actualMarketGram)}/g"
              },
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.ExtraBold,
              color = PhysicalGoldLuminous
            )
            Text(
              text = CurrencyService.format(currentValueDisplay, displayCurrency),
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PhysicalGoldReflective,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // 3. Net Profit / Loss Box
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background((if (isGain) EmeraldLight else LossRed).copy(alpha = 0.18f))
            .border(1.dp, (if (isGain) EmeraldLight else LossRed).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) {
                if (isGain) "ئەنجام: فایدە" else "ئەنجام: خسارە"
              } else {
                if (isGain) "Net Profit" else "Net Loss"
              },
              fontSize = 10.sp,
              color = if (isGain) EmeraldLight else LossRed,
              fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${if (isGain) "+" else ""}${CurrencyService.format(profitLossDisplay, displayCurrency)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Black,
              color = if (isGain) EmeraldLight else LossRed,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isGain) {
                if (isKurdish) "قازانج کریە" else "In Profit"
              } else {
                if (isKurdish) "زەرەر کریە" else "In Loss"
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (isGain) EmeraldLight else LossRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Bottom Action Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .heightIn(min = 36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color.Black.copy(alpha = 0.40f))
              .border(0.8.dp, PhysicalGoldBorderBrush, RoundedCornerShape(10.dp))
              .clickable { onEdit() }
              .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Edit, contentDescription = null, tint = PhysicalGoldLuminous, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = if (isKurdish) "دەستکاری" else "Edit",
                fontSize = 11.5.sp,
                color = PhysicalGoldReflective,
                fontWeight = FontWeight.Bold
              )
            }
          }

          if (!holding.isSold) {
            Box(
              modifier = Modifier
                .heightIn(min = 36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EmeraldLight)
                .clickable { onSell() }
                .padding(horizontal = 12.dp, vertical = 7.dp)
                .testTag("sell_gold_holding_button_${holding.id}"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = if (isKurdish) "فرۆتن (+ کاش)" else "Sell (+ Cash)",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF0F172A)
                )
              }
            }
          } else {
            Box(
              modifier = Modifier
                .heightIn(min = 36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.40f))
                .border(0.8.dp, PhysicalGoldBorderWarm.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .clickable { onReopen() }
                .padding(horizontal = 12.dp, vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isKurdish) "زڤڕاندن بۆ چالاک" else "Reopen Active",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = PhysicalGoldReflective
              )
            }
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(LossRed.copy(alpha = 0.15f))
            .border(0.8.dp, LossRed.copy(alpha = 0.4f), CircleShape)
        ) {
          Icon(
            Icons.Default.DeleteOutline,
            contentDescription = if (isKurdish) "ژێبرن" else "Delete",
            tint = LossRed,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}


@Composable
fun AddEditGoldDialog(
  holding: GoldHoldingEntity?,
  initialMetalType: String = "GOLD",
  defaultGoldPrice24K: Double,
  defaultSilverPrice9999: Double = 1.05,
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (
    metalType: String,
    title: String,
    grams: Double,
    pricePerGram: Double,
    karatOrPurity: Int,
    notes: String,
    deductedFromCash: Boolean
  ) -> Unit
) {
  var selectedMetal by remember {
    mutableStateOf(holding?.metalType ?: initialMetalType)
  }
  var deductFromCash by remember {
    mutableStateOf(holding?.deductedFromCash ?: true)
  }
  var entryCurrency by remember { mutableStateOf("USD") }
  var entryIqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  val activeEntryRate = CurrencyService.normalizeIqdRatePerUsd(
    entryIqdRateInput.toDoubleOrNull() ?: currentIqdRate,
    currentIqdRate
  )
  val isSilver = selectedMetal.equals("SILVER", ignoreCase = true)
  val activeColor = if (isSilver) SilverAccent else GoldAccent

  var title by remember(selectedMetal) {
    mutableStateOf(
      holding?.title ?: if (isSilver) {
        if (isKurdish) "زیڤێ خالس 9999" else "Fine Silver 9999"
      } else {
        if (isKurdish) "زێڕێ عەیار ٢٤" else "Physical Gold Bullion"
      }
    )
  }
  var weightGramsInput by remember(selectedMetal) {
    mutableStateOf(
      holding?.weightGrams?.toString() ?: if (isSilver) "1000.0" else "50.0"
    )
  }
  var buyPriceInput by remember(selectedMetal) {
    mutableStateOf(
      if (holding != null) {
        "%.4f".format(Locale.US, holding.effectiveBuyPricePerGram)
      } else if (isSilver) {
        "%.4f".format(Locale.US, defaultSilverPrice9999)
      } else {
        "%.1f".format(Locale.US, defaultGoldPrice24K)
      }
    )
  }
  var buyPricePerKgInput by remember(selectedMetal) {
    mutableStateOf(
      if (holding != null) {
        "%.2f".format(Locale.US, holding.buyPricePerKg)
      } else {
        "%.2f".format(Locale.US, defaultSilverPrice9999 * 1000.0)
      }
    )
  }
  var selectedKarat by remember(selectedMetal) {
    mutableIntStateOf(
      holding?.karat ?: if (isSilver) 9999 else 24
    )
  }
  var notes by remember {
    mutableStateOf(holding?.notes ?: "")
  }

  val grams = weightGramsInput.toDoubleOrNull() ?: 0.0
  val rawPriceEntered = buyPriceInput.toDoubleOrNull() ?: 0.0
  val price = if (entryCurrency == "IQD") {
    CurrencyService.convert(rawPriceEntered, "IQD", "USD", activeEntryRate)
  } else {
    rawPriceEntered
  }
  val calculatedTotalBuyUsd = grams * price
  val purityFactor = if (isSilver) {
    if (selectedKarat == 925) 0.925 else 1.0
  } else {
    when (selectedKarat) {
      24 -> 1.0
      22 -> 22.0 / 24.0
      21 -> 21.0 / 24.0
      18 -> 18.0 / 24.0
      else -> 1.0
    }
  }
  val baseSpot = if (isSilver) defaultSilverPrice9999 else defaultGoldPrice24K
  val currentMarketGramUsd = baseSpot * purityFactor
  val calculatedCurrentValUsd = grams * currentMarketGramUsd
  val previewProfitLossUsd = calculatedCurrentValUsd - calculatedTotalBuyUsd
  val isPreviewProfit = previewProfitLossUsd >= 0.0

  val dialogBg = if (isSilver) Color(0xFF1B2430) else Color(0xFF2B1C06)
  val dialogBorder = if (isSilver) FineGoldInlayBorderBrush else PhysicalGoldBorderBrush

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = dialogBg,
    titleContentColor = if (isSilver) SilverAccentBright else PhysicalGoldReflective,
    textContentColor = Color(0xFFE2E8F0),
    shape = RoundedCornerShape(22.dp),
    modifier = Modifier.border(1.2.dp, dialogBorder, RoundedCornerShape(22.dp)),
    title = {
      Text(
        text = if (holding == null) {
          if (isSilver) {
            if (isKurdish) "زێدەکرنا زیڤی (عەیار 9999)" else "Add Silver 9999 Holding"
          } else {
            if (isKurdish) "زێدەکرنا زێڕێ نوی" else "Add Gold Holding"
          }
        } else {
          if (isKurdish) "دەستکاریکردنا تۆمارێ" else "Edit Holding"
        },
        fontWeight = FontWeight.ExtraBold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Metal Type Selector (🥇 زێڕ | 🥈 زیڤ 9999)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          VaultSegmentTab(
            title = if (isKurdish) "🥇 زێڕ (Gold)" else "🥇 Gold",
            selected = !isSilver,
            activeColor = GoldAccent,
            onClick = { selectedMetal = "GOLD" },
            modifier = Modifier.weight(1f)
          )
          VaultSegmentTab(
            title = if (isKurdish) "🥈 زیڤ 9999 (Silver)" else "🥈 Silver 9999",
            selected = isSilver,
            activeColor = SilverAccent,
            onClick = { selectedMetal = "SILVER" },
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = {
            Text(
              if (isSilver) {
                if (isKurdish) "ناڤێ زیڤی (بۆ نموونە: زیڤێ 9999)" else "Silver Holding Title"
              } else {
                if (isKurdish) "ناڤێ زێڕی" else "Gold Holding Title"
              }
            )
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Karat / Purity Selector
        Text(
          text = if (isSilver) {
            if (isKurdish) "عەیارێ زیڤی هەڵبژێره:" else "Select Silver Purity:"
          } else {
            if (isKurdish) "عەیارێ زێڕی هەڵبژێره:" else "Select Gold Karat:"
          },
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold
        )

        if (isSilver) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(9999 to (if (isKurdish) "9999 (خالس)" else "9999 Fine"), 925 to (if (isKurdish) "925 (ستێرلینگ)" else "925 Sterling")).forEach { (purityVal, label) ->
              Button(
                onClick = { selectedKarat = purityVal },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (selectedKarat == purityVal) SilverAccent else MaterialTheme.colorScheme.surfaceVariant,
                  contentColor = if (selectedKarat == purityVal) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(24, 22, 21, 18).forEach { k ->
              Button(
                onClick = { selectedKarat = k },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (selectedKarat == k) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                  contentColor = if (selectedKarat == k) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("${k}K", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Weight Input with Quick Selection Pills
        OutlinedTextField(
          value = weightGramsInput,
          onValueChange = { weightGramsInput = it },
          label = { Text(if (isKurdish) "کێش ب گرام (Grams)" else "Weight in Grams") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gold_weight_input")
        )

        val weightPresets = if (isSilver) {
          listOf("100" to "100g", "250" to "250g", "500" to "500g", "1000" to "1 Kg", "5000" to "5 Kg")
        } else {
          listOf("5" to "1M (5g)", "10" to "2M (10g)", "50" to "50g", "100" to "100g", "210" to "210g")
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          weightPresets.forEach { (valStr, label) ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                  if (weightGramsInput == valStr) activeColor else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { weightGramsInput = valStr }
                .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (weightGramsInput == valStr) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Buy Price Input + 1-Tap Global Spot Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isSilver) {
              if (isKurdish) "سعرا کڕینێ (ب ١ کیلۆ یان ١ گرام USD):" else "Silver Buy Price (1 Kg or 1g USD):"
            } else {
              if (isKurdish) "سعرا کڕینێ بۆ هەر گرامەکێ (USD):" else "Buy Price per Gram (USD):"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (isSilver) {
              if (isKurdish) {
                "⚡ سعرا عالمی ($${"%,.0f".format(currentMarketGramUsd * 1000.0)}/Kg)"
              } else {
                "⚡ Global Spot ($${"%,.0f".format(currentMarketGramUsd * 1000.0)}/Kg)"
              }
            } else {
              if (isKurdish) {
                "⚡ سعرا نوکە ($${"%.2f".format(currentMarketGramUsd)})"
              } else {
                "⚡ Current Spot ($${"%.2f".format(currentMarketGramUsd)})"
              }
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = activeColor,
            modifier = Modifier.clickable {
              buyPriceInput = if (isSilver) "%.4f".format(Locale.US, currentMarketGramUsd) else "%.2f".format(Locale.US, currentMarketGramUsd)
              buyPricePerKgInput = "%.2f".format(Locale.US, currentMarketGramUsd * 1000.0)
            }
          )
        }

        if (isSilver) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = buyPricePerKgInput,
              onValueChange = { newKg ->
                buyPricePerKgInput = newKg
                val kgVal = newKg.toDoubleOrNull()
                if (kgVal != null) {
                  buyPriceInput = "%.4f".format(Locale.US, kgVal / 1000.0)
                }
              },
              label = {
                Text(if (isKurdish) "بهایێ ١ کیلۆ (USD/Kg)" else "1 Kg Price (USD/Kg)", fontSize = 11.sp)
              },
              singleLine = true,
              modifier = Modifier
                .weight(1.1f)
                .testTag("silver_buy_price_kg_input")
            )

            OutlinedTextField(
              value = buyPriceInput,
              onValueChange = { newG ->
                buyPriceInput = newG
                val gVal = newG.toDoubleOrNull()
                if (gVal != null) {
                  buyPricePerKgInput = "%.2f".format(Locale.US, gVal * 1000.0)
                }
              },
              label = {
                Text(if (isKurdish) "بهایێ ١ گرام (USD/g)" else "1g Price (USD/g)", fontSize = 11.sp)
              },
              singleLine = true,
              modifier = Modifier
                .weight(0.9f)
                .testTag("gold_buy_price_input")
            )
          }
        } else {
          OutlinedTextField(
            value = buyPriceInput,
            onValueChange = { buyPriceInput = it },
            label = {
              Text(if (isKurdish) "سعرا کڕینێ بۆ ١ گرام زێڕ ($entryCurrency)" else "Gold Buy Price per Gram ($entryCurrency)")
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("gold_buy_price_input")
          )
        }

        // USD / IQD Currency Toggle + Dinar & Dollar Exchange Rate Card
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            Button(
              onClick = { entryCurrency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (entryCurrency == cur) activeColor else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = cur,
                color = if (entryCurrency == cur) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = grams * rawPriceEntered,
          selectedCurrency = entryCurrency,
          isKurdish = isKurdish,
          testTag = "gold_entry_iqd_rate_input"
        )

        // Live Preview: سعرا کڕینێ vs بهایێ نوکە (فایدە یان خسارە)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background((if (isPreviewProfit) EmeraldLight else LossRed).copy(alpha = 0.14f))
            .border(1.dp, (if (isPreviewProfit) EmeraldLight else LossRed).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isKurdish) "سعرا کڕینێ: $${"%.2f".format(calculatedTotalBuyUsd)}" else "Buy Cost: $${"%.2f".format(calculatedTotalBuyUsd)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isKurdish) "بهایێ نوکە: $${"%.2f".format(calculatedCurrentValUsd)}" else "Now: $${"%.2f".format(calculatedCurrentValUsd)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isPreviewProfit) {
                  if (isKurdish) "🟢 ئەنجام: فایدە (قازانج)" else "🟢 Result: Profit"
                } else {
                  if (isKurdish) "🔴 ئەنجام: خسارە (زەرەر)" else "🔴 Result: Loss"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isPreviewProfit) EmeraldLight else LossRed
              )
              Text(
                text = "${if (isPreviewProfit) "+" else ""}$${"%.2f".format(previewProfitLossUsd)} USD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isPreviewProfit) EmeraldLight else LossRed
              )
            }
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isKurdish) "تێبینی" else "Notes / Vault Location") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Deduct from Cash Wealth Toggle (بشتی ئەڤرۆ کێمکرن ژ سەروەتێ کاشی)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (deductFromCash) EmeraldLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isKurdish) "💵 کێمکرن ژ سەروەتێ کاشی" else "💵 Deduct from Cash Wealth",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (deductFromCash) EmeraldLight else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isKurdish) "بهایێ کڕینا زێڕ/زیڤی ژ کاشی کێم دکەت" else "Deduct buy cost from your Cash Wealth",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = deductFromCash,
            onCheckedChange = { deductFromCash = it },
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (grams > 0 && price > 0) {
            onConfirm(selectedMetal, title, grams, price, selectedKarat, notes, deductFromCash)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = activeColor,
          contentColor = Color(0xFF0F172A)
        ),
        modifier = Modifier.testTag("confirm_gold_holding_button")
      ) {
        Text(
          text = if (isKurdish) "تۆمارکرن" else "Save Holding",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

@Composable
fun SilverKgGlobalPriceDialog(
  currentSilverGram9999: Double,
  liveQuoteSilverGram9999: Double,
  isCustomSimulated: Boolean,
  displayCurrency: String,
  iqdRate: Double,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onUseLiveGlobal: () -> Unit,
  onSaveCustomKgPrice: (Double) -> Unit
) {
  val currentKgUsd = currentSilverGram9999 * 1000.0
  val liveApiKgUsd = liveQuoteSilverGram9999 * 1000.0
  var kgPriceInput by remember(currentKgUsd) {
    mutableStateOf("%.2f".format(Locale.US, currentKgUsd))
  }
  val parsedKg = kgPriceInput.toDoubleOrNull() ?: currentKgUsd
  val parsedGram = parsedKg / 1000.0
  val parsedOz = parsedGram * 31.1034768

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF1B2430),
    titleContentColor = SilverAccentBright,
    textContentColor = Color(0xFFE2E8F0),
    shape = RoundedCornerShape(22.dp),
    modifier = Modifier.border(1.2.dp, FineGoldInlayBorderBrush, RoundedCornerShape(22.dp)),
    title = {
      Text(
        text = if (isKurdish) "🥈 بهایێ ١ کیلۆ زیڤێ عالمی (9999)" else "🥈 Global 1 Kg Silver 9999 Price",
        fontWeight = FontWeight.ExtraBold
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = if (isKurdish) {
            "بهایێ ١ کیلۆ زیڤێ خالس (1000 گرام = 32.15 ئۆنس) ڕاستەوخۆ ب سعرا بۆرسا جیهانی (XAG/USD) دهێتە هژمارتن. هەروەسا دشێی بهایێ ١ کیلۆ ب دەست دیاربکەی:"
          } else {
            "1 Kg of Fine Silver 9999 (1,000g = 32.1507 Troy Oz) is synced with the live global XAG/USD spot market. You can also calibrate the 1 Kg price below:"
          },
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Live Global API Feed Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EmeraldLight.copy(alpha = 0.12f))
            .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onUseLiveGlobal() }
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (isKurdish) "🌐 سعرا بۆرسا جیهانی یا ڕاستەوخۆ" else "🌐 Live Global Market Spot (XAG/USD)",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLight
              )
              Text(
                text = "$${"%,.2f".format(liveApiKgUsd)} / Kg ($${"%.2f".format(liveQuoteSilverGram9999)}/g)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldLight)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (isKurdish) "🔄 عالمی" else "🔄 Sync Live",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
              )
            }
          }
        }

        OutlinedTextField(
          value = kgPriceInput,
          onValueChange = { kgPriceInput = it },
          label = {
            Text(if (isKurdish) "بهایێ ١ کیلۆ زیڤێ 9999 ب دۆلار (USD / 1 Kg)" else "1 Kg Silver 9999 Price (USD)")
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("silver_custom_1kg_input")
        )

        // Quick Global Presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(1965.0 to "$1,965", 2006.0 to "$2,006", 2050.0 to "$2,050", 2090.0 to "$2,090").forEach { (presetKg, label) ->
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { kgPriceInput = "%.2f".format(Locale.US, presetKg) }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Breakdown Preview
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "بهایێ ١ کیلۆ (1000g):" else "1 Kilogram (1000g):", fontSize = 11.sp)
              Text("$${"%,.2f".format(parsedKg)} USD", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "بهایێ ١ گرام (9999):" else "1 Gram (9999):", fontSize = 11.sp)
              Text("$${"%.4f".format(parsedGram)} USD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "ئۆنسا جیهانی (31.10g):" else "Troy Ounce (31.10g):", fontSize = 11.sp)
              Text("$${"%.2f".format(parsedOz)} USD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (displayCurrency == "IQD") {
              val iqdKg = CurrencyService.convert(parsedKg, "USD", "IQD", iqdRate)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isKurdish) "ب دینارێ عێراقی:" else "In IQD:", fontSize = 11.sp)
                Text(CurrencyService.format(iqdKg, "IQD"), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = SilverAccentBright)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (parsedKg > 0.0) {
            onSaveCustomKgPrice(parsedKg)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = SilverAccent,
          contentColor = Color(0xFF0F172A)
        )
      ) {
        Text(
          text = if (isKurdish) "چەسپاندنا سعرا ١ کیلۆ" else "Save 1 Kg Price",
          fontWeight = FontWeight.ExtraBold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(if (isKurdish) "داخستن" else "Close")
      }
    }
  )
}

@Composable
fun SellGoldSilverDialog(
  holding: GoldHoldingEntity,
  marketPrice24K: Double,
  marketSilverPrice9999: Double,
  displayCurrency: String,
  iqdRate: Double,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (soldPriceUsd: Double, notes: String) -> Unit
) {
  val currentSpotUsd = holding.currentValue(marketPrice24K, marketSilverPrice9999)
  var saleCurrency by remember { mutableStateOf("USD") }
  var entryIqdRateInput by remember(iqdRate) { mutableStateOf(iqdRate.toInt().toString()) }
  val activeEntryRate = CurrencyService.normalizeIqdRatePerUsd(
    entryIqdRateInput.toDoubleOrNull() ?: iqdRate,
    iqdRate
  )
  var soldPriceInput by remember {
    mutableStateOf("%.2f".format(Locale.US, currentSpotUsd))
  }
  var notesInput by remember { mutableStateOf("") }
  val rawEnteredSale = soldPriceInput.toDoubleOrNull() ?: 0.0
  val parsedSaleUsd = if (saleCurrency == "IQD") {
    CurrencyService.convert(rawEnteredSale, "IQD", "USD", activeEntryRate)
  } else {
    rawEnteredSale
  }
  val profitLossUsd = parsedSaleUsd - holding.totalCost
  val isGain = profitLossUsd >= 0.0
  val saleDisplay = CurrencyService.convert(parsedSaleUsd, "USD", displayCurrency, activeEntryRate)

  val dialogBg = if (holding.isSilver) Color(0xFF1B2430) else Color(0xFF2B1C06)
  val dialogBorder = if (holding.isSilver) FineGoldInlayBorderBrush else PhysicalGoldBorderBrush

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = dialogBg,
    titleContentColor = if (holding.isSilver) SilverAccentBright else PhysicalGoldReflective,
    textContentColor = Color(0xFFE2E8F0),
    shape = RoundedCornerShape(22.dp),
    modifier = Modifier.border(1.2.dp, dialogBorder, RoundedCornerShape(22.dp)),
    title = {
      Text(
        text = if (holding.isSilver) {
          if (isKurdish) "💵 فرۆتنا زیڤی (+ کاش)" else "💵 Sell Silver (+ Cash)"
        } else {
          if (isKurdish) "💵 فرۆتنا زێڕی (+ کاش)" else "💵 Sell Gold (+ Cash)"
        },
        fontWeight = FontWeight.ExtraBold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "${holding.title} (${holding.weightGrams}g)",
          fontWeight = FontWeight.Bold,
          color = if (holding.isSilver) SilverAccent else GoldAccent
        )
        Text(
          text = if (isKurdish) {
            "بهایێ فرۆتنێ ڕاستەوخۆ ل سەروەتێ کاشی زێدە دبیت."
          } else {
            "Proceeds from selling this holding are directly added to your Cash Wealth."
          },
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = soldPriceInput,
          onValueChange = { soldPriceInput = it },
          label = { Text(if (isKurdish) "کۆیێ بهایێ فرۆتنێ ($saleCurrency)" else "Total Sale Price ($saleCurrency)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gold_sold_price_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            Button(
              onClick = { saleCurrency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (saleCurrency == cur) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = cur,
                color = if (saleCurrency == cur) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = iqdRate,
          enteredAmount = rawEnteredSale,
          selectedCurrency = saleCurrency,
          isKurdish = isKurdish,
          testTag = "sell_gold_iqd_rate_input"
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background((if (isGain) EmeraldLight else LossRed).copy(alpha = 0.14f))
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "زێدەبوون بۆ سەروەتێ کاش:" else "Added to Cash Wealth:", fontSize = 11.sp)
              Text(
                text = "+${CurrencyService.format(saleDisplay, displayCurrency)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLight
              )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "فایدە / خسارە (USD):" else "Net Profit / Loss (USD):", fontSize = 11.sp)
              Text(
                text = "${if (isGain) "+" else ""}$${"%.2f".format(profitLossUsd)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGain) EmeraldLight else LossRed
              )
            }
          }
        }
        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینیێن فرۆتنێ" else "Sale Notes") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (parsedSaleUsd > 0.0) {
            onConfirm(parsedSaleUsd, notesInput)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = EmeraldLight,
          contentColor = Color(0xFF0F172A)
        ),
        modifier = Modifier.testTag("confirm_sell_gold_button")
      ) {
        Text(
          text = if (isKurdish) "پشتڕاستکرنا فرۆتنێ (+ کاش)" else "Confirm Sale (+ Cash)",
          fontWeight = FontWeight.ExtraBold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}
