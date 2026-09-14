package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.Model
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural generator for stylized medieval village buildings:
 * - Small Cottages, Medium Townhouses, Farmhouses, Barns, Windmills, Market Stalls, Wells, Watchtowers.
 * Features:
 * - Stone foundations, timber frames with diagonal braces, cream stucco plaster walls,
 *   steep pitched tile roofs with overhangs, stone chimneys, wooden doors, and shuttered windows.
 */
object MedievalHouseBuilder {

    /**
     * Builds a medieval house variant.
     * @param variant 0 = Small Cottage, 1 = Two-Story Townhouse, 2 = Farmhouse / Long Barn, 3 = Blacksmith Forge
     */
    fun buildHouse(variant: Int = 0): Model {
        val name = "village_house_$variant"
        val builder = ProceduralModelBuilder(name)

        val roofMat = when (variant % 4) {
            0 -> ProceduralMaterial.RED_ROOF
            1 -> ProceduralMaterial.BLUE_ROOF
            2 -> ProceduralMaterial.DARK_WOOD
            else -> ProceduralMaterial.DARK_STONE
        }

        when (variant % 4) {
            0 -> {
                // Small Cottage
                val w = 24f; val h = 14f; val d = 20f
                val stoneH = 4.5f

                // Stone foundation
                builder.addBox(w, stoneH, d, ProceduralMaterial.STONE, ty = stoneH * 0.5f)
                // Half-timbered plaster upper section
                builder.addBox(w - 0.8f, h - stoneH, d - 0.8f, ProceduralMaterial.PLASTER, ty = stoneH + (h - stoneH) * 0.5f)

                // Corner timber posts
                for (sx in listOf(-1f, 1f)) {
                    for (sz in listOf(-1f, 1f)) {
                        builder.addBox(1.5f, h - stoneH, 1.5f, ProceduralMaterial.DARK_WOOD,
                            tx = sx * (w * 0.5f - 0.8f), ty = stoneH + (h - stoneH) * 0.5f, tz = sz * (d * 0.5f - 0.8f))
                    }
                }

                // Front wooden door
                builder.addBox(4.5f, 7.5f, 0.6f, ProceduralMaterial.WOOD, tz = d * 0.5f, ty = 4.5f)
                // Shuttered window
                builder.addBox(3.5f, 3.5f, 0.6f, ProceduralMaterial.DARK_WOOD, tx = 6f, ty = 8f, tz = d * 0.5f)

                // Pitched roof
                val rH = 10f
                builder.addPitchedRoof(w, rH, d, roofMat, overhang = 2f, ty = h + rH * 0.5f)

                // Stone Chimney
                builder.addBox(3.5f, 12f, 3.5f, ProceduralMaterial.DARK_STONE, tx = w * 0.3f, ty = h + 5f, tz = d * 0.2f)
            }
            1 -> {
                // Two-Story Townhouse with overhanging upper floor
                val w = 26f; val h1 = 12f; val h2 = 12f; val d = 24f

                // Ground Floor (Stone)
                builder.addBox(w, h1, d, ProceduralMaterial.DARK_STONE, ty = h1 * 0.5f)

                // Second Floor Jetty / Overhang (Plaster + Timber)
                val w2 = w + 3f
                val d2 = d + 3f
                builder.addBox(w2, h2, d2, ProceduralMaterial.PLASTER, ty = h1 + h2 * 0.5f)

                // Timber corner beams and braces on second floor
                for (sx in listOf(-1f, 1f)) {
                    for (sz in listOf(-1f, 1f)) {
                        builder.addBox(1.8f, h2, 1.8f, ProceduralMaterial.DARK_WOOD,
                            tx = sx * (w2 * 0.5f - 0.9f), ty = h1 + h2 * 0.5f, tz = sz * (d2 * 0.5f - 0.9f))
                        // Overhang support corbels
                        builder.addBox(1.5f, 3.5f, 2.5f, ProceduralMaterial.WOOD,
                            tx = sx * (w * 0.5f - 0.5f), ty = h1 - 1.5f, tz = sz * (d * 0.5f - 0.5f))
                    }
                }

                // Windows
                builder.addBox(3.5f, 4f, 0.5f, ProceduralMaterial.LIGHT_WOOD, tx = -5f, ty = h1 + 6f, tz = d2 * 0.5f)
                builder.addBox(3.5f, 4f, 0.5f, ProceduralMaterial.LIGHT_WOOD, tx = 5f, ty = h1 + 6f, tz = d2 * 0.5f)

                // Front Door with stone arch frame
                builder.addBox(5f, 8f, 0.8f, ProceduralMaterial.DARK_WOOD, ty = 4f, tz = d * 0.5f)

                // Steep Pitched Roof
                val rH = 14f
                builder.addPitchedRoof(w2, rH, d2, roofMat, overhang = 2.5f, ty = h1 + h2 + rH * 0.5f)

                // Tall Chimney
                builder.addBox(4f, 16f, 4f, ProceduralMaterial.STONE, tx = -w2 * 0.32f, ty = h1 + h2 + 6f, tz = 0f)
            }
            2 -> {
                // Farmhouse / Long Barn
                val w = 36f; val h = 13f; val d = 22f

                // Stone foundation
                builder.addBox(w, 3.5f, d, ProceduralMaterial.STONE, ty = 1.75f)
                // Dark wood log walls
                builder.addBox(w - 1f, h - 3.5f, d - 1f, ProceduralMaterial.DARK_WOOD, ty = 3.5f + (h - 3.5f) * 0.5f)

                // Large double barn doors
                builder.addBox(8f, 9f, 0.6f, ProceduralMaterial.WOOD, ty = 4.5f, tz = d * 0.5f)
                builder.addBox(8.4f, 0.8f, 0.8f, ProceduralMaterial.IRON, ty = 4.5f, tz = d * 0.5f)

                // Low-angle rustic pitched roof
                val rH = 11f
                builder.addPitchedRoof(w, rH, d, roofMat, overhang = 3f, ty = h + rH * 0.5f)

                // Hayloft hoist beam & pulley
                builder.addBox(1.5f, 1.5f, 6f, ProceduralMaterial.WOOD, ty = h + 2f, tz = d * 0.5f + 2f)
            }
            else -> {
                // Village Blacksmith / Forge
                val w = 30f; val h = 15f; val d = 22f

                // Solid Stone Workshop
                builder.addBox(w * 0.65f, h, d, ProceduralMaterial.DARK_STONE, tx = -w * 0.175f, ty = h * 0.5f)
                // Huge Stone Smelting Chimney
                builder.addBox(6.5f, 22f, 6.5f, ProceduralMaterial.STONE, tx = -w * 0.4f, ty = 11f, tz = -d * 0.2f)
                builder.addBox(5.0f, 4.0f, 4.0f, ProceduralMaterial.FIRE_EMBER, tx = -w * 0.4f, ty = 3f, tz = d * 0.35f) // Open hearth

                // Open-sided timber awning for outdoor anvil work
                builder.addBox(1.5f, h - 2f, 1.5f, ProceduralMaterial.DARK_WOOD, tx = w * 0.45f, ty = (h - 2f) * 0.5f, tz = d * 0.45f)
                builder.addBox(1.5f, h - 2f, 1.5f, ProceduralMaterial.DARK_WOOD, tx = w * 0.45f, ty = (h - 2f) * 0.5f, tz = -d * 0.45f)
                // Shed awning roof
                builder.addBox(w * 0.45f + 2f, 1.2f, d + 2f, ProceduralMaterial.WOOD, tx = w * 0.25f, ty = h - 0.5f, rx = 8f)

                // Heavy Iron Anvil on tree stump
                builder.addCylinder(2.2f, 2.8f, ProceduralMaterial.DARK_WOOD, segments = 8, tx = w * 0.25f, ty = 1.4f, tz = 0f)
                builder.addBox(2.2f, 1.5f, 4.5f, ProceduralMaterial.IRON, tx = w * 0.25f, ty = 3.5f, tz = 0f)
                builder.addCone(1.2f, 2.0f, ProceduralMaterial.IRON, segments = 6, tx = w * 0.25f, ty = 3.8f, tz = 3f, rx = 90f) // Horn

                // Quenching water trough
                builder.addBox(3.0f, 2.5f, 7.0f, ProceduralMaterial.WOOD, tx = w * 0.25f, ty = 1.25f, tz = -6f)
                builder.addBox(2.2f, 0.4f, 6.2f, ProceduralMaterial.BLUE_ROOF, tx = w * 0.25f, ty = 2.2f, tz = -6f)

                // Workshop pitched roof
                builder.addPitchedRoof(w * 0.65f + 3f, 11f, d + 3f, roofMat, overhang = 2f, tx = -w * 0.175f, ty = h + 5.5f)
            }
        }

        return builder.buildModel()
    }

