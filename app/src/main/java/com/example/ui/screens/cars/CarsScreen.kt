package com.example.ui.screens.cars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Surface
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.CarPaymentEntity
import com.example.data.local.CarServiceEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.validatePartnershipShares
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.CarSubItemDetailActionDialog
import com.example.ui.components.CyberNeonCategoryButton
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.DynamicCategoryExtractor
import com.example.ui.components.EntryExchangeRateCard
import com.example.ui.components.SectionQuickHistoryStrip
import com.example.ui.history.ActionSection
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════════════════
// LUXURY DARK AUTOMOTIVE SHOWROOM STUDIO DESIGN SYSTEM
// ═══════════════════════════════════════════════════════════════════════════════
// GRAND ROYAL PALACE AUTOMOTIVE SHOWROOM STUDIO DESIGN SYSTEM
// ═══════════════════════════════════════════════════════════════════════════════
private val ShowroomDarkBg = Brush.verticalGradient(
  colors = listOf(
    Color(0xC404070D),
    Color(0x9E080E17),
    Color(0x8A0E1624),
    Color(0xAA131D2D),
    Color(0xCC0B121E),
    Color(0xF204060B)
  )
)

private val ShowroomPedestalBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xF60B1220),
    Color(0xEE0F172A),
    Color(0xF6111B2F),
    Color(0xEE0F172A),
    Color(0xF60B1220)
  ),
  start = Offset(0f, 0f),
  end = Offset(1000f, 1000f)
)

private val ShowroomPedestalBorderBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFF64748B),
    Color(0xFFFFD700),
    Color(0xFF38BDF8),
    Color(0xFFFFFFFF),
    Color(0xFFFFD700),
    Color(0xFF0284C7),
    Color(0xFF64748B)
  )
)

private val ShowroomPedestalActiveBorderBrush = Brush.linearGradient(
  colors = listOf(
    Color(0xFFFFD700),
    Color(0xFF38BDF8),
    Color(0xFFBAE6FD),
    Color(0xFFFFDF73),
    Color(0xFF0EA5E9),
    Color(0xFFFFD700)
  )
)

private val ShowroomGroundReflectionBrush = Brush.verticalGradient(
  colors = listOf(
    Color(0x3538BDF8),
    Color(0x180284C7),
    Color(0x06070B12),
    Color.Transparent
  )
)

private val ShowroomSpotlightCyan = Color(0xFF38BDF8)
private val ShowroomNeonGlow = Color(0xFFBAE6FD)

@Composable
private fun ShowroomAtmosphereCanvas(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@Canvas

    // 1. Center Grand Palace Crystal Chandelier Glow (Warm Gold & Cyan ambient cone)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x38FFD700),
          Color(0x2238BDF8),
          Color(0x0C0F172A),
          Color.Transparent
        ),
        center = Offset(w * 0.5f, h * 0.08f),
        radius = w * 0.95f
      ),
      center = Offset(w * 0.5f, h * 0.08f),
      radius = w * 0.95f
    )

    // 2. Dual Overhead Crystal Spotlight Beams (Flanking car bays)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x2E38BDF8),
          Color(0x12FFDF73),
          Color.Transparent
        ),
        center = Offset(w * 0.18f, h * 0.04f),
        radius = w * 0.60f
      ),
      center = Offset(w * 0.18f, h * 0.04f),
      radius = w * 0.60f
    )
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x2E38BDF8),
          Color(0x12FFDF73),
          Color.Transparent
        ),
        center = Offset(w * 0.82f, h * 0.04f),
        radius = w * 0.60f
      ),
      center = Offset(w * 0.82f, h * 0.04f),
      radius = w * 0.60f
    )

    // 3. Polished Imperial Showroom Floor Reflections (Glossy marble grid perspective)
    val horizonY = h * 0.28f
    val floorHeight = h - horizonY
    val stepY = 32.dp.toPx()
    var currY = horizonY
    while (currY < h) {
      val progress = (currY - horizonY) / floorHeight
      val alpha = (0.03f + progress * 0.09f).coerceIn(0f, 0.15f)
      drawLine(
        color = Color(0xFF38BDF8).copy(alpha = alpha),
        start = Offset(0f, currY),
        end = Offset(w, currY),
        strokeWidth = (1f + progress * 1.2f).dp.toPx()
      )
      currY += stepY * (0.6f + progress * 0.8f)
    }

    // Perspective marble floor lines radiating from vanishing horizon
    val vanishingX = w * 0.5f
    val perspectiveAngles = listOf(-0.45f, -0.30f, -0.15f, 0.15f, 0.30f, 0.45f)
    for (offsetRatio in perspectiveAngles) {
      val bottomX = vanishingX + offsetRatio * w * 2.2f
      drawLine(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.Transparent,
            Color(0x0C38BDF8),
            Color(0x2038BDF8),
            Color(0x0838BDF8)
          ),
          startY = horizonY,
          endY = h
        ),
        start = Offset(vanishingX + offsetRatio * w * 0.2f, horizonY),
        end = Offset(bottomX, h),
        strokeWidth = 1.dp.toPx()
      )
    }

    // 4. Studio Ceiling Lighting Track Rig (Overhead fixture bar across top)
    drawLine(
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color.Transparent,
          Color(0x66FFD700),
          Color(0xE0BAE6FD),
          Color(0x6638BDF8),
          Color.Transparent
        )
      ),
      start = Offset(w * 0.04f, 3.dp.toPx()),
      end = Offset(w * 0.96f, 3.dp.toPx()),
      strokeWidth = 2.5.dp.toPx()
    )

    // Crystal ceiling spotlight fixtures (glowing pinpoint light heads)
    val fixturePositions = listOf(0.18f, 0.38f, 0.50f, 0.62f, 0.82f)
    for (pos in fixturePositions) {
      val fixtureX = w * pos
      drawCircle(
        color = Color(0xFFFFFFFF),
        radius = 3.dp.toPx(),
        center = Offset(fixtureX, 3.dp.toPx())
      )
      drawCircle(
        color = Color(0x7738BDF8),
        radius = 7.dp.toPx(),
        center = Offset(fixtureX, 3.dp.toPx())
      )
    }
  }
}

