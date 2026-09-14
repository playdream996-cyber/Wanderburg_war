package com.game.castlewar.model3d

/**
 * Stylized matte medieval fantasy material.
 * Supports base color tinting, diffuse shading, specular highlights, and optional diffuse texture.
 */
data class Material(
    val name: String = "default_mat",
    var baseColorR: Float = 0.8f,
    var baseColorG: Float = 0.8f,
    var baseColorB: Float = 0.8f,
    var baseColorA: Float = 1.0f,
    var roughness: Float = 0.65f,
    var metallic: Float = 0.1f,
    var texture: Texture? = null,
    var doubleSided: Boolean = false
) {
    companion object {
        val STONE = Material(name = "Stone", baseColorR = 0.52f, baseColorG = 0.53f, baseColorB = 0.55f, roughness = 0.8f)
        val WOOD = Material(name = "Wood", baseColorR = 0.50f, baseColorG = 0.32f, baseColorB = 0.16f, roughness = 0.7f)
        val IRON = Material(name = "Iron", baseColorR = 0.30f, baseColorG = 0.32f, baseColorB = 0.35f, roughness = 0.4f, metallic = 0.7f)
        val GOLD = Material(name = "Gold", baseColorR = 1.0f, baseColorG = 0.82f, baseColorB = 0.15f, roughness = 0.3f, metallic = 0.8f)
        val ROOF_RED = Material(name = "RoofRed", baseColorR = 0.75f, baseColorG = 0.22f, baseColorB = 0.18f, roughness = 0.6f)
        val LEAF_GREEN = Material(name = "LeafGreen", baseColorR = 0.28f, baseColorG = 0.58f, baseColorB = 0.22f, roughness = 0.85f)
        val TRUNK_BROWN = Material(name = "TrunkBrown", baseColorR = 0.42f, baseColorG = 0.26f, baseColorB = 0.14f, roughness = 0.8f)
        val ENEMY_ARMOR = Material(name = "EnemyArmor", baseColorR = 0.65f, baseColorG = 0.20f, baseColorB = 0.20f, roughness = 0.5f)
    }
}
