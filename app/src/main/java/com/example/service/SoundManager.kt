package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object SoundManager {
    val AVAILABLE_RINGTONES = listOf(
        "Zen Bell",
        "Tibetan Bowl",
        "Wind Chimes",
        "Digital Marimba",
        "System Alarm",
        "System Notification"
    )

    val AMBIENT_SOUNDSCAPES = listOf(
        "None",
        "White Noise",
        "Rain Sounds",
        "Forest Sounds",
        "Ocean Waves",
        "Cozy Fireplace",
        "Soft Lofi Chords"
    )

    private var ambientJob: Job? = null
    private var ambientTrack: AudioTrack? = null
    private var isAmbientPlaying = false
    private var currentAmbientName = "None"
    private var ambientVolume = 0.5f

    fun playRingtone(context: Context, ringtoneName: String, vibrate: Boolean = true) {
        if (vibrate) {
            triggerVibration(context)
        }

        when (ringtoneName) {
            "System Alarm" -> playSystemSound(context, RingtoneManager.TYPE_ALARM)
            "System Notification" -> playSystemSound(context, RingtoneManager.TYPE_NOTIFICATION)
            "Zen Bell" -> playZenBell()
            "Tibetan Bowl" -> playTibetanBowl()
            "Wind Chimes" -> playWindChimes()
            "Digital Marimba" -> playMarimba()
            else -> playZenBell()
        }
    }

    private fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 300, 200, 400), -1)
            }
        } catch (_: Exception) {}
    }

    private fun playSystemSound(context: Context, type: Int) {
        try {
            val uri = RingtoneManager.getDefaultUri(type)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        } catch (_: Exception) {
            playZenBell()
        }
    }

    // Synthesized Calming Bell: Low frequency with harmonic overtones and long decay
    fun playZenBell() {
        CoroutineScope(Dispatchers.Default).launch {
            synthesizeTone(
                durationSec = 3.5,
                baseFreq = 261.63, // C4
                harmonics = listOf(1.0 to 1.0, 2.0 to 0.5, 3.01 to 0.25, 4.2 to 0.1),
                decayRate = 1.2
            )
        }
    }

    // Synthesized Tibetan Singing Bowl: Warm pulsing resonance
    fun playTibetanBowl() {
        CoroutineScope(Dispatchers.Default).launch {
            synthesizeTone(
                durationSec = 4.0,
                baseFreq = 196.0, // G3
                harmonics = listOf(1.0 to 1.0, 2.76 to 0.6, 5.4 to 0.3),
                decayRate = 0.8,
                tremoloRate = 3.0
            )
        }
    }

    // Synthesized Wind Chimes: Cascading high bells
    fun playWindChimes() {
        CoroutineScope(Dispatchers.Default).launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            for (i in 0..4) {
                val freq = notes[i % notes.size]
                launch {
                    synthesizeTone(
                        durationSec = 2.0,
                        baseFreq = freq,
                        harmonics = listOf(1.0 to 0.8, 2.0 to 0.3),
                        decayRate = 2.5
                    )
                }
                delay(120)
            }
        }
    }

    // Synthesized Marimba Tone
    fun playMarimba() {
        CoroutineScope(Dispatchers.Default).launch {
            val chords = listOf(329.63, 392.0, 493.88, 659.25)
            for (freq in chords) {
                launch {
                    synthesizeTone(
                        durationSec = 1.5,
                        baseFreq = freq,
                        harmonics = listOf(1.0 to 1.0, 4.0 to 0.15),
                        decayRate = 4.0
                    )
                }
                delay(100)
            }
        }
    }

    private fun synthesizeTone(
        durationSec: Double,
        baseFreq: Double,
        harmonics: List<Pair<Double, Double>>,
        decayRate: Double,
        tremoloRate: Double = 0.0
    ) {
        val sampleRate = 44100
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-decayRate * t)
            val tremolo = if (tremoloRate > 0) 1.0 + 0.15 * sin(2 * PI * tremoloRate * t) else 1.0

            var sample = 0.0
            for ((harmonicMult, amplitude) in harmonics) {
                sample += amplitude * sin(2 * PI * baseFreq * harmonicMult * t)
            }

            val finalSample = (sample * envelope * tremolo * 0.7 * Short.MAX_VALUE).coerceIn(
                Short.MIN_VALUE.toDouble(),
                Short.MAX_VALUE.toDouble()
            )
            buffer[i] = finalSample.toInt().toShort()
        }

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Auto release after playback
            CoroutineScope(Dispatchers.Default).launch {
                delay((durationSec * 1000).toLong() + 200)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    // --- Ambient Soundscapes ---
    fun startAmbientSound(name: String, volume: Float = 0.5f) {
        stopAmbientSound()
        if (name == "None") return

        currentAmbientName = name
        ambientVolume = volume
        isAmbientPlaying = true

        ambientJob = CoroutineScope(Dispatchers.Default).launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            try {
                ambientTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                ambientTrack?.play()
                val chunk = ShortArray(bufferSize / 2)
                var lastBrown = 0.0
                var lastPink1 = 0.0
                var lastPink2 = 0.0
                var phase = 0.0
                var wavePhase = 0.0
                var birdChirpCountdown = 0
                var birdFreq = 2200.0
                var birdPhase = 0.0
                var totalSampleCount = 0L

                while (isActive && isAmbientPlaying) {
                    for (i in chunk.indices) {
                        totalSampleCount++
                        val t = totalSampleCount.toDouble() / sampleRate

                        val sample = when (currentAmbientName) {
                            "White Noise" -> {
                                val white = Random.nextDouble(-1.0, 1.0)
                                // Soft pink-ish filter for comfortable smooth reading white noise
                                lastPink1 = 0.95 * lastPink1 + 0.05 * white
                                (lastPink1 * 2.5 + white * 0.15) * 0.35 * ambientVolume
                            }
                            "Rain Sounds", "Gentle Rain" -> {
                                // Filtered rain hiss + occasional rain drop impulse
                                val white = Random.nextDouble(-1.0, 1.0)
                                lastBrown = (lastBrown + (0.025 * white)) / 1.025
                                lastPink1 = 0.9 * lastPink1 + 0.1 * white
                                val raindrop = if (Random.nextDouble() > 0.998) Random.nextDouble(0.2, 0.6) else 0.0
                                (lastBrown * 0.6 + lastPink1 * 0.35 + raindrop) * 0.6 * ambientVolume
                            }
                            "Forest Sounds" -> {
                                // Rustling wind breeze + gentle bird chirps
                                val white = Random.nextDouble(-0.8, 0.8)
                                lastBrown = (lastBrown + (0.015 * white)) / 1.015
                                val windSwell = 0.5 + 0.5 * sin(2 * PI * 0.08 * t)
                                var birdSound = 0.0

                                if (birdChirpCountdown <= 0) {
                                    // Random chance to start bird chirp sequence
                                    if (Random.nextDouble() > 0.9995) {
                                        birdChirpCountdown = (sampleRate * Random.nextDouble(0.12, 0.25)).toInt()
                                        birdFreq = Random.nextDouble(1800.0, 3200.0)
                                    }
                                } else {
                                    birdChirpCountdown--
                                    birdFreq += sin(birdChirpCountdown * 0.05) * 25.0
                                    birdPhase += (2 * PI * birdFreq) / sampleRate
                                    val birdEnv = sin(PI * (1.0 - birdChirpCountdown.toDouble() / (sampleRate * 0.2)).coerceIn(0.0, 1.0))
                                    birdSound = sin(birdPhase) * birdEnv * 0.4
                                }

                                (lastBrown * (0.3 + 0.2 * windSwell) + birdSound) * 0.65 * ambientVolume
                            }
                            "Ocean Waves", "Deep Focus Noise" -> {
                                // Swelling wave tide cycle (approx 5 second periods)
                                val white = Random.nextDouble(-0.7, 0.7)
                                lastBrown = (lastBrown + (0.03 * white)) / 1.03
                                wavePhase += (2 * PI * 0.18) / sampleRate
                                val waveSwell = (0.2 + 0.8 * (0.5 + 0.5 * sin(wavePhase)))
                                (lastBrown * waveSwell * 1.2) * 0.7 * ambientVolume
                            }
                            "Cozy Fireplace" -> {
                                val white = Random.nextDouble(-0.4, 0.4)
                                lastBrown = (lastBrown + (0.012 * white)) / 1.012
                                val crackle = if (Random.nextDouble() > 0.996) Random.nextDouble(-0.85, 0.85) else 0.0
                                val pop = if (Random.nextDouble() > 0.9997) Random.nextDouble(-1.0, 1.0) else 0.0
                                (lastBrown * 0.4 + crackle * 0.5 + pop * 0.6) * 0.65 * ambientVolume
                            }
                            "Soft Lofi Chords" -> {
                                // 4-chord slow relaxing progression: Cmaj7 -> Am7 -> Fmaj7 -> G
                                val chordDuration = 4.0 // 4 seconds per chord
                                val chordIndex = ((t / chordDuration).toInt()) % 4
                                val chordFreqs = when (chordIndex) {
                                    0 -> listOf(130.81, 164.81, 196.00, 246.94) // Cmaj7 (C3, E3, G3, B3)
                                    1 -> listOf(110.00, 130.81, 164.81, 196.00) // Am7 (A2, C3, E3, G3)
                                    2 -> listOf(87.31, 130.81, 174.61, 220.00)  // Fmaj7 (F2, C3, F3, A3)
                                    else -> listOf(98.00, 123.47, 146.83, 174.61) // G7 (G2, B2, D3, F3)
                                }
                                phase += (2 * PI) / sampleRate
                                var chordSample = 0.0
                                for (freq in chordFreqs) {
                                    chordSample += sin(phase * freq) * 0.25
                                }
                                val lofiTremolo = 1.0 + 0.12 * sin(2 * PI * 4.0 * t)
                                (chordSample * lofiTremolo * 0.4) * ambientVolume
                            }
                            else -> 0.0
                        }

                        val pcm = (sample * Short.MAX_VALUE).coerceIn(
                            Short.MIN_VALUE.toDouble(),
                            Short.MAX_VALUE.toDouble()
                        ).toInt().toShort()
                        chunk[i] = pcm
                    }
                    ambientTrack?.write(chunk, 0, chunk.size)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    ambientTrack?.stop()
                    ambientTrack?.release()
                    ambientTrack = null
                } catch (_: Exception) {}
            }
        }
    }

    fun setAmbientVolume(volume: Float) {
        ambientVolume = volume.coerceIn(0f, 1f)
    }

    fun stopAmbientSound() {
        isAmbientPlaying = false
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.stop()
            ambientTrack?.release()
            ambientTrack = null
        } catch (_: Exception) {}
    }

    fun isPlaying(): Boolean = isAmbientPlaying
    fun getActiveAmbient(): String = currentAmbientName
    fun getVolume(): Float = ambientVolume
}

