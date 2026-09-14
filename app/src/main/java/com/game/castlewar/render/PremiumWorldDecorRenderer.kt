package com.game.castlewar.render

import android.content.Context
import com.game.castlewar.model3d.GameAssetManager
import com.game.castlewar.model3d.ModelRenderer
import com.game.castlewar.model3d.TerrainRenderer3D
import com.game.castlewar.model3d.Transform
import kotlin.math.abs
import kotlin.random.Random

/**
 * Extra visual-only dressing layer for a richer fantasy overworld.
 * Props are deliberately kept away from the main travel corridor so they improve composition
 * without changing collision/gameplay. Distance culling keeps the pass mobile friendly.
 */
class PremiumWorldDecorRenderer(context: Context) {

    private data class Prop(val id: String, val x: Float, val z: Float, val scale: Float, val rot: Float)

    private val assets = GameAssetManager(context)
    private val renderer = ModelRenderer()
    private val transform = Transform()
    private val props = ArrayList<Prop>(320)

    init {
        generateDecor()
    }

    fun initialize() {
        renderer.initialize()
        // Slightly warmer/high-key lighting dedicated to landscape dressing.
        renderer.sunDirection[0] = 0.48f
        renderer.sunDirection[1] = 0.84f
        renderer.sunDirection[2] = 0.28f
        renderer.sunColor[0] = 1.12f
        renderer.sunColor[1] = 1.03f
        renderer.sunColor[2] = 0.90f
        renderer.skyColor[0] = 0.52f
        renderer.skyColor[1] = 0.61f
        renderer.skyColor[2] = 0.73f
        renderer.groundColor[0] = 0.27f
        renderer.groundColor[1] = 0.34f
        renderer.groundColor[2] = 0.22f
    }

    private fun generateDecor() {
        val rng = Random(7351)

        // Large forest masses frame the playable corridor and create strong parallax/depth.
        val forests = listOf(
            -1150f to -850f, -980f to 720f, -420f to 1150f,
            980f to -900f, 1120f to 650f, 520f to 1220f,
            -1450f to 250f, 1480f to 100f
        )
        for ((cx, cz) in forests) {
            repeat(24) {
                val x = cx + (rng.nextFloat() - 0.5f) * 430f
                val z = cz + (rng.nextFloat() - 0.5f) * 430f
                if (!isSafeDecorPoint(x, z)) return@repeat
                val id = when (rng.nextInt(10)) {
                    in 0..3 -> "tree_01"
                    in 4..6 -> "tree_pine"
                    7 -> "tree_autumn"
                    else -> "bush"
                }
                val scale = when (id) {
                    "bush" -> 1.1f + rng.nextFloat() * 0.65f
                    else -> 1.20f + rng.nextFloat() * 0.75f
                }
                props.add(Prop(id, x, z, scale, rng.nextFloat() * 360f))
            }
        }

        // Settlement-edge clusters: trees, hay, fences, crates and lanterns.
        val settlements = listOf(
            -360f to -230f,
            -450f to 410f,
            420f to -270f,
            510f to 440f,
            850f to -100f,
            -350f to 860f
        )
        for ((cx, cz) in settlements) {
            repeat(16) { index ->
                val angle = (index / 16f) * 6.28318f + rng.nextFloat() * 0.18f
                val radius = 105f + rng.nextFloat() * 115f
                val x = cx + kotlin.math.cos(angle) * radius
                val z = cz + kotlin.math.sin(angle) * radius
                val id = when (index % 8) {
                    0 -> "tree_01"
                    1 -> "tree_autumn"
                    2 -> "bush"
                    3 -> "fence"
                    4 -> "haystack"
                    5 -> "crates_barrels"
                    6 -> "lantern_post"
                    else -> "rock_01"
                }
                val scale = when (id) {
                    "tree_01", "tree_autumn" -> 1.20f + rng.nextFloat() * 0.45f
                    "lantern_post" -> 1.20f
                    else -> 1.0f + rng.nextFloat() * 0.30f
                }
                props.add(Prop(id, x, z, scale, rng.nextFloat() * 360f))
            }
        }

        // Hero landmarks visible from a distance.
        props.addAll(
            listOf(
                Prop("tree_elder_oak", -720f, 170f, 1.65f, 0f),
                Prop("tree_elder_oak", 720f, 220f, 1.55f, 35f),
                Prop("stone_ruin", 1000f, -300f, 1.45f, 30f),
                Prop("stone_ruin", -980f, 360f, 1.35f, -35f),
                Prop("rock_formation_01", 1160f, 460f, 1.55f, 55f),
                Prop("rock_formation_01", -1180f, -380f, 1.50f, 20f),
                Prop("signpost", 135f, 165f, 1.30f, 15f),
                Prop("signpost", -135f, -190f, 1.30f, -20f),
                Prop("lantern_post", 155f, 210f, 1.35f, 0f),
                Prop("lantern_post", -155f, -235f, 1.35f, 180f)
            )
        )

        // Rock/bush scatter breaks up empty terrain without filling the roads.
        repeat(90) {
            val x = (rng.nextFloat() - 0.5f) * 3300f
            val z = (rng.nextFloat() - 0.5f) * 3300f
            if (!isSafeDecorPoint(x, z)) return@repeat
            val id = when (rng.nextInt(5)) {
                0 -> "rock_01"
                1 -> "rock_02"
                2 -> "bush"
                3 -> "tree_pine"
                else -> "tree_01"
            }
            props.add(Prop(id, x, z, 0.95f + rng.nextFloat() * 0.55f, rng.nextFloat() * 360f))
        }
    }

    private fun isSafeDecorPoint(x: Float, z: Float): Boolean {
        // Preserve the central north/south highway and immediate starting arena.
        if (abs(x) < 135f && z in -1550f..1550f) return false
        if (x * x + z * z < 230f * 230f) return false
        // Keep river bridge approach clean.
        if (abs(x) < 220f && z in -820f..-500f) return false
        return true
    }

    fun render(camera: GameCamera) {
        renderer.begin(camera.viewMatrix, camera.projectionMatrix, camera.eyeX, camera.eyeY, camera.eyeZ)
        renderer.setLightingProfile(ModelRenderer.PROFILE_ENVIRONMENT)

        val camX = camera.positionX
        val camZ = camera.positionY
        val maxDistSq = 1200f * 1200f
        val shadow = assets.getModel("blob_shadow")

        for (prop in props) {
            val dx = prop.x - camX
            val dz = prop.z - camZ
            if (dx * dx + dz * dz > maxDistSq) continue

            val groundY = TerrainRenderer3D.getTerrainHeight(prop.x, prop.z)

            // Soft contact shadow.
            transform.setPosition(prop.x, groundY + 0.15f, prop.z)
            transform.setRotation(0f, 0f, 0f)
            transform.setScale(prop.scale * 1.15f)
            renderer.renderModel(shadow, transform)

            transform.setPosition(prop.x, groundY, prop.z)
            transform.setRotation(0f, prop.rot, 0f)
            transform.setScale(prop.scale)
            renderer.renderModel(assets.getModel(prop.id), transform)
        }

        renderer.end()
    }

    fun release() {
        assets.releaseAll()
        renderer.release()
    }
}
