package com.game.castlewar.model3d.animation

/**
 * Keyframe channel data for translation, rotation, and scale.
 */
data class TransformKeyframe(
    val time: Float,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,
    val rotXDeg: Float = 0f,
    val rotYDeg: Float = 0f,
    val rotZDeg: Float = 0f,
    val scale: Float = 1f
)

/**
 * Skeletal Animation Clip (e.g. Idle, Walk, Attack, Death).
 */
class AnimationClip(
    val name: String,
    val durationSeconds: Float,
    val isLooping: Boolean = true
) {
    val boneTracks = mutableMapOf<String, List<TransformKeyframe>>()

    fun addTrack(boneName: String, keyframes: List<TransformKeyframe>) {
        boneTracks[boneName] = keyframes
    }

    fun sample(boneName: String, time: Float): TransformKeyframe? {
        val track = boneTracks[boneName] ?: return null
        if (track.isEmpty()) return null
        if (track.size == 1) return track[0]

        val sampleTime = if (isLooping) {
            (time % durationSeconds).coerceAtLeast(0f)
        } else {
            time.coerceIn(0f, durationSeconds)
        }

        // Find surrounding keyframes
        for (i in 0 until track.size - 1) {
            val k0 = track[i]
            val k1 = track[i + 1]
            if (sampleTime in k0.time..k1.time) {
                val span = k1.time - k0.time
                val factor = if (span > 0.0001f) (sampleTime - k0.time) / span else 0f
                return TransformKeyframe(
                    time = sampleTime,
                    posX = k0.posX + (k1.posX - k0.posX) * factor,
                    posY = k0.posY + (k1.posY - k0.posY) * factor,
                    posZ = k0.posZ + (k1.posZ - k0.posZ) * factor,
                    rotXDeg = k0.rotXDeg + (k1.rotXDeg - k0.rotXDeg) * factor,
                    rotYDeg = k0.rotYDeg + (k1.rotYDeg - k0.rotYDeg) * factor,
                    rotZDeg = k0.rotZDeg + (k1.rotZDeg - k0.rotZDeg) * factor,
                    scale = k0.scale + (k1.scale - k0.scale) * factor
                )
            }
        }
        return track.last()
    }
}
