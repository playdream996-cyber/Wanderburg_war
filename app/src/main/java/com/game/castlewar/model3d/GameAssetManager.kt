package com.game.castlewar.model3d

import android.content.Context
import android.util.Log

/**
 * Central asset repository and lifecycle manager for 3D models and textures.
 * Preloads, caches, and provides zero-allocation lookup during gameplay frames.
 */
class GameAssetManager(private val context: Context) {

    companion object {
        private const val TAG = "GameAssetManager"
    }

    private val modelLoader = ModelLoader(context)
    private val modelCache = mutableMapOf<String, Model>()
    private val textureCache = mutableMapOf<String, Texture>()

    // Standard model mapping from model ID to asset path
    private val modelAssetPaths = mapOf(
        "player_castle" to "models/castle/player_castle.glb",
        "castle_wheel" to "models/castle/castle_wheel.glb",
        "cannon" to "models/weapons/cannon.glb",
        "enemy_soldier" to "models/enemies/soldier.glb",
        "soldier" to "models/enemies/soldier.glb",
        "house_01" to "models/buildings/house_01.glb",
        "tree_01" to "models/environment/tree_01.glb",
        "rock_01" to "models/environment/rock_01.glb",
        "gold_pickup" to "models/resources/gold_pickup.glb"
    )

    /**
     * Preloads and initializes the key 3D game models.
     */
    fun preloadAssets() {
        Log.i(TAG, "Preloading primary 3D medieval models...")
        for ((id, path) in modelAssetPaths) {
            if (!modelCache.containsKey(id)) {
                val model = modelLoader.loadModel(path, id)
                model.initializeGL()
                modelCache[id] = model
            }
        }
        Log.i(TAG, "Preloaded ${modelCache.size} 3D models into GPU memory.")
    }

