package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.ModelRenderer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hierarchical procedural character rig for Wanderburg-style low-poly medieval units.
 * Controls limbs for natural walking, attacking, hit reactions, and death falls.
 */
class ProceduralCharacterRig(
    val root: ModelPart,
    val headPart: ModelPart,
    val torsoPart: ModelPart,
    val leftArmPart: ModelPart,
    val rightArmPart: ModelPart,
    val leftLegPart: ModelPart,
    val rightLegPart: ModelPart,
    val weaponPart: ModelPart? = null,
    val shieldPart: ModelPart? = null
) {
    var isMoving: Boolean = false
    var walkCycle: Float = 0f
    var attackTimer: Float = 0f
    var isAttacking: Boolean = false
    var hitFlashTimer: Float = 0f
    var isDead: Boolean = false
    var deathTimer: Float = 0f

    /**
     * Updates limb rotations based on locomotion speed, attack state, and life.
     */
    fun update(deltaTime: Float, speed: Float) {
        if (isDead) {
            deathTimer = (deathTimer + deltaTime * 3f).coerceAtMost(1f)
            // Fall backwards onto ground
            root.localRotationX = -90f * deathTimer
            root.localPositionY = -deathTimer * 3f
            return
        }

        if (hitFlashTimer > 0f) {
            hitFlashTimer -= deltaTime
        }

        if (speed > 0.1f) {
            walkCycle += deltaTime * speed * 0.4f
            val legSwing = sin(walkCycle) * 32f
            val armSwing = sin(walkCycle) * 28f

            leftLegPart.localRotationX = legSwing
            rightLegPart.localRotationX = -legSwing

            // Arms swing opposite to legs
            if (!isAttacking) {
                leftArmPart.localRotationX = -armSwing
                rightArmPart.localRotationX = armSwing
            }
            // Subtle torso bob
            torsoPart.localPositionY = 4.5f + kotlin.math.abs(sin(walkCycle * 2f)) * 0.6f
        } else {
            // Idle breathing
            walkCycle += deltaTime * 2f
            leftLegPart.localRotationX = 0f
            rightLegPart.localRotationX = 0f
            if (!isAttacking) {
                leftArmPart.localRotationX = sin(walkCycle) * 4f
                rightArmPart.localRotationX = -sin(walkCycle) * 4f
            }
            torsoPart.localPositionY = 4.5f + sin(walkCycle) * 0.2f
        }

        // Attack animation (Right arm swing/thrust)
        if (isAttacking) {
            attackTimer += deltaTime * 8f
            val swing = sin(attackTimer.coerceIn(0f, 3.1415f)) * 75f
            rightArmPart.localRotationX = -swing
            if (attackTimer >= 3.1415f) {
                isAttacking = false
                attackTimer = 0f
            }
        }
    }

    fun triggerAttack() {
        isAttacking = true
        attackTimer = 0f
    }

    fun triggerHit() {
        hitFlashTimer = 0.12f
    }

    fun render(modelRenderer: ModelRenderer, parentMatrix: FloatArray, hitColor: Material? = null) {
        val overrideMat = if (hitFlashTimer > 0f) hitColor ?: ProceduralMaterial.RED_CLOTH else null
        root.render(modelRenderer, parentMatrix, overrideMat)
    }

    fun release() {
        root.release()
    }
}
