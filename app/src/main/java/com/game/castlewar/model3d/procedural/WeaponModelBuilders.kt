package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural builders for the 4 castle-mounted weapons:
 * 1. Cannon: Rotating heavy metal barrel, flared muzzle, wooden carriage, dual wheels.
 * 2. Ballista: Wooden frame, curved tension bow arms, taut string, heavy bolt guide, swivel base.
 * 3. Catapult: Wheeled chassis, pivot axle, long throwing arm, counterweight, stone bucket.
 * 4. Archer Tower: Castle turret, battlements, wooden sun roof, arrow loops, animated archer figure!
 */
object WeaponModelBuilders {

    /**
     * Builds the Heavy Mounted Medieval Cannon.
     */
    /**
     * Builds the Heavy Mounted Medieval Cannon with visual upgrades based on weapon level.
     * Level 1: Standard carriage & barrel.
     * Level 2: Extended rifled barrel, brass muzzle ring, armored carriage cheek plates.
     * Level 3+: Twin bombard barrels with heavy iron frontal gunshield!
     */
    fun buildCannon(level: Int = 1): Model {
        val modelName = if (level <= 1) "cannon" else "cannon_lvl$level"
        val builder = ProceduralModelBuilder(modelName)

        // 1. Rotating Swivel Base Plate
        builder.addCylinder(
            radius = 6.5f,
            height = 1.8f,
            material = ProceduralMaterial.DARK_WOOD,
            segments = 10,
            ty = 0.9f
        )
        builder.addCylinder(
            radius = 4.5f,
            height = 1.0f,
            material = if (level >= 3) ProceduralMaterial.GOLD else ProceduralMaterial.IRON,
            segments = 10,
            ty = 2.3f
        )

        // 2. Heavy Wooden Carriage Cheeks (Left and Right)
        val cheekW = 2.0f
        val cheekH = 6.5f
        val cheekD = 14.0f

        builder.addBox(
            width = cheekW,
            height = cheekH,
            depth = cheekD,
            material = ProceduralMaterial.WOOD,
            tx = -4.0f,
            ty = 4.5f,
            tz = -1.0f
        )
        builder.addBox(
            width = cheekW,
            height = cheekH,
            depth = cheekD,
            material = ProceduralMaterial.WOOD,
            tx = 4.0f,
            ty = 4.5f,
            tz = -1.0f
        )

        // Level 2+ Armored side cheek plates
        if (level >= 2) {
            builder.addBox(0.6f, 7.0f, 12.0f, ProceduralMaterial.IRON, tx = -5.1f, ty = 4.5f, tz = -1.0f)
            builder.addBox(0.6f, 7.0f, 12.0f, ProceduralMaterial.IRON, tx = 5.1f, ty = 4.5f, tz = -1.0f)
        }

        // Iron Carriage Reinforcement Brackets & Pivot Trunnion Bearings
        builder.addCylinder(
            radius = 1.6f,
            height = 10.5f,
            material = ProceduralMaterial.IRON,
            segments = 8,
            ty = 6.0f,
            tz = 0.5f,
            rz = 90f
        )

        // 3. Two Carriage Spoked Wheels
        for (sx in listOf(-1f, 1f)) {
            val wx = sx * 6.5f
            builder.addCylinder(
                radius = 4.5f,
                height = 1.4f,
                material = ProceduralMaterial.WOOD,
                segments = 12,
                tx = wx,
                ty = 4.0f,
                tz = -1.0f,
                rz = 90f
            )
            builder.addCylinder(
                radius = 4.6f,
                height = 0.7f,
                material = ProceduralMaterial.IRON,
                segments = 12,
                tx = wx,
                ty = 4.0f,
                tz = -1.0f,
                rz = 90f
            )
        }

        // Level 3+ Heavy Frontal Gunshield
        if (level >= 3) {
            builder.addBox(
                width = 13.0f,
                height = 9.0f,
                depth = 1.2f,
                material = ProceduralMaterial.IRON,
                ty = 7.5f,
                tz = 3.0f
            )
            builder.addBox(
                width = 13.2f,
                height = 1.2f,
                depth = 1.4f,
                material = ProceduralMaterial.GOLD,
                ty = 11.5f,
                tz = 3.0f
            )
        }

        // 4. Heavy Metal Cannon Barrel(s)
        if (level >= 3) {
            // Twin Barrels!
            for (bx in listOf(-2.4f, 2.4f)) {
                // Rear Breech
                builder.addCylinder(2.2f, 6.0f, ProceduralMaterial.IRON, segments = 8, tx = bx, ty = 6.0f, tz = -3.5f, rx = 90f)
                builder.addSphere(1.4f, ProceduralMaterial.IRON, tx = bx, ty = 6.0f, tz = -6.8f)
                // Main Barrel
                builder.addCylinder(1.8f, 18.0f, ProceduralMaterial.IRON, segments = 8, tx = bx, ty = 6.0f, tz = 7.0f, rx = 90f)
                // Flared Muzzle
                builder.addCylinder(2.4f, 2.5f, ProceduralMaterial.GOLD, segments = 8, tx = bx, ty = 6.0f, tz = 16.5f, rx = 90f)
                builder.addCylinder(1.3f, 0.5f, ProceduralMaterial.BOMB_BLACK, segments = 8, tx = bx, ty = 6.0f, tz = 17.8f, rx = 90f)
            }
        } else {
            // Single Barrel (Level 1: 14f length, Level 2: 20f length with reinforced gold collar)
            val barrelLen = if (level == 2) 20.0f else 14.0f
            val muzzleZ = if (level == 2) 16.0f else 12.5f

            // Rear Breech
            builder.addCylinder(3.2f, 6.0f, ProceduralMaterial.IRON, segments = 10, ty = 6.0f, tz = -3.5f, rx = 90f)
            builder.addSphere(1.8f, ProceduralMaterial.IRON, ty = 6.0f, tz = -7.0f)

            // Main Tapered Barrel
            builder.addCylinder(2.6f, barrelLen, ProceduralMaterial.IRON, segments = 10, ty = 6.0f, tz = barrelLen * 0.5f - 2f, rx = 90f)

            // Flared Muzzle Ring
            val muzzleMat = if (level == 2) ProceduralMaterial.GOLD else ProceduralMaterial.IRON
            builder.addCylinder(3.3f, 2.2f, muzzleMat, segments = 10, ty = 6.0f, tz = muzzleZ, rx = 90f)

            // Hollow Bore Opening (Dark interior)
            builder.addCylinder(1.8f, 0.5f, ProceduralMaterial.BOMB_BLACK, segments = 8, ty = 6.0f, tz = muzzleZ + 1.1f, rx = 90f)
        }

        return builder.buildModel()
    }

