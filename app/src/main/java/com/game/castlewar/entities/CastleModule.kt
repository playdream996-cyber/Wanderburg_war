package com.game.castlewar.entities

/**
 * Module types that can be attached to the moving castle's sockets.
 */
enum class CastleModuleType(val displayName: String, val description: String) {
    CORE("Castle Keep", "The central fortress keep with command battlements"),
    STONE_WALL("Reinforced Wall", "Heavy stone bulwark absorbing enemy attacks"),
    ARCHER_TOWER("Archer Tower", "Rapid-fire garrison firing piercing volleys"),
    CANNON_TOWER("Bombard Cannon", "Devastating siege cannon dealing area blast damage"),
    BALLISTA("Heavy Ballista", "Sniper siege bolt launcher with extreme range"),
    CATAPULT("Catapult", "Launches heavy boulders over enemy fortifications"),
    MAGIC_TOWER("Arcane Spire", "Unleashes mystical elemental bolts"),
    ARMOR_MODULE("Iron Plating", "Massive iron sheets bolstering total damage resistance"),
    RESOURCE_STORAGE("Vault Bastion", "Expands treasury capacity and increases resource magnet pull")
}

/**
 * An individual slot located on the moving fortress hull.
 */
data class CastleSlot(
    val id: Int,
    val offsetX: Float,
    val offsetY: Float,
    var rotation: Float = 0f,
    var occupied: Boolean = false,
    var moduleType: CastleModuleType? = null,
    var moduleLevel: Int = 1
)

/**
 * An attached castle module instance.
 */
class CastleModule(
    val slot: CastleSlot,
    val type: CastleModuleType,
    var level: Int = 1,
    var currentHealth: Float = 200f,
    var maxHealth: Float = 200f
)
