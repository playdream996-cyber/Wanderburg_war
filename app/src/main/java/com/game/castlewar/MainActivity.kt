package com.game.castlewar

import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.game.castlewar.game.GameManager
import com.game.castlewar.input.VirtualJoystick
import com.game.castlewar.render.GameSurfaceView
import com.game.castlewar.ui.FantasyGameOverlayView
import com.game.castlewar.world.GameWorld

/**
 * Main entry activity for Castle War.
 * Hosts the OpenGL battlefield with the premium fantasy HUD layered above it.
 */
class MainActivity : ComponentActivity() {

    private lateinit var gameSurfaceView: GameSurfaceView
    private lateinit var gameOverlayView: FantasyGameOverlayView

    private val gameManager = GameManager()
    private val gameWorld = GameWorld()
    private val joystick = VirtualJoystick()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        gameSurfaceView = GameSurfaceView(
            context = this,
            gameManager = gameManager,
            gameWorld = gameWorld,
            joystick = joystick
        )

        gameOverlayView = FantasyGameOverlayView(
            context = this,
            gameManager = gameManager,
            gameWorld = gameWorld,
            joystick = joystick
        ).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        rootLayout.addView(gameSurfaceView)
        rootLayout.addView(gameOverlayView)
        setContentView(rootLayout)
    }

    override fun onResume() {
        super.onResume()
        gameSurfaceView.onResume()
    }

    override fun onPause() {
        gameSurfaceView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        gameSurfaceView.onDestroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setupImmersiveFullscreen()
    }

    private fun setupImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
