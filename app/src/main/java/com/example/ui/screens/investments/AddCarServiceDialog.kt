package com.example.ui.screens.investments

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.InvestmentEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent

@Composable
fun AddCarServiceDialog(
  cars: List<InvestmentEntity>,
  preselectedCarId: Long? = null,
  displayCurrency: String = "USD",
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (
    carInvestmentId: Long,
    carName: String,
    serviceType: String,
    cost: Double,
    currency: String,
    paidBy: String,
    partnerName: String,
    notes: String
  ) -> Unit
) {
  val carList = remember(cars) {
    if (cars.isNotEmpty()) cars else listOf(
      InvestmentEntity(
        id = 1L,
        name = "Partner Vehicle",
        category = "Cars & Vehicles",
        initialCapital = 20000.0,
        currentValue = 22500.0,
        isCoOwned = true,
        partnerName = "Partner"
      )
    )
  }

  var selectedCar by remember {
    mutableStateOf(
      carList.find { it.id == preselectedCarId } ?: carList.first()
    )
  }
  var showCarDropdown by remember { mutableStateOf(false) }

  val servicePresets = listOf(
    (if (isKurdish) "گوهۆڕینا ڕۆنی" else "Oil Change") to "🛢️",
    (if (isKurdish) "گوهۆڕینا باتریێ" else "Battery Change") to "🔋",
    (if (isKurdish) "تایەر و برێک" else "Tires & Brakes") to "🛞",
    (if (isKurdish) "پۆڵش و جوانکاری" else "Polishing & Detailing") to "✨",
    (if (isKurdish) "میکانیک و چاکسازی" else "Mechanical / Repairs") to "⚙️",
    (if (isKurdish) "بۆیاخ و تەعدیل" else "Bodywork & Paint") to "🎨",
    (if (isKurdish) "شوشتن و پاکژکرن" else "Car Wash & Detailing") to "🧼",
    (if (isKurdish) "فەحس و هەزە" else "Inspection & Registration") to "📋",
    (if (isKurdish) "مەسڕەفێن دی" else "Other Spending") to "🔧"
  )

  var serviceType by remember { mutableStateOf(servicePresets.first().first) }
  var costInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var paidBy by remember { mutableStateOf("ME") } // "ME", "PARTNER", "SPLIT"
  var partnerNameInput by remember {
    mutableStateOf(if (selectedCar.partnerName.isNotBlank()) selectedCar.partnerName else (if (isKurdish) "شەریک" else "Partner"))
  }
  var notesInput by remember { mutableStateOf("") }

  val costValue = costInput.toDoubleOrNull() ?: 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = if (isKurdish) "زێدەکرنا مەسڕەفێن سەیارێ" else "Add Car Spending",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          Text(
            text = if (isKurdish) "تۆمارکرنا ڕۆن، باتری، چاکسازی و دابەشکرنا مەسڕەفان" else "Track oil change, battery, maintenance & payer split",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // 1. Car Selector
        Text(
          text = if (isKurdish) "سەیارا مەبەست:" else "Target Vehicle:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Box(modifier = Modifier.fillMaxWidth()) {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showCarDropdown = true }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = selectedCar.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  if (selectedCar.isCoOwned) {
                    Text(
                      text = if (isKurdish) "🤝 شەریکاتی دگەل ${selectedCar.partnerName.ifBlank { "شەریک" }}" else "🤝 Co-owned with ${selectedCar.partnerName.ifBlank { "Partner" }}",
                      fontSize = 11.sp,
                      color = GoldAccent
                    )
                  }
                }
              }
              Text(
                text = if (isKurdish) "گوهۆڕین ▼" else "Change ▼",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          DropdownMenu(
            expanded = showCarDropdown,
            onDismissRequest = { showCarDropdown = false }
          ) {
            carList.forEach { car ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(car.name, fontWeight = FontWeight.Bold)
                    if (car.isCoOwned) {
                      Text(if (isKurdish) "شەریکاتی دگەل ${car.partnerName.ifBlank { "شەریک" }}" else "Co-owned with ${car.partnerName.ifBlank { "Partner" }}", fontSize = 11.sp, color = GoldAccent)
                    }
                  }
                },
                onClick = {
                  selectedCar = car
                  if (car.partnerName.isNotBlank()) {
                    partnerNameInput = car.partnerName
                  }
                  showCarDropdown = false
                }
              )
            }
          }
        }

        // 2. Spending Category Presets
        Text(
          text = if (isKurdish) "پۆلێنکرنا مەسڕەفی:" else "Spending Category:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          servicePresets.forEach { (preset, iconEmoji) ->
            val isSelected = serviceType == preset
            Button(
              onClick = { serviceType = preset },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "$iconEmoji $preset",
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }

        // 3. Cost & Currency Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = costInput,
            onValueChange = { costInput = it },
            label = { Text("${if (isKurdish) "بڕێ مەسڕەفکری" else "Amount Spent"} ($currency)") },
            placeholder = { Text(if (isKurdish) "بۆ نموونە ٨٠" else "e.g. 80") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1.5f)
              .testTag("car_service_cost_input")
          )

          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf("USD", "IQD").forEach { cur ->
              val isSel = currency == cur
              Button(
                onClick = { currency = cur },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSel) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant,
                  contentColor = if (isSel) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(text = cur, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // 4. Who Spent / Paid? (The core request)
        Text(
          text = if (isKurdish) "کیژان مەسڕەف کر؟" else "Who Paid / Spent for this?",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF38BDF8)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Option A: I Paid (ME)
          val isMe = paidBy == "ME"
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isMe) EmeraldLight.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { paidBy = "ME" }
          ) {
            Column(
              modifier = Modifier.padding(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isMe) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isKurdish) "👤 من دا" else "👤 I Paid",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMe) EmeraldLight else MaterialTheme.colorScheme.onSurface
              )
              Text(if (isKurdish) "١٠٠٪ تە" else "100% You", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // Option B: Partner Paid
          val isPartner = paidBy == "PARTNER"
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isPartner) Color(0xFF38BDF8).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { paidBy = "PARTNER" }
          ) {
            Column(
              modifier = Modifier.padding(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = if (isPartner) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isKurdish) "🤝 شەریکی دا" else "🤝 Partner",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPartner) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
              )
              Text(if (isKurdish) "١٠٠٪ شەریک" else "100% Partner", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // Option C: Split 50/50
          val isSplit = paidBy == "SPLIT"
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSplit) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { paidBy = "SPLIT" }
          ) {
            Column(
              modifier = Modifier.padding(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (isSplit) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isKurdish) "⚖️ نیڤ ب نیڤ (٥٠/٥٠)" else "⚖️ Split 50/50",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSplit) GoldAccent else MaterialTheme.colorScheme.onSurface
              )
              Text(if (isKurdish) "هەڤبەش" else "Equal Share", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }

        // Live Settlement Calculation Preview
        if (costValue > 0) {
          val halfShare = costValue / 2.0
          val formattedHalf = CurrencyService.format(halfShare, currency)
          val partnerDisplayName = partnerNameInput.ifBlank { if (isKurdish) "شەریک" else "Partner" }
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = when (paidBy) {
                "ME" -> EmeraldLight.copy(alpha = 0.15f)
                "PARTNER" -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                else -> GoldAccent.copy(alpha = 0.15f)
              }
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = when (paidBy) {
                  "ME" -> if (isKurdish) "💡 تە کۆیێ $costValue $currency مەسڕەف کر. شەریک ($partnerDisplayName) پێدڤیە نیڤا وێ بدەتە تە: $formattedHalf"
                          else "💡 You paid total $costValue $currency. Partner ($partnerDisplayName) owes you their half: $formattedHalf"
                  "PARTNER" -> if (isKurdish) "💡 شەریکی کۆیێ $costValue $currency مەسڕەف کر. پێدڤیە تۆ نیڤا وێ بدەیە شەریکی: $formattedHalf"
                               else "💡 Partner paid total $costValue $currency. You owe partner your half: $formattedHalf"
                  else -> if (isKurdish) "⚖️ نیڤ ب نیڤ: تە $formattedHalf دا، شەریکی $formattedHalf دا"
                          else "⚖️ Split evenly: You paid $formattedHalf, Partner paid $formattedHalf"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (paidBy) {
                  "ME" -> EmeraldLight
                  "PARTNER" -> Color(0xFF38BDF8)
                  else -> GoldAccent
                }
              )
            }
          }
        }

        if (paidBy != "ME") {
          OutlinedTextField(
            value = partnerNameInput,
            onValueChange = { partnerNameInput = it },
            label = { Text(if (isKurdish) "ناڤێ شەریکی" else "Partner Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی / هوورکاری (بۆ نموونە ڕۆنێ 5W-30، پاتریێ بوش)" else "Notes / Details (e.g. 5W-30 synthetic, Bosch 70Ah)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cost = costInput.toDoubleOrNull() ?: 0.0
          if (cost > 0) {
            onConfirm(
              selectedCar.id,
              selectedCar.name,
              serviceType,
              cost,
              currency,
              paidBy,
              partnerNameInput.ifBlank { if (isKurdish) "شەریک" else "Partner" },
              notesInput
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
        modifier = Modifier.testTag("confirm_add_car_service_button")
      ) {
        Text(
          text = if (isKurdish) "تۆمارکرنا مەسڕەفی" else "Save Spending",
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
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

