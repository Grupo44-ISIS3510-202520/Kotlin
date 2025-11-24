package com.example.brigadeapp.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "protocol_cache")

private val PROTOCOL_VERSIONS_KEY = stringPreferencesKey("protocol_versions_map")

@Singleton
class ProtocolVersionDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore
    private val gson = Gson()


    suspend fun readLocalVersions(): Map<String, String> {
        val jsonString = dataStore.data.map { preferences ->
            preferences[PROTOCOL_VERSIONS_KEY]
        }.first()

        if (jsonString.isNullOrEmpty()) {
            return emptyMap()
        }

        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }


    suspend fun saveLocalVersions(versions: Map<String, String>) {
        val jsonString = gson.toJson(versions)
        dataStore.edit { preferences ->
            preferences[PROTOCOL_VERSIONS_KEY] = jsonString
        }
    }
}