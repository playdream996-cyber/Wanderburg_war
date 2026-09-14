package com.game.castlewar.model3d

import android.opengl.GLES30
import com.game.castlewar.model3d.procedural.ProceduralMaterial
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Stylized 3D terrain renderer for Wanderburg medieval aesthetic:
 * - Low-poly rolling hills with faceted normals that catch directional sunlight
 * - Visible color variation (Lush Meadow, Sunny Hill, Deep Forest, and Rocky Heights)
 * - Continuous curved dirt roads and crossroads connecting settlements
 * - Cobblestone village plazas and clearings under fortified outposts
 */
class TerrainRenderer3D {

    companion object {
        const val CHUNK_SIZE = 400f
        const val CHUNK_GRID = 6 // 6x6 low-poly quads per chunk
        const val WORLD_BOUNDS = 2400f

        /**
         * Computes the terrain height at any world coordinate (x, z).
         * Designed so the moving castle travels smoothly along roads and bridges.
         */
        fun getTerrainHeight(x: Float, z: Float): Float {
            // Check King's Highway bridge area over the river
            val isBridgeCorridor = kotlin.math.abs(x) < 55f && z in -730f..-570f
            if (isBridgeCorridor) {
                return 0.5f // Level bridge roadway
            }

            // Riverbed depression (curving from southwest to southeast)
            val riverZ = -650f - 0.22f * x - 0.00012f * x * x
            val riverDist = kotlin.math.abs(z - riverZ)
            if (riverDist < 75f) {
                val dip = (1f - (riverDist / 75f)) * 4.2f
                return -3.2f - dip
            }

            // Road flattening factor so roads remain smooth and non-bumpy
            val nearHighway = kotlin.math.abs(x) < 70f
            val roadFactor = if (nearHighway) 0.35f else 1.0f

            // Soft rolling medieval terrain with gentle hills and small slopes
            val rolling = (sin(x * 0.0055f) * cos(z * 0.0055f) * 5.5f +
                           sin(x * 0.015f + z * 0.010f) * 2.2f)

            // Highland elevation in northern enemy territory
            val highland = if (z > 450f) ((z - 450f) * 0.008f).coerceAtMost(6.5f) else 0f

            return (rolling + highland) * roadFactor
        }
    }

    // Faceted terrain chunk mesh
    private var terrainVaoId: Int = 0
    private var terrainVboId: Int = 0
    private var terrainVertexCount: Int = 0

    // Road network & village plaza mesh
    private var roadVaoId: Int = 0
    private var roadVboId: Int = 0
    private var roadIndexCount: Int = 0

    // Stylized river mesh
    private var waterVaoId: Int = 0
    private var waterVboId: Int = 0
    private var waterIndexCount: Int = 0

    private var isInitialized: Boolean = false

    // Wanderburg stylized terrain materials
    private val lushGrassMaterial = Material(
        name = "LushGrass",
        baseColorR = 0.28f, baseColorG = 0.66f, baseColorB = 0.22f,
        roughness = 0.92f, metallic = 0.0f
    )
    private val sunnyGrassMaterial = Material(
        name = "SunnyGrass",
        baseColorR = 0.36f, baseColorG = 0.72f, baseColorB = 0.26f,
        roughness = 0.90f, metallic = 0.0f
    )
    private val forestGrassMaterial = Material(
        name = "ForestGrass",
        baseColorR = 0.18f, baseColorG = 0.48f, baseColorB = 0.16f,
        roughness = 0.95f, metallic = 0.0f
    )
    private val rockyHillMaterial = Material(
        name = "RockyHill",
        baseColorR = 0.50f, baseColorG = 0.48f, baseColorB = 0.44f,
        roughness = 0.96f, metallic = 0.0f
    )
    private val scorchedEarthMaterial = Material(
        name = "ScorchedEarth",
        baseColorR = 0.34f, baseColorG = 0.28f, baseColorB = 0.24f,
        roughness = 0.96f, metallic = 0.0f
    )
    private val dirtRoadMaterial = Material(
        name = "WanderburgDirtRoad",
        baseColorR = 0.60f, baseColorG = 0.42f, baseColorB = 0.26f,
        roughness = 0.94f, metallic = 0.0f
    )
    private val waterMaterial = Material(
        name = "StylizedWater",
        baseColorR = 0.18f, baseColorG = 0.62f, baseColorB = 0.74f, baseColorA = 0.88f,
        roughness = 0.18f, metallic = 0.08f
    )
    private val cobblestonePlazaMaterial = Material(
        name = "WanderburgPlaza",
        baseColorR = 0.55f, baseColorG = 0.52f, baseColorB = 0.48f,
        roughness = 0.90f, metallic = 0.02f
    )

