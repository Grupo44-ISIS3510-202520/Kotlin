package com.example.brigadeapp.model.repository.implementation

import com.example.brigadeapp.model.domain.Protocol
import com.example.brigadeapp.model.repository.interfaces.ProtocolRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

class ProtocolRepositoryImpl @Inject constructor(
    @Named("protocols") private val firestore: FirebaseFirestore
) : ProtocolRepository {

    override suspend fun getProtocols(): List<Protocol> {
        return try {
            val snapshot = firestore.collection("protocols-and-manuals").get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(Protocol::class.java) }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
