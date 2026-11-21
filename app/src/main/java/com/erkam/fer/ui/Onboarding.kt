package com.erkam.fer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.erkam.fer.Feedback
import com.erkam.fer.FerWidget
import com.erkam.fer.R
import com.erkam.fer.Store
import com.erkam.fer.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val TR = Locale("tr", "TR")
private fun fmt(n: Int): String = NumberFormat.getIntegerInstance(TR).format(n)

@Composable
fun Onboarding(onDone: (openInstagram: Boolean) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val back: () -> Unit = { if (step > 0) step-- }
    val next: () -> Unit = { step++ }
    BackHandler(enabled = step > 0) { back() }
    Crossfade(targetState = step, animationSpec = tween(220), label = "onb") { s ->
        Box(Modifier.fillMaxSize().background(C.Bg)) {
            when (s) {
                0 -> StepAcilis(next)
                1 -> StepTanisma(next)
                2 -> StepIsim(back, next)
                3 -> StepSorular(back, next)
                4 -> StepSok(back, next)
                5 -> StepHafiza(back, next)
                6 -> StepBilim(back, next)
                7 -> StepNeden(back, next)
                8 -> StepHedef(back, next)
                9 -> StepSinir(back, next)
                10 -> StepSoz(back, next)
                11 -> StepGizlilik(back, next)
                12 -> StepIzin(back, next)
                13 -> StepHesap(back, next)
                else -> StepHazir(onDone)
            }
        }
    }
}

// ---------------------------------------------------------------- 1 · Açılış
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepAcilis(next: () -> Unit) {
    val reels = listOf(R.drawable.reel1, R.drawable.reel2, R.drawable.reel3, R.drawable.reel4, R.drawable.reel5, R.drawable.reel6, R.drawable.reel1)
    val density = LocalDensity.current
    val cardPx = with(density) { 424.dp.toPx() }
    val offset = remember { Animatable(0f) }
    var count by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        var pos = 0
        while (isActive) {
            delay(1100)
            pos++
            count = if (count >= 62) 0 else count + 1
            Feedback.tick()
            Feedback.sound("whoosh", 0.18f)
            offset.animateTo(pos * cardPx, tween(550, easing = FastOutSlowInEasing))
            if (pos >= 6) { pos = 0; offset.snapTo(0f) }
        }
    }
    val stage = when { count <= 10 -> 0; count <= 22 -> 1; count <= 36 -> 2; count <= 50 -> 3; else -> 4 }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)))
            Spacer(Modifier.width(10.dp))
            T("Fer", 18.sp, weight = FontWeight.ExtraBold)
        }
        Box(
            Modifier.size(236.dp, 430.dp).clip(RoundedCornerShape(40.dp)).background(C.Line2).padding(3.dp)
                .clip(RoundedCornerShape(37.dp)).background(C.Surface)
        ) {
            Column(
                Modifier.wrapContentHeight(Alignment.Top, unbounded = true).graphicsLayer { translationY = -offset.value }
            ) {
                reels.forEach {
                    Image(painterResource(it), null, contentScale = ContentScale.Crop, modifier = Modifier.size(230.dp, 424.dp))
                }
            }
            Row(
                Modifier.align(Alignment.TopCenter).padding(top = 14.dp).clip(RoundedCornerShape(50))
                    .background(Color(0xD90B0C10)).border(1.dp, Color(0x29FFFFFF), RoundedCornerShape(50))
                    .padding(start = 3.dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(30.dp).clip(CircleShape).background(if (stage == 4) Color(0xFFE9E6DF) else C.Warm), contentAlignment = Alignment.Center) {
                    Face(stage, 27.dp)
                }
                Spacer(Modifier.width(6.dp))
                T(count.toString(), 14.sp, weight = FontWeight.ExtraBold)
            }
        }
        Title("Kaydırdıkça\neniğin yorulur", 29.sp, TextAlign.Center)
        T("Fer kaç kısa video izlediğini sayar.\nGerisini eniğinin yüzünden anlarsın.", 14.sp, C.Text2, align = TextAlign.Center)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Instagram", "TikTok", "YouTube Shorts", "Facebook", "Snapchat").forEach {
                Box(Modifier.clip(RoundedCornerShape(50)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    T(it, 12.sp, C.Text2, FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Başla") { next() }
    }
}

// ---------------------------------------------------------------- 2 · Tanışma
@Composable
private fun StepTanisma(next: () -> Unit) {
    LaunchedEffect(Unit) { delay(350); Feedback.sound("pop", 0.6f); Feedback.click() }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(C.Surface2).border(1.dp, C.Line2, RoundedCornerShape(20.dp)).padding(horizontal = 18.dp, vertical = 12.dp)) {
            T("Hav!", 20.sp, weight = FontWeight.ExtraBold)
        }
        Box(Modifier.size(290.dp).clip(CircleShape).background(C.Warm), contentAlignment = Alignment.Center) {
            Art(R.drawable.tam, 250.dp, desc = "Enik")
        }
        Title("Ben senin eniğinim", 30.sp, TextAlign.Center)
        T("Sen Reels kaydırdıkça ben yorulurum.\nAz kaydırırsan pırıl pırıl kalırım.", 16.sp, C.Text2, align = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        PrimaryButton("Merhaba!") { next() }
    }
}

