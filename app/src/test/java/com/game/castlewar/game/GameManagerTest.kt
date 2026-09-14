package com.game.castlewar.game

import com.game.castlewar.core.GameSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameManagerTest {

    private lateinit var gameManager: GameManager

    @Before
    fun setUp() {
        gameManager = GameManager()
    }

    @Test
    fun `initial state is MAIN_MENU`() {
        assertEquals(GameState.MAIN_MENU, gameManager.currentState)
        assertFalse(gameManager.currentState.isSimulationActive)
        assertFalse(gameManager.currentState.isRunActive)
    }

    @Test
    fun `startGame transitions to PLAYING and activates simulation`() {
        gameManager.startGame()
        assertEquals(GameState.PLAYING, gameManager.currentState)
        assertTrue(gameManager.currentState.isSimulationActive)
        assertTrue(gameManager.currentState.isRunActive)
    }

    @Test
    fun `pause and resume transitions work correctly`() {
        gameManager.startGame()
        assertEquals(GameState.PLAYING, gameManager.currentState)

        gameManager.pauseGame()
        assertEquals(GameState.PAUSED, gameManager.currentState)
        assertFalse(gameManager.currentState.isSimulationActive)
        assertTrue(gameManager.currentState.isRunActive)

        gameManager.resumeGame()
        assertEquals(GameState.PLAYING, gameManager.currentState)
        assertTrue(gameManager.currentState.isSimulationActive)
    }

    @Test
    fun `level up pauses simulation and returns to active combat on completion`() {
        gameManager.startGame()
        gameManager.triggerLevelUp()

        assertEquals(GameState.LEVEL_UP, gameManager.currentState)
        assertFalse(gameManager.currentState.isSimulationActive)
        assertTrue(gameManager.currentState.isModalOverlay)

        gameManager.completeLevelUp()
        assertEquals(GameState.PLAYING, gameManager.currentState)
        assertTrue(gameManager.currentState.isSimulationActive)
    }

    @Test
    fun `boss fight transitions and game over flow`() {
        gameManager.startGame()
        gameManager.startBossFight()

        assertEquals(GameState.BOSS_FIGHT, gameManager.currentState)
        assertTrue(gameManager.currentState.isSimulationActive)

        gameManager.triggerGameOver()
        assertEquals(GameState.GAME_OVER, gameManager.currentState)
        assertFalse(gameManager.currentState.isSimulationActive)
        assertFalse(gameManager.currentState.isRunActive)
    }

    @Test
    fun `registered subsystem receives state change and update ticks`() {
        var stateChangeNotified = false
        var updateTickCount = 0

        val testSystem = object : GameSystem {
            override fun onStateChanged(oldState: GameState, newState: GameState) {
                stateChangeNotified = true
            }

            override fun onUpdate(deltaTime: Float) {
                updateTickCount++
            }
        }

        gameManager.registerSystem(testSystem)
        gameManager.startGame()

        assertTrue(stateChangeNotified)

        // While PLAYING, update should tick subsystem
        gameManager.update(0.016f)
        assertEquals(1, updateTickCount)

        // When PAUSED, update should NOT tick subsystem
        gameManager.pauseGame()
        gameManager.update(0.016f)
        assertEquals(1, updateTickCount)
    }

    @Test
    fun `state listener receives transition callbacks`() {
        var observedOldState: GameState? = null
        var observedNewState: GameState? = null

        gameManager.addStateListener { old, new ->
            observedOldState = old
            observedNewState = new
        }

        gameManager.changeState(GameState.UPGRADE_MENU)
        assertEquals(GameState.MAIN_MENU, observedOldState)
        assertEquals(GameState.UPGRADE_MENU, observedNewState)
    }
}
