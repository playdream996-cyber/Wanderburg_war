package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model

/**
 * Builds the Wanderburg-style Moving Medieval Fortress for the player.
 *
 * Characteristics:
 * - Chunky, powerful medieval silhouette readable from the isometric 42° camera
 * - Heavy stone foundation chassis with dark timber beams and iron corner clamps
 * - 4 massive corner towers with crenelated battlements and conical slate roofs
 * - Central elevated keep with half-timbered walls and pitched terracotta red roof
 * - Front portcullis gate with stone arch
 * - Lower and upper weapon decks for mounting cannons, ballistas, catapults, and archer towers
 * - Diagonal timber reinforcement struts and iron corner plates
 * - Heraldic pennant flags fluttering above the towers
 * - Decorative barrels, crates, and arrow slit details
 */
object CastleModelBuilder {

    fun buildPlayerCastle(tier: Int = 1): Model {
        val modelName = if (tier == 1) "player_castle" else "player_castle_tier_$tier"
        val builder = ProceduralModelBuilder(modelName)

        val chassisW = 72f + (tier - 1) * 3f
        val chassisH = 14f
        val chassisD = 90f + (tier - 1) * 4f

        // 1. Heavy Stone Chassis Foundation
        builder.addBox(
            width = chassisW,
            height = chassisH,
            depth = chassisD,
            material = if (tier >= 4) ProceduralMaterial.DARK_STONE else ProceduralMaterial.DARK_STONE,
            ty = chassisH * 0.5f + 4f
        )

        // Lower Wood Bumper & Axle Beams
        builder.addBox(
            width = chassisW + 4f,
            height = 5f,
            depth = chassisD + 4f,
            material = ProceduralMaterial.DARK_WOOD,
            ty = 6f
        )

        // Iron Corner Reinforcement Clamps
        val cx = chassisW * 0.5f + 1f
        val cz = chassisD * 0.5f + 1f
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(
                    width = 6f,
                    height = chassisH + 4f,
                    depth = 6f,
                    material = if (tier >= 4) ProceduralMaterial.GOLD else ProceduralMaterial.IRON,
                    tx = sx * cx,
                    ty = chassisH * 0.5f + 4f,
                    tz = sz * cz
                )
            }
        }

        // Tiers 2+: Iron Side Bumper and Armor Brackets
        if (tier >= 2) {
            for (sx in listOf(-1f, 1f)) {
                builder.addBox(
                    width = 2.5f,
                    height = 8f,
                    depth = chassisD * 0.7f,
                    material = ProceduralMaterial.IRON,
                    tx = sx * (chassisW * 0.5f + 2.5f),
                    ty = 8f,
                    tz = 0f
                )
            }
        }

        // Tiers 4 & 5: Heavy Spiked Iron Ram Bumper on the Front
        if (tier >= 4) {
            val ramZ = chassisD * 0.5f + 8f
            builder.addBox(
                width = chassisW * 0.85f,
                height = 7f,
                depth = 8f,
                material = ProceduralMaterial.IRON,
                ty = 7f,
                tz = ramZ
            )
            // Heavy Spikes
            for (i in -3..3) {
                builder.addCone(
                    radius = 2.5f,
                    height = 10f,
                    material = if (tier >= 5) ProceduralMaterial.GOLD else ProceduralMaterial.IRON,
                    segments = 6,
                    tx = i * 8.5f,
                    ty = 7f,
                    tz = ramZ + 5f,
                    rx = -90f
                )
            }
        }

        // 2. Main Stone Fortress Walls (Lower Courtyard Tier)
        val wallW = chassisW - 6f
        val wallH = 16f + (tier - 1) * 2f
        val wallD = chassisD - 8f
        val wallBaseY = chassisH + 4f

        builder.addBox(
            width = wallW,
            height = wallH,
            depth = wallD,
            material = ProceduralMaterial.STONE,
            ty = wallBaseY + wallH * 0.5f
        )

        // 3. Front Gatehouse & Heavy Iron Portcullis
        val gateW = 22f
        val gateH = 18f
        val gateD = 10f
        val gateZ = wallD * 0.5f + 2f
        val gateBaseY = wallBaseY

        // Stone Archway
        builder.addBox(
            width = gateW + 8f,
            height = gateH + 4f,
            depth = gateD,
            material = ProceduralMaterial.LIGHT_STONE,
            ty = gateBaseY + (gateH + 4f) * 0.5f,
            tz = gateZ
        )
        // Gate Opening / Recess
        builder.addBox(
            width = gateW,
            height = gateH,
            depth = gateD + 1f,
            material = ProceduralMaterial.DARK_WOOD,
            ty = gateBaseY + gateH * 0.5f,
            tz = gateZ
        )
        // Iron Grille Bars
        for (ix in -2..2) {
            builder.addCylinder(
                radius = 0.7f,
                height = gateH * 0.85f,
                material = ProceduralMaterial.IRON,
                tx = ix * 3.8f,
                ty = gateBaseY + gateH * 0.45f,
                tz = gateZ + gateD * 0.5f
            )
        }

        // 4. Massive Corner Bastion Towers (Tiers 2-5 get larger fortified towers)
        val towerR = 11f + (tier - 1) * 1.5f
        val towerH = 32f + (tier - 1) * 4f
        val towerX = wallW * 0.5f - 2f
        val towerZ = wallD * 0.5f - 4f

        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                val tx = sx * towerX
                val tz = sz * towerZ

                // Round stone tower shaft
                builder.addCylinder(
                    radius = towerR,
                    height = towerH,
                    material = ProceduralMaterial.STONE,
                    segments = 10,
                    tx = tx,
                    ty = wallBaseY + towerH * 0.5f,
                    tz = tz
                )

                // Flared stone corbel parapet
                builder.addCylinder(
                    radius = towerR * 1.25f,
                    height = 5f,
                    material = ProceduralMaterial.LIGHT_STONE,
                    segments = 10,
                    tx = tx,
                    ty = wallBaseY + towerH + 2.5f,
                    tz = tz
                )

                // Conical Slate Roof (Tier 1 gets simple timber roof, Tiers 2+ get blue slate with gold finials)
                val roofH = 18f + (tier - 1) * 2f
                val roofMat = if (tier == 1) ProceduralMaterial.WOOD else ProceduralMaterial.BLUE_ROOF
                builder.addCone(
                    radius = towerR * 1.35f,
                    height = roofH,
                    material = roofMat,
                    segments = 10,
                    tx = tx,
                    ty = wallBaseY + towerH + 5f + roofH * 0.5f,
                    tz = tz
                )

                // Gold Finial & Flagpole
                builder.addCylinder(
                    radius = 0.6f,
                    height = 12f,
                    material = ProceduralMaterial.GOLD,
                    tx = tx,
                    ty = wallBaseY + towerH + 5f + roofH + 5f,
                    tz = tz
                )
                // Red Heraldic Pennant Flag
                builder.addBox(
                    width = 9f,
                    height = 4.5f,
                    depth = 0.4f,
                    material = if (tier >= 4) ProceduralMaterial.RED_CLOTH else ProceduralMaterial.BLUE_CLOTH,
                    tx = tx + 4.5f,
                    ty = wallBaseY + towerH + 5f + roofH + 7f,
                    tz = tz
                )
            }
        }

        // 5. Crenelated Battlements along the Lower Walls
        val batH = 4.5f
        val batW = 5.5f
        val batD = 3f
        val batY = wallBaseY + wallH + batH * 0.5f

        // Front & Back battlements
        for (i in -3..3) {
            val bx = i * 8.5f
            builder.addBox(batW, batH, batD, ProceduralMaterial.LIGHT_STONE, tx = bx, ty = batY, tz = wallD * 0.5f)
            builder.addBox(batW, batH, batD, ProceduralMaterial.LIGHT_STONE, tx = bx, ty = batY, tz = -wallD * 0.5f)
        }
        // Left & Right battlements
        for (i in -4..4) {
            val bz = i * 8.5f
            builder.addBox(batD, batH, batW, ProceduralMaterial.LIGHT_STONE, tx = wallW * 0.5f, ty = batY, tz = bz)
            builder.addBox(batD, batH, batW, ProceduralMaterial.LIGHT_STONE, tx = -wallW * 0.5f, ty = batY, tz = bz)
        }

        // 6. Elevated Central Keep
        val keepW = 38f
        val keepH = 26f
        val keepD = 44f
        val keepBaseY = wallBaseY + wallH

        // Lower Keep Stone Floor
        builder.addBox(
            width = keepW,
            height = keepH * 0.45f,
            depth = keepD,
            material = ProceduralMaterial.STONE,
            ty = keepBaseY + keepH * 0.225f,
            tz = -2f
        )

        // Upper Keep Half-Timbered Plaster Section
        builder.addBox(
            width = keepW - 2f,
            height = keepH * 0.55f,
            depth = keepD - 2f,
            material = ProceduralMaterial.PLASTER,
            ty = keepBaseY + keepH * 0.45f + keepH * 0.275f,
            tz = -2f
        )

        // Timber Corner Posts & Cross Beams on Keep
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(
                    width = 2.5f,
                    height = keepH * 0.55f,
                    depth = 2.5f,
                    material = ProceduralMaterial.DARK_WOOD,
                    tx = sx * (keepW * 0.5f - 2f),
                    ty = keepBaseY + keepH * 0.45f + keepH * 0.275f,
                    tz = -2f + sz * (keepD * 0.5f - 2f)
                )
            }
        }

        // Steep Pitched Red Terracotta Roof on Keep
        val roofH = 16f
        builder.addPitchedRoof(
            width = keepW,
            height = roofH,
            depth = keepD,
            material = ProceduralMaterial.RED_ROOF,
            overhang = 3f,
            tx = 0f,
            ty = keepBaseY + keepH + roofH * 0.5f,
            tz = -2f
        )

        // Stone Chimney on Keep Roof
        builder.addBox(
            width = 5f,
            height = 14f,
            depth = 5f,
            material = ProceduralMaterial.DARK_STONE,
            tx = keepW * 0.28f,
            ty = keepBaseY + keepH + 10f,
            tz = 4f
        )

        // 7. Weapon Mount Platforms (Stout Timber Plinths)
        // Front deck weapon mount (Cannon)
        builder.addCylinder(
            radius = 8.5f,
            height = 3.5f,
            material = ProceduralMaterial.WOOD,
            tx = 0f,
            ty = wallBaseY + wallH + 1.75f,
            tz = 24f
        )
        builder.addCylinder(
            radius = 6f,
            height = 1.5f,
            material = ProceduralMaterial.IRON,
            tx = 0f,
            ty = wallBaseY + wallH + 4f,
            tz = 24f
        )

        // Left deck weapon mount
        builder.addCylinder(
            radius = 7.5f,
            height = 3.5f,
            material = ProceduralMaterial.WOOD,
            tx = -22f,
            ty = wallBaseY + wallH + 1.75f,
            tz = 0f
        )

        // Right deck weapon mount
        builder.addCylinder(
            radius = 7.5f,
            height = 3.5f,
            material = ProceduralMaterial.WOOD,
            tx = 22f,
            ty = wallBaseY + wallH + 1.75f,
            tz = 0f
        )

        // Rear deck weapon mount
        builder.addCylinder(
            radius = 7.5f,
            height = 3.5f,
            material = ProceduralMaterial.WOOD,
            tx = 0f,
            ty = wallBaseY + wallH + 1.75f,
            tz = -28f
        )

        // 8. Decorative Castle Details: Timber Braces, Barrels, Crates
        // Diagonal timber struts supporting the overhanging battlements
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(
                    width = 3f,
                    height = 12f,
                    depth = 3f,
                    material = ProceduralMaterial.WOOD,
                    tx = sx * (chassisW * 0.5f + 1f),
                    ty = wallBaseY - 2f,
                    tz = sz * (chassisD * 0.35f),
                    rz = sx * 30f
                )
            }
        }

        // Barrels on deck
        builder.addCylinder(
            radius = 2.5f,
            height = 6f,
            material = ProceduralMaterial.DARK_WOOD,
            tx = 18f,
            ty = wallBaseY + wallH + 3f,
            tz = 14f
        )
        builder.addCylinder(
            radius = 2.2f,
            height = 5.5f,
            material = ProceduralMaterial.DARK_WOOD,
            tx = 22f,
            ty = wallBaseY + wallH + 2.75f,
            tz = 12f
        )

        // Storage crates
        builder.addBox(
            width = 5f,
            height = 5f,
            depth = 5f,
            material = ProceduralMaterial.LIGHT_WOOD,
            tx = -18f,
            ty = wallBaseY + wallH + 2.5f,
            tz = 14f
        )

        // Tier 5: Colossal War Citadel Grand Features - Arcane Spire & Golden Crests
        if (tier >= 5) {
            // Central Tower Spire on Keep
            val spireBaseY = keepBaseY + keepH + 16f
            builder.addCylinder(
                radius = 6.0f,
                height = 14f,
                material = ProceduralMaterial.LIGHT_STONE,
                segments = 8,
                ty = spireBaseY + 7f,
                tz = -2f
            )
            builder.addCone(
                radius = 7.5f,
                height = 12f,
                material = ProceduralMaterial.BLUE_ROOF,
                segments = 8,
                ty = spireBaseY + 20f,
                tz = -2f
            )
            // Glowing Arcane Power Crystal atop the Citadel
            builder.addSphere(
                radius = 4.2f,
                material = ProceduralMaterial.MAGIC,
                ty = spireBaseY + 28f,
                tz = -2f
            )
            builder.addCylinder(
                radius = 0.8f,
                height = 18f,
                material = ProceduralMaterial.GOLD,
                ty = spireBaseY + 30f,
                tz = -2f
            )

            // Golden Eagle Crest on Gate
            builder.addBox(
                width = 8f,
                height = 6f,
                depth = 2f,
                material = ProceduralMaterial.GOLD,
                ty = gateBaseY + gateH + 5f,
                tz = gateZ + 2f
            )
        }

        return builder.buildModel()
    }
}
