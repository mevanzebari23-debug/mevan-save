package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent

@Composable
fun LockOverlay(
  onUnlockBiometric: () -> Unit,
  onUnlockWithPin: (String) -> Boolean,
  savedPin: String = "1234",
  isKurdish: Boolean = true,
  biometricStatusLabel: String = "",
  autoTriggerBiometric: Boolean = true,
  modifier: Modifier = Modifier
) {
  var pinInput by remember { mutableStateOf("") }
  var showPinDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(autoTriggerBiometric) {
    if (autoTriggerBiometric) {
      onUnlockBiometric()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            EmeraldContainerDark,
            MaterialTheme.colorScheme.background
          )
        )
      )
      .padding(24.dp)
      .testTag("biometric_lock_overlay"),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, EmeraldLight.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Box(
          modifier = Modifier
            .size(88.dp)
            .background(EmeraldLight.copy(alpha = 0.15f), CircleShape)
            .border(2.dp, EmeraldLight.copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Biometric Vault Lock",
            tint = EmeraldLight,
            modifier = Modifier.size(48.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = if (isKurdish) "پاراستنا BiometricPrompt چالاکە" else "BiometricPrompt API Guard",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAccent
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = if (isKurdish) "قاصەیا سەرمایە و قەرزان" else "Capital & Debt Vault",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (isKurdish) {
            "داتایێن تە یێن دارایی، سەرمایە، سەیارە و قەرز ب ڕێکا پەنجەمۆرێ (BiometricPrompt) هاتینە قفلکرن."
          } else {
            "Application entry point and sensitive financial views are secured by Android BiometricPrompt encryption."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        if (biometricStatusLabel.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = biometricStatusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldLight,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (showPinDialog) {
          OutlinedTextField(
            value = pinInput,
            onValueChange = {
              if (it.length <= 8) {
                pinInput = it
                errorMessage = null
              }
            },
            label = { Text(if (isKurdish) "کۆدێ نهێنی (PIN) بنڤیسە" else "Enter Security PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pin_input_field")
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            OutlinedButton(
              onClick = { showPinDialog = false; pinInput = "" },
              modifier = Modifier.weight(1f)
            ) {
              Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
            }
            Button(
              onClick = {
                val success = onUnlockWithPin(pinInput)
                if (!success) {
                  errorMessage = if (isKurdish) {
                    "کۆدێ PIN خەلەتە. کۆدێ بنەڕەت: $savedPin"
                  } else {
                    "Incorrect PIN code. Default is $savedPin"
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
              modifier = Modifier
                .weight(1f)
                .testTag("submit_pin_button")
            ) {
              Text(if (isKurdish) "ڤەکرن" else "Unlock", fontWeight = FontWeight.Bold)
            }
          }
        } else {
          Button(
            onClick = onUnlockBiometric,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("biometric_unlock_button")
          ) {
            Icon(
              imageVector = Icons.Default.Fingerprint,
              contentDescription = null,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isKurdish) "ڤەکرن ب پەنجەمۆرێ (Biometric)" else "Authenticate with BiometricPrompt",
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = { showPinDialog = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("use_pin_button")
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isKurdish) "بکارئینانا کۆدێ PIN ($savedPin)" else "Use Backup PIN (Default: $savedPin)",
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}

@Composable
fun SensitiveViewBiometricDialog(
  title: String,
  subtitle: String,
  savedPin: String,
  isKurdish: Boolean,
  onTriggerBiometric: () -> Unit,
  onVerifyPin: (String) -> Boolean,
  onDismiss: () -> Unit
) {
  var pinInput by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = EmeraldLight,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
          onClick = onTriggerBiometric,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldLight,
            contentColor = Color(0xFF0F172A)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("sensitive_dialog_biometric_button")
        ) {
          Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isKurdish) "پەنجەمۆر (BiometricPrompt)" else "Use BiometricPrompt",
            fontWeight = FontWeight.Bold
          )
        }

        OutlinedTextField(
          value = pinInput,
          onValueChange = {
            if (it.length <= 8) {
              pinInput = it
              errorText = null
            }
          },
          label = { Text(if (isKurdish) "یان کۆدێ PIN بنڤیسە ($savedPin)" else "Or enter Security PIN ($savedPin)") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sensitive_dialog_pin_input")
        )

        if (errorText != null) {
          Text(
            text = errorText!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (onVerifyPin(pinInput)) {
            onDismiss()
          } else {
            errorText = if (isKurdish) "کۆدێ PIN خەلەتە ($savedPin)" else "Invalid PIN ($savedPin)"
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0F172A))
      ) {
        Text(if (isKurdish) "پشتڕاستکرن" else "Verify PIN", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(if (isKurdish) "پاشگەزبوون" else "Cancel")
      }
    }
  )
}
