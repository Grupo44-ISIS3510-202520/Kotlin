package com.example.brigadeapp.data.repository

import android.util.Log
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProtocolRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProtocolRepository {

    private val collection = firestore.collection("protocols-and-manuals")

    override suspend fun getAllProtocols(): List<Protocol> {
        return try {
            Log.d("FirebaseDebug", "Buscando en colección: ${collection.path}")
            val snapshot = collection.get().await()
            val list = snapshot.toObjects(Protocol::class.java)
            Log.d("FirebaseDebug", "Protocolos encontrados: ${list.size}")
            list
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error al cargar protocolos", e)
            emptyList()
        }
    }

    override suspend fun getUpdatedProtocols(localVersions: Map<String, Int>): List<Protocol> {
        return try {
            val allProtocols = getAllProtocols()
            val updatedList = allProtocols.filter { protocol ->
                val oldVersion = localVersions[protocol.name] ?: 0
                protocol.version > oldVersion
            }
            Log.d("FirebaseDebug", "Protocolos actualizados: ${updatedList.size}")
            updatedList
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error al chequear actualizaciones", e)
            emptyList()
        }
    }
}
