package com.aura.dating.core.localization

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String
) {
    SYSTEM("system", "System Default", "Sistem Dili", "🌐"),
    ENGLISH("en", "English", "English", "🇬🇧"),
    TURKISH("tr", "Turkish", "Türkçe", "🇹🇷"),
    RUSSIAN("ru", "Russian", "Русский", "🇷🇺"),
    TURKMEN("tk", "Turkmen", "Türkmençe", "🇹🇲");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}

object LanguageManager {
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = if (languageCode == "system" || languageCode.isBlank()) {
            Locale.getDefault()
        } else {
            Locale.forLanguageTag(languageCode)
        }

        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            if (languageCode == "system" || languageCode.isBlank()) {
                localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
            } else {
                localeManager?.applicationLocales = LocaleList.forLanguageTags(languageCode)
            }
        }

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
