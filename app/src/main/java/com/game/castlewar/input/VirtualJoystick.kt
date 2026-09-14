package com.game.castlewar.input

import kotlin.math.sqrt

/**
 * On-screen virtual joystick for controlling the colossal moving fortress.
 * Anchored to the left half of the display with smooth normalized output and deadzone handling.
 */
class VirtualJoystick {

    var isActive: Boolean = false
    var pointerId: Int = -1

    var centerX: Float = 220f
    var centerY: Float = 750f
    var knobX: Float = 220f
    var knobY: Float = 750f

    val baseRadius: Float = 110f
    val knobRadius: Float = 48f
    val deadZone: Float = 0.12f

    var inputX: Float = 0f
        private set
    var inputY: Float = 0f
        private set

    fun reset() {
        isActive = false
        pointerId = -1
        inputX = 0f
        inputY = 0f
        knobX = centerX
        knobY = centerY
    }

    fun onTouchDown(id: Int, x: Float, y: Float, screenWidth: Float, screenHeight: Float): Boolean {
        // Only accept touches on the left half of the screen
        if (x < screenWidth * 0.55f && !isActive) {
            isActive = true
            pointerId = id
            // Dynamic placement near touch location
            centerX = x.coerceIn(baseRadius + 20f, screenWidth * 0.5f)
            centerY = y.coerceIn(baseRadius + 20f, screenHeight - baseRadius - 20f)
            knobX = centerX
            knobY = centerY
            inputX = 0f
            inputY = 0f
            return true
        }
        return false
    }

    fun onTouchMove(id: Int, x: Float, y: Float) {
        if (isActive && pointerId == id) {
            val dx = x - centerX
            val dy = y - centerY
            val dist = sqrt(dx * dx + dy * dy)

            if (dist > baseRadius) {
                knobX = centerX + (dx / dist) * baseRadius
                knobY = centerY + (dy / dist) * baseRadius
            } else {
                knobX = x
                knobY = y
            }

            val rawX = (knobX - centerX) / baseRadius
            val rawY = (knobY - centerY) / baseRadius
            val mag = sqrt(rawX * rawX + rawY * rawY)

            if (mag < deadZone) {
                inputX = 0f
                inputY = 0f
            } else {
                val normalizedMag = (mag - deadZone) / (1.0f - deadZone)
                inputX = (rawX / mag) * normalizedMag
                inputY = (rawY / mag) * normalizedMag
            }
        }
    }

    fun onTouchUp(id: Int) {
        if (isActive && pointerId == id) {
            reset()
        }
    }
}
