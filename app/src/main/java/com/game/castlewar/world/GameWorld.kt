package com.game.castlewar.world

import com.game.castlewar.buildings.Building
import com.game.castlewar.buildings.BuildingType
import com.game.castlewar.effects.ParticleManager
import com.game.castlewar.enemies.Enemy
import com.game.castlewar.enemies.EnemyBoss
import com.game.castlewar.enemies.EnemySpawner
import com.game.castlewar.entities.MovingCastle
import com.game.castlewar.render.GameCamera
import com.game.castlewar.resources.ResourcePickup
import com.game.castlewar.resources.ResourceType
import com.game.castlewar.utils.CollisionUtils
import com.game.castlewar.weapons.Projectile
import com.game.castlewar.weapons.ProjectileManager
import com.game.castlewar.weapons.ProjectileType
import kotlin.random.Random

/**
 * The living medieval battlefield world in Castle War.
 * Contains terrain elements, destructible settlements, enemy armies, and weapon interactions.
 */
class GameWorld {

    val worldWidth = 4000f
    val worldHeight = 4000f

    val castle = MovingCastle(0f, 0f)
    val enemies = mutableListOf<Enemy>()
    val buildings = mutableListOf<Building>()
    val pickups = Array(200) { ResourcePickup() }
    val projectileManager = ProjectileManager(180)
    val particleManager = ParticleManager(600)
    val spawner = EnemySpawner()

    var activeBoss: EnemyBoss? = null
    var onLevelUpTriggered: (() -> Unit)? = null
    var onGameOverTriggered: (() -> Unit)? = null
    var onBossSpawned: (() -> Unit)? = null

    init {
        generateWorld()
    }

    fun reset() {
        castle.reset(0f, 0f)
        enemies.clear()
        projectileManager.clear()
        particleManager.clear()
        for (p in pickups) p.isActive = false
        spawner.reset()
        activeBoss = null
        generateWorld()
    }

