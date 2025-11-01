package com.example.brigadeapp.viewmodel.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.IsInsideCampusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val isInsideCampusUseCase: IsInsideCampusUseCase
) : ViewModel() {

    private val _isInsideCampus = MutableStateFlow(false)
    val isInsideCampus = _isInsideCampus.asStateFlow()

    init {
        checkLocation()
    }

    private fun checkLocation() {
        viewModelScope.launch {
            _isInsideCampus.value = isInsideCampusUseCase()
        }
    }
}