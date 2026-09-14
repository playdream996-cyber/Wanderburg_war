package com.game.castlewar.effects

import kotlin.random.Random

enum class ParticleType {
    DUST,
    SMOKE,
    FIRE,
    EXPLOSION,
    SPARK,
    MAGIC,
    HIT,
    DEBRIS,
    GOLD_SPARK,
    SHOCKWAVE
}

class Particle {
    var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var lifetime: Float = 0f
    var maxLifetime: Float = 0f
    var size: Float = 4f
    var startSize: Float = 4f
    var endSize: Float = 1f
    var type: ParticleType = ParticleType.DUST

    var r: Float = 1f
    var g: Float = 1f
    var b: Float = 1f
    var a: Float = 1f

    fun reset(
        posX: Float,
        posY: Float,
        velX: Float,
        velY: Float,
        duration: Float,
        initialSize: Float,
        targetSize: Float,
        pType: ParticleType,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        x = posX
        y = posY
        vx = velX
        vy = velY
        lifetime = duration
        maxLifetime = duration
        startSize = initialSize
        endSize = targetSize
        size = initialSize
        type = pType
        r = red
        g = green
        b = blue
        a = alpha
        isActive = true
    }

    fun update(deltaTime: Float): Boolean {
        if (!isActive) return false

        lifetime -= deltaTime
        if (lifetime <= 0f) {
            isActive = false
            return false
        }

        x += vx * deltaTime
        y += vy * deltaTime

        // Decelerate with drag based on particle type
        val drag = if (type == ParticleType.DEBRIS || type == ParticleType.SPARK) 0.88f else 0.94f
        vx *= drag
        vy *= drag

        val progress = 1f - (lifetime / maxLifetime)
        size = startSize + (endSize - startSize) * progress
        a = (1f - progress).coerceIn(0f, 1f)

        return true
    }
}

class ParticleManager(private val maxParticles: Int = 600) {

    val pool = Array(maxParticles) { Particle() }

    fun spawn(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        lifetime: Float,
        startSize: Float,
        endSize: Float,
        type: ParticleType,
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1f
    ) {
        for (p in pool) {
            if (!p.isActive) {
                p.reset(x, y, vx, vy, lifetime, startSize, endSize, type, r, g, b, a)
                return
            }
        }
    }