    /**
     * Seeds villages, watchtowers, palisades, and encampments across the terrain.
     */
    fun generateWorld() {
        buildings.clear()
        var buildingId = 1

        // Spawn multiple encampments, landmarks, and fortified outposts
        // 1. Grand River Crossing Landmark (0f, -650f): Massive stone arch bridge spanning the river
        buildings.add(Building(buildingId++, 0f, -650f, BuildingType.STONE_BRIDGE))
        buildings.add(Building(buildingId++, -65f, -650f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 65f, -650f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, -130f, -650f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, 130f, -650f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, 0f, -750f, BuildingType.BARRACKS))

        // 2. Riverbend Village (-350f, -250f): Thriving medieval village cluster
        buildings.add(Building(buildingId++, -350f, -250f, BuildingType.VILLAGE_WELL))
        buildings.add(Building(buildingId++, -390f, -220f, BuildingType.TOWNHOUSE))
        buildings.add(Building(buildingId++, -310f, -270f, BuildingType.VILLAGE_HOUSE))
        buildings.add(Building(buildingId++, -360f, -310f, BuildingType.MARKET_STALL))
        buildings.add(Building(buildingId++, -280f, -200f, BuildingType.WINDMILL)) // Windmill on the gentle scenic hill
        buildings.add(Building(buildingId++, -330f, -180f, BuildingType.BLACKSMITH)) // Village forge with smoking chimney
        buildings.add(Building(buildingId++, -430f, -260f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, -470f, -280f, BuildingType.CROP_FIELD)) // Wheat crop field
        buildings.add(Building(buildingId++, -430f, -340f, BuildingType.FARM_SHED)) // Rustic tool shed

        // 3. The Great Elder Oak Landmark (-450f, 50f): Ancient landmark tree towering over crossroads
        buildings.add(Building(buildingId++, -450f, 50f, BuildingType.ELDER_OAK))

        // 4. Wheatfield Farmstead (-450f, 400f): Rustic agricultural settlement
        buildings.add(Building(buildingId++, -450f, 400f, BuildingType.FARM_BARN))
        buildings.add(Building(buildingId++, -510f, 440f, BuildingType.WINDMILL))
        buildings.add(Building(buildingId++, -390f, 420f, BuildingType.VILLAGE_HOUSE))
        buildings.add(Building(buildingId++, -450f, 340f, BuildingType.MARKET_STALL))
        buildings.add(Building(buildingId++, -520f, 370f, BuildingType.CROP_FIELD))
        buildings.add(Building(buildingId++, -380f, 360f, BuildingType.CROP_FIELD))
        buildings.add(Building(buildingId++, -490f, 330f, BuildingType.FARM_SHED))

        // 5. Eastern Garrison & Staging Camp (400f, -300f): Heavy military outpost
        buildings.add(Building(buildingId++, 400f, -300f, BuildingType.BARRACKS))
        buildings.add(Building(buildingId++, 470f, -270f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 340f, -330f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, 430f, -360f, BuildingType.SUPPLY_TENT))
        buildings.add(Building(buildingId++, 360f, -240f, BuildingType.STONE_RUIN))
        buildings.add(Building(buildingId++, 440f, -220f, BuildingType.BLACKSMITH))

        // 6. Mid-Highway Fortified Checkpoint (50f, 350f)
        buildings.add(Building(buildingId++, -45f, 350f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 95f, 350f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 25f, 380f, BuildingType.BARRICADE))
        buildings.add(Building(buildingId++, 80f, 290f, BuildingType.BURNED_CART))

        // 7. Northern Border Fort (500f, 450f): Heavy garrison overlooking highway
        buildings.add(Building(buildingId++, 500f, 450f, BuildingType.BARRACKS))
        buildings.add(Building(buildingId++, 560f, 480f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 440f, 420f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, 520f, 380f, BuildingType.SUPPLY_TENT))
        buildings.add(Building(buildingId++, 450f, 490f, BuildingType.TOWNHOUSE))
        buildings.add(Building(buildingId++, 480f, 530f, BuildingType.BALLISTA))

        // 8. Forward Siege Engine Camps (Late Area / Z > 600f):
        // Western Siege Battery
        buildings.add(Building(buildingId++, -350f, 850f, BuildingType.CATAPULT))
        buildings.add(Building(buildingId++, -400f, 820f, BuildingType.SUPPLY_TENT))
        buildings.add(Building(buildingId++, -310f, 880f, BuildingType.BARRICADE))
        buildings.add(Building(buildingId++, -280f, 820f, BuildingType.BALLISTA))

        // Eastern Siege Battery
        buildings.add(Building(buildingId++, 380f, 650f, BuildingType.CATAPULT))
        buildings.add(Building(buildingId++, 430f, 620f, BuildingType.SUPPLY_TENT))
        buildings.add(Building(buildingId++, 330f, 670f, BuildingType.BARRICADE))
        buildings.add(Building(buildingId++, 360f, 720f, BuildingType.BURNED_CART))

        // 9. Deep Forest Outpost (-800f, -50f): Secluded woodland hideout
        buildings.add(Building(buildingId++, -800f, -50f, BuildingType.BARRACKS))
        buildings.add(Building(buildingId++, -740f, -20f, BuildingType.VILLAGE_HOUSE))
        buildings.add(Building(buildingId++, -840f, -90f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, -780f, -110f, BuildingType.SUPPLY_TENT))

        // 10. Ancient Ruins of Eldermoor (850f, -100f): Crumbled stone monument site
        buildings.add(Building(buildingId++, 850f, -100f, BuildingType.STONE_RUIN))
        buildings.add(Building(buildingId++, 890f, -60f, BuildingType.STONE_RUIN))
        buildings.add(Building(buildingId++, 810f, -130f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 860f, -160f, BuildingType.SUPPLY_TENT))

        // 11. Highland Redoubt & Fortified Village (-200f, 750f)
        buildings.add(Building(buildingId++, -200f, 750f, BuildingType.TOWNHOUSE))
        buildings.add(Building(buildingId++, -260f, 780f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, -140f, 720f, BuildingType.VILLAGE_HOUSE))
        buildings.add(Building(buildingId++, -200f, 680f, BuildingType.FARM_BARN))
        buildings.add(Building(buildingId++, -270f, 710f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, -160f, 660f, BuildingType.BURNED_CART))

        // 12. Boss Citadel Defensive Outer Perimeters (0f, 1150f to 1350f)
        buildings.add(Building(buildingId++, -90f, 1150f, BuildingType.BARRICADE))
        buildings.add(Building(buildingId++, 90f, 1150f, BuildingType.BARRICADE))
        buildings.add(Building(buildingId++, -120f, 1220f, BuildingType.CATAPULT))
        buildings.add(Building(buildingId++, 120f, 1220f, BuildingType.CATAPULT))
        buildings.add(Building(buildingId++, -70f, 1280f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, 70f, 1280f, BuildingType.WATCHTOWER))
        buildings.add(Building(buildingId++, -160f, 1280f, BuildingType.WALL_SEGMENT))
        buildings.add(Building(buildingId++, 160f, 1280f, BuildingType.WALL_SEGMENT))

        // Scatter cottages, barns, and supply tents across countryside
        val rand = Random(42)
        for (i in 0 until 16) {
            val rx = (rand.nextFloat() - 0.5f) * 2800f
            val ry = (rand.nextFloat() - 0.5f) * 2800f
            if (CollisionUtils.distance(rx, ry, 0f, 0f) > 300f) {
                val type = when (i % 5) {
                    0 -> BuildingType.VILLAGE_HOUSE
                    1 -> BuildingType.FARM_BARN
                    2 -> BuildingType.MARKET_STALL
                    3 -> BuildingType.BLACKSMITH
                    else -> BuildingType.SUPPLY_TENT
                }
                buildings.add(Building(buildingId++, rx, ry, type))
            }
        }
    }

