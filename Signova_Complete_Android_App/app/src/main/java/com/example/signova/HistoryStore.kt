package com.example.signova

import android.content.Context

object HistoryStore {
    private const val PREFS = "signova_history"
    private const val KEY = "records"

    fun add(context: Context, sequence: String, sentence: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val old = prefs.getString(KEY, "") ?: ""
        val cleanSequence = sequence.replace("|", "/").replace("\n", " ")
        val cleanSentence = sentence.replace("|", "/").replace("\n", " ")
        val record = "${System.currentTimeMillis()}|$cleanSequence|$cleanSentence"
        val records = (listOf(record) + old.lines().filter { it.isNotBlank() }).take(30)
        prefs.edit().putString(KEY, records.joinToString("\n")).apply()
    }

    fun all(context: Context): List<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "")
            .orEmpty()
            .lines()
            .filter { it.isNotBlank() }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
