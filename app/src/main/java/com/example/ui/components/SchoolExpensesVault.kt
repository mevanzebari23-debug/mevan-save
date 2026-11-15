package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.currency.CurrencyService
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SchoolBlueAccent = Color(0xFF38BDF8)
private val SchoolIndigoDark = Color(0xFF0B192C)
private val SchoolNavySurface = Color(0xFF0F172A)

/**
 * Dedicated Standalone School Summary Card (جدا ژ مەسروفاتێن گشتی)
 * Shows the combined total spent on School and provides instant buttons to:
 * 1. Open the full School Account Hub (ڤەکرنا حسابا قوتابخانێ) where all school expenses are summed together.
 * 2. Add a new School item (+ زێدەکرن ل قوتابخانێ) which asks whether to deduct from Cash or not.
 */
@Composable
fun SchoolSummarySectionCard(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onOpenSchoolHub: () -> Unit,
  onQuickAddSchoolItem: () -> Unit,
  modifier: Modifier = Modifier
) {
  val totalDisplay = uiState.totalSchoolSpendingDisplay
  val deductedDisplay = uiState.schoolDeductedFromCashDisplay
  val notDeductedDisplay = uiState.schoolNotDeductedFromCashDisplay
  val schoolCount = uiState.schoolTransactions.size

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xD0030B14)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(1.1.dp, SchoolBlueAccent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
      .clickable { onOpenSchoolHub() }
      .testTag("dedicated_school_vault_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.Transparent)
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(SchoolBlueAccent.copy(alpha = 0.18f))
                .border(1.dp, SchoolBlueAccent.copy(alpha = 0.45f), RoundedCornerShape(11.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = "School",
                tint = SchoolBlueAccent,
                modifier = Modifier.size(20.dp)
              )
            }
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = if (isKurdish) "🎓 حسابا تایبەت یا قوتابخانێ" else "🎓 School Account",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = SchoolBlueAccent
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SchoolBlueAccent.copy(alpha = 0.16f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "$schoolCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SchoolBlueAccent
                  )
                }
              }
              Text(
                text = if (isKurdish) {
                  "ژ کاشی: ${CurrencyService.formatShort(deductedDisplay, uiState.displayCurrency)} • دەرڤەی کاشی: ${CurrencyService.formatShort(notDeductedDisplay, uiState.displayCurrency)}"
                } else {
                  "From Cash: ${CurrencyService.formatShort(deductedDisplay, uiState.displayCurrency)} • Separate: ${CurrencyService.formatShort(notDeductedDisplay, uiState.displayCurrency)}"
                },
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = CurrencyService.format(totalDisplay, uiState.displayCurrency),
              fontSize = 17.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              modifier = Modifier.testTag("school_total_spent_value")
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(SchoolBlueAccent.copy(alpha = 0.18f))
                  .clickable { onOpenSchoolHub() }
                  .padding(horizontal = 8.dp, vertical = 3.dp)
                  .testTag("open_school_hub_button")
              ) {
                Text(
                  text = if (isKurdish) "تۆمار ➔" else "Hub ➔",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = SchoolBlueAccent
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(SchoolBlueAccent)
                  .clickable { onQuickAddSchoolItem() }
                  .padding(horizontal = 8.dp, vertical = 3.dp)
                  .testTag("add_school_item_button")
              ) {
                Text(
                  text = if (isKurdish) "+ زێدەکرن" else "+ Add",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF041322)
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Full-Screen Unified School Account Hub Dialog
 * (وەختێ ڤەدکەم خر حساب ببیتە یەک دا بزانم من چەند صەرف کریە ل قوتابخانێ)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchoolAccountHubDialog(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onOpenAddSchoolItem: () -> Unit,
  onToggleCashDeduction: (TransactionEntity) -> Unit,
  onDeleteSchoolItem: (Long) -> Unit,
  onEditSchoolItem: (TransactionEntity) -> Unit = {}
) {
  val schoolTxs = uiState.schoolTransactions
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US) }
  var selectedSubFilter by remember { mutableStateOf<String?>(null) }

  // Group by sub-category so the user sees how much was spent on each part of School + the combined total
  val subCategoryTotals = remember(schoolTxs, uiState.displayCurrency, uiState.iqdRate) {
    schoolTxs.groupBy { tx ->
      extractSchoolSubCategoryLabel(tx.category, isKurdish)
    }.mapValues { (_, list) ->
      list.sumOf {
        CurrencyService.convert(
          it.amount,
          it.currency,
          uiState.displayCurrency,
          uiState.iqdRate,
          entryIqdRate = it.exchangeRateAtEntry
        )
      }
    }
  }

  val filteredSchoolTxs = remember(schoolTxs, selectedSubFilter, isKurdish) {
    if (selectedSubFilter == null) {
      schoolTxs
    } else {
      schoolTxs.filter { extractSchoolSubCategoryLabel(it.category, isKurdish) == selectedSubFilter }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 18.dp)
        .clip(RoundedCornerShape(24.dp))
        .border(1.5.dp, SchoolBlueAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Top Bar
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
                .size(40.dp)
                .clip(CircleShape)
                .background(SchoolBlueAccent.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = SchoolBlueAccent,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                text = if (isKurdish) "🎓 دەفتەرا گشتی یا حسابا قوتابخانێ" else "🎓 Unified School Account Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = SchoolBlueAccent
              )
              Text(
                text = if (isKurdish) "هەمی مەسروفێن قوتابخانێ ل ڤێرێ دبنە یەک حساب" else "All school expenses combined into one total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        // Grand Total Unified Card (خر حساب ببیتە یەک دا بزانم من چەند صەرف کریە ل قوتابخانێ)
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = SchoolIndigoDark),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, SchoolBlueAccent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
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
              Column {
                Text(
                  text = if (isKurdish) "کۆما گشتی یا هەمی مەسروفێن قوتابخانێ" else "GRAND TOTAL SPENT ON SCHOOL",
                  style = MaterialTheme.typography.labelSmall,
                  color = SchoolBlueAccent,
                  fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = CurrencyService.format(uiState.totalSchoolSpendingDisplay, uiState.displayCurrency),
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.White,
                  modifier = Modifier.testTag("school_hub_grand_total_text")
                )
              }

              // Both Currencies Pill
              Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.07f))
                  .padding(horizontal = 10.dp, vertical = 7.dp)
              ) {
                Text(
                  text = "USD: ${CurrencyService.format(uiState.totalSchoolSpendingUsd, "USD")}",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldLight
                )
                Text(
                  text = "IQD: ${CurrencyService.format(uiState.totalSchoolSpendingIqd, "IQD")}",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoldAccent
                )
              }
            }

            // Cash Deduction Split Summary
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val deductedCount = schoolTxs.count { it.affectsCapital }
              val notDeductedCount = schoolTxs.count { !it.affectsCapital }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(LossRed.copy(alpha = 0.14f))
                  .border(0.8.dp, LossRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "✅ ژ کاشی کێم بوویە ($deductedCount)" else "✅ Deducted from Cash ($deductedCount)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = LossRed
                  )
                  Text(
                    text = CurrencyService.format(uiState.schoolDeductedFromCashDisplay, uiState.displayCurrency),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                }
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(EmeraldLight.copy(alpha = 0.14f))
                  .border(0.8.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = if (isKurdish) "🛡️ بێی کێمکرنا کاشی ($notDeductedCount)" else "🛡️ Not Deducted ($notDeductedCount)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                  )
                  Text(
                    text = CurrencyService.format(uiState.schoolNotDeductedFromCashDisplay, uiState.displayCurrency),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                }
              }
            }

            // Big Add Button inside Hub
            Button(
              onClick = onOpenAddSchoolItem,
              colors = ButtonDefaults.buttonColors(
                containerColor = SchoolBlueAccent,
                contentColor = Color(0xFF041322)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp)
                .testTag("school_hub_add_item_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isKurdish) "+ زێدەکرنا تشتەکێ نوی بۆ قوتابخانێ" else "+ Add New School Expense",
                fontWeight = FontWeight.Black,
                fontSize = 13.5.sp
              )
            }
          }
        }

        // Sub-Category Breakdown Chips (if records exist)
        if (subCategoryTotals.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "پوختەیا بەشێن قوتابخانێ:" else "Breakdown by School Item:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
              )
              if (selectedSubFilter != null) {
                Text(
                  text = if (isKurdish) "نیشاندانا هەمیان ✕" else "Show All ✕",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = SchoolBlueAccent,
                  modifier = Modifier.clickable { selectedSubFilter = null }
                )
              }
            }
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              subCategoryTotals.forEach { (subLabel, sumAmt) ->
                val isSelected = selectedSubFilter == subLabel
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                      if (isSelected) SchoolBlueAccent.copy(alpha = 0.24f)
                      else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                      width = if (isSelected) 1.2.dp else 0.7.dp,
                      color = if (isSelected) SchoolBlueAccent else SchoolBlueAccent.copy(alpha = 0.3f),
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                      selectedSubFilter = if (isSelected) null else subLabel
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = "$subLabel: ${CurrencyService.formatShort(sumAmt, uiState.displayCurrency)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) SchoolBlueAccent else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }

        // List of All School Items
        Text(
          text = if (isKurdish) {
            "تۆمارێن قوتابخانێ (${filteredSchoolTxs.size})"
          } else {
            "School Expense Records (${filteredSchoolTxs.size})"
          },
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold
        )

        if (filteredSchoolTxs.isEmpty()) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = null,
                  tint = SchoolBlueAccent.copy(alpha = 0.5f),
                  modifier = Modifier.size(42.dp)
                )
                Text(
                  text = if (isKurdish) {
                    "هێشتا چ مەسروفێن قوتابخانێ نەهاتینە تۆمارکرن.\nکلیک ل دوگمەیا (+ زێدەکرن) بکە بۆ دەستپێکرنێ."
                  } else {
                    "No school expenses recorded yet.\nTap '+ Add New School Expense' above to begin."
                  },
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(filteredSchoolTxs, key = { it.id }) { tx ->
              val converted = CurrencyService.convert(
                tx.amount,
                tx.currency,
                uiState.displayCurrency,
                uiState.iqdRate,
                entryIqdRate = tx.exchangeRateAtEntry
              )
              val subTitle = extractSchoolSubCategoryLabel(tx.category, isKurdish)
              val deductedFromCash = tx.affectsCapital

              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(
                    width = 1.dp,
                    color = if (deductedFromCash) LossRed.copy(alpha = 0.3f) else EmeraldLight.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                  )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
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
                          .size(38.dp)
                          .clip(CircleShape)
                          .background(SchoolBlueAccent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.School,
                          contentDescription = null,
                          tint = SchoolBlueAccent,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                      Column {
                        Text(
                          text = subTitle,
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.ExtraBold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                        if (tx.notes.isNotBlank()) {
                          Text(
                            text = tx.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                        Text(
                          text = dateFormat.format(Date(tx.dateMillis)),
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = CurrencyService.format(converted, uiState.displayCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = SchoolBlueAccent
                      )
                      if (tx.currency != uiState.displayCurrency) {
                        Text(
                          text = "(${CurrencyService.formatShort(tx.amount, tx.currency)})",
                          style = MaterialTheme.typography.labelSmall,
                          color = GoldAccent
                        )
                      }
                    }
                  }

                  // Bottom Row: Cash Deduction Status Badge (tappable to toggle) + Delete Button
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    val statusColor = if (deductedFromCash) LossRed else EmeraldLight
                    Row(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.14f))
                        .border(0.8.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onToggleCashDeduction(tx) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                      Icon(
                        imageVector = if (deductedFromCash) Icons.Default.AccountBalanceWallet else Icons.Default.DoNotDisturbOn,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                      )
                      Text(
                        text = if (deductedFromCash) {
                          if (isKurdish) "✅ ژ کاشی هاتیە کێمکرن (کلیک بۆ گوهۆڕینێ)" else "✅ Deducted from Cash (Tap to toggle)"
                        } else {
                          if (isKurdish) "🛡️ ژ کاشی نەهاتیە کێمکرن (کلیک بۆ گوهۆڕینێ)" else "🛡️ Not Deducted from Cash (Tap to toggle)"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                      )
                    }

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      IconButton(
                        onClick = { onEditSchoolItem(tx) },
                        modifier = Modifier
                          .size(32.dp)
                          .clip(CircleShape)
                          .background(GoldAccent.copy(alpha = 0.14f))
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = "Edit",
                          tint = GoldAccent,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                      IconButton(
                        onClick = { onDeleteSchoolItem(tx.id) },
                        modifier = Modifier
                          .size(32.dp)
                          .clip(CircleShape)
                          .background(LossRed.copy(alpha = 0.12f))
                      ) {
                        Icon(
                          imageVector = Icons.Default.DeleteOutline,
                          contentDescription = "Delete",
                          tint = LossRed,
                          modifier = Modifier.size(17.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Dedicated Dialog for Adding a School Item / Expense
 * Explicitly asks: "ئەرێ ژ کاشی کێم بیت یان نە؟" (Deduct from Cash Balance: Yes or No)
 * and includes Dinar/Dollar Exchange Rate entry (`EntryExchangeRateCard`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSchoolExpenseDialog(
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (
    amount: Double,
    currency: String,
    subCategory: String,
    notes: String,
    deductFromCash: Boolean,
    customIqdRate: Double
  ) -> Unit
) {
  var amountInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(displayCurrency) }
  var entryIqdRateInput by remember(currentIqdRate) { mutableStateOf(currentIqdRate.toInt().toString()) }
  var deductFromCash by remember { mutableStateOf(true) }
  var notesInput by remember { mutableStateOf("") }

  val schoolSubCategories = remember(isKurdish) {
    if (isKurdish) {
      listOf(
        "قستێ قوتابخانێ",
        "پەرتووک و قەرتاسیە",
        "هاتنوچوون (پاس)",
        "جلوبەرگێ قوتابخانێ",
        "خوارن و مصرفێ ڕۆژانە",
        "خول و وانەیێن تایبەت",
        "دیتر"
      )
    } else {
      listOf(
        "Tuition / Installment",
        "Books & Stationery",
        "School Transport",
        "Uniform & Clothing",
        "Daily Allowance & Food",
        "Courses & Tutoring",
        "Other"
      )
    }
  }
  var selectedSubCategory by remember(schoolSubCategories) { mutableStateOf(schoolSubCategories.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.School,
          contentDescription = null,
          tint = SchoolBlueAccent
        )
        Text(
          text = if (isKurdish) "زێدەکرنا مەسروفێ قوتابخانێ (🎓)" else "Add School Expense (🎓)",
          fontWeight = FontWeight.Black,
          fontSize = 17.sp
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
        // 1. PROMINENT QUESTION: Deduct from Cash or Not? (ئەرێ ژ کاشی کێم بیت یان نە؟)
        CashDeductionQuestionCard(
          deductFromCash = deductFromCash,
          onSelectDeductFromCash = { deductFromCash = it },
          isKurdish = isKurdish,
          testTagPrefix = "school_expense"
        )

        // 2. Amount Input
        OutlinedTextField(
          value = amountInput,
          onValueChange = { amountInput = it },
          label = {
            Text("${if (isKurdish) "بڕێ پارەی بۆ قوتابخانێ" else "School Expense Amount"} ($currency)")
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("school_expense_amount_input")
        )

        // Quick Amount Chips
        val quickAmounts = if (currency == "USD") {
          listOf(25.0, 50.0, 100.0, 250.0, 500.0)
        } else {
          listOf(10000.0, 25000.0, 50000.0, 100000.0, 250000.0)
        }
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          quickAmounts.forEach { preset ->
            val label = if (currency == "USD") "+$${preset.toLong()}" else "+${"%,d".format(Locale.US, preset.toLong())}"
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SchoolBlueAccent.copy(alpha = 0.16f))
                .border(0.8.dp, SchoolBlueAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .clickable {
                  val current = amountInput.toDoubleOrNull() ?: 0.0
                  val next = current + preset
                  amountInput = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SchoolBlueAccent
              )
            }
          }
          if (amountInput.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(LossRed.copy(alpha = 0.16f))
                .clickable { amountInput = "" }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = if (isKurdish) "پاقژکرن ✕" else "Clear ✕",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LossRed
              )
            }
          }
        }

        // Currency Selector (USD / IQD)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            val isSelectedCur = currency == cur
            Button(
              onClick = { currency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelectedCur) SchoolBlueAccent else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 42.dp)
            ) {
              Text(
                text = cur,
                fontWeight = FontWeight.Bold,
                color = if (isSelectedCur) Color(0xFF041322) else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // 3. Dinar & Dollar Exchange Rate Card (سعرا دیناری و دۆلاری)
        EntryExchangeRateCard(
          rateInput = entryIqdRateInput,
          onRateChange = { entryIqdRateInput = it },
          currentGlobalIqdRate = currentIqdRate,
          enteredAmount = amountInput.toDoubleOrNull() ?: 0.0,
          selectedCurrency = currency,
          isKurdish = isKurdish,
          testTag = "school_expense_iqd_rate_input"
        )

        // 4. School Sub-Category Selector
        Text(
          text = if (isKurdish) "جۆرێ مەسروفێ قوتابخانێ:" else "School Expense Type:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          schoolSubCategories.forEach { subCat ->
            val isSelected = selectedSubCategory == subCat
            Box(
              modifier = Modifier
                .heightIn(min = 36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (isSelected) SchoolBlueAccent.copy(alpha = 0.22f)
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                  width = if (isSelected) 1.2.dp else 0.6.dp,
                  color = if (isSelected) SchoolBlueAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { selectedSubCategory = subCat }
                .padding(horizontal = 10.dp, vertical = 7.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = subCat,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) SchoolBlueAccent else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // 5. Notes / Item Detail Input
        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = {
            Text(if (isKurdish) "ناڤێ تشتی یان تێبینی (بۆ نموونە: قستێ ١، پەرتووک...)" else "Item Name or Notes (Optional)")
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("school_expense_notes_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
            entryIqdRateInput.toDoubleOrNull() ?: currentIqdRate,
            currentIqdRate
          )
          if (amt > 0.0) {
            onConfirm(amt, currency, selectedSubCategory, notesInput, deductFromCash, lockedRate)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = SchoolBlueAccent,
          contentColor = Color(0xFF041322)
        ),
        modifier = Modifier
          .heightIn(min = 44.dp)
          .testTag("confirm_school_expense_button")
      ) {
        Text(
          text = if (isKurdish) "تۆمارکرن ل قوتابخانێ" else "Save School Expense",
          fontWeight = FontWeight.Black
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.heightIn(min = 44.dp)
      ) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}

/**
 * Reusable Question Card asking:
 * "ئەرێ ژ کاشی کێم بیت یان نە؟" (Should this be deducted from Cash Balance or not?)
 */
@Composable
fun CashDeductionQuestionCard(
  deductFromCash: Boolean,
  onSelectDeductFromCash: (Boolean) -> Unit,
  isKurdish: Boolean,
  testTagPrefix: String = "expense"
) {
  val activeBorderColor = if (deductFromCash) LossRed else EmeraldLight
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
      .border(1.2.dp, activeBorderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = if (isKurdish) {
        "💵 ئەرێ ژ کاشی کێم بیت یان نە؟"
      } else {
        "💵 Deduct from Cash Balance or Not?"
      },
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Black,
      color = MaterialTheme.colorScheme.onSurface
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Option 1: YES — Deduct from Cash (بەلێ، ژ کاشی کێم بیت)
      Box(
        modifier = Modifier
          .weight(1f)
          .heightIn(min = 44.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            if (deductFromCash) LossRed.copy(alpha = 0.22f)
            else MaterialTheme.colorScheme.surface
          )
          .border(
            width = if (deductFromCash) 1.5.dp else 0.8.dp,
            color = if (deductFromCash) LossRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(10.dp)
          )
          .clickable { onSelectDeductFromCash(true) }
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .testTag("${testTagPrefix}_deduct_cash_yes_btn"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (deductFromCash) LossRed else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = if (isKurdish) "بەلێ، ژ کاشی کێم بیت" else "Yes, Deduct Cash",
            fontSize = 11.5.sp,
            fontWeight = if (deductFromCash) FontWeight.Black else FontWeight.SemiBold,
            color = if (deductFromCash) LossRed else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }

      // Option 2: NO — Do NOT deduct from Cash (نەخێر، ژ کاشی کێم نەبیت)
      Box(
        modifier = Modifier
          .weight(1f)
          .heightIn(min = 44.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            if (!deductFromCash) EmeraldLight.copy(alpha = 0.22f)
            else MaterialTheme.colorScheme.surface
          )
          .border(
            width = if (!deductFromCash) 1.5.dp else 0.8.dp,
            color = if (!deductFromCash) EmeraldLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(10.dp)
          )
          .clickable { onSelectDeductFromCash(false) }
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .testTag("${testTagPrefix}_deduct_cash_no_btn"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DoNotDisturbOn,
            contentDescription = null,
            tint = if (!deductFromCash) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = if (isKurdish) "نەخێر، کێم نەبیت" else "No, Keep Cash",
            fontSize = 11.5.sp,
            fontWeight = if (!deductFromCash) FontWeight.Black else FontWeight.SemiBold,
            color = if (!deductFromCash) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }
    }

    Text(
      text = if (deductFromCash) {
        if (isKurdish) {
          "• دێ ل سەر حسابا قوتابخانێ هێتە زێدەکرن و ڕاستەوخۆ دێ ژ کاشێ تە کێم بیت."
        } else {
          "• Added to School total AND deducted directly from your Cash balance."
        }
      } else {
        if (isKurdish) {
          "• دێ تەنێ ل سەر حسابا قوتابخانێ هێتە هەژمارتن و ژ کاشێ تە کێم نابیت."
        } else {
          "• Counted in your School total WITHOUT reducing your Cash balance."
        }
      },
      fontSize = 10.5.sp,
      fontWeight = FontWeight.SemiBold,
      color = activeBorderColor
    )
  }
}

fun extractSchoolSubCategoryLabel(rawCategory: String, isKurdish: Boolean): String {
  val trimmed = rawCategory.trim()
  return when {
    trimmed.contains(":") -> trimmed.substringAfter(":").trim().ifBlank {
      if (isKurdish) "گشتی یا قوتابخانێ" else "General School"
    }
    trimmed.contains("-") -> trimmed.substringAfter("-").trim().ifBlank {
      if (isKurdish) "گشتی یا قوتابخانێ" else "General School"
    }
    trimmed == "قوتابخانە" || trimmed.equals("School & Education", ignoreCase = true) || trimmed.equals("School", ignoreCase = true) -> {
      if (isKurdish) "گشتی یا قوتابخانێ" else "General School"
    }
    else -> trimmed
  }
}
