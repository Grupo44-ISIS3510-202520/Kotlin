package com.example.brigadeapp.data.repository

import android.util.Log
import com.example.brigadeapp.domain.entity.Protocol // ✅ Verifica que estés usando 'entity.Protocol' o 'model.Protocol'
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
            // ✅ Asegúrate que tu Protocol data class coincida con Firestore
            val list = snapshot.toObjects(Protocol::class.java)
            Log.d("FirebaseDebug", "Protocolos encontrados: ${list.size}")
            list
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error al cargar protocolos", e)
            emptyList()
        }
    }

    override suspend fun getUpdatedProtocols(localVersions: Map<String, String>): List<Protocol> {
        return try {
            val allProtocols = getAllProtocols()
            val updatedList = allProtocols.filter { protocol ->
                val oldVersion = localVersions[protocol.name] ?: "0.0.0" // Versión base

                // ✅ LÓGICA DE COMPARACIÓN DE CADENAS:
                // compareTo() retorna > 0 si la versión actual es lexicográficamente mayor
                // Esto funciona bien para versiones estandarizadas (ej: "1.2" > "1.1")
                protocol.version.compareTo(oldVersion) > 0
            }
            Log.d("FirebaseDebug", "Protocolos actualizados: ${updatedList.size}")
            updatedList
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error al chequear actualizaciones", e)
            emptyList()
        }
    }
}
