package com.erkam.fer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.erkam.fer.Feedback
import com.erkam.fer.FerService
import com.erkam.fer.FerWidget
import com.erkam.fer.R
import com.erkam.fer.Store
import com.erkam.fer.Util
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.abs

@Composable
fun MainScreens() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var page by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = page.isNotEmpty() || tab != 0) { if (page.isNotEmpty()) page = "" else tab = 0 }
    // Uygulamaya her dönüşte sayıları tazele
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) { Store.touchToday(); Store.version.intValue++ } }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    Box(Modifier.fillMaxSize().background(C.Bg)) {
        when (page) {
            "ayarlar" -> Ayarlar(onBack = { page = "" })
            "bilim" -> Bilim(onBack = { page = "" })
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    Crossfade(targetState = tab, animationSpec = tween(180), label = "tab") { t ->
                        when (t) {
                            0 -> Ev(openAyarlar = { page = "ayarlar" }, openBilim = { page = "bilim" }, openKalkan = { tab = 1 })
                            1 -> Kalkan()
                            2 -> Dostlar()
                            else -> Defter(openBilim = { page = "bilim" })
                        }
                    }
                }
                BottomBar(tab) { tab = it }
            }
        }
    }
}

@Composable
private fun BottomBar(tab: Int, onTab: (Int) -> Unit) {
    val items = listOf<Pair<String, ImageVector>>("Ev" to Icons.home, "Kalkan" to Icons.shield, "Dostlar" to Icons.friends, "Defter" to Icons.book)
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
        Row(Modifier.fillMaxWidth().background(C.Nav).padding(horizontal = 14.dp, vertical = 10.dp)) {
            items.forEachIndexed { i, (label, icon) ->
                val on = i == tab
                Column(
                    Modifier.weight(1f).tap(haptic = false) { if (!on) { Feedback.tick(); onTab(i) } },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.width(56.dp).height(32.dp).clip(RoundedCornerShape(16.dp)).background(if (on) C.AmberBg else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) { Icon(icon, label, tint = if (on) C.Amber else C.Muted, modifier = Modifier.size(22.dp)) }
                    Spacer(Modifier.height(4.dp))
                    T(label, 12.sp, if (on) C.Amber else C.Muted, if (on) FontWeight.ExtraBold else FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TabTitle(title: String, sub: String) {
    Column {
        Title(title, 30.sp)
        T(sub, 14.sp, C.Muted)
    }
}

// ================================================================ EV
@Composable
private fun Ev(openAyarlar: () -> Unit, openBilim: () -> Unit, openKalkan: () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val v = Store.version.intValue
    val ctx = LocalContext.current
    val c = Store.count()
    val limit = Store.limit
    val p = (c.toFloat() / limit.coerceAtLeast(1)).coerceIn(0f, 1f)
    val lamp = when { p < 0.25f -> 0; p < 0.5f -> 1; p < 0.75f -> 2; c < limit -> 3; else -> 4 }
    val ring by animateFloatAsState(p, tween(700, easing = FastOutSlowInEasing), label = "ring")
    val flick = rememberInfiniteTransition(label = "f")
    val glowA by flick.animateFloat(0.82f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "g")
    val breath by flick.animateFloat(1f, 1.025f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b")
    val bump = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val a11y = Util.a11yOn(ctx)
    val yesterday = Store.count(Store.day(1))
    val left = (limit - c).coerceAtLeast(0)
    val status = when {
        c >= limit -> "Fenerin söndü. Bugünlük bu kadar."
        p >= 0.8f -> "Fenerin titriyor: sadece $left reellik ışık kaldı."
        else -> "Fenerinde $left reellik ışık var."
    }
    val hour = LocalTime.now().hour
    val greet = when (hour) { in 5..11 -> "Günaydın"; in 12..17 -> "İyi günler"; in 18..22 -> "İyi akşamlar"; else -> "İyi geceler" }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(C.Surface).border(1.dp, C.Line2, CircleShape).tap(onClick = openAyarlar),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.user, "Profil ve ayarlar", tint = C.Text2, modifier = Modifier.size(22.dp)) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                T(greet, 12.sp, C.Muted)
                T("${Store.name} seninle", 15.sp, weight = FontWeight.ExtraBold)
            }
            Row(
                Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(C.Warm).border(1.dp, Color(0xFF3A2C17), RoundedCornerShape(20.dp)).padding(start = 10.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlameIcon(18.dp)
                Spacer(Modifier.width(6.dp))
                T(Store.streak().toString(), 15.sp, weight = FontWeight.ExtraBold)
            }
        }

        if (!a11y) {
            Card(bg = Color(0xFF2A1A16), border = Color(0xFF5A2E25)) {
                T("Sayaç kapalı", 15.sp, weight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                T("Reels'ini sayabilmem için erişilebilirlik iznini açman gerekiyor.", 13.sp, C.Text2)
                Spacer(Modifier.height(10.dp))
                SmallButton("İzni aç", filled = true) { Util.openA11y(ctx) }
            }
        }

        Box(Modifier.size(300.dp).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val glow = (1f - p) * glowA
                if (glow > 0.02f) {
                    drawCircle(
                        Brush.radialGradient(listOf(C.Amber.copy(alpha = 0.42f * glow), Color.Transparent), center = center, radius = size.minDimension * 0.42f),
                        radius = size.minDimension * 0.42f, center = center
                    )
                }
                val st = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                val inset = 6.dp.toPx() + 6.dp.toPx()
                val arcSize = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2)
                val tl = Offset(inset, inset)
                drawArc(C.Surface2, 0f, 360f, false, tl, arcSize, style = st)
                val col = when { c >= limit -> C.Red; p >= 0.8f -> Color(0xFFF0904A); else -> C.Amber }
                if (ring > 0f) drawArc(col, -90f, 360f * ring, false, tl, arcSize, style = st)
            }
            Crossfade(targetState = lamp, animationSpec = tween(500), label = "lamp") { l ->
                Art(
                    FENERS[l], 186.dp,
                    Modifier.graphicsLayer { val s = breath * bump.value; scaleX = s; scaleY = s }.tap(haptic = false) {
                        Feedback.tick(); Feedback.sound("pop", 0.4f)
                        scope.launch { bump.snapTo(1.08f); bump.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f)) }
                    },
                    "Fener tutan enik"
                )
            }
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                T(c.toString(), 54.sp, if (c >= limit) C.Coral else C.Text, FontWeight.ExtraBold, letterSpacing = (-2).sp, lineHeight = 56.sp)
                Spacer(Modifier.width(6.dp))
                T("/ $limit reel", 16.sp, C.Muted, FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            }
            T(status, 14.sp, C.Text2, align = TextAlign.Center)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("~${Store.minutes(c)} dk", "bugün", modifier = Modifier.weight(1f))
            StatTile(Store.topAppToday(), "en çok", modifier = Modifier.weight(1f))
            val diff = if (yesterday > 0) ((c - yesterday) * 100 / yesterday) else null
            StatTile(
                if (diff == null) "—" else if (diff <= 0) "−%${abs(diff)}" else "+%$diff",
                "düne göre",
                if (diff == null) C.Text else if (diff <= 0) C.Green else C.Coral,
                Modifier.weight(1f)
            )
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(18.dp))
                .tap(onClick = openKalkan).padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.shield, null, tint = C.Amber, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            T(
                if (Store.nightLock) "Gece kilidi: ${"%02d".format(Store.bedHour)}:00 – ${"%02d".format(Store.wakeHour)}:00" else "Gece kilidi kapalı",
                14.sp, modifier = Modifier.weight(1f)
            )
            Icon(Icons.chevron, null, tint = C.Muted, modifier = Modifier.size(18.dp))
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(18.dp))
                .tap(onClick = openBilim).padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.flask, null, tint = C.Amber, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            T("Neden izlememeliyiz? Araştırmalar ne diyor", 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.chevron, null, tint = C.Muted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ================================================================ KALKAN
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Kalkan() {
    @Suppress("UNUSED_VARIABLE") val v = Store.version.intValue
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { TabTitle("Kalkan", "Kuralları sen koy. Hepsi ücretsiz.") }
            Art(com.erkam.fer.R.drawable.kilit, 76.dp)
        }
        Card {
            T("Bugünün planı", 13.sp, C.Text2, FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val bed = Store.bedHour
            val wake = Store.wakeHour
            val night = Store.nightLock
            val nowH = LocalTime.now().let { it.hour + it.minute / 60f }
            Canvas(Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(8.dp))) {
                drawRect(C.Surface2)
                if (night) {
                    val w = size.width
                    if (bed > wake) {
                        drawRect(C.AmberBg, Offset(w * bed / 24f, 0f), androidx.compose.ui.geometry.Size(w * (24 - bed) / 24f, size.height))
                        drawRect(C.AmberBg, Offset(0f, 0f), androidx.compose.ui.geometry.Size(w * wake / 24f, size.height))
                    } else {
                        drawRect(C.AmberBg, Offset(w * bed / 24f, 0f), androidx.compose.ui.geometry.Size(w * (wake - bed) / 24f, size.height))
                    }
                }
                drawRect(C.Text, Offset(size.width * nowH / 24f - 1f, 0f), androidx.compose.ui.geometry.Size(2.dp.toPx(), size.height))
            }
            Spacer(Modifier.height(6.dp))
            Row {
                listOf("00", "06", "12", "18", "24").forEachIndexed { i, s ->
                    T(s, 11.sp, C.Muted, modifier = Modifier.weight(1f), align = if (i == 0) TextAlign.Start else if (i == 4) TextAlign.End else TextAlign.Center)
                }
            }
        }
        RuleCard(Icons.moon, "Gece kilidi", "${"%02d".format(Store.bedHour)}:00 – ${"%02d".format(Store.wakeHour)}:00 arası kısa videolar açılmaz", Store.nightLock) { Store.nightLock = it }
        if (Store.nightLock) {
            Card {
                T("Uyku saatim", 13.sp, C.Text2, FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(21, 22, 23, 0, 1).forEach { h -> Chip("%02d:00".format(h), Store.bedHour == h) { Store.bedHour = h } }
                }
                Spacer(Modifier.height(12.dp))
                T("Uyanma saatim", 13.sp, C.Text2, FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 6, 7, 8, 9).forEach { h -> Chip("%02d:00".format(h), Store.wakeHour == h) { Store.wakeHour = h } }
                }
            }
        }
        RuleCard(Icons.timer, "Sınır dolunca mola", "${Store.limit} reelde mola ekranı açılır", Store.limitLock) { Store.limitLock = it }
        RuleCard(Icons.eyeOff, "Reels'i gizle", "Instagram'da Reels açılınca geri çıkar; mesajlar çalışır", Store.hideReels) { Store.hideReels = it }
        RuleCard(Icons.lock, "Sıkı mod", "Mola ekranındaki \"5 dakika daha\" seçeneği kapanır", Store.strict) { Store.strict = it }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RuleCard(icon: ImageVector, title: String, sub: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if (on) C.AmberBg else C.Surface2), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (on) C.Amber else C.Muted, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            T(title, 15.sp, weight = FontWeight.ExtraBold)
            T(sub, 12.sp, C.Muted)
        }
        Spacer(Modifier.width(10.dp))
        Toggle(on, onChange)
    }
}

