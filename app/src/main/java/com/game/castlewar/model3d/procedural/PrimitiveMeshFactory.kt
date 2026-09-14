package com.game.castlewar.model3d.procedural

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Procedural primitive 3D mesh factory for low-poly stylized medieval game assets.
 * Generates clean vertex data (Position, Normal, UV) and triangle indices for:
 * Box, Cylinder, Cone, Sphere, Prism, Wedge, Plane, Wheel, Roof, Tower, Wall, Arch, Battlement, Beam.
 */
object PrimitiveMeshFactory {

    /**
     * Centered Box (Axis-Aligned Cuboid).
     */
    fun createBox(width: Float, height: Float, depth: Float): MeshData {
        val hw = width * 0.5f
        val hh = height * 0.5f
        val hd = depth * 0.5f

        // 6 faces * 4 vertices = 24 vertices
        val verts = floatArrayOf(
            // Front face (Z+)
            -hw, -hh,  hd,   0f,  0f,  1f,   0f, 1f,
             hw, -hh,  hd,   0f,  0f,  1f,   1f, 1f,
             hw,  hh,  hd,   0f,  0f,  1f,   1f, 0f,
            -hw,  hh,  hd,   0f,  0f,  1f,   0f, 0f,
            // Back face (Z-)
             hw, -hh, -hd,   0f,  0f, -1f,   0f, 1f,
            -hw, -hh, -hd,   0f,  0f, -1f,   1f, 1f,
            -hw,  hh, -hd,   0f,  0f, -1f,   1f, 0f,
             hw,  hh, -hd,   0f,  0f, -1f,   0f, 0f,
            // Top face (Y+)
            -hw,  hh,  hd,   0f,  1f,  0f,   0f, 1f,
             hw,  hh,  hd,   0f,  1f,  0f,   1f, 1f,
             hw,  hh, -hd,   0f,  1f,  0f,   1f, 0f,
            -hw,  hh, -hd,   0f,  1f,  0f,   0f, 0f,
            // Bottom face (Y-)
            -hw, -hh, -hd,   0f, -1f,  0f,   0f, 1f,
             hw, -hh, -hd,   0f, -1f,  0f,   1f, 1f,
             hw, -hh,  hd,   0f, -1f,  0f,   1f, 0f,
            -hw, -hh,  hd,   0f, -1f,  0f,   0f, 0f,
            // Right face (X+)
             hw, -hh,  hd,   1f,  0f,  0f,   0f, 1f,
             hw, -hh, -hd,   1f,  0f,  0f,   1f, 1f,
             hw,  hh, -hd,   1f,  0f,  0f,   1f, 0f,
             hw,  hh,  hd,   1f,  0f,  0f,   0f, 0f,
            // Left face (X-)
            -hw, -hh, -hd,  -1f,  0f,  0f,   0f, 1f,
            -hw, -hh,  hd,  -1f,  0f,  0f,   1f, 1f,
            -hw,  hh,  hd,  -1f,  0f,  0f,   1f, 0f,
            -hw,  hh, -hd,  -1f,  0f,  0f,   0f, 0f
        )

        val indices = IntArray(36)
        var iIdx = 0
        for (f in 0 until 6) {
            val vBase = f * 4
            indices[iIdx++] = vBase + 0
            indices[iIdx++] = vBase + 1
            indices[iIdx++] = vBase + 2
            indices[iIdx++] = vBase + 0
            indices[iIdx++] = vBase + 2
            indices[iIdx++] = vBase + 3
        }

        return MeshData(verts, indices)
    }