    /**
     * Builds a Medieval Windmill with rotating sails.
     */
    fun buildWindmill(): Model {
        val builder = ProceduralModelBuilder("village_windmill")

        val towerH = 34f
        val baseR = 11f
        val topR = 8f

        // 1. Tapered Round Stone Tower (approximated by stacked tiers)
        builder.addCylinder(baseR, towerH * 0.5f, ProceduralMaterial.STONE, segments = 10, ty = towerH * 0.25f)
        builder.addCylinder(topR, towerH * 0.5f, ProceduralMaterial.PLASTER, segments = 10, ty = towerH * 0.75f)

        // Wood belt trim
        builder.addCylinder(topR + 0.8f, 1.8f, ProceduralMaterial.DARK_WOOD, segments = 10, ty = towerH * 0.5f)

        // Conical Roof Cap
        val capH = 10f
        builder.addCone(topR * 1.25f, capH, ProceduralMaterial.BLUE_ROOF, segments = 10, ty = towerH + capH * 0.5f)

        // Front Winch Hub
        builder.addCylinder(1.8f, 3.5f, ProceduralMaterial.DARK_WOOD, segments = 8, ty = towerH - 2f, tz = topR + 1f, rx = 90f)

        // 4 Sail Blades
        for (i in 0..3) {
            val a = (i * 90f) * (PI.toFloat() / 180f)
            val deg = i * 90f
            // Sail Spar
            builder.addBox(
                width = 1.0f,
                height = 20.0f,
                depth = 0.8f,
                material = ProceduralMaterial.WOOD,
                tx = cos(a) * 10f,
                ty = (towerH - 2f) + sin(a) * 10f,
                tz = topR + 2.5f,
                rz = deg
            )
            // Cloth Sail Canvas
            builder.addBox(
                width = 3.5f,
                height = 16.0f,
                depth = 0.2f,
                material = ProceduralMaterial.PLASTER,
                tx = cos(a) * 10f + 1.2f,
                ty = (towerH - 2f) + sin(a) * 10f,
                tz = topR + 2.6f,
                rz = deg
            )
        }

        // Entrance Door
        builder.addBox(4f, 7f, 0.6f, ProceduralMaterial.DARK_WOOD, tz = baseR, ty = 3.5f)

        return builder.buildModel()
    }