// ---------------------------------------------------------------- 3 · İsim
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepIsim(back: () -> Unit, next: () -> Unit) {
    var name by remember { mutableStateOf(Store.name) }
    var girl by remember { mutableStateOf(Store.girl) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ProgressTop(0.08f, back)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Art(R.drawable.yuz_mutlu, 150.dp, desc = "Mutlu enik")
            Title("Bana bir isim verir misin?", 26.sp, TextAlign.Center)
        }
        T("Eniğinin adı", 13.sp, C.Muted, FontWeight.SemiBold)
        BasicTextField(
            value = name,
            onValueChange = { if (it.length <= 16) name = it },
            singleLine = true,
            textStyle = TextStyle(color = C.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold),
            cursorBrush = SolidColor(C.Amber),
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)).background(C.Surface)
                .border(1.5.dp, C.Amber, RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 16.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Pamuk", "Fıstık", "Karamel", "Boncuk", "Zeytin").forEach { n ->
                Chip(n, name == n) { name = n; Feedback.tick() }
            }
        }
        T("O bir…", 13.sp, C.Muted, FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Kız", girl, Modifier.weight(1f)) { girl = true }
            Chip("Oğlan", !girl, Modifier.weight(1f)) { girl = false }
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton(if (name.isBlank()) "Bir isim seç" else "Merhaba ${name.trim()}!", enabled = name.isNotBlank()) {
            Store.name = name.trim(); Store.girl = girl
            Feedback.sound("pop", 0.6f)
            next()
        }
    }
}

// ---------------------------------------------------------------- 4 · Sorular
private data class Q(val q: String, val opts: List<String>)

private val QUESTIONS = listOf(
    Q("Sence dün kaç reel izledin?", listOf("50 reel", "100 reel", "200 reel", "300 reel", "500 ve üstü")),
    Q("En çok ne zaman izliyorsun?", listOf("Sabah", "Ders / iş arasında", "Akşam", "Yatakta")),
    Q("Reels'ten sonra nasıl hissediyorsun?", listOf("Yorgun", "Suçlu", "Rahatlamış", "Fark etmez")),
    Q("Genelde kaçta uyuyorsun?", listOf("22:00", "23:00", "00:00", "01:00 ve sonrası")),
    Q("Seni en çok ne rahatsız ediyor?", listOf("Odaklanamamak", "Uykusuzluk", "Zaman kaybı", "Ruh hâlim")),
)

