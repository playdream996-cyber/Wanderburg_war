package com.game.castlewar.game

import android.util.Log
import com.game.castlewar.core.GameSystem

/**
 * Coordinates game states, transitions, run statistics, and subsystems.
 * Maintains independence between game logic and the OpenGL rendering pipeline.
 *
 * Initialized in [GameState.MAIN_MENU].
 */
class GameManager {

    companion object {
        private const val TAG = "GameManager"
    }

    /**
     * Holds run-level statistics and progression metrics.
     */
    data class RunStats(
        var survivalTimeSeconds: Float = 0f,
        var enemiesDefeated: Int = 0,
        var buildingsDestroyed: Int = 0,
        var goldCollected: Int = 0,
        var currentLevel: Int = 1
    ) {
        fun reset() {
            survivalTimeSeconds = 0f
            enemiesDefeated = 0
            buildingsDestroyed = 0
            goldCollected = 0
            currentLevel = 1
        }
    }

    /** Current game state, initialized in MAIN_MENU */
    @Volatile
    var currentState: GameState = GameState.MAIN_MENU
        private set

    /** Previous active state (used when resuming from PAUSED or LEVEL_UP) */
    @Volatile
    var previousState: GameState = GameState.MAIN_MENU
        private set

    /** Active session statistics */
    val runStats = RunStats()

    private val stateListeners = mutableListOf<(oldState: GameState, newState: GameState) -> Unit>()
    private val systems = mutableListOf<GameSystem>()

    // =========================================================================
    // Subsystem Coordination
    // =========================================================================

    fun registerSystem(system: GameSystem) {
        synchronized(systems) {
            if (!systems.contains(system)) {
                systems.add(system)
            }
        }
    }

    fun unregisterSystem(system: GameSystem) {
        synchronized(systems) {
            systems.remove(system)
        }
    }

    // =========================================================================
    // State Listeners
    // =========================================================================

    fun addStateListener(listener: (oldState: GameState, newState: GameState) -> Unit) {
        synchronized(stateListeners) {
            stateListeners.add(listener)
        }
    }

    fun removeStateListener(listener: (oldState: GameState, newState: GameState) -> Unit) {
        synchronized(stateListeners) {
            stateListeners.remove(listener)
        }
    }

    // =========================================================================
    // State Transitions
    // =========================================================================

    /**
     * Core transition method. Validates and notifies listeners & subsystems.
     */
    fun changeState(newState: GameState) {
        val oldState = currentState
        if (oldState == newState) return

        Log.i(TAG, "Game state transition: $oldState -> $newState")
        previousState = oldState
        currentState = newState

        // Notify state listeners
        val listenersCopy = synchronized(stateListeners) { stateListeners.toList() }
        for (listener in listenersCopy) {
            listener(oldState, newState)
        }

        // Notify coordinated subsystems
        val systemsCopy = synchronized(systems) { systems.toList() }
        for (system in systemsCopy) {
            system.onStateChanged(oldState, newState)
        }
    }

    /**
     * Starts a new run or resumes from main menu into PLAYING.
     */
    fun startGame() {
        runStats.reset()
        val systemsCopy = synchronized(systems) { systems.toList() }
        for (system in systemsCopy) {
            system.onReset()
        }
        changeState(GameState.PLAYING)
    }

    /**
     * Pauses the game if a run is currently active.
     */
    fun pauseGame() {
        if (currentState.isSimulationActive) {
            changeState(GameState.PAUSED)
        }
    }

    /**
     * Resumes the game from PAUSED back to the previous gameplay state.
     */
    fun resumeGame() {
        if (currentState == GameState.PAUSED) {
            val targetState = if (previousState == GameState.BOSS_FIGHT) GameState.BOSS_FIGHT else GameState.PLAYING
            changeState(targetState)
        }
    }

    /**
     * Triggers the level-up state, pausing the simulation to present upgrade choices.
     */
    fun triggerLevelUp() {
        if (currentState.isSimulationActive) {
            changeState(GameState.LEVEL_UP)
        }
    }

    /**
     * Concludes the level-up sequence and returns to active combat.
     */
    fun completeLevelUp() {
        if (currentState == GameState.LEVEL_UP) {
            val targetState = if (previousState == GameState.BOSS_FIGHT) GameState.BOSS_FIGHT else GameState.PLAYING
            changeState(targetState)
        }
    }

    /**
     * Transitions into the intense boss encounter state.
     */
    fun startBossFight() {
        if (currentState == GameState.PLAYING) {
            changeState(GameState.BOSS_FIGHT)
        }
    }

    /**
     * Triggered when the fortress core is destroyed.
     */
    fun triggerGameOver() {
        changeState(GameState.GAME_OVER)
    }

    /**
     * Opens the meta-progression upgrades menu.
     */
    fun openUpgradeMenu() {
        changeState(GameState.UPGRADE_MENU)
    }

    /**
     * Returns to the main menu screen.
     */
    fun returnToMainMenu() {
        changeState(GameState.MAIN_MENU)
    }

    /**
     * Restarts a run from game over or pause.
     */
    fun restartGame() {
        startGame()
    }

    // =========================================================================
    // Fixed Timestep Update
    // =========================================================================

    /**
     * Fixed timestep logic update tick invoked by the game loop.
     * @param deltaTime Fixed seconds elapsed (e.g. 0.0166667f for 60Hz)
     */
    fun update(deltaTime: Float) {
        if (currentState.isSimulationActive) {
            runStats.survivalTimeSeconds += deltaTime

            // Step all coordinated subsystems
            val systemsCopy = synchronized(systems) { systems.toList() }
            for (system in systemsCopy) {
                system.onUpdate(deltaTime)
            }
        }
    }
}
