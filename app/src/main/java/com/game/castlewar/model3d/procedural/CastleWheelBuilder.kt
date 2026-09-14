package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Builds the large, rugged medieval wooden wheel for the Wanderburg-style moving castle.
 *
 * Components:
 * - Heavy outer wooden rim with beveled facets
 * - Outer dark iron tire reinforcement band with protective studs
 * - Inner wooden ring
 * - Massive cylindrical wooden hub with iron bearing ring
 * - Solid iron center hub cap
 * - 8 heavy wooden spokes connecting the hub to the rim
 * - Protruding axle shaft
 */
object CastleWheelBuilder {

    fun buildWheel(radius: Float = 16f, width: Float = 6f): Model {
        val builder = ProceduralModelBuilder("castle_wheel")

        val hubR = radius * 0.28f
        val spokeCount = 8

        // 1. Heavy Wooden Outer Rim (Chunky faceted cylinder)
        builder.addCylinder(
            radius = radius,
            height = width,
            material = ProceduralMaterial.WOOD,
            segments = 16,
            rz = 90f // Aligned with axle along X axis
        )

        // 2. Iron Reinforcement Tire Band (slightly wider on the outer tread)
        builder.addCylinder(
            radius = radius * 1.02f,
            height = width * 0.45f,
            material = ProceduralMaterial.IRON,
            segments = 16,
            rz = 90f
        )

        // 3. Inner Wooden Ring
        builder.addCylinder(
            radius = radius * 0.82f,
            height = width * 0.85f,
            material = ProceduralMaterial.DARK_WOOD,
            segments = 14,
            rz = 90f
        )

        // 4. Central Wooden Hub
        builder.addCylinder(
            radius = hubR,
            height = width * 1.25f,
            material = ProceduralMaterial.WOOD,
            segments = 10,
            rz = 90f
        )

        // 5. Metal Center Bearing & Axle
        builder.addCylinder(
            radius = hubR * 0.45f,
            height = width * 1.65f,
            material = ProceduralMaterial.IRON,
            segments = 8,
            rz = 90f
        )

        // 6. 8 Heavy Wooden Spokes
        val spokeLen = radius * 0.82f - hubR
        val spokeThick = radius * 0.12f
        val spokeMidR = hubR + spokeLen * 0.5f

        for (i in 0 until spokeCount) {
            val angle = (i.toFloat() / spokeCount) * 2f * PI.toFloat()
            val deg = Math.toDegrees(angle.toDouble()).toFloat()

            val pz = cos(angle) * spokeMidR
            val py = sin(angle) * spokeMidR

            builder.addBox(
                width = width * 0.60f,
                height = spokeThick,
                depth = spokeLen,
                material = ProceduralMaterial.LIGHT_WOOD,
                tx = 0f,
                ty = py,
                tz = pz,
                rx = deg
            )

            // Metal reinforcement brackets on spokes
            if (i % 2 == 0) {
                builder.addBox(
                    width = width * 0.70f,
                    height = spokeThick * 1.2f,
                    depth = spokeLen * 0.25f,
                    material = ProceduralMaterial.IRON,
                    tx = 0f,
                    ty = py,
                    tz = pz,
                    rx = deg
                )
            }
        }

        return builder.buildModel()
    }
}
