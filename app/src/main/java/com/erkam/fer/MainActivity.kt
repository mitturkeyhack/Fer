package com.erkam.fer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.erkam.fer.ui.MainScreens
import com.erkam.fer.ui.Onboarding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        Feedback.init(this)
        Store.touchToday()
        setContent { FerApp() }
    }

    override fun onPause() {
        super.onPause()
        FerWidget.updateAll(this)
    }
}

@Composable
private fun FerApp() {
    @Suppress("UNUSED_VARIABLE") val v = Store.version.intValue
    val ctx = LocalContext.current
    if (!Store.onboarded) {
        Onboarding { openInstagram ->
            Store.onboarded = true
            if (openInstagram) Util.launchApp(ctx, Store.IG)
        }
    } else {
        MainScreens()
    }
}
