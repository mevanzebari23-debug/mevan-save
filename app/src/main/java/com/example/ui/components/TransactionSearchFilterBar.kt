package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.categories.AppCategoryRegistry
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Filter helper to check if a transaction date falls into the active date range preset.
 */
fun isTransactionDateInRange(
  dateMillis: Long,
  preset: String,
  customStartMillis: Long?,
  customEndMillis: Long?
): Boolean {
  val now = System.currentTimeMillis()
  return when (preset) {
    "TODAY" -> {
      val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis
      val endOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
      }.timeInMillis
      dateMillis in startOfDay..endOfDay
    }
    "THIS_WEEK" -> {
      val startOfWeek = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis
      dateMillis >= startOfWeek
    }
    "THIS_MONTH" -> {
      val startOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }.timeInMillis
      dateMillis >= startOfMonth
    }
    "LAST_30_DAYS" -> {
      val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
      dateMillis >= thirtyDaysAgo
    }
    "CUSTOM" -> {
      val start = customStartMillis ?: 0L
      val end = customEndMillis ?: Long.MAX_VALUE
      dateMillis in start..end
    }
    else -> true // "ALL"
  }
}

/**
 * Comprehensive Search & Filter Bar for Transaction History.
 * Supports:
 * - Dynamic Search Text (notes, category, amount, currency)
 * - Filtering by Transaction Type (All, Inflow (+), Outflow (-), School)
 * - Filtering by Category
 * - Filtering by Date Range (All time, Today, This Week, This Month, Last 30 Days, Custom Range)
 */
