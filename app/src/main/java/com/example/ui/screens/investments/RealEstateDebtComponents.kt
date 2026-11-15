package com.example.ui.screens.investments

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.currency.CurrencyService
import com.example.data.local.ActiveDebtCreditLoan
import com.example.ui.FinanceUiState
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedLight

/**
 * Standalone Real Estate Debt & Credit Ledger Card.
 * Allows users to track debts and credits specifically for real estate transactions
 * (e.g. "I lent someone 5,000 on a property" or "I owe 5,000 on real estate"),
 * completely decoupled from master liquid cash and 100% isolated from the main Gold tab.
 */
@Composable
fun RealEstateDebtLedgerCard(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onAddNewLoan: (type: String) -> Unit,
  onRecordPayment: (loan: ActiveDebtCreditLoan) -> Unit,
  onSettleLoan: (loan: ActiveDebtCreditLoan) -> Unit,
  onDeleteLoan: (loan: ActiveDebtCreditLoan) -> Unit,
  onToggleClassification: ((loan: ActiveDebtCreditLoan) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(true) }
  var showImportGeneralDialog by remember { mutableStateOf(false) }
  val activeLoans = uiState.activeRealEstateDebtCreditLoans
  val settledLoans = uiState.settledRealEstateDebtCreditLoans
  val allLoans = uiState.realEstateDebtCreditLoans

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.2.dp,
      brush = Brush.horizontalGradient(
        colors = listOf(
          Color(0xFF38BDF8).copy(alpha = 0.8f),
          EmeraldLight.copy(alpha = 0.6f),
          GoldAccent.copy(alpha = 0.7f)
        )
      )
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("real_estate_debt_ledger_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkGlassCardSurface)
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Header: Icon + Title + Standalone Badge + Expand/Collapse
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(
                Brush.linearGradient(
                  colors = listOf(Color(0xFF38BDF8), EmeraldLight)
                ),
                RoundedCornerShape(10.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Handshake,
              contentDescription = null,
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isKurdish) "🤝 حسابا قەرز و دەینێن عەقاری" else "🤝 Real Estate Debt & Credit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(EmeraldLight.copy(alpha = 0.18f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (isKurdish) "سەربەخۆ" else "STANDALONE",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = EmeraldLight
                )
              }
            }
            Text(
              text = if (isKurdish) "بتنێ بۆ عەقاری (جودا ژ سەرمایەی و زێڕێ سەرەکی)" else "Dedicated property ledger (0 impact on cash/main gold)",
              fontSize = 10.5.sp,
              color = Color.White.copy(alpha = 0.75f)
            )
          }
        }

        IconButton(
          onClick = { isExpanded = !isExpanded },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f)
          )
        }
      }

      // 2. Summary Mini-Cards: Lent to Others (+) vs Debt Owed (−) vs Net
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Receivable (+ money lent out)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0x2210B981)),
          border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.35f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = if (isKurdish) "📥 قەرزێ من (تایبەت)" else "📥 Private Money Owed",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
            Text(
              text = "+${CurrencyService.formatShort(uiState.totalPrivateAccountCreditsReceivable, uiState.displayCurrency)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = EmeraldLight
            )
          }
        }

        // Payable (− debt owed)
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0x22EF4444)),
          border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.35f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = if (isKurdish) "📤 قەرزێ لسەر من (تایبەت)" else "📤 Private Debts I Owe",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = LossRedLight
            )
            Text(
              text = "−${CurrencyService.formatShort(uiState.totalRealEstateDebtsPayable, uiState.displayCurrency)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = LossRedLight
            )
          }
        }

        // Net Balance
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0x2238BDF8)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = if (isKurdish) "⚖️ صافی قەرزێ تایبەت" else "⚖️ Net Private Offset",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
            val net = uiState.netRealEstateDebtCreditBalance
            val sign = if (net > 0) "+" else ""
            Text(
              text = "$sign${CurrencyService.formatShort(net, uiState.displayCurrency)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = if (net >= 0) EmeraldLight else LossRedLight
            )
          }
        }
      }

      // 3. Quick Action Buttons: "I lent someone 5,000 (+)" vs "I borrowed 5,000 (−)"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { onAddNewLoan("CREDIT") },
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldLight,
            contentColor = Color(0xFF0F172A)
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("add_real_estate_credit_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isKurdish) "📥 من دا یە ئێکی (+٥٠٠٠)" else "📥 Lent to Person (+)",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }

        Button(
          onClick = { onAddNewLoan("DEBT") },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("add_real_estate_debt_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isKurdish) "📤 من قەرز کر (−٥٠٠٠)" else "📤 Borrowed Debt (−)",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      // 4. Expanded Content: List of loans
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (allLoans.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isKurdish) {
                  "💡 چ قەرز و دەینێن عەقاری نینن.\nدەمێ تە پارە دا ئێکی ب قەرز یان قەرز لسەر عەقاری کر، ل ڤێرێ تۆمار بکە."
                } else {
                  "💡 No real estate debts logged yet.\nUse buttons above to record property loans (0 impact on main capital)."
                },
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          } else {
            // Active loans
            allLoans.forEach { loan ->
              RealEstateLoanItemCard(
                loan = loan,
                isKurdish = isKurdish,
                onRecordPayment = { onRecordPayment(loan) },
                onSettle = { onSettleLoan(loan) },
                onDelete = { onDeleteLoan(loan) },
                onToggleClassification = onToggleClassification?.let { { it(loan) } }
              )
            }
          }

          if (uiState.activeCreditLoans.isNotEmpty() && onToggleClassification != null) {
            OutlinedButton(
              onClick = { showImportGeneralDialog = true },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (isKurdish) "📥 گواستنەوەی قەرزەکێ ژ بەشێ گشتی بۆ ڤێرێ (${uiState.activeCreditLoans.size})"
                else "📥 Move a Loan from General Debts (${uiState.activeCreditLoans.size})",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }

  if (showImportGeneralDialog && onToggleClassification != null) {
    AlertDialog(
      onDismissRequest = { showImportGeneralDialog = false },
      title = {
        Text(
          text = if (isKurdish) "گواستنەوە بۆ حسابا تایبەت" else "Move to Private Account",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (isKurdish) "قەرزەکێ هەڵبژێرە دا بچیتە سەر حسابا تایبەت:" else "Select a loan to move to Private Account:",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
          )
          uiState.activeCreditLoans.forEach { creditLoan ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0x2210B981)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onToggleClassification(creditLoan)
                  showImportGeneralDialog = false
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(creditLoan.personName, fontWeight = FontWeight.Bold, color = Color.White)
                  Text(
                    text = creditLoan.notes.ifBlank { if (isKurdish) "قەرزێ ل دەڤ خەلکی" else "Credit Loan" },
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                  )
                }
                Text(
                  text = "+${CurrencyService.format(creditLoan.remainingBalance, creditLoan.currency)}",
                  fontWeight = FontWeight.Black,
                  color = EmeraldLight
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showImportGeneralDialog = false }) {
          Text(if (isKurdish) "گرتن" else "Close")
        }
      }
    )
  }
}

