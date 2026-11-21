package com.erkam.fer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkam.fer.ui.Art
import com.erkam.fer.ui.C
import com.erkam.fer.ui.GhostButton
import com.erkam.fer.ui.Icons
import com.erkam.fer.ui.PrimaryButton
import com.erkam.fer.ui.StatTile
import com.erkam.fer.ui.T
import com.erkam.fer.ui.Title
import com.erkam.fer.ui.tap
import java.time.LocalTime

/** Sınır dolunca ya da gece kilidinde açılan mola ekranı. */
class MolaActivity : ComponentActivity() {
    private var night by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        Feedback.init(this)
        night = intent.getBooleanExtra("night", false)
        setContent {
            Mola(
                night = night,
                leave = { FerService.instance?.goHome(); finish() },
                snooze = { Store.snoozeUntil = System.currentTimeMillis() + 5 * 60 * 1000L; finish() },
                unlimited = { Store.unlimitedDay = Store.today(); finish() }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        night = intent.getBooleanExtra("night", false)
    }
}

private val NUGGETS = listOf(
    "98.299 kişilik araştırmaya göre kısa videolar en çok dikkat ve kendini durdurma gücüyle ilişkili.",
    "Kısa video akışı, yapmayı planladığın işleri hatırlamayı zorlaştırabiliyor.",
    "Kısa video bağımlılığı daha kötü uyku ve daha çok erteleme ile bağlantılı bulundu.",
)
private val MANEVI = listOf(
    "Ömür bir sermaye; her dakikası kıymetli.",
    "Geçen vakit geri gelmez, ama şimdiki an hâlâ senin.",
    "Göz de kalp de dinlenmek ister. Biraz sükûnet iyi gelir.",
)

@Composable
private fun Mola(night: Boolean, leave: () -> Unit, snooze: () -> Unit, unlimited: () -> Unit) {
    BackHandler { leave() }
    val c = Store.count()
    val pop = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 250f)) }
    val line = remember { if (Store.manevi) MANEVI.random() else NUGGETS.random() }
    Column(
        Modifier.fillMaxSize().background(Color(0xFF08090C)).padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier.size(210.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }.clip(CircleShape)
                .background(if (night) C.Surface else Color(0xFFE9E6DF)),
            contentAlignment = Alignment.Center
        ) {
            Art(if (night) R.drawable.yatak else R.drawable.fener4, if (night) 190.dp else 160.dp)
        }
        if (night) {
            Title("Uyku vakti", 30.sp, TextAlign.Center)
            val now = LocalTime.now()
            T("Saat ${"%02d:%02d".format(now.hour, now.minute)}. ${Store.name} uyumak istiyor;\nsen de dinlen, yarın yine buradayız.", 15.sp, C.Text2, align = TextAlign.Center)
        } else {
            Title("Bugünlük bu kadar", 30.sp, TextAlign.Center)
            T("${Store.name} çok yoruldu, fenerinin ışığı bitti.\nBiraz nefes alma zamanı.", 15.sp, C.Text2, align = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("$c", "reel bugün", modifier = Modifier.weight(1f))
                StatTile("~${Store.minutes(c)} dk", "tahmini süre", modifier = Modifier.weight(1f))
            }
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.Surface).border(1.dp, C.Line, RoundedCornerShape(18.dp)).padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(if (Store.manevi) Icons.heart else Icons.flask, null, tint = C.Amber, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            T(line, 14.sp, C.Text2)
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(if (night) "Telefonu bırak" else "Uygulamadan çık") { Feedback.click(); leave() }
        if (!Store.strict) {
            GhostButton("5 dakika daha") { snooze() }
            if (!night) {
                T("Bugün için sınırı kaldır", 13.sp, C.Muted, FontWeight.SemiBold, modifier = Modifier.tap { unlimited() }.padding(10.dp))
            }
        }
    }
}
