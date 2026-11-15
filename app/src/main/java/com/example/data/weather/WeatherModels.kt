package com.example.data.weather

enum class WeatherConditionType {
  CLEAR,
  PARTLY_CLOUDY,
  CLOUDY,
  RAIN,
  SNOW,
  THUNDERSTORM;

  fun isPrecipitating(): Boolean = this == RAIN || this == SNOW || this == THUNDERSTORM
}

data class LiveWeatherData(
  val condition: WeatherConditionType = WeatherConditionType.CLEAR,
  val temperatureCelsius: Double = 18.0,
  val apparentTemperatureCelsius: Double = 17.5,
  val humidityPercent: Int = 55,
  val windSpeedKmh: Double = 12.0,
  val cityName: String = "Duhok",
  val cityNameKurdish: String = "دهۆک",
  val weatherCode: Int = 0,
  val isDay: Boolean = true,
  val isLiveLocation: Boolean = false,
  val latitude: Double = 36.8665,
  val longitude: Double = 42.9880,
  val conditionLabelEn: String = "Clear Sky",
  val conditionLabelKu: String = "سەقایێ ساماڵ و ئارام",
  val conditionIcon: String = "☀️",
  val simulatedOverride: WeatherConditionType? = null,
  val lastUpdatedMillis: Long = System.currentTimeMillis(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null
) {
  val activeCondition: WeatherConditionType
    get() = simulatedOverride ?: condition

  val isRaining: Boolean
    get() = activeCondition == WeatherConditionType.RAIN || activeCondition == WeatherConditionType.THUNDERSTORM

  val isSnowing: Boolean
    get() = activeCondition == WeatherConditionType.SNOW

  val isClear: Boolean
    get() = activeCondition == WeatherConditionType.CLEAR

  val isCloudy: Boolean
    get() = activeCondition == WeatherConditionType.CLOUDY || activeCondition == WeatherConditionType.PARTLY_CLOUDY

  val displayIcon: String
    get() = when (activeCondition) {
      WeatherConditionType.RAIN -> "🌧️"
      WeatherConditionType.SNOW -> "❄️"
      WeatherConditionType.THUNDERSTORM -> "⛈️"
      WeatherConditionType.CLOUDY -> "☁️"
      WeatherConditionType.PARTLY_CLOUDY -> if (isDay) "⛅" else "☁️"
      WeatherConditionType.CLEAR -> if (isDay) "☀️" else "🌙"
    }

  val displayLabelEn: String
    get() = when (activeCondition) {
      WeatherConditionType.RAIN -> "Live Rainfall"
      WeatherConditionType.SNOW -> "Live Snowfall"
      WeatherConditionType.THUNDERSTORM -> "Thunderstorm & Rain"
      WeatherConditionType.CLOUDY -> "Overcast Clouds"
      WeatherConditionType.PARTLY_CLOUDY -> "Partly Cloudy"
      WeatherConditionType.CLEAR -> if (isDay) "Clear & Sunny" else "Clear Night"
    }

  val displayLabelKu: String
    get() = when (activeCondition) {
      WeatherConditionType.RAIN -> "بارانا زیندی دباریت"
      WeatherConditionType.SNOW -> "بەفرا سپی دباریت"
      WeatherConditionType.THUNDERSTORM -> "برۆسی و بارانا ب خولک"
      WeatherConditionType.CLOUDY -> "ئاسمانێ هەوری"
      WeatherConditionType.PARTLY_CLOUDY -> "نیڤ هەور"
      WeatherConditionType.CLEAR -> if (isDay) "سەقایێ ساماڵ و گەش" else "شەڤا گەش و ساماڵ"
    }
}
