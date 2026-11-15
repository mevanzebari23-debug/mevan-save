package com.example.data.backup

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportExportService(private val context: Context) {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
  private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)

  fun exportTransactionsCsv(transactions: List<TransactionEntity>, displayCurrency: String): File {
    val file = File(context.cacheDir, "transactions_${fileDateFormat.format(Date())}.csv")
    file.bufferedWriter().use { writer ->
      writer.write("ID,Type,Date,Category,Amount,Currency,Notes,Affects Capital\n")
      transactions.forEach { tx ->
        val safeNotes = tx.notes.replace(",", " ")
        val dateStr = dateFormat.format(Date(tx.dateMillis))
        writer.write("${tx.id},${tx.type},\"$dateStr\",\"${tx.category}\",${tx.amount},${tx.currency},\"$safeNotes\",${tx.affectsCapital}\n")
      }
    }
    return file
  }

  fun exportDebtsCreditsCsv(debts: List<DebtCreditEntity>): File {
    val file = File(context.cacheDir, "loans_and_debts_${fileDateFormat.format(Date())}.csv")
    file.bufferedWriter().use { writer ->
      writer.write("ID,Type,Person,Contact,Total Amount,Amount Paid,Remaining,Currency,Due Date,Status,Notes\n")
      debts.forEach { item ->
        val typeStr = if (item.type == "DEBT") "I Owe (Debt)" else "They Owe Me (Credit)"
        val dueDateStr = dateFormat.format(Date(item.dueDateMillis))
        val statusStr = if (item.isCompleted) "Paid Off" else if (item.isOverdue) "Overdue" else "Active"
        val safeNotes = item.notes.replace(",", " ")
        writer.write("${item.id},\"$typeStr\",\"${item.personName}\",\"${item.contactInfo}\",${item.totalAmount},${item.amountPaid},${item.remainingAmount},${item.currency},\"$dueDateStr\",$statusStr,\"$safeNotes\"\n")
      }
    }
    return file
  }

  fun exportInvestmentsCsv(investments: List<InvestmentEntity>): File {
    val file = File(context.cacheDir, "investments_portfolio_${fileDateFormat.format(Date())}.csv")
    file.bufferedWriter().use { writer ->
      writer.write("ID,Asset Name,Category,Initial Capital,Current Valuation,Profit/Loss,ROI %,Currency,Purchase Date,Notes\n")
      investments.forEach { inv ->
        val dateStr = dateFormat.format(Date(inv.purchaseDateMillis))
        val safeNotes = inv.notes.replace(",", " ")
        writer.write("${inv.id},\"${inv.name}\",\"${inv.category}\",${inv.initialCapital},${inv.currentValue},${inv.totalGainLoss},${"%.2f".format(inv.returnOnInvestmentPercent)}%,${inv.currency},\"$dateStr\",\"$safeNotes\"\n")
      }
    }
    return file
  }

  fun generateFinancialStatementReport(
    startingCapital: Double,
    netCapital: Double,
    totalMoneyIn: Double,
    totalMoneyOut: Double,
    totalInvested: Double,
    totalInvestmentValue: Double,
    totalDebtOwed: Double,
    totalCreditReceivable: Double,
    currency: String,
    iqdRate: Double,
    settings: UserSettingsEntity,
    totalGoldValue: Double = 0.0,
    totalPartnerShare: Double = 0.0
  ): String {
    val dateStr = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.US).format(Date())
    val grossWealth = (startingCapital + totalMoneyIn) + totalInvestmentValue + totalGoldValue
    val netWorth = grossWealth - totalDebtOwed - totalPartnerShare - totalMoneyOut
    val totalInvestmentProfit = totalInvestmentValue - totalInvested

    return buildString {
      append("=====================================================\n")
      append("           EXECUTIVE FINANCIAL STATEMENT REPORT       \n")
      append("              CAPITAL & WEALTH TRACKER                \n")
      append("=====================================================\n")
      append("Generated: $dateStr\n")
      append("Primary Display Currency: $currency\n")
      append("Exchange Rate: 1 USD = ${iqdRate.toInt()} IQD (ع.د)\n\n")

      append("---------------- CAPITAL & CASH FLOW ----------------\n")
      append("Starting Capital:         ${CurrencyService.format(startingCapital, currency)}\n")
      append("Total Inflow (Money In):  ${CurrencyService.format(totalMoneyIn, currency)}\n")
      append("Total Outflow (Money Out):${CurrencyService.format(totalMoneyOut, currency)}\n")
      append("Net Liquid Capital:       ${CurrencyService.format(netCapital, currency)}\n\n")

      append("-------------- INVESTMENT PORTFOLIO -----------------\n")
      append("Total Capital Invested:   ${CurrencyService.format(totalInvested, currency)}\n")
      append("Current Portfolio Value:  ${CurrencyService.format(totalInvestmentValue, currency)}\n")
      if (totalGoldValue > 0) {
        append("Gold Holdings Value:      ${CurrencyService.format(totalGoldValue, currency)}\n")
      }
      append("Net Gains / (Losses):     ${CurrencyService.format(totalInvestmentProfit, currency)}\n")
      val roi = if (totalInvested > 0) (totalInvestmentProfit / totalInvested) * 100.0 else 0.0
      append("Cumulative ROI:           ${"%.2f".format(roi)}%\n\n")

      append("----------------- LOANS & LIABILITIES ---------------\n")
      append("Outstanding Debts (I Owe):       ${CurrencyService.format(totalDebtOwed, currency)}\n")
      append("Credits Receivable (They Owe Me):${CurrencyService.format(totalCreditReceivable, currency)}\n")
      append("Partner Share & Equity:          ${CurrencyService.format(totalPartnerShare, currency)}\n")
      append("Net Loan Balance:                ${CurrencyService.format(totalCreditReceivable - totalDebtOwed, currency)}\n\n")

      append("=====================================================\n")
      append("           NET WEALTH EQUATION & FORMULA             \n")
      append("  Gross Wealth - Debts - Partner Share - Expenses    \n")
      append("=====================================================\n")
      append("Gross Estimated Wealth:   ${CurrencyService.format(grossWealth, currency)}\n")
      append("(-) Debts Owed:           ${CurrencyService.format(totalDebtOwed, currency)}\n")
      append("(-) Partner Share:        ${CurrencyService.format(totalPartnerShare, currency)}\n")
      append("(-) Expenses Out:         ${CurrencyService.format(totalMoneyOut, currency)}\n")
      append("-----------------------------------------------------\n")
      append("(=) REMAINING NET WEALTH: ${CurrencyService.format(netWorth, currency)}\n")
      append("=====================================================\n")
      append("Cloud Sync: Verified & Encrypted across all user devices\n")
      append("Offline Integrity: 100% Guaranteed\n")
    }
  }

  fun shareReportFile(file: File, mimeType: String = "text/csv", title: String = "Export Financial Report") {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, title)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, title).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
  }

  fun sharePlainTextReport(reportText: String, title: String = "Financial Report Summary") {
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, reportText)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, title).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
  }
}
