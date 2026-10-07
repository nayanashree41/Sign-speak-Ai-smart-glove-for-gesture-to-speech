package com.example.signova

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val autoSpeak = findViewById<SwitchMaterial>(R.id.switchAutoSpeak)
        val threshold = findViewById<SeekBar>(R.id.seekThreshold)
        val tvThreshold = findViewById<TextView>(R.id.tvThreshold)
        val rate = findViewById<SeekBar>(R.id.seekSpeechRate)
        val tvRate = findViewById<TextView>(R.id.tvSpeechRate)
        val language = findViewById<Spinner>(R.id.spinnerLanguage)
        val languages = listOf("English", "Hindi", "Kannada", "Malayalam", "Tamil")
        language.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)

        autoSpeak.isChecked = AppSettings.autoSpeak(this)
        threshold.progress = AppSettings.confidenceThreshold(this) - 50
        tvThreshold.text = "${AppSettings.confidenceThreshold(this)}%"
        rate.progress = ((AppSettings.speechRate(this) - 0.5f) * 100).toInt().coerceIn(0,100)
        tvRate.text = String.format("%.1fx", AppSettings.speechRate(this))
        language.setSelection(languages.indexOf(AppSettings.language(this)).coerceAtLeast(0))

        threshold.setOnSeekBarChangeListener(simpleListener { p -> tvThreshold.text = "${50 + p}%" })
        rate.setOnSeekBarChangeListener(simpleListener { p -> tvRate.text = String.format("%.1fx", 0.5f + p / 100f) })

        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            AppSettings.save(this, 50 + threshold.progress, 0.5f + rate.progress / 100f, autoSpeak.isChecked, language.selectedItem.toString())
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnSettingsHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
    }

    private fun simpleListener(onProgress: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = onProgress(progress)
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}