    /**
     * Cannon Muzzle Flash: High energy fiery burst forward, smoke cone, and hot spark ejection.
     */
    fun spawnCannonMuzzleFlash(x: Float, y: Float, dirAngle: Float) {
        val cosA = kotlin.math.cos(dirAngle)
        val sinA = kotlin.math.sin(dirAngle)

        // Blazing Fire Flash Core
        for (i in 0 until 10) {
            val spread = (Random.nextFloat() - 0.5f) * 0.55f
            val a = dirAngle + spread
            val speed = Random.nextFloat() * 180f + 120f
            spawn(
                x = x + cosA * 10f,
                y = y + sinA * 10f,
                vx = kotlin.math.cos(a) * speed,
                vy = kotlin.math.sin(a) * speed,
                lifetime = Random.nextFloat() * 0.18f + 0.10f,
                startSize = Random.nextFloat() * 18f + 14f,
                endSize = 3f,
                type = ParticleType.FIRE,
                r = 1.0f,
                g = Random.nextFloat() * 0.4f + 0.5f,
                b = 0.1f
            )
        }

        // Heavy Gunpowder Smoke Plume
        for (i in 0 until 8) {
            val spread = (Random.nextFloat() - 0.5f) * 0.9f
            val a = dirAngle + spread
            val speed = Random.nextFloat() * 90f + 40f
            spawn(
                x = x + cosA * 15f,
                y = y + sinA * 15f,
                vx = kotlin.math.cos(a) * speed,
                vy = kotlin.math.sin(a) * speed,
                lifetime = Random.nextFloat() * 0.55f + 0.35f,
                startSize = 8f,
                endSize = Random.nextFloat() * 26f + 20f,
                type = ParticleType.SMOKE,
                r = 0.28f,
                g = 0.28f,
                b = 0.32f,
                a = 0.75f
            )
        }

        // Hot ejecta sparks
        for (i in 0 until 8) {
            val spread = (Random.nextFloat() - 0.5f) * 1.2f
            val a = dirAngle + spread
            val speed = Random.nextFloat() * 220f + 90f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(a) * speed,
                vy = kotlin.math.sin(a) * speed,
                lifetime = Random.nextFloat() * 0.25f + 0.15f,
                startSize = 5f,
                endSize = 1f,
                type = ParticleType.SPARK,
                r = 1.0f,
                g = 0.88f,
                b = 0.3f
            )
        }
    }

    /**
     * Cannon Shell Impact: Giant shockwave, fiery detonation, flying stone/dirt chunks, and lingering smoke.
     */
    fun spawnCannonImpact(x: Float, y: Float, radius: Float = 45f) {
        // 1. Core Fireball Burst
        for (i in 0 until 20) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 200f + 50f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.40f + 0.22f,
                startSize = Random.nextFloat() * 22f + 14f,
                endSize = 4f,
                type = ParticleType.FIRE,
                r = 1.0f,
                g = Random.nextFloat() * 0.45f + 0.25f,
                b = 0.05f
            )
        }

        // 2. Thick Dark Smoke Cloud
        for (i in 0 until 14) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 110f + 30f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.75f + 0.45f,
                startSize = 12f,
                endSize = Random.nextFloat() * 34f + 24f,
                type = ParticleType.SMOKE,
                r = 0.22f,
                g = 0.22f,
                b = 0.25f,
                a = 0.85f
            )
        }

        // 3. Flying Stone/Dirt Shrapnel Debris
        for (i in 0 until 14) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 240f + 70f
            val isWood = Random.nextBoolean()
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.45f + 0.30f,
                startSize = Random.nextFloat() * 6f + 4f,
                endSize = 2f,
                type = ParticleType.DEBRIS,
                r = if (isWood) 0.52f else 0.42f,
                g = if (isWood) 0.36f else 0.40f,
                b = if (isWood) 0.22f else 0.40f
            )
        }
    }

    /**
     * Arrow Impact: Wooden splinters and hit puff.
     */
    fun spawnArrowImpact(x: Float, y: Float) {
        for (i in 0 until 5) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 80f + 30f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = 0.22f,
                startSize = 3.5f,
                endSize = 1f,
                type = ParticleType.DEBRIS,
                r = 0.65f,
                g = 0.48f,
                b = 0.30f
            )
        }
    }

    /**
     * Ballista Heavy Bolt Impact: Destructive splinter spray & sparks.
     */
    fun spawnBallistaImpact(x: Float, y: Float) {
        for (i in 0 until 10) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 150f + 60f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = 0.32f,
                startSize = 5.5f,
                endSize = 1.5f,
                type = ParticleType.DEBRIS,
                r = 0.58f,
                g = 0.42f,
                b = 0.25f
            )
        }
        spawnHitSparks(x, y, count = 6)
    }

    /**
     * Building Collapse: Large billowing dust and timber rubble.
     */
    fun spawnBuildingCollapse(x: Float, y: Float, radius: Float = 50f) {
        // Massive tan/grey dust billowing
        for (i in 0 until 22) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 120f + 25f
            spawn(
                x = x + (Random.nextFloat() - 0.5f) * radius * 0.6f,
                y = y + (Random.nextFloat() - 0.5f) * radius * 0.6f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.85f + 0.5f,
                startSize = 12f,
                endSize = Random.nextFloat() * 40f + 28f,
                type = ParticleType.DUST,
                r = 0.62f,
                g = 0.56f,
                b = 0.46f,
                a = 0.80f
            )
        }
        // Collapsed burning embers
        for (i in 0 until 10) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 90f + 30f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.5f + 0.25f,
                startSize = 8f,
                endSize = 2f,
                type = ParticleType.FIRE,
                r = 1.0f,
                g = 0.45f,
                b = 0.1f
            )
        }
        // Flying Timber and Stone Rubble
        for (i in 0 until 16) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 180f + 60f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.45f + 0.35f,
                startSize = Random.nextFloat() * 7f + 4f,
                endSize = 2f,
                type = ParticleType.DEBRIS,
                r = 0.45f,
                g = 0.42f,
                b = 0.38f
            )
        }
    }

    /**
     * Castle Level-Up / Tier Upgrade Celebration Burst: Radiant golden sparks and arcane rays!
     */
    fun spawnCastleUpgradeBurst(x: Float, y: Float) {
        // Golden sparkling fountain
        for (i in 0 until 35) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 220f + 60f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = Random.nextFloat() * 0.65f + 0.35f,
                startSize = Random.nextFloat() * 12f + 6f,
                endSize = 2f,
                type = ParticleType.MAGIC,
                r = 1.0f,
                g = Random.nextFloat() * 0.4f + 0.6f,
                b = 0.1f
            )
        }
        // Blue Arcane Energy Ring
        for (i in 0 until 20) {
            val angle = (i.toFloat() / 20f) * 6.28318f
            val speed = 150f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = 0.5f,
                startSize = 8f,
                endSize = 18f,
                type = ParticleType.MAGIC,
                r = 0.25f,
                g = 0.75f,
                b = 1.0f,
                a = 0.85f
            )
        }
    }

    /**
     * Castle Damage Smoke: Spawns periodic smoke plumes and embers from damaged fortress hull.
     */
    fun spawnCastleDamageSmoke(x: Float, y: Float, isSeverelyDamaged: Boolean) {
        if (Random.nextFloat() > (if (isSeverelyDamaged) 0.65f else 0.35f)) {
            spawn(
                x = x + (Random.nextFloat() - 0.5f) * 30f,
                y = y + (Random.nextFloat() - 0.5f) * 30f,
                vx = (Random.nextFloat() - 0.5f) * 20f,
                vy = (Random.nextFloat() - 0.5f) * 20f,
                lifetime = Random.nextFloat() * 0.6f + 0.4f,
                startSize = 8f,
                endSize = Random.nextFloat() * 24f + 16f,
                type = ParticleType.SMOKE,
                r = 0.20f,
                g = 0.20f,
                b = 0.22f,
                a = 0.70f
            )
            if (isSeverelyDamaged && Random.nextFloat() < 0.4f) {
                spawn(
                    x = x + (Random.nextFloat() - 0.5f) * 25f,
                    y = y + (Random.nextFloat() - 0.5f) * 25f,
                    vx = (Random.nextFloat() - 0.5f) * 40f,
                    vy = (Random.nextFloat() - 0.5f) * 40f,
                    lifetime = 0.3f,
                    startSize = 9f,
                    endSize = 2f,
                    type = ParticleType.FIRE,
                    r = 1.0f,
                    g = 0.4f,
                    b = 0.05f
                )
            }
        }
    }

    fun spawnExplosion(x: Float, y: Float, radius: Float = 30f) {
        spawnCannonImpact(x, y, radius)
    }

    fun spawnHitSparks(x: Float, y: Float, count: Int = 5) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.28318f
            val speed = Random.nextFloat() * 90f + 30f
            spawn(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                lifetime = 0.2f,
                startSize = 4f,
                endSize = 1f,
                type = ParticleType.SPARK,
                r = 1.0f,
                g = 0.85f,
                b = 0.2f
            )
        }
    }

    fun spawnTreadDust(x: Float, y: Float) {
        if (Random.nextFloat() > 0.4f) return
        spawn(
            x = x + (Random.nextFloat() - 0.5f) * 16f,
            y = y + (Random.nextFloat() - 0.5f) * 16f,
            vx = (Random.nextFloat() - 0.5f) * 20f,
            vy = (Random.nextFloat() - 0.5f) * 20f,
            lifetime = 0.4f,
            startSize = 5f,
            endSize = 14f,
            type = ParticleType.DUST,
            r = 0.55f,
            g = 0.50f,
            b = 0.40f,
            a = 0.4f
        )
    }

    fun update(deltaTime: Float) {
        for (p in pool) {
            if (p.isActive) {
                p.update(deltaTime)
            }
        }
    }

    fun clear() {
        for (p in pool) {
            p.isActive = false
        }
    }
}
