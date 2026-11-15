package com.example.data.repository

import com.example.data.currency.CurrencyService
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CarPaymentEntity
import com.example.data.local.CarServiceEntity
import com.example.data.local.DebtCreditEntity
import com.example.data.local.DebtPaymentEntity
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.FinanceDao
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.InvestmentMonthRecordEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UnifiedDebtCreditWithPayments
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class FinanceRepository(private val database: AppDatabase) {
  private val dao: FinanceDao = database.financeDao()

  val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
  val allInvestments: Flow<List<InvestmentEntity>> = dao.getAllInvestments()
  val allMonthlyRecords: Flow<List<InvestmentMonthRecordEntity>> = dao.getAllMonthlyRecords()
  val allDebtsCredits: Flow<List<DebtCreditEntity>> = dao.getAllDebtsCredits()
  val allUnifiedDebtsCredits: Flow<List<UnifiedDebtCreditWithPayments>> = dao.getAllUnifiedDebtsCreditsWithPayments()
  val allDebtPayments: Flow<List<DebtPaymentEntity>> = dao.getAllDebtPayments()
  val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
  val allGoldHoldings: Flow<List<GoldHoldingEntity>> = dao.getAllGoldHoldings()
  val allCarServices: Flow<List<CarServiceEntity>> = dao.getAllCarServices()
  val allCarPayments: Flow<List<CarPaymentEntity>> = dao.getAllCarPayments()
  val allExpenseCategories: Flow<List<ExpenseCategoryEntity>> = dao.getAllExpenseCategories()
  val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()

  suspend fun getAllExpenseCategoriesSync(): List<ExpenseCategoryEntity> = dao.getAllExpenseCategoriesSync()

  suspend fun addExpenseCategory(name: String, type: String = "OUT"): Long? {
    val cleanName = name.trim().replace("\n", " ")
    if (cleanName.isBlank()) return null
    val normalizedType = if (type.equals("IN", ignoreCase = true)) "IN" else "OUT"
    val existing = dao.getAllExpenseCategoriesSync().find {
      it.name.equals(cleanName, ignoreCase = true) && it.type.equals(normalizedType, ignoreCase = true)
    }
    if (existing != null) return existing.id
    return dao.insertExpenseCategory(
      ExpenseCategoryEntity(
        name = cleanName,
        type = normalizedType,
        createdAtMillis = System.currentTimeMillis()
      )
    )
  }

  suspend fun deleteExpenseCategory(
    name: String,
    type: String = "OUT",
    deleteAssociatedTransactions: Boolean = false,
    reassignToCategory: String? = null
  ) {
    val cleanName = name.trim()
    if (cleanName.isBlank()) return
    val normalizedType = if (type.equals("IN", ignoreCase = true)) "IN" else "OUT"
    if (deleteAssociatedTransactions) {
      dao.deleteTransactionsByCategory(cleanName, normalizedType)
      if (normalizedType == "OUT") {
        dao.deleteBudgetsByCategory(cleanName)
      }
    } else {
      val fallbackCategory = reassignToCategory?.trim()?.takeIf {
        it.isNotEmpty() && !it.equals(cleanName, ignoreCase = true)
      } ?: if (normalizedType == "IN") "داهاتێ گشتی" else "مەسروفێ گشتی"
      dao.reassignTransactionsCategory(
        oldCategory = cleanName,
        newCategory = fallbackCategory,
        type = normalizedType
      )
      if (normalizedType == "OUT") {
        dao.reassignBudgetsCategory(
          oldCategory = cleanName,
          newCategory = fallbackCategory
        )
      }
    }
    dao.deleteExpenseCategoryByNameAndType(cleanName, normalizedType)
  }

  suspend fun deleteExpenseCategoryById(
    id: Long,
    deleteAssociatedTransactions: Boolean = false,
    reassignToCategory: String? = null
  ) {
    val existing = dao.getExpenseCategoryByIdSync(id)
    if (existing != null) {
      deleteExpenseCategory(
        name = existing.name,
        type = existing.type,
        deleteAssociatedTransactions = deleteAssociatedTransactions,
        reassignToCategory = reassignToCategory
      )
    }
    dao.deleteExpenseCategoryById(id)
  }

  suspend fun insertTransaction(transaction: TransactionEntity) = dao.insertTransaction(transaction)
  suspend fun updateTransaction(transaction: TransactionEntity) = dao.updateTransaction(transaction)
  suspend fun deleteTransaction(id: Long) = dao.deleteTransaction(id)

  suspend fun insertInvestment(investment: InvestmentEntity) = dao.insertInvestment(investment)
  suspend fun updateInvestment(investment: InvestmentEntity) = dao.updateInvestment(investment)
  suspend fun deleteInvestment(id: Long) {
    dao.deleteInvestment(id)
    dao.deleteServicesForCar(id)
    dao.deletePaymentsForCar(id)
    dao.deleteMonthlyRecordsForInvestment(id)
  }

  suspend fun insertCarService(service: CarServiceEntity) = dao.insertCarService(service)
  suspend fun updateCarService(service: CarServiceEntity) = dao.updateCarService(service)
  suspend fun deleteCarService(id: Long) = dao.deleteCarService(id)

  suspend fun insertCarPayment(payment: CarPaymentEntity) = dao.insertCarPayment(payment)
  suspend fun updateCarPayment(payment: CarPaymentEntity) = dao.updateCarPayment(payment)
  suspend fun deleteCarPayment(id: Long) = dao.deleteCarPayment(id)
  suspend fun clearAllCarPayments() = dao.clearAllCarPayments()

  suspend fun insertMonthlyRecord(record: InvestmentMonthRecordEntity) = dao.insertMonthlyRecord(record)
  suspend fun deleteMonthlyRecord(id: Long) = dao.deleteMonthlyRecord(id)

  suspend fun insertGoldHolding(holding: GoldHoldingEntity) = dao.insertGoldHolding(holding)
  suspend fun updateGoldHolding(holding: GoldHoldingEntity) = dao.updateGoldHolding(holding)
  suspend fun deleteGoldHolding(id: Long) = dao.deleteGoldHolding(id)

  suspend fun insertDebtCredit(item: DebtCreditEntity) = dao.insertDebtCredit(item)
  suspend fun updateDebtCredit(item: DebtCreditEntity) = dao.updateDebtCredit(item)
  suspend fun deleteDebtCredit(id: Long) {
    dao.deletePaymentsForDebt(id)
    dao.deleteDebtCredit(id)
  }

  suspend fun deleteDebtPayment(paymentId: Long) {
    val allPayments = dao.getAllDebtPayments().firstOrNull() ?: emptyList()
    val targetPayment = allPayments.find { it.id == paymentId }
    if (targetPayment != null) {
      val debts = dao.getAllDebtsCredits().firstOrNull() ?: emptyList()
      val parentDebt = debts.find { it.id == targetPayment.debtId }
      if (parentDebt != null) {
        val updatedPaid = (parentDebt.amountPaid - targetPayment.amount).coerceAtLeast(0.0)
        val currentCashPaid = parentDebt.effectiveCashPaidImpact
        val updatedCashPaid = if (targetPayment.affectCashBalance) {
          (currentCashPaid - targetPayment.amount).coerceAtLeast(0.0)
        } else {
          currentCashPaid
        }
        dao.updateDebtCredit(
          parentDebt.copy(
            amountPaid = updatedPaid,
            cashPaidImpactAmount = updatedCashPaid,
            isCompleted = updatedPaid >= parentDebt.totalAmount && parentDebt.totalAmount > 0.0
          )
        )
      }
    }
    dao.deleteDebtPayment(paymentId)
  }

  suspend fun clearAllDebtPayments() {
    dao.clearAllDebtPayments()
  }

  suspend fun recordDebtPayment(
    debtId: Long,
    paymentAmount: Double,
    notes: String,
    affectCashBalance: Boolean = true,
    customIqdRate: Double? = null
  ): DebtPaymentEntity? {
    val debts = dao.getAllDebtsCredits().firstOrNull() ?: emptyList()
    val targetDebt = debts.find { it.id == debtId } ?: return null
    val settings = dao.getUserSettingsSync()
    val currentRate = settings?.iqdPerUsdRate ?: 1310.0
    val entryRate = when {
      customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
      targetDebt.exchangeRateAtEntry > 0.0 -> targetDebt.exchangeRateAtEntry
      else -> currentRate
    }
    val newPaid = targetDebt.amountPaid + paymentAmount
    val isDone = newPaid >= targetDebt.totalAmount

    val allPayments = dao.getAllDebtPayments().firstOrNull() ?: emptyList()
    val existingDebtPayments = allPayments.filter { it.debtId == debtId }
    val baseCashPaid = when {
      targetDebt.cashPaidImpactAmount >= 0.0 -> targetDebt.cashPaidImpactAmount
      existingDebtPayments.isNotEmpty() -> existingDebtPayments.filter { it.affectCashBalance }.sumOf { it.amount }
      targetDebt.affectCashBalance -> targetDebt.amountPaid
      else -> 0.0
    }
    val newCashPaid = if (affectCashBalance) baseCashPaid + paymentAmount else baseCashPaid

    dao.updateDebtCredit(
      targetDebt.copy(
        amountPaid = newPaid,
        cashPaidImpactAmount = newCashPaid,
        exchangeRateAtEntry = entryRate,
        isCompleted = isDone
      )
    )

    val paymentToInsert = DebtPaymentEntity(
      debtId = debtId,
      amount = paymentAmount,
      dateMillis = System.currentTimeMillis(),
      notes = notes,
      exchangeRateAtEntry = entryRate,
      affectCashBalance = affectCashBalance
    )
    val insertedId = dao.insertDebtPayment(paymentToInsert)
    return paymentToInsert.copy(id = insertedId)
  }

  suspend fun settleDebtCreditInFull(
    debtId: Long,
    notes: String = "Full Settlement",
    affectCashBalance: Boolean = true,
    customIqdRate: Double? = null
  ): DebtPaymentEntity? {
    val debts = dao.getAllDebtsCredits().firstOrNull() ?: emptyList()
    val targetDebt = debts.find { it.id == debtId } ?: return null
    val remaining = targetDebt.remainingAmount
    if (remaining <= 0.0) {
      dao.updateDebtCredit(targetDebt.copy(isCompleted = true))
      return null
    }
    return recordDebtPayment(
      debtId = debtId,
      paymentAmount = remaining,
      notes = notes,
      affectCashBalance = affectCashBalance,
      customIqdRate = customIqdRate
    )
  }

  suspend fun insertBudget(budget: BudgetEntity) = dao.insertBudget(budget)
  suspend fun updateBudget(budget: BudgetEntity) = dao.updateBudget(budget)
  suspend fun deleteBudget(id: Long) = dao.deleteBudget(id)

  suspend fun updateSettings(settings: UserSettingsEntity) = dao.insertOrUpdateSettings(settings)

  /**
   * Locks the given IQD exchange rate onto any existing records that do not yet have
   * an explicit exchangeRateAtEntry (> 0.0), so that changing the global exchange rate
   * later never alters the USD value of previously entered IQD records.
   */
  suspend fun lockHistoricalIqdRates(rateToLock: Double) {
    if (rateToLock <= 0.0) return
    val txs = dao.getAllTransactions().firstOrNull() ?: emptyList()
    txs.filter { it.exchangeRateAtEntry <= 0.0 }.forEach { tx ->
      dao.updateTransaction(tx.copy(exchangeRateAtEntry = rateToLock))
    }
    val debts = dao.getAllDebtsCredits().firstOrNull() ?: emptyList()
    debts.filter { it.exchangeRateAtEntry <= 0.0 }.forEach { d ->
      dao.updateDebtCredit(d.copy(exchangeRateAtEntry = rateToLock))
    }
    val invs = dao.getAllInvestments().firstOrNull() ?: emptyList()
    invs.filter { it.exchangeRateAtEntry <= 0.0 }.forEach { inv ->
      dao.updateInvestment(inv.copy(exchangeRateAtEntry = rateToLock))
    }
    val golds = dao.getAllGoldHoldings().firstOrNull() ?: emptyList()
    golds.filter { it.exchangeRateAtEntry <= 0.0 }.forEach { g ->
      dao.updateGoldHolding(g.copy(exchangeRateAtEntry = rateToLock))
    }
    val budgets = dao.getAllBudgets().firstOrNull() ?: emptyList()
    budgets.filter { it.exchangeRateAtEntry <= 0.0 }.forEach { b ->
      dao.updateBudget(b.copy(exchangeRateAtEntry = rateToLock))
    }
    val settings = dao.getUserSettingsSync()
    if (settings != null && settings.capitalExchangeRateAtEntry <= 0.0 && settings.startingCapital > 0.0) {
      dao.insertOrUpdateSettings(settings.copy(capitalExchangeRateAtEntry = rateToLock))
    }
  }

  suspend fun ensureInitialized() {
    val existingSettings = dao.getUserSettingsSync()
    if (existingSettings == null) {
      val defaultSettings = UserSettingsEntity(
        id = 1,
        startingCapital = 0.0,
        capitalCurrency = "USD",
        displayCurrency = "USD",
        iqdPerUsdRate = 1310.0,
        eurPerUsdRate = 0.92,
        lastRateUpdateMillis = System.currentTimeMillis(),
        biometricEnabled = false,
        pinCode = "1234",
        isAppLocked = false,
        themeMode = "DARK",
        capitalExchangeRateAtEntry = 1310.0
      )
      dao.insertOrUpdateSettings(defaultSettings)
    } else {
      lockHistoricalIqdRates(existingSettings.iqdPerUsdRate)
    }
  }

  data class PurgedIdsResult(
    val transactionIds: List<Long> = emptyList(),
    val investmentIds: List<Long> = emptyList(),
    val carServiceIds: List<Long> = emptyList(),
    val carPaymentIds: List<Long> = emptyList(),
    val monthlyRecordIds: List<Long> = emptyList(),
    val debtCreditIds: List<Long> = emptyList(),
    val debtPaymentIds: List<Long> = emptyList(),
    val goldHoldingIds: List<Long> = emptyList(),
    val budgetIds: List<Long> = emptyList()
  )

  /**
   * Removes any previously auto-seeded demo records that were added without the user's action,
   * as well as any orphan child records whose parent car/investment/debt no longer exists.
   */
  suspend fun purgeAutoSeededAndOrphanRecords(cleanSeedData: Boolean): PurgedIdsResult {
    val purgedTxIds = mutableListOf<Long>()
    val purgedInvIds = mutableListOf<Long>()
    val purgedCarServiceIds = mutableListOf<Long>()
    val purgedCarPaymentIds = mutableListOf<Long>()
    val purgedMonthlyRecordIds = mutableListOf<Long>()
    val purgedDebtIds = mutableListOf<Long>()
    val purgedDebtPaymentIds = mutableListOf<Long>()
    val purgedGoldIds = mutableListOf<Long>()
    val purgedBudgetIds = mutableListOf<Long>()

    if (cleanSeedData) {
      val settings = dao.getUserSettingsSync()
      if (settings != null && settings.startingCapital == 25000.0) {
        dao.insertOrUpdateSettings(settings.copy(startingCapital = 0.0))
      }

      val seedTxNotes = setOf(
        "Monthly client advisory retainer",
        "Real estate rental dividend",
        "Software stack and server hosting",
        "Monthly workspace and utilities"
      )
      val txs = dao.getAllTransactions().firstOrNull() ?: emptyList()
      txs.filter { it.notes in seedTxNotes }.forEach { tx ->
        dao.deleteTransaction(tx.id)
        purgedTxIds.add(tx.id)
      }

      val seedInvNames = setOf(
        "Erbil Commercial Real Estate",
        "Global Growth Tech Index",
        "Toyota Land Cruiser Deal"
      )
      val seedInvNotes = setOf(
        "High-yield commercial lease property",
        "Diversified technology equities",
        "Co-owned partnership deal. Initial buy $20,000."
      )
      val invs = dao.getAllInvestments().firstOrNull() ?: emptyList()
      invs.filter { it.name in seedInvNames && it.notes in seedInvNotes }.forEach { inv ->
        dao.deleteInvestment(inv.id)
        dao.deleteServicesForCar(inv.id)
        dao.deletePaymentsForCar(inv.id)
        purgedInvIds.add(inv.id)
      }

      val seedDebts = setOf(
        "Karim Al-Hassan" to "Bridge loan for auto import business",
        "Tech Equipment Corp" to "Installment for hardware workstation"
      )
      val debts = dao.getAllDebtsCredits().firstOrNull() ?: emptyList()
      debts.filter { (it.personName to it.notes) in seedDebts }.forEach { debt ->
        dao.deletePaymentsForDebt(debt.id)
        dao.deleteDebtCredit(debt.id)
        purgedDebtIds.add(debt.id)
      }

      val golds = dao.getAllGoldHoldings().firstOrNull() ?: emptyList()
      golds.filter {
        it.title == "Gold Bullion (210g)" &&
          it.weightGrams == 210.0 &&
          it.buyPricePerGram == 127.0 &&
          it.notes == "Purchased 210 grams at $127/g"
      }.forEach { g ->
        dao.deleteGoldHolding(g.id)
        purgedGoldIds.add(g.id)
      }

      val budgets = dao.getAllBudgets().firstOrNull() ?: emptyList()
      budgets.filter {
        (it.category == "Monthly Spending" && it.monthlyLimit == 3000.0) ||
          (it.category == "Operations & Tools" && it.monthlyLimit == 1000.0)
      }.forEach { b ->
        dao.deleteBudget(b.id)
        purgedBudgetIds.add(b.id)
      }
    }

    // Always clean up any orphan child records whose parent entity was deleted
    val remainingInvs = dao.getAllInvestments().firstOrNull() ?: emptyList()
    val validInvIds = remainingInvs.map { it.id }.toSet()

    val allServices = dao.getAllCarServices().firstOrNull() ?: emptyList()
    allServices.filter { it.carInvestmentId !in validInvIds }.forEach { cs ->
      dao.deleteCarService(cs.id)
      purgedCarServiceIds.add(cs.id)
    }

    val allCarPayments = dao.getAllCarPayments().firstOrNull() ?: emptyList()
    allCarPayments.filter { it.carId !in validInvIds }.forEach { cp ->
      dao.deleteCarPayment(cp.id)
      purgedCarPaymentIds.add(cp.id)
    }

    val allMonthRecords = dao.getAllMonthlyRecords().firstOrNull() ?: emptyList()
    allMonthRecords.filter { it.investmentId !in validInvIds }.forEach { mr ->
      dao.deleteMonthlyRecord(mr.id)
      purgedMonthlyRecordIds.add(mr.id)
    }

    val remainingDebts = dao.getAllDebtsCredits().firstOrNull() ?: emptyList()
    val validDebtIds = remainingDebts.map { it.id }.toSet()
    val allDebtPayments = dao.getAllDebtPayments().firstOrNull() ?: emptyList()
    allDebtPayments.filter { it.debtId !in validDebtIds }.forEach { dp ->
      dao.deleteDebtPayment(dp.id)
      purgedDebtPaymentIds.add(dp.id)
    }

    return PurgedIdsResult(
      transactionIds = purgedTxIds,
      investmentIds = purgedInvIds,
      carServiceIds = purgedCarServiceIds,
      carPaymentIds = purgedCarPaymentIds,
      monthlyRecordIds = purgedMonthlyRecordIds,
      debtCreditIds = purgedDebtIds,
      debtPaymentIds = purgedDebtPaymentIds,
      goldHoldingIds = purgedGoldIds,
      budgetIds = purgedBudgetIds
    )
  }

  suspend fun purgeAllSilverHoldings(): List<Long> {
    val silverItems = dao.getAllSilverHoldingsSync()
    val ids = silverItems.map { it.id }
    dao.deleteAllSilverHoldings()
    return ids
  }
}