    fun spawnPickup(x: Float, y: Float, type: ResourceType, amount: Int) {
        for (p in pickups) {
            if (!p.isActive) {
                p.reset(x, y, type, amount)
                return
            }
        }
    }

    /**
     * Fixed-timestep update of simulation logic.
     */
    fun update(deltaTime: Float, inputX: Float, inputY: Float, camera: GameCamera) {
        // 1. Fortress steering & physics
        castle.steer(inputX, inputY, deltaTime)

        // Spawn track dust when moving
        if (castle.velocityX != 0f || castle.velocityY != 0f) {
            particleManager.spawnTreadDust(castle.positionX, castle.positionY)
        }

        // Camera smoothly tracks fortress
        camera.follow(castle.positionX, castle.positionY)

        // 2. Automated weapons targeting and firing
        for (weapon in castle.weapons) {
            weapon.update(
                deltaTime = deltaTime,
                castleWorldX = castle.positionX,
                castleWorldY = castle.positionY,
                enemies = enemies,
                projectileManager = projectileManager,
                particleManager = particleManager,
                camera = camera
            )
        }

        // 3. Projectiles movement and collision
        projectileManager.update(deltaTime, particleManager, camera)
        for (projectile in projectileManager.pool) {
            if (!projectile.isActive) continue

            // Check enemy projectile hitting Player Moving Castle
            if (projectile.isEnemyProjectile) {
                if (CollisionUtils.circleVsCircle(
                        projectile.x, projectile.y, projectile.radius,
                        castle.positionX, castle.positionY, castle.collisionRadius
                    )
                ) {
                    val destroyed = castle.takeDamage(projectile.damage)
                    camera.shake(intensity = if (projectile.type == ProjectileType.FIREBALL) 14f else 5f, duration = 0.2f)
                    particleManager.spawnHitSparks(projectile.x, projectile.y, count = 5)

                    if (projectile.type == ProjectileType.FIREBALL) {
                        particleManager.spawnExplosion(projectile.x, projectile.y, radius = projectile.explosionRadius)
                        camera.triggerCannonImpactShake()
                    }

                    projectile.isActive = false
                    if (destroyed) {
                        particleManager.spawnExplosion(castle.positionX, castle.positionY, radius = 80f)
                        camera.shake(intensity = 25f, duration = 0.8f)
                        onGameOverTriggered?.invoke()
                    }
                }
                continue
            }

            // Check player projectile collision vs enemies
            for (enemy in enemies) {
                if (!enemy.isAlive) continue
                if (projectile.hitEntities.contains(enemy.hashCode())) continue

                if (CollisionUtils.circleVsCircle(
                        projectile.x, projectile.y, projectile.radius,
                        enemy.positionX, enemy.positionY, enemy.radius
                    )
                ) {
                    projectile.hitEntities.add(enemy.hashCode())
                    val killed = enemy.takeDamage(projectile.damage)
                    particleManager.spawnHitSparks(enemy.positionX, enemy.positionY)

                    // Area of effect explosion for cannonballs
                    if (projectile.explosionRadius > 0f) {
                        for (other in enemies) {
                            if (other !== enemy && other.isAlive) {
                                val distSq = CollisionUtils.distanceSquared(
                                    projectile.x, projectile.y,
                                    other.positionX, other.positionY
                                )
                                if (distSq <= projectile.explosionRadius * projectile.explosionRadius) {
                                    val otherKilled = other.takeDamage(projectile.damage * 0.75f)
                                    if (otherKilled) handleEnemyDefeated(other)
                                }
                            }
                        }
                    }

                    if (killed) {
                        handleEnemyDefeated(enemy)
                    }

                    if (projectile.hitEntities.size >= projectile.pierceCount) {
                        projectile.isActive = false
                        break
                    }
                }
            }

            // Check collision vs buildings
            if (projectile.isActive) {
                for (b in buildings) {
                    if (b.isDestroyed) continue
                    if (CollisionUtils.circleVsRectangle(
                            projectile.x, projectile.y, projectile.radius,
                            b.positionX - b.type.width * 0.5f,
                            b.positionY - b.type.height * 0.5f,
                            b.type.width, b.type.height
                        )
                    ) {
                        b.takeDamage(projectile.damage, particleManager, camera) { px, py, res, amt ->
                            spawnPickup(px, py, res, amt)
                        }
                        if (b.type == BuildingType.BARRACKS && Random.nextFloat() < 0.12f) {
                            spawner.spawnGarrisonReinforcements(b.positionX, b.positionY) { enemies.add(it) }
                        }
                        projectile.isActive = false
                        break
                    }
                }
            }
        }

        // 4. Enemy spawning and movement
        spawner.update(
            deltaTime = deltaTime,
            castleX = castle.positionX,
            castleY = castle.positionY,
            currentEnemyCount = enemies.count { it.isAlive },
            onSpawnEnemy = { enemy ->
                enemies.add(enemy)
                if (enemy is EnemyBoss) {
                    activeBoss = enemy
                    onBossSpawned?.invoke()
                    camera.shake(intensity = 20f, duration = 0.5f)
                    camera.zoomTo(target = 0.85f, speed = 1.5f) // Zoom out for epic boss fight
                }
            },
            onBossTriggered = {
                // Boss music / event hook
            }
        )

        // 5. Enemy updates & attacks on fortress
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            if (!enemy.isAlive) {
                enemyIterator.remove()
                continue
            }

            val damageDealt = enemy.update(
                deltaTime = deltaTime,
                targetCastleX = castle.positionX,
                targetCastleY = castle.positionY,
                onFireProjectile = { type, fx, fy, tx, ty, spd, dmg, rad, expRad ->
                    projectileManager.spawn(
                        type = type,
                        fromX = fx,
                        fromY = fy,
                        targetX = tx,
                        targetY = ty,
                        speed = spd,
                        damage = dmg,
                        radius = rad,
                        explosionRadius = expRad,
                        pierce = 1,
                        isEnemy = true
                    )
                }
            )

            // Boss phase summons when dropping below 70% and 35% health
            if (enemy is EnemyBoss) {
                if (!enemy.summonPhase1Done && enemy.health <= enemy.maxHealth * 0.70f) {
                    enemy.summonPhase1Done = true
                    spawner.spawnGarrisonReinforcements(enemy.positionX, enemy.positionY) { enemies.add(it) }
                    particleManager.spawnExplosion(enemy.positionX, enemy.positionY, radius = 50f)
                    camera.shake(intensity = 15f, duration = 0.4f)
                }
                if (!enemy.summonPhase2Done && enemy.health <= enemy.maxHealth * 0.35f) {
                    enemy.summonPhase2Done = true
                    spawner.spawnGarrisonReinforcements(enemy.positionX - 40f, enemy.positionY) { enemies.add(it) }
                    spawner.spawnGarrisonReinforcements(enemy.positionX + 40f, enemy.positionY) { enemies.add(it) }
                    particleManager.spawnExplosion(enemy.positionX, enemy.positionY, radius = 70f)
                    camera.shake(intensity = 20f, duration = 0.5f)
                }
            }

            if (damageDealt > 0f) {
                val destroyed = castle.takeDamage(damageDealt)
                camera.shake(intensity = 6f, duration = 0.15f)
                particleManager.spawnHitSparks(castle.positionX, castle.positionY, count = 3)

                if (enemy is com.game.castlewar.enemies.EnemyBomber) {
                    // Bomber explodes on contact
                    particleManager.spawnExplosion(enemy.positionX, enemy.positionY)
                    enemy.takeDamage(9999f)
                }

                if (destroyed) {
                    particleManager.spawnExplosion(castle.positionX, castle.positionY, radius = 80f)
                    camera.shake(intensity = 25f, duration = 0.8f)
                    onGameOverTriggered?.invoke()
                }
            }

            // Ramming damage: Fortress crushing into enemies!
            if (CollisionUtils.circleVsCircle(
                    castle.positionX, castle.positionY, castle.collisionRadius,
                    enemy.positionX, enemy.positionY, enemy.radius
                )
            ) {
                // Heavy crushing ram damage from massive iron treads!
                val ramKilled = enemy.takeDamage(60f * deltaTime + 5f)
                particleManager.spawnTreadDust(enemy.positionX, enemy.positionY)
                if (ramKilled) handleEnemyDefeated(enemy)
            }
        }

