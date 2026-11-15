package com.example.ui.history

import com.example.data.local.BudgetEntity
import com.example.data.local.CarPaymentEntity
import com.example.data.local.CarServiceEntity
import com.example.data.local.DebtCreditEntity
import com.example.data.local.DebtPaymentEntity
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.InvestmentMonthRecordEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity

enum class ActionSection(
  val labelKu: String,
  val labelEn: String,
  val tabIndex: Int
) {
  CASH("💵 کاش و مەسروفات", "💵 Cash & Ledger", 3),
  SCHOOL("🎓 قوتابخانە", "🎓 School Account", 3),
  DEBT("🤝 قەرز و دەین", "🤝 Debts & Credits", 1),
  CAR("🚗 سەیارە", "🚗 Cars Fleet", 2),
  REAL_ESTATE("🏢 حسابا تایبەت", "🏢 Private Account", 2),
  GOLD("🥇 زێڕ و زیڤ", "🥇 Gold & Silver", 2),
  BUDGET("🎯 بودجە", "🎯 Budgets", 3),
  CAPITAL("🏛️ سەرمایە و نرخ", "🏛️ Capital & Rates", 0)
}

enum class ActionOperationType(
  val labelKu: String,
  val labelEn: String
) {
  ADD("زێدەکرن", "Added"),
  EDIT("دەستکاری", "Edited"),
  DELETE("ژێبرن", "Deleted"),
  STATUS_CHANGE("گوهۆڕینا بارودۆخی", "Updated")
}

sealed class ActionPayloadSnapshot {
  data class TransactionSnapshot(
    val before: TransactionEntity?,
    val after: TransactionEntity?
  ) : ActionPayloadSnapshot()

  data class InvestmentSnapshot(
    val before: InvestmentEntity?,
    val after: InvestmentEntity?,
    val deletedServices: List<CarServiceEntity> = emptyList(),
    val deletedPayments: List<CarPaymentEntity> = emptyList(),
    val deletedMonthlyRecords: List<InvestmentMonthRecordEntity> = emptyList(),
    val extraCarPaymentAddedId: Long? = null
  ) : ActionPayloadSnapshot()

  data class CarPaymentSnapshot(
    val before: CarPaymentEntity?,
    val after: CarPaymentEntity?,
    val parentCarBefore: InvestmentEntity? = null
  ) : ActionPayloadSnapshot()

  data class CarServiceSnapshot(
    val before: CarServiceEntity?,
    val after: CarServiceEntity?
  ) : ActionPayloadSnapshot()

  data class GoldHoldingSnapshot(
    val before: GoldHoldingEntity?,
    val after: GoldHoldingEntity?
  ) : ActionPayloadSnapshot()

  data class DebtCreditSnapshot(
    val before: DebtCreditEntity?,
    val after: DebtCreditEntity?,
    val deletedPayments: List<DebtPaymentEntity> = emptyList(),
    val addedPayment: DebtPaymentEntity? = null
  ) : ActionPayloadSnapshot()

  data class DebtPaymentSnapshot(
    val before: DebtPaymentEntity?,
    val after: DebtPaymentEntity?,
    val parentDebtBefore: DebtCreditEntity? = null
  ) : ActionPayloadSnapshot()

  data class SettingsSnapshot(
    val before: UserSettingsEntity,
    val after: UserSettingsEntity
  ) : ActionPayloadSnapshot()

  data class BudgetSnapshot(
    val before: BudgetEntity?,
    val after: BudgetEntity?
  ) : ActionPayloadSnapshot()
}

data class ReversibleAction(
  val id: String,
  val timestampMillis: Long = System.currentTimeMillis(),
  val section: ActionSection,
  val operationType: ActionOperationType,
  val titleKu: String,
  val titleEn: String,
  val subtitleKu: String,
  val subtitleEn: String,
  val cashDeltaDisplay: Double = 0.0,
  val netWorthDeltaDisplay: Double = 0.0,
  val displayCurrency: String = "USD",
  val snapshot: ActionPayloadSnapshot
) {
  fun title(isKurdish: Boolean): String = if (isKurdish) titleKu else titleEn
  fun subtitle(isKurdish: Boolean): String = if (isKurdish) subtitleKu else subtitleEn

  val canQuickEdit: Boolean
    get() = when (val s = snapshot) {
      is ActionPayloadSnapshot.TransactionSnapshot -> (s.after ?: s.before) != null
      is ActionPayloadSnapshot.InvestmentSnapshot -> (s.after ?: s.before) != null
      is ActionPayloadSnapshot.GoldHoldingSnapshot -> (s.after ?: s.before) != null
      is ActionPayloadSnapshot.DebtCreditSnapshot -> (s.after ?: s.before) != null
      is ActionPayloadSnapshot.BudgetSnapshot -> (s.after ?: s.before) != null
      else -> false
    }
}

sealed class QuickEditableTarget {
  data class TransactionTarget(val transaction: TransactionEntity, val isSchool: Boolean) : QuickEditableTarget()
  data class InvestmentTarget(val investment: InvestmentEntity) : QuickEditableTarget()
  data class GoldTarget(val holding: GoldHoldingEntity) : QuickEditableTarget()
  data class DebtTarget(val debtCredit: DebtCreditEntity) : QuickEditableTarget()
  data class BudgetTarget(val budget: BudgetEntity) : QuickEditableTarget()
}
