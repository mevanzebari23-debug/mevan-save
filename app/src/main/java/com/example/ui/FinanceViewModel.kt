package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiSearchGroundingService
import com.example.data.ai.GroundedSearchResponse
import com.example.data.backup.CloudBackupService
import com.example.data.backup.ReportExportService
import com.example.data.currency.CurrencyService
import com.example.data.currency.GoldPriceQuote
import com.example.data.currency.OnlineGoldService
import com.example.data.firebase.FirebaseAuthUserState
import com.example.data.firebase.FirebaseDataManager
import com.example.data.firebase.FirestoreSyncState
import com.example.data.network.NetworkConnectivityObserver
import com.example.data.sync.OfflineSyncManager
import com.example.data.local.AppDatabase
import com.example.data.local.ActiveDebtCreditLoan
import com.example.data.local.BudgetEntity
import com.example.data.local.CarPaymentEntity
import com.example.data.local.CarServiceEntity
import com.example.data.local.DebtCreditEntity
import com.example.data.local.DebtPaymentEntity
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.InvestmentMonthRecordEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UnifiedCapitalDebtSummary
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.FinanceRepository
import com.example.data.security.BiometricSecurityManager
import com.example.ui.components.FinancialLogCategory
import com.example.ui.components.FinancialLogEntry
import com.example.ui.history.ActionOperationType
import com.example.ui.history.ActionPayloadSnapshot
import com.example.ui.history.ActionSection
import com.example.ui.history.QuickEditableTarget
import com.example.ui.history.ReversibleAction
import com.example.ui.intelligence.BudgetRecommendation
import com.example.ui.intelligence.DebtPayoffStep
import com.example.ui.intelligence.DebtPayoffStrategy
import com.example.ui.intelligence.FinancialIntelligenceEngine
import com.example.ui.intelligence.SmartIntelligenceSummary
import com.example.data.weather.LiveWeatherData
import com.example.data.weather.WeatherConditionType
import com.example.data.weather.WeatherService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FinanceUiState(
  val settings: UserSettingsEntity = UserSettingsEntity(),
  val transactions: List<TransactionEntity> = emptyList(),
  val investments: List<InvestmentEntity> = emptyList(),
  val monthlyRecords: List<InvestmentMonthRecordEntity> = emptyList(),
  val debtsCredits: List<DebtCreditEntity> = emptyList(),
  val debtPayments: List<DebtPaymentEntity> = emptyList(),
  val budgets: List<BudgetEntity> = emptyList(),
  val goldHoldings: List<GoldHoldingEntity> = emptyList(),
  val carServices: List<CarServiceEntity> = emptyList(),
  val carPayments: List<CarPaymentEntity> = emptyList(),
  val goldQuote: GoldPriceQuote = OnlineGoldService.createFallbackQuote(
    customPricePerGram = OnlineGoldService.BASELINE_PRICE_PER_GRAM,
    customSilverPricePerGram9999 = OnlineGoldService.BASELINE_SILVER_PRICE_PER_GRAM_9999,
    isOnline = true
  ),
  val simulatedGoldPricePerGram: Double? = null,
  val simulatedSilverPricePerGram: Double? = null,
  val isFetchingGold: Boolean = false,
  val displayCurrency: String = "USD",
  val isAppLocked: Boolean = false,
  val isSensitiveDataMasked: Boolean = false,
  val isSyncing: Boolean = false,
  val syncMessage: String = "All data stored offline & cloud synced",
  val exchangeRateMessage: String = "1 USD = 1,310 IQD (Persistent Rate)",
  val selectedTab: Int = 0,
  val firebaseAuthUser: FirebaseAuthUserState = FirebaseAuthUserState(),
  val firestoreSyncState: FirestoreSyncState = FirestoreSyncState(),
  val isSearchGroundingLoading: Boolean = false,
  val lastGroundedSearchResult: GroundedSearchResponse? = null,
  val focusedEntityType: String? = null,
  val focusedEntityId: Long? = null,
  val actionHistory: List<ReversibleAction> = emptyList(),
  val redoStack: List<ReversibleAction> = emptyList(),
  val activeUndoBannerAction: ReversibleAction? = null,
  val quickEditTarget: QuickEditableTarget? = null,
  val isHistorySheetOpen: Boolean = false,
  val debtPayoffStrategy: DebtPayoffStrategy = DebtPayoffStrategy.AVALANCHE,
  val expenseCategories: List<ExpenseCategoryEntity> = emptyList(),
  val customExpenseCategories: List<String> = emptyList(),
  val customIncomeCategories: List<String> = emptyList(),
  val weatherData: LiveWeatherData = LiveWeatherData(),
  val isNetworkOnline: Boolean = true,
  val pendingOfflineMutations: Int = 0,
  val showAutoSyncSuccessBanner: Boolean = false,
  val lastSuccessfulSyncMillis: Long = 0L,
  val offlineStatusMessage: String = "Ready"
) {
  val iqdRate: Double get() = settings.iqdPerUsdRate
  
  // Master Global Capital - connected to every project and asset in the app
  val masterCapital: Double get() = CurrencyService.convert(
    settings.startingCapital,
    settings.capitalCurrency,
    displayCurrency,
    iqdRate,
    entryIqdRate = settings.capitalExchangeRateAtEntry
  )
  val startingCapitalDisplay: Double get() = masterCapital

  val baseTransactionsIn: Double get() = transactions
    .filter { it.type == "IN" && it.affectsCapital }
    .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Repayments collected on CREDIT (دەف خەلکی) vs Repayments paid on DEBT (لسەر من)
  val totalCreditRepaymentsCollected: Double get() = debtsCredits
    .filter { it.type == "CREDIT" && it.effectiveCashPaidImpact > 0.0 }
    .sumOf { CurrencyService.convert(it.effectiveCashPaidImpact, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalDebtRepaymentsPaid: Double get() = debtsCredits
    .filter { it.type == "DEBT" && it.effectiveCashPaidImpact > 0.0 }
    .sumOf { CurrencyService.convert(it.effectiveCashPaidImpact, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Net cash impact of debt/credit repayments on Income (داهات):
  // Collecting credit increases Income (+), paying back debt decreases Income (-)
  val netDebtCreditCashImpact: Double get() =
    totalCreditRepaymentsCollected - totalDebtRepaymentsPaid

  // Realized cash from Cars: when a car is sold (وەختێ فرۆتن) or when an installment comes in (قستەک هات)
  fun getCarRealizedCashForMe(car: InvestmentEntity): Double {
    val paidInstallments = getCarMyPaidTotal(car)
    return if (car.isSold) {
      val targetSalePrice = if (car.soldPrice > 0.0) car.soldPrice else car.currentValue
      val mySaleShare = if (car.isCoOwned) {
        targetSalePrice * (car.mySharePercent / 100.0)
      } else {
        targetSalePrice
      }
      val mySaleShareConverted = CurrencyService.convert(mySaleShare, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
      maxOf(mySaleShareConverted, paidInstallments)
    } else {
      paidInstallments
    }
  }

  val totalCarSalesAndInstallmentsCashIn: Double get() {
    val fromCarEntities = carInvestments.sumOf { getCarRealizedCashForMe(it) }
    val knownCarIds = carInvestments.map { it.id }.toSet()
    val orphanPayments = carPayments
      .filter { it.carId !in knownCarIds }
      .sumOf { CurrencyService.convert(it.effectiveMyShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
    return fromCarEntities + orphanPayments
  }

  // Realized cash from selling Real Estate & non-car projects (وەختێ فرۆتنا عەقار یان پرۆژە)
  fun getNonCarRealizedSaleCashForMe(inv: InvestmentEntity): Double {
    if (!inv.isSold) return 0.0
    if (!inv.deductedFromCash) return 0.0
    val targetSalePrice = if (inv.soldPrice > 0.0) inv.soldPrice else inv.currentValue
    val mySaleShare = if (inv.isCoOwned) {
      targetSalePrice * (inv.mySharePercent / 100.0)
    } else {
      targetSalePrice
    }
    return CurrencyService.convert(mySaleShare, inv.currency, displayCurrency, iqdRate, entryIqdRate = inv.exchangeRateAtEntry)
  }

  val totalNonCarSalesCashIn: Double get() = nonCarInvestments
    .filter { it.isSold }
    .sumOf { getNonCarRealizedSaleCashForMe(it) }

  // Realized cash from selling Gold or Silver (وەختێ فرۆتنا زێڕ یان زیڤ)
  fun getGoldSilverRealizedSaleCash(holding: GoldHoldingEntity): Double {
    if (!holding.isSold) return 0.0
    val targetSaleUsd = if (holding.soldPriceUsd > 0.0) {
      holding.soldPriceUsd
    } else {
      holding.currentValue(effectiveGoldPricePerGram24K, effectiveSilverPricePerGram9999)
    }
    return CurrencyService.convert(targetSaleUsd, holding.currency, displayCurrency, iqdRate, entryIqdRate = holding.exchangeRateAtEntry)
  }

  val totalGoldSilverSalesCashIn: Double get() = soldGoldHoldings
    .sumOf { getGoldSilverRealizedSaleCash(it) }

  val totalAssetSalesAndInstallmentsCashIn: Double get() =
    totalCarSalesAndInstallmentsCashIn + totalNonCarSalesCashIn + totalGoldSilverSalesCashIn

  // Post-today asset purchases deducted from Cash Wealth (بشتی ئەڤرۆ هەر سەیارەک یان عەقارەک یان زێڕ و زیڤ بکررم ل سەروەتێ کاشی بهێتە کێمکرن)
  val totalCarPurchasesCashOut: Double get() = carInvestments
    .filter { it.deductedFromCash }
    .sumOf { CurrencyService.convert(it.myEffectiveCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalNonCarPurchasesCashOut: Double get() = nonCarInvestments
    .filter { it.deductedFromCash }
    .sumOf { CurrencyService.convert(it.myEffectiveCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalGoldSilverPurchasesCashOut: Double get() = goldHoldings
    .filter { it.deductedFromCash }
    .sumOf { CurrencyService.convert(it.totalCost, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Loans given (CREDIT) deducted from Cash & Loans borrowed (DEBT) added to Cash using incremental effectiveCashPrincipalImpact
  val totalLoansBorrowedCashIn: Double get() = debtsCredits
    .filter { it.type == "DEBT" && it.effectiveCashPrincipalImpact > 0.0 }
    .sumOf { CurrencyService.convert(it.effectiveCashPrincipalImpact, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalLoansLentCashOut: Double get() = debtsCredits
    .filter { it.type == "CREDIT" && it.effectiveCashPrincipalImpact > 0.0 }
    .sumOf { CurrencyService.convert(it.effectiveCashPrincipalImpact, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalPostTodayAssetPurchasesOut: Double get() =
    totalCarPurchasesCashOut + totalNonCarPurchasesCashOut + totalGoldSilverPurchasesCashOut

  val totalAssetPurchasesCashOut: Double get() = totalPostTodayAssetPurchasesOut
  val totalAssetSalesCashIn: Double get() = totalAssetSalesAndInstallmentsCashIn

  val totalMoneyIn: Double get() =
    baseTransactionsIn + totalAssetSalesAndInstallmentsCashIn + netDebtCreditCashImpact + totalLoansBorrowedCashIn

  val baseTransactionsOut: Double get() = transactions
    .filter { it.type == "OUT" && it.affectsCapital }
    .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Dedicated School & Education Account (قوتابخانە ژ مەسروفاتان جدا بیت و هەمی حساب ببیتە یەک)
  fun isSchoolTransaction(tx: TransactionEntity): Boolean {
    val cat = tx.category.trim()
    return cat == "قوتابخانە" ||
      cat.equals("School & Education", ignoreCase = true) ||
      cat.equals("School", ignoreCase = true) ||
      cat.startsWith("قوتابخانە") ||
      cat.startsWith("School:", ignoreCase = true) ||
      cat.contains("قوتابخانە") ||
      cat.contains("School", ignoreCase = true)
  }

  val schoolTransactions: List<TransactionEntity> get() = transactions
    .filter { it.type == "OUT" && isSchoolTransaction(it) }
    .sortedByDescending { it.dateMillis }

  val generalOutTransactions: List<TransactionEntity> get() = transactions
    .filter { it.type == "OUT" && !isSchoolTransaction(it) }
    .sortedByDescending { it.dateMillis }

  val totalSchoolSpendingDisplay: Double get() = schoolTransactions
    .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalSchoolSpendingUsd: Double get() = schoolTransactions
    .sumOf { CurrencyService.convert(it.amount, it.currency, "USD", iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalSchoolSpendingIqd: Double get() = schoolTransactions
    .sumOf { CurrencyService.convert(it.amount, it.currency, "IQD", iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val schoolDeductedFromCashDisplay: Double get() = schoolTransactions
    .filter { it.affectsCapital }
    .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val schoolNotDeductedFromCashDisplay: Double get() = schoolTransactions
    .filter { !it.affectsCapital }
    .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val generalExpensesOutDisplay: Double get() = generalOutTransactions
    .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalMoneyOut: Double get() =
    baseTransactionsOut + totalPostTodayAssetPurchasesOut + totalLoansLentCashOut

  // سەروەتێ کاشی (Pure Cash Wealth — directly reduced when buying Car/Real Estate/Gold/Silver and increased when selling)
  val pureCashBalance: Double get() = startingCapitalDisplay + totalMoneyIn - totalMoneyOut

  // Gold & Silver (زێڕ و زیڤ) active holdings + Pure Cash Wealth
  val netCapital: Double get() = pureCashBalance + totalGoldCurrentValueDisplay

  // Total Investment Portfolio
  val totalInvested: Double get() = investments
    .sumOf { CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalInvestmentValue: Double get() = investments
    .sumOf { CurrencyService.convert(it.currentValue, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  val totalInvestmentProfit: Double get() = totalInvestmentValue - totalInvested

  val investmentRoiPercent: Double get() = if (totalInvested > 0) (totalInvestmentProfit / totalInvested) * 100.0 else 0.0

  // Cars Specific Investment Tracking
  val carInvestments: List<InvestmentEntity> get() = investments.filter { it.isCar }
  val totalInvestedInCars: Double get() = carInvestments.sumOf { CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalCarValuation: Double get() = carInvestments.sumOf { CurrencyService.convert(it.currentValue, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalCarProfit: Double get() = totalCarValuation - totalInvestedInCars
  val carRoiPercent: Double get() = if (totalInvestedInCars > 0) (totalCarProfit / totalInvestedInCars) * 100.0 else 0.0

  // Partner Co-Owned Cars vs Solo Cars
  val partnerCars: List<InvestmentEntity> get() = carInvestments.filter { it.isCoOwned }
  val soloCars: List<InvestmentEntity> get() = carInvestments.filter { !it.isCoOwned }

  // Effective My Capital Invested in Cars (accounting for partner co-investment)
  val myInvestedInCars: Double get() = carInvestments.sumOf {
    val cap = if (it.isCoOwned && it.myInvestmentAmount > 0) it.myInvestmentAmount else it.initialCapital
    CurrencyService.convert(cap, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val partnerInvestedInCars: Double get() = partnerCars.sumOf {
    CurrencyService.convert(it.partnerInvestmentAmount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val myCarValuationShare: Double get() = carInvestments.sumOf {
    val valShare = if (it.isCoOwned) it.currentValue * (it.mySharePercent / 100.0) else it.currentValue
    CurrencyService.convert(valShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val partnerCarValuationShare: Double get() = partnerCars.sumOf {
    CurrencyService.convert(it.currentValue * (it.partnerSharePercent / 100.0), it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }

  // Car Services & Maintenance Spending (Oil Change, Polishing, Repairs)
  val totalCarServicesCost: Double get() = carServices.sumOf {
    CurrencyService.convert(it.cost, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalCarServicesPaidByMe: Double get() = carServices.sumOf {
    val converted = CurrencyService.convert(it.cost, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    when (it.paidBy) {
      "ME" -> converted
      "SPLIT" -> converted / 2.0
      else -> 0.0
    }
  }
  val totalCarServicesPaidByPartner: Double get() = carServices.sumOf {
    val converted = CurrencyService.convert(it.cost, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    when (it.paidBy) {
      "PARTNER" -> converted
      "SPLIT" -> converted / 2.0
      else -> 0.0
    }
  }
  // If positive: partner owes me. If negative: I owe partner for service expenses.
  val partnerMaintenanceBalance: Double get() = totalCarServicesPaidByMe - totalCarServicesPaidByPartner

  // Car Sold & Active Filtering
  val activeCarInvestments: List<InvestmentEntity> get() = carInvestments.filter { !it.isSold }
  val soldCarInvestments: List<InvestmentEntity> get() = carInvestments.filter { it.isSold }
  val activePartnerCars: List<InvestmentEntity> get() = partnerCars.filter { !it.isSold }
  val soldPartnerCars: List<InvestmentEntity> get() = partnerCars.filter { it.isSold }
  val totalSoldPartnerCarsProfitLoss: Double get() = soldPartnerCars.sumOf { getCarNetProfitLoss(it) }
  val totalSoldPartnerCarsRevenue: Double get() = soldPartnerCars.sumOf { getCarTotalRevenue(it) }
  val totalSoldPartnerCarsCost: Double get() = soldPartnerCars.sumOf { getCarTotalCost(it) }
  val mySoldPartnerCarsProfitShare: Double get() = soldPartnerCars.sumOf { getCarMyProfitLossShare(it) }
  val partnerSoldPartnerCarsProfitShare: Double get() = soldPartnerCars.sumOf { getCarPartnerProfitLossShare(it) }

  // Car Monthly Payments & Remaining Money
  fun getCarMonthlyPayments(carId: Long): List<CarPaymentEntity> =
    carPayments.filter { it.carId == carId }.sortedByDescending { it.paymentDateMillis }

  fun getCarMonthlyPaidTotal(carId: Long): Double =
    carPayments.filter { it.carId == carId }
      .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Target total price the buyer owes
  fun getCarTotalPrice(car: InvestmentEntity): Double {
    val target = if (car.soldPrice > 0) car.soldPrice else car.initialCapital
    return CurrencyService.convert(target, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
  }

  // Total paid by the buyer towards this car (installments recorded)
  fun getCarTotalPaid(car: InvestmentEntity): Double =
    getCarMonthlyPaidTotal(car.id)

  fun getCarDownPayment(car: InvestmentEntity): Double = 0.0

  // Installment Car Helpers: Sold on installment, Total Paid (واصل), Remaining (باقی)
  fun getCarRemainingMoney(car: InvestmentEntity): Double {
    val target = if (car.soldPrice > 0) car.soldPrice else 0.0
    if (target <= 0.0) return 0.0
    val targetConverted = CurrencyService.convert(target, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
    val totalPaid = getCarTotalPaid(car)
    return maxOf(0.0, targetConverted - totalPaid)
  }

  fun isCarSoldOnInstallments(car: InvestmentEntity): Boolean = car.soldPrice > 0
  fun isCarInstallmentsCompleted(car: InvestmentEntity): Boolean = car.soldPrice > 0 && getCarRemainingMoney(car) <= 0.001

  fun getCarMyPaidTotal(car: InvestmentEntity): Double =
    carPayments.filter { it.carId == car.id }
      .sumOf { CurrencyService.convert(it.effectiveMyShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  fun getCarPartnerPaidTotal(car: InvestmentEntity): Double {
    if (!car.isCoOwned) return 0.0
    return carPayments.filter { it.carId == car.id }
      .sumOf { CurrencyService.convert(it.effectivePartnerShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  }

  fun getCarMyDownPayment(car: InvestmentEntity): Double {
    val myCap = if (car.isCoOwned) {
      if (car.myInvestmentAmount > 0) car.myInvestmentAmount else car.initialCapital * (car.mySharePercent / 100.0)
    } else {
      car.initialCapital
    }
    return CurrencyService.convert(myCap, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
  }

  fun getCarPartnerDownPayment(car: InvestmentEntity): Double {
    if (!car.isCoOwned) return 0.0
    val partnerCap = if (car.partnerInvestmentAmount > 0) car.partnerInvestmentAmount else car.initialCapital * (car.partnerSharePercent / 100.0)
    return CurrencyService.convert(partnerCap, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
  }

  fun getCarMyMonthlyPaidShare(carId: Long): Double =
    carPayments.filter { it.carId == carId }
      .sumOf { CurrencyService.convert(it.effectiveMyShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  fun getCarPartnerMonthlyPaidShare(carId: Long): Double =
    carPayments.filter { it.carId == carId }
      .sumOf { CurrencyService.convert(it.effectivePartnerShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  fun getCarServicesForCar(carId: Long): List<CarServiceEntity> =
    carServices.filter { it.carInvestmentId == carId }.sortedByDescending { it.dateMillis }

  fun getCarTotalSpending(carId: Long): Double =
    carServices.filter { it.carInvestmentId == carId }
      .sumOf { CurrencyService.convert(it.cost, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Winning and Losing (Profit / Loss) Calculations for Cars & Partner Cars
  fun getCarTotalCost(car: InvestmentEntity): Double {
    val initialCap = CurrencyService.convert(car.initialCapital, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
    val spending = getCarTotalSpending(car.id)
    return initialCap + spending
  }

  fun getCarTotalRevenue(car: InvestmentEntity): Double {
    return if (car.isSold) {
      CurrencyService.convert(car.soldPrice, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
    } else {
      CurrencyService.convert(car.currentValue, car.currency, displayCurrency, iqdRate, entryIqdRate = car.exchangeRateAtEntry)
    }
  }

  fun getCarNetProfitLoss(car: InvestmentEntity): Double {
    return getCarTotalRevenue(car) - getCarTotalCost(car)
  }

  fun isCarWinning(car: InvestmentEntity): Boolean {
    return getCarNetProfitLoss(car) >= 0.0
  }

  fun getCarRoiPercent(car: InvestmentEntity): Double {
    val cost = getCarTotalCost(car)
    return if (cost > 0) (getCarNetProfitLoss(car) / cost) * 100.0 else 0.0
  }

  fun getCarMyProfitLossShare(car: InvestmentEntity): Double {
    val net = getCarNetProfitLoss(car)
    return if (car.isCoOwned) net * (car.mySharePercent / 100.0) else net
  }

  fun getCarPartnerProfitLossShare(car: InvestmentEntity): Double {
    val net = getCarNetProfitLoss(car)
    return if (car.isCoOwned) net * (car.partnerSharePercent / 100.0) else 0.0
  }

  val totalSoldCarsProfitLoss: Double get() = soldCarInvestments.sumOf { getCarNetProfitLoss(it) }
  val totalSoldCarsCost: Double get() = soldCarInvestments.sumOf { getCarTotalCost(it) }
  val totalSoldCarsRevenue: Double get() = soldCarInvestments.sumOf { getCarTotalRevenue(it) }
  val isFleetWinning: Boolean get() = totalSoldCarsProfitLoss >= 0.0

  val totalCarsRemainingMoney: Double get() = carInvestments.sumOf { getCarRemainingMoney(it) }
  val totalPartnerCarsCustomerRemaining: Double get() = partnerCars.sumOf { getCarRemainingMoney(it) }

  // Car Installments Remaining: ONLY MY SHARE without partner (تنێ پشکا من بێ شەریک)
  fun getCarMyRemainingMoney(car: InvestmentEntity): Double {
    val totalRemaining = getCarRemainingMoney(car)
    if (totalRemaining <= 0.0) return 0.0
    return if (car.isCoOwned) {
      totalRemaining * (car.mySharePercent / 100.0)
    } else {
      totalRemaining
    }
  }

  val totalCarsMyRemainingMoney: Double
    get() = carInvestments.sumOf { getCarMyRemainingMoney(it) }

  val myActiveCarValuationShare: Double
    get() = activeCarInvestments.sumOf {
      val valShare = if (it.isCoOwned) it.currentValue * (it.mySharePercent / 100.0) else it.currentValue
      CurrencyService.convert(valShare, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }

  val totalInstallmentCarsCount: Int get() = carInvestments.count { isCarSoldOnInstallments(it) && !isCarInstallmentsCompleted(it) }
  val totalCarsCashInvested: Double get() = carInvestments.sumOf { getCarTotalCost(it) }
  val totalActiveCarsCashInvested: Double get() = activeCarInvestments.sumOf { getCarTotalCost(it) }
  val totalActiveCarsValuation: Double get() = activeCarInvestments.sumOf {
    CurrencyService.convert(it.currentValue, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalPartnerCarsInvested: Double get() = partnerCars.sumOf {
    CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val myTotalPartnerCarsCapital: Double get() = partnerCars.sumOf {
    val amount = if (it.myInvestmentAmount > 0) it.myInvestmentAmount else it.initialCapital * (it.mySharePercent / 100.0)
    CurrencyService.convert(amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val partnerTotalPartnerCarsCapital: Double get() = partnerCars.sumOf {
    val amount = if (it.partnerInvestmentAmount > 0) it.partnerInvestmentAmount else it.initialCapital * (it.partnerSharePercent / 100.0)
    CurrencyService.convert(amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val totalPartnerCarsCustomerPaid: Double get() = partnerCars.sumOf { getCarMonthlyPaidTotal(it.id) }
  val totalCarsTotalPaid: Double get() = carInvestments.sumOf { getCarTotalPaid(it) }
  val totalCarsMonthlyPaid: Double get() = carInvestments.sumOf { getCarMonthlyPaidTotal(it.id) }

  // Other Projects (Excluding Cars)
  val nonCarInvestments: List<InvestmentEntity> get() = investments.filter { !it.isCar }
  val activeNonCarInvestments: List<InvestmentEntity> get() = nonCarInvestments.filter { !it.isSold }
  val soldNonCarInvestments: List<InvestmentEntity> get() = nonCarInvestments.filter { it.isSold }
  val totalInvestedInOtherProjects: Double get() = activeNonCarInvestments.sumOf { CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalOtherProjectsValuation: Double get() = activeNonCarInvestments.sumOf { CurrencyService.convert(it.currentValue, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalOtherProjectsProfit: Double get() = totalOtherProjectsValuation - totalInvestedInOtherProjects
  val otherProjectsRoiPercent: Double get() = if (totalInvestedInOtherProjects > 0) (totalOtherProjectsProfit / totalInvestedInOtherProjects) * 100.0 else 0.0

  val realEstateInvestments: List<InvestmentEntity> get() = nonCarInvestments.filter { it.isRealEstate }
  val activeRealEstateInvestments: List<InvestmentEntity> get() = activeNonCarInvestments.filter { it.isRealEstate }
  val totalInvestedInRealEstate: Double get() = activeRealEstateInvestments.sumOf { CurrencyService.convert(it.myEffectiveCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalRealEstateValuation: Double get() = activeRealEstateInvestments.sumOf { CurrencyService.convert(it.currentValue, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalPartnerShareInRealEstate: Double get() = activeRealEstateInvestments
    .filter { it.isCoOwned }
    .sumOf {
      val partnerAmount = when {
        it.isSold && it.soldPrice > 0 -> it.soldPrice * (it.partnerSharePercent / 100.0)
        it.currentValue > 0 -> it.currentValue * (it.partnerSharePercent / 100.0)
        it.partnerInvestmentAmount > 0 -> it.partnerInvestmentAmount
        else -> it.initialCapital * (it.partnerSharePercent / 100.0)
      }
      CurrencyService.convert(partnerAmount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  val myRealEstateValuation: Double get() = activeRealEstateInvestments.sumOf { CurrencyService.convert(it.myCurrentValuation, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }

  // Sub-categories of Real Estate (Houses, Lands, Commercial Buildings, Gold Valuables)
  val realEstateHousesInvestments: List<InvestmentEntity> get() = activeRealEstateInvestments.filter { it.isHouse }
  val realEstateLandsInvestments: List<InvestmentEntity> get() = activeRealEstateInvestments.filter { it.isLand }
  val realEstateCommercialInvestments: List<InvestmentEntity> get() = activeRealEstateInvestments.filter { it.isCommercial }
  val goldValuablesInvestments: List<InvestmentEntity> get() = activeNonCarInvestments.filter { it.isGoldValuable }

  val realEstateHousesValuation: Double get() = realEstateHousesInvestments.sumOf {
    CurrencyService.convert(it.myCurrentValuation, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val realEstateLandsValuation: Double get() = realEstateLandsInvestments.sumOf {
    CurrencyService.convert(it.myCurrentValuation, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val realEstateCommercialValuation: Double get() = realEstateCommercialInvestments.sumOf {
    CurrencyService.convert(it.myCurrentValuation, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
  }
  val goldValuablesInvestmentsValuation: Double get() = goldValuablesInvestments.sumOf {
    if (it.goldEffectiveWeightGrams(effectiveGoldPricePerGram24K) > 0.0) {
      val usdVal = it.goldLiveValuationUsd(effectiveGoldPricePerGram24K)
      CurrencyService.convert(usdVal, "USD", displayCurrency, iqdRate)
    } else {
      CurrencyService.convert(it.myCurrentValuation, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  }

  // Credits owed by others in Private Account (قەرزی من ل دەڤ خەلکی) - تنێ قەرزێن حسابا تایبەت و قەرزێ عام ب چ ئاوایان ناهێتە تێکەڵکرن
  val totalPrivateAccountCreditsReceivable: Double
    get() = totalRealEstateCreditsReceivable

  // Total Combined Real Estate & Standalone Asset Wealth: Properties + Real Estate Gold (Live World Spot Valuation) + Private Credits Owed by Others (قەرزی من ل دەڤ خەلکی یێ حسابا تایبەت تنێ)
  val totalIndependentWealth: Double
    get() = myRealEstateValuation + goldValuablesInvestmentsValuation + totalPrivateAccountCreditsReceivable

  // Net Standalone Real Estate Worth: Properties + Private Credits (money lent out) - Private Debts (money owed on real estate/private account)
  val totalNetIndependentWealth: Double get() =
    (totalIndependentWealth - totalRealEstateDebtsPayable).coerceAtLeast(0.0)

  // Total Standalone Real Estate Debts Payable (تنێ قەرزێن لسەر ملوکان د حسابا تایبەت دا)
  val totalIndependentRealEstateDebt: Double get() = totalRealEstateDebtsPayable

  // Ventures (Commercial ventures, stocks, startups) - 100% separate from Real Estate & Gold
  val ventureInvestments: List<InvestmentEntity> get() = nonCarInvestments.filter { !it.isRealEstate && !it.isGoldValuable }
  val activeVentureInvestments: List<InvestmentEntity> get() = activeNonCarInvestments.filter { !it.isRealEstate && !it.isGoldValuable }
  val totalInvestedInVentures: Double get() = activeVentureInvestments.sumOf { CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalVenturesValuation: Double get() = activeVentureInvestments.sumOf { CurrencyService.convert(it.currentValue, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
  val totalPartnerShareInVentures: Double get() = activeVentureInvestments
    .filter { it.isCoOwned }
    .sumOf {
      val partnerAmount = when {
        it.isSold && it.soldPrice > 0 -> it.soldPrice * (it.partnerSharePercent / 100.0)
        it.currentValue > 0 -> it.currentValue * (it.partnerSharePercent / 100.0)
        it.partnerInvestmentAmount > 0 -> it.partnerInvestmentAmount
        else -> it.initialCapital * (it.partnerSharePercent / 100.0)
      }
      CurrencyService.convert(partnerAmount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
  val myVenturesValuation: Double get() =
    (totalVenturesValuation - totalPartnerShareInVentures).coerceAtLeast(0.0)

  // Live / Effective Gold & Silver 9999 Prices
  val effectiveGoldPricePerGram24K: Double
    get() = simulatedGoldPricePerGram ?: goldQuote.pricePerGram24K

  val effectiveSilverPricePerGram9999: Double
    get() = simulatedSilverPricePerGram ?: goldQuote.silverPricePerGram9999

  val effectiveSilverPricePerKg9999: Double
    get() = effectiveSilverPricePerGram9999 * 1000.0

  val effectiveSilverSpotOzUsd: Double
    get() = effectiveSilverPricePerGram9999 * OnlineGoldService.GRAMS_PER_TROY_OUNCE

  val activeGoldHoldings: List<GoldHoldingEntity> get() = goldHoldings.filter { !it.isSold }
  val soldGoldHoldings: List<GoldHoldingEntity> get() = goldHoldings.filter { it.isSold }
  val goldOnlyHoldings: List<GoldHoldingEntity> get() = activeGoldHoldings.filter { it.isGold }
  val silverHoldings: List<GoldHoldingEntity> get() = activeGoldHoldings.filter { it.isSilver }

  val totalGoldWeightGrams: Double get() = goldOnlyHoldings.sumOf { it.weightGrams }
  val totalSilverWeightGrams: Double get() = silverHoldings.sumOf { it.weightGrams }

  val totalGoldOnlyCostUsd: Double get() = goldOnlyHoldings.sumOf { it.totalCost }
  val totalGoldOnlyValueUsd: Double get() = goldOnlyHoldings.sumOf { it.currentValue(effectiveGoldPricePerGram24K, effectiveSilverPricePerGram9999) }

  val totalGoldOnlyCostDisplay: Double get() = CurrencyService.convert(totalGoldOnlyCostUsd, "USD", displayCurrency, iqdRate)
  val totalGoldOnlyValueDisplay: Double get() = CurrencyService.convert(totalGoldOnlyValueUsd, "USD", displayCurrency, iqdRate)
  val totalGoldOnlyProfitLossUsd: Double get() = totalGoldOnlyValueUsd - totalGoldOnlyCostUsd
  val totalGoldOnlyProfitLossDisplay: Double get() = CurrencyService.convert(totalGoldOnlyProfitLossUsd, "USD", displayCurrency, iqdRate)
  val totalGoldOnlyRoiPercent: Double get() = if (totalGoldOnlyCostUsd > 0) (totalGoldOnlyProfitLossUsd / totalGoldOnlyCostUsd) * 100.0 else 0.0

  // Silver 9999 Specific Portfolio
  val totalSilverCostUsd: Double get() = silverHoldings.sumOf { it.totalCost }
  val totalSilverValueUsd: Double get() = silverHoldings.sumOf { it.currentValue(effectiveGoldPricePerGram24K, effectiveSilverPricePerGram9999) }

  val totalSilverCostDisplay: Double get() = CurrencyService.convert(totalSilverCostUsd, "USD", displayCurrency, iqdRate)
  val totalSilverValueDisplay: Double get() = CurrencyService.convert(totalSilverValueUsd, "USD", displayCurrency, iqdRate)
  val totalSilverProfitLossUsd: Double get() = totalSilverValueUsd - totalSilverCostUsd
  val totalSilverProfitLossDisplay: Double get() = CurrencyService.convert(totalSilverProfitLossUsd, "USD", displayCurrency, iqdRate)
  val totalSilverRoiPercent: Double get() = if (totalSilverCostUsd > 0) (totalSilverProfitLossUsd / totalSilverCostUsd) * 100.0 else 0.0

  val totalGoldHoldingCostUsd: Double get() = totalGoldOnlyCostUsd + totalSilverCostUsd

  // Total Precious Metals Portfolio (Gold + Silver)
  val totalGoldCostUsd: Double get() = totalGoldOnlyCostUsd + totalSilverCostUsd
  val totalGoldCostDisplay: Double get() = CurrencyService.convert(totalGoldCostUsd, "USD", displayCurrency, iqdRate)

  val totalGoldCurrentValueUsd: Double get() = totalGoldOnlyValueUsd + totalSilverValueUsd
  val totalGoldCurrentValueDisplay: Double get() = CurrencyService.convert(totalGoldCurrentValueUsd, "USD", displayCurrency, iqdRate)

  val totalGoldProfitLossUsd: Double get() = totalGoldCurrentValueUsd - totalGoldCostUsd
  val totalGoldProfitLossDisplay: Double get() = CurrencyService.convert(totalGoldProfitLossUsd, "USD", displayCurrency, iqdRate)

  val totalGoldRoiPercent: Double get() = if (totalGoldCostUsd > 0) (totalGoldProfitLossUsd / totalGoldCostUsd) * 100.0 else 0.0

  val isGoldRising: Boolean get() = if (simulatedGoldPricePerGram != null || simulatedSilverPricePerGram != null) totalGoldProfitLossUsd >= 0 else goldQuote.isRising

  val totalDebtOwed: Double get() = debtsCredits
    .filter { it.type == "DEBT" && !it.isCompleted && !it.isRealEstateDebt }
    .sumOf {
      if (it.isGoldDebt) {
        val usdVal = it.goldLiveValueUsd(effectiveGoldPricePerGram24K)
        CurrencyService.convert(usdVal, "USD", displayCurrency, iqdRate)
      } else {
        CurrencyService.convert(it.remainingAmount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
      }
    }

  val totalCreditReceivable: Double get() = debtsCredits
    .filter { it.type == "CREDIT" && !it.isCompleted && !it.isRealEstateDebt }
    .sumOf {
      if (it.isGoldDebt) {
        val usdVal = it.goldLiveValueUsd(effectiveGoldPricePerGram24K)
        CurrencyService.convert(usdVal, "USD", displayCurrency, iqdRate)
      } else {
        CurrencyService.convert(it.remainingAmount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
      }
    }

  // Active Gold Debts & Credits (قەرزێن زێڕی: مسقال و گرام ب بهایێ نوکە یێ بۆرسێ)
  val activeGoldDebtCreditLoans: List<ActiveDebtCreditLoan> get() = activeDebtCreditLoans.filter { it.isGoldDebt }
  val activeGoldCreditLoans: List<ActiveDebtCreditLoan> get() = activeGoldDebtCreditLoans.filter { it.isCreditLoan }
  val activeGoldDebtLoans: List<ActiveDebtCreditLoan> get() = activeGoldDebtCreditLoans.filter { it.isDebtLoan }

  val totalGoldCreditWeightMithqal: Double get() = activeGoldCreditLoans.sumOf { it.goldRemainingMithqal }
  val totalGoldCreditWeightGrams: Double get() = activeGoldCreditLoans.sumOf { it.goldRemainingGrams }
  val totalGoldCreditLiveValueDisplay: Double get() = activeGoldCreditLoans.sumOf {
    val usd = it.goldLiveValueUsd(effectiveGoldPricePerGram24K)
    CurrencyService.convert(usd, "USD", displayCurrency, iqdRate)
  }

  val totalGoldDebtWeightMithqal: Double get() = activeGoldDebtLoans.sumOf { it.goldRemainingMithqal }
  val totalGoldDebtWeightGrams: Double get() = activeGoldDebtLoans.sumOf { it.goldRemainingGrams }
  val totalGoldDebtLiveValueDisplay: Double get() = activeGoldDebtLoans.sumOf {
    val usd = it.goldLiveValueUsd(effectiveGoldPricePerGram24K)
    CurrencyService.convert(usd, "USD", displayCurrency, iqdRate)
  }

  val hasGoldDebtsOrCredits: Boolean get() = activeGoldDebtCreditLoans.isNotEmpty()

  val allDebtCreditLoanModels: List<ActiveDebtCreditLoan> get() = debtsCredits.map { debt ->
    debt.toActiveDebtCreditLoan(debtPayments.filter { it.debtId == debt.id })
  }

  val activeDebtCreditLoans: List<ActiveDebtCreditLoan> get() = allDebtCreditLoanModels
    .filter { it.isActive }
    .sortedBy { it.dueDateMillis }

  val activeDebtLoans: List<ActiveDebtCreditLoan> get() = activeDebtCreditLoans
    .filter { it.isDebtLoan }

  val activeCreditLoans: List<ActiveDebtCreditLoan> get() = activeDebtCreditLoans
    .filter { it.isCreditLoan }

  val settledDebtCreditLoans: List<ActiveDebtCreditLoan> get() = allDebtCreditLoanModels
    .filter { !it.isActive && !it.isRealEstateDebt }

  // Standalone Real Estate Debt & Credit Ledger (حسابا قەرز و دەینێن عەقاری - سەربەخۆ ژ سەرمایەی)
  val realEstateDebtCreditLoans: List<ActiveDebtCreditLoan> get() = allDebtCreditLoanModels.filter { it.isRealEstateDebt }
  val activeRealEstateDebtCreditLoans: List<ActiveDebtCreditLoan> get() = realEstateDebtCreditLoans.filter { it.isActive }
  val settledRealEstateDebtCreditLoans: List<ActiveDebtCreditLoan> get() = realEstateDebtCreditLoans.filter { !it.isActive }

  // Money I gave to someone / lent on real estate (+) (وەکی من ٥٠٠٠ دانە ئێکی ب قەرز)
  val realEstateCreditLoans: List<ActiveDebtCreditLoan> get() = activeRealEstateDebtCreditLoans.filter { it.isCreditLoan }
  val totalRealEstateCreditsReceivable: Double get() = realEstateCreditLoans.sumOf {
    CurrencyService.convert(it.remainingBalance, it.currency, displayCurrency, iqdRate)
  }

  // Debts I owe on real estate (−) (قەرزێن لسەر من د عەقاری دا)
  val realEstateDebtLoans: List<ActiveDebtCreditLoan> get() = activeRealEstateDebtCreditLoans.filter { it.isDebtLoan }
  val totalRealEstateDebtsPayable: Double get() = realEstateDebtLoans.sumOf {
    CurrencyService.convert(it.remainingBalance, it.currency, displayCurrency, iqdRate)
  }

  // Net Real Estate Debt/Credit Offset (+ when people owe me more on real estate, - when I owe more)
  val netRealEstateDebtCreditBalance: Double get() = totalRealEstateCreditsReceivable - totalRealEstateDebtsPayable

  // Net Debt & Credit Offset (دەین لسەر خەلکی و دەینێ لسەر من ل ئێک بکوژیت):
  // Positive when people owe me more than I owe; negative when I owe more than people owe me; 0 when equal.
  val netDebtCreditBalance: Double get() = totalCreditReceivable - totalDebtOwed

  // Net Capital Balance automatically updated by money owed by others (+CREDIT) and debts owed to creditors (-DEBT)
  val netCapitalBalance: Double get() = netCapital + netDebtCreditBalance

  val unifiedCapitalDebtSummary: UnifiedCapitalDebtSummary
    get() = UnifiedCapitalDebtSummary(
      baseCapitalBeforeDebtCredit = netCapital,
      totalOwedByOthers = totalCreditReceivable,
      totalDebtsToCreditors = totalDebtOwed,
      netDebtCreditOffset = netDebtCreditBalance,
      totalCreditRepaymentsCollected = totalCreditRepaymentsCollected,
      totalDebtRepaymentsPaid = totalDebtRepaymentsPaid,
      netCapitalBalance = netCapitalBalance,
      totalNetWorth = netWorth,
      activeReceivablesCount = activeCreditLoans.size,
      activePayablesCount = activeDebtLoans.size,
      settledCount = settledDebtCreditLoans.size
    )

  // Connected Master Capital Allocation Overview (Gold & Silver treated as liquid cash wealth)
  val totalCapitalDeployedInAssets: Double get() = totalInvestedInOtherProjects + totalActiveCarsCashInvested
  val liquidAvailableCapital: Double get() = (netCapital - totalInvestedInOtherProjects).coerceAtLeast(0.0)

  // Total Partner Money/Share in Investments & Cars (پارێ شریکاتی)
  val totalPartnerShareInInvestments: Double get() = investments
    .filter { it.isCoOwned }
    .sumOf {
      val partnerAmount = when {
        it.isSold && it.soldPrice > 0 -> it.soldPrice * (it.partnerSharePercent / 100.0)
        it.currentValue > 0 -> it.currentValue * (it.partnerSharePercent / 100.0)
        it.partnerInvestmentAmount > 0 -> it.partnerInvestmentAmount
        else -> it.initialCapital * (it.partnerSharePercent / 100.0)
      }
      CurrencyService.convert(partnerAmount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }

  val totalPartnerShareInOtherProjects: Double get() = totalPartnerShareInVentures

  // Non-car venture projects valuation included in Net Worth (Excluding Real Estate which is standalone: عەقار بلا داخلی سەروەت و سامان نەبیت تنێ بیتن)
  val myOtherProjectsValuation: Double get() = myVenturesValuation

  // Total Expenses Out (مەسروفات)
  val totalExpenses: Double get() = totalMoneyOut

  // Total Physical Assets & Cash Base (کاش + زێڕ و زیڤ + فرۆتن/قستێ سەیاران + ملوک و پرۆژە + قەرزێ دەف خەلکی)
  val totalPhysicalAndCashAssets: Double get() =
    (startingCapitalDisplay + totalMoneyIn) + totalGoldCurrentValueDisplay + myOtherProjectsValuation + totalCreditReceivable

  // Gross Estimated Total Wealth (سەروەتێ مەزەندەکری یێ گشتی)
  val grossEstimatedTotalWealth: Double get() =
    totalPhysicalAndCashAssets

  // Total Deductions from Gross Wealth: قەرزێن لسەر من + مەسروفات
  val totalDeductionsFromWealth: Double get() =
    totalDebtOwed + totalExpenses

  // Total Capital Assets before Debt/Credit Offset (سەروەتێ کاش و زێڕ و زیڤ + فرۆتن/قستێ سەیاران + پرۆژە)
  val totalCapitalAssets: Double get() =
    netCapital + myOtherProjectsValuation

  // Unified Clean Net Worth (صافی سەروەت):
  // - Cash + Income - Expenses
  // - Gold & Silver seen as Cash Wealth (زێڕ و زیڤ وەکی سەروەتێ کاش)
  // - Cars: when sold or when an installment comes in, directly increases Net Worth (سیارە وەختێ فرۆتن یان قستەک هات)
  // - Other Projects (my share)
  // - Credit on people & Debt on me cancel each other out (دەین لسەر خەلکی و دەینێ لسەر من ل ئێک دکوژیت)
  val remainingNetWorth: Double get() =
    totalCapitalAssets + netDebtCreditBalance

  val netWorth: Double get() =
    totalCapitalAssets + netDebtCreditBalance

  // Net Flow metrics for Positive / Negative Flow Indicator
  val netCashFlow: Double get() = totalMoneyIn - totalMoneyOut
  val netWealthFlowDelta: Double get() = netWorth - startingCapitalDisplay
  val netWealthFlowPercent: Double
    get() = if (startingCapitalDisplay > 0.0) {
      (netWealthFlowDelta / startingCapitalDisplay) * 100.0
    } else if (totalCapitalAssets > 0.0) {
      ((totalCapitalAssets - totalDebtOwed) / totalCapitalAssets) * 100.0
    } else {
      0.0
    }
  val isPositiveFlow: Boolean get() = netWorth >= 0.0 && netWealthFlowDelta >= 0.0
  val debtToAssetRatioPercent: Int
    get() = if (totalCapitalAssets > 0.0) {
      ((totalDebtOwed / totalCapitalAssets) * 100.0).toInt().coerceIn(0, 100)
    } else if (totalDebtOwed > 0.0) {
      100
    } else {
      0
    }
  val netAssetHealthRatio: Float
    get() = if (totalCapitalAssets > 0.0) {
      ((totalCapitalAssets - totalDebtOwed) / totalCapitalAssets).coerceIn(0.0, 1.0).toFloat()
    } else if (totalDebtOwed > 0.0) {
      0f
    } else {
      1f
    }

  // Active payment reminders: items not completed with reminders enabled
  val activePaymentReminders: List<DebtCreditEntity> get() = debtsCredits
    .filter { !it.isCompleted && it.reminderEnabled }
    .sortedBy { it.dueDateMillis }

  // Monthly budget alerts: calculate spending for current month vs budget
  val budgetAlerts: List<BudgetAlertItem> get() {
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    val currentMonthExpenses = transactions.filter {
      if (it.type != "OUT") return@filter false
      val tCal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
      tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear
    }

    val totalSpent = currentMonthExpenses.sumOf {
      CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }

    return budgets.map { b ->
      val limitInDisplay = CurrencyService.convert(b.monthlyLimit, b.currency, displayCurrency, iqdRate, entryIqdRate = b.exchangeRateAtEntry)
      val spentForCategory = if (b.category == "All Spending" || b.category == "Monthly Spending") {
        totalSpent
      } else {
        currentMonthExpenses
          .filter { it.category.contains(b.category, ignoreCase = true) }
          .sumOf { CurrencyService.convert(it.amount, it.currency, displayCurrency, iqdRate, entryIqdRate = it.exchangeRateAtEntry) }
      }
      val percentUsed = if (limitInDisplay > 0) (spentForCategory / limitInDisplay) * 100.0 else 0.0
      val isWarning = percentUsed >= b.alertThresholdPercent
      val isExceeded = percentUsed >= 100.0

      BudgetAlertItem(
        budget = b,
        spentAmount = spentForCategory,
        limitAmount = limitInDisplay,
        percentageUsed = percentUsed,
        isWarning = isWarning,
        isExceeded = isExceeded
      )
    }
  }

  val smartIntelligence: SmartIntelligenceSummary by lazy {
    FinancialIntelligenceEngine.analyze(this)
  }

  fun simulateWithTarget(target: QuickEditableTarget): Pair<Double, Double> {
    val simulated = when (target) {
      is QuickEditableTarget.TransactionTarget -> {
        val nextTxs = if (transactions.any { it.id == target.transaction.id }) {
          transactions.map { if (it.id == target.transaction.id) target.transaction else it }
        } else {
          transactions + target.transaction
        }
        copy(transactions = nextTxs)
      }
      is QuickEditableTarget.InvestmentTarget -> {
        val nextInvs = if (investments.any { it.id == target.investment.id }) {
          investments.map { if (it.id == target.investment.id) target.investment else it }
        } else {
          investments + target.investment
        }
        copy(investments = nextInvs)
      }
      is QuickEditableTarget.GoldTarget -> {
        val nextGolds = if (goldHoldings.any { it.id == target.holding.id }) {
          goldHoldings.map { if (it.id == target.holding.id) target.holding else it }
        } else {
          goldHoldings + target.holding
        }
        copy(goldHoldings = nextGolds)
      }
      is QuickEditableTarget.DebtTarget -> {
        val nextDebts = if (debtsCredits.any { it.id == target.debtCredit.id }) {
          debtsCredits.map { if (it.id == target.debtCredit.id) target.debtCredit else it }
        } else {
          debtsCredits + target.debtCredit
        }
        copy(debtsCredits = nextDebts)
      }
      is QuickEditableTarget.BudgetTarget -> this
    }
    return simulated.pureCashBalance to simulated.netWorth
  }
}

data class BudgetAlertItem(
  val budget: BudgetEntity,
  val spentAmount: Double,
  val limitAmount: Double,
  val percentageUsed: Double,
  val isWarning: Boolean,
  val isExceeded: Boolean
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  val repository = FinanceRepository(database)
  val cloudBackupService = CloudBackupService(database, application)
  val reportExportService = ReportExportService(application)
  val biometricSecurityManager = BiometricSecurityManager(application)
  val firebaseDataManager = FirebaseDataManager(application, database)
  val networkConnectivityObserver = NetworkConnectivityObserver(application)
  val offlineSyncManager = OfflineSyncManager(application, firebaseDataManager, networkConnectivityObserver)

  private val metalsPrefs = application.getSharedPreferences("precious_metals_prefs", Context.MODE_PRIVATE)
  private val categoriesPrefs = application.getSharedPreferences("custom_categories_prefs", Context.MODE_PRIVATE)

  private val _uiState = MutableStateFlow(FinanceUiState())
  val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

  private var undoBannerJob: Job? = null

  private fun computeDeltasForSnapshot(snapshot: ActionPayloadSnapshot): Pair<Double, Double> {
    val state = _uiState.value
    val beforeCash = state.pureCashBalance
    val beforeWorth = state.netWorth
    val simulatedAfterState = when (snapshot) {
      is ActionPayloadSnapshot.TransactionSnapshot -> {
        val nextList = when {
          snapshot.before == null && snapshot.after != null -> state.transactions + snapshot.after
          snapshot.before != null && snapshot.after != null -> state.transactions.map { if (it.id == snapshot.before.id) snapshot.after else it }
          snapshot.before != null && snapshot.after == null -> state.transactions.filterNot { it.id == snapshot.before.id }
          else -> state.transactions
        }
        state.copy(transactions = nextList)
      }
      is ActionPayloadSnapshot.InvestmentSnapshot -> {
        val nextList = when {
          snapshot.before == null && snapshot.after != null -> state.investments + snapshot.after
          snapshot.before != null && snapshot.after != null -> state.investments.map { if (it.id == snapshot.before.id) snapshot.after else it }
          snapshot.before != null && snapshot.after == null -> state.investments.filterNot { it.id == snapshot.before.id }
          else -> state.investments
        }
        state.copy(investments = nextList)
      }
      is ActionPayloadSnapshot.CarPaymentSnapshot -> {
        val nextList = when {
          snapshot.before == null && snapshot.after != null -> state.carPayments + snapshot.after
          snapshot.before != null && snapshot.after != null -> state.carPayments.map { if (it.id == snapshot.before.id) snapshot.after else it }
          snapshot.before != null && snapshot.after == null -> state.carPayments.filterNot { it.id == snapshot.before.id }
          else -> state.carPayments
        }
        state.copy(carPayments = nextList)
      }
      is ActionPayloadSnapshot.CarServiceSnapshot -> {
        val nextList = when {
          snapshot.before == null && snapshot.after != null -> state.carServices + snapshot.after
          snapshot.before != null && snapshot.after != null -> state.carServices.map { if (it.id == snapshot.before.id) snapshot.after else it }
          snapshot.before != null && snapshot.after == null -> state.carServices.filterNot { it.id == snapshot.before.id }
          else -> state.carServices
        }
        state.copy(carServices = nextList)
      }
      is ActionPayloadSnapshot.GoldHoldingSnapshot -> {
        val nextList = when {
          snapshot.before == null && snapshot.after != null -> state.goldHoldings + snapshot.after
          snapshot.before != null && snapshot.after != null -> state.goldHoldings.map { if (it.id == snapshot.before.id) snapshot.after else it }
          snapshot.before != null && snapshot.after == null -> state.goldHoldings.filterNot { it.id == snapshot.before.id }
          else -> state.goldHoldings
        }
        state.copy(goldHoldings = nextList)
      }
      is ActionPayloadSnapshot.DebtCreditSnapshot -> {
        val nextList = when {
          snapshot.before == null && snapshot.after != null -> state.debtsCredits + snapshot.after
          snapshot.before != null && snapshot.after != null -> state.debtsCredits.map { if (it.id == snapshot.before.id) snapshot.after else it }
          snapshot.before != null && snapshot.after == null -> state.debtsCredits.filterNot { it.id == snapshot.before.id }
          else -> state.debtsCredits
        }
        state.copy(debtsCredits = nextList)
      }
      is ActionPayloadSnapshot.DebtPaymentSnapshot -> state
      is ActionPayloadSnapshot.SettingsSnapshot -> state.copy(settings = snapshot.after)
      is ActionPayloadSnapshot.BudgetSnapshot -> state
    }
    return (simulatedAfterState.pureCashBalance - beforeCash) to (simulatedAfterState.netWorth - beforeWorth)
  }

  private fun recordReversibleAction(
    section: ActionSection,
    operationType: ActionOperationType,
    titleKu: String,
    titleEn: String,
    subtitleKu: String,
    subtitleEn: String,
    snapshot: ActionPayloadSnapshot
  ) {
    val (cashDelta, worthDelta) = computeDeltasForSnapshot(snapshot)
    val action = ReversibleAction(
      id = UUID.randomUUID().toString(),
      timestampMillis = System.currentTimeMillis(),
      section = section,
      operationType = operationType,
      titleKu = titleKu,
      titleEn = titleEn,
      subtitleKu = subtitleKu,
      subtitleEn = subtitleEn,
      cashDeltaDisplay = cashDelta,
      netWorthDeltaDisplay = worthDelta,
      displayCurrency = _uiState.value.displayCurrency,
      snapshot = snapshot
    )
    val nextHistory = (listOf(action) + _uiState.value.actionHistory).take(50)
    _uiState.value = _uiState.value.copy(
      actionHistory = nextHistory,
      redoStack = emptyList(),
      activeUndoBannerAction = action
    )
    undoBannerJob?.cancel()
    undoBannerJob = viewModelScope.launch {
      delay(8000L)
      if (_uiState.value.activeUndoBannerAction?.id == action.id) {
        _uiState.value = _uiState.value.copy(activeUndoBannerAction = null)
      }
    }
    offlineSyncManager.onDataMutated()
  }

  fun dismissUndoBanner() {
    undoBannerJob?.cancel()
    _uiState.value = _uiState.value.copy(activeUndoBannerAction = null)
  }

  fun openHistorySheet() {
    _uiState.value = _uiState.value.copy(isHistorySheetOpen = true)
  }

  fun closeHistorySheet() {
    _uiState.value = _uiState.value.copy(isHistorySheetOpen = false)
  }

  fun openQuickEdit(target: QuickEditableTarget) {
    _uiState.value = _uiState.value.copy(quickEditTarget = target)
  }

  fun openQuickEditForTransaction(tx: TransactionEntity) {
    openQuickEdit(QuickEditableTarget.TransactionTarget(tx, _uiState.value.isSchoolTransaction(tx)))
  }

  fun openQuickEditForInvestment(inv: InvestmentEntity) {
    openQuickEdit(QuickEditableTarget.InvestmentTarget(inv))
  }

  fun openQuickEditForGold(holding: GoldHoldingEntity) {
    openQuickEdit(QuickEditableTarget.GoldTarget(holding))
  }

  fun openQuickEditForDebt(debt: DebtCreditEntity) {
    openQuickEdit(QuickEditableTarget.DebtTarget(debt))
  }

  fun closeQuickEdit() {
    _uiState.value = _uiState.value.copy(quickEditTarget = null)
  }

  fun openQuickEditFromAction(action: ReversibleAction) {
    val state = _uiState.value
    val target: QuickEditableTarget? = when (val s = action.snapshot) {
      is ActionPayloadSnapshot.TransactionSnapshot -> {
        val candidate = s.after ?: s.before
        val live = candidate?.let { c -> state.transactions.find { it.id == c.id } ?: c }
        live?.let { QuickEditableTarget.TransactionTarget(it, state.isSchoolTransaction(it)) }
      }
      is ActionPayloadSnapshot.InvestmentSnapshot -> {
        val candidate = s.after ?: s.before
        val live = candidate?.let { c -> state.investments.find { it.id == c.id } ?: c }
        live?.let { QuickEditableTarget.InvestmentTarget(it) }
      }
      is ActionPayloadSnapshot.GoldHoldingSnapshot -> {
        val candidate = s.after ?: s.before
        val live = candidate?.let { c -> state.goldHoldings.find { it.id == c.id } ?: c }
        live?.let { QuickEditableTarget.GoldTarget(it) }
      }
      is ActionPayloadSnapshot.DebtCreditSnapshot -> {
        val candidate = s.after ?: s.before
        val live = candidate?.let { c -> state.debtsCredits.find { it.id == c.id } ?: c }
        live?.let { QuickEditableTarget.DebtTarget(it) }
      }
      is ActionPayloadSnapshot.BudgetSnapshot -> {
        val candidate = s.after ?: s.before
        val live = candidate?.let { c -> state.budgets.find { it.id == c.id } ?: c }
        live?.let { QuickEditableTarget.BudgetTarget(it) }
      }
      else -> null
    }
    if (target != null) {
      _uiState.value = _uiState.value.copy(quickEditTarget = target)
    }
  }

  fun clearActionHistory() {
    undoBannerJob?.cancel()
    _uiState.value = _uiState.value.copy(
      actionHistory = emptyList(),
      redoStack = emptyList(),
      activeUndoBannerAction = null
    )
  }

  fun undoLastAction() {
    val latest = _uiState.value.actionHistory.firstOrNull() ?: return
    undoAction(latest.id)
  }

  fun undoAction(actionId: String) {
    val action = _uiState.value.actionHistory.find { it.id == actionId } ?: return
    viewModelScope.launch {
      applySnapshotRevert(action.snapshot)
      val nextHistory = _uiState.value.actionHistory.filterNot { it.id == actionId }
      val nextRedo = (listOf(action) + _uiState.value.redoStack).take(30)
      _uiState.value = _uiState.value.copy(
        actionHistory = nextHistory,
        redoStack = nextRedo,
        activeUndoBannerAction = if (_uiState.value.activeUndoBannerAction?.id == actionId) null else _uiState.value.activeUndoBannerAction
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun redoLastAction() {
    val action = _uiState.value.redoStack.firstOrNull() ?: return
    viewModelScope.launch {
      applySnapshotForward(action.snapshot)
      val nextRedo = _uiState.value.redoStack.drop(1)
      val nextHistory = (listOf(action) + _uiState.value.actionHistory).take(50)
      _uiState.value = _uiState.value.copy(
        actionHistory = nextHistory,
        redoStack = nextRedo,
        activeUndoBannerAction = action
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  private suspend fun applySnapshotRevert(snapshot: ActionPayloadSnapshot) {
    when (snapshot) {
      is ActionPayloadSnapshot.TransactionSnapshot -> {
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteTransaction(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("transactions", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertTransaction(snapshot.before)
          }
          snapshot.before != null -> {
            repository.updateTransaction(snapshot.before)
          }
        }
      }
      is ActionPayloadSnapshot.InvestmentSnapshot -> {
        if (snapshot.extraCarPaymentAddedId != null) {
          repository.deleteCarPayment(snapshot.extraCarPaymentAddedId)
          firebaseDataManager.deleteDocumentFromSubcollection("car_payments", snapshot.extraCarPaymentAddedId)
        }
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteInvestment(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("investments", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertInvestment(snapshot.before)
            snapshot.deletedServices.forEach { repository.insertCarService(it) }
            snapshot.deletedPayments.forEach { repository.insertCarPayment(it) }
            snapshot.deletedMonthlyRecords.forEach { repository.insertMonthlyRecord(it) }
          }
          snapshot.before != null -> {
            repository.updateInvestment(snapshot.before)
          }
        }
      }
      is ActionPayloadSnapshot.CarPaymentSnapshot -> {
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteCarPayment(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("car_payments", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertCarPayment(snapshot.before)
          }
          snapshot.before != null -> {
            repository.updateCarPayment(snapshot.before)
          }
        }
        if (snapshot.parentCarBefore != null) {
          repository.updateInvestment(snapshot.parentCarBefore)
        }
      }
      is ActionPayloadSnapshot.CarServiceSnapshot -> {
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteCarService(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("car_services", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertCarService(snapshot.before)
          }
          snapshot.before != null -> {
            repository.updateCarService(snapshot.before)
          }
        }
      }
      is ActionPayloadSnapshot.GoldHoldingSnapshot -> {
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteGoldHolding(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("gold_holdings", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertGoldHolding(snapshot.before)
          }
          snapshot.before != null -> {
            repository.updateGoldHolding(snapshot.before)
          }
        }
      }
      is ActionPayloadSnapshot.DebtCreditSnapshot -> {
        if (snapshot.addedPayment != null) {
          repository.deleteDebtPayment(snapshot.addedPayment.id)
          firebaseDataManager.deleteDocumentFromSubcollection("debt_payments", snapshot.addedPayment.id)
        }
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteDebtCredit(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("debts_credits", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertDebtCredit(snapshot.before)
            snapshot.deletedPayments.forEach { database.financeDao().insertDebtPayment(it) }
          }
          snapshot.before != null -> {
            repository.updateDebtCredit(snapshot.before)
          }
        }
      }
      is ActionPayloadSnapshot.DebtPaymentSnapshot -> {
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteDebtPayment(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("debt_payments", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            database.financeDao().insertDebtPayment(snapshot.before)
            if (snapshot.parentDebtBefore != null) {
              repository.updateDebtCredit(snapshot.parentDebtBefore)
            }
          }
        }
      }
      is ActionPayloadSnapshot.SettingsSnapshot -> {
        repository.updateSettings(snapshot.before)
      }
      is ActionPayloadSnapshot.BudgetSnapshot -> {
        when {
          snapshot.before == null && snapshot.after != null -> {
            repository.deleteBudget(snapshot.after.id)
            firebaseDataManager.deleteDocumentFromSubcollection("budgets", snapshot.after.id)
          }
          snapshot.before != null && snapshot.after == null -> {
            repository.insertBudget(snapshot.before)
          }
          snapshot.before != null -> {
            repository.updateBudget(snapshot.before)
          }
        }
      }
    }
  }

  private suspend fun applySnapshotForward(snapshot: ActionPayloadSnapshot) {
    when (snapshot) {
      is ActionPayloadSnapshot.TransactionSnapshot -> {
        when {
          snapshot.after != null -> repository.insertTransaction(snapshot.after)
          snapshot.before != null -> {
            repository.deleteTransaction(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("transactions", snapshot.before.id)
          }
        }
      }
      is ActionPayloadSnapshot.InvestmentSnapshot -> {
        when {
          snapshot.after != null -> repository.insertInvestment(snapshot.after)
          snapshot.before != null -> {
            repository.deleteInvestment(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("investments", snapshot.before.id)
          }
        }
      }
      is ActionPayloadSnapshot.CarPaymentSnapshot -> {
        when {
          snapshot.after != null -> repository.insertCarPayment(snapshot.after)
          snapshot.before != null -> {
            repository.deleteCarPayment(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("car_payments", snapshot.before.id)
          }
        }
      }
      is ActionPayloadSnapshot.CarServiceSnapshot -> {
        when {
          snapshot.after != null -> repository.insertCarService(snapshot.after)
          snapshot.before != null -> {
            repository.deleteCarService(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("car_services", snapshot.before.id)
          }
        }
      }
      is ActionPayloadSnapshot.GoldHoldingSnapshot -> {
        when {
          snapshot.after != null -> repository.insertGoldHolding(snapshot.after)
          snapshot.before != null -> {
            repository.deleteGoldHolding(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("gold_holdings", snapshot.before.id)
          }
        }
      }
      is ActionPayloadSnapshot.DebtCreditSnapshot -> {
        when {
          snapshot.after != null -> {
            repository.insertDebtCredit(snapshot.after)
            if (snapshot.addedPayment != null) {
              database.financeDao().insertDebtPayment(snapshot.addedPayment)
            }
          }
          snapshot.before != null -> {
            repository.deleteDebtCredit(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("debts_credits", snapshot.before.id)
          }
        }
      }
      is ActionPayloadSnapshot.DebtPaymentSnapshot -> {
        when {
          snapshot.after != null -> database.financeDao().insertDebtPayment(snapshot.after)
          snapshot.before != null -> repository.deleteDebtPayment(snapshot.before.id)
        }
      }
      is ActionPayloadSnapshot.SettingsSnapshot -> {
        repository.updateSettings(snapshot.after)
      }
      is ActionPayloadSnapshot.BudgetSnapshot -> {
        when {
          snapshot.after != null -> repository.insertBudget(snapshot.after)
          snapshot.before != null -> {
            repository.deleteBudget(snapshot.before.id)
            firebaseDataManager.deleteDocumentFromSubcollection("budgets", snapshot.before.id)
          }
        }
      }
    }
  }

  fun setDebtPayoffStrategy(strategy: DebtPayoffStrategy) {
    _uiState.value = _uiState.value.copy(debtPayoffStrategy = strategy)
  }

  fun applyDebtPayoffStep(step: DebtPayoffStep, affectCashBalance: Boolean = true) {
    val debt = step.debtEntity
    val state = _uiState.value
    val payInDebtCurrency = CurrencyService.convert(
      step.recommendedPaymentDisplay,
      state.displayCurrency,
      debt.currency,
      state.iqdRate,
      entryIqdRate = debt.exchangeRateAtEntry
    ).coerceIn(0.01, debt.remainingAmount)
    recordLoanPayment(
      debtId = debt.id,
      paymentAmount = payInDebtCurrency,
      notes = if (state.settings.language == "KU") "ئۆپتیمایزەرێ قەرزان (${state.debtPayoffStrategy.name})" else "Smart Payoff Optimizer (${state.debtPayoffStrategy.name})",
      affectCashBalance = affectCashBalance,
      customIqdRate = debt.exchangeRateAtEntry
    )
  }

  fun applyRecommendedBudget(rec: BudgetRecommendation) {
    viewModelScope.launch {
      val state = _uiState.value
      val existing = rec.existingBudgetId?.let { id -> state.budgets.find { it.id == id } }
        ?: state.budgets.find { it.category.equals(rec.category, ignoreCase = true) }

      if (existing != null) {
        val updated = existing.copy(
          monthlyLimit = rec.recommendedMonthlyLimitDisplay,
          currency = state.displayCurrency,
          exchangeRateAtEntry = state.iqdRate
        )
        repository.updateBudget(updated)
        recordReversibleAction(
          section = ActionSection.BUDGET,
          operationType = ActionOperationType.EDIT,
          titleKu = "نویکرنا بودجەیا زیرەک: ${rec.categoryLabelKu}",
          titleEn = "Smart Budget Updated: ${rec.categoryLabelEn}",
          subtitleKu = "سنوورێ هەیڤانە هاتە دانان لسەر ${CurrencyService.format(rec.recommendedMonthlyLimitDisplay, state.displayCurrency)}",
          subtitleEn = "Monthly limit set to ${CurrencyService.format(rec.recommendedMonthlyLimitDisplay, state.displayCurrency)}",
          snapshot = ActionPayloadSnapshot.BudgetSnapshot(before = existing, after = updated)
        )
      } else {
        val toInsert = BudgetEntity(
          category = rec.category,
          monthlyLimit = rec.recommendedMonthlyLimitDisplay,
          currency = state.displayCurrency,
          alertThresholdPercent = 80,
          exchangeRateAtEntry = state.iqdRate
        )
        val newId = repository.insertBudget(toInsert)
        val inserted = toInsert.copy(id = newId)
        recordReversibleAction(
          section = ActionSection.BUDGET,
          operationType = ActionOperationType.ADD,
          titleKu = "دانانا بودجەیا زیرەک: ${rec.categoryLabelKu}",
          titleEn = "Smart Budget Created: ${rec.categoryLabelEn}",
          subtitleKu = "سنوورێ هەیڤانە: ${CurrencyService.format(rec.recommendedMonthlyLimitDisplay, state.displayCurrency)}",
          subtitleEn = "Monthly limit: ${CurrencyService.format(rec.recommendedMonthlyLimitDisplay, state.displayCurrency)}",
          snapshot = ActionPayloadSnapshot.BudgetSnapshot(before = null, after = inserted)
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun applyAllRecommendedBudgets() {
    val recs = _uiState.value.smartIntelligence.budgetRecommendations.filter { !it.isAlreadyAligned }
    recs.forEach { applyRecommendedBudget(it) }
  }

  init {
    // Restore cached live or custom silver/gold rates immediately on startup
    val cachedGoldGram = metalsPrefs.getFloat("cached_gold_gram_24k", OnlineGoldService.BASELINE_PRICE_PER_GRAM.toFloat()).toDouble()
    val cachedSilverGram = metalsPrefs.getFloat("cached_silver_gram_9999", OnlineGoldService.BASELINE_SILVER_PRICE_PER_GRAM_9999.toFloat()).toDouble()
    val customSilverGram = if (metalsPrefs.contains("custom_silver_gram_9999")) {
      metalsPrefs.getFloat("custom_silver_gram_9999", 0f).toDouble().takeIf { it > 0.1 }
    } else null

    val validCachedSilverGram = if (cachedSilverGram >= 1.75) cachedSilverGram else OnlineGoldService.BASELINE_SILVER_PRICE_PER_GRAM_9999
    val savedExpenseCats = loadCustomCategories("custom_expense_categories_v1")
    val savedIncomeCats = loadCustomCategories("custom_income_categories_v1")
    _uiState.value = _uiState.value.copy(
      goldQuote = OnlineGoldService.createFallbackQuote(
        customPricePerGram = if (cachedGoldGram > 50.0) cachedGoldGram else OnlineGoldService.BASELINE_PRICE_PER_GRAM,
        customSilverPricePerGram9999 = validCachedSilverGram,
        isOnline = true
      ),
      simulatedSilverPricePerGram = customSilverGram,
      customExpenseCategories = savedExpenseCats,
      customIncomeCategories = savedIncomeCats
    )

    viewModelScope.launch {
      repository.ensureInitialized()
      // Migrate any previously saved custom categories from SharedPreferences into the Room database table
      savedExpenseCats.forEach { catName ->
        repository.addExpenseCategory(name = catName, type = "OUT")
      }
      savedIncomeCats.forEach { catName ->
        repository.addExpenseCategory(name = catName, type = "IN")
      }

      val cleanupPrefs = application.getSharedPreferences("repayments_cleanup_prefs", Context.MODE_PRIVATE)
      if (!cleanupPrefs.getBoolean("cleanup_four_repayments_v1", false)) {
        repository.clearAllDebtPayments()
        repository.clearAllCarPayments()
        firebaseDataManager.clearSubcollection("debt_payments")
        firebaseDataManager.clearSubcollection("car_payments")
        cleanupPrefs.edit().putBoolean("cleanup_four_repayments_v1", true).apply()
      }
      val shouldCleanSeed = !cleanupPrefs.getBoolean("cleanup_auto_seeded_records_v2", false)
      val purged = repository.purgeAutoSeededAndOrphanRecords(cleanSeedData = shouldCleanSeed)
      if (shouldCleanSeed) {
        cleanupPrefs.edit().putBoolean("cleanup_auto_seeded_records_v2", true).apply()
      }
      purged.transactionIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("transactions", it) }
      purged.investmentIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("investments", it) }
      purged.carServiceIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("car_services", it) }
      purged.carPaymentIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("car_payments", it) }
      purged.monthlyRecordIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("monthly_records", it) }
      purged.debtCreditIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("debts_credits", it) }
      purged.debtPaymentIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("debt_payments", it) }
      purged.goldHoldingIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("gold_holdings", it) }
      purged.budgetIds.forEach { firebaseDataManager.deleteDocumentFromSubcollection("budgets", it) }

      cloudBackupService.createCloudBackup()
      // Sync initial or updated Room records with Cloud Firestore
      firebaseDataManager.syncAllDataToFirestore()
      // We do NOT call refreshRates() on startup so user-written exchange rates are permanently preserved
      fetchLiveGoldQuote()
      fetchLiveWeather()
    }

    viewModelScope.launch {
      repository.allExpenseCategories.collect { categories ->
        val outNames = categories
          .filter { !it.type.equals("IN", ignoreCase = true) }
          .map { it.name.trim() }
          .filter { it.isNotEmpty() }
          .distinctBy { it.lowercase() }
        val inNames = categories
          .filter { it.type.equals("IN", ignoreCase = true) }
          .map { it.name.trim() }
          .filter { it.isNotEmpty() }
          .distinctBy { it.lowercase() }
        saveCustomCategories("custom_expense_categories_v1", outNames)
        saveCustomCategories("custom_income_categories_v1", inNames)
        _uiState.value = _uiState.value.copy(
          expenseCategories = categories,
          customExpenseCategories = outNames,
          customIncomeCategories = inNames
        )
      }
    }

    viewModelScope.launch {
      firebaseDataManager.authState.collect { authUser ->
        _uiState.value = _uiState.value.copy(firebaseAuthUser = authUser)
      }
    }

    viewModelScope.launch {
      firebaseDataManager.firestoreState.collect { fsState ->
        _uiState.value = _uiState.value.copy(
          firestoreSyncState = fsState,
          isSyncing = fsState.isSyncing,
          syncMessage = fsState.statusMessage
        )
      }
    }

    viewModelScope.launch {
      offlineSyncManager.state.collect { syncState ->
        _uiState.value = _uiState.value.copy(
          isNetworkOnline = syncState.isOnline,
          pendingOfflineMutations = syncState.pendingMutationsCount,
          showAutoSyncSuccessBanner = syncState.showSyncSuccessBanner,
          lastSuccessfulSyncMillis = syncState.lastSuccessfulSyncMillis,
          offlineStatusMessage = syncState.statusMessage
        )
      }
    }

    viewModelScope.launch {
      repository.allDebtPayments.collect { payments ->
        _uiState.value = _uiState.value.copy(debtPayments = payments)
      }
    }

    viewModelScope.launch {
      val flowGroupA = combine(
        repository.userSettings,
        repository.allTransactions,
        repository.allInvestments
      ) { settings, txs, invs ->
        Triple(settings, txs, invs)
      }

      val flowGroupB = combine(
        repository.allMonthlyRecords,
        repository.allDebtsCredits,
        repository.allBudgets
      ) { records, debts, buds ->
        Triple(records, debts, buds)
      }

      val flowGroupC = combine(
        repository.allGoldHoldings,
        repository.allCarServices,
        repository.allCarPayments
      ) { goldList, carServicesList, carPaymentsList ->
        Triple(goldList, carServicesList, carPaymentsList)
      }

      combine(flowGroupA, flowGroupB, flowGroupC) { groupA, groupB, groupC ->
        val safeSettings = groupA.first ?: UserSettingsEntity()
        val current = _uiState.value
        current.copy(
          settings = safeSettings,
          transactions = groupA.second,
          investments = groupA.third,
          monthlyRecords = groupB.first,
          debtsCredits = groupB.second,
          budgets = groupB.third,
          goldHoldings = groupC.first,
          carServices = groupC.second,
          carPayments = groupC.third,
          displayCurrency = safeSettings.displayCurrency,
          isAppLocked = safeSettings.biometricEnabled && safeSettings.isAppLocked,
          exchangeRateMessage = if (safeSettings.isCustomIqdRate)
            "Saved Rate: 1 USD = ${safeSettings.iqdPerUsdRate.toInt()} IQD (Permanent)"
          else
            "Market Rate: 1 USD = ${safeSettings.iqdPerUsdRate.toInt()} IQD"
        )
      }.collect { state ->
        _uiState.value = state
      }
    }
  }

  fun fetchLiveGoldQuote() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isFetchingGold = true)
      val quote = OnlineGoldService.fetchLiveGoldPrice()
      metalsPrefs.edit()
        .putFloat("cached_gold_gram_24k", quote.pricePerGram24K.toFloat())
        .putFloat("cached_silver_gram_9999", quote.silverPricePerGram9999.toFloat())
        .apply()
      _uiState.value = _uiState.value.copy(
        goldQuote = quote,
        isFetchingGold = false
      )
    }
  }

  fun fetchLiveWeather(context: Context? = null) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(
        weatherData = _uiState.value.weatherData.copy(isLoading = true)
      )
      val liveData = WeatherService.fetchCurrentWeather(
        context = context ?: getApplication()
      )
      val currentOverride = _uiState.value.weatherData.simulatedOverride
      _uiState.value = _uiState.value.copy(
        weatherData = liveData.copy(
          simulatedOverride = currentOverride,
          isLoading = false
        )
      )
    }
  }

  fun setWeatherSimulationOverride(overrideCondition: WeatherConditionType?) {
    _uiState.value = _uiState.value.copy(
      weatherData = _uiState.value.weatherData.copy(simulatedOverride = overrideCondition)
    )
  }

  fun setSimulatedGoldPrice(price: Double?) {
    _uiState.value = _uiState.value.copy(simulatedGoldPricePerGram = price)
  }

  fun setSimulatedSilverPrice(price: Double?) {
    if (price != null && price > 0.0) {
      metalsPrefs.edit().putFloat("custom_silver_gram_9999", price.toFloat()).apply()
    } else {
      metalsPrefs.edit().remove("custom_silver_gram_9999").apply()
    }
    _uiState.value = _uiState.value.copy(simulatedSilverPricePerGram = price)
  }

  fun setCustomSilverPricePerKg(pricePerKgUsd: Double?) {
    val perGram = pricePerKgUsd?.takeIf { it > 0.0 }?.let { it / 1000.0 }
    setSimulatedSilverPrice(perGram)
  }

  fun addPreciousMetalHolding(
    metalType: String = "GOLD",
    title: String,
    weightGrams: Double,
    buyPricePerGram: Double,
    karatOrPurity: Int = 24,
    purityLabel: String = "24K",
    notes: String = "",
    deductedFromCash: Boolean = true,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val cleanMetal = if (metalType.equals("SILVER", ignoreCase = true)) "SILVER" else "GOLD"
      val defaultTitle = if (cleanMetal == "SILVER") {
        "Silver 9999 (${weightGrams}g)"
      } else {
        "Physical Gold (${weightGrams}g)"
      }
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val finalTitle = title.ifBlank { defaultTitle }
      val toInsert = GoldHoldingEntity(
        metalType = cleanMetal,
        title = finalTitle,
        weightGrams = weightGrams,
        buyPricePerGram = buyPricePerGram,
        currency = "USD",
        purchaseDateMillis = System.currentTimeMillis(),
        karat = karatOrPurity,
        purityLabel = purityLabel,
        notes = notes,
        deductedFromCash = deductedFromCash,
        exchangeRateAtEntry = entryRate
      )
      val id = repository.insertGoldHolding(toInsert)
      val inserted = toInsert.copy(id = id)
      recordReversibleAction(
        section = ActionSection.GOLD,
        operationType = ActionOperationType.ADD,
        titleKu = "زێدەکرنا زێڕ/زیڤ: $finalTitle",
        titleEn = "Added Metal: $finalTitle",
        subtitleKu = "${weightGrams}g • کری ب $${"%.2f".format(buyPricePerGram)}/g",
        subtitleEn = "${weightGrams}g • Bought at $${"%.2f".format(buyPricePerGram)}/g",
        snapshot = ActionPayloadSnapshot.GoldHoldingSnapshot(before = null, after = inserted)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun addGoldHolding(
    title: String,
    weightGrams: Double,
    buyPricePerGram: Double,
    karat: Int = 24,
    notes: String = "",
    deductedFromCash: Boolean = true,
    customIqdRate: Double? = null
  ) {
    addPreciousMetalHolding(
      metalType = "GOLD",
      title = title,
      weightGrams = weightGrams,
      buyPricePerGram = buyPricePerGram,
      karatOrPurity = karat,
      purityLabel = "${karat}K",
      notes = notes,
      deductedFromCash = deductedFromCash,
      customIqdRate = customIqdRate
    )
  }

  fun addSilverHolding(
    title: String,
    weightGrams: Double,
    buyPricePerGram: Double,
    purity: Int = 9999,
    notes: String = "",
    deductedFromCash: Boolean = true,
    customIqdRate: Double? = null
  ) {
    addPreciousMetalHolding(
      metalType = "SILVER",
      title = title,
      weightGrams = weightGrams,
      buyPricePerGram = buyPricePerGram,
      karatOrPurity = purity,
      purityLabel = "$purity",
      notes = notes,
      deductedFromCash = deductedFromCash,
      customIqdRate = customIqdRate
    )
  }

  fun sellGoldHolding(holdingId: Long, soldPriceUsd: Double, notes: String = "", customIqdRate: Double? = null) {
    viewModelScope.launch {
      val existing = _uiState.value.goldHoldings.find { it.id == holdingId }
      if (existing != null) {
        val entryRate = when {
          customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
          existing.exchangeRateAtEntry > 0.0 -> existing.exchangeRateAtEntry
          else -> _uiState.value.settings.iqdPerUsdRate
        }
        val updated = existing.copy(
          isSold = true,
          soldPriceUsd = soldPriceUsd,
          soldDateMillis = System.currentTimeMillis(),
          exchangeRateAtEntry = entryRate,
          notes = if (notes.isNotBlank()) {
            if (existing.notes.isNotBlank()) "${existing.notes} | Sold: $notes" else "Sold: $notes"
          } else existing.notes
        )
        repository.updateGoldHolding(updated)
        recordReversibleAction(
          section = ActionSection.GOLD,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "فرۆتنا زێڕ/زیڤ: ${existing.title}",
          titleEn = "Sold Metal: ${existing.title}",
          subtitleKu = "فرۆتن ب $${"%,.2f".format(soldPriceUsd)}",
          subtitleEn = "Sold for $${"%,.2f".format(soldPriceUsd)}",
          snapshot = ActionPayloadSnapshot.GoldHoldingSnapshot(before = existing, after = updated)
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun reopenGoldHoldingAsActive(holdingId: Long) {
    viewModelScope.launch {
      val existing = _uiState.value.goldHoldings.find { it.id == holdingId }
      if (existing != null) {
        val updated = existing.copy(
          isSold = false,
          soldPriceUsd = 0.0,
          soldDateMillis = 0L
        )
        repository.updateGoldHolding(updated)
        recordReversibleAction(
          section = ActionSection.GOLD,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "زڤڕاندنا زێڕ/زیڤ بۆ ئەکتیڤ: ${existing.title}",
          titleEn = "Reopened Metal: ${existing.title}",
          subtitleKu = "بارودۆخ هاتە زڤڕاندن بۆ ئەکتیڤ",
          subtitleEn = "Restored to active holdings",
          snapshot = ActionPayloadSnapshot.GoldHoldingSnapshot(before = existing, after = updated)
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun updateGoldHolding(holding: GoldHoldingEntity) {
    viewModelScope.launch {
      val before = _uiState.value.goldHoldings.find { it.id == holding.id }
      repository.updateGoldHolding(holding)
      recordReversibleAction(
        section = ActionSection.GOLD,
        operationType = ActionOperationType.EDIT,
        titleKu = "دەستکاریا زێڕ/زیڤ: ${holding.title}",
        titleEn = "Edited Metal: ${holding.title}",
        subtitleKu = "${holding.weightGrams}g • $${"%.2f".format(holding.buyPricePerGram)}/g",
        subtitleEn = "${holding.weightGrams}g • $${"%.2f".format(holding.buyPricePerGram)}/g",
        snapshot = ActionPayloadSnapshot.GoldHoldingSnapshot(before = before, after = holding)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteGoldHolding(id: Long) {
    viewModelScope.launch {
      val before = _uiState.value.goldHoldings.find { it.id == id }
      repository.deleteGoldHolding(id)
      if (before != null) {
        recordReversibleAction(
          section = ActionSection.GOLD,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا زێڕ/زیڤ: ${before.title}",
          titleEn = "Deleted Metal: ${before.title}",
          subtitleKu = "${before.weightGrams}g",
          subtitleEn = "${before.weightGrams}g",
          snapshot = ActionPayloadSnapshot.GoldHoldingSnapshot(before = before, after = null)
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.deleteDocumentFromSubcollection("gold_holdings", id)
    }
  }

  fun createCloudBackupSnapshot(onComplete: (String) -> Unit) {
    viewModelScope.launch {
      val backup = cloudBackupService.createCloudBackup()
      onComplete(backup.backupJson)
    }
  }

  fun setTab(tabIndex: Int) {
    _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
  }

  fun openSearchResultItem(targetTab: Int, entityType: String, entityId: Long) {
    _uiState.value = _uiState.value.copy(
      selectedTab = targetTab,
      focusedEntityType = entityType,
      focusedEntityId = entityId
    )
  }

  fun clearFocusedSearchResult() {
    _uiState.value = _uiState.value.copy(
      focusedEntityType = null,
      focusedEntityId = null
    )
  }

  fun toggleDisplayCurrency() {
    val current = _uiState.value.displayCurrency
    val nextCurrency = if (current == CurrencyService.CURRENCY_USD) CurrencyService.CURRENCY_IQD else CurrencyService.CURRENCY_USD
    updateDisplayCurrency(nextCurrency)
  }

  fun updateDisplayCurrency(currency: String) {
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(displayCurrency = currency)
      repository.updateSettings(settings)
      _uiState.value = _uiState.value.copy(displayCurrency = currency)
    }
  }

  fun updateIqdRate(newRate: Double) {
    viewModelScope.launch {
      val beforeSettings = _uiState.value.settings
      val oldRate = beforeSettings.iqdPerUsdRate
      val normalizedRate = CurrencyService.normalizeIqdRatePerUsd(newRate, oldRate)
      // Lock all previously entered IQD records to the old exchange rate before changing the rate
      repository.lockHistoricalIqdRates(oldRate)
      val settings = beforeSettings.copy(
        iqdPerUsdRate = normalizedRate,
        isCustomIqdRate = true,
        lastRateUpdateMillis = System.currentTimeMillis()
      )
      repository.updateSettings(settings)
      recordReversibleAction(
        section = ActionSection.CAPITAL,
        operationType = ActionOperationType.EDIT,
        titleKu = "گوهۆڕینا نرخێ دیناری: ${normalizedRate.toInt()} IQD",
        titleEn = "Exchange Rate Updated: ${normalizedRate.toInt()} IQD",
        subtitleKu = "ژ ${oldRate.toInt()} بۆ ${normalizedRate.toInt()} IQD",
        subtitleEn = "From ${oldRate.toInt()} to ${normalizedRate.toInt()} IQD",
        snapshot = ActionPayloadSnapshot.SettingsSnapshot(before = beforeSettings, after = settings)
      )
      _uiState.value = _uiState.value.copy(
        settings = settings,
        exchangeRateMessage = "Saved Rate: 1 USD = ${normalizedRate.toInt()} IQD (Old records stay locked at their entry rate)"
      )
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun refreshRates(forceMarketRate: Boolean = false) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(exchangeRateMessage = "Updating live exchange rates...")
      val result = CurrencyService.fetchRealtimeRates()
      if (result.success) {
        val currentSettings = _uiState.value.settings
        val shouldUpdateIqd = forceMarketRate || !currentSettings.isCustomIqdRate
        if (shouldUpdateIqd) {
          repository.lockHistoricalIqdRates(currentSettings.iqdPerUsdRate)
        }
        val newIqd = if (shouldUpdateIqd) result.iqdPerUsd else currentSettings.iqdPerUsdRate
        val settings = currentSettings.copy(
          iqdPerUsdRate = newIqd,
          eurPerUsdRate = result.eurPerUsd,
          lastRateUpdateMillis = result.timestamp
        )
        repository.updateSettings(settings)
        _uiState.value = _uiState.value.copy(
          settings = settings,
          exchangeRateMessage = if (currentSettings.isCustomIqdRate && !forceMarketRate)
            "Custom Rate: 1 USD = ${newIqd.toInt()} IQD (Preserved)"
          else
            "Live Market Rate: 1 USD = ${newIqd.toInt()} IQD (Updated)"
        )
      } else {
        _uiState.value = _uiState.value.copy(
          exchangeRateMessage = "1 USD = ${_uiState.value.settings.iqdPerUsdRate.toInt()} IQD (${result.message})"
        )
      }
    }
  }

  fun setStartingCapital(amount: Double, currency: String = "USD", customIqdRate: Double = 0.0) {
    viewModelScope.launch {
      val beforeSettings = _uiState.value.settings
      val entryRate = if (customIqdRate > 0.0) customIqdRate else beforeSettings.iqdPerUsdRate
      val settings = beforeSettings.copy(
        startingCapital = amount,
        capitalCurrency = currency,
        capitalExchangeRateAtEntry = entryRate
      )
      repository.updateSettings(settings)
      recordReversibleAction(
        section = ActionSection.CAPITAL,
        operationType = ActionOperationType.EDIT,
        titleKu = "دەستکاریا سەرمایێ دەستپێکی",
        titleEn = "Updated Starting Capital",
        subtitleKu = CurrencyService.format(amount, currency),
        subtitleEn = CurrencyService.format(amount, currency),
        snapshot = ActionPayloadSnapshot.SettingsSnapshot(before = beforeSettings, after = settings)
      )
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun resetCashAccountToZero() {
    viewModelScope.launch {
      val beforeSettings = _uiState.value.settings
      val nextSettings = beforeSettings.copy(
        startingCapital = 0.0,
        capitalExchangeRateAtEntry = 0.0
      )
      repository.updateSettings(nextSettings)

      // Clear cash transactions to zero out operating cash balance
      val txs = _uiState.value.transactions
      for (tx in txs) {
        repository.deleteTransaction(tx.id)
      }

      recordReversibleAction(
        section = ActionSection.CAPITAL,
        operationType = ActionOperationType.EDIT,
        titleKu = "سفرکرنا حسێبا کاشی بۆ ٠",
        titleEn = "Reset Cash Account to 0",
        subtitleKu = "سەرمایێ کاش هاتە سفرکرن",
        subtitleEn = "Cash reset to zero",
        snapshot = ActionPayloadSnapshot.SettingsSnapshot(before = beforeSettings, after = nextSettings)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun addCapitalAddition(
    amount: Double,
    currency: String = _uiState.value.displayCurrency,
    category: String = "Capital Addition",
    notes: String = "",
    customIqdRate: Double? = null
  ) {
    addTransaction(
      type = "IN",
      amount = amount,
      category = category.ifBlank { "Capital Addition" },
      currency = currency,
      notes = notes.ifBlank { "Addition to Capital" },
      customIqdRate = customIqdRate
    )
  }

  private fun loadCustomCategories(key: String): List<String> {
    val raw = categoriesPrefs.getString(key, "") ?: ""
    return raw.split("\n")
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinctBy { it.lowercase() }
  }

  private fun saveCustomCategories(key: String, list: List<String>) {
    categoriesPrefs.edit().putString(key, list.joinToString("\n")).apply()
  }

  fun addExpenseCategory(name: String, type: String = "OUT") {
    val cleanName = name.trim().replace("\n", " ")
    if (cleanName.isBlank()) return
    val normalizedType = if (type.equals("IN", ignoreCase = true)) "IN" else "OUT"
    if (normalizedType == "IN") {
      val current = _uiState.value.customIncomeCategories
      if (current.none { it.equals(cleanName, ignoreCase = true) }) {
        val next = current + cleanName
        saveCustomCategories("custom_income_categories_v1", next)
        _uiState.value = _uiState.value.copy(customIncomeCategories = next)
      }
    } else {
      val current = _uiState.value.customExpenseCategories
      if (current.none { it.equals(cleanName, ignoreCase = true) }) {
        val next = current + cleanName
        saveCustomCategories("custom_expense_categories_v1", next)
        _uiState.value = _uiState.value.copy(customExpenseCategories = next)
      }
    }
    viewModelScope.launch {
      repository.addExpenseCategory(name = cleanName, type = normalizedType)
      cloudBackupService.createCloudBackup()
    }
  }

  fun getTransactionsForCategory(name: String, type: String = "OUT"): List<TransactionEntity> {
    val cleanName = name.trim()
    if (cleanName.isBlank()) return emptyList()
    val normalizedType = if (type.equals("IN", ignoreCase = true)) "IN" else "OUT"
    return _uiState.value.transactions.filter {
      it.type.equals(normalizedType, ignoreCase = true) &&
        it.category.trim().equals(cleanName, ignoreCase = true)
    }
  }

  fun deleteExpenseCategory(
    name: String,
    type: String = "OUT",
    deleteAssociatedTransactions: Boolean = false,
    reassignToCategory: String? = null
  ) {
    val cleanName = name.trim()
    if (cleanName.isBlank()) return
    val normalizedType = if (type.equals("IN", ignoreCase = true)) "IN" else "OUT"
    val isKu = _uiState.value.settings.language != "en"

    val remainingCategories = if (normalizedType == "IN") {
      _uiState.value.customIncomeCategories.filterNot { it.equals(cleanName, ignoreCase = true) }
    } else {
      _uiState.value.customExpenseCategories.filterNot { it.equals(cleanName, ignoreCase = true) }
    }

    val resolvedTargetCategory = reassignToCategory?.trim()?.takeIf {
      it.isNotEmpty() && !it.equals(cleanName, ignoreCase = true)
    } ?: remainingCategories.firstOrNull()
      ?: if (normalizedType == "IN") {
        if (isKu) "داهاتێ گشتی" else "General Income"
      } else {
        if (isKu) "مەسروفێ گشتی" else "General Expense"
      }

    // If user explicitly provided a custom target category that isn't a generic fallback, ensure it's in the list
    val genericFallbacks = setOf("مەسروفێ گشتی", "داهاتێ گشتی", "General Expense", "General Income", "مەسروف", "داهات", "Expense", "Income")
    val finalCustomCategories = if (
      !deleteAssociatedTransactions &&
      !reassignToCategory.isNullOrBlank() &&
      genericFallbacks.none { it.equals(resolvedTargetCategory, ignoreCase = true) } &&
      remainingCategories.none { it.equals(resolvedTargetCategory, ignoreCase = true) }
    ) {
      remainingCategories + resolvedTargetCategory
    } else {
      remainingCategories
    }

    val updatedTransactions = if (deleteAssociatedTransactions) {
      _uiState.value.transactions.filterNot {
        it.type.equals(normalizedType, ignoreCase = true) &&
          it.category.trim().equals(cleanName, ignoreCase = true)
      }
    } else {
      _uiState.value.transactions.map { tx ->
        if (
          tx.type.equals(normalizedType, ignoreCase = true) &&
          tx.category.trim().equals(cleanName, ignoreCase = true)
        ) {
          tx.copy(category = resolvedTargetCategory)
        } else {
          tx
        }
      }
    }

    val updatedBudgets = if (normalizedType == "OUT") {
      if (deleteAssociatedTransactions) {
        _uiState.value.budgets.filterNot { it.category.trim().equals(cleanName, ignoreCase = true) }
      } else {
        _uiState.value.budgets.map { b ->
          if (b.category.trim().equals(cleanName, ignoreCase = true)) {
            b.copy(category = resolvedTargetCategory)
          } else {
            b
          }
        }
      }
    } else {
      _uiState.value.budgets
    }

    if (normalizedType == "IN") {
      saveCustomCategories("custom_income_categories_v1", finalCustomCategories)
      _uiState.value = _uiState.value.copy(
        customIncomeCategories = finalCustomCategories,
        transactions = updatedTransactions,
        budgets = updatedBudgets,
        expenseCategories = _uiState.value.expenseCategories.filterNot {
          it.type.equals("IN", ignoreCase = true) && it.name.equals(cleanName, ignoreCase = true)
        }
      )
    } else {
      saveCustomCategories("custom_expense_categories_v1", finalCustomCategories)
      _uiState.value = _uiState.value.copy(
        customExpenseCategories = finalCustomCategories,
        transactions = updatedTransactions,
        budgets = updatedBudgets,
        expenseCategories = _uiState.value.expenseCategories.filterNot {
          !it.type.equals("IN", ignoreCase = true) && it.name.equals(cleanName, ignoreCase = true)
        }
      )
    }

    viewModelScope.launch {
      if (
        !deleteAssociatedTransactions &&
        !reassignToCategory.isNullOrBlank() &&
        genericFallbacks.none { it.equals(resolvedTargetCategory, ignoreCase = true) }
      ) {
        repository.addExpenseCategory(name = resolvedTargetCategory, type = normalizedType)
      }
      repository.deleteExpenseCategory(
        name = cleanName,
        type = normalizedType,
        deleteAssociatedTransactions = deleteAssociatedTransactions,
        reassignToCategory = resolvedTargetCategory
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteExpenseCategoryById(
    id: Long,
    deleteAssociatedTransactions: Boolean = false,
    reassignToCategory: String? = null
  ) {
    val target = _uiState.value.expenseCategories.find { it.id == id }
    if (target != null) {
      deleteExpenseCategory(
        name = target.name,
        type = target.type,
        deleteAssociatedTransactions = deleteAssociatedTransactions,
        reassignToCategory = reassignToCategory
      )
    } else {
      viewModelScope.launch {
        repository.deleteExpenseCategoryById(
          id = id,
          deleteAssociatedTransactions = deleteAssociatedTransactions,
          reassignToCategory = reassignToCategory
        )
        cloudBackupService.createCloudBackup()
      }
    }
  }

  fun addCustomCategory(type: String, rawName: String) {
    addExpenseCategory(name = rawName, type = type)
  }

  fun removeCustomCategory(
    type: String,
    rawName: String,
    deleteAssociatedTransactions: Boolean = false,
    reassignToCategory: String? = null
  ) {
    deleteExpenseCategory(
      name = rawName,
      type = type,
      deleteAssociatedTransactions = deleteAssociatedTransactions,
      reassignToCategory = reassignToCategory
    )
  }

  fun addTransaction(
    type: String,
    amount: Double,
    category: String,
    currency: String,
    notes: String = "",
    dateMillis: Long = System.currentTimeMillis(),
    affectsCapital: Boolean = true,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val cleanCategory = category.trim().ifBlank {
        if (type == "IN") "داهات" else "مەسروف"
      }
      val toInsert = TransactionEntity(
        type = type,
        amount = amount,
        category = cleanCategory,
        currency = currency,
        notes = notes,
        dateMillis = dateMillis,
        affectsCapital = affectsCapital,
        exchangeRateAtEntry = entryRate
      )
      val id = repository.insertTransaction(toInsert)
      val inserted = toInsert.copy(id = id)
      val isSchool = _uiState.value.isSchoolTransaction(inserted)
      if (!isSchool && cleanCategory != "Capital Addition" && cleanCategory != "داهات" && cleanCategory != "مەسروف" && cleanCategory != "Income" && cleanCategory != "Expense") {
        addCustomCategory(type, cleanCategory)
      }
      recordReversibleAction(
        section = if (isSchool) ActionSection.SCHOOL else ActionSection.CASH,
        operationType = ActionOperationType.ADD,
        titleKu = if (isSchool) "مەسروفا قوتابخانێ: $cleanCategory" else if (type == "IN") "هاتنا پارەی: $cleanCategory" else "چوونا پارەی: $cleanCategory",
        titleEn = if (isSchool) "School Expense: $cleanCategory" else if (type == "IN") "Cash Inflow: $cleanCategory" else "Expense Out: $cleanCategory",
        subtitleKu = "${CurrencyService.format(amount, currency)} • ${if (affectsCapital) "ژ کاشی هاتە کێمکرن" else "ژ دەرڤەی کاشی"}",
        subtitleEn = "${CurrencyService.format(amount, currency)} • ${if (affectsCapital) "Deducted from Cash" else "Separate from Cash"}",
        snapshot = ActionPayloadSnapshot.TransactionSnapshot(before = null, after = inserted)
      )
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun updateTransaction(transaction: TransactionEntity) {
    viewModelScope.launch {
      val before = _uiState.value.transactions.find { it.id == transaction.id }
      repository.updateTransaction(transaction)
      val isSchool = _uiState.value.isSchoolTransaction(transaction)
      recordReversibleAction(
        section = if (isSchool) ActionSection.SCHOOL else ActionSection.CASH,
        operationType = ActionOperationType.EDIT,
        titleKu = "دەستکاریا مامەلێ: ${transaction.category}",
        titleEn = "Edited Transaction: ${transaction.category}",
        subtitleKu = "${CurrencyService.format(transaction.amount, transaction.currency)} • ${if (transaction.affectsCapital) "ژ کاشی" else "ژ دەرڤەی کاشی"}",
        subtitleEn = "${CurrencyService.format(transaction.amount, transaction.currency)} • ${if (transaction.affectsCapital) "Affects Cash" else "Outside Cash"}",
        snapshot = ActionPayloadSnapshot.TransactionSnapshot(before = before, after = transaction)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun addSchoolExpense(
    amount: Double,
    currency: String,
    subCategory: String = "",
    notes: String = "",
    deductFromCash: Boolean = true,
    dateMillis: Long = System.currentTimeMillis(),
    customIqdRate: Double? = null
  ) {
    val cleanSub = subCategory.trim()
    val formattedCategory = when {
      cleanSub.isBlank() || cleanSub == "قوتابخانە" || cleanSub.equals("School & Education", ignoreCase = true) -> "قوتابخانە"
      cleanSub.startsWith("قوتابخانە") || cleanSub.startsWith("School", ignoreCase = true) -> cleanSub
      else -> "قوتابخانە: $cleanSub"
    }
    addTransaction(
      type = "OUT",
      amount = amount,
      category = formattedCategory,
      currency = currency,
      notes = notes,
      dateMillis = dateMillis,
      affectsCapital = deductFromCash,
      customIqdRate = customIqdRate
    )
  }

  fun toggleTransactionCashDeduction(transaction: TransactionEntity) {
    viewModelScope.launch {
      val updated = transaction.copy(affectsCapital = !transaction.affectsCapital)
      repository.updateTransaction(updated)
      val isSchool = _uiState.value.isSchoolTransaction(updated)
      recordReversibleAction(
        section = if (isSchool) ActionSection.SCHOOL else ActionSection.CASH,
        operationType = ActionOperationType.STATUS_CHANGE,
        titleKu = "گوهۆڕینا حسابا کاشی: ${updated.category}",
        titleEn = "Toggled Cash Impact: ${updated.category}",
        subtitleKu = if (updated.affectsCapital) "نوکە ژ کاشی دهێتە کێمکرن" else "نوکە ژ کاشی ناهێتە کێمکرن",
        subtitleEn = if (updated.affectsCapital) "Now deducted from Pure Cash" else "Now excluded from Pure Cash",
        snapshot = ActionPayloadSnapshot.TransactionSnapshot(before = transaction, after = updated)
      )
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteTransaction(id: Long) {
    viewModelScope.launch {
      val before = _uiState.value.transactions.find { it.id == id }
      repository.deleteTransaction(id)
      if (before != null) {
        val isSchool = _uiState.value.isSchoolTransaction(before)
        recordReversibleAction(
          section = if (isSchool) ActionSection.SCHOOL else ActionSection.CASH,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا مامەلێ: ${before.category}",
          titleEn = "Deleted Transaction: ${before.category}",
          subtitleKu = CurrencyService.format(before.amount, before.currency),
          subtitleEn = CurrencyService.format(before.amount, before.currency),
          snapshot = ActionPayloadSnapshot.TransactionSnapshot(before = before, after = null)
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.deleteDocumentFromSubcollection("transactions", id)
    }
  }

  fun deleteFinancialLogEntry(entry: FinancialLogEntry) {
    when {
      entry.sourceEntityType == "CAPITAL_BASE" -> {
        setStartingCapital(0.0, _uiState.value.settings.capitalCurrency)
      }
      entry.sourceEntityType == "TRANSACTION" -> {
        deleteTransaction(entry.sourceEntityId)
      }
      entry.sourceEntityType == "CAR_PAYMENT" -> {
        val payId = entry.paymentEntityId ?: entry.sourceEntityId
        deleteCarPayment(payId)
      }
      entry.sourceEntityType == "INVESTMENT" -> {
        deleteInvestment(entry.sourceEntityId)
      }
      entry.sourceEntityType == "GOLD" -> {
        deleteGoldHolding(entry.sourceEntityId)
      }
      entry.category == FinancialLogCategory.LOAN_REPAYMENT && entry.paymentEntityId != null -> {
        deleteDebtPayment(entry.paymentEntityId)
      }
      entry.category == FinancialLogCategory.LOAN_TAKEN || entry.sourceEntityType == "DEBT" -> {
        deleteDebtCredit(entry.sourceEntityId)
      }
    }
  }

  fun addInvestment(
    name: String,
    category: String,
    initialCapital: Double,
    currentValue: Double,
    currency: String,
    notes: String = "",
    isCoOwned: Boolean = false,
    partnerName: String = "",
    myInvestmentAmount: Double = 0.0,
    partnerInvestmentAmount: Double = 0.0,
    deductedFromCash: Boolean = true,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val myCap = if (isCoOwned && myInvestmentAmount > 0) myInvestmentAmount else initialCapital
      val partCap = if (isCoOwned) partnerInvestmentAmount else 0.0
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val toInsert = InvestmentEntity(
        name = name,
        category = category,
        initialCapital = initialCapital,
        currentValue = currentValue,
        currency = currency,
        notes = notes,
        isCoOwned = isCoOwned,
        partnerName = partnerName,
        myInvestmentAmount = myCap,
        partnerInvestmentAmount = partCap,
        deductedFromCash = deductedFromCash,
        exchangeRateAtEntry = entryRate
      )
      val id = repository.insertInvestment(toInsert)
      val inserted = toInsert.copy(id = id)
      recordReversibleAction(
        section = if (inserted.isCar) ActionSection.CAR else ActionSection.REAL_ESTATE,
        operationType = ActionOperationType.ADD,
        titleKu = "زێدەکرنا وەبەرهێنانێ: $name",
        titleEn = "Added Asset: $name",
        subtitleKu = CurrencyService.format(initialCapital, currency),
        subtitleEn = CurrencyService.format(initialCapital, currency),
        snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = null, after = inserted)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun sellInvestment(
    investmentId: Long,
    soldPrice: Double,
    notes: String = "",
    customIqdRate: Double? = null,
    addToCashWealth: Boolean? = null
  ) {
    viewModelScope.launch {
      val inv = _uiState.value.investments.find { it.id == investmentId }
      if (inv != null) {
        val entryRate = when {
          customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
          inv.exchangeRateAtEntry > 0.0 -> inv.exchangeRateAtEntry
          else -> _uiState.value.settings.iqdPerUsdRate
        }
        val updated = inv.copy(
          isSold = true,
          soldPrice = soldPrice,
          currentValue = if (soldPrice > 0.0) soldPrice else inv.currentValue,
          soldDateMillis = System.currentTimeMillis(),
          exchangeRateAtEntry = entryRate,
          deductedFromCash = addToCashWealth ?: inv.deductedFromCash,
          notes = if (notes.isNotBlank()) {
            if (inv.notes.isNotBlank()) "${inv.notes} | Sold: $notes" else "Sold: $notes"
          } else inv.notes
        )
        repository.updateInvestment(updated)
        recordReversibleAction(
          section = if (inv.isCar) ActionSection.CAR else ActionSection.REAL_ESTATE,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "فرۆتنا ملوک/پرۆژە: ${inv.name}",
          titleEn = "Sold Asset: ${inv.name}",
          subtitleKu = CurrencyService.format(soldPrice, inv.currency),
          subtitleEn = CurrencyService.format(soldPrice, inv.currency),
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = inv, after = updated)
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun reopenInvestmentAsActive(investmentId: Long) {
    viewModelScope.launch {
      val inv = _uiState.value.investments.find { it.id == investmentId }
      if (inv != null) {
        val updated = inv.copy(
          isSold = false,
          soldPrice = 0.0,
          soldDateMillis = 0L
        )
        repository.updateInvestment(updated)
        recordReversibleAction(
          section = if (inv.isCar) ActionSection.CAR else ActionSection.REAL_ESTATE,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "زڤڕاندنا پرۆژەی بۆ ئەکتیڤ: ${inv.name}",
          titleEn = "Reopened Asset: ${inv.name}",
          subtitleKu = "بارودۆخ هاتە زڤڕاندن",
          subtitleEn = "Restored to active status",
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = inv, after = updated)
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun updateInvestment(investment: InvestmentEntity) {
    viewModelScope.launch {
      val before = _uiState.value.investments.find { it.id == investment.id }
      repository.updateInvestment(investment)
      recordReversibleAction(
        section = if (investment.isCar) ActionSection.CAR else ActionSection.REAL_ESTATE,
        operationType = ActionOperationType.EDIT,
        titleKu = "دەستکاریا ملوک/سەیارە: ${investment.name}",
        titleEn = "Edited Asset: ${investment.name}",
        subtitleKu = "بها: ${CurrencyService.format(investment.currentValue, investment.currency)}",
        subtitleEn = "Value: ${CurrencyService.format(investment.currentValue, investment.currency)}",
        snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = before, after = investment)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteInvestment(id: Long) {
    viewModelScope.launch {
      val before = _uiState.value.investments.find { it.id == id }
      val deletedServices = _uiState.value.carServices.filter { it.carInvestmentId == id }
      val deletedPayments = _uiState.value.carPayments.filter { it.carId == id }
      val deletedRecords = _uiState.value.monthlyRecords.filter { it.investmentId == id }
      repository.deleteInvestment(id)
      if (before != null) {
        recordReversibleAction(
          section = if (before.isCar) ActionSection.CAR else ActionSection.REAL_ESTATE,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا ملوک/سەیارە: ${before.name}",
          titleEn = "Deleted Asset: ${before.name}",
          subtitleKu = CurrencyService.format(before.initialCapital, before.currency),
          subtitleEn = CurrencyService.format(before.initialCapital, before.currency),
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(
            before = before,
            after = null,
            deletedServices = deletedServices,
            deletedPayments = deletedPayments,
            deletedMonthlyRecords = deletedRecords
          )
        )
      }
      deletedServices.forEach { firebaseDataManager.deleteDocumentFromSubcollection("car_services", it.id) }
      deletedPayments.forEach { firebaseDataManager.deleteDocumentFromSubcollection("car_payments", it.id) }
      deletedRecords.forEach { firebaseDataManager.deleteDocumentFromSubcollection("monthly_records", it.id) }
      firebaseDataManager.deleteDocumentFromSubcollection("investments", id)
      cloudBackupService.createCloudBackup()
    }
  }

  fun addCarService(
    carInvestmentId: Long,
    carName: String,
    serviceType: String,
    cost: Double,
    currency: String,
    paidBy: String = "ME", // "ME", "PARTNER", "SPLIT"
    partnerName: String = "Partner",
    notes: String = "",
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val toInsert = CarServiceEntity(
        carInvestmentId = carInvestmentId,
        carName = carName,
        serviceType = serviceType,
        cost = cost,
        currency = currency,
        paidBy = paidBy,
        partnerName = partnerName,
        dateMillis = System.currentTimeMillis(),
        notes = notes,
        exchangeRateAtEntry = entryRate
      )
      val id = repository.insertCarService(toInsert)
      val inserted = toInsert.copy(id = id)
      recordReversibleAction(
        section = ActionSection.CAR,
        operationType = ActionOperationType.ADD,
        titleKu = "مەسروفا سەیارێ ($carName): $serviceType",
        titleEn = "Car Service ($carName): $serviceType",
        subtitleKu = CurrencyService.format(cost, currency),
        subtitleEn = CurrencyService.format(cost, currency),
        snapshot = ActionPayloadSnapshot.CarServiceSnapshot(before = null, after = inserted)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteCarService(id: Long) {
    viewModelScope.launch {
      val before = _uiState.value.carServices.find { it.id == id }
      repository.deleteCarService(id)
      if (before != null) {
        recordReversibleAction(
          section = ActionSection.CAR,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا سێرڤسا سەیارێ: ${before.serviceType}",
          titleEn = "Deleted Car Service: ${before.serviceType}",
          subtitleKu = CurrencyService.format(before.cost, before.currency),
          subtitleEn = CurrencyService.format(before.cost, before.currency),
          snapshot = ActionPayloadSnapshot.CarServiceSnapshot(before = before, after = null)
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.deleteDocumentFromSubcollection("car_services", id)
    }
  }

  fun addCarPayment(
    carId: Long,
    carName: String,
    amount: Double,
    currency: String = "USD",
    paymentDateMillis: Long = System.currentTimeMillis(),
    paymentMonth: String = "",
    paidBy: String = "ME", // "ME", "PARTNER", "SPLIT"
    partnerName: String = "Partner",
    myShare: Double = 0.0,
    partnerShare: Double = 0.0,
    notes: String = "",
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val parentBefore = _uiState.value.investments.find { it.id == carId }
      val toInsert = CarPaymentEntity(
        carId = carId,
        carName = carName,
        amount = amount,
        currency = currency,
        paymentDateMillis = paymentDateMillis,
        paymentMonth = paymentMonth,
        paidBy = paidBy,
        partnerName = partnerName,
        myShare = myShare,
        partnerShare = partnerShare,
        notes = notes,
        exchangeRateAtEntry = entryRate
      )
      val id = repository.insertCarPayment(toInsert)
      val inserted = toInsert.copy(id = id)

      // If car is on installments and this payment finishes all remaining balance, mark completed
      if (parentBefore != null && parentBefore.soldPrice > 0) {
        val totalPaidAfter = _uiState.value.getCarTotalPaid(parentBefore) + amount
        if (totalPaidAfter >= parentBefore.soldPrice - 0.001) {
          repository.updateInvestment(parentBefore.copy(isSold = true))
        }
      }

      recordReversibleAction(
        section = ActionSection.CAR,
        operationType = ActionOperationType.ADD,
        titleKu = "وەرگرتنا قستێ سەیارێ: $carName",
        titleEn = "Car Installment Received: $carName",
        subtitleKu = CurrencyService.format(amount, currency),
        subtitleEn = CurrencyService.format(amount, currency),
        snapshot = ActionPayloadSnapshot.CarPaymentSnapshot(
          before = null,
          after = inserted,
          parentCarBefore = parentBefore
        )
      )

      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteCarPayment(id: Long) {
    viewModelScope.launch {
      val payment = _uiState.value.carPayments.find { it.id == id }
      val parentBefore = payment?.let { p -> _uiState.value.investments.find { it.id == p.carId } }
      repository.deleteCarPayment(id)
      if (payment != null) {
        if (parentBefore != null && parentBefore.soldPrice > 0.0 && parentBefore.isSold) {
          val remainingPaid = _uiState.value.carPayments
            .filter { it.carId == parentBefore.id && it.id != id }
            .sumOf { it.amount }
          if (remainingPaid < parentBefore.soldPrice - 0.001) {
            repository.updateInvestment(parentBefore.copy(isSold = false))
          }
        }
        recordReversibleAction(
          section = ActionSection.CAR,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا قستێ سەیارێ: ${payment.carName}",
          titleEn = "Deleted Car Installment: ${payment.carName}",
          subtitleKu = CurrencyService.format(payment.amount, payment.currency),
          subtitleEn = CurrencyService.format(payment.amount, payment.currency),
          snapshot = ActionPayloadSnapshot.CarPaymentSnapshot(
            before = payment,
            after = null,
            parentCarBefore = parentBefore
          )
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.deleteDocumentFromSubcollection("car_payments", id)
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun clearAllCarPayments() {
    viewModelScope.launch {
      repository.clearAllCarPayments()
      cloudBackupService.createCloudBackup()
      firebaseDataManager.clearSubcollection("car_payments")
    }
  }

  fun markCarAsSold(carId: Long, soldPrice: Double, notes: String = "", customIqdRate: Double? = null) {
    viewModelScope.launch {
      val inv = _uiState.value.investments.find { it.id == carId }
      if (inv != null) {
        val entryRate = when {
          customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
          inv.exchangeRateAtEntry > 0.0 -> inv.exchangeRateAtEntry
          else -> _uiState.value.settings.iqdPerUsdRate
        }
        val updated = inv.copy(
          isSold = true,
          soldPrice = soldPrice,
          currentValue = if (soldPrice > 0.0) soldPrice else inv.currentValue,
          soldDateMillis = System.currentTimeMillis(),
          exchangeRateAtEntry = entryRate,
          notes = if (notes.isNotBlank()) {
            if (inv.notes.isNotBlank()) "${inv.notes} | Sold: $notes" else "Sold: $notes"
          } else inv.notes
        )
        repository.updateInvestment(updated)
        recordReversibleAction(
          section = ActionSection.CAR,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "فرۆتنا سەیارێ: ${inv.name}",
          titleEn = "Sold Car: ${inv.name}",
          subtitleKu = CurrencyService.format(soldPrice, inv.currency),
          subtitleEn = CurrencyService.format(soldPrice, inv.currency),
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = inv, after = updated)
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun sellCarOnInstallments(
    carId: Long,
    soldPrice: Double,
    downPaymentReceived: Double,
    notes: String = "",
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val inv = _uiState.value.investments.find { it.id == carId }
      if (inv != null) {
        val entryRate = when {
          customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
          inv.exchangeRateAtEntry > 0.0 -> inv.exchangeRateAtEntry
          else -> _uiState.value.settings.iqdPerUsdRate
        }
        val isFullyPaid = soldPrice > 0 && downPaymentReceived >= (soldPrice - 0.001)
        val updated = inv.copy(
          isSold = isFullyPaid,
          soldPrice = soldPrice,
          currentValue = soldPrice,
          soldDateMillis = System.currentTimeMillis(),
          exchangeRateAtEntry = entryRate,
          notes = if (notes.isNotBlank()) {
            if (inv.notes.isNotBlank()) "${inv.notes} | Installment Sale: $notes" else "Installment Sale: $notes"
          } else inv.notes
        )
        repository.updateInvestment(updated)

        var addedPayId: Long? = null
        if (downPaymentReceived > 0) {
          val monthStr = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
          addedPayId = repository.insertCarPayment(
            CarPaymentEntity(
              carId = carId,
              carName = inv.name,
              amount = downPaymentReceived,
              currency = inv.currency,
              paymentDateMillis = System.currentTimeMillis(),
              paymentMonth = monthStr,
              paidBy = if (inv.isCoOwned) "CUSTOMER" else "ME",
              partnerName = inv.partnerName.ifBlank { "Partner" },
              myShare = if (inv.isCoOwned) downPaymentReceived * (inv.mySharePercent / 100.0) else downPaymentReceived,
              partnerShare = if (inv.isCoOwned) downPaymentReceived * (inv.partnerSharePercent / 100.0) else 0.0,
              notes = "پێشەکی واصل / Down Payment (واصل)",
              exchangeRateAtEntry = entryRate
            )
          )
        }
        recordReversibleAction(
          section = ActionSection.CAR,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "فرۆتنا سەیارێ ب قست: ${inv.name}",
          titleEn = "Installment Car Sale: ${inv.name}",
          subtitleKu = "فرۆتن: ${CurrencyService.format(soldPrice, inv.currency)} • واصل: ${CurrencyService.format(downPaymentReceived, inv.currency)}",
          subtitleEn = "Sale: ${CurrencyService.format(soldPrice, inv.currency)} • Down: ${CurrencyService.format(downPaymentReceived, inv.currency)}",
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(
            before = inv,
            after = updated,
            extraCarPaymentAddedId = addedPayId
          )
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun addCarWithInstallments(
    name: String,
    purchasePrice: Double,
    currency: String,
    notes: String = "",
    isCoOwned: Boolean = false,
    partnerName: String = "",
    myInvestmentAmount: Double = 0.0,
    partnerInvestmentAmount: Double = 0.0,
    soldPrice: Double = 0.0,
    downPaymentReceived: Double = 0.0,
    deductedFromCash: Boolean = true,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val isFullyPaid = soldPrice > 0 && downPaymentReceived >= (soldPrice - 0.001)
      val toInsert = InvestmentEntity(
        name = name,
        category = if (isCoOwned) "Partner Car" else "Cars & Vehicles",
        initialCapital = purchasePrice,
        currentValue = if (soldPrice > 0) soldPrice else purchasePrice,
        currency = currency,
        notes = notes,
        isCoOwned = isCoOwned,
        partnerName = partnerName,
        myInvestmentAmount = if (isCoOwned && myInvestmentAmount > 0) myInvestmentAmount else purchasePrice,
        partnerInvestmentAmount = if (isCoOwned) partnerInvestmentAmount else 0.0,
        isSold = isFullyPaid,
        soldPrice = soldPrice,
        deductedFromCash = deductedFromCash,
        soldDateMillis = if (soldPrice > 0) System.currentTimeMillis() else 0L,
        exchangeRateAtEntry = entryRate
      )
      val invId = repository.insertInvestment(toInsert)
      val inserted = toInsert.copy(id = invId)

      var addedPayId: Long? = null
      if (downPaymentReceived > 0) {
        val monthStr = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
        addedPayId = repository.insertCarPayment(
          CarPaymentEntity(
            carId = invId,
            carName = name,
            amount = downPaymentReceived,
            currency = currency,
            paymentDateMillis = System.currentTimeMillis(),
            paymentMonth = monthStr,
            paidBy = if (isCoOwned) "CUSTOMER" else "ME",
            partnerName = partnerName.ifBlank { "Partner" },
            myShare = if (isCoOwned && purchasePrice > 0) downPaymentReceived * (myInvestmentAmount / purchasePrice) else downPaymentReceived,
            partnerShare = if (isCoOwned && purchasePrice > 0) downPaymentReceived * (partnerInvestmentAmount / purchasePrice) else 0.0,
            notes = "پێشەکی واصل / Down Payment (واصل)",
            exchangeRateAtEntry = entryRate
          )
        )
      }
      recordReversibleAction(
        section = ActionSection.CAR,
        operationType = ActionOperationType.ADD,
        titleKu = "زێدەکرنا سەیارێ: $name",
        titleEn = "Added Car: $name",
        subtitleKu = CurrencyService.format(purchasePrice, currency),
        subtitleEn = CurrencyService.format(purchasePrice, currency),
        snapshot = ActionPayloadSnapshot.InvestmentSnapshot(
          before = null,
          after = inserted,
          extraCarPaymentAddedId = addedPayId
        )
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun reopenCarAsActive(carId: Long) {
    viewModelScope.launch {
      val inv = _uiState.value.investments.find { it.id == carId }
      if (inv != null) {
        val updated = inv.copy(isSold = false)
        repository.updateInvestment(updated)
        recordReversibleAction(
          section = ActionSection.CAR,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "زڤڕاندنا سەیارێ بۆ ئەکتیڤ: ${inv.name}",
          titleEn = "Reopened Car: ${inv.name}",
          subtitleKu = "بارودۆخ هاتە زڤڕاندن",
          subtitleEn = "Restored to active fleet",
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = inv, after = updated)
        )
        cloudBackupService.createCloudBackup()
        firebaseDataManager.triggerBackgroundFirestoreSync()
      }
    }
  }

  fun recordMonthlyGainLoss(investmentId: Long, yearMonth: String, gainLossAmount: Double, newTotalValuation: Double) {
    viewModelScope.launch {
      repository.insertMonthlyRecord(
        InvestmentMonthRecordEntity(
          investmentId = investmentId,
          yearMonth = yearMonth,
          gainLossAmount = gainLossAmount,
          recordedValue = newTotalValuation
        )
      )
      // Update the investment's currentValue
      val inv = _uiState.value.investments.find { it.id == investmentId }
      if (inv != null) {
        val updated = inv.copy(currentValue = newTotalValuation)
        repository.updateInvestment(updated)
        recordReversibleAction(
          section = if (inv.isCar) ActionSection.CAR else ActionSection.REAL_ESTATE,
          operationType = ActionOperationType.EDIT,
          titleKu = "نویکرنا بهایێ پرۆژەی: ${inv.name}",
          titleEn = "Updated Valuation: ${inv.name}",
          subtitleKu = CurrencyService.format(newTotalValuation, inv.currency),
          subtitleEn = CurrencyService.format(newTotalValuation, inv.currency),
          snapshot = ActionPayloadSnapshot.InvestmentSnapshot(before = inv, after = updated)
        )
      }
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun addDebtCredit(
    type: String,
    personName: String,
    contactInfo: String,
    totalAmount: Double,
    amountPaid: Double,
    currency: String,
    dueDateMillis: Long,
    notes: String,
    reminderEnabled: Boolean,
    affectCashBalance: Boolean = false,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val toInsert = DebtCreditEntity(
        type = type,
        personName = personName,
        contactInfo = contactInfo,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        currency = currency,
        dueDateMillis = dueDateMillis,
        notes = notes,
        reminderEnabled = reminderEnabled,
        affectCashBalance = affectCashBalance,
        isCompleted = amountPaid >= totalAmount,
        exchangeRateAtEntry = entryRate,
        cashImpactAmount = if (affectCashBalance) totalAmount else 0.0,
        cashPaidImpactAmount = if (affectCashBalance) amountPaid else 0.0
      )
      val id = repository.insertDebtCredit(toInsert)
      val inserted = toInsert.copy(id = id)
      recordReversibleAction(
        section = ActionSection.DEBT,
        operationType = ActionOperationType.ADD,
        titleKu = if (type == "DEBT") "تۆمارکرنا قەرزێ لسەر من: $personName" else "تۆمارکرنا قەرزێ دەف خەلکی: $personName",
        titleEn = if (type == "DEBT") "Added Debt Owed: $personName" else "Added Credit Receivable: $personName",
        subtitleKu = CurrencyService.format(totalAmount, currency),
        subtitleEn = CurrencyService.format(totalAmount, currency),
        snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(before = null, after = inserted)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun addRealEstateDebtCredit(
    type: String, // "CREDIT" (من دا یە ئێکی ب قەرز) or "DEBT" (قەرز لسەر منە)
    personName: String,
    amount: Double,
    currency: String,
    notes: String = "",
    dueDateMillis: Long = System.currentTimeMillis() + 30L * 24L * 3600L * 1000L
  ) {
    val realEstateNotes = if (notes.isNotBlank()) "[REAL_ESTATE_DEBT] $notes" else "[REAL_ESTATE_DEBT]"
    addDebtCredit(
      type = type,
      personName = personName,
      contactInfo = "",
      totalAmount = amount,
      amountPaid = 0.0,
      currency = currency,
      dueDateMillis = dueDateMillis,
      notes = realEstateNotes,
      reminderEnabled = false,
      affectCashBalance = false
    )
  }

  fun toggleDebtCreditCashImpact(debtId: Long) {
    viewModelScope.launch {
      val existing = _uiState.value.debtsCredits.find { it.id == debtId } ?: return@launch
      val nextAffect = !existing.affectCashBalance
      val nextCashImpact = if (nextAffect) {
        if (existing.cashImpactAmount > 0.0) existing.cashImpactAmount else existing.totalAmount
      } else {
        0.0
      }
      val updated = existing.copy(
        affectCashBalance = nextAffect,
        cashImpactAmount = nextCashImpact
      )
      repository.updateDebtCredit(updated)
      recordReversibleAction(
        section = ActionSection.DEBT,
        operationType = ActionOperationType.STATUS_CHANGE,
        titleKu = "گوهۆڕینا کاریگەریا کاشی: ${existing.personName}",
        titleEn = "Toggled Debt Cash Impact: ${existing.personName}",
        subtitleKu = if (nextAffect) "کاریگەری لسەر کاشی چالاکە" else "بێ کاریگەری لسەر کاشی",
        subtitleEn = if (nextAffect) "Cash impact enabled" else "Cash impact disabled",
        snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(before = existing, after = updated)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun toggleDebtRealEstateClassification(debtId: Long) {
    viewModelScope.launch {
      val existing = _uiState.value.debtsCredits.find { it.id == debtId } ?: return@launch
      val isNowRealEstate = existing.isRealEstateDebt
      val updatedNotes = if (isNowRealEstate) {
        existing.notes.replace("[REAL_ESTATE_DEBT]", "").trim()
      } else {
        if (existing.notes.isNotBlank()) "[REAL_ESTATE_DEBT] ${existing.notes}" else "[REAL_ESTATE_DEBT]"
      }
      val updated = existing.copy(notes = updatedNotes)
      repository.updateDebtCredit(updated)
      recordReversibleAction(
        section = ActionSection.DEBT,
        operationType = ActionOperationType.EDIT,
        titleKu = if (!isNowRealEstate) "گواستنەوە بۆ حسابا تایبەت: ${existing.personName}" else "گواستنەوە بۆ قەرزێن گشتی: ${existing.personName}",
        titleEn = if (!isNowRealEstate) "Moved to Private Account: ${existing.personName}" else "Moved to General Debts: ${existing.personName}",
        subtitleKu = existing.personName,
        subtitleEn = existing.personName,
        snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(before = existing, after = updated)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun recordLoanPayment(
    debtId: Long,
    paymentAmount: Double,
    notes: String,
    affectCashBalance: Boolean = true,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val before = _uiState.value.debtsCredits.find { it.id == debtId }
      val insertedPay = repository.recordDebtPayment(debtId, paymentAmount, notes, affectCashBalance, customIqdRate)
      if (before != null) {
        val newPaid = before.amountPaid + paymentAmount
        val after = before.copy(
          amountPaid = newPaid,
          isCompleted = newPaid >= before.totalAmount,
          cashPaidImpactAmount = if (affectCashBalance) before.effectiveCashPaidImpact + paymentAmount else before.effectiveCashPaidImpact
        )
        recordReversibleAction(
          section = ActionSection.DEBT,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = if (before.type == "DEBT") "قەرز دانەڤە: ${before.personName}" else "وەرگرتنەڤا قەرزی: ${before.personName}",
          titleEn = if (before.type == "DEBT") "Paid Debt: ${before.personName}" else "Collected Credit: ${before.personName}",
          subtitleKu = CurrencyService.format(paymentAmount, before.currency),
          subtitleEn = CurrencyService.format(paymentAmount, before.currency),
          snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(
            before = before,
            after = after,
            addedPayment = insertedPay
          )
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun settleDebtCreditInFull(
    debtId: Long,
    notes: String = "Full Settlement",
    affectCashBalance: Boolean = true,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val before = _uiState.value.debtsCredits.find { it.id == debtId }
      val insertedPay = repository.settleDebtCreditInFull(debtId, notes, affectCashBalance, customIqdRate)
      if (before != null) {
        val remaining = before.remainingAmount
        val after = before.copy(
          amountPaid = before.totalAmount,
          isCompleted = true,
          cashPaidImpactAmount = if (affectCashBalance) before.effectiveCashPaidImpact + remaining else before.effectiveCashPaidImpact
        )
        recordReversibleAction(
          section = ActionSection.DEBT,
          operationType = ActionOperationType.STATUS_CHANGE,
          titleKu = "داخستنا تەماما قەرزی: ${before.personName}",
          titleEn = "Settled Loan in Full: ${before.personName}",
          subtitleKu = CurrencyService.format(remaining, before.currency),
          subtitleEn = CurrencyService.format(remaining, before.currency),
          snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(
            before = before,
            after = after,
            addedPayment = insertedPay
          )
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun updateDebtCredit(
    id: Long,
    type: String,
    personName: String,
    contactInfo: String,
    totalAmount: Double,
    amountPaid: Double,
    currency: String,
    dueDateMillis: Long,
    notes: String,
    reminderEnabled: Boolean,
    affectCashBalance: Boolean? = null,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val existing = _uiState.value.debtsCredits.find { it.id == id }
      val nextAffect = affectCashBalance ?: existing?.affectCashBalance ?: false
      val entryRate = when {
        customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
        existing != null && existing.exchangeRateAtEntry > 0.0 -> existing.exchangeRateAtEntry
        else -> _uiState.value.settings.iqdPerUsdRate
      }
      // If user edits a loan and increases totalAmount (e.g. $4,700 -> $4,800) with affectCash = true,
      // only add the incremental delta ($100) to the previous cash impact instead of the full $4,800!
      val nextCashImpact = if (!nextAffect) {
        0.0
      } else if (existing != null) {
        val oldCashPrincipal = existing.effectiveCashPrincipalImpact
        val deltaPrincipal = totalAmount - existing.totalAmount
        if (!existing.affectCashBalance && deltaPrincipal > 0.0) {
          deltaPrincipal
        } else if (existing.affectCashBalance) {
          (oldCashPrincipal + deltaPrincipal).coerceAtLeast(0.0)
        } else {
          totalAmount
        }
      } else {
        totalAmount
      }

      val nextCashPaidImpact = if (existing != null) {
        val oldPaid = existing.amountPaid
        val deltaPaid = amountPaid - oldPaid
        val basePaidCash = existing.effectiveCashPaidImpact
        if (nextAffect && deltaPaid > 0.0) {
          basePaidCash + deltaPaid
        } else {
          basePaidCash.coerceAtMost(amountPaid)
        }
      } else {
        if (nextAffect) amountPaid else 0.0
      }

      val updated = DebtCreditEntity(
        id = id,
        type = type,
        personName = personName,
        contactInfo = contactInfo,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        currency = currency,
        dueDateMillis = dueDateMillis,
        notes = notes,
        reminderEnabled = reminderEnabled,
        isCompleted = amountPaid >= totalAmount && totalAmount > 0.0,
        affectCashBalance = nextAffect,
        createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
        exchangeRateAtEntry = entryRate,
        cashImpactAmount = nextCashImpact,
        cashPaidImpactAmount = nextCashPaidImpact
      )
      repository.updateDebtCredit(updated)
      recordReversibleAction(
        section = ActionSection.DEBT,
        operationType = ActionOperationType.EDIT,
        titleKu = "دەستکاریا قەرز/دەین: $personName",
        titleEn = "Edited Debt/Credit: $personName",
        subtitleKu = "ماوە: ${CurrencyService.format(updated.remainingAmount, currency)}",
        subtitleEn = "Remaining: ${CurrencyService.format(updated.remainingAmount, currency)}",
        snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(before = existing, after = updated)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun addMoneyToLoan(
    debtId: Long,
    additionalAmount: Double,
    notes: String,
    affectCashBalance: Boolean? = null,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val debts = _uiState.value.debtsCredits
      val targetDebt = debts.find { it.id == debtId } ?: return@launch
      val newTotal = targetDebt.totalAmount + additionalAmount
      val shouldAffectCash = affectCashBalance ?: true
      val oldCashImpact = targetDebt.effectiveCashPrincipalImpact
      // CRITICAL: Only add additionalAmount (e.g., $100) to cash impact, NEVER the whole $4,800!
      val newCashImpact = if (shouldAffectCash) oldCashImpact + additionalAmount else oldCashImpact
      val entryRate = when {
        customIqdRate != null && customIqdRate > 0.0 -> customIqdRate
        targetDebt.exchangeRateAtEntry > 0.0 -> targetDebt.exchangeRateAtEntry
        else -> _uiState.value.settings.iqdPerUsdRate
      }
      val updatedNotes = if (notes.isNotBlank()) {
        if (targetDebt.notes.isNotBlank()) "${targetDebt.notes} | +${additionalAmount} ($notes)" else "+${additionalAmount} ($notes)"
      } else targetDebt.notes
      val updated = targetDebt.copy(
        totalAmount = newTotal,
        notes = updatedNotes,
        affectCashBalance = newCashImpact > 0.0,
        cashImpactAmount = newCashImpact,
        exchangeRateAtEntry = entryRate,
        isCompleted = targetDebt.amountPaid >= newTotal
      )
      repository.updateDebtCredit(updated)
      recordReversibleAction(
        section = ActionSection.DEBT,
        operationType = ActionOperationType.EDIT,
        titleKu = "زێدەکرنا پارەی لسەر قەرزی: ${targetDebt.personName}",
        titleEn = "Added Principal to Loan: ${targetDebt.personName}",
        subtitleKu = "+${CurrencyService.format(additionalAmount, targetDebt.currency)}",
        subtitleEn = "+${CurrencyService.format(additionalAmount, targetDebt.currency)}",
        snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(before = targetDebt, after = updated)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteDebtCredit(id: Long) {
    viewModelScope.launch {
      val before = _uiState.value.debtsCredits.find { it.id == id }
      val deletedPayments = _uiState.value.debtPayments.filter { it.debtId == id }
      repository.deleteDebtCredit(id)
      if (before != null) {
        recordReversibleAction(
          section = ActionSection.DEBT,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا قەرز/دەین: ${before.personName}",
          titleEn = "Deleted Debt/Credit: ${before.personName}",
          subtitleKu = CurrencyService.format(before.remainingAmount, before.currency),
          subtitleEn = CurrencyService.format(before.remainingAmount, before.currency),
          snapshot = ActionPayloadSnapshot.DebtCreditSnapshot(
            before = before,
            after = null,
            deletedPayments = deletedPayments
          )
        )
      }
      deletedPayments.forEach { pay ->
        firebaseDataManager.deleteDocumentFromSubcollection("debt_payments", pay.id)
      }
      firebaseDataManager.deleteDocumentFromSubcollection("debts_credits", id)
      cloudBackupService.createCloudBackup()
    }
  }

  fun deleteDebtPayment(paymentId: Long) {
    viewModelScope.launch {
      val payment = _uiState.value.debtPayments.find { it.id == paymentId }
      val parentBefore = payment?.let { p -> _uiState.value.debtsCredits.find { it.id == p.debtId } }
      repository.deleteDebtPayment(paymentId)
      if (payment != null) {
        recordReversibleAction(
          section = ActionSection.DEBT,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا دفعاتا قەرزی: ${parentBefore?.personName ?: ""}",
          titleEn = "Deleted Loan Payment: ${parentBefore?.personName ?: ""}",
          subtitleKu = CurrencyService.format(payment.amount, parentBefore?.currency ?: "USD"),
          subtitleEn = CurrencyService.format(payment.amount, parentBefore?.currency ?: "USD"),
          snapshot = ActionPayloadSnapshot.DebtPaymentSnapshot(
            before = payment,
            after = null,
            parentDebtBefore = parentBefore
          )
        )
      }
      firebaseDataManager.deleteDocumentFromSubcollection("debt_payments", paymentId)
      cloudBackupService.createCloudBackup()
    }
  }

  fun clearAllDebtPayments() {
    viewModelScope.launch {
      repository.clearAllDebtPayments()
      firebaseDataManager.clearSubcollection("debt_payments")
      cloudBackupService.createCloudBackup()
    }
  }

  fun addBudget(
    category: String,
    monthlyLimit: Double,
    currency: String,
    alertThreshold: Int = 80,
    customIqdRate: Double? = null
  ) {
    viewModelScope.launch {
      val entryRate = if (customIqdRate != null && customIqdRate > 0.0) customIqdRate else _uiState.value.settings.iqdPerUsdRate
      val toInsert = BudgetEntity(
        category = category,
        monthlyLimit = monthlyLimit,
        currency = currency,
        alertThresholdPercent = alertThreshold,
        exchangeRateAtEntry = entryRate
      )
      val id = repository.insertBudget(toInsert)
      val inserted = toInsert.copy(id = id)
      recordReversibleAction(
        section = ActionSection.BUDGET,
        operationType = ActionOperationType.ADD,
        titleKu = "زێدەکرنا بودجێ: $category",
        titleEn = "Added Budget: $category",
        subtitleKu = CurrencyService.format(monthlyLimit, currency),
        subtitleEn = CurrencyService.format(monthlyLimit, currency),
        snapshot = ActionPayloadSnapshot.BudgetSnapshot(before = null, after = inserted)
      )
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun updateBudget(budget: BudgetEntity) {
    viewModelScope.launch {
      val before = _uiState.value.budgets.find { it.id == budget.id }
      repository.updateBudget(budget)
      recordReversibleAction(
        section = ActionSection.BUDGET,
        operationType = ActionOperationType.EDIT,
        titleKu = "دەستکاریا بودجێ: ${budget.category}",
        titleEn = "Edited Budget: ${budget.category}",
        subtitleKu = CurrencyService.format(budget.monthlyLimit, budget.currency),
        subtitleEn = CurrencyService.format(budget.monthlyLimit, budget.currency),
        snapshot = ActionPayloadSnapshot.BudgetSnapshot(before = before, after = budget)
      )
      cloudBackupService.createCloudBackup()
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  fun deleteBudget(id: Long) {
    viewModelScope.launch {
      val before = _uiState.value.budgets.find { it.id == id }
      repository.deleteBudget(id)
      if (before != null) {
        recordReversibleAction(
          section = ActionSection.BUDGET,
          operationType = ActionOperationType.DELETE,
          titleKu = "ژێبرنا بودجێ: ${before.category}",
          titleEn = "Deleted Budget: ${before.category}",
          subtitleKu = CurrencyService.format(before.monthlyLimit, before.currency),
          subtitleEn = CurrencyService.format(before.monthlyLimit, before.currency),
          snapshot = ActionPayloadSnapshot.BudgetSnapshot(before = before, after = null)
        )
      }
      cloudBackupService.createCloudBackup()
      firebaseDataManager.deleteDocumentFromSubcollection("budgets", id)
    }
  }

  fun toggleBiometric(enabled: Boolean) {
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(
        biometricEnabled = enabled,
        isAppLocked = false
      )
      repository.updateSettings(settings)
    }
  }

  fun setPinCode(pin: String) {
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(pinCode = pin)
      repository.updateSettings(settings)
    }
  }

  fun lockApp() {
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(isAppLocked = true)
      repository.updateSettings(settings)
      _uiState.value = _uiState.value.copy(isAppLocked = true)
    }
  }

  fun lockAppOnBackgroundIfEnabled() {
    if (_uiState.value.settings.biometricEnabled) {
      lockApp()
    }
  }

  fun setSensitiveDataMasked(masked: Boolean) {
    _uiState.value = _uiState.value.copy(isSensitiveDataMasked = masked)
  }

  fun verifyPinCode(pin: String): Boolean {
    return pin == _uiState.value.settings.pinCode
  }

  fun unlockApp(pin: String? = null): Boolean {
    if (pin != null && pin != _uiState.value.settings.pinCode) {
      return false
    }
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(isAppLocked = false)
      repository.updateSettings(settings)
      _uiState.value = _uiState.value.copy(isAppLocked = false)
    }
    return true
  }

  fun setThemeMode(mode: String) {
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(themeMode = mode)
      repository.updateSettings(settings)
    }
  }

  fun setLanguage(language: String) {
    viewModelScope.launch {
      val settings = _uiState.value.settings.copy(language = language)
      repository.updateSettings(settings)
    }
  }

  fun toggleLanguage() {
    val currentLang = _uiState.value.settings.language
    val newLang = if (currentLang == "KU") "EN" else "KU"
    setLanguage(newLang)
  }

  fun performCloudSync() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSyncing = true, syncMessage = "Synchronizing with Cloud Firestore & encrypted backup...")
      val status = cloudBackupService.performCloudSync()
      val fsResult = firebaseDataManager.syncAllDataToFirestore()
      _uiState.value = _uiState.value.copy(
        isSyncing = false,
        syncMessage = if (fsResult.success) fsResult.message else status.statusMessage
      )
    }
  }

  fun signInWithGoogle(
    activityContext: Context,
    onResult: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val res = firebaseDataManager.signInWithGoogle(activityContext)
      res.fold(
        onSuccess = { userState ->
          onResult(true, userState.authStatusMessage)
        },
        onFailure = { err ->
          onResult(false, err.localizedMessage ?: "Google Sign-In requires Google account or Web Client ID")
        }
      )
    }
  }

  fun signInWithFirebaseEmail(
    email: String,
    password: String,
    displayName: String,
    onResult: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val res = firebaseDataManager.signInOrRegisterWithEmail(email, password, displayName)
      res.fold(
        onSuccess = { userState ->
          onResult(true, userState.authStatusMessage)
        },
        onFailure = { err ->
          onResult(false, err.localizedMessage ?: "Authentication failed")
        }
      )
    }
  }

  fun signOutFirebase(
    activityContext: Context? = null,
    onResult: () -> Unit = {}
  ) {
    viewModelScope.launch {
      firebaseDataManager.signOut(activityContext)
      onResult()
    }
  }

  fun syncToFirestore(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
    viewModelScope.launch {
      val result = firebaseDataManager.syncAllDataToFirestore()
      cloudBackupService.createCloudBackup()
      onResult(result.success, result.message)
    }
  }

  fun restoreFromFirestore(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
    viewModelScope.launch {
      val result = firebaseDataManager.restoreAllDataFromFirestore()
      onResult(result.success, result.message)
    }
  }

  fun runGroundedMarketSearch(query: String) {
    viewModelScope.launch {
      val state = _uiState.value
      val isKurdish = state.settings.language == "KU"
      val portfolioSummary = buildString {
        append("Display Currency: ${state.displayCurrency}, ")
        append("Saved IQD Rate: 1 USD = ${state.iqdRate.toInt()} IQD, ")
        append("Gold Holdings: ${"%.1f".format(state.totalGoldWeightGrams)}g (${"%.1f".format(state.totalGoldWeightGrams / 5.0)} Mithqals), ")
        val carNames = state.carInvestments.joinToString(", ") { it.name }
        if (carNames.isNotBlank()) append("Cars in Fleet: $carNames")
      }

      _uiState.value = _uiState.value.copy(isSearchGroundingLoading = true)
      val response = GeminiSearchGroundingService.searchWithGrounding(
        userQuery = query,
        isKurdish = isKurdish,
        portfolioContext = portfolioSummary
      )
      _uiState.value = _uiState.value.copy(
        isSearchGroundingLoading = false,
        lastGroundedSearchResult = response
      )
    }
  }

  fun restoreFromBackup(json: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val success = cloudBackupService.restoreFromBackup(json)
      onComplete(success)
    }
  }

  fun backupToDevice(context: Context, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val result = cloudBackupService.saveBackupToDevice(context)
      onResult(result.first, result.second)
    }
  }

  fun backupToEmail(
    context: Context,
    recipientEmail: String = CloudBackupService.DEFAULT_BACKUP_EMAIL,
    onResult: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val state = _uiState.value
      val summary = buildString {
        appendLine("• Master Capital: ${CurrencyService.format(state.startingCapitalDisplay, state.displayCurrency)}")
        appendLine("• Liquid Cashflow: ${CurrencyService.format(state.netCapital, state.displayCurrency)}")
        appendLine("• Gold Portfolio: ${"%.1f".format(state.totalGoldWeightGrams)}g (${"%.1f".format(state.totalGoldWeightGrams / 5.0)} Mithqals) - Valuation: ${CurrencyService.format(state.totalGoldCurrentValueDisplay, state.displayCurrency)}")
        appendLine("• Investments: ${state.investments.size} items - Valuation: ${CurrencyService.format(state.totalInvestmentValue, state.displayCurrency)}")
        appendLine("• Money Borrowed (Debts): ${CurrencyService.format(state.totalDebtOwed, state.displayCurrency)}")
        appendLine("• Partner Equity Share: ${CurrencyService.format(state.totalPartnerShareInInvestments, state.displayCurrency)}")
        appendLine("• Total Assets Valuation: ${CurrencyService.format(state.totalPhysicalAndCashAssets, state.displayCurrency)}")
        appendLine("• Net Remaining Wealth (صافی پارێت من): ${CurrencyService.format(state.netWorth, state.displayCurrency)}")
      }
      val result = cloudBackupService.sendBackupToEmail(context, recipientEmail, summary)
      onResult(result.first, result.second)
    }
  }

  fun restoreFromAutoBackup(context: Context, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val result = cloudBackupService.restoreAutoBackup(context)
      onResult(result.first, result.second)
    }
  }

  fun exportTransactionsCsv(): File {
    return reportExportService.exportTransactionsCsv(
      _uiState.value.transactions,
      _uiState.value.displayCurrency
    )
  }

  fun exportDebtsCsv(): File {
    return reportExportService.exportDebtsCreditsCsv(_uiState.value.debtsCredits)
  }

  fun exportInvestmentsCsv(): File {
    return reportExportService.exportInvestmentsCsv(_uiState.value.investments)
  }

  fun generateFinancialReportText(): String {
    val state = _uiState.value
    return reportExportService.generateFinancialStatementReport(
      startingCapital = state.startingCapitalDisplay,
      netCapital = state.netCapital,
      totalMoneyIn = state.totalMoneyIn,
      totalMoneyOut = state.totalMoneyOut,
      totalInvested = state.totalInvested,
      totalInvestmentValue = state.totalInvestmentValue,
      totalDebtOwed = state.totalDebtOwed,
      totalCreditReceivable = state.totalCreditReceivable,
      currency = state.displayCurrency,
      iqdRate = state.iqdRate,
      settings = state.settings,
      totalGoldValue = state.totalGoldCurrentValueDisplay,
      totalPartnerShare = state.totalPartnerShareInInvestments
    )
  }

  fun triggerManualSync(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
    offlineSyncManager.triggerManualSync(onComplete)
  }

  fun dismissAutoSyncSuccessBanner() {
    offlineSyncManager.dismissSuccessBanner()
  }
}