    /**
     * Cylinder aligned along Y axis.
     */
    fun createCylinder(radius: Float, height: Float, segments: Int = 10, capped: Boolean = true): MeshData {
        val seg = segments.coerceAtLeast(3)
        val hh = height * 0.5f

        val vertList = mutableListOf<Float>()
        val indList = mutableListOf<Int>()

        // Side quads
        for (i in 0 until seg) {
            val a0 = (i.toFloat() / seg) * 2f * PI.toFloat()
            val a1 = ((i + 1).toFloat() / seg) * 2f * PI.toFloat()

            val c0 = cos(a0); val s0 = sin(a0)
            val c1 = cos(a1); val s1 = sin(a1)

            val vBase = vertList.size / MeshData.FLOATS_PER_VERTEX

            // v0: bottom left
            vertList.addAll(listOf(c0 * radius, -hh, s0 * radius,  c0, 0f, s0,  0f, 1f))
            // v1: bottom right
            vertList.addAll(listOf(c1 * radius, -hh, s1 * radius,  c1, 0f, s1,  1f, 1f))
            // v2: top right
            vertList.addAll(listOf(c1 * radius,  hh, s1 * radius,  c1, 0f, s1,  1f, 0f))
            // v3: top left
            vertList.addAll(listOf(c0 * radius,  hh, s0 * radius,  c0, 0f, s0,  0f, 0f))

            indList.addAll(listOf(vBase, vBase + 1, vBase + 2, vBase, vBase + 2, vBase + 3))
        }

        if (capped) {
            // Top cap
            val topCenter = vertList.size / MeshData.FLOATS_PER_VERTEX
            vertList.addAll(listOf(0f, hh, 0f,  0f, 1f, 0f,  0.5f, 0.5f))
            val topRingBase = vertList.size / MeshData.FLOATS_PER_VERTEX
            for (i in 0 until seg) {
                val a = (i.toFloat() / seg) * 2f * PI.toFloat()
                vertList.addAll(listOf(cos(a) * radius, hh, sin(a) * radius,  0f, 1f, 0f,  0.5f + cos(a) * 0.5f, 0.5f + sin(a) * 0.5f))
            }
            for (i in 0 until seg) {
                val nxt = (i + 1) % seg
                indList.addAll(listOf(topCenter, topRingBase + i, topRingBase + nxt))
            }

            // Bottom cap
            val botCenter = vertList.size / MeshData.FLOATS_PER_VERTEX
            vertList.addAll(listOf(0f, -hh, 0f,  0f, -1f, 0f,  0.5f, 0.5f))
            val botRingBase = vertList.size / MeshData.FLOATS_PER_VERTEX
            for (i in 0 until seg) {
                val a = (i.toFloat() / seg) * 2f * PI.toFloat()
                vertList.addAll(listOf(cos(a) * radius, -hh, sin(a) * radius,  0f, -1f, 0f,  0.5f + cos(a) * 0.5f, 0.5f + sin(a) * 0.5f))
            }
            for (i in 0 until seg) {
                val nxt = (i + 1) % seg
                indList.addAll(listOf(botCenter, botRingBase + nxt, botRingBase + i))
            }
        }

        return MeshData(vertList.toFloatArray(), indList.toIntArray())
    }

