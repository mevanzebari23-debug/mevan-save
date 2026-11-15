package com.example.ui.i18n

object AppStrings {

  fun isKurdish(language: String): Boolean = language == "KU"

  // Navigation (Compact for 4 Main Tabs)
  fun navCapital(lang: String) = if (isKurdish(lang)) "سەرەکی" else "Home"
  fun navDebts(lang: String) = if (isKurdish(lang)) "قەرز" else "Debts"
  fun navAssetsInvest(lang: String) = if (isKurdish(lang)) "ملوک و وەبەرهێنان" else "Assets"
  fun navReportsTools(lang: String) = if (isKurdish(lang)) "ئامراز" else "Tools"
  fun navCashflow(lang: String) = if (isKurdish(lang)) "کاش" else "Cash"
  fun navCars(lang: String) = if (isKurdish(lang)) "سەیارە" else "Cars"
  fun navInvest(lang: String) = if (isKurdish(lang)) "حسابا تایبەت" else "Private Acc"
  fun navGold(lang: String) = if (isKurdish(lang)) "زێڕ/زیڤ" else "Metals"
  fun navTools(lang: String) = if (isKurdish(lang)) "ئامراز" else "Tools"

  fun navSchool(lang: String) = if (isKurdish(lang)) "قوتابخانە" else "School"

  fun sectionFullTitle(lang: String, tab: Int): String = when (tab) {
    0 -> if (isKurdish(lang)) "سەرەکی - سەروەت و سەرمایێ گشتی" else "Executive Capital & Net Worth"
    1 -> if (isKurdish(lang)) "سجلا قەرزان (قەرزێن لسەر من و دەف خەلکی)" else "Receivables & Debts Registry"
    2 -> if (isKurdish(lang)) "پێشانگەها سەیاران (سەیارە و قست)" else "Vehicles Fleet & Deals"
    3 -> if (isKurdish(lang)) "ملوک و خەزینە (عەقارات، پڕۆژە و زێڕ)" else "Assets & Gold Vault"
    4 -> if (isKurdish(lang)) "جهێ تایبەتێ قوتابخانێ (پەروەردە و مەسروف)" else "Dedicated School & Education Vault"
    5 -> if (isKurdish(lang)) "⚙️ ئامراز و راپۆرتێن دارایی" else "⚙️ Tools & Reports Hub"
    else -> if (isKurdish(lang)) "ئامراز" else "Tools"
  }

  // Top Bar
  fun appTitle(lang: String) = if (isKurdish(lang)) "سەرمایێ من" else "Capital Vault"
  fun appSubtitle(lang: String) = if (isKurdish(lang)) "ئۆفلاین و سەر هەورێ پاراستیە" else "Offline & Cloud Synced"
  fun searchTooltip(lang: String) = if (isKurdish(lang)) "لێکگەڕیان" else "Search"
  fun searchEverything(lang: String) = if (isKurdish(lang)) "لێکگەڕیانا سەرتاسەری" else "Global Search"
  fun languageButton(lang: String) = if (isKurdish(lang)) "کوردی" else "English"
  fun languageSwitchPrompt(lang: String) = if (isKurdish(lang)) "گوهۆڕینا زمانێ بەرنامەی" else "Switch App Language"

  // Overview Screen
  fun totalNetWorth(lang: String) = if (isKurdish(lang)) "سەروەت و سەرمایێ گشتی" else "Total Net Worth"
  fun grossEstimatedWealth(lang: String) = if (isKurdish(lang)) "سەروەت و سامانێ گشتی" else "Total Capital & Assets"
  fun partnerShareInDeals(lang: String) = if (isKurdish(lang)) "پارێ شریکاتی" else "Partner Share & Capital"
  fun expensesOut(lang: String) = if (isKurdish(lang)) "مەسروفات" else "Expenses"
  fun remainingNetWorth(lang: String) = if (isKurdish(lang)) "باقی ژ سەروەت (صافی پارێت من)" else "Remaining Net Wealth (Clean Money)"
  fun equationTitle(lang: String) = if (isKurdish(lang)) "معادلا سەروەتێ گشتی و صافی پارێت من" else "Net Worth Formula & Equation"
  fun equationSubtitle(lang: String) = if (isKurdish(lang)) "سامان - قەرزێن لسەر من - پارێ شریکاتی - مەسروفات = باقی ژ سەروەت (عەقار ب سەربەخۆیی تنێیە)" else "Assets - Debts - Partner Share - Expenses = Net Wealth (Real Estate Standalone)"
  fun startingCapital(lang: String) = if (isKurdish(lang)) "سەرمایێ دەستپێکێ" else "Starting Capital"
  fun availableCash(lang: String) = if (isKurdish(lang)) "کاشێ بەردەست" else "Available Liquid Cash"
  fun fleetValuation(lang: String) = if (isKurdish(lang)) "بهایێ سەیارێن بەردەست" else "Active Fleet Valuation"
  fun goldHoldings(lang: String) = if (isKurdish(lang)) "خەزینەیا زێڕ و زیڤی" else "Gold & Silver Vault"
  fun debtsOwedToMe(lang: String) = if (isKurdish(lang)) "قەرزێن دەف خەلکی" else "Owed to Me (Credits)"
  fun debtsIOwe(lang: String) = if (isKurdish(lang)) "قەرزێن لسەر من" else "I Owe (Debts)"
  fun quickActions(lang: String) = if (isKurdish(lang)) "کریارێن لەزگین" else "Quick Actions"
  fun addIncome(lang: String) = if (isKurdish(lang)) "+ داهات" else "+ Income"
  fun addExpense(lang: String) = if (isKurdish(lang)) "- مەسروف" else "- Expense"
  fun addCar(lang: String) = if (isKurdish(lang)) "+ سەیارە" else "+ Vehicle"
  fun addDebt(lang: String) = if (isKurdish(lang)) "+ قەرز" else "+ Loan"
  fun recentActivity(lang: String) = if (isKurdish(lang)) "دوماهیک کریار و تەحویلات" else "Recent Transactions"

