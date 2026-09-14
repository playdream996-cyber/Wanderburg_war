package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Model
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural builders for medieval environment props, foliage, and resource pickups.
 */
object EnvironmentModelBuilders {

    /**
     * Stylized Oak Tree: Tapered trunk + 3 clustered rounded foliage canopy puffs.
     */
    fun buildOakTree(): Model {
        val builder = ProceduralModelBuilder("env_tree_oak")

        // Trunk
        builder.addCylinder(1.8f, 10f, ProceduralMaterial.WOOD, segments = 8, ty = 5f)
        builder.addCone(3.2f, 3.5f, ProceduralMaterial.DARK_WOOD, segments = 6, ty = 1.75f) // Root flare

        // Foliage Clusters
        builder.addSphere(6.5f, ProceduralMaterial.LEAVES_OAK, rings = 5, slices = 7, ty = 14f)
        builder.addSphere(5.2f, ProceduralMaterial.LEAVES_OAK, rings = 4, slices = 6, tx = -2.8f, ty = 12.5f, tz = 1.5f)
        builder.addSphere(5.0f, ProceduralMaterial.LEAVES_OAK, rings = 4, slices = 6, tx = 2.6f, ty = 13.0f, tz = -1.8f)
        builder.addSphere(4.2f, ProceduralMaterial.LEAVES_OAK, rings = 4, slices = 6, ty = 18.0f)

        return builder.buildModel()
    }

    /**
     * Stylized Pine Tree: Straight trunk + 3 tiered conical foliage layers.
     */
    fun buildPineTree(): Model {
        val builder = ProceduralModelBuilder("env_tree_pine")

        // Trunk
        builder.addCylinder(1.4f, 14f, ProceduralMaterial.DARK_WOOD, segments = 6, ty = 7f)

        // Tier 1 (Bottom)
        builder.addCone(7.5f, 9.0f, ProceduralMaterial.LEAVES_PINE, segments = 8, ty = 9.5f)
        // Tier 2 (Middle)
        builder.addCone(6.0f, 8.0f, ProceduralMaterial.LEAVES_PINE, segments = 8, ty = 14.0f)
        // Tier 3 (Top)
        builder.addCone(4.2f, 7.5f, ProceduralMaterial.LEAVES_PINE, segments = 8, ty = 18.5f)

        return builder.buildModel()
    }

    /**
     * Stylized Birch / Autumn Tree with golden amber foliage.
     */
    fun buildAutumnTree(): Model {
        val builder = ProceduralModelBuilder("env_tree_autumn")

        // Pale wood trunk
        builder.addCylinder(1.6f, 11f, ProceduralMaterial.LIGHT_WOOD, segments = 8, ty = 5.5f)
        builder.addCone(2.8f, 3f, ProceduralMaterial.WOOD, segments = 6, ty = 1.5f)

        // Warm golden foliage clusters
        builder.addSphere(6.0f, ProceduralMaterial.LEAVES_AUTUMN, rings = 5, slices = 7, ty = 15f)
        builder.addSphere(4.8f, ProceduralMaterial.LEAVES_AUTUMN, rings = 4, slices = 6, tx = -2.4f, ty = 13.5f, tz = 1.8f)
        builder.addSphere(4.6f, ProceduralMaterial.LEAVES_AUTUMN, rings = 4, slices = 6, tx = 2.2f, ty = 14.0f, tz = -1.6f)
        builder.addSphere(3.8f, ProceduralMaterial.LEAVES_AUTUMN, rings = 4, slices = 6, ty = 19.0f)

        return builder.buildModel()
    }

    /**
     * Stylized Gnarled Dead Tree (spooky twisted branches).
     */
    fun buildDeadTree(): Model {
        val builder = ProceduralModelBuilder("env_tree_dead")

        // Main twisted trunk
        builder.addCylinder(2.0f, 13f, ProceduralMaterial.DARK_WOOD, segments = 6, ty = 6.5f, rz = 8f)
        builder.addCone(3.6f, 3.5f, ProceduralMaterial.DARK_WOOD, segments = 6, ty = 1.75f)

        // Bare twisted limb branches
        builder.addCylinder(1.0f, 7.5f, ProceduralMaterial.DARK_WOOD, segments = 5, tx = -2.5f, ty = 12f, tz = 1f, rz = -40f, ry = 20f)
        builder.addCylinder(0.9f, 6.5f, ProceduralMaterial.DARK_WOOD, segments = 5, tx = 2.8f, ty = 13f, tz = -0.5f, rz = 45f, ry = -15f)
        builder.addCylinder(0.7f, 5.0f, ProceduralMaterial.DARK_WOOD, segments = 5, tx = -0.5f, ty = 15f, tz = -2.0f, rx = -35f)

        return builder.buildModel()
    }

    /**
     * Stylized Bush / Low foliage cluster.
     */
    fun buildBush(): Model {
        val builder = ProceduralModelBuilder("env_bush")
        builder.addSphere(3.8f, ProceduralMaterial.LEAVES_OAK, rings = 4, slices = 6, ty = 2.6f)
        builder.addSphere(2.8f, ProceduralMaterial.LEAVES_PINE, rings = 3, slices = 5, tx = 2.2f, ty = 2.0f, tz = 0.8f)
        builder.addSphere(2.6f, ProceduralMaterial.LEAVES_OAK, rings = 3, slices = 5, tx = -1.8f, ty = 1.8f, tz = -1.2f)
        return builder.buildModel()
    }

