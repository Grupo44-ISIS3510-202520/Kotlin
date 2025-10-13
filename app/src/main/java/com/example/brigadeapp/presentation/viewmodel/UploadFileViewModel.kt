package com.example.brigadeapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.PostFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val uploadFileUseCase: PostFileUseCase
): ViewModel() {

    suspend fun uploadFile(
        file: File,
        bucket: String,
        blob: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val result = uploadFileUseCase(file, bucket, blob)
            result.getOrThrow().also { url ->
                Log.i("GCP", "Link en el success ViewModel: $url")
            }
        } catch (e: Exception) {
            Log.e("GCP", "Error al cargar el archivo", e)
            throw Exception("No se pudo cargar la imagen")
        }
    }

}