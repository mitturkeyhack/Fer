package com.erkam.fer.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkam.fer.Feedback
import com.erkam.fer.R
import kotlinx.coroutines.delay
import kotlin.random.Random

object C {
    val Bg = Color(0xFF0B0C10)
    val Surface = Color(0xFF14161D)
    val Surface2 = Color(0xFF1B1E27)
    val Nav = Color(0xFF0E0F14)
    val Line = Color(0xFF22252E)
    val Line2 = Color(0xFF2A2D36)
    val Line3 = Color(0xFF3A3D46)
    val Text = Color(0xFFF5F3EE)
    val Text2 = Color(0xFFC9C7C0)
    val Muted = Color(0xFFA9A8A3)
    val Faint = Color(0xFF8A8984)
    val Amber = Color(0xFFFFB547)
    val AmberBg = Color(0xFF2A2113)
    val Warm = Color(0xFF1E1A15)
    val OnAmber = Color(0xFF1A1206)
    val Green = Color(0xFF8FD3A8)
    val GreenBg = Color(0xFF16281E)
    val Coral = Color(0xFFFF8A6B)
    val Red = Color(0xFFFF6B5A)
}

val FACES = intArrayOf(R.drawable.dog0, R.drawable.dog1, R.drawable.dog2, R.drawable.dog3, R.drawable.dog4)
val BLINKS = intArrayOf(R.drawable.blink0, R.drawable.blink1, 0, R.drawable.blink3, R.drawable.blink4)
val FENERS = intArrayOf(R.drawable.fener0, R.drawable.fener1, R.drawable.fener2, R.drawable.fener3, R.drawable.fener4)

@Composable
fun T(
    text: String,
    size: TextUnit = 15.sp,
    color: Color = C.Text,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign? = null,
    modifier: Modifier = Modifier,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight,
            textAlign = align ?: TextAlign.Unspecified,
            lineHeight = if (lineHeight == TextUnit.Unspecified) (size.value * 1.4f).sp else lineHeight,
            letterSpacing = letterSpacing,
        )
    )
}

@Composable
fun Title(text: String, size: TextUnit = 28.sp, align: TextAlign? = null, modifier: Modifier = Modifier) =
    T(text, size, C.Text, FontWeight.ExtraBold, align, modifier, lineHeight = (size.value * 1.18f).sp, letterSpacing = (-0.4).sp)

/** Basılabilir alan: hafif titreşim + ripple'sız. */
fun Modifier.tap(haptic: Boolean = true, onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        role = Role.Button,
    ) {
        if (haptic) Feedback.click()
        onClick()
    }
)

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) C.Amber else C.Surface2)
            .then(if (enabled) Modifier.tap(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        T(text, 16.sp, if (enabled) C.OnAmber else C.Muted, FontWeight.ExtraBold)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, height: Dp = 52.dp, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .border(1.dp, C.Line3, RoundedCornerShape(height / 2))
            .tap(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { T(text, 15.sp, C.Text, FontWeight.Bold) }
}

@Composable
fun SmallButton(text: String, filled: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (filled) C.Amber else Color.Transparent)
            .border(1.dp, if (filled) C.Amber else C.Line3, RoundedCornerShape(20.dp))
            .tap(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) { T(text, 13.sp, if (filled) C.OnAmber else C.Text, FontWeight.Bold) }
}

@Composable
fun Card(modifier: Modifier = Modifier, bg: Color = C.Surface, border: Color = C.Line, radius: Dp = 22.dp, padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(radius))
            .padding(padding),
        content = content
    )
}

@Composable
fun ProgressTop(progress: Float, onBack: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(C.Surface).border(1.dp, C.Line, CircleShape).tap(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.back, "Geri", tint = C.Text, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
        }
        val p by animateFloatAsState(progress, tween(450), label = "p")
        Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(C.Line)) {
            Box(Modifier.fillMaxWidth(p).height(6.dp).clip(RoundedCornerShape(3.dp)).background(C.Amber))
        }
    }
}

@Composable
fun Toggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    val x by animateFloatAsState(if (checked) 22f else 0f, tween(180), label = "x")
    Box(
        Modifier
            .width(52.dp).height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) C.Amber else C.Line2)
            .tap { onChange(!checked) }
            .padding(3.dp)
    ) {
        Box(Modifier.offset(x = x.dp).size(24.dp).clip(CircleShape).background(if (checked) C.OnAmber else C.Faint))
    }
}

@Composable
fun Chip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(if (selected) C.AmberBg else C.Surface)
            .border(1.5.dp, if (selected) C.Amber else C.Line2, RoundedCornerShape(23.dp))
            .tap(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) { T(text, 15.sp, C.Text, FontWeight.Bold) }
}

