package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.util.GeneratedPdfResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.PrintPreviewDialog
import com.example.ui.screens.debts.PersonDebtDetailsDialog
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.PdfExportService
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DebtsReportView(
  uiState: FinanceUiState,
  searchQuery: String,
  isKurdish: Boolean,
  modifier: Modifier = Modifier,
  viewModel: FinanceViewModel? = null
) {
  val context = LocalContext.current
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
  var previewPdfResult by remember { mutableStateOf<com.example.util.GeneratedPdfResult?>(null) }
  var selectedDebtForDetail by remember { mutableStateOf<DebtCreditEntity?>(null) }
  val filteredDebts = remember(uiState.debtsCredits, searchQuery) {
    if (searchQuery.isBlank()) uiState.debtsCredits
    else uiState.debtsCredits.filter {
      it.personName.contains(searchQuery, ignoreCase = true) ||
        it.notes.contains(searchQuery, ignoreCase = true)
    }
  }

  val credits = filteredDebts.filter { it.type == "CREDIT" }
  val payables = filteredDebts.filter { it.type == "DEBT" }

  val totalCreditsOwedToMe = credits.sumOf {
    CurrencyService.convert(it.remainingBalance, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalDebtsIOwe = payables.sumOf {
    CurrencyService.convert(it.remainingBalance, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val netBalance = totalCreditsOwedToMe - totalDebtsIOwe

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Report Title & Export CSV Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "🤝 راپۆرتا دارایی یا قەرزان" else "🤝 Debts & Loans Report",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFF97316)
          )
          Text(
            text = if (isKurdish) "خولاسەیا قەرزێن دەف خەلکی و لسەر من (${filteredDebts.size})" else "Receivables and payables summary (${filteredDebts.size})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Box(
          modifier = Modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF97316).copy(alpha = 0.18f))
            .border(0.8.dp, Color(0xFFF97316).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable {
              previewPdfResult = PdfExportService.generateDebtsPdf(
                context = context,
                debts = filteredDebts,
                displayCurrency = uiState.displayCurrency,
                currentIqdRate = uiState.iqdRate,
                isKurdish = isKurdish,
                isMulukOnly = false
              )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("export_debts_pdf_btn"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(15.dp))
            Text(text = "PDF", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF97316))
          }
        }
      }
    }

    // 2. Summary Metric Grid (3 Pillars)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Credits (دەف خەلکی)
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
              text = if (isKurdish) "📥 دەف خەلکی (+)" else "Owed to Me (+)",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Text(
              text = CurrencyService.formatShort(totalCreditsOwedToMe, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = EmeraldLight
            )
          }
        }

        // Debts (لسەر من)
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(LossRed.copy(alpha = 0.12f))
            .border(0.8.dp, LossRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "📤 لسەر من (-)" else "I Owe (-)",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = LossRed
            )
            Text(
              text = CurrencyService.formatShort(totalDebtsIOwe, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = LossRed
            )
          }
        }

        // Net Balance (صافی)
        val netColor = if (netBalance >= 0.0) EmeraldLight else LossRed
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(netColor.copy(alpha = 0.12f))
            .border(0.8.dp, netColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "⚖️ صافی قەرز" else "Net Position",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = netColor
            )
            Text(
              text = "${if (netBalance >= 0) "+" else ""}${CurrencyService.formatShort(netBalance, uiState.displayCurrency)}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = netColor
            )
          }
        }
      }
    }

    // 3. Interactive Debts & Credits Button List
    if (filteredDebts.isEmpty()) {
      item {
        Text(
          text = if (isKurdish) "چ قەرز نەهاتنە دیتن" else "No debts match your filter",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp,
          modifier = Modifier.padding(vertical = 16.dp)
        )
      }
    } else {
      items(filteredDebts, key = { it.id }) { debt ->
        val isCredit = debt.type == "CREDIT"
        val accent = if (debt.isCompleted) EmeraldLight else if (isCredit) Color(0xFF22D3EE) else LossRed
        val remainingDisplay = CurrencyService.convert(
          amount = debt.remainingAmount,
          from = debt.currency,
          to = uiState.displayCurrency,
          iqdRate = uiState.iqdRate,
          entryIqdRate = debt.exchangeRateAtEntry
        )
        val otherCur = if (uiState.displayCurrency == "USD") "IQD" else "USD"
        val secondaryDisplay = CurrencyService.convert(
          amount = debt.remainingAmount,
          from = debt.currency,
          to = otherCur,
          iqdRate = uiState.iqdRate,
          entryIqdRate = debt.exchangeRateAtEntry
        )
        val ratio = if (debt.totalAmount > 0) (debt.amountPaid / debt.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
        CyberRecordButton(
          title = debt.personName,
          subtitle = buildString {
            append(if (isCredit) (if (isKurdish) "دەف خەلکی (+)" else "Owed to Me") else (if (isKurdish) "لسەر من (-)" else "I Owe"))
            if (debt.notes.isNotBlank()) append(" • ${debt.notes}")
          },
          primaryValue = CurrencyService.formatShort(remainingDisplay, uiState.displayCurrency),
          secondaryValue = "≈ ${CurrencyService.formatShort(secondaryDisplay, otherCur)}",
          accentColor = accent,
          icon = Icons.Default.Handshake,
          statusBadge = if (debt.isCompleted) (if (isKurdish) "تەواوبوو" else "Settled") else "${(ratio * 100).toInt()}% paid",
          statusBadgeColor = accent,
          progressRatio = if (debt.totalAmount > 0) ratio else null,
          actionHintLabel = if (isKurdish) "دەف کەسی" else "Details",
          quickActionLabel = if (viewModel != null) (if (isKurdish) "دەستکاری" else "Edit") else null,
          quickActionIcon = Icons.Default.Edit,
          quickActionColor = GoldAccent,
          onQuickActionClick = if (viewModel != null) { { viewModel.openQuickEditForDebt(debt) } } else null,
          onClick = { selectedDebtForDetail = debt },
          testTagKey = "report_debt_btn_${debt.id}"
        )
      }
    }
  }

  val activeDebt = selectedDebtForDetail
  if (activeDebt != null && viewModel != null) {
    val latest = uiState.debtsCredits.find { it.id == activeDebt.id } ?: activeDebt
    PersonDebtDetailsDialog(
      debt = latest,
      uiState = uiState,
      isKurdish = isKurdish,
      dateFormat = dateFormat,
      onRecordPayment = {
        selectedDebtForDetail = null
        viewModel.openSearchResultItem(1, "DEBT", latest.id)
      },
      onAddMoney = {
        selectedDebtForDetail = null
        viewModel.openSearchResultItem(1, "DEBT", latest.id)
      },
      onEditDebt = {
        selectedDebtForDetail = null
        viewModel.openQuickEditForDebt(latest)
      },
      onSettleFull = {
        viewModel.settleDebtCreditInFull(latest.id)
        selectedDebtForDetail = null
      },
      onDeletePayment = { viewModel.deleteDebtPayment(it) },
      onDelete = {
        viewModel.deleteDebtCredit(latest.id)
        selectedDebtForDetail = null
      },
      onDismiss = { selectedDebtForDetail = null }
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
