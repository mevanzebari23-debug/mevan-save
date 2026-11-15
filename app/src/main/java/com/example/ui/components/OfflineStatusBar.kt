package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceUiState
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reactive Offline Mode Status Bar & Automatic Sync Banner.
 * Displays when device is disconnected, reassuring the user that all data input
 * is 100% saved locally in Room, and indicates automatic synchronization once online.
 */
@Composable
fun OfflineStatusBar(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onTriggerSync: () -> Unit,
  onDismissSuccessBanner: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showDetailDialog by remember { mutableStateOf(false) }
  val isOffline = !uiState.isNetworkOnline
  val showSuccess = uiState.showAutoSyncSuccessBanner

  AnimatedVisibility(
    visible = isOffline || showSuccess,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically(),
    modifier = modifier.fillMaxWidth()
  ) {
    if (isOffline) {
      // 1. OFFLINE MODE ACTIVE BANNER
      Surface(
        color = Color(0xFF1E170A),
        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.7f)),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showDetailDialog = true }
          .testTag("offline_mode_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFD97706).copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline Mode",
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(14.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
              Text(
                text = if (isKurdish) "مۆدی بێ ئینتەرنێت (Offline Mode)" else "Offline Mode Active",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFBBF24)
              )
              Text(
                text = if (isKurdish) {
                  "داتا ب تەواوی دهێتە خەزنکرن • دەمێ ئینتەرنێت زڤڕی خۆکار دێ هێتە هاوسەنگکرن${if (uiState.pendingOfflineMutations > 0) " (${uiState.pendingOfflineMutations} گۆڕانکاریێن ماین)" else ""}"
                } else {
                  "Full local entry enabled • Auto-syncs when online${if (uiState.pendingOfflineMutations > 0) " (${uiState.pendingOfflineMutations} queued)" else ""}"
                },
                fontSize = 10.sp,
                color = Color(0xFFFDE68A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFD97706).copy(alpha = 0.25f),
            border = BorderStroke(0.7.dp, Color(0xFFD97706))
          ) {
            Text(
              text = if (isKurdish) "دۆخ" else "INFO",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFBBF24),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    } else if (showSuccess) {
      // 2. CONNECTIVITY RESTORED & AUTO-SYNC SUCCESS BANNER
      Surface(
        color = Color(0xFF042F1A),
        border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showDetailDialog = true }
          .testTag("auto_sync_success_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(EmeraldLight.copy(alpha = 0.22f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Sync Complete",
                tint = EmeraldLight,
                modifier = Modifier.size(14.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
              Text(
                text = if (isKurdish) "ئینتەرنێت زڤڕی • هاوسەنگکرنا خۆکار سەرکەفت" else "Online • Cloud Auto-Sync Complete",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = EmeraldLight
              )
              Text(
                text = if (isKurdish) "هەمی داتا دگەل کلاودێ هاتنە نویکرن و هاوسەنگکرن" else "All local records synchronized with Cloud Firestore",
                fontSize = 10.sp,
                color = Color(0xFF86EFAC)
              )
            }
          }

          IconButton(
            onClick = onDismissSuccessBanner,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Dismiss",
              tint = Color(0xFF86EFAC),
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }

  // Offline Mode & Sync Details Dialog
  if (showDetailDialog) {
    OfflineSyncDetailDialog(
      uiState = uiState,
      isKurdish = isKurdish,
      onTriggerSync = onTriggerSync,
      onDismiss = { showDetailDialog = false }
    )
  }
}

/**
 * Detailed Information Dialog for Offline Storage & Cloud Synchronization
 */
@Composable
fun OfflineSyncDetailDialog(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onTriggerSync: () -> Unit,
  onDismiss: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US) }
  val lastSyncStr = if (uiState.lastSuccessfulSyncMillis > 0L) {
    dateFormat.format(Date(uiState.lastSuccessfulSyncMillis))
  } else {
    if (isKurdish) "هێشتا نەهاتیە تۆمارکرن" else "Never"
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(18.dp),
    containerColor = DarkGlassCardBg,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = if (uiState.isNetworkOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
          contentDescription = null,
          tint = if (uiState.isNetworkOnline) EmeraldLight else Color(0xFFFBBF24)
        )
        Text(
          text = if (isKurdish) "مۆدی بێ ئینتەرنێت و هاوسەنگکرن" else "Offline Mode & Cloud Sync",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 1. Connection Status Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.isNetworkOnline) Color(0xFF062817) else Color(0xFF2B1D06)
          ),
          border = BorderStroke(
            0.8.dp,
            if (uiState.isNetworkOnline) EmeraldLight.copy(alpha = 0.5f) else Color(0xFFD97706).copy(alpha = 0.5f)
          )
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                  if (uiState.isNetworkOnline) EmeraldLight.copy(alpha = 0.2f) else Color(0xFFD97706).copy(alpha = 0.2f)
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (uiState.isNetworkOnline) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (uiState.isNetworkOnline) EmeraldLight else Color(0xFFFBBF24),
                modifier = Modifier.size(18.dp)
              )
            }
            Column {
              Text(
                text = if (uiState.isNetworkOnline) {
                  if (isKurdish) "سەرهێڵ (Online)" else "Connected (Online)"
                } else {
                  if (isKurdish) "بێ ئینتەرنێت (Offline Mode)" else "Offline Mode"
                },
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = if (uiState.isNetworkOnline) {
                  if (isKurdish) "پەیوەندی ب سێرڤەری ڤە هەیە و ئۆتۆ-سینک کار دکەت" else "Connected to cloud. Auto-sync active."
                } else {
                  if (isKurdish) "داتا د داتابەیسا ناڤخۆیی دا پاراستیە و دەمێ ئینتەرنێت بهێت دێ سینک بیت" else "Data safely stored locally. Will auto-sync on reconnect."
                },
                fontSize = 10.5.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
        }

        // 2. Local Database Engine Info
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0F172A),
          border = BorderStroke(0.8.dp, Color(0xFF334155))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Storage, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
              Text(
                text = if (isKurdish) "داتابەیسا ناڤخۆیی (Room SQLite Engine)" else "Local Database (Room SQLite Engine)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isKurdish) "هەمی داهات، مەسروف، قەرز، عەقارات و سەیارە ب شێوەیەکێ نەپچڕاو د مۆبایلێ دا د پاراستینە چ ئینتەرنێت هەبیت یان نە."
                     else "All transactions, debts, real estate, and cars are permanently preserved on device with 100% offline availability.",
              fontSize = 10.sp,
              color = Color(0xFFCBD5E1)
            )
          }
        }

        // 3. Sync Details Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = if (isKurdish) "گۆڕانکاریێن چاڤەڕێ (Offline Queue)" else "Queued Changes",
              fontSize = 10.5.sp,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = "${uiState.pendingOfflineMutations} ${if (isKurdish) "بڕگە" else "items"}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (uiState.pendingOfflineMutations > 0) Color(0xFFFBBF24) else EmeraldLight
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = if (isKurdish) "دوایین هاوسەنگکرن" else "Last Synced",
              fontSize = 10.5.sp,
              color = Color(0xFF94A3B8)
            )
            Text(
              text = lastSyncStr,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onTriggerSync()
          onDismiss()
        },
        enabled = uiState.isNetworkOnline && !uiState.isSyncing,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
        shape = RoundedCornerShape(10.dp)
      ) {
        if (uiState.isSyncing) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF020617), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(6.dp))
        } else {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF020617), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
          text = if (isKurdish) "هاوسەنگکرن نوکە (Sync Now)" else "Sync Now",
          color = Color(0xFF020617),
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp)
      ) {
        Text(if (isKurdish) "داخستن" else "Close", color = Color(0xFF94A3B8))
      }
    }
  )
}
