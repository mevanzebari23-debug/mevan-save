package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.data.local.DebtCreditType
import com.example.data.local.TransactionEntity
import com.example.util.DebtBalanceResult
import com.example.util.LedgerFormattingHelper
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Polymorphic contract representing any financial item that can be rendered inside [LedgerTile].
 * Can represent cashflow transactions, loans/debts, or custom ledger adjustments.
 */
sealed interface LedgerItem {
  val id: String
  val title: String
  val subtitle: String
  val amount: Double
  val currency: String
  val dateMillis: Long
  val isPositive: Boolean
  val statusLabel: String
  val accentColor: Color
  val icon: ImageVector
  val remainingBalance: Double?
  val progressRatio: Float?
  val isOverdue: Boolean
  val isCompleted: Boolean

  /**
   * Cashflow Transaction implementation
   */
  data class TransactionRecord(
    val entity: TransactionEntity,
    val isKurdish: Boolean = true
  ) : LedgerItem {
    override val id: String get() = "tx_${entity.id}"
    override val title: String get() = entity.category.ifBlank { if (isKurdish) "مامەڵە" else "Transaction" }
    override val subtitle: String get() = entity.notes.ifBlank {
      val df = SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.US)
      df.format(Date(entity.dateMillis))
    }
    override val amount: Double get() = entity.amount
    override val currency: String get() = entity.currency
    override val dateMillis: Long get() = entity.dateMillis
    override val isPositive: Boolean get() = entity.type.equals("IN", ignoreCase = true)
    override val statusLabel: String
      get() = if (isPositive) {
        if (isKurdish) "داهات (Inflow)" else "Cash Inflow"
      } else {
        if (isKurdish) "مەسروفات (Outflow)" else "Cash Outflow"
      }
    override val accentColor: Color get() = if (isPositive) EmeraldLight else LossRed
    override val icon: ImageVector
      get() = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    override val remainingBalance: Double? get() = null
    override val progressRatio: Float? get() = null
    override val isOverdue: Boolean get() = false
    override val isCompleted: Boolean get() = true
  }

  /**
   * Debt or Credit/Receivable record implementation
   */
  data class DebtRecord(
    val entity: DebtCreditEntity,
    val isKurdish: Boolean = true,
    val displayCurrency: String = "USD",
    val currentIqdRate: Double = 152000.0
  ) : LedgerItem {
    private val balanceResult: DebtBalanceResult by lazy {
      LedgerFormattingHelper.calculateDebtRemainingBalance(
        debt = entity,
        targetDisplayCurrency = displayCurrency,
        currentIqdRate = currentIqdRate,
        isKurdish = isKurdish
      )
    }

    override val id: String get() = "debt_${entity.id}"
    override val title: String get() = entity.personName.ifBlank { if (isKurdish) "قەرزدار" else "Counterparty" }
    override val subtitle: String get() = entity.notes.ifBlank {
      val df = SimpleDateFormat("yyyy/MM/dd", Locale.US)
      if (entity.dueDateMillis > 0L) {
        val dateStr = df.format(Date(entity.dueDateMillis))
        if (isKurdish) "بەروارێ دانێ: $dateStr" else "Due: $dateStr"
      } else {
        df.format(Date(entity.createdAtMillis))
      }
    }
    override val amount: Double get() = balanceResult.originalTotal
    override val currency: String get() = entity.currency
    override val dateMillis: Long get() = entity.createdAtMillis
    override val isPositive: Boolean get() = entity.isCreditReceivable
    override val statusLabel: String
      get() = when {
        balanceResult.isCompleted -> if (isKurdish) "تمامکری (Settled)" else "Settled"
        balanceResult.isOverdue -> if (isKurdish) "دیرکەفتی (Overdue)" else "Overdue"
        entity.isCreditReceivable -> if (isKurdish) "دەف خەلکی (Receivable)" else "Receivable"
        else -> if (isKurdish) "لسەر من (Payable)" else "Payable"
      }
    override val accentColor: Color
      get() = when {
        balanceResult.isCompleted -> GoldAccent
        balanceResult.isOverdue -> LossRed
        entity.isCreditReceivable -> EmeraldLight
        else -> Color(0xFFF97316) // Vibrant Orange for personal debt
      }
    override val icon: ImageVector
      get() = when {
        balanceResult.isCompleted -> Icons.Default.CheckCircle
        balanceResult.isOverdue -> Icons.Default.Warning
        entity.isCreditReceivable -> Icons.Default.Handshake
        else -> Icons.Default.Payment
      }
    override val remainingBalance: Double get() = balanceResult.remainingNominal
    override val progressRatio: Float get() = balanceResult.repaymentRatio
    override val isOverdue: Boolean get() = balanceResult.isOverdue
    override val isCompleted: Boolean get() = balanceResult.isCompleted
  }

  /**
   * Generic Ledger record for adjustments, transfers, or custom items
   */
  data class GenericRecord(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val amount: Double,
    override val currency: String,
    override val dateMillis: Long = System.currentTimeMillis(),
    override val isPositive: Boolean = true,
    override val statusLabel: String = "ACTIVE",
    override val accentColor: Color = Color(0xFF38BDF8),
    override val icon: ImageVector = Icons.AutoMirrored.Filled.ReceiptLong,
    override val remainingBalance: Double? = null,
    override val progressRatio: Float? = null,
    override val isOverdue: Boolean = false,
    override val isCompleted: Boolean = false
  ) : LedgerItem
}

