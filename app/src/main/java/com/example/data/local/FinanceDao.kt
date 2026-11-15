package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

  // TRANSACTIONS
  @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateMillis DESC")
  fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransaction(id: Long)

  @Query("DELETE FROM transactions")
  suspend fun clearTransactions()

  // INVESTMENTS
  @Query("SELECT * FROM investments ORDER BY purchaseDateMillis DESC")
  fun getAllInvestments(): Flow<List<InvestmentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvestment(investment: InvestmentEntity): Long

  @Update
  suspend fun updateInvestment(investment: InvestmentEntity)

  @Query("DELETE FROM investments WHERE id = :id")
  suspend fun deleteInvestment(id: Long)

  // INVESTMENT MONTH RECORDS (FOR PROFIT TRENDS)
  @Query("SELECT * FROM investment_month_records WHERE investmentId = :invId ORDER BY yearMonth ASC")
  fun getMonthlyRecordsForInvestment(invId: Long): Flow<List<InvestmentMonthRecordEntity>>

  @Query("SELECT * FROM investment_month_records ORDER BY yearMonth ASC, timestamp ASC")
  fun getAllMonthlyRecords(): Flow<List<InvestmentMonthRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMonthlyRecord(record: InvestmentMonthRecordEntity): Long

  @Query("DELETE FROM investment_month_records WHERE id = :id")
  suspend fun deleteMonthlyRecord(id: Long)

  @Query("DELETE FROM investment_month_records WHERE investmentId = :invId")
  suspend fun deleteMonthlyRecordsForInvestment(invId: Long)

  // DEBTS & CREDITS (I DEBT / I CREDIT SOMEONE)
  @Query("SELECT * FROM debts_credits ORDER BY dueDateMillis ASC")
  fun getAllDebtsCredits(): Flow<List<DebtCreditEntity>>

  @Transaction
  @Query("SELECT * FROM debts_credits ORDER BY isCompleted ASC, dueDateMillis ASC")
  fun getAllUnifiedDebtsCreditsWithPayments(): Flow<List<UnifiedDebtCreditWithPayments>>

  @Query("SELECT * FROM debts_credits WHERE type = :type ORDER BY dueDateMillis ASC")
  fun getDebtsCreditsByType(type: String): Flow<List<DebtCreditEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDebtCredit(item: DebtCreditEntity): Long

  @Update
  suspend fun updateDebtCredit(item: DebtCreditEntity)

  @Query("DELETE FROM debts_credits WHERE id = :id")
  suspend fun deleteDebtCredit(id: Long)

  @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY dateMillis DESC")
  fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>>

  @Query("SELECT * FROM debt_payments ORDER BY dateMillis DESC")
  fun getAllDebtPayments(): Flow<List<DebtPaymentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDebtPayment(payment: DebtPaymentEntity): Long

  @Query("DELETE FROM debt_payments WHERE id = :id")
  suspend fun deleteDebtPayment(id: Long)

  @Query("DELETE FROM debt_payments WHERE debtId = :debtId")
  suspend fun deletePaymentsForDebt(debtId: Long)

  @Query("DELETE FROM debt_payments")
  suspend fun clearAllDebtPayments()

  // BUDGETS
  @Query("SELECT * FROM budgets")
  fun getAllBudgets(): Flow<List<BudgetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBudget(budget: BudgetEntity): Long

  @Update
  suspend fun updateBudget(budget: BudgetEntity)

  @Query("DELETE FROM budgets WHERE id = :id")
  suspend fun deleteBudget(id: Long)

  // USER SETTINGS
  @Query("SELECT * FROM user_settings WHERE id = 1")
  fun getUserSettings(): Flow<UserSettingsEntity?>

  @Query("SELECT * FROM user_settings WHERE id = 1")
  suspend fun getUserSettingsSync(): UserSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)

  // GOLD & SILVER HOLDINGS
  @Query("SELECT * FROM gold_holdings ORDER BY purchaseDateMillis DESC")
  fun getAllGoldHoldings(): Flow<List<GoldHoldingEntity>>

  @Query("SELECT * FROM gold_holdings WHERE UPPER(metalType) = 'SILVER'")
  suspend fun getAllSilverHoldingsSync(): List<GoldHoldingEntity>

  @Query("DELETE FROM gold_holdings WHERE UPPER(metalType) = 'SILVER'")
  suspend fun deleteAllSilverHoldings()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoldHolding(holding: GoldHoldingEntity): Long

  @Update
  suspend fun updateGoldHolding(holding: GoldHoldingEntity)

  @Query("DELETE FROM gold_holdings WHERE id = :id")
  suspend fun deleteGoldHolding(id: Long)

  // CAR SERVICES & MAINTENANCE
  @Query("SELECT * FROM car_services ORDER BY dateMillis DESC")
  fun getAllCarServices(): Flow<List<CarServiceEntity>>

  @Query("SELECT * FROM car_services WHERE carInvestmentId = :carId ORDER BY dateMillis DESC")
  fun getServicesForCar(carId: Long): Flow<List<CarServiceEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCarService(service: CarServiceEntity): Long

  @Update
  suspend fun updateCarService(service: CarServiceEntity)

  @Query("DELETE FROM car_services WHERE id = :id")
  suspend fun deleteCarService(id: Long)

  @Query("DELETE FROM car_services WHERE carInvestmentId = :carId")
  suspend fun deleteServicesForCar(carId: Long)

  // CAR PAYMENTS (MONTHLY INSTALLMENTS / REMAINING BALANCE)
  @Query("SELECT * FROM car_payments ORDER BY paymentDateMillis DESC")
  fun getAllCarPayments(): Flow<List<CarPaymentEntity>>

  @Query("SELECT * FROM car_payments WHERE carId = :carId ORDER BY paymentDateMillis DESC")
  fun getPaymentsForCar(carId: Long): Flow<List<CarPaymentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCarPayment(payment: CarPaymentEntity): Long

  @Update
  suspend fun updateCarPayment(payment: CarPaymentEntity)

  @Query("DELETE FROM car_payments WHERE id = :id")
  suspend fun deleteCarPayment(id: Long)

  @Query("DELETE FROM car_payments WHERE carId = :carId")
  suspend fun deletePaymentsForCar(carId: Long)

  @Query("DELETE FROM car_payments")
  suspend fun clearAllCarPayments()

  // EXPENSE & INCOME CUSTOM CATEGORIES
  @Query("SELECT * FROM expense_categories ORDER BY createdAtMillis ASC, id ASC")
  fun getAllExpenseCategories(): Flow<List<ExpenseCategoryEntity>>

  @Query("SELECT * FROM expense_categories WHERE type = :type ORDER BY createdAtMillis ASC, id ASC")
  fun getExpenseCategoriesByType(type: String): Flow<List<ExpenseCategoryEntity>>

  @Query("SELECT * FROM expense_categories ORDER BY createdAtMillis ASC, id ASC")
  suspend fun getAllExpenseCategoriesSync(): List<ExpenseCategoryEntity>

  @Query("SELECT * FROM expense_categories WHERE id = :id LIMIT 1")
  suspend fun getExpenseCategoryByIdSync(id: Long): ExpenseCategoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenseCategory(category: ExpenseCategoryEntity): Long

  @Query("DELETE FROM expense_categories WHERE id = :id")
  suspend fun deleteExpenseCategoryById(id: Long)

  @Query("DELETE FROM expense_categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND UPPER(TRIM(type)) = UPPER(TRIM(:type))")
  suspend fun deleteExpenseCategoryByNameAndType(name: String, type: String)

  @Query("UPDATE transactions SET category = :newCategory WHERE LOWER(TRIM(category)) = LOWER(TRIM(:oldCategory)) AND UPPER(TRIM(type)) = UPPER(TRIM(:type))")
  suspend fun reassignTransactionsCategory(oldCategory: String, newCategory: String, type: String)

  @Query("DELETE FROM transactions WHERE LOWER(TRIM(category)) = LOWER(TRIM(:category)) AND UPPER(TRIM(type)) = UPPER(TRIM(:type))")
  suspend fun deleteTransactionsByCategory(category: String, type: String)

  @Query("UPDATE budgets SET category = :newCategory WHERE LOWER(TRIM(category)) = LOWER(TRIM(:oldCategory))")
  suspend fun reassignBudgetsCategory(oldCategory: String, newCategory: String)

  @Query("DELETE FROM budgets WHERE LOWER(TRIM(category)) = LOWER(TRIM(:category))")
  suspend fun deleteBudgetsByCategory(category: String)
}
