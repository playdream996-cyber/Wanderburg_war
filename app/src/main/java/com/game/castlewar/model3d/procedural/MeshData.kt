package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.Mesh
import kotlin.math.cos
import kotlin.math.sin

/**
 * CPU-side geometric mesh data container containing vertices (pos, normal, uv) and indices.
 * Allows easy procedural combination, vertex manipulation, and transformation before GPU upload.
 */
class MeshData(
    var vertices: FloatArray,
    var indices: IntArray
) {
    companion object {
        const val FLOATS_PER_VERTEX = 8
        const val OFFSET_POS = 0
        const val OFFSET_NORMAL = 3
        const val OFFSET_UV = 6

        fun empty(): MeshData = MeshData(FloatArray(0), IntArray(0))
    }

    val vertexCount: Int
        get() = vertices.size / FLOATS_PER_VERTEX

    val triangleCount: Int
        get() = indices.size / 3

    /**
     * Translates all vertices by (dx, dy, dz).
     */
    fun translate(dx: Float, dy: Float, dz: Float): MeshData {
        val count = vertexCount
        for (i in 0 until count) {
            val base = i * FLOATS_PER_VERTEX
            vertices[base + 0] += dx
            vertices[base + 1] += dy
            vertices[base + 2] += dz
        }
        return this
    }

    /**
     * Scales all vertices by (sx, sy, sz).
     */
    fun scale(sx: Float, sy: Float, sz: Float): MeshData {
        val count = vertexCount
        for (i in 0 until count) {
            val base = i * FLOATS_PER_VERTEX
            vertices[base + 0] *= sx
            vertices[base + 1] *= sy
            vertices[base + 2] *= sz
        }
        return this
    }

    /**
     * Rotates around X, Y, Z axes (in degrees).
     */
    fun rotate(rxDeg: Float, ryDeg: Float, rzDeg: Float): MeshData {
        val radX = Math.toRadians(rxDeg.toDouble()).toFloat()
        val radY = Math.toRadians(ryDeg.toDouble()).toFloat()
        val radZ = Math.toRadians(rzDeg.toDouble()).toFloat()

        val cosX = cos(radX); val sinX = sin(radX)
        val cosY = cos(radY); val sinY = sin(radY)
        val cosZ = cos(radZ); val sinZ = sin(radZ)

        val count = vertexCount
        for (i in 0 until count) {
            val base = i * FLOATS_PER_VERTEX
            var x = vertices[base + 0]
            var y = vertices[base + 1]
            var z = vertices[base + 2]

            var nx = vertices[base + 3]
            var ny = vertices[base + 4]
            var nz = vertices[base + 5]

            // Rotate X
            var y1 = y * cosX - z * sinX
            var z1 = y * sinX + z * cosX
            var ny1 = ny * cosX - nz * sinX
            var nz1 = ny * sinX + nz * cosX

            // Rotate Y
            var x2 = x * cosY + z1 * sinY
            var z2 = -x * sinY + z1 * cosY
            var nx2 = nx * cosY + nz1 * sinY
            var nz2 = -nx * sinY + nz1 * cosY

            // Rotate Z
            var x3 = x2 * cosZ - y1 * sinZ
            var y3 = x2 * sinZ + y1 * cosZ
            var nx3 = nx2 * cosZ - ny1 * sinZ
            var ny3 = nx2 * sinZ + ny1 * cosZ

            vertices[base + 0] = x3
            vertices[base + 1] = y3
            vertices[base + 2] = z2
            vertices[base + 3] = nx3
            vertices[base + 4] = ny3
            vertices[base + 5] = nz2
        }
        return this
    }

    /**
     * Transforms position, rotation, scale in standard order (Scale -> Rotate -> Translate).
     */
    fun transform(
        tx: Float, ty: Float, tz: Float,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f,
        sx: Float = 1f, sy: Float = 1f, sz: Float = 1f
    ): MeshData {
        if (sx != 1f || sy != 1f || sz != 1f) scale(sx, sy, sz)
        if (rx != 0f || ry != 0f || rz != 0f) rotate(rx, ry, rz)
        if (tx != 0f || ty != 0f || tz != 0f) translate(tx, ty, tz)
        return this
    }

    /**
     * Creates a deep copy of this geometry.
     */
    fun clone(): MeshData {
        return MeshData(vertices.clone(), indices.clone())
    }

    /**
     * Combines another MeshData into this one.
     */
    fun append(other: MeshData): MeshData {
        val currentVertCount = vertexCount
        val newVerts = FloatArray(vertices.size + other.vertices.size)
        System.arraycopy(vertices, 0, newVerts, 0, vertices.size)
        System.arraycopy(other.vertices, 0, newVerts, vertices.size, other.vertices.size)

        val newIndices = IntArray(indices.size + other.indices.size)
        System.arraycopy(indices, 0, newIndices, 0, indices.size)
        for (i in other.indices.indices) {
            newIndices[indices.size + i] = other.indices[i] + currentVertCount
        }

        this.vertices = newVerts
        this.indices = newIndices
        return this
    }

    /**
     * Converts to an OpenGL ES 3.0 Mesh with the given material.
     */
    fun toMesh(material: Material): Mesh {
        return Mesh(vertices, indices, material)
    }
}
