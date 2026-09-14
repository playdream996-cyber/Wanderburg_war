package com.game.castlewar.resources

import com.game.castlewar.utils.CollisionUtils
import kotlin.math.sqrt

/**
 * World pickup drop (XP gems, gold bags, wood, stone, iron, crystals).
 * Automatically attracts toward the fortress with smooth magnetic pull.
 */
class ResourcePickup(
    var positionX: Float = 0f,
    var positionY: Float = 0f,
    var type: ResourceType = ResourceType.EXPERIENCE,
    var amount: Int = 1
) {
    var isActive: Boolean = true
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var pickupRadius: Float = 14f

    var bobbingTimer: Float = 0f

    fun reset(x: Float, y: Float, resType: ResourceType, amt: Int) {
        positionX = x
        positionY = y
        type = resType
        amount = amt
        isActive = true
        velocityX = 0f
        velocityY = 0f
        bobbingTimer = (x + y) % 6.28f
    }

    /**
     * Updates magnetic pull toward castle when inside magnet range.
     */
    fun update(deltaTime: Float, targetX: Float, targetY: Float, magnetRange: Float): Boolean {
        if (!isActive) return false

        bobbingTimer += deltaTime * 4f

        val distSq = CollisionUtils.distanceSquared(positionX, positionY, targetX, targetY)
        val magnetRangeSq = magnetRange * magnetRange

        if (distSq <= magnetRangeSq) {
            val dist = sqrt(distSq).coerceAtLeast(0.1f)
            val dirX = (targetX - positionX) / dist
            val dirY = (targetY - positionY) / dist

            // Exponential magnetic pull speed as it nears the castle
            val pullSpeed = 220f + (1.0f - (dist / magnetRange).coerceIn(0f, 1f)) * 550f
            velocityX = dirX * pullSpeed
            velocityY = dirY * pullSpeed

            positionX += velocityX * deltaTime
            positionY += velocityY * deltaTime

            // Collection threshold reached (near castle core)
            if (dist <= 36f) {
                isActive = false
                return true // Collected!
            }
        } else {
            // Apply light friction if drifting
            velocityX *= 0.85f
            velocityY *= 0.85f
            positionX += velocityX * deltaTime
            positionY += velocityY * deltaTime
        }

        return false
    }
}