    /**
     * Stylized Haystack with pitchfork.
     */
    fun buildHaystack(): Model {
        val builder = ProceduralModelBuilder("prop_haystack")
        // Domed haystack body
        builder.addCylinder(4.5f, 3.0f, ProceduralMaterial.HAY, segments = 8, ty = 1.5f)
        builder.addCone(4.5f, 5.5f, ProceduralMaterial.HAY, segments = 8, ty = 5.75f)
        // Wooden pitchfork leaning against stack
        builder.addCylinder(0.25f, 7.5f, ProceduralMaterial.LIGHT_WOOD, segments = 5, tx = 3.2f, ty = 3.5f, tz = 2.2f, rz = -22f)
        builder.addBox(1.0f, 1.2f, 0.2f, ProceduralMaterial.IRON, tx = 4.4f, ty = 6.2f, tz = 2.2f, rz = -22f)
        return builder.buildModel()
    }

    /**
     * Stylized Wooden Post-and-Rail Fence segment.
     */
    fun buildFence(): Model {
        val builder = ProceduralModelBuilder("prop_fence")
        // 2 Posts
        builder.addBox(1.2f, 5.5f, 1.2f, ProceduralMaterial.WOOD, tx = -6f, ty = 2.75f)
        builder.addBox(1.2f, 5.5f, 1.2f, ProceduralMaterial.WOOD, tx = 6f, ty = 2.75f)
        // 2 Horizontal Rails
        builder.addBox(13.5f, 0.8f, 0.8f, ProceduralMaterial.LIGHT_WOOD, ty = 2.0f)
        builder.addBox(13.5f, 0.8f, 0.8f, ProceduralMaterial.LIGHT_WOOD, ty = 4.2f)
        return builder.buildModel()
    }

    /**
     * Stylized Road Direction Signpost.
     */
    fun buildSignPost(): Model {
        val builder = ProceduralModelBuilder("prop_signpost")
        // Vertical post
        builder.addBox(1.4f, 8.5f, 1.4f, ProceduralMaterial.WOOD, ty = 4.25f)
        // Upper arrow board pointing right
        builder.addBox(5.5f, 1.4f, 0.4f, ProceduralMaterial.LIGHT_WOOD, tx = 1.8f, ty = 7.0f, ry = 10f)
        // Lower arrow board pointing left
        builder.addBox(5.0f, 1.3f, 0.4f, ProceduralMaterial.LIGHT_WOOD, tx = -1.5f, ty = 5.2f, ry = -15f)
        return builder.buildModel()
    }

    /**
     * Stylized Broken Wooden Cart with wheel off.
     */
    fun buildBrokenCart(): Model {
        val builder = ProceduralModelBuilder("prop_broken_cart")
        // Tilted cart bed
        builder.addBox(9.0f, 1.2f, 14.0f, ProceduralMaterial.WOOD, ty = 2.8f, rz = 12f)
        // Side rails
        builder.addBox(0.8f, 3.0f, 14.0f, ProceduralMaterial.DARK_WOOD, tx = -4.0f, ty = 4.4f, rz = 12f)
        builder.addBox(0.8f, 3.0f, 14.0f, ProceduralMaterial.DARK_WOOD, tx = 4.0f, ty = 2.4f, rz = 12f)
        // Left wheel standing
        builder.addCylinder(3.5f, 1.0f, ProceduralMaterial.DARK_WOOD, segments = 10, tx = -4.8f, ty = 3.5f, tz = 0f, rz = 90f)
        // Broken right wheel lying flat on ground
        builder.addCylinder(3.5f, 0.8f, ProceduralMaterial.DARK_WOOD, segments = 10, tx = 6.2f, ty = 0.4f, tz = 2.5f)
        return builder.buildModel()
    }

    /**
     * Stylized Stacked Firewood Pile.
     */
    fun buildWoodPile(): Model {
        val builder = ProceduralModelBuilder("prop_wood_pile")
        // End posts
        builder.addBox(0.8f, 4.0f, 0.8f, ProceduralMaterial.WOOD, tx = -3.8f, ty = 2.0f, tz = -2f)
        builder.addBox(0.8f, 4.0f, 0.8f, ProceduralMaterial.WOOD, tx = -3.8f, ty = 2.0f, tz = 2f)
        builder.addBox(0.8f, 4.0f, 0.8f, ProceduralMaterial.WOOD, tx = 3.8f, ty = 2.0f, tz = -2f)
        builder.addBox(0.8f, 4.0f, 0.8f, ProceduralMaterial.WOOD, tx = 3.8f, ty = 2.0f, tz = 2f)
        // Stacked logs
        for (layer in 0..2) {
            val y = 0.7f + layer * 1.1f
            for (lx in -2..2) {
                builder.addCylinder(0.55f, 7.2f, ProceduralMaterial.LIGHT_WOOD, segments = 6,
                    tx = lx * 1.1f, ty = y, tz = 0f, rx = 90f)
            }
        }
        return builder.buildModel()
    }

