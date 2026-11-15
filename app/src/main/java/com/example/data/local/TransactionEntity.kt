package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: String, // "IN" or "OUT"
  val amount: Double,
  val currency: String = "USD", // "USD", "IQD", "EUR"
  val category: String, // e.g., "Salary", "Investment Payout", "Business", "Capital Injection", "Food", "Rent", etc.
  val dateMillis: Long = System.currentTimeMillis(),
  val notes: String = "",
  val affectsCapital: Boolean = true,
  val exchangeRateAtEntry: Double = 0.0
)
