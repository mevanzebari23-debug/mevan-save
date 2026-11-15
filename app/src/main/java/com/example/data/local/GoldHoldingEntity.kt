package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gold_holdings")
data class GoldHoldingEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val metalType: String = "GOLD",
  val title: String = "Physical Gold",
  val weightGrams: Double,           // e.g. 210.0 grams
  val buyPricePerGram: Double,       // e.g. 127.0 USD
  val currency: String = "USD",
  val purchaseDateMillis: Long = System.currentTimeMillis(),
  val karat: Int = 24,               // 24, 22, 21, 18
  val purityLabel: String = "24K",   // "24K", "22K", "21K", "18K"
  val notes: String = "",
  val deductedFromCash: Boolean = false,
  val isSold: Boolean = false,
  val soldPriceUsd: Double = 0.0,
  val soldDateMillis: Long = 0L,
  val exchangeRateAtEntry: Double = 0.0
) {
  val isSilver: Boolean get() = metalType.equals("SILVER", ignoreCase = true)
  val isGold: Boolean get() = !isSilver

  // If a user entered the 1Kg price (e.g., 2000 USD) directly into buyPricePerGram for silver, normalize to per-gram
  val effectiveBuyPricePerGram: Double
    get() = if (isSilver && buyPricePerGram > 50.0) buyPricePerGram / 1000.0 else buyPricePerGram

  val totalCost: Double
    get() = weightGrams * effectiveBuyPricePerGram

  val buyPricePerKg: Double
    get() = effectiveBuyPricePerGram * 1000.0

  fun getPurityFactor(): Double {
    if (isSilver) {
      return when (karat) {
        9999, 999, 1000, 24 -> 1.0
        925 -> 0.925
        900 -> 0.900
        else -> 1.0
      }
    }
    return when (karat) {
      24 -> 1.0
      22 -> 22.0 / 24.0
      21 -> 21.0 / 24.0
      18 -> 18.0 / 24.0
      else -> 1.0
    }
  }

  fun getDisplayPurityBadge(isKurdish: Boolean): String {
    return if (isSilver) {
      val p = if (karat == 925) "925" else "9999"
      if (isKurdish) "عەیار $p زیڤ" else "Silver $p"
    } else {
      if (isKurdish) "عەیار ${karat}K زێڕ" else "${karat}K Gold"
    }
  }

  fun currentValue(
    marketPricePerGram24K: Double,
    marketSilverPricePerGram9999: Double = 2.00745
  ): Double {
    val baseGramPrice = if (isSilver) marketSilverPricePerGram9999 else marketPricePerGram24K
    return weightGrams * (baseGramPrice * getPurityFactor())
  }

  fun profitLoss(
    marketPricePerGram24K: Double,
    marketSilverPricePerGram9999: Double = 2.00745
  ): Double {
    return currentValue(marketPricePerGram24K, marketSilverPricePerGram9999) - totalCost
  }

  fun roiPercent(
    marketPricePerGram24K: Double,
    marketSilverPricePerGram9999: Double = 2.00745
  ): Double {
    if (totalCost <= 0) return 0.0
    return (profitLoss(marketPricePerGram24K, marketSilverPricePerGram9999) / totalCost) * 100.0
  }

  // Weight in Iraqi Mithqal (1 mithqal = 5 grams in retail trade)
  val weightMithqal: Double
    get() = weightGrams / 5.0

  // Weight in Kilograms (1 kg = 1000 grams)
  val weightKilograms: Double
    get() = weightGrams / 1000.0
}
