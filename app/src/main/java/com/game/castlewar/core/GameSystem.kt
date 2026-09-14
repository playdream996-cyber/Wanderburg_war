package com.game.castlewar.core

import com.game.castlewar.game.GameState

/**
 * Interface for modular game subsystems coordinated by GameManager.
 */
interface GameSystem {
    /**
     * Called when the GameManager transitions from [oldState] to [newState].
     */
    fun onStateChanged(oldState: GameState, newState: GameState) {}

    /**
     * Fixed-timestep update tick. Called while the simulation is active.
     */
    fun onUpdate(deltaTime: Float) {}

    /**
     * Resets the subsystem state for a fresh game run.
     */
    fun onReset() {}
}
