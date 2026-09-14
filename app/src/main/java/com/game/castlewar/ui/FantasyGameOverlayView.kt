package com.game.castlewar.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.MotionEvent
import android.view.View
import com.game.castlewar.game.GameManager
import com.game.castlewar.game.GameState
import com.game.castlewar.input.VirtualJoystick
import com.game.castlewar.resources.ResourceType
import com.game.castlewar.upgrades.UpgradeCard
import com.game.castlewar.upgrades.UpgradeManager
import com.game.castlewar.world.GameWorld
import kotlin.math.abs
import kotlin.math.sin

/**
 * Premium fantasy HUD/menu skin for the walking-citadel game.
 * Keeps the existing game flow/touch behavior while replacing the flat UI with
 * layered parchment, iron, brass, crystal and heraldic visual language.
 */
class FantasyGameOverlayView(
    context: Context,
    private val gameManager: GameManager,
    private val gameWorld: GameWorld,
    private val joystick: VirtualJoystick
) : View(context) {

    private val upgrades = UpgradeManager()
    private var choices: List<UpgradeCard> = emptyList()

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GOLD
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val pauseRect = RectF()
    private val playRect = RectF()
    private val restartRect = RectF()
    private val resumeRect = RectF()
    private val menuRect = RectF()
    private val cardRects = mutableListOf<RectF>()

    private var tutorialStep = 0
    private var tutorialFinished = false
    private var lastTier = gameWorld.castle.tier
    private var tierBannerUntil = 0L

    init {
        isFocusable = true
        isClickable = true

        gameWorld.onLevelUpTriggered = {
            post {
                choices = upgrades.getRandomChoices(3)
                gameManager.triggerLevelUp()
                invalidate()
            }
        }
        gameWorld.onGameOverTriggered = {
            post {
                gameManager.triggerGameOver()
                invalidate()
            }
        }
        gameManager.addStateListener { _, _ -> postInvalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        updateTutorial()

        when (gameManager.currentState) {
            GameState.MAIN_MENU -> drawMainMenu(canvas, w, h)
            GameState.PLAYING, GameState.BOSS_FIGHT -> {
                drawHud(canvas, w, h)
                drawJoystick(canvas, w, h)
                drawTutorial(canvas, w, h)
                drawTierCelebration(canvas, w, h)
            }
            GameState.PAUSED -> {
                drawHud(canvas, w, h)
                drawModalBackdrop(canvas, w, h)
                drawPause(canvas, w, h)
            }
            GameState.LEVEL_UP -> {
                drawHud(canvas, w, h)
                drawLevelUp(canvas, w, h)
            }
            GameState.GAME_OVER -> drawGameOver(canvas, w, h)
            GameState.UPGRADE_MENU -> drawMainMenu(canvas, w, h)
        }

        postInvalidateOnAnimation()
    }

    private fun drawMainMenu(canvas: Canvas, w: Float, h: Float) {
        fill.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(18, 28, 35), Color.rgb(7, 10, 13), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, fill)
        fill.shader = null

        // Soft magical glow behind title.
        fill.shader = RadialGradient(w * 0.5f, h * 0.25f, h * 0.42f, Color.argb(80, 74, 128, 172), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawCircle(w * 0.5f, h * 0.25f, h * 0.42f, fill)
        fill.shader = null

        val panel = RectF(w * 0.18f, h * 0.10f, w * 0.82f, h * 0.88f)
        drawFantasyPanel(canvas, panel, Color.rgb(34, 39, 43), Color.rgb(13, 16, 18), GOLD_DARK, 28f)
        drawCornerOrnaments(canvas, panel)

        title.textSize = h * 0.105f
        title.color = GOLD_LIGHT
        canvas.drawText("CASTLE WAR", w * 0.5f, h * 0.28f, title)

        text.textAlign = Paint.Align.CENTER
        text.textSize = h * 0.032f
        text.color = PARCHMENT
        canvas.drawText("THE WANDERING CITADEL", w * 0.5f, h * 0.35f, text)

        stroke.color = Color.argb(180, 203, 159, 71)
        stroke.strokeWidth = 2f
        canvas.drawLine(w * 0.34f, h * 0.39f, w * 0.66f, h * 0.39f, stroke)

        val btnW = w * 0.31f
        val btnH = h * 0.13f
        val bx = (w - btnW) * 0.5f
        val by = h * 0.50f
        playRect.set(bx, by, bx + btnW, by + btnH)
        drawBevelButton(canvas, playRect, CRIMSON, Color.rgb(87, 19, 20), "BEGIN MARCH")

        text.textSize = h * 0.025f
        text.color = Color.rgb(189, 194, 188)
        canvas.drawText("STEER  •  CRUSH  •  LOOT  •  EVOLVE", w * 0.5f, h * 0.72f, text)
        text.textSize = h * 0.020f
        text.color = Color.rgb(130, 139, 137)
        canvas.drawText("Weapons fire automatically. Keep the citadel moving.", w * 0.5f, h * 0.78f, text)
    }

    private fun drawHud(canvas: Canvas, w: Float, h: Float) {
        val castle = gameWorld.castle

        // Hero crest panel.
        val left = RectF(18f, 14f, (w * 0.31f).coerceAtLeast(390f), 112f)
        drawFantasyPanel(canvas, left, Color.argb(235, 28, 34, 37), Color.argb(245, 11, 15, 17), GOLD_DARK, 18f)

        val crestX = left.left + 43f
        val crestY = left.centerY()
        fill.shader = RadialGradient(crestX, crestY, 31f, Color.rgb(74, 130, 172), Color.rgb(21, 42, 60), Shader.TileMode.CLAMP)
        canvas.drawCircle(crestX, crestY, 31f, fill)
        fill.shader = null
        stroke.color = GOLD
        stroke.strokeWidth = 3f
        canvas.drawCircle(crestX, crestY, 31f, stroke)
        title.textSize = 23f
        title.color = Color.WHITE
        canvas.drawText(castle.tier.toString(), crestX, crestY + 8f, title)

        text.textAlign = Paint.Align.LEFT
        text.textSize = 15f
        text.isFakeBoldText = true
        text.color = GOLD_LIGHT
        canvas.drawText("CITADEL  •  LEVEL ${castle.level}", left.left + 84f, left.top + 27f, text)
        text.isFakeBoldText = false

        val barLeft = left.left + 84f
        val barRight = left.right - 14f
        val hpRatio = castle.healthRatio
        drawBar(canvas, RectF(barLeft, left.top + 39f, barRight, left.top + 63f), hpRatio, Color.rgb(207, 52, 46), Color.rgb(94, 20, 22))
        text.textSize = 11f
        text.color = Color.WHITE
        canvas.drawText("HP ${castle.currentHealth.toInt()} / ${castle.maximumHealth.toInt()}", barLeft + 8f, left.top + 55f, text)

        val xpRatio = (castle.experience.toFloat() / castle.experienceRequired).coerceIn(0f, 1f)
        drawBar(canvas, RectF(barLeft, left.top + 70f, barRight, left.top + 86f), xpRatio, Color.rgb(65, 177, 225), Color.rgb(17, 62, 91))
        text.textSize = 10f
        text.color = Color.rgb(211, 232, 242)
        canvas.drawText("ARCANE XP  ${castle.experience}/${castle.experienceRequired}", barLeft + 6f, left.top + 82f, text)

        // Center timer banner.
        val timer = RectF(w * 0.435f, 14f, w * 0.565f, 86f)
        drawFantasyPanel(canvas, timer, Color.argb(240, 34, 36, 38), Color.argb(250, 12, 14, 16), GOLD_DARK, 16f)
        val secondsAll = gameManager.runStats.survivalTimeSeconds.toInt()
        val timerString = String.format("%02d:%02d", secondsAll / 60, secondsAll % 60)
        title.textSize = 29f
        title.color = GOLD_LIGHT
        canvas.drawText(timerString, timer.centerX(), timer.top + 35f, title)
        text.textAlign = Paint.Align.CENTER
        text.textSize = 10f
        text.color = Color.rgb(160, 166, 164)
        canvas.drawText("MARCH TIME", timer.centerX(), timer.bottom - 10f, text)

        // Treasury segmented bar.
        val res = RectF(w * 0.68f, 14f, w - 80f, 78f)
        drawFantasyPanel(canvas, res, Color.argb(235, 28, 34, 37), Color.argb(245, 11, 15, 17), GOLD_DARK, 16f)
        val values = arrayOf(
            Triple("G", castle.getResource(ResourceType.GOLD), Color.rgb(242, 193, 55)),
            Triple("W", castle.getResource(ResourceType.WOOD), Color.rgb(184, 125, 70)),
            Triple("S", castle.getResource(ResourceType.STONE), Color.rgb(166, 176, 181)),
            Triple("I", castle.getResource(ResourceType.IRON), Color.rgb(95, 181, 207))
        )
        val cell = res.width() / 4f
        values.forEachIndexed { index, value ->
            val cx = res.left + cell * index + 20f
            val cy = res.centerY()
            fill.color = value.third
            canvas.drawCircle(cx, cy, 11f, fill)
            text.textAlign = Paint.Align.CENTER
            text.textSize = 9f
            text.isFakeBoldText = true
            text.color = Color.rgb(22, 24, 25)
            canvas.drawText(value.first, cx, cy + 3f, text)
            text.isFakeBoldText = false
            text.textAlign = Paint.Align.LEFT
            text.textSize = 13f
            text.color = Color.WHITE
            canvas.drawText(value.second.toString(), cx + 17f, cy + 5f, text)
        }

        val p = 52f
        pauseRect.set(w - p - 16f, 14f, w - 16f, 14f + p)
        drawFantasyPanel(canvas, pauseRect, Color.rgb(45, 49, 51), Color.rgb(17, 20, 22), GOLD_DARK, 14f)
        title.textSize = 24f
        title.color = PARCHMENT
        canvas.drawText("II", pauseRect.centerX(), pauseRect.centerY() + 8f, title)

        gameWorld.activeBoss?.let { boss ->
            val br = RectF(w * 0.27f, 100f, w * 0.73f, 150f)
            drawFantasyPanel(canvas, br, Color.argb(245, 58, 16, 18), Color.argb(250, 24, 8, 10), Color.rgb(145, 41, 38), 12f)
            text.textAlign = Paint.Align.CENTER
            text.isFakeBoldText = true
            text.textSize = 12f
            text.color = Color.rgb(245, 207, 190)
            canvas.drawText("SIEGE TITAN", br.centerX(), br.top + 17f, text)
            text.isFakeBoldText = false
            drawBar(canvas, RectF(br.left + 15f, br.top + 25f, br.right - 15f, br.bottom - 10f), (boss.health / boss.maxHealth).coerceIn(0f, 1f), Color.rgb(196, 34, 34), Color.rgb(74, 11, 15))
        }
    }

    private fun drawJoystick(canvas: Canvas, w: Float, h: Float) {
        val cx = if (joystick.isActive) joystick.centerX else w * 0.12f
        val cy = if (joystick.isActive) joystick.centerY else h * 0.80f
        val kx = if (joystick.isActive) joystick.knobX else cx
        val ky = if (joystick.isActive) joystick.knobY else cy

        fill.shader = RadialGradient(cx, cy, joystick.baseRadius, Color.argb(95, 89, 123, 130), Color.argb(18, 12, 18, 20), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, joystick.baseRadius, fill)
        fill.shader = null
        stroke.color = Color.argb(180, 197, 154, 67)
        stroke.strokeWidth = 4f
        canvas.drawCircle(cx, cy, joystick.baseRadius, stroke)
        stroke.color = Color.argb(100, 211, 180, 105)
        stroke.strokeWidth = 1.5f
        canvas.drawCircle(cx, cy, joystick.baseRadius * 0.74f, stroke)

        val pulse = ((sin(System.currentTimeMillis() / 220.0) + 1.0) * 0.5).toFloat()
        fill.shader = RadialGradient(kx, ky, joystick.knobRadius, Color.rgb(239, 204, 113), Color.rgb(102, 70, 31), Shader.TileMode.CLAMP)
        canvas.drawCircle(kx, ky, joystick.knobRadius + pulse * 2f, fill)
        fill.shader = null
        stroke.color = GOLD_LIGHT
        stroke.strokeWidth = 2f
        canvas.drawCircle(kx, ky, joystick.knobRadius, stroke)
    }

    private fun drawLevelUp(canvas: Canvas, w: Float, h: Float) {
        drawModalBackdrop(canvas, w, h)
        val header = RectF(w * 0.27f, h * 0.07f, w * 0.73f, h * 0.20f)
        drawFantasyPanel(canvas, header, Color.rgb(55, 45, 31), Color.rgb(19, 18, 18), GOLD, 20f)
        title.textSize = h * 0.055f
        title.color = GOLD_LIGHT
        canvas.drawText("CHOOSE YOUR LEGACY", w * 0.5f, h * 0.135f, title)
        text.textAlign = Paint.Align.CENTER
        text.textSize = h * 0.021f
        text.color = PARCHMENT
        canvas.drawText("One relic will reshape the wandering fortress", w * 0.5f, h * 0.175f, text)

        cardRects.clear()
        val count = choices.size.coerceAtMost(3)
        if (count == 0) return
        val cardW = w * 0.25f
        val cardH = h * 0.59f
        val gap = w * 0.035f
        val total = count * cardW + (count - 1) * gap
        val start = (w - total) * 0.5f
        val y = h * 0.26f

        repeat(count) { i ->
            val card = choices[i]
            val rect = RectF(start + i * (cardW + gap), y, start + i * (cardW + gap) + cardW, y + cardH)
            cardRects.add(rect)
            val rarity = card.rarity.colorHex.toInt()
            drawFantasyPanel(canvas, rect, Color.rgb(39, 43, 45), Color.rgb(15, 18, 20), rarity, 22f)

            fill.color = Color.argb(90, Color.red(rarity), Color.green(rarity), Color.blue(rarity))
            canvas.drawCircle(rect.centerX(), rect.top + cardH * 0.18f, cardH * 0.09f, fill)
            stroke.color = rarity
            stroke.strokeWidth = 3f
            canvas.drawCircle(rect.centerX(), rect.top + cardH * 0.18f, cardH * 0.09f, stroke)
            text.textAlign = Paint.Align.CENTER
            text.textSize = cardH * 0.105f
            text.color = Color.WHITE
            canvas.drawText(card.iconSymbol, rect.centerX(), rect.top + cardH * 0.215f, text)

            text.textSize = cardH * 0.032f
            text.isFakeBoldText = true
            text.color = rarity
            canvas.drawText(card.rarity.label.uppercase(), rect.centerX(), rect.top + cardH * 0.35f, text)
            text.textSize = cardH * 0.052f
            text.color = Color.WHITE
            canvas.drawText(card.title, rect.centerX(), rect.top + cardH * 0.45f, text)
            text.isFakeBoldText = false

            text.textSize = cardH * 0.032f
            text.color = Color.rgb(199, 204, 201)
            drawWrapped(canvas, card.description, rect.centerX(), rect.top + cardH * 0.55f, cardW - 36f, text)

            val button = RectF(rect.left + 18f, rect.bottom - cardH * 0.16f, rect.right - 18f, rect.bottom - 18f)
            drawBevelButton(canvas, button, rarity, darken(rarity, 0.48f), "CLAIM RELIC")
        }
    }

    private fun drawPause(canvas: Canvas, w: Float, h: Float) {
        val panel = RectF(w * 0.34f, h * 0.18f, w * 0.66f, h * 0.86f)
        drawFantasyPanel(canvas, panel, Color.rgb(39, 43, 45), Color.rgb(12, 15, 17), GOLD_DARK, 26f)
        drawCornerOrnaments(canvas, panel)
        title.textSize = h * 0.065f
        title.color = PARCHMENT
        canvas.drawText("WAR COUNCIL", panel.centerX(), h * 0.30f, title)

        val bw = panel.width() * 0.68f
        val bh = h * 0.105f
        val bx = panel.centerX() - bw * 0.5f
        resumeRect.set(bx, h * 0.40f, bx + bw, h * 0.40f + bh)
        restartRect.set(bx, h * 0.56f, bx + bw, h * 0.56f + bh)
        menuRect.set(bx, h * 0.72f, bx + bw, h * 0.72f + bh)
        drawBevelButton(canvas, resumeRect, Color.rgb(48, 116, 79), Color.rgb(20, 62, 43), "RESUME MARCH")
        drawBevelButton(canvas, restartRect, Color.rgb(151, 55, 39), Color.rgb(75, 23, 20), "RESTART RUN")
        drawBevelButton(canvas, menuRect, Color.rgb(67, 76, 82), Color.rgb(29, 35, 38), "RETURN TO KEEP")
    }

    private fun drawGameOver(canvas: Canvas, w: Float, h: Float) {
        fill.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(50, 12, 14), Color.rgb(8, 9, 10), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, fill)
        fill.shader = null
        val panel = RectF(w * 0.27f, h * 0.13f, w * 0.73f, h * 0.87f)
        drawFantasyPanel(canvas, panel, Color.rgb(48, 36, 34), Color.rgb(15, 14, 15), Color.rgb(150, 48, 42), 28f)
        title.textSize = h * 0.075f
        title.color = Color.rgb(242, 110, 84)
        canvas.drawText("THE CITADEL HAS FALLEN", panel.centerX(), h * 0.29f, title)

        text.textAlign = Paint.Align.CENTER
        text.textSize = h * 0.027f
        text.color = PARCHMENT
        val secondsAll = gameManager.runStats.survivalTimeSeconds.toInt()
        canvas.drawText("SURVIVED  ${secondsAll / 60}m ${secondsAll % 60}s   •   LEVEL ${gameWorld.castle.level}", panel.centerX(), h * 0.39f, text)
        canvas.drawText("GOLD RECOVERED  ${gameWorld.castle.getResource(ResourceType.GOLD)}", panel.centerX(), h * 0.45f, text)

        val bw = panel.width() * 0.55f
        val bh = h * 0.11f
        val bx = panel.centerX() - bw * 0.5f
        restartRect.set(bx, h * 0.56f, bx + bw, h * 0.56f + bh)
        menuRect.set(bx, h * 0.72f, bx + bw, h * 0.72f + bh)
        drawBevelButton(canvas, restartRect, CRIMSON, Color.rgb(80, 18, 19), "MARCH AGAIN")
        drawBevelButton(canvas, menuRect, Color.rgb(65, 75, 79), Color.rgb(27, 33, 35), "RETURN TO KEEP")
    }

    private fun drawTutorial(canvas: Canvas, w: Float, h: Float) {
        if (tutorialFinished) return
        val message = when (tutorialStep) {
            0 -> "DRAG THE CREST TO MOVE YOUR CITADEL"
            1 -> "APPROACH ENEMIES — YOUR WEAPONS FIRE AUTOMATICALLY"
            else -> "COLLECT ARCANE XP TO EVOLVE THE FORTRESS"
        }
        val rect = RectF(w * 0.31f, h * 0.86f, w * 0.69f, h * 0.94f)
        drawFantasyPanel(canvas, rect, Color.argb(220, 33, 39, 41), Color.argb(235, 12, 15, 17), GOLD_DARK, 16f)
        text.textAlign = Paint.Align.CENTER
        text.textSize = h * 0.019f
        text.isFakeBoldText = true
        text.color = GOLD_LIGHT
        canvas.drawText("BATTLE GUIDE ${tutorialStep + 1}/3", rect.centerX(), rect.top + rect.height() * 0.37f, text)
        text.textSize = h * 0.016f
        text.isFakeBoldText = false
        text.color = Color.rgb(211, 216, 211)
        canvas.drawText(message, rect.centerX(), rect.top + rect.height() * 0.70f, text)
    }

    private fun drawTierCelebration(canvas: Canvas, w: Float, h: Float) {
        val now = System.currentTimeMillis()
        if (now >= tierBannerUntil) return
        val remaining = (tierBannerUntil - now).coerceAtLeast(0L)
        val alpha = (remaining / 650f).coerceIn(0f, 1f)
        stroke.color = Color.argb((180 * alpha).toInt(), 245, 196, 74)
        stroke.strokeWidth = 8f
        canvas.drawRoundRect(RectF(8f, 8f, w - 8f, h - 8f), 28f, 28f, stroke)
        val rect = RectF(w * 0.34f, h * 0.23f, w * 0.66f, h * 0.37f)
        drawFantasyPanel(canvas, rect, Color.argb((235 * alpha).toInt(), 54, 45, 28), Color.argb((245 * alpha).toInt(), 14, 16, 18), GOLD, 22f)
        title.textSize = h * 0.044f
        title.color = GOLD_LIGHT
        canvas.drawText("CITADEL EVOLVED", rect.centerX(), rect.top + rect.height() * 0.48f, title)
        text.textAlign = Paint.Align.CENTER
        text.textSize = h * 0.023f
        text.color = Color.WHITE
        canvas.drawText("TIER ${gameWorld.castle.tier} FORTRESS UNLOCKED", rect.centerX(), rect.top + rect.height() * 0.76f, text)
    }

    private fun updateTutorial() {
        val tier = gameWorld.castle.tier
        if (tier < lastTier) {
            lastTier = tier
            tutorialStep = 0
            tutorialFinished = false
        } else if (tier > lastTier) {
            lastTier = tier
            tierBannerUntil = System.currentTimeMillis() + 2500L
            tutorialFinished = true
        }
        if (tutorialFinished) return
        val moved = abs(joystick.inputX) > 0.12f || abs(joystick.inputY) > 0.12f
        if (tutorialStep == 0 && moved) tutorialStep = 1
        if (tutorialStep <= 1 && gameWorld.castle.experience > 0) tutorialStep = 2
        if (gameWorld.castle.level >= 2) tutorialFinished = true
    }

    private fun drawModalBackdrop(canvas: Canvas, w: Float, h: Float) {
        fill.color = Color.argb(205, 4, 7, 9)
        fill.shader = null
        canvas.drawRect(0f, 0f, w, h, fill)
    }

    private fun drawFantasyPanel(canvas: Canvas, rect: RectF, topColor: Int, bottomColor: Int, borderColor: Int, radius: Float) {
        fill.shader = LinearGradient(rect.left, rect.top, rect.left, rect.bottom, topColor, bottomColor, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, radius, radius, fill)
        fill.shader = null
        stroke.color = borderColor
        stroke.strokeWidth = 3f
        canvas.drawRoundRect(rect, radius, radius, stroke)
        val inner = RectF(rect.left + 5f, rect.top + 5f, rect.right - 5f, rect.bottom - 5f)
        stroke.color = Color.argb(65, 255, 231, 176)
        stroke.strokeWidth = 1f
        canvas.drawRoundRect(inner, (radius - 4f).coerceAtLeast(4f), (radius - 4f).coerceAtLeast(4f), stroke)
    }

    private fun drawBevelButton(canvas: Canvas, rect: RectF, topColor: Int, bottomColor: Int, label: String) {
        fill.shader = LinearGradient(rect.left, rect.top, rect.left, rect.bottom, topColor, bottomColor, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, 15f, 15f, fill)
        fill.shader = null
        stroke.color = GOLD
        stroke.strokeWidth = 3f
        canvas.drawRoundRect(rect, 15f, 15f, stroke)
        stroke.color = Color.argb(90, 255, 245, 210)
        stroke.strokeWidth = 1f
        canvas.drawRoundRect(RectF(rect.left + 5f, rect.top + 5f, rect.right - 5f, rect.bottom - 5f), 11f, 11f, stroke)
        title.textSize = rect.height() * 0.36f
        title.color = Color.WHITE
        canvas.drawText(label, rect.centerX(), rect.centerY() + title.textSize * 0.35f, title)
    }

    private fun drawBar(canvas: Canvas, rect: RectF, ratio: Float, startColor: Int, endColor: Int) {
        fill.color = Color.rgb(19, 22, 23)
        fill.shader = null
        canvas.drawRoundRect(rect, rect.height() * 0.35f, rect.height() * 0.35f, fill)
        val r = ratio.coerceIn(0f, 1f)
        if (r > 0f) {
            val filled = RectF(rect.left, rect.top, rect.left + rect.width() * r, rect.bottom)
            fill.shader = LinearGradient(filled.left, filled.top, filled.right, filled.bottom, startColor, endColor, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(filled, rect.height() * 0.35f, rect.height() * 0.35f, fill)
            fill.shader = null
        }
        stroke.color = Color.argb(110, 230, 212, 171)
        stroke.strokeWidth = 1f
        canvas.drawRoundRect(rect, rect.height() * 0.35f, rect.height() * 0.35f, stroke)
    }

    private fun drawCornerOrnaments(canvas: Canvas, rect: RectF) {
        stroke.color = GOLD
        stroke.strokeWidth = 3f
        val s = 20f
        canvas.drawLine(rect.left + 8f, rect.top + s, rect.left + 8f, rect.top + 8f, stroke)
        canvas.drawLine(rect.left + 8f, rect.top + 8f, rect.left + s, rect.top + 8f, stroke)
        canvas.drawLine(rect.right - s, rect.top + 8f, rect.right - 8f, rect.top + 8f, stroke)
        canvas.drawLine(rect.right - 8f, rect.top + 8f, rect.right - 8f, rect.top + s, stroke)
        canvas.drawLine(rect.left + 8f, rect.bottom - s, rect.left + 8f, rect.bottom - 8f, stroke)
        canvas.drawLine(rect.left + 8f, rect.bottom - 8f, rect.left + s, rect.bottom - 8f, stroke)
        canvas.drawLine(rect.right - s, rect.bottom - 8f, rect.right - 8f, rect.bottom - 8f, stroke)
        canvas.drawLine(rect.right - 8f, rect.bottom - s, rect.right - 8f, rect.bottom - 8f, stroke)
    }

    private fun drawWrapped(canvas: Canvas, value: String, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        val words = value.split(" ")
        var line = ""
        var yy = y
        words.forEach { word ->
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) <= maxWidth) {
                line = test
            } else {
                canvas.drawText(line, x, yy, paint)
                line = word
                yy += paint.textSize * 1.32f
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, yy, paint)
    }

    private fun darken(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) * factor).toInt().coerceIn(0, 255),
        (Color.green(color) * factor).toInt().coerceIn(0, 255),
        (Color.blue(color) * factor).toInt().coerceIn(0, 255)
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val index = event.actionIndex
        val pointerId = event.getPointerId(index)
        val x = event.getX(index)
        val y = event.getY(index)

        when (gameManager.currentState) {
            GameState.MAIN_MENU -> if (action == MotionEvent.ACTION_UP && playRect.contains(x, y)) {
                gameWorld.reset(); gameManager.startGame(); return true
            }
            GameState.PLAYING, GameState.BOSS_FIGHT -> {
                if (action == MotionEvent.ACTION_UP && pauseRect.contains(x, y)) {
                    gameManager.pauseGame(); return true
                }
                when (action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> if (joystick.onTouchDown(pointerId, x, y, width.toFloat(), height.toFloat())) return true
                    MotionEvent.ACTION_MOVE -> {
                        for (i in 0 until event.pointerCount) joystick.onTouchMove(event.getPointerId(i), event.getX(i), event.getY(i))
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> { joystick.onTouchUp(pointerId); return true }
                    MotionEvent.ACTION_CANCEL -> { joystick.reset(); return true }
                }
            }
            GameState.PAUSED -> if (action == MotionEvent.ACTION_UP) {
                when {
                    resumeRect.contains(x, y) -> { gameManager.resumeGame(); return true }
                    restartRect.contains(x, y) -> { gameWorld.reset(); gameManager.startGame(); return true }
                    menuRect.contains(x, y) -> { gameManager.returnToMainMenu(); return true }
                }
            }
            GameState.LEVEL_UP -> if (action == MotionEvent.ACTION_UP) {
                for (i in cardRects.indices) {
                    if (cardRects[i].contains(x, y) && i < choices.size) {
                        choices[i].apply(gameWorld.castle)
                        gameManager.completeLevelUp()
                        return true
                    }
                }
            }
            GameState.GAME_OVER -> if (action == MotionEvent.ACTION_UP) {
                when {
                    restartRect.contains(x, y) -> { gameWorld.reset(); gameManager.startGame(); return true }
                    menuRect.contains(x, y) -> { gameManager.returnToMainMenu(); return true }
                }
            }
            GameState.UPGRADE_MENU -> if (action == MotionEvent.ACTION_UP) { gameManager.returnToMainMenu(); return true }
        }
        return true
    }

    companion object {
        private val GOLD = Color.rgb(191, 145, 56)
        private val GOLD_DARK = Color.rgb(123, 89, 36)
        private val GOLD_LIGHT = Color.rgb(245, 211, 124)
        private val PARCHMENT = Color.rgb(233, 224, 199)
        private val CRIMSON = Color.rgb(158, 41, 43)
    }
}
