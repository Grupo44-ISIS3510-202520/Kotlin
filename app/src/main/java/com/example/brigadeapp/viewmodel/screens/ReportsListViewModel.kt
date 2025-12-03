package com.example.brigadeapp.viewmodel.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.data.services.local.SyncPreferencesService
import com.example.brigadeapp.data.source.local.sensors.ConnectivityManagerObserver
import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.domain.usecase.GetReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ReportsListState(
    val reports: List<CachedReport> = emptyList(),
    val pendingReports: List<CachedReport> = emptyList(),
    val syncedReports: List<CachedReport> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastSyncTime: String = "",
    val dataAge: String = "",
    val isOnline: Boolean = false
)

@HiltViewModel
class ReportsListViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase,
    private val syncPreferences: SyncPreferencesService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var state by mutableStateOf(ReportsListState())
        private set

    init {
        observeReportsRealtime()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            ConnectivityManagerObserver(context).observe().collect { isOnline ->
                state = state.copy(isOnline = isOnline)
            }
        }
    }

    private fun observeReportsRealtime() {
        viewModelScope.launch {
            getReportsUseCase.observe(limit = 10).collect { result ->
                if (result.isSuccess) {
                    val allReports = result.getOrNull() ?: emptyList()
                    val pending = allReports.filter { !it.synced }
                    val synced = allReports.filter { it.synced }
                    
                    state = state.copy(
                        reports = allReports,
                        pendingReports = pending,
                        syncedReports = synced,
                        isLoading = false,
                        error = null,
                        lastSyncTime = formatLastSyncTime(),
                        dataAge = calculateDataAge()
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun formatLastSyncTime(): String {
        val timestamp = syncPreferences.getLastSyncTime()
        if (timestamp == 0L) return "Never"
        
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    private fun calculateDataAge(): String {
        val timestamp = syncPreferences.getLastSyncTime()
        if (timestamp == 0L) return "Unknown"
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            else -> "$days day${if (days > 1) "s" else ""} ago"
        }
    }
}
