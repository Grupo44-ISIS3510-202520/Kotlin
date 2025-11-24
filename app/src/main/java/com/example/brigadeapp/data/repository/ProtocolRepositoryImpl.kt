package com.example.brigadeapp.data.repository

import android.content.Context
import android.util.Log
import com.example.brigadeapp.data.source.local.ProtocolVersionDataStore
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

    companion object {
        private const val TAG = "ProtocolRepository"
        private const val CACHE_DIR_NAME = "protocols"
    }

    private val collection = firestore.collection("protocols-and-manuals")

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
                Log.d(TAG, "Cache directory created: $absolutePath")
            }
        }
    }

    override suspend fun getAllProtocols(): List<Protocol> {
        return try {
            Log.d(TAG, "Fetching protocols from Firestore: ${collection.path}")
            val snapshot = collection.get().await()
            val list = snapshot.toObjects(Protocol::class.java)
            Log.d(TAG, "Protocols found: ${list.size}")
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading protocols from Firestore", e)
            emptyList()
        }
    }

    override suspend fun readLocalVersions(): Map<String, String> {
        return try {
            val versions = versionDataStore.readLocalVersions()
            Log.d(TAG, "Read ${versions.size} local versions")
            versions
        } catch (e: Exception) {
            Log.e(TAG, "Error reading local versions", e)
            emptyMap()
        }
    }

    override suspend fun saveLocalVersions(versions: Map<String, String>) {
        try {
            versionDataStore.saveLocalVersions(versions)
            Log.d(TAG, "Saved ${versions.size} local versions")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving local versions", e)
        }
    }

    override suspend fun getUpdatedProtocols(localVersions: Map<String, String>): List<Protocol> {
        return try {
            val allProtocols = getAllProtocols()
            val updatedProtocols = allProtocols.filter { protocol ->
                val oldVersion = localVersions[protocol.name] ?: "0.0.0"
                val isNewer = protocol.version.compareTo(oldVersion) > 0

                if (isNewer) {
                    Log.d(TAG, "Updated: ${protocol.name} ($oldVersion → ${protocol.version})")
                }

                isNewer
            }

            Log.d(TAG, "Found ${updatedProtocols.size} updated protocols")
            updatedProtocols
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            emptyList()
        }
    }

    override suspend fun getProtocolFile(protocol: Protocol): Result<File> {
        return try {
            val fileName = sanitizeFileName(protocol.name, protocol.version)
            val localFile = File(cacheDir, fileName)

            if (localFile.exists() && localFile.length() > 0) {
                Log.d(TAG, "Cache HIT: ${protocol.name} (${formatFileSize(localFile.length())})")
                return Result.success(localFile)
            }

            Log.d(TAG, "Cache MISS: Downloading ${protocol.name}")
            Log.d(TAG, "URL: ${protocol.url}")

            val result = fileDownloader.downloadFile(protocol.url, fileName)

            result.fold(
                onSuccess = { file ->
                    Log.d(TAG, "Downloaded: ${protocol.name} (${formatFileSize(file.length())})")
                },
                onFailure = { error ->
                    Log.e(TAG, "Download failed: ${protocol.name}", error)
                }
            )

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting protocol file: ${protocol.name}", e)
            Result.failure(e)
        }
    }

    override fun getCacheSize(): Long {
        return try {
            val size = calculateDirectorySize(cacheDir)
            Log.d(TAG, "Total cache size: ${formatFileSize(size)}")
            size
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
            0L
        }
    }

    override suspend fun clearCache() {
        try {
            Log.d(TAG, "Clearing cache...")

            var deletedCount = 0
            var deletedSize = 0L

            fileDownloader.clearCache()

            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".pdf") || file.name.contains("_v"))) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        deletedSize += size
                        Log.d(TAG, "Deleted: ${file.name}")
                    }
                }
            }

            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".pdf") || file.name.contains("_v"))) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        deletedSize += size
                    }
                }
            }

            Log.d(TAG, "Cache cleared: $deletedCount files (${formatFileSize(deletedSize)})")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    fun isProtocolCached(protocol: Protocol): Boolean {
        val fileName = sanitizeFileName(protocol.name, protocol.version)
        val file = File(cacheDir, fileName)
        val isCached = file.exists() && file.length() > 0

        if (isCached) {
            Log.d(TAG, "Cached: ${protocol.name}")
        }

        return isCached
    }

    fun getCachedProtocols(allProtocols: List<Protocol>): List<Protocol> {
        return allProtocols.filter { isProtocolCached(it) }
    }

    fun getCacheInfo(): CacheInfo {
        val files = cacheDir.listFiles()?.filter { it.isFile && it.name.endsWith(".pdf") } ?: emptyList()
        val totalSize = files.sumOf { it.length() }
        val fileCount = files.size

        return CacheInfo(
            fileCount = fileCount,
            totalSize = totalSize,
            formattedSize = formatFileSize(totalSize),
            oldestFile = files.minByOrNull { it.lastModified() }?.name,
            newestFile = files.maxByOrNull { it.lastModified() }?.name
        )
    }

    private fun sanitizeFileName(name: String, version: String): String {
        val n = name.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(50)
        val v = version.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(10)
        return "${n}_v${v}.pdf"
    }

    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "%.2f KB".format(sizeInBytes / 1024.0)
            sizeInBytes < 1024 * 1024 * 1024 -> "%.2f MB".format(sizeInBytes / (1024.0 * 1024.0))
            else -> "%.2f GB".format(sizeInBytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}

data class CacheInfo(
    val fileCount: Int,
    val totalSize: Long,
    val formattedSize: String,
    val oldestFile: String?,
    val newestFile: String?
)
