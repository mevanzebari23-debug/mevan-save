package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseDataManager
import com.example.data.network.NetworkConnectivityObserver
import com.example.data.network.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OfflineSyncState(
  val isOnline: Boolean = true,
  val pendingMutationsCount: Int = 0,
  val isSyncing: Boolean = false,
  val lastSuccessfulSyncMillis: Long = 0L,
  val showSyncSuccessBanner: Boolean = false,
  val statusMessage: String = "Ready"
)

/**
 * Offline Sync Manager for Capital & Debt Tracker.
 * Manages zero-loss offline data entry, monitors network status, and
 * automatically initiates bidirectional Cloud Firestore sync when connectivity is restored.
 */
class OfflineSyncManager(
  private val context: Context,
  private val firebaseDataManager: FirebaseDataManager,
  private val connectivityObserver: NetworkConnectivityObserver
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val prefs = context.getSharedPreferences("offline_sync_manager_prefs", Context.MODE_PRIVATE)

  private val _state = MutableStateFlow(
    OfflineSyncState(
      isOnline = connectivityObserver.isCurrentlyOnline(),
      pendingMutationsCount = prefs.getInt("pending_mutations_count", 0),
      lastSuccessfulSyncMillis = prefs.getLong("last_successful_sync_millis", 0L)
    )
  )
  val state: StateFlow<OfflineSyncState> = _state.asStateFlow()

  private var wasPreviouslyOffline = !connectivityObserver.isCurrentlyOnline()

  init {
    observeConnectivityChanges()
  }

  private fun observeConnectivityChanges() {
    scope.launch {
      connectivityObserver.networkStatusFlow.collect { status ->
        val isNowOnline = (status == NetworkStatus.ONLINE)
        val previouslyOffline = !_state.value.isOnline

        Log.d("OfflineSyncManager", "Connectivity changed: isOnline=$isNowOnline, wasOffline=$previouslyOffline")

        _state.value = _state.value.copy(
          isOnline = isNowOnline,
          statusMessage = if (isNowOnline) "Online" else "Offline Mode Active"
        )

        // Automatically trigger synchronization when transitioning from Offline to Online!
        if (isNowOnline && previouslyOffline) {
          Log.i("OfflineSyncManager", "Network connection restored! Initiating automatic cloud sync...")
          triggerAutomaticReconnectionSync()
        }
      }
    }
  }

  /**
   * Called by FinanceViewModel whenever a financial data mutation (insert, update, delete)
   * occurs in the local Room database.
   */
  fun onDataMutated() {
    val isOnline = _state.value.isOnline
    if (!isOnline) {
      val newCount = _state.value.pendingMutationsCount + 1
      prefs.edit().putInt("pending_mutations_count", newCount).apply()
      _state.value = _state.value.copy(
        pendingMutationsCount = newCount,
        statusMessage = "Offline: $newCount changes queued for cloud sync"
      )
      Log.d("OfflineSyncManager", "Offline mutation recorded. Total pending: $newCount")
    } else {
      // Online: trigger background sync immediately
      firebaseDataManager.triggerBackgroundFirestoreSync()
    }
  }

  /**
   * Automatically synchronizes all queued offline data with Cloud Firestore
   * once network connectivity is restored.
   */
  fun triggerAutomaticReconnectionSync() {
    scope.launch {
      _state.value = _state.value.copy(
        isSyncing = true,
        statusMessage = "Auto-syncing changes to cloud..."
      )

      try {
        // 1. Sync all local entities to Firestore
        val syncResult = firebaseDataManager.syncAllDataToFirestore()
        // 2. Pull down any remote changes
        firebaseDataManager.restoreAllDataFromFirestore()

        val now = System.currentTimeMillis()
        prefs.edit()
          .putInt("pending_mutations_count", 0)
          .putLong("last_successful_sync_millis", now)
          .apply()

        _state.value = _state.value.copy(
          isSyncing = false,
          pendingMutationsCount = 0,
          lastSuccessfulSyncMillis = now,
          showSyncSuccessBanner = true,
          statusMessage = "Auto-sync completed successfully"
        )

        Log.i("OfflineSyncManager", "Automatic cloud sync succeeded: ${syncResult.message}")

        // Auto-dismiss success banner after 5 seconds
        kotlinx.coroutines.delay(5000)
        _state.value = _state.value.copy(showSyncSuccessBanner = false)
      } catch (e: Exception) {
        Log.e("OfflineSyncManager", "Automatic sync failed: ${e.message}")
        _state.value = _state.value.copy(
          isSyncing = false,
          statusMessage = "Auto-sync will retry on next connection"
        )
      }
    }
  }

  /**
   * Manual trigger by user via UI (e.g., "Sync Now" button)
   */
  fun triggerManualSync(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
    scope.launch {
      _state.value = _state.value.copy(
        isSyncing = true,
        statusMessage = "Manual sync in progress..."
      )

      try {
        val result = firebaseDataManager.syncAllDataToFirestore()
        firebaseDataManager.restoreAllDataFromFirestore()

        val now = System.currentTimeMillis()
        prefs.edit()
          .putInt("pending_mutations_count", 0)
          .putLong("last_successful_sync_millis", now)
          .apply()

        _state.value = _state.value.copy(
          isSyncing = false,
          pendingMutationsCount = 0,
          lastSuccessfulSyncMillis = now,
          showSyncSuccessBanner = true,
          statusMessage = "Sync completed"
        )

        onComplete(true, "Synchronized ${result.recordCount} items")

        kotlinx.coroutines.delay(4000)
        _state.value = _state.value.copy(showSyncSuccessBanner = false)
      } catch (e: Exception) {
        _state.value = _state.value.copy(
          isSyncing = false,
          statusMessage = "Sync error: ${e.message}"
        )
        onComplete(false, e.message ?: "Sync error")
      }
    }
  }

  fun dismissSuccessBanner() {
    _state.value = _state.value.copy(showSyncSuccessBanner = false)
  }
}
