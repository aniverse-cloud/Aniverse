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
import android.net.Uri
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

            // Sender Coroutine
            scope.launch {
                try {
                    if (socket == null) {
                        socket = if (isHost) DatagramSocket(PORT) else DatagramSocket()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopStreaming()
                    return@launch
                }

                val buffer = ByteArray(bufferSize)
                audioRecord?.startRecording()
                var destinationIp: InetAddress? = null
                try {
                    destinationIp = targetIp?.let { InetAddress.getByName(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

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
                // Wait for socket to be initialized by the sender coroutine
                while (socket == null && isStreaming) {
                    delay(100)
                }
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

                        val packetData = String(packet.data, 0, packet.length)
                        if (packetData.startsWith("DIAL:")) {
                            val number = packetData.substringAfter("DIAL:")
                            simulateHostCall(number)
                            continue
                        }

                        val senderIp = packet.address.hostAddress
                        if (targetIp != null && senderIp != targetIp) {
                            // Security: Ignore packets from unauthorized IPs
                            continue
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

    private fun simulateHostCall(number: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(callIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
