package com.game.castlewar.core

import android.os.SystemClock

/**
 * High-precision fixed-timestep game loop architecture.
 *
 * Runs physics/logic updates at a deterministic fixed interval (default 60 updates/sec)
 * while rendering dynamically to match display refresh or GL frame pacing with
 * fractional interpolation.
 */
class GameLoop(
    val targetFps: Int = TARGET_FPS,
    private val callback: Callback
) {
    interface Callback {
        fun onUpdate(deltaTime: Float)
        fun onRender(interpolation: Float)
    }

    companion object {
        const val TARGET_FPS = 60
        const val FIXED_TIME_STEP_SEC = 1.0f / TARGET_FPS.toFloat() // ~0.0166667s
        private const val FIXED_TIME_STEP_NS = (1_000_000_000L / TARGET_FPS)
        private const val MAX_ACCUMULATED_NS = 250_000_000L // 250ms clamp against spiral of death
    }

    private var isRunning: Boolean = false
    private var isPaused: Boolean = false

    private var lastFrameTimeNs: Long = 0L
    private var accumulatorNs: Long = 0L

    // Metrics
    var currentFps: Int = 0
        private set
    private var frameCounter: Int = 0
    private var fpsTimerNs: Long = 0L

    private var isFirstFrame: Boolean = true

    fun start() {
        isRunning = true
        isPaused = false
        lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
        accumulatorNs = 0L
        frameCounter = 0
        fpsTimerNs = lastFrameTimeNs
        isFirstFrame = true
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        if (!isRunning) {
            start()
        } else {
            isPaused = false
            lastFrameTimeNs = SystemClock.elapsedRealtimeNanos()
            accumulatorNs = 0L
            isFirstFrame = true
        }
    }

    fun stop() {
        isRunning = false
    }

    /**
     * Called once per frame inside onDrawFrame().
     * Steps fixed physics/logic updates as needed, calculates interpolation, and renders.
     */
    fun onDrawFrameTick() {
        if (!isRunning || isPaused) {
            callback.onRender(0.0f)
            return
        }

        val currentFrameTimeNs = SystemClock.elapsedRealtimeNanos()
        if (isFirstFrame) {
            isFirstFrame = false
            lastFrameTimeNs = currentFrameTimeNs
            accumulatorNs = 0L
            callback.onUpdate(FIXED_TIME_STEP_SEC)
            callback.onRender(1.0f)
            return
        }

        var deltaNs = currentFrameTimeNs - lastFrameTimeNs
        lastFrameTimeNs = currentFrameTimeNs

        // Clamp delta time if frame dropped drastically (e.g. background switch or stall)
        if (deltaNs > MAX_ACCUMULATED_NS) {
            deltaNs = MAX_ACCUMULATED_NS
        }
        if (deltaNs < 0L) {
            deltaNs = 0L
        }

        accumulatorNs += deltaNs

        // Fixed timestep consumption
        while (accumulatorNs >= FIXED_TIME_STEP_NS) {
            callback.onUpdate(FIXED_TIME_STEP_SEC)
            accumulatorNs -= FIXED_TIME_STEP_NS
        }

        // Fractional interpolation for smooth rendering between logic ticks
        val interpolation = (accumulatorNs.toFloat() / FIXED_TIME_STEP_NS.toFloat()).coerceIn(0.0f, 1.0f)
        callback.onRender(interpolation)

        // FPS metric computation
        frameCounter++
        if (currentFrameTimeNs - fpsTimerNs >= 1_000_000_000L) {
            currentFps = frameCounter
            frameCounter = 0
            fpsTimerNs = currentFrameTimeNs
        }
    }
}
