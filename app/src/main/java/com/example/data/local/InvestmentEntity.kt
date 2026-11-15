package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class InvestmentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val category: String, // "Cars & Vehicles", "Real Estate", "Stocks & ETFs", "Crypto", "Business Venture", "Other"
  val initialCapital: Double,
  val currentValue: Double,
  val currency: String = "USD",
  val purchaseDateMillis: Long = System.currentTimeMillis(),
  val notes: String = "",
  val isCoOwned: Boolean = false,
  val partnerName: String = "",
  val myInvestmentAmount: Double = 0.0,
  val partnerInvestmentAmount: Double = 0.0,
  val isSold: Boolean = false,
  val soldPrice: Double = 0.0,
  val deductedFromCash: Boolean = false,
  val soldDateMillis: Long = 0L,
  val exchangeRateAtEntry: Double = 0.0
) {
  val isHouse: Boolean get() = category.contains("House", ignoreCase = true) || category.contains("خانوو", ignoreCase = true) || category.contains("شوقە", ignoreCase = true) || category.contains("Apartment", ignoreCase = true) || category.contains("Residential", ignoreCase = true)
  val isLand: Boolean get() = category.contains("Land", ignoreCase = true) || category.contains("ئەرد", ignoreCase = true) || category.contains("زەوی", ignoreCase = true) || category.contains("Plot", ignoreCase = true)
  val isCommercial: Boolean get() = category.contains("Commercial", ignoreCase = true) || category.contains("بازرگانی", ignoreCase = true) || category.contains("بینا", ignoreCase = true) || category.contains("Building", ignoreCase = true)
  val isGoldValuable: Boolean get() = category.contains("Gold", ignoreCase = true) || category.contains("زێڕ", ignoreCase = true) || category.contains("زێر", ignoreCase = true) || category.contains("Valuable", ignoreCase = true) || category.contains("گرانبەها", ignoreCase = true)

  val isCar: Boolean get() = category.contains("Car", ignoreCase = true) || category.contains("Vehicle", ignoreCase = true)
  val isRealEstate: Boolean get() = !isGoldValuable && (category.contains("Real", ignoreCase = true) || category.contains("Property", ignoreCase = true) || category.contains("عەقار", ignoreCase = true) || isHouse || isLand || isCommercial)
  val isIndependentAsset: Boolean get() = isRealEstate || isGoldValuable
  val totalGainLoss: Double get() = (if (isSold && soldPrice > 0.0) soldPrice else currentValue) - initialCapital
  val returnOnInvestmentPercent: Double get() = if (initialCapital > 0) (totalGainLoss / initialCapital) * 100.0 else 0.0

  // Independent debt or remaining installment balance on this property / investment
  val independentDebtAmount: Double get() {
    val regex = Regex("""\[(?:DEBT|INDEPENDENT_DEBT|قەرز):([0-9]+(?:\.[0-9]+)?)\]""")
    val match = regex.find(notes)
    if (match != null) {
      return match.groupValues[1].toDoubleOrNull() ?: 0.0
    }
    return if (!isCoOwned && initialRemainingMoney > 0.0) initialRemainingMoney else 0.0
  }
  val netIndependentValuation: Double get() = (myCurrentValuation - independentDebtAmount).coerceAtLeast(0.0)

  // Gold Global Spot Market Price Parsing & Live Valuation (بهایێ عالمي یێ زێڕی)
  val isGoldTagged: Boolean get() = isGoldValuable || notes.contains("[GOLD_VALUABLE")

  val goldUnit: String get() {
    val regex = Regex("""\[GOLD_VALUABLE:[0-9.]+:(?:[0-9]+):([A-Z_]+)\]""")
    val match = regex.find(notes)
    if (match != null) return match.groupValues[1]
    if (notes.contains("گرام") || notes.contains("gram", ignoreCase = true)) return "GRAM"
    return "MITHQAL"
  }

  val goldKarat: Int get() {
    val regex = Regex("""\[GOLD_VALUABLE:[0-9.]+:(18|21|22|24):(?:[A-Z_]+)\]""")
    val match = regex.find(notes)
    if (match != null) return match.groupValues[1].toIntOrNull() ?: 21
    val karatMatch = Regex("""(18|21|22|24)\s*(?:عیار|عەیار|k|karat)""", RegexOption.IGNORE_CASE).find(notes + " " + name)
    return karatMatch?.groupValues?.get(1)?.toIntOrNull() ?: 21
  }

  val goldWeightMithqal: Double get() {
    if (!isGoldValuable) return 0.0
    val regex = Regex("""\[GOLD_VALUABLE:([0-9.]+):(18|21|22|24):([A-Z_]+)\]""")
    val match = regex.find(notes)
    if (match != null) {
      val raw = match.groupValues[1].toDoubleOrNull() ?: 0.0
      val unit = match.groupValues[3]
      return if (unit.contains("GRAM")) raw / 5.0 else raw
    }
    val mithqalMatch = Regex("""([0-9.]+)\s*(?:مسقال|میسقال|mithqal)""", RegexOption.IGNORE_CASE).find(notes + " " + name)
    if (mithqalMatch != null) return mithqalMatch.groupValues[1].toDoubleOrNull() ?: 0.0
    val gramMatch = Regex("""([0-9.]+)\s*(?:گرام|gram)""", RegexOption.IGNORE_CASE).find(notes + " " + name)
    if (gramMatch != null) return (gramMatch.groupValues[1].toDoubleOrNull() ?: 0.0) / 5.0
    return 0.0
  }

  val goldWeightGrams: Double get() {
    if (!isGoldValuable) return 0.0
    val m = goldWeightMithqal
    if (m > 0.0) return m * 5.0
    val gramMatch = Regex("""([0-9.]+)\s*(?:گرام|gram)""", RegexOption.IGNORE_CASE).find(notes + " " + name)
    if (gramMatch != null) return gramMatch.groupValues[1].toDoubleOrNull() ?: 0.0
    return 0.0
  }

  val goldPurityRatio: Double get() = when (goldKarat) {
    24 -> 1.0
    22 -> 22.0 / 24.0
    21 -> 21.0 / 24.0
    18 -> 18.0 / 24.0
    else -> 21.0 / 24.0
  }

  fun goldPricePerGramUsd(effectiveGoldPricePerGram24K: Double): Double {
    val benchmark = if (effectiveGoldPricePerGram24K > 0.0) effectiveGoldPricePerGram24K else 85.0
    return benchmark * goldPurityRatio
  }

  fun goldPricePerMithqalUsd(effectiveGoldPricePerGram24K: Double): Double {
    return goldPricePerGramUsd(effectiveGoldPricePerGram24K) * 5.0
  }

  fun goldEffectiveWeightGrams(benchmarkPrice24K: Double = 85.0): Double {
    val explicit = goldWeightGrams
    if (explicit > 0.0) return explicit
    if (!isGoldValuable) return 0.0
    val benchmarkPurityPrice = (if (benchmarkPrice24K > 0.0) benchmarkPrice24K else 85.0) * goldPurityRatio
    val baseValUsd = if (currency == "USD") {
      if (initialCapital > 0.0) initialCapital else currentValue
    } else {
      val rate = if (exchangeRateAtEntry > 0.0) exchangeRateAtEntry else 1500.0
      (if (initialCapital > 0.0) initialCapital else currentValue) / rate
    }
    return if (baseValUsd > 0.0 && benchmarkPurityPrice > 0.0) baseValUsd / benchmarkPurityPrice else 0.0
  }

  fun goldLiveValuationUsd(effectiveGoldPricePerGram24K: Double): Double {
    if (!isGoldValuable) return currentValue
    val grams = goldEffectiveWeightGrams(effectiveGoldPricePerGram24K)
    if (grams <= 0.0) return currentValue
    val pricePerGram = goldPricePerGramUsd(effectiveGoldPricePerGram24K)
    return grams * pricePerGram
  }

  fun goldLiveGainLossUsd(effectiveGoldPricePerGram24K: Double): Double {
    val liveValUsd = goldLiveValuationUsd(effectiveGoldPricePerGram24K)
    val initialUsd = if (currency == "USD") initialCapital else {
      val rate = if (exchangeRateAtEntry > 0.0) exchangeRateAtEntry else 1500.0
      initialCapital / rate
    }
    return liveValUsd - initialUsd
  }

  fun goldLiveRoiPercent(effectiveGoldPricePerGram24K: Double): Double {
    val initialUsd = if (currency == "USD") initialCapital else {
      val rate = if (exchangeRateAtEntry > 0.0) exchangeRateAtEntry else 1500.0
      initialCapital / rate
    }
    if (initialUsd <= 0.0) return 0.0
    val gain = goldLiveGainLossUsd(effectiveGoldPricePerGram24K)
    return (gain / initialUsd) * 100.0
  }

  // Partner Co-Ownership helpers
  val myEffectiveCapital: Double get() = if (isCoOwned && myInvestmentAmount > 0) myInvestmentAmount else initialCapital
  val partnerEffectiveCapital: Double get() = if (isCoOwned) partnerInvestmentAmount else 0.0
  val mySharePercent: Double get() = when {
    !isCoOwned -> 100.0
    initialCapital > 0 && myInvestmentAmount > 0 -> (myInvestmentAmount / initialCapital) * 100.0
    else -> 50.0
  }
  val partnerSharePercent: Double get() = when {
    !isCoOwned -> 0.0
    initialCapital > 0 && partnerInvestmentAmount > 0 -> (partnerInvestmentAmount / initialCapital) * 100.0
    else -> 50.0
  }
  val myCurrentValuation: Double get() = if (isCoOwned) currentValue * (mySharePercent / 100.0) else currentValue
  val partnerCurrentValuation: Double get() = if (isCoOwned) currentValue * (partnerSharePercent / 100.0) else 0.0
  val myProfitShare: Double get() = if (isCoOwned) totalGainLoss * (mySharePercent / 100.0) else totalGainLoss
  val partnerProfitShare: Double get() = if (isCoOwned) totalGainLoss * (partnerSharePercent / 100.0) else 0.0

  // Down Payment & Initial Remaining Balance for Cars
  val downPaymentTotal: Double get() = if (isCoOwned) {
    (myInvestmentAmount + partnerInvestmentAmount)
  } else {
    if (myInvestmentAmount > 0) myInvestmentAmount else initialCapital
  }

  val initialRemainingMoney: Double get() = maxOf(0.0, initialCapital - downPaymentTotal)
}

