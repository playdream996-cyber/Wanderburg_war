package com.game.castlewar.buildings

import com.game.castlewar.effects.ParticleManager
import com.game.castlewar.render.GameCamera
import com.game.castlewar.resources.ResourcePickup
import com.game.castlewar.resources.ResourceType

enum class BuildingType(
    val displayName: String,
    val maxHp: Float,
    val width: Float,
    val height: Float,
    val rewardWood: Int,
    val rewardStone: Int,
    val rewardGold: Int,
    val rewardXP: Int
) {
    SUPPLY_TENT("Supply Tent", 60f, 44f, 40f, 15, 0, 5, 20),
    VILLAGE_HOUSE("Thatch Cottage", 120f, 60f, 54f, 30, 10, 12, 35),
    TOWNHOUSE("Medieval Townhouse", 170f, 66f, 60f, 35, 25, 20, 45),
    FARM_BARN("Hay Barn", 150f, 75f, 55f, 40, 10, 10, 40),
    WINDMILL("Old Windmill", 200f, 65f, 65f, 35, 30, 25, 55),
    MARKET_STALL("Market Stall", 80f, 46f, 42f, 20, 5, 30, 30),
    VILLAGE_WELL("Village Well", 140f, 44f, 44f, 10, 25, 15, 25),
    BARRACKS("Guard Garrison", 260f, 85f, 75f, 45, 25, 25, 60),
    WATCHTOWER("Timber Watchtower", 220f, 48f, 70f, 25, 35, 18, 50),
    WALL_SEGMENT("Palisade Wall", 180f, 64f, 32f, 35, 15, 5, 15),
    STONE_RUIN("Ancient Ruin", 160f, 60f, 40f, 5, 45, 15, 30),
    BLACKSMITH("Village Forge", 240f, 65f, 60f, 30, 40, 35, 60),
    CATAPULT("Siege Catapult", 300f, 60f, 70f, 50, 20, 40, 80),
    BALLISTA("Siege Ballista", 200f, 50f, 60f, 35, 15, 25, 50),
    BARRICADE("Wooden Barricade", 150f, 55f, 25f, 30, 5, 5, 15),
    CROP_FIELD("Farmland Crop", 60f, 70f, 70f, 30, 0, 15, 20),
    FARM_SHED("Tool Shed", 100f, 50f, 45f, 35, 10, 10, 25),
    ELDER_OAK("Great Elder Oak", 1000f, 80f, 80f, 100, 0, 0, 50),
    STONE_BRIDGE("River Arch Bridge", 2000f, 120f, 100f, 0, 150, 50, 100),
    BURNED_CART("Burned Cart", 80f, 40f, 40f, 20, 5, 10, 20)
}

class Building(
    val id: Int,
    var positionX: Float,
    var positionY: Float,
    val type: BuildingType
) {
    var maxHealth: Float = type.maxHp
    var currentHealth: Float = type.maxHp
    var isDestroyed: Boolean = false

    fun takeDamage(
        amount: Float,
        particleManager: ParticleManager,
        camera: GameCamera,
        onDropResource: (x: Float, y: Float, type: ResourceType, amount: Int) -> Unit
    ): Boolean {
        if (isDestroyed) return false

        currentHealth -= amount
        particleManager.spawnHitSparks(positionX, positionY, count = 3)

        if (currentHealth <= 0f) {
            currentHealth = 0f
            isDestroyed = true

            // Destruction burst
            particleManager.spawnExplosion(positionX, positionY, radius = 40f)
            camera.shake(intensity = 10f, duration = 0.25f)

            // Spawn resource drops
            if (type.rewardWood > 0) onDropResource(positionX - 15f, positionY, ResourceType.WOOD, type.rewardWood)
            if (type.rewardStone > 0) onDropResource(positionX + 15f, positionY, ResourceType.STONE, type.rewardStone)
            if (type.rewardGold > 0) onDropResource(positionX, positionY - 15f, ResourceType.GOLD, type.rewardGold)
            if (type.rewardXP > 0) onDropResource(positionX, positionY + 15f, ResourceType.EXPERIENCE, type.rewardXP)

            return true
        }
        return false
    }
}
