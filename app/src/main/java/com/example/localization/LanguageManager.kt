package com.example.localization

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(
    val code: String,
    val nativeLabel: String,
    val englishName: String,
    val promptName: String
) {
    ENGLISH("en", "English", "English", "English"),
    HINDI("hi", "हिन्दी", "Hindi", "Hindi (Devanagari script)"),
    TAMIL("ta", "தமிழ்", "Tamil", "Tamil"),
    BENGALI("bn", "বাংলা", "Bengali", "Bengali"),
    MARATHI("mr", "मराठी", "Marathi", "Marathi (Devanagari script)"),
    TELUGU("te", "తెలుగు", "Telugu", "Telugu"),
    SPANISH("es", "Español", "Spanish", "Spanish"),
    FRENCH("fr", "Français", "French", "French");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object LanguageManager {
    private const val PREFS_NAME = "astranova_prefs"
    private const val KEY_LANGUAGE = "preferred_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
        _currentLanguage.value = AppLanguage.fromCode(saved)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
    }
}
