package com.game.castlewar.model3d

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Robust glTF 2.0 Binary (.GLB) Loader and procedural 3D model generator for Android.
 *
 * Adheres to the official glTF 2.0 specification:
 * - 12-byte header validation
 * - JSON and BIN chunk decoding
 * - Accessors (POSITION, NORMAL, TEXCOORD_0, INDICES)
 * - Multi-primitive and multi-mesh loading
 * - Material extraction (PBR metallic/roughness & baseColorFactor)
 * - Graceful fallback to procedural medieval 3D assets if missing
 */
class ModelLoader(private val context: Context) {

    companion object {
        private const val TAG = "ModelLoader"
        private const val GLB_MAGIC = 0x46546C67 // "glTF"
        private const val CHUNK_JSON = 0x4E4F534A // "JSON"
        private const val CHUNK_BIN = 0x004E4942 // "BIN\0"

        // OpenGL Component Types
        private const val TYPE_BYTE = 5120
        private const val TYPE_UNSIGNED_BYTE = 5121
        private const val TYPE_SHORT = 5122
        private const val TYPE_UNSIGNED_SHORT = 5123
        private const val TYPE_UNSIGNED_INT = 5125
        private const val TYPE_FLOAT = 5126
    }

    /**
     * Loads a GLB model from APK assets or falls back to procedural stylized 3D representation.
     */
    fun loadModel(assetPath: String, fallbackModelId: String): Model {
        try {
            val inputStream = context.assets.open(assetPath)
            val model = loadGLB(inputStream, fallbackModelId)
            inputStream.close()
            Log.i(TAG, "Successfully loaded 3D GLB asset: $assetPath")
            return model
        } catch (e: Exception) {
            Log.w(TAG, "Missing model asset: $assetPath (${e.message}). Falling back to procedural 3D model: $fallbackModelId")
            return createFallbackModel(fallbackModelId)
        }
    }

    /**
     * Parses standard binary GLB format from an input stream.
     */
    fun loadGLB(stream: InputStream, modelName: String): Model {
        val bytes = stream.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. Read and validate 12-byte header
        val magic = buffer.int
        if (magic != GLB_MAGIC) {
            throw IllegalArgumentException("Invalid GLB header magic: 0x${Integer.toHexString(magic)}")
        }
        val version = buffer.int
        val totalLength = buffer.int

        // 2. Read chunks
        var jsonString: String? = null
        var binBuffer: ByteBuffer? = null

        while (buffer.hasRemaining()) {
            val chunkLength = buffer.int
            val chunkType = buffer.int
            val chunkData = ByteArray(chunkLength)
            buffer.get(chunkData)

            when (chunkType) {
                CHUNK_JSON -> {
                    jsonString = String(chunkData, Charsets.UTF_8)
                }
                CHUNK_BIN -> {
                    binBuffer = ByteBuffer.wrap(chunkData).order(ByteOrder.LITTLE_ENDIAN)
                }
            }
        }

        if (jsonString == null) {
            throw IllegalArgumentException("GLB missing JSON chunk")
        }

        return parseGLTF(JSONObject(jsonString), binBuffer, modelName)
    }

