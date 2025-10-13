package com.example.brigadeapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.PostFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val uploadFileUseCase: PostFileUseCase
): ViewModel() {

    fun uploadFile(file: File,
                   bucket: String,
                   blob: String,
                   onFileSaved: (String?) -> Unit) {

        viewModelScope.launch {
            try {
                val result = uploadFileUseCase(file, bucket, blob)
                result.onSuccess { url ->
                    val urlResult = url
                    Log.i("GCP", "Link en el success ViewModel: $urlResult")
                    onFileSaved(urlResult)
                }.onFailure {
                    throw Exception("Error al cargar el Archivo")
                }
            } catch (e: Exception) {
                throw Exception("Error al cargar el Archivo")
            }
        }
    }
}