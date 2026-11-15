package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val category: String, // "All Spending", "Business", "Living & Food", "Personal", "Investments"
  val monthlyLimit: Double,
  val currency: String = "USD",
  val alertThresholdPercent: Int = 80, // alert when reaching 80%
  val isRecurring: Boolean = true,
  val exchangeRateAtEntry: Double = 0.0
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val startingCapital: Double = 0.0,
  val capitalCurrency: String = "USD",
  val displayCurrency: String = "USD", // "USD" or "IQD"
  val iqdPerUsdRate: Double = 1310.0, // User-written / market rate (e.g. 1,310 or 1,480 - 1,530)
  val isCustomIqdRate: Boolean = true, // When user writes rate, keep it persistent across app close/open!
  val eurPerUsdRate: Double = 0.92,
  val lastRateUpdateMillis: Long = System.currentTimeMillis(),
  val biometricEnabled: Boolean = false,
  val pinCode: String = "1234",
  val isAppLocked: Boolean = false,
  val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
  val language: String = "KU", // "KU" (Badini Kurdish) or "EN" (English)
  val lastCloudBackupMillis: Long = 0L,
  val cloudBackupStatus: String = "Ready",
  val cloudDeviceId: String = "Device-Android-Alpha",
  val capitalExchangeRateAtEntry: Double = 0.0
)
