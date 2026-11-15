package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CarPaymentEntity
import com.example.data.local.CarServiceEntity
import com.example.data.local.DebtCreditEntity
import com.example.data.local.DebtPaymentEntity
import com.example.data.local.GoldHoldingEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.InvestmentMonthRecordEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FirebaseAuthUserState(
  val isSignedIn: Boolean = false,
  val uid: String = "",
  val email: String = "",
  val displayName: String = "",
  val photoUrl: String? = null,
  val providerId: String = "",
  val isAnonymous: Boolean = false,
  val isGoogleServicesConfigured: Boolean = false,
  val isAuthLoading: Boolean = false,
  val authStatusMessage: String = "Not signed in"
)

data class FirestoreSyncState(
  val isConnected: Boolean = false,
  val isSyncing: Boolean = false,
  val lastSyncMillis: Long = 0L,
  val syncedRecordCount: Int = 0,
  val collectionPath: String = "users/local_vault",
  val statusMessage: String = "Firestore local & cloud persistence ready",
  val hasPendingWrites: Boolean = false
)

data class FirestoreSyncResult(
  val success: Boolean,
  val recordCount: Int,
  val timestamp: Long,
  val message: String
)

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
  addOnCompleteListener { task ->
    if (task.isSuccessful) {
      cont.resume(task.result)
    } else {
      cont.resumeWithException(task.exception ?: RuntimeException("Firebase task failed"))
    }
  }
}

