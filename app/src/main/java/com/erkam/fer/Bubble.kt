package com.erkam.fer

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/** Kısa video oynatıcısının üstünde duran canlı enik kabarcığı. */
class Bubble(private val svc: AccessibilityService) {
    private val wm = svc.getSystemService(WindowManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var root: LinearLayout? = null
    private lateinit var face: ImageView
    private lateinit var faceRing: FrameLayout
    private lateinit var countText: TextView
    private lateinit var barFill: View
    private var shown = false
    private var stage = -1
    private var blinking = false
    private var scale = 1f

    private val faces = intArrayOf(R.drawable.dog0, R.drawable.dog1, R.drawable.dog2, R.drawable.dog3, R.drawable.dog4)
    private val blinks = intArrayOf(R.drawable.blink0, R.drawable.blink1, 0, R.drawable.blink3, R.drawable.blink4)
    private val rings = intArrayOf(0xFF3A2C17.toInt(), 0xFF33291B.toInt(), 0xFF2B2620.toInt(), 0xFF25241F.toInt(), 0xFFE9E6DF.toInt())

    private fun dp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v * scale, svc.resources.displayMetrics).toInt()

    private fun build() {
        scale = when (Store.bubbleSize) { 0 -> 0.85f; 2 -> 1.2f; else -> 1f }
        val r = LinearLayout(svc).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4f), dp(4f), dp(14f), dp(4f))
            background = GradientDrawable().apply {
                cornerRadius = dp(999f).toFloat()
                setColor(0xE00B0C10.toInt())
                setStroke(dp(1f).coerceAtLeast(1), 0x29FFFFFF)
            }
            elevation = dp(6f).toFloat()
        }
        faceRing = FrameLayout(svc).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(rings[0]) }
        }
        face = ImageView(svc).apply { setImageResource(R.drawable.dog0); scaleType = ImageView.ScaleType.FIT_CENTER }
        faceRing.addView(face, FrameLayout.LayoutParams(dp(34f), dp(34f), Gravity.CENTER))
        r.addView(faceRing, LinearLayout.LayoutParams(dp(40f), dp(40f)))

        val col = LinearLayout(svc).apply { orientation = LinearLayout.VERTICAL }
        countText = TextView(svc).apply {
            setTextColor(Color.parseColor("#F5F3EE"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f * scale)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            includeFontPadding = false
        }
        col.addView(countText)
        val track = FrameLayout(svc).apply {
            background = GradientDrawable().apply { cornerRadius = dp(2f).toFloat(); setColor(0x24FFFFFF) }
        }
        barFill = View(svc).apply {
            background = GradientDrawable().apply { cornerRadius = dp(2f).toFloat(); setColor(Color.parseColor("#FFB547")) }
        }
        track.addView(barFill, FrameLayout.LayoutParams(0, dp(4f)))
        col.addView(track, LinearLayout.LayoutParams(dp(56f), dp(4f)).apply { topMargin = dp(5f) })
        r.addView(col, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { leftMargin = dp(9f) })
        root = r
    }

    private fun params(): WindowManager.LayoutParams {
        val statusBar = svc.resources.getIdentifier("status_bar_height", "dimen", "android").let {
            if (it > 0) svc.resources.getDimensionPixelSize(it) else dp(24f)
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            // Uygulamanın üst başlığını (ör. "Reels", "Senin için") kapatmamak için biraz aşağıda.
            y = statusBar + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58f, svc.resources.displayMetrics).toInt()
        }
    }

    fun show(count: Int, limit: Int) {
        if (!shown) {
            build()
            try {
                wm.addView(root, params())
                shown = true
                stage = -1
                root?.alpha = 0f
                root?.animate()?.alpha(1f)?.setDuration(180)?.start()
                scheduleBlink()
            } catch (e: Exception) {
                shown = false
                root = null
                return
            }
        }
        render(count, limit, false)
    }

    fun hide() {
        if (!shown) return
        handler.removeCallbacksAndMessages(null)
        try { wm.removeView(root) } catch (_: Exception) {}
        root = null
        shown = false
    }

    fun isShown() = shown

    /** Sayı arttığında: zıplama ve gerekirse aşama değişimi. Aşama değiştiyse true döner. */
    fun bump(count: Int, limit: Int): Boolean {
        if (!shown) return false
        val changed = render(count, limit, true)
        face.animate().cancel()
        face.animate().scaleX(1.18f).scaleY(1.18f).translationY(-dp(3f).toFloat()).setDuration(110).withEndAction {
            face.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(160).start()
        }.start()
        return changed
    }

    private fun render(count: Int, limit: Int, animate: Boolean): Boolean {
        val s = Store.stage(count, limit)
        countText.text = count.toString()
        val ratio = (count.toFloat() / limit.coerceAtLeast(1)).coerceIn(0f, 1f)
        barFill.layoutParams = barFill.layoutParams.apply { width = (dp(56f) * ratio).toInt() }
        barFill.requestLayout()
        (barFill.background as? GradientDrawable)?.setColor(if (count >= limit) Color.parseColor("#FF6B5A") else Color.parseColor("#FFB547"))
        val changed = s != stage
        if (changed) {
            stage = s
            (faceRing.background as? GradientDrawable)?.setColor(rings[s])
            if (!blinking) face.setImageResource(faces[s])
        }
        return changed && animate
    }

    private fun scheduleBlink() {
        handler.postDelayed({
            if (!shown) return@postDelayed
            val b = blinks.getOrElse(stage) { 0 }
            if (b != 0) {
                blinking = true
                face.setImageResource(b)
                handler.postDelayed({
                    blinking = false
                    if (shown && stage >= 0) face.setImageResource(faces[stage])
                    scheduleBlink()
                }, 170)
            } else {
                scheduleBlink()
            }
        }, 4000L + Random.nextLong(4000L))
    }
}
