package com.example.brigadeapp.data.repository

import android.util.Log
import com.example.brigadeapp.data.source.local.TrainingOutboxDataStore
import com.example.brigadeapp.domain.entity.Lesson
import com.example.brigadeapp.domain.entity.PendingTrainingUpdate
import com.example.brigadeapp.domain.entity.QuizQuestion
import com.example.brigadeapp.domain.entity.TrainingModule
import com.example.brigadeapp.domain.repository.TrainingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CprProgress(
    val lessonsVisited: Int = 0,
    val totalLessons: Int = 4,
    val quizScore: Int = 0,
    val quizTotal: Int = 0,
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap() = mapOf(
        "lessonsVisited" to lessonsVisited,
        "totalLessons" to totalLessons,
        "quizScore" to quizScore,
        "quizTotal" to quizTotal,
        "completed" to completed,
        "updatedAt" to System.currentTimeMillis()
    )
    companion object {
        fun fromMap(m: Map<*, *>?): CprProgress {
            if (m == null) return CprProgress()
            fun num(k: String) = (m[k] as? Number)?.toInt() ?: 0
            return CprProgress(
                lessonsVisited = num("lessonsVisited"),
                totalLessons   = num("totalLessons").takeIf { it > 0 } ?: 4,
                quizScore      = num("quizScore"),
                quizTotal      = num("quizTotal"),
                completed      = (m["completed"] as? Boolean) ?: false,
                updatedAt      = (m["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val outbox: TrainingOutboxDataStore
) : TrainingRepository {

    private fun doc(uid: String) = db.collection("user_trainings").document(uid)

    override fun observeCprProgress(): Flow<CprProgress> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(CprProgress()); close(); return@callbackFlow }
        val reg = doc(uid).addSnapshotListener { snap, _ ->
            val cprMap = snap?.data?.get("cpr") as? Map<*, *>
            trySend(CprProgress.fromMap(cprMap))
        }
        awaitClose { reg.remove() }
    }


    // Observes all training modules from Firestore "trainings" collection.
    // Concurrency: Uses callbackFlow + Firestore snapshot listener for real-time updates
    // Local storage: Foundation for caching layer (future enhancement)

    override fun observeTrainingModules(): Flow<List<TrainingModule>> = callbackFlow {
        Log.d("TrainingRepositoryImpl", "Starting to observe training modules from Firestore")

        val reg = db.collection("trainings")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TrainingRepositoryImpl", "Error observing trainings", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val modules = snapshot.documents.mapNotNull { doc ->
                        try {
                            TrainingModule(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                subtitle = doc.getString("subtitle") ?: "",
                                description = doc.getString("description") ?: "",
                                type = doc.getString("type") ?: "course",
                                hasQuiz = doc.getBoolean("hasQuiz") ?: false,
                                totalLessons = doc.getLong("totalLessons")?.toInt() ?: 0,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                order = doc.getLong("order")?.toInt() ?: 0,
                                isActive = doc.getBoolean("isActive") ?: true,
                                createdAt = doc.getLong("createdAt") ?: 0L,
                                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            Log.e("TrainingRepositoryImpl", "Error parsing training doc ${doc.id}", e)
                            null
                        }
                    }
                    // Filter active trainings in-memory (avoids composite index requirement)
                    val activeModules = modules.filter { it.isActive }
                    Log.d("TrainingRepositoryImpl", "Loaded ${activeModules.size} active training modules (${modules.size} total)")
                    trySend(activeModules)
                } else {
                    Log.d("TrainingRepositoryImpl", "No training modules found")
                    trySend(emptyList())
                }
            }

        awaitClose {
            Log.d("TrainingRepositoryImpl", "Closing training modules observer")
            reg.remove()
        }
    }


    override fun observeLessons(trainingId: String): Flow<List<Lesson>> = callbackFlow {
        Log.d("TrainingRepositoryImpl", "Observing lessons for training: $trainingId")

        val reg = db.collection("trainings")
            .document(trainingId)
            .collection("lessons")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TrainingRepositoryImpl", "Error observing lessons", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lessons = snapshot.documents.mapNotNull { doc ->
                        try {
                            Lesson(
                                id = doc.id,
                                order = doc.getLong("order")?.toInt() ?: 0,
                                title = doc.getString("title") ?: "",
                                content = doc.getString("content") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("TrainingRepositoryImpl", "Error parsing lesson ${doc.id}", e)
                            null
                        }
                    }
                    Log.d("TrainingRepositoryImpl", "Loaded ${lessons.size} lessons for $trainingId")
                    trySend(lessons)
                } else {
                    Log.d("TrainingRepositoryImpl", "No lessons found for $trainingId")
                    trySend(emptyList())
                }
            }

        awaitClose {
            Log.d("TrainingRepositoryImpl", "Closing lessons observer for $trainingId")
            reg.remove()
        }
    }


    override fun observeQuizQuestions(trainingId: String): Flow<List<QuizQuestion>> = callbackFlow {
        Log.d("TrainingRepositoryImpl", "Observing quiz questions for training: $trainingId")

        val reg = db.collection("trainings")
            .document(trainingId)
            .collection("quiz_questions")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TrainingRepositoryImpl", "Error observing quiz questions", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val questions = snapshot.documents.mapNotNull { doc ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val optionsList = doc.get("options") as? List<String> ?: emptyList()

                            QuizQuestion(
                                id = doc.id,
                                order = doc.getLong("order")?.toInt() ?: 0,
                                text = doc.getString("text") ?: "",
                                options = optionsList,
                                correctIndex = doc.getLong("correctIndex")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            Log.e("TrainingRepositoryImpl", "Error parsing quiz question ${doc.id}", e)
                            null
                        }
                    }
                    Log.d("TrainingRepositoryImpl", "Loaded ${questions.size} quiz questions for $trainingId")
                    trySend(questions)
                } else {
                    Log.d("TrainingRepositoryImpl", "No quiz questions found for $trainingId")
                    trySend(emptyList())
                }
            }

        awaitClose {
            Log.d("TrainingRepositoryImpl", "Closing quiz questions observer for $trainingId")
            reg.remove()
        }
    }


    override suspend fun markLessonVisited(pageIndex: Int, totalLessons: Int) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val ref = doc(uid)

        try {
            // Attempt immediate Firestore write
            db.runTransaction { tx ->
                val data = tx.get(ref).data
                val curr = CprProgress.fromMap(data?.get("cpr") as? Map<*, *>)
                val newVisited = maxOf(curr.lessonsVisited, pageIndex + 1)
                val next = curr.copy(lessonsVisited = newVisited, totalLessons = totalLessons)
                tx.set(ref, mapOf("cpr" to next.toMap()), SetOptions.merge())
            }.await()
            Log.d("TrainingRepositoryImpl", "Lesson $pageIndex marked as visited")
        } catch (e: Exception) {
            // [Eventual connectivity] Write failed - add to outbox for later retry
            Log.w("TrainingRepositoryImpl", "Failed to mark lesson visited, adding to outbox: ${e.message}")
            val pendingUpdate = PendingTrainingUpdate.lessonProgress(pageIndex, totalLessons)
            outbox.addPendingUpdate(pendingUpdate)
        }
    }


    override suspend fun submitQuiz(correct: Int, total: Int) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val ref = doc(uid)
        val passed = total > 0 && correct.toFloat() / total >= 0.8f

        try {
            // Attempt immediate Firestore write
            db.runTransaction { tx ->
                val data = tx.get(ref).data
                val curr = CprProgress.fromMap(data?.get("cpr") as? Map<*, *>)
                val next = curr.copy(quizScore = correct, quizTotal = total, completed = passed)
                tx.set(ref, mapOf("cpr" to next.toMap()), SetOptions.merge())
            }.await()
            Log.d("TrainingRepositoryImpl", "Quiz submitted: $correct/$total (passed=$passed)")
        } catch (e: Exception) {
            // Eventual connectivity write failed: it adds to outbox for later retry
            Log.w("TrainingRepositoryImpl", "Failed to submit quiz, adding to outbox: ${e.message}")
            val pendingUpdate = PendingTrainingUpdate.quizResult(correct, total)
            outbox.addPendingUpdate(pendingUpdate)
        }
    }


    override suspend fun flushPendingUpdates(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pending = outbox.readPendingUpdates()

            if (pending.isEmpty()) {
                Log.d("TrainingRepositoryImpl", "No pending updates to flush")
                return@withContext Result.success(Unit)
            }

            Log.d("TrainingRepositoryImpl", "Flushing ${pending.size} pending updates")

            for (update in pending) {
                try {
                    // Apply the update based on type
                    when (update.type) {
                        PendingTrainingUpdate.UpdateType.LESSON_PROGRESS -> {
                            val pageIndex = update.pageIndex ?: continue
                            val totalLessons = update.totalLessons ?: continue

                            val uid = auth.currentUser?.uid ?: continue
                            val ref = doc(uid)
                            db.runTransaction { tx ->
                                val data = tx.get(ref).data
                                val curr = CprProgress.fromMap(data?.get("cpr") as? Map<*, *>)
                                val newVisited = maxOf(curr.lessonsVisited, pageIndex + 1)
                                val next = curr.copy(lessonsVisited = newVisited, totalLessons = totalLessons)
                                tx.set(ref, mapOf("cpr" to next.toMap()), SetOptions.merge())
                            }.await()

                            Log.d("TrainingRepositoryImpl", "Flushed lesson progress: ${update.id}")
                        }

                        PendingTrainingUpdate.UpdateType.QUIZ_RESULT -> {
                            val correct = update.quizCorrect ?: continue
                            val total = update.quizTotal ?: continue

                            val uid = auth.currentUser?.uid ?: continue
                            val ref = doc(uid)
                            val passed = total > 0 && correct.toFloat() / total >= 0.8f

                            db.runTransaction { tx ->
                                val data = tx.get(ref).data
                                val curr = CprProgress.fromMap(data?.get("cpr") as? Map<*, *>)
                                val next = curr.copy(quizScore = correct, quizTotal = total, completed = passed)
                                tx.set(ref, mapOf("cpr" to next.toMap()), SetOptions.merge())
                            }.await()

                            Log.d("TrainingRepositoryImpl", "Flushed quiz result: ${update.id}")
                        }
                    }

                    // Successfully applied - remove from outbox
                    outbox.removePendingUpdate(update.id)

                } catch (e: Exception) {
                    // Update failed - stop flushing to avoid loops
                    Log.w("TrainingRepositoryImpl", "Failed to flush update ${update.id}, will retry later: ${e.message}")
                    return@withContext Result.failure(e)
                }
            }

            Log.d("TrainingRepositoryImpl", "Successfully flushed all ${pending.size} pending updates")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TrainingRepositoryImpl", "Error flushing pending updates", e)
            Result.failure(e)
        }
    }
}
