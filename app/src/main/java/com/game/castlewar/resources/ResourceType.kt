package com.game.castlewar.resources

/**
 * Medieval resources collected by the fortress for repairs, upgrades, and construction.
 */
enum class ResourceType(val displayName: String, val colorHex: Long) {
    GOLD("Gold", 0xFFFFD700),
    WOOD("Wood", 0xFF8B5A2B),
    STONE("Stone", 0xFF9E9E9E),
    IRON("Iron", 0xFFB0C4DE),
    MAGIC_CRYSTAL("Crystal", 0xFF9932CC),
    EXPERIENCE("XP", 0xFF00E5FF)
}
