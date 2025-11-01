package com.example.brigadeapp.viewmodel.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.domain.usecase.GetUpdatedProtocolsUseCase
import com.example.brigadeapp.domain.utils.CachedFileDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProtocolsViewModel @Inject constructor(
    private val getLightLevel: GetLightLevelUseCase,
    private val getUpdatedProtocols: GetUpdatedProtocolsUseCase,
    private val repo: ProtocolRepository,
    private val cachedFileDownloader: CachedFileDownloader
) : ViewModel() {

    companion object {
        private const val TAG = "ProtocolsViewModel"
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

    init {
        observeLightSensor()
        loadProtocolsAndCheckUpdates()
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
                val localVersions = repo.readLocalVersions()
                val allRemoteProtocols = repo.getAllProtocols()
                _protocols.value = allRemoteProtocols

                val updatedList = getUpdatedProtocols(localVersions)
                _updatedProtocols.value = updatedList
                _updatedCount.value = updatedList.size

                val newLocalVersions = allRemoteProtocols.associate { it.name to it.version }
                repo.saveLocalVersions(newLocalVersions)

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

            Log.d(TAG, "Opening protocol: ${protocol.name}")

            val fileName = sanitizeFileName(protocol.name, protocol.version)
            val result = cachedFileDownloader.downloadFile(protocol.url, fileName)

            result.fold(
                onSuccess = { file ->
                    _currentPdfFile.value = file
                    Log.d(TAG, "Protocol ready: ${protocol.name}")
                    _openFileEvent.send(file)
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

    private fun sanitizeFileName(name: String, version: String): String {
        val sanitizedName = name.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(50)
        val sanitizedVersion = version.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(10)
        return "${sanitizedName}_v${sanitizedVersion}.pdf"
    }
}