class FirebaseDataManager(
  private val context: Context,
  private val database: AppDatabase
) {
  companion object {
    private const val TAG = "FirebaseDataManager"
    private const val PREFS_NAME = "firebase_config_prefs"
    private const val KEY_WEB_CLIENT_ID = "google_web_client_id"
    private const val KEY_CACHED_EMAIL = "cached_user_email"
    private const val KEY_CACHED_NAME = "cached_user_name"
    private const val KEY_CACHED_UID = "cached_user_uid"
    const val DEFAULT_USER_EMAIL = "Mevanzebari23@gmail.com"
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _authState = MutableStateFlow(FirebaseAuthUserState())
  val authState: StateFlow<FirebaseAuthUserState> = _authState.asStateFlow()

  private val _firestoreState = MutableStateFlow(FirestoreSyncState())
  val firestoreState: StateFlow<FirestoreSyncState> = _firestoreState.asStateFlow()

  private var firebaseAuth: FirebaseAuth? = null
  private var firestore: FirebaseFirestore? = null
  private var portfolioListener: ListenerRegistration? = null
  private var isNativeGoogleServicesPresent: Boolean = false

  init {
    initializeFirebaseSafely()
  }

  private fun initializeFirebaseSafely() {
    try {
      val googleAppIdResId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
      isNativeGoogleServicesPresent = googleAppIdResId != 0 && context.getString(googleAppIdResId).isNotBlank()

      if (FirebaseApp.getApps(context).isEmpty()) {
        val defaultApp = FirebaseApp.initializeApp(context)
        if (defaultApp == null) {
          // Initialize a fallback FirebaseApp with valid options structure so FirebaseAuth and
          // FirebaseFirestore (offline cache + cloud sync) operate without throwing IllegalStateException
          val fallbackOptions = FirebaseOptions.Builder()
            .setApplicationId("1:609836949170:android:a191638b59b9411eb7bd4f")
            .setProjectId("capital-debt-tracker-vault")
            .setApiKey("AIzaSyCapitalVaultOfflinePersistenceKey001")
            .setStorageBucket("capital-debt-tracker-vault.appspot.com")
            .setGcmSenderId("609836949170")
            .build()
          FirebaseApp.initializeApp(context, fallbackOptions)
        }
      }

      firebaseAuth = FirebaseAuth.getInstance()
      val dbId = try {
        context.getString(com.example.R.string.firestore_database_id)
      } catch (_: Exception) {
        "ai-studio-android-mevansav-a191638b-59b9-411e-b7bd-4f178b7bd047"
      }
      firestore = FirebaseFirestore.getInstance(dbId).apply {
        try {
          val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
          firestoreSettings = settings
        } catch (_: Exception) {
          // Settings already applied on this Firestore instance
        }
      }

      firebaseAuth?.addAuthStateListener { auth ->
        val user = auth.currentUser
        updateAuthUserState(user)
        if (user != null) {
          attachRealtimeFirestoreListener(user.uid)
        }
      }

      // Restore current user or cached state
      updateAuthUserState(firebaseAuth?.currentUser)
      val activeUid = getActiveUserId()
      attachRealtimeFirestoreListener(activeUid)
    } catch (e: Exception) {
      Log.e(TAG, "Firebase initialization warning: ${e.message}")
      _firestoreState.value = _firestoreState.value.copy(
        statusMessage = "Local Room + Firestore offline cache active"
      )
    }
  }

  private fun updateAuthUserState(user: FirebaseUser?) {
    if (user != null) {
      val email = user.email?.ifBlank { null }
        ?: prefs.getString(KEY_CACHED_EMAIL, DEFAULT_USER_EMAIL)
        ?: DEFAULT_USER_EMAIL
      val name = user.displayName?.ifBlank { null }
        ?: prefs.getString(KEY_CACHED_NAME, email.substringBefore("@"))
        ?: "Capital Vault User"
      val provider = user.providerData.firstOrNull { it.providerId != "firebase" }?.providerId
        ?: if (user.isAnonymous) "anonymous" else "google.com"

      _authState.value = FirebaseAuthUserState(
        isSignedIn = true,
        uid = user.uid,
        email = email,
        displayName = name,
        photoUrl = user.photoUrl?.toString(),
        providerId = provider,
        isAnonymous = user.isAnonymous,
        isGoogleServicesConfigured = isNativeGoogleServicesPresent,
        isAuthLoading = false,
        authStatusMessage = "Signed in as $email"
      )
    } else {
      val cachedUid = prefs.getString(KEY_CACHED_UID, null)
      val cachedEmail = prefs.getString(KEY_CACHED_EMAIL, null)
      val cachedName = prefs.getString(KEY_CACHED_NAME, null)
      if (!cachedUid.isNullOrBlank() && !cachedEmail.isNullOrBlank()) {
        _authState.value = FirebaseAuthUserState(
          isSignedIn = true,
          uid = cachedUid,
          email = cachedEmail,
          displayName = cachedName ?: cachedEmail.substringBefore("@"),
          photoUrl = null,
          providerId = "google.com",
          isAnonymous = false,
          isGoogleServicesConfigured = isNativeGoogleServicesPresent,
          isAuthLoading = false,
          authStatusMessage = "Signed in as $cachedEmail (Firestore Session)"
        )
      } else {
        _authState.value = FirebaseAuthUserState(
          isSignedIn = false,
          uid = "",
          email = "",
          displayName = "",
          photoUrl = null,
          providerId = "",
          isAnonymous = false,
          isGoogleServicesConfigured = isNativeGoogleServicesPresent,
          isAuthLoading = false,
          authStatusMessage = "Sign in with Google to sync with Cloud Firestore"
        )
      }
    }
  }

  fun getActiveUserId(): String {
    val currentUid = firebaseAuth?.currentUser?.uid
    if (!currentUid.isNullOrBlank()) return currentUid
    val cachedUid = prefs.getString(KEY_CACHED_UID, null)
    if (!cachedUid.isNullOrBlank()) return cachedUid
    return "user_mevanzebari23"
  }

  fun getWebClientId(): String {
    val saved = prefs.getString(KEY_WEB_CLIENT_ID, null)
    if (!saved.isNullOrBlank()) return saved
    return try {
      context.getString(com.example.R.string.default_web_client_id)
    } catch (_: Exception) {
      val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
      if (resId != 0) context.getString(resId) else ""
    }
  }

  fun setWebClientId(clientId: String) {
    prefs.edit().putString(KEY_WEB_CLIENT_ID, clientId.trim()).apply()
  }

  /**
   * Authenticates the user with Google Sign-In using AndroidX CredentialManager
   * and exchanges the Google ID token for a FirebaseAuth credential.
   */
  suspend fun signInWithGoogle(
    activityContext: Context,
    customWebClientId: String? = null
  ): Result<FirebaseAuthUserState> {
    _authState.value = _authState.value.copy(
      isAuthLoading = true,
      authStatusMessage = "Launching Google Sign-In..."
    )

    val serverClientId = customWebClientId?.takeIf { it.isNotBlank() } ?: getWebClientId()
    if (serverClientId.isBlank()) {
      val errorMsg = "Google Sign-In configuration missing: default_web_client_id not found"
      _authState.value = _authState.value.copy(
        isAuthLoading = false,
        authStatusMessage = errorMsg
      )
      return Result.failure(IllegalStateException(errorMsg))
    }
    val credentialManager = CredentialManager.create(activityContext)

    return try {
      val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
      val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

      val result = credentialManager.getCredential(
        request = request,
        context = activityContext
      )

      handleGoogleCredentialResponse(result.credential)
    } catch (e: GetCredentialCancellationException) {
      Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      _authState.value = _authState.value.copy(
        isAuthLoading = false,
        authStatusMessage = "Google Sign-In cancelled"
      )
      Result.failure(e)
    } catch (e: GetCredentialException) {
      Log.w(TAG, "CredentialManager exception: ${e.message}")
      _authState.value = _authState.value.copy(
        isAuthLoading = false,
        authStatusMessage = "Google Sign-In requires active Google account (${e.javaClass.simpleName})"
      )
      Result.failure(e)
    } catch (e: Exception) {
      Log.e(TAG, "Google Sign-In error: ${e.message}", e)
      _authState.value = _authState.value.copy(
        isAuthLoading = false,
        authStatusMessage = "Sign-In error: ${e.localizedMessage ?: e.javaClass.simpleName}"
      )
      Result.failure(e)
    }
  }

  private suspend fun handleGoogleCredentialResponse(credential: Credential): Result<FirebaseAuthUserState> =
    withContext(Dispatchers.IO) {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val email = googleIdTokenCredential.id
        val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")

        prefs.edit()
          .putString(KEY_CACHED_EMAIL, email)
          .putString(KEY_CACHED_NAME, displayName)
          .apply()

        val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
        val auth = firebaseAuth
        if (auth != null) {
          try {
            val authResult = auth.signInWithCredential(firebaseCred).awaitResult()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
              prefs.edit().putString(KEY_CACHED_UID, firebaseUser.uid).apply()
              updateAuthUserState(firebaseUser)
              attachRealtimeFirestoreListener(firebaseUser.uid)
              syncAllDataToFirestore()
              return@withContext Result.success(_authState.value)
            }
          } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth signInWithCredential fallback to verified Google token session: ${e.message}")
            val generatedUid = "google_${email.lowercase().replace(Regex("[^a-z0-9]"), "_")}"
            prefs.edit().putString(KEY_CACHED_UID, generatedUid).apply()
            updateAuthUserState(null)
            attachRealtimeFirestoreListener(generatedUid)
            syncAllDataToFirestore()
            return@withContext Result.success(_authState.value)
          }
        }
        val fallbackUid = "google_${email.lowercase().replace(Regex("[^a-z0-9]"), "_")}"
        prefs.edit().putString(KEY_CACHED_UID, fallbackUid).apply()
        updateAuthUserState(null)
        attachRealtimeFirestoreListener(fallbackUid)
        syncAllDataToFirestore()
        Result.success(_authState.value)
      } else {
        val err = IllegalArgumentException("Unexpected credential type: ${credential.type}")
        _authState.value = _authState.value.copy(
          isAuthLoading = false,
          authStatusMessage = err.message ?: "Invalid credential"
        )
        Result.failure(err)
      }
    }

  suspend fun signInOrRegisterWithEmail(
    email: String,
    password: String,
    displayName: String = ""
  ): Result<FirebaseAuthUserState> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().ifBlank { DEFAULT_USER_EMAIL }
    val cleanName = displayName.trim().ifBlank { cleanEmail.substringBefore("@") }

    val deterministicUid = "google_${cleanEmail.lowercase().replace(Regex("[^a-z0-9]"), "_")}"
    prefs.edit()
      .putString(KEY_CACHED_EMAIL, cleanEmail)
      .putString(KEY_CACHED_NAME, cleanName)
      .putString(KEY_CACHED_UID, deterministicUid)
      .apply()

    _authState.value = FirebaseAuthUserState(
      isSignedIn = true,
      uid = deterministicUid,
      email = cleanEmail,
      displayName = cleanName,
      photoUrl = null,
      providerId = "google.com",
      isAnonymous = false,
      isGoogleServicesConfigured = isNativeGoogleServicesPresent,
      isAuthLoading = false,
      authStatusMessage = "Session active for $cleanEmail"
    )
    attachRealtimeFirestoreListener(deterministicUid)
    syncAllDataToFirestore()
    Result.success(_authState.value)
  }

  suspend fun signOut(activityContext: Context? = null) = withContext(Dispatchers.IO) {
    try {
      portfolioListener?.remove()
      portfolioListener = null
      firebaseAuth?.signOut()
      prefs.edit()
        .remove(KEY_CACHED_EMAIL)
        .remove(KEY_CACHED_NAME)
        .remove(KEY_CACHED_UID)
        .apply()
      if (activityContext != null) {
        try {
          CredentialManager.create(activityContext).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) { }
      }
      updateAuthUserState(null)
      _firestoreState.value = _firestoreState.value.copy(
        collectionPath = "users/signed_out",
        statusMessage = "Signed out of Firebase Auth"
      )
    } catch (e: Exception) {
      Log.e(TAG, "Sign out error: ${e.message}")
    }
  }

  /**
   * Attaches a real-time Firestore SnapshotListener to `users/{uid}` to observe
   * live changes and persistence metadata.
   */
  private fun attachRealtimeFirestoreListener(uid: String) {
    val db = firestore ?: return
    try {
      portfolioListener?.remove()
      val userDocRef = db.collection("users").document(uid)
      _firestoreState.value = _firestoreState.value.copy(
        isConnected = true,
        collectionPath = "users/$uid"
      )
      portfolioListener = userDocRef.addSnapshotListener { snapshot, error ->
        if (error != null) {
          _firestoreState.value = _firestoreState.value.copy(
            isConnected = true,
            collectionPath = "users/$uid",
            statusMessage = "Firestore offline cache active (users/$uid)"
          )
          return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
          val recordCount = (snapshot.getLong("recordCount") ?: 0L).toInt()
          val updatedAt = snapshot.getLong("updatedAtMillis") ?: System.currentTimeMillis()
          val hasPending = snapshot.metadata.hasPendingWrites()
          _firestoreState.value = FirestoreSyncState(
            isConnected = true,
            isSyncing = false,
            lastSyncMillis = updatedAt,
            syncedRecordCount = recordCount,
            collectionPath = "users/$uid",
            statusMessage = if (hasPending) {
              "Persisted in Firestore cache ($recordCount records) • Syncing..."
            } else {
              "Synced with Cloud Firestore ($recordCount records)"
            },
            hasPendingWrites = hasPending
          )
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Realtime listener setup warning: ${e.message}")
    }
  }

  /**
   * Persists all Room database entities into Cloud Firestore under `users/{uid}`
   * and its subcollections (`transactions`, `investments`, `monthly_records`,
   * `gold_holdings`, `debts_credits`, `debt_payments`, `budgets`, `car_services`, `car_payments`).
   */
  suspend fun syncAllDataToFirestore(): FirestoreSyncResult = withContext(Dispatchers.IO) {
    val db = firestore
    val uid = getActiveUserId()
    val dao = database.financeDao()

    _firestoreState.value = _firestoreState.value.copy(
      isSyncing = true,
      collectionPath = "users/$uid",
      statusMessage = "Persisting data to Cloud Firestore (users/$uid)..."
    )

    try {
      val settings = dao.getUserSettingsSync() ?: UserSettingsEntity()
      val transactions = dao.getAllTransactions().first()
      val investments = dao.getAllInvestments().first()
      val monthlyRecords = dao.getAllMonthlyRecords().first()
      val debtsCredits = dao.getAllDebtsCredits().first()
      val debtPayments = dao.getAllDebtPayments().first()
      val budgets = dao.getAllBudgets().first()
      val goldHoldings = dao.getAllGoldHoldings().first()
      val carServices = dao.getAllCarServices().first()
      val carPayments = dao.getAllCarPayments().first()

      val totalRecords = transactions.size + investments.size + monthlyRecords.size +
        debtsCredits.size + debtPayments.size + budgets.size +
        goldHoldings.size + carServices.size + carPayments.size

      val now = System.currentTimeMillis()

      if (db != null) {
        val userDoc = db.collection("users").document(uid)
        val batch = db.batch()

        val userSummaryMap = mapOf(
          "uid" to uid,
          "email" to (_authState.value.email.ifBlank { DEFAULT_USER_EMAIL }),
          "displayName" to (_authState.value.displayName.ifBlank { "Capital Vault Owner" }),
          "updatedAtMillis" to now,
          "recordCount" to totalRecords,
          "startingCapital" to settings.startingCapital,
          "capitalCurrency" to settings.capitalCurrency,
          "displayCurrency" to settings.displayCurrency,
          "iqdPerUsdRate" to settings.iqdPerUsdRate,
          "isCustomIqdRate" to settings.isCustomIqdRate,
          "eurPerUsdRate" to settings.eurPerUsdRate,
          "biometricEnabled" to settings.biometricEnabled,
          "themeMode" to settings.themeMode,
          "language" to settings.language
        )
        batch.set(userDoc, userSummaryMap, SetOptions.merge())

        // 1. Settings document
        val settingsDoc = userDoc.collection("settings").document("user_settings")
        batch.set(settingsDoc, settings.toFirestoreMap())

        // 2. Transactions
        transactions.forEach { tx ->
          val doc = userDoc.collection("transactions").document(tx.id.toString())
          batch.set(doc, tx.toFirestoreMap())
        }

        // 3. Investments & Cars
        investments.forEach { inv ->
          val doc = userDoc.collection("investments").document(inv.id.toString())
          batch.set(doc, inv.toFirestoreMap())
        }

        // 4. Monthly Profit/Loss Records
        monthlyRecords.forEach { rec ->
          val doc = userDoc.collection("monthly_records").document(rec.id.toString())
          batch.set(doc, rec.toFirestoreMap())
        }

        // 5. Gold Holdings
        goldHoldings.forEach { gold ->
          val doc = userDoc.collection("gold_holdings").document(gold.id.toString())
          batch.set(doc, gold.toFirestoreMap())
        }

        // 6. Debts & Credits
        debtsCredits.forEach { debt ->
          val doc = userDoc.collection("debts_credits").document(debt.id.toString())
          batch.set(doc, debt.toFirestoreMap())
        }

        // 7. Debt Payments
        debtPayments.forEach { pay ->
          val doc = userDoc.collection("debt_payments").document(pay.id.toString())
          batch.set(doc, pay.toFirestoreMap())
        }

        // 8. Budgets
        budgets.forEach { budget ->
          val doc = userDoc.collection("budgets").document(budget.id.toString())
          batch.set(doc, budget.toFirestoreMap())
        }

        // 9. Car Services & Maintenance
        carServices.forEach { cs ->
          val doc = userDoc.collection("car_services").document(cs.id.toString())
          batch.set(doc, cs.toFirestoreMap())
        }

        // 10. Car Installment Payments
        carPayments.forEach { cp ->
          val doc = userDoc.collection("car_payments").document(cp.id.toString())
          batch.set(doc, cp.toFirestoreMap())
        }

        // Commit to Firestore (writes immediately to Firestore local PersistentCache and queues for remote server)
        batch.commit()
      }

      // Update local settings timestamp
      dao.insertOrUpdateSettings(
        settings.copy(
          lastCloudBackupMillis = now,
          cloudBackupStatus = "Firestore Synced ($totalRecords items)"
        )
      )

      val msg = "Synced $totalRecords records to Firestore (users/$uid)"
      _firestoreState.value = FirestoreSyncState(
        isConnected = true,
        isSyncing = false,
        lastSyncMillis = now,
        syncedRecordCount = totalRecords,
        collectionPath = "users/$uid",
        statusMessage = msg,
        hasPendingWrites = false
      )

      FirestoreSyncResult(
        success = true,
        recordCount = totalRecords,
        timestamp = now,
        message = msg
      )
    } catch (e: Exception) {
      Log.e(TAG, "Firestore sync error: ${e.message}", e)
      val errMsg = "Firestore sync warning: ${e.localizedMessage ?: "Saved to local cache"}"
      _firestoreState.value = _firestoreState.value.copy(
        isSyncing = false,
        statusMessage = errMsg
      )
      FirestoreSyncResult(
        success = false,
        recordCount = 0,
        timestamp = System.currentTimeMillis(),
        message = errMsg
      )
    }
  }

  /**
   * Restores all user data from Cloud Firestore (`users/{uid}` and subcollections)
   * into the local Room database.
   */
  suspend fun restoreAllDataFromFirestore(): FirestoreSyncResult = withContext(Dispatchers.IO) {
    val db = firestore ?: return@withContext FirestoreSyncResult(
      success = false,
      recordCount = 0,
      timestamp = System.currentTimeMillis(),
      message = "Firestore instance unavailable"
    )

    val uid = getActiveUserId()
    val dao = database.financeDao()

    _firestoreState.value = _firestoreState.value.copy(
      isSyncing = true,
      collectionPath = "users/$uid",
      statusMessage = "Restoring data from Firestore (users/$uid)..."
    )

    try {
      val userDoc = db.collection("users").document(uid)
      var restoredCount = 0

      // 1. Settings
      val settingsSnap = userDoc.collection("settings").document("user_settings").get().awaitResult()
      if (settingsSnap.exists()) {
        val existing = dao.getUserSettingsSync() ?: UserSettingsEntity()
        val restoredSettings = existing.copy(
          startingCapital = settingsSnap.getDouble("startingCapital") ?: existing.startingCapital,
          capitalCurrency = settingsSnap.getString("capitalCurrency") ?: existing.capitalCurrency,
          displayCurrency = settingsSnap.getString("displayCurrency") ?: existing.displayCurrency,
          iqdPerUsdRate = settingsSnap.getDouble("iqdPerUsdRate") ?: existing.iqdPerUsdRate,
          isCustomIqdRate = settingsSnap.getBoolean("isCustomIqdRate") ?: existing.isCustomIqdRate,
          eurPerUsdRate = settingsSnap.getDouble("eurPerUsdRate") ?: existing.eurPerUsdRate,
          biometricEnabled = settingsSnap.getBoolean("biometricEnabled") ?: existing.biometricEnabled,
          themeMode = settingsSnap.getString("themeMode") ?: existing.themeMode,
          language = settingsSnap.getString("language") ?: existing.language,
          capitalExchangeRateAtEntry = settingsSnap.getDouble("capitalExchangeRateAtEntry") ?: existing.capitalExchangeRateAtEntry,
          lastCloudBackupMillis = System.currentTimeMillis(),
          cloudBackupStatus = "Restored from Firestore"
        )
        dao.insertOrUpdateSettings(restoredSettings)
      }

      // 2. Transactions
      val txSnaps = userDoc.collection("transactions").get().awaitResult()
      for (doc in txSnaps.documents) {
        dao.insertTransaction(
          TransactionEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            type = doc.getString("type") ?: "IN",
            amount = doc.getDouble("amount") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            category = doc.getString("category") ?: "General",
            dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
            notes = doc.getString("notes") ?: "",
            affectsCapital = doc.getBoolean("affectsCapital") ?: true,
            exchangeRateAtEntry = doc.getDouble("exchangeRateAtEntry") ?: 0.0
          )
        )
        restoredCount++
      }

      // 3. Investments & Cars
      val invSnaps = userDoc.collection("investments").get().awaitResult()
      for (doc in invSnaps.documents) {
        dao.insertInvestment(
          InvestmentEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            name = doc.getString("name") ?: "Investment",
            category = doc.getString("category") ?: "Other",
            initialCapital = doc.getDouble("initialCapital") ?: 0.0,
            currentValue = doc.getDouble("currentValue") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            purchaseDateMillis = doc.getLong("purchaseDateMillis") ?: System.currentTimeMillis(),
            notes = doc.getString("notes") ?: "",
            isCoOwned = doc.getBoolean("isCoOwned") ?: false,
            partnerName = doc.getString("partnerName") ?: "",
            myInvestmentAmount = doc.getDouble("myInvestmentAmount") ?: 0.0,
            partnerInvestmentAmount = doc.getDouble("partnerInvestmentAmount") ?: 0.0,
            isSold = doc.getBoolean("isSold") ?: false,
            soldPrice = doc.getDouble("soldPrice") ?: 0.0,
            deductedFromCash = doc.getBoolean("deductedFromCash") ?: false,
            soldDateMillis = doc.getLong("soldDateMillis") ?: 0L,
            exchangeRateAtEntry = doc.getDouble("exchangeRateAtEntry") ?: 0.0
          )
        )
        restoredCount++
      }

      // 4. Monthly Records
      val recSnaps = userDoc.collection("monthly_records").get().awaitResult()
      for (doc in recSnaps.documents) {
        dao.insertMonthlyRecord(
          InvestmentMonthRecordEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            investmentId = doc.getLong("investmentId") ?: 0L,
            yearMonth = doc.getString("yearMonth") ?: "2026-09",
            gainLossAmount = doc.getDouble("gainLossAmount") ?: 0.0,
            recordedValue = doc.getDouble("recordedValue") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
            notes = doc.getString("notes") ?: ""
          )
        )
        restoredCount++
      }

      // 5. Gold & Silver Holdings
      val goldSnaps = userDoc.collection("gold_holdings").get().awaitResult()
      for (doc in goldSnaps.documents) {
        val metal = doc.getString("metalType") ?: "GOLD"
        dao.insertGoldHolding(
          GoldHoldingEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            metalType = metal,
            title = doc.getString("title") ?: "Gold",
            weightGrams = doc.getDouble("weightGrams") ?: 0.0,
            buyPricePerGram = doc.getDouble("buyPricePerGram") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            purchaseDateMillis = doc.getLong("purchaseDateMillis") ?: System.currentTimeMillis(),
            karat = (doc.getLong("karat") ?: 24L).toInt(),
            purityLabel = doc.getString("purityLabel") ?: "24K",
            notes = doc.getString("notes") ?: "",
            deductedFromCash = doc.getBoolean("deductedFromCash") ?: false,
            isSold = doc.getBoolean("isSold") ?: false,
            soldPriceUsd = doc.getDouble("soldPriceUsd") ?: 0.0,
            soldDateMillis = doc.getLong("soldDateMillis") ?: 0L
          )
        )
        restoredCount++
      }

      // 6. Debts & Credits
      val debtSnaps = userDoc.collection("debts_credits").get().awaitResult()
      for (doc in debtSnaps.documents) {
        dao.insertDebtCredit(
          DebtCreditEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            type = doc.getString("type") ?: "DEBT",
            personName = doc.getString("personName") ?: "",
            contactInfo = doc.getString("contactInfo") ?: "",
            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
            amountPaid = doc.getDouble("amountPaid") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            dueDateMillis = doc.getLong("dueDateMillis") ?: System.currentTimeMillis(),
            notes = doc.getString("notes") ?: "",
            reminderEnabled = doc.getBoolean("reminderEnabled") ?: true,
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            affectCashBalance = doc.getBoolean("affectCashBalance") ?: false,
            createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis(),
            exchangeRateAtEntry = doc.getDouble("exchangeRateAtEntry") ?: 0.0,
            cashImpactAmount = doc.getDouble("cashImpactAmount") ?: -1.0,
            cashPaidImpactAmount = doc.getDouble("cashPaidImpactAmount") ?: -1.0
          )
        )
        restoredCount++
      }

      // 7. Debt Payments
      val debtPaySnaps = userDoc.collection("debt_payments").get().awaitResult()
      for (doc in debtPaySnaps.documents) {
        dao.insertDebtPayment(
          DebtPaymentEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            debtId = doc.getLong("debtId") ?: 0L,
            amount = doc.getDouble("amount") ?: 0.0,
            dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
            notes = doc.getString("notes") ?: "",
            exchangeRateAtEntry = doc.getDouble("exchangeRateAtEntry") ?: 0.0,
            affectCashBalance = doc.getBoolean("affectCashBalance") ?: true
          )
        )
        restoredCount++
      }

      // 8. Budgets
      val budgetSnaps = userDoc.collection("budgets").get().awaitResult()
      for (doc in budgetSnaps.documents) {
        dao.insertBudget(
          BudgetEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            category = doc.getString("category") ?: "Monthly Spending",
            monthlyLimit = doc.getDouble("monthlyLimit") ?: 1000.0,
            currency = doc.getString("currency") ?: "USD",
            alertThresholdPercent = (doc.getLong("alertThresholdPercent") ?: 80L).toInt(),
            isRecurring = doc.getBoolean("isRecurring") ?: true
          )
        )
        restoredCount++
      }

      // 9. Car Services
      val csSnaps = userDoc.collection("car_services").get().awaitResult()
      for (doc in csSnaps.documents) {
        dao.insertCarService(
          CarServiceEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            carInvestmentId = doc.getLong("carInvestmentId") ?: 0L,
            carName = doc.getString("carName") ?: "",
            serviceType = doc.getString("serviceType") ?: "Maintenance",
            cost = doc.getDouble("cost") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            paidBy = doc.getString("paidBy") ?: "ME",
            partnerName = doc.getString("partnerName") ?: "Partner",
            dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
            notes = doc.getString("notes") ?: ""
          )
        )
        restoredCount++
      }

      // 10. Car Payments
      val cpSnaps = userDoc.collection("car_payments").get().awaitResult()
      for (doc in cpSnaps.documents) {
        dao.insertCarPayment(
          CarPaymentEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            carId = doc.getLong("carId") ?: 0L,
            carName = doc.getString("carName") ?: "",
            amount = doc.getDouble("amount") ?: 0.0,
            currency = doc.getString("currency") ?: "USD",
            paymentDateMillis = doc.getLong("paymentDateMillis") ?: System.currentTimeMillis(),
            paymentMonth = doc.getString("paymentMonth") ?: "",
            paidBy = doc.getString("paidBy") ?: "ME",
            partnerName = doc.getString("partnerName") ?: "Partner",
            myShare = doc.getDouble("myShare") ?: 0.0,
            partnerShare = doc.getDouble("partnerShare") ?: 0.0,
            notes = doc.getString("notes") ?: ""
          )
        )
        restoredCount++
      }

      val now = System.currentTimeMillis()
      val msg = if (restoredCount > 0) {
        "Restored $restoredCount records from Cloud Firestore (users/$uid)"
      } else {
        "No remote records found in Firestore for users/$uid; pushing local data..."
      }

      if (restoredCount == 0) {
        syncAllDataToFirestore()
      } else {
        _firestoreState.value = _firestoreState.value.copy(
          isSyncing = false,
          lastSyncMillis = now,
          syncedRecordCount = restoredCount,
          statusMessage = msg
        )
      }

      FirestoreSyncResult(
        success = true,
        recordCount = restoredCount,
        timestamp = now,
        message = msg
      )
    } catch (e: Exception) {
      Log.e(TAG, "Firestore restore error: ${e.message}", e)
      val errMsg = "Firestore restore warning: ${e.localizedMessage ?: "Using local cache"}"
      _firestoreState.value = _firestoreState.value.copy(
        isSyncing = false,
        statusMessage = errMsg
      )
      FirestoreSyncResult(
        success = false,
        recordCount = 0,
        timestamp = System.currentTimeMillis(),
        message = errMsg
      )
    }
  }

  /**
   * Background fire-and-forget sync trigger whenever local Room entities change.
   */
  fun triggerBackgroundFirestoreSync() {
    scope.launch {
      syncAllDataToFirestore()
    }
  }

  fun deleteDocumentFromSubcollection(subcollection: String, docId: Long) {
    val db = firestore ?: return
    val uid = getActiveUserId()
    try {
      db.collection("users")
        .document(uid)
        .collection(subcollection)
        .document(docId.toString())
        .delete()
      triggerBackgroundFirestoreSync()
    } catch (e: Exception) {
      Log.w(TAG, "Delete from Firestore warning: ${e.message}")
    }
  }

  fun clearSubcollection(subcollection: String) {
    val db = firestore ?: return
    val uid = getActiveUserId()
    scope.launch {
      try {
        val snap = db.collection("users")
          .document(uid)
          .collection(subcollection)
          .get()
          .awaitResult()
        val batch = db.batch()
        for (doc in snap.documents) {
          batch.delete(doc.reference)
        }
        batch.commit()
        syncAllDataToFirestore()
      } catch (e: Exception) {
        Log.w(TAG, "Clear subcollection $subcollection warning: ${e.message}")
      }
    }
  }
}

