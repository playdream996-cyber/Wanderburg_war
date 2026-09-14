package com.game.castlewar.model3d

import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Matrix and Vector mathematics utility for OpenGL ES 3.0.
 * Operates on standard 16-element column-major float matrices with zero allocations per frame.
 */
object MatrixUtils {

    /**
     * Creates an identity matrix.
     */
    fun identity(outMatrix: FloatArray) {
        Matrix.setIdentityM(outMatrix, 0)
    }

    /**
     * Builds a model transformation matrix: Translation * RotationY * RotationX * RotationZ * Scale.
     */
    fun buildModelMatrix(
        outMatrix: FloatArray,
        posX: Float, posY: Float, posZ: Float,
        rotXDeg: Float, rotYDeg: Float, rotZDeg: Float,
        scaleX: Float, scaleY: Float, scaleZ: Float
    ) {
        Matrix.setIdentityM(outMatrix, 0)
        Matrix.translateM(outMatrix, 0, posX, posY, posZ)

        if (rotYDeg != 0f) Matrix.rotateM(outMatrix, 0, rotYDeg, 0f, 1f, 0f)
        if (rotXDeg != 0f) Matrix.rotateM(outMatrix, 0, rotXDeg, 1f, 0f, 0f)
        if (rotZDeg != 0f) Matrix.rotateM(outMatrix, 0, rotZDeg, 0f, 0f, 1f)

        if (scaleX != 1f || scaleY != 1f || scaleZ != 1f) {
            Matrix.scaleM(outMatrix, 0, scaleX, scaleY, scaleZ)
        }
    }

    /**
     * Configures a perspective projection matrix.
     */
    fun perspective(
        outMatrix: FloatArray,
        fovYDegrees: Float,
        aspectRatio: Float,
        nearZ: Float,
        farZ: Float
    ) {
        Matrix.perspectiveM(outMatrix, 0, fovYDegrees, aspectRatio, nearZ, farZ)
    }

    /**
     * Configures a lookAt view matrix.
     */
    fun lookAt(
        outMatrix: FloatArray,
        eyeX: Float, eyeY: Float, eyeZ: Float,
        targetX: Float, targetY: Float, targetZ: Float,
        upX: Float = 0f, upY: Float = 1f, upZ: Float = 0f
    ) {
        Matrix.setLookAtM(outMatrix, 0, eyeX, eyeY, eyeZ, targetX, targetY, targetZ, upX, upY, upZ)
    }

    /**
     * Multiplies two 4x4 matrices: result = lhs * rhs
     */
    fun multiply(outResult: FloatArray, lhs: FloatArray, rhs: FloatArray) {
        Matrix.multiplyMM(outResult, 0, lhs, 0, rhs, 0)
    }

    private val tempInverted = FloatArray(16)

    /**
     * Inverts a 4x4 matrix and transposes it to produce a 3D Normal Matrix.
     */
    fun computeNormalMatrix(outNormalMat4: FloatArray, modelMatrix: FloatArray) {
        if (Matrix.invertM(tempInverted, 0, modelMatrix, 0)) {
            Matrix.transposeM(outNormalMat4, 0, tempInverted, 0)
        } else {
            Matrix.setIdentityM(outNormalMat4, 0)
        }
    }
}
