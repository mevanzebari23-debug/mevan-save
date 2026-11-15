package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.history.ActionOperationType
import com.example.ui.history.ActionSection
import com.example.ui.history.QuickEditableTarget
import com.example.ui.history.ReversibleAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun FloatingUndoAndEditBar(
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val isKurdish = uiState.settings.language == "KU"
  val activeAction = uiState.activeUndoBannerAction

  AnimatedVisibility(
    visible = activeAction != null,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier
  ) {
    if (activeAction != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                Color(0xFF10B981).copy(alpha = 0.7f),
                Color(0xFFF59E0B).copy(alpha = 0.7f),
                Color(0xFF38BDF8).copy(alpha = 0.7f)
              )
            ),
            shape = RoundedCornerShape(18.dp)
          )
          .testTag("floating_undo_edit_bar"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF121824).copy(alpha = 0.97f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF10B981).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(18.dp)
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = activeAction.title(isKurdish),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = activeAction.subtitle(isKurdish),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF94A3B8),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            if (abs(activeAction.cashDeltaDisplay) >= 0.01) {
              val isPos = activeAction.cashDeltaDisplay > 0
              val deltaColor = if (isPos) Color(0xFF10B981) else Color(0xFFEF4444)
              val sign = if (isPos) "+" else "-"
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = deltaColor.copy(alpha = 0.16f)
              ) {
                Text(
                  text = "$sign${CurrencyService.format(abs(activeAction.cashDeltaDisplay), uiState.displayCurrency)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = deltaColor,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            IconButton(
              onClick = { viewModel.dismissUndoBanner() },
              modifier = Modifier
                .size(28.dp)
                .testTag("dismiss_undo_banner_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = if (isKurdish) "داخستن" else "Dismiss",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { viewModel.undoLastAction() },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color(0xFF06130E)
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("undo_last_action_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKurdish) "زڤڕاندن (Undo)" else "Undo Now",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
              )
            }

            if (activeAction.canQuickEdit) {
              OutlinedButton(
                onClick = {
                  viewModel.dismissUndoBanner()
                  viewModel.openQuickEditFromAction(activeAction)
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .testTag("quick_edit_last_action_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isKurdish) "دەستکاری / پێشبینین" else "Edit & Preview",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFDE68A)
                )
              }
            }

            OutlinedButton(
              onClick = {
                viewModel.dismissUndoBanner()
                viewModel.openHistorySheet()
              },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier
                .height(38.dp)
                .testTag("open_history_from_banner_btn")
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${uiState.actionHistory.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHistoryAndPreviewSheet(
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  onDismiss: () -> Unit
) {
  val isKurdish = uiState.settings.language == "KU"
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedSectionFilter by remember { mutableStateOf<ActionSection?>(null) }

  val filteredHistory = remember(uiState.actionHistory, selectedSectionFilter) {
    if (selectedSectionFilter == null) {
      uiState.actionHistory
    } else {
      uiState.actionHistory.filter { it.section == selectedSectionFilter }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF0E131F),
    contentColor = Color.White
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
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
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF59E0B).copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = Color(0xFFF59E0B)
            )
          }
          Column {
            Text(
              text = if (isKurdish) "🕒 مێژوویا گوهۆڕینان و پێشبینینا بلەز" else "🕒 Edit History & Instant Undo",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = if (isKurdish)
                "هەر گوهۆڕینەکێ ب ئێک کلیک بزڤڕینە یان پێشبینین و دەستکاری بکە"
              else
                "Revert any change in 1 tap or preview its impact on Cash & Net Worth",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )
          }
        }
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = if (isKurdish) "داخستن" else "Close",
            tint = Color(0xFF94A3B8)
          )
        }
      }

      // Live Current Balance Strip + Undo/Redo Controls
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF172033))
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isKurdish) "کاشێ صافی یێ نوکە (Pure Cash)" else "Current Pure Cash",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = CurrencyService.format(uiState.pureCashBalance, uiState.displayCurrency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF10B981)
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = if (isKurdish) "صافی سەروەت (Net Worth)" else "Current Net Worth",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = CurrencyService.format(uiState.netWorth, uiState.displayCurrency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF59E0B)
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.undoLastAction() },
              enabled = uiState.actionHistory.isNotEmpty(),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color(0xFF06130E)
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("sheet_undo_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "زڤڕاندن (${uiState.actionHistory.size})" else "Undo (${uiState.actionHistory.size})",
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = { viewModel.redoLastAction() },
              enabled = uiState.redoStack.isNotEmpty(),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("sheet_redo_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "دووبارەکرن (${uiState.redoStack.size})" else "Redo (${uiState.redoStack.size})",
                fontWeight = FontWeight.Bold
              )
            }

            if (uiState.actionHistory.isNotEmpty() || uiState.redoStack.isNotEmpty()) {
              TextButton(
                onClick = { viewModel.clearActionHistory() },
                modifier = Modifier.testTag("sheet_clear_history_btn")
              ) {
                Text(
                  text = if (isKurdish) "پاقژکرن" else "Clear",
                  color = Color(0xFFEF4444)
                )
              }
            }
          }
        }
      }

      // Section Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedSectionFilter == null,
          onClick = { selectedSectionFilter = null },
          label = { Text(if (isKurdish) "هەمی (${uiState.actionHistory.size})" else "All (${uiState.actionHistory.size})", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedSectionFilter == ActionSection.CASH,
          onClick = { selectedSectionFilter = if (selectedSectionFilter == ActionSection.CASH) null else ActionSection.CASH },
          label = { Text(if (isKurdish) "💵 کاش" else "💵 Cash", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedSectionFilter == ActionSection.SCHOOL,
          onClick = { selectedSectionFilter = if (selectedSectionFilter == ActionSection.SCHOOL) null else ActionSection.SCHOOL },
          label = { Text(if (isKurdish) "🎓 قوتابخانە" else "🎓 School", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedSectionFilter == ActionSection.DEBT,
          onClick = { selectedSectionFilter = if (selectedSectionFilter == ActionSection.DEBT) null else ActionSection.DEBT },
          label = { Text(if (isKurdish) "🤝 قەرز" else "🤝 Debts", fontSize = 11.sp) }
        )
      }

      if (filteredHistory.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = Color(0xFF64748B),
              modifier = Modifier.size(42.dp)
            )
            Text(
              text = if (isKurdish) "هیچ گوهۆڕینەکا نوی نەهاتیە تۆمارکرن" else "No recent edits or changes in this session yet",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = if (isKurdish)
                "دەمێ تو هەر تشتەکی زێدە دکەی، دەستکاری دکەی یان ژێدبەی، ل ڤێرە دیار دبیت بۆ زڤڕاندنێ."
              else
                "Any addition, edit, or deletion across Cash, School, Cars, Real Estate, Gold, or Debts will appear here.",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF64748B)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredHistory, key = { it.id }) { item ->
            ReversibleActionHistoryCard(
              action = item,
              isKurdish = isKurdish,
              displayCurrency = uiState.displayCurrency,
              onUndo = { viewModel.undoAction(item.id) },
              onQuickEdit = {
                onDismiss()
                viewModel.openQuickEditFromAction(item)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ReversibleActionHistoryCard(
  action: ReversibleAction,
  isKurdish: Boolean,
  displayCurrency: String,
  onUndo: () -> Unit,
  onQuickEdit: () -> Unit
) {
  val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
  val opColor = when (action.operationType) {
    ActionOperationType.ADD -> Color(0xFF10B981)
    ActionOperationType.EDIT -> Color(0xFFF59E0B)
    ActionOperationType.DELETE -> Color(0xFFEF4444)
    ActionOperationType.STATUS_CHANGE -> Color(0xFF38BDF8)
  }
  val primaryImpact = when {
    kotlin.math.abs(action.cashDeltaDisplay) >= 0.01 -> {
      val sign = if (action.cashDeltaDisplay > 0) "+" else "-"
      "$sign${CurrencyService.formatShort(kotlin.math.abs(action.cashDeltaDisplay), displayCurrency)}"
    }
    kotlin.math.abs(action.netWorthDeltaDisplay) >= 0.01 -> {
      val sign = if (action.netWorthDeltaDisplay > 0) "+" else "-"
      "$sign${CurrencyService.formatShort(kotlin.math.abs(action.netWorthDeltaDisplay), displayCurrency)}"
    }
    else -> timeFormatter.format(Date(action.timestampMillis))
  }
  val secondaryImpact = buildString {
    if (kotlin.math.abs(action.cashDeltaDisplay) >= 0.01 && kotlin.math.abs(action.netWorthDeltaDisplay) >= 0.01) {
      val sign = if (action.netWorthDeltaDisplay > 0) "+" else "-"
      append("${if (isKurdish) "سەروەت" else "NW"}: $sign${CurrencyService.formatShort(kotlin.math.abs(action.netWorthDeltaDisplay), displayCurrency)}")
    }
  }.takeIf { it.isNotBlank() }

  CyberRecordButton(
    title = action.title(isKurdish),
    subtitle = action.subtitle(isKurdish),
    primaryValue = primaryImpact,
    secondaryValue = secondaryImpact,
    dateOrMeta = "${if (isKurdish) action.section.labelKu else action.section.labelEn} • ${timeFormatter.format(Date(action.timestampMillis))}",
    accentColor = opColor,
    icon = Icons.Default.History,
    statusBadge = if (isKurdish) action.operationType.labelKu else action.operationType.labelEn,
    statusBadgeColor = opColor,
    actionHintLabel = if (action.canQuickEdit) (if (isKurdish) "دەستکاری" else "Edit") else (if (isKurdish) "زڤڕاندن" else "Undo"),
    quickActionLabel = if (isKurdish) "زڤڕاندن" else "Undo",
    quickActionIcon = Icons.AutoMirrored.Filled.Undo,
    quickActionColor = Color(0xFF10B981),
    onQuickActionClick = onUndo,
    onClick = { if (action.canQuickEdit) onQuickEdit() else onUndo() },
    testTagKey = "history_action_btn_${action.id}"
  )
}

@Composable
fun LiveBalanceImpactPreviewBanner(
  isKurdish: Boolean,
  displayCurrency: String,
  currentCash: Double,
  simulatedCash: Double,
  currentNetWorth: Double,
  simulatedNetWorth: Double,
  modifier: Modifier = Modifier
) {
  val cashDelta = simulatedCash - currentCash
  val worthDelta = simulatedNetWorth - currentNetWorth

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = Color(0xFFF59E0B).copy(alpha = 0.45f),
        shape = RoundedCornerShape(14.dp)
      ),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF141D2E))
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
        Text(
          text = if (isKurdish) "⚡ پێشبینینا زیندوو یا کاریگەریێ (Live Preview)" else "⚡ Live Balance Impact Preview",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFFF59E0B)
        )
        if (abs(cashDelta) >= 0.01) {
          val isPos = cashDelta > 0
          val cColor = if (isPos) Color(0xFF10B981) else Color(0xFFEF4444)
          val sign = if (isPos) "+" else "-"
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = cColor.copy(alpha = 0.18f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = cColor,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "$sign${CurrencyService.format(abs(cashDelta), displayCurrency)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = cColor
              )
            }
          }
        }
      }

      HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

      // Pure Cash Before -> After
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isKurdish) "💵 کاشێ صافی (Pure Cash):" else "💵 Pure Cash:",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFFCBD5E1)
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = CurrencyService.format(currentCash, displayCurrency),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )
          Text(
            text = "→",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFF59E0B),
            fontWeight = FontWeight.Bold
          )
          Text(
            text = CurrencyService.format(simulatedCash, displayCurrency),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (simulatedCash >= currentCash) Color(0xFF10B981) else Color(0xFFEF4444)
          )
        }
      }

      // Net Worth Before -> After
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isKurdish) "🏛️ صافی سەروەت (Net Worth):" else "🏛️ Net Worth:",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFFCBD5E1)
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = CurrencyService.format(currentNetWorth, displayCurrency),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )
          Text(
            text = "→",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFF59E0B),
            fontWeight = FontWeight.Bold
          )
          Text(
            text = CurrencyService.format(simulatedNetWorth, displayCurrency),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (worthDelta >= 0.0) Color(0xFF38BDF8) else Color(0xFFEF4444)
          )
        }
      }
    }
  }
}

