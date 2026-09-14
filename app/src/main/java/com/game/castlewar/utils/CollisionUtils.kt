package com.game.castlewar.utils

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance 2D collision detection utilities without external physics overhead.
 */
object CollisionUtils {

    /**
     * Test collision between two circles.
     */
    fun circleVsCircle(
        c1x: Float, c1y: Float, r1: Float,
        c2x: Float, c2y: Float, r2: Float
    ): Boolean {
        val dx = c2x - c1x
        val dy = c2y - c1y
        val distSq = dx * dx + dy * dy
        val radiusSum = r1 + r2
        return distSq <= radiusSum * radiusSum
    }

    /**
     * Test collision between a circle and an axis-aligned rectangle.
     */
    fun circleVsRectangle(
        cx: Float, cy: Float, radius: Float,
        rx: Float, ry: Float, rw: Float, rh: Float
    ): Boolean {
        // Find closest point on rectangle to circle center
        val closestX = cx.coerceIn(rx, rx + rw)
        val closestY = cy.coerceIn(ry, ry + rh)

        val dx = cx - closestX
        val dy = cy - closestY
        return (dx * dx + dy * dy) <= (radius * radius)
    }

    /**
     * Test collision between two axis-aligned rectangles.
     */
    fun rectangleVsRectangle(
        r1x: Float, r1y: Float, r1w: Float, r1h: Float,
        r2x: Float, r2y: Float, r2w: Float, r2h: Float
    ): Boolean {
        return r1x < r2x + r2w &&
               r1x + r1w > r2x &&
               r1y < r2y + r2h &&
               r1y + r1h > r2y
    }

    /**
     * Calculates Euclidean distance between two points.
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Calculates squared distance (avoids sqrt for fast threshold checks).
     */
    fun distanceSquared(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return dx * dx + dy * dy
    }
}
