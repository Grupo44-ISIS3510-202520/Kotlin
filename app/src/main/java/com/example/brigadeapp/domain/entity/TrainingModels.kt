package com.example.brigadeapp.domain.entity

data class CourseProgress(
    val courseId: String,
    val lastPage: Int = 0,
    val quizPassed: Boolean = false,
    val percent: Double = 0.0,
    val completed: Boolean = false
)


data class Lesson(
    val id: String = "",
    val order: Int = 0,
    val title: String = "",
    val content: String = ""
)


data class QuizQuestion(
    val id: String = "",
    val order: Int = 0,
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0
)


data class TrainingModule(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val type: String = "course", // "course" or "quiz_only"
    val hasQuiz: Boolean = false,
    val totalLessons: Int = 0,
    val imageUrl: String = "",
    val order: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
