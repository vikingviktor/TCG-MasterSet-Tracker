package com.example.pokemonmastersettracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val COUNT_VARIANTS_KEY = booleanPreferencesKey("count_variants_in_collection")
    }
    
    /**
     * Whether to count variants toward collection total
     * Default: false (count unique cards only)
     */
    val countVariantsInCollection: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[COUNT_VARIANTS_KEY] ?: false
        }
    
    suspend fun setCountVariantsInCollection(countVariants: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[COUNT_VARIANTS_KEY] = countVariants
        }
    }
}