        // 5.5 Crowd separation between alive enemies to keep tactical lines
        val enemyCount = enemies.size
        for (i in 0 until enemyCount) {
            val e1 = enemies[i]
            if (!e1.isAlive) continue
            for (j in i + 1 until enemyCount) {
                val e2 = enemies[j]
                if (!e2.isAlive) continue
                val cdx = e2.positionX - e1.positionX
                val cdy = e2.positionY - e1.positionY
                val distSq = cdx * cdx + cdy * cdy
                val minDist = e1.radius + e2.radius + 6f
                if (distSq < minDist * minDist && distSq > 0.04f) {
                    val dist = kotlin.math.sqrt(distSq)
                    val overlap = (minDist - dist) * 0.4f
                    val nx = cdx / dist
                    val ny = cdy / dist
                    e1.positionX -= nx * overlap
                    e1.positionY -= ny * overlap
                    e2.positionX += nx * overlap
                    e2.positionY += ny * overlap
                }
            }
        }

        // 5.6 Smoke emission from damaged fortress hull
        if (castle.isDamaged && Random.nextFloat() < 0.35f) {
            particleManager.spawnCastleDamageSmoke(castle.positionX, castle.positionY, castle.isSeverelyDamaged)
        }

        // 6. Fortress ramming into destructible buildings (crushing walls/tents)
        for (b in buildings) {
            if (b.isDestroyed) continue
            if (CollisionUtils.circleVsRectangle(
                    castle.positionX, castle.positionY, castle.collisionRadius + 10f,
                    b.positionX - b.type.width * 0.5f,
                    b.positionY - b.type.height * 0.5f,
                    b.type.width, b.type.height
                )
            ) {
                b.takeDamage(120f * deltaTime + 10f, particleManager, camera) { px, py, res, amt ->
                    spawnPickup(px, py, res, amt)
                }
            }
        }

