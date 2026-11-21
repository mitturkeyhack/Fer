package com.erkam.fer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/** Uygulamanın tüm kalıcı verisi. İnternet yok; her şey telefonda. */
object Store {
    private lateinit var p: SharedPreferences

    /** Compose ekranları bunu okuyarak veri değişince yeniden çizilir. */
    val version = mutableIntStateOf(0)

    const val IG = "com.instagram.android"
    const val YT = "com.google.android.youtube"
    const val TT = "com.zhiliaoapp.musically"
    const val TT2 = "com.ss.android.ugc.trill"
    const val FB = "com.facebook.katana"
    const val SC = "com.snapchat.android"

    data class AppInfo(val pkg: String, val name: String, val ready: Boolean)

    val APPS = listOf(
        AppInfo(IG, "Instagram Reels", true),
        AppInfo(YT, "YouTube Shorts", true),
        AppInfo(TT, "TikTok", true),
        AppInfo(FB, "Facebook Reels", false),
        AppInfo(SC, "Snapchat Spotlight", false),
    )

    fun init(ctx: Context) {
        if (!::p.isInitialized) p = ctx.applicationContext.getSharedPreferences("fer", Context.MODE_PRIVATE)
    }

    private fun changed() {
        version.intValue = version.intValue + 1
    }

    private fun b(k: String, d: Boolean) = p.getBoolean(k, d)
    private fun setB(k: String, v: Boolean) { p.edit().putBoolean(k, v).apply(); changed() }
    private fun i(k: String, d: Int) = p.getInt(k, d)
    private fun setI(k: String, v: Int) { p.edit().putInt(k, v).apply(); changed() }
    private fun s(k: String, d: String) = p.getString(k, d) ?: d
    private fun setS(k: String, v: String) { p.edit().putString(k, v).apply(); changed() }

    // ---- profil ----
    var onboarded: Boolean get() = b("onboarded", false); set(v) = setB("onboarded", v)
    var name: String get() = s("name", "Pamuk"); set(v) = setS("name", v)
    var girl: Boolean get() = b("girl", true); set(v) = setB("girl", v)
    var guess: Int get() = i("guess", 200); set(v) = setI("guess", v)
    var limit: Int get() = i("limit", 100); set(v) = setI("limit", v)
    var goals: String get() = s("goals", ""); set(v) = setS("goals", v)
    var answers: String get() = s("answers", ""); set(v) = setS("answers", v)

    // ---- kalkan ----
    var nightLock: Boolean get() = b("nightLock", true); set(v) = setB("nightLock", v)
    var limitLock: Boolean get() = b("limitLock", true); set(v) = setB("limitLock", v)
    var hideReels: Boolean get() = b("hideReels", false); set(v) = setB("hideReels", v)
    var strict: Boolean get() = b("strict", false); set(v) = setB("strict", v)
    var bedHour: Int get() = i("bedHour", 23); set(v) = setI("bedHour", v)
    var wakeHour: Int get() = i("wakeHour", 7); set(v) = setI("wakeHour", v)

    // ---- his ----
    var sound: Boolean get() = b("sound", true); set(v) = setB("sound", v)
    var haptic: Boolean get() = b("haptic", true); set(v) = setB("haptic", v)
    var manevi: Boolean get() = b("manevi", false); set(v) = setB("manevi", v)
    var bubbleSize: Int get() = i("bubbleSize", 1); set(v) = setI("bubbleSize", v)

    fun appEnabled(pkg: String): Boolean {
        val key = if (pkg == TT2) TT else pkg
        val info = APPS.firstOrNull { it.pkg == key } ?: return false
        return info.ready && b("app_$key", true)
    }
    fun setAppEnabled(pkg: String, v: Boolean) = setB("app_$pkg", v)

    // ---- ertele / sınırsız ----
    var snoozeUntil: Long get() = p.getLong("snoozeUntil", 0L); set(v) { p.edit().putLong("snoozeUntil", v).apply(); changed() }
    var unlimitedDay: String get() = s("unlimitedDay", ""); set(v) = setS("unlimitedDay", v)
    fun snoozed() = System.currentTimeMillis() < snoozeUntil
    fun unlimitedToday() = unlimitedDay == today()

    fun nightNow(): Boolean {
        val h = LocalTime.now().hour
        val bed = bedHour
        val wake = wakeHour
        return if (bed > wake) h >= bed || h < wake else h in bed until wake
    }

    // ---- sayımlar ----
    fun today(): String = LocalDate.now().toString()
    fun day(offset: Long): String = LocalDate.now().minusDays(offset).toString()
    fun count(d: String = today()) = p.getInt("c_$d", 0)
    fun appCount(pkg: String, d: String = today()) = p.getInt("a_${d}_$pkg", 0)
    fun hasDay(d: String) = p.contains("c_$d")

    fun addReel(pkg: String): Int {
        val key = if (pkg == TT2) TT else pkg
        val d = today()
        val n = count(d) + 1
        p.edit()
            .putInt("c_$d", n)
            .putInt("a_${d}_$key", appCount(key, d) + 1)
            .putString("lastDay", d)
            .apply()
        changed()
        return n
    }

    /** Gün başladığında bugünün kaydını sıfırla (kayıt yoksa 0 yazar, seri hesabı için). */
    fun touchToday() {
        val d = today()
        if (!p.contains("c_$d")) p.edit().putInt("c_$d", 0).apply()
    }

    fun topAppToday(): String {
        val d = today()
        val best = APPS.maxByOrNull { appCount(it.pkg, d) } ?: return "—"
        if (appCount(best.pkg, d) == 0) return "—"
        return best.name.substringBefore(" ")
    }

    /** 0 parlak · 1 yorgun · 2 bulanık · 3 sönük · 4 karanlık */
    fun stage(c: Int = count(), l: Int = limit): Int {
        val r = c.toFloat() / l.coerceAtLeast(1)
        return when {
            r < 0.2f -> 0
            r < 0.45f -> 1
            r < 0.7f -> 2
            r < 1f -> 3
            else -> 4
        }
    }

    fun minutes(c: Int = count()) = (c * 20 + 30) / 60

    /** Dünden geriye, sınırın altında kalınan art arda gün sayısı (+ bugün sınırın altındaysa). */
    fun streak(): Int {
        var n = 0
        var off = 1L
        while (off < 400) {
            val d = day(off)
            if (!hasDay(d)) break
            if (count(d) > limit) break
            n++; off++
        }
        if (count() <= limit && n > 0) n++
        return n
    }

    fun week(): List<Pair<String, Int>> {
        val names = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        return (6 downTo 0).map { off ->
            val date = LocalDate.now().minusDays(off.toLong())
            val label = if (off == 0) "Bugün" else names[date.dayOfWeek.value - 1]
            label to count(date.toString())
        }
    }

    fun weekTotal(startOffset: Long): Int = (startOffset until startOffset + 7).sumOf { count(day(it)) }

    // ---- defter ----
    fun notes(): List<Pair<String, String>> {
        val arr = JSONArray(s("notes", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            o.optString("d") to o.optString("t")
        }.reversed()
    }

    fun addNote(text: String) {
        val arr = JSONArray(s("notes", "[]"))
        arr.put(JSONObject().put("d", today()).put("t", text.trim()))
        setS("notes", arr.toString())
    }

    fun noteToday(): String? = notes().firstOrNull { it.first == today() }?.second
}
