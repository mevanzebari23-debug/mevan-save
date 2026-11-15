package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

object DebtCreditType {
  const val CREDIT = "CREDIT" // Money owed by others to me (Receivable / دەف خەلکی)
  const val DEBT = "DEBT"     // Debt owed by me to creditors (Payable / لسەر من)
}

@Entity(tableName = "debts_credits")
data class DebtCreditEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: String, // "DEBT" (I owe a creditor) or "CREDIT" (Others owe me)
  val personName: String,
  val contactInfo: String = "",
  val totalAmount: Double,
  val amountPaid: Double = 0.0,
  val currency: String = "USD",
  val dueDateMillis: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
  val notes: String = "",
  val reminderEnabled: Boolean = true,
  val reminderDaysBefore: Int = 3,
  val isCompleted: Boolean = amountPaid >= totalAmount,
  val affectCashBalance: Boolean = false,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val exchangeRateAtEntry: Double = 0.0,
  val cashImpactAmount: Double = -1.0,
  val cashPaidImpactAmount: Double = -1.0
) {
  val isCreditReceivable: Boolean get() = type.equals(DebtCreditType.CREDIT, ignoreCase = true)
  val isDebtPayable: Boolean get() = type.equals(DebtCreditType.DEBT, ignoreCase = true)

  val lenderName: String get() = personName
  val borrowerName: String get() = personName
  val counterpartyName: String get() = personName
  val remainingAmount: Double get() = (totalAmount - amountPaid).coerceAtLeast(0.0)
  val remainingBalance: Double get() = remainingAmount
  val isOverdue: Boolean get() = !isCompleted && remainingAmount > 0.0 && System.currentTimeMillis() > dueDateMillis
  val isActiveLoan: Boolean get() = !isCompleted && remainingBalance > 0.0
  val repaymentProgressRatio: Float
    get() = if (totalAmount > 0.0) (amountPaid / totalAmount).coerceIn(0.0, 1.0).toFloat() else 0f
  val repaymentProgressPercent: Int
    get() = (repaymentProgressRatio * 100f).toInt().coerceIn(0, 100)

  /**
   * Exact amount of principal that affects liquid cash balance.
   * When a user adds $100 to an existing $4,700 loan and chooses "Affect Cash",
   * only $100 affects cash instead of the entire $4,800.
   */
  val effectiveCashPrincipalImpact: Double
    get() = when {
      !affectCashBalance -> 0.0
      cashImpactAmount >= 0.0 -> cashImpactAmount
      else -> {
        val regex = Regex("""\+([0-9]+(?:\.[0-9]+)?)\s*\(""")
        val matches = regex.findAll(notes).mapNotNull { it.groupValues[1].toDoubleOrNull() }.toList()
        if (matches.isNotEmpty()) matches.sum() else totalAmount
      }
    }

  /**
   * Exact amount of repayments (received or paid) that affects liquid cash balance.
   */
  val effectiveCashPaidImpact: Double
    get() = when {
      cashPaidImpactAmount >= 0.0 -> cashPaidImpactAmount
      else -> amountPaid
    }

  val isRealEstateDebt: Boolean get() = notes.contains("[REAL_ESTATE_DEBT", ignoreCase = true)

  val isMulukDebt: Boolean
    get() = isRealEstateDebt ||
      notes.contains("[MULUK", ignoreCase = true) ||
      notes.contains("ملوک", ignoreCase = true) ||
      notes.contains("عەقار", ignoreCase = true) ||
      notes.contains("[REAL_ESTATE", ignoreCase = true) ||
      notes.contains("[PRIVATE_ACCOUNT", ignoreCase = true) ||
      notes.contains("تایبەت", ignoreCase = true)

  /**
   * Signed impact of this record's remaining balance on Net Capital Balance:
   * - Positive (+remainingAmount) when money is owed by others (CREDIT)
   * - Negative (-remainingAmount) when debt is owed to a creditor (DEBT)
   * - 0.0 for dedicated Real Estate debts (independent account)
   */
  val signedNetCapitalImpact: Double
    get() = if (isCompleted || isMulukDebt) 0.0 else if (isCreditReceivable) remainingAmount else -remainingAmount

  /**
   * Signed impact of repayments collected/paid on liquid cashflow:
   * - Positive (+effectiveCashPaidImpact) when collecting money owed by others (CREDIT)
   * - Negative (-effectiveCashPaidImpact) when paying back a creditor (DEBT)
   */
  val signedCashRepaymentImpact: Double
    get() = if (isCreditReceivable) effectiveCashPaidImpact else -effectiveCashPaidImpact

  // Gold Debt (قەرز ب زێڕ: مسقال و گرام)
  val isGoldDebt: Boolean get() = currency.startsWith("GOLD", ignoreCase = true) || notes.contains("[GOLD_DEBT", ignoreCase = true)

  val goldUnit: String get() {
    val regex = Regex("""\[GOLD_DEBT:[0-9.]+:([0-9]+):([A-Z_]+)\]""")
    val match = regex.find(notes)
    return match?.groupValues?.get(2) ?: if (currency.contains("GRAM", ignoreCase = true)) "GRAM" else "MITHQAL"
  }

  val goldKarat: Int get() {
    val regex = Regex("""\[GOLD_DEBT:[0-9.]+:([0-9]+):[A-Z_]+\]""")
    val match = regex.find(notes)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 21
  }

  val goldWeightMithqal: Double get() {
    if (!isGoldDebt) return 0.0
    val regex = Regex("""\[GOLD_DEBT:([0-9.]+):([0-9]+):([A-Z_]+)\]""")
    val match = regex.find(notes)
    if (match != null) {
      val raw = match.groupValues[1].toDoubleOrNull() ?: totalAmount
      val unit = match.groupValues[2]
      return if (unit.contains("GRAM")) raw / 5.0 else raw
    }
    return if (goldUnit.contains("GRAM")) totalAmount / 5.0 else totalAmount
  }

  val goldWeightGrams: Double get() = goldWeightMithqal * 5.0

  val goldRemainingMithqal: Double get() = if (totalAmount > 0) (goldWeightMithqal * (remainingAmount / totalAmount)).coerceAtLeast(0.0) else goldWeightMithqal
  val goldRemainingGrams: Double get() = goldRemainingMithqal * 5.0

  fun goldLiveValueUsd(goldPricePerGram24K: Double): Double {
    if (!isGoldDebt) return remainingAmount
    val purity = when (goldKarat) {
      24 -> 1.0
      22 -> 22.0 / 24.0
      21 -> 21.0 / 24.0
      18 -> 18.0 / 24.0
      else -> 21.0 / 24.0
    }
    val pricePerGram = if (goldPricePerGram24K > 0.0) goldPricePerGram24K * purity else 80.0 * purity
    return goldRemainingGrams * pricePerGram
  }

  fun toActiveDebtCreditLoan(payments: List<DebtPaymentEntity> = emptyList()): ActiveDebtCreditLoan {
    return ActiveDebtCreditLoan(
      id = id,
      type = type,
      lenderName = personName,
      borrowerName = personName,
      counterpartyName = personName,
      contactInfo = contactInfo,
      totalAmount = totalAmount,
      amountPaid = amountPaid,
      remainingBalance = remainingBalance,
      signedNetCapitalImpact = signedNetCapitalImpact,
      currency = currency,
      dueDateMillis = dueDateMillis,
      createdAtMillis = createdAtMillis,
      notes = notes,
      reminderEnabled = reminderEnabled,
      isCompleted = isCompleted || remainingBalance <= 0.0,
      isOverdue = isOverdue,
      repaymentProgressRatio = repaymentProgressRatio,
      repaymentProgressPercent = repaymentProgressPercent,
      payments = payments.sortedByDescending { it.dateMillis }
    )
  }
}

