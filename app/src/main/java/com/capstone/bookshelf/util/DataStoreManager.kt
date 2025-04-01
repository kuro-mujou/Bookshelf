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
    val AUTO_SCROLL_SPEED = intPreferencesKey("AUTO_SCROLL_SPEED")
    val DELAY_TIME_AT_START = intPreferencesKey("DELAY_TIME_AT_START")
    val DELAY_TIME_AT_END = intPreferencesKey("DELAY_TIME_AT_END")
    val AUTO_SCROLL_RESUME_MODE = booleanPreferencesKey("AUTO_SCROLL_RESUME_MODE")
    val AUTO_SCROLL_RESUME_DELAY_TIME = intPreferencesKey("AUTO_SCROLL_RESUME_DELAY_TIME")
    val BACKGROUND_COLOR = intPreferencesKey("BACKGROUND_COLOR")
    val TEXT_COLOR = intPreferencesKey("TEXT_COLOR")
    val SELECTED_COLOR_SET = intPreferencesKey("SELECTED_COLOR_SET")
    val FONT_SIZE = intPreferencesKey("FONT_SIZE")
    val TEXT_ALIGN = booleanPreferencesKey("TEXT_ALIGN")
    val TEXT_INDENT = booleanPreferencesKey("TEXT_INDENT")
    val LINE_SPACING = intPreferencesKey("LINE_SPACING")
    val FONT_FAMILY = intPreferencesKey("FONT_FAMILY")
    val IS_SORTED_BY_FAVORITE = booleanPreferencesKey("IS_SORTED_BY_FAVORITE")
    val ENABLE_BACKGROUND_MUSIC = booleanPreferencesKey("ENABLE_BACKGROUND_MUSIC")
    val PLAYER_VOLUME = floatPreferencesKey("PLAYER_VOLUME")
}
class DataStoreManager(val context: Context) {
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
    val autoScrollSpeed: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SCROLL_SPEED] ?: 10000
    }
    val delayTimeAtStart: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DELAY_TIME_AT_START] ?: 3000
    }
    val delayTimeAtEnd: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DELAY_TIME_AT_END] ?: 3000
    }
    val autoScrollResumeMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SCROLL_RESUME_MODE] ?: false
    }
    val autoScrollResumeDelayTime: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SCROLL_RESUME_DELAY_TIME] ?: 2000
    }
    val backgroundColor: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BACKGROUND_COLOR] ?: Color(0xFFD3C3A3).toArgb()
    }
    val textColor: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TEXT_COLOR] ?: Color(0xFF3A3129).toArgb()
    }
    val selectedColorSet: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_COLOR_SET] ?: 1
    }
    val fontSize: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SIZE] ?: 20
    }
    val textAlign: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TEXT_ALIGN] ?: true
    }
    val textIndent: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TEXT_INDENT] ?: true
    }
    val lineSpacing: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LINE_SPACING] ?: 14
    }
    val fontFamily: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_FAMILY] ?: 0
    }
    val isSortedByFavorite: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_SORTED_BY_FAVORITE] ?: true
    }
    val enableBackgroundMusic: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ENABLE_BACKGROUND_MUSIC] ?: false
    }
    val playerVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PLAYER_VOLUME] ?: 1f
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
    suspend fun setAutoScrollSpeed(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCROLL_SPEED] = value
        }
    }
    suspend fun setDelayTimeAtStart(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELAY_TIME_AT_START] = value
        }
    }
    suspend fun setDelayTimeAtEnd(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELAY_TIME_AT_END] = value
        }
    }
    suspend fun setAutoScrollResumeMode(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCROLL_RESUME_MODE] = value
        }
    }
    suspend fun setAutoScrollResumeDelayTime(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCROLL_RESUME_DELAY_TIME] = value
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
    suspend fun setFontSize(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = value
        }
    }
    suspend fun setTextAlign(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_ALIGN] = value
        }
    }
    suspend fun setTextIndent(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_INDENT] = value
        }
    }
    suspend fun setLineSpacing(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LINE_SPACING] = value
        }
    }
    suspend fun setFontFamily(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY] = value
        }
    }
    suspend fun setSortByFavorite(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SORTED_BY_FAVORITE] = value
        }
    }
    suspend fun setEnableBackgroundMusic(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_BACKGROUND_MUSIC] = value
        }
    }
    suspend fun setPlayerVolume(value: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_VOLUME] = value
        }
    }
}