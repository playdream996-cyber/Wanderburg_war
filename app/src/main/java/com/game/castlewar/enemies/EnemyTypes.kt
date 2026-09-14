package com.game.castlewar.enemies

/**
 * Basic medieval foot soldier with sword and shield.
 */
class EnemyMelee(x: Float, y: Float) : Enemy(
    positionX = x,
    positionY = y,
    health = 45f,
    maxHealth = 45f,
    moveSpeed = 70f,
    damage = 8f,
    attackRange = 40f,
    attackCooldown = 1.0f,
    rewardXP = 15,
    rewardGold = 2,
    radius = 16f
)

/**
 * Medieval crossbowman / archer that fires arrows from a distance.
 */
class EnemyArcher(x: Float, y: Float) : Enemy(
    positionX = x,
    positionY = y,
    health = 35f,
    maxHealth = 35f,
    moveSpeed = 55f,
    damage = 10f,
    attackRange = 190f,
    attackCooldown = 1.8f,
    rewardXP = 20,
    rewardGold = 3,
    radius = 14f
) {
    override fun onAttackTriggered(
        targetX: Float,
        targetY: Float,
        onFireProjectile: ((type: com.game.castlewar.weapons.ProjectileType, fromX: Float, fromY: Float, toX: Float, toY: Float, speed: Float, dmg: Float, radius: Float, expRadius: Float) -> Unit)?
    ): Float {
        onFireProjectile?.invoke(
            com.game.castlewar.weapons.ProjectileType.ARROW,
            positionX,
            positionY,
            targetX,
            targetY,
            460f,
            damage,
            5f,
            0f
        )
        return 0f
    }
}

/**
 * Heavily armored knight with greatshield and plate armor.
 */
class EnemyKnight(x: Float, y: Float) : Enemy(
    positionX = x,
    positionY = y,
    health = 130f,
    maxHealth = 130f,
    moveSpeed = 45f,
    damage = 22f,
    attackRange = 45f,
    attackCooldown = 1.4f,
    rewardXP = 40,
    rewardGold = 8,
    radius = 20f
)

/**
 * Suicide sapper carrying a powder keg barrel, charging to detonate on fortress walls.
 */
class EnemyBomber(x: Float, y: Float) : Enemy(
    positionX = x,
    positionY = y,
    health = 25f,
    maxHealth = 25f,
    moveSpeed = 110f,
    damage = 45f,
    attackRange = 45f,
    attackCooldown = 0.1f,
    rewardXP = 25,
    rewardGold = 5,
    radius = 15f
)
