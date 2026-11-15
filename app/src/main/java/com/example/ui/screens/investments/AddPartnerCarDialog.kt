package com.example.ui.screens.investments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent

/**
 * Professional dialog specifically designed for logging co-owned / partner car purchases.
 * Solves mobile keyboard input issues with a smooth scrollable column, decimal keyboards,
 * and an automatic 50/50 equal-split calculator (e.g. $20,000 buy = $10,000 You / $10,000 Partner).
 */
@Composable
fun AddPartnerCarDialog(
  displayCurrency: String = "USD",
  isKurdish: Boolean = false,
  onDismiss: () -> Unit,
  onConfirm: (
    carName: String,
    totalPrice: Double,
    currentValuation: Double,
    currency: String,
    partnerName: String,
    myPaidAmount: Double,
    partnerPaidAmount: Double,
    notes: String
  ) -> Unit
) {
  var carNameInput by remember { mutableStateOf("") }
  var totalPriceInput by remember { mutableStateOf("") }
  var currentValuationInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var partnerNameInput by remember { mutableStateOf("") }
  var myPaidInput by remember { mutableStateOf("") }
  var partnerPaidInput by remember { mutableStateOf("") }
  var notesInput by remember { mutableStateOf("") }

  // Split mode: "50/50", "60/40", "70/30", "CUSTOM"
  var selectedSplitMode by remember { mutableStateOf("50/50") }

  // Quick vehicle name recommendations
  val popularVehicles = listOf(
    "Toyota Land Cruiser",
    "Mercedes-Benz G63",
    "BMW X5",
    "Range Rover Sport",
    "Nissan Patrol",
    "Hyundai Tucson"
  )

  fun recalculateSplit(totalStr: String, mode: String) {
    val total = totalStr.toDoubleOrNull() ?: 0.0
    if (total <= 0.0) return
    when (mode) {
      "50/50" -> {
        val half = total * 0.50
        myPaidInput = if (half % 1.0 == 0.0) half.toLong().toString() else "%.2f".format(half)
        partnerPaidInput = myPaidInput
      }
      "60/40" -> {
        val myShare = total * 0.60
        val pShare = total * 0.40
        myPaidInput = if (myShare % 1.0 == 0.0) myShare.toLong().toString() else "%.2f".format(myShare)
        partnerPaidInput = if (pShare % 1.0 == 0.0) pShare.toLong().toString() else "%.2f".format(pShare)
      }
      "70/30" -> {
        val myShare = total * 0.70
        val pShare = total * 0.30
        myPaidInput = if (myShare % 1.0 == 0.0) myShare.toLong().toString() else "%.2f".format(myShare)
        partnerPaidInput = if (pShare % 1.0 == 0.0) pShare.toLong().toString() else "%.2f".format(pShare)
      }
    }
  }

  val totalBuyPrice = totalPriceInput.toDoubleOrNull() ?: 0.0
  val myPaidAmount = myPaidInput.toDoubleOrNull() ?: (totalBuyPrice / 2.0)
  val partnerPaidAmount = partnerPaidInput.toDoubleOrNull() ?: (totalBuyPrice - myPaidAmount)

  val myPercent = if (totalBuyPrice > 0.0) (myPaidAmount / totalBuyPrice) * 100.0 else 50.0
  val partnerPercent = if (totalBuyPrice > 0.0) (partnerPaidAmount / totalBuyPrice) * 100.0 else 50.0

  val effectivePartnerName = partnerNameInput.ifBlank { if (isKurdish) "شەریک" else "Partner" }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .imePadding(),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Handshake,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = if (isKurdish) "زێدەکرنا سەیارا شەریکاتیێ" else "Add Partner Co-Owned Car",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isKurdish) "مامەلەیا هەڤپشک (بۆ نموونە کڕین ب $20k = $10k تە / $10k شەریک)" else "Joint vehicle deal (e.g. $20,000 buy = $10k You / $10k Partner)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
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
        // 1. Vehicle Name Field
        Column {
          Text(
            text = if (isKurdish) "جور و مۆدێلا سەیارێ:" else "Vehicle Make & Model:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = carNameInput,
            onValueChange = { carNameInput = it },
            placeholder = { Text(if (isKurdish) "بۆ نموونە تۆیۆتا لاند کرۆزەر ٢٠٢٢" else "e.g. Toyota Land Cruiser 2022") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color(0xFF38BDF8)
              )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("partner_car_name_input")
          )

          // Quick Suggestion Chips
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            popularVehicles.take(3).forEach { suggestion ->
              Box(
                modifier = Modifier
                  .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                  .clickable {
                    carNameInput = suggestion
                    if (notesInput.isBlank()) notesInput = if (isKurdish) "مامەلەیا سەیارێ: $suggestion" else "Vehicle deal: $suggestion"
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = suggestion.take(16),
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // 2. Currency Selector (USD vs IQD)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("USD" to "USD ($)", "IQD" to (if (isKurdish) "دینار (IQD)" else "IQD (ع.د)")).forEach { (curCode, label) ->
            val isSelected = currency == curCode
            Button(
              onClick = { currency = curCode },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) GoldAccent else Color.Transparent,
                contentColor = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(vertical = 6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        // 3. Total Purchase Price
        Column {
          Text(
            text = "${if (isKurdish) "کۆیێ بهایێ کڕینا سەیارێ" else "Total Vehicle Purchase Price"} ($currency):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = totalPriceInput,
            onValueChange = {
              totalPriceInput = it
              if (currentValuationInput.isBlank()) {
                currentValuationInput = it
              }
              recalculateSplit(it, selectedSplitMode)
            },
            placeholder = { Text(if (isKurdish) "بۆ نموونە ٢٠٠٠٠" else "e.g. 20000") },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("partner_car_total_price_input")
          )
        }

        // 4. Partner Name
        Column {
          Text(
            text = if (isKurdish) "ناڤێ شەریکی:" else "Partner's Name:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = partnerNameInput,
            onValueChange = { partnerNameInput = it },
            placeholder = { Text(if (isKurdish) "بۆ نموونە ئەحمەد / شەریک" else "e.g. Ahmed / Partner") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = GoldAccent
              )
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("partner_name_input")
          )
        }

        // 5. Co-Ownership Capital Split (50/50 Equal or Custom)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = GoldAccent.copy(alpha = 0.08f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "💰 دابەشکرنا سەرمایێ کڕینێ" else "💰 Purchase Capital Split",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )

              // Quick split pill presets
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                  "50/50" to "50/50",
                  "60/40" to "60/40",
                  "CUSTOM" to (if (isKurdish) "تایبەت" else "CUSTOM")
                ).forEach { (mode, label) ->
                  val isModeSelected = selectedSplitMode == mode
                  Box(
                    modifier = Modifier
                      .background(
                        if (isModeSelected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(6.dp)
                      )
                      .clickable {
                        selectedSplitMode = mode
                        recalculateSplit(totalPriceInput, mode)
                      }
                      .padding(horizontal = 6.dp, vertical = 3.dp)
                  ) {
                    Text(
                      text = label,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isModeSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two input fields: You Paid vs Partner Paid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${if (isKurdish) "تە دا" else "You Paid"} ($currency):",
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldLight,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = myPaidInput,
                  onValueChange = {
                    myPaidInput = it
                    selectedSplitMode = "CUSTOM"
                    val myVal = it.toDoubleOrNull() ?: 0.0
                    val total = totalPriceInput.toDoubleOrNull() ?: 0.0
                    if (total >= myVal) {
                      val pVal = total - myVal
                      partnerPaidInput = if (pVal % 1.0 == 0.0) pVal.toLong().toString() else "%.2f".format(pVal)
                    }
                  },
                  placeholder = { Text("10000") },
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                  ),
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldLight,
                    unfocusedBorderColor = EmeraldLight.copy(alpha = 0.5f)
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("partner_car_my_paid_input")
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "$effectivePartnerName ${if (isKurdish) "دا" else "Paid"} ($currency):",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF38BDF8),
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = partnerPaidInput,
                  onValueChange = {
                    partnerPaidInput = it
                    selectedSplitMode = "CUSTOM"
                    val pVal = it.toDoubleOrNull() ?: 0.0
                    val total = totalPriceInput.toDoubleOrNull() ?: 0.0
                    if (total >= pVal) {
                      val myVal = total - pVal
                      myPaidInput = if (myVal % 1.0 == 0.0) myVal.toLong().toString() else "%.2f".format(myVal)
                    }
                  },
                  placeholder = { Text("10000") },
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                  ),
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF38BDF8).copy(alpha = 0.5f)
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("partner_car_partner_paid_input")
                )
              }
            }

            // Live Percentage Share Indicator
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${if (isKurdish) "👤 پشکا تە: " else "👤 Your Share: "}${"%.1f".format(myPercent)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldLight
              )
              Text(
                text = "🤝 $effectivePartnerName: ${"%.1f".format(partnerPercent)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
              )
            }
          }
        }

        // 6. Current Estimated Valuation / Target Resale Price
        Column {
          Text(
            text = "${if (isKurdish) "بهایێ نوکە یێ بازاڕی" else "Current Market Valuation"} ($currency):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = currentValuationInput,
            onValueChange = { currentValuationInput = it },
            placeholder = { Text(if (isKurdish) "هەمان بهایێ کڕینێ دهێتە دانان" else "Defaults to purchase price") },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("partner_car_current_valuation_input")
          )
        }

        // 7. Specifications / Notes
        Column {
          Text(
            text = if (isKurdish) "تایبەتمەندی و تێبینی (دڵخواز):" else "Vehicle Specifications & Notes (Optional):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = notesInput,
            onValueChange = { notesInput = it },
            placeholder = { Text(if (isKurdish) "بۆ نموونە مۆدێل ٢٠٢٢، ڕەنگێ سپی، مواسەفات خەلیجی" else "e.g. Model 2022, Clean Title, GCC Spec, White color") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }

        // 8. Live Summary Card
        if (totalBuyPrice > 0.0) {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = if (isKurdish) "📋 پوختەیا مامەلێ" else "📋 Deal Summary Preview",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(if (isKurdish) "کۆیێ کڕینێ:" else "Total Bought:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                Text(
                  CurrencyService.format(totalBuyPrice, currency),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(if (isKurdish) "تە دا:" else "You Paid:", fontSize = 11.sp, color = EmeraldLight)
                Text(
                  "${CurrencyService.format(myPaidAmount, currency)} (${"%.1f".format(myPercent)}%)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldLight
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("$effectivePartnerName ${if (isKurdish) "دا:" else "Paid:"}", fontSize = 11.sp, color = Color(0xFF38BDF8))
                Text(
                  "${CurrencyService.format(partnerPaidAmount, currency)} (${"%.1f".format(partnerPercent)}%)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF38BDF8)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val buyPrice = totalPriceInput.toDoubleOrNull() ?: 0.0
          val curVal = currentValuationInput.toDoubleOrNull() ?: buyPrice
          val name = carNameInput.ifBlank { if (isKurdish) "مامەلەیا سەیارێ" else "Vehicle Deal" }
          val pName = partnerNameInput.ifBlank { if (isKurdish) "شەریک" else "Partner" }
          val myAmt = myPaidInput.toDoubleOrNull() ?: (buyPrice / 2.0)
          val pAmt = partnerPaidInput.toDoubleOrNull() ?: (buyPrice - myAmt)

          if (buyPrice > 0.0) {
            onConfirm(
              name,
              buyPrice,
              curVal,
              currency,
              pName,
              myAmt,
              pAmt,
              notesInput
            )
          }
        },
        enabled = totalPriceInput.toDoubleOrNull() != null && totalPriceInput.toDoubleOrNull()!! > 0.0,
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("save_partner_car_button")
      ) {
        Text(
          text = if (isKurdish) "تۆمارکرنا سەیارا شەریکی" else "Save Partner Car",
          color = Color(0xFF0F172A),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp)
      ) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}