/**
 * Individual Card representing one Real Estate Loan / Credit record.
 */
@Composable
fun RealEstateLoanItemCard(
  loan: ActiveDebtCreditLoan,
  isKurdish: Boolean,
  onRecordPayment: () -> Unit,
  onSettle: () -> Unit,
  onDelete: () -> Unit,
  onToggleClassification: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isCredit = loan.isCreditLoan
  val accentColor = if (isCredit) EmeraldLight else LossRedLight
  val bgTint = if (isCredit) Color(0x1510B981) else Color(0x15EF4444)
  val cleanNotes = loan.notes.replace("[REAL_ESTATE_DEBT]", "").trim()

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = bgTint),
    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Row 1: Person Name + Loan Type Badge + Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .background(accentColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isCredit) "📥" else "📤",
              fontSize = 14.sp
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = loan.personName,
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isCredit) {
                if (isKurdish) "قەرزێ من ل دەف وی" else "Lent by me (Receivable)"
              } else {
                if (isKurdish) "قەرزێ وی لسەر من" else "Owed by me (Payable)"
              },
              fontSize = 10.sp,
              color = accentColor,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Status or Amount
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = CurrencyService.format(loan.remainingBalance, loan.currency),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = accentColor
          )
          if (loan.amountPaid > 0) {
            Text(
              text = if (isKurdish) "داینە: ${CurrencyService.formatShort(loan.amountPaid, loan.currency)}" else "Paid: ${CurrencyService.formatShort(loan.amountPaid, loan.currency)}",
              fontSize = 10.sp,
              color = Color.White.copy(alpha = 0.6f)
            )
          }
        }
      }

      // Progress bar if partial payment
      if (loan.totalAmount > 0) {
        val progress = (loan.amountPaid / loan.totalAmount).coerceIn(0.0, 1.0).toFloat()
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = accentColor,
          trackColor = Color.White.copy(alpha = 0.12f)
        )
      }

      // Notes (if any)
      if (cleanNotes.isNotBlank()) {
        Text(
          text = "📝 $cleanNotes",
          fontSize = 11.sp,
          color = Color.White.copy(alpha = 0.8f),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (loan.isActive) {
          // Record payment
          Button(
            onClick = onRecordPayment,
            colors = ButtonDefaults.buttonColors(
              containerColor = accentColor.copy(alpha = 0.25f),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp), tint = accentColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "تۆمارکرنا پارەدانێ" else "Pay / Collect",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = accentColor
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Settle in full
          Button(
            onClick = onSettle,
            colors = ButtonDefaults.buttonColors(
              containerColor = EmeraldLight.copy(alpha = 0.25f),
              contentColor = EmeraldLight
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldLight)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) "تەواوکرن" else "Settle",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
          }
        } else {
          Box(
            modifier = Modifier
              .background(EmeraldLight.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (isKurdish) "✅ تەواوبوویە" else "✅ Settled",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldLight
            )
          }
        }

        if (onToggleClassification != null) {
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = onToggleClassification,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.White.copy(alpha = 0.12f),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text(
              text = if (isKurdish) "گواستنەوە" else "Move",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Delete button
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Interactive Dialog to add a Real Estate Debt or Credit (loan to someone or loan taken).
 */
@Composable
fun AddRealEstateDebtDialog(
  defaultType: String = "CREDIT",
  displayCurrency: String = "USD",
  currentIqdRate: Double = 1530.0,
  isKurdish: Boolean = true,
  onDismiss: () -> Unit,
  onConfirm: (type: String, personName: String, amount: Double, currency: String, notes: String) -> Unit
) {
  var type by remember { mutableStateOf(defaultType) } // "CREDIT" or "DEBT"
  var personName by remember { mutableStateOf("") }
  var amountInput by remember { mutableStateOf("") }
  var currency by remember { mutableStateOf(if (displayCurrency == "IQD") "IQD" else "USD") }
  var notesInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Handshake,
          contentDescription = null,
          tint = if (type == "CREDIT") EmeraldLight else LossRedLight,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isKurdish) "قەرزێ سەربەخۆ یێ عەقاری" else "Real Estate Loan / Debt",
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Type Selector Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { type = "CREDIT" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (type == "CREDIT") EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = if (isKurdish) "📥 من دا یە ئێکی\n(وەکی ٥٠٠٠ ب قەرز)" else "📥 Lent to Person\n(Credit Receivable)",
              color = if (type == "CREDIT") Color(0xFF0F172A) else Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }

          Button(
            onClick = { type = "DEBT" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (type == "DEBT") LossRed else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = if (isKurdish) "📤 من قەرز کر\n(قەرز لسەر منە)" else "📤 Borrowed Debt\n(Debt Payable)",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }

        // Standalone Notice
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1810B981), RoundedCornerShape(8.dp))
            .border(0.8.dp, EmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Text(
            text = if (isKurdish) {
              "ℹ️ ئەڤ قەرزە حسابەکا سەربەخۆیە بۆ عەقاری؛ ڕاستەوخۆ د عەقاری دا حساب دبیت و چ کار ناکەتە سەر سەرمایێ گشتی."
            } else {
              "ℹ️ This is a standalone property ledger; it updates real estate net worth without impacting general cash."
            },
            fontSize = 10.5.sp,
            color = EmeraldLight
          )
        }

        // Person Name Input
        OutlinedTextField(
          value = personName,
          onValueChange = { personName = it },
          label = { Text(if (isKurdish) "ناڤێ کەسی / کڕیار یان فرۆشیار" else "Person Name (Buyer, Seller, Partner)") },
          placeholder = { Text(if (isKurdish) "بۆ نموونە: ئەحمەد، ڕێبین" else "e.g. Ahmed, Partner") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Currency Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USD", "IQD").forEach { cur ->
            Button(
              onClick = { currency = cur },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (currency == cur) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = cur,
                color = if (currency == cur) Color(0xFF0F172A) else Color.White,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Amount Input
        OutlinedTextField(
          value = amountInput,
          onValueChange = { amountInput = it },
          label = { Text("${if (isKurdish) "بڕێ پارەی یێ قەرزی" else "Loan Amount"} ($currency)") },
          placeholder = { Text("5000") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Notes Input
        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text(if (isKurdish) "تێبینی (بۆ نموونە: لسەر شوقەیێ، بەشێ عەقاری)" else "Notes (e.g. property details, plot number)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull() ?: 0.0
          if (personName.isNotBlank() && amt > 0.0) {
            onConfirm(type, personName.trim(), amt, currency, notesInput.trim())
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (type == "CREDIT") EmeraldLight else LossRed,
          contentColor = if (type == "CREDIT") Color(0xFF0F172A) else Color.White
        ),
        enabled = personName.isNotBlank() && (amountInput.toDoubleOrNull() ?: 0.0) > 0.0
      ) {
        Text(if (isKurdish) "تۆمارکرن" else "Save Loan", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}

/**
 * Dialog to record payment or full settlement on a real estate loan.
 */
@Composable
fun RecordRealEstatePaymentDialog(
  loan: ActiveDebtCreditLoan,
  isKurdish: Boolean = true,
  onDismiss: () -> Unit,
  onConfirmPayment: (amount: Double, notes: String) -> Unit,
  onSettleInFull: () -> Unit,
  onDeleteLoan: () -> Unit
) {
  var paymentInput by remember { mutableStateOf("") }
  var paymentNotes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isKurdish) "تۆمارکرنا پارەدانا قەرزی: ${loan.personName}" else "Record Loan Payment: ${loan.personName}",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Loan balance summary
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
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
                text = if (isKurdish) "ماوەیێ قەرزی:" else "Remaining Balance:",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
              )
              Text(
                text = CurrencyService.format(loan.remainingBalance, loan.currency),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = EmeraldLight
              )
            }

            Button(
              onClick = {
                paymentInput = if (loan.remainingBalance % 1.0 == 0.0) {
                  loan.remainingBalance.toLong().toString()
                } else {
                  loan.remainingBalance.toString()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent.copy(alpha = 0.25f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(if (isKurdish) "کامل مەبلەغ" else "Full", fontSize = 10.5.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
            }
          }
        }

        OutlinedTextField(
          value = paymentInput,
          onValueChange = { paymentInput = it },
          label = { Text("${if (isKurdish) "بڕێ پارێ هاتیەدان" else "Payment Amount"} (${loan.currency})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = paymentNotes,
          onValueChange = { paymentNotes = it },
          label = { Text(if (isKurdish) "تێبینی (بۆ نموونە: پارەدانا ئێکێ)" else "Notes (e.g. 1st installment)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Settle In Full button
        OutlinedButton(
          onClick = onSettleInFull,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
          border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isKurdish) "✅ تەواوکرنا قەرزی ب تەمامی" else "✅ Settle In Full", fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = paymentInput.toDoubleOrNull() ?: 0.0
          if (amt > 0.0) {
            onConfirmPayment(amt, paymentNotes)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
        enabled = (paymentInput.toDoubleOrNull() ?: 0.0) > 0.0
      ) {
        Text(if (isKurdish) "تۆمارکرن" else "Confirm Payment", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Row {
        TextButton(
          onClick = onDeleteLoan,
          colors = ButtonDefaults.textButtonColors(contentColor = LossRed)
        ) {
          Text(if (isKurdish) "ژێبرن" else "Delete")
        }
        TextButton(onClick = onDismiss) {
          Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
        }
      }
    }
  )
}
