package com.example.evfunenhancer.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * True while the device's default network has internet capability. Reacts at once to airplane
 * mode or Wi-Fi/mobile data going off; a network without real internet access still counts as
 * available, which the Firestore connection state catches. Purely local: no network traffic.
 */
fun networkAvailableFlow(context: Context): Flow<Boolean> = callbackFlow {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    fun hasInternet(caps: NetworkCapabilities?) =
        caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    trySend(hasInternet(cm.getNetworkCapabilities(cm.activeNetwork)))
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            trySend(hasInternet(caps))
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }
    cm.registerDefaultNetworkCallback(callback)
    awaitClose { cm.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()