    private val tempModelMatrix = FloatArray(16)

    fun initializeGL() {
        if (isInitialized) return

        buildFacetedTerrainMesh()
        buildRiverMesh()
        buildRoadNetworkMesh()

        isInitialized = true
    }

    /**
     * Builds a winding stylized river ribbon flowing across the southern landscape.
     */
    private fun buildRiverMesh() {
        val riverVerts = mutableListOf<Float>()
        val riverIndices = mutableListOf<Int>()
        val riverWidth = 120f

        // Sample points along the river curve
        val points = listOf(
            Pair(-1400f, -380f),
            Pair(-1000f, -480f),
            Pair(-600f, -570f),
            Pair(-200f, -630f),
            Pair(0f, -650f),
            Pair(250f, -680f),
            Pair(600f, -740f),
            Pair(1000f, -840f),
            Pair(1400f, -920f)
        )

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val dx = p1.first - p0.first
            val dz = p1.second - p0.second
            val len = sqrt(dx * dx + dz * dz).coerceAtLeast(1f)
            val nx = -dz / len * (riverWidth * 0.5f)
            val nz = dx / len * (riverWidth * 0.5f)

            val baseIndex = riverVerts.size / Mesh.FLOATS_PER_VERTEX
            val y = -2.6f

            // Quad for river segment
            addVertex(riverVerts, p0.first + nx, y, p0.second + nz, 0f, 1f, 0f, 0f, 0f)
            addVertex(riverVerts, p0.first - nx, y, p0.second - nz, 0f, 1f, 0f, 1f, 0f)
            addVertex(riverVerts, p1.first - nx, y, p1.second - nz, 0f, 1f, 0f, 1f, 1f)
            addVertex(riverVerts, p1.first + nx, y, p1.second + nz, 0f, 1f, 0f, 0f, 1f)

            riverIndices.add(baseIndex)
            riverIndices.add(baseIndex + 1)
            riverIndices.add(baseIndex + 2)
            riverIndices.add(baseIndex)
            riverIndices.add(baseIndex + 2)
            riverIndices.add(baseIndex + 3)
        }

        waterIndexCount = riverIndices.size

        val vaos = IntArray(1)
        val vbos = IntArray(1)
        val ebos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        GLES30.glGenBuffers(1, ebos, 0)
        waterVaoId = vaos[0]
        waterVboId = vbos[0]

        GLES30.glBindVertexArray(waterVaoId)

