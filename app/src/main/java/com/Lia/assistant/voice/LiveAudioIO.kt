package com.Lia.assistant.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.sqrt

class LiveAudioIO(
    private val context: Context,
    private val onMicChunk: (ByteArray) -> Unit,
    private val onMicAmplitude: (Float) -> Unit
) {
    companion object {
        const val TAG = "LiveAudioIO"
        const val MIC_SAMPLE_RATE = 16000
        const val SPK_SAMPLE_RATE = 24000
        const val CHUNK_SIZE = 2048

        fun calculateRms(pcmBytes: ByteArray, length: Int = pcmBytes.size): Float {
            if (length < 2) return 0f
            var sum = 0.0
            val samples = length / 2
            var idx = 0
            while (idx < length - 1) {
                val b1 = pcmBytes[idx].toInt() and 0xFF
                val b2 = pcmBytes[idx + 1].toInt()
                val sample = (b2 shl 8) or b1
                sum += (sample * sample).toDouble()
                idx += 2
            }
            val mean = sum / samples
            val rms = sqrt(mean) / 32768.0
            return rms.coerceIn(0.0, 1.0).toFloat()
        }
    }

    private var audioRecord: AudioRecord? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null

    private var audioTrack: AudioTrack? = null
    private val isRecording = AtomicBoolean(false)
    private var recordingThread: Thread? = null

    init {
        configureAudioRouting()
    }

    private fun configureAudioRouting() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let { am ->
                am.mode = AudioManager.MODE_NORMAL
                am.isSpeakerphoneOn = true
                Log.i(TAG, "Configured AudioManager to MODE_NORMAL and speakerphone ON")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to configure speakerphone", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording.get()) return

        configureAudioRouting()

        try {
            val minBuf = AudioRecord.getMinBufferSize(
                MIC_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = max(minBuf, 4096)

            // AudioSource.MIC avoids Android locking output to the phone call earpiece
            var record: AudioRecord? = null
            try {
                record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    MIC_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            } catch (e: Exception) {
                Log.w(TAG, "AudioSource.MIC failed: ${e.message}")
            }

            if (record == null || record.state != AudioRecord.STATE_INITIALIZED) {
                record?.release()
                record = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MIC_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            }

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                record.release()
                return
            }

            val audioSessionId = record.audioSessionId
            if (AcousticEchoCanceler.isAvailable()) {
                try {
                    echoCanceler = AcousticEchoCanceler.create(audioSessionId)?.apply { enabled = true }
                } catch (_: Exception) {}
            }
            if (NoiseSuppressor.isAvailable()) {
                try {
                    noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply { enabled = true }
                } catch (_: Exception) {}
            }
            if (AutomaticGainControl.isAvailable()) {
                try {
                    gainControl = AutomaticGainControl.create(audioSessionId)?.apply { enabled = true }
                } catch (_: Exception) {}
            }

            record.startRecording()
            audioRecord = record
            isRecording.set(true)
            Log.i(TAG, "Microphone recording started at ${MIC_SAMPLE_RATE}Hz")

            recordingThread = Thread({
                val buffer = ByteArray(CHUNK_SIZE)
                while (isRecording.get()) {
                    val activeRecord = audioRecord ?: break
                    val read = activeRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val chunk = if (read == CHUNK_SIZE) buffer.clone() else buffer.copyOf(read)
                        val amp = calculateRms(chunk, read)
                        onMicAmplitude(amp)
                        onMicChunk(chunk)
                    }
                }
            }, "LiaLiveAudioRecordThread").apply { start() }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioRecord", e)
            isRecording.set(false)
        }
    }

    fun stopRecording() {
        isRecording.set(false)
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            echoCanceler?.release()
            echoCanceler = null
            noiseSuppressor?.release()
            noiseSuppressor = null
            gainControl?.release()
            gainControl = null
        } catch (_: Exception) {}
    }

    @Synchronized
    private fun getOrCreateAudioTrack(): AudioTrack {
        audioTrack?.let { return it }

        val minBuf = AudioTrack.getMinBufferSize(
            SPK_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBuf * 4, 8192)

        // USAGE_MEDIA plays loudly through the device's main speakers
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SPK_SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val track = AudioTrack(
            attributes,
            format,
            bufferSize,
            AudioTrack.MODE_STREAM,
            android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        track.setVolume(1.0f)
        track.play()
        audioTrack = track
        Log.i(TAG, "AudioTrack initialized at ${SPK_SAMPLE_RATE}Hz with max volume")
        return track
    }

    @Synchronized
    fun playChunk(pcmBytes: ByteArray): Float {
        if (pcmBytes.isEmpty()) return 0f
        return try {
            val track = getOrCreateAudioTrack()
            track.write(pcmBytes, 0, pcmBytes.size)
            calculateRms(pcmBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio chunk", e)
            0f
        }
    }

    @Synchronized
    fun flushPlayback() {
        try {
            audioTrack?.let { track ->
                track.pause()
                track.flush()
                track.play()
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    fun setVolume(volume: Float) {
        try {
            audioTrack?.setVolume(volume.coerceIn(0f, 1f))
        } catch (_: Exception) {}
    }

    fun release() {
        stopRecording()
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }
}
