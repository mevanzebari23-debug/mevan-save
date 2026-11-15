package com.example.ui.categories

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed

enum class CategorySectionType {
  EXPENSE,
  INCOME,
  DEBT,
  REAL_ESTATE
}

data class AppCategoryItem(
  val id: String,
  val nameKu: String,
  val nameEn: String,
  val section: CategorySectionType,
  val emoji: String,
  val colorHex: Long
) {
  fun displayName(isKurdish: Boolean): String = if (isKurdish) "$emoji $nameKu" else "$emoji $nameEn"
  fun shortName(isKurdish: Boolean): String = if (isKurdish) nameKu else nameEn
  val color: Color get() = Color(colorHex)
}

object AppCategoryRegistry {

  // 1. Curated Expense Categories (مەسروفاتێن ڕۆژانە و کارگێڕی)
  val expenseCategories = listOf(
    AppCategoryItem("exp_groceries", "پێدڤیێن مالێ", "Groceries & Supplies", CategorySectionType.EXPENSE, "🛒", 0xFF10B981),
    AppCategoryItem("exp_utilities", "کارەب و ئاڤ و خزمەتگوزاری", "Utilities & Bills", CategorySectionType.EXPENSE, "💡", 0xFFF59E0B),
    AppCategoryItem("exp_fuel", "بەنزین و هاتنوچوون", "Fuel & Transport", CategorySectionType.EXPENSE, "⛽", 0xFF38BDF8),
    AppCategoryItem("exp_maintenance", "چاککردن و سێرڤس", "Maintenance & Repairs", CategorySectionType.EXPENSE, "🛠️", 0xFFF97316),
    AppCategoryItem("exp_health", "تەندروستی و دەرمان", "Health & Medical", CategorySectionType.EXPENSE, "🏥", 0xFFEF4444),
    AppCategoryItem("exp_school", "قوتابخانە و خواندن", "Education & School", CategorySectionType.EXPENSE, "🎓", 0xFF8B5CF6),
    AppCategoryItem("exp_dining", "خوارنگەهـ و کافێ", "Dining & Food", CategorySectionType.EXPENSE, "🍽️", 0xFFEC4899),
    AppCategoryItem("exp_charity", "بەخشین و زەکات", "Charity & Zakat", CategorySectionType.EXPENSE, "🎁", 0xFF14B8A6),
    AppCategoryItem("exp_personal", "جلوبەرگ و پێدڤیێن کەسی", "Clothing & Personal", CategorySectionType.EXPENSE, "👔", 0xFFA855F7),
    AppCategoryItem("exp_business", "مەسروفاتێن کار و بازرگانیێ", "Business Operations", CategorySectionType.EXPENSE, "📦", 0xFF6366F1),
    AppCategoryItem("exp_internet", "ئینتەرنێت و پەیوەندی", "Internet & Mobile", CategorySectionType.EXPENSE, "📱", 0xFF0EA5E9),
    AppCategoryItem("exp_travel", "گەشت و بێهنڤەدان", "Travel & Leisure", CategorySectionType.EXPENSE, "✈️", 0xFF06B6D4)
  )

  // 2. Curated Income Categories (داهات و زێدەکرنا کاشی)
  val incomeCategories = listOf(
    AppCategoryItem("inc_salary", "مووچە و داهاتێ سەرەکی", "Salary & Primary", CategorySectionType.INCOME, "💼", 0xFF10B981),
    AppCategoryItem("inc_trade", "قازانجێ بازرگانیێ", "Trade Profit", CategorySectionType.INCOME, "📈", 0xFFF59E0B),
    AppCategoryItem("inc_rent", "کرێ و داهاتێ موڵکی", "Rental Income", CategorySectionType.INCOME, "🏠", 0xFF38BDF8),
    AppCategoryItem("inc_cars", "قازانج و داهاتێ سەیاران", "Car Sales & Dividends", CategorySectionType.INCOME, "🚗", 0xFF8B5CF6),
    AppCategoryItem("inc_gold", "قازانجێ کڕین و فرۆتنا زێڕی", "Gold Trading Return", CategorySectionType.INCOME, "🥇", 0xFFEAB308),
    AppCategoryItem("inc_misc", "داهاتێ جۆراوجۆر یێ سەربەخۆ", "Misc Inflow", CategorySectionType.INCOME, "🤝", 0xFF14B8A6),
    AppCategoryItem("inc_gift", "دیاری و پاداشت", "Gift & Bonus", CategorySectionType.INCOME, "🎁", 0xFFEC4899)
  )

