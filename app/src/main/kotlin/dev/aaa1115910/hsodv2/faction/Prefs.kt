package dev.aaa1115910.hsodv2.faction

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

suspend fun Context.getUserPreferences(): UserPreferences {
    return this.dataStore.data.map { preferences ->
        val xpId = preferences[PreferencesKeys.XP_ID] ?: 1
        val score = preferences[PreferencesKeys.SCORE] ?: 0
        val warningLine = preferences[PreferencesKeys.WARNING_LINE] ?: 0
        UserPreferences(xpId, score, warningLine)
    }.first()
}

data class UserPreferences(val xpId: Int, val score: Int, val warningLine: Int)

object PreferencesKeys {
    val XP_ID = intPreferencesKey("xp_id")
    val SCORE = intPreferencesKey("score")
    val WARNING_LINE = intPreferencesKey("warning_line")
}