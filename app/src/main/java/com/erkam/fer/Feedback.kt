package com.erkam.fer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/** Ses ve titreşim — premium his için tek yerden yönetilir. */
object Feedback {
    private var pool: SoundPool? = null
    private val ids = HashMap<String, Int>()
    private var vib: Vibrator? = null

    fun init(ctx: Context) {
        Store.init(ctx)
        if (vib == null) {
            @Suppress("DEPRECATION")
            vib = ctx.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (pool == null) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val sp = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
            val app = ctx.applicationContext
            ids["tick"] = sp.load(app, R.raw.tick, 1)
            ids["pop"] = sp.load(app, R.raw.pop, 1)
            ids["chime"] = sp.load(app, R.raw.chime, 1)
            ids["whoosh"] = sp.load(app, R.raw.whoosh, 1)
            ids["thud"] = sp.load(app, R.raw.thud, 1)
            pool = sp
        }
    }

    fun sound(name: String, volume: Float = 0.7f) {
        if (!Store.sound) return
        val id = ids[name] ?: return
        pool?.play(id, volume, volume, 1, 0, 1f)
    }

    private fun predefined(effect: Int, fallbackMs: Long, amp: Int) {
        if (!Store.haptic) return
        val v = vib ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 29) {
            v.vibrate(VibrationEffect.createPredefined(effect))
        } else {
            v.vibrate(VibrationEffect.createOneShot(fallbackMs, amp))
        }
    }

    /** Çok hafif tık: seçimler, sayı değişimi. */
    fun tick() = predefined(VibrationEffect.EFFECT_TICK, 8, 60)

    /** Düğme basışı. */
    fun click() = predefined(VibrationEffect.EFFECT_CLICK, 14, 120)

    /** Aşama değişimi. */
    fun double() = predefined(VibrationEffect.EFFECT_DOUBLE_CLICK, 30, 150)

    /** Sınır doldu, söz tamam. */
    fun heavy() = predefined(VibrationEffect.EFFECT_HEAVY_CLICK, 40, 255)

    /** Kalp atışı (söz verirken). */
    fun heartbeat() {
        if (!Store.haptic) return
        val v = vib ?: return
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 28, 110, 40), intArrayOf(0, 140, 0, 200), -1))
    }

    /** Kutlama: neşeli üçlü. */
    fun celebrate() {
        if (!Store.haptic) return
        val v = vib ?: return
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 70, 20, 70, 40), intArrayOf(0, 160, 0, 160, 0, 255), -1))
    }
}
