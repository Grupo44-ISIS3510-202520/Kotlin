package com.example.brigadeapp.viewmodel.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.data.repository.RagRepository
import com.example.brigadeapp.domain.model.RagState
import com.example.brigadeapp.domain.model.RagCacheEntry
import com.example.brigadeapp.domain.usecase.GetRagAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RagViewModel @Inject constructor(
    private val getRagAnswerUseCase: GetRagAnswerUseCase,
    private val repository: RagRepository
) : ViewModel() {

    private val _ragState = MutableStateFlow<RagState>(RagState.Idle)
    val ragState: StateFlow<RagState> = _ragState.asStateFlow()

    private val _cacheSize = MutableStateFlow(0)
    val cacheSize: StateFlow<Int> = _cacheSize.asStateFlow()

    private val _cacheHistory = MutableStateFlow<List<RagCacheEntry>>(emptyList())
    val cacheHistory: StateFlow<List<RagCacheEntry>> = _cacheHistory.asStateFlow()

    init {
        initializeCache()
    }

    private fun initializeCache() {
        viewModelScope.launch {
            repository.initializeCache()
            updateCacheSize()
            loadCacheHistory()
        }
    }

    fun askQuestion(query: String) {
        viewModelScope.launch {
            getRagAnswerUseCase(query).collect { state ->
                _ragState.value = state

                if (state is RagState.Success) {
                    updateCacheSize()
                    loadCacheHistory()
                }
            }
        }
    }

    fun clearState() {
        _ragState.value = RagState.Idle
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            updateCacheSize()
            loadCacheHistory()
        }
    }

    fun loadCacheHistory() {
        viewModelScope.launch {
            val history = repository.getCacheHistory()
            _cacheHistory.value = history.sortedByDescending { it.timestamp }
        }
    }

    fun useCachedQuery(entry: RagCacheEntry) {
        _ragState.value = RagState.Success(
            answer = entry.answer,
            sources = entry.sources,
            fromCache = true
        )
    }

    private suspend fun updateCacheSize() {
        _cacheSize.value = repository.getCacheSize()
    }
}