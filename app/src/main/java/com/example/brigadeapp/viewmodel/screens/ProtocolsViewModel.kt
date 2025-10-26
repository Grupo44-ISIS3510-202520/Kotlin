package com.example.brigadeapp.viewmodel.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.domain.usecase.GetUpdatedProtocolsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProtocolsViewModel @Inject constructor(
    private val getLightLevel: GetLightLevelUseCase,
    private val getUpdatedProtocols: GetUpdatedProtocolsUseCase
) : ViewModel() {

    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux

    private val _readingMode = MutableStateFlow(false)
    val readingMode: StateFlow<Boolean> = _readingMode

    private val _updatedCount = MutableStateFlow(0)
    val updatedCount: StateFlow<Int> = _updatedCount

    private val _protocols = MutableStateFlow<List<Protocol>>(emptyList())
    val protocols: StateFlow<List<Protocol>> = _protocols

    private var lastSessionTime = System.currentTimeMillis() - 86400000

    init {
        observeLightSensor()
        loadProtocols()
        checkUpdates()
    }

    private fun observeLightSensor() {
        viewModelScope.launch {
            getLightLevel().collect { value ->
                _lux.value = value
                _readingMode.value = value < 30f
            }
        }
    }

    private fun loadProtocols() {
        viewModelScope.launch {
            _protocols.value = getUpdatedProtocols.repo.getProtocols()
        }
    }

    fun checkUpdates() {
        viewModelScope.launch {
            val count = getUpdatedProtocols(lastSessionTime)
            _updatedCount.value = count
            lastSessionTime = System.currentTimeMillis()
        }
    }
}
