package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material

/**
 * Palette of curated, stylized matte materials matching Wanderburg's visual design.
 * Provides soft diffuse reflections, low specular glare, and bright, readable medieval colors.
 */
object ProceduralMaterial {

    val STONE = Material(
        name = "Stone",
        baseColorR = 0.50f, baseColorG = 0.52f, baseColorB = 0.55f,
        roughness = 0.88f, metallic = 0.05f
    )

    val LIGHT_STONE = Material(
        name = "LightStone",
        baseColorR = 0.72f, baseColorG = 0.75f, baseColorB = 0.77f,
        roughness = 0.82f, metallic = 0.05f
    )

    val DARK_STONE = Material(
        name = "DarkStone",
        baseColorR = 0.32f, baseColorG = 0.34f, baseColorB = 0.38f,
        roughness = 0.90f, metallic = 0.08f
    )

    val WOOD = Material(
        name = "Wood",
        baseColorR = 0.55f, baseColorG = 0.36f, baseColorB = 0.20f,
        roughness = 0.85f, metallic = 0.0f
    )

    val DARK_WOOD = Material(
        name = "DarkWood",
        baseColorR = 0.36f, baseColorG = 0.23f, baseColorB = 0.13f,
        roughness = 0.88f, metallic = 0.0f
    )

    val LIGHT_WOOD = Material(
        name = "LightWood",
        baseColorR = 0.73f, baseColorG = 0.54f, baseColorB = 0.35f,
        roughness = 0.80f, metallic = 0.0f
    )

    val IRON = Material(
        name = "Iron",
        baseColorR = 0.24f, baseColorG = 0.28f, baseColorB = 0.32f,
        roughness = 0.55f, metallic = 0.45f
    )

    val GOLD = Material(
        name = "Gold",
        baseColorR = 0.95f, baseColorG = 0.76f, baseColorB = 0.18f,
        roughness = 0.40f, metallic = 0.60f
    )

    val GRASS = Material(
        name = "Grass",
        baseColorR = 0.32f, baseColorG = 0.62f, baseColorB = 0.25f,
        roughness = 0.92f, metallic = 0.0f
    )

    val DIRT = Material(
        name = "Dirt",
        baseColorR = 0.58f, baseColorG = 0.44f, baseColorB = 0.29f,
        roughness = 0.95f, metallic = 0.0f
    )

    val RED_ROOF = Material(
        name = "RedRoof",
        baseColorR = 0.76f, baseColorG = 0.26f, baseColorB = 0.20f,
        roughness = 0.78f, metallic = 0.0f
    )

    val BLUE_ROOF = Material(
        name = "BlueRoof",
        baseColorR = 0.22f, baseColorG = 0.42f, baseColorB = 0.64f,
        roughness = 0.78f, metallic = 0.0f
    )

    val RED_CLOTH = Material(
        name = "RedCloth",
        baseColorR = 0.82f, baseColorG = 0.18f, baseColorB = 0.15f,
        roughness = 0.85f, metallic = 0.0f
    )

    val BLUE_CLOTH = Material(
        name = "BlueCloth",
        baseColorR = 0.18f, baseColorG = 0.45f, baseColorB = 0.78f,
        roughness = 0.85f, metallic = 0.0f
    )

    val MAGIC = Material(
        name = "Magic",
        baseColorR = 0.68f, baseColorG = 0.35f, baseColorB = 0.85f,
        roughness = 0.30f, metallic = 0.30f
    )

    val LEAVES_OAK = Material(
        name = "LeavesOak",
        baseColorR = 0.28f, baseColorG = 0.55f, baseColorB = 0.22f,
        roughness = 0.90f, metallic = 0.0f
    )

    val LEAVES_PINE = Material(
        name = "LeavesPine",
        baseColorR = 0.16f, baseColorG = 0.38f, baseColorB = 0.18f,
        roughness = 0.90f, metallic = 0.0f
    )

