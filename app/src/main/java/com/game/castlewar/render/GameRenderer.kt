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

    // Viewport dimensions
    var viewportWidth: Int = 0
        private set
    var viewportHeight: Int = 0
        private set

    // Game loop timing instance
    val gameLoop = GameLoop(targetFps = GameLoop.TARGET_FPS, callback = this)

    // Visual rendering subsystems
    val camera = GameCamera()
    private val worldRenderer = WorldRenderer(context)

    // Clear color (Atmospheric dark medieval battlefield slate)
    private val clearRed = 0.11f
    private val clearGreen = 0.13f
    private val clearBlue = 0.10f
    private val clearAlpha = 1.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val glVersion = GLES30.glGetString(GLES30.GL_VERSION)
        val glRenderer = GLES30.glGetString(GLES30.GL_RENDERER)
        val glVendor = GLES30.glGetString(GLES30.GL_VENDOR)

        Log.i(TAG, "OpenGL ES Surface Created.")
        Log.i(TAG, "GL Version : $glVersion")
        Log.i(TAG, "GL Renderer: $glRenderer")
        Log.i(TAG, "GL Vendor  : $glVendor")

        // Set clear color
        GLES30.glClearColor(clearRed, clearGreen, clearBlue, clearAlpha)

        // Depth and blending state
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // Initialize world batch shader and buffers
        worldRenderer.initialize()

        // Start timing loop
        gameLoop.start()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height

        Log.i(TAG, "OpenGL ES Viewport Changed: ${width}x$height")
        GLES30.glViewport(0, 0, width, height)
        camera.setViewport(width, height)
        camera.follow(gameWorld.castle.positionX, gameWorld.castle.positionY, true)
        camera.update(0.016f)
    }

    private var initialFramesPresented = 0

    override fun onDrawFrame(gl: GL10?) {
        // Guarantee the very first frame presents immediately (<5ms) to satisfy SurfaceSyncGroup
        if (initialFramesPresented < 1) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            initialFramesPresented++
            return
        }

        // Ticks fixed timestep updates and fires onRender callback
        gameLoop.onDrawFrameTick()
    }

    /**
     * Fixed-timestep logic tick callback from GameLoop.
     */
    override fun onUpdate(deltaTime: Float) {
        gameManager.update(deltaTime)

        if (gameManager.currentState.isSimulationActive) {
            gameWorld.update(deltaTime, joystick.inputX, joystick.inputY, camera)
        }
        camera.update(deltaTime)
    }

    /**
     * Render callback from GameLoop.
     * @param interpolation Fractional alpha [0..1] between the last two physics frames.
     */
    override fun onRender(interpolation: Float) {
        // Clear color and depth buffers
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Render the 2.5D medieval world, moving fortress, enemies, projectiles, and particles
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