  // Cashflow Screen
  fun cashflowTitle(lang: String) = if (isKurdish(lang)) "تۆمارا داهات و مەسروفی" else "Cashflow Register"
  fun totalIncome(lang: String) = if (isKurdish(lang)) "داهاتێ هاتى" else "Total Income"
  fun totalExpense(lang: String) = if (isKurdish(lang)) "مەسروفێ دەرچووى" else "Total Expenses"
  fun netCashflow(lang: String) = if (isKurdish(lang)) "مایێ سافی" else "Net Cashflow"
  fun filterAll(lang: String) = if (isKurdish(lang)) "هەمی" else "All"
  fun filterIncome(lang: String) = if (isKurdish(lang)) "داهات" else "Income"
  fun filterExpense(lang: String) = if (isKurdish(lang)) "مەسروف" else "Expense"
  fun recordCashflow(lang: String) = if (isKurdish(lang)) "تۆمارکرنا داهات یان مەسروفی" else "Record Cashflow"
  fun amount(lang: String) = if (isKurdish(lang)) "بڕێ پارەی" else "Amount"
  fun category(lang: String) = if (isKurdish(lang)) "بەش / جۆر" else "Category"
  fun notes(lang: String) = if (isKurdish(lang)) "تێبینی" else "Notes"
  fun save(lang: String) = if (isKurdish(lang)) "تۆمار بکە" else "Save"
  fun cancel(lang: String) = if (isKurdish(lang)) "پاشگەزبوون" else "Cancel"

  // Cars Screen
  fun carsTitle(lang: String) = if (isKurdish(lang)) "پێشانگەها سەیاران و قست" else "Vehicle Fleet & Deals"
  fun tabAllCars(lang: String, count: Int) = if (isKurdish(lang)) "هەمی ($count)" else "All ($count)"
  fun tabActiveCars(lang: String, count: Int) = if (isKurdish(lang)) "بەردەست ($count)" else "Active ($count)"
  fun tabSoldCars(lang: String, count: Int) = if (isKurdish(lang)) "فرۆتی ($count)" else "Sold ($count)"
  fun tabPartnerCars(lang: String, count: Int) = if (isKurdish(lang)) "شەریک ($count)" else "Partner ($count)"
  fun tabMaintenanceCars(lang: String, count: Int) = if (isKurdish(lang)) "مەسروفات ($count)" else "Maintenance ($count)"
  fun tabInstallmentsCars(lang: String, count: Int) = if (isKurdish(lang)) "قستێ هەیڤانە ($count)" else "قستێ هەیڤانە ($count)"
  fun sellCar(lang: String) = if (isKurdish(lang)) "فرۆتنا سەیارێ" else "Sell Vehicle"
  fun recordInstallment(lang: String) = if (isKurdish(lang)) "+ قستێ هەیڤانە" else "+ قستێ هەیڤانە"
  fun addSpending(lang: String) = if (isKurdish(lang)) "+ مەسروفات" else "+ Spending"
  fun viewDetails(lang: String) = if (isKurdish(lang)) "وردەکاری" else "Details"
  fun remainingMoney(lang: String) = if (isKurdish(lang)) "باقیێ ماى" else "Remaining"
  fun paidAmount(lang: String) = if (isKurdish(lang)) "واصل بووی" else "Paid"
  fun totalFleetCost(lang: String) = if (isKurdish(lang)) "سەرمایێ سەیاران" else "Total Fleet Invested"
  fun fullCashBadge(lang: String) = if (isKurdish(lang)) "کاشێ تەواو" else "100% Cash"
  fun partnerBadge(lang: String) = if (isKurdish(lang)) "شەریکی" else "Partner Deal"
  fun installmentBadge(lang: String) = if (isKurdish(lang)) "ب قستا" else "Installment Deal"
  fun fullySettledBadge(lang: String) = if (isKurdish(lang)) "هەمی واصل بوو" else "Fully Settled"

