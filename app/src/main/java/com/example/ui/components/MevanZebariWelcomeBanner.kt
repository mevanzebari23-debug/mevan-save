package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.weather.LiveWeatherData
import com.example.data.weather.LocationHelper
import com.example.data.weather.WeatherConditionType
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import java.util.Calendar

@Composable
fun MevanZebariWelcomeBanner(
  weatherData: LiveWeatherData,
  isKurdish: Boolean,
  onRefreshWeather: () -> Unit,
  onSetSimulatedCondition: (WeatherConditionType?) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isTesterExpanded by remember { mutableStateOf(false) }

  // Location permission launcher
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    if (perms.values.any { it }) {
      onRefreshWeather()
    }
  }

  // Calculate greeting by current hour
  val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
  val timeGreetingKu = when (currentHour) {
    in 5..11 -> "سپێدە باش، ڕۆژەکا پڕی بەرەکەت"
    in 12..16 -> "نیڤرۆ باش، سەرمایێ تە یێ ئارامە"
    in 17..22 -> "ئێڤارە باش، خێر د مال و سەرمایێ تە دا"
    else -> "شەڤ باش، سەرمایێ تە د پاراستییە"
  }
  val timeGreetingEn = when (currentHour) {
    in 5..11 -> "Good morning, wishing you a prosperous day"
    in 12..16 -> "Good afternoon, your assets are growing"
    in 17..22 -> "Good evening, blessings upon your wealth"
    else -> "Good night, your capital is secure"
  }

  // Dynamic atmospheric card theme gradient - Translucent Frosted Glass
  val activeCond = weatherData.activeCondition
  val bgGradientColors = when (activeCond) {
    WeatherConditionType.RAIN, WeatherConditionType.THUNDERSTORM -> listOf(
      Color(0x350F2027),
      Color(0x28203A43),
      Color(0x301B3B4B)
    )
    WeatherConditionType.SNOW -> listOf(
      Color(0x350D1B2A),
      Color(0x281B263B),
      Color(0x30243B55)
    )
    WeatherConditionType.CLOUDY, WeatherConditionType.PARTLY_CLOUDY -> listOf(
      Color(0x351A2634),
      Color(0x28243342),
      Color(0x302D3E50)
    )
    WeatherConditionType.CLEAR -> if (weatherData.isDay) {
      listOf(
        Color(0x350A2619),
        Color(0x28133E29),
        Color(0x301D4D33)
      )
    } else {
      listOf(
        Color(0x350C1322),
        Color(0x28162032),
        Color(0x301F2D44)
      )
    }
  }

  val animatedBorderColor by animateColorAsState(
    targetValue = when (activeCond) {
      WeatherConditionType.RAIN -> Color(0xFF64B5F6)
      WeatherConditionType.SNOW -> Color(0xFFB3E5FC)
      WeatherConditionType.THUNDERSTORM -> Color(0xFFFFD54F)
      else -> GoldAccent
    },
    animationSpec = tween(700),
    label = "bannerBorder"
  )

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("mevan_zebari_welcome_banner")
      .border(
        width = 1.3.dp,
        brush = Brush.horizontalGradient(
          listOf(
            animatedBorderColor.copy(alpha = 0.75f),
            GoldAccent.copy(alpha = 0.50f),
            animatedBorderColor.copy(alpha = 0.70f)
          )
        ),
        shape = RoundedCornerShape(24.dp)
      )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.linearGradient(bgGradientColors))
    ) {
      // 0. Futuristic Command Center Hero Backdrop Asset
      Image(
        painter = painterResource(id = R.drawable.img_fin_hero_banner),
        contentDescription = null,
        modifier = Modifier
          .matchParentSize()
          .alpha(0.24f),
        contentScale = ContentScale.Crop
      )

      // 1. Animated Atmospheric Particle Layer (Rain, Snow, Shimmer)
      WeatherParticleCanvas(
        condition = activeCond,
        modifier = Modifier.matchParentSize(),
        densityMultiplier = 1.0f
      )

      // 2. Foreground Content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        // Top Row: Royal VIP Capsule & Weather Status Indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Royal VIP Zebari Badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(30.dp))
              .background(GoldAccent.copy(alpha = 0.18f))
              .border(0.8.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(30.dp))
              .padding(horizontal = 9.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.WorkspacePremium,
              contentDescription = "VIP",
              tint = GoldAccent,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = if (isKurdish) "👑 زێباری • ZEBARI" else "👑 ZEBARI VIP",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp,
              color = GoldAccent
            )
          }

          // Live Weather Badge with Location
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color.Black.copy(alpha = 0.35f))
              .border(0.7.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
              .clickable {
                if (!LocationHelper.hasLocationPermission(context)) {
                  locationPermissionLauncher.launch(
                    arrayOf(
                      Manifest.permission.ACCESS_FINE_LOCATION,
                      Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                  )
                } else {
                  onRefreshWeather()
                }
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Location",
              tint = if (weatherData.isLiveLocation) EmeraldLight else GoldAccent,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isKurdish) weatherData.cityNameKurdish else weatherData.cityName,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${weatherData.temperatureCelsius.toInt()}°C",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = if (activeCond == WeatherConditionType.SNOW) Color(0xFFE1F5FE) else GoldAccent
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Grand Title: Welcome Mevan Zebari
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isKurdish) "بخێرهاتی میڤان زێباری 👑" else "Welcome Mevan Zebari 👑",
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              letterSpacing = 0.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (isKurdish) timeGreetingKu else timeGreetingEn,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.85f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Weather Icon Big Avatar Badge
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    animatedBorderColor.copy(alpha = 0.35f),
                    Color.Transparent
                  )
                )
              )
              .border(1.dp, animatedBorderColor.copy(alpha = 0.40f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = weatherData.displayIcon,
              fontSize = 25.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Condition Strip (Rain/Snow State + Metrics)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Weather Condition Status Text
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(
                  when {
                    weatherData.isRaining -> Color(0xFF4FC3F7)
                    weatherData.isSnowing -> Color(0xFFE1F5FE)
                    else -> EmeraldLight
                  },
                  CircleShape
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKurdish) weatherData.displayLabelKu else weatherData.displayLabelEn,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          // Compact Metrics (Humidity, Wind)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Opacity,
                contentDescription = "Humidity",
                tint = Color(0xFF81D4FA),
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "${weatherData.humidityPercent}%",
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Air,
                contentDescription = "Wind",
                tint = Color(0xFFA5D6A7),
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "${weatherData.windSpeedKmh.toInt()} km/h",
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weather Simulator & Live Controls Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1. Live GPS / Kurdistan
          WeatherModeChip(
            label = if (isKurdish) "📡 ڕاستەوخۆ" else "📡 Live GPS",
            isSelected = weatherData.simulatedOverride == null,
            onClick = { onSetSimulatedCondition(null) }
          )

          // 2. Rain Mode
          WeatherModeChip(
            label = if (isKurdish) "🌧️ باران" else "🌧️ Rain",
            isSelected = weatherData.simulatedOverride == WeatherConditionType.RAIN,
            onClick = { onSetSimulatedCondition(WeatherConditionType.RAIN) }
          )

          // 3. Snow Mode
          WeatherModeChip(
            label = if (isKurdish) "❄️ بەفر" else "❄️ Snow",
            isSelected = weatherData.simulatedOverride == WeatherConditionType.SNOW,
            onClick = { onSetSimulatedCondition(WeatherConditionType.SNOW) }
          )

          // 4. Clear Mode
          WeatherModeChip(
            label = if (isKurdish) "☀️ ساماڵ" else "☀️ Clear",
            isSelected = weatherData.simulatedOverride == WeatherConditionType.CLEAR,
            onClick = { onSetSimulatedCondition(WeatherConditionType.CLEAR) }
          )

          // 5. Thunder Mode
          WeatherModeChip(
            label = if (isKurdish) "⛈️ برۆسی" else "⛈️ Thunder",
            isSelected = weatherData.simulatedOverride == WeatherConditionType.THUNDERSTORM,
            onClick = { onSetSimulatedCondition(WeatherConditionType.THUNDERSTORM) }
          )

          // 6. Refresh Weather Button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.12f))
              .clickable { onRefreshWeather() }
              .padding(horizontal = 8.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh",
              tint = Color.White,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WeatherModeChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(
        if (isSelected) GoldAccent.copy(alpha = 0.28f)
        else Color.White.copy(alpha = 0.10f)
      )
      .border(
        width = if (isSelected) 1.dp else 0.5.dp,
        color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.22f),
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 9.dp, vertical = 5.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 10.5.sp,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
      color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.90f)
    )
  }
}
