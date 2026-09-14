package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Mesh
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Procedural low-poly terrain chunk with gentle height variation, winding dirt roads,
 * and lush green grass for the Wanderburg medieval countryside.
 */
class TerrainChunk(
    val chunkX: Int,
    val chunkZ: Int,
    val size: Float = 120f,
    val resolution: Int = 12
) {
    var grassMesh: Mesh? = null
        private set
    var roadMesh: Mesh? = null
        private set

    val worldOriginX: Float = chunkX * size
    val worldOriginZ: Float = chunkZ * size

    fun generate() {
        val grassData = MeshData.empty()
        val roadData = MeshData.empty()

        val step = size / resolution
        val halfSize = size * 0.5f

        val verts = mutableListOf<Float>()
        val inds = mutableListOf<Int>()

        val roadVerts = mutableListOf<Float>()
        val roadInds = mutableListOf<Int>()

        for (z in 0..resolution) {
            val wz = worldOriginZ - halfSize + z * step
            for (x in 0..resolution) {
                val wx = worldOriginX - halfSize + x * step

                // Gentle rolling hills height formula
                val h = (sin(wx * 0.008) * cos(wz * 0.008) * 6.0 +
                         sin(wx * 0.02) * 2.5).toFloat()

                // Dirt road path formula: Winding north-south road along X = sin(Z * 0.015) * 45
                val roadCenter = (sin(wz * 0.015) * 45.0).toFloat()
                val distToRoad = kotlin.math.abs(wx - roadCenter)
                val isRoad = distToRoad < 9.0f

                // Normal estimation
                val hL = (sin((wx - 1f) * 0.008) * cos(wz * 0.008) * 6.0).toFloat()
                val hR = (sin((wx + 1f) * 0.008) * cos(wz * 0.008) * 6.0).toFloat()
                val hD = (sin(wx * 0.008) * cos((wz - 1f) * 0.008) * 6.0).toFloat()
                val hU = (sin(wx * 0.008) * cos((wz + 1f) * 0.008) * 6.0).toFloat()

                val nx = (hL - hR) * 0.5f
                val ny = 1.0f
                val nz = (hD - hU) * 0.5f
                val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)

                val lx = -halfSize + x * step
                val lz = -halfSize + z * step

                if (isRoad) {
                    // Slight elevation on dirt road to prevent z-fighting
                    roadVerts.addAll(listOf(lx, h + 0.15f, lz,  nx / len, ny / len, nz / len,  x.toFloat() / resolution, z.toFloat() / resolution))
                }
                verts.addAll(listOf(lx, h, lz,  nx / len, ny / len, nz / len,  x.toFloat() / resolution, z.toFloat() / resolution))
            }
        }

        val stride = resolution + 1
        for (z in 0 until resolution) {
            for (x in 0 until resolution) {
                val v0 = z * stride + x
                val v1 = v0 + 1
                val v2 = (z + 1) * stride + x
                val v3 = v2 + 1

                inds.addAll(listOf(v0, v2, v1, v1, v2, v3))
            }
        }

        grassData.vertices = verts.toFloatArray()
        grassData.indices = inds.toIntArray()
        grassMesh = grassData.toMesh(ProceduralMaterial.GRASS)

        // Road strip
        if (roadVerts.size >= 8 * 4) {
            val rStride = resolution + 1
            for (z in 0 until resolution) {
                for (x in 0 until resolution) {
                    val v0 = z * rStride + x
                    val v1 = v0 + 1
                    val v2 = (z + 1) * rStride + x
                    val v3 = v2 + 1
                    if (v3 < roadVerts.size / 8) {
                        roadInds.addAll(listOf(v0, v2, v1, v1, v2, v3))
                    }
                }
            }
            roadData.vertices = roadVerts.toFloatArray()
            roadData.indices = roadInds.toIntArray()
            roadMesh = roadData.toMesh(ProceduralMaterial.DIRT)
        }
    }

    fun release() {
        grassMesh?.release()
        roadMesh?.release()
    }
}
