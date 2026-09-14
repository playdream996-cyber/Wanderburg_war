package com.game.castlewar.model3d

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance 3D Mesh for OpenGL ES 3.0 using Vertex Array Objects (VAO),
 * Vertex Buffer Objects (VBO), and Index Buffer Objects (EBO).
 *
 * Vertex Layout (Stride = 8 floats = 32 bytes):
 * - Location 0: vec3 aPosition (offset 0)
 * - Location 1: vec3 aNormal   (offset 12)
 * - Location 2: vec2 aTexCoord (offset 24)
 */
class Mesh(
    val vertexBufferData: FloatArray,
    val indexBufferData: IntArray,
    var material: Material = Material()
) {
    companion object {
        const val FLOATS_PER_VERTEX = 8
        const val STRIDE_BYTES = FLOATS_PER_VERTEX * 4
        const val OFFSET_POS = 0
        const val OFFSET_NORMAL = 3 * 4
        const val OFFSET_UV = 6 * 4
    }

    private var vaoId: Int = 0
    private var vboId: Int = 0
    private var eboId: Int = 0
    val indexCount: Int = indexBufferData.size
    private var isInitialized: Boolean = false

    fun initializeGL() {
        if (isInitialized) return

        val vaos = IntArray(1)
        val vbos = IntArray(1)
        val ebos = IntArray(1)

        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        GLES30.glGenBuffers(1, ebos, 0)

        vaoId = vaos[0]
        vboId = vbos[0]
        eboId = ebos[0]

        // Bind VAO
        GLES30.glBindVertexArray(vaoId)

        // Upload vertex buffer
        val vBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexBufferData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexBufferData)
        vBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertexBufferData.size * 4,
            vBuffer,
            GLES30.GL_STATIC_DRAW
        )

        // Upload index buffer (32-bit unsigned int indices)
        val iBuffer: IntBuffer = ByteBuffer.allocateDirect(indexBufferData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(indexBufferData)
        iBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboId)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indexBufferData.size * 4,
            iBuffer,
            GLES30.GL_STATIC_DRAW
        )

        // Position attribute (layout = 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            0, 3, GLES30.GL_FLOAT, false,
            STRIDE_BYTES, OFFSET_POS
        )

        // Normal attribute (layout = 1)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(
            1, 3, GLES30.GL_FLOAT, false,
            STRIDE_BYTES, OFFSET_NORMAL
        )

        // TexCoord attribute (layout = 2)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(
            2, 2, GLES30.GL_FLOAT, false,
            STRIDE_BYTES, OFFSET_UV
        )

        // Unbind VAO
        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)

        isInitialized = true
    }

    fun draw() {
        if (!isInitialized) {
            initializeGL()
        }

        GLES30.glBindVertexArray(vaoId)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            indexCount,
            GLES30.GL_UNSIGNED_INT,
            0
        )
        GLES30.glBindVertexArray(0)
    }

    fun release() {
        if (isInitialized) {
            val vaos = intArrayOf(vaoId)
            val vbos = intArrayOf(vboId)
            val ebos = intArrayOf(eboId)
            GLES30.glDeleteVertexArrays(1, vaos, 0)
            GLES30.glDeleteBuffers(1, vbos, 0)
            GLES30.glDeleteBuffers(1, ebos, 0)
            isInitialized = false
        }
    }
}
