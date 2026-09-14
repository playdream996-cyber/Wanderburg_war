package com.game.castlewar.render

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.game.castlewar.core.GameLoop
import com.game.castlewar.game.GameManager
import com.game.castlewar.input.VirtualJoystick
import com.game.castlewar.world.GameWorld
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL ES 3.0 Renderer for Castle War.
 *
 * Coordinates surface lifecycle, viewport sizing, 3D world rendering,
 * and ties the fixed-timestep GameLoop to GL render frames.
 */
class GameRenderer(
    val context: Context,
    private val gameManager: GameManager,
    val gameWorld: GameWorld = GameWorld(),
    val joystick: VirtualJoystick = VirtualJoystick()
) : GLSurfaceView.Renderer, GameLoop.Callback {

    companion object {
        private const val TAG = "GameRenderer"
    }

    var viewportWidth: Int = 0
        private set
    var viewportHeight: Int = 0
        private set

    val gameLoop = GameLoop(targetFps = GameLoop.TARGET_FPS, callback = this)

    val camera = GameCamera()
    private val worldRenderer = WorldRenderer(context)

    // Brighter neutral battlefield base so the premium lighting pass does not crush shadows.
    private val clearRed = 0.16f
    private val clearGreen = 0.18f
    private val clearBlue = 0.15f
    private val clearAlpha = 1.0f

    private var lastCastleTier = gameWorld.castle.tier

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val glVersion = GLES30.glGetString(GLES30.GL_VERSION)
        val glRenderer = GLES30.glGetString(GLES30.GL_RENDERER)
        val glVendor = GLES30.glGetString(GLES30.GL_VENDOR)

        Log.i(TAG, "OpenGL ES Surface Created.")
        Log.i(TAG, "GL Version : $glVersion")
        Log.i(TAG, "GL Renderer: $glRenderer")
        Log.i(TAG, "GL Vendor  : $glVendor")

        GLES30.glClearColor(clearRed, clearGreen, clearBlue, clearAlpha)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        worldRenderer.initialize()
        gameLoop.start()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height

        Log.i(TAG, "OpenGL ES Viewport Changed: ${width}x$height")
        GLES30.glViewport(0, 0, width, height)
        camera.setViewport(width, height)
        camera.setCastleTier(gameWorld.castle.tier)
        camera.follow(gameWorld.castle.positionX, gameWorld.castle.positionY, true)
        camera.update(0.016f)
    }

    private var initialFramesPresented = 0

    override fun onDrawFrame(gl: GL10?) {
        if (initialFramesPresented < 1) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            initialFramesPresented++
            return
        }

        gameLoop.onDrawFrameTick()
    }

    override fun onUpdate(deltaTime: Float) {
        gameManager.update(deltaTime)

        if (gameManager.currentState.isSimulationActive) {
            gameWorld.update(deltaTime, joystick.inputX, joystick.inputY, camera)
        }

        val currentTier = gameWorld.castle.tier
        if (currentTier != lastCastleTier) {
            lastCastleTier = currentTier
            camera.setCastleTier(currentTier)
            camera.triggerUpgradePullback()
            camera.shake(intensity = 14f, duration = 0.35f)
        }

        camera.update(deltaTime)
    }

    override fun onRender(interpolation: Float) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        worldRenderer.render(gameWorld, camera)
    }

    fun onPause() {
        gameLoop.pause()
    }

    fun onResume() {
        gameLoop.resume()
    }

    fun onDestroy() {
        gameLoop.stop()
    }
}