@Composable
private fun StepSorular(back: () -> Unit, next: () -> Unit) {
    var qi by rememberSaveable { mutableIntStateOf(0) }
    val picks = remember { mutableStateListOf(-1, -1, -1, -1, -1) }
    BackHandler(enabled = qi > 0) { qi-- }
    val q = QUESTIONS[qi]
    var shown by remember(qi) { mutableIntStateOf(0) }
    LaunchedEffect(qi) {
        shown = 0
        while (shown < q.q.length) { delay(18); shown++ }
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { ProgressTop(0.12f + 0.04f * qi, { if (qi > 0) qi-- else back() }) }
            Spacer(Modifier.width(10.dp))
            T("${qi + 1} / 5", 12.sp, C.Muted, FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            FaceCircle(0, 52.dp)
            Spacer(Modifier.width(10.dp))
            Column(
                Modifier.clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)).background(C.Surface2)
                    .border(1.dp, C.Line2, RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)).padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                T(Store.name, 12.sp, C.Amber, FontWeight.Bold)
                T(q.q.take(shown), 17.sp, weight = FontWeight.Bold)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.opts.forEachIndexed { i, o ->
                val on = picks[qi] == i
                Row(
                    Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(18.dp))
                        .background(if (on) C.AmberBg else C.Surface)
                        .border(1.5.dp, if (on) C.Amber else C.Line, RoundedCornerShape(18.dp))
                        .tap(haptic = false) { picks[qi] = i; Feedback.tick() }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    T(o, 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).background(if (on) C.Amber else Color.Transparent)
                            .border(2.dp, if (on) C.Amber else Color(0xFF4A4D56), CircleShape)
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Devam", enabled = picks[qi] >= 0) {
            if (qi < QUESTIONS.size - 1) {
                qi++
            } else {
                Store.guess = listOf(50, 100, 200, 300, 500)[picks[0].coerceAtLeast(0)]
                Store.bedHour = listOf(22, 23, 0, 1)[picks[3].coerceAtLeast(0)]
                Store.answers = picks.joinToString(",")
                next()
            }
        }
    }
}

// ---------------------------------------------------------------- 5 · Şok hesabı
@Composable
private fun StepSok(back: () -> Unit, next: () -> Unit) {
    val g = Store.guess
    val yearly = g * 365
    val days = ((g * 20L * 365L) / 86400L).toInt().coerceIn(1, 365)
    val counter = remember { Animatable(0f) }
    var lit by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        launch {
            var last = 0
            while (counter.value < yearly) {
                delay(90)
                val step = (counter.value / yearly * 10).toInt()
                if (step != last) { last = step; Feedback.tick() }
            }
        }
        counter.animateTo(yearly.toFloat(), tween(1600, easing = FastOutSlowInEasing))
        Feedback.heavy(); Feedback.sound("thud", 0.5f)
        delay(250)
        while (lit < days) {
            lit++
            if (lit % 2 == 1) Feedback.tick()
            delay((900L / days).coerceIn(30L, 140L))
        }
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProgressTop(0.32f, back)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            T("Günde $g reel, yılda", 15.sp, C.Text2)
            T(fmt(counter.value.roundToInt()), 40.sp, C.Coral, FontWeight.ExtraBold, letterSpacing = (-1).sp)
            T("haftada ${fmt(g * 7)} · ayda ${fmt(g * 30)}", 13.sp, C.Muted)
        }
        Card(padding = 16.dp) {
            T("Bir yılın 365 günü", 13.sp, C.Text2, FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(10.dp))
            Canvas(Modifier.fillMaxWidth().aspectRatio(25f / 15f)) {
                val cell = size.width / 25f
                val r = cell * 0.36f
                for (i in 0 until 365) {
                    val cx = (i % 25) * cell + cell / 2
                    val cy = (i / 25) * cell + cell / 2
                    val on = i >= 365 - lit
                    drawCircle(if (on) C.Coral else C.Line2, if (on && i == 365 - lit) r * 1.5f else r, Offset(cx, cy))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Dot(C.Coral, 9.dp); Spacer(Modifier.width(6.dp)); T("Reels'e giden günler", 12.sp, C.Muted) }
                Row(verticalAlignment = Alignment.CenterVertically) { Dot(C.Line2, 9.dp); Spacer(Modifier.width(6.dp)); T("Geri kalanı", 12.sp, C.Muted) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Art(R.drawable.yatak, 150.dp, desc = "Gece telefona bakan enik")
            Spacer(Modifier.width(12.dp))
            Column {
                T("≈ $lit gün", 42.sp, weight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp)
                T("her yıl Reels'te\nkayboluyor", 14.sp, C.Text2)
            }
        }
        T("Reel başına ortalama 20 saniye ile hesaplandı.", 12.sp, C.Faint, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.weight(1f))
        PrimaryButton("Devam") { next() }
    }
}

