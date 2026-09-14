package com.game.castlewar.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.game.castlewar.game.GameManager
import com.game.castlewar.game.GameState
import com.game.castlewar.input.VirtualJoystick
import com.game.castlewar.world.GameWorld
import kotlin.math.sin

/**
 * Lightweight non-interactive coach layer drawn above the gameplay HUD.
 * Provides a first-run animated walkthrough plus a clear castle-tier evolution banner.
 * Touches pass through to the existing GameOverlayView/GameSurfaceView.
 */
class GuidanceOverlayView(
    context: Context,
    private val gameManager: GameManager,
    private val gameWorld: GameWorld,
    private val joystick: VirtualJoystick
) : View(context) {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F3F0E8")
        textAlign = Paint.Align.CENTER
    }

    private var tutorialStep = 0
    private var tutorialFinished = false
    private var lastTier = gameWorld.castle.tier
    private var tierBannerUntilMs = 0L

    init {
        isClickable = false
        isFocusable = false
        gameManager.addStateListener { _, _ -> postInvalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val now = System.currentTimeMillis()
        updateProgress(now)

        if (gameManager.currentState == GameState.PLAYING || gameManager.currentState == GameState.BOSS_FIGHT) {
            if (!tutorialFinished) renderTutorial(canvas, w, h, now)
            if (now < tierBannerUntilMs) renderTierBanner(canvas, w, h, now)
        }

        postInvalidateOnAnimation()
    }

    private fun updateProgress(now: Long) {
        val tier = gameWorld.castle.tier
        if (tier > lastTier) {
            lastTier = tier
            tierBannerUntilMs = now + 2600L
            tutorialFinished = true
        }

        if (tutorialFinished) return

        val moved = kotlin.math.abs(joystick.inputX) > 0.12f || kotlin.math.abs(joystick.inputY) > 0.12f
        if (tutorialStep == 0 && moved) tutorialStep = 1
        if (tutorialStep <= 1 && gameWorld.castle.experience > 0) tutorialStep = 2
        if (gameWorld.castle.level >= 2) tutorialFinished = true
    }

    private fun renderTutorial(canvas: Canvas, w: Float, h: Float, now: Long) {
        val pulse = ((sin(now / 180.0) + 1.0) * 0.5).toFloat()
        val panelW = w * 0.54f
        val panelH = h * 0.13f
        val left = (w - panelW) * 0.5f
        val top = h * 0.055f
        val rect = RectF(left, top, left + panelW, top + panelH)

        fill.color = Color.argb(215, 19, 22, 20)
        canvas.drawRoundRect(rect, 24f, 24f, fill)

        stroke.color = Color.argb(210, 221, 172, 68)
        stroke.strokeWidth = 3f + pulse * 2f
        canvas.drawRoundRect(rect, 24f, 24f, stroke)

        title.textSize = h * 0.030f
        title.color = Color.parseColor("#FFD66B")
        canvas.drawText("BATTLE GUIDE  ${tutorialStep + 1}/3", w * 0.5f, top + panelH * 0.38f, title)

        body.textSize = h * 0.025f
        val message = when (tutorialStep) {
            0 -> "Drag the joystick to drive your walking fortress"
            1 -> "Move toward enemies — your weapons fire automatically"
            else -> "Collect XP drops to evolve the castle and unlock upgrades"
        }
        canvas.drawText(message, w * 0.5f, top + panelH * 0.72f, body)

        val arrowX: Float
        val arrowY: Float
        when (tutorialStep) {
            0 -> {
                arrowX = w * 0.16f
                arrowY = h * (0.79f - pulse * 0.025f)
            }
            1 -> {
                arrowX = w * 0.5f
                arrowY = h * (0.42f - pulse * 0.025f)
            }
            else -> {
                arrowX = w * 0.40f
                arrowY = h * (0.18f + pulse * 0.018f)
            }
        }

        fill.color = Color.argb(65 + (pulse * 65).toInt(), 255, 196, 64)
        canvas.drawCircle(arrowX, arrowY, 34f + pulse * 10f, fill)
        title.textSize = h * 0.060f
        title.color = Color.WHITE
        canvas.drawText("↓", arrowX, arrowY + h * 0.020f, title)
    }

    private fun renderTierBanner(canvas: Canvas, w: Float, h: Float, now: Long) {
        val remaining = (tierBannerUntilMs - now).coerceAtLeast(0L)
        val alpha = (remaining / 700.0).coerceIn(0.0, 1.0).toFloat()
        val pulse = ((sin(now / 110.0) + 1.0) * 0.5).toFloat()

        // Brief golden frame around the gameplay view makes the transformation impossible to miss.
        stroke.color = Color.argb((180 * alpha).toInt(), 255, 190, 45)
        stroke.strokeWidth = 8f + pulse * 5f
        canvas.drawRoundRect(RectF(8f, 8f, w - 8f, h - 8f), 26f, 26f, stroke)

        val bannerW = w * 0.46f
        val bannerH = h * 0.14f
        val left = (w - bannerW) * 0.5f
        val top = h * 0.25f
        val rect = RectF(left, top, left + bannerW, top + bannerH)

        fill.color = Color.argb((225 * alpha).toInt(), 31, 24, 12)
        canvas.drawRoundRect(rect, 28f, 28f, fill)
        stroke.color = Color.argb((255 * alpha).toInt(), 255, 200, 61)
        stroke.strokeWidth = 4f
        canvas.drawRoundRect(rect, 28f, 28f, stroke)

        title.textSize = h * 0.043f
        title.color = Color.argb((255 * alpha).toInt(), 255, 215, 92)
        canvas.drawText("CITADEL EVOLVED!", w * 0.5f, top + bannerH * 0.47f, title)

        body.textSize = h * 0.028f
        body.color = Color.argb((255 * alpha).toInt(), 255, 255, 255)
        canvas.drawText("TIER ${gameWorld.castle.tier}  •  New fortress form unlocked", w * 0.5f, top + bannerH * 0.76f, body)
    }
}