    /**
     * Builds the Castle-Mounted Heavy Ballista.
     * Level 1: Single bolt rail and standard wooden limbs.
     * Level 2+: Double-limb tension arms, reinforced steel winch, dual heavy bolt tracks.
     */
    fun buildBallista(level: Int = 1): Model {
        val modelName = if (level <= 1) "ballista" else "ballista_lvl$level"
        val builder = ProceduralModelBuilder(modelName)

        // 1. Turntable Pedestal Base
        builder.addCylinder(
            radius = 6.0f,
            height = 2.0f,
            material = ProceduralMaterial.WOOD,
            ty = 1.0f
        )
        builder.addCylinder(
            radius = 2.5f,
            height = 4.0f,
            material = if (level >= 2) ProceduralMaterial.GOLD else ProceduralMaterial.IRON,
            ty = 4.0f
        )

        // 2. Main Guide Rail / Stock (pointing along +Z)
        builder.addBox(
            width = if (level >= 2) 4.8f else 3.5f,
            height = 2.5f,
            depth = 22.0f,
            material = ProceduralMaterial.DARK_WOOD,
            ty = 6.5f,
            tz = 2.0f
        )
        // Central Metal Arrow Channel / Track
        builder.addBox(
            width = 1.2f,
            height = 1.0f,
            depth = 20.0f,
            material = ProceduralMaterial.IRON,
            ty = 7.5f,
            tz = 2.0f
        )

        // 3. Front Bow Crosshead & Curved Wooden Bow Limbs
        builder.addBox(
            width = 8.0f,
            height = 3.0f,
            depth = 3.5f,
            material = ProceduralMaterial.IRON,
            ty = 6.5f,
            tz = 10.5f
        )

        // Left Bow Arm (angled backwards)
        builder.addBox(
            width = 12.0f,
            height = 2.0f,
            depth = 2.0f,
            material = ProceduralMaterial.LIGHT_WOOD,
            tx = -7.5f,
            ty = 6.5f,
            tz = 8.0f,
            ry = -25f
        )
        // Right Bow Arm (angled backwards)
        builder.addBox(
            width = 12.0f,
            height = 2.0f,
            depth = 2.0f,
            material = ProceduralMaterial.LIGHT_WOOD,
            tx = 7.5f,
            ty = 6.5f,
            tz = 8.0f,
            ry = 25f
        )

        // Level 2+ Double-limb reinforced tension arms
        if (level >= 2) {
            builder.addBox(12.0f, 1.8f, 1.8f, ProceduralMaterial.IRON, tx = -7.5f, ty = 8.5f, tz = 8.0f, ry = -25f)
            builder.addBox(12.0f, 1.8f, 1.8f, ProceduralMaterial.IRON, tx = 7.5f, ty = 8.5f, tz = 8.0f, ry = 25f)
        }

        // 4. Taut Bowstring (Two angled cable strands meeting in center)
        builder.addCylinder(
            radius = 0.25f,
            height = 12.5f,
            material = ProceduralMaterial.PLASTER,
            tx = -6.0f,
            ty = 6.5f,
            tz = 4.0f,
            rz = 80f,
            ry = -30f
        )
        builder.addCylinder(
            radius = 0.25f,
            height = 12.5f,
            material = ProceduralMaterial.PLASTER,
            tx = 6.0f,
            ty = 6.5f,
            tz = 4.0f,
            rz = -80f,
            ry = 30f
        )

        // 5. Massive Loaded Ballista Bolt
        // Shaft
        builder.addCylinder(
            radius = 0.6f,
            height = 18.0f,
            material = ProceduralMaterial.LIGHT_WOOD,
            segments = 6,
            ty = 7.8f,
            tz = 5.0f,
            rx = 90f
        )
        // Barbed Steel Arrowhead
        builder.addCone(
            radius = 1.4f,
            height = 4.0f,
            material = if (level >= 2) ProceduralMaterial.GOLD else ProceduralMaterial.IRON,
            segments = 6,
            ty = 7.8f,
            tz = 14.5f,
            rx = 90f
        )
        // Red Fletching Feathers
        builder.addBox(
            width = 2.2f,
            height = 1.8f,
            depth = 0.3f,
            material = ProceduralMaterial.RED_CLOTH,
            ty = 7.8f,
            tz = -3.0f
        )

        // 6. Rear Winding Winch & Handwheels
        builder.addCylinder(
            radius = 1.8f,
            height = 7.0f,
            material = ProceduralMaterial.IRON,
            segments = 8,
            ty = 6.5f,
            tz = -6.5f,
            rz = 90f
        )

        return builder.buildModel()
    }

