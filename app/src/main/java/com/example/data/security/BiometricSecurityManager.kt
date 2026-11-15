package com.example.data.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAuthStatus {
  READY,
  NO_HARDWARE,
  HARDWARE_UNAVAILABLE,
  NONE_ENROLLED,
  UNKNOWN
}

class BiometricSecurityManager(private val context: Context) {

  fun getBiometricStatus(): BiometricAuthStatus {
    val biometricManager = BiometricManager.from(context)
    val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
      BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }

    return when (biometricManager.canAuthenticate(authenticators)) {
      BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NO_HARDWARE
      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthStatus.HARDWARE_UNAVAILABLE
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.NONE_ENROLLED
      else -> BiometricAuthStatus.UNKNOWN
    }
  }

  fun canAuthenticate(): Boolean {
    return getBiometricStatus() == BiometricAuthStatus.READY
  }

  fun getStatusLabel(isKurdish: Boolean): String {
    return when (getBiometricStatus()) {
      BiometricAuthStatus.READY ->
        if (isKurdish) "ئامادەیە (BiometricPrompt چالاکە)" else "BiometricPrompt Ready (Hardware Verified)"
      BiometricAuthStatus.NONE_ENROLLED ->
        if (isKurdish) "پەنجەمۆر ل سەر ئامێری نەهاتیە تۆمارکرن (PIN بەردەستە)" else "No Biometrics Enrolled (PIN Fallback Active)"
      BiometricAuthStatus.NO_HARDWARE ->
        if (isKurdish) "سێنسەرێ پەنجەمۆرێ نینە (پاراستنا PIN کارایە)" else "No Biometric Sensor (PIN Vault Active)"
      BiometricAuthStatus.HARDWARE_UNAVAILABLE ->
        if (isKurdish) "سێنسەرێ پەنجەمۆرێ نوکە بەردەست نینە" else "Biometric Hardware Busy (Use PIN)"
      BiometricAuthStatus.UNKNOWN ->
        if (isKurdish) "پاراستنا پەنجەمۆر و PIN چالاکە" else "Biometric & PIN Security Active"
    }
  }

  fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String = "Biometric Authentication",
    subtitle: String = "Unlock Capital & Debt Tracker",
    description: String = "Confirm your biometric identity to access financial data",
    negativeButtonText: String = "Use Security PIN",
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onFallbackToPin: (() -> Unit)? = null
  ) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        if (
          errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
          errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
          errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
          errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
        ) {
          if (onFallbackToPin != null) {
            onFallbackToPin()
            return
          }
        }
        onError(errString.toString())
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        onError("Biometric authentication failed. Try again or enter PIN.")
      }
    }

    val prompt = BiometricPrompt(activity, executor, callback)

    val promptInfo = try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BiometricPrompt.PromptInfo.Builder()
          .setTitle(title)
          .setSubtitle(subtitle)
          .setDescription(description)
          .setConfirmationRequired(false)
          .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
              BiometricManager.Authenticators.BIOMETRIC_WEAK or
              BiometricManager.Authenticators.DEVICE_CREDENTIAL
          )
          .build()
      } else {
        BiometricPrompt.PromptInfo.Builder()
          .setTitle(title)
          .setSubtitle(subtitle)
          .setDescription(description)
          .setConfirmationRequired(false)
          .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
              BiometricManager.Authenticators.BIOMETRIC_WEAK
          )
          .setNegativeButtonText(negativeButtonText)
          .build()
      }
    } catch (e: Exception) {
      BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .build()
    }

    try {
      prompt.authenticate(promptInfo)
    } catch (e: Exception) {
      if (onFallbackToPin != null) {
        onFallbackToPin()
      } else {
        onError(e.message ?: "Authentication error")
      }
    }
  }
}