data class PartnershipShareValidation(
  val isValid: Boolean,
  val totalPrice: Double,
  val myShare: Double,
  val partnerShare: Double,
  val combinedShares: Double,
  val difference: Double, // > 0 means missing (کێمە), < 0 means excess (زێدەیە)
  val errorMessageKu: String = "",
  val errorMessageEn: String = ""
)

fun validatePartnershipShares(
  totalPrice: Double,
  myShare: Double,
  partnerShare: Double,
  currencySymbolOrCode: String = ""
): PartnershipShareValidation {
  val combined = myShare + partnerShare
  val diff = totalPrice - combined
  val isMatch = totalPrice > 0.0 && myShare >= 0.0 && partnerShare >= 0.0 && kotlin.math.abs(diff) <= 0.005
  if (isMatch) {
    return PartnershipShareValidation(
      isValid = true,
      totalPrice = totalPrice,
      myShare = myShare,
      partnerShare = partnerShare,
      combinedShares = combined,
      difference = 0.0
    )
  }
  val fmtNum: (Double) -> String = { v ->
    if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(v)
  }
  val unit = if (currencySymbolOrCode.isNotBlank()) " $currencySymbolOrCode" else ""
  val errKu = when {
    totalPrice <= 0.0 -> "خەلەتە، مەبلەغی کامل بکە! بهایێ کڕینێ داخل بکە."
    diff > 0.005 -> "خەلەتە، مەبلەغی کامل بکە! بهایێ کڕینێ (${fmtNum(totalPrice)}$unit) یە، بەلێ پشکا تە (${fmtNum(myShare)}$unit) و پشکا شەریکی (${fmtNum(partnerShare)}$unit) دبنە ${fmtNum(combined)}$unit — بڕێ ${fmtNum(diff)}$unit یا کێمە."
    else -> "خەلەتە، مەبلەغی کامل بکە! کۆیێ هەردوو پشکان (${fmtNum(combined)}$unit) ژ بهایێ کڕینێ (${fmtNum(totalPrice)}$unit) پترە ب بڕێ ${fmtNum(-diff)}$unit."
  }
  val errEn = when {
    totalPrice <= 0.0 -> "Invalid purchase price! Please enter the total purchase price."
    diff > 0.005 -> "Wrong amount, complete the full amount! Purchase price is ${fmtNum(totalPrice)}$unit, but your share (${fmtNum(myShare)}$unit) + partner share (${fmtNum(partnerShare)}$unit) = ${fmtNum(combined)}$unit (${fmtNum(diff)}$unit is missing)."
    else -> "Wrong amount, adjust the shares! Combined shares (${fmtNum(combined)}$unit) exceed purchase price (${fmtNum(totalPrice)}$unit) by ${fmtNum(-diff)}$unit."
  }
  return PartnershipShareValidation(
    isValid = false,
    totalPrice = totalPrice,
    myShare = myShare,
    partnerShare = partnerShare,
    combinedShares = combined,
    difference = diff,
    errorMessageKu = errKu,
    errorMessageEn = errEn
  )
}

