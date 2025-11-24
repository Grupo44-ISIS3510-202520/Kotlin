package com.example.brigadeapp.domain.entity


data class PendingTrainingUpdate(
    val id: String,
    val type: UpdateType,
    val timestamp: Long,
    val pageIndex: Int? = null,
    val totalLessons: Int? = null,
    val quizCorrect: Int? = null,
    val quizTotal: Int? = null
) {
    enum class UpdateType {
        LESSON_PROGRESS,
        QUIZ_RESULT
    }

    companion object {
        fun lessonProgress(pageIndex: Int, totalLessons: Int): PendingTrainingUpdate {
            return PendingTrainingUpdate(
                id = "lesson_${pageIndex}_${System.currentTimeMillis()}",
                type = UpdateType.LESSON_PROGRESS,
                timestamp = System.currentTimeMillis(),
                pageIndex = pageIndex,
                totalLessons = totalLessons
            )
        }

        fun quizResult(correct: Int, total: Int): PendingTrainingUpdate {
            return PendingTrainingUpdate(
                id = "quiz_${correct}_${total}_${System.currentTimeMillis()}",
                type = UpdateType.QUIZ_RESULT,
                timestamp = System.currentTimeMillis(),
                quizCorrect = correct,
                quizTotal = total
            )
        }
    }
}
