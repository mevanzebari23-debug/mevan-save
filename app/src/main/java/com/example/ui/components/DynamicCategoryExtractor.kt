package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.Color
import com.example.data.local.InvestmentEntity
import com.example.ui.FinanceUiState
import com.example.ui.theme.HoloNeonBlue
import com.example.ui.theme.HoloNeonCyan
import com.example.ui.theme.HoloNeonEmerald
import com.example.ui.theme.HoloNeonGold
import com.example.ui.theme.HoloNeonViolet
import com.example.ui.theme.LossRed

data class CategoryQuickItem(
  val key: String,
  val label: String,
  val count: Int? = null,
  val accentColor: Color,
  val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

/**
 * Intelligent Dynamic Extractor that scans ALL items entered by the user
 * (Cars, Real Estate, Debts, Gold, School) and generates dynamic category buttons.
 * Whatever the user enters ("هەر تشت داخل بکەم") automatically lines up as a button ("وەکی دوکمەی ڕیز ببیتن").
 */
object DynamicCategoryExtractor {

  private val KNOWN_CAR_BRANDS = listOf(
    "Toyota" to "Toyota • تۆیۆتا",
    "Land Cruiser" to "Land Cruiser • لاندکرۆزەر",
    "Lexus" to "Lexus • لێکسز",
    "BMW" to "BMW • بی ئێم",
    "Mercedes" to "Mercedes • مێرسیدس",
    "Nissan" to "Nissan • نیسان",
    "Patrol" to "Patrol • پاترۆل",
    "Hyundai" to "Hyundai • هیۆندای",
    "Kia" to "Kia • کیا",
    "GMC" to "GMC • جی ئێم سی",
    "Chevrolet" to "Chevrolet • شۆفرلێت",
    "Cadillac" to "Cadillac • کادیلاک",
    "Ford" to "Ford • فۆڕد",
    "Dodge" to "Dodge • دۆج",
    "Jeep" to "Jeep • جیپ",
    "Range Rover" to "Range Rover • ڕەینج",
    "Audi" to "Audi • ئۆدی",
    "Porsche" to "Porsche • پۆرش",
    "Volkswagen" to "Volkswagen • ڤۆڵکس",
    "Honda" to "Honda • هۆندا",
    "Mazda" to "Mazda • مەزدا"
  )

  /**
   * Dynamically extracts all distinct car brands present in the user's inventory
   * plus standard popular brands.
   */
  fun extractCarBrandButtons(
    cars: List<InvestmentEntity>,
    isKurdish: Boolean
  ): List<CategoryQuickItem> {
    val result = mutableListOf<CategoryQuickItem>()

    // Always include All Cars button
    result.add(
      CategoryQuickItem(
        key = "ALL_CARS",
        label = if (isKurdish) "🚗 هەمی سەیارە" else "🚗 All Cars",
        count = cars.size.takeIf { it > 0 },
        accentColor = HoloNeonCyan,
        icon = Icons.Default.DirectionsCar
      )
    )

    // Check each known brand against user's cars
    val matchedKeys = mutableSetOf<String>()
    for ((brandKey, brandLabel) in KNOWN_CAR_BRANDS) {
      val count = cars.count {
        it.name.contains(brandKey, ignoreCase = true) ||
          it.notes.contains(brandKey, ignoreCase = true) ||
          (brandKey == "Mercedes" && (it.name.contains("Benz", ignoreCase = true) || it.name.contains("مێرسیدس", ignoreCase = true))) ||
          (brandKey == "Land Cruiser" && it.name.contains("لاندکرۆزەر", ignoreCase = true)) ||
          (brandKey == "Toyota" && it.name.contains("تۆیۆتا", ignoreCase = true))
      }
      if (count > 0 || brandKey in listOf("Toyota", "Land Cruiser", "BMW", "Mercedes", "Nissan")) {
        matchedKeys.add(brandKey.lowercase())
        val color = when (brandKey) {
          "Toyota", "Land Cruiser" -> HoloNeonCyan
          "BMW", "Mercedes", "Audi" -> HoloNeonBlue
          "Nissan", "Hyundai", "Kia" -> HoloNeonEmerald
          "Lexus", "Porsche", "Range Rover" -> HoloNeonGold
          else -> HoloNeonViolet
        }
        result.add(
          CategoryQuickItem(
            key = brandKey,
            label = brandLabel,
            count = count.takeIf { it > 0 },
            accentColor = color,
            icon = Icons.Default.DirectionsCar
          )
        )
      }
    }

    // Now dynamically detect any custom brand/word entered by the user in cars
    for (car in cars) {
      val firstWord = car.name.trim().split(" ", "-", "_").firstOrNull()?.trim() ?: ""
      if (firstWord.length >= 3 && !matchedKeys.contains(firstWord.lowercase())) {
        val count = cars.count { it.name.contains(firstWord, ignoreCase = true) }
        matchedKeys.add(firstWord.lowercase())
        result.add(
          CategoryQuickItem(
            key = firstWord,
            label = "🚗 $firstWord",
            count = count.takeIf { it > 0 },
            accentColor = HoloNeonGold,
            icon = Icons.Default.DirectionsCar
          )
        )
      }
    }

    return result
  }

  /**
   * Dynamically extracts all real estate / property / investment categories
   */
  fun extractRealEstateButtons(
    investments: List<InvestmentEntity>,
    isKurdish: Boolean
  ): List<CategoryQuickItem> {
    val housesCount = investments.count { it.isHouse }
    val landsCount = investments.count { it.isLand }
    val apartmentsCount = investments.count { it.isHouse && (it.name.contains("شوقە", ignoreCase = true) || it.name.contains("Apartment", ignoreCase = true) || it.category.contains("Apartment", ignoreCase = true)) }
    val commercialCount = investments.count { it.isCommercial }
    val goldCount = investments.count { it.isGoldValuable }

    val result = mutableListOf<CategoryQuickItem>()

    result.add(
      CategoryQuickItem(
        key = "ALL_ASSETS",
        label = if (isKurdish) "🏢 هەمی ملوک" else "🏢 All Properties",
        count = investments.size.takeIf { it > 0 },
        accentColor = HoloNeonEmerald,
        icon = Icons.Default.Apartment
      )
    )

    result.add(
      CategoryQuickItem(
        key = "خانوو",
        label = if (isKurdish) "🏡 خانوو و ڤێلا" else "🏡 Houses & Villas",
        count = housesCount.takeIf { it > 0 },
        accentColor = HoloNeonEmerald,
        icon = Icons.Default.Home
      )
    )

    result.add(
      CategoryQuickItem(
        key = "زەوی",
        label = if (isKurdish) "🗺️ زەوی و پارچە" else "🗺️ Lands",
        count = landsCount.takeIf { it > 0 },
        accentColor = HoloNeonEmerald,
        icon = Icons.Default.Place
      )
    )

    result.add(
      CategoryQuickItem(
        key = "شوقە",
        label = if (isKurdish) "🏢 شوقە" else "🏢 Apartments",
        count = apartmentsCount.takeIf { it > 0 },
        accentColor = HoloNeonBlue,
        icon = Icons.Default.Apartment
      )
    )

    if (commercialCount > 0) {
      result.add(
        CategoryQuickItem(
          key = "بازرگانی",
          label = if (isKurdish) "🏪 بازرگانی و دکان" else "🏪 Commercial",
          count = commercialCount,
          accentColor = HoloNeonGold,
          icon = Icons.Default.Apartment
        )
      )
    }

    // Dynamically detect any custom investment categories or locations entered by user
    val customCategories = investments
      .map { it.category.trim() }
      .filter { it.isNotBlank() && it !in listOf("Real Estate", "Car", "Gold", "عەقارات", "سەیارە", "زێڕ") }
      .distinct()

    for (cat in customCategories.take(4)) {
      val count = investments.count { it.category.equals(cat, ignoreCase = true) }
      if (count > 0) {
        result.add(
          CategoryQuickItem(
            key = cat,
            label = "📁 $cat",
            count = count,
            accentColor = HoloNeonCyan,
            icon = Icons.Default.Apartment
          )
        )
      }
    }

    return result
  }

  /**
   * Dynamically extracts debt categories and (optionally) individual contacts entered by user.
   * Individual contact names (like Sara) stay exclusively inside the Debts tab and use exact PERSON: keys
   * so a person's name only appears with that person's own records, not everywhere across the app.
   */
  fun extractDebtButtons(
    uiState: FinanceUiState,
    isKurdish: Boolean,
    includePersonButtons: Boolean = true
  ): List<CategoryQuickItem> {
    val debts = uiState.debtsCredits
    val activeDebts = debts.filter { !it.isCompleted }
    val receivables = activeDebts.filter { it.type == "CREDIT" || it.type == "RECEIVABLE" }
    val payables = activeDebts.filter { it.type == "DEBT" || it.type == "PAYABLE" }
    val overdue = activeDebts.filter { it.dueDateMillis > 0 && it.dueDateMillis < System.currentTimeMillis() }
    val settled = debts.filter { it.isCompleted }

    val result = mutableListOf<CategoryQuickItem>()

    result.add(
      CategoryQuickItem(
        key = "ALL_DEBTS",
        label = if (isKurdish) "🤝 هەمی قەرز" else "🤝 All Loans",
        count = activeDebts.size.takeIf { it > 0 },
        accentColor = HoloNeonCyan,
        icon = Icons.Default.Handshake
      )
    )

    result.add(
      CategoryQuickItem(
        key = "دەف خەلکی",
        label = if (isKurdish) "📥 دەف خەلکی (Receivables)" else "📥 Receivables",
        count = receivables.size.takeIf { it > 0 },
        accentColor = HoloNeonCyan,
        icon = Icons.Default.Handshake
      )
    )

    result.add(
      CategoryQuickItem(
        key = "لسەر من",
        label = if (isKurdish) "📤 لسەر من (Payables)" else "📤 Payables",
        count = payables.size.takeIf { it > 0 },
        accentColor = LossRed,
        icon = Icons.Default.Handshake
      )
    )

    if (overdue.isNotEmpty()) {
      result.add(
        CategoryQuickItem(
          key = "درەنگکەفتی",
          label = if (isKurdish) "⚠️ درەنگکەفتی (Overdue)" else "⚠️ Overdue",
          count = overdue.size,
          accentColor = Color(0xFFFF5252),
          icon = Icons.Default.Handshake
        )
      )
    }

    if (settled.isNotEmpty()) {
      result.add(
        CategoryQuickItem(
          key = "تەواوکری",
          label = if (isKurdish) "✅ تەواوکری (Settled)" else "✅ Settled",
          count = settled.size,
          accentColor = HoloNeonEmerald,
          icon = Icons.Default.Handshake
        )
      )
    }

    if (includePersonButtons) {
      // Dynamically detect individual contacts entered by the user inside Debts tab only
      val distinctPersons = activeDebts
        .groupBy { it.personName.trim() }
        .filter { it.key.isNotBlank() }
        .map { (name, list) -> name to list.size }
        .sortedByDescending { it.second }

      for ((personName, count) in distinctPersons) {
        result.add(
          CategoryQuickItem(
            key = "PERSON:$personName",
            label = "👤 $personName",
            count = count,
            accentColor = HoloNeonGold,
            icon = Icons.Default.Person
          )
        )
      }
    }

    return result
  }

  /**
   * Extracts all distinct category & brand buttons across the entire app
   * (without leaking individual debtor names into the general home strip)
   */
  fun extractAllQuickItems(
    uiState: FinanceUiState,
    isKurdish: Boolean
  ): List<CategoryQuickItem> {
    val items = mutableListOf<CategoryQuickItem>()

    // 1. Car Brands
    items.addAll(extractCarBrandButtons(uiState.carInvestments, isKurdish))

    // 2. Real Estate & Properties
    items.addAll(extractRealEstateButtons(uiState.nonCarInvestments, isKurdish))

    // 3. Gold & Silver
    val goldCount = uiState.activeGoldHoldings.size
    items.add(
      CategoryQuickItem(
        key = "زێڕ",
        label = if (isKurdish) "🟡 زێڕ و زیڤ (Gold)" else "🟡 Gold & Silver",
        count = goldCount.takeIf { it > 0 },
        accentColor = HoloNeonGold,
        icon = Icons.Default.MonetizationOn
      )
    )

    // 4. Debt Category Buttons only (keep individual person names strictly inside Debts tab)
    items.addAll(extractDebtButtons(uiState, isKurdish, includePersonButtons = false))

    // 5. School
    val schoolCount = uiState.schoolTransactions.size
    if (schoolCount > 0) {
      items.add(
        CategoryQuickItem(
          key = "قوتابخانە",
          label = if (isKurdish) "🎓 قوتابخانە (School)" else "🎓 School Vault",
          count = schoolCount,
          accentColor = HoloNeonViolet,
          icon = Icons.Default.School
        )
      )
    }

    return items
  }
}