    /**
     * Stylized Campfire with stones and embers.
     */
    fun buildCampfire(): Model {
        val builder = ProceduralModelBuilder("prop_campfire")
        // Stone ring
        for (i in 0 until 8) {
            val a = (i * 45f) * (PI.toFloat() / 180f)
            builder.addSphere(1.2f, ProceduralMaterial.STONE, rings = 3, slices = 5,
                tx = cos(a) * 3.6f, ty = 0.7f, tz = sin(a) * 3.6f)
        }
        // Teepee logs
        builder.addCylinder(0.5f, 4.0f, ProceduralMaterial.DARK_WOOD, segments = 5, ty = 1.5f, rz = 30f)
        builder.addCylinder(0.5f, 4.0f, ProceduralMaterial.DARK_WOOD, segments = 5, ty = 1.5f, rz = -30f)
        builder.addCylinder(0.5f, 4.0f, ProceduralMaterial.DARK_WOOD, segments = 5, ty = 1.5f, rx = 30f)
        // Glowing ember center
        builder.addSphere(1.4f, ProceduralMaterial.FIRE_EMBER, rings = 3, slices = 5, ty = 0.8f)
        return builder.buildModel()
    }

    /**
     * Stylized Tall Wooden Lantern Post.
     */
    fun buildLanternPost(): Model {
        val builder = ProceduralModelBuilder("prop_lantern_post")
        // Base and post
        builder.addBox(2.2f, 1.0f, 2.2f, ProceduralMaterial.STONE, ty = 0.5f)
        builder.addCylinder(0.8f, 12.0f, ProceduralMaterial.WOOD, segments = 6, ty = 6.5f)
        // Curved lamp arm
        builder.addBox(3.2f, 0.7f, 0.7f, ProceduralMaterial.IRON, tx = 1.4f, ty = 12.0f)
        // Hanging Lantern
        builder.addBox(1.5f, 2.2f, 1.5f, ProceduralMaterial.IRON, tx = 2.8f, ty = 10.4f)
        builder.addBox(1.1f, 1.6f, 1.1f, ProceduralMaterial.LANTERN_GLOW, tx = 2.8f, ty = 10.4f)
        return builder.buildModel()
    }

    /**
     * Stylized Training Dummy.
     */
    fun buildTrainingDummy(): Model {
        val builder = ProceduralModelBuilder("prop_training_dummy")
        // Base post & crossbar
        builder.addCylinder(0.7f, 8.0f, ProceduralMaterial.WOOD, segments = 6, ty = 4.0f)
        builder.addBox(8.0f, 0.8f, 0.8f, ProceduralMaterial.WOOD, ty = 6.0f)
        // Straw body sack
        builder.addCylinder(1.8f, 4.5f, ProceduralMaterial.HAY, segments = 8, ty = 4.5f)
        // Head with bucket helm
        builder.addCylinder(1.2f, 1.8f, ProceduralMaterial.IRON, segments = 6, ty = 7.6f)
        // Round straw target shield on arm
        builder.addCylinder(2.2f, 0.4f, ProceduralMaterial.LIGHT_WOOD, segments = 8, tx = -3.6f, ty = 6.0f, rz = 90f)
        builder.addCylinder(0.9f, 0.5f, ProceduralMaterial.RED_CLOTH, segments = 8, tx = -3.6f, ty = 6.0f, rz = 90f)
        return builder.buildModel()
    }

    /**
     * Stylized Crates & Barrels Cluster.
     */
    fun buildCratesAndBarrels(): Model {
        val builder = ProceduralModelBuilder("prop_crates_barrels")
        // Big Crate
        builder.addBox(3.4f, 3.4f, 3.4f, ProceduralMaterial.WOOD, tx = -1.6f, ty = 1.7f, tz = -1.0f)
        builder.addBox(3.5f, 0.4f, 3.5f, ProceduralMaterial.DARK_WOOD, tx = -1.6f, ty = 1.7f, tz = -1.0f)
        // Smaller Crate stacked
        builder.addBox(2.4f, 2.4f, 2.4f, ProceduralMaterial.LIGHT_WOOD, tx = -1.4f, ty = 4.6f, tz = -0.8f, ry = 15f)
        // Cider Barrel standing
        builder.addCylinder(1.8f, 3.6f, ProceduralMaterial.DARK_WOOD, segments = 8, tx = 2.4f, ty = 1.8f, tz = 0.5f)
        builder.addCylinder(1.85f, 0.4f, ProceduralMaterial.IRON, segments = 8, tx = 2.4f, ty = 1.2f, tz = 0.5f)
        builder.addCylinder(1.85f, 0.4f, ProceduralMaterial.IRON, segments = 8, tx = 2.4f, ty = 2.4f, tz = 0.5f)
        return builder.buildModel()
    }

