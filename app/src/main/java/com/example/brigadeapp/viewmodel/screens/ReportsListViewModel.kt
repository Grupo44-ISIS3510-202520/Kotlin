package com.example.brigadeapp.viewmodel.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.domain.usecase.GetReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsListState(
    val reports: List<CachedReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportsListViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase
) : ViewModel() {

    var state by mutableStateOf(ReportsListState())
        private set

    init {
        observeReportsRealtime()
    }

    private fun observeReportsRealtime() {
        viewModelScope.launch {
            getReportsUseCase.observe(limit = 10).collect { result ->
                state = if (result.isSuccess) {
                    state.copy(
                        reports = result.getOrNull() ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}
