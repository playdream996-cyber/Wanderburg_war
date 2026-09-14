package com.game.castlewar.model3d

import android.content.Context
import android.util.Log
import com.game.castlewar.model3d.procedural.CastleWheelBuilder
import com.game.castlewar.model3d.procedural.EnvironmentModelBuilders
import com.game.castlewar.model3d.procedural.FortressModelBuilder
import com.game.castlewar.model3d.procedural.HeroicCastleModelBuilder
import com.game.castlewar.model3d.procedural.HeroicSoldierModelBuilder
import com.game.castlewar.model3d.procedural.MedievalHouseBuilder
import com.game.castlewar.model3d.procedural.WeaponModelBuilders

/**
 * Central asset repository and lifecycle manager for 3D models and textures.
 * Core gameplay/world objects deliberately use a single procedural art pipeline so the
 * game keeps a cohesive stylized-fantasy look instead of mixing unrelated GLB styles.
 */
class GameAssetManager(private val context: Context) {

    companion object {
        private const val TAG = "GameAssetManager"
    }

    private val modelLoader = ModelLoader(context)
    private val modelCache = mutableMapOf<String, Model>()
    private val textureCache = mutableMapOf<String, Texture>()

    // Keep external GLBs only for assets that already fit the desired silhouette.
    // Houses/trees/rocks/units/castles are generated procedurally for art consistency.
    private val modelAssetPaths = mapOf(
        "castle_wheel" to "models/castle/castle_wheel.glb",
        "cannon" to "models/weapons/cannon.glb"
    )

    fun preloadAssets() {
        Log.i(TAG, "Preloading primary 3D medieval models...")
        for ((id, path) in modelAssetPaths) {
            if (!modelCache.containsKey(id)) {
                val model = modelLoader.loadModel(path, id)
                model.initializeGL()
                modelCache[id] = model
            }
        }
        Log.i(TAG, "Preloaded ${modelCache.size} external models; remaining art is procedural.")
    }

