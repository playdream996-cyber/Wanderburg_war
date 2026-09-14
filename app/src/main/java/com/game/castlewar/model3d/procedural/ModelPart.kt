package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.MatrixUtils
import com.game.castlewar.model3d.Mesh
import com.game.castlewar.model3d.ModelRenderer

/**
 * Reusable hierarchical component for composite 3D models.
 * Contains local translation, rotation, scale, mesh, material, and nested children.
 */
class ModelPart(
    var mesh: Mesh? = null,
    var material: Material? = null,
    var localPositionX: Float = 0f,
    var localPositionY: Float = 0f,
    var localPositionZ: Float = 0f,
    var localRotationX: Float = 0f,
    var localRotationY: Float = 0f,
    var localRotationZ: Float = 0f,
    var localScaleX: Float = 1f,
    var localScaleY: Float = 1f,
    var localScaleZ: Float = 1f
) {
    val children = mutableListOf<ModelPart>()

    private val localMatrix = FloatArray(16)
    private val worldMatrix = FloatArray(16)

    fun addChild(part: ModelPart): ModelPart {
        children.add(part)
        return this
    }

    fun setPosition(x: Float, y: Float, z: Float): ModelPart {
        localPositionX = x
        localPositionY = y
        localPositionZ = z
        return this
    }

    fun setRotation(rx: Float, ry: Float, rz: Float): ModelPart {
        localRotationX = rx
        localRotationY = ry
        localRotationZ = rz
        return this
    }

    fun setScale(sx: Float, sy: Float, sz: Float): ModelPart {
        localScaleX = sx
        localScaleY = sy
        localScaleZ = sz
        return this
    }

    fun setScale(uniformScale: Float): ModelPart {
        return setScale(uniformScale, uniformScale, uniformScale)
    }

    /**
     * Computes the local transform matrix.
     */
    fun updateLocalMatrix() {
        MatrixUtils.buildModelMatrix(
            localMatrix,
            localPositionX, localPositionY, localPositionZ,
            localRotationX, localRotationY, localRotationZ,
            localScaleX, localScaleY, localScaleZ
        )
    }

    /**
     * Renders this part and all its hierarchical children using ModelRenderer.
     */
    fun render(modelRenderer: ModelRenderer, parentMatrix: FloatArray, overrideColor: Material? = null) {
        updateLocalMatrix()
        MatrixUtils.multiply(worldMatrix, parentMatrix, localMatrix)

        mesh?.let { m ->
            val mat = overrideColor ?: material ?: m.material
            modelRenderer.renderMesh(m, worldMatrix, mat)
        }

        for (child in children) {
            child.render(modelRenderer, worldMatrix, overrideColor)
        }
    }

    /**
     * Releases GPU resources for this part and children.
     */
    fun release() {
        mesh?.release()
        for (child in children) {
            child.release()
        }
    }
}
