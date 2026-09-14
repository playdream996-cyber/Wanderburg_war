package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural generator for Enemy Fortresses and the colossal Boss Fortress.
 */
object FortressModelBuilder {

    /**
     * Builds standard Enemy Castle / Fortress Outpost.
     */
    fun buildEnemyFortress(): Model {
        val builder = ProceduralModelBuilder("enemy_fortress")

        val w = 84f
        val h = 22f
        val d = 84f

        // 1. Dark Stone Base & Walls
        builder.addBox(w, h, d, ProceduralMaterial.DARK_STONE, ty = h * 0.5f)

        // Wall battlements
        val batH = 5f
        val batY = h + batH * 0.5f
        for (i in -4..4) {
            val offset = i * 9.5f
            builder.addBox(6f, batH, 3f, ProceduralMaterial.STONE, tx = offset, ty = batY, tz = d * 0.5f)
            builder.addBox(6f, batH, 3f, ProceduralMaterial.STONE, tx = offset, ty = batY, tz = -d * 0.5f)
            builder.addBox(3f, batH, 6f, ProceduralMaterial.STONE, tx = w * 0.5f, ty = batY, tz = offset)
            builder.addBox(3f, batH, 6f, ProceduralMaterial.STONE, tx = -w * 0.5f, ty = batY, tz = offset)
        }

        // 2. 4 Heavy Corner Towers
        val towerR = 12f
        val towerH = 36f
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                val tx = sx * (w * 0.5f)
                val tz = sz * (d * 0.5f)
                builder.addCylinder(towerR, towerH, ProceduralMaterial.DARK_STONE, segments = 10, tx = tx, ty = towerH * 0.5f, tz = tz)
                builder.addCone(towerR * 1.3f, 16f, ProceduralMaterial.RED_ROOF, segments = 10, tx = tx, ty = towerH + 8f, tz = tz)
                // Red Enemy Banner
                builder.addBox(8f, 14f, 0.4f, ProceduralMaterial.RED_CLOTH, tx = tx + sx * 6f, ty = towerH - 4f, tz = tz)
            }
        }

        // 3. Central Keep
        val keepW = 44f
        val keepH = 32f
        val keepD = 44f
        builder.addBox(keepW, keepH, keepD, ProceduralMaterial.STONE, ty = h + keepH * 0.5f)
        builder.addPitchedRoof(keepW, 16f, keepD, ProceduralMaterial.RED_ROOF, overhang = 2f, ty = h + keepH + 8f)

        // 4. Front Spiked Portcullis Gate
        builder.addBox(18f, 14f, 6f, ProceduralMaterial.DARK_WOOD, ty = 7f, tz = d * 0.5f + 1f)
        for (i in -2..2) {
            builder.addCylinder(0.8f, 14f, ProceduralMaterial.IRON, tx = i * 3.6f, ty = 7f, tz = d * 0.5f + 4f)
        }

        return builder.buildModel()
    }

    /**
     * Builds the Colossal Boss Fortress (Giant Mobile War Citadel).
     */
    fun buildBossFortress(): Model {
        val builder = ProceduralModelBuilder("boss_fortress")

        val w = 140f
        val h = 32f
        val d = 160f

        // 1. Massive Iron-Plated Foundation Hull
        builder.addBox(w, h, d, ProceduralMaterial.IRON, ty = h * 0.5f + 6f)

        // Heavy Spiked Bumper Plating & Ram on front
        builder.addWedge(w * 0.7f, h * 0.8f, 30f, ProceduralMaterial.DARK_STONE,
            ty = h * 0.5f + 6f, tz = d * 0.5f + 15f, ry = 180f)
        // Iron Spikes on Ram
        for (i in -3..3) {
            builder.addCone(3.5f, 14f, ProceduralMaterial.IRON, segments = 6,
                tx = i * 14f, ty = 12f, tz = d * 0.5f + 30f, rx = 90f)
        }

        // 2. Multi-tier Dark Granite Castle Superstructure
        val tier2W = 110f
        val tier2H = 26f
        val tier2D = 130f
        val tier2Y = h + 6f

        builder.addBox(tier2W, tier2H, tier2D, ProceduralMaterial.DARK_STONE, ty = tier2Y + tier2H * 0.5f)

        // Iron Armor Plates Bolted to Citadel Walls
        for (i in -4..4) {
            val pz = i * 14f
            builder.addBox(1.5f, 18f, 10f, ProceduralMaterial.IRON, tx = tier2W * 0.5f + 1f, ty = tier2Y + 11f, tz = pz)
            builder.addBox(1.5f, 18f, 10f, ProceduralMaterial.IRON, tx = -tier2W * 0.5f - 1f, ty = tier2Y + 11f, tz = pz)
        }

        // 3. 4 Colossal Bastion Towers
        val bR = 18f
        val bH = 55f
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                val tx = sx * (tier2W * 0.5f)
                val tz = sz * (tier2D * 0.5f)
                builder.addCylinder(bR, bH, ProceduralMaterial.DARK_STONE, segments = 12, tx = tx, ty = bH * 0.5f + 6f, tz = tz)
                builder.addCylinder(bR * 1.2f, 6f, ProceduralMaterial.IRON, segments = 12, tx = tx, ty = bH + 9f, tz = tz)
                builder.addCone(bR * 1.35f, 24f, ProceduralMaterial.RED_ROOF, segments = 12, tx = tx, ty = bH + 24f, tz = tz)

                // Giant Blood-Red War Flags
                builder.addBox(14f, 8f, 0.4f, ProceduralMaterial.RED_CLOTH, tx = tx + sx * 7f, ty = bH + 38f, tz = tz)
            }
        }

        // 4. Colossal Central Citadel Keep & Spire
        val keepW = 65f
        val keepH = 45f
        val keepD = 75f
        val keepY = tier2Y + tier2H

        builder.addBox(keepW, keepH, keepD, ProceduralMaterial.DARK_STONE, ty = keepY + keepH * 0.5f)
        builder.addPitchedRoof(keepW, 25f, keepD, ProceduralMaterial.RED_ROOF, overhang = 4f, ty = keepY + keepH + 12.5f)

        // Central Crown Watchtower & Searchlight Beacon
        builder.addCylinder(14f, 30f, ProceduralMaterial.IRON, segments = 10, ty = keepY + keepH + 25f)
        builder.addSphere(7f, ProceduralMaterial.GOLD, ty = keepY + keepH + 42f)

        // 5. Multiple Heavy Cannon Batteries on Lower Deck
        for (sx in listOf(-1f, 1f)) {
            for (cz in listOf(-30f, 0f, 30f)) {
                builder.addCylinder(3.0f, 16f, ProceduralMaterial.IRON, segments = 8,
                    tx = sx * (tier2W * 0.5f + 6f), ty = tier2Y + 8f, tz = cz, rz = sx * 90f)
            }
        }

        return builder.buildModel()
    }
}
