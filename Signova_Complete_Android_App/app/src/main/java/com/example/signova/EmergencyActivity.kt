package com.example.signova

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class EmergencyActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)
        tts = TextToSpeech(this, this)
        findViewById<Button>(R.id.btnEmergencySpeak).setOnClickListener {
            val msg = "Please help me. I need assistance."
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "emergency")
            HistoryStore.add(this, "EMERGENCY", msg)
        }
        findViewById<Button>(R.id.btnEmergencyBack).setOnClickListener { finish() }
    }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale.US }
    override fun onDestroy() { tts.stop(); tts.shutdown(); super.onDestroy() }
}