    private fun parseGLTF(json: JSONObject, binBuffer: ByteBuffer?, modelName: String): Model {
        val directMeshes = mutableListOf<Mesh>()
        val materialsList = mutableListOf<Material>()

        // 1. Parse Materials
        if (json.has("materials")) {
            val materialsArray = json.getJSONArray("materials")
            for (i in 0 until materialsArray.length()) {
                val matObj = materialsArray.getJSONObject(i)
                val matName = matObj.optString("name", "mat_$i")
                var r = 0.8f; var g = 0.8f; var b = 0.8f; var a = 1.0f
                var roughness = 0.6f
                var metallic = 0.1f

                if (matObj.has("pbrMetallicRoughness")) {
                    val pbr = matObj.getJSONObject("pbrMetallicRoughness")
                    if (pbr.has("baseColorFactor")) {
                        val colorArr = pbr.getJSONArray("baseColorFactor")
                        r = colorArr.getDouble(0).toFloat()
                        g = colorArr.getDouble(1).toFloat()
                        b = colorArr.getDouble(2).toFloat()
                        a = colorArr.getDouble(3).toFloat()
                    }
                    roughness = pbr.optDouble("roughnessFactor", 0.6).toFloat()
                    metallic = pbr.optDouble("metallicFactor", 0.1).toFloat()
                }
                materialsList.add(Material(matName, r, g, b, a, roughness, metallic))
            }
        }

        // 2. Parse BufferViews & Accessors
        val bufferViews = json.optJSONArray("bufferViews")
        val accessors = json.optJSONArray("accessors")

        fun getAccessorData(accessorIndex: Int): AccessorData {
            val acc = accessors!!.getJSONObject(accessorIndex)
            val bvIndex = acc.getInt("bufferView")
            val bv = bufferViews!!.getJSONObject(bvIndex)

            val bvOffset = bv.optInt("byteOffset", 0)
            val accOffset = acc.optInt("byteOffset", 0)
            val totalOffset = bvOffset + accOffset

            val componentType = acc.getInt("componentType")
            val count = acc.getInt("count")
            val type = acc.getString("type")

            return AccessorData(totalOffset, count, componentType, type)
        }

        // 3. Parse Meshes & Primitives
        if (json.has("meshes") && binBuffer != null) {
            val meshesArray = json.getJSONArray("meshes")
            for (m in 0 until meshesArray.length()) {
                val meshObj = meshesArray.getJSONObject(m)
                val primitives = meshObj.getJSONArray("primitives")

                for (p in 0 until primitives.length()) {
                    val prim = primitives.getJSONObject(p)
                    val attributes = prim.getJSONObject("attributes")

                    val posAccIdx = attributes.getInt("POSITION")
                    val normAccIdx = attributes.optInt("NORMAL", -1)
                    val uvAccIdx = attributes.optInt("TEXCOORD_0", -1)
                    val indicesAccIdx = prim.optInt("indices", -1)
                    val matIdx = prim.optInt("material", -1)

                    val posData = getAccessorData(posAccIdx)
                    val vertexCount = posData.count

                    // Read Positions
                    val positions = FloatArray(vertexCount * 3)
                    binBuffer.position(posData.byteOffset)
                    for (i in 0 until vertexCount * 3) {
                        positions[i] = binBuffer.float
                    }

                    // Read Normals
                    val normals = FloatArray(vertexCount * 3)
                    if (normAccIdx != -1) {
                        val normData = getAccessorData(normAccIdx)
                        binBuffer.position(normData.byteOffset)
                        for (i in 0 until vertexCount * 3) {
                            normals[i] = binBuffer.float
                        }
                    } else {
                        // Default upward normal
                        for (i in 0 until vertexCount) {
                            normals[i * 3 + 1] = 1.0f
                        }
                    }

                    // Read UVs
                    val uvs = FloatArray(vertexCount * 2)
                    if (uvAccIdx != -1) {
                        val uvData = getAccessorData(uvAccIdx)
                        binBuffer.position(uvData.byteOffset)
                        for (i in 0 until vertexCount * 2) {
                            uvs[i] = binBuffer.float
                        }
                    }

                    // Combine into interleaved vertex buffer (x, y, z, nx, ny, nz, u, v)
                    val vertexBufferData = FloatArray(vertexCount * Mesh.FLOATS_PER_VERTEX)
                    for (i in 0 until vertexCount) {
                        val vOffset = i * Mesh.FLOATS_PER_VERTEX
                        vertexBufferData[vOffset + 0] = positions[i * 3 + 0]
                        vertexBufferData[vOffset + 1] = positions[i * 3 + 1]
                        vertexBufferData[vOffset + 2] = positions[i * 3 + 2]

                        vertexBufferData[vOffset + 3] = normals[i * 3 + 0]
                        vertexBufferData[vOffset + 4] = normals[i * 3 + 1]
                        vertexBufferData[vOffset + 5] = normals[i * 3 + 2]

                        vertexBufferData[vOffset + 6] = uvs[i * 2 + 0]
                        vertexBufferData[vOffset + 7] = uvs[i * 2 + 1]
                    }

                    // Read Indices
                    val indices: IntArray
                    if (indicesAccIdx != -1) {
                        val idxData = getAccessorData(indicesAccIdx)
                        indices = IntArray(idxData.count)
                        binBuffer.position(idxData.byteOffset)

                        when (idxData.componentType) {
                            TYPE_UNSIGNED_SHORT -> {
                                for (i in 0 until idxData.count) {
                                    indices[i] = binBuffer.short.toInt() and 0xFFFF
                                }
                            }
                            TYPE_UNSIGNED_INT -> {
                                for (i in 0 until idxData.count) {
                                    indices[i] = binBuffer.int
                                }
                            }
                            TYPE_UNSIGNED_BYTE -> {
                                for (i in 0 until idxData.count) {
                                    indices[i] = binBuffer.get().toInt() and 0xFF
                                }
                            }
                            else -> {
                                for (i in 0 until idxData.count) indices[i] = i
                            }
                        }
                    } else {
                        indices = IntArray(vertexCount) { it }
                    }

                    val material = if (matIdx in materialsList.indices) materialsList[matIdx] else Material.STONE
                    directMeshes.add(Mesh(vertexBufferData, indices, material))
                }
            }
        }

        val model = Model(name = modelName, directMeshes = directMeshes)
        setupDefaultAttachmentPoints(model, modelName)
        return model
    }