@Composable
fun TransactionSearchFilterBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedType: String, // "ALL", "IN", "OUT", "SCHOOL"
  onSelectType: (String) -> Unit,
  selectedCategory: String?,
  onSelectCategory: (String?) -> Unit,
  categories: List<String>,
  dateRangePreset: String, // "ALL", "TODAY", "THIS_WEEK", "THIS_MONTH", "LAST_30_DAYS", "CUSTOM"
  onSelectDateRangePreset: (String) -> Unit,
  customStartDateMillis: Long?,
  customEndDateMillis: Long?,
  onSetCustomDateRange: (startMillis: Long, endMillis: Long) -> Unit,
  totalMatchedCount: Int,
  totalTransactionsCount: Int,
  isKurdish: Boolean = true,
  onResetAllFilters: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }
  val hasActiveFilters = searchQuery.isNotBlank() ||
    selectedType != "ALL" ||
    selectedCategory != null ||
    dateRangePreset != "ALL"

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.2.dp,
      brush = Brush.horizontalGradient(
        listOf(
          Color(0xFF107C41),
          Color(0xFF0284C7),
          Color(0xFF107C41)
        )
      )
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("transaction_search_filter_bar_container")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Search Bar Input
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
          Text(
            text = if (isKurdish) "لێگەڕیان ب ناڤێ پۆل، تێبینی، بڕێ پارەی، دراڤ..." else "Search by category, notes, amount, currency...",
            fontSize = 12.5.sp,
            color = Color(0xFF94A3B8)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchQueryChange("") },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear Search",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color(0xFF22C55E),
          unfocusedBorderColor = Color(0xFF334155),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          cursorColor = Color(0xFF22C55E)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("transaction_search_input")
      )

      // 2. Filter Section: Transaction Type Selector
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = if (isKurdish) "جۆرێ مامەڵەیێ (Type):" else "Transaction Type:",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF94A3B8)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val typeOptions = listOf(
            "ALL" to (if (isKurdish) "هەمی (All)" else "All"),
            "IN" to (if (isKurdish) "داهات (+)" else "Income (+)"),
            "OUT" to (if (isKurdish) "مەسروف (-)" else "Expense (-)"),
            "SCHOOL" to (if (isKurdish) "🎓 قوتابخانە" else "🎓 School")
          )

          typeOptions.forEach { (key, label) ->
            val isSelected = selectedType == key
            val accentColor = when (key) {
              "IN" -> EmeraldLight
              "OUT" -> LossRed
              "SCHOOL" -> Color(0xFF38BDF8)
              else -> Color(0xFF107C41)
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) accentColor else Color(0xFF1E293B),
              border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isSelected) accentColor else Color(0xFF334155)
              ),
              modifier = Modifier
                .clickable {
                  onSelectType(key)
                  if (key == "IN" || key == "SCHOOL") {
                    onSelectCategory(null)
                  }
                }
                .testTag("filter_type_$key")
            ) {
              Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // 3. Filter Section: Date Range Selector
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = if (isKurdish) "مەودایێ بەرواری (Date Range):" else "Date Range:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF94A3B8)
            )
          }

          if (dateRangePreset == "CUSTOM" && customStartDateMillis != null && customEndDateMillis != null) {
            Text(
              text = "${dateFormat.format(Date(customStartDateMillis))} ➔ ${dateFormat.format(Date(customEndDateMillis))}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val datePresets = listOf(
            "ALL" to (if (isKurdish) "هەمی دەم" else "All Time"),
            "TODAY" to (if (isKurdish) "ئەڤرۆ" else "Today"),
            "THIS_WEEK" to (if (isKurdish) "ئەڤ حەفتە" else "This Week"),
            "THIS_MONTH" to (if (isKurdish) "ئەڤ مەهە" else "This Month"),
            "LAST_30_DAYS" to (if (isKurdish) "٣٠ رۆژێن بۆری" else "Last 30 Days"),
            "CUSTOM" to (if (isKurdish) "دیاریکرن 📅" else "Custom 📅")
          )

          datePresets.forEach { (presetKey, presetLabel) ->
            val isSelected = dateRangePreset == presetKey

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
              border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
              ),
              modifier = Modifier
                .clickable {
                  if (presetKey == "CUSTOM") {
                    // Open Date Picker for Start Date
                    val cal = Calendar.getInstance()
                    if (customStartDateMillis != null) cal.timeInMillis = customStartDateMillis
                    DatePickerDialog(
                      context,
                      { _, startYear, startMonth, startDay ->
                        val startCal = Calendar.getInstance().apply {
                          set(startYear, startMonth, startDay, 0, 0, 0)
                          set(Calendar.MILLISECOND, 0)
                        }
                        // Open Date Picker for End Date immediately after
                        val endCal = Calendar.getInstance()
                        if (customEndDateMillis != null) endCal.timeInMillis = customEndDateMillis
                        DatePickerDialog(
                          context,
                          { _, endYear, endMonth, endDay ->
                            val finalEndCal = Calendar.getInstance().apply {
                              set(endYear, endMonth, endDay, 23, 59, 59)
                              set(Calendar.MILLISECOND, 999)
                            }
                            onSetCustomDateRange(startCal.timeInMillis, finalEndCal.timeInMillis)
                            onSelectDateRangePreset("CUSTOM")
                          },
                          endCal.get(Calendar.YEAR),
                          endCal.get(Calendar.MONTH),
                          endCal.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                          setTitle(if (isKurdish) "بەروارێ دوماهیێ دیار بکە (End Date)" else "Select End Date")
                        }.show()
                      },
                      cal.get(Calendar.YEAR),
                      cal.get(Calendar.MONTH),
                      cal.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                      setTitle(if (isKurdish) "بەروارێ دەستپێکێ دیار بکە (Start Date)" else "Select Start Date")
                    }.show()
                  } else {
                    onSelectDateRangePreset(presetKey)
                  }
                }
                .testTag("filter_date_$presetKey")
            ) {
              Text(
                text = presetLabel,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // 4. Filter Section: Category Selector
      if (selectedType != "SCHOOL" && categories.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = if (isKurdish) "پۆل (Category):" else "Category:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val isAllCats = selectedCategory == null
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isAllCats) Color(0xFFD97706) else Color(0xFF1E293B),
              border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isAllCats) GoldAccent else Color(0xFF334155)
              ),
              modifier = Modifier
                .clickable { onSelectCategory(null) }
                .testTag("filter_category_all")
            ) {
              Text(
                text = if (isKurdish) "هەمی پۆل" else "All Categories",
                fontSize = 11.sp,
                fontWeight = if (isAllCats) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isAllCats) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              )
            }

            categories.forEach { cat ->
              val isSelected = selectedCategory.equals(cat, ignoreCase = true)
              val catColor = AppCategoryRegistry.findCategoryColor(cat)

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) catColor else Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(
                  width = 1.dp,
                  color = if (isSelected) catColor else Color(0xFF334155)
                ),
                modifier = Modifier
                  .clickable {
                    if (isSelected) onSelectCategory(null) else onSelectCategory(cat)
                  }
                  .testTag("filter_cat_$cat")
              ) {
                Text(
                  text = cat,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
              }
            }
          }
        }
      }

      // 5. Active Filters Indicator & Reset All Button
      AnimatedVisibility(visible = hasActiveFilters) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .border(0.8.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.FilterAlt,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (isKurdish) "$totalMatchedCount ئەنجام هاتنە دیتن (ژ سەرجەمێ $totalTransactionsCount)"
                     else "$totalMatchedCount matched (out of $totalTransactionsCount)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFBE123C).copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFF43F5E)),
            modifier = Modifier
              .clickable { onResetAllFilters() }
              .testTag("reset_all_filters_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Filters",
                tint = Color(0xFFFDA4AF),
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = if (isKurdish) "پاقژکرن" else "Reset",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDA4AF)
              )
            }
          }
        }
      }
    }
  }
}
