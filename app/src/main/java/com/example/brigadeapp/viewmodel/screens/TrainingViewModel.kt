package com.example.brigadeapp.viewmodel.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.data.repository.CprProgress
import com.example.brigadeapp.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val repo: TrainingRepository
) : ViewModel() {

    val cprProgress: StateFlow<CprProgress> =
        repo.observeCprProgress()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CprProgress())

    fun onVisitedPage(pageIndex: Int, totalPages: Int) {
        viewModelScope.launch { repo.markLessonVisited(pageIndex, totalPages) }
    }

    fun onQuizSubmitted(correct: Int, total: Int) {
        viewModelScope.launch { repo.submitQuiz(correct, total) }
    }
}
