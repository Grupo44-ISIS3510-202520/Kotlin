package com.example.brigadeapp.data.repository

import android.content.Context
import android.util.Log
import com.example.brigadeapp.data.datastore.ProtocolVersionDataStore
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository
import com.example.brigadeapp.domain.utils.CachedFileDownloader
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class ProtocolRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val versionDataStore: ProtocolVersionDataStore,
    private val fileDownloader: CachedFileDownloader,
    @ApplicationContext private val context: Context
) : ProtocolRepository {

    private val collection = firestore.collection("protocols-and-manuals")

    override suspend fun getAllProtocols(): List<Protocol> {
        return try {
            Log.d("FirebaseDebug", "Searching in collection: ${collection.path}")
            val snapshot = collection.get().await()
            val list = snapshot.toObjects(Protocol::class.java)
            Log.d("FirebaseDebug", "Protocols found: ${list.size}")
            list
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error loading protocols", e)
            emptyList()
        }
    }

    override suspend fun readLocalVersions(): Map<String, String> = versionDataStore.readLocalVersions()

    override suspend fun saveLocalVersions(versions: Map<String, String>) =
        versionDataStore.saveLocalVersions(versions)

    override suspend fun getUpdatedProtocols(localVersions: Map<String, String>): List<Protocol> {
        return try {
            val allProtocols = getAllProtocols()
            allProtocols.filter {
                val oldVersion = localVersions[it.name] ?: "0.0.0"
                it.version.compareTo(oldVersion) > 0
            }
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error checking updates", e)
            emptyList()
        }
    }

    override suspend fun getProtocolFile(protocol: Protocol): Result<File> {
        val fileName = sanitizeFileName(protocol.name, protocol.version)
        val localFile = File(context.cacheDir, fileName)

        if (localFile.exists()) {
            Log.d("ProtocolRepo", "Cache HIT (File): ${localFile.name}")
            return Result.success(localFile)
        }

        Log.d("ProtocolRepo", "Cache MISS (File). Downloading ${protocol.url}")
        return fileDownloader.downloadFile(protocol.url, fileName)
    }

    override fun getCacheSize(): Long = fileDownloader.getCacheSize()

    override suspend fun clearCache() {
        fileDownloader.clearCache()
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".pdf") || file.name.contains("_v"))) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("ProtocolRepo", "Cache error", e)
        }
    }

    private fun sanitizeFileName(name: String, version: String): String {
        val n = name.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(50)
        val v = version.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(10)
        return "${n}_v${v}.pdf"
    }
}
