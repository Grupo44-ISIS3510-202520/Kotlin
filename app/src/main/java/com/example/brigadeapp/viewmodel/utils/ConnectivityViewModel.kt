package com.example.brigadeapp.viewmodel.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.ObserveConnectivityUseCase
import com.example.brigadeapp.domain.utils.AnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    observeConnectivityUseCase: ObserveConnectivityUseCase
) : ViewModel() {

    val isOnline = observeConnectivityUseCase()
        .onEach { isConnected ->
            AnalyticsLogger.logConnectivityChange(isConnected)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
}
