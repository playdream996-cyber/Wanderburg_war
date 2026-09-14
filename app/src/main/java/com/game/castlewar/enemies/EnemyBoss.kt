package com.game.castlewar.enemies

import com.game.castlewar.weapons.ProjectileType
import kotlin.random.Random

/**
 * Colossal enemy fortress / siege titan boss with heavy armor and brutal siege weaponry.
 */
class EnemyBoss(x: Float, y: Float) : Enemy(
    positionX = x,
    positionY = y,
    health = 3200f,
    maxHealth = 3200f,
    moveSpeed = 35f,
    damage = 50f,
    attackRange = 360f,
    attackCooldown = 3.0f,
    rewardXP = 600,
    rewardGold = 300,
    radius = 55f
) {
    var summonPhase1Done: Boolean = false
    var summonPhase2Done: Boolean = false

    override fun onAttackTriggered(
        targetX: Float,
        targetY: Float,
        onFireProjectile: ((type: ProjectileType, fromX: Float, fromY: Float, toX: Float, toY: Float, speed: Float, dmg: Float, radius: Float, expRadius: Float) -> Unit)?
    ): Float {
        // Fire 2 devastating siege fireballs
        for (i in 0 until 2) {
            val spreadX = (Random.nextFloat() - 0.5f) * 45f
            val spreadY = (Random.nextFloat() - 0.5f) * 45f
            onFireProjectile?.invoke(
                ProjectileType.FIREBALL,
                positionX,
                positionY,
                targetX + spreadX,
                targetY + spreadY,
                320f,
                damage,
                14f,
                55f
            )
        }
        return 0f
    }
}
