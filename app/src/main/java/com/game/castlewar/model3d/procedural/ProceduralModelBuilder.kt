package com.game.castlewar.model3d.procedural

import com.game.castlewar.model3d.Material
import com.game.castlewar.model3d.Mesh
import com.game.castlewar.model3d.Model

/**
 * Fluent builder for assembling stylized procedural 3D models.
 * Collects primitive geometry, transforms it in local space, groups it by Material,
 * and compiles it into high-performance combined meshes.
 */
class ProceduralModelBuilder(val modelName: String) {

    // Geometry batches mapped by Material to reduce draw calls
    private val materialBatches = mutableMapOf<Material, MeshData>()
    private val directParts = mutableListOf<ModelPart>()

    /**
     * Adds a raw MeshData with specified material and local transformation.
     */
    fun addMesh(
        meshData: MeshData,
        material: Material,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f,
        sx: Float = 1f, sy: Float = 1f, sz: Float = 1f
    ): ProceduralModelBuilder {
        val transformed = meshData.clone().transform(tx, ty, tz, rx, ry, rz, sx, sy, sz)
        val batch = materialBatches.getOrPut(material) { MeshData.empty() }
        batch.append(transformed)
        return this
    }

    /**
     * Adds a Box primitive.
     */
    fun addBox(
        width: Float, height: Float, depth: Float,
        material: Material,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f
    ): ProceduralModelBuilder {
        val box = PrimitiveMeshFactory.createBox(width, height, depth)
        return addMesh(box, material, tx, ty, tz, rx, ry, rz)
    }

    /**
     * Adds a Cylinder primitive.
     */
    fun addCylinder(
        radius: Float, height: Float,
        material: Material,
        segments: Int = 10,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f
    ): ProceduralModelBuilder {
        val cyl = PrimitiveMeshFactory.createCylinder(radius, height, segments)
        return addMesh(cyl, material, tx, ty, tz, rx, ry, rz)
    }

    /**
     * Adds a Cone primitive.
     */
    fun addCone(
        radius: Float, height: Float,
        material: Material,
        segments: Int = 10,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f
    ): ProceduralModelBuilder {
        val cone = PrimitiveMeshFactory.createCone(radius, height, segments)
        return addMesh(cone, material, tx, ty, tz, rx, ry, rz)
    }

    /**
     * Adds a Sphere primitive.
     */
    fun addSphere(
        radius: Float,
        material: Material,
        rings: Int = 6, slices: Int = 8,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f
    ): ProceduralModelBuilder {
        val sphere = PrimitiveMeshFactory.createSphere(radius, rings, slices)
        return addMesh(sphere, material, tx, ty, tz)
    }

    /**
     * Adds a Wedge/Ramp primitive.
     */
    fun addWedge(
        width: Float, height: Float, depth: Float,
        material: Material,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f
    ): ProceduralModelBuilder {
        val wedge = PrimitiveMeshFactory.createWedge(width, height, depth)
        return addMesh(wedge, material, tx, ty, tz, rx, ry, rz)
    }

    /**
     * Adds a Pitched Roof primitive.
     */
    fun addPitchedRoof(
        width: Float, height: Float, depth: Float,
        material: Material,
        overhang: Float = 0.15f,
        tx: Float = 0f, ty: Float = 0f, tz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f
    ): ProceduralModelBuilder {
        val roof = PrimitiveMeshFactory.createPitchedRoof(width, height, depth, overhang)
        return addMesh(roof, material, tx, ty, tz, rx, ry, rz)
    }

    /**
     * Adds a separate hierarchical ModelPart.
     */
    fun addPart(part: ModelPart): ProceduralModelBuilder {
        directParts.add(part)
        return this
    }

    /**
     * Compiles all geometry into an optimized GeneratedModel.
     * Geometry with identical materials is merged into single VBO meshes to maximize 60 FPS performance.
     */
    fun build(): GeneratedModel {
        val compiledParts = mutableListOf<ModelPart>()

        // Create an optimized single-mesh part for each material
        for ((mat, meshData) in materialBatches) {
            if (meshData.vertexCount > 0) {
                val mesh = meshData.toMesh(mat)
                compiledParts.add(ModelPart(mesh = mesh, material = mat))
            }
        }

        // Add any separate dynamic parts
        compiledParts.addAll(directParts)

        return GeneratedModel(modelName, compiledParts)
    }

    /**
     * Compiles directly into standard Model class.
     */
    fun buildModel(): Model {
        val model = Model(modelName)
        for ((mat, meshData) in materialBatches) {
            if (meshData.vertexCount > 0) {
                val mesh = meshData.toMesh(mat)
                model.addMesh(mesh)
            }
        }
        return model
    }
}
