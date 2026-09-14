package com.game.castlewar.render

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance 2D/2.5D batch geometry renderer for OpenGL ES 3.0 / 2.0.
 *
 * Efficiently batches colored quads, circles, and lines using a single dynamic
 * VBO/IBO to eliminate individual draw-call overhead.
 */
class Renderer2D {

    companion object {
        private const val TAG = "Renderer2D"
        private const val MAX_QUADS = 2000
        private const val VERTICES_PER_QUAD = 4
        private const val INDICES_PER_QUAD = 6
        private const val FLOATS_PER_VERTEX = 6 // x, y, r, g, b, a
    }

    private var programId: Int = 0
    private var uProjectionLoc: Int = -1
    private var aPositionLoc: Int = 0
    private var aColorLoc: Int = 1

    private val vertexData = FloatArray(MAX_QUADS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX)
    private var vertexIndex = 0
    private var quadCount = 0

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    fun initialize() {
        val vertexShaderCode = """
            #version 300 es
            layout(location = 0) in vec2 aPosition;
            layout(location = 1) in vec4 aColor;
            uniform mat4 uVPMatrix;
            out vec4 vColor;
            void main() {
                gl_Position = uVPMatrix * vec4(aPosition, 0.0, 1.0);
                vColor = aColor;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            in vec4 vColor;
            out vec4 fragColor;
            void main() {
                fragColor = vColor;
            }
        """.trimIndent()

        val vShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vShader)
        GLES30.glAttachShader(programId, fShader)
        GLES30.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error linking program: " + GLES30.glGetProgramInfoLog(programId))
        }

        uVPMatrixLoc = GLES30.glGetUniformLocation(programId, "uVPMatrix")

        // Vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        // Index buffer (0,1,2, 2,3,0 per quad)
        val indices = ShortArray(MAX_QUADS * INDICES_PER_QUAD)
        for (i in 0 until MAX_QUADS) {
            val vOffset = (i * 4).toShort()
            val iOffset = i * 6
            indices[iOffset + 0] = (vOffset + 0).toShort()
            indices[iOffset + 1] = (vOffset + 1).toShort()
            indices[iOffset + 2] = (vOffset + 2).toShort()
            indices[iOffset + 3] = (vOffset + 2).toShort()
            indices[iOffset + 4] = (vOffset + 3).toShort()
            indices[iOffset + 5] = (vOffset + 0).toShort()
        }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
        indexBuffer.position(0)
    }

    private var uVPMatrixLoc: Int = -1

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $type: " + GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun begin(camera: GameCamera) {
        val w = camera.viewportWidth.toFloat().coerceAtLeast(1f)
        val h = camera.viewportHeight.toFloat().coerceAtLeast(1f)

        // Setup orthographic projection matching screen pixels: (0,0) top-left to (w,h) bottom-right
        Matrix.orthoM(projectionMatrix, 0, 0f, w, h, 0f, -1f, 1f)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        GLES30.glUseProgram(programId)
        GLES30.glUniformMatrix4fv(uVPMatrixLoc, 1, false, vpMatrix, 0)

        // Disable depth test for 2D screen billboard overlays so they are never clipped by 3D geometry
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        quadCount = 0
        vertexIndex = 0
    }

    fun end() {
        flush()
        // Re-enable depth test for 3D passes
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
    }

    fun flush() {
        if (quadCount == 0) return

        vertexBuffer.position(0)
        vertexBuffer.put(vertexData, 0, vertexIndex)
        vertexBuffer.position(0)

        // Setup vertex attributes
        val stride = FLOATS_PER_VERTEX * 4

        GLES30.glEnableVertexAttribArray(0)
        vertexBuffer.position(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, stride, vertexBuffer)

        GLES30.glEnableVertexAttribArray(1)
        vertexBuffer.position(2)
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, stride, vertexBuffer)

        indexBuffer.position(0)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, quadCount * 6, GLES30.GL_UNSIGNED_SHORT, indexBuffer)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)

        quadCount = 0
        vertexIndex = 0
    }

    /**
     * Draws an axis-aligned colored quad.
     */
    fun drawRect(
        x: Float, y: Float, width: Float, height: Float,
        r: Float, g: Float, b: Float, a: Float = 1.0f
    ) {
        if (quadCount >= MAX_QUADS - 1) flush()

        val x2 = x + width
        val y2 = y + height

        addVertex(x, y, r, g, b, a)
        addVertex(x2, y, r, g, b, a)
        addVertex(x2, y2, r, g, b, a)
        addVertex(x, y2, r, g, b, a)

        quadCount++
    }

    /**
     * Draws a rotated, centered quad (for turrets, fortress hull, tread links, projectiles).
     */
    fun drawRotatedRect(
        cx: Float, cy: Float, width: Float, height: Float, angleRad: Float,
        r: Float, g: Float, b: Float, a: Float = 1.0f
    ) {
        if (quadCount >= MAX_QUADS - 1) flush()

        val halfW = width * 0.5f
        val halfH = height * 0.5f

        val cosA = cos(angleRad)
        val sinA = sin(angleRad)

        // Local 4 corners
        val x0 = -halfW; val y0 = -halfH
        val x1 =  halfW; val y1 = -halfH
        val x2 =  halfW; val y2 =  halfH
        val x3 = -halfW; val y3 =  halfH

        // Rotate & translate
        addVertex(cx + (x0 * cosA - y0 * sinA), cy + (x0 * sinA + y0 * cosA), r, g, b, a)
        addVertex(cx + (x1 * cosA - y1 * sinA), cy + (x1 * sinA + y1 * cosA), r, g, b, a)
        addVertex(cx + (x2 * cosA - y2 * sinA), cy + (x2 * sinA + y2 * cosA), r, g, b, a)
        addVertex(cx + (x3 * cosA - y3 * sinA), cy + (x3 * sinA + y3 * cosA), r, g, b, a)

        quadCount++
    }

    /**
     * Draws a circle approximated with polygon fans as quads.
     */
    fun drawCircle(
        cx: Float, cy: Float, radius: Float,
        r: Float, g: Float, b: Float, a: Float = 1.0f,
        segments: Int = 12
    ) {
        val angleStep = (6.2831853f / segments)
        for (i in 0 until segments step 2) {
            if (quadCount >= MAX_QUADS - 1) flush()

            val a0 = i * angleStep
            val a1 = (i + 1) * angleStep
            val a2 = (i + 2) * angleStep

            addVertex(cx, cy, r, g, b, a)
            addVertex(cx + cos(a0) * radius, cy + sin(a0) * radius, r, g, b, a)
            addVertex(cx + cos(a1) * radius, cy + sin(a1) * radius, r, g, b, a)
            addVertex(cx + cos(a2) * radius, cy + sin(a2) * radius, r, g, b, a)

            quadCount++
        }
    }

    private fun addVertex(x: Float, y: Float, r: Float, g: Float, b: Float, a: Float) {
        vertexData[vertexIndex++] = x
        vertexData[vertexIndex++] = y
        vertexData[vertexIndex++] = r
        vertexData[vertexIndex++] = g
        vertexData[vertexIndex++] = b
        vertexData[vertexIndex++] = a
    }
}
