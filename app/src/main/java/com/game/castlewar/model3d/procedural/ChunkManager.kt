package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.MatrixUtils
import com.game.castlewar.model3d.ModelRenderer
import kotlin.math.floor

/**
 * Manages active terrain chunks around the camera and moving castle.
 */
class ChunkManager(
    val chunkSize: Float = 120f,
    val viewRadiusChunks: Int = 2
) {
    private val activeChunks = mutableMapOf<Long, TerrainChunk>()
    private val tempModelMatrix = FloatArray(16)

    private fun chunkKey(cx: Int, cz: Int): Long {
        return (cx.toLong() shl 32) or (cz.toLong() and 0xFFFFFFFFL)
    }

    /**
     * Updates loaded chunks around the center position (X, Z).
     */
    fun update(centerX: Float, centerZ: Float) {
        val centerChunkX = floor(centerX / chunkSize).toInt()
        val centerChunkZ = floor(centerZ / chunkSize).toInt()

        val requiredKeys = mutableSetOf<Long>()

        for (dx in -viewRadiusChunks..viewRadiusChunks) {
            for (dz in -viewRadiusChunks..viewRadiusChunks) {
                val cx = centerChunkX + dx
                val cz = centerChunkZ + dz
                val key = chunkKey(cx, cz)
                requiredKeys.add(key)

                if (!activeChunks.containsKey(key)) {
                    val chunk = TerrainChunk(cx, cz, chunkSize)
                    chunk.generate()
                    activeChunks[key] = chunk
                }
            }
        }

        // Unload out-of-range chunks
        val iterator = activeChunks.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!requiredKeys.contains(entry.key)) {
                entry.value.release()
                iterator.remove()
            }
        }
    }

    /**
     * Renders all visible terrain and road meshes.
     */
    fun render(modelRenderer: ModelRenderer) {
        for (chunk in activeChunks.values) {
            MatrixUtils.buildModelMatrix(
                tempModelMatrix,
                chunk.worldOriginX, 0f, chunk.worldOriginZ,
                0f, 0f, 0f,
                1f, 1f, 1f
            )

            chunk.grassMesh?.let { mesh ->
                modelRenderer.renderMesh(mesh, tempModelMatrix, ProceduralMaterial.GRASS)
            }
            chunk.roadMesh?.let { mesh ->
                modelRenderer.renderMesh(mesh, tempModelMatrix, ProceduralMaterial.DIRT)
            }
        }
    }

    fun release() {
        for (chunk in activeChunks.values) {
            chunk.release()
        }
        activeChunks.clear()
    }
}