@Composable
fun CarsScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onOpenGoogleSearchGrounding: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Active, 2: Sold & P&L, 3: Partner, 4: Spending, 5: Monthly Payments
  var showAddCarDialog by remember { mutableStateOf(false) }
  var carForMonthlyPayment by remember { mutableStateOf<InvestmentEntity?>(null) }
  var carForSpending by remember { mutableStateOf<InvestmentEntity?>(null) }
  var carForValuation by remember { mutableStateOf<InvestmentEntity?>(null) }
  var carForEditingShares by remember { mutableStateOf<InvestmentEntity?>(null) }
  var carForDetails by remember { mutableStateOf<InvestmentEntity?>(null) }
  var carForSelling by remember { mutableStateOf<InvestmentEntity?>(null) }
  var filterSpendingCategory by remember { mutableStateOf("ALL") }
  var partnerCarFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "SOLD"
  var selectedBrandFilter by remember { mutableStateOf("ALL") }
  var carServiceForDetail by remember { mutableStateOf<CarServiceEntity?>(null) }
  var carPaymentForDetail by remember { mutableStateOf<CarPaymentEntity?>(null) }

  val allCarsRaw = uiState.carInvestments
  val cars = remember(allCarsRaw, selectedBrandFilter) {
    if (selectedBrandFilter == "ALL" || selectedBrandFilter == "ALL_CARS") allCarsRaw
    else allCarsRaw.filter { it.name.contains(selectedBrandFilter, ignoreCase = true) || it.notes.contains(selectedBrandFilter, ignoreCase = true) }
  }
  val activeCars = remember(cars) { cars.filter { !it.isSold } }
  val soldCars = remember(cars) { cars.filter { it.isSold } }
  val soloCars = remember(cars) { cars.filter { !it.isCoOwned } }
  val partnerCars = remember(cars) { cars.filter { it.isCoOwned } }
  val displayCurrency = uiState.displayCurrency

  LaunchedEffect(uiState.focusedEntityType, uiState.focusedEntityId, cars) {
    if (uiState.focusedEntityType == "CAR" && uiState.focusedEntityId != null) {
      val targetCar = cars.find { it.id == uiState.focusedEntityId }
      if (targetCar != null) {
        selectedTab = 0
        carForDetails = targetCar
        viewModel.clearFocusedSearchResult()
      }
    }
  }

  Box(
    modifier = modifier.fillMaxSize()
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Top Executive Banner
      item {
        CarsExecutiveBanner(
          uiState = uiState,
          carsCount = cars.size,
          partnerCarsCount = partnerCars.size,
          displayCurrency = displayCurrency,
          isKurdish = isKurdish,
          onAddCar = { showAddCarDialog = true }
        )
      }

      // 2. Tab Navigation
      item {
        CarsTabSelector(
          selectedTab = selectedTab,
          onSelectTab = { selectedTab = it },
          allCount = cars.size,
          activeCount = activeCars.size,
          soldCount = soldCars.size,
          partnerCount = partnerCars.size,
          spendingCount = uiState.carServices.size,
          paymentsCount = uiState.carPayments.size,
          isKurdish = isKurdish
        )
      }

      // 2.5. Brand Quick-Access Category Buttons (Toyota, Land Cruiser, BMW, Mercedes, Nissan, etc.)
      item {
        val carBrandItems = remember(allCarsRaw, isKurdish) {
          DynamicCategoryExtractor.extractCarBrandButtons(allCarsRaw, isKurdish)
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) "🚗 دوکمەیێن مارکێن سەیاران (${carBrandItems.size}):" else "🚗 Brand Buttons (${carBrandItems.size}):",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
            if (selectedBrandFilter != "ALL" && selectedBrandFilter != "ALL_CARS") {
              Text(
                text = if (isKurdish) "پاککرنەوە ✕" else "Reset ✕",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF3366),
                modifier = Modifier.clickable { selectedBrandFilter = "ALL" }
              )
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            carBrandItems.forEach { item ->
              val isBrandSelected = selectedBrandFilter.equals(item.key, ignoreCase = true) ||
                (selectedBrandFilter == "ALL" && item.key == "ALL_CARS")

              CyberNeonCategoryButton(
                label = item.label,
                accentColor = item.accentColor,
                isSelected = isBrandSelected,
                count = item.count,
                onClick = { selectedBrandFilter = item.key },
                testTagKey = "cars_brand_button_${item.key.lowercase().replace(" ", "_")}"
              )
            }
          }
        }
      }

      // 3. Tab Content
      when (selectedTab) {
        0 -> { // All Cars
          item {
            CarsListHeader(
              title = if (isKurdish) "هەمی سەیارێن تۆمارکری (${cars.size})" else "All Registered Vehicles (${cars.size})",
              subtitle = if (isKurdish) "کلیک ل سەر هەر سەیارەکێ بکە بۆ وردەکاری، قست، مەسروف یان فرۆتنێ" else "Tap any vehicle for details, installments, expenses or sale",
              onAddCar = { showAddCarDialog = true },
              isKurdish = isKurdish
            )
          }

          if (cars.isEmpty()) {
            item {
              EmptyCarsPlaceholder(
                message = if (isKurdish) "چ سەیارە نەهاتینە تۆمارکرن" else "No cars in your fleet yet.",
                isKurdish = isKurdish,
                onAddCar = { showAddCarDialog = true }
              )
            }
          } else {
            items(cars, key = { it.id }) { car ->
              CarItemCard(
                car = car,
                uiState = uiState,
                onAddMonthlyPayment = { carForMonthlyPayment = car },
                onAddSpending = { carForSpending = car },
                onUpdateValuation = { carForValuation = car },
                onEditPartnerShares = { carForEditingShares = car },
                onViewDetails = { carForDetails = car },
                onMarkAsSold = { carForSelling = car },
                onReopenAsActive = { viewModel.reopenCarAsActive(car.id) },
                onQuickEdit = { viewModel.openQuickEditForInvestment(car) },
                onDelete = { viewModel.deleteInvestment(car.id) },
                onDeletePayment = { viewModel.deleteCarPayment(it) },
                onDeleteSpending = { viewModel.deleteCarService(it) }
              )
            }
          }
        }
        1 -> { // Active Fleet Cars
          item {
            CarsListHeader(
              title = if (isKurdish) "سەیارێن بەردەست (${activeCars.size})" else "Active Vehicles (${activeCars.size})",
              subtitle = if (isKurdish) "سەیارێن نوکە د پێشانگەهێ دا یان ب قستا دهێنە دان" else "Active fleet vehicles & ongoing installment deals",
              onAddCar = { showAddCarDialog = true },
              isKurdish = isKurdish
            )
          }

          if (activeCars.isEmpty()) {
            item {
              EmptyCarsPlaceholder(
                message = if (isKurdish) "چ سەیارێن بەردەست نینن" else "No active fleet vehicles currently.",
                isKurdish = isKurdish,
                onAddCar = { showAddCarDialog = true }
              )
            }
          } else {
            items(activeCars, key = { it.id }) { car ->
              CarItemCard(
                car = car,
                uiState = uiState,
                onAddMonthlyPayment = { carForMonthlyPayment = car },
                onAddSpending = { carForSpending = car },
                onUpdateValuation = { carForValuation = car },
                onEditPartnerShares = { carForEditingShares = car },
                onViewDetails = { carForDetails = car },
                onMarkAsSold = { carForSelling = car },
                onReopenAsActive = { viewModel.reopenCarAsActive(car.id) },
                onQuickEdit = { viewModel.openQuickEditForInvestment(car) },
                onDelete = { viewModel.deleteInvestment(car.id) },
                onDeletePayment = { viewModel.deleteCarPayment(it) },
                onDeleteSpending = { viewModel.deleteCarService(it) }
              )
            }
          }
        }
        2 -> { // Sold Cars & Winning/Losing (P&L)
          item {
            SoldCarsSummaryCard(
              uiState = uiState,
              displayCurrency = displayCurrency,
              isKurdish = isKurdish
            )
          }

          if (soldCars.isEmpty()) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x24141F36)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(16.dp))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(imageVector = Icons.Default.Sell, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(36.dp))
                  Text(
                    text = if (isKurdish) "چ سەیارێن فرۆتی نەهاتینە تۆمارکرن" else "No Sold Cars Logged Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = if (isKurdish) {
                      "دەمێ سەیارەکێ دفرۆشی، ل دوگمەیا 'فرۆتنا سەیارێ' بدە دا قازانج یان زەرەرێ وێ بهێتە هەژمارتن."
                    } else {
                      "When you sell a vehicle, tap 'Sell Car' to record the sale price and track profit or loss."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                }
              }
            }
          } else {
            items(soldCars, key = { it.id }) { car ->
              CarItemCard(
                car = car,
                uiState = uiState,
                onAddMonthlyPayment = { carForMonthlyPayment = car },
                onAddSpending = { carForSpending = car },
                onUpdateValuation = { carForValuation = car },
                onEditPartnerShares = { carForEditingShares = car },
                onViewDetails = { carForDetails = car },
                onMarkAsSold = { carForSelling = car },
                onReopenAsActive = { viewModel.reopenCarAsActive(car.id) },
                onQuickEdit = { viewModel.openQuickEditForInvestment(car) },
                onDelete = { viewModel.deleteInvestment(car.id) },
                onDeletePayment = { viewModel.deleteCarPayment(it) },
                onDeleteSpending = { viewModel.deleteCarService(it) }
              )
            }
          }
        }
        3 -> { // Partner Cars (Buyer Paid Monthly & Sold Co-Owned Deals)
          val activePartner = uiState.activePartnerCars
          val soldPartner = uiState.soldPartnerCars
          val displayedPartnerCars = when (partnerCarFilter) {
            "ACTIVE" -> activePartner
            "SOLD" -> soldPartner
            else -> partnerCars
          }

          if (partnerCars.isEmpty()) {
            item {
              EmptyCarsPlaceholder(
                message = if (isKurdish) "چ سەیارێن شەریکاتیێ نینن" else "No partner co-owned cars yet.",
                isKurdish = isKurdish,
                onAddCar = { showAddCarDialog = true }
              )
            }
          } else {
            item {
              PartnerSettlementCard(uiState = uiState, isKurdish = isKurdish)
            }

            item {
              CarsListHeader(
                title = if (isKurdish) "سەیارێن شەریکاتیێ (${partnerCars.size})" else "Partner Co-Owned Cars (${partnerCars.size})",
                subtitle = if (isKurdish) "سەیارێن هەڤپشک دگەل شەریکان و دابەشکرنا قازانجی" else "Co-owned deals with capital and profit splits",
                onAddCar = { showAddCarDialog = true },
                isKurdish = isKurdish
              )
            }

            // Sub-filter tabs: All Partner Cars, Active (Paying Monthly), Sold & P&L
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf(
                  "ALL" to if (isKurdish) "هەمی (${partnerCars.size})" else "All (${partnerCars.size})",
                  "ACTIVE" to if (isKurdish) "بەردەست (${activePartner.size})" else "Active (${activePartner.size})",
                  "SOLD" to if (isKurdish) "فرۆتی (${soldPartner.size})" else "Sold (${soldPartner.size})"
                ).forEach { (filterKey, label) ->
                  val isSelected = partnerCarFilter == filterKey
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (isSelected) GoldAccent else Color.Transparent)
                      .clickable { partnerCarFilter = filterKey }
                      .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                      maxLines = 1
                    )
                  }
                }
              }
            }

            if (displayedPartnerCars.isEmpty()) {
              item {
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = Color(0x24141F36)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(14.dp))
                ) {
                  Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(
                      text = if (isKurdish) "چ سەیارێن شەریکاتیێ د ڤی بەشی دا نینن." else "No partner cars in this view.",
                      style = MaterialTheme.typography.bodySmall,
                      color = Color(0xFFE2E8F0)
                    )
                  }
                }
              }
            } else {
              items(displayedPartnerCars, key = { it.id }) { car ->
                CarItemCard(
                  car = car,
                  uiState = uiState,
                  onAddMonthlyPayment = { carForMonthlyPayment = car },
                  onAddSpending = { carForSpending = car },
                  onUpdateValuation = { carForValuation = car },
                  onEditPartnerShares = { carForEditingShares = car },
                  onViewDetails = { carForDetails = car },
                  onMarkAsSold = { carForSelling = car },
                  onReopenAsActive = { viewModel.reopenCarAsActive(car.id) },
                  onQuickEdit = { viewModel.openQuickEditForInvestment(car) },
                  onDelete = { viewModel.deleteInvestment(car.id) },
                  onDeletePayment = { viewModel.deleteCarPayment(it) },
                  onDeleteSpending = { viewModel.deleteCarService(it) }
                )
              }
            }
          }
        }
        4 -> { // Car Spending Log (Oil, Detailing, etc.)
          item {
            CarSpendingSectionHeader(
              totalSpending = uiState.totalCarServicesCost,
              paidByMe = uiState.totalCarServicesPaidByMe,
              paidByPartner = uiState.totalCarServicesPaidByPartner,
              displayCurrency = displayCurrency,
              isKurdish = isKurdish,
              filterCategory = filterSpendingCategory,
              onFilterChange = { filterSpendingCategory = it },
              onQuickAddSpending = {
                if (cars.isNotEmpty()) carForSpending = cars.first()
                else showAddCarDialog = true
              }
            )
          }

          val filteredServices = uiState.carServices.filter {
            if (filterSpendingCategory == "ALL") true
            else it.serviceType.contains(filterSpendingCategory, ignoreCase = true)
          }

          if (filteredServices.isEmpty()) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x24141F36)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(14.dp))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = if (isKurdish) "چ مەسروفاتێن سەیاران نەهاتینە تۆمارکرن" else "No maintenance or spending logged yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = if (isKurdish) "تۆمارکرنا گوهۆڕینا روون، پۆلیش، تایر و مەسروفاتێن هەر سەیارەکێ." else "Track oil changes, polishing, tires, and maintenance expenses per vehicle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0)
                  )
                }
              }
            }
          } else {
            items(filteredServices, key = { it.id }) { service ->
              CarSpendingItemRow(
                service = service,
                displayCurrency = displayCurrency,
                isKurdish = isKurdish,
                onClick = { carServiceForDetail = service },
                onDelete = { viewModel.deleteCarService(service.id) }
              )
            }
          }
        }
        5 -> { // Monthly Payments Log
          item {
            CarMonthlyPaymentsHeader(
              totalMonthlyPaid = uiState.totalCarsMonthlyPaid,
              totalRemainingMoney = uiState.totalCarsRemainingMoney,
              installmentCarsCount = uiState.totalInstallmentCarsCount,
              displayCurrency = displayCurrency,
              isKurdish = isKurdish,
              onQuickAddPayment = {
                if (cars.isNotEmpty()) carForMonthlyPayment = cars.first()
                else showAddCarDialog = true
              }
            )
          }

          val payments = uiState.carPayments.sortedByDescending { it.paymentDateMillis }
          if (payments.isEmpty()) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x24141F36)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(14.dp))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = if (isKurdish) "چ قستێن هەیڤانە نەهاتینە تۆمارکرن" else "No monthly installments logged yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = if (isKurdish) "تۆمارکرنا قستێ هەیڤانە یێ سەیارێ دگەل بەروار و پارێ ماى." else "Record monthly vehicle installments with date and remaining balance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0)
                  )
                }
              }
            }
          } else {
            items(payments, key = { it.id }) { payment ->
              CarMonthlyPaymentItemRow(
                payment = payment,
                displayCurrency = displayCurrency,
                isKurdish = isKurdish,
                onClick = { carPaymentForDetail = payment },
                onDelete = { viewModel.deleteCarPayment(payment.id) }
              )
            }
          }
        }
      }
    }

    FloatingActionButton(
      onClick = { showAddCarDialog = true },
      containerColor = Color(0xFF38BDF8),
      contentColor = Color(0xFF0F172A),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 24.dp, end = 20.dp)
        .testTag("fab_add_car")
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = "Add Car")
    }
  }

  // DIALOGS
  if (showAddCarDialog) {
    AddCarDialog(
      displayCurrency = displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { showAddCarDialog = false },
      onConfirm = { name, isCoOwned, partnerName, totalPrice, downPayment, myDownPayment, partnerDownPayment, notes, cur, isInstallment, soldPrice, downPaymentReceived, deductFromCash, customRate ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        if (isInstallment && soldPrice > 0) {
          viewModel.addCarWithInstallments(
            name = name,
            purchasePrice = totalPrice,
            currency = cur,
            notes = notes,
            isCoOwned = isCoOwned,
            partnerName = partnerName,
            myInvestmentAmount = if (isCoOwned) myDownPayment else totalPrice,
            partnerInvestmentAmount = if (isCoOwned) partnerDownPayment else 0.0,
            soldPrice = soldPrice,
            downPaymentReceived = downPaymentReceived,
            deductedFromCash = deductFromCash
          )
        } else {
          viewModel.addInvestment(
            name = name,
            category = if (isCoOwned) "Partner Car" else "Cars & Vehicles",
            initialCapital = totalPrice,
            currentValue = totalPrice,
            currency = cur,
            notes = notes,
            isCoOwned = isCoOwned,
            partnerName = partnerName,
            myInvestmentAmount = if (isCoOwned) myDownPayment else downPayment,
            partnerInvestmentAmount = if (isCoOwned) partnerDownPayment else 0.0,
            deductedFromCash = deductFromCash
          )
        }
        showAddCarDialog = false
      }
    )
  }

  if (carForMonthlyPayment != null) {
    val targetCar = carForMonthlyPayment!!
    AddCarMonthlyPaymentDialog(
      car = targetCar,
      uiState = uiState,
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      onDismiss = { carForMonthlyPayment = null },
      onConfirm = { amount, dateMillis, monthLabel, paidBy, myShare, partnerShare, notes, cur, customRate ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        viewModel.addCarPayment(
          carId = targetCar.id,
          carName = targetCar.name,
          amount = amount,
          currency = cur,
          paymentDateMillis = dateMillis,
          paymentMonth = monthLabel,
          paidBy = paidBy,
          partnerName = targetCar.partnerName.ifBlank { "Partner" },
          myShare = myShare,
          partnerShare = partnerShare,
          notes = notes
        )
        carForMonthlyPayment = null
      }
    )
  }

  if (carForSpending != null) {
    val targetCar = carForSpending!!
    AddCarSpendingDialog(
      car = targetCar,
      displayCurrency = displayCurrency,
      currentIqdRate = uiState.iqdRate,
      isKurdish = isKurdish,
      onDismiss = { carForSpending = null },
      onConfirm = { serviceType, cost, cur, paidBy, notes, customRate ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        viewModel.addCarService(
          carInvestmentId = targetCar.id,
          carName = targetCar.name,
          serviceType = serviceType,
          cost = cost,
          currency = cur,
          paidBy = paidBy,
          partnerName = targetCar.partnerName.ifBlank { "Partner" },
          notes = notes
        )
        carForSpending = null
      }
    )
  }

  if (carForValuation != null) {
    val targetCar = carForValuation!!
    UpdateCarValuationDialog(
      car = targetCar,
      isKurdish = isKurdish,
      onDismiss = { carForValuation = null },
      onConfirm = { newVal ->
        viewModel.updateInvestment(targetCar.copy(currentValue = newVal))
        carForValuation = null
      }
    )
  }

  if (carForEditingShares != null) {
    val targetCar = carForEditingShares!!
    EditPartnerCarSharesDialog(
      car = targetCar,
      isKurdish = isKurdish,
      onDismiss = { carForEditingShares = null },
      onConfirm = { newTotalPrice, newMyShare, newPartnerShare, newPartnerName ->
        viewModel.updateInvestment(
          targetCar.copy(
            initialCapital = newTotalPrice,
            currentValue = if (!targetCar.isSold && targetCar.soldPrice <= 0.0 && targetCar.currentValue == targetCar.initialCapital) {
              newTotalPrice
            } else {
              targetCar.currentValue
            },
            myInvestmentAmount = newMyShare,
            partnerInvestmentAmount = newPartnerShare,
            partnerName = newPartnerName.ifBlank { "Partner" }
          )
        )
        carForEditingShares = null
      }
    )
  }

  if (carForDetails != null) {
    val targetCar = carForDetails!!
    CarDetailsDialog(
      car = targetCar,
      uiState = uiState,
      isKurdish = isKurdish,
      onDismiss = { carForDetails = null },
      onAddPayment = {
        val c = targetCar
        carForDetails = null
        carForMonthlyPayment = c
      },
      onAddSpending = {
        val c = targetCar
        carForDetails = null
        carForSpending = c
      },
      onEditCar = {
        carForDetails = null
        viewModel.openQuickEditForInvestment(targetCar)
      },
      onSellCar = {
        val c = targetCar
        carForDetails = null
        carForSelling = c
      },
      onDeletePayment = { viewModel.deleteCarPayment(it) },
      onDeleteSpending = { viewModel.deleteCarService(it) }
    )
  }

  if (carServiceForDetail != null) {
    val srv = carServiceForDetail!!
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(srv.dateMillis))
    val paidLabel = when (srv.paidBy) {
      "ME" -> if (isKurdish) "👤 ژ لایێ من" else "👤 I Paid"
      "PARTNER" -> "🤝 ${srv.partnerName}"
      else -> if (isKurdish) "⚖️ 50/50 هەڤپشک" else "⚖️ Split 50/50"
    }
    val parentCar = allCarsRaw.find { it.id == srv.carInvestmentId }
    CarSubItemDetailActionDialog(
      title = srv.serviceType,
      subtitle = "🚗 ${srv.carName}",
      amountFormatted = CurrencyService.format(srv.cost, srv.currency),
      secondaryAmountFormatted = CurrencyService.formatShort(
        CurrencyService.convert(srv.cost, srv.currency, displayCurrency, uiState.iqdRate, entryIqdRate = srv.exchangeRateAtEntry),
        displayCurrency
      ),
      dateFormatted = dateStr,
      paidByLabel = paidLabel,
      notes = srv.notes,
      accentColor = GoldAccent,
      isKurdish = isKurdish,
      onOpenCar = parentCar?.let { car -> { carForDetails = car } },
      onDelete = { viewModel.deleteCarService(srv.id) },
      onDismiss = { carServiceForDetail = null }
    )
  }

  if (carPaymentForDetail != null) {
    val pay = carPaymentForDetail!!
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(pay.paymentDateMillis))
    val paidLabel = when (pay.paidBy) {
      "ME" -> if (isKurdish) "👤 وەرگرتی" else "👤 Received"
      "PARTNER" -> "🤝 ${pay.partnerName}"
      else -> if (isKurdish) "⚖️ 50/50 هەڤپشک" else "⚖️ Split 50/50"
    }
    val parentCar = allCarsRaw.find { it.id == pay.carId }
    CarSubItemDetailActionDialog(
      title = pay.paymentMonth.ifBlank { dateStr },
      subtitle = "🚗 ${pay.carName}",
      amountFormatted = CurrencyService.format(pay.amount, pay.currency),
      secondaryAmountFormatted = CurrencyService.formatShort(
        CurrencyService.convert(pay.amount, pay.currency, displayCurrency, uiState.iqdRate, entryIqdRate = pay.exchangeRateAtEntry),
        displayCurrency
      ),
      dateFormatted = dateStr,
      paidByLabel = paidLabel,
      notes = pay.notes,
      accentColor = EmeraldLight,
      isKurdish = isKurdish,
      onOpenCar = parentCar?.let { car -> { carForDetails = car } },
      onDelete = { viewModel.deleteCarPayment(pay.id) },
      onDismiss = { carPaymentForDetail = null }
    )
  }

  if (carForSelling != null) {
    val targetCar = carForSelling!!
    MarkCarAsSoldDialog(
      car = targetCar,
      uiState = uiState,
      isKurdish = isKurdish,
      onDismiss = { carForSelling = null },
      onConfirm = { soldPrice, downPaymentReceived, isInstallment, notes, customRate ->
        if (customRate != null && customRate > 0) {
          viewModel.updateIqdRate(customRate)
        }
        if (isInstallment) {
          viewModel.sellCarOnInstallments(targetCar.id, soldPrice, downPaymentReceived, notes)
        } else {
          viewModel.markCarAsSold(targetCar.id, soldPrice, notes)
        }
        carForSelling = null
      }
    )
  }
}

