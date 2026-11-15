package com.example.ui.screens.budgets

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyService
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import com.example.ui.BudgetAlertItem
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.BudgetDetailActionDialog
import com.example.ui.components.CyberRecordButton
import com.example.ui.components.EntryExchangeRateCard
import com.example.ui.components.PrintPreviewDialog
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.WoodCardSurface
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodInlayGold
import com.example.ui.theme.DarkGlassCardBg
import com.example.ui.theme.DarkGlassCardSurface
import androidx.compose.ui.graphics.Brush
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetsAndToolsScreen(
  viewModel: FinanceViewModel,
  uiState: FinanceUiState,
  onLockApp: () -> Unit,
  onTestBiometric: () -> Unit = {},
  onOpenFirebaseAuthDialog: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lang = uiState.settings.language
  val isKurdish = AppStrings.isKurdish(lang)

  var showAddBudgetDialog by remember { mutableStateOf(false) }
  var showChangePinDialog by remember { mutableStateOf(false) }
  var showBackupDetailsDialog by remember { mutableStateOf<String?>(null) }
  var showResetCashConfirmDialog by remember { mutableStateOf(false) }
  var previewPdfResult by remember { mutableStateOf<GeneratedPdfResult?>(null) }
  var selectedBudgetAlertForDetail by remember { mutableStateOf<BudgetAlertItem?>(null) }

  val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 0. LANGUAGE SELECTION (زمانێ بەرنامەی)
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkGlassCardSurface, RoundedCornerShape(18.dp))
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                EmeraldLight.copy(alpha = 0.50f),
                GoldAccent.copy(alpha = 0.40f),
                Color(0x33FFFFFF)
              )
            ),
            shape = RoundedCornerShape(18.dp)
          )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(EmeraldLight.copy(alpha = 0.20f), CircleShape)
                  .border(1.dp, EmeraldLight.copy(alpha = 0.40f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Language,
                  contentDescription = null,
                  tint = EmeraldLight,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = if (isKurdish) "زمانێ بەرنامەی" else "App Language",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (isKurdish) "کوردی (بادینی) چالاکە" else "Choose English or Badini Kurdish",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val isKu = uiState.settings.language == "KU"
            Button(
              onClick = { viewModel.setLanguage("KU") },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isKu) EmeraldLight else Color(0xFF1E293B).copy(alpha = 0.45f)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(
                  width = 1.dp,
                  color = if (isKu) EmeraldLight else Color(0x33FFFFFF),
                  shape = RoundedCornerShape(12.dp)
                )
                .testTag("lang_kurdish_button")
            ) {
              Text(
                text = "☀️ کوردی (بادینی)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isKu) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
              )
            }

            Button(
              onClick = { viewModel.setLanguage("EN") },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (!isKu) EmeraldLight else Color(0xFF1E293B).copy(alpha = 0.45f)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(
                  width = 1.dp,
                  color = if (!isKu) EmeraldLight else Color(0x33FFFFFF),
                  shape = RoundedCornerShape(12.dp)
                )
                .testTag("lang_english_button")
            ) {
              Text(
                text = "🌐 English",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (!isKu) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // 1. RECURRING MONTHLY BUDGET ALERTS
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isKurdish) "بودجە و هۆشداریێن خەرجیێ" else "Monthly Budget Alerts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isKurdish) "دیارکرنا سنورێ مەسروفی بۆ هەر بەشەکی" else "Recurring monthly spending limits with early warnings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = { showAddBudgetDialog = true },
          modifier = Modifier.testTag("add_budget_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Budget Target",
            tint = EmeraldLight
          )
        }
      }
    }

    if (uiState.budgetAlerts.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkGlassCardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
        ) {
          Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text(
              text = if (isKurdish) "چو چاڤدێریێن بودجەی نەهاتینە دانان. ل سەر '+' بدە بۆ دیارکرنا سنورێ هەیڤانە." else "No recurring budgets defined. Tap '+' to set a monthly spending cap.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(uiState.budgetAlerts, key = { it.budget.id }) { alertItem ->
        val budget = alertItem.budget
        val accent = when {
          alertItem.isExceeded -> LossRed
          alertItem.isWarning -> GoldAccent
          else -> EmeraldLight
        }
        val progress = (alertItem.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f)
        val statusMsg = when {
          alertItem.isExceeded -> if (isKurdish) "سنوور تێپەڕی (${alertItem.percentageUsed.toInt()}%)" else "Exceeded (${alertItem.percentageUsed.toInt()}%)"
          alertItem.isWarning -> if (isKurdish) "نێزیکی سنووری (${alertItem.percentageUsed.toInt()}%)" else "Warning (${alertItem.percentageUsed.toInt()}%)"
          else -> if (isKurdish) "ئاسایی (${alertItem.percentageUsed.toInt()}%)" else "On Track (${alertItem.percentageUsed.toInt()}%)"
        }
        CyberRecordButton(
          title = budget.category,
          subtitle = if (alertItem.isExceeded) {
            if (isKurdish) "ئاگەهداری: سنوورێ هەیڤانە تێپەڕی!" else "Monthly budget cap exceeded!"
          } else if (alertItem.isWarning) {
            if (isKurdish) "ئاگەهداری: نێزیکی ${budget.alertThresholdPercent}% بوو" else "Approaching ${budget.alertThresholdPercent}% limit"
          } else {
            if (isKurdish) "سنوور: ${CurrencyService.format(alertItem.limitAmount, uiState.displayCurrency)}" else "Limit: ${CurrencyService.format(alertItem.limitAmount, uiState.displayCurrency)}"
          },
          primaryValue = CurrencyService.format(alertItem.spentAmount, uiState.displayCurrency),
          secondaryValue = "/ ${CurrencyService.format(alertItem.limitAmount, uiState.displayCurrency)}",
          accentColor = accent,
          icon = if (alertItem.isExceeded) Icons.Default.Warning else if (alertItem.isWarning) Icons.Default.NotificationsActive else Icons.Default.Tune,
          statusBadge = statusMsg,
          statusBadgeColor = accent,
          progressRatio = progress,
          actionHintLabel = if (isKurdish) "دەستکاری" else "Edit",
          quickActionLabel = if (isKurdish) "ژێبرن" else "Delete",
          quickActionIcon = Icons.Default.DeleteOutline,
          quickActionColor = LossRed,
          onQuickActionClick = { viewModel.deleteBudget(budget.id) },
          onClick = { selectedBudgetAlertForDetail = alertItem },
          testTagKey = "budget_alert_btn_${budget.id}"
        )
      }
    }

    // 2. BIOMETRIC AUTHENTICATION & SECURITY
    item {
      Text(
        text = if (isKurdish) "ئاسایشا پەنجەمۆرێ و قفلکرنا بەرنامەی" else "Biometric Security & Vault Lock",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkGlassCardSurface, RoundedCornerShape(18.dp))
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                EmeraldLight.copy(alpha = 0.50f),
                Color(0xFF38BDF8).copy(alpha = 0.40f),
                Color(0x33FFFFFF)
              )
            ),
            shape = RoundedCornerShape(18.dp)
          )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(EmeraldLight.copy(alpha = 0.20f), CircleShape)
                  .border(1.dp, EmeraldLight.copy(alpha = 0.40f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = EmeraldLight,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = if (isKurdish) "ئیمانکرنا پەنجەمۆرێ" else "Biometric Authentication",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (uiState.settings.biometricEnabled) {
                    if (isKurdish) "چالاکە (پەنجەمۆر / Face ID)" else "Enabled (Fingerprint / Face ID)"
                  } else {
                    if (isKurdish) "نەچالاکە" else "Disabled"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Switch(
              checked = uiState.settings.biometricEnabled,
              onCheckedChange = { enabled ->
                viewModel.toggleBiometric(enabled)
                if (enabled) {
                  onTestBiometric()
                }
                Toast.makeText(
                  context,
                  if (enabled) {
                    if (isKurdish) "ئاسایشا پەنجەمۆرێ هاتە چالاککرن" else "Biometric security activated"
                  } else {
                    if (isKurdish) "ئاسایشا پەنجەمۆرێ هاتە کوژاندن" else "Biometric security turned off"
                  },
                  Toast.LENGTH_SHORT
                ).show()
              },
              colors = SwitchDefaults.colors(checkedThumbColor = EmeraldLight),
              modifier = Modifier.testTag("toggle_biometric_switch")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Sensitive Financial Data Masking Toggle Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F172A).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
              .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (isKurdish) "ڤەشارتنا ژمارەیێن دارایی یێن هەستیار" else "Mask Sensitive Financial Views",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (isKurdish) "داخوازکرنا BiometricPrompt بەری نیشاندانا سەرمایە و چارتان" else "Require BiometricPrompt authentication to reveal balances",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Switch(
              checked = uiState.isSensitiveDataMasked,
              onCheckedChange = { masked ->
                viewModel.setSensitiveDataMasked(masked)
              },
              colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent),
              modifier = Modifier.testTag("toggle_sensitive_mask_switch")
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { showChangePinDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "گوهۆڕینا PIN" else "Change PIN", fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = onTestBiometric,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("test_biometric_prompt_button")
            ) {
              Icon(Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "تاقیکرنا پەنجەمۆر" else "Test Biometric", fontSize = 11.sp)
            }

            Button(
              onClick = onLockApp,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("lock_app_now_button")
            ) {
              Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "نوکە قفل بکە" else "Lock Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    // 3. FIREBASE AUTH (GOOGLE SIGN-IN) & CLOUD FIRESTORE PERSISTENCE
    item {
      Text(
        text = if (isKurdish) "Firebase Auth (Google) و بنگەهێ داتایێ Firestore" else "Firebase Auth (Google Sign-In) & Cloud Firestore",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      val authUser = uiState.firebaseAuthUser
      val fsState = uiState.firestoreSyncState

      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkGlassCardSurface, RoundedCornerShape(18.dp))
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                Color(0xFF0284C7).copy(alpha = 0.55f),
                GoldAccent.copy(alpha = 0.40f),
                Color(0x33FFFFFF)
              )
            ),
            shape = RoundedCornerShape(18.dp)
          )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Firebase Auth User Identity Banner
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                if (authUser.isSignedIn) EmeraldLight.copy(alpha = 0.16f) else GoldAccent.copy(alpha = 0.16f),
                RoundedCornerShape(12.dp)
              )
              .border(
                width = 1.dp,
                color = (if (authUser.isSignedIn) EmeraldLight else GoldAccent).copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
              )
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(
                    if (authUser.isSignedIn) EmeraldLight else GoldAccent.copy(alpha = 0.25f),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (authUser.isSignedIn) Icons.Default.VerifiedUser else Icons.Default.AccountCircle,
                  contentDescription = null,
                  tint = if (authUser.isSignedIn) Color(0xFF0F172A) else GoldAccent,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (authUser.isSignedIn) {
                    authUser.displayName.ifBlank { authUser.email }
                  } else {
                    if (isKurdish) "چوونا ژوور ب هەژمارا Google" else "Google Sign-In (Firebase Auth)"
                  },
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (authUser.isSignedIn) {
                    "${authUser.email} • Firestore (${fsState.collectionPath})"
                  } else {
                    if (isKurdish) "هەژمارا خۆ گرێدە ب Cloud Firestore" else "Sign in with Google to persist data in Firestore"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (authUser.isSignedIn) {
              IconButton(
                onClick = {
                  viewModel.signOutFirebase(context) {
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.testTag("tools_firebase_sign_out_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Logout,
                  contentDescription = "Sign Out",
                  tint = LossRed
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Google Sign-In & Firebase Auth Account Manager Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.signInWithGoogle(context) { success, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                  if (!success) {
                    onOpenFirebaseAuthDialog()
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldLight,
                contentColor = Color(0xFF0F172A)
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("tools_google_sign_in_button")
            ) {
              Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (authUser.isSignedIn) {
                  if (isKurdish) "گوهۆڕینا Google" else "Switch Google"
                } else {
                  if (isKurdish) "چوونا ژوور ب Google" else "Google Sign-In"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = onOpenFirebaseAuthDialog,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("tools_open_firebase_dialog_button")
            ) {
              Icon(Icons.Default.Storage, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "کۆنسۆلا Firestore" else "Firestore Console",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Firestore Sync & Restore Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.syncToFirestore { _, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("tools_firestore_sync_button")
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "ناردن بۆ Firestore" else "Save to Firestore",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = {
                viewModel.restoreFromFirestore { _, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("tools_firestore_restore_button")
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isKurdish) "زڤڕاندن ژ Firestore" else "Restore Firestore",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Divider(color = MaterialTheme.colorScheme.surfaceVariant)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(Color(0xFF0284C7).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = Color(0xFF0284C7),
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = if (isKurdish) "پاشەکەوتکرنا پاراستی یا سەر هەورێ" else "Encrypted Cloud Storage",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = uiState.syncMessage,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Offline mode callout banner
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F172A).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
              .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.FlightTakeoff,
              contentDescription = null,
              tint = EmeraldLight,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isKurdish) "مۆدێ ئۆفلاین: ١٠٠٪ کارایە. بەردەستە بێ ئینتەرنێت و د سەفەرێ دا." else "Offline Mode: 100% Active. Full access during flights and travel with zero connection.",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Primary Backup Actions: Device & Email
          Text(
            text = "Data Safety & Backups (Target: Mevanzebari23@gmail.com)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.backupToDevice(context) { success, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("backup_to_device_button")
            ) {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save to Device", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                viewModel.backupToEmail(context) { success, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0F172A)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("backup_to_email_button")
            ) {
              Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Email Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                viewModel.restoreFromAutoBackup(context) { success, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("restore_auto_backup_button")
            ) {
              Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Restore Backup", fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = {
                viewModel.performCloudSync()
                Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("sync_cloud_button")
            ) {
              if (uiState.isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sync Devices", fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = {
                viewModel.createCloudBackupSnapshot { json ->
                  showBackupDetailsDialog = json
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Snapshot", fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = { showResetCashConfirmDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reset_cash_tool_button")
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = LossRed)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKurdish) "سفرکرنا حسێبا کاشی بۆ ٠ $ (ژێبرنا ١,٣٠٠$ کۆن)" else "Reset Cash Account to 0$ (Clear 1,300$ old balance)",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = LossRed
            )
          }
        }
      }
    }

    // 4. EXPORT REPORTS (CSV & FINANCIAL STATEMENT)
    item {
      Text(
        text = if (isKurdish) "دەرخستنا ڕاپۆرتێن دارایی" else "Export Reports & Statements",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkGlassCardSurface, RoundedCornerShape(18.dp))
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                GoldAccent.copy(alpha = 0.55f),
                EmeraldLight.copy(alpha = 0.40f),
                Color(0x33FFFFFF)
              )
            ),
            shape = RoundedCornerShape(18.dp)
          )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Master Executive Financial Ledger PDF Statement
          Button(
            onClick = {
              previewPdfResult = PdfExportService.generateMasterLedgerPdf(context, uiState, isKurdish)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldAccent,
              contentColor = Color(0xFF0F172A)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("export_master_ledger_pdf_btn")
          ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "Master Ledger PDF", tint = Color(0xFF0F172A))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isKurdish) "ڕاپۆرتا سەرەکی یا داراییا گشتی (Master PDF Ledger)" else "Executive Financial Ledger PDF Report",
              color = Color(0xFF0F172A),
              fontWeight = FontWeight.Black
            )
          }

          Divider(color = Color(0x33FFFFFF))

          // Printable White-paper PDF Statements for Cashflow, Debts, and Assets
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                previewPdfResult = PdfExportService.generateTransactionsPdf(
                  context = context,
                  transactions = uiState.transactions,
                  displayCurrency = uiState.displayCurrency,
                  currentIqdRate = uiState.iqdRate,
                  isKurdish = isKurdish
                )
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("export_cashflow_pdf_btn")
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF38BDF8))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "تەحویلات PDF" else "Cashflow PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = {
                previewPdfResult = PdfExportService.generateDebtsPdf(
                  context = context,
                  debts = uiState.debtsCredits,
                  displayCurrency = uiState.displayCurrency,
                  currentIqdRate = uiState.iqdRate,
                  isKurdish = isKurdish,
                  isMulukOnly = false
                )
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("export_debts_pdf_btn")
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFF97316))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "قەرز PDF" else "Debts PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = {
                previewPdfResult = PdfExportService.generateAssetsPdf(
                  context = context,
                  assets = uiState.investments,
                  displayCurrency = uiState.displayCurrency,
                  currentIqdRate = uiState.iqdRate,
                  isKurdish = isKurdish
                )
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("export_assets_pdf_btn")
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF10B981))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isKurdish) "سەرمایە PDF" else "Assets PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 5. THEME & DISPLAY PREFERENCES
    item {
      Text(
        text = if (isKurdish) "شێوازێ ڕەنگان" else "Theme & Preferences",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGlassCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkGlassCardSurface, RoundedCornerShape(18.dp))
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
              listOf(
                Color(0xFFA855F7).copy(alpha = 0.50f),
                GoldAccent.copy(alpha = 0.35f),
                Color(0x33FFFFFF)
              )
            ),
            shape = RoundedCornerShape(18.dp)
          )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DarkMode, contentDescription = null, tint = GoldAccent)
              Spacer(modifier = Modifier.width(10.dp))
              Text(if (isKurdish) "مۆدێ شاشێ" else "Theme Mode", fontWeight = FontWeight.SemiBold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("DARK", "LIGHT").forEach { mode ->
                val isSel = uiState.settings.themeMode == mode
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) EmeraldLight else Color(0xFF1E293B).copy(alpha = 0.45f))
                    .border(1.dp, if (isSel) EmeraldLight else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .clickable { viewModel.setThemeMode(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = if (isKurdish) {
                      if (mode == "DARK") "دارێ گویزێ (تۆخ)" else "دارێ بەڕوویێ (ڕووناک)"
                    } else {
                      if (mode == "DARK") "Dark Walnut" else "Light Oak"
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSel) Color(0xFF140D08) else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Add Monthly Budget Dialog
  if (showAddBudgetDialog) {
    var categoryInput by remember { mutableStateOf(if (isKurdish) "قوتابخانە" else "School & Education") }
    var limitInput by remember { mutableStateOf("") }
    var budgetCurrency by remember { mutableStateOf(uiState.displayCurrency) }
    var thresholdInput by remember { mutableStateOf("80") }
    var entryIqdRateInput by remember(uiState.iqdRate) { mutableStateOf(uiState.iqdRate.toInt().toString()) }

    val budgetCategoryPresets = remember(isKurdish, uiState.customExpenseCategories) {
      val base = listOf(if (isKurdish) "قوتابخانە" else "School & Education")
      val tail = listOf(if (isKurdish) "مەسروفێ هەیڤانە" else "Monthly Spending")
      (base + uiState.customExpenseCategories + tail).distinct()
    }

    AlertDialog(
      onDismissRequest = { showAddBudgetDialog = false },
      containerColor = WoodCardSurface.copy(alpha = 0.95f),
      title = { Text(if (isKurdish) "دیارکرنا ئاگەهدارییا بودجێ هەیڤانە" else "Set Monthly Budget Alert") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            budgetCategoryPresets.forEach { preset ->
              val isSel = categoryInput == preset
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSel) EmeraldLight.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant)
                  .border(1.dp, if (isSel) EmeraldLight else Color.Transparent, RoundedCornerShape(10.dp))
                  .clickable { categoryInput = preset }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = preset,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                  color = if (isSel) EmeraldLight else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          OutlinedTextField(
            value = categoryInput,
            onValueChange = { categoryInput = it },
            label = { Text(if (isKurdish) "بەش / ناڤێ بودجەی" else "Category / Budget Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = limitInput,
            onValueChange = { limitInput = it },
            label = { Text("${if (isKurdish) "سنوورێ مەسروفی یێ هەیڤانە" else "Monthly Spending Limit"} ($budgetCurrency)") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("budget_limit_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("USD", "IQD").forEach { cur ->
              Button(
                onClick = { budgetCurrency = cur },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (budgetCurrency == cur) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = cur,
                  color = if (budgetCurrency == cur) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          EntryExchangeRateCard(
            rateInput = entryIqdRateInput,
            onRateChange = { entryIqdRateInput = it },
            currentGlobalIqdRate = uiState.iqdRate,
            enteredAmount = limitInput.toDoubleOrNull() ?: 0.0,
            selectedCurrency = budgetCurrency,
            isKurdish = isKurdish,
            testTag = "budget_entry_iqd_rate_input"
          )

          OutlinedTextField(
            value = thresholdInput,
            onValueChange = { thresholdInput = it },
            label = { Text(if (isKurdish) "ڕێژا ئاگەهدارکرنێ ب چەند٪ بیت" else "Alert Warning Threshold (%)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val limit = limitInput.toDoubleOrNull() ?: 0.0
            val thresh = thresholdInput.toIntOrNull() ?: 80
            val lockedRate = CurrencyService.normalizeIqdRatePerUsd(
              entryIqdRateInput.toDoubleOrNull() ?: uiState.iqdRate,
              uiState.iqdRate
            )
            if (limit > 0) {
              viewModel.updateIqdRate(lockedRate)
              viewModel.addBudget(
                category = categoryInput,
                monthlyLimit = limit,
                currency = budgetCurrency,
                alertThreshold = thresh
              )
              showAddBudgetDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
          modifier = Modifier.testTag("confirm_add_budget_button")
        ) {
          Text(if (isKurdish) "تۆمارکرنا بودجەی" else "Save Budget")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddBudgetDialog = false }) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
      }
    )
  }

  // Change PIN Dialog
  if (showChangePinDialog) {
    var newPinInput by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showChangePinDialog = false },
      containerColor = WoodCardSurface.copy(alpha = 0.95f),
      title = { Text(if (isKurdish) "گوهۆڕینا ژمارا نهێنی (PIN)" else "Change Security PIN") },
      text = {
        Column {
          Text(if (isKurdish) "کۆدەکێ ٤ ژمارەیی یێ نوی بنڤیسە بۆ پاراستنا بەرنامەی." else "Enter new 4-digit backup PIN code for biometric vault.")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = newPinInput,
            onValueChange = { if (it.length <= 6) newPinInput = it },
            label = { Text(if (isKurdish) "ژمارا نهێنی یا نوی" else "New PIN") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPinInput.length >= 4) {
              viewModel.setPinCode(newPinInput)
              showChangePinDialog = false
              Toast.makeText(context, if (isKurdish) "ژمارا نهێنی هاتە نویژەنکرن" else "PIN updated successfully", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight)
        ) {
          Text(if (isKurdish) "نویکرنا ژمارا نهێنی" else "Update PIN")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showChangePinDialog = false }) { Text(if (isKurdish) "پاشگەزبوون" else "Cancel") }
      }
    )
  }

  // Backup JSON Dialog
  if (showBackupDetailsDialog != null) {
    AlertDialog(
      onDismissRequest = { showBackupDetailsDialog = null },
      containerColor = WoodCardSurface.copy(alpha = 0.95f),
      title = { Text(if (isKurdish) "پاشەکەوتا ئەورەکی (Cloud Backup)" else "Cloud Backup Snapshot") },
      text = {
        Column {
          Text(if (isKurdish) "داتایێن پاراستی ب شێوازێ پاراستنێ (SHA-256) هاتینە ئامادەکرن بۆ پاشەکەوتکرنێ." else "Encrypted snapshot verified with SHA-256 checksum and synchronized to device cloud storage.")
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = showBackupDetailsDialog!!.take(400) + "...\n[Full Encrypted Payload]",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.reportExportService.sharePlainTextReport(showBackupDetailsDialog!!, "Capital & Debt Cloud Backup")
            showBackupDetailsDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isKurdish) "هنارتنا فایلی" else "Export Snapshot")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showBackupDetailsDialog = null }) { Text(if (isKurdish) "داخستن" else "Close") }
      }
    )
  }

  // Reset Cash Confirmation Dialog
  if (showResetCashConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetCashConfirmDialog = false },
      containerColor = WoodCardSurface.copy(alpha = 0.95f),
      icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = LossRed) },
      title = { Text(if (isKurdish) "سفرکرنا حسێبا کاشی" else "Reset Cash Account to 0$") },
      text = {
        Text(
          text = if (isKurdish)
            "ئەرێ تۆ پشتڕاستی دتەوێت حسێبا کاشی و پارێ دەستپێکی سفر بکەی؟ ئەو باڵانسا ١,٣٠٠ دۆلار و مامەڵێن کەڤن دێ هێنە ژێبرن، بەڵام عەقارات و سەیارە و زێڕ دەستکاری نابن."
          else
            "Are you sure you want to reset the cash account and starting capital to 0$? The old $1,300 balance will be wiped, but your real estate, cars, and gold will remain completely untouched."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetCashAccountToZero()
            showResetCashConfirmDialog = false
            Toast.makeText(
              context,
              if (isKurdish) "حسێبا کاشی ب سەرکەفتوویی بۆ ٠ هاتە سفرکرن" else "Cash account successfully reset to 0$",
              Toast.LENGTH_SHORT
            ).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = LossRed)
        ) {
          Text(if (isKurdish) "بەڵێ، سفر بکە" else "Yes, Reset to 0$")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showResetCashConfirmDialog = false }) {
          Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
        }
      }
    )
  }

  val activeBudgetDetail = selectedBudgetAlertForDetail
  if (activeBudgetDetail != null) {
    BudgetDetailActionDialog(
      budget = activeBudgetDetail.budget,
      spentDisplay = activeBudgetDetail.spentAmount,
      limitDisplay = activeBudgetDetail.limitAmount,
      uiState = uiState,
      viewModel = viewModel,
      isKurdish = isKurdish,
      onDismiss = { selectedBudgetAlertForDetail = null }
    )
  }

  val currentPreview = previewPdfResult
  if (currentPreview != null) {
    PrintPreviewDialog(
      result = currentPreview,
      onDismiss = { previewPdfResult = null }
    )
  }
}
