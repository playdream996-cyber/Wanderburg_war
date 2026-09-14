package com.game.castlewar.render

import android.content.Context
import com.game.castlewar.buildings.Building
import com.game.castlewar.buildings.BuildingType
import com.game.castlewar.effects.Particle
import com.game.castlewar.effects.ParticleType
import com.game.castlewar.enemies.Enemy
import com.game.castlewar.enemies.EnemyArcher
import com.game.castlewar.enemies.EnemyBomber
import com.game.castlewar.enemies.EnemyBoss
import com.game.castlewar.enemies.EnemyKnight
import com.game.castlewar.enemies.EnemyMelee
import com.game.castlewar.entities.MovingCastle
import com.game.castlewar.model3d.GameAssetManager
import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.MatrixUtils
import com.game.castlewar.model3d.ModelRenderer
import com.game.castlewar.model3d.TerrainRenderer3D
import com.game.castlewar.model3d.Transform
import com.game.castlewar.resources.ResourcePickup
import com.game.castlewar.resources.ResourceType
import com.game.castlewar.weapons.ArcherTowerWeapon
import com.game.castlewar.weapons.BallistaWeapon
import com.game.castlewar.weapons.CannonWeapon
import com.game.castlewar.weapons.Projectile
import com.game.castlewar.weapons.ProjectileType
import com.game.castlewar.world.GameWorld
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Full 3D World Renderer for Castle War.
 * Renders stylized 3D terrain, environmental props, medieval settlements,
 * the moving fortress with rotating wheels and dynamic cannons, and 3D enemies.
 */
class WorldRenderer(context: Context) {

    private val assetManager = GameAssetManager(context)
    private val modelRenderer = ModelRenderer()
    private val terrainRenderer = TerrainRenderer3D()
    private val billboardRenderer = Renderer2D()

    // Reusable matrices and transforms to guarantee zero allocations during draw calls
    private val tempTransform = Transform()
    private val tempModelMatrix = FloatArray(16)
    private val screenPos = FloatArray(2)

    // Environmental 3D props (Trees & Rocks)
    private data class EnvProp(
        val modelId: String,
        val x: Float,
        val z: Float,
        val scale: Float,
        val rotYDeg: Float
    )
    private val envProps = mutableListOf<EnvProp>()

    init {
        generateEnvironmentalProps()
    }

