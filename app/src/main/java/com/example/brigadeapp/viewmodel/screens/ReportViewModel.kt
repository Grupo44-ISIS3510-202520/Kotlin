package com.example.brigadeapp.viewmodel.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.brigadeapp.domain.entity.ReportBuilder
import javax.inject.Provider
import com.example.brigadeapp.domain.usecase.PostFileUseCase
import com.example.brigadeapp.domain.usecase.PostReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReportUseCase: PostReportUseCase,
    private val uploadFileUseCase: PostFileUseCase,
    private val reportBuilderProvider: Provider<ReportBuilder>
) : ViewModel() {

    var state by mutableStateOf(ReportState())
        internal set

    fun startSubmitting() {
        state = state.copy(isLoading = true, error = null)
    }

    suspend fun submitReport(
        type: String,
        place: String,
        time: String?,
        description: String,
        followUp: Boolean,
        imageFile: File?,
        audioFile: File?,
        elapsedTime: Long,
        userId: String
    ) {
        Log.d("SubmitReport", "Entró a ViewModel")

        var imageUrl by mutableStateOf<String?>(null)
        var audioUrl by mutableStateOf<String?>(null)

        if (imageFile != null) {
            try {
                val result = uploadFileUseCase(
                    imageFile,
                    "brigadeapp-report-images",
                    "${System.currentTimeMillis()}.jpg"
                )
                result.onSuccess {
                    imageUrl = it
                }

            } catch (e: Exception){
                state = state.copy(isLoading = false, error = "Image cannot be uploaded")
                Log.e("Upload Image", e.message.toString())
            }
        }

        if (audioFile != null) {
            try {
                val result = uploadFileUseCase(
                    audioFile,
                    "brigadeapp-report-audios",
                    "${System.currentTimeMillis()}.mp3"
                )

                result.onSuccess {
                    audioUrl = it
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = "Audio cannot be uploaded")
                Log.e("Upload Audio", e.message.toString())
            }
        }

        try {
            state = state.copy(isLoading = true, error = null)

            val report = reportBuilderProvider.get()
                .setType(type)
                .setPlace(place)
                .setTime(time)
                .setDescription(description)
                .setFollowUp(followUp)
                .setImageUrl(imageUrl)
                .setAudioUrl(audioUrl)
                .setElapsedTime(elapsedTime)
                .setUserId(userId)
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
