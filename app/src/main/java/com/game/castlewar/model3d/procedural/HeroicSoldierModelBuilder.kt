package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model

/**
 * Chunky, high-readability fantasy units for mobile isometric combat.
 * Large helmets, broad torsos, saturated class colors and oversized weapons
 * prevent soldiers from visually collapsing into tiny ants at gameplay distance.
 */
object HeroicSoldierModelBuilder {

    fun build(unitClass: Int): Model {
        val name = when (unitClass) {
            1 -> "heroic_archer"
            2 -> "heroic_knight"
            3 -> "heroic_bomber"
            4 -> "heroic_mage"
            5 -> "heroic_engineer"
            6 -> "heroic_villager"
            else -> "heroic_swordsman"
        }
        val b = ProceduralModelBuilder(name)

        // Oversized boots/legs create a grounded silhouette.
        for (sx in listOf(-1.55f, 1.55f)) {
            b.addBox(1.8f, 4.2f, 1.9f, ProceduralMaterial.DARK_WOOD, tx = sx, ty = 2.7f)
            b.addBox(2.3f, 1.7f, 3.0f, ProceduralMaterial.IRON, tx = sx, ty = 0.9f, tz = 0.5f)
        }

        val torsoMat = when (unitClass) {
            1 -> ProceduralMaterial.LEAVES_PINE
            2 -> ProceduralMaterial.IRON
            3 -> ProceduralMaterial.DIRT
            4 -> ProceduralMaterial.MAGIC
            5 -> ProceduralMaterial.BLUE_CLOTH
            6 -> ProceduralMaterial.LIGHT_STONE
            else -> ProceduralMaterial.BLUE_CLOTH
        }

        // Broad torso + waist color block.
        b.addBox(if (unitClass == 2) 5.8f else 5.1f, 5.7f, 3.5f, torsoMat, ty = 7.2f)
        b.addBox(5.3f, 1.0f, 3.7f, if (unitClass == 2) ProceduralMaterial.GOLD else ProceduralMaterial.DARK_WOOD, ty = 4.7f)

        if (unitClass == 2) {
            b.addBox(6.7f, 1.8f, 4f, ProceduralMaterial.GOLD, ty = 9.3f)
            b.addBox(5.2f, 1.2f, 3.8f, ProceduralMaterial.RED_CLOTH, ty = 5.1f)
        } else if (unitClass == 4) {
            b.addCone(4.4f, 8f, ProceduralMaterial.MAGIC, segments = 10, ty = 4.8f)
        }

        // Large head so characters remain expressive/readable.
        val headY = 11.7f
        b.addSphere(if (unitClass == 2) 2.5f else 2.35f, ProceduralMaterial.FLESH, ty = headY)

        when (unitClass) {
            1 -> {
                b.addCone(2.7f, 2.6f, ProceduralMaterial.LEAVES_PINE, segments = 8, ty = 13.4f, rz = -12f)
                b.addBox(0.5f, 4.0f, 0.8f, ProceduralMaterial.RED_CLOTH, tx = 1.2f, ty = 15.0f)
            }
            2 -> {
                b.addCylinder(2.7f, 3.8f, ProceduralMaterial.IRON, segments = 10, ty = 13.1f)
                b.addBox(2.8f, 0.7f, 2.9f, ProceduralMaterial.GOLD, ty = 12.7f, tz = 1.2f)
                b.addBox(0.7f, 4.2f, 3.0f, ProceduralMaterial.RED_CLOTH, ty = 16.1f)
            }
            3 -> {
                b.addSphere(2.7f, ProceduralMaterial.DARK_WOOD, ty = 12.1f)
                b.addBox(3.2f, 1.1f, 3f, ProceduralMaterial.DARK_WOOD, ty = 10.8f)
            }
            4 -> {
                b.addCylinder(4.1f, 0.55f, ProceduralMaterial.MAGIC, segments = 10, ty = 12.7f)
                b.addCone(2.8f, 5.5f, ProceduralMaterial.MAGIC, segments = 10, ty = 15.1f, rz = -10f)
            }
            6 -> {
                b.addCylinder(3.7f, 0.5f, ProceduralMaterial.HAY, segments = 10, ty = 12.7f)
                b.addCone(2.5f, 1.7f, ProceduralMaterial.HAY, segments = 10, ty = 13.6f)
            }
            else -> {
                b.addCone(2.8f, 3.0f, ProceduralMaterial.IRON, segments = 8, ty = 13.5f)
                b.addCylinder(2.85f, 0.6f, ProceduralMaterial.GOLD, segments = 8, ty = 12.3f)
            }
        }

        // Arms are deliberately thick.
        val armMat = if (unitClass == 2) ProceduralMaterial.IRON else ProceduralMaterial.FLESH
        b.addBox(1.55f, 4.8f, 1.55f, armMat, tx = -3.2f, ty = 7.1f)
        b.addBox(1.55f, 4.8f, 1.55f, armMat, tx = 3.2f, ty = 7.1f)

        when (unitClass) {
            0 -> {
                // Broad sword + bright shield.
                b.addBox(0.75f, 10.0f, 1.3f, ProceduralMaterial.IRON, tx = 3.7f, ty = 8.4f, tz = 2.3f, rx = 38f)
                b.addBox(3.3f, 0.65f, 0.7f, ProceduralMaterial.GOLD, tx = 3.7f, ty = 5.5f, tz = 0.4f)
                b.addBox(1.0f, 8.2f, 5.8f, ProceduralMaterial.RED_CLOTH, tx = -4.2f, ty = 7.1f, tz = 1.2f)
                b.addBox(1.15f, 8.4f, 1.0f, ProceduralMaterial.GOLD, tx = -4.2f, ty = 7.1f, tz = 1.2f)
                b.addBox(1.15f, 1.0f, 5.8f, ProceduralMaterial.GOLD, tx = -4.2f, ty = 7.1f, tz = 1.2f)
            }
            1 -> {
                // Giant readable bow and quiver.
                b.addCylinder(0.42f, 10.8f, ProceduralMaterial.DARK_WOOD, tx = -3.8f, ty = 7.8f, tz = 2.0f, rx = 22f)
                b.addCylinder(0.14f, 10.3f, ProceduralMaterial.WHITE_CLOTH, tx = -3.8f, ty = 7.8f, tz = 1.3f, rx = 22f)
                b.addCylinder(1.35f, 6.3f, ProceduralMaterial.DARK_WOOD, tx = 0.6f, ty = 8.4f, tz = -2.3f, rx = -25f)
                b.addBox(1.2f, 2.5f, 1.2f, ProceduralMaterial.RED_CLOTH, tx = 0.6f, ty = 11.0f, tz = -2.8f)
            }
            2 -> {
                // Massive knight shield and great sword.
                b.addBox(0.9f, 12.0f, 1.5f, ProceduralMaterial.IRON, tx = 4.1f, ty = 9.1f, tz = 2.1f, rx = 30f)
                b.addBox(4.0f, 0.8f, 0.8f, ProceduralMaterial.GOLD, tx = 4.1f, ty = 5.7f, tz = 0.4f)
                b.addBox(1.2f, 10.5f, 6.6f, ProceduralMaterial.IRON, tx = -4.8f, ty = 7.3f, tz = 1.2f)
                b.addBox(1.35f, 10.8f, 1.2f, ProceduralMaterial.GOLD, tx = -4.8f, ty = 7.3f, tz = 1.2f)
            }
            3 -> {
                b.addSphere(4.2f, ProceduralMaterial.BOMB_BLACK, ty = 7.0f, tz = 4.8f)
                b.addCylinder(1.0f, 2.0f, ProceduralMaterial.IRON, ty = 10.7f, tz = 4.8f)
                b.addCylinder(0.3f, 2.6f, ProceduralMaterial.GOLD, ty = 12.4f, tz = 4.8f, rz = 25f)
                b.addSphere(0.8f, ProceduralMaterial.FIRE_EMBER, ty = 13.7f, tz = 4.8f)
            }
            4 -> {
                b.addCylinder(0.55f, 15.5f, ProceduralMaterial.DARK_WOOD, tx = 4.0f, ty = 9.0f, tz = 1.4f)
                b.addSphere(2.2f, ProceduralMaterial.CRYSTAL, tx = 4.0f, ty = 17.0f, tz = 1.4f)
                b.addSphere(0.9f, ProceduralMaterial.MAGIC, tx = 4.0f, ty = 19.3f, tz = 1.4f)
            }
            5 -> {
                b.addCylinder(0.6f, 9.2f, ProceduralMaterial.DARK_WOOD, tx = 3.8f, ty = 8.4f, tz = 1.8f, rx = 30f)
                b.addBox(3.7f, 3.0f, 4.7f, ProceduralMaterial.IRON, tx = 3.8f, ty = 12.2f, tz = 3.4f)
            }
            6 -> {
                b.addBox(4.0f, 3.1f, 3.0f, ProceduralMaterial.DARK_WOOD, ty = 6.1f, tz = 2.5f)
                b.addSphere(1.6f, ProceduralMaterial.HAY, ty = 7.4f, tz = 2.5f)
            }
        }

        // Heraldic back marker makes unit facing/class color readable from above.
        if (unitClass != 6) {
            b.addBox(
                width = 3.5f,
                height = 4.8f,
                depth = 0.55f,
                material = if (unitClass == 2 || unitClass == 3) ProceduralMaterial.RED_CLOTH else ProceduralMaterial.BLUE_CLOTH,
                ty = 8.2f,
                tz = -2.1f
            )
        }

        return b.buildModel()
    }
}