        // 7. Pickups magnet pull & collection
        for (pickup in pickups) {
            if (!pickup.isActive) continue
            val collected = pickup.update(
                deltaTime = deltaTime,
                targetX = castle.positionX,
                targetY = castle.positionY,
                magnetRange = castle.magnetRange
            )
            if (collected) {
                if (pickup.type == ResourceType.EXPERIENCE) {
                    val leveledUp = castle.addExperience(pickup.amount)
                    if (leveledUp) {
                        particleManager.spawnCastleUpgradeBurst(castle.positionX, castle.positionY)
                        camera.shake(intensity = 12f, duration = 0.35f)
                        onLevelUpTriggered?.invoke()
                    }
                } else {
                    castle.addResource(pickup.type, pickup.amount)
                }
                particleManager.spawn(
                    x = castle.positionX,
                    y = castle.positionY,
                    vx = (Random.nextFloat() - 0.5f) * 40f,
                    vy = (Random.nextFloat() - 0.5f) * 40f,
                    lifetime = 0.3f,
                    startSize = 4f,
                    endSize = 1f,
                    type = com.game.castlewar.effects.ParticleType.SPARK,
                    r = 1f, g = 0.9f, b = 0.2f
                )
            }
        }

        // 8. Update active particles
        particleManager.update(deltaTime)
    }

    private fun handleEnemyDefeated(enemy: Enemy) {
        particleManager.spawnHitSparks(enemy.positionX, enemy.positionY, count = 6)
        // Drop XP
        spawnPickup(enemy.positionX, enemy.positionY, ResourceType.EXPERIENCE, enemy.rewardXP)
        // Drop Gold
        spawnPickup(enemy.positionX + 8f, enemy.positionY - 8f, ResourceType.GOLD, enemy.rewardGold)

        if (enemy === activeBoss) {
            activeBoss = null
            // Huge victory bounty!
            spawnPickup(enemy.positionX - 20f, enemy.positionY, ResourceType.MAGIC_CRYSTAL, 10)
            spawnPickup(enemy.positionX + 20f, enemy.positionY, ResourceType.IRON, 40)
        }
    }
}