    val PLASTER = Material(
        name = "Plaster",
        baseColorR = 0.90f, baseColorG = 0.87f, baseColorB = 0.80f,
        roughness = 0.88f, metallic = 0.0f
    )

    val FLESH = Material(
        name = "Flesh",
        baseColorR = 0.92f, baseColorG = 0.76f, baseColorB = 0.64f,
        roughness = 0.80f, metallic = 0.0f
    )

    val BOMB_BLACK = Material(
        name = "BombBlack",
        baseColorR = 0.12f, baseColorG = 0.14f, baseColorB = 0.16f,
        roughness = 0.70f, metallic = 0.30f
    )

    val SHADOW = Material(
        name = "BlobShadow",
        baseColorR = 0.03f, baseColorG = 0.04f, baseColorB = 0.05f, baseColorA = 0.45f,
        roughness = 1.0f, metallic = 0.0f
    )

    val CRYSTAL = Material(
        name = "Crystal",
        baseColorR = 0.30f, baseColorG = 0.85f, baseColorB = 0.95f,
        roughness = 0.20f, metallic = 0.50f
    )

    val LEAVES_AUTUMN = Material(
        name = "LeavesAutumn",
        baseColorR = 0.92f, baseColorG = 0.65f, baseColorB = 0.18f,
        roughness = 0.90f, metallic = 0.0f
    )

    val HAY = Material(
        name = "Hay",
        baseColorR = 0.88f, baseColorG = 0.74f, baseColorB = 0.36f,
        roughness = 0.94f, metallic = 0.0f
    )

    val LANTERN_GLOW = Material(
        name = "LanternGlow",
        baseColorR = 1.0f, baseColorG = 0.85f, baseColorB = 0.35f,
        roughness = 0.10f, metallic = 0.0f
    )

    val WHITE_CLOTH = Material(
        name = "WhiteCloth",
        baseColorR = 0.92f, baseColorG = 0.90f, baseColorB = 0.84f,
        roughness = 0.85f, metallic = 0.0f
    )

    val COBBLESTONE = Material(
        name = "Cobblestone",
        baseColorR = 0.54f, baseColorG = 0.51f, baseColorB = 0.47f,
        roughness = 0.92f, metallic = 0.02f
    )

    val FIRE_EMBER = Material(
        name = "FireEmber",
        baseColorR = 0.98f, baseColorG = 0.42f, baseColorB = 0.12f,
        roughness = 0.20f, metallic = 0.0f
    )

    val WATER = Material(
        name = "StylizedWater",
        baseColorR = 0.18f, baseColorG = 0.60f, baseColorB = 0.72f, baseColorA = 0.88f,
        roughness = 0.18f, metallic = 0.08f
    )

    val SCORCHED_EARTH = Material(
        name = "ScorchedEarth",
        baseColorR = 0.34f, baseColorG = 0.29f, baseColorB = 0.25f,
        roughness = 0.96f, metallic = 0.0f
    )

    val WAR_BANNER_RED = Material(
        name = "WarBannerRed",
        baseColorR = 0.88f, baseColorG = 0.14f, baseColorB = 0.12f,
        roughness = 0.82f, metallic = 0.0f
    )

    val CROPS_WHEAT = Material(
        name = "CropsWheat",
        baseColorR = 0.86f, baseColorG = 0.74f, baseColorB = 0.26f,
        roughness = 0.92f, metallic = 0.0f
    )

    val CROPS_VEGGIE = Material(
        name = "CropsVeggie",
        baseColorR = 0.26f, baseColorG = 0.70f, baseColorB = 0.24f,
        roughness = 0.90f, metallic = 0.0f
    )

    val CHARRED_WOOD = Material(
        name = "CharredWood",
        baseColorR = 0.16f, baseColorG = 0.15f, baseColorB = 0.14f,
        roughness = 0.94f, metallic = 0.0f
    )
}
