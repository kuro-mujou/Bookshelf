package com.capstone.bookshelf.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

const val USER_DATASTORE = "book_setting"

val Context.dataStore by preferencesDataStore(name = USER_DATASTORE)
object PreferencesKeys {
    val KEEP_SCREEN_ON = booleanPreferencesKey("KEEP_SCREEN_ON")
    val TTS_SPEED = floatPreferencesKey("TTS_SPEED")
    val TTS_PITCH = floatPreferencesKey("TTS_PITCH")
    val TTS_LOCALE = stringPreferencesKey("TTS_LOCALE")
    val TTS_VOICE = stringPreferencesKey("TTS_VOICE")
    val AUTO_SCROLL_SPEED = floatPreferencesKey("AUTO_SCROLL_SPEED")
    val BACKGROUND_COLOR = intPreferencesKey("BACKGROUND_COLOR")
    val TEXT_COLOR = intPreferencesKey("TEXT_COLOR")
    val SELECTED_COLOR_SET = intPreferencesKey("SELECTED_COLOR_SET")
}
class DataStoreManger(val context: Context) {
    private val dataStore = context.dataStore
    val keepScreenOn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: false
    }
    val ttsSpeed: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TTS_SPEED] ?: 1f
    }
    val ttsPitch: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TTS_PITCH] ?: 1f
    }
    val ttsLocale: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TTS_LOCALE] ?: Locale.getDefault().displayName
    }
    val ttsVoice: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TTS_VOICE] ?: ""
    }
    val autoScrollSpeed: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SCROLL_SPEED] ?: 1f
    }
    val backgroundColor: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BACKGROUND_COLOR] ?: Color.White.toArgb()
    }
    val textColor: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TEXT_COLOR] ?: Color.Black.toArgb()
    }
    val selectedColorSet: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_COLOR_SET] ?: 5
    }
    suspend fun setKeepScreenOn(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = value
        }
    }
    suspend fun setTTSSpeed(value: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_SPEED] = value
        }
    }
    suspend fun setTTSPitch(value: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_PITCH] = value
        }
    }
    suspend fun setTTSLocale(value: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_LOCALE] = value
        }
    }
    suspend fun setTTSVoice(value: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_VOICE] = value
        }
    }
    suspend fun setAutoScrollSpeed(value: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCROLL_SPEED] = value
        }
    }
    suspend fun setBackgroundColor(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKGROUND_COLOR] = value
        }
    }
    suspend fun setTextColor(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_COLOR] = value
        }
    }
    suspend fun setSelectedColorSet(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_COLOR_SET] = value
        }
    }
}