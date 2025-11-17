package com.example.brigadeapp.viewmodel.screens

import ReportState
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.ReportBuilder
import com.example.brigadeapp.domain.usecase.PostFileUseCase
import com.example.brigadeapp.domain.usecase.PostReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReportUseCase: PostReportUseCase,
    private val uploadFileUseCase: PostFileUseCase
) : ViewModel() {

    var state by mutableStateOf(ReportState())
        internal set

    fun submitReport(
        type: String,
        place: String,
        time: String?,
        description: String,
        followUp: Boolean,
        imageFile: File?,
        audioFile: File?,
        elapsedTime: Long
    ) {
        Log.d("SubmitReport", "Entró a ViewModel")
        viewModelScope.launch {

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
