package com.game.castlewar.model3d

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.ByteBuffer

/**
 * 2D Texture wrapper for OpenGL ES 3.0.
 */
class Texture(
    val textureId: Int,
    val width: Int,
    val height: Int
) {
    companion object {
        /**
         * Creates a 1x1 solid colored texture (used for fallback or flat-shaded materials).
         */
        fun createSolidColor(r: Float, g: Float, b: Float, a: Float = 1.0f): Texture {
            val textureIds = IntArray(1)
            GLES30.glGenTextures(1, textureIds, 0)
            val texId = textureIds[0]

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)

            val pixel = ByteBuffer.allocateDirect(4)
            pixel.put((r.coerceIn(0f, 1f) * 255).toInt().toByte())
            pixel.put((g.coerceIn(0f, 1f) * 255).toInt().toByte())
            pixel.put((b.coerceIn(0f, 1f) * 255).toInt().toByte())
            pixel.put((a.coerceIn(0f, 1f) * 255).toInt().toByte())
            pixel.position(0)

            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                1, 1, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixel
            )
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

            return Texture(texId, 1, 1)
        }

        /**
         * Loads a Texture from an Android Bitmap with automatic mipmap generation.
         */
        fun fromBitmap(bitmap: Bitmap): Texture {
            val textureIds = IntArray(1)
            GLES30.glGenTextures(1, textureIds, 0)
            val texId = textureIds[0]

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)

            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

            return Texture(texId, bitmap.width, bitmap.height)
        }
    }

    fun bind(unit: Int = 0) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
    }

    fun unbind() {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    fun release() {
        if (textureId != 0) {
            val ids = intArrayOf(textureId)
            GLES30.glDeleteTextures(1, ids, 0)
        }
    }
}
