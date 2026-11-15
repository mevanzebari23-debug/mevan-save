package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    TransactionEntity::class,
    InvestmentEntity::class,
    InvestmentMonthRecordEntity::class,
    DebtCreditEntity::class,
    DebtPaymentEntity::class,
    BudgetEntity::class,
    UserSettingsEntity::class,
    GoldHoldingEntity::class,
    CarServiceEntity::class,
    CarPaymentEntity::class,
    ExpenseCategoryEntity::class
  ],
  version = 10,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun financeDao(): FinanceDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE investments ADD COLUMN isCoOwned INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE investments ADD COLUMN partnerName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE investments ADD COLUMN myInvestmentAmount REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE investments ADD COLUMN partnerInvestmentAmount REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE investments ADD COLUMN isSold INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE investments ADD COLUMN soldPrice REAL NOT NULL DEFAULT 0.0")
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `car_services` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `carInvestmentId` INTEGER NOT NULL,
            `carName` TEXT NOT NULL,
            `serviceType` TEXT NOT NULL,
            `cost` REAL NOT NULL,
            `currency` TEXT NOT NULL,
            `paidBy` TEXT NOT NULL,
            `partnerName` TEXT NOT NULL,
            `dateMillis` INTEGER NOT NULL,
            `notes` TEXT NOT NULL
          )
        """.trimIndent())
      }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `car_payments` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `carId` INTEGER NOT NULL,
            `carName` TEXT NOT NULL DEFAULT '',
            `amount` REAL NOT NULL,
            `currency` TEXT NOT NULL DEFAULT 'USD',
            `paymentDateMillis` INTEGER NOT NULL,
            `paymentMonth` TEXT NOT NULL DEFAULT '',
            `paidBy` TEXT NOT NULL DEFAULT 'ME',
            `partnerName` TEXT NOT NULL DEFAULT 'Partner',
            `myShare` REAL NOT NULL DEFAULT 0.0,
            `partnerShare` REAL NOT NULL DEFAULT 0.0,
            `notes` TEXT NOT NULL DEFAULT ''
          )
        """.trimIndent())
      }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_settings ADD COLUMN language TEXT NOT NULL DEFAULT 'KU'")
      }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE investments ADD COLUMN deductedFromCash INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE investments ADD COLUMN soldDateMillis INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE gold_holdings ADD COLUMN deductedFromCash INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE gold_holdings ADD COLUMN isSold INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE gold_holdings ADD COLUMN soldPriceUsd REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE gold_holdings ADD COLUMN soldDateMillis INTEGER NOT NULL DEFAULT 0")
      }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE debts_credits ADD COLUMN affectCashBalance INTEGER NOT NULL DEFAULT 0")
      }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE debts_credits ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE debts_credits ADD COLUMN cashImpactAmount REAL NOT NULL DEFAULT -1.0")
        db.execSQL("ALTER TABLE debts_credits ADD COLUMN cashPaidImpactAmount REAL NOT NULL DEFAULT -1.0")
        db.execSQL("ALTER TABLE debt_payments ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE debt_payments ADD COLUMN affectCashBalance INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE investments ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE car_payments ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE car_services ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE investment_month_records ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE gold_holdings ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE budgets ADD COLUMN exchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE user_settings ADD COLUMN capitalExchangeRateAtEntry REAL NOT NULL DEFAULT 0.0")
      }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `expense_categories` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL,
            `type` TEXT NOT NULL DEFAULT 'OUT',
            `createdAtMillis` INTEGER NOT NULL
          )
        """.trimIndent())
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "capital_debt_database.db"
        )
          .addMigrations(
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10
          )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
