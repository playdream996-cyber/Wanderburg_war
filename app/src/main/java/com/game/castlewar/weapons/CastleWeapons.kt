package com.game.castlewar.weapons

import com.game.castlewar.effects.ParticleManager
import com.game.castlewar.enemies.Enemy
import com.game.castlewar.render.GameCamera
import kotlin.random.Random

/**
 * Bombard Cannon: Slow firing, high damage, arcing ballistic projectile, explosive AoE.
 */
class CannonWeapon : Weapon(
    id = "cannon",
    name = "Heavy Bombard",
    damage = 50f,
    range = 320f,
    attackSpeed = 0.75f,
    projectileSpeed = 380f,
    criticalChance = 0.10f
) {
    var explosionRadius: Float = 60f
    var projectilesPerShot: Int = 1

    override fun attack(
        originX: Float,
        originY: Float,
        target: Enemy,
        projectileManager: ProjectileManager,
        particleManager: ParticleManager,
        camera: GameCamera
    ) {
        val dirAngle = kotlin.math.atan2(target.positionY - originY, target.positionX - originX)
        // Fiery explosive cannon muzzle flash with heavy smoke plume
        particleManager.spawnCannonMuzzleFlash(originX, originY, dirAngle)
        camera.triggerCannonRecoilShake()

        for (i in 0 until projectilesPerShot) {
            val spreadX = if (projectilesPerShot > 1) (Random.nextFloat() - 0.5f) * 30f else 0f
            val spreadY = if (projectilesPerShot > 1) (Random.nextFloat() - 0.5f) * 30f else 0f

            projectileManager.spawn(
                type = ProjectileType.CANNONBALL,
                fromX = originX,
                fromY = originY,
                targetX = target.positionX + spreadX,
                targetY = target.positionY + spreadY,
                speed = projectileSpeed,
                damage = damage,
                radius = 9f,
                explosionRadius = explosionRadius,
                pierce = 1
            )
        }
    }

    override fun upgrade() {
        level++
        when (level) {
            2 -> {
                damage *= 1.25f // +25% damage
            }
            3 -> {
                explosionRadius += 25f // larger explosion
                damage += 15f
            }
            4 -> {
                projectilesPerShot = 2 // Double projectile volley!
                attackSpeed *= 1.15f
            }
            else -> {
                damage *= 1.20f
                explosionRadius += 10f
            }
        }
    }
}

/**
 * Archer Tower: Fast firing, low-medium damage, high precision.
 */
class ArcherTowerWeapon : Weapon(
    id = "archer_tower",
    name = "Garrison Archers",
    damage = 18f,
    range = 280f,
    attackSpeed = 2.2f,
    projectileSpeed = 520f,
    criticalChance = 0.15f
) {
    var arrowsPerVolley: Int = 1
    var pierceCount: Int = 1

    override fun attack(
        originX: Float,
        originY: Float,
        target: Enemy,
        projectileManager: ProjectileManager,
        particleManager: ParticleManager,
        camera: GameCamera
    ) {
        for (i in 0 until arrowsPerVolley) {
            val spreadX = if (arrowsPerVolley > 1) (Random.nextFloat() - 0.5f) * 20f else 0f
            val spreadY = if (arrowsPerVolley > 1) (Random.nextFloat() - 0.5f) * 20f else 0f

            projectileManager.spawn(
                type = ProjectileType.ARROW,
                fromX = originX,
                fromY = originY,
                targetX = target.positionX + spreadX,
                targetY = target.positionY + spreadY,
                speed = projectileSpeed,
                damage = damage,
                radius = 5f,
                explosionRadius = 0f,
                pierce = pierceCount
            )
        }
    }

    override fun upgrade() {
        level++
        when (level) {
            2 -> {
                attackSpeed *= 1.3f
                damage += 5f
            }
            3 -> {
                arrowsPerVolley = 2
            }
            4 -> {
                pierceCount = 2 // Piercing arrows
                damage += 8f
            }
            else -> {
                arrowsPerVolley++
                damage *= 1.15f
            }
        }
    }
}

/**
 * Ballista: Long range, heavy sniper bolt, high single-target armor punch.
 */
class BallistaWeapon : Weapon(
    id = "ballista",
    name = "Iron Ballista",
    damage = 85f,
    range = 420f,
    attackSpeed = 0.85f,
    projectileSpeed = 650f,
    criticalChance = 0.25f
) {
    var pierceCount: Int = 2

    override fun attack(
        originX: Float,
        originY: Float,
        target: Enemy,
        projectileManager: ProjectileManager,
        particleManager: ParticleManager,
        camera: GameCamera
    ) {
        particleManager.spawnHitSparks(originX, originY, count = 5)
        camera.shake(intensity = 3.5f, duration = 0.12f)

        projectileManager.spawn(
            type = ProjectileType.BALLISTA_BOLT,
            fromX = originX,
            fromY = originY,
            targetX = target.positionX,
            targetY = target.positionY,
            speed = projectileSpeed,
            damage = damage,
            radius = 7f,
            explosionRadius = 0f,
            pierce = pierceCount
        )
    }

    override fun upgrade() {
        level++
        damage *= 1.35f
        range += 40f
        pierceCount++
    }
}