    /**
     * Stylized Market Stall with canvas awning and food crates.
     */
    fun buildMarketStall(): Model {
        val builder = ProceduralModelBuilder("prop_market_stall")
        val w = 12f; val h = 9f; val d = 8f

        // 4 Timber corner posts
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                builder.addBox(0.9f, h, 0.9f, ProceduralMaterial.WOOD,
                    tx = sx * (w * 0.5f - 0.5f), ty = h * 0.5f, tz = sz * (d * 0.5f - 0.5f))
            }
        }
        // Wooden Table Counter
        builder.addBox(w - 0.5f, 1.0f, d * 0.6f, ProceduralMaterial.LIGHT_WOOD, ty = 3.6f, tz = 0.5f)
        // Fruit/Bread Crates on table
        builder.addBox(3.0f, 1.2f, 2.0f, ProceduralMaterial.WOOD, tx = -2.8f, ty = 4.5f, tz = 0.5f)
        builder.addBox(2.8f, 1.2f, 2.0f, ProceduralMaterial.DARK_WOOD, tx = 2.6f, ty = 4.5f, tz = 0.5f)

        // Slanted Canvas Awning (Alternating Red and White stripes look)
        builder.addBox(w + 2f, 0.4f, d + 2f, ProceduralMaterial.RED_ROOF, ty = h + 0.5f, rx = -12f)
        builder.addBox(w * 0.5f, 0.5f, d + 2f, ProceduralMaterial.WHITE_CLOTH, tx = 0f, ty = h + 0.55f, rx = -12f)

        return builder.buildModel()
    }

    /**
     * Stylized Ancient Stone Ruin / Crumbled Wall.
     */
    fun buildStoneRuin(): Model {
        val builder = ProceduralModelBuilder("prop_stone_ruin")
        // Jagged broken wall
        builder.addBox(14.0f, 7.0f, 3.2f, ProceduralMaterial.STONE, ty = 3.5f)
        builder.addBox(6.0f, 5.0f, 3.4f, ProceduralMaterial.DARK_STONE, tx = -3.5f, ty = 8.5f)
        // Fallen stone blocks on ground
        builder.addBox(3.0f, 2.0f, 2.6f, ProceduralMaterial.LIGHT_STONE, tx = 5.0f, ty = 1.0f, tz = 2.4f, ry = 25f)
        builder.addBox(2.4f, 1.6f, 2.0f, ProceduralMaterial.STONE, tx = -4.5f, ty = 0.8f, tz = -2.8f, ry = -35f)
        return builder.buildModel()
    }

    /**
     * Stylized Irregular Rock Boulder.
     */
    fun buildRock(variant: Int = 0): Model {
        val builder = ProceduralModelBuilder("env_rock_$variant")

        val scale = when (variant % 3) {
            0 -> 1.0f
            1 -> 1.5f
            else -> 0.7f
        }

        // Base boulder
        builder.addSphere(4.5f * scale, ProceduralMaterial.DARK_STONE, rings = 4, slices = 6, ty = 2.5f * scale)
        // Smaller intersecting rock facets
        builder.addBox(5.0f * scale, 3.5f * scale, 4.0f * scale, ProceduralMaterial.STONE,
            tx = 1.0f * scale, ty = 2.0f * scale, ry = 35f)
        builder.addBox(3.5f * scale, 2.8f * scale, 4.5f * scale, ProceduralMaterial.LIGHT_STONE,
            tx = -1.2f * scale, ty = 1.8f * scale, rz = -20f)

        return builder.buildModel()
    }

    /**
     * Resource Pickups:
     * 0 = Gold Coin, 1 = Wood Bundle, 2 = Stone Pile, 3 = Iron Ingot, 4 = Magic Crystal
     */
    fun buildResource(type: Int): Model {
        val name = when (type) {
            1 -> "pickup_wood"
            2 -> "pickup_stone"
            3 -> "pickup_iron"
            4 -> "pickup_crystal"
            else -> "pickup_gold"
        }
        val builder = ProceduralModelBuilder(name)

        when (type) {
            0 -> {
                // Gold Coin (Faceted Medallion)
                builder.addCylinder(2.5f, 0.6f, ProceduralMaterial.GOLD, segments = 12, rx = 90f)
                builder.addCylinder(1.8f, 0.75f, ProceduralMaterial.GOLD, segments = 10, rx = 90f)
            }
            1 -> {
                // Wood Logs Bundle
                for (angle in listOf(0f, 120f, 240f)) {
                    val a = angle * (PI.toFloat() / 180f)
                    builder.addCylinder(0.8f, 4.5f, ProceduralMaterial.WOOD, segments = 6,
                        tx = cos(a) * 0.7f, ty = sin(a) * 0.7f, rx = 90f)
                }
                // Rope tie around bundle
                builder.addCylinder(1.8f, 0.5f, ProceduralMaterial.LIGHT_WOOD, segments = 8, rx = 90f)
            }
            2 -> {
                // Stone Pile
                builder.addBox(2.2f, 1.4f, 2.0f, ProceduralMaterial.STONE, ty = 0.7f)
                builder.addBox(1.8f, 1.2f, 1.6f, ProceduralMaterial.DARK_STONE, tx = 0.8f, ty = 1.8f, ry = 25f)
                builder.addBox(1.5f, 1.0f, 1.4f, ProceduralMaterial.LIGHT_STONE, tx = -0.6f, ty = 1.6f, rz = -20f)
            }
            3 -> {
                // Iron Ingot (Trapezoidal Beveled Bar)
                builder.addBox(4.0f, 1.4f, 2.2f, ProceduralMaterial.IRON, ty = 0.7f)
                builder.addBox(3.4f, 0.4f, 1.8f, ProceduralMaterial.IRON, ty = 1.5f)
            }
            4 -> {
                // Magic Crystal (Floating Octahedron)
                builder.addCone(2.0f, 3.5f, ProceduralMaterial.MAGIC, segments = 6, ty = 1.75f)
                builder.addCone(2.0f, 3.5f, ProceduralMaterial.MAGIC, segments = 6, ty = -1.75f, rx = 180f)
            }
        }

        return builder.buildModel()
    }

    /**
     * Low-Poly Circular Blob Shadow for realistic ground contact.
     */
    fun buildBlobShadow(radius: Float): Model {
        val builder = ProceduralModelBuilder("blob_shadow")
        builder.addCylinder(radius, 0.05f, ProceduralMaterial.SHADOW, segments = 12, ty = 0.05f)
        return builder.buildModel()
    }

    /**
     * Stylized Medieval Siege Catapult / Trebuchet:
     * Heavy timber A-frame chassis with counterweight box, throwing arm, winch drum, and 4 chunky wheels.
     */
    fun buildSiegeCatapult(): Model {
        val builder = ProceduralModelBuilder("prop_catapult")
        val w = 18f; val l = 28f

        // Lower timber chassis beams
        builder.addBox(w, 2.2f, 2.5f, ProceduralMaterial.DARK_WOOD, ty = 3.5f, tz = -l * 0.45f)
        builder.addBox(w, 2.2f, 2.5f, ProceduralMaterial.DARK_WOOD, ty = 3.5f, tz = l * 0.45f)
        builder.addBox(2.5f, 2.2f, l, ProceduralMaterial.DARK_WOOD, tx = -w * 0.45f, ty = 3.5f)
        builder.addBox(2.5f, 2.2f, l, ProceduralMaterial.DARK_WOOD, tx = w * 0.45f, ty = 3.5f)

        // 4 Chunky spoke wooden wheels with iron rims
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-0.38f, 0.38f)) {
                val wx = sx * (w * 0.5f + 1.2f)
                val wz = sz * l
                builder.addCylinder(3.8f, 1.4f, ProceduralMaterial.WOOD, segments = 10, tx = wx, ty = 3.8f, tz = wz, rz = 90f)
                builder.addCylinder(4.0f, 0.3f, ProceduralMaterial.IRON, segments = 10, tx = wx, ty = 3.8f, tz = wz, rz = 90f)
            }
        }

        // A-Frame upright stanchions
        val aH = 22f
        for (sx in listOf(-1f, 1f)) {
            val ax = sx * (w * 0.38f)
            builder.addBox(2.0f, aH, 2.0f, ProceduralMaterial.WOOD, tx = ax, ty = aH * 0.5f + 3f, tz = -3f, rx = 12f)
            builder.addBox(2.0f, aH, 2.0f, ProceduralMaterial.WOOD, tx = ax, ty = aH * 0.5f + 3f, tz = 3f, rx = -12f)
        }
        // Heavy iron axle
        builder.addCylinder(1.2f, w + 2f, ProceduralMaterial.IRON, segments = 8, ty = aH + 2f, rz = 90f)

        // Tilted throwing beam
        builder.addBox(2.4f, 2.4f, 32f, ProceduralMaterial.DARK_WOOD, ty = aH + 2f, rx = 28f)
        // Heavy stone counterweight box at short end
        builder.addBox(7.5f, 8.5f, 7.5f, ProceduralMaterial.DARK_STONE, ty = aH - 4f, tz = -11f)
        builder.addBox(8.0f, 1.2f, 8.0f, ProceduralMaterial.IRON, ty = aH - 4f, tz = -11f)
        // Sling cup with boulder at long end
        builder.addBox(4.5f, 1.2f, 4.5f, ProceduralMaterial.LIGHT_WOOD, ty = aH + 11f, tz = 13f, rx = 28f)
        builder.addSphere(2.2f, ProceduralMaterial.STONE, rings = 4, slices = 6, ty = aH + 13f, tz = 13f)

        return builder.buildModel()
    }

    /**
     * Stylized Siege Ballista:
     * Massive mounted crossbow on pivoting swivel mount, armed with giant spear bolt.
     */
    fun buildSiegeBallista(): Model {
        val builder = ProceduralModelBuilder("prop_ballista")

        // Tripod timber base
        builder.addCylinder(1.5f, 10f, ProceduralMaterial.DARK_WOOD, segments = 6, ty = 5f)
        for (i in 0..2) {
            val a = (i * 120f) * (PI.toFloat() / 180f)
            builder.addBox(1.6f, 9f, 1.6f, ProceduralMaterial.WOOD,
                tx = cos(a) * 4f, ty = 4f, tz = sin(a) * 4f, rz = -25f, ry = -i * 120f)
        }

        // Swivel stock & flight trough
        builder.addBox(3.0f, 2.0f, 24f, ProceduralMaterial.WOOD, ty = 11.5f, rx = -10f)
        builder.addBox(3.4f, 0.8f, 24f, ProceduralMaterial.IRON, ty = 12.6f, rx = -10f)

        // Chunky recurved wooden bow limbs
        builder.addBox(26f, 2.0f, 2.2f, ProceduralMaterial.DARK_WOOD, ty = 12.5f, tz = 9f, rx = -10f)
        // Iron limb tips & bowstring
        builder.addBox(2.2f, 3.0f, 2.2f, ProceduralMaterial.IRON, tx = -13f, ty = 12.5f, tz = 8.5f)
        builder.addBox(2.2f, 3.0f, 2.2f, ProceduralMaterial.IRON, tx = 13f, ty = 12.5f, tz = 8.5f)

        // Loaded giant iron-tipped ballista bolt
        builder.addCylinder(0.7f, 20f, ProceduralMaterial.LIGHT_WOOD, segments = 6, ty = 13.2f, rx = 80f)
        builder.addCone(1.6f, 4.5f, ProceduralMaterial.IRON, segments = 6, ty = 14.8f, tz = 9.5f, rx = -100f)

        return builder.buildModel()
    }

    /**
     * Stylized Military Siege Tent / Commander Pavilion:
     * Red and gold trimmed conical bell tent with center spear pole and pennant.
     */
    fun buildSiegeTent(): Model {
        val builder = ProceduralModelBuilder("prop_siege_tent")
        val r = 13f; val wallH = 8f; val roofH = 14f

        // Cylindrical canvas walls
        builder.addCylinder(r, wallH, ProceduralMaterial.WHITE_CLOTH, segments = 10, ty = wallH * 0.5f)
        // Red heraldic scalloped bottom trim
        builder.addCylinder(r + 0.3f, 1.8f, ProceduralMaterial.WAR_BANNER_RED, segments = 10, ty = 1.0f)

        // Conical canvas roof
        builder.addCone(r * 1.15f, roofH, ProceduralMaterial.WAR_BANNER_RED, segments = 10, ty = wallH + roofH * 0.5f)

        // Center wooden spear pole protruding through top
        builder.addCylinder(0.8f, wallH + roofH + 6f, ProceduralMaterial.WOOD, segments = 6, ty = (wallH + roofH + 6f) * 0.5f)
        // Gold spearhead finial
        builder.addCone(1.2f, 3.0f, ProceduralMaterial.GOLD, segments = 6, ty = wallH + roofH + 6f)
        // Fluttering crimson pennant flag
        builder.addBox(5.5f, 2.2f, 0.2f, ProceduralMaterial.WAR_BANNER_RED, tx = 2.8f, ty = wallH + roofH + 4.5f)

        // Front tent door opening
        builder.addBox(4.5f, 6.5f, 0.4f, ProceduralMaterial.DARK_WOOD, ty = 3.25f, tz = r - 0.2f)

        return builder.buildModel()
    }

    /**
     * Stylized Defensive Wooden Barricade / Spiked Chevaux-de-frise:
     * Crossed sharpened logs bound with iron, blocking roads and encampments.
     */
    fun buildBarricade(): Model {
        val builder = ProceduralModelBuilder("prop_barricade")
        val w = 22f

        // Central horizontal timber beam
        builder.addBox(w, 2.0f, 2.0f, ProceduralMaterial.DARK_WOOD, ty = 4.5f)
        builder.addBox(w + 0.4f, 2.2f, 0.6f, ProceduralMaterial.IRON, ty = 4.5f) // Iron bracing bands

        // 5 Crossed pairs of sharpened spikes
        val numPairs = 5
        val step = w / numPairs
        for (i in 0 until numPairs) {
            val px = -w * 0.5f + (i + 0.5f) * step
            // Spike 1 (slanted forward)
            builder.addCylinder(0.9f, 10f, ProceduralMaterial.WOOD, segments = 6, tx = px, ty = 4.5f, rx = 40f)
            builder.addCone(0.9f, 2.8f, ProceduralMaterial.LIGHT_WOOD, segments = 6, tx = px, ty = 8.5f, tz = 3.6f, rx = 40f)
            // Spike 2 (slanted backward)
            builder.addCylinder(0.9f, 10f, ProceduralMaterial.WOOD, segments = 6, tx = px, ty = 4.5f, rx = -40f)
            builder.addCone(0.9f, 2.8f, ProceduralMaterial.LIGHT_WOOD, segments = 6, tx = px, ty = 8.5f, tz = -3.6f, rx = -40f)
        }

        return builder.buildModel()
    }

    /**
     * Stylized Weapon Rack:
     * Timber rack loaded with spears, halberds, and shields.
     */
    fun buildWeaponRack(): Model {
        val builder = ProceduralModelBuilder("prop_weapon_rack")
        val w = 12f; val h = 9f

        // 2 Side A-Frame posts
        for (sx in listOf(-1f, 1f)) {
            val x = sx * (w * 0.5f - 0.6f)
            builder.addBox(0.9f, h, 1.2f, ProceduralMaterial.DARK_WOOD, tx = x, ty = h * 0.5f)
            builder.addBox(0.9f, 1.2f, 5.0f, ProceduralMaterial.DARK_WOOD, tx = x, ty = 0.6f)
        }
        // Top and bottom cross bars
        builder.addBox(w, 0.8f, 0.8f, ProceduralMaterial.WOOD, ty = 2.5f)
        builder.addBox(w, 0.8f, 0.8f, ProceduralMaterial.WOOD, ty = 7.0f)

        // 4 Slanted spears/halberds
        for (i in 0..3) {
            val sx = -4.2f + i * 2.8f
            builder.addCylinder(0.35f, 13f, ProceduralMaterial.LIGHT_WOOD, segments = 5, tx = sx, ty = 6.5f, tz = 0.3f, rz = 5f)
            builder.addCone(0.8f, 2.5f, ProceduralMaterial.IRON, segments = 5, tx = sx - 0.5f, ty = 13.5f, tz = 0.3f)
        }
        // Round shield hung on side
        builder.addCylinder(2.6f, 0.5f, ProceduralMaterial.WAR_BANNER_RED, segments = 8, tx = w * 0.5f + 0.3f, ty = 5f, rz = 90f)
        builder.addSphere(0.9f, ProceduralMaterial.IRON, rings = 3, slices = 5, tx = w * 0.5f + 0.6f, ty = 5f)

        return builder.buildModel()
    }

    /**
     * Stylized Farmland Crop Field:
     * Raised dark tilled furrow soil beds with rows of ripe golden wheat or crisp green vegetables.
     */
    fun buildCropField(variant: Int = 0): Model {
        val name = if (variant == 0) "farm_crop_wheat" else "farm_crop_veggie"
        val builder = ProceduralModelBuilder(name)
        val w = 32f; val d = 36f

        // Dark tilled furrow earth bed
        builder.addBox(w, 1.2f, d, ProceduralMaterial.DIRT, ty = 0.6f)

        val cropMat = if (variant == 0) ProceduralMaterial.CROPS_WHEAT else ProceduralMaterial.CROPS_VEGGIE
        // 5 Raised furrow mounds with plants
        for (i in 0..4) {
            val cz = -d * 0.4f + i * (d * 0.2f)
            // Soil ridge
            builder.addBox(w - 2f, 0.6f, 3.2f, ProceduralMaterial.DARK_WOOD, ty = 1.4f, tz = cz)

            // Crop clusters along each row
            for (j in 0..6) {
                val cx = -w * 0.42f + j * (w * 0.14f)
                if (variant == 0) {
                    // Golden wheat sheaves
                    builder.addCylinder(1.2f, 3.5f, cropMat, segments = 6, tx = cx, ty = 3.0f, tz = cz)
                    builder.addCone(1.6f, 2.0f, cropMat, segments = 6, tx = cx, ty = 5.2f, tz = cz)
                } else {
                    // Cabbage / veggie domes
                    builder.addSphere(1.4f, cropMat, rings = 3, slices = 5, tx = cx, ty = 2.0f, tz = cz)
                }
            }
        }

        // Weathered post fence around borders
        for (sx in listOf(-1f, 1f)) {
            builder.addBox(1.0f, 4.0f, d + 2f, ProceduralMaterial.LIGHT_WOOD, tx = sx * (w * 0.5f + 0.5f), ty = 2f)
        }

        return builder.buildModel()
    }

    /**
     * Stylized Farm Shed / Tool Barn:
     * Open lean-to wooden shed with log pile, scythe, and hay bales.
     */
    fun buildFarmShed(): Model {
        val builder = ProceduralModelBuilder("farm_shed")
        val w = 24f; val h = 14f; val d = 16f

        // 4 Timber corner posts
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                val pz = sz * (d * 0.5f - 0.8f)
                val ph = if (sz < 0) h else h - 3.5f // Slanted roof slope
                builder.addBox(1.5f, ph, 1.5f, ProceduralMaterial.DARK_WOOD,
                    tx = sx * (w * 0.5f - 0.8f), ty = ph * 0.5f, tz = pz)
            }
        }

        // Back and left board walls
        builder.addBox(w - 1.5f, h - 1.5f, 0.8f, ProceduralMaterial.WOOD, ty = (h - 1.5f) * 0.5f, tz = -d * 0.5f + 0.4f)
        builder.addBox(0.8f, h - 2f, d - 1.5f, ProceduralMaterial.WOOD, tx = -w * 0.5f + 0.4f, ty = (h - 2f) * 0.5f)

        // Sloped cedar-shingle shed roof with generous overhang
        builder.addBox(w + 3.5f, 1.2f, d + 4.5f, ProceduralMaterial.RED_ROOF, ty = h + 0.5f, rx = 14f)

        // Sheltered interior items: Hay bale, stack of logs, and tool box
        builder.addBox(5.0f, 3.5f, 4.0f, ProceduralMaterial.HAY, tx = 4.5f, ty = 1.75f, tz = -2.5f)
        builder.addBox(6.0f, 2.5f, 3.5f, ProceduralMaterial.LIGHT_WOOD, tx = -4.5f, ty = 1.25f, tz = -2.5f)

        return builder.buildModel()
    }

    /**
     * Stylized The Great Elder Oak (World Landmark):
     * Colossal ancient tree with twisted gnarled trunk and massive low-poly tiered foliage domes.
     */
    fun buildElderOakTree(): Model {
        val builder = ProceduralModelBuilder("tree_elder_oak")

        // Massive flared base & roots
        builder.addCone(12.0f, 12.0f, ProceduralMaterial.DARK_WOOD, segments = 8, ty = 6.0f)
        builder.addCylinder(6.5f, 32.0f, ProceduralMaterial.WOOD, segments = 8, ty = 18.0f)

        // 4 Massive spreading limb boughs
        for (i in 0..3) {
            val a = (i * 90f + 45f) * (PI.toFloat() / 180f)
            val bx = cos(a) * 12f
            val bz = sin(a) * 12f
            builder.addCylinder(3.2f, 18.0f, ProceduralMaterial.DARK_WOOD, segments = 6,
                tx = bx * 0.6f, ty = 30.0f, tz = bz * 0.6f, rz = cos(a) * 45f, rx = sin(a) * 45f)

            // Massive foliage puffs on each bough
            builder.addSphere(14.0f, ProceduralMaterial.LEAVES_OAK, rings = 5, slices = 8,
                tx = bx * 1.3f, ty = 36.0f, tz = bz * 1.3f)
        }

        // Giant central crowning canopy
        builder.addSphere(18.0f, ProceduralMaterial.LEAVES_OAK, rings = 6, slices = 9, ty = 46.0f)
        builder.addSphere(12.0f, ProceduralMaterial.LEAVES_AUTUMN, rings = 4, slices = 7, ty = 52.0f)

        return builder.buildModel()
    }

    /**
     * Stylized Arched Stone Bridge:
     * Heavy stone bridge with double arch barrels, paved roadway deck, cutwater stone piers, and parapets.
     */
    fun buildStoneBridge(): Model {
        val builder = ProceduralModelBuilder("stone_bridge")
        val w = 110f; val spanL = 80f; val deckH = 14f

        // Central and shore stone pier abutments
        builder.addBox(w, deckH, 16f, ProceduralMaterial.DARK_STONE, ty = deckH * 0.5f, tz = -spanL * 0.45f)
        builder.addBox(w, deckH, 16f, ProceduralMaterial.DARK_STONE, ty = deckH * 0.5f, tz = spanL * 0.45f)
        builder.addBox(w, deckH - 2f, 14f, ProceduralMaterial.STONE, ty = (deckH - 2f) * 0.5f, tz = 0f) // Center pier

        // Paved Roadway Deck
        builder.addBox(w, 2.5f, spanL + 4f, ProceduralMaterial.COBBLESTONE, ty = deckH + 1.25f)

        // Heavy stone parapet guardrails with decorative pedestals
        val railH = 4.5f
        for (sx in listOf(-1f, 1f)) {
            val rx = sx * (w * 0.5f - 1.8f)
            builder.addBox(3.5f, railH, spanL + 4f, ProceduralMaterial.STONE, tx = rx, ty = deckH + 2.5f + railH * 0.5f)

            // Corner lantern / banner pedestals
            for (sz in listOf(-1f, 1f)) {
                val pz = sz * (spanL * 0.5f)
                builder.addBox(5.0f, railH + 3f, 5.0f, ProceduralMaterial.LIGHT_STONE, tx = rx, ty = deckH + 2.5f + (railH + 3f) * 0.5f, tz = pz)
                builder.addBox(2.2f, 3.5f, 2.2f, ProceduralMaterial.IRON, tx = rx, ty = deckH + railH + 6f, tz = pz)
                builder.addBox(1.5f, 2.2f, 1.5f, ProceduralMaterial.LANTERN_GLOW, tx = rx, ty = deckH + railH + 6f, tz = pz)
            }
        }

        return builder.buildModel()
    }

    /**
     * Stylized Burned Cart & Siege Debris (Environmental storytelling):
     * Charred timber frame, broken burning wheels, fallen shields, and scorched rubble.
     */
    fun buildBurnedCart(): Model {
        val builder = ProceduralModelBuilder("prop_burned_cart")

        // Smashed charred cart bed tilted into dirt
        builder.addBox(10.0f, 1.2f, 14.0f, ProceduralMaterial.CHARRED_WOOD, ty = 1.8f, rz = 18f, ry = 25f)
        // Broken burning wheel with ember glow
        builder.addCylinder(3.6f, 0.8f, ProceduralMaterial.CHARRED_WOOD, segments = 8, tx = -5.5f, ty = 2.4f, tz = 1f, rz = 75f)
        builder.addSphere(1.2f, ProceduralMaterial.FIRE_EMBER, rings = 3, slices = 5, tx = -5.5f, ty = 2.4f, tz = 1f)
        // Second wheel splintered on ground
        builder.addBox(4.5f, 0.6f, 1.2f, ProceduralMaterial.CHARRED_WOOD, tx = 5.0f, ty = 0.4f, tz = -2.5f, ry = 40f)

        // Fallen spiked barricade fragments & iron bolts
        builder.addBox(8.0f, 0.9f, 0.9f, ProceduralMaterial.CHARRED_WOOD, tx = 0f, ty = 0.6f, tz = 4.5f, ry = -30f)
        builder.addCone(0.8f, 2.4f, ProceduralMaterial.IRON, segments = 5, tx = 3.5f, ty = 0.6f, tz = 4.5f, rx = 90f)

        return builder.buildModel()
    }

    /**
     * Stylized Large Faceted Rock Formation:
     * Massive geometric crag boulders that form natural borders and arena obstacles.
     */
    fun buildRockFormation(): Model {
        val builder = ProceduralModelBuilder("rock_formation")

        // Main massive angular crag
        builder.addBox(16f, 14f, 14f, ProceduralMaterial.DARK_STONE, ty = 7f, ry = 22f)
        builder.addBox(12f, 18f, 10f, ProceduralMaterial.STONE, tx = 2f, ty = 9f, tz = -2f, ry = -15f)
        builder.addBox(14f, 10f, 12f, ProceduralMaterial.LIGHT_STONE, tx = -4f, ty = 5f, tz = 3f, rz = 18f)

        // Flanking boulder slabs
        builder.addBox(8f, 6f, 9f, ProceduralMaterial.STONE, tx = 10f, ty = 3f, tz = 4f, ry = 45f)
        builder.addBox(9f, 7f, 7f, ProceduralMaterial.DARK_STONE, tx = -9f, ty = 3.5f, tz = -4f, ry = -35f)

        return builder.buildModel()
    }
}
