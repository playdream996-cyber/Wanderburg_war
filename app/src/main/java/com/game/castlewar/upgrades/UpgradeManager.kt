package com.game.castlewar.upgrades

import com.game.castlewar.entities.MovingCastle
import com.game.castlewar.weapons.ArcherTowerWeapon
import com.game.castlewar.weapons.BallistaWeapon
import com.game.castlewar.weapons.CannonWeapon

enum class UpgradeRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF9E9E9E),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFFF9800)
}

data class UpgradeCard(
    val id: String,
    val title: String,
    val description: String,
    val rarity: UpgradeRarity,
    val iconSymbol: String,
    val apply: (MovingCastle) -> Unit
)

class UpgradeManager {

    val allUpgrades = listOf(
        UpgradeCard(
            id = "bombard_power",
            title = "Heavy Powder",
            description = "+25% Cannon Damage and +20% Blast Radius",
            rarity = UpgradeRarity.COMMON,
            iconSymbol = "💣",
            apply = { castle ->
                castle.weapons.filterIsInstance<CannonWeapon>().forEach { it.upgrade() }
            }
        ),
        UpgradeCard(
            id = "archer_volley",
            title = "Elven Composite Bows",
            description = "+30% Archer Fire Rate & Volley Count",
            rarity = UpgradeRarity.RARE,
            iconSymbol = "🏹",
            apply = { castle ->
                castle.weapons.filterIsInstance<ArcherTowerWeapon>().forEach { it.upgrade() }
            }
        ),
        UpgradeCard(
            id = "ballista_pierce",
            title = "Steel-Tipped Harpoons",
            description = "+40% Ballista Damage and +1 Pierce",
            rarity = UpgradeRarity.RARE,
            iconSymbol = "⚡",
            apply = { castle ->
                castle.weapons.filterIsInstance<BallistaWeapon>().forEach { it.upgrade() }
            }
        ),
        UpgradeCard(
            id = "reinforced_hull",
            title = "Ironclad Bulwark",
            description = "+250 Max HP and +10 Armor",
            rarity = UpgradeRarity.COMMON,
            iconSymbol = "🛡️",
            apply = { castle ->
                castle.maximumHealth += 250f
                castle.currentHealth += 250f
                castle.armor += 10f
            }
        ),
        UpgradeCard(
            id = "steam_treads",
            title = "Clockwork Gears",
            description = "+25% Fortress Speed and Turn Rate",
            rarity = UpgradeRarity.COMMON,
            iconSymbol = "⚙️",
            apply = { castle ->
                castle.moveSpeed *= 1.25f
                castle.rotationSpeed *= 1.25f
            }
        ),
        UpgradeCard(
            id = "magnetic_bastion",
            title = "Lodestone Magnet",
            description = "+80% Resource Magnet Range",
            rarity = UpgradeRarity.COMMON,
            iconSymbol = "🧲",
            apply = { castle ->
                castle.magnetRange *= 1.8f
            }
        ),
        UpgradeCard(
            id = "field_repair",
            title = "Masonry Restoration",
            description = "Instantly restore 400 Fortress HP",
            rarity = UpgradeRarity.COMMON,
            iconSymbol = "❤️",
            apply = { castle ->
                castle.repair(400f)
            }
        ),
        UpgradeCard(
            id = "double_bombard",
            title = "Twin Bombards",
            description = "Cannons fire 2 simultaneous explosive projectiles",
            rarity = UpgradeRarity.EPIC,
            iconSymbol = "💥",
            apply = { castle ->
                castle.weapons.filterIsInstance<CannonWeapon>().forEach {
                    it.projectilesPerShot = 2
                    it.damage *= 1.2f
                }
            }
        ),
        UpgradeCard(
            id = "titan_citadel",
            title = "Walking Citadel",
            description = "+500 Max HP, +20 Armor, and +50% Crushing Ram Damage",
            rarity = UpgradeRarity.LEGENDARY,
            iconSymbol = "👑",
            apply = { castle ->
                castle.maximumHealth += 500f
                castle.currentHealth += 500f
                castle.armor += 20f
                castle.moveSpeed *= 1.15f
            }
        )
    )

    /**
     * Generates 3 random non-duplicate upgrade choices.
     */
    fun getRandomChoices(count: Int = 3): List<UpgradeCard> {
        return allUpgrades.shuffled().take(count)
    }
}
