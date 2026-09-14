package com.game.castlewar.render

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.game.castlewar.game.GameManager
import com.game.castlewar.input.VirtualJoystick
import com.game.castlewar.world.GameWorld
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/**
 * Custom GLSurfaceView for Castle War.
 * Configured specifically for OpenGL ES continuous rendering with resilient EGL fallback.
 */
class GameSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    val gameManager: GameManager = GameManager(),
    val gameWorld: GameWorld = GameWorld(),
    val joystick: VirtualJoystick = VirtualJoystick()
) : GLSurfaceView(context, attrs) {

    val renderer: GameRenderer

    init {
        // Explicitly set pixel format for hardware compositor and SurfaceSyncGroup alignment
        holder.setFormat(android.graphics.PixelFormat.RGBA_8888)

        // Request OpenGL ES 3.0 context with automatic fallback
        setEGLContextClientVersion(3)

        // Resilient standard EGL chooser (8-8-8-8 RGBA, 16-bit depth)
        try {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        } catch (e: Exception) {
            setEGLConfigChooser(true)
        }

        // Resilient Context Factory that tries ES 3.0 first and falls back to ES 2.0
        setEGLContextFactory(ResilientEGLContextFactory())

        // Preserve EGL context across pause/resume cycles if hardware supports it
        preserveEGLContextOnPause = true

        // Initialize and bind the renderer
        renderer = GameRenderer(context, gameManager, gameWorld, joystick)
        setRenderer(renderer)

        // Render continuously for smooth 60 FPS gameplay
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onResume() {
        super.onResume()
        renderer.onResume()
    }

    override fun onPause() {
        renderer.onPause()
        super.onPause()
    }

    fun onDestroy() {
        renderer.onDestroy()
    }

    private class ResilientEGLContextFactory : EGLContextFactory {
        override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): javax.microedition.khronos.egl.EGLContext {
            val EGL_CONTEXT_CLIENT_VERSION = 0x3098
            // Try OpenGL ES 3.0
            var context = egl.eglCreateContext(
                display, config, EGL10.EGL_NO_CONTEXT,
                intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE)
            )
            if (context == null || context == EGL10.EGL_NO_CONTEXT) {
                // Fallback to OpenGL ES 2.0
                context = egl.eglCreateContext(
                    display, config, EGL10.EGL_NO_CONTEXT,
                    intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
                )
            }
            return context
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay, context: javax.microedition.khronos.egl.EGLContext) {
            egl.eglDestroyContext(display, context)
        }
    }
}
