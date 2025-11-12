package com.example.brigadeapp.data.repository

import com.example.brigadeapp.domain.repository.TrainingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
    private val auth: FirebaseAuth
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

    override suspend fun markLessonVisited(pageIndex: Int, totalLessons: Int) {
        val uid = auth.currentUser?.uid ?: return
        val ref = doc(uid)
        db.runTransaction { tx ->
            val data = tx.get(ref).data
            val curr = CprProgress.fromMap(data?.get("cpr") as? Map<*, *>)
            val newVisited = maxOf(curr.lessonsVisited, pageIndex + 1)
            val next = curr.copy(lessonsVisited = newVisited, totalLessons = totalLessons)
            tx.set(ref, mapOf("cpr" to next.toMap()), SetOptions.merge())
        }.await()
    }

    override suspend fun submitQuiz(correct: Int, total: Int) {
        val uid = auth.currentUser?.uid ?: return
        val ref = doc(uid)
        val passed = total > 0 && correct.toFloat() / total >= 0.8f
        db.runTransaction { tx ->
            val data = tx.get(ref).data
            val curr = CprProgress.fromMap(data?.get("cpr") as? Map<*, *>)
            val next = curr.copy(quizScore = correct, quizTotal = total, completed = passed)
            tx.set(ref, mapOf("cpr" to next.toMap()), SetOptions.merge())
        }.await()
    }
}
