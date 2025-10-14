package com.example.brigadeapp.viewmodel.screens

import ReportState
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.model.domain.ReportBuilder
import com.example.brigadeapp.model.usecase.PostReportUseCase
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReportUseCase: PostReportUseCase,
) : ViewModel() {

    var state by mutableStateOf(ReportState())
        internal set

    fun submitReport(
        type: String,
        place: String,
        time: String?,
        description: String,
        followUp: Boolean,
        imageUrl: String?,
        audioUrl: String?,
        elapsedTime: Long
    ) {
        Log.d("SubmitReport", "Entró a ViewModel")
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)

                val report = ReportBuilder()
                    .setType(type)
                    .setPlace(place)
                    .setTime(time)
                    .setDescription(description)
                    .setFollowUp(followUp)
                    .setImageUrl(imageUrl)
                    .setAudioUrl(audioUrl)
                    .setElapsedTime(elapsedTime)
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