    /**
     * Builds the Heavy Siege Catapult.
     */
    fun buildCatapult(): Model {
        val builder = ProceduralModelBuilder("weapon_catapult")

        val chassisW = 12.0f
        val chassisH = 2.5f
        val chassisD = 20.0f

        // 1. Heavy Wheeled Chassis Frame
        builder.addBox(chassisW, chassisH, chassisD, ProceduralMaterial.DARK_WOOD, ty = 3.0f)

        // 4 Wooden Wheels
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                val wx = sx * (chassisW * 0.5f + 1.2f)
                val wz = sz * (chassisD * 0.35f)
                builder.addCylinder(
                    radius = 3.5f,
                    height = 1.6f,
                    material = ProceduralMaterial.WOOD,
                    segments = 12,
                    tx = wx,
                    ty = 3.5f,
                    tz = wz,
                    rz = 90f
                )
                builder.addCylinder(
                    radius = 3.6f,
                    height = 0.7f,
                    material = ProceduralMaterial.IRON,
                    segments = 12,
                    tx = wx,
                    ty = 3.5f,
                    tz = wz,
                    rz = 90f
                )
            }
        }

        // 2. Upright Stanchions & Crossbar Pivot
        for (sx in listOf(-1f, 1f)) {
            val px = sx * (chassisW * 0.4f)
            builder.addBox(
                width = 2.2f,
                height = 12.0f,
                depth = 2.2f,
                material = ProceduralMaterial.WOOD,
                tx = px,
                ty = 9.0f,
                tz = 0f
            )
            // Diagonal structural brace
            builder.addBox(
                width = 1.8f,
                height = 12.5f,
                depth = 1.8f,
                material = ProceduralMaterial.DARK_WOOD,
                tx = px,
                ty = 8.0f,
                tz = -3.5f,
                rx = -30f
            )
        }

        // Horizontal Iron Pivot Axle
        builder.addCylinder(
            radius = 1.2f,
            height = chassisW + 2f,
            material = ProceduralMaterial.IRON,
            segments = 8,
            ty = 13.0f,
            tz = 0f,
            rz = 90f
        )

        // 3. Throwing Arm with Iron-Banded Stone Bucket
        // Arm beam
        builder.addBox(
            width = 2.0f,
            height = 20.0f,
            depth = 2.2f,
            material = ProceduralMaterial.LIGHT_WOOD,
            ty = 18.0f,
            tz = -1.5f,
            rx = -15f
        )

        // Heavy Counterweight at bottom of arm
        builder.addBox(
            width = 5.5f,
            height = 5.0f,
            depth = 5.0f,
            material = ProceduralMaterial.IRON,
            ty = 7.0f,
            tz = 2.0f
        )

        // Stone Throwing Bucket at top of arm
        builder.addBox(
            width = 5.5f,
            height = 3.5f,
            depth = 5.5f,
            material = ProceduralMaterial.DARK_WOOD,
            ty = 26.5f,
            tz = -4.0f
        )
        // Iron Straps on Bucket
        builder.addBox(
            width = 5.7f,
            height = 1.2f,
            depth = 5.7f,
            material = ProceduralMaterial.IRON,
            ty = 26.5f,
            tz = -4.0f
        )

        // Large Hewn Boulder inside bucket
        builder.addSphere(
            radius = 2.0f,
            material = ProceduralMaterial.STONE,
            rings = 4,
            slices = 6,
            tx = 0f,
            ty = 28.5f,
            tz = -4.0f
        )

        return builder.buildModel()
    }

    /**
     * Builds the Castle-Mounted Archer Tower with crenelations, sun roof, and animated archer figure.
     * Level 1: Open wooden parapet with 1 marksman archer.
     * Level 2+: Fortified stone turret with conical roof, arrow slits, and 2 marksmen!
     */
    fun buildArcherTower(level: Int = 1): Model {
        val modelName = if (level <= 1) "archer_tower" else "archer_tower_lvl$level"
        val builder = ProceduralModelBuilder(modelName)

        val towerR = 7.5f
        val towerH = 18.0f

        // 1. Stone Round Tower Shaft
        builder.addCylinder(
            radius = towerR,
            height = towerH,
            material = ProceduralMaterial.STONE,
            segments = 10,
            ty = towerH * 0.5f
        )

        // Arrow slit recesses
        for (i in 0..3) {
            val a = (i * 90f) * (PI.toFloat() / 180f)
            builder.addBox(
                width = 1.4f,
                height = 5.5f,
                depth = 1.2f,
                material = ProceduralMaterial.DARK_STONE,
                tx = cos(a) * (towerR - 0.2f),
                ty = 10f,
                tz = sin(a) * (towerR - 0.2f),
                ry = -i * 90f
            )
        }

        // 2. Flared Corbel Parapet Platform
        builder.addCylinder(
            radius = towerR * 1.22f,
            height = 3.0f,
            material = ProceduralMaterial.LIGHT_STONE,
            segments = 10,
            ty = towerH + 1.5f
        )

        // Wooden Platform Flooring
        builder.addCylinder(
            radius = towerR * 1.15f,
            height = 1.0f,
            material = ProceduralMaterial.WOOD,
            segments = 10,
            ty = towerH + 2.5f
        )

        // 3. Parapet Battlements / Merlons (8 around the perimeter)
        for (i in 0 until 8) {
            if (i % 2 == 0) {
                val a = (i.toFloat() / 8f) * 2f * PI.toFloat()
                val deg = Math.toDegrees(a.toDouble()).toFloat()
                builder.addBox(
                    width = 4.0f,
                    height = 3.5f,
                    depth = 1.6f,
                    material = ProceduralMaterial.LIGHT_STONE,
                    tx = cos(a) * (towerR * 1.15f),
                    ty = towerH + 4.5f,
                    tz = sin(a) * (towerR * 1.15f),
                    ry = -deg + 90f
                )
            }
        }

        // 4. 4 Timber Roof Posts
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(
                    width = 1.2f,
                    height = 10.0f,
                    depth = 1.2f,
                    material = ProceduralMaterial.DARK_WOOD,
                    tx = sx * 4.5f,
                    ty = towerH + 7.5f,
                    tz = sz * 4.5f
                )
            }
        }

        // 5. Pyramidal Timber / Slate Roof
        builder.addCone(
            radius = towerR * 1.35f,
            height = 9.0f,
            material = if (level >= 2) ProceduralMaterial.BLUE_ROOF else ProceduralMaterial.WOOD,
            segments = 8,
            ty = towerH + 14.5f
        )

        // Flagpole & Pennant
        builder.addCylinder(
            radius = 0.3f,
            height = 7.0f,
            material = ProceduralMaterial.GOLD,
            ty = towerH + 20f
        )
        builder.addBox(
            width = 5.0f,
            height = 2.5f,
            depth = 0.2f,
            material = ProceduralMaterial.RED_CLOTH,
            tx = 2.5f,
            ty = towerH + 21f
        )

        // 6. Archer Figures stationed on platform (1 for Level 1, 2 for Level 2+)
        val archerPositions = if (level >= 2) listOf(-2.2f, 2.2f) else listOf(0f)
        for (ax in archerPositions) {
            // Legs / Tunic
            builder.addBox(2.2f, 3.5f, 1.8f, ProceduralMaterial.LEAVES_OAK, tx = ax, ty = towerH + 4.2f)
            // Torso
            builder.addBox(2.6f, 3.2f, 2.0f, ProceduralMaterial.WOOD, tx = ax, ty = towerH + 6.5f)
            // Head / Archer Cap
            builder.addSphere(1.2f, ProceduralMaterial.FLESH, tx = ax, ty = towerH + 8.8f)
            builder.addCone(1.4f, 1.8f, ProceduralMaterial.LEAVES_OAK, segments = 6, tx = ax, ty = towerH + 9.8f)
            // Recurve Bow in hands
            builder.addCylinder(0.2f, 5.0f, ProceduralMaterial.DARK_WOOD, tx = ax + 1.8f, ty = towerH + 6.8f, tz = 1.2f, rx = 20f)
            // Arrow Quiver on back
            builder.addCylinder(0.8f, 3.5f, ProceduralMaterial.DARK_WOOD, tx = ax - 0.8f, ty = towerH + 7.0f, tz = -1.2f, rx = -20f)
        }

        return builder.buildModel()
    }
}