    private fun createProceduralModel(modelId: String): Model {
        return when (modelId) {
            "player_castle", "player_castle_tier_1" -> HeroicCastleModelBuilder.build(1)
            "player_castle_tier_2" -> HeroicCastleModelBuilder.build(2)
            "player_castle_tier_3" -> HeroicCastleModelBuilder.build(3)
            "player_castle_tier_4" -> HeroicCastleModelBuilder.build(4)
            "player_castle_tier_5" -> HeroicCastleModelBuilder.build(5)

            "castle_wheel" -> CastleWheelBuilder.buildWheel()
            "cannon", "cannon_lvl1" -> WeaponModelBuilders.buildCannon(1)
            "cannon_lvl2" -> WeaponModelBuilders.buildCannon(2)
            "cannon_lvl3", "cannon_lvl4" -> WeaponModelBuilders.buildCannon(3)
            "ballista", "ballista_lvl1" -> WeaponModelBuilders.buildBallista(1)
            "ballista_lvl2", "ballista_lvl3" -> WeaponModelBuilders.buildBallista(2)
            "catapult" -> WeaponModelBuilders.buildCatapult()
            "archer_tower", "archer_tower_lvl1" -> WeaponModelBuilders.buildArcherTower(1)
            "archer_tower_lvl2", "archer_tower_lvl3" -> WeaponModelBuilders.buildArcherTower(2)

            "soldier", "enemy_soldier", "unit_swordsman" -> HeroicSoldierModelBuilder.build(0)
            "unit_archer" -> HeroicSoldierModelBuilder.build(1)
            "unit_knight" -> HeroicSoldierModelBuilder.build(2)
            "unit_bomber" -> HeroicSoldierModelBuilder.build(3)
            "unit_mage" -> HeroicSoldierModelBuilder.build(4)
            "unit_engineer" -> HeroicSoldierModelBuilder.build(5)
            "unit_villager" -> HeroicSoldierModelBuilder.build(6)

            "house_01" -> MedievalHouseBuilder.buildHouse(0)
            "house_02" -> MedievalHouseBuilder.buildHouse(1)
            "house_03" -> MedievalHouseBuilder.buildHouse(2)
            "house_04", "blacksmith" -> MedievalHouseBuilder.buildHouse(3)
            "windmill" -> MedievalHouseBuilder.buildWindmill()
            "well" -> MedievalHouseBuilder.buildWell()
            "watchtower" -> MedievalHouseBuilder.buildWatchtower()
            "village_barn", "barn" -> MedievalHouseBuilder.buildBarn()
            "palisade_wall" -> MedievalHouseBuilder.buildPalisadeWall()
            "stone_bridge" -> EnvironmentModelBuilders.buildStoneBridge()
            "enemy_fortress" -> FortressModelBuilder.buildEnemyFortress()
            "boss_fortress" -> FortressModelBuilder.buildBossFortress()

            "tree_01", "tree_oak" -> EnvironmentModelBuilders.buildOakTree()
            "tree_pine" -> EnvironmentModelBuilders.buildPineTree()
            "tree_autumn" -> EnvironmentModelBuilders.buildAutumnTree()
            "tree_dead" -> EnvironmentModelBuilders.buildDeadTree()
            "tree_elder_oak" -> EnvironmentModelBuilders.buildElderOakTree()
            "bush", "env_bush" -> EnvironmentModelBuilders.buildBush()
            "haystack", "prop_haystack" -> EnvironmentModelBuilders.buildHaystack()
            "fence", "prop_fence" -> EnvironmentModelBuilders.buildFence()
            "signpost", "prop_signpost" -> EnvironmentModelBuilders.buildSignPost()
            "broken_cart", "prop_broken_cart" -> EnvironmentModelBuilders.buildBrokenCart()
            "burned_cart", "prop_burned_cart" -> EnvironmentModelBuilders.buildBurnedCart()
            "wood_pile", "prop_wood_pile" -> EnvironmentModelBuilders.buildWoodPile()
            "campfire", "prop_campfire" -> EnvironmentModelBuilders.buildCampfire()
            "lantern_post", "prop_lantern_post" -> EnvironmentModelBuilders.buildLanternPost()
            "training_dummy", "prop_training_dummy" -> EnvironmentModelBuilders.buildTrainingDummy()
            "crates_barrels", "prop_crates_barrels" -> EnvironmentModelBuilders.buildCratesAndBarrels()
            "market_stall", "prop_market_stall" -> EnvironmentModelBuilders.buildMarketStall()
            "stone_ruin", "prop_stone_ruin" -> EnvironmentModelBuilders.buildStoneRuin()
            "siege_catapult", "prop_catapult" -> EnvironmentModelBuilders.buildSiegeCatapult()
            "siege_ballista", "prop_ballista" -> EnvironmentModelBuilders.buildSiegeBallista()
            "siege_tent", "prop_siege_tent" -> EnvironmentModelBuilders.buildSiegeTent()
            "weapon_rack", "prop_weapon_rack" -> EnvironmentModelBuilders.buildWeaponRack()
            "barricade", "prop_barricade" -> EnvironmentModelBuilders.buildBarricade()
            "farm_crop_wheat", "crop_wheat" -> EnvironmentModelBuilders.buildCropField(0)
            "farm_crop_veggie", "crop_veggie" -> EnvironmentModelBuilders.buildCropField(1)
            "farm_shed" -> EnvironmentModelBuilders.buildFarmShed()

            "rock_01" -> EnvironmentModelBuilders.buildRock(0)
            "rock_02" -> EnvironmentModelBuilders.buildRock(1)
            "rock_03" -> EnvironmentModelBuilders.buildRock(2)
            "rock_formation", "rock_cluster", "rock_formation_01" -> EnvironmentModelBuilders.buildRockFormation()

            "gold_pickup" -> EnvironmentModelBuilders.buildResource(0)
            "pickup_wood" -> EnvironmentModelBuilders.buildResource(1)
            "pickup_stone" -> EnvironmentModelBuilders.buildResource(2)
            "pickup_iron" -> EnvironmentModelBuilders.buildResource(3)
            "pickup_crystal" -> EnvironmentModelBuilders.buildResource(4)

            "blob_shadow" -> EnvironmentModelBuilders.buildBlobShadow(14f)
            "blob_shadow_castle" -> EnvironmentModelBuilders.buildBlobShadow(48f)
            "blob_shadow_unit" -> EnvironmentModelBuilders.buildBlobShadow(5f)
            else -> modelLoader.loadModel("models/$modelId.glb", modelId)
        }
    }

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

    fun hasModel(modelId: String): Boolean = modelCache.containsKey(modelId)

    fun releaseAll() {
        Log.i(TAG, "Releasing all 3D assets from GPU.")
        for (model in modelCache.values) model.release()
        modelCache.clear()
        for (tex in textureCache.values) tex.release()
        textureCache.clear()
    }
}
