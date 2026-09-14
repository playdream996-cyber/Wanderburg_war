package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.MatrixUtils
import com.game.castlewar.model3d.Mesh
import com.game.castlewar.model3d.Model
import com.game.castlewar.model3d.ModelRenderer
import com.game.castlewar.model3d.Transform

/**
 * A composite procedural 3D model composed of multiple hierarchical ModelParts.
 * Supports rendering with transform, root-level culling, and conversion to standard Model.
 */
class GeneratedModel(
    val name: String,
    val parts: List<ModelPart> = emptyList()
) {
    private val modelMatrix = FloatArray(16)
    private var isInitialized: Boolean = false

    fun initializeGL() {
        if (isInitialized) return
        // GPU buffers are lazily uploaded in ModelPart / Mesh on first draw
        isInitialized = true
    }

    /**
     * Renders all parts using a 4x4 model matrix.
     */
    fun render(modelRenderer: ModelRenderer, transformMatrix: FloatArray, overrideColor: Material? = null) {
        for (part in parts) {
            part.render(modelRenderer, transformMatrix, overrideColor)
        }
    }

    /**
     * Renders all parts using world Transform (position, rotation, scale).
     */
    fun render(modelRenderer: ModelRenderer, worldTransform: Transform, overrideColor: Material? = null) {
        render(modelRenderer, worldTransform.getMatrix(), overrideColor)
    }

    /**
     * Converts to a flat optimized Model by combining parts into cached meshes per material.
     */
    fun toModel(): Model {
        val model = Model(name)
        for (part in parts) {
            collectMeshes(part, FloatArray(16).apply { MatrixUtils.identity(this) }, model)
        }
        return model
    }

    private fun collectMeshes(part: ModelPart, parentMatrix: FloatArray, outModel: Model) {
        val localMat = FloatArray(16)
        val worldMat = FloatArray(16)
        part.updateLocalMatrix()
        MatrixUtils.buildModelMatrix(
            localMat,
            part.localPositionX, part.localPositionY, part.localPositionZ,
            part.localRotationX, part.localRotationY, part.localRotationZ,
            part.localScaleX, part.localScaleY, part.localScaleZ
        )
        MatrixUtils.multiply(worldMat, parentMatrix, localMat)

        part.mesh?.let { m ->
            outModel.addMesh(m)
        }

        for (child in part.children) {
            collectMeshes(child, worldMat, outModel)
        }
    }

    fun release() {
        for (part in parts) {
            part.release()
        }
    }
}