    private fun generateEnvironmentalProps() {
        val rng = Random(42) // Deterministic world seed

        // 1. Clustered Forest Areas (dense clumps of Oak, Pine, Autumn trees, and bushes)
        val forestCenters = listOf(
            Pair(-1000f, -600f),
            Pair(-700f, 600f),
            Pair(700f, -800f),
            Pair(800f, 600f),
            Pair(-1100f, 100f),
            Pair(1000f, 200f)
        )
        for (fc in forestCenters) {
            for (i in 0 until 24) {
                val ox = (rng.nextFloat() - 0.5f) * 450f
                val oz = (rng.nextFloat() - 0.5f) * 450f
                val x = fc.first + ox
                val z = fc.second + oz
                val scale = 0.8f + rng.nextFloat() * 0.55f
                val rot = rng.nextFloat() * 360f
                val modelId = when (rng.nextInt(5)) {
                    0, 1 -> "tree_01"
                    2 -> "tree_pine"
                    3 -> "tree_autumn"
                    else -> "bush"
                }
                envProps.add(EnvProp(modelId, x, z, scale, rot))
            }
        }

        // 2. Traversal Trees and Foliage scattered along hills
        for (i in 0 until 100) {
            val x = (rng.nextFloat() - 0.5f) * 3800f
            val z = (rng.nextFloat() - 0.5f) * 3800f
            if (kotlin.math.abs(x) < 80f && kotlin.math.abs(z) < 80f) continue
            val scale = 0.75f + rng.nextFloat() * 0.45f
            val rot = rng.nextFloat() * 360f
            val modelId = when (rng.nextInt(4)) {
                0 -> "tree_01"
                1 -> "tree_pine"
                2 -> "tree_autumn"
                else -> "bush"
            }
            envProps.add(EnvProp(modelId, x, z, scale, rot))
        }

        // 3. Rocky Patches & Boulders (Crest of hills and quarry zones)
        val rockCenters = listOf(
            Pair(750f, 500f),
            Pair(-850f, -450f),
            Pair(600f, -600f),
            Pair(-300f, 900f)
        )
        for (rc in rockCenters) {
            for (i in 0 until 14) {
                val rx = rc.first + (rng.nextFloat() - 0.5f) * 350f
                val rz = rc.second + (rng.nextFloat() - 0.5f) * 350f
                val scale = 0.8f + rng.nextFloat() * 0.8f
                val rot = rng.nextFloat() * 360f
                val modelId = if (rng.nextBoolean()) "rock_01" else "rock_02"
                envProps.add(EnvProp(modelId, rx, rz, scale, rot))
            }
        }

        // 4. Dead Trees & Ruins Debris near ancient sites
        for (i in 0 until 8) {
            val dx = 850f + (rng.nextFloat() - 0.5f) * 260f
            val dz = -100f + (rng.nextFloat() - 0.5f) * 260f
            envProps.add(EnvProp("tree_dead", dx, dz, 1.0f + rng.nextFloat() * 0.4f, rng.nextFloat() * 360f))
        }

        // 5. Roadside Details & Wayfinding Props
        val roadProps = listOf(
            // Central Crossroads signpost & lanterns
            EnvProp("signpost", 70f, 60f, 1.1f, 15f),
            EnvProp("lantern_post", -65f, 65f, 1.1f, 0f),
            EnvProp("lantern_post", 65f, -65f, 1.1f, 180f),
            EnvProp("broken_cart", -75f, -120f, 1.0f, 35f),

            // Riverbend Village & Blacksmith Props
            EnvProp("fence", -340f, -190f, 1.1f, 0f),
            EnvProp("fence", -420f, -190f, 1.1f, 0f),
            EnvProp("crates_barrels", -320f, -240f, 1.2f, 25f),
            EnvProp("lantern_post", -300f, -230f, 1.1f, -40f),
            EnvProp("wood_pile", -410f, -240f, 1.1f, 10f),
            EnvProp("weapon_rack", -360f, -170f, 1.15f, 90f),

            // Stone Bridge Landmark Props (0f, -650f)
            EnvProp("lantern_post", -40f, -600f, 1.2f, 0f),
            EnvProp("lantern_post", 40f, -600f, 1.2f, 0f),
            EnvProp("lantern_post", -40f, -700f, 1.2f, 180f),
            EnvProp("lantern_post", 40f, -700f, 1.2f, 180f),
            EnvProp("rock_formation_01", -90f, -640f, 1.2f, 45f),
            EnvProp("rock_formation_01", 90f, -660f, 1.15f, -30f),

            // Farmstead Props
            EnvProp("haystack", -430f, 440f, 1.25f, 0f),
            EnvProp("haystack", -480f, 480f, 1.1f, 40f),
            EnvProp("fence", -420f, 360f, 1.1f, 80f),
            EnvProp("fence", -520f, 380f, 1.1f, -20f),
            EnvProp("broken_cart", -370f, 370f, 1.0f, -15f),

            // Garrison & Outpost Props
            EnvProp("training_dummy", 440f, -250f, 1.2f, -30f),
            EnvProp("training_dummy", 460f, -230f, 1.2f, -15f),
            EnvProp("weapon_rack", 420f, -270f, 1.2f, 0f),
            EnvProp("campfire", 380f, -280f, 1.2f, 0f),
            EnvProp("crates_barrels", 420f, -320f, 1.2f, 45f),
            EnvProp("wood_pile", 340f, -270f, 1.1f, 0f),
            EnvProp("lantern_post", 410f, -200f, 1.1f, 90f),

            // Northern Fort & Forward Siege Camp Props
            EnvProp("training_dummy", 530f, 420f, 1.2f, 45f),
            EnvProp("campfire", 480f, 410f, 1.2f, 0f),
            EnvProp("wood_pile", 540f, 450f, 1.1f, 20f),
            EnvProp("weapon_rack", 510f, 470f, 1.2f, -35f),
            EnvProp("rock_formation_01", 620f, 480f, 1.3f, 60f),
            EnvProp("rock_formation_01", -380f, 920f, 1.4f, -45f),
            EnvProp("campfire", -370f, 830f, 1.2f, 0f),
            EnvProp("weapon_rack", -330f, 850f, 1.2f, 45f),

            // Eldermoor Ruins Props
            EnvProp("campfire", 860f, -90f, 1.2f, 0f),
            EnvProp("crates_barrels", 820f, -80f, 1.1f, -25f),
            EnvProp("broken_cart", 880f, -140f, 1.0f, 60f),
            EnvProp("rock_formation_01", 920f, -80f, 1.35f, 25f)
        )
        envProps.addAll(roadProps)
    }