/**
 * Unified Room relation joining a Debt/Credit record with all of its installment repayments.
 */
data class UnifiedDebtCreditWithPayments(
  @Embedded val entry: DebtCreditEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "debtId"
  )
  val payments: List<DebtPaymentEntity> = emptyList()
) {
  fun toDomainModel(): ActiveDebtCreditLoan = entry.toActiveDebtCreditLoan(payments)
}

/**
 * Aggregated snapshot of how money owed by others (CREDIT) and debts owed to creditors (DEBT)
 * automatically update the Net Capital Balance.
 */
data class UnifiedCapitalDebtSummary(
  val baseCapitalBeforeDebtCredit: Double,
  val totalOwedByOthers: Double,
  val totalDebtsToCreditors: Double,
  val netDebtCreditOffset: Double,
  val totalCreditRepaymentsCollected: Double,
  val totalDebtRepaymentsPaid: Double,
  val netCapitalBalance: Double,
  val totalNetWorth: Double,
  val activeReceivablesCount: Int,
  val activePayablesCount: Int,
  val settledCount: Int
)

/**
 * Domain data model for tracking active Debt and Credit loans, including
 * lender/borrower names, principal, paid amount, remaining balance, repayment progress,
 * due date status, and payment history.
 */
data class ActiveDebtCreditLoan(
  val id: Long,
  val type: String, // "DEBT" (Borrowed from Lender) or "CREDIT" (Credited / Lent to Borrower)
  val lenderName: String,
  val borrowerName: String,
  val counterpartyName: String,
  val contactInfo: String,
  val totalAmount: Double,
  val amountPaid: Double,
  val remainingBalance: Double,
  val signedNetCapitalImpact: Double = if (type == DebtCreditType.CREDIT) remainingBalance else -remainingBalance,
  val currency: String,
  val dueDateMillis: Long,
  val createdAtMillis: Long,
  val notes: String,
  val reminderEnabled: Boolean,
  val isCompleted: Boolean,
  val isOverdue: Boolean,
  val repaymentProgressRatio: Float,
  val repaymentProgressPercent: Int,
  val payments: List<DebtPaymentEntity> = emptyList()
) {
  val isRealEstateDebt: Boolean get() = notes.contains("[REAL_ESTATE_DEBT", ignoreCase = true)
  val personName: String get() = counterpartyName
  val isActive: Boolean get() = !isCompleted && remainingBalance > 0.0
  val isDebtLoan: Boolean get() = type == DebtCreditType.DEBT
  val isCreditLoan: Boolean get() = type == DebtCreditType.CREDIT
  val cleanNotesWithoutTags: String get() = notes
    .replace(Regex("""\[REAL_ESTATE_DEBT\]"""), "")
    .replace(Regex("""\[GOLD_DEBT:[0-9.]+:([0-9]+):([A-Z_]+)\]"""), "")
    .trim()

  val isGoldDebt: Boolean get() = currency.startsWith("GOLD", ignoreCase = true) || notes.contains("[GOLD_DEBT", ignoreCase = true)

  val goldUnit: String get() {
    val regex = Regex("""\[GOLD_DEBT:[0-9.]+:([0-9]+):([A-Z_]+)\]""")
    val match = regex.find(notes)
    return match?.groupValues?.get(2) ?: if (currency.contains("GRAM", ignoreCase = true)) "GRAM" else "MITHQAL"
  }

  val goldKarat: Int get() {
    val regex = Regex("""\[GOLD_DEBT:[0-9.]+:([0-9]+):[A-Z_]+\]""")
    val match = regex.find(notes)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 21
  }

  val goldWeightMithqal: Double get() {
    if (!isGoldDebt) return 0.0
    val regex = Regex("""\[GOLD_DEBT:([0-9.]+):([0-9]+):([A-Z_]+)\]""")
    val match = regex.find(notes)
    if (match != null) {
      val raw = match.groupValues[1].toDoubleOrNull() ?: totalAmount
      val unit = match.groupValues[2]
      return if (unit.contains("GRAM")) raw / 5.0 else raw
    }
    return if (goldUnit.contains("GRAM")) totalAmount / 5.0 else totalAmount
  }

  val goldWeightGrams: Double get() = goldWeightMithqal * 5.0
  val goldRemainingMithqal: Double get() = if (totalAmount > 0) (goldWeightMithqal * (remainingBalance / totalAmount)).coerceAtLeast(0.0) else goldWeightMithqal
  val goldRemainingGrams: Double get() = goldRemainingMithqal * 5.0

  fun goldLiveValueUsd(goldPricePerGram24K: Double): Double {
    if (!isGoldDebt) return remainingBalance
    val purity = when (goldKarat) {
      24 -> 1.0
      22 -> 22.0 / 24.0
      21 -> 21.0 / 24.0
      18 -> 18.0 / 24.0
      else -> 21.0 / 24.0
    }
    val pricePerGram = if (goldPricePerGram24K > 0.0) goldPricePerGram24K * purity else 80.0 * purity
    return goldRemainingGrams * pricePerGram
  }
}

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val debtId: Long,
  val amount: Double,
  val dateMillis: Long = System.currentTimeMillis(),
  val notes: String = "",
  val exchangeRateAtEntry: Double = 0.0,
  val affectCashBalance: Boolean = true
)