@Entity(tableName = "car_payments")
data class CarPaymentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val carId: Long,
  val carName: String = "",
  val amount: Double,
  val currency: String = "USD",
  val paymentDateMillis: Long = System.currentTimeMillis(),
  val paymentMonth: String = "", // e.g. "Sep 2026", "2026-09"
  val paidBy: String = "ME", // "ME", "PARTNER", "SPLIT"
  val partnerName: String = "Partner",
  val myShare: Double = 0.0,
  val partnerShare: Double = 0.0,
  val notes: String = "",
  val exchangeRateAtEntry: Double = 0.0
) {
  val isPaidByMe: Boolean get() = paidBy == "ME"
  val isPaidByPartner: Boolean get() = paidBy == "PARTNER"
  val isSplit: Boolean get() = paidBy == "SPLIT"

  val effectiveMyShare: Double get() = when (paidBy) {
    "ME" -> amount
    "SPLIT", "CUSTOMER" -> if (myShare > 0) myShare else amount / 2.0
    else -> if (myShare > 0) myShare else 0.0
  }

  val effectivePartnerShare: Double get() = when (paidBy) {
    "PARTNER" -> amount
    "SPLIT", "CUSTOMER" -> if (partnerShare > 0) partnerShare else amount / 2.0
    else -> if (partnerShare > 0) partnerShare else 0.0
  }
}