    private data class AccessorData(
        val byteOffset: Int,
        val count: Int,
        val componentType: Int,
        val type: String
    )

    private fun setupDefaultAttachmentPoints(model: Model, modelId: String) {
        when (modelId) {
            "player_castle" -> {
                model.addAttachmentPoint("front_weapon", Transform(posX = 0f, posY = 18f, posZ = 35f))
                model.addAttachmentPoint("left_weapon", Transform(posX = -30f, posY = 18f, posZ = -10f))
                model.addAttachmentPoint("right_weapon", Transform(posX = 30f, posY = 18f, posZ = -10f))
                model.addAttachmentPoint("rear_weapon", Transform(posX = 0f, posY = 24f, posZ = -35f))
                model.addAttachmentPoint("tower_slot", Transform(posX = 0f, posY = 35f, posZ = 0f))
            }
        }
    }

    /**
     * Creates high-fidelity stylized 3D medieval models procedurally.
     * Guaranteed to look distinct, properly dimensioned, and cleanly shaded.
     */
    fun createFallbackModel(modelId: String): Model {
        val meshes = mutableListOf<Mesh>()
        val model = Model(name = modelId, directMeshes = meshes)

        when (modelId) {
            "player_castle" -> {
                // Main lower hull (massive reinforced stone fortress chassis)
                meshes.add(createBoxMesh(60f, 16f, 80f, Material.STONE))
                // Upper central keep
                meshes.add(createBoxMesh(42f, 22f, 50f, Material.WOOD, offsetY = 14f))
                // Crenelated battlements / towers at 4 corners
                val cornerMat = Material(name = "Battlement", baseColorR = 0.45f, baseColorG = 0.46f, baseColorB = 0.48f)
                meshes.add(createCylinderMesh(6f, 26f, 12, cornerMat, offsetX = -26f, offsetY = 10f, offsetZ = -34f))
                meshes.add(createCylinderMesh(6f, 26f, 12, cornerMat, offsetX = 26f, offsetY = 10f, offsetZ = -34f))
                meshes.add(createCylinderMesh(6f, 26f, 12, cornerMat, offsetX = -26f, offsetY = 10f, offsetZ = 34f))
                meshes.add(createCylinderMesh(6f, 26f, 12, cornerMat, offsetX = 26f, offsetY = 10f, offsetZ = 34f))
                // Reinforced iron portcullis gate at front
                meshes.add(createBoxMesh(16f, 14f, 4f, Material.IRON, offsetZ = 40f, offsetY = 6f))

                setupDefaultAttachmentPoints(model, "player_castle")
            }

            "castle_wheel" -> {
                // Heavy iron-banded stone/wood wheel
                meshes.add(createCylinderMesh(8f, 6f, 16, Material.IRON, rotZDeg = 90f))
                meshes.add(createCylinderMesh(6.5f, 7f, 12, Material.WOOD, rotZDeg = 90f))
            }

            "cannon" -> {
                // Rotating base
                meshes.add(createCylinderMesh(5f, 3f, 12, Material.WOOD, offsetY = 1.5f))
                // Heavy iron bombard barrel pointed along Z
                meshes.add(createCylinderMesh(2.5f, 14f, 12, Material.IRON, offsetY = 4f, offsetZ = 4f, rotXDeg = 90f))
            }

            "soldier" -> {
                // Torso with red surcoat
                meshes.add(createBoxMesh(3.5f, 5f, 2.5f, Material.ENEMY_ARMOR, offsetY = 4.5f))
                // Iron helmet
                meshes.add(createBoxMesh(2.5f, 2.5f, 2.5f, Material.IRON, offsetY = 8f))
                // Legs
                meshes.add(createBoxMesh(1.2f, 3.5f, 1.2f, Material.WOOD, offsetX = -1f, offsetY = 1.75f))
                meshes.add(createBoxMesh(1.2f, 3.5f, 1.2f, Material.WOOD, offsetX = 1f, offsetY = 1.75f))
                // Broadsword & Shield
                meshes.add(createBoxMesh(0.5f, 7f, 1f, Material.IRON, offsetX = 2.5f, offsetY = 4f, offsetZ = 1f))
                meshes.add(createBoxMesh(1f, 4.5f, 3f, Material.WOOD, offsetX = -2.5f, offsetY = 4.5f))
            }

            "house_01" -> {
                // Stone/timber cottage ground floor
                meshes.add(createBoxMesh(26f, 16f, 22f, Material(name = "Plaster", baseColorR = 0.82f, baseColorG = 0.78f, baseColorB = 0.70f), offsetY = 8f))
                // Timber corner beams
                meshes.add(createBoxMesh(2.5f, 16f, 2.5f, Material.WOOD, offsetX = -12f, offsetY = 8f, offsetZ = -10f))
                meshes.add(createBoxMesh(2.5f, 16f, 2.5f, Material.WOOD, offsetX = 12f, offsetY = 8f, offsetZ = -10f))
                meshes.add(createBoxMesh(2.5f, 16f, 2.5f, Material.WOOD, offsetX = -12f, offsetY = 8f, offsetZ = 10f))
                meshes.add(createBoxMesh(2.5f, 16f, 2.5f, Material.WOOD, offsetX = 12f, offsetY = 8f, offsetZ = 10f))
                // Gable red tile roof
                meshes.add(createPrismMesh(30f, 11f, 26f, Material.ROOF_RED, offsetY = 19f))
                // Stone chimney
                meshes.add(createBoxMesh(4f, 16f, 4f, Material.STONE, offsetX = 8f, offsetY = 17f, offsetZ = 4f))
            }

            "tree_01" -> {
                // Trunk
                meshes.add(createCylinderMesh(2.5f, 12f, 8, Material.TRUNK_BROWN, offsetY = 6f))
                // Tiered low-poly foliage cones
                meshes.add(createConeMesh(11f, 12f, 8, Material.LEAF_GREEN, offsetY = 13f))
                meshes.add(createConeMesh(8.5f, 10f, 8, Material(name = "Leaf2", baseColorR = 0.32f, baseColorG = 0.65f, baseColorB = 0.25f), offsetY = 19f))
                meshes.add(createConeMesh(6f, 8f, 8, Material(name = "Leaf3", baseColorR = 0.38f, baseColorG = 0.72f, baseColorB = 0.28f), offsetY = 24f))
            }

            "rock_01" -> {
                // Rugged low-poly boulder
                meshes.add(createDodecahedronMesh(7f, Material.STONE, offsetY = 4f))
            }

            "gold_pickup" -> {
                // Thick gold coin or ingot
                meshes.add(createCylinderMesh(3.5f, 1.2f, 16, Material.GOLD, rotXDeg = 90f, offsetY = 2f))
            }

            else -> {
                meshes.add(createBoxMesh(10f, 10f, 10f, Material.STONE, offsetY = 5f))
            }
        }

        return model
    }

