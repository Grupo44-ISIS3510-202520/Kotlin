package com.example.brigadeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.domain.usecase.GetUpdatedProtocolsUseCase // si lo usas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProtocolsViewModel(
    private val getLightLevel: GetLightLevelUseCase,
    private val getUpdatedProtocols: GetUpdatedProtocolsUseCase? = null, // opcional
    private val now: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    // --- Sensor de luz ---
    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux

    private val _readingMode = MutableStateFlow(false)
    val readingMode: StateFlow<Boolean> = _readingMode

    // --- (Opcional) protocolos actualizados ---
    private val _updatedCount = MutableStateFlow(0)
    val updatedCount: StateFlow<Int> = _updatedCount

    private var lastSessionTime: Long = now() - 24L * 60 * 60 * 1000 // simula sesión pasada

    init {
        observeLightSensor()
        // si tienes el use case de updates, calcula una vez:
        getUpdatedProtocols?.let { checkUpdates() }
    }

    private fun observeLightSensor() {
        viewModelScope.launch {
            getLightLevel().collect { value ->
                _lux.value = value
                _readingMode.value = value < 15f // umbral simple para “modo lectura”
            }
        }
    }

    fun checkUpdates() {
        val useCase = getUpdatedProtocols ?: return
        viewModelScope.launch {
            val count = useCase(lastSessionTime) // tu use-case devuelve Int
            _updatedCount.value = count
            lastSessionTime = now()
        }
    }
}
