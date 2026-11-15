package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val type: String = "OUT", // "OUT" for Expense, "IN" for Income
  val createdAtMillis: Long = System.currentTimeMillis()
)

typealias ExpenseCategories = ExpenseCategoryEntity
