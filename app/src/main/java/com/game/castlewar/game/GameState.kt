package com.game.castlewar.game

/**
 * High-level discrete states for the Castle War roguelike engine.
 */
enum class GameState {
    /** Main menu screen with Play, Castle, Weapons, Upgrades, Settings */
    MAIN_MENU,

    /** Active fortress combat and exploration */
    PLAYING,

    /** Paused gameplay with options to resume or exit */
    PAUSED,

    /** Level-up modal presenting three random roguelike upgrade cards */
    LEVEL_UP,

    /** Intense fortress-vs-fortress boss encounter */
    BOSS_FIGHT,

    /** Castle destroyed summary screen with restart and meta-upgrade options */
    GAME_OVER,

    /** Meta-progression menu for permanent fortress upgrades */
    UPGRADE_MENU;

    /**
     * True when physics and combat simulation ticks should proceed.
     */
    val isSimulationActive: Boolean
        get() = this == PLAYING || this == BOSS_FIGHT

    /**
     * True when a run is currently in progress (even if paused or in level-up).
     */
    val isRunActive: Boolean
        get() = this == PLAYING || this == PAUSED || this == LEVEL_UP || this == BOSS_FIGHT

    /**
     * True if the current state presents an overlay menu over gameplay.
     */
    val isModalOverlay: Boolean
        get() = this == PAUSED || this == LEVEL_UP || this == UPGRADE_MENU
}

