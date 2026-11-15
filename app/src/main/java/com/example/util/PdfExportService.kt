package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.components.BalanceSheetExcelRow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise Native Android PDF Document Generator, Preview Engine & Print Controller.
 * Generates clean, official white-paper statements optimized for printing, PDF viewing, and sharing.
 */
object PdfExportService {

  private const val PAGE_WIDTH = 595  // Standard A4 width in PostScript points (72 dpi)
  private const val PAGE_HEIGHT = 842 // Standard A4 height in PostScript points (72 dpi)
  private const val MARGIN = 36f      // 0.5 inch margin

  /**
   * Generates a Master Executive Financial Statement & Ledger PDF document.
   * Consolidates all accounts, liquid cash, liabilities, property assets, and net worth into
   * a professional, printable white-paper audit statement.
   */
  fun generateMasterLedgerPdf(
    context: Context,
    uiState: FinanceUiState,
    isKurdish: Boolean
  ): GeneratedPdfResult? {
    val title = if (isKurdish) "ڕاپۆرتا دارایی و ميزانیەیا گشتی (Master Financial Ledger)" else "Executive Financial Ledger & Audit Statement"
    val subtitle = if (isKurdish) "تەختا فەرمی یا سەروەت، قەرز، کاش و سامانێن مێڤان زێباری" else "Master Capital, Liabilities & Wealth Audit • Mevan Zebari"
    val fileName = "Master_Financial_Ledger_${System.currentTimeMillis()}.pdf"
    val cur = uiState.displayCurrency

    val totalAssets = uiState.pureCashBalance + uiState.myActiveCarValuationShare + uiState.myRealEstateValuation +
      uiState.goldValuablesInvestmentsValuation + uiState.totalGoldCurrentValueDisplay +
      uiState.totalCreditReceivable + uiState.totalPrivateAccountCreditsReceivable
    val totalLiabilities = uiState.totalDebtOwed + uiState.totalCarsMyRemainingMoney + uiState.totalIndependentRealEstateDebt
    val netWorth = totalAssets - totalLiabilities - uiState.totalPartnerShareInInvestments

    val ledgerRows = listOfNotNull(
      PdfRowData(listOf("1", if (isKurdish) "سەرمایێ دەستپێکێ (بنەڕەتی)" else "Initial Starting Capital", "CAPITAL", CurrencyService.formatShort(uiState.settings.startingCapital, cur), "Base Capital"), isPositive = true),
      PdfRowData(listOf("2", if (isKurdish) "کاشێ بەردەست (سیولە د خەزینێ دا)" else "Pure Liquid Cash Vault", "ASSET", CurrencyService.formatShort(uiState.pureCashBalance, cur), "Liquid Cash"), isPositive = uiState.pureCashBalance >= 0),
      PdfRowData(listOf("3", if (isKurdish) "کۆیێ داهاتی (تەحویلات IN)" else "Total Inflow (Cash Received)", "INFLOW", "+${CurrencyService.formatShort(uiState.totalMoneyIn, cur)}", "Cash In"), isPositive = true),
      PdfRowData(listOf("4", if (isKurdish) "کۆیێ مەسروفی (خەرجی OUT)" else "Total Outflow (Cash Spent)", "OUTFLOW", "-${CurrencyService.formatShort(uiState.totalMoneyOut, cur)}", "Cash Out"), isPositive = false),
      PdfRowData(listOf("5", if (isKurdish) "سەرمایێ سەیاران (تنێ پشکا من)" else "Vehicles Fleet Valuation (My Share)", "ASSET", CurrencyService.formatShort(uiState.myActiveCarValuationShare, cur), "Fleet Asset"), isPositive = true),
      PdfRowData(listOf("6", if (isKurdish) "عەقارات، خانوو، ئەرد و ملوک" else "Real Estate & Muluk Properties", "ASSET", CurrencyService.formatShort(uiState.myRealEstateValuation, cur), "Property"), isPositive = true),
      PdfRowData(listOf("7", if (isKurdish) "خەزینەیا زێڕ و زیڤی (بهایێ عالمي)" else "Gold & Precious Metals Vault", "ASSET", CurrencyService.formatShort(uiState.totalGoldCurrentValueDisplay + uiState.goldValuablesInvestmentsValuation, cur), "Spot Metals"), isPositive = true),
      PdfRowData(listOf("8", if (isKurdish) "قەرزێن گشتی دەف خەلکی (ماف)" else "Receivables (General Loans Given)", "RECEIVABLE", "+${CurrencyService.formatShort(uiState.totalCreditReceivable, cur)}", "Active Loan"), isPositive = true),
      if (uiState.totalPrivateAccountCreditsReceivable > 0.0) {
        PdfRowData(listOf("9", if (isKurdish) "قەرزێ تایبەتێ ملوکان دەف خەلکی" else "Muluk Isolated Receivables", "RECEIVABLE", "+${CurrencyService.formatShort(uiState.totalPrivateAccountCreditsReceivable, cur)}", "Isolated"), isPositive = true)
      } else null,
      PdfRowData(listOf("10", if (isKurdish) "قەرزێن گشتی یێن لسەر من" else "Payables (General Debts Owed)", "LIABILITY", "-${CurrencyService.formatShort(uiState.totalDebtOwed, cur)}", "Payable"), isPositive = false),
      PdfRowData(listOf("11", if (isKurdish) "باقیێ قستێن سەیاران (تنێ پشکا من)" else "Car Installment Debt (My Share)", "LIABILITY", "-${CurrencyService.formatShort(uiState.totalCarsMyRemainingMoney, cur)}", "Installment"), isPositive = false),
      PdfRowData(listOf("12", if (isKurdish) "قەرزێ ماى لسەر ملوکان (عەقار)" else "Muluk Property Installment Debt", "LIABILITY", "-${CurrencyService.formatShort(uiState.totalIndependentRealEstateDebt, cur)}", "Property Debt"), isPositive = false),
      if (uiState.totalPartnerShareInInvestments > 0.0) {
        PdfRowData(listOf("13", if (isKurdish) "پارێ شەریکی د معامەلان دا" else "Partner Share & Outside Equity", "EQUITY_DEDUCT", "-${CurrencyService.formatShort(uiState.totalPartnerShareInInvestments, cur)}", "Partner Share"), isPositive = false)
      } else null
    )

    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = title,
      subtitle = subtitle,
      displayCurrency = cur,
      isKurdish = isKurdish,
      headers = listOf("#", "Financial Ledger Line Item", "Type", "Valuation / Balance", "Status"),
      columnWidths = floatArrayOf(25f, 210f, 95f, 105f, 88f),
      dataRows = ledgerRows,
      summaryCards = listOf(
        SummaryCardData(
          label = if (isKurdish) "کۆیێ سامانێن گشتی" else "Total Gross Assets",
          value = "+${CurrencyService.formatShort(totalAssets, cur)}",
          color = Color.rgb(4, 120, 87)
        ),
        SummaryCardData(
          label = if (isKurdish) "کۆیێ ئیلتزام و قەرزان" else "Total Liabilities",
          value = "-${CurrencyService.formatShort(totalLiabilities, cur)}",
          color = Color.rgb(185, 28, 28)
        ),
        SummaryCardData(
          label = if (isKurdish) "سەروەتێ صافی (Net Worth)" else "Net Worth Equity",
          value = "${if (netWorth >= 0) "+" else ""}${CurrencyService.formatShort(netWorth, cur)}",
          color = if (netWorth >= 0) Color.rgb(4, 120, 87) else Color.rgb(185, 28, 28)
        )
      )
    )
  }

  /**
   * Generates a professional Executive Balance Sheet PDF document.
   */
  fun generateBalanceSheetPdf(
    context: Context,
    rows: List<BalanceSheetExcelRow>,
    totalAssets: Double,
    totalLiabilities: Double,
    netWorth: Double,
    displayCurrency: String,
    isKurdish: Boolean
  ): GeneratedPdfResult? {
    val title = if (isKurdish) "راپۆرتا میزانیەیێ و سەروەتێ گشتی" else "Statement of Financial Position"
    val subtitle = if (isKurdish) "تەختا فەرمی یا سەروەتێ صافی • مێڤان زێباری" else "Official Executive Balance Sheet • Mevan Zebari"
    val fileName = "Balance_Sheet_${System.currentTimeMillis()}.pdf"

    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = title,
      subtitle = subtitle,
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      headers = listOf("Cell", "Account Line Item", "Type", "Formula", "Amount", "Cur"),
      columnWidths = floatArrayOf(45f, 175f, 75f, 95f, 90f, 43f),
      dataRows = rows.map { r ->
        val name = if (isKurdish) r.accountNameKu else r.accountNameEn
        val typeLabel = if (r.accountType == "ASSET") (if (isKurdish) "سامان" else "ASSET") else (if (isKurdish) "ئیلتزام" else "LIABILITY")
        val amtStr = "${if (r.accountType == "ASSET") "+" else "-"}${CurrencyService.formatShort(r.amount, displayCurrency)}"
        PdfRowData(
          cells = listOf(r.cellRef, name, typeLabel, r.formula, amtStr, displayCurrency),
          isPositive = r.accountType == "ASSET"
        )
      },
      summaryCards = listOf(
        SummaryCardData(
          label = if (isKurdish) "کۆیێ گشتیێ سامانان" else "Total Assets",
          value = "+${CurrencyService.formatShort(totalAssets, displayCurrency)}",
          color = Color.rgb(4, 120, 87) // Deep Emerald
        ),
        SummaryCardData(
          label = if (isKurdish) "کۆیێ گشتیێ قەرزان" else "Total Liabilities",
          value = "-${CurrencyService.formatShort(totalLiabilities, displayCurrency)}",
          color = Color.rgb(185, 28, 28) // Deep Crimson
        ),
        SummaryCardData(
          label = if (isKurdish) "سەروەتێ صافی (Net Worth)" else "Net Worth (Equity)",
          value = "${if (netWorth >= 0) "+" else ""}${CurrencyService.formatShort(netWorth, displayCurrency)}",
          color = if (netWorth >= 0) Color.rgb(4, 120, 87) else Color.rgb(185, 28, 28)
        )
      )
    )
  }

  fun generateAndShareBalanceSheetPdf(
    context: Context,
    rows: List<BalanceSheetExcelRow>,
    totalAssets: Double,
    totalLiabilities: Double,
    netWorth: Double,
    displayCurrency: String,
    isKurdish: Boolean
  ): Boolean {
    val result = generateBalanceSheetPdf(context, rows, totalAssets, totalLiabilities, netWorth, displayCurrency, isKurdish) ?: return false
    sharePdfFile(context, result.file, result.documentTitle)
    return true
  }

  /**
   * Generates an official Debts & Receivables PDF Statement.
   * Allows strict filtering between General Debts and Muluk (Real Estate) Debts.
   */
  fun generateDebtsPdf(
    context: Context,
    debts: List<DebtCreditEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean,
    isMulukOnly: Boolean = false
  ): GeneratedPdfResult? {
    val title = when {
      isMulukOnly && isKurdish -> "راپۆرتا تایبەت یا قەرزێن عەقارات و ملوکان"
      isMulukOnly -> "Muluk Real Estate Debts Statement"
      isKurdish -> "راپۆرتا فەرمی یا قەرز و دەینان"
      else -> "Official Debts & Receivables Statement"
    }
    val subtitle = if (isKurdish) {
      if (isMulukOnly) "جوداکرنا قەرزێن سەربەخۆ یێن ملوکان ژ قەرزێن گشتی" else "قەرزێن گشتی یێن کەسی و بازرگانی"
    } else {
      if (isMulukOnly) "Isolated Muluk Account Debts (Excluded from general pool)" else "Active General Personal & Commercial Debts"
    }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val totalCredit = debts.filter { it.type == "CREDIT" && !it.isCompleted }.sumOf {
      CurrencyService.convert(it.remainingBalance, it.currency, displayCurrency, currentIqdRate)
    }
    val totalDebt = debts.filter { it.type == "DEBT" && !it.isCompleted }.sumOf {
      CurrencyService.convert(it.remainingBalance, it.currency, displayCurrency, currentIqdRate)
    }
    val netBalance = totalCredit - totalDebt

    val fileName = if (isMulukOnly) "Muluk_Debts_${System.currentTimeMillis()}.pdf" else "General_Debts_${System.currentTimeMillis()}.pdf"

    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = title,
      subtitle = subtitle,
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      headers = listOf("#", "Counterparty", "Type", "Remaining", "Due Date", "Notes"),
      columnWidths = floatArrayOf(25f, 150f, 75f, 95f, 75f, 103f),
      dataRows = debts.mapIndexed { index, d ->
        val typeLabel = if (d.type == "CREDIT") (if (isKurdish) "دەف خەلکی" else "RECEIVABLE") else (if (isKurdish) "لسەر من" else "PAYABLE")
        val dueStr = if (d.dueDateMillis > 0L) dateFormat.format(Date(d.dueDateMillis)) else "-"
        val remainingFormatted = "${if (d.type == "CREDIT") "+" else "-"}${CurrencyService.formatShort(d.remainingBalance, d.currency)}"
        PdfRowData(
          cells = listOf(
            (index + 1).toString(),
            d.personName,
            typeLabel,
            remainingFormatted,
            dueStr,
            d.notes.replace("\n", " ").take(30)
          ),
          isPositive = d.type == "CREDIT"
        )
      },
      summaryCards = listOf(
        SummaryCardData(
          label = if (isKurdish) "کۆیێ دەف خەلکی (Receivable)" else "Total Receivables",
          value = "+${CurrencyService.formatShort(totalCredit, displayCurrency)}",
          color = Color.rgb(4, 120, 87)
        ),
        SummaryCardData(
          label = if (isKurdish) "کۆیێ لسەر من (Payable)" else "Total Payables",
          value = "-${CurrencyService.formatShort(totalDebt, displayCurrency)}",
          color = Color.rgb(185, 28, 28)
        ),
        SummaryCardData(
          label = if (isKurdish) "مافێ صافی (Net Balance)" else "Net Debt Position",
          value = "${if (netBalance >= 0) "+" else ""}${CurrencyService.formatShort(netBalance, displayCurrency)}",
          color = if (netBalance >= 0) Color.rgb(4, 120, 87) else Color.rgb(185, 28, 28)
        )
      )
    )
  }

  fun generateAndShareDebtsPdf(
    context: Context,
    debts: List<DebtCreditEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean,
    isMulukOnly: Boolean = false
  ): Boolean {
    val result = generateDebtsPdf(context, debts, displayCurrency, currentIqdRate, isKurdish, isMulukOnly) ?: return false
    sharePdfFile(context, result.file, result.documentTitle)
    return true
  }

  /**
   * Generates a generic multi-sheet workbook PDF statement (e.g. for Vehicles Fleet, Muluk Assets).
   */
  fun generateGenericWorkbookPdf(
    context: Context,
    sheetTitle: String,
    headers: List<String>,
    columnWidths: FloatArray,
    rows: List<PdfRowData>,
    summaryCards: List<SummaryCardData>,
    displayCurrency: String,
    isKurdish: Boolean
  ): GeneratedPdfResult? {
    val fileName = "${sheetTitle.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = sheetTitle,
      subtitle = if (isKurdish) "راپۆرتا فەرمی یا چاپکراو • خشتەیێ دارایی" else "Official Financial Spreadsheet Statement",
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      headers = headers,
      columnWidths = columnWidths,
      dataRows = rows,
      summaryCards = summaryCards
    )
  }

  fun generateAndShareGenericWorkbookPdf(
    context: Context,
    sheetTitle: String,
    headers: List<String>,
    columnWidths: FloatArray,
    rows: List<PdfRowData>,
    summaryCards: List<SummaryCardData>,
    displayCurrency: String,
    isKurdish: Boolean
  ): Boolean {
    val result = generateGenericWorkbookPdf(context, sheetTitle, headers, columnWidths, rows, summaryCards, displayCurrency, isKurdish) ?: return false
    sharePdfFile(context, result.file, result.documentTitle)
    return true
  }

  /**
   * Generates an official Vehicles Fleet PDF Statement.
   */
  fun generateCarsPdf(
    context: Context,
    cars: List<InvestmentEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean
  ): GeneratedPdfResult? {
    val title = if (isKurdish) "راپۆرتا فەرمی یا سەیاران (Fleet Statement)" else "Official Vehicles Fleet Statement"
    val subtitle = if (isKurdish) "تەختا چاپکراو یا سەرمایێ سەیاران و قازانجێ مێڤان زێباری" else "Capital, Valuations & Profit Statement • Mevan Zebari"
    val fileName = "Vehicles_Fleet_${System.currentTimeMillis()}.pdf"

    val totalCost = cars.sumOf {
      CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
    val totalValuation = cars.sumOf {
      val price = if (it.isSold) it.soldPrice else it.currentValue
      CurrencyService.convert(price, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
    val totalProfit = totalValuation - totalCost

    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = title,
      subtitle = subtitle,
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      headers = listOf("#", "Car Model / Name", "Cost", "Current / Sold", "Profit", "Status"),
      columnWidths = floatArrayOf(25f, 170f, 85f, 85f, 85f, 73f),
      dataRows = cars.mapIndexed { index, car ->
        val profit = if (car.isSold) (car.soldPrice - car.initialCapital) else (car.currentValue - car.initialCapital)
        val statusStr = if (car.isSold) (if (isKurdish) "فرۆتی" else "SOLD") else (if (isKurdish) "بەردەست" else "ACTIVE")
        PdfRowData(
          cells = listOf(
            (index + 1).toString(),
            car.name,
            CurrencyService.formatShort(car.initialCapital, car.currency),
            CurrencyService.formatShort(if (car.isSold) car.soldPrice else car.currentValue, car.currency),
            "${if (profit >= 0) "+" else ""}${CurrencyService.formatShort(profit, car.currency)}",
            statusStr
          ),
          isPositive = profit >= 0
        )
      },
      summaryCards = listOf(
        SummaryCardData(
          label = if (isKurdish) "کۆیێ سەرمایێ کڕینێ" else "Total Purchase Cost",
          value = CurrencyService.formatShort(totalCost, displayCurrency),
          color = Color.rgb(15, 23, 42)
        ),
        SummaryCardData(
          label = if (isKurdish) "بهایێ گشتیێ سەیاران" else "Total Fleet Valuation",
          value = CurrencyService.formatShort(totalValuation, displayCurrency),
          color = Color.rgb(4, 120, 87)
        ),
        SummaryCardData(
          label = if (isKurdish) "قازانج / زەرەر" else "Net Fleet Return",
          value = "${if (totalProfit >= 0) "+" else ""}${CurrencyService.formatShort(totalProfit, displayCurrency)}",
          color = if (totalProfit >= 0) Color.rgb(4, 120, 87) else Color.rgb(185, 28, 28)
        )
      )
    )
  }

  fun generateAndShareCarsPdf(
    context: Context,
    cars: List<InvestmentEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean
  ): Boolean {
    val result = generateCarsPdf(context, cars, displayCurrency, currentIqdRate, isKurdish) ?: return false
    sharePdfFile(context, result.file, result.documentTitle)
    return true
  }

  /**
   * Generates an official CashFlow Financial Transactions PDF Statement.
   */
  fun generateTransactionsPdf(
    context: Context,
    transactions: List<TransactionEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean
  ): GeneratedPdfResult? {
    val title = if (isKurdish) "راپۆرتا فەرمی یا تەڤگەرێن کاشی و مەسروفی" else "CashFlow Transactions Statement"
    val subtitle = if (isKurdish) "تۆمارا داهات و خەرجیان • مێڤان زێباری" else "Income & Expense General Ledger • Mevan Zebari"
    val fileName = "CashFlow_Statement_${System.currentTimeMillis()}.pdf"

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val totalIn = transactions.filter { it.type == "IN" }.sumOf {
      CurrencyService.convert(it.amount, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
    val totalOut = transactions.filter { it.type == "OUT" }.sumOf {
      CurrencyService.convert(it.amount, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
    val netCash = totalIn - totalOut

    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = title,
      subtitle = subtitle,
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      headers = listOf("#", "Date", "Category / Note", "Type", "Amount", "Currency"),
      columnWidths = floatArrayOf(25f, 75f, 185f, 75f, 95f, 68f),
      dataRows = transactions.mapIndexed { index, tx ->
        val typeLabel = if (tx.type == "IN") (if (isKurdish) "داهات (+)" else "INCOME") else (if (isKurdish) "مەسروف (-)" else "EXPENSE")
        PdfRowData(
          cells = listOf(
            (index + 1).toString(),
            dateFormat.format(Date(tx.dateMillis)),
            if (tx.notes.isNotBlank()) tx.notes.take(30) else tx.category,
            typeLabel,
            "${if (tx.type == "IN") "+" else "-"}${CurrencyService.formatShort(tx.amount, tx.currency)}",
            tx.currency
          ),
          isPositive = tx.type == "IN"
        )
      },
      summaryCards = listOf(
        SummaryCardData(
          label = if (isKurdish) "سەرجەمێ داهاتی (IN)" else "Total Inflow (+)",
          value = "+${CurrencyService.formatShort(totalIn, displayCurrency)}",
          color = Color.rgb(4, 120, 87)
        ),
        SummaryCardData(
          label = if (isKurdish) "سەرجەمێ مەسروفی (OUT)" else "Total Outflow (-)",
          value = "-${CurrencyService.formatShort(totalOut, displayCurrency)}",
          color = Color.rgb(185, 28, 28)
        ),
        SummaryCardData(
          label = if (isKurdish) "صافیێ کاشی" else "Net Cash Balance",
          value = "${if (netCash >= 0) "+" else ""}${CurrencyService.formatShort(netCash, displayCurrency)}",
          color = if (netCash >= 0) Color.rgb(4, 120, 87) else Color.rgb(185, 28, 28)
        )
      )
    )
  }

  fun generateAndShareTransactionsPdf(
    context: Context,
    transactions: List<TransactionEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean
  ): Boolean {
    val result = generateTransactionsPdf(context, transactions, displayCurrency, currentIqdRate, isKurdish) ?: return false
    sharePdfFile(context, result.file, result.documentTitle)
    return true
  }

  /**
   * Generates an official Real Estate Muluk & Gold Investments PDF Statement.
   */
  fun generateAssetsPdf(
    context: Context,
    assets: List<InvestmentEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean
  ): GeneratedPdfResult? {
    val title = if (isKurdish) "راپۆرتا فەرمی یا عەقارات و ملوکان" else "Real Estate & Assets Statement"
    val subtitle = if (isKurdish) "تەختا چاپکراو یا ملوک، عەقارات و سامانێن زێڕی • مێڤان زێباری" else "Real Estate Properties & Assets • Mevan Zebari"
    val fileName = "Muluk_Assets_${System.currentTimeMillis()}.pdf"

    val totalCost = assets.sumOf {
      CurrencyService.convert(it.initialCapital, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
    val totalValuation = assets.sumOf {
      CurrencyService.convert(it.currentValue, it.currency, displayCurrency, currentIqdRate, entryIqdRate = it.exchangeRateAtEntry)
    }
    val netGain = totalValuation - totalCost

    return createPdfDocument(
      context = context,
      fileName = fileName,
      documentTitle = title,
      subtitle = subtitle,
      displayCurrency = displayCurrency,
      isKurdish = isKurdish,
      headers = listOf("#", "Asset / Property Name", "Category", "Cost", "Valuation", "Gain/Loss"),
      columnWidths = floatArrayOf(25f, 170f, 80f, 85f, 85f, 78f),
      dataRows = assets.mapIndexed { index, a ->
        val gain = a.currentValue - a.initialCapital
        PdfRowData(
          cells = listOf(
            (index + 1).toString(),
            a.name,
            a.category,
            CurrencyService.formatShort(a.initialCapital, a.currency),
            CurrencyService.formatShort(a.currentValue, a.currency),
            "${if (gain >= 0) "+" else ""}${CurrencyService.formatShort(gain, a.currency)}"
          ),
          isPositive = gain >= 0
        )
      },
      summaryCards = listOf(
        SummaryCardData(
          label = if (isKurdish) "کۆیێ سەرمایێ ملوکان" else "Total Acquisition Cost",
          value = CurrencyService.formatShort(totalCost, displayCurrency),
          color = Color.rgb(15, 23, 42)
        ),
        SummaryCardData(
          label = if (isKurdish) "بهایێ نوکە یێ ملوکان" else "Current Market Valuation",
          value = CurrencyService.formatShort(totalValuation, displayCurrency),
          color = Color.rgb(4, 120, 87)
        ),
        SummaryCardData(
          label = if (isKurdish) "قازانجێ کۆیێ ملوکان" else "Net Asset Appreciation",
          value = "${if (netGain >= 0) "+" else ""}${CurrencyService.formatShort(netGain, displayCurrency)}",
          color = if (netGain >= 0) Color.rgb(4, 120, 87) else Color.rgb(185, 28, 28)
        )
      )
    )
  }

  fun generateAndShareAssetsPdf(
    context: Context,
    assets: List<InvestmentEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean
  ): Boolean {
    val result = generateAssetsPdf(context, assets, displayCurrency, currentIqdRate, isKurdish) ?: return false
    sharePdfFile(context, result.file, result.documentTitle)
    return true
  }

  // -------------------------------------------------------------
  // Internal PDF Rendering Engine (Multi-page, White-paper, Print-ready)
  // -------------------------------------------------------------

  fun createPdfDocument(
    context: Context,
    fileName: String,
    documentTitle: String,
    subtitle: String,
    displayCurrency: String,
    isKurdish: Boolean,
    headers: List<String>,
    columnWidths: FloatArray,
    dataRows: List<PdfRowData>,
    summaryCards: List<SummaryCardData>
  ): GeneratedPdfResult? {
    val document = PdfDocument()

    // Paints
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(15, 23, 42) // Slate 900
      textSize = 10f
    }
    val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(15, 23, 42)
      textSize = 10f
      isFakeBoldText = true
    }
    val paintHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(15, 23, 42)
      textSize = 15f
      isFakeBoldText = true
    }
    val paintSubtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(71, 85, 105) // Slate 600
      textSize = 9.5f
    }
    val paintMuted = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(100, 116, 139) // Slate 500
      textSize = 8.5f
    }
    val paintGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(4, 120, 87)
      textSize = 9.5f
      isFakeBoldText = true
    }
    val paintRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(185, 28, 28)
      textSize = 9.5f
      isFakeBoldText = true
    }
    val paintBg = Paint().apply {
      color = Color.WHITE
      style = Paint.Style.FILL
    }
    val paintFill = Paint().apply {
      style = Paint.Style.FILL
    }
    val paintStroke = Paint().apply {
      style = Paint.Style.STROKE
      strokeWidth = 0.75f
      color = Color.rgb(203, 213, 225) // Slate 300
    }

    val rowsPerPage = 22
    val totalPages = ((dataRows.size + rowsPerPage - 1) / rowsPerPage).coerceAtLeast(1)

    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    val timestampStr = dateFormat.format(Date())

    for (pageIndex in 0 until totalPages) {
      val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
      val page = document.startPage(pageInfo)
      val canvas: Canvas = page.canvas

      // 1. White paper background
      canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paintBg)

      var currentY = MARGIN

      // 2. Official Statement Top Header
      // Top Green Brand Accent Strip
      paintFill.color = Color.rgb(4, 120, 87) // Deep Emerald
      canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 3.5f, paintFill)
      currentY += 16f

      // Title & User Info
      canvas.drawText(documentTitle, MARGIN, currentY + 12f, paintHeader)
      canvas.drawText("MEVAN ZEBARI PORTFOLIO", PAGE_WIDTH - MARGIN - 145f, currentY + 10f, paintBold)
      currentY += 26f

      canvas.drawText(subtitle, MARGIN, currentY + 4f, paintSubtitle)
      canvas.drawText("Date: $timestampStr", PAGE_WIDTH - MARGIN - 145f, currentY + 4f, paintMuted)
      currentY += 16f

      canvas.drawText("Currency: $displayCurrency  •  Ref: DOC-${System.currentTimeMillis() % 100000}", MARGIN, currentY, paintMuted)
      currentY += 12f

      // Horizontal Divider
      paintStroke.color = Color.rgb(226, 232, 240) // Slate 200
      canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, paintStroke)
      currentY += 14f

      // 3. Table Header Row
      val tableWidth = PAGE_WIDTH - (MARGIN * 2)
      val headerHeight = 24f
      paintFill.color = Color.rgb(241, 245, 249) // Slate 100
      canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + headerHeight, paintFill)
      paintStroke.color = Color.rgb(148, 163, 184) // Slate 400
      canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + headerHeight, paintStroke)

      var curColX = MARGIN
      headers.forEachIndexed { colIdx, colTitle ->
        val w = if (colIdx < columnWidths.size) columnWidths[colIdx] else 80f
        canvas.drawText(colTitle, curColX + 5f, currentY + 15f, paintBold)
        curColX += w
      }
      currentY += headerHeight

      // 4. Data Rows for this page
      val startRow = pageIndex * rowsPerPage
      val endRow = (startRow + rowsPerPage).coerceAtMost(dataRows.size)
      val rowHeight = 20f

      for (rIdx in startRow until endRow) {
        val rowData = dataRows[rIdx]
        val isEven = (rIdx - startRow) % 2 == 0

        // Zebra background
        paintFill.color = if (isEven) Color.WHITE else Color.rgb(248, 250, 252) // Slate 50
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, paintFill)

        // Row border
        paintStroke.color = Color.rgb(226, 232, 240)
        canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, paintStroke)

        var cellX = MARGIN
        rowData.cells.forEachIndexed { cIdx, textVal ->
          val w = if (cIdx < columnWidths.size) columnWidths[cIdx] else 80f
          val isAmtCol = cIdx == 4 || cIdx == 3 && rowData.cells.size <= 5
          val rowPaint = if (isAmtCol) {
            if (rowData.isPositive) paintGreen else paintRed
          } else {
            paintText
          }

          // Truncate text if wider than column
          val truncated = truncateText(textVal, w - 8f, rowPaint)
          canvas.drawText(truncated, cellX + 5f, currentY + 13.5f, rowPaint)
          cellX += w
        }

        currentY += rowHeight
      }

      // Outer table border
      paintStroke.color = Color.rgb(148, 163, 184)
      canvas.drawRect(MARGIN, currentY - ((endRow - startRow) * rowHeight) - headerHeight, PAGE_WIDTH - MARGIN, currentY, paintStroke)
      currentY += 16f

      // 5. Summary AutoSum Cards (on final page)
      if (pageIndex == totalPages - 1 && summaryCards.isNotEmpty()) {
        val cardWidth = (tableWidth - (summaryCards.size - 1) * 8f) / summaryCards.size
        val cardHeight = 44f

        summaryCards.forEachIndexed { sIdx, card ->
          val cardX = MARGIN + sIdx * (cardWidth + 8f)
          paintFill.color = Color.rgb(248, 250, 252)
          canvas.drawRoundRect(RectF(cardX, currentY, cardX + cardWidth, currentY + cardHeight), 4f, 4f, paintFill)

          paintStroke.color = Color.rgb(203, 213, 225)
          canvas.drawRoundRect(RectF(cardX, currentY, cardX + cardWidth, currentY + cardHeight), 4f, 4f, paintStroke)

          // Card Label
          canvas.drawText(card.label, cardX + 8f, currentY + 16f, paintMuted)

          // Card Value
          val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = card.color
            textSize = 12f
            isFakeBoldText = true
          }
          canvas.drawText(card.value, cardX + 8f, currentY + 33f, valPaint)
        }
        currentY += cardHeight + 14f
      }

      // 6. Page Footer
      val footerY = PAGE_HEIGHT - MARGIN + 8f
      paintStroke.color = Color.rgb(226, 232, 240)
      canvas.drawLine(MARGIN, footerY - 14f, PAGE_WIDTH - MARGIN, footerY - 14f, paintStroke)

      canvas.drawText(
        if (isKurdish) "بەڵگەنامەیا فەرمی • هەمی ماف پاراستینە © Mevan Zebari" else "Confidential Financial Statement • All rights reserved © Mevan Zebari",
        MARGIN,
        footerY,
        paintMuted
      )
      val pageLabel = "Page ${pageIndex + 1} of $totalPages"
      canvas.drawText(pageLabel, PAGE_WIDTH - MARGIN - 60f, footerY, paintMuted)

      document.finishPage(page)
    }

    // Save PDF to cache
    return try {
      val exportDir = File(context.cacheDir, "pdf_exports")
      if (!exportDir.exists()) exportDir.mkdirs()
      val pdfFile = File(exportDir, fileName)

      FileOutputStream(pdfFile).use { out ->
        document.writeTo(out)
      }
      document.close()

      GeneratedPdfResult(
        file = pdfFile,
        documentTitle = documentTitle,
        subtitle = subtitle,
        totalPages = totalPages,
        displayCurrency = displayCurrency,
        isKurdish = isKurdish,
        headers = headers,
        columnWidths = columnWidths,
        dataRows = dataRows,
        summaryCards = summaryCards,
        timestampStr = timestampStr
      )
    } catch (e: Exception) {
      document.close()
      e.printStackTrace()
      Toast.makeText(
        context,
        if (isKurdish) "خەلەتیا دروستکرنا PDF: ${e.localizedMessage}" else "Error creating PDF: ${e.localizedMessage}",
        Toast.LENGTH_LONG
      ).show()
      null
    }
  }

  /**
   * Triggers the Android Native System Print Dialog via PrintManager.
   */
  fun printPdfFile(context: Context, file: File, jobName: String) {
    try {
      val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
      if (printManager != null) {
        val printAdapter = object : PrintDocumentAdapter() {
          override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
          ) {
            if (cancellationSignal?.isCanceled == true) {
              callback?.onLayoutCancelled()
              return
            }
            val pdi = PrintDocumentInfo.Builder(jobName)
              .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
              .build()
            callback?.onLayoutFinished(pdi, true)
          }

          override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback?
          ) {
            try {
              val input = FileInputStream(file)
              val output = FileOutputStream(destination?.fileDescriptor)
              val buf = ByteArray(16384)
              var bytesRead: Int
              while (input.read(buf).also { bytesRead = it } >= 0) {
                if (cancellationSignal?.isCanceled == true) {
                  callback?.onWriteCancelled()
                  input.close()
                  output.close()
                  return
                }
                output.write(buf, 0, bytesRead)
              }
              input.close()
              output.close()
              callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
              callback?.onWriteFailed(e.message)
            }
          }
        }
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
      } else {
        // Fallback to share/view
        sharePdfFile(context, file, jobName)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      sharePdfFile(context, file, jobName)
    }
  }

  /**
   * Shares the generated PDF file via the standard Android system share sheet.
   */
  fun sharePdfFile(context: Context, file: File, title: String) {
    try {
      val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(intent, title).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * Renders the pages of a generated PDF file into high-resolution Bitmaps for real-time Compose preview.
   */
  fun renderPdfPagesToBitmaps(file: File, maxPages: Int = 10, scaleFactor: Float = 1.6f): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    try {
      pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
      renderer = PdfRenderer(pfd)
      val pageCount = renderer.pageCount.coerceAtMost(maxPages)
      for (i in 0 until pageCount) {
        val page = renderer.openPage(i)
        val w = (page.width * scaleFactor).toInt().coerceAtLeast(1)
        val h = (page.height * scaleFactor).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmaps.add(bitmap)
      }
    } catch (e: Exception) {
      // In tests or headless environments, PdfRenderer may throw; caller falls back to Compose canvas preview
      e.printStackTrace()
    } finally {
      try {
        renderer?.close()
        pfd?.close()
      } catch (_: Exception) {}
    }
    return bitmaps
  }

  private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
    if (paint.measureText(text) <= maxWidth) return text
    var truncated = text
    while (truncated.isNotEmpty() && paint.measureText("$truncated…") > maxWidth) {
      truncated = truncated.dropLast(1)
    }
    return "$truncated…"
  }
}

/**
 * Result model returned after generating a PDF, containing file handle and metadata for Preview & Print.
 */
data class GeneratedPdfResult(
  val file: File,
  val documentTitle: String,
  val subtitle: String,
  val totalPages: Int,
  val displayCurrency: String,
  val isKurdish: Boolean,
  val headers: List<String>,
  val columnWidths: FloatArray,
  val dataRows: List<PdfRowData>,
  val summaryCards: List<SummaryCardData> = emptyList(),
  val timestampStr: String = ""
)

data class PdfRowData(
  val cells: List<String>,
  val isPositive: Boolean = true
)

data class SummaryCardData(
  val label: String,
  val value: String,
  val color: Int
)
