package com.example.ui.screens.reports

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FileDownload
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import com.example.data.currency.CurrencyService
import com.example.data.local.InvestmentEntity
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.AssetDetailActionDialog
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.PrintPreviewDialog
import com.example.ui.screens.cars.CarDetailsDialog
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.util.PdfExportService

@Composable
fun CarFleetReportView(
  uiState: FinanceUiState,
  searchQuery: String,
  isKurdish: Boolean,
  modifier: Modifier = Modifier,
  viewModel: FinanceViewModel? = null
) {
  val context = LocalContext.current
  var previewPdfResult by remember { mutableStateOf<com.example.util.GeneratedPdfResult?>(null) }
  var selectedCarForDetail by remember { mutableStateOf<InvestmentEntity?>(null) }
  val filteredCars = remember(uiState.carInvestments, searchQuery) {
    if (searchQuery.isBlank()) uiState.carInvestments
    else uiState.carInvestments.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
        it.partnerName.contains(searchQuery, ignoreCase = true) ||
        it.notes.contains(searchQuery, ignoreCase = true)
    }
  }

  val activeCars = filteredCars.filter { !it.isSold }
  val soldCars = filteredCars.filter { it.isSold }

  val totalInvested = filteredCars.sumOf {
    CurrencyService.convert(it.initialCapital, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalValuation = activeCars.sumOf {
    CurrencyService.convert(it.currentValue, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalRealizedProfit = soldCars.sumOf {
    val price = if (it.soldPrice > 0.0) it.soldPrice else it.currentValue
    val profit = price - it.initialCapital
    CurrencyService.convert(profit, it.currency, uiState.displayCurrency, uiState.iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }

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
            text = if (isKurdish) "🚗 راپۆرتا دارایی یا سەیاران" else "🚗 Vehicles Financial Report",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF38BDF8)
          )
          Text(
            text = if (isKurdish) "کۆیێ گشتیێ سەیارێن بەردەست و فرۆتی (${filteredCars.size})" else "Active and sold fleet overview (${filteredCars.size})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Box(
          modifier = Modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF38BDF8).copy(alpha = 0.18f))
            .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable {
              previewPdfResult = PdfExportService.generateCarsPdf(
                context = context,
                cars = filteredCars,
                displayCurrency = uiState.displayCurrency,
                currentIqdRate = uiState.iqdRate,
                isKurdish = isKurdish
              )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("export_cars_pdf_btn"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
            Text(text = "PDF", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
          }
        }
      }
    }

    // 2. Summary Metric Grid (4 Pillars)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Pillar 1: Active Valuation
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF38BDF8).copy(alpha = 0.12f))
            .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "🚗 بەردەست (${activeCars.size})" else "Active (${activeCars.size})",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
            Text(
              text = CurrencyService.formatShort(totalValuation, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }

        // Pillar 2: Realized Profit
        val profitColor = if (totalRealizedProfit >= 0.0) EmeraldLight else LossRed
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(profitColor.copy(alpha = 0.12f))
            .border(0.8.dp, profitColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "📈 قازانجێ فرۆتنێ" else "Profit Made",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = profitColor
            )
            Text(
              text = "${if (totalRealizedProfit >= 0) "+" else ""}${CurrencyService.formatShort(totalRealizedProfit, uiState.displayCurrency)}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = profitColor
            )
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Pillar 3: Total Invested
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.8.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "🏛️ سەرمایێ کڕینێ" else "Total Invested",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = CurrencyService.formatShort(totalInvested, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        // Pillar 4: Maintenance Spending
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(LossRed.copy(alpha = 0.10f))
            .border(0.8.dp, LossRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "🔧 مەسروفات و چاکسازی" else "Maintenance Cost",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = LossRed
            )
            Text(
              text = CurrencyService.formatShort(uiState.totalCarServicesCost, uiState.displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = LossRed
            )
          }
        }
      }
    }

    // 3. Interactive Vehicle Fleet Buttons
    if (filteredCars.isEmpty()) {
      item {
        Text(
          text = if (isKurdish) "چ سەیارە نەهاتنە دیتن" else "No vehicles match your filter",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp,
          modifier = Modifier.padding(vertical = 16.dp)
        )
      }
    } else {
      items(filteredCars, key = { it.id }) { car ->
        val accent = if (car.isSold) EmeraldLight else Color(0xFF38BDF8)
        val primaryVal = CurrencyService.convert(
          amount = if (car.isSold && car.soldPrice > 0) car.soldPrice else car.currentValue,
          from = car.currency,
          to = uiState.displayCurrency,
          iqdRate = uiState.iqdRate,
          entryIqdRate = car.exchangeRateAtEntry
        )
        val otherCur = if (uiState.displayCurrency == "USD") "IQD" else "USD"
        val secondaryVal = CurrencyService.convert(
          amount = if (car.isSold && car.soldPrice > 0) car.soldPrice else car.currentValue,
          from = car.currency,
          to = otherCur,
          iqdRate = uiState.iqdRate,
          entryIqdRate = car.exchangeRateAtEntry
        )
        val roi = uiState.getCarRoiPercent(car)
        CyberRecordButton(
          title = car.name,
          subtitle = buildString {
            append(if (car.isSold) (if (isKurdish) "فرۆتی" else "Sold") else (if (isKurdish) "بەردەست" else "In Stock"))
            if (car.isCoOwned && car.partnerName.isNotBlank()) append(" • 🤝 ${car.partnerName}")
          },
          primaryValue = CurrencyService.formatShort(primaryVal, uiState.displayCurrency),
          secondaryValue = "≈ ${CurrencyService.formatShort(secondaryVal, otherCur)}",
          accentColor = accent,
          icon = Icons.Default.DirectionsCar,
          statusBadge = "ROI: ${if (roi >= 0) "+" else ""}${"%.1f".format(roi)}%",
          statusBadgeColor = if (roi >= 0) EmeraldLight else LossRed,
          actionHintLabel = if (isKurdish) "کردار" else "Actions",
          quickActionLabel = if (viewModel != null) (if (isKurdish) "دەستکاری" else "Edit") else null,
          quickActionIcon = Icons.Default.Edit,
          quickActionColor = GoldAccent,
          onQuickActionClick = if (viewModel != null) { { viewModel.openQuickEditForInvestment(car) } } else null,
          onClick = { selectedCarForDetail = car },
          testTagKey = "report_car_btn_${car.id}"
        )
      }
    }
  }

  val activeCar = selectedCarForDetail
  if (activeCar != null) {
    if (viewModel != null) {
      CarDetailsDialog(
        car = uiState.carInvestments.find { it.id == activeCar.id } ?: activeCar,
        uiState = uiState,
        isKurdish = isKurdish,
        onDismiss = { selectedCarForDetail = null },
        onAddPayment = {
          selectedCarForDetail = null
          viewModel.openSearchResultItem(2, "CAR", activeCar.id)
        },
        onAddSpending = {
          selectedCarForDetail = null
          viewModel.openSearchResultItem(2, "CAR", activeCar.id)
        },
        onEditCar = {
          selectedCarForDetail = null
          viewModel.openQuickEditForInvestment(activeCar)
        },
        onDeletePayment = { viewModel.deleteCarPayment(it) },
        onDeleteSpending = { viewModel.deleteCarService(it) }
      )
    }
  }

  val currentPreview = previewPdfResult
  if (currentPreview != null) {
    PrintPreviewDialog(
      result = currentPreview,
      onDismiss = { previewPdfResult = null }
    )
  }
}