@Composable
fun QuickEditAndLivePreviewModal(
  target: QuickEditableTarget,
  uiState: FinanceUiState,
  viewModel: FinanceViewModel,
  onDismiss: () -> Unit
) {
  val isKurdish = uiState.settings.language == "KU"

  when (target) {
    is QuickEditableTarget.TransactionTarget -> {
      val tx = target.transaction
      var amountText by remember(tx.id) {
        mutableStateOf(if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString())
      }
      var categoryText by remember(tx.id) { mutableStateOf(tx.category) }
      var notesText by remember(tx.id) { mutableStateOf(tx.notes) }
      var currency by remember(tx.id) { mutableStateOf(tx.currency) }
      var affectsCapital by remember(tx.id) { mutableStateOf(tx.affectsCapital) }
      var txType by remember(tx.id) { mutableStateOf(tx.type) }

      val draftTx = remember(tx, amountText, categoryText, notesText, currency, affectsCapital, txType) {
        tx.copy(
          type = txType,
          amount = amountText.replace(",", "").toDoubleOrNull() ?: tx.amount,
          category = categoryText.ifBlank { tx.category },
          notes = notesText,
          currency = currency,
          affectsCapital = affectsCapital
        )
      }
      val (simCash, simWorth) = remember(uiState, draftTx) {
        uiState.simulateWithTarget(QuickEditableTarget.TransactionTarget(draftTx, target.isSchool))
      }

      AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
            text = if (target.isSchool) {
              if (isKurdish) "✏️ دەستکاریا حسابا قوتابخانێ" else "✏️ Quick-Edit School Expense"
            } else {
              if (isKurdish) "✏️ دەستکاریا مامەلا کاشی" else "✏️ Quick-Edit Cash Transaction"
            },
            fontWeight = FontWeight.ExtraBold
          )
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            LiveBalanceImpactPreviewBanner(
              isKurdish = isKurdish,
              displayCurrency = uiState.displayCurrency,
              currentCash = uiState.pureCashBalance,
              simulatedCash = simCash,
              currentNetWorth = uiState.netWorth,
              simulatedNetWorth = simWorth
            )

            if (!target.isSchool) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = txType == "OUT",
                  onClick = { txType = "OUT" },
                  label = { Text(if (isKurdish) "چوونا پارەی (OUT)" else "Expense (OUT)") }
                )
                FilterChip(
                  selected = txType == "IN",
                  onClick = { txType = "IN" },
                  label = { Text(if (isKurdish) "هاتنا پارەی (IN)" else "Income (IN)") }
                )
              }
            }

            OutlinedTextField(
              value = categoryText,
              onValueChange = { categoryText = it },
              label = { Text(if (isKurdish) "بابەت / ناڤونیشان" else "Category / Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = amountText,
              onValueChange = { amountText = it },
              label = { Text(if (isKurdish) "کوژمە ($currency)" else "Amount ($currency)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = currency == "USD",
                onClick = { currency = "USD" },
                label = { Text("USD ($)") }
              )
              FilterChip(
                selected = currency == "IQD",
                onClick = { currency = "IQD" },
                label = { Text("IQD (د.ع)") }
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (affectsCapital) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF64748B).copy(alpha = 0.12f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { affectsCapital = !affectsCapital }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isKurdish) "ژ کاشێ صافی بهێتە کێمکرن؟" else "Deduct from Pure Cash?",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = if (affectsCapital) {
                      if (isKurdish) "بەلێ — ڕاستەوخۆ کاریگەریێ لسەر کاشی دکەت" else "Yes — directly affects Pure Cash balance"
                    } else {
                      if (isKurdish) "نەخێر — تەنێ د حسابا تایبەت دا دمینیت" else "No — tracked separately without reducing Cash"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                  )
                }
                Switch(
                  checked = affectsCapital,
                  onCheckedChange = { affectsCapital = it },
                  colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                )
              }
            }

            OutlinedTextField(
              value = notesText,
              onValueChange = { notesText = it },
              label = { Text(if (isKurdish) "تێبینی" else "Notes") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.updateTransaction(draftTx)
              onDismiss()
            }
          ) {
            Text(if (isKurdish) "پاشەکەفتکرن" else "Save Changes", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(
              onClick = {
                viewModel.deleteTransaction(tx.id)
                onDismiss()
              }
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "ژێبرن" else "Delete", color = Color(0xFFEF4444))
            }
            TextButton(onClick = onDismiss) {
              Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
            }
          }
        }
      )
    }

    is QuickEditableTarget.InvestmentTarget -> {
      val inv = target.investment
      var nameText by remember(inv.id) { mutableStateOf(inv.name) }
      var initialCapText by remember(inv.id) {
        mutableStateOf(if (inv.initialCapital % 1.0 == 0.0) inv.initialCapital.toLong().toString() else inv.initialCapital.toString())
      }
      var currentValText by remember(inv.id) {
        mutableStateOf(if (inv.currentValue % 1.0 == 0.0) inv.currentValue.toLong().toString() else inv.currentValue.toString())
      }
      val initialDebt = inv.independentDebtAmount
      var independentDebtText by remember(inv.id) {
        mutableStateOf(if (initialDebt > 0.0) (if (initialDebt % 1.0 == 0.0) initialDebt.toLong().toString() else initialDebt.toString()) else "")
      }
      var notesText by remember(inv.id) { mutableStateOf(inv.notes) }
      var deductedFromCash by remember(inv.id) { mutableStateOf(inv.deductedFromCash) }

      val draftInv = remember(inv, nameText, initialCapText, currentValText, independentDebtText, notesText, deductedFromCash) {
        val newCap = initialCapText.replace(",", "").toDoubleOrNull() ?: inv.initialCapital
        val newVal = currentValText.replace(",", "").toDoubleOrNull() ?: inv.currentValue
        val parsedDebt = independentDebtText.replace(",", "").toDoubleOrNull() ?: 0.0
        val cleanNotes = notesText.replace(Regex("""\[(?:DEBT|INDEPENDENT_DEBT|قەرز):[0-9]+(?:\.[0-9]+)?\]"""), "").trim()
        val finalNotes = if (parsedDebt > 0.0) {
          if (cleanNotes.isNotBlank()) "$cleanNotes [INDEPENDENT_DEBT:$parsedDebt]" else "[INDEPENDENT_DEBT:$parsedDebt]"
        } else {
          cleanNotes
        }
        inv.copy(
          name = nameText.ifBlank { inv.name },
          initialCapital = newCap,
          currentValue = newVal,
          myInvestmentAmount = if (inv.isCoOwned) inv.myInvestmentAmount else newCap,
          notes = finalNotes,
          deductedFromCash = deductedFromCash
        )
      }
      val (simCash, simWorth) = remember(uiState, draftInv) {
        uiState.simulateWithTarget(QuickEditableTarget.InvestmentTarget(draftInv))
      }

      AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
            text = if (inv.isCar) {
              if (isKurdish) "✏️ دەستکاریا سەیارێ و پێشبینین" else "✏️ Quick-Edit Car & Preview"
            } else if (inv.isGoldValuable) {
              if (isKurdish) "✏️ دەستکاریا زێڕێ حسابا تایبەت" else "✏️ Quick-Edit Private Account Gold"
            } else {
              if (isKurdish) "✏️ دەستکاریا عەقار/پرۆژە و پێشبینین" else "✏️ Quick-Edit Asset & Preview"
            },
            fontWeight = FontWeight.ExtraBold
          )
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            LiveBalanceImpactPreviewBanner(
              isKurdish = isKurdish,
              displayCurrency = uiState.displayCurrency,
              currentCash = uiState.pureCashBalance,
              simulatedCash = simCash,
              currentNetWorth = uiState.netWorth,
              simulatedNetWorth = simWorth
            )

            OutlinedTextField(
              value = nameText,
              onValueChange = { nameText = it },
              label = { Text(if (isKurdish) "ناڤێ سەیارێ / پرۆژەی" else "Name") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = initialCapText,
              onValueChange = { initialCapText = it },
              label = { Text(if (isKurdish) "سەرمایێ کرینێ (${inv.currency})" else "Purchase Capital (${inv.currency})") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = currentValText,
              onValueChange = { currentValText = it },
              label = { Text(if (isKurdish) "بهایێ نوکە / فرۆتن (${inv.currency})" else "Current / Target Value (${inv.currency})") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            if (inv.isGoldValuable) {
              val liveUsd = inv.goldLiveValuationUsd(uiState.effectiveGoldPricePerGram24K)
              val liveConverted = CurrencyService.convert(liveUsd, "USD", inv.currency, uiState.iqdRate)
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldAccent.copy(alpha = 0.15f),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    val formatted = "%.0f".format(liveConverted)
                    currentValText = formatted
                  }
                  .padding(vertical = 4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (isKurdish) "⚡ بهایێ نوکە یێ عالمي:" else "⚡ Live World Valuation:",
                    fontSize = 11.5.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = CurrencyService.format(liveConverted, inv.currency),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }
              }
            }

            OutlinedTextField(
              value = independentDebtText,
              onValueChange = { independentDebtText = it },
              label = { Text(if (isKurdish) "🤝 قەرزێ ماى / سەربەخۆ لسەر سامانی (${inv.currency})" else "🤝 Independent Debt on Asset (${inv.currency})") },
              placeholder = { Text(if (isKurdish) "بۆ نموونە: ٥٠٠٠" else "e.g. 5000") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (deductedFromCash) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF64748B).copy(alpha = 0.12f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { deductedFromCash = !deductedFromCash }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isKurdish) "سەرمایێ کرینێ ژ کاشی بهێتە کێمکرن؟" else "Deduct Purchase Capital from Cash?",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                  )
                }
                Switch(
                  checked = deductedFromCash,
                  onCheckedChange = { deductedFromCash = it }
                )
              }
            }

            OutlinedTextField(
              value = notesText,
              onValueChange = { notesText = it },
              label = { Text(if (isKurdish) "تێبینی" else "Notes") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.updateInvestment(draftInv)
              onDismiss()
            }
          ) {
            Text(if (isKurdish) "پاشەکەفتکرن" else "Save Changes", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(
              onClick = {
                viewModel.deleteInvestment(inv.id)
                onDismiss()
              }
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "ژێبرن" else "Delete", color = Color(0xFFEF4444))
            }
            TextButton(onClick = onDismiss) {
              Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
            }
          }
        }
      )
    }

    is QuickEditableTarget.GoldTarget -> {
      val holding = target.holding
      var titleText by remember(holding.id) { mutableStateOf(holding.title) }
      var weightText by remember(holding.id) { mutableStateOf(holding.weightGrams.toString()) }
      var buyPriceText by remember(holding.id) { mutableStateOf(holding.buyPricePerGram.toString()) }
      var deductedFromCash by remember(holding.id) { mutableStateOf(holding.deductedFromCash) }
      var notesText by remember(holding.id) { mutableStateOf(holding.notes) }

      val draftHolding = remember(holding, titleText, weightText, buyPriceText, deductedFromCash, notesText) {
        holding.copy(
          title = titleText.ifBlank { holding.title },
          weightGrams = weightText.replace(",", "").toDoubleOrNull() ?: holding.weightGrams,
          buyPricePerGram = buyPriceText.replace(",", "").toDoubleOrNull() ?: holding.buyPricePerGram,
          deductedFromCash = deductedFromCash,
          notes = notesText
        )
      }
      val (simCash, simWorth) = remember(uiState, draftHolding) {
        uiState.simulateWithTarget(QuickEditableTarget.GoldTarget(draftHolding))
      }

      AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
            text = if (isKurdish) "✏️ دەستکاریا زێڕ / زیڤ و پێشبینین" else "✏️ Quick-Edit Gold / Silver",
            fontWeight = FontWeight.ExtraBold
          )
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            LiveBalanceImpactPreviewBanner(
              isKurdish = isKurdish,
              displayCurrency = uiState.displayCurrency,
              currentCash = uiState.pureCashBalance,
              simulatedCash = simCash,
              currentNetWorth = uiState.netWorth,
              simulatedNetWorth = simWorth
            )

            OutlinedTextField(
              value = titleText,
              onValueChange = { titleText = it },
              label = { Text(if (isKurdish) "ناڤونیشان" else "Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = weightText,
              onValueChange = { weightText = it },
              label = { Text(if (isKurdish) "کێش ب گرام (Grams)" else "Weight in Grams") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = buyPriceText,
              onValueChange = { buyPriceText = it },
              label = { Text(if (isKurdish) "نرخێ کرینێ بۆ هەر گرامەکێ ($)" else "Buy Price per Gram ($)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (deductedFromCash) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF64748B).copy(alpha = 0.12f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { deductedFromCash = !deductedFromCash }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isKurdish) "ژ کاشێ صافی بهێتە کێمکرن؟" else "Deduct Cost from Pure Cash?",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.weight(1f)
                )
                Switch(
                  checked = deductedFromCash,
                  onCheckedChange = { deductedFromCash = it }
                )
              }
            }

            OutlinedTextField(
              value = notesText,
              onValueChange = { notesText = it },
              label = { Text(if (isKurdish) "تێبینی" else "Notes") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.updateGoldHolding(draftHolding)
              onDismiss()
            }
          ) {
            Text(if (isKurdish) "پاشەکەفتکرن" else "Save Changes", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(
              onClick = {
                viewModel.deleteGoldHolding(holding.id)
                onDismiss()
              }
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "ژێبرن" else "Delete", color = Color(0xFFEF4444))
            }
            TextButton(onClick = onDismiss) {
              Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
            }
          }
        }
      )
    }

    is QuickEditableTarget.DebtTarget -> {
      val debt = target.debtCredit
      var personName by remember(debt.id) { mutableStateOf(debt.personName) }
      var totalText by remember(debt.id) {
        mutableStateOf(if (debt.totalAmount % 1.0 == 0.0) debt.totalAmount.toLong().toString() else debt.totalAmount.toString())
      }
      var paidText by remember(debt.id) {
        mutableStateOf(if (debt.amountPaid % 1.0 == 0.0) debt.amountPaid.toLong().toString() else debt.amountPaid.toString())
      }
      var affectCash by remember(debt.id) { mutableStateOf(debt.affectCashBalance) }
      var notesText by remember(debt.id) { mutableStateOf(debt.notes) }

      val draftDebt = remember(debt, personName, totalText, paidText, affectCash, notesText) {
        val nextTotal = totalText.replace(",", "").toDoubleOrNull() ?: debt.totalAmount
        val nextPaid = paidText.replace(",", "").toDoubleOrNull() ?: debt.amountPaid
        debt.copy(
          personName = personName.ifBlank { debt.personName },
          totalAmount = nextTotal,
          amountPaid = nextPaid,
          affectCashBalance = affectCash,
          cashImpactAmount = if (affectCash) nextTotal else 0.0,
          cashPaidImpactAmount = if (affectCash) nextPaid else 0.0,
          isCompleted = nextPaid >= nextTotal && nextTotal > 0.0,
          notes = notesText
        )
      }
      val (simCash, simWorth) = remember(uiState, draftDebt) {
        uiState.simulateWithTarget(QuickEditableTarget.DebtTarget(draftDebt))
      }

      AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
            text = if (isKurdish) "✏️ دەستکاریا قەرز / دەین و پێشبینین" else "✏️ Quick-Edit Debt / Credit",
            fontWeight = FontWeight.ExtraBold
          )
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            LiveBalanceImpactPreviewBanner(
              isKurdish = isKurdish,
              displayCurrency = uiState.displayCurrency,
              currentCash = uiState.pureCashBalance,
              simulatedCash = simCash,
              currentNetWorth = uiState.netWorth,
              simulatedNetWorth = simWorth
            )

            OutlinedTextField(
              value = personName,
              onValueChange = { personName = it },
              label = { Text(if (isKurdish) "ناڤێ کەسی / لایەنی" else "Person / Counterparty") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = totalText,
              onValueChange = { totalText = it },
              label = { Text(if (isKurdish) "کۆژمێ گشتی (${debt.currency})" else "Total Principal (${debt.currency})") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = paidText,
              onValueChange = { paidText = it },
              label = { Text(if (isKurdish) "کوژمێ واصلکری (${debt.currency})" else "Amount Paid / Collected (${debt.currency})") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (affectCash) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF64748B).copy(alpha = 0.12f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { affectCash = !affectCash }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isKurdish) "کاریگەری لسەر کاشێ صافی هەبیت؟" else "Affect Pure Cash Balance?",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.weight(1f)
                )
                Switch(
                  checked = affectCash,
                  onCheckedChange = { affectCash = it }
                )
              }
            }

            OutlinedTextField(
              value = notesText,
              onValueChange = { notesText = it },
              label = { Text(if (isKurdish) "تێبینی" else "Notes") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.updateDebtCredit(
                id = debt.id,
                type = debt.type,
                personName = personName.ifBlank { debt.personName },
                contactInfo = debt.contactInfo,
                totalAmount = totalText.replace(",", "").toDoubleOrNull() ?: debt.totalAmount,
                amountPaid = paidText.replace(",", "").toDoubleOrNull() ?: debt.amountPaid,
                currency = debt.currency,
                dueDateMillis = debt.dueDateMillis,
                notes = notesText,
                reminderEnabled = debt.reminderEnabled,
                affectCashBalance = affectCash
              )
              onDismiss()
            }
          ) {
            Text(if (isKurdish) "پاشەکەفتکرن" else "Save Changes", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(
              onClick = {
                viewModel.deleteDebtCredit(debt.id)
                onDismiss()
              }
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "ژێبرن" else "Delete", color = Color(0xFFEF4444))
            }
            TextButton(onClick = onDismiss) {
              Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
            }
          }
        }
      )
    }

    is QuickEditableTarget.BudgetTarget -> {
      val budget = target.budget
      var categoryText by remember(budget.id) { mutableStateOf(budget.category) }
      var limitText by remember(budget.id) {
        mutableStateOf(if (budget.monthlyLimit % 1.0 == 0.0) budget.monthlyLimit.toLong().toString() else budget.monthlyLimit.toString())
      }

      AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
            text = if (isKurdish) "✏️ دەستکاریا بودجەیا هەیڤانە" else "✏️ Quick-Edit Monthly Budget",
            fontWeight = FontWeight.ExtraBold
          )
        },
        text = {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = categoryText,
              onValueChange = { categoryText = it },
              label = { Text(if (isKurdish) "بابەت" else "Category") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
              value = limitText,
              onValueChange = { limitText = it },
              label = { Text(if (isKurdish) "سنوورێ هەیڤانە (${budget.currency})" else "Monthly Limit (${budget.currency})") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val nextLimit = limitText.replace(",", "").toDoubleOrNull() ?: budget.monthlyLimit
              viewModel.updateBudget(
                budget.copy(
                  category = categoryText.ifBlank { budget.category },
                  monthlyLimit = nextLimit
                )
              )
              onDismiss()
            }
          ) {
            Text(if (isKurdish) "پاشەکەفتکرن" else "Save", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = onDismiss) {
            Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
          }
        }
      )
    }
  }
}