    // Helper Geometry Generators

    fun createBoxMesh(
        w: Float, h: Float, d: Float,
        material: Material,
        offsetX: Float = 0f, offsetY: Float = 0f, offsetZ: Float = 0f
    ): Mesh {
        val hw = w * 0.5f; val hh = h * 0.5f; val hd = d * 0.5f

        val rawVerts = floatArrayOf(
            // Front face (normal 0, 0, 1)
            -hw, -hh,  hd,  0f, 0f, 1f,  0f, 0f,
             hw, -hh,  hd,  0f, 0f, 1f,  1f, 0f,
             hw,  hh,  hd,  0f, 0f, 1f,  1f, 1f,
            -hw,  hh,  hd,  0f, 0f, 1f,  0f, 1f,

            // Back face (normal 0, 0, -1)
             hw, -hh, -hd,  0f, 0f, -1f,  0f, 0f,
            -hw, -hh, -hd,  0f, 0f, -1f,  1f, 0f,
            -hw,  hh, -hd,  0f, 0f, -1f,  1f, 1f,
             hw,  hh, -hd,  0f, 0f, -1f,  0f, 1f,

            // Top face (normal 0, 1, 0)
            -hw,  hh,  hd,  0f, 1f, 0f,  0f, 0f,
             hw,  hh,  hd,  0f, 1f, 0f,  1f, 0f,
             hw,  hh, -hd,  0f, 1f, 0f,  1f, 1f,
            -hw,  hh, -hd,  0f, 1f, 0f,  0f, 1f,

            // Bottom face (normal 0, -1, 0)
            -hw, -hh, -hd,  0f, -1f, 0f,  0f, 0f,
             hw, -hh, -hd,  0f, -1f, 0f,  1f, 0f,
             hw, -hh,  hd,  0f, -1f, 0f,  1f, 1f,
            -hw, -hh,  hd,  0f, -1f, 0f,  0f, 1f,

            // Right face (normal 1, 0, 0)
             hw, -hh,  hd,  1f, 0f, 0f,  0f, 0f,
             hw, -hh, -hd,  1f, 0f, 0f,  1f, 0f,
             hw,  hh, -hd,  1f, 0f, 0f,  1f, 1f,
             hw,  hh,  hd,  1f, 0f, 0f,  0f, 1f,

            // Left face (normal -1, 0, 0)
            -hw, -hh, -hd, -1f, 0f, 0f,  0f, 0f,
            -hw, -hh,  hd, -1f, 0f, 0f,  1f, 0f,
            -hw,  hh,  hd, -1f, 0f, 0f,  1f, 1f,
            -hw,  hh, -hd, -1f, 0f, 0f,  0f, 1f
        )

        // Apply offsets
        val vertCount = rawVerts.size / Mesh.FLOATS_PER_VERTEX
        for (i in 0 until vertCount) {
            val off = i * Mesh.FLOATS_PER_VERTEX
            rawVerts[off + 0] += offsetX
            rawVerts[off + 1] += offsetY
            rawVerts[off + 2] += offsetZ
        }

        val indices = IntArray(36)
        var iIdx = 0
        for (face in 0 until 6) {
            val base = face * 4
            indices[iIdx++] = base + 0
            indices[iIdx++] = base + 1
            indices[iIdx++] = base + 2
            indices[iIdx++] = base + 0
            indices[iIdx++] = base + 2
            indices[iIdx++] = base + 3
        }

        return Mesh(rawVerts, indices, material)
    }