    /**
     * Builds a Village Water Well with stone curb, timber posts, and roof canopy.
     */
    fun buildWell(): Model {
        val builder = ProceduralModelBuilder("village_well")

        // Stone circular basin
        builder.addCylinder(5f, 4f, ProceduralMaterial.STONE, segments = 10, ty = 2f)
        // Water surface inside
        builder.addCylinder(3.8f, 0.2f, ProceduralMaterial.BLUE_ROOF, segments = 8, ty = 3f)

        // Two vertical timber posts
        for (sx in listOf(-1f, 1f)) {
            builder.addBox(1.2f, 10f, 1.2f, ProceduralMaterial.DARK_WOOD, tx = sx * 4.5f, ty = 5f)
        }
        // Top cross beam
        builder.addBox(10.5f, 1.2f, 1.2f, ProceduralMaterial.DARK_WOOD, ty = 9.5f)

        // Winch drum & rope
        builder.addCylinder(1.0f, 6.0f, ProceduralMaterial.WOOD, segments = 8, ty = 8.5f, rz = 90f)

        // Small pitched roof canopy
        builder.addPitchedRoof(12f, 4.5f, 8f, ProceduralMaterial.RED_ROOF, overhang = 1.5f, ty = 12f)

        return builder.buildModel()
    }

