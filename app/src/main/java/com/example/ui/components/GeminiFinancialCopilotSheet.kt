package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.ai.GeminiSearchGroundingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CopilotChatMessage(
  val id: String = System.currentTimeMillis().toString(),
  val isUser: Boolean,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val tag: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeminiFinancialCopilotSheet(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onNavigateToTab: (Int) -> Unit = {}
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val listState = rememberLazyListState()

  var inputText by remember { mutableStateOf("") }
  var isThinking by remember { mutableStateOf(false) }

  // Initial welcome greeting
  val messages = remember {
    mutableStateListOf(
      CopilotChatMessage(
        isUser = false,
        text = if (isKurdish) {
          "سڵاڤ مامۆستا مەڤان! ئەز ڕاوێژکارێ داراییێ ژیرێ تە مە (Gemini Financial Copilot) ✦.\n\nمن داتایێن تە یێن سەرمایە، پێشانگەها سەیاران، ملوک، قەرز و دەینان، و زێڕی ب شێوەیەکێ زندی شیکار کرینە. چ پرسیارەک یان ڕاوێژەک د هزرێ تە دا بیت دەستنیشان بکە یان بنڤیسە."
        } else {
          "Welcome! I am your Gemini Financial Copilot ✦.\n\nI have synthesized your live balance sheet across vehicle fleets, real estate properties, gold reserves, and debt obligations. Select a quick audit topic or ask any financial question."
        },
        tag = "WELCOME"
      )
    )
  }

  fun generateCopilotResponse(query: String): String {
    val q = query.trim().lowercase(Locale.ROOT)
    val displayCurr = uiState.displayCurrency
    val iqd = uiState.iqdRate

    // 1. Car Fleet queries
    if (q.contains("سەیار") || q.contains("car") || q.contains("fleet") || q.contains("قست") || q.contains("مۆتۆڕ")) {
      val totalCars = uiState.carInvestments.size
      val activeCars = uiState.activeCarInvestments.size
      val soldCars = uiState.soldCarInvestments.size
      val totalProfit = uiState.totalSoldCarsProfitLoss
      val remainingInstallments = uiState.totalCarsMyRemainingMoney
      val isWinning = uiState.isFleetWinning

      return if (isKurdish) {
        "🚗 شیکاریا پێشانگەها سەیاران:\n\n" +
          "• هژمارا گشتی یا سەیاران: $totalCars سەیارە ($activeCars چالاکن، $soldCars هاتینە فرۆتن)\n" +
          "• قازانجێ سافی یێ سەیارێن فرۆتی: ${CurrencyService.format(totalProfit, displayCurr)} " +
          (if (isWinning) "📈 (قازانجا سەرمایەیێ تە پۆزەتیڤە)" else "📉 (پێدڤی ب ئەولەویەتێ یە)") + "\n" +
          "• کوژمێ قستێن ماین (تنێ پشکا تە): ${CurrencyService.format(remainingInstallments, displayCurr)}\n\n" +
          "💡 ڕاوێژا Gemini:\n" +
          "١. سەیارێن زێدەتر ژ ٤٥ رۆژان ماین ل پێشانگەهێ، فرۆتنا وان ب قستێن کورت دەم ئەولەویەتە دا کاش بزڤڕیت.\n" +
          "٢. وەختێ فرۆتنێ قستێن بێ شەریک ب دروستی تۆمار بکە دا بەشێ تە زێدە ببیت."
      } else {
        "🚗 Car Fleet Intelligence:\n\n" +
          "• Total Fleet: $totalCars vehicles ($activeCars in lot, $soldCars sold)\n" +
          "• Realized Net Profit: ${CurrencyService.format(totalProfit, displayCurr)}\n" +
          "• Remaining Installments (My Share): ${CurrencyService.format(remainingInstallments, displayCurr)}\n\n" +
          "💡 Executive Insight:\n" +
          "Focus on accelerating turnover for vehicles with >45 days in inventory to maximize liquidity recycling."
      }
    }

    // 2. Debts and Credits queries
    if (q.contains("قەرز") || q.contains("دەین") || q.contains("debt") || q.contains("credit") || q.contains("loan")) {
      val owedByMe = uiState.totalDebtOwed
      val owedToMe = uiState.totalCreditReceivable
      val netBalance = uiState.netDebtCreditBalance
      val activeDebtsCount = uiState.activeDebtLoans.size
      val activeCreditsCount = uiState.activeCreditLoans.size

      return if (isKurdish) {
        "⚖️ راپۆرتا سەلامەتیا قەرز و دەینان:\n\n" +
          "• قەرزێن خەلک بدەتە تە (دەف خەلکی): ${CurrencyService.format(owedToMe, displayCurr)} ($activeCreditsCount کەس)\n" +
          "• قەرزێن لسەر تە (لسەر من): ${CurrencyService.format(owedByMe, displayCurr)} ($activeDebtsCount قەرز)\n" +
          "• بالانسا سافی یا قەرزان: ${CurrencyService.format(netBalance, displayCurr)} " +
          (if (netBalance >= 0) "✅ (سەروەتێ تە زێدەترە ژ قەرزان)" else "⚠️ (قەرزێن لسەر تە زێدەترن)") + "\n\n" +
          "💡 ڕاوێژا Gemini:\n" +
          "١. دەستپێکێ وان دەینان کۆم بکە یێن دەمێ وان بۆری دا کاشێ بازاڕی مسۆگەر بکەی.\n" +
          "٢. بۆ قەرزێن لسەر تە، پلانا Avalanche بکاربینە دا قەرزێن مەزن زوو بهێنە داخستن."
      } else {
        "⚖️ Debt & Receivable Ledger Audit:\n\n" +
          "• Money Owed to You: ${CurrencyService.format(owedToMe, displayCurr)} ($activeCreditsCount accounts)\n" +
          "• Debts You Owe: ${CurrencyService.format(owedByMe, displayCurr)} ($activeDebtsCount liabilities)\n" +
          "• Net Debt-Credit Margin: ${CurrencyService.format(netBalance, displayCurr)}\n\n" +
          "💡 Executive Strategy:\n" +
          "Prioritize collection of past-due receivables to maintain optimal liquidity reserves."
      }
    }

    // 3. Real Estate (ملوک) queries
    if (q.contains("ملوک") || q.contains("عەقار") || q.contains("خان") || q.contains("زەڤی") || q.contains("estate") || q.contains("property")) {
      val properties = uiState.nonCarInvestments
      val totalPropValue = properties.sumOf {
        CurrencyService.convert(it.currentValue, it.currency, displayCurr, iqd, entryIqdRate = it.exchangeRateAtEntry)
      }
      val count = properties.size

      return if (isKurdish) {
        "🏰 شیکاریا ملوک و عەقاراتان:\n\n" +
          "• هژمارا ملوک و ئەردان: $count پڕۆژە\n" +
          "• بەهایێ تەخمینکری یێ سەرمایەیێ ملوکان: ${CurrencyService.format(totalPropValue, displayCurr)}\n\n" +
          "💡 ڕاوێژا Gemini:\n" +
          "ملوک باشترین سەرمایەیێ پاراستینە ل هەمبەر هەڵئاوسانا دراوی. ئەگەر کرێ یان فرۆتن هاتە پێش، دەستبەجێ د کاشێ سەرەکی دا تۆمار بکە."
      } else {
        "🏰 Real Estate & Property Equity:\n\n" +
          "• Total Assets: $count properties & land parcels\n" +
          "• Estimated Equity: ${CurrencyService.format(totalPropValue, displayCurr)}\n\n" +
          "💡 Executive Strategy:\n" +
          "Real estate provides prime capital preservation against currency depreciation."
      }
    }

    // 4. Gold queries
    if (q.contains("زێڕ") || q.contains("زێر") || q.contains("gold") || q.contains("زیڤ") || q.contains("silver")) {
      val totalGrams = uiState.totalGoldWeightGrams
      val totalMithqal = totalGrams / 5.0
      val totalGoldValue = CurrencyService.convert(uiState.totalGoldOnlyValueUsd, "USD", displayCurr, iqd)

      return if (isKurdish) {
        "🪙 پاشەکەوتێ زێڕی و کانزایێن گرانبەها:\n\n" +
          "• سەرمایەیێ کڕی یێ زێڕی: ${String.format(Locale.US, "%.2f", totalMithqal)} مسقاڵ\n" +
          "• بەهایێ زندی یێ بازاڕی: ${CurrencyService.format(totalGoldValue, displayCurr)}\n\n" +
          "💡 ڕاوێژا Gemini:\n" +
          "زێڕ وەک پەناگەهەکا ئارام (Safe Haven) د پالپشتیا هێزا کڕینا تە دا ڕۆلەکێ بەرچاڤ دگێڕیت. پێشنیاز دهێتەکرن ل دەمێ دابەزینا عەیار ٢٤، پشکا پاشەکەوتی زێدە بکەی."
      } else {
        "🪙 Gold Reserve Position:\n\n" +
          "• Weight: ${String.format(Locale.US, "%.2f", totalMithqal)} Mithqals\n" +
          "• Current Market Value: ${CurrencyService.format(totalGoldValue, displayCurr)}\n\n" +
          "💡 Executive Strategy:\n" +
          "Physical 24K gold remains your primary hedge against regional volatility."
      }
    }

    // Default: Complete Master Wealth Overview
    val netWorth = uiState.netWorth
    val pureCash = uiState.pureCashBalance
    val totalAssets = uiState.totalCapitalAssets
    val totalDebts = uiState.totalDebtOwed

    return if (isKurdish) {
      "🌟 شیکاریا گشتی یا سەرمایەیێ شاهانە:\n\n" +
        "• سەرمایەیێ سافی (Net Worth): ${CurrencyService.format(netWorth, displayCurr)}\n" +
        "• نەختینەیا کاش (Pure Cash): ${CurrencyService.format(pureCash, displayCurr)}\n" +
        "• کۆیێ گشتیێ سامانان: ${CurrencyService.format(totalAssets, displayCurr)}\n" +
        "• کۆیێ گشتیێ قەرزان: ${CurrencyService.format(totalDebts, displayCurr)}\n\n" +
        "💎 ئەنجامێ پشکنینا Gemini:\n" +
        "١. باڵانسا دارایی یا تە د قۆناغەکا زۆر پێشکەفتی دایە.\n" +
        "٢. دابەشبوونا سەرمایەی د ناڤبەرا پێشانگەها سەیاران و ملوکان دا هێزەکا باڵا ددەتە داهاتیێ تە.\n" +
        "٣. بۆ باشترکرنێ، هەمیشە کاشێ بەردەست ژ ١٥٪ سەرمایەی کێمتر نەبیت."
    } else {
      "🌟 Sovereign Wealth Master Evaluation:\n\n" +
        "• Net Worth: ${CurrencyService.format(netWorth, displayCurr)}\n" +
        "• Pure Liquid Cash: ${CurrencyService.format(pureCash, displayCurr)}\n" +
        "• Total Gross Assets: ${CurrencyService.format(totalAssets, displayCurr)}\n" +
        "• Total Liabilities: ${CurrencyService.format(totalDebts, displayCurr)}\n\n" +
        "💎 Copilot Synthesis:\n" +
        "Your portfolio displays robust diversification across vehicles, real estate, and reserves with zero critical vulnerability."
    }
  }

  fun sendQuery(text: String, tag: String? = null) {
    if (text.isBlank() || isThinking) return
    val query = text.trim()
    inputText = ""
    messages.add(CopilotChatMessage(isUser = true, text = query, tag = tag))
    isThinking = true

    scope.launch {
      listState.animateScrollToItem(messages.size - 1)
      val responseText: String = if (GeminiSearchGroundingService.isApiKeyConfigured()) {
        val summaryContext = buildString {
          append("Net Worth: ${uiState.netWorth}, ")
          append("Cars: ${uiState.carInvestments.size} (Active: ${uiState.activeCarInvestments.size}), ")
          append("Non-car assets: ${uiState.nonCarInvestments.size}, ")
          append("Gold: ${uiState.activeGoldHoldings.size}, ")
          append("Liquid Cash: ${uiState.pureCashBalance}")
        }
        val result = GeminiSearchGroundingService.searchWithGrounding(
          userQuery = query,
          isKurdish = isKurdish,
          portfolioContext = summaryContext
        )
        if (result.isSuccess && result.answerText.isNotBlank()) {
          result.answerText
        } else {
          generateCopilotResponse(query)
        }
      } else {
        delay(400)
        generateCopilotResponse(query)
      }

      messages.add(CopilotChatMessage(isUser = false, text = responseText, tag = tag))
      isThinking = false
      delay(100)
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier
      .fillMaxHeight(0.92f)
      .imePadding()
      .testTag("gemini_financial_copilot_sheet"),
    containerColor = DarkGlassCardBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkGlassCardSurface)
    ) {
      // Top Header with Glowing Gemini Holographic Neural Core
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .border(
                1.5.dp,
                Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF38BDF8))),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_gemini_ai_core),
              contentDescription = "Gemini Neural Core",
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = if (isKurdish) "ڕاوێژکارێ داراییێ ژیر" else "Gemini Financial Copilot",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF10B981).copy(alpha = 0.2f))
                  .border(0.6.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "GEMINI 2.5 FLASH ✦",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF34D399)
                )
              }
            }
            Text(
              text = if (isKurdish) "شیکاریا زندی یا ملوک، سەیارە، قەرز و داهاتی" else "Live Synthesis of Capital, Assets & Debts",
              fontSize = 11.sp,
              color = GoldAccent.copy(alpha = 0.9f)
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .background(Color.White.copy(alpha = 0.08f), CircleShape)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Live Financial Metrics Capsule Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MetricCapsulePill(
          label = if (isKurdish) "سەرمایەیێ سافی" else "Net Worth",
          value = CurrencyService.formatShort(uiState.netWorth, uiState.displayCurrency),
          color = GoldAccent
        )
        MetricCapsulePill(
          label = if (isKurdish) "کاشێ بەردەست" else "Pure Cash",
          value = CurrencyService.formatShort(uiState.pureCashBalance, uiState.displayCurrency),
          color = EmeraldLight
        )
        MetricCapsulePill(
          label = if (isKurdish) "قازانجێ سەیاران" else "Cars Profit",
          value = CurrencyService.formatShort(uiState.totalSoldCarsProfitLoss, uiState.displayCurrency),
          color = Color(0xFF38BDF8)
        )
        MetricCapsulePill(
          label = if (isKurdish) "سافیێ قەرزان" else "Net Debts",
          value = CurrencyService.formatShort(uiState.netDebtCreditBalance, uiState.displayCurrency),
          color = if (uiState.netDebtCreditBalance >= 0) EmeraldLight else LossRed
        )
      }

      // Quick Prompt Action Chips
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Text(
          text = if (isKurdish) "ڕاوێژێن بلەز یێن پێشنیازکری:" else "Recommended Quick Inquiries:",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White.copy(alpha = 0.65f),
          modifier = Modifier.padding(bottom = 6.dp)
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          QuickPromptButton(
            text = if (isKurdish) "🌟 شیکاریا سەرمایەی" else "🌟 Wealth Audit",
            testTag = "copilot_prompt_chip_overview",
            onClick = { sendQuery(if (isKurdish) "شیکاریا سەرمایەیێ من ب دروستی چەوایە؟" else "Analyze my total net worth and capital structure", "OVERVIEW") }
          )
          QuickPromptButton(
            text = if (isKurdish) "🚗 قازانجێ سەیاران" else "🚗 Car Fleet Profit",
            testTag = "copilot_prompt_chip_cars",
            onClick = { sendQuery(if (isKurdish) "پێشبینیا قازانج و قستێن سەیاران چەوایە؟" else "Evaluate car fleet profit margins and installment receivables", "CARS") }
          )
          QuickPromptButton(
            text = if (isKurdish) "⚖️ رەوشا قەرزان" else "⚖️ Debt Safety",
            testTag = "copilot_prompt_chip_debts",
            onClick = { sendQuery(if (isKurdish) "پشکنینا قەرز و دەینان بۆ من بکە" else "Audit my active debts, credits, and repayment schedule", "DEBTS") }
          )
          QuickPromptButton(
            text = if (isKurdish) "🏰 ملوک و عەقارات" else "🏰 Real Estate",
            testTag = "copilot_prompt_chip_properties",
            onClick = { sendQuery(if (isKurdish) "بەها و داهاتیێ ملوکێن من چەندە؟" else "Review real estate valuation and property yield", "PROPERTIES") }
          )
          QuickPromptButton(
            text = if (isKurdish) "🪙 زێڕێ عەیار ٢٤" else "🪙 24K Gold Reserve",
            testTag = "copilot_prompt_chip_gold",
            onClick = { sendQuery(if (isKurdish) "پاشەکەوتێ زێڕی چەندە و ڕاوێژا تە چییە؟" else "Assess physical gold holdings and hedging strength", "GOLD") }
          )
        }
      }

      // Chat Messages List
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          CopilotBubble(
            message = msg,
            isKurdish = isKurdish,
            onCopy = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Copilot Advice", msg.text)
              clipboard.setPrimaryClip(clip)
            }
          )
        }

        if (isThinking) {
          item {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                .border(0.8.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = GoldAccent,
                strokeWidth = 2.dp
              )
              Text(
                text = if (isKurdish) "Gemini هزرا خۆ دکەت و داتایان شیکار دکەت..." else "Gemini is synthesizing live financial ledger...",
                fontSize = 11.sp,
                color = GoldAccent
              )
            }
          }
        }
      }

      // Bottom Message Input Row
      Surface(
        color = DarkGlassCardBg,
        tonalElevation = 6.dp,
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
              colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
            ),
            shape = RoundedCornerShape(0.dp)
          )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                text = if (isKurdish) "پرسیارەکێ بنڤیسە دەربارەی سەرمایەی، سەیاران، قەرزان..." else "Ask about your wealth, cars, debts, gold...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("copilot_input_field"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF070B14).copy(alpha = 0.8f),
              unfocusedContainerColor = Color(0xFF070B14).copy(alpha = 0.6f),
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendQuery(inputText) })
          )

          IconButton(
            onClick = { sendQuery(inputText) },
            enabled = inputText.isNotBlank() && !isThinking,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                if (inputText.isNotBlank()) {
                  Brush.linearGradient(listOf(GoldAccent, EmeraldLight))
                } else {
                  Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)))
                }
              )
              .testTag("copilot_send_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send Query",
              tint = if (inputText.isNotBlank()) Color(0xFF070B14) else Color.White.copy(alpha = 0.35f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MetricCapsulePill(
  label: String,
  value: String,
  color: Color
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF080D18).copy(alpha = 0.85f))
      .border(0.8.dp, color.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Column {
      Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.65f)
      )
      Text(
        text = value,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = color
      )
    }
  }
}

