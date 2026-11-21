package com.erkam.fer

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

/**
 * Akıllı sayaç:
 * - Sadece kısa video oynatıcısındayken kabarcığı gösterir, çıkınca gizler.
 * - Ekrandaki videonun "imzasını" (görünen yazılar) çıkarır; yeni bir imza 2 kez üst üste
 *   aynı görünürse 1 reel sayar. Aynı reele geri dönünce tekrar saymaz. Reklamları saymaz.
 */
class FerService : AccessibilityService() {

    companion object {
        @Volatile var instance: FerService? = null

        private val AD_WORDS = setOf("sponsorlu", "sponsored", "reklam", "ad", "promoted", "sponsorlu içerik", "reklamı atla", "skip ad")
        private val NUMBERISH = Regex("^[\\d.,\\s:%+]*[\\d]+[\\d.,\\s:%]*(b|bn|k|m|mn|b\\.)?$", RegexOption.IGNORE_CASE)

        private val VIEWER_IDS = mapOf(
            Store.IG to listOf("com.instagram.android:id/clips_viewer_view_pager", "com.instagram.android:id/clips_viewer_pager"),
            Store.YT to listOf("com.google.android.youtube:id/reel_recycler", "com.google.android.youtube:id/reel_player_page_container", "com.google.android.youtube:id/reel_watch_player"),
        )
        private val VIEWER_HINTS = mapOf(
            Store.IG to listOf("clips_viewer"),
            Store.YT to listOf("reel_recycler", "reel_player", "reel_watch"),
        )
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bubble: Bubble
    private var checkPending = false
    private var lastCheck = 0L

    private var lastSig: String? = null
    private var candidate: String? = null
    private var candidateAt = 0L
    private val seen = LinkedHashSet<String>()
    private var seenDay = ""
    private var lastMola = 0L
    private var lastBack = 0L
    private var lastWidget = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        Store.init(this)
        Feedback.init(this)
        bubble = Bubble(this)
        instance = this
    }

    override fun onDestroy() {
        instance = null
        if (::bubble.isInitialized) bubble.hide()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        scheduleCheck(if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) 60L else 250L)
    }

    private fun scheduleCheck(delay: Long) {
        if (checkPending) return
        checkPending = true
        val since = SystemClock.uptimeMillis() - lastCheck
        handler.postDelayed({
            checkPending = false
            lastCheck = SystemClock.uptimeMillis()
            try { check() } catch (_: Exception) {}
        }, if (since < 300) maxOf(delay, 300 - since) else delay)
    }

    private fun check() {
        if (!::bubble.isInitialized) return
        val root = rootInActiveWindow
        val pkg = root?.packageName?.toString()
        if (root == null || pkg == null || pkg == packageName || !Store.onboarded || !Store.appEnabled(pkg)) {
            bubble.hide()
            return
        }
        val viewer = findViewer(root, pkg)
        if (viewer == null) {
            bubble.hide()
            candidate = null
            return
        }

        val now = System.currentTimeMillis()

        // Gece kilidi
        if (Store.nightLock && Store.nightNow() && !Store.snoozed()) {
            bubble.hide()
            openMola(night = true)
            return
        }
        // Reels'i gizle (Instagram)
        if (Store.hideReels && pkg == Store.IG) {
            bubble.hide()
            if (now - lastBack > 1500) {
                lastBack = now
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(this, "Fer: Reels kapalı", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (seenDay != Store.today()) {
            seenDay = Store.today()
            seen.clear()
            Store.touchToday()
        }

        val limit = Store.limit
        bubble.show(Store.count(), limit)

        val texts = visibleTexts(viewer)
        if (texts.isEmpty()) return
        val sig = texts.take(8).joinToString("|")
        if (sig == lastSig) return

        if (sig != candidate) {
            candidate = sig
            candidateAt = SystemClock.uptimeMillis()
            scheduleCheck(650)
            return
        }
        if (SystemClock.uptimeMillis() - candidateAt < 550) {
            scheduleCheck(300)
            return
        }

        lastSig = sig
        if (texts.any { it.lowercase() in AD_WORDS || it.lowercase().startsWith("sponsorlu") }) return
        if (!seen.add(sig)) return
        if (seen.size > 600) seen.remove(seen.first())

        val n = Store.addReel(pkg)
        val stageChanged = bubble.bump(n, limit)
        Feedback.tick()
        Feedback.sound("pop", 0.35f)
        if (stageChanged) {
            Feedback.double()
        }
        updateWidget(force = false)

        if (n >= limit && Store.limitLock && !Store.snoozed() && !Store.unlimitedToday()) {
            Feedback.heavy()
            Feedback.sound("thud", 0.6f)
            openMola(night = false)
        }
    }

    private fun findViewer(root: AccessibilityNodeInfo, pkg: String): AccessibilityNodeInfo? {
        VIEWER_IDS[pkg]?.forEach { id ->
            val found = root.findAccessibilityNodeInfosByViewId(id)
            val v = found?.firstOrNull { it.isVisibleToUser }
            if (v != null) return v
        }
        val hints = VIEWER_HINTS[pkg]
        return when {
            hints != null -> traverseFind(root, 900) { n ->
                val id = n.viewIdResourceName ?: return@traverseFind false
                n.isVisibleToUser && hints.any { id.contains(it) }
            }
            pkg == Store.TT || pkg == Store.TT2 -> fullScreenPager(root)
            else -> null
        }
    }

    /** TikTok: ekranı kaplayan dikey video sayfalayıcısı. */
    private fun fullScreenPager(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val screen = Rect()
        root.getBoundsInScreen(screen)
        val r = Rect()
        return traverseFind(root, 700) { n ->
            val cls = n.className?.toString() ?: return@traverseFind false
            if (!cls.contains("ViewPager") && !cls.contains("RecyclerView")) return@traverseFind false
            if (!n.isVisibleToUser) return@traverseFind false
            n.getBoundsInScreen(r)
            r.height() >= screen.height() * 0.7f && r.width() >= screen.width() * 0.95f
        }
    }

    private fun traverseFind(root: AccessibilityNodeInfo, max: Int, test: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < max) {
            val n = queue.removeFirst()
            visited++
            if (test(n)) return n
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
        }
        return null
    }

    /** Görünen videonun yazıları (kullanıcı adı, açıklama, müzik…). Sayılar hariç (beğeni sayısı değişebilir). */
    private fun visibleTexts(viewer: AccessibilityNodeInfo): List<String> {
        val out = ArrayList<String>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(viewer)
        var visited = 0
        while (queue.isNotEmpty() && visited < 500 && out.size < 12) {
            val n = queue.removeFirst()
            visited++
            if (n.isVisibleToUser) {
                for (raw in listOf(n.text, n.contentDescription)) {
                    val t = raw?.toString()?.trim() ?: continue
                    if (t.length < 2 || t.length > 120) continue
                    if (NUMBERISH.matches(t)) continue
                    if (t !in out) out.add(t)
                }
            }
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
        }
        return out
    }

    private fun openMola(night: Boolean) {
        val now = System.currentTimeMillis()
        if (now - lastMola < 4000) return
        lastMola = now
        val i = Intent(this, MolaActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("night", night)
        try { startActivity(i) } catch (_: Exception) {}
    }

    fun goHome() {
        bubble.hide()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun refreshBubbleStyle() {
        if (::bubble.isInitialized) bubble.hide()
    }

    private fun updateWidget(force: Boolean) {
        val now = SystemClock.uptimeMillis()
        if (!force && now - lastWidget < 3000) return
        lastWidget = now
        FerWidget.updateAll(this)
    }
}
