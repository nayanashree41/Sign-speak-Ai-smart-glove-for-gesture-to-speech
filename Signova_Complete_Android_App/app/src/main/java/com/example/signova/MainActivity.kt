package com.example.signova

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var bluetooth: BluetoothGloveManager
    private lateinit var dlGateway: DeepLearningGateway
    private val sensorWindow = ArrayDeque<FloatArray>()
    private val sequence = mutableListOf<String>()
    private val simulatedGestures = listOf("HELLO", "I", "NEED", "WATER", "THANK YOU", "HELP", "YES", "NO", "FOOD", "MEDICINE")
    private var simulationIndex = 0

    private lateinit var tvConnection: TextView
    private lateinit var tvGesture: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var tvSequence: TextView
    private lateinit var tvSentence: TextView
    private lateinit var tvRaw: TextView
    private lateinit var progress: ProgressBar
    private lateinit var simulationSwitch: SwitchMaterial

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) bluetooth.connect()
        else Toast.makeText(this, "Bluetooth permission is required to connect the glove.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)
        dlGateway = DeepLearningGateway(this)

        tvConnection = findViewById(R.id.tvConnectionStatus)
        tvGesture = findViewById(R.id.tvGesture)
        tvConfidence = findViewById(R.id.tvConfidence)
        tvSequence = findViewById(R.id.tvSequence)
        tvSentence = findViewById(R.id.tvSentence)
        tvRaw = findViewById(R.id.tvRawSensor)
        progress = findViewById(R.id.progressConfidence)
        simulationSwitch = findViewById(R.id.switchSimulation)

        bluetooth = BluetoothGloveManager(
            this,
            onStatus = { status -> runOnUiThread { tvConnection.text = status } },
            onSensorLine = { line -> runOnUiThread { handleSensorLine(line) } }
        )

        findViewById<Button>(R.id.btnConnect).setOnClickListener { requestBluetoothAndConnect() }
        findViewById<Button>(R.id.btnSimulate).setOnClickListener { simulateGesture() }
        findViewById<Button>(R.id.btnSpeak).setOnClickListener { speakCurrentSentence() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearSequence() }
        findViewById<Button>(R.id.btnEmergency).setOnClickListener { startActivity(Intent(this, EmergencyActivity::class.java)) }
        findViewById<Button>(R.id.navHistory).setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        findViewById<Button>(R.id.navLearn).setOnClickListener { startActivity(Intent(this, LearnActivity::class.java)) }
        findViewById<Button>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        simulationSwitch.setOnCheckedChangeListener { _, checked ->
            tvRaw.text = if (checked) "Sensor stream: simulation" else "Sensor stream: waiting for ESP32"
        }
    }

    private fun requestBluetoothAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val needed = listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
                .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray()) else bluetooth.connect()
        } else bluetooth.connect()
    }

    private fun simulateGesture() {
        if (!simulationSwitch.isChecked) {
            Toast.makeText(this, "Enable Simulation Mode or connect the glove.", Toast.LENGTH_SHORT).show()
            return
        }
        val label = simulatedGestures[simulationIndex % simulatedGestures.size]
        simulationIndex++
        val confidence = listOf(0.91f, 0.94f, 0.88f, 0.96f, 0.93f)[simulationIndex % 5]
        acceptPrediction(label, confidence)
    }

    private fun handleSensorLine(line: String) {
        tvRaw.text = "Sensor stream: $line"
        val values = dlGateway.parseSensorLine(line) ?: return
        sensorWindow.addLast(values)
        while (sensorWindow.size > 40) sensorWindow.removeFirst()
        val prediction = dlGateway.classifyWindow(sensorWindow.toList())
        if (prediction != null) {
            acceptPrediction(prediction.label, prediction.confidence)
        } else if (!dlGateway.modelAssetPresent()) {
            tvGesture.text = "DL model required"
            tvConfidence.text = "Add gesture_model.tflite after training"
        }
    }

    private fun acceptPrediction(label: String, confidence: Float) {
        val percent = (confidence * 100).roundToInt()
        tvGesture.text = label
        tvConfidence.text = "$percent%"
        progress.progress = percent

        val threshold = AppSettings.confidenceThreshold(this)
        if (percent < threshold) return
        if (sequence.lastOrNull() != label) sequence.add(label)
        if (sequence.size > 8) sequence.removeAt(0)
        tvSequence.text = sequence.joinToString("  →  ")
        tvSentence.text = SentenceAgent.generate(sequence)

        if (AppSettings.autoSpeak(this)) speakCurrentSentence()
    }

    private fun speakCurrentSentence() {
        val sentence = tvSentence.text.toString()
        if (sentence.startsWith("Your sentence")) return
        tts.setSpeechRate(AppSettings.speechRate(this))
        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "signova_output")
        HistoryStore.add(this, sequence.joinToString(" → ").ifBlank { tvGesture.text.toString() }, sentence)
    }

    private fun clearSequence() {
        sequence.clear()
        tvGesture.text = "Waiting..."
        tvConfidence.text = "--"
        progress.progress = 0
        tvSequence.text = "No gestures yet"
        tvSentence.text = "Your sentence will appear here."
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = when (AppSettings.language(this)) {
                "Hindi" -> Locale("hi", "IN")
                "Kannada" -> Locale("kn", "IN")
                "Malayalam" -> Locale("ml", "IN")
                "Tamil" -> Locale("ta", "IN")
                else -> Locale.US
            }
            tts.language = locale
        }
    }

    override fun onDestroy() {
        bluetooth.disconnect()
        tts.stop(); tts.shutdown()
        super.onDestroy()
    }
}