    /**
     * Cone aligned along Y axis (apex at top, base at -height/2).
     */
    fun createCone(radius: Float, height: Float, segments: Int = 10): MeshData {
        val seg = segments.coerceAtLeast(3)
        val hh = height * 0.5f

        val vertList = mutableListOf<Float>()
        val indList = mutableListOf<Int>()

        // Side triangles
        for (i in 0 until seg) {
            val a0 = (i.toFloat() / seg) * 2f * PI.toFloat()
            val a1 = ((i + 1).toFloat() / seg) * 2f * PI.toFloat()
            val aMid = (a0 + a1) * 0.5f

            val c0 = cos(a0); val s0 = sin(a0)
            val c1 = cos(a1); val s1 = sin(a1)
            val cm = cos(aMid); val sm = sin(aMid)

            val vBase = vertList.size / MeshData.FLOATS_PER_VERTEX

            // Apex
            vertList.addAll(listOf(0f, hh, 0f,  cm * 0.7f, 0.5f, sm * 0.7f,  0.5f, 0f))
            // Base vertex 0
            vertList.addAll(listOf(c0 * radius, -hh, s0 * radius,  c0 * 0.7f, 0.5f, s0 * 0.7f,  0f, 1f))
            // Base vertex 1
            vertList.addAll(listOf(c1 * radius, -hh, s1 * radius,  c1 * 0.7f, 0.5f, s1 * 0.7f,  1f, 1f))

            indList.addAll(listOf(vBase, vBase + 1, vBase + 2))
        }

        // Bottom cap
        val botCenter = vertList.size / MeshData.FLOATS_PER_VERTEX
        vertList.addAll(listOf(0f, -hh, 0f,  0f, -1f, 0f,  0.5f, 0.5f))
        val botRingBase = vertList.size / MeshData.FLOATS_PER_VERTEX
        for (i in 0 until seg) {
            val a = (i.toFloat() / seg) * 2f * PI.toFloat()
            vertList.addAll(listOf(cos(a) * radius, -hh, sin(a) * radius,  0f, -1f, 0f,  0.5f + cos(a) * 0.5f, 0.5f + sin(a) * 0.5f))
        }
        for (i in 0 until seg) {
            val nxt = (i + 1) % seg
            indList.addAll(listOf(botCenter, botRingBase + nxt, botRingBase + i))
        }

        return MeshData(vertList.toFloatArray(), indList.toIntArray())
    }

    /**
     * Low-poly stylized Sphere (icosphere or latitude/longitude sphere).
     */
    fun createSphere(radius: Float, rings: Int = 6, slices: Int = 8): MeshData {
        val vertList = mutableListOf<Float>()
        val indList = mutableListOf<Int>()

        for (r in 0..rings) {
            val phi = (r.toFloat() / rings) * PI.toFloat()
            val y = cos(phi) * radius
            val rRing = sin(phi) * radius

            for (s in 0..slices) {
                val theta = (s.toFloat() / slices) * 2f * PI.toFloat()
                val x = cos(theta) * rRing
                val z = sin(theta) * rRing

                val len = sqrt(x * x + y * y + z * z).coerceAtLeast(0.0001f)
                val nx = x / len; val ny = y / len; val nz = z / len
                val u = s.toFloat() / slices
                val v = r.toFloat() / rings

                vertList.addAll(listOf(x, y, z,  nx, ny, nz,  u, v))
            }
        }

        val stride = slices + 1
        for (r in 0 until rings) {
            for (s in 0 until slices) {
                val v0 = r * stride + s
                val v1 = v0 + 1
                val v2 = (r + 1) * stride + s
                val v3 = v2 + 1

                indList.addAll(listOf(v0, v2, v1, v1, v2, v3))
            }
        }

        return MeshData(vertList.toFloatArray(), indList.toIntArray())
    }

    /**
     * Prism (N-sided regular prism, e.g. 3 = triangular, 6 = hexagonal).
     */
    fun createPrism(radius: Float, height: Float, sides: Int = 6): MeshData {
        return createCylinder(radius, height, sides, capped = true)
    }

