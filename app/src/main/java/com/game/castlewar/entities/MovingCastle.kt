package com.game.castlewar.entities

import com.game.castlewar.resources.ResourceType
import com.game.castlewar.weapons.ArcherTowerWeapon
import com.game.castlewar.weapons.BallistaWeapon
import com.game.castlewar.weapons.CannonWeapon
import com.game.castlewar.weapons.Weapon
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The player's colossal mobile fortress.
 * Features heavy tracked locomotion, upgradable battlements, and automated siege modules.
 */
class MovingCastle(
    var positionX: Float = 0f,
    var positionY: Float = 0f
) {
    // Movement and Physics
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var moveSpeed: Float = 110f
    var rotation: Float = 0f // In radians
    var targetRotation: Float = 0f
    var rotationSpeed: Float = 3.5f // Radians per second
    var treadAnimationTimer: Float = 0f
    var wheelRotationDeg: Float = 0f

    // Fortress Dimensions & Hitbox
    val width: Float = 110f
    val height: Float = 130f
    val collisionRadius: Float = 60f

    // Combat Stats
    var maximumHealth: Float = 1000f
    var currentHealth: Float = 1000f
    var armor: Float = 15f
    var healthRegenPerSec: Float = 2.0f
    var magnetRange: Float = 220f

    // Progression
    var level: Int = 1
    var experience: Int = 0
    var experienceRequired: Int = 100

    /**
     * Visual and structural tier of the fortress (1 to 5).
     * Level 1-2 = Tier 1 (Wood & Stone Chassis)
     * Level 3-4 = Tier 2 (Stone Bastions & Iron Bumpers)
     * Level 5-6 = Tier 3 (Upper Half-timbered Keep & Conical Blue Towers)
     * Level 7-8 = Tier 4 (Armored Skirting & Heavy Spiked Iron Ram)
     * Level 9+ = Tier 5 (Colossal War Citadel with Glowing Arcane Spire)
     */
    val tier: Int
        get() = ((level - 1) / 2 + 1).coerceIn(1, 5)

    val healthRatio: Float
        get() = if (maximumHealth > 0f) (currentHealth / maximumHealth).coerceIn(0f, 1f) else 0f

    val isDamaged: Boolean
        get() = healthRatio < 0.60f

    val isSeverelyDamaged: Boolean
        get() = healthRatio < 0.30f

    // Resources Stored
    val resources = mutableMapOf<ResourceType, Int>().apply {
        ResourceType.values().forEach { put(it, 0) }
    }

    // Modular Slots
    val slots = listOf(
        CastleSlot(id = 0, offsetX = 0f, offsetY = 0f, moduleType = CastleModuleType.CORE, occupied = true),
        CastleSlot(id = 1, offsetX = -42f, offsetY = -35f, moduleType = CastleModuleType.CANNON_TOWER, occupied = true),
        CastleSlot(id = 2, offsetX = 42f, offsetY = -35f, moduleType = CastleModuleType.ARCHER_TOWER, occupied = true),
        CastleSlot(id = 3, offsetX = 0f, offsetY = 45f, moduleType = CastleModuleType.BALLISTA, occupied = true),
        CastleSlot(id = 4, offsetX = -42f, offsetY = 35f, moduleType = CastleModuleType.STONE_WALL, occupied = true),
        CastleSlot(id = 5, offsetX = 42f, offsetY = 35f, moduleType = CastleModuleType.STONE_WALL, occupied = true)
    )

    // Weapons mounted on slots
    val weapons = mutableListOf<Weapon>().apply {
        add(CannonWeapon().apply { slotOffsetX = -42f; slotOffsetY = -35f })
        add(ArcherTowerWeapon().apply { slotOffsetX = 42f; slotOffsetY = -35f })
        add(BallistaWeapon().apply { slotOffsetX = 0f; slotOffsetY = 45f })
    }

    val isAlive: Boolean
        get() = currentHealth > 0f

    fun reset(startX: Float = 0f, startY: Float = 0f) {
        positionX = startX
        positionY = startY
        velocityX = 0f
        velocityY = 0f
        rotation = 0f
        targetRotation = 0f
        maximumHealth = 1000f
        currentHealth = 1000f
        armor = 15f
        moveSpeed = 110f
        magnetRange = 220f
        level = 1
        experience = 0
        experienceRequired = 100

        ResourceType.values().forEach { resources[it] = 0 }

        weapons.clear()
        weapons.add(CannonWeapon().apply { slotOffsetX = -42f; slotOffsetY = -35f })
        weapons.add(ArcherTowerWeapon().apply { slotOffsetX = 42f; slotOffsetY = -35f })
        weapons.add(BallistaWeapon().apply { slotOffsetX = 0f; slotOffsetY = 45f })
    }

    /**
     * Heavy, responsive steering from the virtual joystick.
     */
    fun steer(inputX: Float, inputY: Float, deltaTime: Float) {
        val inputMagnitude = sqrt(inputX * inputX + inputY * inputY)

        if (inputMagnitude > 0.08f) {
            val normX = inputX / inputMagnitude
            val normY = inputY / inputMagnitude

            // Target direction
            targetRotation = atan2(normY, normX)

            // Smooth angular turning
            var diff = targetRotation - rotation
            while (diff < -Math.PI.toFloat()) diff += 2f * Math.PI.toFloat()
            while (diff > Math.PI.toFloat()) diff -= 2f * Math.PI.toFloat()
            rotation += diff * (rotationSpeed * deltaTime).coerceIn(0f, 1f)

            // Accelerate fortress in the direction it faces
            val targetVx = normX * moveSpeed * inputMagnitude.coerceAtMost(1f)
            val targetVy = normY * moveSpeed * inputMagnitude.coerceAtMost(1f)

            // Heavy inertia smoothing
            val accel = 6.0f * deltaTime
            velocityX += (targetVx - velocityX) * accel.coerceIn(0f, 1f)
            velocityY += (targetVy - velocityY) * accel.coerceIn(0f, 1f)

            treadAnimationTimer += deltaTime * (moveSpeed * 0.1f)
        } else {
            // Decelerate with friction
            val friction = 0.88f
            velocityX *= friction
            velocityY *= friction
        }

        positionX += velocityX * deltaTime
        positionY += velocityY * deltaTime

        // Rotate castle wheels according to ground velocity
        val currentSpeed = sqrt(velocityX * velocityX + velocityY * velocityY)
        if (currentSpeed > 1f) {
            wheelRotationDeg = (wheelRotationDeg + currentSpeed * deltaTime * 14f) % 360f
        }

        // Passive health regeneration
        if (currentHealth < maximumHealth && isAlive) {
            currentHealth = (currentHealth + healthRegenPerSec * deltaTime).coerceAtMost(maximumHealth)
        }
    }

    /**
     * Applies damage reduced by armor formula: Damage = Raw * (100 / (100 + Armor)).
     */
    fun takeDamage(amount: Float): Boolean {
        if (!isAlive) return false

        val damageReduction = 100f / (100f + armor)
        val finalDamage = max(1f, amount * damageReduction)
        currentHealth -= finalDamage

        if (currentHealth <= 0f) {
            currentHealth = 0f
            return true // Destroyed
        }
        return false
    }

    fun repair(amount: Float) {
        currentHealth = (currentHealth + amount).coerceAtMost(maximumHealth)
    }

    /**
     * Awards experience and checks for level up.
     * Returns true if leveled up.
     */
    fun addExperience(amount: Int): Boolean {
        experience += amount
        if (experience >= experienceRequired) {
            experience -= experienceRequired
            level++
            // Increase requirement for next level by 35%
            experienceRequired = (experienceRequired * 1.35f).toInt()
            return true
        }
        return false
    }

    fun addResource(type: ResourceType, amount: Int) {
        resources[type] = (resources[type] ?: 0) + amount
    }

    fun getResource(type: ResourceType): Int = resources[type] ?: 0
}