// -------------------------------------------------------------
// CARS LIST ACTION HEADER
// -------------------------------------------------------------
@Composable
fun CarsListHeader(
  title: String,
  subtitle: String,
  onAddCar: () -> Unit,
  isKurdish: Boolean = false
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x24141F36)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(14.dp))
      .testTag("cars_list_action_header")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(Color(0xFF38BDF8).copy(alpha = 0.20f), CircleShape)
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(20.dp)
          )
        }
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = Color(0xFFE2E8F0)
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = onAddCar,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.testTag("btn_add_car_header")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          tint = Color(0xFF0F172A),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isKurdish) "+ داخلکرنا سەیارێ" else "+ Add Vehicle",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = Color(0xFF0F172A)
        )
      }
    }
  }
}

// -------------------------------------------------------------
// EXECUTIVE BANNER (ORGANIZED 2x2 GRID)
// -------------------------------------------------------------
@Composable
fun CarsExecutiveBanner(
  uiState: FinanceUiState,
  carsCount: Int,
  partnerCarsCount: Int,
  displayCurrency: String,
  isKurdish: Boolean = false,
  onAddCar: () -> Unit = {}
) {
  val totalValuation = uiState.totalActiveCarsValuation
  val totalInvestedCash = uiState.totalCarsCashInvested
  val totalRealizedProfit = uiState.totalSoldCarsProfitLoss
  val totalSpending = uiState.totalCarServicesCost
  val totalMonthlyPaid = uiState.totalCarsMonthlyPaid
  val totalRemaining = uiState.totalCarsRemainingMoney
  val isWinningOverall = totalRealizedProfit >= 0.0
  val SkyBlue = Color(0xFF38BDF8)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .background(ShowroomPedestalBrush, RoundedCornerShape(22.dp))
      .border(1.2.dp, ShowroomPedestalBorderBrush, RoundedCornerShape(22.dp))
      .testTag("cars_executive_banner"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                )
              )
              .border(1.dp, ShowroomNeonGlow.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(24.dp)
            )
          }
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isKurdish) "پێشانگەها شاهانە یا سەیاران" else "Luxury Automotive Showroom",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = ShowroomNeonGlow
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "STUDIO",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(ShowroomSpotlightCyan)
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
            Text(
              text = if (isKurdish) {
                "$carsCount سەیارێن پێشانگەهێ • $partnerCarsCount شەریکاتی • ${uiState.activeCarInvestments.size} بەردەست"
              } else {
                "$carsCount Showroom Vehicles • $partnerCarsCount Partner • ${uiState.activeCarInvestments.size} on Floor"
              },
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFE2E8F0)
            )
          }
        }

        Box(
          modifier = Modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
              )
            )
            .border(1.dp, ShowroomNeonGlow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onAddCar() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isKurdish) "+ سەیارا نوی" else "+ New Intake",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x280F1728))
            .border(1.dp, ShowroomSpotlightCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "💰 سەرمایێ کڕینێ" else "💰 Total Cost",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = ShowroomSpotlightCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyService.format(totalInvestedCash, displayCurrency),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isKurdish) {
                "مەسروفات: ${CurrencyService.formatShort(totalSpending, displayCurrency)}"
              } else {
                "Maint: ${CurrencyService.formatShort(totalSpending, displayCurrency)}"
              },
              fontSize = 10.sp,
              color = Color(0xFFE2E8F0)
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x280D1E22))
            .border(1.dp, EmeraldLight.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "🚗 بهایێ بەردەست" else "🚗 Active Fleet Value",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyService.format(totalValuation, displayCurrency),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isKurdish) "${uiState.activeCarInvestments.size} سەیارێن بەردەست" else "${uiState.activeCarInvestments.size} on showroom floor",
              fontSize = 10.sp,
              color = Color(0xFFE2E8F0)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val profitColor = if (isWinningOverall) EmeraldLight else LossRed
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(profitColor.copy(alpha = 0.16f))
            .border(1.dp, profitColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) {
                if (isWinningOverall) "📈 قازانجێ فرۆتنێ" else "📉 زەرەرێ فرۆتنێ"
              } else {
                if (isWinningOverall) "📈 Sold Net Profit" else "📉 Sold Net Loss"
              },
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = profitColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${if (isWinningOverall) "+" else ""}${CurrencyService.format(totalRealizedProfit, displayCurrency)}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = profitColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isKurdish) "${uiState.soldCarInvestments.size} سەیارێن فرۆتی" else "${uiState.soldCarInvestments.size} sold deals",
              fontSize = 10.sp,
              color = Color(0xFFE2E8F0)
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x28221A0C))
            .border(1.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "📅 قستێن وەرگرتی (واصل)" else "📅 Installments Paid",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyService.format(totalMonthlyPaid, displayCurrency),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isKurdish) {
                "باقی: ${CurrencyService.formatShort(totalRemaining, displayCurrency)}"
              } else {
                "Left: ${CurrencyService.formatShort(totalRemaining, displayCurrency)}"
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = GoldAccent
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB SELECTOR
// -------------------------------------------------------------
@Composable
fun CarsTabSelector(
  selectedTab: Int,
  onSelectTab: (Int) -> Unit,
  allCount: Int,
  activeCount: Int,
  soldCount: Int,
  partnerCount: Int,
  spendingCount: Int,
  paymentsCount: Int = 0,
  isKurdish: Boolean = false
) {
  val tabs = listOf(
    if (isKurdish) "🚗 هەمی ($allCount)" else "🚗 All ($allCount)",
    if (isKurdish) "✅ بەردەست ($activeCount)" else "✅ Active ($activeCount)",
    if (isKurdish) "🏷️ فرۆتی ($soldCount)" else "🏷️ Sold ($soldCount)",
    if (isKurdish) "🤝 شەریک ($partnerCount)" else "🤝 Partner ($partnerCount)",
    if (isKurdish) "🔧 مەسروفات ($spendingCount)" else "🔧 Maint ($spendingCount)",
    if (isKurdish) "📅 قست ($paymentsCount)" else "📅 Installments ($paymentsCount)"
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0x28070B14))
      .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(14.dp))
      .padding(6.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    tabs.forEachIndexed { index, title ->
      val isSelected = selectedTab == index
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(
            if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
            else Brush.linearGradient(listOf(Color(0x301E293B), Color(0x301E293B)))
          )
          .border(
            width = if (isSelected) 1.dp else 0.5.dp,
            brush = if (isSelected) Brush.horizontalGradient(listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8))) else Brush.linearGradient(listOf(Color(0x25FFFFFF), Color(0x25FFFFFF))),
            shape = RoundedCornerShape(10.dp)
          )
          .clickable { onSelectTab(index) }
          .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
          color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
          maxLines = 1
        )
      }
    }
  }
}

