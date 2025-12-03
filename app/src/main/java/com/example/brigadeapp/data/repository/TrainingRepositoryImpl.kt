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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.example.brigadeapp.data.source.local.leaderboard.LeaderboardDao
import com.example.brigadeapp.data.source.local.leaderboard.LeaderboardEntryEntity
import com.example.brigadeapp.domain.entity.LeaderboardEntry
import com.example.brigadeapp.domain.entity.LeaderboardSnapshot
import com.example.brigadeapp.domain.entity.Timeframe


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
    private val outbox: TrainingOutboxDataStore,
    private val leaderboardDao: LeaderboardDao
) : TrainingRepository {

    // --- Leaderboard cache (in-memory + TTL) ---
    private val leaderboardMemoryCache =
        mutableMapOf<Timeframe, List<LeaderboardEntry>>()

    private val lastRemoteRefreshMillis =
        mutableMapOf<Timeframe, Long>()

    // 60 seconds TTL
    private val leaderboardTtlMillis = 60_000L

    private fun LeaderboardEntryEntity.toDomain(): LeaderboardEntry =
        LeaderboardEntry(
            userId = userId,
            displayName = displayName,
            emailPrefix = emailPrefix,
            totalCompleted = totalCompleted,
            weeklyCompleted = weeklyCompleted
        )

    private fun LeaderboardEntry.toEntity(
        timeframe: Timeframe,
        lastUpdatedMillis: Long
    ): LeaderboardEntryEntity =
        LeaderboardEntryEntity(
            userId = userId,
            displayName = displayName,
            emailPrefix = emailPrefix,
            totalCompleted = totalCompleted,
            weeklyCompleted = weeklyCompleted,
            timeframe = timeframe.name,
            lastUpdatedMillis = lastUpdatedMillis
        )


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


    override suspend fun markLessonVisited(trainingId: String, pageIndex: Int, totalLessons: Int) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val userTrainingRef = db.collection("user_trainings").document(uid)

        try {
            // Get current progress
            val currentDoc = userTrainingRef.get().await()
            val trainingData = currentDoc.data?.get(trainingId) as? Map<*, *>
            val currentCompleted = (trainingData?.get("completed") as? Boolean) ?: false
            val currentQuizScore = (trainingData?.get("quizScore") as? Number)?.toInt() ?: 0
            val currentQuizTotal = (trainingData?.get("quizTotal") as? Number)?.toInt() ?: 0
            val currentLessonsVisited = (trainingData?.get("lessonsVisited") as? Number)?.toInt() ?: 0
            val currentQuizVisited = (trainingData?.get("quizVisited") as? Boolean) ?: false
            
            // Only update if this is a NEW highest lesson reached
            // pageIndex + 1 because pageIndex is 0-based (lesson 0 = first lesson = 1 visited)
            val newVisited = maxOf(currentLessonsVisited, pageIndex + 1)
            
            // Cap at totalLessons to prevent going over
            val cappedVisited = minOf(newVisited, totalLessons)
            
            // Only write if there's an actual change
            if (cappedVisited != currentLessonsVisited) {
                val updatedTrainingData = mapOf(
                    trainingId to mapOf(
                        "completed" to currentCompleted,
                        "lessonsVisited" to cappedVisited,
                        "quizVisited" to currentQuizVisited,
                        "quizScore" to currentQuizScore,
                        "quizTotal" to currentQuizTotal,
                        "totalLessons" to totalLessons,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                
                userTrainingRef.set(updatedTrainingData, SetOptions.merge()).await()
                Log.d("TrainingRepositoryImpl", "Lesson $pageIndex marked as visited for $trainingId (new max: $cappedVisited)")
            }
        } catch (e: Exception) {
            // [Eventual connectivity] Write failed - add to outbox for later retry
            Log.w("TrainingRepositoryImpl", "Failed to mark lesson visited, adding to outbox: ${e.message}")
            val pendingUpdate = PendingTrainingUpdate.lessonProgress(pageIndex, totalLessons)
            outbox.addPendingUpdate(pendingUpdate)
        }
    }

    override suspend fun markQuizVisited(trainingId: String) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val userTrainingRef = db.collection("user_trainings").document(uid)

        try {
            val currentDoc = userTrainingRef.get().await()
            val trainingData = currentDoc.data?.get(trainingId) as? Map<*, *>
            val currentCompleted = (trainingData?.get("completed") as? Boolean) ?: false
            val currentLessonsVisited = (trainingData?.get("lessonsVisited") as? Number)?.toInt() ?: 0
            val currentQuizScore = (trainingData?.get("quizScore") as? Number)?.toInt() ?: 0
            val currentQuizTotal = (trainingData?.get("quizTotal") as? Number)?.toInt() ?: 0
            val currentTotalLessons = (trainingData?.get("totalLessons") as? Number)?.toInt() ?: 0
            
            val updatedTrainingData = mapOf(
                trainingId to mapOf(
                    "completed" to currentCompleted,
                    "lessonsVisited" to currentLessonsVisited,
                    "quizVisited" to true,
                    "quizScore" to currentQuizScore,
                    "quizTotal" to currentQuizTotal,
                    "totalLessons" to currentTotalLessons,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            
            userTrainingRef.set(updatedTrainingData, SetOptions.merge()).await()
            Log.d("TrainingRepositoryImpl", "Quiz page visited for $trainingId")
        } catch (e: Exception) {
            Log.w("TrainingRepositoryImpl", "Failed to mark quiz visited: ${e.message}")
        }
    }

    override suspend fun getTrainingProgress(trainingId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        try {
            val userTrainingRef = db.collection("user_trainings").document(uid)
            val doc = userTrainingRef.get().await()
            val trainingData = doc.data?.get(trainingId) as? Map<*, *>
            
            return@withContext trainingData?.mapKeys { it.key.toString() }?.mapValues { it.value ?: 0 }
        } catch (e: Exception) {
            Log.w("TrainingRepositoryImpl", "Failed to get training progress: ${e.message}")
            return@withContext null
        }
    }

    override fun observeAllTrainingsProgress(): Flow<Map<String, Map<String, Any>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }
        
        val reg = db.collection("user_trainings")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TrainingRepositoryImpl", "Error observing trainings progress", error)
                    trySend(emptyMap())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val allProgress = mutableMapOf<String, Map<String, Any>>()
                    snapshot.data?.forEach { (trainingId, data) ->
                        if (data is Map<*, *>) {
                            val progressMap = data.mapKeys { it.key.toString() }.mapValues { it.value ?: 0 }
                            allProgress[trainingId] = progressMap
                        }
                    }
                    trySend(allProgress)
                } else {
                    trySend(emptyMap())
                }
            }
        
        awaitClose { reg.remove() }
    }

    override suspend fun getAllTrainingsProgress(): Map<String, Map<String, Any>> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyMap()
        try {
            val userTrainingRef = db.collection("user_trainings").document(uid)
            val doc = userTrainingRef.get().await()
            
            val allProgress = mutableMapOf<String, Map<String, Any>>()
            doc.data?.forEach { (trainingId, data) ->
                if (data is Map<*, *>) {
                    val progressMap = data.mapKeys { it.key.toString() }.mapValues { it.value ?: 0 }
                    allProgress[trainingId] = progressMap
                }
            }
            
            return@withContext allProgress
        } catch (e: Exception) {
            Log.w("TrainingRepositoryImpl", "Failed to get all trainings progress: ${e.message}")
            return@withContext emptyMap()
        }
    }


    override suspend fun submitQuiz(trainingId: String, correct: Int, total: Int) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val passed = total > 0 && correct.toFloat() / total >= 0.8f

        try {
            // Save to user_trainings/{uid} with field {trainingId}
            val userTrainingRef = db.collection("user_trainings").document(uid)
            
            // Get current progress to preserve lessonsVisited and totalLessons
            val currentDoc = userTrainingRef.get().await()
            val trainingData = currentDoc.data?.get(trainingId) as? Map<*, *>
            val currentLessonsVisited = (trainingData?.get("lessonsVisited") as? Number)?.toInt() ?: 0
            val currentTotalLessons = (trainingData?.get("totalLessons") as? Number)?.toInt() ?: 0
            
            val updatedTrainingData = mapOf(
                trainingId to mapOf(
                    "completed" to passed,
                    "lessonsVisited" to currentLessonsVisited,
                    "quizVisited" to true,
                    "quizScore" to correct,
                    "quizTotal" to total,
                    "totalLessons" to currentTotalLessons,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            
            userTrainingRef.set(updatedTrainingData, SetOptions.merge()).await()
            Log.d("TrainingRepositoryImpl", "Quiz submitted for $trainingId: $correct/$total (passed=$passed)")
            
            // If passed, update training progress for leaderboard
            if (passed) {
                updateTrainingProgress(uid, trainingId)
            }
        } catch (e: Exception) {
            // Eventual connectivity write failed: it adds to outbox for later retry
            Log.w("TrainingRepositoryImpl", "Failed to submit quiz, adding to outbox: ${e.message}")
            val pendingUpdate = PendingTrainingUpdate.quizResult(correct, total)
            outbox.addPendingUpdate(pendingUpdate)
        }
    }
    
    private suspend fun updateTrainingProgress(uid: String, trainingId: String) {
        try {
            val now = System.currentTimeMillis()
            val progressRef = db.collection("trainingProgress").document(uid)
            
            // Create completion event in subcollection
            val completionRef = progressRef.collection("completedTrainings").document()
            val completionData = mapOf(
                "trainingId" to trainingId,
                "completedAt" to now
            )
            completionRef.set(completionData).await()
            
            // Increment totalCompleted counter
            db.runTransaction { tx ->
                val progressDoc = tx.get(progressRef)
                val currentTotal = progressDoc.getLong("totalCompleted") ?: 0L
                
                val updates = mapOf(
                    "userId" to uid,
                    "totalCompleted" to (currentTotal + 1L),
                    "updatedAt" to now
                )
                
                tx.set(progressRef, updates, SetOptions.merge())
            }.await()
            
            Log.d("TrainingRepositoryImpl", "Training progress updated for user $uid: training $trainingId completed")
        } catch (e: Exception) {
            Log.w("TrainingRepositoryImpl", "Failed to update training progress: ${e.message}")
        }
    }

    // --- Leaderboard API implementation ---

    override suspend fun getCachedLeaderboard(
        timeframe: Timeframe
    ): LeaderboardSnapshot = withContext(Dispatchers.IO) {
        // 1) Try in-memory cache first
        val inMemory = leaderboardMemoryCache[timeframe]
        val inMemoryLastUpdate = lastRemoteRefreshMillis[timeframe]
        if (inMemory != null) {
            return@withContext LeaderboardSnapshot(
                entries = inMemory,
                lastUpdatedMillis = inMemoryLastUpdate
            )
        }

        // 2) Fallback to Room cache
        val entities = leaderboardDao.getEntriesForTimeframe(timeframe.name)
        if (entities.isEmpty()) {
            return@withContext LeaderboardSnapshot()
        }

        val entries = entities.map { it.toDomain() }
        val lastUpdated = entities.maxOf { it.lastUpdatedMillis }

        // hydrate memory cache
        leaderboardMemoryCache[timeframe] = entries
        lastRemoteRefreshMillis[timeframe] = lastUpdated

        LeaderboardSnapshot(entries = entries, lastUpdatedMillis = lastUpdated)
    }

    override suspend fun refreshLeaderboard(
        timeframe: Timeframe,
        force: Boolean
    ): LeaderboardSnapshot = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val lastRefresh = lastRemoteRefreshMillis[timeframe]

        // TTL: skip remote call if last refresh < 60s and force = false
        if (!force && lastRefresh != null && now - lastRefresh < leaderboardTtlMillis) {
            Log.d("TrainingRepositoryImpl", "Skipping remote leaderboard fetch (TTL) for $timeframe")
            return@withContext getCachedLeaderboard(timeframe)
        }

        try {
            Log.d("TrainingRepositoryImpl", "Fetching leaderboard from Firestore for $timeframe")

            // Step 1: Fetch data from trainingProgress collection
            val progressSnapshot = db.collection("trainingProgress")
                .get()
                .await()

            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
            
            // Map to store aggregated user data: userId -> (totalCompleted, weeklyCompleted, displayName, emailPrefix)
            val userDataMap = mutableMapOf<String, MutableMap<String, Any>>()

            // Process trainingProgress data
            for (doc in progressSnapshot.documents) {
                try {
                    val userId = doc.getString("userId") ?: doc.id
                    val totalCompleted = doc.getLong("totalCompleted") ?: 0L

                    // Recalculate weeklyCompleted by querying completedTrainings subcollection
                    val completionsSnapshot = doc.reference
                        .collection("completedTrainings")
                        .whereGreaterThanOrEqualTo("completedAt", sevenDaysAgo)
                        .get()
                        .await()
                    
                    val weeklyCompleted = completionsSnapshot.size().toLong()

                    // Fetch user data from users collection
                    val userDoc = db.collection("users").document(userId).get().await()
                    
                    val name = userDoc.getString("name") ?: ""
                    val lastName = userDoc.getString("lastName") ?: ""
                    val email = userDoc.getString("email") ?: ""
                    
                    val displayName = "$name $lastName".trim().ifEmpty { "Brigadist" }
                    val emailPrefix = email.substringBefore("@").ifEmpty { "user" }

                    userDataMap[userId] = mutableMapOf(
                        "totalCompleted" to totalCompleted,
                        "weeklyCompleted" to weeklyCompleted,
                        "displayName" to displayName,
                        "emailPrefix" to emailPrefix
                    )
                } catch (e: Exception) {
                    Log.e("TrainingRepositoryImpl", "Error parsing trainingProgress doc ${doc.id}", e)
                }
            }

            // Step 2: Fetch data from weekly_leaderboard collection
            val weeklyLeaderboardSnapshot = db.collection("weekly_leaderboard")
                .get()
                .await()

            for (weekDoc in weeklyLeaderboardSnapshot.documents) {
                try {
                    val entries = weekDoc.get("entries") as? List<Map<String, Any>> ?: continue
                    
                    for (entry in entries) {
                        val uid = entry["uid"] as? String ?: continue
                        val completedCount = (entry["completedCount"] as? Number)?.toLong() ?: 0L
                        val emailPrefix = entry["emailPrefix"] as? String ?: "user"
                        val lastCompletedAt = (entry["lastCompletedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                            ?: (entry["lastCompletedAt"] as? Number)?.toLong() ?: 0L
                        
                        // Determine if this week is within the last 7 days
                        val isRecent = lastCompletedAt >= sevenDaysAgo
                        
                        // Get or create user entry
                        val userData = userDataMap.getOrPut(uid) {
                            // Fetch user data if not already present
                            try {
                                val userDoc = db.collection("users").document(uid).get().await()
                                val name = userDoc.getString("name") ?: ""
                                val lastName = userDoc.getString("lastName") ?: ""
                                val displayName = "$name $lastName".trim().ifEmpty { "Brigadist" }
                                
                                mutableMapOf(
                                    "totalCompleted" to 0L,
                                    "weeklyCompleted" to 0L,
                                    "displayName" to displayName,
                                    "emailPrefix" to emailPrefix
                                )
                            } catch (e: Exception) {
                                Log.e("TrainingRepositoryImpl", "Error fetching user $uid", e)
                                mutableMapOf(
                                    "totalCompleted" to 0L,
                                    "weeklyCompleted" to 0L,
                                    "displayName" to "Brigadist",
                                    "emailPrefix" to emailPrefix
                                )
                            }
                        }
                        
                        // Add counts from weekly_leaderboard
                        val currentTotal = userData["totalCompleted"] as Long
                        userData["totalCompleted"] = currentTotal + completedCount
                        
                        if (isRecent) {
                            val currentWeekly = userData["weeklyCompleted"] as Long
                            userData["weeklyCompleted"] = currentWeekly + completedCount
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TrainingRepositoryImpl", "Error parsing weekly_leaderboard doc ${weekDoc.id}", e)
                }
            }

            // Step 3: Convert to LeaderboardEntry list
            val entries = userDataMap.map { (userId, data) ->
                LeaderboardEntry(
                    userId = userId,
                    displayName = data["displayName"] as String,
                    emailPrefix = data["emailPrefix"] as String,
                    totalCompleted = data["totalCompleted"] as Long,
                    weeklyCompleted = data["weeklyCompleted"] as Long
                )
            }.sortedByDescending { entry ->
                // Sort by the appropriate field based on timeframe
                when (timeframe) {
                    Timeframe.ALL_TIME -> entry.totalCompleted
                    Timeframe.LAST_7_DAYS -> entry.weeklyCompleted
                }
            }

            val lastUpdated = now

            // Persist on disk
            val entities = entries.map { it.toEntity(timeframe, lastUpdated) }
            leaderboardDao.clearForTimeframe(timeframe.name)
            leaderboardDao.insertAll(entities)

            // Update in-memory cache
            leaderboardMemoryCache[timeframe] = entries
            lastRemoteRefreshMillis[timeframe] = lastUpdated
            
            // Background cleanup: delete old completion records (older than 8 days for safety margin)
            cleanupOldCompletions(sevenDaysAgo - (24 * 60 * 60 * 1000L))

            LeaderboardSnapshot(entries = entries, lastUpdatedMillis = lastUpdated)
        } catch (e: Exception) {
            Log.w("TrainingRepositoryImpl", "Error refreshing leaderboard from Firestore, falling back to cache", e)
            // Fallback: return cached data
            getCachedLeaderboard(timeframe)
        }
    }

    /**
     * Background cleanup task: deletes completion records older than the cutoff timestamp.
     * This runs in a background coroutine to avoid blocking the main leaderboard refresh.
     */
    private fun cleanupOldCompletions(cutoffTimestamp: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val progressSnapshot = db.collection("trainingProgress").get().await()
                
                var deletedCount = 0
                for (doc in progressSnapshot.documents) {
                    val oldCompletions = doc.reference
                        .collection("completedTrainings")
                        .whereLessThan("completedAt", cutoffTimestamp)
                        .get()
                        .await()
                    
                    // Delete old completion records in batches
                    val batch = db.batch()
                    for (completion in oldCompletions.documents) {
                        batch.delete(completion.reference)
                        deletedCount++
                    }
                    
                    if (oldCompletions.documents.isNotEmpty()) {
                        batch.commit().await()
                    }
                }
                
                Log.d("TrainingRepositoryImpl", "Cleaned up $deletedCount old completion records")
            } catch (e: Exception) {
                Log.w("TrainingRepositoryImpl", "Error cleaning up old completions: ${e.message}")
            }
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