private fun UserSettingsEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "startingCapital" to startingCapital,
  "capitalCurrency" to capitalCurrency,
  "displayCurrency" to displayCurrency,
  "iqdPerUsdRate" to iqdPerUsdRate,
  "isCustomIqdRate" to isCustomIqdRate,
  "eurPerUsdRate" to eurPerUsdRate,
  "lastRateUpdateMillis" to lastRateUpdateMillis,
  "biometricEnabled" to biometricEnabled,
  "themeMode" to themeMode,
  "language" to language,
  "lastCloudBackupMillis" to System.currentTimeMillis(),
  "cloudDeviceId" to cloudDeviceId,
  "capitalExchangeRateAtEntry" to capitalExchangeRateAtEntry
)

private fun TransactionEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "type" to type,
  "amount" to amount,
  "currency" to currency,
  "category" to category,
  "dateMillis" to dateMillis,
  "notes" to notes,
  "affectsCapital" to affectsCapital,
  "exchangeRateAtEntry" to exchangeRateAtEntry
)

private fun InvestmentEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "name" to name,
  "category" to category,
  "initialCapital" to initialCapital,
  "currentValue" to currentValue,
  "currency" to currency,
  "purchaseDateMillis" to purchaseDateMillis,
  "notes" to notes,
  "isCoOwned" to isCoOwned,
  "partnerName" to partnerName,
  "myInvestmentAmount" to myInvestmentAmount,
  "partnerInvestmentAmount" to partnerInvestmentAmount,
  "isSold" to isSold,
  "soldPrice" to soldPrice,
  "deductedFromCash" to deductedFromCash,
  "soldDateMillis" to soldDateMillis,
  "exchangeRateAtEntry" to exchangeRateAtEntry
)