// -------------------------------------------------------------
// SOLD CARS SUMMARY CARD
// -------------------------------------------------------------
@Composable
fun SoldCarsSummaryCard(
  uiState: FinanceUiState,
  displayCurrency: String,
  isKurdish: Boolean = false
) {
  val soldCars = uiState.soldCarInvestments
  val totalProfitLoss = uiState.totalSoldCarsProfitLoss
  val totalRevenue = uiState.totalSoldCarsRevenue
  val totalCost = uiState.totalSoldCarsCost
  val isWinningOverall = totalProfitLoss >= 0
  val statusColor = if (isWinningOverall) EmeraldLight else LossRed

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x24141F36))
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(statusColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isWinningOverall) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = if (isKurdish) "ئەنجامێ سەیارێن فرۆتی" else "Sold Vehicles Profit & Loss",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = Color.White
            )
            Text(
              text = if (isKurdish) {
                "${soldCars.size} سەیارە هاتینە فرۆتن"
              } else {
                "${soldCars.size} vehicles sold"
              },
              fontSize = 11.sp,
              color = Color(0xFFE2E8F0)
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${if (isWinningOverall) "+" else ""}${CurrencyService.format(totalProfitLoss, displayCurrency)}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = statusColor
          )
          Text(
            text = if (isKurdish) {
              if (isWinningOverall) "🟢 قازانج (فایدە)" else "🔴 زەرەر (خسارە)"
            } else {
              if (isWinningOverall) "🏆 WINNING" else "⚠️ LOSING"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = statusColor
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x350F1728))
            .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(10.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "سەرمایێ کڕینێ و مەسروف" else "Total Capital & Cost",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFBAE6FD)
            )
            Text(
              text = CurrencyService.format(totalCost, displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldLight.copy(alpha = 0.1f))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "کۆژمێ فرۆتنێ" else "Total Sale Revenue",
              fontSize = 10.sp,
              color = EmeraldLight
            )
            Text(
              text = CurrencyService.format(totalRevenue, displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldLight
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CAR ITEM CARD (ORGANIZED & CLEAN)
// -------------------------------------------------------------
@Composable
fun CarItemCard(
  car: InvestmentEntity,
  uiState: FinanceUiState,
  onAddMonthlyPayment: () -> Unit,
  onAddSpending: () -> Unit,
  onUpdateValuation: () -> Unit,
  onEditPartnerShares: () -> Unit = {},
  onViewDetails: () -> Unit,
  onMarkAsSold: () -> Unit,
  onReopenAsActive: () -> Unit,
  onQuickEdit: () -> Unit = {},
  onDelete: () -> Unit,
  onDeletePayment: (Long) -> Unit = {},
  onDeleteSpending: (Long) -> Unit = {}
) {
  val isKurdish = AppStrings.isKurdish(uiState.settings.language)
  val displayCurrency = uiState.displayCurrency
  val SkyBlue = Color(0xFF38BDF8)

  val purchasePrice = CurrencyService.convert(car.initialCapital, car.currency, displayCurrency, uiState.iqdRate)
  val totalSpending = uiState.getCarTotalSpending(car.id)
  val totalCashInvested = purchasePrice + totalSpending
  val services = uiState.getCarServicesForCar(car.id)
  val payments = uiState.getCarMonthlyPayments(car.id)
  val totalMonthlyPaid = uiState.getCarTotalPaid(car)

  val isInstallmentDeal = car.soldPrice > 0
  val remainingMoney = uiState.getCarRemainingMoney(car)
  val isInstallmentFinished = isInstallmentDeal && remainingMoney <= 0.001
  val soldVal = if (isInstallmentDeal) CurrencyService.convert(car.soldPrice, car.currency, displayCurrency, uiState.iqdRate) else 0.0

  val netProfit = uiState.getCarNetProfitLoss(car)
  val isWinning = uiState.isCarWinning(car)
  val roi = uiState.getCarRoiPercent(car)
  val totalRevenue = uiState.getCarTotalRevenue(car)
  val marketValuation = CurrencyService.convert(car.currentValue, car.currency, displayCurrency, uiState.iqdRate)

  val accentColor = when {
    isInstallmentDeal -> if (isInstallmentFinished) EmeraldLight else GoldAccent
    car.isSold -> if (isWinning) EmeraldLight else LossRed
    car.isCoOwned -> GoldAccent
    else -> SkyBlue
  }

  var isExpanded by remember { mutableStateOf(false) }

  val bayNumber = (car.id.hashCode() % 89 + 11).let { if (it < 0) -it else it }
  val showroomPodiumBorder = if (car.isSold) {
    if (isWinning) ShowroomPedestalActiveBorderBrush else ShowroomPedestalBorderBrush
  } else if (car.isCoOwned) {
    Brush.linearGradient(
      listOf(Color(0xFFFFD700), Color(0xFF38BDF8), Color(0xFFFFD700), Color(0xFFBAE6FD), Color(0xFFFFD700))
    )
  } else {
    ShowroomPedestalActiveBorderBrush
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.2.dp, showroomPodiumBorder, RoundedCornerShape(22.dp))
      .clip(RoundedCornerShape(22.dp))
      .clickable { onViewDetails() }
      .testTag("car_item_card_${car.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(ShowroomPedestalBrush)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 0. Royal Palace Showroom Podium Top Badge
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x35060A14))
          .border(0.6.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          Text(text = "👑", fontSize = 11.sp)
          Text(
            text = if (isKurdish) "سەکۆیا شاهانە • پێشانگەها سەیاران" else "ROYAL SHOWROOM • PODIUM BAY #$bayNumber",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = Color(0xFFFFD700)
          )
        }
        Text(
          text = if (car.isSold) (if (isKurdish) "فرۆتی" else "ARCHIVED") else (if (isKurdish) "بەردەست" else "VIP PODIUM"),
          fontSize = 9.sp,
          fontWeight = FontWeight.ExtraBold,
          color = accentColor
        )
      }

      // 1. Header: Vehicle Icon, Name, Clean Status Badges & Delete Button
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
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(accentColor.copy(alpha = 0.20f))
              .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when {
                car.isSold -> Icons.Default.Sell
                car.isCoOwned -> Icons.Default.Handshake
                else -> Icons.Default.DirectionsCar
              },
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(24.dp)
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = car.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Status Pill
              val statusBadgeText = when {
                isInstallmentDeal && isInstallmentFinished -> if (isKurdish) "🎉 هەمی واصل بوو" else "🎉 Fully Paid"
                isInstallmentDeal -> if (isKurdish) "🏷️ ب قست" else "🏷️ Installment"
                car.isSold -> if (isKurdish) {
                  if (isWinning) "🟢 فرۆتی (قازانج)" else "🔴 فرۆتی (زەرەر)"
                } else {
                  if (isWinning) "🟢 Sold (Profit)" else "🔴 Sold (Loss)"
                }
                else -> if (isKurdish) "✅ بەردەست" else "✅ Active"
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(accentColor.copy(alpha = 0.20f))
                  .border(0.8.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Text(
                  text = statusBadgeText,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = accentColor
                )
              }

              // Ownership Pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(
                    if (car.isCoOwned) GoldAccent.copy(alpha = 0.20f)
                    else SkyBlue.copy(alpha = 0.18f)
                  )
                  .border(
                    0.8.dp,
                    if (car.isCoOwned) GoldAccent.copy(alpha = 0.45f) else SkyBlue.copy(alpha = 0.45f),
                    RoundedCornerShape(6.dp)
                  )
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (car.isCoOwned) {
                    val pName = car.partnerName.ifBlank { if (isKurdish) "شەریک" else "Partner" }
                    "🤝 $pName (${car.mySharePercent.toInt()}%)"
                  } else {
                    if (isKurdish) "🚗 سەربەخۆ" else "🚗 Solo"
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (car.isCoOwned) GoldAccent else SkyBlue
                )
              }

              if (car.deductedFromCash) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LossRed.copy(alpha = 0.20f))
                    .border(0.8.dp, LossRed.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = if (isKurdish) "💵 کێم ژ کاشی" else "💵 -Cash",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LossRed
                  )
                }
              }

              if (car.isSold || isInstallmentDeal) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldLight.copy(alpha = 0.20f))
                    .border(0.8.dp, EmeraldLight.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = if (isKurdish) "💵 +کاش" else "💵 +Cash",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldLight
                  )
                }
              }
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = onQuickEdit,
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(GoldAccent.copy(alpha = 0.18f))
              .border(0.8.dp, GoldAccent.copy(alpha = 0.40f), CircleShape)
              .testTag("quick_edit_car_${car.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit & Preview",
              tint = GoldAccent,
              modifier = Modifier.size(16.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(34.dp)
              .testTag("delete_car_${car.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete",
              tint = LossRed,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      if (car.notes.isNotBlank()) {
        Text(
          text = "📝 ${car.notes}",
          fontSize = 11.5.sp,
          color = Color(0xFFE2E8F0),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // 2. Unified 3-Box Financial Comparison (No Duplicate Rows!)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Box 1: Buy Price + Spending
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x350F1728))
            .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) "سعرا کڕینێ" else "Buy Cost",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFBAE6FD)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = CurrencyService.format(totalCashInvested, displayCurrency),
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (totalSpending > 0) {
                if (isKurdish) "+ مەسروف: ${CurrencyService.formatShort(totalSpending, displayCurrency)}"
                else "+ Maint: ${CurrencyService.formatShort(totalSpending, displayCurrency)}"
              } else {
                if (isKurdish) "بێ مەسروف" else "No maint"
              },
              fontSize = 9.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (totalSpending > 0) GoldAccent else Color(0xFFCBD5E1)
            )
          }
        }

        // Box 2: Sale Price OR Market Valuation
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(SkyBlue.copy(alpha = 0.14f))
            .border(1.dp, SkyBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = when {
                isInstallmentDeal || car.isSold -> if (isKurdish) "بهایێ فرۆتنێ" else "Sale Price"
                else -> if (isKurdish) "بهایێ نوکە" else "Market Value"
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = SkyBlue
            )
            Spacer(modifier = Modifier.height(2.dp))
            val secondBoxVal = when {
              isInstallmentDeal -> soldVal
              car.isSold -> totalRevenue
              else -> marketValuation
            }
            Text(
              text = CurrencyService.format(secondBoxVal, displayCurrency),
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = when {
                isInstallmentDeal -> if (isKurdish) "فرۆتنا ب قست" else "Installment deal"
                car.isSold -> if (isKurdish) "فرۆتنا کاش" else "Cash sale"
                else -> if (isKurdish) "نرخێ بازاڕی" else "Active estimate"
              },
              fontSize = 9.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFE2E8F0)
            )
          }
        }

        // Box 3: Profit / Loss Result
        val pnlValue = when {
          isInstallmentDeal -> soldVal - totalCashInvested
          car.isSold -> netProfit
          else -> marketValuation - totalCashInvested
        }
        val isPositivePnl = pnlValue >= 0.0
        val pnlColor = if (isPositivePnl) EmeraldLight else LossRed

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(pnlColor.copy(alpha = 0.12f))
            .border(1.dp, pnlColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = if (isKurdish) {
                if (isPositivePnl) "🟢 قازانج (فایدە)" else "🔴 زەرەر (خسارە)"
              } else {
                if (isPositivePnl) "🟢 Profit" else "🔴 Loss"
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = pnlColor,
              maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${if (isPositivePnl) "+" else ""}${CurrencyService.format(pnlValue, displayCurrency)}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = pnlColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            val pct = if (totalCashInvested > 0) (pnlValue / totalCashInvested) * 100.0 else 0.0
            Text(
              text = "${if (isPositivePnl) "+" else ""}${"%.1f".format(pct)}%",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = pnlColor
            )
          }
        }
      }

      // 3. Dedicated Installment Progress Box (When sold on installments or has monthly payments)
      if (isInstallmentDeal) {
        val progress = if (soldVal > 0) (totalMonthlyPaid / soldVal).toFloat().coerceIn(0f, 1f) else 0f
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, (if (isInstallmentFinished) EmeraldLight else GoldAccent).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) {
                  "✅ واصل: ${CurrencyService.format(totalMonthlyPaid, displayCurrency)} (${payments.size} قست)"
                } else {
                  "✅ Paid: ${CurrencyService.format(totalMonthlyPaid, displayCurrency)} (${payments.size})"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLight
              )
              Text(
                text = if (isInstallmentFinished) {
                  if (isKurdish) "🎉 100% تەمام بوو" else "🎉 100% Paid"
                } else {
                  if (isKurdish) "⏳ باقی: ${CurrencyService.format(remainingMoney, displayCurrency)}"
                  else "⏳ Left: ${CurrencyService.format(remainingMoney, displayCurrency)}"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isInstallmentFinished) EmeraldLight else GoldAccent
              )
            }

            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = EmeraldLight,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }
      } else if (payments.isNotEmpty() || totalMonthlyPaid > 0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldLight.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) "📅 قستێن وەرگرتی (${payments.size} قست):" else "📅 Installments Paid (${payments.size}):",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Text(
              text = CurrencyService.format(totalMonthlyPaid, displayCurrency),
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldLight
            )
          }
        }
      }

      // 4. Clean Partner Split Box (Only for Partner Cars)
      if (car.isCoOwned) {
        val myDown = uiState.getCarMyDownPayment(car)
        val partnerDown = uiState.getCarPartnerDownPayment(car)
        val pName = car.partnerName.ifBlank { if (isKurdish) "شەریک" else "Partner" }
        val existingShareValidation = validatePartnershipShares(
          totalPrice = car.initialCapital,
          myShare = car.myInvestmentAmount,
          partnerShare = car.partnerInvestmentAmount,
          currencySymbolOrCode = car.currency
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (!existingShareValidation.isValid) LossRed.copy(alpha = 0.12f)
              else GoldAccent.copy(alpha = 0.08f)
            )
            .border(
              1.dp,
              if (!existingShareValidation.isValid) LossRed.copy(alpha = 0.5f)
              else GoldAccent.copy(alpha = 0.25f),
              RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  text = if (isKurdish) {
                    "👤 پشکا تە (${car.mySharePercent.toInt()}%): ${CurrencyService.format(myDown, displayCurrency)}"
                  } else {
                    "👤 You (${car.mySharePercent.toInt()}%): ${CurrencyService.format(myDown, displayCurrency)}"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldLight
                )
                Text(
                  text = "🤝 $pName (${car.partnerSharePercent.toInt()}%): ${CurrencyService.format(partnerDown, displayCurrency)}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                  .clickable { onEditPartnerShares() }
                  .padding(horizontal = 8.dp, vertical = 5.dp)
                  .testTag("btn_edit_car_shares_${car.id}")
              ) {
                Text(
                  text = if (isKurdish) "✏️ پشک" else "✏️ Shares",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoldAccent
                )
              }
            }

            if (!existingShareValidation.isValid) {
              Divider(color = LossRed.copy(alpha = 0.3f))
              Text(
                text = if (isKurdish) "⚠️ خەلەتە، مەبلەغی کامل بکە!" else "⚠️ Wrong Share Amount! Complete the Full Price",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LossRed
              )
              Text(
                text = if (isKurdish) existingShareValidation.errorMessageKu else existingShareValidation.errorMessageEn,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Button(
                onClick = onEditPartnerShares,
                colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_fix_car_shares_${car.id}")
              ) {
                Text(
                  text = if (isKurdish) "⚡ کاملکرن و ڕاستڤەکرنا پشکان" else "⚡ Complete & Fix Shares",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }
            }

            if (car.isSold) {
              val myProfit = uiState.getCarMyProfitLossShare(car)
              val partnerProfit = uiState.getCarPartnerProfitLossShare(car)
              Divider(color = GoldAccent.copy(alpha = 0.2f))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isKurdish) {
                    "قازانجێ تە: ${if (myProfit >= 0) "+" else ""}${CurrencyService.format(myProfit, displayCurrency)}"
                  } else {
                    "Your P&L: ${if (myProfit >= 0) "+" else ""}${CurrencyService.format(myProfit, displayCurrency)}"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (myProfit >= 0) EmeraldLight else LossRed
                )
                Text(
                  text = if (isKurdish) {
                    "پشکا $pName: ${if (partnerProfit >= 0) "+" else ""}${CurrencyService.format(partnerProfit, displayCurrency)}"
                  } else {
                    "$pName P&L: ${if (partnerProfit >= 0) "+" else ""}${CurrencyService.format(partnerProfit, displayCurrency)}"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (partnerProfit >= 0) EmeraldLight else LossRed
                )
              }
            }
          }
        }
      }

      // 5. Organized 2x2 Action Grid
      if (isInstallmentDeal) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onAddMonthlyPayment,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_add_car_installment_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "+ داخلکرنا قستی" else "+ Installment",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
              )
            }

            if (!isInstallmentFinished) {
              Button(
                onClick = onMarkAsSold,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isKurdish) "هەمی واصل بوو" else "Mark Fully Paid",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A),
                  maxLines = 1
                )
              }
            } else {
              OutlinedButton(
                onClick = onMarkAsSold,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(imageVector = Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isKurdish) "دەستکاریا فرۆتنێ" else "Edit Deal",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = onAddSpending,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_add_car_spending_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "+ مەسروف" else "+ Spending",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBlue,
                maxLines = 1
              )
            }

            OutlinedButton(
              onClick = onViewDetails,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_car_details_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "وردەکاری (${payments.size + services.size})" else "Details (${payments.size + services.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
              )
            }
          }
        }
      } else if (car.isSold) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onMarkAsSold,
            colors = ButtonDefaults.buttonColors(containerColor = if (isWinning) EmeraldLight else LossRed),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.weight(1.1f)
          ) {
            Icon(imageVector = Icons.Default.Sell, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "بهایێ فرۆتنێ" else "Edit Sale",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A),
              maxLines = 1
            )
          }

          OutlinedButton(
            onClick = onReopenAsActive,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "زڤڕاندن" else "Reopen",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }

          OutlinedButton(
            onClick = onViewDetails,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "وردەکاری" else "Details",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onMarkAsSold,
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_mark_car_sold_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.Sell, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "فرۆتنا سەیارێ" else "Sell Car",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
              )
            }

            Button(
              onClick = onAddMonthlyPayment,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_add_car_payment_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "+ داخلکرنا قستی" else "+ Installment",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = onAddSpending,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_add_car_spending_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "+ داخلکرنا مەسروفی" else "+ Spending",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBlue,
                maxLines = 1
              )
            }

            OutlinedButton(
              onClick = onViewDetails,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("btn_car_details_${car.id}")
            ) {
              Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "وردەکاری (${payments.size + services.size})" else "Details (${payments.size + services.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
              )
            }
          }
        }
      }

      // 6. Single Clean Expandable Activity Log (No Duplicate Snippets!)
      if (payments.isNotEmpty() || services.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val lastPayment = payments.firstOrNull()
          val lastService = services.firstOrNull()

          val summaryText = when {
            lastPayment != null && lastService != null -> {
              val pDate = SimpleDateFormat("dd MMM", Locale.US).format(Date(lastPayment.paymentDateMillis))
              if (isKurdish) {
                "دویماهیک قست: ${CurrencyService.format(lastPayment.amount, lastPayment.currency)} ($pDate) • 🔧 ${lastService.serviceType}"
              } else {
                "Last: ${CurrencyService.format(lastPayment.amount, lastPayment.currency)} ($pDate) • 🔧 ${lastService.serviceType}"
              }
            }
            lastPayment != null -> {
              val pDate = SimpleDateFormat("dd MMM", Locale.US).format(Date(lastPayment.paymentDateMillis))
              if (isKurdish) {
                "دویماهیک قست: ${CurrencyService.format(lastPayment.amount, lastPayment.currency)} ($pDate)"
              } else {
                "Last Installment: ${CurrencyService.format(lastPayment.amount, lastPayment.currency)} ($pDate)"
              }
            }
            lastService != null -> {
              if (isKurdish) {
                "دویماهیک مەسروف: ${lastService.serviceType} (${CurrencyService.format(lastService.cost, lastService.currency)})"
              } else {
                "Last Maint: ${lastService.serviceType} (${CurrencyService.format(lastService.cost, lastService.currency)})"
              }
            }
            else -> ""
          }

          Text(
            text = summaryText,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }

        AnimatedVisibility(visible = isExpanded) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = if (isKurdish) "تۆمارێن دویماهیکێ" else "Recent Activity",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurface
            )

            payments.forEach { p ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(p.paymentDateMillis))
                Text(
                  text = "📅 ${p.paymentMonth.ifBlank { dateStr }}",
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = CurrencyService.format(p.amount, p.currency),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                  )
                  IconButton(
                    onClick = { onDeletePayment(p.id) },
                    modifier = Modifier
                      .size(28.dp)
                      .testTag("delete_car_payment_inline_${p.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = if (isKurdish) "ژێبرن" else "Delete",
                      tint = LossRed,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }

            services.forEach { s ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(s.dateMillis))
                Text(
                  text = "🔧 ${s.serviceType} ($dateStr)",
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = CurrencyService.format(s.cost, s.currency),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                  )
                  IconButton(
                    onClick = { onDeleteSpending(s.id) },
                    modifier = Modifier
                      .size(28.dp)
                      .testTag("delete_car_service_inline_${s.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = if (isKurdish) "ژێبرن" else "Delete",
                      tint = LossRed,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Glowing Under-Chassis Neon & Ground Reflection Light Bar
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(CircleShape)
          .background(
            Brush.horizontalGradient(
              listOf(
                Color.Transparent,
                accentColor.copy(alpha = 0.25f),
                accentColor.copy(alpha = 0.95f),
                Color(0xFFFFFFFF),
                accentColor.copy(alpha = 0.95f),
                accentColor.copy(alpha = 0.25f),
                Color.Transparent
              )
            )
          )
      )
    }
  }
}

