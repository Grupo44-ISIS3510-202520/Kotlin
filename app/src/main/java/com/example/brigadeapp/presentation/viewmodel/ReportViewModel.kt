package com.example.brigadeapp.presentation.viewmodel

import ReportState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.model.ReportBuilder
import com.example.brigadeapp.domain.usecase.SubmitReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReportUseCase: SubmitReportUseCase
) : ViewModel() {

    var state by mutableStateOf(ReportState())
        private set

    fun submitReport(
        type: String,
        place: String,
        time: String?,
        description: String,
        followUp: Boolean,
        imageUri: String?,
        audioUri: String?
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)

                val report = ReportBuilder()
                    .setType(type)
                    .setPlace(place)
                    .setTime(time)
                    .setDescription(description)
                    .setFollowUp(followUp)
                    .setImageUri(imageUri)
                    .setAudioUri(audioUri)
                    .build()

                val result = submitReportUseCase(report)
                state = if (result.isSuccess) {
                    state.copy(isLoading = false, success = true)
                } else {
                    state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

}
