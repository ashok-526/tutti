package app.tutti

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.tutti.session.Screen
import app.tutti.session.TipJar
import app.tutti.session.TuttiState
import app.tutti.ui.ConductScreen
import app.tutti.ui.FinaleScreen
import app.tutti.ui.Motion
import app.tutti.ui.ProgrammeScreen
import app.tutti.ui.ScoreScreen
import app.tutti.ui.TuttiTheme
import app.tutti.ui.animationsEnabled
import app.tutti.ui.tutti

class MainActivity : ComponentActivity() {
    private lateinit var state: TuttiState

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        state = TuttiState(applicationContext)
        if (TipJar.configure(applicationContext)) state.tipJar.load()
        state.rehearsalSpeed = intent.getFloatExtra("rehearsalSpeed", state.rehearsalSpeed)
        if (intent.getBooleanExtra("record", false)) {
            // Demo capture: record everything the orchestra plays from launch onwards.
            state.sessionRecording = true
            state.orchestra.record(java.io.File(getExternalFilesDir(null) ?: cacheDir, "session.wav"))
            Log.i("Tutti", "session-recording-start ${System.currentTimeMillis()}")
        }
        state.orchestra.start()
        setContent { TuttiTheme { TuttiApp(state) } }
    }

    override fun onDestroy() {
        state.orchestra.stop()
        super.onDestroy()
    }
}

@Composable
fun TuttiApp(state: TuttiState) {
    val view = LocalView.current
    val colors = tutti
    val onStage = state.screen == Screen.CONDUCT
    val motion = animationsEnabled()
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !colors.dark
            isAppearanceLightNavigationBars = !colors.dark
        }
    }
    DisposableEffect(onStage) {
        view.keepScreenOn = onStage
        onDispose { view.keepScreenOn = false }
    }
    BackHandler(enabled = state.screen != Screen.PROGRAMME) { state.back() }

    AnimatedContent(
        targetState = state.screen,
        modifier = Modifier.fillMaxSize().background(colors.plinth),
        transitionSpec = {
            if (motion) {
                (fadeIn(tween(240, delayMillis = 80, easing = Motion.out)) +
                    scaleIn(tween(280, delayMillis = 80, easing = Motion.out), initialScale = 0.97f)) togetherWith
                    fadeOut(tween(90))
            } else {
                fadeIn(tween(0)) togetherWith fadeOut(tween(0))
            }
        },
        label = "screen",
    ) { screen ->
        when (screen) {
            Screen.PROGRAMME -> ProgrammeScreen(state)
            Screen.SCORE -> ScoreScreen(state)
            Screen.CONDUCT -> state.performance?.let { ConductScreen(state, it) }
            Screen.FINALE -> FinaleScreen(state)
        }
    }
}