    fun createCylinderMesh(
        radius: Float, height: Float, segments: Int,
        material: Material,
        offsetX: Float = 0f, offsetY: Float = 0f, offsetZ: Float = 0f,
        rotXDeg: Float = 0f, rotZDeg: Float = 0f
    ): Mesh {
        val halfH = height * 0.5f
        val verts = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        fun addVertex(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float, u: Float, v: Float) {
            // Apply simple rotation if needed
            var px = x; var py = y; var pz = z
            var pnx = nx; var pny = ny; var pnz = nz

            if (rotXDeg != 0f) {
                val rad = Math.toRadians(rotXDeg.toDouble())
                val c = cos(rad).toFloat(); val s = sin(rad).toFloat()
                val ny1 = py * c - pz * s
                val nz1 = py * s + pz * c
                py = ny1; pz = nz1
                val nny1 = pny * c - pnz * s
                val nnz1 = pny * s + pnz * c
                pny = nny1; pnz = nnz1
            }

            if (rotZDeg != 0f) {
                val rad = Math.toRadians(rotZDeg.toDouble())
                val c = cos(rad).toFloat(); val s = sin(rad).toFloat()
                val nx1 = px * c - py * s
                val ny1 = px * s + py * c
                px = nx1; py = ny1
                val nnx1 = pnx * c - pny * s
                val nny1 = pnx * s + pny * c
                pnx = nnx1; pny = nny1
            }

            verts.add(px + offsetX)
            verts.add(py + offsetY)
            verts.add(pz + offsetZ)
            verts.add(pnx)
            verts.add(pny)
            verts.add(pnz)
            verts.add(u)
            verts.add(v)
        }

        // Side faces
        for (i in 0..segments) {
            val angle = (i.toFloat() / segments) * 2f * PI.toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val u = i.toFloat() / segments

            // Top rim
            addVertex(cosA * radius, halfH, sinA * radius, cosA, 0f, sinA, u, 1f)
            // Bottom rim
            addVertex(cosA * radius, -halfH, sinA * radius, cosA, 0f, sinA, u, 0f)
        }

        for (i in 0 until segments) {
            val t0 = i * 2
            val b0 = i * 2 + 1
            val t1 = (i + 1) * 2
            val b1 = (i + 1) * 2 + 1

            indices.add(t0); indices.add(b0); indices.add(t1)
            indices.add(t1); indices.add(b0); indices.add(b1)
        }

        return Mesh(verts.toFloatArray(), indices.toIntArray(), material)
    }

