package com.game.castlewar.model3d

import android.opengl.Matrix

/**
 * Node within a hierarchical 3D model.
 * Enables relative transformations for parts like rotating wheels, turret bases, and cannon barrels.
 */
class ModelNode(
    val name: String,
    val mesh: Mesh? = null,
    val localTransform: Transform = Transform()
) {
    val children = mutableListOf<ModelNode>()

    fun addChild(child: ModelNode) {
        children.add(child)
    }

    fun findNode(targetName: String): ModelNode? {
        if (name == targetName) return this
        for (child in children) {
            val found = child.findNode(targetName)
            if (found != null) return found
        }
        return null
    }
}

/**
 * Complete 3D Model composed of one or more meshes, materials, and node hierarchies.
 */
class Model(
    val name: String,
    val rootNodes: List<ModelNode> = emptyList(),
    val directMeshes: MutableList<Mesh> = mutableListOf()
) {
    // Standard attachment points for modular equipment (weapons, banners, wheels)
    val attachmentPoints = mutableMapOf<String, Transform>()

    fun addMesh(mesh: Mesh) {
        directMeshes.add(mesh)
    }

    fun addAttachmentPoint(pointName: String, transform: Transform) {
        attachmentPoints[pointName] = transform
    }

    fun getAttachmentTransform(pointName: String): Transform? = attachmentPoints[pointName]

    fun findNode(nodeName: String): ModelNode? {
        for (root in rootNodes) {
            val found = root.findNode(nodeName)
            if (found != null) return found
        }
        return null
    }

    /**
     * Initializes all meshes on the OpenGL render thread.
     */
    fun initializeGL() {
        directMeshes.forEach { it.initializeGL() }
        fun initNode(node: ModelNode) {
            node.mesh?.initializeGL()
            node.children.forEach { initNode(it) }
        }
        rootNodes.forEach { initNode(it) }
    }

    /**
     * Releases OpenGL GPU resources.
     */
    fun release() {
        directMeshes.forEach { it.release() }
        fun releaseNode(node: ModelNode) {
            node.mesh?.release()
            node.children.forEach { releaseNode(it) }
        }
        rootNodes.forEach { releaseNode(it) }
    }
}
