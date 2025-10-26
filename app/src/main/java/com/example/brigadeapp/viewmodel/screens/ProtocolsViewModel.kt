package com.example.brigadeapp.viewmodel.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository
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
    private val getUpdatedProtocols: GetUpdatedProtocolsUseCase, //
    private val repo: ProtocolRepository //
) : ViewModel() {

    // --- SENSOR ---
    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux

    private val _readingMode = MutableStateFlow(false)
    val readingMode: StateFlow<Boolean> = _readingMode

    // --- FIREBASE DATA ---
    private val _protocols = MutableStateFlow<List<Protocol>>(emptyList())
    val protocols: StateFlow<List<Protocol>> = _protocols

    private val _updatedProtocols = MutableStateFlow<List<Protocol>>(emptyList())
    val updatedProtocols: StateFlow<List<Protocol>> = _updatedProtocols

    private val _updatedCount = MutableStateFlow(0)
    val updatedCount: StateFlow<Int> = _updatedCount

    private var localVersions = mutableMapOf<String, Int>()

    init {
        observeLightSensor()
    }

    private fun observeLightSensor() {
        viewModelScope.launch {
            getLightLevel().collect { value ->
                _lux.value = value
                _readingMode.value = value < 25f
            }
        }
    }

    fun loadProtocolsAndCheckUpdates() {
        viewModelScope.launch {
            val allRemoteProtocols = repo.getAllProtocols() //
            _protocols.value = allRemoteProtocols

            val updatedList = getUpdatedProtocols(localVersions) //
            _updatedProtocols.value = updatedList
            _updatedCount.value = updatedList.size

            localVersions = allRemoteProtocols.associate { it.name to it.version }.toMutableMap()
        }
    }

    fun markProtocolAsRead(protocolName: String) {
        viewModelScope.launch {
            val currentUpdatedList = _updatedProtocols.value.toMutableList()
            val wasRemoved = currentUpdatedList.removeAll { it.name == protocolName }

            if (wasRemoved) {
                _updatedProtocols.value = currentUpdatedList
                _updatedCount.value = currentUpdatedList.size
            }
        }
    }
}