/**
 * Executive Reusable Ledger Tile (مربەعا پێشکەفتی یا تۆمارا دارایی)
 *
 * Implements a modern obsidian glass surface with neon accent borders, dynamic status glow,
 * formatted monospace monetary values, currency conversion preview, repayment progress indicator,
 * and quick-action trigger buttons with full accessible touch targets (48dp+).
 *
 * @param item Polymorphic financial item ([TransactionEntity], [DebtCreditEntity], or [LedgerItem]).
 * @param displayCurrency Target currency ("USD" or "IQD").
 * @param currentIqdRate Exchange rate for conversion calculations.
 * @param isKurdish Whether Kurdish Badini localization is enabled.
 * @param onClick Triggered when the whole tile is tapped to view details.
 * @param onSettleClick Triggered when the quick-settle action button is tapped (for debt records).
 * @param onDetailsClick Triggered when the dedicated details or edit button is tapped.
 * @param modifier Compose modifier.
 */
@Composable
fun LedgerTile(
  item: LedgerItem,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onClick: () -> Unit = {},
  onSettleClick: (() -> Unit)? = null,
  onDetailsClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val accentColor = item.accentColor
  val isPositive = item.isPositive
  val formattedDate = remember(item.dateMillis) {
    SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(item.dateMillis))
  }

  // Animated subtle status pulse for overdue items
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val borderGlowAlpha = if (item.isOverdue) pulseAlpha else 0.75f
  val cardBorderBrush = Brush.linearGradient(
    listOf(
      accentColor.copy(alpha = borderGlowAlpha),
      accentColor.copy(alpha = 0.35f),
      Color(0xFF1E2D4A).copy(alpha = 0.5f)
    )
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("ledger_tile_${item.id}"),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xF2091426), // Deep dark obsidian glass
    border = BorderStroke(1.2.dp, cardBorderBrush),
    tonalElevation = 4.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFF0F1D36).copy(alpha = 0.65f),
              Color(0xFF080F1E).copy(alpha = 0.95f)
            )
          )
        )
    ) {
      // Subtle top-right ambient neon glow
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(100.dp)
          .background(
            Brush.radialGradient(
              listOf(
                accentColor.copy(alpha = if (item.isOverdue) 0.18f else 0.10f),
                Color.Transparent
              )
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 1. Top Header Row: Directional Icon Badge + Titles + Monetary Amount
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          // Left: Directional Icon Badge and Titles
          Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Neon Icon Surface
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = accentColor.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = item.icon,
                  contentDescription = item.statusLabel,
                  tint = accentColor,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = item.subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Right: Amount and Currency Tag
          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            val prefix = if (isPositive) "+" else "-"
            val formattedAmount = LedgerFormattingHelper.formatCurrencyAuto(
              amount = item.amount,
              currency = item.currency,
              isKurdish = isKurdish,
              useCompactIqd = item.amount >= 1_000_000.0
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$prefix$formattedAmount",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
              )
              Spacer(modifier = Modifier.width(4.dp))
              Surface(
                shape = RoundedCornerShape(5.dp),
                color = accentColor.copy(alpha = 0.18f),
                border = BorderStroke(0.6.dp, accentColor.copy(alpha = 0.5f))
              ) {
                Text(
                  text = item.currency,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = accentColor,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }

            // Secondary Converted Value if displayCurrency differs
            if (item.currency != displayCurrency) {
              val converted = CurrencyService.convert(
                amount = item.amount,
                from = item.currency,
                to = displayCurrency,
                iqdRate = currentIqdRate
              )
              val formattedConverted = LedgerFormattingHelper.formatCurrencyAuto(
                amount = converted,
                currency = displayCurrency,
                isKurdish = isKurdish,
                useCompactIqd = converted >= 1_000_000.0
              )
              Text(
                text = "≈ $formattedConverted",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // 2. Middle Row: Status Badge Pill + Date Tag
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Status Pill Badge
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.14f),
            border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.6f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(accentColor)
              )
              Text(
                text = item.statusLabel,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          // Date Tag
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = Color(0xFF64748B),
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = formattedDate,
              fontSize = 10.5.sp,
              color = Color(0xFF64748B),
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // 3. Conditional Loan Repayment Progress Bar (For Debt / Credit Items)
        if (item.progressRatio != null && item.remainingBalance != null) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isKurdish) "باقیێ ماى:" else "Remaining Balance:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
              )
              val remFormatted = LedgerFormattingHelper.formatCurrencyAuto(
                amount = item.remainingBalance!!,
                currency = item.currency,
                isKurdish = isKurdish,
                useCompactIqd = item.remainingBalance!! >= 1_000_000.0
              )
              val remDisplay = if (item.currency != displayCurrency) {
                val conv = CurrencyService.convert(
                  amount = item.remainingBalance!!,
                  from = item.currency,
                  to = displayCurrency,
                  iqdRate = currentIqdRate
                )
                val convFormatted = LedgerFormattingHelper.formatCurrencyAuto(
                  amount = conv,
                  currency = displayCurrency,
                  isKurdish = isKurdish,
                  useCompactIqd = conv >= 1_000_000.0
                )
                "$remFormatted (≈ $convFormatted)"
              } else {
                remFormatted
              }

              Text(
                text = remDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isCompleted) EmeraldLight else Color.White,
                fontFamily = FontFamily.Monospace
              )
            }

            LinearProgressIndicator(
              progress = { item.progressRatio!! },
              modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = if (item.isCompleted) EmeraldLight else accentColor,
              trackColor = Color(0xFF1E293B)
            )
          }
        }

        // 4. Action Row (Settle Loan / Details / Edit)
        if (onSettleClick != null || onDetailsClick != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Settle Trigger Button (If not completed)
            if (onSettleClick != null && !item.isCompleted) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldLight.copy(alpha = 0.16f),
                border = BorderStroke(0.8.dp, EmeraldLight.copy(alpha = 0.7f)),
                modifier = Modifier
                  .clickable { onSettleClick() }
                  .testTag("ledger_tile_settle_btn_${item.id}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Settle",
                    tint = EmeraldLight,
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = if (isKurdish) "دان یان پارەدان" else "Settle / Pay",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                  )
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
            }

            // Details / Edit Trigger Button
            if (onDetailsClick != null) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF142036),
                border = BorderStroke(0.8.dp, Color(0xFF283B60)),
                modifier = Modifier
                  .clickable { onDetailsClick() }
                  .testTag("ledger_tile_details_btn_${item.id}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Details",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = if (isKurdish) "وردەکاری" else "Details",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
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

/**
 * Convenience Overload: Directly accepts a [TransactionEntity]
 */
@Composable
fun LedgerTile(
  transaction: TransactionEntity,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onClick: () -> Unit = {},
  onDetailsClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val item = remember(transaction, isKurdish) {
    LedgerItem.TransactionRecord(entity = transaction, isKurdish = isKurdish)
  }
  LedgerTile(
    item = item,
    displayCurrency = displayCurrency,
    currentIqdRate = currentIqdRate,
    isKurdish = isKurdish,
    onClick = onClick,
    onDetailsClick = onDetailsClick,
    modifier = modifier
  )
}

/**
 * Convenience Overload: Directly accepts a [DebtCreditEntity]
 */
@Composable
fun LedgerTile(
  debt: DebtCreditEntity,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onClick: () -> Unit = {},
  onSettleClick: (() -> Unit)? = null,
  onDetailsClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val item = remember(debt, isKurdish, displayCurrency, currentIqdRate) {
    LedgerItem.DebtRecord(
      entity = debt,
      isKurdish = isKurdish,
      displayCurrency = displayCurrency,
      currentIqdRate = currentIqdRate
    )
  }
  LedgerTile(
    item = item,
    displayCurrency = displayCurrency,
    currentIqdRate = currentIqdRate,
    isKurdish = isKurdish,
    onClick = onClick,
    onSettleClick = onSettleClick,
    onDetailsClick = onDetailsClick,
    modifier = modifier
  )
}

/**
 * Convenience Overload: Directly accepts a [FinancialLogEntry]
 */
@Composable
fun LedgerTile(
  entry: FinancialLogEntry,
  displayCurrency: String,
  currentIqdRate: Double,
  isKurdish: Boolean = true,
  onClick: () -> Unit = {},
  onSettleClick: (() -> Unit)? = null,
  onDetailsClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val accentColor: Color = when (entry.category) {
    FinancialLogCategory.CAPITAL_ADDITION -> EmeraldLight
    FinancialLogCategory.LOAN_TAKEN -> GoldAccent
    FinancialLogCategory.LOAN_REPAYMENT -> Color(0xFF38BDF8)
    FinancialLogCategory.EXPENSE_OUTFLOW -> LossRed
    else -> if (entry.isPositiveInflow) EmeraldLight else LossRed
  }

  val icon: ImageVector = when (entry.category) {
    FinancialLogCategory.CAPITAL_ADDITION -> Icons.AutoMirrored.Filled.TrendingUp
    FinancialLogCategory.LOAN_TAKEN -> Icons.Default.Handshake
    FinancialLogCategory.LOAN_REPAYMENT -> Icons.Default.Payment
    FinancialLogCategory.EXPENSE_OUTFLOW -> Icons.AutoMirrored.Filled.TrendingDown
    else -> Icons.AutoMirrored.Filled.ReceiptLong
  }

  val item = remember(entry, isKurdish) {
    LedgerItem.GenericRecord(
      id = entry.id,
      title = entry.title,
      subtitle = entry.subtitle,
      amount = entry.amount,
      currency = entry.currency,
      dateMillis = entry.dateMillis,
      isPositive = entry.isPositiveInflow,
      statusLabel = entry.badgeLabel,
      accentColor = accentColor,
      icon = icon,
      remainingBalance = entry.remainingLoanBalance,
      progressRatio = null
    )
  }

  LedgerTile(
    item = item,
    displayCurrency = displayCurrency,
    currentIqdRate = currentIqdRate,
    isKurdish = isKurdish,
    onClick = onClick,
    onSettleClick = onSettleClick,
    onDetailsClick = onDetailsClick,
    modifier = modifier
  )
}

// Compose Previews
// -------------------------------------------------------------

@Preview(name = "Transaction Inflow Preview", showBackground = true, backgroundColor = 0xFF070B14)
@Composable
private fun PreviewTransactionInflow() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      LedgerTile(
        item = LedgerItem.GenericRecord(
          id = "tx_preview_1",
          title = "Salary / مووچە",
          subtitle = "Monthly corporate consulting payout",
          amount = 4500.0,
          currency = "USD",
          isPositive = true,
          statusLabel = "داهات (Inflow)",
          accentColor = EmeraldLight,
          icon = Icons.AutoMirrored.Filled.TrendingUp
        ),
        displayCurrency = "IQD",
        currentIqdRate = 152000.0,
        isKurdish = true
      )
    }
  }
}

