package com.example.ui.screens.investments

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.data.local.CarServiceEntity
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CarServicesTabContent(
  uiState: FinanceUiState,
  onAddServiceClick: () -> Unit,
  onDeleteService: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var filterType by remember { mutableStateOf("ALL") }

  val filteredServices = remember(uiState.carServices, filterType) {
    when (filterType) {
      "OIL" -> uiState.carServices.filter { it.serviceType.contains("Oil", ignoreCase = true) }
      "POLISH" -> uiState.carServices.filter { it.serviceType.contains("Polish", ignoreCase = true) || it.serviceType.contains("Detail", ignoreCase = true) }
      "TIRES" -> uiState.carServices.filter { it.serviceType.contains("Tire", ignoreCase = true) || it.serviceType.contains("Brake", ignoreCase = true) }
      "ENGINE" -> uiState.carServices.filter { it.serviceType.contains("Engine", ignoreCase = true) || it.serviceType.contains("Transmission", ignoreCase = true) }
      "ME" -> uiState.carServices.filter { it.paidBy == "ME" || it.paidBy == "SPLIT" }
      "PARTNER" -> uiState.carServices.filter { it.paidBy == "PARTNER" || it.paidBy == "SPLIT" }
      else -> uiState.carServices
    }
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Overview Banner: Total Services Spent & Who Paid
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B)
              )
            )
          )
          .padding(20.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Build,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "CAR MAINTENANCE SPENDING",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF38BDF8),
                  letterSpacing = 1.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Oil Changes, Polishing & Repairs",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.7f)
                )
              }
            }

            Text(
              text = CurrencyService.format(uiState.totalCarServicesCost, uiState.displayCurrency),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Two-column spending breakdown: Paid by Me vs Paid by Partner
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Me
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(EmeraldLight, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "I Paid (My Spending)",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = CurrencyService.format(uiState.totalCarServicesPaidByMe, uiState.displayCurrency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldLight
              )
            }

            // Partner
            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.End
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF38BDF8), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Partner Paid",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = CurrencyService.format(uiState.totalCarServicesPaidByPartner, uiState.displayCurrency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Partner Settle-up balance card
          val balance = uiState.partnerMaintenanceBalance
          val balanceCardColor = when {
            balance > 1.0 -> EmeraldLight.copy(alpha = 0.15f)
            balance < -1.0 -> LossRed.copy(alpha = 0.15f)
            else -> Color.White.copy(alpha = 0.08f)
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(balanceCardColor, RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = when {
                balance > 1.0 -> Icons.Default.CheckCircle
                balance < -1.0 -> Icons.Default.Warning
                else -> Icons.Default.Handshake
              },
              contentDescription = null,
              tint = when {
                balance > 1.0 -> EmeraldLight
                balance < -1.0 -> LossRed
                else -> GoldAccent
              },
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = when {
                balance > 1.0 -> "Partner owes you ${CurrencyService.format(balance, uiState.displayCurrency)} for shared services"
                balance < -1.0 -> "You owe partner ${CurrencyService.format(-balance, uiState.displayCurrency)} for shared services"
                else -> "Shared car maintenance expenses are fully balanced"
              },
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = when {
                balance > 1.0 -> EmeraldLight
                balance < -1.0 -> LossRed
                else -> Color.White
              }
            )
          }
        }
      }
    }

    // 2. Action row: "+ Log Car Service" button
    Button(
      onClick = onAddServiceClick,
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF38BDF8),
        contentColor = Color(0xFF0F172A)
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("log_car_service_button")
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = null,
        tint = Color(0xFF0F172A),
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "+ Log Car Service / Expense (Oil, Polish, etc.)",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }

    // 3. Service Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(
        "ALL" to "All (${uiState.carServices.size})",
        "OIL" to "🛢️ Oil Change",
        "POLISH" to "✨ Polishing",
        "TIRES" to "🚗 Tires/Brakes",
        "ENGINE" to "⚙️ Engine",
        "ME" to "👤 Paid by Me",
        "PARTNER" to "🤝 Paid by Partner"
      ).forEach { (key, label) ->
        val isSelected = filterType == key
        Button(
          onClick = { filterType = key },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        }
      }
    }

    // 4. List of Services
    if (filteredServices.isEmpty()) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No car services recorded in this filter",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = onAddServiceClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
          ) {
            Text(
              text = "+ Log First Service",
              color = Color(0xFF0F172A),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    } else {
      filteredServices.forEach { service ->
        CarServiceItemCard(
          service = service,
          displayCurrency = uiState.displayCurrency,
          iqdRate = uiState.iqdRate,
          onDelete = { onDeleteService(service.id) }
        )
      }
    }
  }
}

@Composable
fun CarServiceItemCard(
  service: CarServiceEntity,
  displayCurrency: String,
  iqdRate: Double,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val convertedCost = remember(service.cost, service.currency, displayCurrency, iqdRate) {
    CurrencyService.convert(service.cost, service.currency, displayCurrency, iqdRate)
  }

  val formattedDate = remember(service.dateMillis) {
    SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(service.dateMillis))
  }

  val icon = when {
    service.serviceType.contains("Oil", ignoreCase = true) -> Icons.Default.OilBarrel
    service.serviceType.contains("Polish", ignoreCase = true) || service.serviceType.contains("Detail", ignoreCase = true) -> Icons.Default.AutoAwesome
    service.serviceType.contains("Tire", ignoreCase = true) || service.serviceType.contains("Brake", ignoreCase = true) -> Icons.Default.DirectionsCar
    service.serviceType.contains("Engine", ignoreCase = true) || service.serviceType.contains("Transmission", ignoreCase = true) -> Icons.Default.Build
    else -> Icons.Default.Build
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(Color(0xFF38BDF8).copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = service.serviceType,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${service.carName} • $formattedDate",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = CurrencyService.format(convertedCost, displayCurrency),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete service",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      if (service.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = service.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Payer tag and share breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        when (service.paidBy) {
          "ME" -> {
            Box(
              modifier = Modifier
                .background(EmeraldLight.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = EmeraldLight,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Paid by Me (100%)",
                  color = EmeraldLight,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
          "PARTNER" -> {
            Box(
              modifier = Modifier
                .background(Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Handshake,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Paid by ${service.partnerName.ifBlank { "Partner" }} (100%)",
                  color = Color(0xFF38BDF8),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
          else -> { // SPLIT
            Box(
              modifier = Modifier
                .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Split 50/50 (${CurrencyService.format(convertedCost / 2, displayCurrency)} each)",
                  color = GoldAccent,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Text(
          text = "Original: ${CurrencyService.format(service.cost, service.currency)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