// ================================================================ DOSTLAR (2. sürüm)
@Composable
private fun Dostlar() {
    @Suppress("UNUSED_VARIABLE") val v = Store.version.intValue
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TabTitle("Dostlar", "En az kaydıran kazanır")
        Card(padding = 0.dp) {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(26.dp).clip(CircleShape).background(C.Amber), contentAlignment = Alignment.Center) { T("1", 13.sp, C.OnAmber, FontWeight.ExtraBold) }
                Spacer(Modifier.width(12.dp))
                FaceCircle(Store.stage(), 40.dp, C.Surface2)
                Spacer(Modifier.width(12.dp))
                T("Sen", 15.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                T(Store.count().toString(), 17.sp, weight = FontWeight.ExtraBold)
            }
            listOf("Kardeşini davet et", "Ev arkadaşını davet et", "Sınıf arkadaşını davet et").forEachIndexed { i, t ->
                Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(C.Line))
                Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(26.dp).clip(CircleShape).background(C.Line2), contentAlignment = Alignment.Center) { T("${i + 2}", 13.sp, C.Text2, FontWeight.ExtraBold) }
                    Spacer(Modifier.width(12.dp))
                    T(t, 15.sp, C.Muted, FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
            }
        }
        Card(bg = C.Warm, border = Color(0xFF3A2C17)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Art(com.erkam.fer.R.drawable.kutlama, 70.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    T("Yakında: haftalık yarış", 15.sp, weight = FontWeight.ExtraBold)
                    T("Google ile giriş geldiğinde arkadaşlarınla yarışacaksın. Haftanın kazananı eniği için yeni bir tasma alacak.", 13.sp, C.Text2)
                }
            }
        }
        PrimaryButton("Arkadaşına Fer'i öner") {
            Util.share(ctx, "Reels'te ne kadar zaman geçirdiğini gösteren, ücretsiz ve reklamsız bir uygulama buldum: Fer. Eniğin, kaydırdıkça yoruluyor. İndir: ${Util.RELEASE}")
        }
    }
}