// -------------------------------------------------------------
// PARTNER CARS & CUSTOMER SETTLEMENT CARD
// -------------------------------------------------------------
@Composable
fun PartnerSettlementCard(
  uiState: FinanceUiState,
  isKurdish: Boolean = false
) {
  val balance = uiState.partnerMaintenanceBalance
  val displayCurrency = uiState.displayCurrency
  val myCash = uiState.myTotalPartnerCarsCapital
  val partnerCash = uiState.partnerTotalPartnerCarsCapital
  val totalPartnerProfit = uiState.totalSoldPartnerCarsProfitLoss
  val myProfitShare = uiState.mySoldPartnerCarsProfitShare
  val partnerProfitShare = uiState.partnerSoldPartnerCarsProfitShare
  val soldPartnerCars = uiState.soldPartnerCars
  val partnerMonthlyPaid = uiState.partnerCars.sumOf { uiState.getCarTotalPaid(it) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x24141F36))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(GoldAccent.copy(alpha = 0.20f))
              .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
          }
          Column {
            Text(
              text = if (isKurdish) "سەرمایە و حساباتا شەریکاتیێ" else "Partner Co-Owned Cars Summary",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = Color.White
            )
            Text(
              text = if (isKurdish) "پشکێن سەرمایێ، قازانج و مەسروفاتێن هەڤپشک" else "Capital contributions, profit shares & maintenance balance",
              fontSize = 11.sp,
              color = Color(0xFFE2E8F0)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.12f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = if (isKurdish) "👤 سەرمایێ تە" else "👤 My Cash Invested",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = CurrencyService.format(myCash, displayCurrency),
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldLight
            )
          }
        }

        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = if (isKurdish) "🤝 سەرمایێ شەریکی" else "🤝 Partner Invested",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = CurrencyService.format(partnerCash, displayCurrency),
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GoldAccent
            )
          }
        }
      }

      if (partnerMonthlyPaid > 0) {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.1f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isKurdish) "📅 قستێن وەرگرتی یێن شەریکی:" else "📅 Partner Car Installments Paid:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Text(
              text = CurrencyService.format(partnerMonthlyPaid, displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldLight
            )
          }
        }
      }

      if (soldPartnerCars.isNotEmpty()) {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = (if (totalPartnerProfit >= 0) EmeraldLight else LossRed).copy(alpha = 0.12f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = if (isKurdish) "ئەنجامێ سەیارێن شەریکاتیێ یێن فرۆتی" else "Sold Partner Cars P&L",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${if (totalPartnerProfit >= 0) "+" else ""}${CurrencyService.format(totalPartnerProfit, displayCurrency)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (totalPartnerProfit >= 0) EmeraldLight else LossRed
              )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = if (isKurdish) {
                  "پشکا تە: ${if (myProfitShare >= 0) "+" else ""}${CurrencyService.format(myProfitShare, displayCurrency)}"
                } else {
                  "Your Share: ${if (myProfitShare >= 0) "+" else ""}${CurrencyService.format(myProfitShare, displayCurrency)}"
                },
                fontSize = 10.sp,
                color = if (myProfitShare >= 0) EmeraldLight else LossRed,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isKurdish) {
                  "پشکا شەریکی: ${if (partnerProfitShare >= 0) "+" else ""}${CurrencyService.format(partnerProfitShare, displayCurrency)}"
                } else {
                  "Partner Share: ${if (partnerProfitShare >= 0) "+" else ""}${CurrencyService.format(partnerProfitShare, displayCurrency)}"
                },
                fontSize = 10.sp,
                color = if (partnerProfitShare >= 0) EmeraldLight else LossRed,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isKurdish) "⚖️ حساباتا مەسروفاتێن سەیاران" else "⚖️ Maintenance Balance",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = when {
                balance > 0 -> if (isKurdish) "شەریک قەردارێ تەیە ژ مەسروفاتان" else "Partner owes you for maintenance"
                balance < 0 -> if (isKurdish) "تۆ قەردارێ شەریکی ژ مەسروفاتان" else "You owe partner for maintenance"
                else -> if (isKurdish) "حساباتا مەسروفاتان سفرە (بەرامبەر)" else "Maintenance split equally"
              },
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Text(
            text = CurrencyService.format(kotlin.math.abs(balance), displayCurrency),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (balance >= 0) EmeraldLight else LossRed
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CAR SPENDING SECTION HEADER
// -------------------------------------------------------------
@Composable
fun CarSpendingSectionHeader(
  totalSpending: Double,
  paidByMe: Double,
  paidByPartner: Double,
  displayCurrency: String,
  isKurdish: Boolean = false,
  filterCategory: String,
  onFilterChange: (String) -> Unit,
  onQuickAddSpending: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0x24141F36))
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isKurdish) "🔧 سەرجەمێ مەسروفاتێن سەیاران" else "🔧 Total Vehicle Maintenance",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = CurrencyService.format(totalSpending, displayCurrency),
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = GoldAccent
            )
          }

          Button(
            onClick = onQuickAddSpending,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "+ مەسروف بنڤیسە" else "+ Add Spending",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(EmeraldLight.copy(alpha = 0.16f))
              .border(0.8.dp, EmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
              .padding(8.dp)
          ) {
            Text(
              text = if (isKurdish) "👤 ژ لایێ تە: ${CurrencyService.format(paidByMe, displayCurrency)}"
              else "👤 Paid By Me: ${CurrencyService.format(paidByMe, displayCurrency)}",
              fontSize = 11.sp,
              color = EmeraldLight,
              fontWeight = FontWeight.Bold
            )
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(GoldAccent.copy(alpha = 0.16f))
              .border(0.8.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
              .padding(8.dp)
          ) {
            Text(
              text = if (isKurdish) "🤝 ژ لایێ شەریکی: ${CurrencyService.format(paidByPartner, displayCurrency)}"
              else "🤝 Partner: ${CurrencyService.format(paidByPartner, displayCurrency)}",
              fontSize = 11.sp,
              color = GoldAccent,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    val filterOptions = listOf(
      "ALL" to if (isKurdish) "هەمی" else "All",
      "Oil" to if (isKurdish) "🛢️ روون" else "🛢️ Oil",
      "Polishing" to if (isKurdish) "✨ پۆلیش" else "✨ Detailing",
      "Tires" to if (isKurdish) "🛞 تایر" else "🛞 Tires",
      "Engine" to if (isKurdish) "⚙️ مەکینە" else "⚙️ Engine",
      "Paint" to if (isKurdish) "🎨 بۆیاغ" else "🎨 Body",
      "Fuel" to if (isKurdish) "⛽ بەنزین" else "⛽ Fuel"
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x28070B14))
        .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(12.dp))
        .padding(5.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      filterOptions.forEach { (key, label) ->
        val isSelected = filterCategory == key
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isSelected) Color(0xFF38BDF8)
              else Color(0x301E293B)
            )
            .border(
              0.8.dp,
              if (isSelected) Color(0xFFBAE6FD) else Color(0x20FFFFFF),
              RoundedCornerShape(8.dp)
            )
            .clickable { onFilterChange(key) }
            .padding(horizontal = 12.dp, vertical = 7.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
            maxLines = 1
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CAR SPENDING ITEM ROW
// -------------------------------------------------------------
@Composable
fun CarSpendingItemRow(
  service: CarServiceEntity,
  displayCurrency: String,
  isKurdish: Boolean = false,
  onClick: () -> Unit = {},
  onDelete: () -> Unit
) {
  val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(service.dateMillis))
  val paidLabel = when (service.paidBy) {
    "ME" -> if (isKurdish) "👤 ژ لایێ من" else "👤 I Paid"
    "PARTNER" -> "🤝 ${service.partnerName}"
    else -> if (isKurdish) "⚖️ 50/50 هەڤپشک" else "⚖️ Split 50/50"
  }
  CyberRecordButton(
    title = service.serviceType,
    subtitle = "🚗 ${service.carName}${if (service.notes.isNotBlank()) " • ${service.notes}" else ""}",
    dateOrMeta = dateStr,
    primaryValue = CurrencyService.format(service.cost, service.currency),
    accentColor = GoldAccent,
    icon = Icons.Default.Tune,
    statusBadge = paidLabel,
    statusBadgeColor = if (service.paidBy == "ME") EmeraldLight else Color(0xFF38BDF8),
    actionHintLabel = if (isKurdish) "وردەکاری / ژێبرن ▸" else "Details / Delete ▸",
    quickActionLabel = if (isKurdish) "ژێبرن" else "Delete",
    quickActionIcon = Icons.Default.DeleteOutline,
    quickActionColor = LossRed,
    onQuickActionClick = onDelete,
    onClick = onClick,
    testTagKey = "car_spending_btn_${service.id}"
  )
}

// -------------------------------------------------------------
// CAR MONTHLY PAYMENTS HEADER
// -------------------------------------------------------------
@Composable
fun CarMonthlyPaymentsHeader(
  totalMonthlyPaid: Double,
  totalRemainingMoney: Double = 0.0,
  installmentCarsCount: Int = 0,
  displayCurrency: String,
  isKurdish: Boolean = false,
  onQuickAddPayment: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, EmeraldLight.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x24141F36))
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "📅 تۆمارا قستێن هەیڤانە" else "📅 Monthly Installments Log",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
          Text(
            text = "${if (isKurdish) "واصل: " else "Paid: "}${CurrencyService.format(totalMonthlyPaid, displayCurrency)}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = EmeraldLight
          )
        }

        Button(
          onClick = onQuickAddPayment,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isKurdish) "+ قست بنڤیسە" else "+ Add Installment",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.16f)),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, EmeraldLight.copy(alpha = 0.35f))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = if (isKurdish) "✅ واصل (Paid)" else "✅ Total Paid",
              fontSize = 10.sp,
              color = EmeraldLight,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = CurrencyService.format(totalMonthlyPaid, displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = EmeraldLight
            )
          }
        }

        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.16f)),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.35f))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = if (isKurdish) "⏳ باقی (Left)" else "⏳ Remaining",
              fontSize = 10.sp,
              color = GoldAccent,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = CurrencyService.format(totalRemainingMoney, displayCurrency),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GoldAccent
            )
          }
        }

        Card(
          modifier = Modifier.weight(0.85f),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0x350F1728)),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x38FFFFFF))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = if (isKurdish) "🚗 سەیارە" else "🚗 Deals",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFBAE6FD)
            )
            Text(
              text = if (isKurdish) "$installmentCarsCount سەیارە" else "$installmentCarsCount cars",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CAR MONTHLY PAYMENT ITEM ROW
// -------------------------------------------------------------
@Composable
fun CarMonthlyPaymentItemRow(
  payment: CarPaymentEntity,
  displayCurrency: String,
  isKurdish: Boolean = false,
  onClick: () -> Unit = {},
  onDelete: () -> Unit
) {
  val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(payment.paymentDateMillis))
  val paidLabel = when (payment.paidBy) {
    "ME" -> if (isKurdish) "👤 وەرگرتی" else "👤 Received"
    "PARTNER" -> "🤝 ${payment.partnerName}"
    else -> if (isKurdish) "⚖️ 50/50 هەڤپشک" else "⚖️ Split 50/50"
  }
  CyberRecordButton(
    title = payment.paymentMonth.ifBlank { dateStr },
    subtitle = "🚗 ${payment.carName}${if (payment.notes.isNotBlank()) " • ${payment.notes}" else ""}",
    dateOrMeta = dateStr,
    primaryValue = CurrencyService.format(payment.amount, payment.currency),
    accentColor = EmeraldLight,
    icon = Icons.Default.CalendarMonth,
    statusBadge = paidLabel,
    statusBadgeColor = if (payment.paidBy == "ME") EmeraldLight else Color(0xFF38BDF8),
    actionHintLabel = if (isKurdish) "وردەکاری / ژێبرن ▸" else "Details / Delete ▸",
    quickActionLabel = if (isKurdish) "ژێبرن" else "Delete",
    quickActionIcon = Icons.Default.DeleteOutline,
    quickActionColor = LossRed,
    onQuickActionClick = onDelete,
    onClick = onClick,
    testTagKey = "car_payment_btn_${payment.id}"
  )
}

// -------------------------------------------------------------
// EMPTY PLACEHOLDER
// -------------------------------------------------------------
@Composable
fun EmptyCarsPlaceholder(
  message: String = "No cars in your fleet yet.",
  isKurdish: Boolean = false,
  onAddCar: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x24141F36))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(60.dp)
          .clip(CircleShape)
          .background(Color(0xFF38BDF8).copy(alpha = 0.20f))
          .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.DirectionsCar,
          contentDescription = null,
          tint = Color(0xFF38BDF8),
          modifier = Modifier.size(32.dp)
        )
      }

      Text(
        text = message,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = Color.White
      )
      Text(
        text = if (isKurdish) {
          "تۆمارکرنا سەیارێن تایبەت و یێن شەریکی، کڕین و فرۆتنا کاش یان ب قست، دگەل پارێ ماى و مەسروفاتان."
        } else {
          "Track solo and partner-owned vehicles, cash or installment sales, remaining balance, and maintenance."
        },
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFE2E8F0),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )

      Button(
        onClick = onAddCar,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isKurdish) "+ داخلکرنا سەیارێ" else "+ Add Vehicle",
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      }
    }
  }
}