  // Debts & Credits Screen
  fun debtsTitle(lang: String) = if (isKurdish(lang)) "سجلا قەرزان و دەینان" else "Debt & Loan Registry"
  fun tabPeopleOweMe(lang: String, count: Int) = if (isKurdish(lang)) "دەف خەلکی ($count)" else "Owed to Me ($count)"
  fun tabIOwePeople(lang: String, count: Int) = if (isKurdish(lang)) "لسەر من ($count)" else "I Owe ($count)"
  fun addLoan(lang: String) = if (isKurdish(lang)) "+ قەرزەکێ نوی" else "+ New Loan"
  fun personName(lang: String) = if (isKurdish(lang)) "ناڤێ کەسی" else "Person Name"
  fun settleDebt(lang: String) = if (isKurdish(lang)) "واصلکرنا قەرزی" else "Record Payment"

  // Investments Screen
  fun investTitle(lang: String) = if (isKurdish(lang)) "وەبەرهێنان و پڕۆژە" else "Investments & Ventures"
  fun monthlyProfit(lang: String) = if (isKurdish(lang)) "قازانجێ هەیڤانە" else "Monthly Profit"
  fun addInvestment(lang: String) = if (isKurdish(lang)) "+ وەبەرهێنان" else "+ Investment"

  // Gold Screen
  fun goldTitle(lang: String) = if (isKurdish(lang)) "پاشەکەوتا زێڕێ عەیاردار" else "Physical Gold Vault"
  fun currentPricePerMithqal(lang: String) = if (isKurdish(lang)) "نرخێ مسقالی" else "Price / Mithqal"
  fun addGoldHolding(lang: String) = if (isKurdish(lang)) "+ کڕینا زێڕی" else "+ Buy Gold"

  // Budgets & Settings Screen
  fun settingsTitle(lang: String) = if (isKurdish(lang)) "ڕێکخستن و بودجە و پاراستن" else "Settings, Budgets & Vault"
  fun languageSection(lang: String) = if (isKurdish(lang)) "زمانێ بەرنامەی (Language)" else "App Language"
  fun languageDesc(lang: String) = if (isKurdish(lang)) "هەلبژارتنا زمانێ کوردی بادینی یان ئینگلیزی" else "Choose between Kurdish (Badini) and English"
  fun kurdishBadini(lang: String) = "کوردی (بادینی)"
  fun english(lang: String) = "English"
  fun biometricSection(lang: String) = if (isKurdish(lang)) "ئاسایشا پەنجەمۆرێ" else "Biometric Security"
  fun biometricEnabled(lang: String) = if (isKurdish(lang)) "چالاکە (پەنجەمۆر)" else "Enabled (Fingerprint)"
  fun biometricDisabled(lang: String) = if (isKurdish(lang)) "نەچالاکە" else "Disabled"
  fun changePin(lang: String) = if (isKurdish(lang)) "گوهۆڕینا ژمارا نهێنی" else "Change PIN"
  fun lockNow(lang: String) = if (isKurdish(lang)) "نوکە قفل بکە" else "Lock Now"
  fun cloudSection(lang: String) = if (isKurdish(lang)) "پاشەکەوتکرنا سەر هەورێ (Cloud Sync)" else "Cloud Backup & Sync"
  fun syncNow(lang: String) = if (isKurdish(lang)) "نوکە سینک بکە" else "Sync Now"
  fun exportSection(lang: String) = if (isKurdish(lang)) "دەرخستنا ڕاپۆرتێن دارایی" else "Export Reports & Statements"
  fun exportStatement(lang: String) = if (isKurdish(lang)) "ڕاپۆرتا دارایی یا گشتی (PDF/Text)" else "Export Financial Statement"
  fun themeSection(lang: String) = if (isKurdish(lang)) "شێوازێ ڕەنگان" else "Theme Preferences"
  fun darkMode(lang: String) = if (isKurdish(lang)) "تاریک (Dark)" else "DARK"
  fun lightMode(lang: String) = if (isKurdish(lang)) "ڕووناک (Light)" else "LIGHT"
  fun startingCapitalTitle(lang: String) = if (isKurdish(lang)) "دەستکاریکرنا سەرمایێ سەرەکی" else "Edit Starting Capital"
  fun saveSettings(lang: String) = if (isKurdish(lang)) "تۆمارکرن" else "Save Settings"
  fun currencyRateSection(lang: String) = if (isKurdish(lang)) "سعرێ دۆلاری بەرامبەر دیناری" else "Exchange Rate (USD / IQD)"
}
