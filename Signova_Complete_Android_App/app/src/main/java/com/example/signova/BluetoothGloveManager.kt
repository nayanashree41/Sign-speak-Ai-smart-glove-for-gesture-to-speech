package com.example.signova

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlin.concurrent.thread

class BluetoothGloveManager(
    private val context: Context,
    private val onStatus: (String) -> Unit,
    private val onSensorLine: (String) -> Unit
) {
    private var socket: BluetoothSocket? = null
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private fun hasBluetoothPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        if (!hasBluetoothPermission()) {
            onStatus("Bluetooth permission required")
            return
        }
        val manager = context.getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter = manager?.adapter ?: run {
            onStatus("Bluetooth not supported")
            return
        }
        if (!adapter.isEnabled) {
            onStatus("Turn on Bluetooth")
            return
        }
        val device = adapter.bondedDevices.firstOrNull {
            val n = it.name?.lowercase().orEmpty()
            n.contains("signova") || n.contains("esp32")
        }
        if (device == null) {
            onStatus("Pair Signova_Glove / ESP32 in phone Bluetooth settings first")
            return
        }

        onStatus("Connecting to ${device.name}...")
        thread {
            try {
                socket?.close()
                socket = device.createRfcommSocketToServiceRecord(sppUuid)
                socket?.connect()
                onStatus("Connected: ${device.name}")
                val reader = BufferedReader(InputStreamReader(socket!!.inputStream))
                while (socket?.isConnected == true) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) onSensorLine(line.trim())
                }
            } catch (e: Exception) {
                onStatus("Connection failed: ${e.message ?: "unknown error"}")
            }
        }
    }

    fun disconnect() {
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        onStatus("Disconnected")
    }
}