        val vArr = riverVerts.toFloatArray()
        val vBuffer: FloatBuffer = ByteBuffer.allocateDirect(vArr.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vArr)
        vBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, waterVboId)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vArr.size * 4, vBuffer, GLES30.GL_STATIC_DRAW)

        val iArr = riverIndices.toIntArray()
        val iBuffer: IntBuffer = ByteBuffer.allocateDirect(iArr.size * 4)
            .order(ByteOrder.nativeOrder()).asIntBuffer().put(iArr)
        iBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebos[0])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, iArr.size * 4, iBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_POS)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_NORMAL)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_UV)

        GLES30.glBindVertexArray(0)
    }

    /**
     * Builds a low-poly faceted terrain chunk where each triangle has its own flat normal.
     * This creates the iconic stylized faceted Wanderburg terrain look.
     */
    private fun buildFacetedTerrainMesh() {
        val step = CHUNK_SIZE / CHUNK_GRID
        val quads = CHUNK_GRID * CHUNK_GRID
        val totalTriangles = quads * 2
        terrainVertexCount = totalTriangles * 3

        val vertices = FloatArray(terrainVertexCount * Mesh.FLOATS_PER_VERTEX)
        var vIdx = 0

        for (iz in 0 until CHUNK_GRID) {
            val z0 = -CHUNK_SIZE * 0.5f + iz * step
            val z1 = z0 + step
            for (ix in 0 until CHUNK_GRID) {
                val x0 = -CHUNK_SIZE * 0.5f + ix * step
                val x1 = x0 + step

                val y00 = getTerrainHeight(x0, z0)
                val y10 = getTerrainHeight(x1, z0)
                val y01 = getTerrainHeight(x0, z1)
                val y11 = getTerrainHeight(x1, z1)

                // Triangle 1: (x0, y00, z0), (x1, y10, z0), (x1, y11, z1)
                addFacetedTriangle(vertices, vIdx, x0, y00, z0, x1, y10, z0, x1, y11, z1)
                vIdx += 3 * Mesh.FLOATS_PER_VERTEX

                // Triangle 2: (x0, y00, z0), (x1, y11, z1), (x0, y01, z1)
                addFacetedTriangle(vertices, vIdx, x0, y00, z0, x1, y11, z1, x0, y01, z1)
                vIdx += 3 * Mesh.FLOATS_PER_VERTEX
            }
        }

        val vaos = IntArray(1)
        val vbos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        terrainVaoId = vaos[0]
        terrainVboId = vbos[0]

        GLES30.glBindVertexArray(terrainVaoId)

        val vBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, terrainVboId)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, vBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_POS)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_NORMAL)

        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_UV)

        GLES30.glBindVertexArray(0)
    }

    private fun addFacetedTriangle(
        arr: FloatArray, offset: Int,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float
    ) {
        // Compute flat face normal via cross product
        val ax = x2 - x1; val ay = y2 - y1; val az = z2 - z1
        val bx = x3 - x1; val by = y3 - y1; val bz = z3 - z1
        var nx = ay * bz - az * by
        var ny = az * bx - ax * bz
        var nz = ax * by - ay * bx
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
        nx /= len; ny /= len; nz /= len

        var idx = offset

        // Vertex 1
        arr[idx++] = x1; arr[idx++] = y1; arr[idx++] = z1
        arr[idx++] = nx; arr[idx++] = ny; arr[idx++] = nz
        arr[idx++] = 0f; arr[idx++] = 0f

        // Vertex 2
        arr[idx++] = x2; arr[idx++] = y2; arr[idx++] = z2
        arr[idx++] = nx; arr[idx++] = ny; arr[idx++] = nz
        arr[idx++] = 1f; arr[idx++] = 0f

        // Vertex 3
        arr[idx++] = x3; arr[idx++] = y3; arr[idx++] = z3
        arr[idx++] = nx; arr[idx++] = ny; arr[idx++] = nz
        arr[idx++] = 0.5f; arr[idx++] = 1f
    }

    /**
     * Builds the curved dirt road ribbons and circular village cobblestone clearings.
     * Raised slightly (0.12f above terrain) to prevent depth fighting.
     */
    private fun buildRoadNetworkMesh() {
        val roadVerts = mutableListOf<Float>()
        val roadIndices = mutableListOf<Int>()

        val roadWidth = 110f // Wide enough for moving castle hull

        // Road waypoints connecting central crossroads to villages and fortresses
        val routes = listOf(
            // North-South King's Highway
            listOf(Pair(0f, -1400f), Pair(0f, -650f), Pair(0f, 0f), Pair(50f, 350f), Pair(0f, 800f), Pair(0f, 1500f)),
            // West branch (through Riverbend village and Forest Outpost)
            listOf(Pair(0f, 0f), Pair(-180f, -100f), Pair(-350f, -250f), Pair(-550f, -180f), Pair(-800f, -50f)),
            // Northwest branch (Village clearings)
            listOf(Pair(0f, 0f), Pair(-220f, 180f), Pair(-450f, 400f)),
            // East branch (Eastern garrison and ruins)
            listOf(Pair(0f, 0f), Pair(200f, -150f), Pair(400f, -300f), Pair(620f, -200f), Pair(850f, -100f)),
            // Northeast branch (Fortified checkpoint)
            listOf(Pair(50f, 350f), Pair(260f, 400f), Pair(500f, 450f))
        )

        for (route in routes) {
            for (i in 0 until route.size - 1) {
                val p0 = route[i]
                val p1 = route[i + 1]
                addRoadSegment(roadVerts, roadIndices, p0.first, p0.second, p1.first, p1.second, roadWidth)
            }
        }

        // Circular village clearings / plazas under settlements
        val plazas = listOf(
            Pair(0f, 0f),       // Spawn crossroads
            Pair(-350f, -250f), // Village 1
            Pair(400f, -300f),  // Garrison
            Pair(-450f, 400f),  // Farmstead
            Pair(500f, 450f),   // Outpost
            Pair(-800f, -50f),  // Forest Camp
            Pair(850f, -100f),  // East ruins
            Pair(0f, -650f),    // South gate
            Pair(0f, 1400f)     // Boss fortress citadel
        )

        for (plaza in plazas) {
            addPlazaDisc(roadVerts, roadIndices, plaza.first, plaza.second, radius = 135f)
        }

        roadIndexCount = roadIndices.size

        val vaos = IntArray(1)
        val vbos = IntArray(1)
        val ebos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        GLES30.glGenBuffers(1, ebos, 0)
        roadVaoId = vaos[0]
        roadVboId = vbos[0]

        GLES30.glBindVertexArray(roadVaoId)

        val vArr = roadVerts.toFloatArray()
        val vBuffer: FloatBuffer = ByteBuffer.allocateDirect(vArr.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vArr)
        vBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, roadVboId)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vArr.size * 4, vBuffer, GLES30.GL_STATIC_DRAW)

        val iArr = roadIndices.toIntArray()
        val iBuffer: IntBuffer = ByteBuffer.allocateDirect(iArr.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(iArr)
        iBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebos[0])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, iArr.size * 4, iBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_POS)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_NORMAL)

        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, Mesh.STRIDE_BYTES, Mesh.OFFSET_UV)

        GLES30.glBindVertexArray(0)
    }

    private fun addRoadSegment(
        verts: MutableList<Float>, indices: MutableList<Int>,
        x0: Float, z0: Float, x1: Float, z1: Float, width: Float
    ) {
        val dx = x1 - x0
        val dz = z1 - z0
        val len = sqrt(dx * dx + dz * dz).coerceAtLeast(1f)
        val nx = -dz / len * (width * 0.5f)
        val nz = dx / len * (width * 0.5f)

        val baseIndex = verts.size / Mesh.FLOATS_PER_VERTEX

        val y0 = getTerrainHeight(x0, z0) + 0.14f
        val y1 = getTerrainHeight(x1, z1) + 0.14f

        // V0: Left start
        addVertex(verts, x0 + nx, y0, z0 + nz, 0f, 1f, 0f, 0f, 0f)
        // V1: Right start
        addVertex(verts, x0 - nx, y0, z0 - nz, 0f, 1f, 0f, 1f, 0f)
        // V2: Right end
        addVertex(verts, x1 - nx, y1, z1 - nz, 0f, 1f, 0f, 1f, 1f)
        // V3: Left end
        addVertex(verts, x1 + nx, y1, z1 + nz, 0f, 1f, 0f, 0f, 1f)

        indices.add(baseIndex)
        indices.add(baseIndex + 1)
        indices.add(baseIndex + 2)
        indices.add(baseIndex)
        indices.add(baseIndex + 2)
        indices.add(baseIndex + 3)
    }

    private fun addPlazaDisc(
        verts: MutableList<Float>, indices: MutableList<Int>,
        cx: Float, cz: Float, radius: Float
    ) {
        val segments = 12
        val baseIndex = verts.size / Mesh.FLOATS_PER_VERTEX
        val cy = getTerrainHeight(cx, cz) + 0.12f

        // Center vertex
        addVertex(verts, cx, cy, cz, 0f, 1f, 0f, 0.5f, 0.5f)

        for (i in 0 until segments) {
            val angle = (i * 2f * PI.toFloat() / segments)
            val px = cx + cos(angle) * radius
            val pz = cz + sin(angle) * radius
            val py = getTerrainHeight(px, pz) + 0.12f
            addVertex(verts, px, py, pz, 0f, 1f, 0f, (cos(angle) + 1f) * 0.5f, (sin(angle) + 1f) * 0.5f)
        }

        for (i in 0 until segments) {
            val i0 = baseIndex
            val i1 = baseIndex + 1 + i
            val i2 = baseIndex + 1 + ((i + 1) % segments)
            indices.add(i0)
            indices.add(i1)
            indices.add(i2)
        }
    }

    private fun addVertex(
        verts: MutableList<Float>,
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        u: Float, v: Float
    ) {
        verts.add(x); verts.add(y); verts.add(z)
        verts.add(nx); verts.add(ny); verts.add(nz)
        verts.add(u); verts.add(v)
    }

    /**
     * Renders visible terrain chunks and road network using distance culling.
     */
    fun render(modelRenderer: ModelRenderer, camX: Float, camZ: Float) {
        if (!isInitialized) initializeGL()

        val renderDist = 1350f
        val startX = (camX - renderDist)
        val endX = (camX + renderDist)
        val startZ = (camZ - renderDist)
        val endZ = (camZ + renderDist)

        val chunkRadius = (CHUNK_SIZE * 0.5f)

        // 1. Render Base Rolling Terrain with varied biome palettes
        var cx = -WORLD_BOUNDS + chunkRadius
        while (cx <= WORLD_BOUNDS) {
            var cz = -WORLD_BOUNDS + chunkRadius
            while (cz <= WORLD_BOUNDS) {
                if (cx in startX..endX && cz in startZ..endZ) {
                    MatrixUtils.buildModelMatrix(
                        tempModelMatrix,
                        cx, 0f, cz,
                        0f, 0f, 0f,
                        1f, 1f, 1f
                    )

                    // Choose biome material for visible natural color variation
                    val mat = when {
                        // Northern war-torn area (heavy fortifications / destroyed terrain)
                        cz > 650f -> scorchedEarthMaterial
                        // Elevated rocky regions
                        (cx > 600f && cz > 400f) || (cx < -700f && cz < -400f) -> rockyHillMaterial
                        // Deep forest perimeter
                        cx < -500f || cz < -600f -> forestGrassMaterial
                        // Sunny southern / central meadows
                        (cx + cz) % 300f > 100f -> sunnyGrassMaterial
                        // Lush green base
                        else -> lushGrassMaterial
                    }

                    modelRenderer.setMaterialAndTransform(tempModelMatrix, mat)
                    GLES30.glBindVertexArray(terrainVaoId)
                    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, terrainVertexCount)
                    GLES30.glBindVertexArray(0)
                }
                cz += CHUNK_SIZE
            }
            cx += CHUNK_SIZE
        }

        // 2. Render Stylized River Water Ribbon
        MatrixUtils.buildModelMatrix(tempModelMatrix, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f)
        modelRenderer.setMaterialAndTransform(tempModelMatrix, waterMaterial)
        GLES30.glBindVertexArray(waterVaoId)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, waterIndexCount, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)

        // 3. Render Curved Dirt Roads & Village Plazas
        MatrixUtils.buildModelMatrix(tempModelMatrix, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f)
        modelRenderer.setMaterialAndTransform(tempModelMatrix, dirtRoadMaterial)
        GLES30.glBindVertexArray(roadVaoId)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, roadIndexCount, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)
    }

    fun release() {
        if (isInitialized) {
            GLES30.glDeleteVertexArrays(3, intArrayOf(terrainVaoId, roadVaoId, waterVaoId), 0)
            GLES30.glDeleteBuffers(3, intArrayOf(terrainVboId, roadVboId, waterVboId), 0)
            isInitialized = false
        }
    }
}
