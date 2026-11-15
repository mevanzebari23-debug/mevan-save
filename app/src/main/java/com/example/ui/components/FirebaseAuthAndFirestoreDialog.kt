package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseAuthUserState
import com.example.data.firebase.FirebaseDataManager
import com.example.data.firebase.FirestoreSyncState
import com.example.ui.FinanceUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FirebaseAuthAndFirestoreDialog(
  uiState: FinanceUiState,
  authState: FirebaseAuthUserState,
  firestoreState: FirestoreSyncState,
  isKurdish: Boolean,
  onGoogleSignIn: () -> Unit,
  onEmailSignIn: (email: String, password: String, displayName: String) -> Unit,
  onSignOut: () -> Unit,
  onSyncToFirestore: () -> Unit,
  onRestoreFromFirestore: () -> Unit,
  onUpdateWebClientId: (String) -> Unit,
  currentWebClientId: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var showEmailAuthForm by remember { mutableStateOf(false) }
  var showWebClientConfig by remember { mutableStateOf(false) }
  var emailInput by remember { mutableStateOf(authState.email.ifBlank { FirebaseDataManager.DEFAULT_USER_EMAIL }) }
  var nameInput by remember { mutableStateOf(authState.displayName.ifBlank { "Mevan Zebari" }) }
  var passwordInput by remember { mutableStateOf("CapitalVault2026!") }
  var webClientIdInput by remember { mutableStateOf(currentWebClientId) }

  val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
  val totalLocalRecords = uiState.transactions.size +
    uiState.investments.size +
    uiState.goldHoldings.size +
    uiState.debtsCredits.size +
    uiState.budgets.size +
    uiState.carServices.size +
    uiState.carPayments.size

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(GoldAccent.copy(alpha = 0.18f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(20.dp)
          )
        }
        Column {
          Text(
            text = if (isKurdish) "Firebase Auth & Firestore" else "Firebase Auth & Cloud Firestore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isKurdish) "چوونا ژوور ب Google و پاشەکەوتا Firestore" else "Google Sign-In & Real-Time Firestore Database",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // 1. User Identity & Authentication Status Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (authState.isSignedIn) {
              EmeraldLight.copy(alpha = 0.12f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(
                    if (authState.isSignedIn) EmeraldLight else GoldAccent.copy(alpha = 0.25f),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (authState.isSignedIn) Icons.Default.VerifiedUser else Icons.Default.AccountCircle,
                  contentDescription = null,
                  tint = if (authState.isSignedIn) Color(0xFF0F172A) else GoldAccent,
                  modifier = Modifier.size(24.dp)
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (authState.isSignedIn) {
                    authState.displayName.ifBlank { "Authenticated User" }
                  } else {
                    if (isKurdish) "چوونا ژوور ب هەژمارا Google" else "Sign in to Identify Your Account"
                  },
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (authState.isSignedIn) {
                    authState.email
                  } else {
                    if (isKurdish) "بۆ گرێدانا داتایێن خۆ ب Cloud Firestore" else "Link your financial portfolio to Firebase Auth & Firestore"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (authState.isSignedIn && authState.uid.isNotBlank()) {
                  Text(
                    text = "UID: ${authState.uid}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = EmeraldLight
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!authState.isSignedIn) {
              // Google Sign-In Primary Button
              Button(
                onClick = onGoogleSignIn,
                enabled = !authState.isAuthLoading,
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldLight,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("dialog_google_sign_in_button")
              ) {
                if (authState.isAuthLoading) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF0F172A)
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isKurdish) "چوونا ژوور ب Google (Firebase Auth)" else "Sign in with Google (Firebase Auth)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Secondary option: Firebase Email Auth (useful on emulators without Play Store account)
              OutlinedButton(
                onClick = { showEmailAuthForm = !showEmailAuthForm },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("toggle_firebase_email_auth_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Email,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isKurdish) {
                    "چوونا ژوور ب ئیمەیڵی ($emailInput)"
                  } else {
                    "Sign in with Firebase Email ($emailInput)"
                  },
                  fontSize = 11.sp
                )
              }
            } else {
              // Signed-in actions: Switch Google account or Sign Out
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = onGoogleSignIn,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (isKurdish) "گوهۆڕینا هەژمارێ" else "Switch Google", fontSize = 11.sp)
                }

                OutlinedButton(
                  onClick = onSignOut,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("dialog_firebase_sign_out_button")
                ) {
                  Icon(Icons.Default.Logout, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (isKurdish) "دەرکەفتن" else "Sign Out", color = LossRed, fontSize = 11.sp)
                }
              }
            }

            if (showEmailAuthForm && !authState.isSignedIn) {
              Spacer(modifier = Modifier.height(10.dp))
              Divider(color = MaterialTheme.colorScheme.outlineVariant)
              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(if (isKurdish) "ناڤێ تە" else "Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text(if (isKurdish) "ئیمەیڵێ Google / Firebase" else "Email Address") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("firebase_email_input")
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text(if (isKurdish) "وشەیا نهێنی" else "Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("firebase_password_input")
              )
              Spacer(modifier = Modifier.height(10.dp))
              Button(
                onClick = {
                  onEmailSignIn(emailInput, passwordInput, nameInput)
                  showEmailAuthForm = false
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldAccent,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("confirm_firebase_email_sign_in_button")
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isKurdish) "پشتڕاستکرن و سینککرنا Firestore" else "Authenticate & Sync Firestore",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // 2. Cloud Firestore Database Persistence Status & Actions
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = EmeraldLight,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isKurdish) " بنگەهێ داتایێ Cloud Firestore" else "Cloud Firestore Persistence",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(EmeraldLight.copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = if (firestoreState.isSyncing) "SYNCING" else "ACTIVE",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldLight
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = firestoreState.statusMessage,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Collection path & counts
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isKurdish) "ڕێڕەوا Firestore:" else "Firestore Path:",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = firestoreState.collectionPath,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isKurdish) "تۆمارێن هەلگرتی:" else "Persisted Documents:",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$totalLocalRecords docs (Tx: ${uiState.transactions.size}, Cars/Inv: ${uiState.investments.size}, Gold: ${uiState.goldHoldings.size}, Debts: ${uiState.debtsCredits.size})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              if (firestoreState.lastSyncMillis > 0L) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = if (isKurdish) "دوماهیک سینک:" else "Last Firestore Sync:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = dateFormat.format(Date(firestoreState.lastSyncMillis)),
                    fontSize = 11.sp,
                    color = EmeraldLight
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onSyncToFirestore,
                enabled = !firestoreState.isSyncing,
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldLight,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("dialog_firestore_sync_button")
              ) {
                if (firestoreState.isSyncing) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF0F172A)
                  )
                } else {
                  Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isKurdish) "ناردن بۆ Firestore" else "Save to Firestore",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedButton(
                onClick = onRestoreFromFirestore,
                enabled = !firestoreState.isSyncing,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("dialog_firestore_restore_button")
              ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isKurdish) "زڤڕاندن ژ Firestore" else "Restore Firestore",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // 3. Optional OAuth Web Client ID Configuration for Google Sign-In
        OutlinedButton(
          onClick = { showWebClientConfig = !showWebClientConfig },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isKurdish) "ڕێکخستنا Google OAuth Web Client ID" else "Configure Google OAuth Web Client ID",
            fontSize = 11.sp
          )
        }

        if (showWebClientConfig) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = if (isKurdish) {
                "ئەگەر فایلێ google-services.json هێشتا نەهاتیە زێدەکرن، دشێی Web Client ID یێ پرۆژێ خۆ یێ Firebase بنڤیسی:"
              } else {
                "If google-services.json is not yet placed in app/, you can set your Firebase Web Client ID for CredentialManager:"
              },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
              value = webClientIdInput,
              onValueChange = { webClientIdInput = it },
              label = { Text("Server Web Client ID") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Button(
              onClick = {
                onUpdateWebClientId(webClientIdInput)
                showWebClientConfig = false
                Toast.makeText(context, "Web Client ID saved", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(if (isKurdish) "پاشەکەوتکرنا Client ID" else "Save Web Client ID", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
        modifier = Modifier.testTag("close_firebase_dialog_button")
      ) {
        Text(if (isKurdish) "داخستن" else "Done", fontWeight = FontWeight.Bold)
      }
    }
  )
}
