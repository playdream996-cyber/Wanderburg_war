package com.game.castlewar.model3d.animation

/**
 * Manages playback, state transitions, and bone sampling for animated 3D characters.
 */
class Animator3D(
    val skeleton: Skeleton? = null
) {
    private val clips = mutableMapOf<String, AnimationClip>()
    private var currentClip: AnimationClip? = null
    var currentTime: Float = 0f
    var speed: Float = 1.0f
    var isPlaying: Boolean = true

    fun addClip(clip: AnimationClip) {
        clips[clip.name] = clip
        if (currentClip == null) {
            currentClip = clip
        }
    }

    fun play(clipName: String, resetTime: Boolean = false) {
        val clip = clips[clipName] ?: return
        if (currentClip?.name != clipName || resetTime) {
            currentClip = clip
            currentTime = 0f
        }
        isPlaying = true
    }

    fun update(deltaTime: Float) {
        val clip = currentClip ?: return
        if (!isPlaying) return

        currentTime += deltaTime * speed

        if (skeleton != null) {
            for (bone in skeleton.bones) {
                val sample = clip.sample(bone.name, currentTime)
                if (sample != null) {
                    bone.localTransform.setPosition(sample.posX, sample.posY, sample.posZ)
                    bone.localTransform.setRotation(sample.rotXDeg, sample.rotYDeg, sample.rotZDeg)
                    bone.localTransform.setScale(sample.scale)
                }
            }
        }
    }
}