// ---------------------------------------------------------------- 6 · Hafıza testi
@Composable
private fun StepHafiza(back: () -> Unit, next: () -> Unit) {
    val answers = remember { mutableStateListOf("", "", "") }
    var shown by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressTop(0.4f, back)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Art(R.drawable.yuz_saskin, 130.dp, desc = "Şaşkın enik")
            Title("Küçük bir test: dün izlediğin 3 reeli hatırlıyor musun?", 24.sp, TextAlign.Center)
        }
        (0 until 3).forEach { i ->
            Row(
                Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(C.Surface)
                    .border(1.dp, C.Line, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                T("${i + 1}", 14.sp, C.Amber, FontWeight.ExtraBold)
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (answers[i].isEmpty()) T("Neydi?", 15.sp, C.Faint)
                    BasicTextField(
                        value = answers[i], onValueChange = { answers[i] = it }, singleLine = true,
                        textStyle = TextStyle(color = C.Text, fontSize = 15.sp), cursorBrush = SolidColor(C.Amber),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (shown) {
            Card(bg = C.Warm, border = Color(0xFF3A2C17)) {
                T("Çoğu insan hatırlayamıyor", 15.sp, weight = FontWeight.ExtraBold)
                Spacer(Modifier.height(6.dp))
                T("Münih'te yapılan bir deneyde kısa video akışı izleyenler, yapmayı planladıkları işi daha çok unuttu. Hızlı geçişler hafızayı yoruyor.", 14.sp, C.Text2)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (!shown && answers.all { it.isBlank() }) {
            GhostButton("Hatırlamıyorum", height = 56.dp) { shown = true; Feedback.sound("thud", 0.35f) }
        } else {
            PrimaryButton("Devam") { next() }
        }
    }
}

// ---------------------------------------------------------------- 7 · Bilim kartları
data class Sci(val tag: String, val big: String, val sub: String, val title: String, val body: String, val src: String)

val SCIENCE = listOf(
    Sci("1 · DİKKAT", "98.299", "kişi · 71 çalışma", "Dikkatin dağılıyor",
        "Çok kısa video izleyenlerin dikkati daha kolay dağılıyor ve kendini durdurması zorlaşıyor.", "Psychological Bulletin, 2025"),
    Sci("2 · HAFIZA", "60", "kişilik deney", "Yapacağını unutuyorsun",
        "Kısa video akışı izleyenler, yapmayı planladıkları işi daha çok unuttu. Normal videolarda bu görülmedi.", "LMU Münih, CHI 2023"),
    Sci("3 · UYKU", "337", "üniversite öğrencisi", "Uykun kaçıyor",
        "Kısa video bağımlılığı daha kötü uyku, daha az hareket ve daha çok erteleme ile bağlantılı çıktı.", "Frontiers in Psychology, 2024"),
)

@Composable
private fun StepBilim(back: () -> Unit, next: () -> Unit) {
    var i by rememberSaveable { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ProgressTop(0.48f, back)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Title("Bilim insanları\nne buldu?", 26.sp, modifier = Modifier.weight(1f))
            Art(R.drawable.gosteren, 96.dp)
        }
        Crossfade(targetState = i, animationSpec = tween(250), label = "sci") { k ->
            val c = SCIENCE[k]
            Card(radius = 28.dp, padding = 22.dp, modifier = Modifier.heightIn(min = 360.dp)) {
                T(c.tag, 12.sp, C.Amber, FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(12.dp))
                T(c.big, 44.sp, weight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp, lineHeight = 46.sp)
                T(c.sub, 14.sp, C.Muted, FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                T(c.title, 21.sp, weight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                T(c.body, 15.sp, C.Text2)
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.clip(RoundedCornerShape(20.dp)).background(C.Surface2).border(1.dp, C.Line2, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.book, null, tint = C.Text2, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    T("Kaynak: ${c.src}", 13.sp, C.Text2, FontWeight.SemiBold)
                }
            }
        }
        Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SCIENCE.indices.forEach { k ->
                Box(Modifier.width(if (k == i) 24.dp else 6.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(if (k == i) C.Amber else C.Line3))
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(if (i < SCIENCE.size - 1) "Sonraki" else "Devam") {
            if (i < SCIENCE.size - 1) { i++; Feedback.sound("whoosh", 0.3f) } else next()
        }
    }
}

// ---------------------------------------------------------------- 8 · Neden bırakamıyorsun
@Composable
private fun StepNeden(back: () -> Unit, next: () -> Unit) {
    val pics = listOf(R.drawable.yuz_uykulu, R.drawable.yuz_saskin, R.drawable.yuz_mutlu)
    val final = listOf(0, 0, 2)
    val cur = remember { mutableStateListOf(0, 1, 2) }
    val stopped = remember { mutableStateListOf(false, false, false) }
    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        var k = 0
        while (stopped.any { !it }) {
            delay(90)
            k++
            val t = System.currentTimeMillis() - start
            for (s in 0 until 3) {
                if (stopped[s]) continue
                if (t > 700 + s * 450) {
                    cur[s] = final[s]; stopped[s] = true
                    Feedback.tick(); Feedback.sound("tick", 0.6f)
                    if (s == 2) { Feedback.double(); Feedback.sound("chime", 0.45f) }
                } else cur[s] = (k + s) % 3
            }
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ProgressTop(0.56f, back)
        Title("Neden bir türlü bırakamıyorsun?", 28.sp)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(24.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (0 until 3).forEach { s ->
                val win = stopped[s] && final[s] == 2
                Column(
                    Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(16.dp)).background(if (win) C.AmberBg else C.Surface2)
                        .border(1.5.dp, if (win) C.Amber else Color.Transparent, RoundedCornerShape(16.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) {
                    Image(painterResource(pics[cur[s]]), null, Modifier.size(56.dp))
                    Spacer(Modifier.height(8.dp))
                    T(if (!stopped[s]) "…" else if (win) "Harika!" else "Sıkıcı", 12.sp, if (win) C.Amber else C.Muted, FontWeight.Bold)
                }
            }
        }
        T("Reels bir piyango gibi çalışır. Çoğu video sıradandır, arada bir harika biri çıkar. Beynin o \"arada bir\"i bekler ve sen kaydırmaya devam edersin.", 16.sp, C.Text2, lineHeight = 25.sp)
        Card {
            Row {
                Icon(Icons.info, null, tint = C.Amber, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                T("Psikolojide buna değişken ödül denir. Kumar makineleri de aynı yolu kullanır. Suç sende değil; ama bunu bilmek durmayı kolaylaştırır.", 14.sp, C.Text2)
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Anladım") { next() }
    }
}

// ---------------------------------------------------------------- 9 · Hedef
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepHedef(back: () -> Unit, next: () -> Unit) {
    val all = listOf("Ders / sınav", "Uyku", "Kitap", "İbadet", "Aile", "Spor", "Bir hayalim", "İş")
    val sel = remember { mutableStateListOf<String>().apply { addAll(Store.goals.split("|").filter { it.isNotBlank() }) } }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ProgressTop(0.64f, back)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Art(R.drawable.kitap, 170.dp, desc = "Kitap okuyan enik")
            Title("Kazandığın zamanı neye ayırmak istersin?", 25.sp, TextAlign.Center)
            T("Birden fazla seçebilirsin", 14.sp, C.Muted)
        }
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            all.forEach { g ->
                Chip(g, g in sel) { if (g in sel) sel.remove(g) else sel.add(g); Feedback.tick() }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Devam") { Store.goals = sel.joinToString("|"); next() }
    }
}

// ---------------------------------------------------------------- 10 · Sınır
@Composable
private fun StepSinir(back: () -> Unit, next: () -> Unit) {
    val now = Store.guess
    var limit by rememberSaveable { mutableIntStateOf((((now / 2) + 5) / 10 * 10).coerceIn(10, now)) }
    val savedSec = (now - limit).coerceAtLeast(0) * 20
    val ratio = limit.toFloat() / now
    val endStage = when { ratio <= 0.25f -> 0; ratio <= 0.5f -> 1; ratio <= 0.75f -> 2; else -> 3 }
    val anim by animateFloatAsState(limit.toFloat(), tween(300), label = "l")
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressTop(0.72f, back)
        Title("Günde en fazla kaç reel?", 27.sp)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(26.dp)).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundButton("−") { if (limit > 10) { limit -= 10; Feedback.tick() } }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                T(limit.toString(), 56.sp, weight = FontWeight.ExtraBold, letterSpacing = (-2).sp, lineHeight = 58.sp)
                T("reel / gün", 13.sp, C.Muted, FontWeight.SemiBold)
            }
            RoundButton("+") { if (limit < now) { limit += 10; Feedback.tick() } }
        }
        Card {
            Row {
                T("30 günde ${Store.name}", 14.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                T("her gün biraz daha az", 12.sp, C.Muted)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(170.dp)) {
                Canvas(Modifier.fillMaxSize().padding(start = 18.dp, end = 22.dp, top = 22.dp, bottom = 10.dp)) {
                    val w = size.width
                    val h = size.height
                    fun y(v: Float) = (1f - v / now) * h
                    listOf(0f, 0.5f, 1f).forEach { f -> drawLine(C.Line, Offset(0f, h * f), Offset(w, h * f), 1f) }
                    val path = Path()
                    val area = Path()
                    for (i in 0..30) {
                        val t = i / 30f
                        val v = now - (now - anim) * (1 - (1 - t) * (1 - t))
                        val x = t * w
                        if (i == 0) { path.moveTo(x, y(v)); area.moveTo(x, h); area.lineTo(x, y(v)) } else { path.lineTo(x, y(v)); area.lineTo(x, y(v)) }
                    }
                    area.lineTo(w, h); area.close()
                    drawPath(area, C.Amber.copy(alpha = 0.10f))
                    drawPath(path, C.Amber, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
                Box(Modifier.align(Alignment.TopStart).padding(top = 0.dp)) { FaceCircle(3, 44.dp, Color(0xFF25241F)) }
                val endTop = (22 + (1f - anim / now) * (170 - 32) - 24).coerceIn(0f, 120f)
                Box(Modifier.align(Alignment.TopEnd).padding(top = endTop.dp)) { FaceCircle(endStage, 48.dp, C.AmberBg) }
            }
            Row {
                T("Bugün · $now", 12.sp, C.Coral, FontWeight.Bold, modifier = Modifier.weight(1f))
                T("30. gün · $limit", 12.sp, C.Amber, FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("+${(savedSec + 30) / 60} dk", "her gün kazancın", C.Green, Modifier.weight(1f))
            StatTile("+${String.format(TR, "%.1f", savedSec * 365f / 86400f)} gün", "bir yılda kazancın", C.Green, Modifier.weight(1f))
        }
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Hedefimi belirle") { Store.limit = limit; next() }
    }
}

@Composable
fun RoundButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp).clip(CircleShape).background(C.Surface2).border(1.dp, C.Line3, CircleShape).tap(haptic = false, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { T(label, 26.sp, weight = FontWeight.SemiBold) }
}

@Composable
fun StatTile(value: String, label: String, color: Color = C.Text, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        T(value, 20.sp, color, FontWeight.ExtraBold)
        T(label, 12.sp, C.Muted)
    }
}

// ---------------------------------------------------------------- 11 · Söz
@Composable
private fun StepSoz(back: () -> Unit, next: () -> Unit) {
    val prog = remember { Animatable(0f) }
    var holding by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    LaunchedEffect(holding) {
        if (done) return@LaunchedEffect
        if (holding) {
            val hb = launch { while (isActive) { Feedback.heartbeat(); delay(560) } }
            prog.animateTo(1f, tween(((1f - prog.value) * 1700).toInt().coerceAtLeast(1), easing = LinearEasing))
            hb.cancel()
            done = true
            Feedback.heavy(); Feedback.sound("chime", 0.7f)
        } else {
            prog.animateTo(0f, tween(250))
        }
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressTop(0.8f, back)
        Art(R.drawable.soz, 170.dp, desc = "Patisini kalbine koymuş enik")
        Title("Söz veriyor musun?", 28.sp, TextAlign.Center)
        Card(radius = 22.dp, padding = 20.dp) {
            T("${Store.name} için her gün en fazla ${Store.limit} reel izleyeceğime söz veriyorum.", 16.sp, C.Text2, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(132.dp).clip(CircleShape).background(if (done) C.AmberBg else C.Surface)
                .pointerInput(done) {
                    if (!done) detectTapGestures(onPress = {
                        holding = true
                        tryAwaitRelease()
                        holding = false
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize().padding(6.dp)) {
                val st = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                drawArc(C.Line2, 0f, 360f, false, style = st)
                drawArc(C.Amber, -90f, 360f * prog.value, false, style = st)
            }
            Icon(Icons.heart, null, tint = C.Amber, modifier = Modifier.size(44.dp).graphicsLayer {
                val sc = 1f + 0.12f * prog.value
                scaleX = sc; scaleY = sc
            })
        }
        T(if (done) "Söz verildi!" else if (holding) "Bırakma…" else "Basılı tut", 14.sp, if (done) C.Amber else C.Muted, FontWeight.Bold)
        if (done) PrimaryButton("Devam") { next() } else Spacer(Modifier.height(56.dp))
    }
}

// ---------------------------------------------------------------- 12 · Gizlilik
@Composable
private fun StepGizlilik(back: () -> Unit, next: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ProgressTop(0.86f, back)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Art(R.drawable.kilit, 180.dp, desc = "Kilit tutan enik")
            Title("Sırların bende güvende", 26.sp, TextAlign.Center)
        }
        Card(padding = 0.dp) {
            val rows = listOf("Mesajlarını okumam", "Şifreni ve banka bilgilerini görmem", "Sadece kaç reel geçtiğini sayarım", "Hiçbir şey telefonundan dışarı çıkmaz")
            rows.forEachIndexed { i, t ->
                Row(Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(C.GreenBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.check, null, tint = C.Green, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    T(t, 15.sp, weight = FontWeight.SemiBold)
                }
                if (i < rows.size - 1) Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp).background(C.Line))
            }
        }
        T("Hesap açmak zorunda değilsin. Reklam yok, ücret yok.", 13.sp, C.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Anladım") { next() }
    }
}

// ---------------------------------------------------------------- 13 · İzinler
@Composable
private fun StepIzin(back: () -> Unit, next: () -> Unit) {
    val ctx = LocalContext.current
    var a11y by remember { mutableStateOf(Util.a11yOn(ctx)) }
    var batt by remember { mutableStateOf(Util.batteryOk(ctx)) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) { a11y = Util.a11yOn(ctx); batt = Util.batteryOk(ctx) }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    LaunchedEffect(a11y) { if (a11y) { Feedback.double(); Feedback.sound("tick", 0.7f) } }
    LaunchedEffect(batt) { if (batt) Feedback.tick() }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressTop(0.92f, back)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Title("Saymaya başlamam için 2 izin", 26.sp)
                Spacer(Modifier.height(6.dp))
                T("İkisi de 20 saniye sürer", 14.sp, C.Muted)
            }
            Art(R.drawable.gosteren, 100.dp)
        }
        Card(border = if (a11y) Color(0xFF2E5A40) else C.Amber) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumBadge("1", a11y, true)
                Spacer(Modifier.width(12.dp))
                Column {
                    T("Reels'ini görebilmem için", 16.sp, weight = FontWeight.ExtraBold)
                    T("Ayarlarda Fer'i bulup aç", 13.sp, C.Muted)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("\"Yüklü uygulamalar\"a dokun", "\"Fer\"i seç", "Düğmeyi aç").forEachIndexed { i, t ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(C.Surface2).padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        T("${i + 1}", 16.sp, C.Amber, FontWeight.ExtraBold)
                        T(t, 12.sp, C.Text2, align = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (a11y) {
                DoneBar("İzin verildi")
            } else {
                PrimaryButton("İzin ver") { Util.openA11y(ctx) }
                Spacer(Modifier.height(10.dp))
                T("Fer gri ve basılamıyorsa: Uygulama bilgileri › sağ üstteki ⋮ › \"Kısıtlı ayarlara izin ver\", sonra tekrar dene.", 12.sp, C.Muted)
                Spacer(Modifier.height(8.dp))
                SmallButton("Uygulama bilgilerini aç") { Util.openAppDetails(ctx) }
            }
        }
        Card(border = if (batt) Color(0xFF2E5A40) else C.Line) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumBadge("2", batt, a11y)
                Spacer(Modifier.width(12.dp))
                Column {
                    T("Telefonun beni uyutmasın", 16.sp, weight = FontWeight.ExtraBold)
                    T("Yoksa arka planda sayamam", 13.sp, C.Muted)
                }
            }
            Spacer(Modifier.height(14.dp))
            if (batt) DoneBar("Tamamlandı") else GhostButton("Ayarla", height = 50.dp) { Util.requestBattery(ctx) }
        }
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Devam", enabled = a11y) { next() }
        if (!a11y) {
            T("Sonra yaparım", 14.sp, C.Muted, FontWeight.SemiBold, align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().tap { next() }.padding(vertical = 10.dp))
        }
    }
}

@Composable
private fun NumBadge(n: String, done: Boolean, active: Boolean) {
    Box(
        Modifier.size(32.dp).clip(CircleShape).background(if (done) C.GreenBg else if (active) C.Amber else C.Line),
        contentAlignment = Alignment.Center
    ) {
        if (done) Icon(Icons.check, null, tint = C.Green, modifier = Modifier.size(16.dp))
        else T(n, 15.sp, if (active) C.OnAmber else C.Text, FontWeight.ExtraBold)
    }
}

@Composable
private fun DoneBar(text: String) {
    Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)).background(C.GreenBg), contentAlignment = Alignment.Center) {
        T(text, 15.sp, C.Green, FontWeight.ExtraBold)
    }
}

// ---------------------------------------------------------------- 14 · Hesap
@Composable
private fun StepHesap(back: () -> Unit, next: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressTop(0.97f, back)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Art(R.drawable.fener0, 170.dp, desc = "Fener tutan enik")
            Title("İlerlemeni saklamak ister misin?", 26.sp, TextAlign.Center)
            T("İstersen hesapsız devam et. Hesap açarsan:", 15.sp, C.Muted, align = TextAlign.Center)
        }
        listOf("Telefon değişse de serin ve ${Store.name} kaybolmaz", "Arkadaşlarınla az kaydırma yarışı yaparsın").forEach {
            Row(verticalAlignment = Alignment.CenterVertically) { Dot(C.Amber); Spacer(Modifier.width(12.dp)); T(it, 15.sp, C.Text2) }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp)).background(C.Surface2), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T("Google ile devam et", 16.sp, C.Muted, FontWeight.ExtraBold)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.clip(RoundedCornerShape(50)).background(C.AmberBg).padding(horizontal = 8.dp, vertical = 3.dp)) { T("Yakında", 11.sp, C.Amber, FontWeight.ExtraBold) }
            }
        }
        PrimaryButton("Hesapsız devam et") { next() }
        T("Hesapsız kullanımda her şey sadece telefonunda kalır.", 12.sp, C.Faint, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// ---------------------------------------------------------------- 15 · Hazır
@Composable
private fun StepHazir(onDone: (Boolean) -> Unit) {
    val ctx = LocalContext.current
    val bounce = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        Feedback.celebrate(); Feedback.sound("chime", 0.8f)
        bounce.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(30.dp))
        Art(R.drawable.kutlama, 240.dp, Modifier.graphicsLayer { scaleX = 0.6f + 0.4f * bounce.value; scaleY = 0.6f + 0.4f * bounce.value }, "Sevinçle zıplayan enik")
        Title("Her şey hazır!", 30.sp, TextAlign.Center)
        T("Instagram'ı aç; Reels'e girince\n${Store.name} ekranın üstünde seni bekliyor olacak.", 16.sp, C.Text2, align = TextAlign.Center)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(22.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(C.Warm), contentAlignment = Alignment.Center) { Face(0, 52.dp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                T("Ana ekrana ekle", 15.sp, weight = FontWeight.ExtraBold)
                T("Eniğini telefonunu her açtığında gör", 13.sp, C.Muted)
            }
            SmallButton("Ekle") { FerWidget.requestPin(ctx) }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Instagram'ı aç") { onDone(true) }
        T("Önce uygulamayı gezeyim", 14.sp, C.Text2, FontWeight.SemiBold, modifier = Modifier.tap { onDone(false) }.padding(10.dp))
    }
}