// ================================================================ DEFTER
@Composable
private fun Defter(openBilim: () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val v = Store.version.intValue
    val ctx = LocalContext.current
    var text by remember { mutableStateOf("") }
    val todayNote = Store.noteToday()
    val week = Store.week()
    val thisWeek = Store.weekTotal(0)
    val prevWeek = Store.weekTotal(7)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TabTitle("Defter", "Reels yerine yaşadıkların")
        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FaceCircle(0, 40.dp)
                Spacer(Modifier.width(10.dp))
                T("Bugün Reels yerine ne yaptın?", 16.sp, weight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(12.dp))
            if (todayNote != null) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.GreenBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.check, null, tint = C.Green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    T(todayNote, 14.sp, C.Text)
                }
            } else {
                Box(Modifier.fillMaxWidth().heightIn(min = 72.dp).clip(RoundedCornerShape(14.dp)).background(C.Nav).border(1.dp, C.Line2, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    if (text.isEmpty()) T("Bir cümle yeter…", 15.sp, C.Faint)
                    BasicTextField(
                        value = text, onValueChange = { if (it.length <= 140) text = it },
                        textStyle = TextStyle(color = C.Text, fontSize = 15.sp), cursorBrush = SolidColor(C.Amber),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    SmallButton("Kaydet", filled = true) {
                        if (text.isNotBlank()) { Store.addNote(text); text = ""; Feedback.sound("chime", 0.5f); Feedback.double() }
                    }
                }
            }
        }
        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T("Bu hafta", 15.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                if (prevWeek > 0) {
                    val d = prevWeek - thisWeek
                    T(if (d >= 0) "$d reel daha az" else "${-d} reel daha fazla", 13.sp, if (d >= 0) C.Green else C.Coral, FontWeight.ExtraBold)
                } else T("toplam $thisWeek reel", 13.sp, C.Muted, FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            val max = (week.maxOfOrNull { it.second } ?: 0).coerceAtLeast(Store.limit).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                week.forEachIndexed { i, (label, n) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        T(if (n > 0) n.toString() else "", 11.sp, C.Text2, FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        val h = (6 + 70f * n / max).dp
                        val col = when (Store.stage(n, Store.limit)) { 0, 1 -> C.Amber; 2 -> Color(0xFFB59B7A); 3 -> Color(0xFF8C8780); else -> Color(0xFF4B4A48) }
                        Box(Modifier.fillMaxWidth().widthIn(max = 26.dp).height(h).clip(RoundedCornerShape(7.dp)).background(if (n == 0) C.Line else col))
                        Spacer(Modifier.height(6.dp))
                        T(label, 11.sp, if (i == 6) C.Amber else C.Muted, FontWeight.SemiBold)
                    }
                }
            }
        }
        val notes = Store.notes()
        if (notes.isNotEmpty()) {
            T("Kazandığın zamanlar", 13.sp, C.Muted, FontWeight.Bold)
            notes.take(20).forEach { (d, t) ->
                Row {
                    Box(Modifier.padding(top = 7.dp)) { Dot(C.Amber) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        T(if (d == Store.today()) "Bugün" else if (d == Store.day(1)) "Dün" else d, 12.sp, C.Muted)
                        T(t, 15.sp)
                    }
                }
            }
        }
        GhostButton("Haftalık özetimi paylaş") {
            val msg = buildString {
                append("Bu hafta Reels'te ${thisWeek} video izledim")
                if (prevWeek > 0 && prevWeek > thisWeek) append(" — geçen haftadan ${prevWeek - thisWeek} daha az")
                append(". Eniğim ${Store.name} ile kendimi takip ediyorum. Fer: ${Util.RELEASE}")
            }
            Util.share(ctx, msg)
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(18.dp))
                .tap(onClick = openBilim).padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.flask, null, tint = C.Amber, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            T("Bilim ne diyor?", 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.chevron, null, tint = C.Muted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ================================================================ AYARLAR
@Composable
private fun Ayarlar(onBack: () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val v = Store.version.intValue
    val ctx = LocalContext.current
    var privacy by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(C.Surface).border(1.dp, C.Line, CircleShape).tap(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.back, "Geri", tint = C.Text, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            T("Profil ve ayarlar", 18.sp, weight = FontWeight.ExtraBold)
        }
        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FaceCircle(Store.stage(), 64.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    T("Eniğin: ${Store.name}", 17.sp, weight = FontWeight.ExtraBold)
                    T("Hesapsız kullanıyorsun", 13.sp, C.Muted)
                }
                Box(Modifier.clip(RoundedCornerShape(50)).background(C.AmberBg).padding(horizontal = 10.dp, vertical = 5.dp)) { T("Google yakında", 11.sp, C.Amber, FontWeight.ExtraBold) }
            }
        }
        Section("SAYAÇ")
        Card(padding = 0.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                T("Günlük sınır", 15.sp, modifier = Modifier.weight(1f))
                RoundButton("−") { if (Store.limit > 10) { Store.limit = Store.limit - 10; Feedback.tick() } }
                T("${Store.limit}", 18.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center, modifier = Modifier.width(58.dp))
                RoundButton("+") { if (Store.limit < 1000) { Store.limit = Store.limit + 10; Feedback.tick() } }
            }
            Line()
            SettingRow("Sayaç izni", if (Util.a11yOn(ctx)) "Açık" else "Kapalı — aç") { Util.openA11y(ctx) }
            Line()
            Column(Modifier.padding(16.dp)) {
                T("Kabarcık boyutu", 15.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.Nav).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Küçük", "Orta", "Büyük").forEachIndexed { i, s ->
                        val on = Store.bubbleSize == i
                        Box(
                            Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(10.dp)).background(if (on) C.AmberBg else Color.Transparent)
                                .tap { Store.bubbleSize = i; FerService.instance?.refreshBubbleStyle() },
                            contentAlignment = Alignment.Center
                        ) { T(s, 14.sp, if (on) C.Amber else C.Muted, FontWeight.Bold) }
                    }
                }
            }
        }
        Section("UYGULAMALAR")
        Card(padding = 0.dp) {
            Store.APPS.forEachIndexed { i, app ->
                if (i > 0) Line()
                Row(Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    T(app.name, 15.sp, if (app.ready) C.Text else C.Muted, modifier = Modifier.weight(1f))
                    if (app.ready) Toggle(Store.appEnabled(app.pkg)) { Store.setAppEnabled(app.pkg, it) }
                    else Box(Modifier.clip(RoundedCornerShape(50)).border(1.dp, C.Line3, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp)) { T("Yakında", 12.sp, C.Muted, FontWeight.Bold) }
                }
            }
        }
        Section("HİS")
        Card(padding = 0.dp) {
            ToggleRow("Ses efektleri", "Tık, çan ve fıs sesleri", Store.sound) { Store.sound = it; if (it) Feedback.sound("pop") }
            Line()
            ToggleRow("Titreşim", "Dokunuşlarda ve enik değişince", Store.haptic) { Store.haptic = it; if (it) Feedback.double() }
            Line()
            ToggleRow("Manevi mod", "Mola ekranında kısa hatırlatmalar", Store.manevi) { Store.manevi = it }
        }
        Section("DİĞER")
        Card(padding = 0.dp) {
            SettingRow("Ana ekran widget'ı", "Ekle") { FerWidget.requestPin(ctx) }
            Line()
            SettingRow("Pil kısıtlaması", if (Util.batteryOk(ctx)) "Kapalı" else "Kaldır") { Util.requestBattery(ctx) }
            Line()
            SettingRow("Arkadaşına öner", "") { Util.share(ctx, "Fer — Reels sayacı, ücretsiz ve reklamsız: ${Util.RELEASE}") }
            Line()
            SettingRow("Gizlilik", if (privacy) "Gizle" else "") { privacy = !privacy }
            if (privacy) {
                T(
                    "Fer, erişilebilirlik iznini yalnızca kısa video oynatıcısında olup olmadığını anlamak ve izlenen reel sayısını saymak için kullanır. Mesajlarını, şifreni veya banka bilgilerini okumaz. Videonun yazıları sadece aynı reelin iki kez sayılmaması için anlık karşılaştırılır ve kaydedilmez. Hiçbir veri internete gönderilmez; tüm sayılar sadece bu telefonda durur.",
                    13.sp, C.Text2, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            Line()
            SettingRow("Tanıtımı tekrar izle", "") { Store.onboarded = false }
        }
        T("Fer ${appVersion(ctx)} · Erkam Yazılım · Reklamsız ve ücretsiz", 12.sp, C.Faint, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
    }
}

private fun appVersion(ctx: android.content.Context): String = try {
    ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
} catch (e: Exception) { "" }

@Composable
private fun Section(t: String) = T(t, 12.sp, C.Muted, FontWeight.ExtraBold, letterSpacing = 0.8.sp, modifier = Modifier.padding(top = 6.dp))

@Composable
private fun Line() = Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(C.Line))

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).tap(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        T(title, 15.sp, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) T(value, 14.sp, C.Muted)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.chevron, null, tint = C.Muted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ToggleRow(title: String, sub: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            T(title, 15.sp)
            T(sub, 12.sp, C.Muted)
        }
        Toggle(on, onChange)
    }
}

