package dev.aaa1115910.hsodv2.faction

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

data class UserPreferences(val xpId: Int, val score: Int)

object PreferencesKeys {
    val XP_ID = intPreferencesKey("xp_id")
    val SCORE = intPreferencesKey("score")
}