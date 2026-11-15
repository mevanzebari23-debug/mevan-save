package com.example.data.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class GoldCandlePoint(
  val timeMillis: Long,
  val openPerGram: Double,
  val highPerGram: Double,
  val lowPerGram: Double,
  val closePerGram: Double
)

data class GoldPriceQuote(
  val spotPricePerTroyOzUsd: Double,
  val pricePerGram24K: Double,
  val pricePerGram22K: Double,
  val pricePerGram21K: Double,
  val pricePerGram18K: Double,
  val pricePerMithqal21K: Double,
  val priceChange24hUsd: Double,
  val priceChangePercent24h: Double,
  val high24hPerGram: Double,
  val low24hPerGram: Double,
  val isRising: Boolean,
  val silverSpotPricePerTroyOzUsd: Double = OnlineGoldService.BASELINE_SILVER_SPOT_OZ,
  val silverPricePerGram9999: Double = OnlineGoldService.BASELINE_SILVER_PRICE_PER_GRAM_9999,
  val silverPricePerGram925: Double = OnlineGoldService.BASELINE_SILVER_PRICE_PER_GRAM_9999 * 0.925,
  val silverPricePerKg9999: Double = OnlineGoldService.BASELINE_SILVER_PRICE_PER_KG_9999,
  val silverChangePercent24h: Double = 1.18,
  val isSilverRising: Boolean = true,
  val timestamp: Long = System.currentTimeMillis(),
  val isOnline: Boolean = true,
  val statusMessage: String = "Live Global XAU/USD & XAG/USD Spot Feed",
  val candles: List<GoldCandlePoint> = emptyList()
)

object OnlineGoldService {
  const val GRAMS_PER_TROY_OUNCE = 31.1034768
  const val TROY_OUNCES_PER_KG = 1000.0 / GRAMS_PER_TROY_OUNCE // 32.1507466 oz per 1 Kg
  private const val MITHQAL_IN_GRAMS = 5.0

  // Baseline global market prices (~$120/g 24K Gold, ~$62.44/oz = ~$2,007.45/Kg 9999 Fine Silver)
  const val BASELINE_PRICE_PER_GRAM = 120.00
  const val BASELINE_SPOT_OZ = BASELINE_PRICE_PER_GRAM * GRAMS_PER_TROY_OUNCE
  const val BASELINE_SILVER_SPOT_OZ = 62.4387
  const val BASELINE_SILVER_PRICE_PER_GRAM_9999 = BASELINE_SILVER_SPOT_OZ / GRAMS_PER_TROY_OUNCE // ~$2.00745/g
  const val BASELINE_SILVER_PRICE_PER_KG_9999 = BASELINE_SILVER_PRICE_PER_GRAM_9999 * 1000.0 // ~$2,007.45/Kg

  private data class LiveMetalsFeedResult(
    val goldSpotOz: Double?,
    val silverSpotOz: Double?,
    val goldChangeOz: Double?,
    val goldChangePct: Double?,
    val silverChangePct: Double?,
    val sourceName: String
  )

