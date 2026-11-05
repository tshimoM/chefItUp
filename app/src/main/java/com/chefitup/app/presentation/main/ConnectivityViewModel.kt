package com.chefitup.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefitup.app.data.remote.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    networkMonitor: NetworkMonitor
) : ViewModel() {
    val isOffline: StateFlow<Boolean> = networkMonitor.connectivity
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), !networkMonitor.isOnline)
}