  // 3. Curated Debt & Loan Categories (قەرز و دەین)
  val debtCategories = listOf(
    AppCategoryItem("debt_family", "قەرزێ کەسوکار و هەڤالان", "Friends & Family", CategorySectionType.DEBT, "👥", 0xFF38BDF8),
    AppCategoryItem("debt_business", "بازرگانی و کەلوپەل", "Commercial & Business", CategorySectionType.DEBT, "🏢", 0xFFF59E0B),
    AppCategoryItem("debt_real_estate", "عەقارات و بیناسازی", "Real Estate Loans", CategorySectionType.DEBT, "🏗️", 0xFF10B981),
    AppCategoryItem("debt_car", "قەرزێ کڕینا سەیارێ", "Automotive Debt", CategorySectionType.DEBT, "🚗", 0xFFF97316),
    AppCategoryItem("debt_urgent", "قەرزێ فریاگوزاری و لەزگین", "Urgent & Emergency", CategorySectionType.DEBT, "⚡", 0xFFEF4444),
    AppCategoryItem("debt_bank", "بانکی و فەرمی", "Bank & Official", CategorySectionType.DEBT, "🏛️", 0xFF8B5CF6)
  )

  // 4. Curated Real Estate & Assets Categories (ملوک و وەبەرهێنان)
  val realEstateCategories = listOf(
    AppCategoryItem("asset_house", "خانوو و شوقە", "Residential Property", CategorySectionType.REAL_ESTATE, "🏠", 0xFF38BDF8),
    AppCategoryItem("asset_commercial", "زەڤی و پارچەیێن بازرگانی", "Commercial Land", CategorySectionType.REAL_ESTATE, "🏢", 0xFF10B981),
    AppCategoryItem("asset_farmland", "عەرد و بیستان", "Agricultural Estate", CategorySectionType.REAL_ESTATE, "🌾", 0xFF84CC16),
    AppCategoryItem("asset_warehouse", "دوکان و کۆگەهـ", "Retail & Warehouses", CategorySectionType.REAL_ESTATE, "🏬", 0xFFF59E0B),
    AppCategoryItem("asset_construction", "پڕۆژەیێ بیناسازی", "Construction Project", CategorySectionType.REAL_ESTATE, "🏗️", 0xFFF97316)
  )

  fun getPresets(section: CategorySectionType): List<AppCategoryItem> = when (section) {
    CategorySectionType.EXPENSE -> expenseCategories
    CategorySectionType.INCOME -> incomeCategories
    CategorySectionType.DEBT -> debtCategories
    CategorySectionType.REAL_ESTATE -> realEstateCategories
  }

  /**
   * Combines default built-in presets with user custom categories.
   */
  fun getCombinedCategoryNames(
    section: CategorySectionType,
    customCategories: List<String>,
    isKurdish: Boolean
  ): List<String> {
    val presetNames = getPresets(section).map { it.displayName(isKurdish) }
    val cleanedCustom = customCategories
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .filter { custom ->
        presetNames.none { preset ->
          preset.contains(custom, ignoreCase = true) || custom.contains(preset, ignoreCase = true)
        }
      }
    return (presetNames + cleanedCustom).distinct()
  }

  fun findCategoryColor(categoryName: String): Color {
    val clean = categoryName.trim().lowercase()
    val all = expenseCategories + incomeCategories + debtCategories + realEstateCategories
    val match = all.firstOrNull {
      clean.contains(it.nameKu.lowercase()) ||
        clean.contains(it.nameEn.lowercase()) ||
        clean.contains(it.emoji)
    }
    return match?.color ?: when {
      clean.contains("داهات") || clean.contains("income") -> EmeraldLight
      clean.contains("مەسروف") || clean.contains("expense") -> LossRed
      clean.contains("قەرز") || clean.contains("debt") || clean.contains("loan") -> Color(0xFFF97316)
      clean.contains("زێڕ") || clean.contains("gold") -> GoldAccent
      clean.contains("سەیارە") || clean.contains("car") -> Color(0xFF38BDF8)
      else -> Color(0xFF94A3B8)
    }
  }

  fun findCategoryEmoji(categoryName: String): String {
    val clean = categoryName.trim().lowercase()
    val all = expenseCategories + incomeCategories + debtCategories + realEstateCategories
    val match = all.firstOrNull {
      clean.contains(it.nameKu.lowercase()) ||
        clean.contains(it.nameEn.lowercase()) ||
        clean.contains(it.emoji)
    }
    return match?.emoji ?: when {
      clean.contains("داهات") || clean.contains("income") -> "📥"
      clean.contains("مەسروف") || clean.contains("expense") -> "📤"
      clean.contains("قەرز") || clean.contains("debt") || clean.contains("loan") -> "🤝"
      clean.contains("زێڕ") || clean.contains("gold") -> "🥇"
      clean.contains("سەیارە") || clean.contains("car") -> "🚗"
      clean.contains("موڵک") || clean.contains("خانوو") || clean.contains("estate") -> "🏢"
      else -> "🏷️"
    }
  }
}
