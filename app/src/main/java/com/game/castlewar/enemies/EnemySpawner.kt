package com.game.castlewar.enemies

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class SpawnFormation {
    SCATTERED,
    SHIELD_WALL,
    ARCHER_BATTERY,
    SAPPER_RUSH,
    ROYAL_VANGUARD
}

/**
 * Manages wave scaling, tactical enemy squads, escalating difficulty, and boss encounters around the fortress.
 */
class EnemySpawner {

    var spawnTimer: Float = 0f
    var baseSpawnInterval: Float = 2.4f
    var timeElapsed: Float = 0f
    var bossSpawned: Boolean = false

    var nextSurgeTimer: Float = 35f
    var currentWave: Int = 1

    fun reset() {
        spawnTimer = 0f
        timeElapsed = 0f
        bossSpawned = false
        nextSurgeTimer = 35f
        currentWave = 1
    }

    fun update(
        deltaTime: Float,
        castleX: Float,
        castleY: Float,
        currentEnemyCount: Int,
        maxEnemies: Int = 90,
        onSpawnEnemy: (Enemy) -> Unit,
        onBossTriggered: () -> Unit
    ) {
        timeElapsed += deltaTime
        spawnTimer -= deltaTime
        nextSurgeTimer -= deltaTime

        currentWave = when {
            timeElapsed > 120f -> 4
            timeElapsed > 70f -> 3
            timeElapsed > 30f -> 2
            else -> 1
        }

        // Dynamically accelerate spawns as wave advances
        val currentInterval = (baseSpawnInterval - (timeElapsed / 220f) * 1.5f).coerceAtLeast(0.65f)

        // 1. Regular Tactical Squad Spawning
        if (spawnTimer <= 0f && currentEnemyCount < maxEnemies) {
            spawnTimer = currentInterval

            val formation = when (currentWave) {
                1 -> if (Random.nextFloat() < 0.4f) SpawnFormation.ARCHER_BATTERY else SpawnFormation.SCATTERED
                2 -> if (Random.nextFloat() < 0.5f) SpawnFormation.SHIELD_WALL else SpawnFormation.ARCHER_BATTERY
                3 -> {
                    val r = Random.nextFloat()
                    if (r < 0.35f) SpawnFormation.SAPPER_RUSH else if (r < 0.70f) SpawnFormation.SHIELD_WALL else SpawnFormation.ARCHER_BATTERY
                }
                else -> SpawnFormation.ROYAL_VANGUARD
            }

            spawnSquad(castleX, castleY, formation, onSpawnEnemy)
        }

        // 2. Periodic Reinforcement Surge (every 35s): Flanking squad from opposite sides!
        if (nextSurgeTimer <= 0f && currentEnemyCount < maxEnemies - 6) {
            nextSurgeTimer = 40f
            val baseAngle = Random.nextFloat() * 6.28318f
            spawnSquadAtAngle(castleX, castleY, baseAngle, SpawnFormation.SHIELD_WALL, onSpawnEnemy)
            spawnSquadAtAngle(castleX, castleY, baseAngle + 3.14159f, SpawnFormation.ARCHER_BATTERY, onSpawnEnemy)
        }

        // 3. Boss Encounter at Wave 4 milestone (120s)
        if (timeElapsed >= 120f && !bossSpawned) {
            bossSpawned = true
            val bossAngle = Random.nextFloat() * 6.28318f
            val bossX = castleX + cos(bossAngle) * 750f
            val bossY = castleY + sin(bossAngle) * 750f
            val boss = EnemyBoss(bossX, bossY)
            onSpawnEnemy(boss)

            // Boss Royal Guard escorts
            for (i in 0 until 3) {
                val ox = (Random.nextFloat() - 0.5f) * 80f
                val oy = (Random.nextFloat() - 0.5f) * 80f
                onSpawnEnemy(EnemyKnight(bossX + ox, bossY + oy))
                onSpawnEnemy(EnemyArcher(bossX - ox, bossY - oy))
            }

            onBossTriggered()
        }
    }

    private fun spawnSquad(
        castleX: Float,
        castleY: Float,
        formation: SpawnFormation,
        onSpawnEnemy: (Enemy) -> Unit
    ) {
        val spawnAngle = Random.nextFloat() * 6.28318f
        spawnSquadAtAngle(castleX, castleY, spawnAngle, formation, onSpawnEnemy)
    }

    private fun spawnSquadAtAngle(
        castleX: Float,
        castleY: Float,
        angle: Float,
        formation: SpawnFormation,
        onSpawnEnemy: (Enemy) -> Unit
    ) {
        val distance = Random.nextFloat() * 150f + 650f
        val centerX = castleX + cos(angle) * distance
        val centerY = castleY + sin(angle) * distance

        when (formation) {
            SpawnFormation.SHIELD_WALL -> {
                // 2 Knights in front, 3 Melee swordsmen behind
                onSpawnEnemy(EnemyKnight(centerX - 25f, centerY))
                onSpawnEnemy(EnemyKnight(centerX + 25f, centerY))
                onSpawnEnemy(EnemyMelee(centerX - 35f, centerY + 30f))
                onSpawnEnemy(EnemyMelee(centerX, centerY + 30f))
                onSpawnEnemy(EnemyMelee(centerX + 35f, centerY + 30f))
            }
            SpawnFormation.ARCHER_BATTERY -> {
                // 3 Archers spaced across firing line + 1 protective Knight
                onSpawnEnemy(EnemyKnight(centerX, centerY - 20f))
                onSpawnEnemy(EnemyArcher(centerX - 40f, centerY + 15f))
                onSpawnEnemy(EnemyArcher(centerX, centerY + 20f))
                onSpawnEnemy(EnemyArcher(centerX + 40f, centerY + 15f))
            }
            SpawnFormation.SAPPER_RUSH -> {
                // 2-3 Bombers charging with 1 Melee
                onSpawnEnemy(EnemyBomber(centerX - 25f, centerY))
                onSpawnEnemy(EnemyBomber(centerX + 25f, centerY))
                onSpawnEnemy(EnemyMelee(centerX, centerY + 25f))
            }
            SpawnFormation.ROYAL_VANGUARD -> {
                // Elite mixed battle squad
                onSpawnEnemy(EnemyKnight(centerX - 30f, centerY))
                onSpawnEnemy(EnemyKnight(centerX + 30f, centerY))
                onSpawnEnemy(EnemyArcher(centerX - 20f, centerY + 35f))
                onSpawnEnemy(EnemyArcher(centerX + 20f, centerY + 35f))
                onSpawnEnemy(EnemyBomber(centerX, centerY - 25f))
            }
            SpawnFormation.SCATTERED -> {
                val count = Random.nextInt(2, 4)
                for (i in 0 until count) {
                    val ox = (Random.nextFloat() - 0.5f) * 60f
                    val oy = (Random.nextFloat() - 0.5f) * 60f
                    onSpawnEnemy(EnemyMelee(centerX + ox, centerY + oy))
                }
            }
        }
    }

    fun spawnGarrisonReinforcements(
        barracksX: Float,
        barracksY: Float,
        onSpawnEnemy: (Enemy) -> Unit
    ) {
        // Garrison guards rushing out to defend attacked village
        onSpawnEnemy(EnemyKnight(barracksX - 20f, barracksY + 20f))
        onSpawnEnemy(EnemyMelee(barracksX + 20f, barracksY + 20f))
        onSpawnEnemy(EnemyArcher(barracksX, barracksY - 25f))
    }
}