/** Göz kırpan enik kafası. */
@Composable
fun Face(stage: Int, size: Dp, modifier: Modifier = Modifier, blink: Boolean = true) {
    val s = stage.coerceIn(0, 4)
    var closed by remember { mutableStateOf(false) }
    if (blink && BLINKS[s] != 0) {
        LaunchedEffect(s) {
            while (true) {
                delay(4000L + Random.nextLong(4000L))
                closed = true
                delay(170)
                closed = false
            }
        }
    }
    val res = if (closed && BLINKS[s] != 0) BLINKS[s] else FACES[s]
    Image(painterResource(res), contentDescription = "Enik", modifier = modifier.size(size))
}

@Composable
fun Art(@DrawableRes res: Int, width: Dp, modifier: Modifier = Modifier, desc: String? = null) {
    Image(painterResource(res), contentDescription = desc, modifier = modifier.width(width))
}

@Composable
fun FaceCircle(stage: Int, size: Dp, bg: Color = C.Warm) {
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Face(stage, size * 0.86f)
    }
}

@Composable
fun Dot(color: Color, size: Dp = 8.dp) = Box(Modifier.size(size).clip(CircleShape).background(color))

@Composable
fun Screen(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(C.Bg).padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/** Çizgi simgeler (Phosphor tarzı, 24'lük ızgara). */
object Icons {
    private fun icon(name: String, path: String, stroke: Float = 1.9f): ImageVector =
        Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData = PathParser().parsePathString(path).toNodes(),
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ).build()

    val back = icon("back", "M15 6l-6 6 6 6", 2.2f)
    val chevron = icon("chev", "M9 6l6 6-6 6", 2f)
    val home = icon("home", "M4 10.5 12 4l8 6.5V19a1.5 1.5 0 0 1-1.5 1.5H15v-6h-6v6H5.5A1.5 1.5 0 0 1 4 19z")
    val shield = icon("shield", "M12 3l7.5 3v5.5c0 4.6-3.2 8.3-7.5 9.5-4.3-1.2-7.5-4.9-7.5-9.5V6z M9 12l2.2 2.2L15.5 10")
    val friends = icon("friends", "M9 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7z M2.5 20c.6-3.4 3.3-5.5 6.5-5.5s5.9 2.1 6.5 5.5 M16 4.5a3.3 3.3 0 0 1 0 6.4 M18 14.8c1.9.7 3.2 2.5 3.5 5.2")
    val book = icon("book", "M7 3.5h10.5A1.5 1.5 0 0 1 19 5v14a1.5 1.5 0 0 1-1.5 1.5H7z M7 3.5v17 M10.5 8.5H16 M10.5 12H16 M4.5 7.5H7 M4.5 12H7 M4.5 16.5H7")
    val user = icon("user", "M12 12a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7z M5 19.5c1-3.3 3.8-5 7-5s6 1.7 7 5")
    val moon = icon("moon", "M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z")
    val timer = icon("timer", "M12 21a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M12 9v4l2.5 1.5 M9.5 2.5h5")
    val eyeOff = icon("eyeoff", "M3 3l18 18 M10.6 6.1A9.8 9.8 0 0 1 12 6c5 0 8.5 4.5 9.5 6-.5.8-1.4 2-2.7 3.1 M6.6 7.6C4.6 8.9 3.2 10.8 2.5 12c1 1.5 4.5 6 9.5 6 1.5 0 2.9-.4 4.1-1")
    val lock = icon("lock", "M6 11V8a6 6 0 0 1 12 0v3 M5 11h14v9.5H5z M12 15v2.5")
    val check = icon("check", "M5 12.5l4.5 4.5L19 7.5", 2.6f)
    val flask = icon("flask", "M9 3h6M10 3v6l-5 9a2 2 0 0 0 1.7 3h10.6a2 2 0 0 0 1.7-3l-5-9V3 M7.5 15h9")
    val info = icon("info", "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M12 8v5 M12 16.5v.5")
    val heart = icon("heart", "M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z")
    val access = icon("access", "M12 6.3a1.8 1.8 0 1 0 0-3.6 1.8 1.8 0 0 0 0 3.6z M5 8l7 1.5L19 8 M12 9.5V15 M12 15l-3 6 M12 15l3 6")
    val battery = icon("battery", "M3 7h16v10H3z M22 11v2 M7 10.5v3")
    val share = icon("share", "M12 3v12 M7 8l5-5 5 5 M5 14v5.5h14V14")
}

@Composable
fun FlameIcon(size: Dp) {
    Icon(
        Builder(name = "flame", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData = PathParser().parsePathString("M12 2.5c.6 3-1.6 4.6-2.9 6.4C7.6 10.9 6.5 12.6 6.5 15a5.5 5.5 0 0 0 11 0c0-2.2-1-3.9-2-5.1-.3 1.4-1.1 2.3-2.1 2.6.6-3.4-.3-7.2-1.4-10z").toNodes(),
                fill = SolidColor(Color.White)
            ).build(),
        contentDescription = null, tint = Color(0xFFFF8A3D), modifier = Modifier.size(size)
    )
}