    /**
     * Wedge / Ramp (triangular profile along Z axis).
     */
    fun createWedge(width: Float, height: Float, depth: Float): MeshData {
        val hw = width * 0.5f
        val hh = height * 0.5f
        val hd = depth * 0.5f

        val vertList = mutableListOf<Float>()
        val indList = mutableListOf<Int>()

        // 5 faces: bottom, back, slope, left, right
        // Bottom (Y-)
        var b = vertList.size / MeshData.FLOATS_PER_VERTEX
        vertList.addAll(listOf(
            -hw, -hh, -hd,  0f, -1f, 0f,  0f, 0f,
             hw, -hh, -hd,  0f, -1f, 0f,  1f, 0f,
             hw, -hh,  hd,  0f, -1f, 0f,  1f, 1f,
            -hw, -hh,  hd,  0f, -1f, 0f,  0f, 1f
        ))
        indList.addAll(listOf(b, b + 1, b + 2, b, b + 2, b + 3))

        // Back (Z-)
        b = vertList.size / MeshData.FLOATS_PER_VERTEX
        vertList.addAll(listOf(
             hw, -hh, -hd,  0f, 0f, -1f,  0f, 1f,
            -hw, -hh, -hd,  0f, 0f, -1f,  1f, 1f,
            -hw,  hh, -hd,  0f, 0f, -1f,  1f, 0f,
             hw,  hh, -hd,  0f, 0f, -1f,  0f, 0f
        ))
        indList.addAll(listOf(b, b + 1, b + 2, b, b + 2, b + 3))

        // Slope (facing +Y and +Z)
        val hypLen = sqrt(height * height + depth * depth).coerceAtLeast(0.001f)
        val sNy = depth / hypLen
        val sNz = height / hypLen
        b = vertList.size / MeshData.FLOATS_PER_VERTEX
        vertList.addAll(listOf(
            -hw,  hh, -hd,  0f, sNy, sNz,  0f, 0f,
             hw,  hh, -hd,  0f, sNy, sNz,  1f, 0f,
             hw, -hh,  hd,  0f, sNy, sNz,  1f, 1f,
            -hw, -hh,  hd,  0f, sNy, sNz,  0f, 1f
        ))
        indList.addAll(listOf(b, b + 1, b + 2, b, b + 2, b + 3))

        // Left triangle (X-)
        b = vertList.size / MeshData.FLOATS_PER_VERTEX
        vertList.addAll(listOf(
            -hw, -hh,  hd,  -1f, 0f, 0f,  0f, 1f,
            -hw, -hh, -hd,  -1f, 0f, 0f,  1f, 1f,
            -hw,  hh, -hd,  -1f, 0f, 0f,  1f, 0f
        ))
        indList.addAll(listOf(b, b + 1, b + 2))

        // Right triangle (X+)
        b = vertList.size / MeshData.FLOATS_PER_VERTEX
        vertList.addAll(listOf(
             hw, -hh, -hd,   1f, 0f, 0f,  0f, 1f,
             hw, -hh,  hd,   1f, 0f, 0f,  1f, 1f,
             hw,  hh, -hd,   1f, 0f, 0f,  0f, 0f
        ))
        indList.addAll(listOf(b, b + 1, b + 2))

        return MeshData(vertList.toFloatArray(), indList.toIntArray())
    }

    /**
     * Horizontal flat Plane.
     */
    fun createPlane(width: Float, depth: Float): MeshData {
        val hw = width * 0.5f
        val hd = depth * 0.5f
        val verts = floatArrayOf(
            -hw, 0f,  hd,   0f, 1f, 0f,   0f, 1f,
             hw, 0f,  hd,   0f, 1f, 0f,   1f, 1f,
             hw, 0f, -hd,   0f, 1f, 0f,   1f, 0f,
            -hw, 0f, -hd,   0f, 1f, 0f,   0f, 0f
        )
        val indices = intArrayOf(0, 1, 2, 0, 2, 3)
        return MeshData(verts, indices)
    }

