package com.example.brigadeapp.data.source.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.brigadeapp.domain.entity.PendingTrainingUpdate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.trainingOutboxDataStore: DataStore<Preferences> by preferencesDataStore(name = "training_outbox")

private val PENDING_UPDATES_KEY = stringPreferencesKey("pending_updates")

@Singleton
class TrainingOutboxDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.trainingOutboxDataStore
    private val gson = Gson()


    suspend fun readPendingUpdates(): List<PendingTrainingUpdate> {
        val jsonString = dataStore.data.map { preferences ->
            preferences[PENDING_UPDATES_KEY]
        }.first()

        if (jsonString.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val type = object : TypeToken<List<PendingTrainingUpdate>>() {}.type
            gson.fromJson<List<PendingTrainingUpdate>>(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("TrainingOutboxDataStore", "Error parsing pending updates", e)
            emptyList()
        }
    }


    suspend fun addPendingUpdate(update: PendingTrainingUpdate) {
        dataStore.edit { preferences ->
            val current = preferences[PENDING_UPDATES_KEY]
            val existingList = if (!current.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<PendingTrainingUpdate>>() {}.type
                    gson.fromJson<List<PendingTrainingUpdate>>(current, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val updatedList = existingList + update
            preferences[PENDING_UPDATES_KEY] = gson.toJson(updatedList)
            Log.d("TrainingOutboxDataStore", "Added pending update ${update.id}, outbox size: ${updatedList.size}")
        }
    }


    suspend fun removePendingUpdate(updateId: String) {
        dataStore.edit { preferences ->
            val current = preferences[PENDING_UPDATES_KEY]
            if (current.isNullOrEmpty()) return@edit

            val existingList = try {
                val type = object : TypeToken<List<PendingTrainingUpdate>>() {}.type
                gson.fromJson<List<PendingTrainingUpdate>>(current, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val updatedList = existingList.filterNot { it.id == updateId }
            preferences[PENDING_UPDATES_KEY] = gson.toJson(updatedList)
            Log.d("TrainingOutboxDataStore", "Removed pending update $updateId, outbox size: ${updatedList.size}")
        }
    }


    suspend fun clearOutbox() {
        dataStore.edit { preferences ->
            preferences.remove(PENDING_UPDATES_KEY)
            Log.d("TrainingOutboxDataStore", "Cleared outbox")
        }
    }
}
