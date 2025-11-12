package com.example.brigadeapp.domain.entity

data class CourseProgress(
    val courseId: String,
    val lastPage: Int = 0,        // index 0..3 (4 pages)
    val quizPassed: Boolean = false,
    val percent: Double = 0.0,    // 0.0 .. 1.0
    val completed: Boolean = false
)

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)
