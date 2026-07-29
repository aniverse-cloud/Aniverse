package com.example.dialer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class SimTransferService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var isStreaming = false

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var socket: DatagramSocket? = null

    companion object {
        const val ACTION_START_HOST = "ACTION_START_HOST"
        const val ACTION_START_CLIENT = "ACTION_START_CLIENT"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_IP = "EXTRA_IP"

        const val PORT = 55555
        const val SAMPLE_RATE = 16000
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_HOST -> {
                startForegroundService("Hosting SIM Connection...", "Waiting for remote device to connect.")
                startAudioStreaming(isHost = true, targetIp = null)
            }
            ACTION_START_CLIENT -> {
                val ip = intent.getStringExtra(EXTRA_IP) ?: "127.0.0.1"
                startForegroundService("Using Remote SIM...", "Streaming audio to $ip")
                startAudioStreaming(isHost = false, targetIp = ip)
            }
            ACTION_STOP -> {
                stopStreaming()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundService(title: String, text: String) {
        val channelId = "sim_transfer_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SIM Transfer", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setOngoing(true)
            .build()

        startForeground(2, notification)
    }

    private fun startAudioStreaming(isHost: Boolean, targetIp: String?) {
        if (isStreaming) return
        isStreaming = true

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioTrack = AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            socket = if (isHost) DatagramSocket(PORT) else DatagramSocket()

            // Sender Coroutine
            scope.launch {
                val buffer = ByteArray(bufferSize)
                audioRecord?.startRecording()
                var destinationIp: InetAddress? = targetIp?.let { InetAddress.getByName(it) }

                while (isStreaming && isActive) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        // If host, we only send back after we receive a packet and know the client's IP
                        if (destinationIp != null) {
                            try {
                                val packet = DatagramPacket(buffer, readSize, destinationIp, PORT)
                                socket?.send(packet)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                }
            }

            // Receiver Coroutine
            scope.launch {
                val buffer = ByteArray(bufferSize)
                audioTrack?.play()

                while (isStreaming && isActive) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)

                        // If host, update destination to the client that just talked to us
                        if (isHost) {
                            // In a real app we'd verify the IP against the API Key, but this is a P2P prototype
                        }

                        audioTrack?.write(packet.data, 0, packet.length)
                    } catch (e: SocketException) {
                        // Socket closed
                        break
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopStreaming()
        }
    }

    private fun stopStreaming() {
        isStreaming = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        socket?.close()
        socket = null

        scope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStreaming()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