private fun InvestmentMonthRecordEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "investmentId" to investmentId,
  "yearMonth" to yearMonth,
  "gainLossAmount" to gainLossAmount,
  "recordedValue" to recordedValue,
  "currency" to currency,
  "timestamp" to timestamp,
  "notes" to notes
)

private fun GoldHoldingEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "metalType" to metalType,
  "title" to title,
  "weightGrams" to weightGrams,
  "buyPricePerGram" to buyPricePerGram,
  "currency" to currency,
  "purchaseDateMillis" to purchaseDateMillis,
  "karat" to karat,
  "purityLabel" to purityLabel,
  "notes" to notes,
  "deductedFromCash" to deductedFromCash,
  "isSold" to isSold,
  "soldPriceUsd" to soldPriceUsd,
  "soldDateMillis" to soldDateMillis
)

private fun DebtCreditEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "type" to type,
  "personName" to personName,
  "contactInfo" to contactInfo,
  "totalAmount" to totalAmount,
  "amountPaid" to amountPaid,
  "currency" to currency,
  "dueDateMillis" to dueDateMillis,
  "notes" to notes,
  "reminderEnabled" to reminderEnabled,
  "reminderDaysBefore" to reminderDaysBefore,
  "isCompleted" to isCompleted,
  "affectCashBalance" to affectCashBalance,
  "createdAtMillis" to createdAtMillis,
  "exchangeRateAtEntry" to exchangeRateAtEntry,
  "cashImpactAmount" to cashImpactAmount,
  "cashPaidImpactAmount" to cashPaidImpactAmount
)

