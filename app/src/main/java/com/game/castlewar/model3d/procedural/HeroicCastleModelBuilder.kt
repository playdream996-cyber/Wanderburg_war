package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model

/**
 * Builds a bold, mobile-readable fantasy walking citadel.
 * The silhouette is intentionally exaggerated: broader base, taller towers,
 * stronger roof color blocking, large banners and tier-specific landmarks.
 */
object HeroicCastleModelBuilder {

    fun build(tier: Int): Model {
        val t = tier.coerceIn(1, 5)
        val b = ProceduralModelBuilder("heroic_castle_tier_$t")

        val baseW = 82f + (t - 1) * 4f
        val baseD = 100f + (t - 1) * 5f
        val baseH = 16f
        val wallH = 20f + (t - 1) * 2.5f
        val wallW = baseW - 8f
        val wallD = baseD - 10f
        val wallY = baseH + 5f

        // Massive readable chassis.
        b.addBox(baseW + 5f, 7f, baseD + 5f, ProceduralMaterial.DARK_WOOD, ty = 5f)
        b.addBox(baseW, baseH, baseD, ProceduralMaterial.DARK_STONE, ty = 11f)
        b.addBox(baseW - 6f, 3f, baseD - 4f, ProceduralMaterial.IRON, ty = 18.5f)

        // Gold/iron corner armor makes the base silhouette pop from terrain.
        val cornerX = baseW * 0.5f - 3f
        val cornerZ = baseD * 0.5f - 3f
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                b.addBox(
                    7f, 18f, 7f,
                    if (t >= 4) ProceduralMaterial.GOLD else ProceduralMaterial.IRON,
                    tx = sx * cornerX,
                    ty = 12f,
                    tz = sz * cornerZ
                )
            }
        }

        // Fortress wall mass.
        b.addBox(wallW, wallH, wallD, ProceduralMaterial.STONE, ty = wallY + wallH * 0.5f)
        b.addBox(wallW - 5f, 4f, wallD - 5f, ProceduralMaterial.LIGHT_STONE, ty = wallY + wallH + 2f)

        // Front gatehouse: oversized and highly legible.
        val gateZ = wallD * 0.5f + 4f
        b.addBox(32f, 27f, 12f, ProceduralMaterial.LIGHT_STONE, ty = wallY + 13.5f, tz = gateZ)
        b.addBox(22f, 19f, 13f, ProceduralMaterial.DARK_WOOD, ty = wallY + 9.5f, tz = gateZ + 1f)
        for (i in -2..2) {
            b.addCylinder(0.8f, 18f, ProceduralMaterial.IRON, tx = i * 4f, ty = wallY + 9f, tz = gateZ + 7f)
        }
        b.addBox(18f, 4f, 2f, ProceduralMaterial.GOLD, ty = wallY + 23f, tz = gateZ + 7f)

        // Corner towers: main silhouette anchors.
        val towerR = 13f + (t - 1) * 1.6f
        val towerH = 39f + (t - 1) * 5f
        val tx = wallW * 0.5f - 3f
        val tz = wallD * 0.5f - 5f
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                val x = sx * tx
                val z = sz * tz
                b.addCylinder(towerR, towerH, ProceduralMaterial.STONE, segments = 12, tx = x, ty = wallY + towerH * 0.5f, tz = z)
                b.addCylinder(towerR * 1.24f, 5f, ProceduralMaterial.LIGHT_STONE, segments = 12, tx = x, ty = wallY + towerH + 2.5f, tz = z)

                // Battlement teeth around tower lip.
                for (k in 0 until 8) {
                    val angle = Math.toRadians((k * 45.0))
                    val bx = x + kotlin.math.cos(angle).toFloat() * towerR * 1.18f
                    val bz = z + kotlin.math.sin(angle).toFloat() * towerR * 1.18f
                    b.addBox(4f, 6f, 4f, ProceduralMaterial.LIGHT_STONE, tx = bx, ty = wallY + towerH + 5f, tz = bz)
                }

                val roofH = 20f + (t - 1) * 2f
                b.addCone(
                    towerR * 1.38f,
                    roofH,
                    if (t == 1) ProceduralMaterial.RED_ROOF else ProceduralMaterial.BLUE_ROOF,
                    segments = 12,
                    tx = x,
                    ty = wallY + towerH + 8f + roofH * 0.5f,
                    tz = z
                )
                b.addSphere(1.6f, ProceduralMaterial.GOLD, tx = x, ty = wallY + towerH + roofH + 9f, tz = z)
                b.addCylinder(0.6f, 11f, ProceduralMaterial.GOLD, tx = x, ty = wallY + towerH + roofH + 14f, tz = z)
                b.addBox(11f, 5f, 0.7f, if (t >= 4) ProceduralMaterial.RED_CLOTH else ProceduralMaterial.BLUE_CLOTH, tx = x + 5.5f, ty = wallY + towerH + roofH + 18f, tz = z)
            }
        }

        // Central keep, intentionally tall and colorful.
        val keepW = 44f + (t - 1) * 2f
        val keepD = 48f + (t - 1) * 2f
        val keepH = 33f + (t - 1) * 3f
        val keepBase = wallY + wallH + 4f
        b.addBox(keepW, keepH * 0.45f, keepD, ProceduralMaterial.STONE, ty = keepBase + keepH * 0.225f, tz = -3f)
        b.addBox(keepW - 2f, keepH * 0.55f, keepD - 2f, ProceduralMaterial.PLASTER, ty = keepBase + keepH * 0.725f, tz = -3f)

        // Heavy timber framing on keep.
        for (sx in listOf(-1f, 1f)) {
            b.addBox(3f, keepH * 0.55f, 3f, ProceduralMaterial.DARK_WOOD, tx = sx * (keepW * 0.5f - 2f), ty = keepBase + keepH * 0.725f, tz = -3f)
        }
        b.addBox(keepW - 3f, 3f, keepD + 1f, ProceduralMaterial.DARK_WOOD, ty = keepBase + keepH * 0.58f, tz = -3f)

        val keepRoofH = 20f
        b.addPitchedRoof(keepW + 2f, keepRoofH, keepD + 3f, ProceduralMaterial.RED_ROOF, overhang = 4f, ty = keepBase + keepH + keepRoofH * 0.5f, tz = -3f)
        b.addBox(6f, 17f, 6f, ProceduralMaterial.DARK_STONE, tx = keepW * 0.27f, ty = keepBase + keepH + 11f, tz = 4f)

        // Strong crenellated side silhouette.
        val battlementY = wallY + wallH + 5f
        for (i in -4..4) {
            b.addBox(6f, 7f, 4f, ProceduralMaterial.LIGHT_STONE, tx = i * 8.2f, ty = battlementY, tz = wallD * 0.5f)
            b.addBox(6f, 7f, 4f, ProceduralMaterial.LIGHT_STONE, tx = i * 8.2f, ty = battlementY, tz = -wallD * 0.5f)
        }

        // Tier progression must be obvious at a glance.
        if (t >= 2) {
            // Blue side roof turrets.
            for (sx in listOf(-1f, 1f)) {
                b.addCylinder(7f, 17f, ProceduralMaterial.LIGHT_STONE, segments = 10, tx = sx * 26f, ty = keepBase + 18f, tz = 14f)
                b.addCone(9f, 14f, ProceduralMaterial.BLUE_ROOF, segments = 10, tx = sx * 26f, ty = keepBase + 33f, tz = 14f)
            }
        }

        if (t >= 3) {
            // Heroic central crown tower.
            b.addCylinder(8f, 21f, ProceduralMaterial.LIGHT_STONE, segments = 10, ty = keepBase + keepH + keepRoofH + 10f, tz = -3f)
            b.addCone(10f, 17f, ProceduralMaterial.BLUE_ROOF, segments = 10, ty = keepBase + keepH + keepRoofH + 29f, tz = -3f)
            b.addSphere(2f, ProceduralMaterial.GOLD, ty = keepBase + keepH + keepRoofH + 39f, tz = -3f)
        }

        if (t >= 4) {
            // Huge front ram and red war banners.
            val ramZ = baseD * 0.5f + 10f
            b.addBox(baseW * 0.82f, 9f, 10f, ProceduralMaterial.IRON, ty = 9f, tz = ramZ)
            for (i in -3..3) {
                b.addCone(3f, 13f, ProceduralMaterial.GOLD, segments = 8, tx = i * 10f, ty = 9f, tz = ramZ + 7f, rx = -90f)
            }
            b.addBox(18f, 9f, 1f, ProceduralMaterial.RED_CLOTH, tx = -25f, ty = wallY + wallH + 14f, tz = wallD * 0.5f + 2f)
            b.addBox(18f, 9f, 1f, ProceduralMaterial.RED_CLOTH, tx = 25f, ty = wallY + wallH + 14f, tz = wallD * 0.5f + 2f)
        }

        if (t >= 5) {
            // Arcane crown reads clearly even at zoomed-out camera distance.
            val crownY = keepBase + keepH + keepRoofH + 47f
            b.addCylinder(7f, 16f, ProceduralMaterial.GOLD, segments = 8, ty = crownY, tz = -3f)
            b.addSphere(6f, ProceduralMaterial.CRYSTAL, ty = crownY + 12f, tz = -3f)
            for (i in 0 until 4) {
                val a = Math.toRadians((45 + i * 90).toDouble())
                b.addSphere(2.2f, ProceduralMaterial.MAGIC, tx = kotlin.math.cos(a).toFloat() * 10f, ty = crownY + 10f, tz = -3f + kotlin.math.sin(a).toFloat() * 10f)
            }
        }

        // Deck props for scale/readability.
        b.addCylinder(4f, 7f, ProceduralMaterial.DARK_WOOD, tx = 19f, ty = wallY + wallH + 4f, tz = 17f)
        b.addBox(7f, 7f, 7f, ProceduralMaterial.LIGHT_WOOD, tx = -18f, ty = wallY + wallH + 3.5f, tz = 18f)

        return b.buildModel()
    }
}
