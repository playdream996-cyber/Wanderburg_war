package com.game.castlewar.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.game.castlewar.entities.MovingCastle
import com.game.castlewar.game.GameManager
import com.game.castlewar.game.GameState
import com.game.castlewar.input.VirtualJoystick
import com.game.castlewar.resources.ResourceType
import com.game.castlewar.upgrades.UpgradeCard
import com.game.castlewar.upgrades.UpgradeManager
import com.game.castlewar.world.GameWorld

/**
 * High-performance HUD & UI overlay drawn directly over the OpenGL ES GameSurfaceView.
 * Renders the virtual joystick, castle health/XP bars, resources, level-up upgrade cards,
 * pause screen, and main menu with direct touch interactions.
 */
class GameOverlayView(
    context: Context,
    val gameManager: GameManager,
    val gameWorld: GameWorld,
    val joystick: VirtualJoystick
) : View(context) {

    private val upgradeManager = UpgradeManager()
    private var currentUpgradeChoices: List<UpgradeCard> = emptyList()

    // Paints
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700") // Gold
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val cardTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        textAlign = Paint.Align.CENTER
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    // UI Clickable Bounds
    private val pauseBtnRect = RectF()
    private val playBtnRect = RectF()
    private val restartBtnRect = RectF()
    private val resumeBtnRect = RectF()
    private val mainMenuBtnRect = RectF()
    private val cardRects = mutableListOf<RectF>()

    init {
        isFocusable = true
        isClickable = true

        // Subscribe to level-up triggers
        gameWorld.onLevelUpTriggered = {
            post {
                currentUpgradeChoices = upgradeManager.getRandomChoices(3)
                gameManager.triggerLevelUp()
                invalidate()
            }
        }

        // Subscribe to game over
        gameWorld.onGameOverTriggered = {
            post {
                gameManager.triggerGameOver()
                invalidate()
            }
        }

        // State changes refresh UI
        gameManager.addStateListener { _, _ ->
            postInvalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        when (gameManager.currentState) {
            GameState.MAIN_MENU -> renderMainMenu(canvas, w, h)
            GameState.PLAYING, GameState.BOSS_FIGHT -> {
                renderHUD(canvas, w, h)
                renderJoystick(canvas)
            }
            GameState.PAUSED -> {
                renderHUD(canvas, w, h)
                renderPauseModal(canvas, w, h)
            }
            GameState.LEVEL_UP -> {
                renderHUD(canvas, w, h)
                renderLevelUpModal(canvas, w, h)
            }
            GameState.GAME_OVER -> {
                renderGameOver(canvas, w, h)
            }
            GameState.UPGRADE_MENU -> {
                renderUpgradeMenu(canvas, w, h)
            }
        }

        // Continuous redraw for responsive HUD animations synchronized with display VSYNC
        postInvalidateOnAnimation()
    }

    // =========================================================================
    // MAIN MENU
    // =========================================================================

    private fun renderMainMenu(canvas: Canvas, w: Float, h: Float) {
        // Semi-translucent dark vignette
        fillPaint.color = Color.parseColor("#C80B0F0C")
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        // Title
        titlePaint.textSize = h * 0.12f
        titlePaint.color = Color.parseColor("#FFD700")
        canvas.drawText("CASTLE WAR", w * 0.5f, h * 0.28f, titlePaint)

        // Subtitle
        textPaint.textSize = h * 0.045f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.parseColor("#B0BEC5")
        canvas.drawText("Mobile Walking Citadel Roguelike", w * 0.5f, h * 0.36f, textPaint)

        // Play Button
        val btnW = w * 0.32f
        val btnH = h * 0.14f
        val btnX = (w - btnW) * 0.5f
        val btnY = h * 0.50f
        playBtnRect.set(btnX, btnY, btnX + btnW, btnY + btnH)

        fillPaint.color = Color.parseColor("#B71C1C") // Crimson
        canvas.drawRoundRect(playBtnRect, 18f, 18f, fillPaint)
        strokePaint.color = Color.parseColor("#FFD700")
        strokePaint.strokeWidth = 5f
        canvas.drawRoundRect(playBtnRect, 18f, 18f, strokePaint)

        titlePaint.textSize = btnH * 0.46f
        titlePaint.color = Color.WHITE
        canvas.drawText("START BATTLE", w * 0.5f, btnY + btnH * 0.64f, titlePaint)

        // How to play hint
        textPaint.textSize = h * 0.038f
        textPaint.color = Color.parseColor("#90A4AE")
        canvas.drawText("Use the on-screen joystick to steer your armored fortress.", w * 0.5f, h * 0.74f, textPaint)
        canvas.drawText("Cannons, archers, and ballistas automatically acquire and destroy targets.", w * 0.5f, h * 0.80f, textPaint)
    }

    // =========================================================================
    // GAMEPLAY HUD
    // =========================================================================

    private fun renderHUD(canvas: Canvas, w: Float, h: Float) {
        val castle = gameWorld.castle

        // 1. Top-Left Floating Medieval Fortress Status Plaque
        val plateW = (w * 0.28f).coerceAtLeast(300f)
        val plateH = 78f
        val plateX = 20f
        val plateY = 16f
        val plaqueRect = RectF(plateX, plateY, plateX + plateW, plateY + plateH)

        // Slate background with warm dark tone
        fillPaint.color = Color.parseColor("#E6181C20")
        canvas.drawRoundRect(plaqueRect, 14f, 14f, fillPaint)
        // Gilded brass border
        strokePaint.color = Color.parseColor("#B8860B")
        strokePaint.strokeWidth = 2.5f
        canvas.drawRoundRect(plaqueRect, 14f, 14f, strokePaint)

        // Tier / Level Crest Header
        textPaint.textSize = 15f
        textPaint.color = Color.parseColor("#FFD700")
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("🏰 CITADEL TIER ${castle.tier}  •  LVL ${castle.level}", plateX + 14f, plateY + 22f, textPaint)

        // Fortress HP Bar
        val hpBarW = plateW - 28f
        val hpBarH = 18f
        val hpBarX = plateX + 14f
        val hpBarY = plateY + 28f

        fillPaint.color = Color.parseColor("#2A0E10")
        canvas.drawRoundRect(hpBarX, hpBarY, hpBarX + hpBarW, hpBarY + hpBarH, 6f, 6f, fillPaint)

        val hpRatio = (castle.currentHealth / castle.maximumHealth).coerceIn(0f, 1f)
        fillPaint.color = Color.parseColor("#E53935") // Crimson
        canvas.drawRoundRect(hpBarX, hpBarY, hpBarX + hpBarW * hpRatio, hpBarY + hpBarH, 6f, 6f, fillPaint)

        // HP Bar border
        strokePaint.color = Color.parseColor("#5F1D1D")
        strokePaint.strokeWidth = 1.5f
        canvas.drawRoundRect(hpBarX, hpBarY, hpBarX + hpBarW, hpBarY + hpBarH, 6f, 6f, strokePaint)

        textPaint.textSize = 12f
        textPaint.color = Color.WHITE
        canvas.drawText("🛡️ HP ${castle.currentHealth.toInt()} / ${castle.maximumHealth.toInt()}", hpBarX + 8f, hpBarY + 13f, textPaint)

        // Fortress XP Bar
        val xpBarY = hpBarY + hpBarH + 5f
        val xpBarH = 10f
        val xpRatio = (castle.experience.toFloat() / castle.experienceRequired).coerceIn(0f, 1f)

        fillPaint.color = Color.parseColor("#0C2028")
        canvas.drawRoundRect(hpBarX, xpBarY, hpBarX + hpBarW, xpBarY + xpBarH, 4f, 4f, fillPaint)

        fillPaint.color = Color.parseColor("#00BCD4") // Arcane Cyan
        canvas.drawRoundRect(hpBarX, xpBarY, hpBarX + hpBarW * xpRatio, xpBarY + xpBarH, 4f, 4f, fillPaint)

        // 2. Top-Center Mission Plaque & Survival Timer
        val timerW = 180f
        val timerH = 58f
        val timerX = (w - timerW) * 0.5f
        val timerY = 16f
        val timerRect = RectF(timerX, timerY, timerX + timerW, timerY + timerH)

        fillPaint.color = Color.parseColor("#E6181C20")
        canvas.drawRoundRect(timerRect, 14f, 14f, fillPaint)
        strokePaint.color = Color.parseColor("#B8860B")
        strokePaint.strokeWidth = 2.5f
        canvas.drawRoundRect(timerRect, 14f, 14f, strokePaint)

        val timeSec = gameManager.runStats.survivalTimeSeconds.toInt()
        val minutes = timeSec / 60
        val seconds = timeSec % 60
        val timerStr = String.format("%02d:%02d", minutes, seconds)

        titlePaint.textSize = 24f
        titlePaint.color = Color.parseColor("#FFD700")
        canvas.drawText(timerStr, w * 0.5f, timerY + 30f, titlePaint)

        textPaint.textSize = 11f
        textPaint.color = Color.parseColor("#B0BEC5")
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("SURVIVAL TIME", w * 0.5f, timerY + 48f, textPaint)

        // 3. Top-Right Floating Resource Treasury Pill
        val resW = (w * 0.28f).coerceAtLeast(290f)
        val resH = 48f
        val pauseBtnSize = 48f
        val resX = w - resW - pauseBtnSize - 32f
        val resY = 16f
        val resRect = RectF(resX, resY, resX + resW, resY + resH)

        fillPaint.color = Color.parseColor("#E6181C20")
        canvas.drawRoundRect(resRect, 14f, 14f, fillPaint)
        strokePaint.color = Color.parseColor("#B8860B")
        strokePaint.strokeWidth = 2.5f
        canvas.drawRoundRect(resRect, 14f, 14f, strokePaint)

        val itemSpacing = resW / 4f
        textPaint.textSize = 14f
        textPaint.textAlign = Paint.Align.LEFT

        // Gold
        textPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("💰 ${castle.getResource(ResourceType.GOLD)}", resX + 10f, resY + 30f, textPaint)
        // Wood
        textPaint.color = Color.parseColor("#D7CCC8")
        canvas.drawText("🪵 ${castle.getResource(ResourceType.WOOD)}", resX + itemSpacing + 8f, resY + 30f, textPaint)
        // Stone
        textPaint.color = Color.parseColor("#CFD8DC")
        canvas.drawText("🪨 ${castle.getResource(ResourceType.STONE)}", resX + itemSpacing * 2f + 8f, resY + 30f, textPaint)
        // Iron
        textPaint.color = Color.parseColor("#80DEEA")
        canvas.drawText("⚙️ ${castle.getResource(ResourceType.IRON)}", resX + itemSpacing * 3f + 8f, resY + 30f, textPaint)

        // 4. Pause Button (Top Right Gilded Crest)
        val pX = w - pauseBtnSize - 18f
        val pY = 16f
        pauseBtnRect.set(pX, pY, pX + pauseBtnSize, pY + pauseBtnSize)

        fillPaint.color = Color.parseColor("#E6181C20")
        canvas.drawRoundRect(pauseBtnRect, 12f, 12f, fillPaint)
        strokePaint.color = Color.parseColor("#B8860B")
        strokePaint.strokeWidth = 2.5f
        canvas.drawRoundRect(pauseBtnRect, 12f, 12f, strokePaint)

        textPaint.textSize = 22f
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("⏸", pX + pauseBtnSize * 0.5f, pY + 33f, textPaint)

        // 5. Boss Health Bar (Grand Gothic Battle Banner)
        gameWorld.activeBoss?.let { boss ->
            val bossBarW = (w * 0.52f).coerceAtLeast(360f)
            val bossBarH = 26f
            val bossBarX = (w - bossBarW) * 0.5f
            val bossBarY = timerY + timerH + 16f

            // Banner Background
            fillPaint.color = Color.parseColor("#EE220808")
            canvas.drawRoundRect(bossBarX - 4f, bossBarY - 24f, bossBarX + bossBarW + 4f, bossBarY + bossBarH + 4f, 8f, 8f, fillPaint)
            strokePaint.color = Color.parseColor("#E53935")
            strokePaint.strokeWidth = 2f
            canvas.drawRoundRect(bossBarX - 4f, bossBarY - 24f, bossBarX + bossBarW + 4f, bossBarY + bossBarH + 4f, 8f, 8f, strokePaint)

            // Boss Title
            textPaint.textSize = 14f
            textPaint.color = Color.parseColor("#FFCDD2")
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("⚔️ SIEGE TITAN CITADEL (${boss.health.toInt()} / ${boss.maxHealth.toInt()}) ⚔️", w * 0.5f, bossBarY - 6f, textPaint)

            // Bar Track
            fillPaint.color = Color.parseColor("#3B0D0D")
            canvas.drawRoundRect(bossBarX, bossBarY, bossBarX + bossBarW, bossBarY + bossBarH, 6f, 6f, fillPaint)

            val bossRatio = (boss.health / boss.maxHealth).coerceIn(0f, 1f)
            fillPaint.color = Color.parseColor("#D50000") // Deep Red
            canvas.drawRoundRect(bossBarX, bossBarY, bossBarX + bossBarW * bossRatio, bossBarY + bossBarH, 6f, 6f, fillPaint)
        }
    }

    // =========================================================================
    // VIRTUAL JOYSTICK
    // =========================================================================

    private fun renderJoystick(canvas: Canvas) {
        val cx = if (joystick.isActive) joystick.centerX else 200f
        val cy = if (joystick.isActive) joystick.centerY else (height - 200f)
        val kx = if (joystick.isActive) joystick.knobX else cx
        val ky = if (joystick.isActive) joystick.knobY else cy

        // Medieval Compass Rose Outer Brass Ring
        fillPaint.color = Color.parseColor("#3814181A")
        canvas.drawCircle(cx, cy, joystick.baseRadius, fillPaint)

        strokePaint.color = Color.parseColor("#B8860B") // Antique Gold
        strokePaint.strokeWidth = 4f
        canvas.drawCircle(cx, cy, joystick.baseRadius, strokePaint)

        // 4 Compass Direction Marks (N, S, E, W)
        val markLen = 10f
        val r = joystick.baseRadius
        strokePaint.strokeWidth = 3f
        canvas.drawLine(cx, cy - r, cx, cy - r + markLen, strokePaint)
        canvas.drawLine(cx, cy + r, cx, cy + r - markLen, strokePaint)
        canvas.drawLine(cx - r, cy, cx - r + markLen, cy, strokePaint)
        canvas.drawLine(cx + r, cy, cx + r - markLen, cy, strokePaint)

        // Center Gemstone Knob
        fillPaint.color = Color.parseColor("#E6B8860B") // Rich gold
        canvas.drawCircle(kx, ky, joystick.knobRadius, fillPaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 3f
        canvas.drawCircle(kx, ky, joystick.knobRadius, strokePaint)

        fillPaint.color = Color.parseColor("#FFD700")
        canvas.drawCircle(kx, ky, joystick.knobRadius * 0.45f, fillPaint)
    }

    // =========================================================================
    // LEVEL UP MODAL (3 ROGUELIKE CARDS)
    // =========================================================================

    private fun renderLevelUpModal(canvas: Canvas, w: Float, h: Float) {
        // Dim background
        fillPaint.color = Color.parseColor("#D9080B0C")
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        titlePaint.textSize = h * 0.085f
        titlePaint.color = Color.parseColor("#FFD700")
        canvas.drawText("FORTRESS UPGRADE", w * 0.5f, h * 0.18f, titlePaint)

        textPaint.textSize = h * 0.035f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.parseColor("#CFD8DC")
        canvas.drawText("Choose a Relic to Reinforce Your Moving Citadel", w * 0.5f, h * 0.24f, textPaint)

        cardRects.clear()
        val cardCount = currentUpgradeChoices.size.coerceAtMost(3)
        if (cardCount == 0) return

        val cardW = (w * 0.26f).coerceAtMost(360f)
        val cardH = h * 0.58f
        val totalSpacing = w - (cardCount * cardW)
        val cardGap = totalSpacing / (cardCount + 1)
        val cardY = h * 0.28f

        for (i in 0 until cardCount) {
            val card = currentUpgradeChoices[i]
            val cardX = cardGap + i * (cardW + cardGap)
            val rect = RectF(cardX, cardY, cardX + cardW, cardY + cardH)
            cardRects.add(rect)

            val rarityColor = card.rarity.colorHex.toInt()

            // Card body background (Dark Slate Parchment)
            fillPaint.color = Color.parseColor("#F0161C22")
            canvas.drawRoundRect(rect, 18f, 18f, fillPaint)

            // Outer Gilded Rarity Border
            strokePaint.color = rarityColor
            strokePaint.strokeWidth = 5f
            canvas.drawRoundRect(rect, 18f, 18f, strokePaint)

            // Inner Fine Gold Inset Line
            val insetRect = RectF(cardX + 6f, cardY + 6f, cardX + cardW - 6f, cardY + cardH - 6f)
            strokePaint.color = Color.parseColor("#44B8860B")
            strokePaint.strokeWidth = 1.5f
            canvas.drawRoundRect(insetRect, 14f, 14f, strokePaint)

            // Icon Circular Heraldic Seal
            val sealRadius = cardH * 0.14f
            val sealCy = cardY + cardH * 0.20f
            fillPaint.color = Color.parseColor("#262E38")
            canvas.drawCircle(rect.centerX(), sealCy, sealRadius, fillPaint)
            strokePaint.color = rarityColor
            strokePaint.strokeWidth = 3f
            canvas.drawCircle(rect.centerX(), sealCy, sealRadius, strokePaint)

            // Icon Symbol
            textPaint.textSize = cardH * 0.16f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(card.iconSymbol, rect.centerX(), sealCy + cardH * 0.055f, textPaint)

            // Rarity Banner Ribbon
            textPaint.textSize = cardH * 0.045f
            textPaint.color = rarityColor
            canvas.drawText("✦ ${card.rarity.label.uppercase()} ✦", rect.centerX(), cardY + cardH * 0.40f, textPaint)

            // Title
            cardTitlePaint.textSize = cardH * 0.065f
            canvas.drawText(card.title, rect.centerX(), cardY + cardH * 0.49f, cardTitlePaint)

            // Description
            descPaint.textSize = cardH * 0.046f
            drawMultilineText(canvas, card.description, rect.centerX(), cardY + cardH * 0.60f, cardW - 36f, descPaint)

            // Equip Button at Card Bottom
            val btnW = cardW - 40f
            val btnH = cardH * 0.12f
            val btnX = rect.centerX() - btnW * 0.5f
            val btnY = cardY + cardH * 0.82f
            val btnRect = RectF(btnX, btnY, btnX + btnW, btnY + btnH)

            fillPaint.color = Color.parseColor("#B8860B")
            canvas.drawRoundRect(btnRect, 10f, 10f, fillPaint)

            textPaint.textSize = btnH * 0.46f
            textPaint.color = Color.parseColor("#14181A")
            textPaint.isFakeBoldText = true
            canvas.drawText("EQUIP RELIC", rect.centerX(), btnY + btnH * 0.65f, textPaint)
            textPaint.isFakeBoldText = false
        }
    }

    // =========================================================================
    // PAUSE MODAL
    // =========================================================================

    private fun renderPauseModal(canvas: Canvas, w: Float, h: Float) {
        fillPaint.color = Color.parseColor("#CC080B09")
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        titlePaint.textSize = h * 0.09f
        titlePaint.color = Color.WHITE
        canvas.drawText("GAME PAUSED", w * 0.5f, h * 0.32f, titlePaint)

        val btnW = w * 0.26f
        val btnH = h * 0.12f
        val btnX = (w - btnW) * 0.5f

        // Resume Button
        val rY = h * 0.44f
        resumeBtnRect.set(btnX, rY, btnX + btnW, rY + btnH)
        fillPaint.color = Color.parseColor("#2E7D32")
        canvas.drawRoundRect(resumeBtnRect, 16f, 16f, fillPaint)
        titlePaint.textSize = btnH * 0.44f
        canvas.drawText("RESUME", w * 0.5f, rY + btnH * 0.64f, titlePaint)

        // Restart Button
        val resY = h * 0.59f
        restartBtnRect.set(btnX, resY, btnX + btnW, resY + btnH)
        fillPaint.color = Color.parseColor("#C62828")
        canvas.drawRoundRect(restartBtnRect, 16f, 16f, fillPaint)
        canvas.drawText("RESTART", w * 0.5f, resY + btnH * 0.64f, titlePaint)

        // Main Menu Button
        val mY = h * 0.74f
        mainMenuBtnRect.set(btnX, mY, btnX + btnW, mY + btnH)
        fillPaint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(mainMenuBtnRect, 16f, 16f, fillPaint)
        canvas.drawText("MAIN MENU", w * 0.5f, mY + btnH * 0.64f, titlePaint)
    }

    // =========================================================================
    // GAME OVER MODAL
    // =========================================================================

    private fun renderGameOver(canvas: Canvas, w: Float, h: Float) {
        fillPaint.color = Color.parseColor("#E60C0505")
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        titlePaint.textSize = h * 0.10f
        titlePaint.color = Color.parseColor("#D32F2F")
        canvas.drawText("FORTRESS DESTROYED", w * 0.5f, h * 0.26f, titlePaint)

        textPaint.textSize = h * 0.045f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        val timeSec = gameManager.runStats.survivalTimeSeconds.toInt()
        canvas.drawText("Survived: ${timeSec / 60}m ${timeSec % 60}s  |  Reached Level ${gameWorld.castle.level}", w * 0.5f, h * 0.36f, textPaint)
        canvas.drawText("Gold Looted: ${gameWorld.castle.getResource(ResourceType.GOLD)}", w * 0.5f, h * 0.43f, textPaint)

        // Retry Button
        val btnW = w * 0.28f
        val btnH = h * 0.13f
        val btnX = (w - btnW) * 0.5f
        val btnY = h * 0.54f
        restartBtnRect.set(btnX, btnY, btnX + btnW, btnY + btnH)

        fillPaint.color = Color.parseColor("#B71C1C")
        canvas.drawRoundRect(restartBtnRect, 18f, 18f, fillPaint)
        strokePaint.color = Color.parseColor("#FFD700")
        strokePaint.strokeWidth = 4f
        canvas.drawRoundRect(restartBtnRect, 18f, 18f, strokePaint)

        titlePaint.textSize = btnH * 0.44f
        titlePaint.color = Color.WHITE
        canvas.drawText("BATTLE AGAIN", w * 0.5f, btnY + btnH * 0.64f, titlePaint)

        // Main Menu
        val mY = h * 0.71f
        mainMenuBtnRect.set(btnX, mY, btnX + btnW, mY + btnH)
        fillPaint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(mainMenuBtnRect, 16f, 16f, fillPaint)
        canvas.drawText("MAIN MENU", w * 0.5f, mY + btnH * 0.64f, titlePaint)
    }

    private fun renderUpgradeMenu(canvas: Canvas, w: Float, h: Float) {
        renderMainMenu(canvas, w, h)
    }

    // =========================================================================
    // TOUCH EVENT HANDLING
    // =========================================================================

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val actionIndex = event.actionIndex
        val pointerId = event.getPointerId(actionIndex)
        val touchX = event.getX(actionIndex)
        val touchY = event.getY(actionIndex)

        when (gameManager.currentState) {
            GameState.MAIN_MENU -> {
                if (action == MotionEvent.ACTION_UP && playBtnRect.contains(touchX, touchY)) {
                    gameWorld.reset()
                    gameManager.startGame()
                    return true
                }
            }
            GameState.PLAYING, GameState.BOSS_FIGHT -> {
                // Pause button tap
                if (action == MotionEvent.ACTION_UP && pauseBtnRect.contains(touchX, touchY)) {
                    gameManager.pauseGame()
                    return true
                }

                // Virtual Joystick forwarding
                when (action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        if (joystick.onTouchDown(pointerId, touchX, touchY, width.toFloat(), height.toFloat())) {
                            return true
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        for (i in 0 until event.pointerCount) {
                            joystick.onTouchMove(event.getPointerId(i), event.getX(i), event.getY(i))
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        joystick.onTouchUp(pointerId)
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        joystick.reset()
                        return true
                    }
                }
            }
            GameState.PAUSED -> {
                if (action == MotionEvent.ACTION_UP) {
                    when {
                        resumeBtnRect.contains(touchX, touchY) -> {
                            gameManager.resumeGame()
                            return true
                        }
                        restartBtnRect.contains(touchX, touchY) -> {
                            gameWorld.reset()
                            gameManager.startGame()
                            return true
                        }
                        mainMenuBtnRect.contains(touchX, touchY) -> {
                            gameManager.returnToMainMenu()
                            return true
                        }
                    }
                }
            }
            GameState.LEVEL_UP -> {
                if (action == MotionEvent.ACTION_UP) {
                    for (i in cardRects.indices) {
                        if (cardRects[i].contains(touchX, touchY) && i < currentUpgradeChoices.size) {
                            val chosenUpgrade = currentUpgradeChoices[i]
                            chosenUpgrade.apply(gameWorld.castle)
                            gameManager.completeLevelUp()
                            return true
                        }
                    }
                }
            }
            GameState.GAME_OVER -> {
                if (action == MotionEvent.ACTION_UP) {
                    when {
                        restartBtnRect.contains(touchX, touchY) -> {
                            gameWorld.reset()
                            gameManager.startGame()
                            return true
                        }
                        mainMenuBtnRect.contains(touchX, touchY) -> {
                            gameManager.returnToMainMenu()
                            return true
                        }
                    }
                }
            }
            GameState.UPGRADE_MENU -> {
                if (action == MotionEvent.ACTION_UP) {
                    gameManager.returnToMainMenu()
                    return true
                }
            }
        }

        return true
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint
    ) {
        val words = text.split(" ")
        var currentLine = ""
        var lineY = y

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, x, lineY, paint)
                currentLine = word
                lineY += paint.textSize * 1.3f
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, lineY, paint)
        }
    }
}