    /**
     * Builds a Village Watchtower.
     */
    fun buildWatchtower(): Model {
        val builder = ProceduralModelBuilder("village_watchtower")

        val h = 42f
        val postSpread = 7f

        // 4 Splayed Timber Legs
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(2.2f, h, 2.2f, ProceduralMaterial.DARK_WOOD,
                    tx = sx * postSpread, ty = h * 0.5f, tz = sz * postSpread)
            }
        }

        // Horizontal and diagonal cross braces
        for (level in listOf(12f, 24f, 36f)) {
            builder.addBox(postSpread * 2f + 2f, 1.5f, 1.5f, ProceduralMaterial.WOOD, ty = level, tz = postSpread)
            builder.addBox(postSpread * 2f + 2f, 1.5f, 1.5f, ProceduralMaterial.WOOD, ty = level, tz = -postSpread)
            builder.addBox(1.5f, 1.5f, postSpread * 2f + 2f, ProceduralMaterial.WOOD, tx = postSpread, ty = level)
            builder.addBox(1.5f, 1.5f, postSpread * 2f + 2f, ProceduralMaterial.WOOD, tx = -postSpread, ty = level)
        }

        // Top Observation Platform
        val platW = 20f
        builder.addBox(platW, 2f, platW, ProceduralMaterial.WOOD, ty = h + 1f)

        // Wooden Parapet Railings
        for (sx in listOf(-1f, 1f)) {
            builder.addBox(platW, 4f, 1.2f, ProceduralMaterial.LIGHT_WOOD, ty = h + 4f, tz = sx * (platW * 0.5f - 0.6f))
            builder.addBox(1.2f, 4f, platW, ProceduralMaterial.LIGHT_WOOD, tx = sx * (platW * 0.5f - 0.6f), ty = h + 4f)
        }

        // Conical / Pyramidal Roof
        builder.addCone(radius = platW * 0.75f, height = 12f, material = ProceduralMaterial.RED_ROOF, segments = 8, ty = h + 12f)

        return builder.buildModel()
    }

    /**
     * Builds a rustic Long Barn with overhanging hayloft.
     */
    fun buildBarn(): Model {
        val builder = ProceduralModelBuilder("village_barn")
        val w = 34f; val h = 22f; val d = 26f

        // Stone foundation plinth
        builder.addBox(w + 1f, 3.5f, d + 1f, ProceduralMaterial.STONE, ty = 1.75f)

        // Weathered red/brown timber barn body
        builder.addBox(w, h - 3.5f, d, ProceduralMaterial.WOOD, ty = 3.5f + (h - 3.5f) * 0.5f)

        // Dark corner posts and vertical studs
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(2.2f, h - 3.5f, 2.2f, ProceduralMaterial.DARK_WOOD,
                    tx = sx * (w * 0.5f - 1f), ty = 3.5f + (h - 3.5f) * 0.5f, tz = sz * (d * 0.5f - 1f))
            }
        }

        // Steeper Gable Hayloft Roof
        val roofH = 14f
        builder.addBox(w + 3f, 1.8f, d + 4f, ProceduralMaterial.RED_ROOF, ty = h + 1f)
        builder.addBox(w - 2f, roofH, d + 2f, ProceduralMaterial.RED_ROOF, ty = h + roofH * 0.5f + 1f)

        // Front barn double doors
        builder.addBox(9.0f, 11.0f, 1.0f, ProceduralMaterial.DARK_WOOD, ty = 8.5f, tz = d * 0.5f + 0.3f)
        builder.addBox(9.2f, 0.8f, 1.2f, ProceduralMaterial.IRON, ty = 12f, tz = d * 0.5f + 0.3f) // Iron hinge

        // Hayloft open window with protruding crane beam
        builder.addBox(4.5f, 4.5f, 1.0f, ProceduralMaterial.DARK_WOOD, ty = h + 4f, tz = d * 0.5f + 0.5f)
        builder.addBox(1.2f, 1.2f, 6.0f, ProceduralMaterial.LIGHT_WOOD, ty = h + 7.5f, tz = d * 0.5f + 2.5f)

        return builder.buildModel()
    }

    /**
     * Builds a chunky Palisade Wall segment with defensive wooden spikes and catwalk.
     */
    fun buildPalisadeWall(): Model {
        val builder = ProceduralModelBuilder("palisade_wall")
        val wallW = 32f; val wallH = 16f

        // Row of pointed wooden logs
        val numLogs = 8
        val logW = wallW / numLogs
        for (i in 0 until numLogs) {
            val lx = -wallW * 0.5f + (i + 0.5f) * logW
            val logH = wallH + (if (i % 2 == 0) 1.5f else -1.0f)
            builder.addCylinder(radius = 1.9f, height = logH - 3f, material = ProceduralMaterial.DARK_WOOD, segments = 6, tx = lx, ty = (logH - 3f) * 0.5f)
            builder.addCone(radius = 1.9f, height = 3.5f, material = ProceduralMaterial.WOOD, segments = 6, tx = lx, ty = logH - 1.2f)
        }

        // Horizontal cross beams
        builder.addBox(wallW + 2f, 1.6f, 2.0f, ProceduralMaterial.WOOD, ty = 5f, tz = -2.2f)
        builder.addBox(wallW + 2f, 1.6f, 2.0f, ProceduralMaterial.WOOD, ty = 11f, tz = -2.2f)

        // Raised wooden catwalk on defender's side
        builder.addBox(wallW, 1.5f, 6.0f, ProceduralMaterial.LIGHT_WOOD, ty = 8.5f, tz = -4.5f)
        builder.addBox(1.8f, 8.5f, 1.8f, ProceduralMaterial.DARK_WOOD, tx = -wallW * 0.35f, ty = 4.25f, tz = -6.5f)
        builder.addBox(1.8f, 8.5f, 1.8f, ProceduralMaterial.DARK_WOOD, tx = wallW * 0.35f, ty = 4.25f, tz = -6.5f)

        return builder.buildModel()
    }
}
