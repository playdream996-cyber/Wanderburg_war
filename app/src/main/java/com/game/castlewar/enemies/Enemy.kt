package com.game.castlewar.enemies

import com.game.castlewar.utils.CollisionUtils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class EnemyState {
    SPAWNING,
    MOVING,
    ATTACKING,
    HURT,
    DEAD
}

abstract class Enemy(
    var positionX: Float = 0f,
    var positionY: Float = 0f,
    var health: Float = 50f,
    var maxHealth: Float = 50f,
    var moveSpeed: Float = 60f,
    var damage: Float = 10f,
    var attackRange: Float = 35f,
    var attackCooldown: Float = 1.0f,
    var rewardXP: Int = 10,
    var rewardGold: Int = 2,
    val radius: Float = 16f
) {
    var state: EnemyState = EnemyState.MOVING
    var attackTimer: Float = 0f
    var hurtTimer: Float = 0f
    var rotation: Float = 0f
    var walkAnimTime: Float = 0f

    val isAlive: Boolean
        get() = state != EnemyState.DEAD && health > 0f

    open fun takeDamage(amount: Float): Boolean {
        if (!isAlive) return false
        health -= amount
        hurtTimer = 0.15f
        if (health <= 0f) {
            health = 0f
            state = EnemyState.DEAD
            return true // Fatal blow
        } else {
            state = EnemyState.HURT
        }
        return false
    }

    open fun update(
        deltaTime: Float,
        targetCastleX: Float,
        targetCastleY: Float,
        onFireProjectile: ((type: com.game.castlewar.weapons.ProjectileType, fromX: Float, fromY: Float, toX: Float, toY: Float, speed: Float, dmg: Float, radius: Float, expRadius: Float) -> Unit)? = null
    ): Float {
        if (!isAlive) return 0f

        walkAnimTime += deltaTime * 8f

        if (hurtTimer > 0f) {
            hurtTimer -= deltaTime
            if (hurtTimer <= 0f && state == EnemyState.HURT) {
                state = EnemyState.MOVING
            }
        }

        if (attackTimer > 0f) {
            attackTimer -= deltaTime
        }

        val dx = targetCastleX - positionX
        val dy = targetCastleY - positionY
        val distSq = dx * dx + dy * dy
        val dist = sqrt(distSq).coerceAtLeast(0.1f)

        // Face towards castle
        rotation = atan2(dy, dx)

        var dealtDamage = 0f

        if (dist <= attackRange) {
            state = EnemyState.ATTACKING
            if (attackTimer <= 0f) {
                dealtDamage = onAttackTriggered(targetCastleX, targetCastleY, onFireProjectile)
                attackTimer = attackCooldown
            }
        } else {
            state = EnemyState.MOVING
            val vx = (dx / dist) * moveSpeed
            val vy = (dy / dist) * moveSpeed
            positionX += vx * deltaTime
            positionY += vy * deltaTime
        }

        return dealtDamage
    }

    open fun onAttackTriggered(
        targetX: Float,
        targetY: Float,
        onFireProjectile: ((type: com.game.castlewar.weapons.ProjectileType, fromX: Float, fromY: Float, toX: Float, toY: Float, speed: Float, dmg: Float, radius: Float, expRadius: Float) -> Unit)?
    ): Float {
        return damage
    }
}
