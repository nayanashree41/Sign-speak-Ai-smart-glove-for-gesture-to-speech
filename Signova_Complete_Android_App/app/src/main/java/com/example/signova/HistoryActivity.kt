package com.example.signova

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        render()
        findViewById<Button>(R.id.btnClearHistory).setOnClickListener { HistoryStore.clear(this); render() }
        findViewById<Button>(R.id.btnBackHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
    }

    private fun render() {
        val container = findViewById<LinearLayout>(R.id.historyContainer)
        container.removeAllViews()
        val records = HistoryStore.all(this)
        if (records.isEmpty()) {
            val empty = TextView(this).apply { text = "No conversations yet. Use Simulation Mode and tap Speak."; textSize = 16f; setTextColor(Color.rgb(90,82,98)); setPadding(16,24,16,24) }
            container.addView(empty)
            return
        }
        val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        records.forEach { raw ->
            val p = raw.split('|', limit = 3)
            if (p.size == 3) {
                val time = p[0].toLongOrNull()?.let { fmt.format(Date(it)) } ?: ""
                val tv = TextView(this).apply {
                    text = "${p[1]}\n${p[2]}\n$time"
                    textSize = 16f
                    setTextColor(Color.rgb(35,29,42))
                    setPadding(22,18,22,18)
                    setBackgroundResource(R.drawable.bg_history_item)
                }
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 14 }
                container.addView(tv, params)
            }
        }
    }
}
