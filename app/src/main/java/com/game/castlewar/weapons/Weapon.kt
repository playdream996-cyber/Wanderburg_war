package com.game.castlewar.weapons

import com.game.castlewar.effects.ParticleManager
import com.game.castlewar.enemies.Enemy
import com.game.castlewar.render.GameCamera
import com.game.castlewar.utils.CollisionUtils
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Base class for all automated castle siege weapons.
 */
abstract class Weapon(
    val id: String,
    val name: String,
    var damage: Float,
    var range: Float,
    var attackSpeed: Float, // Attacks per second
    var projectileSpeed: Float,
    var criticalChance: Float = 0.05f
) {
    var cooldownTimer: Float = 0f
    var level: Int = 1
    var currentTarget: Enemy? = null
    var turretAngle: Float = 0f // Angle turret faces in radians

    // Relative socket offset on the castle
    var slotOffsetX: Float = 0f
    var slotOffsetY: Float = 0f

    /**
     * Ticks cooldown and updates targeting/firing.
     */
    fun update(
        deltaTime: Float,
        castleWorldX: Float,
        castleWorldY: Float,
        enemies: List<Enemy>,
        projectileManager: ProjectileManager,
        particleManager: ParticleManager,
        camera: GameCamera
    ) {
        if (cooldownTimer > 0f) {
            cooldownTimer -= deltaTime
        }

        val weaponWorldX = castleWorldX + slotOffsetX
        val weaponWorldY = castleWorldY + slotOffsetY

        // Validate or acquire target
        if (currentTarget == null || !isTargetValid(currentTarget, weaponWorldX, weaponWorldY)) {
            currentTarget = findTarget(weaponWorldX, weaponWorldY, enemies)
        }

        currentTarget?.let { target ->
            // Smoothly rotate turret toward target
            val targetAngle = atan2(target.positionY - weaponWorldY, target.positionX - weaponWorldX)
            turretAngle = targetAngle

            if (cooldownTimer <= 0f) {
                attack(weaponWorldX, weaponWorldY, target, projectileManager, particleManager, camera)
                cooldownTimer = 1.0f / attackSpeed.coerceAtLeast(0.1f)
            }
        }
    }

    protected open fun isTargetValid(target: Enemy?, originX: Float, originY: Float): Boolean {
        if (target == null || !target.isAlive) return false
        val distSq = CollisionUtils.distanceSquared(originX, originY, target.positionX, target.positionY)
        return distSq <= range * range
    }

    open fun findTarget(originX: Float, originY: Float, enemies: List<Enemy>): Enemy? {
        var closestEnemy: Enemy? = null
        var closestDistSq = range * range

        for (enemy in enemies) {
            if (!enemy.isAlive) continue
            val distSq = CollisionUtils.distanceSquared(originX, originY, enemy.positionX, enemy.positionY)
            if (distSq < closestDistSq) {
                closestDistSq = distSq
                closestEnemy = enemy
            }
        }
        return closestEnemy
    }

    abstract fun attack(
        originX: Float,
        originY: Float,
        target: Enemy,
        projectileManager: ProjectileManager,
        particleManager: ParticleManager,
        camera: GameCamera
    )

    abstract fun upgrade()
}
