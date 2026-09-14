package com.game.castlewar.model3d.animation

import com.game.castlewar.model3d.MatrixUtils
import com.game.castlewar.model3d.Transform

/**
 * Skeletal Bone for 3D character animation.
 */
class Bone(
    val id: Int,
    val name: String,
    val parentId: Int = -1
) {
    val localTransform = Transform()
    val modelMatrix = FloatArray(16)
    val inverseBindMatrix = FloatArray(16)

    init {
        MatrixUtils.identity(modelMatrix)
        MatrixUtils.identity(inverseBindMatrix)
    }
}
