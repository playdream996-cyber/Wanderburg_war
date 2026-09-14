package com.game.castlewar.render

import android.opengl.Matrix
import com.game.castlewar.model3d.MatrixUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real 3D Isometric Perspective Camera for Castle War.
 *
 * Configured for closer premium framing so the fortress and combat units remain readable
 * on mobile screens, while still supporting smooth follow, impact shake and boss zoom-outs.
 */
class GameCamera {

    // Target tracking coordinates on ground plane (3D X and Z)
    var targetX: Float = 0f
    var targetY: Float = 0f // Corresponds to World Z in 3D

    // Current smoothed camera look-at position
    var positionX: Float = 0f
    var positionY: Float = 0f

    // Isometric elevation and pitch
    var pitchDegrees: Float = 46.0f
    var baseCameraDistance: Float = 205.0f
    var cameraDistance: Float = 205.0f
    var fovYDegrees: Float = 42.0f
    var nearZ: Float = 10.0f
    var farZ: Float = 2500.0f

    // Smooth follow speed
    var followSpeed: Float = 7.5f

    // Zoom level (1.0 = standard, < 1.0 = zoomed out for boss/tier, > 1.0 = close up)
    var zoom: Float = 1.0f
    var targetZoom: Float = 1.0f
    var zoomSpeed: Float = 3.2f

    // Upgrade cinematic pullback
    private var upgradePullbackTimer: Float = 0f

    // Screen dimensions (pixels)
    var viewportWidth: Int = 1920
    var viewportHeight: Int = 1080

    // Screen Shake
    private var shakeDuration: Float = 0f
    private var shakeIntensity: Float = 0f
    var currentShakeOffsetX: Float = 0f
        private set
    var currentShakeOffsetY: Float = 0f
        private set
    var currentShakeOffsetZ: Float = 0f
        private set

    // Calculated 3D Camera Eye Position
    var eyeX: Float = 0f
        private set
    var eyeY: Float = 0f
        private set
    var eyeZ: Float = 0f
        private set

    // Matrices
    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    val viewProjectionMatrix = FloatArray(16)

    init {
        MatrixUtils.identity(viewMatrix)
        MatrixUtils.identity(projectionMatrix)
        MatrixUtils.identity(viewProjectionMatrix)
    }

    fun follow(x: Float, y: Float, snap: Boolean = false) {
        targetX = x
        targetY = y
        if (snap) {
            positionX = x
            positionY = y
        }
    }

    fun shake(intensity: Float = 12f, duration: Float = 0.25f) {
        if (intensity >= shakeIntensity || shakeDuration <= 0f) {
            shakeIntensity = intensity
            shakeDuration = duration
        }
    }

    // Stronger but short combat impact shakes for better mobile feedback
    fun shakeCannon() = shake(4.5f, 0.12f)
    fun shakeExplosion() = shake(8.5f, 0.18f)
    fun shakeBuildingCollapse() = shake(11.0f, 0.24f)
    fun shakeBossAttack() = shake(17.0f, 0.38f)
    fun shakeCastleDamage() = shake(12.5f, 0.24f)
    fun triggerCannonRecoilShake() = shake(4.5f, 0.12f)
    fun triggerCannonImpactShake() = shake(8.5f, 0.18f)

    /**
     * Castle upgrade: clearly visible pull-back, then smooth return to close framing.
     */
    fun triggerUpgradePullback() {
        zoomTo(0.80f, speed = 4.8f)
        upgradePullbackTimer = 1.0f
    }

    /**
     * Keeps larger tiers readable without making units feel tiny.
     */
    fun setCastleTier(tier: Int) {
        cameraDistance = baseCameraDistance + (tier.coerceIn(1, 5) - 1) * 10.0f
    }

    fun zoomTo(target: Float, speed: Float = 3.0f) {
        targetZoom = target
        zoomSpeed = speed
    }

    fun setViewport(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        updateProjectionMatrix()
    }

    private fun updateProjectionMatrix() {
        val aspect = if (viewportHeight > 0) viewportWidth.toFloat() / viewportHeight.toFloat() else 1.777f
        MatrixUtils.perspective(projectionMatrix, fovYDegrees, aspect, nearZ, farZ)
    }

    fun update(deltaTime: Float) {
        // Handle upgrade pullback timing
        if (upgradePullbackTimer > 0f) {
            upgradePullbackTimer -= deltaTime
            if (upgradePullbackTimer <= 0f) {
                targetZoom = 1.0f
                zoomSpeed = 2.8f
            }
        }

        // Smooth position follow with lerp
        val t = (followSpeed * deltaTime).coerceIn(0f, 1f)
        positionX += (targetX - positionX) * t
        positionY += (targetY - positionY) * t

        // Smooth zoom interpolation
        val zt = (zoomSpeed * deltaTime).coerceIn(0f, 1f)
        zoom += (targetZoom - zoom) * zt

        // Update screen shake decay
        if (shakeDuration > 0f) {
            shakeDuration -= deltaTime
            val factor = (shakeDuration / 0.25f).coerceIn(0f, 1f)
            val currentPower = shakeIntensity * factor
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            currentShakeOffsetX = cos(angle) * currentPower
            currentShakeOffsetY = sin(angle) * currentPower * 0.5f
            currentShakeOffsetZ = sin(angle) * currentPower
        } else {
            currentShakeOffsetX = 0f
            currentShakeOffsetY = 0f
            currentShakeOffsetZ = 0f
        }

        // Calculate 3D camera eye position based on pitch angle and zoom
        val pitchRad = Math.toRadians(pitchDegrees.toDouble())
        val effectiveDist = cameraDistance / zoom.coerceAtLeast(0.1f)

        val heightY = (sin(pitchRad) * effectiveDist).toFloat()
        val backZ = (cos(pitchRad) * effectiveDist).toFloat()

        eyeX = positionX + currentShakeOffsetX
        eyeY = heightY + currentShakeOffsetY
        eyeZ = positionY + backZ + currentShakeOffsetZ

        val lookAtX = positionX + currentShakeOffsetX
        val lookAtY = 0f + currentShakeOffsetY
        val lookAtZ = positionY + currentShakeOffsetZ

        // Compute 3D LookAt View Matrix
        MatrixUtils.lookAt(
            viewMatrix,
            eyeX, eyeY, eyeZ,
            lookAtX, lookAtY, lookAtZ,
            0f, 1f, 0f
        )

        // Ensure projection matrix is fresh
        updateProjectionMatrix()

        // Combine View-Projection Matrix
        MatrixUtils.multiply(viewProjectionMatrix, projectionMatrix, viewMatrix)
    }

    /**
     * Projects a 3D world position (X, Y, Z) to 2D screen pixels.
     */
    fun worldToScreen(worldX: Float, worldY: Float, outScreen: FloatArray) {
        val inVec = floatArrayOf(worldX, 0f, worldY, 1.0f)
        val clipVec = FloatArray(4)
        Matrix.multiplyMV(clipVec, 0, viewProjectionMatrix, 0, inVec, 0)

        if (clipVec[3] != 0f) {
            val ndcX = clipVec[0] / clipVec[3]
            val ndcY = clipVec[1] / clipVec[3]
            outScreen[0] = (ndcX * 0.5f + 0.5f) * viewportWidth
            outScreen[1] = (1.0f - (ndcY * 0.5f + 0.5f)) * viewportHeight
        } else {
            outScreen[0] = viewportWidth * 0.5f
            outScreen[1] = viewportHeight * 0.5f
        }
    }

    val finalCameraX: Float
        get() = positionX

    val finalCameraY: Float
        get() = positionY
}