  suspend fun fetchLiveGoldPrice(): GoldPriceQuote = withContext(Dispatchers.IO) {
    try {
      var goldSpotOz: Double? = null
      var silverSpotOz: Double? = null
      var goldChangeOz = 0.0
      var goldChangePct = 0.0
      var silverChangePct = 0.85
      var highGoldOz: Double? = null
      var lowGoldOz: Double? = null
      val activeSources = mutableListOf<String>()

      // 1. Primary Global Precious Metals Feed: GoldPrice.org (Live XAU/USD & XAG/USD)
      val gpResult = fetchFromGoldPriceOrg()
      if (gpResult != null) {
        if (gpResult.goldSpotOz != null && gpResult.goldSpotOz > 500.0) {
          goldSpotOz = gpResult.goldSpotOz
        }
        if (gpResult.silverSpotOz != null && gpResult.silverSpotOz > 10.0) {
          silverSpotOz = gpResult.silverSpotOz
        }
        gpResult.goldChangeOz?.let { goldChangeOz = it }
        gpResult.goldChangePct?.let { goldChangePct = it }
        gpResult.silverChangePct?.let { silverChangePct = it }
        activeSources.add(gpResult.sourceName)
      }

      // 2. Institutional Swissquote Live BBO Feed for XAG/USD (Silver) & XAU/USD (Gold)
      if (silverSpotOz == null) {
        val sqSilver = fetchSwissquoteSpot("XAG")
        if (sqSilver != null && sqSilver > 10.0) {
          silverSpotOz = sqSilver
          activeSources.add("Swissquote XAG/USD")
        }
      }
      if (goldSpotOz == null) {
        val sqGold = fetchSwissquoteSpot("XAU")
        if (sqGold != null && sqGold > 500.0) {
          goldSpotOz = sqGold
          activeSources.add("Swissquote XAU/USD")
        }
      }

      // 3. Global Currency/Metals CDN API (@fawazahmed0/currency-api on jsDelivr)
      if (silverSpotOz == null) {
        val cdnSilver = fetchJsDelivrMetalSpot("xag")
        if (cdnSilver != null && cdnSilver > 10.0) {
          silverSpotOz = cdnSilver
          activeSources.add("Global XAG Spot CDN")
        }
      }
      if (goldSpotOz == null) {
        val cdnGold = fetchJsDelivrMetalSpot("xau")
        if (cdnGold != null && cdnGold > 500.0) {
          goldSpotOz = cdnGold
          activeSources.add("Global XAU Spot CDN")
        }
      }

      // 4. Optional Binance PAXG/USDT ticker for 24h Gold High/Low & backup Gold spot
      try {
        val tickerUrl = URL("https://api.binance.com/api/v3/ticker/24hr?symbol=PAXGUSDT")
        val tickerConn = (tickerUrl.openConnection() as HttpURLConnection).apply {
          connectTimeout = 3500
          readTimeout = 3500
          requestMethod = "GET"
          setRequestProperty("Accept", "application/json")
        }
        if (tickerConn.responseCode == 200) {
          val jsonStr = tickerConn.inputStream.bufferedReader().use { it.readText() }
          val root = JSONObject(jsonStr)
          val paxgLast = root.optDouble("lastPrice", 0.0)
          if (goldSpotOz == null && paxgLast > 500.0) {
            goldSpotOz = paxgLast
            activeSources.add("PAXG Spot")
          }
          if (goldChangePct == 0.0) {
            goldChangeOz = root.optDouble("priceChange", 0.0)
            goldChangePct = root.optDouble("priceChangePercent", 0.0)
          }
          val h = root.optDouble("highPrice", 0.0)
          val l = root.optDouble("lowPrice", 0.0)
          if (h > 500.0) highGoldOz = h
          if (l > 500.0) lowGoldOz = l
        }
        tickerConn.disconnect()
      } catch (_: Exception) {
        // Ignore if Binance is geo-restricted; primary XAU/XAG feeds already handled
      }

      if (goldSpotOz != null || silverSpotOz != null) {
        val finalGoldOz = goldSpotOz ?: BASELINE_SPOT_OZ
        val finalSilverOz = silverSpotOz ?: BASELINE_SILVER_SPOT_OZ

        val pricePerGram24K = finalGoldOz / GRAMS_PER_TROY_OUNCE
        val pricePerGram22K = pricePerGram24K * (22.0 / 24.0)
        val pricePerGram21K = pricePerGram24K * (21.0 / 24.0)
        val pricePerGram18K = pricePerGram24K * (18.0 / 24.0)
        val pricePerMithqal21K = pricePerGram21K * MITHQAL_IN_GRAMS

        val high24hGram = (highGoldOz ?: (finalGoldOz * 1.012)) / GRAMS_PER_TROY_OUNCE
        val low24hGram = (lowGoldOz ?: (finalGoldOz * 0.988)) / GRAMS_PER_TROY_OUNCE
        val changeGram = goldChangeOz / GRAMS_PER_TROY_OUNCE

        // Exact Global Silver 9999 math: 1 Kg = 1000g = 32.1507466 Troy Ounces
        val silverGram9999 = finalSilverOz / GRAMS_PER_TROY_OUNCE
        val silverKg9999 = silverGram9999 * 1000.0
        val silverGram925 = silverGram9999 * 0.925

        val candles = fetchCandles(pricePerGram24K)

        return@withContext GoldPriceQuote(
          spotPricePerTroyOzUsd = finalGoldOz,
          pricePerGram24K = pricePerGram24K,
          pricePerGram22K = pricePerGram22K,
          pricePerGram21K = pricePerGram21K,
          pricePerGram18K = pricePerGram18K,
          pricePerMithqal21K = pricePerMithqal21K,
          priceChange24hUsd = changeGram,
          priceChangePercent24h = goldChangePct,
          high24hPerGram = high24hGram,
          low24hPerGram = low24hGram,
          isRising = goldChangePct >= 0.0,
          silverSpotPricePerTroyOzUsd = finalSilverOz,
          silverPricePerGram9999 = silverGram9999,
          silverPricePerGram925 = silverGram925,
          silverPricePerKg9999 = silverKg9999,
          silverChangePercent24h = silverChangePct,
          isSilverRising = silverChangePct >= 0.0,
          timestamp = System.currentTimeMillis(),
          isOnline = true,
          statusMessage = "Live Global Spot (${activeSources.joinToString(", ").ifBlank { "XAU & XAG" }})",
          candles = candles
        )
      }

      createFallbackQuote(isOnline = false, message = "Offline: Using global XAU & XAG spot benchmark")
    } catch (e: Exception) {
      createFallbackQuote(isOnline = false, message = "Offline: Using global XAU & XAG spot benchmark")
    }
  }