@Preview(name = "Debt Receivable Preview", showBackground = true, backgroundColor = 0xFF070B14)
@Composable
private fun PreviewDebtReceivable() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      LedgerTile(
        item = LedgerItem.GenericRecord(
          id = "debt_preview_1",
          title = "ئەحمەد زێباری (Ahmed)",
          subtitle = "Due: 2026/12/01 • دەف خەلکی",
          amount = 12000.0,
          currency = "USD",
          isPositive = true,
          statusLabel = "دەف خەلکی (Receivable)",
          accentColor = EmeraldLight,
          icon = Icons.Default.Handshake,
          remainingBalance = 4000.0,
          progressRatio = 0.67f
        ),
        displayCurrency = "USD",
        currentIqdRate = 152000.0,
        isKurdish = true,
        onSettleClick = {},
        onDetailsClick = {}
      )
    }
  }
}

@Preview(name = "Payable Overdue Preview", showBackground = true, backgroundColor = 0xFF070B14)
@Composable
private fun PreviewPayableOverdue() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      LedgerTile(
        item = LedgerItem.GenericRecord(
          id = "debt_preview_2",
          title = "کۆمپانیا عەقارات (Estate Vendor)",
          subtitle = "Due date expired 5 days ago",
          amount = 8500.0,
          currency = "USD",
          isPositive = false,
          statusLabel = "دیرکەفتی (Overdue)",
          accentColor = LossRed,
          icon = Icons.Default.Warning,
          remainingBalance = 8500.0,
          progressRatio = 0.0f,
          isOverdue = true
        ),
        displayCurrency = "USD",
        currentIqdRate = 152000.0,
        isKurdish = true,
        onSettleClick = {},
        onDetailsClick = {}
      )
    }
  }
}