// -------------------------------------------------------------
// ADD CAR DIALOG
// -------------------------------------------------------------
@Composable
fun AddCarDialog(
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (
    name: String,
    isCoOwned: Boolean,
    partnerName: String,
    totalPrice: Double,
    downPayment: Double,
    myDownPayment: Double,
    partnerDownPayment: Double,
    notes: String,
    currency: String,
    isInstallment: Boolean,
    soldPrice: Double,
    downPaymentReceived: Double,
    deductFromCash: Boolean,
    customIqdRate: Double?
  ) -> Unit
) {
  var nameInput by remember { mutableStateOf("") }
  var isCoOwned by remember { mutableStateOf(false) }
  var partnerNameInput by remember { mutableStateOf(if (isKurdish) "شەریک" else "Partner") }
  var totalPriceInput by remember { mutableStateOf("") }
  var downPaymentInput by remember { mutableStateOf("") }
  var myDownPaymentInput by remember { mutableStateOf("") }
  var partnerDownPaymentInput by remember { mutableStateOf("") }
  var showShareValidationError by remember { mutableStateOf(false) }
  var notesInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var iqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  var deductFromCash by remember { mutableStateOf(true) }

  // Installment Sale toggles & inputs
  var isInstallmentSale by remember { mutableStateOf(false) }
  var soldPriceInput by remember { mutableStateOf("") }
  var downPaymentReceivedInput by remember { mutableStateOf("") }

  val commonPresets = listOf(
    "Toyota Land Cruiser",
    "BMW M4 Competition",
    "Mercedes-Benz G63",
    "Hyundai Tucson",
    "Kia Sportage",
    "Audi RS6"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = if (isCoOwned) Icons.Default.Handshake else Icons.Default.DirectionsCar,
          contentDescription = null,
          tint = if (isCoOwned) GoldAccent else Color(0xFF38BDF8)
        )
        Text(
          text = if (isCoOwned) {
            if (isKurdish) "زێدەکرنا سەیارا شەریکی" else "Add Partner Car"
          } else {
            if (isKurdish) "زێدەکرنا سەیارێ بۆ پێشانگەهێ" else "Add Vehicle to Fleet"
          },
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Quick presets
        Text(if (isKurdish) "هەلبژارتنا لەزگین یا سەیارێ:" else "Vehicle Quick Select", style = MaterialTheme.typography.labelSmall)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          commonPresets.take(3).forEach { preset ->
            OutlinedButton(
              onClick = { nameInput = preset },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(preset.substringBefore(" "), fontSize = 10.sp, maxLines = 1)
            }
          }
        }

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text(if (isKurdish) "جۆر و ناڤێ سەیارێ (بۆ نموونە تۆیۆتا کامری)" else "Vehicle Make & Model (e.g. Toyota Camry)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("car_name_input")
        )

        // Ownership Toggle: Solo vs Partner
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(if (isKurdish) "سەیارا شەریکاتی (هەڤپشک)" else "Partnership / Co-Owned Car", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(if (isKurdish) "بەشکرنا پێشەکی و قستێ هەیڤانە" else "Split down payment & monthly profit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = isCoOwned,
              onCheckedChange = { isCoOwned = it },
              colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldAccent.copy(alpha = 0.3f)),
              modifier = Modifier.testTag("car_co_owned_switch")
            )
          }
        }

        if (isCoOwned) {
          OutlinedTextField(
            value = partnerNameInput,
            onValueChange = { partnerNameInput = it },
            label = { Text(if (isKurdish) "ناڤێ شەریکی" else "Partner Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Total Price & Currency
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = totalPriceInput,
            onValueChange = { totalPriceInput = it },
            label = { Text(if (isKurdish) "کۆیێ بهایێ کڕینێ" else "Total Purchase Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("car_total_price_input")
          )

          Row(
            modifier = Modifier.weight(0.9f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf("USD", "IQD", "EUR").forEach { c ->
              val isSelected = currency == c
              Button(
                onClick = { currency = c },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = c,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        val total = totalPriceInput.toDoubleOrNull() ?: 0.0
        val myDown = myDownPaymentInput.toDoubleOrNull() ?: 0.0
        val partnerDown = partnerDownPaymentInput.toDoubleOrNull() ?: 0.0
        val shareValidation = validatePartnershipShares(total, myDown, partnerDown, currency)
        val hasTypedAnyShare = myDownPaymentInput.isNotBlank() || partnerDownPaymentInput.isNotBlank()
        val isShareMismatchVisible = isCoOwned && !shareValidation.isValid && ((total > 0 && hasTypedAnyShare) || showShareValidationError)
        val fmtCleanNum: (Double) -> String = { v ->
          val clean = v.coerceAtLeast(0.0)
          if (clean % 1.0 == 0.0) clean.toLong().toString() else "%.2f".format(clean)
        }

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = total,
          selectedCurrency = if (currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )

        // Down Payment / Initial Paid
        if (!isCoOwned) {
          OutlinedTextField(
            value = downPaymentInput,
            onValueChange = { downPaymentInput = it },
            label = { Text(if (isKurdish) "پێشەکی هاتیەدان (بەتاڵ بهێلە ئەگەر هەمی کاش بیت)" else "Initial Down Payment Paid (Leave empty if full cash)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = myDownPaymentInput,
                onValueChange = {
                  myDownPaymentInput = it
                  showShareValidationError = false
                },
                isError = isShareMismatchVisible,
                label = { Text(if (isKurdish) "پشکا من (پارێ من)" else "My Share") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("car_my_share_input")
              )

              OutlinedTextField(
                value = partnerDownPaymentInput,
                onValueChange = {
                  partnerDownPaymentInput = it
                  showShareValidationError = false
                },
                isError = isShareMismatchVisible,
                label = { Text(if (isKurdish) "پشکا شەریکی (پارێ شەریکی)" else "Partner Share") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("car_partner_share_input")
              )
            }

            if (total > 0.0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    val half = fmtCleanNum(total / 2.0)
                    myDownPaymentInput = half
                    partnerDownPaymentInput = half
                    showShareValidationError = false
                  },
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = if (isKurdish) "⚖️ بەشکرنا ٥٠/٥٠" else "⚖️ Split 50/50",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                  )
                }

                if (!shareValidation.isValid) {
                  OutlinedButton(
                    onClick = {
                      if (myDownPaymentInput.isNotBlank() && myDown <= total) {
                        partnerDownPaymentInput = fmtCleanNum(total - myDown)
                      } else if (partnerDownPaymentInput.isNotBlank() && partnerDown <= total) {
                        myDownPaymentInput = fmtCleanNum(total - partnerDown)
                      } else {
                        val half = fmtCleanNum(total / 2.0)
                        myDownPaymentInput = half
                        partnerDownPaymentInput = half
                      }
                      showShareValidationError = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("car_complete_remaining_share_button")
                  ) {
                    Text(
                      text = if (isKurdish) "⚡ کاملکرنا مەبلەغی" else "⚡ Complete Full Amount",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = EmeraldLight
                    )
                  }
                }
              }
            }

            if (isShareMismatchVisible) {
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.16f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, LossRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                  .testTag("car_share_validation_error_banner")
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = if (isKurdish) "⚠️ خەلەتە، مەبلەغی کامل بکە!" else "⚠️ Wrong Amount! Complete the Full Purchase Price",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LossRed
                  )
                  Text(
                    text = if (isKurdish) shareValidation.errorMessageKu else shareValidation.errorMessageEn,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }

        if (total > 0 && (!isCoOwned || shareValidation.isValid)) {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.15f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isCoOwned) {
                    if (isKurdish) "✅ هەردوو پشک تەمامن (کۆیێ کڕینێ):" else "✅ Both Shares Complete (Total Deal):"
                  } else {
                    if (isKurdish) "کڕینا کاش یا تەواو:" else "100% Cash Purchase:"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = CurrencyService.format(total, currency),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldLight
                )
              }
              if (isCoOwned) {
                Text(
                  text = if (isKurdish) {
                    "بەشداربووی: تە ${CurrencyService.format(myDown, currency)} • شەریک ${CurrencyService.format(partnerDown, currency)}"
                  } else {
                    "Invested: You ${CurrencyService.format(myDown, currency)} • Partner ${CurrencyService.format(partnerDown, currency)}"
                  },
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // Installment Sale Toggle & Section (ب قسطا)
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isInstallmentSale) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(if (isKurdish) "🏷️ فرۆتن ب قست" else "🏷️ فرۆتن ب قست (Sell on Installments)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(if (isKurdish) "ئەگەر ئەڤ سەیارە ب قست هاتبیتە فرۆتن" else "If this car was sold on installment plan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = isInstallmentSale,
                onCheckedChange = { isInstallmentSale = it }
              )
            }

            if (isInstallmentSale) {
              OutlinedTextField(
                value = soldPriceInput,
                onValueChange = { soldPriceInput = it },
                label = { Text("${if (isKurdish) "بهایێ فرۆتنێ" else "Sale Price"} ($currency)*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = downPaymentReceivedInput,
                onValueChange = { downPaymentReceivedInput = it },
                label = { Text(if (isKurdish) "پێشەکیێ واصلبووی (ئارەزوومەندانە)" else "Down Payment Received (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              val saleVal = soldPriceInput.toDoubleOrNull() ?: 0.0
              val downRec = downPaymentReceivedInput.toDoubleOrNull() ?: 0.0
              val remaining = (saleVal - downRec).coerceAtLeast(0.0)

              if (saleVal > 0) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("${if (isKurdish) "واصل بووی: " else "Paid: "}${CurrencyService.format(downRec, currency)}", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                  Text("${if (isKurdish) "باقیێ ماى: " else "Remaining: "}${CurrencyService.format(remaining, currency)}", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی (مۆدێل، رەنگ، ژمارا شاسی، زەمان)" else "Notes (e.g. 2024 Model, VIN, Dealership, Warranty)") },
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
              text = if (isKurdish) "پشکا کڕینا سەیارێ ژ کاشی بهێتە کێمکرن" else "Deduct your car purchase share from Cash Wealth",
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
          val total = totalPriceInput.toDoubleOrNull() ?: 0.0
          if (nameInput.isNotBlank() && total > 0) {
            val down = downPaymentInput.toDoubleOrNull() ?: total
            val myDown = myDownPaymentInput.toDoubleOrNull() ?: 0.0
            val partnerDown = partnerDownPaymentInput.toDoubleOrNull() ?: 0.0
            if (isCoOwned) {
              val validation = validatePartnershipShares(total, myDown, partnerDown, currency)
              if (!validation.isValid) {
                showShareValidationError = true
                return@Button
              }
            }
            val sold = soldPriceInput.toDoubleOrNull() ?: 0.0
            val downReceived = downPaymentReceivedInput.toDoubleOrNull() ?: 0.0
            val customRate = iqdRateInput.toDoubleOrNull() ?: currentIqdRate

            onConfirm(
              nameInput,
              isCoOwned,
              partnerNameInput.ifBlank { "Partner" },
              total,
              down,
              if (isCoOwned) myDown else total,
              if (isCoOwned) partnerDown else 0.0,
              notesInput,
              currency,
              isInstallmentSale,
              if (isInstallmentSale) sold else 0.0,
              if (isInstallmentSale) downReceived else 0.0,
              deductFromCash,
              customRate
            )
          } else if (isCoOwned) {
            showShareValidationError = true
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = if (isCoOwned) GoldAccent else Color(0xFF38BDF8)),
        modifier = Modifier.testTag("confirm_add_car_button")
      ) {
        Text(
          text = if (isKurdish) {
            if (isInstallmentSale) "تۆمارکرنا سەیارا ب قست" else if (isCoOwned) "تۆمارکرنا سەیارا شەریکی" else "تۆمارکرنا سەیارێ"
          } else {
            if (isInstallmentSale) "Save Installment Car" else if (isCoOwned) "Save Partner Car" else "Save Fleet Car"
          },
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

// -------------------------------------------------------------
// ADD MONTHLY PAYMENT DIALOG (WITH DATE)
// -------------------------------------------------------------
@Composable
fun AddCarMonthlyPaymentDialog(
  car: InvestmentEntity,
  uiState: FinanceUiState,
  displayCurrency: String,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (
    amount: Double,
    dateMillis: Long,
    monthLabel: String,
    paidBy: String,
    myShare: Double,
    partnerShare: Double,
    notes: String,
    currency: String,
    customIqdRate: Double?
  ) -> Unit
) {
  val currentMonth = remember { SimpleDateFormat("MMMM yyyy", Locale.US).format(Date()) }
  val currentDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

  val remainingMoney = uiState.getCarRemainingMoney(car)
  val isInstallmentDeal = car.soldPrice > 0

  var amountInput by remember { mutableStateOf("") }
  var monthInput by remember { mutableStateOf(currentMonth) }
  var dateInput by remember { mutableStateOf(currentDateStr) }
  var currency by remember { mutableStateOf(car.currency.ifBlank { displayCurrency }) }
  var iqdRateInput by remember(uiState.iqdRate) { mutableStateOf(uiState.iqdRate.toInt().toString()) }
  // For partner cars or installment cars, payments are made by the Customer!
  var paidBy by remember { mutableStateOf(if (car.isCoOwned || isInstallmentDeal) "CUSTOMER" else "ME") } // "CUSTOMER", "ME", "PARTNER"
  var notesInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Column {
        Text(
          text = if (isKurdish) "تەسجیلکرنا قستێ هەیڤانە" else "Record Monthly Installment",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
        Text(
          text = "🚗 ${car.name} • ${if (isKurdish) "قستێ هەیڤانە" else "Monthly Payment"}",
          fontSize = 12.sp,
          color = EmeraldLight,
          fontWeight = FontWeight.SemiBold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // If installment deal, show remaining balance banner and quick fill button
        if (isInstallmentDeal) {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f))
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (isKurdish) "بهایێ فرۆتنێ: " else "Sale Price: "}${CurrencyService.format(car.soldPrice, car.currency)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "${if (isKurdish) "باقیێ ماى: " else "Remaining: "}${CurrencyService.format(remainingMoney, displayCurrency)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoldAccent
                )
              }

              if (remainingMoney > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                  OutlinedButton(
                    onClick = { amountInput = remainingMoney.toInt().toString() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = if (isKurdish) {
                        "هەمى باقی بنڤیسە (${CurrencyService.format(remainingMoney, displayCurrency)})"
                      } else {
                        "Fill Remaining (${CurrencyService.format(remainingMoney, displayCurrency)})"
                      },
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
        }
        // Amount and Currency
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = {
              Text(
                if (isKurdish) {
                  if (car.isCoOwned) "کوژمێ پارێ مشتەری" else "کوژمێ قستێ هاتیەدان"
                } else {
                  if (car.isCoOwned) "Customer Payment Amount" else "Payment Amount"
                }
              )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("car_payment_amount_input")
          )

          Row(
            modifier = Modifier.weight(0.9f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf("USD", "IQD", "EUR").forEach { c ->
              val isSelected = currency == c
              Button(
                onClick = { currency = c },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = c,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = uiState.iqdRate,
          enteredAmount = amountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = if (currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )

        // Date and Month Name
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text(if (isKurdish) "بەروار (ساڵ-هەیڤ-ڕۆژ)" else "Payment Date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("car_payment_date_input")
          )

          OutlinedTextField(
            value = monthInput,
            onValueChange = { monthInput = it },
            label = { Text(if (isKurdish) "ناڤێ هەیڤێ (بۆ نموونە تشرین ٢٠٢٦)" else "Month (e.g. Oct 2026)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        // If Partner Car: Customer Payment Allocation
        if (car.isCoOwned) {
          Text(if (isKurdish) "پارێ وەرگرتی ژ مشتەری دچیت بۆ کێ:" else "Payment Received From Customer:", style = MaterialTheme.typography.labelMedium)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(
              Triple(
                "CUSTOMER",
                if (isKurdish) "بەشکرن (${car.mySharePercent.toInt()}/${car.partnerSharePercent.toInt()}%)" else "👤 Split (${car.mySharePercent.toInt()}/${car.partnerSharePercent.toInt()}%)",
                EmeraldLight
              ),
              Triple(
                "ME",
                if (isKurdish) "بۆ من" else "👤 Paid to Me",
                Color(0xFF38BDF8)
              ),
              Triple(
                "PARTNER",
                if (isKurdish) "بۆ ${car.partnerName.ifBlank { "شەریکی" }}" else "🤝 To ${car.partnerName.ifBlank { "Partner" }}",
                GoldAccent
              )
            ).forEach { (key, label, accentColor) ->
              val isSelected = paidBy == key
              Button(
                onClick = { paidBy = key },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1
                )
              }
            }
          }
        }

        // Payment preview
        val pAmt = amountInput.toDoubleOrNull() ?: 0.0
        if (pAmt > 0) {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.15f))
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isKurdish) "قستێ واصل بووی:" else "Payment to Record (Paid):",
                  fontSize = 11.sp
                )
                Text(
                  text = CurrencyService.format(pAmt, currency),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldLight
                )
              }
              Text(
                text = if (isKurdish) "وەک پارەدان هاتە تۆمارکرن • دێ یا بەردەوام بیت هەتا دووماهیک دهێت" else "Recorded as Paid • Stays active until finished and marked sold",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی (بۆ نموونە قستێ ٣ یێ مشتەری، وەسڵا حەوالێ)" else "Notes (e.g. Customer installment #3, Transfer receipt)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          if (amt > 0) {
            val dateMillis = try {
              SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateInput)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
              System.currentTimeMillis()
            }
            val myShare = when (paidBy) {
              "ME" -> amt
              "PARTNER" -> 0.0
              else -> amt * (car.mySharePercent / 100.0)
            }
            val partnerShare = when (paidBy) {
              "ME" -> 0.0
              "PARTNER" -> amt
              else -> amt * (car.partnerSharePercent / 100.0)
            }

            val customRate = iqdRateInput.toDoubleOrNull() ?: uiState.iqdRate
            onConfirm(amt, dateMillis, monthInput, paidBy, myShare, partnerShare, notesInput, currency, customRate)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
        modifier = Modifier.testTag("confirm_add_car_payment_button")
      ) {
        Text(
          text = if (isKurdish) "تەسجیلکرنا قستێ هەیڤانە" else "Record Payment",
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

// -------------------------------------------------------------
// ADD CAR SPENDING DIALOG (OIL, DETAILING, TIRES, ETC.)
// -------------------------------------------------------------
@Composable
fun AddCarSpendingDialog(
  car: InvestmentEntity,
  displayCurrency: String,
  currentIqdRate: Double = CurrencyService.DEFAULT_IQD_PER_USD,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (serviceType: String, cost: Double, currency: String, paidBy: String, notes: String, customIqdRate: Double?) -> Unit
) {
  var serviceType by remember { mutableStateOf(if (isKurdish) "گوهۆڕینا ڕۆنی" else "Oil Change") }
  var costInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(car.currency.ifBlank { displayCurrency }) }
  var iqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  var paidBy by remember { mutableStateOf(if (car.isCoOwned) "SPLIT" else "ME") }
  var notesInput by remember { mutableStateOf("") }

  val commonServices = if (isKurdish) listOf(
    "🛢️ گوهۆڕینا ڕۆنی",
    "✨ پۆلش و شووشتن",
    "🛞 تایەر و میزان",
    "🛑 برێک و ئەکسل",
    "⚙️ مەکینە و گێڕ",
    "🎨 بۆیاخ و حدادی",
    "⛽ بەنزین",
    "🔧 تەرمیم و چاکسازی"
  ) else listOf(
    "🛢️ Oil Change",
    "✨ Polishing & Detailing",
    "🛞 Tires & Alignment",
    "🛑 Brakes & Suspension",
    "⚙️ Engine & Transmission",
    "🎨 Bodywork & Paint",
    "⛽ Fuel & Gas",
    "🔧 General Maintenance"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Column {
        Text(if (isKurdish) "زێدەکرنا مەسڕەف و چاکسازیا سەیارێ" else "Add Car Spending & Maintenance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
          text = "🚗 ${car.name} (${if (isKurdish) "مەسڕەفێن ڕۆن، پۆلش، چاکسازی" else "Spending for Oil, Detailing, Repairs"})",
          fontSize = 12.sp,
          color = Color(0xFF38BDF8),
          fontWeight = FontWeight.SemiBold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Preset Service Selector
        Text(if (isKurdish) "هەلبژارتنا جۆرێ مەسڕەفی:" else "Select Spending Type", style = MaterialTheme.typography.labelSmall)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          commonServices.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              rowItems.forEach { svc ->
                val cleanName = svc.substringAfter(" ")
                val isSelected = serviceType.equals(cleanName, ignoreCase = true)
                Button(
                  onClick = { serviceType = cleanName },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.surfaceVariant
                  ),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = svc,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }

        OutlinedTextField(
          value = serviceType,
          onValueChange = { serviceType = it },
          label = { Text(if (isKurdish) "ناڤێ مەسڕەفی / چاکسازیێ" else "Expense / Service Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = costInput,
            onValueChange = { costInput = it },
            label = { Text(if (isKurdish) "کوژمێ مەسڕەفی" else "Cost Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("car_spending_cost_input")
          )

          Row(
            modifier = Modifier.weight(0.9f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf("USD", "IQD", "EUR").forEach { c ->
              val isSelected = currency == c
              Button(
                onClick = { currency = c },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = c,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = costInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = if (currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )

        // Who Paid?
        Text(if (isKurdish) "کێ ئەڤ مەسڕەفە دا؟" else "Who Paid For This Expense?", style = MaterialTheme.typography.labelSmall)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            Triple("SPLIT", if (isKurdish) "⚖️ هەڤپشک ٥٠/٥٠" else "⚖️ Split 50/50", GoldAccent),
            Triple("ME", if (isKurdish) "👤 من دا" else "👤 I Paid", EmeraldLight),
            Triple("PARTNER", if (isKurdish) "🤝 شەریکی دا" else "🤝 Partner Paid", Color(0xFF38BDF8))
          ).forEach { (key, label, accentColor) ->
            val isSelected = paidBy == key
            Button(
              onClick = { paidBy = key },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant
              ),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
            }
          }
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی (بۆ نموونە ڕۆنێ ئەسڵی، مەعرەز، وەرشە)" else "Notes (e.g. 5W-30 synthetic, Polish brand, Workshop)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cost = costInput.toDoubleOrNull() ?: 0.0
          val customRate = iqdRateInput.toDoubleOrNull() ?: currentIqdRate
          if (serviceType.isNotBlank() && cost > 0) {
            onConfirm(serviceType, cost, currency, paidBy, notesInput, customRate)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
        modifier = Modifier.testTag("confirm_car_spending_button")
      ) {
        Text(if (isKurdish) "تۆمارکرنا مەسڕەفی" else "Save Spending", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

// -------------------------------------------------------------
// UPDATE CAR VALUATION DIALOG
// -------------------------------------------------------------
@Composable
fun UpdateCarValuationDialog(
  car: InvestmentEntity,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (newVal: Double) -> Unit
) {
  var valInput by remember { mutableStateOf(car.currentValue.toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (isKurdish) "نویکرنا بهایێ سەیارێ د بازاڕی دا" else "Update Car Market Valuation") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("${if (isKurdish) "سەیارە: " else "Vehicle: "}${car.name}", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
        Text("${if (isKurdish) "بهایێ نوکە: " else "Current Valuation: "}${CurrencyService.format(car.currentValue, car.currency)}")

        OutlinedTextField(
          value = valInput,
          onValueChange = { valInput = it },
          label = { Text("${if (isKurdish) "بهایێ نوێ یێ بازاڕی" else "New Market Valuation"} (${car.currency})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val v = valInput.toDoubleOrNull() ?: car.currentValue
          onConfirm(v)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
      ) {
        Text(if (isKurdish) "تۆمارکرنا بهایی" else "Save Valuation", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

@Composable
fun EditPartnerCarSharesDialog(
  car: InvestmentEntity,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (newTotalPrice: Double, newMyShare: Double, newPartnerShare: Double, newPartnerName: String) -> Unit
) {
  val fmtCleanNum: (Double) -> String = { v ->
    val clean = v.coerceAtLeast(0.0)
    if (clean % 1.0 == 0.0) clean.toLong().toString() else "%.2f".format(clean)
  }

  var totalPriceInput by remember { mutableStateOf(fmtCleanNum(car.initialCapital)) }
  var myShareInput by remember { mutableStateOf(fmtCleanNum(car.myInvestmentAmount)) }
  var partnerShareInput by remember { mutableStateOf(fmtCleanNum(car.partnerInvestmentAmount)) }
  var partnerNameInput by remember { mutableStateOf(car.partnerName) }
  var showError by remember { mutableStateOf(false) }

  val total = totalPriceInput.toDoubleOrNull() ?: 0.0
  val myShare = myShareInput.toDoubleOrNull() ?: 0.0
  val partnerShare = partnerShareInput.toDoubleOrNull() ?: 0.0
  val validation = validatePartnershipShares(total, myShare, partnerShare, car.currency)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isKurdish) "🤝 ڕاستڤەکرنا پشکێن شەریکاتیێ" else "🤝 Edit Partnership Shares",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = car.name,
          fontWeight = FontWeight.ExtraBold,
          color = GoldAccent
        )

        OutlinedTextField(
          value = partnerNameInput,
          onValueChange = { partnerNameInput = it },
          label = { Text(if (isKurdish) "ناڤێ شەریکی" else "Partner Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = totalPriceInput,
          onValueChange = {
            totalPriceInput = it
            showError = false
          },
          label = { Text("${if (isKurdish) "کۆیێ بهایێ کڕینێ" else "Total Purchase Price"} (${car.currency})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = myShareInput,
            onValueChange = {
              myShareInput = it
              showError = false
            },
            isError = !validation.isValid,
            label = { Text("${if (isKurdish) "پشکا من" else "My Share"} (${car.currency})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = partnerShareInput,
            onValueChange = {
              partnerShareInput = it
              showError = false
            },
            isError = !validation.isValid,
            label = { Text("${if (isKurdish) "پشکا شەریکی" else "Partner Share"} (${car.currency})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        if (total > 0.0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            OutlinedButton(
              onClick = {
                val half = fmtCleanNum(total / 2.0)
                myShareInput = half
                partnerShareInput = half
                showError = false
              },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = if (isKurdish) "⚖️ بەشکرنا ٥٠/٥٠" else "⚖️ Split 50/50",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )
            }

            if (!validation.isValid) {
              OutlinedButton(
                onClick = {
                  if (myShareInput.isNotBlank() && myShare <= total) {
                    partnerShareInput = fmtCleanNum(total - myShare)
                  } else if (partnerShareInput.isNotBlank() && partnerShare <= total) {
                    myShareInput = fmtCleanNum(total - partnerShare)
                  } else {
                    val half = fmtCleanNum(total / 2.0)
                    myShareInput = half
                    partnerShareInput = half
                  }
                  showError = false
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = if (isKurdish) "⚡ کاملکرنا مەبلەغی" else "⚡ Complete Full Amount",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldLight
                )
              }
            }
          }
        }

        if (!validation.isValid) {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.16f)),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, LossRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = if (isKurdish) "⚠️ خەلەتە، مەبلەغی کامل بکە!" else "⚠️ Wrong Amount! Complete the Full Price",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LossRed
              )
              Text(
                text = if (isKurdish) validation.errorMessageKu else validation.errorMessageEn,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        } else {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.14f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (isKurdish) {
                "✅ هەردوو پشک تەمامن (${CurrencyService.format(total, car.currency)})"
              } else {
                "✅ Both shares match total purchase price (${CurrencyService.format(total, car.currency)})"
              },
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!validation.isValid) {
            showError = true
            return@Button
          }
          onConfirm(total, myShare, partnerShare, partnerNameInput)
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
      ) {
        Text(
          text = if (isKurdish) "تۆمارکرنا پشکان" else "Save Shares",
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

// -------------------------------------------------------------
// CAR DETAILS DIALOG (FULL TIMELINE)
// -------------------------------------------------------------
@Composable
fun CarDetailsDialog(
  car: InvestmentEntity,
  uiState: FinanceUiState,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onAddPayment: () -> Unit,
  onAddSpending: () -> Unit,
  onEditCar: (() -> Unit)? = null,
  onSellCar: (() -> Unit)? = null,
  onDeletePayment: (Long) -> Unit,
  onDeleteSpending: (Long) -> Unit
) {
  val displayCurrency = uiState.displayCurrency
  val payments = uiState.getCarMonthlyPayments(car.id)
  val services = uiState.getCarServicesForCar(car.id)
  val totalPaid = uiState.getCarTotalPaid(car)
  val totalCost = CurrencyService.convert(car.initialCapital, car.currency, displayCurrency, uiState.iqdRate)
  val totalSpending = uiState.getCarTotalSpending(car.id)
  val totalCashInvested = totalCost + totalSpending
  val isInstallmentDeal = car.soldPrice > 0
  val remainingMoney = uiState.getCarRemainingMoney(car)
  val isInstallmentFinished = isInstallmentDeal && remainingMoney <= 0.001
  val soldVal = if (isInstallmentDeal) CurrencyService.convert(car.soldPrice, car.currency, displayCurrency, uiState.iqdRate) else 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.96f)
      .imePadding(),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(car.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Text(
            text = if (car.isCoOwned) {
              if (isKurdish) "🤝 شەریکاتی دگەل ${car.partnerName.ifBlank { "شەریکی" }}" else "🤝 Co-owned with ${car.partnerName.ifBlank { "Partner" }}"
            } else {
              if (isKurdish) "🚗 سەیارا تایبەت" else "🚗 Solo Vehicle"
            },
            fontSize = 11.sp,
            color = Color(0xFF38BDF8)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isInstallmentDeal) GoldAccent.copy(alpha = 0.2f) else if (car.isSold) EmeraldLight.copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (isInstallmentDeal) {
              if (isInstallmentFinished) (if (isKurdish) "هەمى واصل بوو" else "Fully Paid") else (if (isKurdish) "ب قسطا" else "Installment")
            } else if (car.isSold) {
              if (isKurdish) "هاتە فرۆتن (کاش)" else "SOLD (CASH)"
            } else {
              if (isKurdish) "١٠٠٪ کاش" else "100% CASH"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isInstallmentDeal) GoldAccent else if (car.isSold) EmeraldLight else Color(0xFF38BDF8)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // If installment deal: highlight banner
        if (isInstallmentDeal) {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f))
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isKurdish) "🏷️ قستێ فرۆتنێ" else "🏷️ Installment Deal", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldAccent)
                Text("${if (isKurdish) "بهایێ فرۆتنێ: " else "Sale Price: "}${CurrencyService.format(soldVal, displayCurrency)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (isKurdish) "واصل بووی: " else "Paid: "}${CurrencyService.format(totalPaid, displayCurrency)}", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.SemiBold)
                Text("${if (isKurdish) "باقیێ ماى: " else "Remaining: "}${CurrencyService.format(remainingMoney, displayCurrency)}", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }

        // Quick Stats: Purchase Price, Spending, Total Cash Invested
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(8.dp)
          ) {
            Column {
              Text(if (isKurdish) "کڕین (کاش)" else "Purchase (Cash)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyService.format(totalCost, displayCurrency), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(8.dp)
          ) {
            Column {
              Text(if (isKurdish) "مەسڕەف (کاش)" else "Spending (Cash)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyService.format(totalSpending, displayCurrency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(8.dp)
          ) {
            Column {
              Text(if (isKurdish) "کۆیێ سەرمایێ" else "Total Cash Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyService.format(totalCashInvested, displayCurrency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
            }
          }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onAddPayment,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).heightIn(min = 46.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "+ قستێ هەیڤانە" else "+ Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }

            Button(
              onClick = onAddSpending,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).heightIn(min = 46.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "+ مەسڕەف" else "+ Spending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
          }
          if (onEditCar != null || onSellCar != null) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (onEditCar != null) {
                Button(
                  onClick = onEditCar,
                  colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                ) {
                  Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (isKurdish) "دەستکاری" else "Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
              }
              if (onSellCar != null && !car.isSold) {
                Button(
                  onClick = onSellCar,
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight.copy(alpha = 0.22f)),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                ) {
                  Icon(imageVector = Icons.Default.Sell, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (isKurdish) "فرۆتن" else "Sell", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                }
              }
            }
          }
        }

        Divider()

        // Monthly Payments History (as Buttons)
        Text(if (isKurdish) "📅 قستێن هەیڤانە (${payments.size})" else "📅 Monthly Installments (${payments.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (payments.isEmpty()) {
          Text(if (isKurdish) "هیچ قستێن هەیڤانە نەهاتینە تەسجیلکرن بو ڤێ سەیارێ." else "No monthly installments recorded yet for this vehicle.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            payments.forEach { p ->
              val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(p.paymentDateMillis))
              CyberRecordButton(
                title = p.paymentMonth.ifBlank { dateStr },
                subtitle = p.notes.ifBlank { "${if (isKurdish) "دایە: " else "Paid By: "}${p.paidBy}" },
                dateOrMeta = dateStr,
                primaryValue = CurrencyService.format(p.amount, p.currency),
                accentColor = EmeraldLight,
                icon = Icons.Default.CalendarMonth,
                actionHintLabel = if (isKurdish) "ژێبرن ✕" else "Delete ✕",
                onClick = { onDeletePayment(p.id) }
              )
            }
          }
        }

        Divider()

        // Maintenance & Services History (as Buttons)
        Text(if (isKurdish) "🔧 مێژوویا مەسڕەف و چاکسازیێ (${services.size})" else "🔧 Maintenance & Spending History (${services.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (services.isEmpty()) {
          Text(if (isKurdish) "هیچ مەسڕەفەک نەهاتیە تۆمارکرن (وەک ڕۆن، پۆلش، چاکسازی)." else "No maintenance or spending logged yet (e.g. oil change, polishing).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            services.forEach { s ->
              val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(s.dateMillis))
              CyberRecordButton(
                title = s.serviceType,
                subtitle = s.notes.ifBlank { "${if (isKurdish) "دایە: " else "Paid By: "}${s.paidBy}" },
                dateOrMeta = dateStr,
                primaryValue = CurrencyService.format(s.cost, s.currency),
                accentColor = GoldAccent,
                icon = Icons.Default.Tune,
                actionHintLabel = if (isKurdish) "ژێبرن ✕" else "Delete ✕",
                onClick = { onDeleteSpending(s.id) }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))) {
        Text(if (isKurdish) "گرتن" else "Close", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
      }
    }
  )
}

// -------------------------------------------------------------
// MARK CAR AS SOLD DIALOG (WITH REALTIME WINNING / LOSING P&L AND INSTALLMENTS)
// -------------------------------------------------------------
@Composable
fun MarkCarAsSoldDialog(
  car: InvestmentEntity,
  uiState: FinanceUiState,
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (soldPrice: Double, downPaymentReceived: Double, isInstallment: Boolean, notes: String, customIqdRate: Double?) -> Unit
) {
  val displayCurrency = uiState.displayCurrency
  var iqdRateInput by remember(uiState.iqdRate) { mutableStateOf(uiState.iqdRate.toInt().toString()) }
  val activeIqdRate = iqdRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: uiState.iqdRate

  val initialCapital = CurrencyService.convert(car.initialCapital, car.currency, displayCurrency, activeIqdRate)
  val totalSpending = uiState.getCarTotalSpending(car.id)
  val monthlyPaid = uiState.getCarMonthlyPaidTotal(car.id)
  val totalCost = initialCapital + totalSpending

  var isInstallmentSale by remember { mutableStateOf(false) }
  var soldPriceInput by remember { mutableStateOf(if (car.soldPrice > 0) car.soldPrice.toString() else car.currentValue.toString()) }
  var downPaymentReceivedInput by remember { mutableStateOf("") }
  var notesInput by remember { mutableStateOf(car.notes) }

  val inputSoldPrice = soldPriceInput.toDoubleOrNull() ?: 0.0
  val inputDownPaymentReceived = downPaymentReceivedInput.toDoubleOrNull() ?: 0.0
  val convertedInputSold = CurrencyService.convert(inputSoldPrice, car.currency, displayCurrency, activeIqdRate)
  val projectedRevenue = convertedInputSold + monthlyPaid
  val projectedProfitLoss = projectedRevenue - totalCost
  val isWinning = projectedProfitLoss >= 0
  val roi = if (totalCost > 0) (projectedProfitLoss / totalCost) * 100.0 else 0.0
  val remainingToCollect = (inputSoldPrice - inputDownPaymentReceived).coerceAtLeast(0.0)

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GoldAccent.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Default.Sell, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
        }
        Column {
          Text(if (isKurdish) "فرۆتنا سەیارێ" else "Mark Vehicle As Sold", fontWeight = FontWeight.Bold, fontSize = 17.sp)
          Text(car.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Sale Type Toggle: 100% Cash vs Installments
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!isInstallmentSale) EmeraldLight else Color.Transparent)
              .clickable { isInstallmentSale = false }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isKurdish) "💵 فرۆتنا کاش" else "💵 Full Cash",
              fontSize = 11.sp,
              fontWeight = if (!isInstallmentSale) FontWeight.Bold else FontWeight.Medium,
              color = if (!isInstallmentSale) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isInstallmentSale) GoldAccent else Color.Transparent)
              .clickable { isInstallmentSale = true }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isKurdish) "🏷️ ب قست" else "🏷️ Installments",
              fontSize = 11.sp,
              fontWeight = if (isInstallmentSale) FontWeight.Bold else FontWeight.Medium,
              color = if (isInstallmentSale) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Sold Price Input
        OutlinedTextField(
          value = soldPriceInput,
          onValueChange = { soldPriceInput = it },
          label = { Text("${if (isKurdish) "بهایێ فرۆتنێ" else "Sale Price"} (${car.currency})*") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("car_sold_price_input")
        )

        EntryExchangeRateCard(
          rateInput = iqdRateInput,
          onRateChange = { iqdRateInput = it },
          currentGlobalIqdRate = uiState.iqdRate,
          enteredAmount = inputSoldPrice,
          selectedCurrency = if (car.currency == "IQD") "IQD" else "USD",
          isKurdish = isKurdish
        )

        // If Installment sale: input upfront down payment
        if (isInstallmentSale) {
          OutlinedTextField(
            value = downPaymentReceivedInput,
            onValueChange = { downPaymentReceivedInput = it },
            label = { Text(if (isKurdish) "پێشەکی واصل بووی نوکە" else "Down Payment Received Now") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f))
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (isKurdish) "واصل بووی: " else "Paid: "}${CurrencyService.format(inputDownPaymentReceived, car.currency)}", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                Text("${if (isKurdish) "باقیێ ماى: " else "Remaining: "}${CurrencyService.format(remainingToCollect, car.currency)}", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Real-time Winning / Losing Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isWinning) EmeraldLight.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isWinning) (if (isKurdish) "🏆 مامەلەکا ب مفایە (قازانج)" else "🏆 WINNING DEAL") else (if (isKurdish) "⚠️ مامەلەکا ب زیانە" else "⚠️ LOSING DEAL"),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = if (isWinning) EmeraldLight else LossRed
              )
              Text(
                text = "${if (isWinning) "+" else ""}${CurrencyService.format(projectedProfitLoss, displayCurrency)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = if (isWinning) EmeraldLight else LossRed
              )
            }

            Text(
              text = "${if (isKurdish) "ڕێژا مفایی (ROI): " else "Estimated ROI: "}${if (isWinning) "+" else ""}${"%.1f".format(roi)}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isWinning) EmeraldLight else LossRed
            )

            Divider(color = (if (isWinning) EmeraldLight else LossRed).copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

            // Breakdown
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "کۆیێ سەرمایە و مەسڕەفان:" else "Total Capital & Spending:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyService.format(totalCost, displayCurrency), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(if (isKurdish) "کۆیێ داهاتی (فرۆتن + قست):" else "Total Revenue (Sale + Installments):", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyService.format(projectedRevenue, displayCurrency), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldLight)
            }

            if (car.isCoOwned) {
              val myProfitShare = projectedProfitLoss * (car.mySharePercent / 100.0)
              val partnerProfitShare = projectedProfitLoss * (car.partnerSharePercent / 100.0)
              Divider(color = (if (isWinning) EmeraldLight else LossRed).copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (isKurdish) "پشکا تە" else "Your Share"} (${car.mySharePercent.toInt()}%): ${if (myProfitShare >= 0) "+" else ""}${CurrencyService.format(myProfitShare, displayCurrency)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (myProfitShare >= 0) EmeraldLight else LossRed)
                Text("${car.partnerName} (${car.partnerSharePercent.toInt()}%): ${if (partnerProfitShare >= 0) "+" else ""}${CurrencyService.format(partnerProfitShare, displayCurrency)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (partnerProfitShare >= 0) EmeraldLight else LossRed)
              }
            }
          }
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینیێن فرۆتنێ (ناڤێ کڕیاری، ژمارا تەلەفۆنێ، گرێبەست)" else "Sale Notes (Buyer name, phone, contract details)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldLight.copy(alpha = 0.12f))
            .padding(10.dp)
        ) {
          Text(
            text = if (isKurdish) {
              "💵 پشتی پشتڕاستکرنێ، پارێ فرۆتنا ڤێ سەیارێ ڕاستەوخۆ دێ ل سەروەتێ کاشی زێدە بیت."
            } else {
              "💵 Confirming this sale automatically increases your Cash Wealth."
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldLight
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (inputSoldPrice > 0) {
            onConfirm(inputSoldPrice, inputDownPaymentReceived, isInstallmentSale, notesInput, activeIqdRate)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = if (isWinning) EmeraldLight else LossRed),
        modifier = Modifier.testTag("confirm_car_sold_button")
      ) {
        Text(
          text = if (isKurdish) {
            if (isInstallmentSale) "پشتڕاستکرنا فرۆتنا ب قست" else if (isWinning) "پشتڕاستکرنا فرۆتنێ (ب قازانج)" else "پشتڕاستکرنا فرۆتنێ (ب زیان)"
          } else {
            if (isInstallmentSale) "Confirm Installment Sale" else if (isWinning) "Confirm Sale (Winning)" else "Confirm Sale (Losing)"
          },
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
    }
  )
}

