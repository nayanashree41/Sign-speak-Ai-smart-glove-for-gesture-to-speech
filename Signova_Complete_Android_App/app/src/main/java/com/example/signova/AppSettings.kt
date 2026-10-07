package com.example.signova

import android.content.Context

object AppSettings {
    private const val PREFS = "signova_settings"

    fun confidenceThreshold(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("confidence", 80)

    fun speechRate(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat("speech_rate", 1.0f)

    fun autoSpeak(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("auto_speak", false)

    fun language(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("language", "English") ?: "English"

    fun save(context: Context, confidence: Int, rate: Float, autoSpeak: Boolean, language: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("confidence", confidence)
            .putFloat("speech_rate", rate)
            .putBoolean("auto_speak", autoSpeak)
            .putString("language", language)
            .apply()
    }
}
