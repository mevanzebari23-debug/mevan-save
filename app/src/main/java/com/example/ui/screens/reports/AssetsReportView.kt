package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.AssetDetailActionDialog
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.GoldHoldingDetailActionDialog
import com.example.ui.components.PrintPreviewDialog
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService

@Composable
fun AssetsReportView(
  uiState: FinanceUiState,
  searchQuery: String,
  isKurdish: Boolean,
  modifier: Modifier = Modifier,
  viewModel: FinanceViewModel? = null
) {
  val context = LocalContext.current
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }
  var selectedAssetForDetail by remember { mutableStateOf<InvestmentEntity?>(null) }
  var selectedGoldForDetail by remember { mutableStateOf<GoldHoldingEntity?>(null) }
  val nonCarAssets = remember(uiState.nonCarInvestments, searchQuery) {
    if (searchQuery.isBlank()) uiState.nonCarInvestments
    else uiState.nonCarInvestments.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
        it.notes.contains(searchQuery, ignoreCase = true)
    }
  }
  val filteredGoldHoldings = remember(uiState.activeGoldHoldings, searchQuery) {
    if (searchQuery.isBlank()) uiState.activeGoldHoldings
    else uiState.activeGoldHoldings.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.purityLabel.contains(searchQuery, ignoreCase = true)
    }
  }

  val totalPropertyValuation = nonCarAssets.sumOf {
    CurrencyService.convert(it.currentValue, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalGoldValuation = uiState.totalGoldCurrentValueDisplay

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isKurdish) "🏢 راپۆرتا ملوک، عەقارات و زێڕی" else "🏢 Real Estate & Gold Assets Report",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccent
          )
          Text(
            text = if (isKurdish) "سەرمایێ سامانێ نەگوێزەر، پرۆژە و خەزینەیا زێڕ و زیڤی" else "Properties, private ventures and precious metals vault",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Box(
          modifier = Modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GoldAccent.copy(alpha = 0.18f))
            .border(0.8.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable {
              previewPdfResult = PdfExportService.generateAssetsPdf(
                context = context,
                assets = nonCarAssets,
                displayCurrency = uiState.displayCurrency,
                currentIqdRate = uiState.iqdRate,
                isKurdish = isKurdish
              )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("export_assets_pdf_btn"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = GoldAccent, modifier = Modifier.size(15.dp))
            Text(text = "PDF", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(GoldAccent.copy(alpha = 0.12f))
            .border(0.8.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "🏢 عەقارات و پرۆژە (${nonCarAssets.size})" else "Properties (${nonCarAssets.size})",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
            Text(
              text = CurrencyService.formatShort(totalPropertyValuation, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(EmeraldLight.copy(alpha = 0.12f))
            .border(0.8.dp, EmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "🥇 زێڕ و زیڤ" else "Gold & Silver",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Text(
              text = CurrencyService.formatShort(totalGoldValuation, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = EmeraldLight
            )
          }
        }
      }
    }

    // 3. Interactive Real Estate & Ventures Buttons
    items(nonCarAssets, key = { "asset_${it.id}" }) { asset ->
      val valDisplay = CurrencyService.convert(asset.currentValue, asset.currency, uiState.displayCurrency, uiState.iqdRate, asset.exchangeRateAtEntry)
      val otherCur = if (uiState.displayCurrency == "USD") "IQD" else "USD"
      val secDisplay = CurrencyService.convert(asset.currentValue, asset.currency, otherCur, uiState.iqdRate, asset.exchangeRateAtEntry)
      val accent = if (asset.isSold) EmeraldLight else GoldAccent
      CyberRecordButton(
        title = asset.name,
        subtitle = "${asset.category}${if (asset.isCoOwned && asset.partnerName.isNotBlank()) " • 🤝 ${asset.partnerName}" else ""}",
        primaryValue = CurrencyService.formatShort(valDisplay, uiState.displayCurrency),
        secondaryValue = "≈ ${CurrencyService.formatShort(secDisplay, otherCur)}",
        accentColor = accent,
        icon = Icons.Default.Apartment,
        statusBadge = if (asset.isSold) (if (isKurdish) "فرۆتی" else "Sold") else (if (isKurdish) "چالاک" else "Active"),
        statusBadgeColor = accent,
        actionHintLabel = if (isKurdish) "کردار" else "Actions",
        quickActionLabel = if (viewModel != null) (if (isKurdish) "دەستکاری" else "Edit") else null,
        quickActionIcon = Icons.Default.Edit,
        quickActionColor = GoldAccent,
        onQuickActionClick = if (viewModel != null) { { viewModel.openQuickEditForInvestment(asset) } } else null,
        onClick = { selectedAssetForDetail = asset },
        testTagKey = "report_asset_btn_${asset.id}"
      )
    }

    // 4. Interactive Gold & Silver Bullion Buttons
    items(filteredGoldHoldings, key = { "gold_${it.id}" }) { holding ->
      val liveUsd = holding.currentValue(uiState.effectiveGoldPricePerGram24K, uiState.effectiveSilverPricePerGram9999)
      val valDisplay = CurrencyService.convert(liveUsd, "USD", uiState.displayCurrency, uiState.iqdRate)
      val otherCur = if (uiState.displayCurrency == "USD") "IQD" else "USD"
      val secDisplay = CurrencyService.convert(liveUsd, "USD", otherCur, uiState.iqdRate)
      val accent = if (holding.isSilver) Color(0xFF94A3B8) else GoldAccent
      CyberRecordButton(
        title = holding.title,
        subtitle = "${holding.getDisplayPurityBadge(isKurdish)} • ${"%.2f".format(holding.weightGrams)}g",
        primaryValue = CurrencyService.formatShort(valDisplay, uiState.displayCurrency),
        secondaryValue = "≈ ${CurrencyService.formatShort(secDisplay, otherCur)}",
        accentColor = accent,
        icon = Icons.Default.MonetizationOn,
        statusBadge = if (holding.isSilver) (if (isKurdish) "زیڤ" else "Silver") else (if (isKurdish) "زێڕ" else "Gold"),
        statusBadgeColor = accent,
        actionHintLabel = if (isKurdish) "کردار" else "Actions",
        quickActionLabel = if (viewModel != null) (if (isKurdish) "دەستکاری" else "Edit") else null,
        quickActionIcon = Icons.Default.Edit,
        quickActionColor = GoldAccent,
        onQuickActionClick = if (viewModel != null) { { viewModel.openQuickEditForGold(holding) } } else null,
        onClick = { selectedGoldForDetail = holding },
        testTagKey = "report_gold_btn_${holding.id}"
      )
    }
  }

  val activeAsset = selectedAssetForDetail
  if (activeAsset != null && viewModel != null) {
    AssetDetailActionDialog(
      investment = activeAsset,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedAssetForDetail = null }
    )
  }

  val activeGold = selectedGoldForDetail
  if (activeGold != null && viewModel != null) {
    GoldHoldingDetailActionDialog(
      holding = activeGold,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedGoldForDetail = null }
    )
  }

  val currentPreview = previewPdfResult
  if (currentPreview != null) {
    PrintPreviewDialog(
      result = currentPreview,
      onDismiss = { previewPdfResult = null }
    )
  }
}
