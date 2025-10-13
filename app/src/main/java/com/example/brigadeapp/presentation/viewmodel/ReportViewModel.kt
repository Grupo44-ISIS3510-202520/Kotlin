package com.example.brigadeapp.presentation.viewmodel

import ReportState
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.model.ReportBuilder
import com.example.brigadeapp.domain.usecase.PostReportUseCase
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReportUseCase: PostReportUseCase,
    private val analytics: FirebaseAnalytics
) : ViewModel() {

    var state by mutableStateOf(ReportState())
        internal set

    private var screenStartTime: Long = 0L

    fun startTimer() {
        screenStartTime = System.currentTimeMillis()
        Log.d("Analytic1", "Inició el tiempo")
    }

    private fun logElapsedTime(action: String) {
        val elapsed = System.currentTimeMillis() - screenStartTime
        val bundle = Bundle().apply {
            putString("action_type", action)
            putLong("elapsed_time_ms", elapsed)
        }

        Log.d("Analytic1", "Inició el tiempo")
        analytics.logEvent("report_action_time", bundle)
        Log.d("Analytics", "Tiempo registrado: $elapsed ms en acción $action")
    }

    fun submitReport(
        type: String,
        place: String,
        time: String?,
        description: String,
        followUp: Boolean,
        imageUrl: String?,
        audioUri: String?
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
                    .setImageUri(imageUrl)
                    .setAudioUri(audioUri)
                    .build()

                val result = submitReportUseCase(report)
                state = if (result.isSuccess) {
                    logElapsedTime("send_report")
                    state.copy(isLoading = false, success = true)
                } else {
                    state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onCallPressed() {
        logElapsedTime("call_button")
    }
}
