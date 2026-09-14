package com.game.castlewar.model3d

/**
 * Represents spatial transformation in 3D world space (position, rotation, scale).
 */
class Transform(
    var posX: Float = 0f,
    var posY: Float = 0f,
    var posZ: Float = 0f,
    var rotXDeg: Float = 0f,
    var rotYDeg: Float = 0f,
    var rotZDeg: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var scaleZ: Float = 1f
) {
    val modelMatrix = FloatArray(16)
    private var isDirty: Boolean = true

    fun setPosition(x: Float, y: Float, z: Float): Transform {
        posX = x
        posY = y
        posZ = z
        isDirty = true
        return this
    }

    fun setRotation(xDeg: Float, yDeg: Float, zDeg: Float): Transform {
        rotXDeg = xDeg
        rotYDeg = yDeg
        rotZDeg = zDeg
        isDirty = true
        return this
    }

    fun setScale(s: Float): Transform {
        scaleX = s
        scaleY = s
        scaleZ = s
        isDirty = true
        return this
    }

    fun setScale(sx: Float, sy: Float, sz: Float): Transform {
        scaleX = sx
        scaleY = sy
        scaleZ = sz
        isDirty = true
        return this
    }

    fun getMatrix(): FloatArray {
        if (isDirty) {
            MatrixUtils.buildModelMatrix(
                modelMatrix,
                posX, posY, posZ,
                rotXDeg, rotYDeg, rotZDeg,
                scaleX, scaleY, scaleZ
            )
            isDirty = false
        }
        return modelMatrix
    }

    fun markDirty() {
        isDirty = true
    }
}
