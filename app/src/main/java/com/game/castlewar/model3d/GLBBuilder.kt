package com.game.castlewar.model3d

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility to encode meshes into official glTF 2.0 Binary (.GLB) files.
 */
object GLBBuilder {

    fun buildGLB(model: Model): ByteArray {
        val binStream = ByteArrayOutputStream()

        val json = JSONObject()
        val asset = JSONObject().apply { put("version", "2.0"); put("generator", "CastleWar-GLBBuilder") }
        json.put("asset", asset)

        val bufferViews = JSONArray()
        val accessors = JSONArray()
        val materials = JSONArray()
        val meshes = JSONArray()
        val nodes = JSONArray()
        val scenes = JSONArray()

        var currentBinOffset = 0

        // Process all direct meshes
        val primitiveJsonList = JSONArray()

        for (mesh in model.directMeshes) {
            val vCount = mesh.vertexBufferData.size / Mesh.FLOATS_PER_VERTEX

            // Extract positions
            val posBytes = ByteArray(vCount * 3 * 4)
            val posBuf = ByteBuffer.wrap(posBytes).order(ByteOrder.LITTLE_ENDIAN)
            var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            for (i in 0 until vCount) {
                val x = mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 0]
                val y = mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 1]
                val z = mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 2]
                posBuf.putFloat(x)
                posBuf.putFloat(y)
                posBuf.putFloat(z)
                if (x < minX) minX = x; if (x > maxX) maxX = x
                if (y < minY) minY = y; if (y > maxY) maxY = y
                if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
            }

            // Extract normals
            val normBytes = ByteArray(vCount * 3 * 4)
            val normBuf = ByteBuffer.wrap(normBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until vCount) {
                normBuf.putFloat(mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 3])
                normBuf.putFloat(mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 4])
                normBuf.putFloat(mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 5])
            }

            // Extract UVs
            val uvBytes = ByteArray(vCount * 2 * 4)
            val uvBuf = ByteBuffer.wrap(uvBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until vCount) {
                uvBuf.putFloat(mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 6])
                uvBuf.putFloat(mesh.vertexBufferData[i * Mesh.FLOATS_PER_VERTEX + 7])
            }

            // Extract Indices
            val idxBytes = ByteArray(mesh.indexBufferData.size * 4)
            val idxBuf = ByteBuffer.wrap(idxBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (idx in mesh.indexBufferData) {
                idxBuf.putInt(idx)
            }

            // BufferView 0: Positions
            val bvPosIdx = bufferViews.length()
            val bvPos = JSONObject().apply {
                put("buffer", 0)
                put("byteOffset", currentBinOffset)
                put("byteLength", posBytes.size)
                put("target", 34962) // ARRAY_BUFFER
            }
            bufferViews.put(bvPos)
            binStream.write(posBytes)
            currentBinOffset += posBytes.size

            val accPosIdx = accessors.length()
            val accPos = JSONObject().apply {
                put("bufferView", bvPosIdx)
                put("componentType", 5126) // FLOAT
                put("count", vCount)
                put("type", "VEC3")
                put("max", JSONArray().put(maxX).put(maxY).put(maxZ))
                put("min", JSONArray().put(minX).put(minY).put(minZ))
            }
            accessors.put(accPos)

            // BufferView 1: Normals
            val bvNormIdx = bufferViews.length()
            val bvNorm = JSONObject().apply {
                put("buffer", 0)
                put("byteOffset", currentBinOffset)
                put("byteLength", normBytes.size)
                put("target", 34962)
            }
            bufferViews.put(bvNorm)
            binStream.write(normBytes)
            currentBinOffset += normBytes.size

            val accNormIdx = accessors.length()
            val accNorm = JSONObject().apply {
                put("bufferView", bvNormIdx)
                put("componentType", 5126)
                put("count", vCount)
                put("type", "VEC3")
            }
            accessors.put(accNorm)

            // BufferView 2: UVs
            val bvUvIdx = bufferViews.length()
            val bvUv = JSONObject().apply {
                put("buffer", 0)
                put("byteOffset", currentBinOffset)
                put("byteLength", uvBytes.size)
                put("target", 34962)
            }
            bufferViews.put(bvUv)
            binStream.write(uvBytes)
            currentBinOffset += uvBytes.size

            val accUvIdx = accessors.length()
            val accUv = JSONObject().apply {
                put("bufferView", bvUvIdx)
                put("componentType", 5126)
                put("count", vCount)
                put("type", "VEC2")
            }
            accessors.put(accUv)

            // BufferView 3: Indices
            val bvIdxIdx = bufferViews.length()
            val bvIdx = JSONObject().apply {
                put("buffer", 0)
                put("byteOffset", currentBinOffset)
                put("byteLength", idxBytes.size)
                put("target", 34963) // ELEMENT_ARRAY_BUFFER
            }
            bufferViews.put(bvIdx)
            binStream.write(idxBytes)
            currentBinOffset += idxBytes.size

            val accIdxIdx = accessors.length()
            val accIdx = JSONObject().apply {
                put("bufferView", bvIdxIdx)
                put("componentType", 5125) // UNSIGNED_INT
                put("count", mesh.indexBufferData.size)
                put("type", "SCALAR")
            }
            accessors.put(accIdx)

            // Material
            val matIdx = materials.length()
            val mat = mesh.material
            val pbr = JSONObject().apply {
                put("baseColorFactor", JSONArray().put(mat.baseColorR).put(mat.baseColorG).put(mat.baseColorB).put(mat.baseColorA))
                put("roughnessFactor", mat.roughness)
                put("metallicFactor", mat.metallic)
            }
            materials.put(JSONObject().apply {
                put("name", mat.name)
                put("pbrMetallicRoughness", pbr)
            })

            val prim = JSONObject().apply {
                val attrs = JSONObject().apply {
                    put("POSITION", accPosIdx)
                    put("NORMAL", accNormIdx)
                    put("TEXCOORD_0", accUvIdx)
                }
                put("attributes", attrs)
                put("indices", accIdxIdx)
                put("material", matIdx)
            }
            primitiveJsonList.put(prim)
        }

        val meshJson = JSONObject().apply {
            put("name", model.name)
            put("primitives", primitiveJsonList)
        }
        meshes.put(meshJson)

        val nodeJson = JSONObject().apply {
            put("name", model.name + "_root")
            put("mesh", 0)
        }
        nodes.put(nodeJson)

        scenes.put(JSONObject().apply {
            put("name", "Scene")
            put("nodes", JSONArray().put(0))
        })

        json.put("bufferViews", bufferViews)
        json.put("accessors", accessors)
        json.put("materials", materials)
        json.put("meshes", meshes)
        json.put("nodes", nodes)
        json.put("scenes", scenes)
        json.put("scene", 0)

        // Buffer length in binary
        val binData = binStream.toByteArray()
        val buffers = JSONArray().put(JSONObject().apply {
            put("byteLength", binData.size)
        })
        json.put("buffers", buffers)

        // Convert JSON to padded bytes
        var jsonBytes = json.toString().toByteArray(Charsets.UTF_8)
        val jsonPad = (4 - (jsonBytes.size % 4)) % 4
        if (jsonPad > 0) {
            val padded = ByteArray(jsonBytes.size + jsonPad)
            System.arraycopy(jsonBytes, 0, padded, 0, jsonBytes.size)
            for (i in 0 until jsonPad) padded[jsonBytes.size + i] = 0x20.toByte() // Space character
            jsonBytes = padded
        }

        // BIN chunk padding
        var finalBinData = binData
        val binPad = (4 - (binData.size % 4)) % 4
        if (binPad > 0) {
            val padded = ByteArray(binData.size + binPad)
            System.arraycopy(binData, 0, padded, 0, binData.size)
            finalBinData = padded
        }

        // Total GLB size
        val totalLength = 12 + 8 + jsonBytes.size + 8 + finalBinData.size
        val glbBytes = ByteArray(totalLength)
        val glbBuf = ByteBuffer.wrap(glbBytes).order(ByteOrder.LITTLE_ENDIAN)

        // 12-byte header
        glbBuf.putInt(0x46546C67) // "glTF"
        glbBuf.putInt(2)          // version 2
        glbBuf.putInt(totalLength)

        // Chunk 0: JSON
        glbBuf.putInt(jsonBytes.size)
        glbBuf.putInt(0x4E4F534A) // "JSON"
        glbBuf.put(jsonBytes)

        // Chunk 1: BIN
        glbBuf.putInt(finalBinData.size)
        glbBuf.putInt(0x004E4942) // "BIN\0"
        glbBuf.put(finalBinData)

        return glbBytes
    }
}
