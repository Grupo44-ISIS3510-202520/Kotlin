package com.example.brigadeapp.data.repository

import android.util.Log
import com.example.brigadeapp.domain.entity.Alert
import com.example.brigadeapp.domain.repository.AlertsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AlertsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlertsRepository {

    override fun getAlerts(): Flow<List<Alert>> = callbackFlow {

        val query = firestore.collection("alerts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)


        val listenerRegistration = query.addSnapshotListener { snapshot, error ->

            // 3. Maneja errores del listener
            if (error != null) {
                Log.w("AlertsRepo", "Listener error", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot == null) {
                return@addSnapshotListener
            }


            val alertsList = mutableListOf<Alert>()
            for (doc in snapshot.documents) {
                try {
                    val alert = doc.toObject(Alert::class.java)
                    if (alert != null) {
                        alertsList.add(alert)
                    }
                } catch (e: Exception) {
                    Log.e("AlertsRepo", "Error parsing document ${doc.id}", e)
                }
            }

            // 6. Envía la lista nueva al flow
            trySend(alertsList)
        }


        awaitClose {
            Log.d("AlertsRepo", "Closing alerts listener")
            listenerRegistration.remove()
        }
    }
}