  private fun fetchFromGoldPriceOrg(): LiveMetalsFeedResult? {
    return try {
      val url = URL("https://data-asg.goldprice.org/dbXRates/USD")
      val conn = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 4000
        readTimeout = 4000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
        setRequestProperty("Accept", "application/json")
      }
      if (conn.responseCode == 200) {
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(body)
        val items = root.optJSONArray("items")
        if (items != null && items.length() > 0) {
          val item = items.getJSONObject(0)
          val xau = item.optDouble("xauPrice", 0.0)
          val xag = item.optDouble("xagPrice", 0.0)
          val chgXau = item.optDouble("chgXau", 0.0)
          val pcXau = item.optDouble("pcXau", 0.0)
          val pcXag = item.optDouble("pcXag", 0.0)
          conn.disconnect()
          return LiveMetalsFeedResult(
            goldSpotOz = if (xau > 500.0) xau else null,
            silverSpotOz = if (xag > 10.0) xag else null,
            goldChangeOz = chgXau,
            goldChangePct = pcXau,
            silverChangePct = pcXag,
            sourceName = "GoldPrice.org XAU/XAG"
          )
        }
      }
      conn.disconnect()
      null
    } catch (_: Exception) {
      null
    }
  }

  private fun fetchSwissquoteSpot(metalSymbol: String): Double? {
    return try {
      val url = URL("https://forex-data-feed.swissquote.com/public-quotes/bboquotes/instrument/$metalSymbol/USD")
      val conn = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 4000
        readTimeout = 4000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
        setRequestProperty("Accept", "application/json")
      }
      if (conn.responseCode == 200) {
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val arr = JSONArray(body)
        for (i in 0 until arr.length()) {
          val platformObj = arr.getJSONObject(i)
          val prices = platformObj.optJSONArray("spreadProfilePrices") ?: continue
          if (prices.length() > 0) {
            val firstProfile = prices.getJSONObject(0)
            val bid = firstProfile.optDouble("bid", 0.0)
            val ask = firstProfile.optDouble("ask", 0.0)
            if (bid > 0.0 && ask > 0.0) {
              conn.disconnect()
              return (bid + ask) / 2.0
            }
          }
        }
      }
      conn.disconnect()
      null
    } catch (_: Exception) {
      null
    }
  }

  private fun fetchJsDelivrMetalSpot(metalCodeLower: String): Double? {
    val urls = listOf(
      "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/$metalCodeLower.json",
      "https://latest.currency-api.pages.dev/v1/currencies/$metalCodeLower.json"
    )
    for (endpoint in urls) {
      try {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
          connectTimeout = 4000
          readTimeout = 4000
          requestMethod = "GET"
          setRequestProperty("Accept", "application/json")
        }
        if (conn.responseCode == 200) {
          val body = conn.inputStream.bufferedReader().use { it.readText() }
          val root = JSONObject(body)
          val metalObj = root.optJSONObject(metalCodeLower)
          val usdVal = metalObj?.optDouble("usd", 0.0) ?: 0.0
          conn.disconnect()
          if (usdVal > 10.0) {
            return usdVal
          }
        }
        conn.disconnect()
      } catch (_: Exception) {
        // try next mirror
      }
    }
    return null
  }

  private fun fetchCandles(fallbackGramPrice: Double): List<GoldCandlePoint> {
    return try {
      val klineUrl = URL("https://api.binance.com/api/v3/klines?symbol=PAXGUSDT&interval=1h&limit=24")
      val klineConn = (klineUrl.openConnection() as HttpURLConnection).apply {
        connectTimeout = 3500
        readTimeout = 3500
        requestMethod = "GET"
      }
      if (klineConn.responseCode == 200) {
        val jsonStr = klineConn.inputStream.bufferedReader().use { it.readText() }
        val array = JSONArray(jsonStr)
        val points = mutableListOf<GoldCandlePoint>()
        for (i in 0 until array.length()) {
          val candle = array.getJSONArray(i)
          val time = candle.getLong(0)
          val open = candle.getDouble(1) / GRAMS_PER_TROY_OUNCE
          val high = candle.getDouble(2) / GRAMS_PER_TROY_OUNCE
          val low = candle.getDouble(3) / GRAMS_PER_TROY_OUNCE
          val close = candle.getDouble(4) / GRAMS_PER_TROY_OUNCE
          points.add(GoldCandlePoint(time, open, high, low, close))
        }
        points
      } else {
        generateSimulatedCandles(fallbackGramPrice)
      }
    } catch (e: Exception) {
      generateSimulatedCandles(fallbackGramPrice)
    }
  }

  fun createFallbackQuote(
    customPricePerGram: Double = BASELINE_PRICE_PER_GRAM,
    customSilverPricePerGram9999: Double = BASELINE_SILVER_PRICE_PER_GRAM_9999,
    isOnline: Boolean = false,
    message: String = "Global XAU & XAG Spot Rates"
  ): GoldPriceQuote {
    val spotOz = customPricePerGram * GRAMS_PER_TROY_OUNCE
    val price24K = customPricePerGram
    val price22K = price24K * (22.0 / 24.0)
    val price21K = price24K * (21.0 / 24.0)
    val price18K = price24K * (18.0 / 24.0)
    val priceMithqal = price21K * MITHQAL_IN_GRAMS

    val silverSpotOz = customSilverPricePerGram9999 * GRAMS_PER_TROY_OUNCE
    val silverKg9999 = customSilverPricePerGram9999 * 1000.0
    val silverGram925 = customSilverPricePerGram9999 * 0.925

    val changePercent = -5.51
    val changeUsd = price24K * (changePercent / 100.0)

    return GoldPriceQuote(
      spotPricePerTroyOzUsd = spotOz,
      pricePerGram24K = price24K,
      pricePerGram22K = price22K,
      pricePerGram21K = price21K,
      pricePerGram18K = price18K,
      pricePerMithqal21K = priceMithqal,
      priceChange24hUsd = changeUsd,
      priceChangePercent24h = changePercent,
      high24hPerGram = price24K * 1.025,
      low24hPerGram = price24K * 0.985,
      isRising = changePercent >= 0,
      silverSpotPricePerTroyOzUsd = silverSpotOz,
      silverPricePerGram9999 = customSilverPricePerGram9999,
      silverPricePerGram925 = silverGram925,
      silverPricePerKg9999 = silverKg9999,
      silverChangePercent24h = 1.18,
      isSilverRising = true,
      timestamp = System.currentTimeMillis(),
      isOnline = isOnline,
      statusMessage = message,
      candles = generateSimulatedCandles(price24K)
    )
  }

  fun generateSimulatedCandles(baseGramPrice: Double): List<GoldCandlePoint> {
    val now = System.currentTimeMillis()
    val hourMillis = 3600 * 1000L
    val points = mutableListOf<GoldCandlePoint>()
    var currentPrice = baseGramPrice * 0.98
    for (i in 24 downTo 0) {
      val time = now - (i * hourMillis)
      val variance = (Math.sin(i.toDouble() * 0.5) * 1.5) + ((Math.random() - 0.48) * 1.2)
      val open = currentPrice
      val close = (currentPrice + variance).coerceAtLeast(10.0)
      val high = maxOf(open, close) + (Math.random() * 0.8)
      val low = minOf(open, close) - (Math.random() * 0.8)
      currentPrice = close
      points.add(GoldCandlePoint(time, open, high, low, close))
    }
    return points
  }
}