    fun initialize() {
        modelRenderer.initialize()
        terrainRenderer.initializeGL()
        billboardRenderer.initialize()
    }

    fun render(world: GameWorld, camera: GameCamera) {
        val camX = camera.positionX
        val camZ = camera.positionY // 2D Y is 3D Z

        // -------------------------------------------------------------
        // 1. 3D PASS: Shaded, lit meshes with depth testing
        // -------------------------------------------------------------
        modelRenderer.begin(camera.viewMatrix, camera.projectionMatrix, camera.eyeX, camera.eyeY, camera.eyeZ)

        // 1.1 Terrain Chunks
        terrainRenderer.render(modelRenderer, camX, camZ)

        // 1.2 Environmental Props (Trees & Rocks) with Distance Culling
        modelRenderer.setLightingProfile(ModelRenderer.PROFILE_DEFAULT)
        renderEnvironmentProps(camX, camZ)

        // 1.3 Medieval Buildings & Settlements
        modelRenderer.setLightingProfile(ModelRenderer.PROFILE_BUILDING)
        for (b in world.buildings) {
            renderBuilding3D(b, camX, camZ)
        }

        // 1.4 Resource Pickups
        modelRenderer.setLightingProfile(ModelRenderer.PROFILE_PICKUP)
        for (p in world.pickups) {
            if (p.isActive) {
                renderPickup3D(p, camX, camZ)
            }
        }

        // 1.5 Enemies
        modelRenderer.setLightingProfile(ModelRenderer.PROFILE_CHARACTER)
        for (enemy in world.enemies) {
            if (enemy.isAlive) {
                renderEnemy3D(enemy, camX, camZ)
            }
        }

        // 1.6 Player Moving Castle (Hull, Rotating Wheels, Dynamic Weapons)
        modelRenderer.setLightingProfile(ModelRenderer.PROFILE_FORTRESS)
        renderCastle3D(world.castle)

        // 1.7 3D Projectiles
        modelRenderer.setLightingProfile(ModelRenderer.PROFILE_PICKUP)
        for (proj in world.projectileManager.pool) {
            if (proj.isActive) {
                renderProjectile3D(proj)
            }
        }

        modelRenderer.end()

        // -------------------------------------------------------------
        // 2. 2D BILLBOARD & HUD PASS: Health bars, floating numbers, particles
        // -------------------------------------------------------------
        billboardRenderer.begin(camera)

        // Render health bars above damaged buildings
        for (b in world.buildings) {
            if (!b.isDestroyed && b.currentHealth < b.maxHealth) {
                camera.worldToScreen(b.positionX, b.positionY, screenPos)
                val barW = 44f
                val barH = 6f
                val ratio = (b.currentHealth / b.maxHealth).coerceIn(0f, 1f)
                val sx = screenPos[0]
                val sy = screenPos[1] - 45f
                billboardRenderer.drawRect(sx - barW * 0.5f, sy, barW, barH, 0.15f, 0.15f, 0.15f, 0.85f)
                billboardRenderer.drawRect(sx - barW * 0.5f, sy, barW * ratio, barH, 0.85f, 0.20f, 0.20f, 0.95f)
            }
        }

        // Render health bars above damaged enemies
        for (enemy in world.enemies) {
            if (enemy.isAlive && enemy.health < enemy.maxHealth) {
                camera.worldToScreen(enemy.positionX, enemy.positionY, screenPos)
                val barW = 32f
                val barH = 5f
                val ratio = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
                val sx = screenPos[0]
                val sy = screenPos[1] - 30f
                billboardRenderer.drawRect(sx - barW * 0.5f, sy, barW, barH, 0.15f, 0.15f, 0.15f, 0.85f)
                billboardRenderer.drawRect(sx - barW * 0.5f, sy, barW * ratio, barH, 0.90f, 0.15f, 0.15f, 0.95f)
            }
        }

        // Particles
        for (particle in world.particleManager.pool) {
            if (particle.isActive) {
                camera.worldToScreen(particle.x, particle.y, screenPos)
                val sx = screenPos[0]
                val sy = screenPos[1]
                when (particle.type) {
                    ParticleType.FIRE -> {
                        billboardRenderer.drawCircle(sx, sy, particle.size, particle.r, particle.g, particle.b, particle.a)
                    }
                    ParticleType.SMOKE, ParticleType.DUST -> {
                        billboardRenderer.drawCircle(sx, sy, particle.size * 1.3f, particle.r, particle.g, particle.b, particle.a * 0.6f)
                    }
                    else -> {
                        billboardRenderer.drawCircle(sx, sy, particle.size, particle.r, particle.g, particle.b, particle.a)
                    }
                }
            }
        }

        billboardRenderer.end()
    }