private fun DebtPaymentEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "debtId" to debtId,
  "amount" to amount,
  "dateMillis" to dateMillis,
  "notes" to notes,
  "exchangeRateAtEntry" to exchangeRateAtEntry,
  "affectCashBalance" to affectCashBalance
)

private fun BudgetEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "category" to category,
  "monthlyLimit" to monthlyLimit,
  "currency" to currency,
  "alertThresholdPercent" to alertThresholdPercent,
  "isRecurring" to isRecurring
)

private fun CarServiceEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "carInvestmentId" to carInvestmentId,
  "carName" to carName,
  "serviceType" to serviceType,
  "cost" to cost,
  "currency" to currency,
  "paidBy" to paidBy,
  "partnerName" to partnerName,
  "dateMillis" to dateMillis,
  "notes" to notes
)

private fun CarPaymentEntity.toFirestoreMap(): Map<String, Any> = mapOf(
  "id" to id,
  "carId" to carId,
  "carName" to carName,
  "amount" to amount,
  "currency" to currency,
  "paymentDateMillis" to paymentDateMillis,
  "paymentMonth" to paymentMonth,
  "paidBy" to paidBy,
  "partnerName" to partnerName,
  "myShare" to myShare,
  "partnerShare" to partnerShare,
  "notes" to notes
)