    private fun createProceduralModel(modelId: String): Model {
        return when (modelId) {
            "player_castle", "player_castle_tier_1" -> com.game.castlewar.model3d.procedural.CastleModelBuilder.buildPlayerCastle(1)
            "player_castle_tier_2" -> com.game.castlewar.model3d.procedural.CastleModelBuilder.buildPlayerCastle(2)
            "player_castle_tier_3" -> com.game.castlewar.model3d.procedural.CastleModelBuilder.buildPlayerCastle(3)
            "player_castle_tier_4" -> com.game.castlewar.model3d.procedural.CastleModelBuilder.buildPlayerCastle(4)
            "player_castle_tier_5" -> com.game.castlewar.model3d.procedural.CastleModelBuilder.buildPlayerCastle(5)
            "castle_wheel" -> com.game.castlewar.model3d.procedural.CastleWheelBuilder.buildWheel()
            "cannon", "cannon_lvl1" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildCannon(1)
            "cannon_lvl2" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildCannon(2)
            "cannon_lvl3", "cannon_lvl4" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildCannon(3)
            "ballista", "ballista_lvl1" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildBallista(1)
            "ballista_lvl2", "ballista_lvl3" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildBallista(2)
            "catapult" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildCatapult()
            "archer_tower", "archer_tower_lvl1" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildArcherTower(1)
            "archer_tower_lvl2", "archer_tower_lvl3" -> com.game.castlewar.model3d.procedural.WeaponModelBuilders.buildArcherTower(2)
            "soldier", "enemy_soldier", "unit_swordsman" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(0)
            "unit_archer" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(1)
            "unit_knight" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(2)
            "unit_bomber" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(3)
            "unit_mage" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(4)
            "unit_engineer" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(5)
            "unit_villager" -> com.game.castlewar.model3d.procedural.SoldierModelBuilder.buildSoldier(6)
            "house_01" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildHouse(0)
            "house_02" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildHouse(1)
            "house_03" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildHouse(2)
            "house_04", "blacksmith" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildHouse(3)
            "windmill" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildWindmill()
            "well" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildWell()
            "watchtower" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildWatchtower()
            "village_barn", "barn" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildBarn()
            "palisade_wall" -> com.game.castlewar.model3d.procedural.MedievalHouseBuilder.buildPalisadeWall()
            "stone_bridge" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildStoneBridge()
            "enemy_fortress" -> com.game.castlewar.model3d.procedural.FortressModelBuilder.buildEnemyFortress()
            "boss_fortress" -> com.game.castlewar.model3d.procedural.FortressModelBuilder.buildBossFortress()
            "tree_01", "tree_oak" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildOakTree()
            "tree_pine" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildPineTree()
            "tree_autumn" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildAutumnTree()
            "tree_dead" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildDeadTree()
            "tree_elder_oak" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildElderOakTree()
            "bush", "env_bush" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBush()
            "haystack", "prop_haystack" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildHaystack()
            "fence", "prop_fence" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildFence()
            "signpost", "prop_signpost" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildSignPost()
            "broken_cart", "prop_broken_cart" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBrokenCart()
            "burned_cart", "prop_burned_cart" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBurnedCart()
            "wood_pile", "prop_wood_pile" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildWoodPile()
            "campfire", "prop_campfire" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildCampfire()
            "lantern_post", "prop_lantern_post" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildLanternPost()
            "training_dummy", "prop_training_dummy" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildTrainingDummy()
            "crates_barrels", "prop_crates_barrels" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildCratesAndBarrels()
            "market_stall", "prop_market_stall" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildMarketStall()
            "stone_ruin", "prop_stone_ruin" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildStoneRuin()
            "siege_catapult", "prop_catapult" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildSiegeCatapult()
            "siege_ballista", "prop_ballista" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildSiegeBallista()
            "siege_tent", "prop_siege_tent" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildSiegeTent()
            "weapon_rack", "prop_weapon_rack" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildWeaponRack()
            "barricade", "prop_barricade" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBarricade()
            "farm_crop_wheat", "crop_wheat" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildCropField(0)
            "farm_crop_veggie", "crop_veggie" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildCropField(1)
            "farm_shed" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildFarmShed()
            "rock_01" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildRock(0)
            "rock_02" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildRock(1)
            "rock_03" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildRock(2)
            "rock_formation", "rock_cluster" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildRockFormation()
            "gold_pickup" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildResource(0)
            "pickup_wood" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildResource(1)
            "pickup_stone" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildResource(2)
            "pickup_iron" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildResource(3)
            "pickup_crystal" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildResource(4)
            "blob_shadow" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBlobShadow(14f)
            "blob_shadow_castle" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBlobShadow(48f)
            "blob_shadow_unit" -> com.game.castlewar.model3d.procedural.EnvironmentModelBuilders.buildBlobShadow(5f)
            else -> modelLoader.loadModel("models/$modelId.glb", modelId)
        }
    }

    /**
     * Retrieves a cached 3D Model by ID. If not loaded, generates or loads it immediately.
     */
    fun getModel(modelId: String): Model {
        modelCache[modelId]?.let { return it }

        val model = try {
            val path = modelAssetPaths[modelId]
            if (path != null) {
                try {
                    val stream = context.assets.open(path)
                    val glb = modelLoader.loadGLB(stream, modelId)
                    stream.close()
                    glb
                } catch (e: Exception) {
                    createProceduralModel(modelId)
                }
            } else {
                createProceduralModel(modelId)
            }
        } catch (e: Exception) {
            createProceduralModel(modelId)
        }

        model.initializeGL()
        modelCache[modelId] = model
        return model
    }

    /**
     * Returns true if a model is cached and ready for rendering.
     */
    fun hasModel(modelId: String): Boolean = modelCache.containsKey(modelId)

    /**
     * Releases all cached OpenGL models and textures.
     */
    fun releaseAll() {
        Log.i(TAG, "Releasing all 3D assets from GPU.")
        for (model in modelCache.values) {
            model.release()
        }
        modelCache.clear()

        for (tex in textureCache.values) {
            tex.release()
        }
        textureCache.clear()
    }
}
