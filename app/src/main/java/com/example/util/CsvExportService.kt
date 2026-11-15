package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.currency.CurrencyService
import com.example.data.local.DebtCreditEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportService {

  private fun escapeCsv(value: String): String {
    val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
    return if (needsQuotes) {
      "\"" + value.replace("\"", "\"\"") + "\""
    } else {
      value
    }
  }

  /**
   * Generates a fully standard RFC 4180 CSV string with UTF-8 BOM (\uFEFF)
   * so Microsoft Excel, Google Sheets, and LibreOffice display Kurdish Badini text natively.
   */
  fun generateTransactionsCsv(
    transactions: List<TransactionEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean = true
  ): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val sb = StringBuilder()

    // UTF-8 Byte Order Mark (BOM) for Excel Kurdish/Arabic text support
    sb.append('\uFEFF')

    // Header row
    val headers = if (isKurdish) {
      listOf(
        "ڕێز (#)",
        "بەروار و دەم",
        "جۆرێ تۆمارێ",
        "پۆل / جۆر",
        "بڕێ پارەی",
        "دراڤ",
        "نرخێ گۆڕینێ (IQD)",
        "بهایێ هاوتا ($displayCurrency)",
        "تێبینی / وەسف",
        "کاریگەری لسەر کاشێ"
      )
    } else {
      listOf(
        "Row (#)",
        "Date & Time",
        "Transaction Type",
        "Category",
        "Amount",
        "Currency",
        "Exchange Rate (IQD)",
        "Converted ($displayCurrency)",
        "Notes / Description",
        "Affects Cash"
      )
    }
    sb.appendLine(headers.joinToString(",") { escapeCsv(it) })

    // Data rows
    transactions.forEachIndexed { index, tx ->
      val dateStr = dateFormat.format(Date(tx.dateMillis))
      val typeStr = if (tx.type == "IN") {
        if (isKurdish) "داهات (+)" else "Income (+)"
      } else {
        if (isKurdish) "مەسروف (-)" else "Expense (-)"
      }
      val convertedVal = CurrencyService.convert(
        amount = tx.amount,
        from = tx.currency,
        to = displayCurrency,
        iqdRate = currentIqdRate,
        entryIqdRate = tx.exchangeRateAtEntry
      )
      val convertedFormatted = "%.2f".format(Locale.US, convertedVal)
      val rateStr = if (tx.exchangeRateAtEntry > 0.0) tx.exchangeRateAtEntry.toLong().toString() else currentIqdRate.toLong().toString()
      val cashImpactStr = if (tx.affectsCapital) {
        if (isKurdish) "بەلێ (د کاشی دایە)" else "Yes (Affects Cash)"
      } else {
        if (isKurdish) "نەخێر (جودایە)" else "No (Separate)"
      }

      val row = listOf(
        (index + 1).toString(),
        dateStr,
        typeStr,
        tx.category,
        "%.2f".format(Locale.US, tx.amount),
        tx.currency,
        rateStr,
        convertedFormatted,
        tx.notes,
        cashImpactStr
      )
      sb.appendLine(row.joinToString(",") { escapeCsv(it) })
    }

    return sb.toString()
  }

  /**
   * Generates CSV for Debts and Loans
   */
  fun generateDebtsCsv(
    debts: List<DebtCreditEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean = true
  ): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sb = StringBuilder()
    sb.append('\uFEFF')

    val headers = if (isKurdish) {
      listOf(
        "ڕێز (#)",
        "جۆرێ قەرزی",
        "ناڤێ کەسی / لایەنی",
        "کۆیێ گشتی",
        "دراڤ",
        "بڕێ واصلبووی",
        "باقیێ مایی",
        "دەمێ دانەوەیێ (Due Date)",
        "تێبینی"
      )
    } else {
      listOf(
        "Row (#)",
        "Loan Type",
        "Person / Entity",
        "Total Amount",
        "Currency",
        "Amount Paid",
        "Remaining Balance",
        "Due Date",
        "Notes"
      )
    }
    sb.appendLine(headers.joinToString(",") { escapeCsv(it) })

    debts.forEachIndexed { index, debt ->
      val typeStr = if (debt.type == "CREDIT") {
        if (isKurdish) "قەرزێ دەف خەلکی (Receivable)" else "Receivable (They owe me)"
      } else {
        if (isKurdish) "قەرزێ لسەر من (Payable)" else "Payable (I owe them)"
      }
      val dueStr = if (debt.dueDateMillis > 0L) dateFormat.format(Date(debt.dueDateMillis)) else "-"

      val row = listOf(
        (index + 1).toString(),
        typeStr,
        debt.personName,
        "%.2f".format(Locale.US, debt.totalAmount),
        debt.currency,
        "%.2f".format(Locale.US, debt.amountPaid),
        "%.2f".format(Locale.US, debt.remainingBalance),
        dueStr,
        debt.notes
      )
      sb.appendLine(row.joinToString(",") { escapeCsv(it) })
    }

    return sb.toString()
  }

  /**
   * Generates a standard RFC 4180 CSV string with UTF-8 BOM for Cars Report
   */
  fun generateCarsReportCsv(
    cars: List<InvestmentEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean = true
  ): String {
    val sb = StringBuilder()
    sb.append('\uFEFF')

    val headers = if (isKurdish) {
      listOf(
        "ڕێز (#)",
        "ناڤێ سەیارێ",
        "سەرمایێ کڕینێ",
        "بهایێ نوکە / فرۆتن",
        "دراڤ",
        "قازانج / زەرەر",
        "حاڵەت",
        "شەریکاتی",
        "پشک (%)",
        "تێبینی"
      )
    } else {
      listOf(
        "Row (#)",
        "Car Name",
        "Purchase Price",
        "Current / Sold Price",
        "Currency",
        "Profit / Loss",
        "Status",
        "Partnership",
        "My Share (%)",
        "Notes"
      )
    }
    sb.appendLine(headers.joinToString(",") { escapeCsv(it) })

    cars.forEachIndexed { index, car ->
      val profit = if (car.isSold) (car.soldPrice - car.initialCapital) else (car.currentValue - car.initialCapital)
      val statusStr = if (car.isSold) {
        if (isKurdish) "فرۆتی (Sold)" else "Sold"
      } else {
        if (isKurdish) "بەردەست (Active)" else "Active"
      }
      val partnerStr = if (car.isCoOwned) {
        if (isKurdish) "شەریکی (${car.partnerName})" else "Co-Owned (${car.partnerName})"
      } else {
        if (isKurdish) "ب تنێ" else "Solo"
      }

      val row = listOf(
        (index + 1).toString(),
        car.name,
        "%.2f".format(Locale.US, car.initialCapital),
        "%.2f".format(Locale.US, if (car.isSold) car.soldPrice else car.currentValue),
        car.currency,
        "%.2f".format(Locale.US, profit),
        statusStr,
        partnerStr,
        if (car.isCoOwned) "${car.mySharePercent}%" else "100%",
        car.notes
      )
      sb.appendLine(row.joinToString(",") { escapeCsv(it) })
    }

    return sb.toString()
  }

  /**
   * Generates a standard RFC 4180 CSV string with UTF-8 BOM for Real Estate & Assets Report
   */
  fun generateAssetsCsv(
    assets: List<InvestmentEntity>,
    displayCurrency: String,
    currentIqdRate: Double,
    isKurdish: Boolean = true
  ): String {
    val sb = StringBuilder()
    sb.append('\uFEFF')

    val headers = if (isKurdish) {
      listOf(
        "ڕێز (#)",
        "ناڤێ موڵکی / سامانی",
        "پۆل",
        "سەرمایێ دەستپێکێ",
        "بهایێ نوکە یێ بازاڕی",
        "دراڤ",
        "قازانج / زەرەر",
        "ڕێژا قازانجی (%)",
        "شەریکاتی",
        "تێبینی"
      )
    } else {
      listOf(
        "Row (#)",
        "Asset / Property Name",
        "Category",
        "Initial Cost",
        "Current Market Value",
        "Currency",
        "Gain / Loss",
        "ROI (%)",
        "Partnership",
        "Notes"
      )
    }
    sb.appendLine(headers.joinToString(",") { escapeCsv(it) })

    assets.forEachIndexed { index, asset ->
      val profit = asset.currentValue - asset.initialCapital
      val roi = if (asset.initialCapital > 0) (profit / asset.initialCapital) * 100.0 else 0.0
      val partnerStr = if (asset.isCoOwned) {
        if (isKurdish) "شەریکی (${asset.partnerName})" else "Co-Owned (${asset.partnerName})"
      } else {
        if (isKurdish) "ب تنێ" else "Solo"
      }

      val row = listOf(
        (index + 1).toString(),
        asset.name,
        asset.category,
        "%.2f".format(Locale.US, asset.initialCapital),
        "%.2f".format(Locale.US, asset.currentValue),
        asset.currency,
        "%.2f".format(Locale.US, profit),
        "%.1f%%".format(Locale.US, roi),
        partnerStr,
        asset.notes
      )
      sb.appendLine(row.joinToString(",") { escapeCsv(it) })
    }

    return sb.toString()
  }

  /**
   * Saves CSV file to app cache and triggers the Android Sharesheet.
   */
  fun exportAndShareCsv(
    context: Context,
    csvContent: String,
    baseFileName: String,
    title: String = "Export Excel CSV"
  ): Boolean {
    return try {
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      val file = File(exportDir, "${baseFileName}_$timeStamp.csv")

      FileOutputStream(file).use { out ->
        out.write(csvContent.toByteArray(Charsets.UTF_8))
      }

      val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.name)
        putExtra(Intent.EXTRA_TEXT, "Excel CSV Export: ${file.name}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(sendIntent, title).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
      true
    } catch (e: Exception) {
      // Fallback: Copy to clipboard and notify user
      try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Excel CSV Data", csvContent)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
        true
      } catch (_: Exception) {
        false
      }
    }
  }
}