    /**
     * Detailed wooden Wheel with outer rim, inner ring, hub, axle, and spokes.
     */
    fun createWheel(
        outerRadius: Float,
        width: Float,
        hubRadius: Float = outerRadius * 0.28f,
        spokeCount: Int = 8,
        segments: Int = 16
    ): MeshData {
        val mesh = MeshData.empty()
        val hw = width * 0.5f

        // 1. Outer rim cylinder (thick tire)
        val rim = createCylinder(outerRadius, width, segments, capped = false)
        mesh.append(rim)

        // 2. Inner rim cylinder
        val innerRim = createCylinder(outerRadius * 0.82f, width * 0.92f, segments, capped = false)
        mesh.append(innerRim)

        // 3. Central Hub
        val hub = createCylinder(hubRadius, width * 1.15f, 10, capped = true)
        mesh.append(hub)

        // 4. Axle core
        val axle = createCylinder(hubRadius * 0.45f, width * 1.5f, 8, capped = true)
        mesh.append(axle)

        // 5. Spokes connecting hub to inner rim
        val spokeLen = outerRadius * 0.82f - hubRadius
        val spokeThick = outerRadius * 0.08f
        val spokeCenterR = hubRadius + spokeLen * 0.5f

        for (i in 0 until spokeCount) {
            val angle = (i.toFloat() / spokeCount) * 2f * PI.toFloat()
            val spoke = createBox(spokeThick, width * 0.65f, spokeLen)
            spoke.rotate(0f, -Math.toDegrees(angle.toDouble()).toFloat(), 0f)
            spoke.translate(cos(angle) * spokeCenterR, 0f, sin(angle) * spokeCenterR)
            mesh.append(spoke)
        }

        return mesh
    }

    /**
     * Pitched Roof (triangular gable roof for medieval houses).
     */
    fun createPitchedRoof(width: Float, height: Float, depth: Float, overhang: Float = 0.2f): MeshData {
        val totalW = width + overhang * 2f
        val totalD = depth + overhang * 2f
        val hw = totalW * 0.5f
        val hh = height * 0.5f
        val hd = totalD * 0.5f

        val mesh = MeshData.empty()

        // Left slope
        val leftSlope = createWedge(totalD, height, hw)
        leftSlope.rotate(0f, -90f, 0f)
        leftSlope.translate(-hw * 0.5f, 0f, 0f)
        mesh.append(leftSlope)

        // Right slope
        val rightSlope = createWedge(totalD, height, hw)
        rightSlope.rotate(0f, 90f, 0f)
        rightSlope.translate(hw * 0.5f, 0f, 0f)
        mesh.append(rightSlope)

        return mesh
    }

    /**
     * Round or Octagonal crenelated Tower section.
     */
    fun createTower(radius: Float, height: Float, segments: Int = 8): MeshData {
        val mesh = MeshData.empty()
        val shaft = createCylinder(radius, height, segments, capped = true)
        mesh.append(shaft)

        // Parapet top flare
        val parapet = createCylinder(radius * 1.15f, height * 0.18f, segments, capped = true)
        parapet.translate(0f, height * 0.5f + height * 0.09f, 0f)
        mesh.append(parapet)

        return mesh
    }

    /**
     * Stone Wall block with beveled edges.
     */
    fun createWall(length: Float, height: Float, thickness: Float): MeshData {
        return createBox(length, height, thickness)
    }

    /**
     * Curved Arch Gateway (horseshoe/round arch).
     */
    fun createArch(width: Float, height: Float, depth: Float, segments: Int = 8): MeshData {
        val mesh = MeshData.empty()
        val postW = width * 0.22f
        val postH = height * 0.7f

        // Left pillar
        val leftPost = createBox(postW, postH, depth)
        leftPost.translate(-width * 0.5f + postW * 0.5f, -height * 0.5f + postH * 0.5f, 0f)
        mesh.append(leftPost)

        // Right pillar
        val rightPost = createBox(postW, postH, depth)
        rightPost.translate(width * 0.5f - postW * 0.5f, -height * 0.5f + postH * 0.5f, 0f)
        mesh.append(rightPost)

        // Arch lintel top
        val lintel = createBox(width, height * 0.28f, depth)
        lintel.translate(0f, height * 0.5f - height * 0.14f, 0f)
        mesh.append(lintel)

        return mesh
    }

    /**
     * Castle Battlement (crenelated tooth).
     */
    fun createBattlement(width: Float, height: Float, depth: Float): MeshData {
        return createBox(width, height, depth)
    }

    /**
     * Wooden Beam / Timber post with beveled cross-section.
     */
    fun createBeam(length: Float, thickness: Float): MeshData {
        return createBox(thickness, length, thickness)
    }
}
