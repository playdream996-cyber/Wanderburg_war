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
 * Coordinates the premium decor pass, core world pass and fixed-step game loop.
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
    private val premiumDecorRenderer = PremiumWorldDecorRenderer(context)
    private val worldRenderer = WorldRenderer(context)

    // Cleaner blue-green atmospheric base to complement bright fantasy terrain.
    private val clearRed = 0.12f
    private val clearGreen = 0.17f
    private val clearBlue = 0.18f
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

        premiumDecorRenderer.initialize()
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
            val evolved = currentTier > lastCastleTier
            lastCastleTier = currentTier
            camera.setCastleTier(currentTier)

            if (evolved) {
                camera.triggerUpgradePullback()
                camera.shake(intensity = 14f, duration = 0.35f)
            } else {
                camera.zoomTo(target = 1.0f, speed = 5.0f)
            }
        }

        camera.update(deltaTime)
    }

    override fun onRender(interpolation: Float) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Decorative forests/landmarks render first; terrain/core world then resolves depth on top.
        premiumDecorRenderer.render(camera)
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
        premiumDecorRenderer.release()
    }
}