    private fun renderEnvironmentProps(camX: Float, camZ: Float) {
        val cullingDistSq = 1400f * 1400f
        val blobShadow = assetManager.getModel("blob_shadow")

        for (prop in envProps) {
            val dx = prop.x - camX
            val dz = prop.z - camZ
            if (dx * dx + dz * dz > cullingDistSq) continue

            val model = assetManager.getModel(prop.modelId)

            // Ground contact shadow
            tempTransform.setPosition(prop.x, 0.05f, prop.z)
            tempTransform.setRotation(0f, 0f, 0f)
            tempTransform.setScale(prop.scale * 1.1f)
            modelRenderer.renderModel(blobShadow, tempTransform)

            // Prop mesh
            tempTransform.setPosition(prop.x, 0f, prop.z)
            tempTransform.setRotation(0f, prop.rotYDeg, 0f)
            tempTransform.setScale(prop.scale)
            modelRenderer.renderModel(model, tempTransform)
        }
    }

    private fun renderBuilding3D(b: Building, camX: Float, camZ: Float) {
        val dx = b.positionX - camX
        val dz = b.positionY - camZ
        if (dx * dx + dz * dz > 1500f * 1500f) return

        val blobShadow = assetManager.getModel("blob_shadow")

        // Ruined rubble mound for destroyed buildings
        if (b.isDestroyed) {
            val rubbleModel = assetManager.getModel("rock_01")
            tempTransform.setPosition(b.positionX, 0.05f, b.positionY)
            tempTransform.setRotation(0f, (b.id * 37 % 360).toFloat(), 0f)
            tempTransform.setScale(1.6f, 0.35f, 1.6f)
            modelRenderer.renderModel(rubbleModel, tempTransform)
            return
        }

        // Contact shadow
        tempTransform.setPosition(b.positionX, 0.05f, b.positionY)
        tempTransform.setRotation(0f, 0f, 0f)
        tempTransform.setScale(1.8f)
        modelRenderer.renderModel(blobShadow, tempTransform)

        // Damage slump / structural tilt as building loses health
        val dmgRatio = (1f - (b.currentHealth / b.maxHealth)).coerceIn(0f, 1f)
        val tiltPitch = dmgRatio * 3.5f
        val tiltRoll = (if (b.id % 2 == 0) 1f else -1f) * dmgRatio * 2.5f

        when (b.type) {
            BuildingType.VILLAGE_HOUSE -> {
                val variant = if (b.id % 2 == 0) "house_01" else "house_03"
                val houseModel = assetManager.getModel(variant)
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 53 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.15f)
                modelRenderer.renderModel(houseModel, tempTransform)
            }
            BuildingType.TOWNHOUSE -> {
                val houseModel = assetManager.getModel("house_02")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 41 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(houseModel, tempTransform)
            }
            BuildingType.FARM_BARN -> {
                val barnModel = assetManager.getModel("village_barn")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 67 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.1f)
                modelRenderer.renderModel(barnModel, tempTransform)
            }
            BuildingType.WINDMILL -> {
                val windmillModel = assetManager.getModel("windmill")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, 0f, tiltRoll)
                tempTransform.setScale(1.15f)
                modelRenderer.renderModel(windmillModel, tempTransform)
            }
            BuildingType.MARKET_STALL -> {
                val stallModel = assetManager.getModel("market_stall")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 83 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.25f)
                modelRenderer.renderModel(stallModel, tempTransform)
            }
            BuildingType.VILLAGE_WELL -> {
                val wellModel = assetManager.getModel("well")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, 0f, tiltRoll)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(wellModel, tempTransform)
            }
            BuildingType.BARRACKS -> {
                val fortModel = assetManager.getModel("enemy_fortress")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, 0f, tiltRoll)
                tempTransform.setScale(0.9f)
                modelRenderer.renderModel(fortModel, tempTransform)
            }
            BuildingType.WATCHTOWER -> {
                val towerModel = assetManager.getModel("watchtower")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch * 1.5f, 0f, tiltRoll * 1.5f)
                tempTransform.setScale(1.05f)
                modelRenderer.renderModel(towerModel, tempTransform)
            }
            BuildingType.WALL_SEGMENT -> {
                val wallModel = assetManager.getModel("palisade_wall")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(0f, (b.id * 90 % 180).toFloat(), 0f)
                tempTransform.setScale(1.2f, 1.1f, 1.2f)
                modelRenderer.renderModel(wallModel, tempTransform)
            }
            BuildingType.STONE_RUIN -> {
                val ruinModel = assetManager.getModel("stone_ruin")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(0f, (b.id * 47 % 360).toFloat(), 0f)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(ruinModel, tempTransform)
            }
            BuildingType.SUPPLY_TENT -> {
                val tentModel = assetManager.getModel("siege_tent")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 61 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.1f)
                modelRenderer.renderModel(tentModel, tempTransform)
            }
            BuildingType.BLACKSMITH -> {
                val forgeModel = assetManager.getModel("blacksmith")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 53 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.15f)
                modelRenderer.renderModel(forgeModel, tempTransform)
            }
            BuildingType.CATAPULT -> {
                val catapultModel = assetManager.getModel("prop_catapult")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 71 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.1f)
                modelRenderer.renderModel(catapultModel, tempTransform)
            }
            BuildingType.BALLISTA -> {
                val ballistaModel = assetManager.getModel("prop_ballista")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 43 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(ballistaModel, tempTransform)
            }
            BuildingType.BARRICADE -> {
                val barricadeModel = assetManager.getModel("prop_barricade")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 90 % 180).toFloat(), tiltRoll)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(barricadeModel, tempTransform)
            }
            BuildingType.CROP_FIELD -> {
                val fieldModel = assetManager.getModel(if (b.id % 2 == 0) "farm_crop_wheat" else "farm_crop_veggie")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(0f, (b.id * 90 % 180).toFloat(), 0f)
                tempTransform.setScale(1.1f)
                modelRenderer.renderModel(fieldModel, tempTransform)
            }
            BuildingType.FARM_SHED -> {
                val shedModel = assetManager.getModel("farm_shed")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 77 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.15f)
                modelRenderer.renderModel(shedModel, tempTransform)
            }
            BuildingType.ELDER_OAK -> {
                val oakModel = assetManager.getModel("tree_elder_oak")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(oakModel, tempTransform)
            }
            BuildingType.STONE_BRIDGE -> {
                val bridgeModel = assetManager.getModel("stone_bridge")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.0f)
                modelRenderer.renderModel(bridgeModel, tempTransform)
            }
            BuildingType.BURNED_CART -> {
                val cartModel = assetManager.getModel("burned_cart")
                tempTransform.setPosition(b.positionX, 0f, b.positionY)
                tempTransform.setRotation(tiltPitch, (b.id * 67 % 360).toFloat(), tiltRoll)
                tempTransform.setScale(1.2f)
                modelRenderer.renderModel(cartModel, tempTransform)
            }
        }
    }

    private fun renderEnemy3D(enemy: Enemy, camX: Float, camZ: Float) {
        val dx = enemy.positionX - camX
        val dz = enemy.positionY - camZ
        if (dx * dx + dz * dz > 1400f * 1400f) return

        // Enemy face direction (Y-axis yaw)
        val yawDeg = -Math.toDegrees(enemy.rotation.toDouble()).toFloat() + 90f

        when (enemy) {
            is EnemyBoss -> {
                // Massive dark iron citadel boss fortress
                val shadow = assetManager.getModel("blob_shadow_castle")
                tempTransform.setPosition(enemy.positionX, 0.05f, enemy.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.8f)
                modelRenderer.renderModel(shadow, tempTransform)

                val bossModel = assetManager.getModel("boss_fortress")
                tempTransform.setPosition(enemy.positionX, 0f, enemy.positionY)
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(1.1f)
                modelRenderer.renderModel(bossModel, tempTransform)
            }
            is EnemyKnight -> {
                // Scaled for strong isometric readability against terrain
                val shadow = assetManager.getModel("blob_shadow_unit")
                tempTransform.setPosition(enemy.positionX, 0.05f, enemy.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.5f)
                modelRenderer.renderModel(shadow, tempTransform)

                val knightModel = assetManager.getModel("unit_knight")
                tempTransform.setPosition(enemy.positionX, 0f, enemy.positionY)
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(1.70f)
                modelRenderer.renderModel(knightModel, tempTransform)
            }
            is EnemyArcher -> {
                val shadow = assetManager.getModel("blob_shadow_unit")
                tempTransform.setPosition(enemy.positionX, 0.05f, enemy.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.4f)
                modelRenderer.renderModel(shadow, tempTransform)

                val archerModel = assetManager.getModel("unit_archer")
                tempTransform.setPosition(enemy.positionX, 0f, enemy.positionY)
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(1.50f)
                modelRenderer.renderModel(archerModel, tempTransform)
            }
            is EnemyBomber -> {
                val shadow = assetManager.getModel("blob_shadow_unit")
                tempTransform.setPosition(enemy.positionX, 0.05f, enemy.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.45f)
                modelRenderer.renderModel(shadow, tempTransform)

                val bomberModel = assetManager.getModel("unit_bomber")
                tempTransform.setPosition(enemy.positionX, 0f, enemy.positionY)
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(1.55f)
                modelRenderer.renderModel(bomberModel, tempTransform)
            }
            else -> {
                val shadow = assetManager.getModel("blob_shadow_unit")
                tempTransform.setPosition(enemy.positionX, 0.05f, enemy.positionY)
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.4f)
                modelRenderer.renderModel(shadow, tempTransform)

                val soldierModel = assetManager.getModel("unit_swordsman")
                tempTransform.setPosition(enemy.positionX, 0f, enemy.positionY)
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(1.50f)
                modelRenderer.renderModel(soldierModel, tempTransform)
            }
        }
    }

    private fun renderCastle3D(castle: MovingCastle) {
        val cx = castle.positionX
        val cz = castle.positionY
        val castleYawDeg = -Math.toDegrees(castle.rotation.toDouble()).toFloat() + 90f

        // 0. Render Large Fortress Contact Shadow
        val shadowModel = assetManager.getModel("blob_shadow_castle")
        tempTransform.setPosition(cx, 0.05f, cz)
        tempTransform.setRotation(0f, 0f, 0f)
        tempTransform.setScale(1.0f + (castle.tier - 1) * 0.08f)
        modelRenderer.renderModel(shadowModel, tempTransform)

        // 1. Render Fortress Main Hull based on current Tier
        val tier = castle.tier.coerceIn(1, 5)
        val castleModel = assetManager.getModel("player_castle_tier_$tier")
        tempTransform.setPosition(cx, 0f, cz)
        tempTransform.setRotation(0f, castleYawDeg, 0f)
        tempTransform.setScale(1.0f)
        modelRenderer.renderModel(castleModel, tempTransform)

        // 2. Render Rotating Castle Wheels (6 wheels for Tier 5 Arcane Fortress, 4 for Tiers 1-4)
        val wheelModel = assetManager.getModel("castle_wheel")
        val wheelOffsets = if (tier >= 5) {
            listOf(
                Pair(-38f, -40f), Pair(38f, -40f), // Rear
                Pair(-40f, 0f),   Pair(40f, 0f),   // Middle
                Pair(-38f, 40f),  Pair(38f, 40f)   // Front
            )
        } else {
            listOf(
                Pair(-36f, -34f), // Rear-Left
                Pair(36f, -34f),  // Rear-Right
                Pair(-36f, 34f),  // Front-Left
                Pair(36f, 34f)    // Front-Right
            )
        }

        val cosYaw = cos(castle.rotation)
        val sinYaw = sin(castle.rotation)

        for ((ox, oz) in wheelOffsets) {
            // Transform local wheel offset by castle yaw
            val wx = cx + (ox * cosYaw - oz * sinYaw)
            val wz = cz + (ox * sinYaw + oz * cosYaw)

            tempTransform.setPosition(wx, 14f, wz)
            // Rotate around wheel rolling axle (X-axis) and yaw direction (Y-axis)
            tempTransform.setRotation(castle.wheelRotationDeg, castleYawDeg, 0f)
            tempTransform.setScale(if (tier >= 4) 1.15f else 1.0f)
            modelRenderer.renderModel(wheelModel, tempTransform)
        }

        // 3. Render Mounted 3D Weapons with Tiered Visual Models
        for (weapon in castle.weapons) {
            val wx = cx + (weapon.slotOffsetX * cosYaw - weapon.slotOffsetY * sinYaw)
            val wz = cz + (weapon.slotOffsetX * sinYaw + weapon.slotOffsetY * cosYaw)
            val weaponYawDeg = -Math.toDegrees(weapon.turretAngle.toDouble()).toFloat() + 90f

            when (weapon) {
                is CannonWeapon -> {
                    val lvl = weapon.level.coerceIn(1, 3)
                    val cannonModel = assetManager.getModel("cannon_lvl$lvl")
                    tempTransform.setPosition(wx, 24f, wz)
                    tempTransform.setRotation(0f, weaponYawDeg, 0f)
                    tempTransform.setScale(1.0f)
                    modelRenderer.renderModel(cannonModel, tempTransform)
                }
                is ArcherTowerWeapon -> {
                    val lvl = weapon.level.coerceIn(1, 2)
                    val archerTowerModel = assetManager.getModel("archer_tower_lvl$lvl")
                    tempTransform.setPosition(wx, 24f, wz)
                    tempTransform.setRotation(0f, weaponYawDeg, 0f)
                    tempTransform.setScale(1.0f)
                    modelRenderer.renderModel(archerTowerModel, tempTransform)
                }
                is BallistaWeapon -> {
                    val lvl = weapon.level.coerceIn(1, 2)
                    val ballistaModel = assetManager.getModel("ballista_lvl$lvl")
                    tempTransform.setPosition(wx, 24f, wz)
                    tempTransform.setRotation(0f, weaponYawDeg, 0f)
                    tempTransform.setScale(1.0f)
                    modelRenderer.renderModel(ballistaModel, tempTransform)
                }
            }
        }
    }

    private fun renderPickup3D(p: ResourcePickup, camX: Float, camZ: Float) {
        val dx = p.positionX - camX
        val dz = p.positionY - camZ
        if (dx * dx + dz * dz > 1100f * 1100f) return

        val pickupModelId = when (p.type) {
            ResourceType.GOLD -> "gold_pickup"
            ResourceType.WOOD -> "pickup_wood"
            ResourceType.STONE -> "pickup_stone"
            ResourceType.IRON -> "pickup_iron"
            ResourceType.MAGIC_CRYSTAL, ResourceType.EXPERIENCE -> "pickup_crystal"
        }

        val pickupModel = assetManager.getModel(pickupModelId)
        val shadowModel = assetManager.getModel("blob_shadow_unit")

        val spinDeg = (p.bobbingTimer * 90f) % 360f
        val heightY = 4.5f + sin(p.bobbingTimer * 3f) * 2.2f

        // Shadow shrinks slightly as pickup bobs higher
        val shadowScale = (1.0f - (heightY - 4.5f) * 0.12f).coerceIn(0.6f, 1.1f)
        tempTransform.setPosition(p.positionX, 0.05f, p.positionY)
        tempTransform.setRotation(0f, 0f, 0f)
        tempTransform.setScale(shadowScale)
        modelRenderer.renderModel(shadowModel, tempTransform)

        // Pickup mesh
        tempTransform.setPosition(p.positionX, heightY, p.positionY)
        tempTransform.setRotation(0f, spinDeg, 0f)
        tempTransform.setScale(1.2f)
        modelRenderer.renderModel(pickupModel, tempTransform)
    }

    private fun renderProjectile3D(p: Projectile) {
        val cannonballModel = assetManager.getModel("gold_pickup")
        val alt = p.visualAltitude

        tempTransform.setPosition(p.x, alt.coerceAtLeast(3f), p.y)
        tempTransform.setScale(0.8f)

        when (p.type) {
            ProjectileType.CANNONBALL -> {
                tempTransform.setRotation(0f, 0f, 0f)
                modelRenderer.renderModel(cannonballModel, tempTransform, Material.IRON)
            }
            ProjectileType.ARROW -> {
                val yawDeg = -Math.toDegrees(p.flightProgress.toDouble()).toFloat()
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(0.4f, 0.4f, 1.4f)
                modelRenderer.renderModel(cannonballModel, tempTransform, Material.WOOD)
            }
            ProjectileType.BALLISTA_BOLT -> {
                val yawDeg = -Math.toDegrees(p.flightProgress.toDouble()).toFloat()
                tempTransform.setRotation(0f, yawDeg, 0f)
                tempTransform.setScale(0.6f, 0.6f, 2.0f)
                modelRenderer.renderModel(cannonballModel, tempTransform, Material(name = "MagicBolt", baseColorR = 0.2f, baseColorG = 0.7f, baseColorB = 1.0f))
            }
            ProjectileType.FIREBALL -> {
                tempTransform.setRotation(0f, 0f, 0f)
                tempTransform.setScale(1.25f)
                modelRenderer.renderModel(cannonballModel, tempTransform, Material(name = "Fireball", baseColorR = 1.0f, baseColorG = 0.35f, baseColorB = 0.05f))
            }
            else -> {
                modelRenderer.renderModel(cannonballModel, tempTransform, Material.GOLD)
            }
        }
    }

    fun release() {
        assetManager.releaseAll()
        modelRenderer.release()
        terrainRenderer.release()
    }
}
