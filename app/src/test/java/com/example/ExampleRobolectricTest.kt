package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Capital & Debt Tracker", appName)
  }

  @Test
  fun `currency conversion USD to IQD`() {
    val usdAmount = 100.0
    val iqdRate = 1310.0
    val iqdResult = CurrencyService.convert(usdAmount, "USD", "IQD", iqdRate)
    assertEquals(131000.0, iqdResult, 0.01)

    val backToUsd = CurrencyService.convert(iqdResult, "IQD", "USD", iqdRate)
    assertEquals(100.0, backToUsd, 0.01)
  }

  @Test
  fun `gold stock holding calculation - user scenario 210g at 127 bought and now 120`() {
    val goldHolding = GoldHoldingEntity(
      title = "Physical Gold Bullion",
      weightGrams = 210.0,
      buyPricePerGram = 127.0,
      karat = 24
    )

    // Verify initial total cost
    assertEquals(26670.0, goldHolding.totalCost, 0.01)

    // Current price dropped to 120 $ (Down)
    val dropPrice = 120.0
    val currentValueDrop = goldHolding.currentValue(dropPrice)
    assertEquals(25200.0, currentValueDrop, 0.01)

    val lossAmount = goldHolding.profitLoss(dropPrice)
    assertEquals(-1470.0, lossAmount, 0.01)

    val roiDrop = goldHolding.roiPercent(dropPrice)
    assertEquals(-5.5118, roiDrop, 0.01)
    assertTrue("Should be negative when price drops", lossAmount < 0)

    // Price rises to 135 $ (Rise and Up)
    val risePrice = 135.0
    val currentValueRise = goldHolding.currentValue(risePrice)
    assertEquals(28350.0, currentValueRise, 0.01)

    val gainAmount = goldHolding.profitLoss(risePrice)
    assertEquals(1680.0, gainAmount, 0.01)

    val roiRise = goldHolding.roiPercent(risePrice)
    assertEquals(6.2992, roiRise, 0.01)
    assertTrue("Should be positive when price rises", gainAmount > 0)
  }

  @Test
  fun `investment ROI calculation`() {
    val inv = InvestmentEntity(
      name = "Gold",
      category = "Gold",
      initialCapital = 1000.0,
      currentValue = 1250.0,
      currency = "USD"
    )
    assertEquals(250.0, inv.totalGainLoss, 0.01)
    assertEquals(25.0, inv.returnOnInvestmentPercent, 0.01)
  }

  @Test
  fun `car investment recognition and profit calculation`() {
    val carInvestment = InvestmentEntity(
      name = "Toyota Camry 2024",
      category = "Cars & Vehicles",
      initialCapital = 22000.0,
      currentValue = 25500.0,
      currency = "USD",
      notes = "Imported sedan for resale"
    )
    assertTrue("Should be recognized as a car investment", carInvestment.isCar)
    assertEquals(3500.0, carInvestment.totalGainLoss, 0.01)
    assertEquals(15.909, carInvestment.returnOnInvestmentPercent, 0.01)
  }

  @Test
  fun `debt and credit loan tracking calculations`() {
    val credit = DebtCreditEntity(
      type = "CREDIT",
      personName = "Ahmed",
      contactInfo = "+9647501234567",
      totalAmount = 500.0,
      amountPaid = 200.0,
      currency = "USD"
    )
    assertEquals(300.0, credit.remainingAmount, 0.01)
    assertFalse(credit.isCompleted)

    val debt = DebtCreditEntity(
      type = "DEBT",
      personName = "Ali Bank",
      contactInfo = "Branch #2",
      totalAmount = 1000.0,
      amountPaid = 1000.0,
      currency = "USD"
    )
    assertEquals(0.0, debt.remainingAmount, 0.01)
    assertTrue(debt.isCompleted)
  }

  @Test
  fun `unified net worth - debt and credit cancel out, repayments update income, gold is cash wealth, car sales and installments increase net worth`() {
    val baseState = com.example.ui.FinanceUiState(
      transactions = listOf(
        com.example.data.local.TransactionEntity(
          type = "IN",
          amount = 5000.0,
          currency = "USD",
          category = "Income"
        )
      )
    )
    val initialIncome = baseState.totalMoneyIn
    val initialNetWorth = baseState.netWorth

    // 1. Debt on people (CREDIT = 1000) and Debt on me (DEBT = 1000) cancel each other out!
    val offsetState = baseState.copy(
      debtsCredits = listOf(
        DebtCreditEntity(
          id = 1L,
          type = "DEBT",
          personName = "Lender A",
          contactInfo = "",
          totalAmount = 1000.0,
          amountPaid = 0.0,
          currency = "USD"
        ),
        DebtCreditEntity(
          id = 2L,
          type = "CREDIT",
          personName = "Borrower B",
          contactInfo = "",
          totalAmount = 1000.0,
          amountPaid = 0.0,
          currency = "USD"
        )
      )
    )
    assertEquals(0.0, offsetState.netDebtCreditBalance, 0.01)
    assertEquals(initialNetWorth, offsetState.netWorth, 0.01)

    // 2. Paying back 400 of DEBT decreases Income (totalMoneyIn) and Cash Wealth (netCapital) by 400
    val afterPayDebtState = offsetState.copy(
      debtsCredits = listOf(
        DebtCreditEntity(
          id = 1L,
          type = "DEBT",
          personName = "Lender A",
          contactInfo = "",
          totalAmount = 1000.0,
          amountPaid = 400.0,
          currency = "USD"
        ),
        DebtCreditEntity(
          id = 2L,
          type = "CREDIT",
          personName = "Borrower B",
          contactInfo = "",
          totalAmount = 1000.0,
          amountPaid = 0.0,
          currency = "USD"
        )
      )
    )
    assertEquals(initialIncome - 400.0, afterPayDebtState.totalMoneyIn, 0.01)
    assertEquals(offsetState.netCapital - 400.0, afterPayDebtState.netCapital, 0.01)

    // 3. Collecting 300 of CREDIT increases Income (totalMoneyIn) and Cash Wealth (netCapital) by 300
    val afterCollectCreditState = afterPayDebtState.copy(
      debtsCredits = listOf(
        DebtCreditEntity(
          id = 1L,
          type = "DEBT",
          personName = "Lender A",
          contactInfo = "",
          totalAmount = 1000.0,
          amountPaid = 400.0,
          currency = "USD"
        ),
        DebtCreditEntity(
          id = 2L,
          type = "CREDIT",
          personName = "Borrower B",
          contactInfo = "",
          totalAmount = 1000.0,
          amountPaid = 300.0,
          currency = "USD"
        )
      )
    )
    assertEquals(afterPayDebtState.totalMoneyIn + 300.0, afterCollectCreditState.totalMoneyIn, 0.01)
    assertEquals(afterPayDebtState.netCapital + 300.0, afterCollectCreditState.netCapital, 0.01)

    // 4. Gold & Silver treated directly as Cash Wealth (netCapital) and Clean Net Worth (netWorth)
    val withGoldAndSilverState = baseState.copy(
      goldHoldings = listOf(
        GoldHoldingEntity(
          id = 10L,
          metalType = "GOLD",
          title = "Gold Bar",
          weightGrams = 10.0,
          buyPricePerGram = 100.0,
          karat = 24
        )
      ),
      simulatedGoldPricePerGram = 120.0
    )
    // 10g * $120 = $1200 gold value added directly to Cash Wealth (netCapital) and Clean Net Worth
    assertEquals(baseState.netCapital + 1200.0, withGoldAndSilverState.netCapital, 0.01)
    assertEquals(baseState.netWorth + 1200.0, withGoldAndSilverState.netWorth, 0.01)

    // 5. Cars: receiving an installment or selling a car directly increases Clean Net Worth (صافی سەروەت)
    val activeCar = InvestmentEntity(
      id = 50L,
      name = "Toyota Land Cruiser",
      category = "Cars & Vehicles",
      initialCapital = 30000.0,
      currentValue = 30000.0,
      soldPrice = 35000.0,
      isSold = false,
      currency = "USD"
    )
    val withActiveCar = baseState.copy(investments = listOf(activeCar))
    // Before installment or sale, active unsold car does not inflate Clean Net Worth
    assertEquals(baseState.netWorth, withActiveCar.netWorth, 0.01)

    // Receiving a $5,000 car installment immediately increases Clean Net Worth & Income by $5,000
    val withCarInstallment = withActiveCar.copy(
      carPayments = listOf(
        com.example.data.local.CarPaymentEntity(
          id = 100L,
          carId = 50L,
          amount = 5000.0,
          myShare = 5000.0,
          currency = "USD",
          paidBy = "ME"
        )
      )
    )
    assertEquals(baseState.netWorth + 5000.0, withCarInstallment.netWorth, 0.01)

    // Selling the car for $35,000 immediately increases Clean Net Worth by $35,000
    val withSoldCar = baseState.copy(
      investments = listOf(activeCar.copy(isSold = true, soldPrice = 35000.0, currentValue = 35000.0))
    )
    assertEquals(baseState.netWorth + 35000.0, withSoldCar.netWorth, 0.01)
  }

  @Test
  fun `unified debt credit schema automatically updates net capital balance`() {
    val baseState = com.example.ui.FinanceUiState()
    val baseCapital = baseState.netCapital

    val receivable = DebtCreditEntity(
      id = 1L,
      type = com.example.data.local.DebtCreditType.CREDIT,
      personName = "Debtor Customer",
      totalAmount = 3000.0,
      amountPaid = 500.0,
      currency = "USD"
    )
    val payable = DebtCreditEntity(
      id = 2L,
      type = com.example.data.local.DebtCreditType.DEBT,
      personName = "Supplier Creditor",
      totalAmount = 1200.0,
      amountPaid = 200.0,
      currency = "USD"
    )

    assertEquals(2500.0, receivable.signedNetCapitalImpact, 0.01)
    assertEquals(-1000.0, payable.signedNetCapitalImpact, 0.01)

    val stateWithDebtsCredits = baseState.copy(
      debtsCredits = listOf(receivable, payable)
    )
    val summary = stateWithDebtsCredits.unifiedCapitalDebtSummary

    assertEquals(2500.0, summary.totalOwedByOthers, 0.01)
    assertEquals(1000.0, summary.totalDebtsToCreditors, 0.01)
    assertEquals(1500.0, summary.netDebtCreditOffset, 0.01)
    // Net cash impact from repayments: +500 collected - 200 paid = +300
    assertEquals(baseCapital + 300.0, stateWithDebtsCredits.netCapital, 0.01)
    // Net Capital Balance = netCapital + netDebtCreditOffset = (baseCapital + 300) + 1500
    assertEquals(stateWithDebtsCredits.netCapital + 1500.0, stateWithDebtsCredits.netCapitalBalance, 0.01)
    assertEquals(stateWithDebtsCredits.netCapitalBalance, summary.netCapitalBalance, 0.01)
  }

  @Test
  fun `buying or selling cars real estate and gold silver automatically updates cash wealth`() {
    val baseState = com.example.ui.FinanceUiState(
      settings = com.example.data.local.UserSettingsEntity(startingCapital = 100000.0, displayCurrency = "USD")
    )
    assertEquals(100000.0, baseState.pureCashBalance, 0.01)

    // 1. Buy a Car for $20,000 from Cash Wealth (deductedFromCash = true) -> Cash Wealth decreases by $20,000
    val carBought = InvestmentEntity(
      id = 101L,
      name = "BMW X5",
      category = "Cars & Vehicles",
      initialCapital = 20000.0,
      currentValue = 20000.0,
      currency = "USD",
      deductedFromCash = true
    )
    val afterBuyCar = baseState.copy(investments = listOf(carBought))
    assertEquals(80000.0, afterBuyCar.pureCashBalance, 0.01)

    // Sell the Car for $24,000 -> Cash Wealth increases by $24,000 (to $104,000)
    val carSold = carBought.copy(isSold = true, soldPrice = 24000.0, currentValue = 24000.0)
    val afterSellCar = baseState.copy(investments = listOf(carSold))
    assertEquals(104000.0, afterSellCar.pureCashBalance, 0.01)

    // 2. Buy Real Estate for $50,000 from Cash Wealth (deductedFromCash = true) -> Cash Wealth decreases by $50,000
    val realEstateBought = InvestmentEntity(
      id = 102L,
      name = "Erbil Apartment",
      category = "Real Estate",
      initialCapital = 50000.0,
      currentValue = 50000.0,
      currency = "USD",
      deductedFromCash = true
    )
    val afterBuyRealEstate = afterSellCar.copy(investments = listOf(carSold, realEstateBought))
    assertEquals(54000.0, afterBuyRealEstate.pureCashBalance, 0.01)

    // Sell Real Estate for $60,000 -> Cash Wealth increases by $60,000 (to $114,000)
    val realEstateSold = realEstateBought.copy(isSold = true, soldPrice = 60000.0, currentValue = 60000.0)
    val afterSellRealEstate = afterSellCar.copy(investments = listOf(carSold, realEstateSold))
    assertEquals(114000.0, afterSellRealEstate.pureCashBalance, 0.01)

    // 3. Buy Gold & Silver for $5,000 from Cash Wealth (deductedFromCash = true) -> Pure Cash decreases by $5,000
    val goldBought = GoldHoldingEntity(
      id = 201L,
      metalType = "GOLD",
      title = "Gold Bullion",
      weightGrams = 50.0,
      buyPricePerGram = 100.0,
      karat = 24,
      deductedFromCash = true
    )
    val afterBuyGold = afterSellRealEstate.copy(
      goldHoldings = listOf(goldBought),
      simulatedGoldPricePerGram = 100.0
    )
    assertEquals(109000.0, afterBuyGold.pureCashBalance, 0.01)
    // And total liquid capital (Cash + Active Gold) has $109,000 cash + $5,000 gold = $114,000
    assertEquals(114000.0, afterBuyGold.netCapital, 0.01)

    // Sell Gold for $5,800 -> Pure Cash increases by $5,800 (to $114,800) and active gold becomes 0
    val goldSold = goldBought.copy(isSold = true, soldPriceUsd = 5800.0)
    val afterSellGold = afterSellRealEstate.copy(
      goldHoldings = listOf(goldSold),
      simulatedGoldPricePerGram = 100.0
    )
    assertEquals(114800.0, afterSellGold.pureCashBalance, 0.01)
    assertEquals(114800.0, afterSellGold.netCapital, 0.01)
  }

  @Test
  fun `giving or receiving loan with affectCashBalance toggle decreases or increases cash wealth or keeps it unchanged`() {
    val baseState = com.example.ui.FinanceUiState(
      settings = com.example.data.local.UserSettingsEntity(startingCapital = 50000.0, displayCurrency = "USD")
    )
    assertEquals(50000.0, baseState.pureCashBalance, 0.01)

    // 1. Give a loan (CREDIT) of $5,000 with affectCashBalance = true -> Cash decreases by $5,000
    val creditWithCash = DebtCreditEntity(
      id = 1L,
      type = "CREDIT",
      personName = "Karwan",
      totalAmount = 5000.0,
      amountPaid = 0.0,
      currency = "USD",
      affectCashBalance = true
    )
    val afterLendWithCash = baseState.copy(debtsCredits = listOf(creditWithCash))
    assertEquals(45000.0, afterLendWithCash.pureCashBalance, 0.01)

    // 2. Give a loan (CREDIT) of $5,000 with affectCashBalance = false -> Cash stays $50,000
    val creditWithoutCash = creditWithCash.copy(affectCashBalance = false)
    val afterLendWithoutCash = baseState.copy(debtsCredits = listOf(creditWithoutCash))
    assertEquals(50000.0, afterLendWithoutCash.pureCashBalance, 0.01)

    // 3. Borrow a loan (DEBT) of $8,000 with affectCashBalance = true -> Cash increases by $8,000
    val debtWithCash = DebtCreditEntity(
      id = 2L,
      type = "DEBT",
      personName = "Merchant",
      totalAmount = 8000.0,
      amountPaid = 0.0,
      currency = "USD",
      affectCashBalance = true
    )
    val afterBorrowWithCash = baseState.copy(debtsCredits = listOf(debtWithCash))
    assertEquals(58000.0, afterBorrowWithCash.pureCashBalance, 0.01)

    // 4. Borrow a loan (DEBT) of $8,000 with affectCashBalance = false -> Cash stays $50,000
    val debtWithoutCash = debtWithCash.copy(affectCashBalance = false)
    val afterBorrowWithoutCash = baseState.copy(debtsCredits = listOf(debtWithoutCash))
    assertEquals(50000.0, afterBorrowWithoutCash.pureCashBalance, 0.01)
  }

  @Test
  fun `private account totalIndependentWealth adds money owed by others to properties and gold`() {
    // Property asset: House worth $30,000 (sole ownership)
    val house = InvestmentEntity(
      id = 101L,
      name = "Duhok Villa",
      category = "Real Estate - House",
      initialCapital = 25000.0,
      currentValue = 30000.0,
      currency = "USD",
      isCoOwned = false
    )

    // Gold asset in Private Account: gold worth $5,000
    val goldItem = InvestmentEntity(
      id = 102L,
      name = "Gold Bullion",
      category = "Real Estate - Gold",
      initialCapital = 5000.0,
      currentValue = 5000.0,
      currency = "USD"
    )

    // Credit owed to me: $6,000 given to a person (marked with [REAL_ESTATE_DEBT])
    val creditOwedToMe = DebtCreditEntity(
      id = 103L,
      type = "CREDIT",
      personName = "Ahmed",
      totalAmount = 6000.0,
      amountPaid = 0.0,
      currency = "USD",
      notes = "[REAL_ESTATE_DEBT] Lent on estate deal"
    )

    val state = com.example.ui.FinanceUiState(
      investments = listOf(house, goldItem),
      debtsCredits = listOf(creditOwedToMe),
      settings = com.example.data.local.UserSettingsEntity(displayCurrency = "USD")
    )

    // Properties: 30,000
    assertEquals(30000.0, state.myRealEstateValuation, 0.01)
    // Gold: 5,000
    assertEquals(5000.0, state.goldValuablesInvestmentsValuation, 0.01)
    // Credit owed by others: 6,000
    assertEquals(6000.0, state.totalPrivateAccountCreditsReceivable, 0.01)
    // Total combined Private Account wealth = 30,000 + 5,000 + 6,000 = 41,000!
    assertEquals(41000.0, state.totalIndependentWealth, 0.01)

    // Also verify when another general credit (not tagged real estate) is owed by someone ($4,000)
    // General debt/credit MUST NOT be added to private account wealth or private credits!
    val generalCredit = DebtCreditEntity(
      id = 104L,
      type = "CREDIT",
      personName = "Botan",
      totalAmount = 4000.0,
      amountPaid = 0.0,
      currency = "USD",
      notes = "General lent money"
    )
    val combinedState = state.copy(debtsCredits = listOf(creditOwedToMe, generalCredit))
    // Private credits receivable remains strictly only private account credits (6,000)
    assertEquals(6000.0, combinedState.totalPrivateAccountCreditsReceivable, 0.01)
    // General credit is tracked separately in totalCreditReceivable (4,000)
    assertEquals(4000.0, combinedState.totalCreditReceivable, 0.01)
    // Total combined Private Account wealth = 30,000 + 5,000 + 6,000 = 41,000 (NOT 45,000!)
    assertEquals(41000.0, combinedState.totalIndependentWealth, 0.01)
  }
}