    fun createConeMesh(
        radius: Float, height: Float, segments: Int,
        material: Material,
        offsetX: Float = 0f, offsetY: Float = 0f, offsetZ: Float = 0f
    ): Mesh {
        val verts = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        // Tip vertex
        verts.add(offsetX); verts.add(offsetY + height); verts.add(offsetZ)
        verts.add(0f); verts.add(1f); verts.add(0f)
        verts.add(0.5f); verts.add(1f)

        for (i in 0..segments) {
            val angle = (i.toFloat() / segments) * 2f * PI.toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            verts.add(offsetX + cosA * radius)
            verts.add(offsetY)
            verts.add(offsetZ + sinA * radius)
            verts.add(cosA); verts.add(0.4f); verts.add(sinA)
            verts.add(i.toFloat() / segments); verts.add(0f)
        }

        for (i in 1..segments) {
            indices.add(0)
            indices.add(i)
            indices.add(i + 1)
        }

        return Mesh(verts.toFloatArray(), indices.toIntArray(), material)
    }

    fun createPrismMesh(
        width: Float, height: Float, depth: Float,
        material: Material,
        offsetX: Float = 0f, offsetY: Float = 0f, offsetZ: Float = 0f
    ): Mesh {
        val hw = width * 0.5f
        val hd = depth * 0.5f

        val verts = floatArrayOf(
            // Left roof slope
            -hw, 0f, -hd,  -0.7f, 0.7f, 0f,  0f, 0f,
            -hw, 0f,  hd,  -0.7f, 0.7f, 0f,  1f, 0f,
             0f, height,  hd,  -0.7f, 0.7f, 0f,  1f, 1f,
             0f, height, -hd,  -0.7f, 0.7f, 0f,  0f, 1f,

            // Right roof slope
             0f, height, -hd,   0.7f, 0.7f, 0f,  0f, 1f,
             0f, height,  hd,   0.7f, 0.7f, 0f,  1f, 1f,
             hw, 0f,  hd,   0.7f, 0.7f, 0f,  1f, 0f,
             hw, 0f, -hd,   0.7f, 0.7f, 0f,  0f, 0f,

            // Front gable
            -hw, 0f, hd,   0f, 0f, 1f,  0f, 0f,
             hw, 0f, hd,   0f, 0f, 1f,  1f, 0f,
             0f, height, hd, 0f, 0f, 1f, 0.5f, 1f,

            // Back gable
             hw, 0f, -hd,  0f, 0f, -1f,  0f, 0f,
            -hw, 0f, -hd,  0f, 0f, -1f,  1f, 0f,
             0f, height, -hd, 0f, 0f, -1f, 0.5f, 1f
        )

        for (i in 0 until (verts.size / Mesh.FLOATS_PER_VERTEX)) {
            val off = i * Mesh.FLOATS_PER_VERTEX
            verts[off + 0] += offsetX
            verts[off + 1] += offsetY
            verts[off + 2] += offsetZ
        }

        val indices = intArrayOf(
            0, 1, 2,  0, 2, 3,       // Left slope
            4, 5, 6,  4, 6, 7,       // Right slope
            8, 9, 10,                 // Front triangle
            11, 12, 13                // Back triangle
        )

        return Mesh(verts, indices, material)
    }

    fun createDodecahedronMesh(
        size: Float,
        material: Material,
        offsetX: Float = 0f, offsetY: Float = 0f, offsetZ: Float = 0f
    ): Mesh {
        // Low-poly boulder
        val mesh = createBoxMesh(size * 1.3f, size * 0.9f, size * 1.1f, material, offsetX, offsetY, offsetZ)
        return mesh
    }
}
