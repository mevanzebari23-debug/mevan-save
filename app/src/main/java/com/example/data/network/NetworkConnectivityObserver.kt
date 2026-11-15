package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class NetworkStatus {
  ONLINE,
  OFFLINE,
  CONNECTING
}

/**
 * Real-time Android Network Connectivity Observer using ConnectivityManager.NetworkCallback.
 * Detects online/offline network transitions and emits reactive state flows.
 */
class NetworkConnectivityObserver(context: Context) {

  private val connectivityManager =
    context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

  /**
   * Flow of NetworkStatus emitting ONLINE or OFFLINE in real-time.
   */
  val networkStatusFlow: Flow<NetworkStatus> = callbackFlow {
    val cm = connectivityManager
    if (cm == null) {
      trySend(NetworkStatus.ONLINE)
      close()
      return@callbackFlow
    }

    // Emit initial status immediately
    val isInitiallyOnline = isCurrentlyOnline()
    trySend(if (isInitiallyOnline) NetworkStatus.ONLINE else NetworkStatus.OFFLINE)

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        Log.d("ConnectivityObserver", "Network onAvailable: Connectivity restored")
        trySend(NetworkStatus.ONLINE)
      }

      override fun onLost(network: Network) {
        Log.d("ConnectivityObserver", "Network onLost: Disconnected from network")
        trySend(NetworkStatus.OFFLINE)
      }

      override fun onUnavailable() {
        Log.d("ConnectivityObserver", "Network onUnavailable")
        trySend(NetworkStatus.OFFLINE)
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isOnline = hasInternet && isValidated
        Log.d("ConnectivityObserver", "CapabilitiesChanged: hasInternet=$hasInternet, isValidated=$isValidated")
        trySend(if (isOnline) NetworkStatus.ONLINE else NetworkStatus.OFFLINE)
      }
    }

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    try {
      cm.registerNetworkCallback(request, callback)
    } catch (e: Exception) {
      Log.w("ConnectivityObserver", "Failed to register network callback: ${e.message}")
    }

    awaitClose {
      try {
        cm.unregisterNetworkCallback(callback)
      } catch (_: Exception) {}
    }
  }.distinctUntilChanged()

  /**
   * Synchronous check for current internet capability
   */
  fun isCurrentlyOnline(): Boolean {
    val cm = connectivityManager ?: return true
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