@Entity(tableName = "car_services")
data class CarServiceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val carInvestmentId: Long,
  val carName: String,
  val serviceType: String, // "Oil Change", "Polishing & Detailing", "Tires & Brakes", "Engine / Transmission", "Bodywork & Paint", "Inspection / Licensing", "Other"
  val cost: Double,
  val currency: String = "USD",
  val paidBy: String = "ME", // "ME", "PARTNER", "SPLIT"
  val partnerName: String = "Partner",
  val dateMillis: Long = System.currentTimeMillis(),
  val notes: String = "",
  val exchangeRateAtEntry: Double = 0.0
) {
  val isPaidByMe: Boolean get() = paidBy == "ME"
  val isPaidByPartner: Boolean get() = paidBy == "PARTNER"
  val isSplit: Boolean get() = paidBy == "SPLIT"

  val myShareCost: Double get() = when (paidBy) {
    "ME" -> cost
    "SPLIT" -> cost / 2.0
    else -> 0.0
  }

  val partnerShareCost: Double get() = when (paidBy) {
    "PARTNER" -> cost
    "SPLIT" -> cost / 2.0
    else -> 0.0
  }
}

@Entity(tableName = "investment_month_records")
data class InvestmentMonthRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val investmentId: Long,
  val yearMonth: String, // "2026-08", "2026-09", etc.
  val gainLossAmount: Double,
  val recordedValue: Double,
  val currency: String = "USD",
  val timestamp: Long = System.currentTimeMillis(),
  val notes: String = "",
  val exchangeRateAtEntry: Double = 0.0
)