// ================================================================ BİLİM
@Composable
fun Bilim(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(C.Surface).border(1.dp, C.Line, CircleShape).tap(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.back, "Geri", tint = C.Text, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            T("Araştırmalar", 13.sp, C.Muted, FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Title("Neden\nizlememeliyiz?", 30.sp, modifier = Modifier.weight(1f))
            Face(3, 84.dp)
        }
        T("Kaydırmak masum görünür. Bilim insanları kısa videoların dikkat, hafıza ve uyku ile ilişkisine baktı.", 15.sp, C.Text2)
        Card(radius = 24.dp, padding = 20.dp) {
            T("1 · DİKKAT", 12.sp, C.Amber, FontWeight.ExtraBold, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                T("98.299", 40.sp, weight = FontWeight.ExtraBold, letterSpacing = (-1).sp, lineHeight = 42.sp)
                Spacer(Modifier.width(8.dp))
                T("kişi · 71 çalışma", 14.sp, C.Muted, FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(8.dp))
            T("Dikkat ve kendini durdurma gücü zayıflıyor", 18.sp, weight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            T("2025 tarihli büyük bir meta-analize göre kısa video kullanımı arttıkça düşünme becerileri zayıflıyor. En güçlü bağ dikkat ve dürtü kontrolünde.", 14.sp, C.Text2)
            Spacer(Modifier.height(12.dp))
            listOf("Genel düşünme becerisi" to 0.34f, "Dikkat" to 0.38f, "Kendini durdurma" to 0.41f).forEach { (l, r) ->
                Row { T(l, 13.sp, C.Text2, modifier = Modifier.weight(1f)); T("−0,${(r * 100).toInt()}", 13.sp, weight = FontWeight.Bold) }
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(C.Line)) {
                    Box(Modifier.fillMaxWidth(r / 0.5f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(C.Amber))
                }
                Spacer(Modifier.height(10.dp))
            }
            T("Nguyen ve ark., Psychological Bulletin, 2025 · r = ilişki gücü (0,5 güçlü sayılır)", 12.sp, C.Faint)
        }
        SciCard("2 · HAFIZA", "Yapacağın işi unutturuyor", "Münih'te 60 kişiyle yapılan deneyde, kısa video akışı izleyenlerin \"planladığı işi zamanında hatırlama\" performansı belirgin şekilde düştü. Twitter ve YouTube gruplarında böyle bir düşüş görülmedi.", "Chiossi ve ark., LMU Münih · CHI 2023")
        SciCard("3 · UYKU", "Uyku kaçıyor, işler erteleniyor", "337 üniversite öğrencisinde kısa video bağımlılığı daha kötü uyku, daha az hareket ve daha çok erteleme ile ilişkili çıktı. 1.629 lise öğrencisinde de bağımlılık ile uyku sorunları arasında anlamlı ilişki bulundu.", "Zhao ve Kou, Frontiers in Psychology, 2024 · BMC Psychology, 2024")
        SciCard("4 · YILIN KELİMESİ", "\"Brain rot\"", "Oxford 2024'te \"beyin çürümesi\" anlamına gelen bu ifadeyi yılın kelimesi seçti: önemsiz çevrim içi içeriği aşırı tüketmenin zihni köreltmesi. Kullanımı bir yılda %230 arttı.", "Oxford University Press, Aralık 2024")
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).border(1.dp, C.Line3, RoundedCornerShape(24.dp)).padding(20.dp)) {
            T("Dürüst bir not", 15.sp, weight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            T("Bu araştırmalar çoğunlukla ilişki gösteriyor, kesin sebep-sonuç değil. En güçlü bağ, ne kadar süre izlediğinden çok bırakamama hâlinde görülüyor. Fer tam olarak bunu fark etmen için var.", 14.sp, C.Text2)
        }
        PrimaryButton("Anladım") { onBack() }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SciCard(tag: String, title: String, body: String, src: String) {
    Card(radius = 24.dp, padding = 20.dp) {
        T(tag, 12.sp, C.Amber, FontWeight.ExtraBold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(8.dp))
        T(title, 18.sp, weight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        T(body, 14.sp, C.Text2)
        Spacer(Modifier.height(10.dp))
        T(src, 12.sp, C.Faint)
    }
}
