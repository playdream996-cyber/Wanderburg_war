package com.game.castlewar

import com.game.castlewar.model3d.GLBBuilder
import com.game.castlewar.model3d.ModelLoader
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class GenerateGLBAssetsTest {

    @Test
    fun generateRealGLBAssets() {
        val context = RuntimeEnvironment.getApplication()
        val loader = ModelLoader(context)

        val assetTargets = mapOf(
            "castle/player_castle.glb" to "player_castle",
            "castle/castle_wheel.glb" to "castle_wheel",
            "weapons/cannon.glb" to "cannon",
            "enemies/soldier.glb" to "soldier",
            "buildings/house_01.glb" to "house_01",
            "environment/tree_01.glb" to "tree_01",
            "environment/rock_01.glb" to "rock_01",
            "resources/gold_pickup.glb" to "gold_pickup"
        )

        // Locate app/src/main/assets/models
        val baseDir = when {
            File("app/src/main").exists() -> File("app/src/main/assets/models")
            File("src/main").exists() -> File("src/main/assets/models")
            else -> File("/app/src/main/assets/models")
        }
        baseDir.mkdirs()

        for ((relPath, modelId) in assetTargets) {
            val model = loader.createFallbackModel(modelId)
            val glbBytes = GLBBuilder.buildGLB(model)
            val targetFile = File(baseDir, relPath)
            targetFile.parentFile?.mkdirs()
            targetFile.writeBytes(glbBytes)
            println("Wrote real GLB asset (${glbBytes.size} bytes) -> ${targetFile.absolutePath}")
            assert(targetFile.exists() && targetFile.length() > 0)
        }
    }
}