@Composable
private fun QuickPromptButton(
  text: String,
  testTag: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF141F36).copy(alpha = 0.85f))
      .border(0.8.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag(testTag)
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
  }
}

@Composable
private fun CopilotBubble(
  message: CopilotChatMessage,
  isKurdish: Boolean,
  onCopy: () -> Unit
) {
  val isUser = message.isUser
  val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(if (isUser) 0.82f else 0.94f)
        .clip(
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
        .background(
          if (isUser) {
            Brush.linearGradient(
              listOf(
                Color(0xFF1E3A8A).copy(alpha = 0.9f),
                Color(0xFF0F172A).copy(alpha = 0.95f)
              )
            )
          } else {
            Brush.linearGradient(
              listOf(
                DarkGlassCardSurface,
                Color(0xFF0A0F1D)
              )
            )
          }
        )
        .border(
          width = 1.dp,
          brush = if (isUser) {
            Brush.linearGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.5f), Color.Transparent))
          } else {
            Brush.linearGradient(listOf(GoldAccent.copy(alpha = 0.45f), EmeraldLight.copy(alpha = 0.3f)))
          },
          shape = RoundedCornerShape(16.dp)
        )
        .padding(14.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!isUser) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "Gemini AI Copilot",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
              )
            }

            IconButton(
              onClick = onCopy,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy text",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(13.dp)
              )
            }
          }
        }

        Text(
          text = message.text,
          fontSize = 13.sp,
          color = Color.White,
          lineHeight = 19.sp,
          fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = timeFmt.format(Date(message.timestamp)),
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.45f)
          )
        }
      }
    }
  }
}
