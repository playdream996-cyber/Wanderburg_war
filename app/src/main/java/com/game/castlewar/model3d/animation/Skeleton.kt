package com.game.castlewar.model3d.animation

/**
 * Skeleton hierarchy containing an array of animated bones and their matrix palette.
 */
class Skeleton(
    val bones: List<Bone>
) {
    val boneMatrices: FloatArray = FloatArray(bones.size * 16)

    init {
        for (i in bones.indices) {
            val offset = i * 16
            bones[i].modelMatrix.copyInto(boneMatrices, offset, 0, 16)
        }
    }

    fun findBone(name: String): Bone? = bones.find { it.name == name }
}
