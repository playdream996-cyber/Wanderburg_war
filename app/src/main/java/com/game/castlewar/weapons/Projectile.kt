package com.game.castlewar.weapons

import com.game.castlewar.effects.ParticleManager
import com.game.castlewar.render.GameCamera
import com.game.castlewar.utils.CollisionUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ProjectileType {
    CANNONBALL,
    ARROW,
    BALLISTA_BOLT,
    MAGIC_ORB,
    FIREBALL
}

class Projectile {
    var isActive: Boolean = false
    var type: ProjectileType = ProjectileType.CANNONBALL
    var x: Float = 0f
    var y: Float = 0f
    var startX: Float = 0f
    var startY: Float = 0f
    var targetX: Float = 0f
    var targetY: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var damage: Float = 30f
    var radius: Float = 10f
    var explosionRadius: Float = 0f
    var lifetime: Float = 2.0f
    var maxLifetime: Float = 2.0f
    var arcHeight: Float = 0f // Arc height for ballistic cannonballs
    var pierceCount: Int = 1
    var isEnemyProjectile: Boolean = false
    var hitEntities = mutableSetOf<Int>()

    fun reset(
        pType: ProjectileType,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        speed: Float,
        dmg: Float,
        rad: Float,
        expRadius: Float = 0f,
        pierce: Int = 1,
        isEnemy: Boolean = false
    ) {
        type = pType
        x = fromX
        y = fromY
        startX = fromX
        startY = fromY
        targetX = toX
        targetY = toY
        damage = dmg
        radius = rad
        explosionRadius = expRadius
        pierceCount = pierce
        isEnemyProjectile = isEnemy
        hitEntities.clear()

        val dx = toX - fromX
        val dy = toY - fromY
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

        lifetime = dist / speed
        maxLifetime = lifetime

        vx = (dx / dist) * speed
        vy = (dy / dist) * speed

        arcHeight = if (pType == ProjectileType.CANNONBALL) 40f else 0f
        isActive = true
    }

    /**
     * Updates projectile position. Returns true if it arrived / expired.
     */
    fun update(deltaTime: Float): Boolean {
        if (!isActive) return false

        lifetime -= deltaTime
        x += vx * deltaTime
        y += vy * deltaTime

        if (lifetime <= 0f) {
            isActive = false
            return true // Trigger impact
        }
        return false
    }

    /**
     * Normalized 0..1 flight progress.
     */
    val flightProgress: Float
        get() = (1f - (lifetime / maxLifetime)).coerceIn(0f, 1f)

    /**
     * Parabolic vertical height offset for 2.5D visual rendering.
     */
    val visualAltitude: Float
        get() {
            if (arcHeight <= 0f) return 0f
            val p = flightProgress
            return 4f * arcHeight * p * (1f - p)
        }
}

class ProjectileManager(private val maxProjectiles: Int = 150) {

    val pool = Array(maxProjectiles) { Projectile() }

    fun spawn(
        type: ProjectileType,
        fromX: Float,
        fromY: Float,
        targetX: Float,
        targetY: Float,
        speed: Float,
        damage: Float,
        radius: Float,
        explosionRadius: Float = 0f,
        pierce: Int = 1,
        isEnemy: Boolean = false
    ): Projectile? {
        for (p in pool) {
            if (!p.isActive) {
                p.reset(type, fromX, fromY, targetX, targetY, speed, damage, radius, explosionRadius, pierce, isEnemy)
                return p
            }
        }
        return null
    }

    fun update(deltaTime: Float, particleManager: ParticleManager, camera: GameCamera) {
        for (p in pool) {
            if (p.isActive) {
                // Smoke trail for cannonballs and ballista bolts
                if (p.type == ProjectileType.CANNONBALL) {
                    particleManager.spawn(
                        x = p.x,
                        y = p.y,
                        vx = -p.vx * 0.08f,
                        vy = -p.vy * 0.08f,
                        lifetime = 0.25f,
                        startSize = 3.5f,
                        endSize = 8.5f,
                        type = com.game.castlewar.effects.ParticleType.SMOKE,
                        r = 0.35f,
                        g = 0.35f,
                        b = 0.38f,
                        a = 0.45f
                    )
                }

                val arrivedAtTarget = p.update(deltaTime)
                if (arrivedAtTarget) {
                    when (p.type) {
                        ProjectileType.CANNONBALL -> {
                            particleManager.spawnCannonImpact(p.x, p.y, p.explosionRadius)
                            camera.triggerCannonImpactShake()
                        }
                        ProjectileType.BALLISTA_BOLT -> {
                            particleManager.spawnBallistaImpact(p.x, p.y)
                            camera.shake(intensity = 5f, duration = 0.15f)
                        }
                        ProjectileType.ARROW -> {
                            particleManager.spawnArrowImpact(p.x, p.y)
                        }
                        else -> {
                            particleManager.spawnHitSparks(p.x, p.y, count = 4)
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        for (p in pool) {
            p.isActive = false
        }
    }
}
