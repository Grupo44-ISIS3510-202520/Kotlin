package com.example.brigadeapp.viewmodel.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.config.PreloadConfig
import com.example.brigadeapp.domain.config.PreloadDecision
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.domain.usecase.GetUpdatedProtocolsUseCase
import com.example.brigadeapp.domain.utils.CachedFileDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProtocolsViewModel @Inject constructor(
    private val getLightLevel: GetLightLevelUseCase,
    private val getUpdatedProtocolsUseCase: GetUpdatedProtocolsUseCase,
    private val repository: ProtocolRepository,
    private val cachedFileDownloader: CachedFileDownloader,
    private val preloadConfig: PreloadConfig
) : ViewModel() {

    companion object {
        private const val TAG = "ProtocolsViewModel"

        private val HIGH_PRIORITY_PROTOCOLS = listOf(

            "4 - Desmayos, convulsiones y Heimlich"
        )

        private const val PRELOAD_DELAY_MS = 3000L
        private const val DOWNLOAD_INTERVAL_MS = 500L
        private const val MAX_PRELOAD_COUNT = 10
    }

    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux

    private val _readingMode = MutableStateFlow(false)
    val readingMode: StateFlow<Boolean> = _readingMode

    private val _protocols = MutableStateFlow<List<Protocol>>(emptyList())
    val protocols: StateFlow<List<Protocol>> = _protocols

    private val _updatedProtocols = MutableStateFlow<List<Protocol>>(emptyList())
    val updatedProtocols: StateFlow<List<Protocol>> = _updatedProtocols

    private val _updatedCount = MutableStateFlow(0)
    val updatedCount: StateFlow<Int> = _updatedCount

    private val _currentPdfFile = MutableStateFlow<File?>(null)
    val currentPdfFile: StateFlow<File?> = _currentPdfFile

    private val _isLoadingPdf = MutableStateFlow(false)
    val isLoadingPdf: StateFlow<Boolean> = _isLoadingPdf

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _openFileEvent = Channel<File>()
    val openFileEvent = _openFileEvent.receiveAsFlow()

    private val _preloadStatus = MutableStateFlow<PreloadStatus>(PreloadStatus.Idle)
    val preloadStatus: StateFlow<PreloadStatus> = _preloadStatus

    init {
        observeLightSensor()
        loadProtocolsAndCheckUpdates()
        scheduleSmartPreload()
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
            try {
                val localVersions = repository.readLocalVersions()
                val allRemoteProtocols = repository.getAllProtocols()
                _protocols.value = allRemoteProtocols

                val updatedList = getUpdatedProtocolsUseCase(localVersions)
                _updatedProtocols.value = updatedList
                _updatedCount.value = updatedList.size

                val newLocalVersions = allRemoteProtocols.associate { it.name to it.version }
                repository.saveLocalVersions(newLocalVersions)

                Log.d(TAG, "Protocols loaded: ${allRemoteProtocols.size}, Updated: ${updatedList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading protocols", e)
                _errorMessage.value = "Error loading protocols"
            }
        }
    }

    fun markProtocolAsRead(protocolName: String) {
        viewModelScope.launch {
            val currentUpdatedList = _updatedProtocols.value.toMutableList()
            val wasRemoved = currentUpdatedList.removeAll { it.name == protocolName }

            if (wasRemoved) {
                _updatedProtocols.value = currentUpdatedList
                _updatedCount.value = currentUpdatedList.size
                Log.d(TAG, "Protocol marked as read: $protocolName")
            }
        }
    }

    fun downloadProtocolForViewing(protocol: Protocol) {
        viewModelScope.launch {
            _isLoadingPdf.value = true
            _errorMessage.value = null

            val fileName = sanitizeFileName(protocol.name, protocol.version)
            val result = cachedFileDownloader.downloadFile(protocol.url, fileName)

            result.fold(
                onSuccess = { file ->
                    _currentPdfFile.value = file
                    _openFileEvent.send(file)
                    markProtocolAsRead(protocol.name)
                    preloadRelatedProtocols(protocol)
                },
                onFailure = { error ->
                    _errorMessage.value = "Protocol not available offline"
                    Log.e(TAG, "Failed: ${protocol.name}", error)
                }
            )

            _isLoadingPdf.value = false
        }
    }

    fun getCacheSize(): String {
        val sizeInMB = cachedFileDownloader.getCacheSize() / (1024.0 * 1024.0)
        return "%.2f MB".format(sizeInMB)
    }

    fun clearCache() {
        viewModelScope.launch {
            cachedFileDownloader.clearCache()
            Log.d(TAG, "Cache cleared")
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearCurrentPdf() {
        _currentPdfFile.value = null
    }

    private fun scheduleSmartPreload() {
        viewModelScope.launch {
            try {
                delay(preloadConfig.preloadDelayMs)
                protocols.first { it.isNotEmpty() }

                when (val decision = preloadConfig.shouldPreload()) {
                    is PreloadDecision.Allowed -> {
                        startSmartPreload()
                    }
                    is PreloadDecision.Denied -> {
                        _preloadStatus.value = PreloadStatus.Idle
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in smart preload", e)
            }
        }
    }

    private suspend fun startSmartPreload() {
        val protocolsToPreload = buildPreloadList()

        if (protocolsToPreload.isEmpty()) {
            return
        }

        preloadProtocols(protocolsToPreload)
    }

    private fun buildPreloadList(): List<Protocol> {
        val toPreload = mutableListOf<Protocol>()
        val allProtocols = _protocols.value

        val updated = _updatedProtocols.value
        toPreload.addAll(updated)

        val highPriorityToAdd = allProtocols.filter { protocol ->
            HIGH_PRIORITY_PROTOCOLS.any { it.equals(protocol.name, ignoreCase = true) } &&
                    protocol !in toPreload
        }
        toPreload.addAll(highPriorityToAdd)

        val limitedList = toPreload.take(preloadConfig.maxPreloadCount)
        return limitedList
    }

    private suspend fun preloadProtocols(protocols: List<Protocol>) {
        _preloadStatus.value = PreloadStatus.InProgress(0, protocols.size)

        var downloaded = 0
        var alreadyCached = 0

        protocols.forEachIndexed { index, protocol ->
            try {
                val fileName = sanitizeFileName(protocol.name, protocol.version)
                val result = cachedFileDownloader.downloadFile(protocol.url, fileName)

                result.fold(
                    onSuccess = {
                        downloaded++
                    },
                    onFailure = { error ->
                        if (error.message?.contains("already exists") == true) {
                            alreadyCached++
                        }
                    }
                )

                _preloadStatus.value = PreloadStatus.InProgress(index + 1, protocols.size)

                if (index < protocols.size - 1) {
                    delay(DOWNLOAD_INTERVAL_MS)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading ${protocol.name}", e)
            }
        }

        val total = downloaded + alreadyCached
        _preloadStatus.value = PreloadStatus.Completed(total)
    }

    private fun preloadRelatedProtocols(currentProtocol: Protocol) {
        viewModelScope.launch {
            try {
                val relatedProtocolsMap = mapOf(
                    "1 - Primer Respondiente" to listOf("2 - SCI", "3 - Apoyo emocional y autocuidado"),
                    "2 - SCI" to listOf("1 - Primer Respondiente"),
                    "3 - Apoyo emocional y autocuidado" to listOf("1 - Primer Respondiente", "2 - SCI"),
                    "4 - Desmayos, convulsiones y Heimlich" to listOf("1 - Primer Respondiente")
                )

                val relatedNames = relatedProtocolsMap[currentProtocol.name] ?: return@launch

                val protocolsToPreload = _protocols.value.filter { protocol ->
                    relatedNames.any { it.equals(protocol.name, ignoreCase = true) }
                }

                protocolsToPreload.forEach { protocol ->
                    delay(1000)
                    val fileName = sanitizeFileName(protocol.name, protocol.version)
                    cachedFileDownloader.downloadFile(protocol.url, fileName)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in predictive preload", e)
            }
        }
    }

    private fun sanitizeFileName(name: String, version: String): String {
        val sanitizedName = name.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(50)
        val sanitizedVersion = version.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(10)
        return "${sanitizedName}_v${sanitizedVersion}.pdf"
    }
}

sealed class PreloadStatus {
    object Idle : PreloadStatus()
    data class InProgress(val current: Int, val total: Int) : PreloadStatus()
    data class Completed(val downloaded: Int) : PreloadStatus()
}
