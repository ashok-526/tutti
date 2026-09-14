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
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.tutti.session.Screen
import app.tutti.session.TuttiState
import app.tutti.ui.ConductScreen
import app.tutti.ui.FinaleScreen
import app.tutti.ui.ProgrammeScreen
import app.tutti.ui.ScoreScreen

class MainActivity : ComponentActivity() {
    private lateinit var state: TuttiState

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        state = TuttiState(applicationContext)
        if (intent.getBooleanExtra("record", false)) {
            // Demo capture: record everything the orchestra plays from launch onwards.
            state.sessionRecording = true
            state.orchestra.record(java.io.File(getExternalFilesDir(null) ?: cacheDir, "session.wav"))
            Log.i("Tutti", "session-recording-start ${System.currentTimeMillis()}")
        }
        state.orchestra.start()
        setContent { TuttiApp(state) }
    }

    override fun onDestroy() {
        state.orchestra.stop()
        super.onDestroy()
    }
}

@Composable
fun TuttiApp(state: TuttiState) {
    val view = LocalView.current
    val onStage = state.screen == Screen.CONDUCT
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !onStage
            isAppearanceLightNavigationBars = !onStage
        }
    }
    DisposableEffect(onStage) {
        view.keepScreenOn = onStage
        onDispose { view.keepScreenOn = false }
    }
    BackHandler(enabled = state.screen != Screen.PROGRAMME) { state.back() }

    AnimatedContent(
        targetState = state.screen,
        transitionSpec = { fadeIn(tween(520, delayMillis = 80)) togetherWith fadeOut(tween(260)) },
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
