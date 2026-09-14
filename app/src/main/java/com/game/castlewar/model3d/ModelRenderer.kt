package com.game.castlewar.model3d

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log

/**
 * High-performance 3D Model Renderer for OpenGL ES 3.0.
 *
 * Features:
 * - Directional sunlight & ambient lighting
 * - Stylized matte medieval shading with Blinn-Phong specular highlights
 * - Zero allocations per frame in draw calls
 * - Recursive hierarchical node rendering
 * - Frustum & distance culling support
 */
class ModelRenderer {

    companion object {
        private const val TAG = "ModelRenderer"

        // Profiles for visual hierarchy
        val PROFILE_FORTRESS = Pair(0.70f, 1.15f)
        val PROFILE_CHARACTER = Pair(0.55f, 1.10f)
        val PROFILE_ENVIRONMENT = Pair(0.15f, 0.94f)
        val PROFILE_DEFAULT = Pair(0.20f, 1.0f)
        val PROFILE_BUILDING = Pair(0.30f, 1.02f)
        val PROFILE_PICKUP = Pair(0.85f, 1.25f)
        val PROFILE_VFX_PICKUP = Pair(0.85f, 1.25f)

        private const val VERTEX_SHADER_SRC = """#version 300 es
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aTexCoord;

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjectionMatrix;
uniform mat4 uNormalMatrix;

out vec3 vWorldPos;
out vec3 vNormal;
out vec2 vTexCoord;

void main() {
    vec4 worldPos = uModelMatrix * vec4(aPosition, 1.0);
    vWorldPos = worldPos.xyz;
    vNormal = normalize((uNormalMatrix * vec4(aNormal, 0.0)).xyz);
    vTexCoord = aTexCoord;
    gl_Position = uProjectionMatrix * uViewMatrix * worldPos;
}
"""

        private const val FRAGMENT_SHADER_SRC = """#version 300 es
precision mediump float;

in vec3 vWorldPos;
in vec3 vNormal;
in vec2 vTexCoord;

uniform vec4 uBaseColor;
uniform float uRoughness;
uniform float uMetallic;
uniform int uHasTexture;
uniform sampler2D uDiffuseTexture;

uniform vec3 uSunDirection;
uniform vec3 uSunColor;
uniform vec3 uSkyColor;
uniform vec3 uGroundColor;
uniform vec3 uCameraPos;

uniform float uRimStrength;
uniform float uBrightnessBoost;

out vec4 fragColor;

void main() {
    vec4 baseColor = uBaseColor;
    if (uHasTexture == 1) {
        vec4 texColor = texture(uDiffuseTexture, vTexCoord);
        baseColor *= texColor;
    }

    vec3 N = normalize(vNormal);
    vec3 L = normalize(uSunDirection);
    vec3 V = normalize(uCameraPos - vWorldPos);

    // 1. Hemisphere sky/ground ambient
    float hemi = N.y * 0.5 + 0.5;
    vec3 ambientHemi = mix(uGroundColor, uSkyColor, hemi);

    // Subtle height-based ambient variation for grounded contact
    float heightFactor = clamp((vWorldPos.y + 4.0) * 0.035, 0.0, 1.0);
    ambientHemi = mix(ambientHemi * 0.88, ambientHemi * 1.12, heightFactor);

    // 2. Soft wrapped Half-Lambert diffuse
    float NdotL = dot(N, L);
    float softDiffuse = smoothstep(-0.25, 0.95, NdotL);

    // Cooler environmental shadow tint for readable deep shadows
    vec3 shadowTint = vec3(0.20, 0.24, 0.35);
    vec3 directLight = mix(shadowTint, uSunColor, softDiffuse);

    // 3. Subtle Blinn-Phong specular highlight
    vec3 H = normalize(L + V);
    float NdotH = max(dot(N, H), 0.0);
    float shininess = mix(48.0, 6.0, uRoughness);
    float specFactor = pow(NdotH, shininess) * (1.0 - uRoughness) * 0.45;
    vec3 specular = uSunColor * specFactor;

    // 4. Soft Rim highlight on silhouette edges
    float rimFactor = 1.0 - max(dot(V, N), 0.0);
    rimFactor = pow(rimFactor, 3.2);
    float rimSunAlignment = max(dot(L, -V) * 0.5 + 0.5, 0.25);
    vec3 rim = uSunColor * (rimFactor * rimSunAlignment * uRimStrength);

    // Layered lighting assembly
    vec3 litRgb = (baseColor.rgb * (ambientHemi + directLight)) + specular + rim;
    litRgb *= uBrightnessBoost;

    // 5. Distance atmospheric tint (soft horizon haze)
    float camDist = length(vWorldPos - uCameraPos);
    float fogFactor = clamp((camDist - 750.0) / 1450.0, 0.0, 0.20);
    vec3 fogColor = vec3(0.66, 0.72, 0.82);
    litRgb = mix(litRgb, fogColor, fogFactor);

    // 6. Premium Color Grading (Warm highlights, rich saturation, deep readable shadows)
    // S-curve contrast
    vec3 sCurve = litRgb * litRgb * (3.0 - 2.0 * litRgb);
    litRgb = mix(litRgb, sCurve, 0.24);

    // Saturation enhancement
    float lum = dot(litRgb, vec3(0.299, 0.587, 0.114));
    litRgb = mix(vec3(lum), litRgb, 1.16);

    fragColor = vec4(clamp(litRgb, 0.0, 1.0), baseColor.a);
}
"""
    }

    private var programId: Int = 0

    // Shader Uniform Locations
    private var uModelMatrixLoc: Int = -1
    private var uViewMatrixLoc: Int = -1
    private var uProjectionMatrixLoc: Int = -1
    private var uNormalMatrixLoc: Int = -1
    private var uBaseColorLoc: Int = -1
    private var uRoughnessLoc: Int = -1
    private var uMetallicLoc: Int = -1
    private var uHasTextureLoc: Int = -1
    private var uDiffuseTextureLoc: Int = -1
    private var uSunDirectionLoc: Int = -1
    private var uSunColorLoc: Int = -1
    private var uSkyColorLoc: Int = -1
    private var uGroundColorLoc: Int = -1
    private var uCameraPosLoc: Int = -1
    private var uRimStrengthLoc: Int = -1
    private var uBrightnessBoostLoc: Int = -1

    // Reusable matrices for zero per-frame garbage
    private val tempNormalMatrix = FloatArray(16)
    private val matrixStack = Array(16) { FloatArray(16) }
    private var stackIndex = 0

    // Sun & Ambient Lighting parameters
    val sunDirection = floatArrayOf(0.55f, 0.75f, 0.35f) // Normalized downward-angled sunlight
    val sunColor = floatArrayOf(1.05f, 0.98f, 0.88f)       // Warm golden sun
    val skyColor = floatArrayOf(0.45f, 0.52f, 0.64f)       // Soft sky ambient blue-gray
    val groundColor = floatArrayOf(0.24f, 0.30f, 0.22f)    // Warm earthy meadow bounce
    val cameraPos = floatArrayOf(0f, 250f, 300f)

    // Current Camera Matrices
    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)

    var isInitialized = false
        private set

    fun initialize() {
        if (isInitialized) return

        val vertShader = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_SRC)
        val fragShader = compileShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SRC)

        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertShader)
        GLES30.glAttachShader(programId, fragShader)
        GLES30.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(programId)
            Log.e(TAG, "Failed to link shader program: $log")
        }

        // Cache uniform locations
        uModelMatrixLoc = GLES30.glGetUniformLocation(programId, "uModelMatrix")
        uViewMatrixLoc = GLES30.glGetUniformLocation(programId, "uViewMatrix")
        uProjectionMatrixLoc = GLES30.glGetUniformLocation(programId, "uProjectionMatrix")
        uNormalMatrixLoc = GLES30.glGetUniformLocation(programId, "uNormalMatrix")
        uBaseColorLoc = GLES30.glGetUniformLocation(programId, "uBaseColor")
        uRoughnessLoc = GLES30.glGetUniformLocation(programId, "uRoughness")
        uMetallicLoc = GLES30.glGetUniformLocation(programId, "uMetallic")
        uHasTextureLoc = GLES30.glGetUniformLocation(programId, "uHasTexture")
        uDiffuseTextureLoc = GLES30.glGetUniformLocation(programId, "uDiffuseTexture")
        uSunDirectionLoc = GLES30.glGetUniformLocation(programId, "uSunDirection")
        uSunColorLoc = GLES30.glGetUniformLocation(programId, "uSunColor")
        uSkyColorLoc = GLES30.glGetUniformLocation(programId, "uSkyColor")
        uGroundColorLoc = GLES30.glGetUniformLocation(programId, "uGroundColor")
        uCameraPosLoc = GLES30.glGetUniformLocation(programId, "uCameraPos")
        uRimStrengthLoc = GLES30.glGetUniformLocation(programId, "uRimStrength")
        uBrightnessBoostLoc = GLES30.glGetUniformLocation(programId, "uBrightnessBoost")

        GLES30.glDeleteShader(vertShader)
        GLES30.glDeleteShader(fragShader)

        isInitialized = true
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader compilation failed ($type): $log")
        }
        return shader
    }

    /**
     * Prepares the shader program and global camera/lighting uniforms for the frame.
     */
    fun begin(
        viewMat: FloatArray,
        projMat: FloatArray,
        camX: Float, camY: Float, camZ: Float
    ) {
        if (!isInitialized) initialize()

        GLES30.glUseProgram(programId)

        // Enable depth test and backface culling for proper 3D rendering
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)

        viewMat.copyInto(viewMatrix, 0, 0, 16)
        projMat.copyInto(projectionMatrix, 0, 0, 16)

        cameraPos[0] = camX
        cameraPos[1] = camY
        cameraPos[2] = camZ

        GLES30.glUniformMatrix4fv(uViewMatrixLoc, 1, false, viewMatrix, 0)
        GLES30.glUniformMatrix4fv(uProjectionMatrixLoc, 1, false, projectionMatrix, 0)
        GLES30.glUniform3fv(uCameraPosLoc, 1, cameraPos, 0)

        // Lighting uniforms
        GLES30.glUniform3fv(uSunDirectionLoc, 1, sunDirection, 0)
        GLES30.glUniform3fv(uSunColorLoc, 1, sunColor, 0)
        GLES30.glUniform3fv(uSkyColorLoc, 1, skyColor, 0)
        GLES30.glUniform3fv(uGroundColorLoc, 1, groundColor, 0)

        // Default profile
        setLightingProfile(0.35f, 1.0f)

        stackIndex = 0
    }

    /**
     * Sets object-specific lighting hierarchy (e.g. higher rim/brightness for castle & characters).
     */
    fun setLightingProfile(rimStrength: Float, brightnessBoost: Float) {
        GLES30.glUniform1f(uRimStrengthLoc, rimStrength)
        GLES30.glUniform1f(uBrightnessBoostLoc, brightnessBoost)
    }

    fun setLightingProfile(profile: Pair<Float, Float>) {
        setLightingProfile(profile.first, profile.second)
    }

    /**
     * Sets material uniforms and model/normal transformation matrices for direct VAO/mesh draws.
     */
    fun setMaterialAndTransform(modelMat: FloatArray, mat: Material) {
        GLES30.glUniformMatrix4fv(uModelMatrixLoc, 1, false, modelMat, 0)

        // Compute normal matrix = transpose(inverse(modelMatrix))
        MatrixUtils.computeNormalMatrix(tempNormalMatrix, modelMat)
        GLES30.glUniformMatrix4fv(uNormalMatrixLoc, 1, false, tempNormalMatrix, 0)

        // Material uniforms
        GLES30.glUniform4f(uBaseColorLoc, mat.baseColorR, mat.baseColorG, mat.baseColorB, mat.baseColorA)
        GLES30.glUniform1f(uRoughnessLoc, mat.roughness)
        GLES30.glUniform1f(uMetallicLoc, mat.metallic)

        if (mat.texture != null) {
            GLES30.glUniform1i(uHasTextureLoc, 1)
            mat.texture!!.bind(0)
            GLES30.glUniform1i(uDiffuseTextureLoc, 0)
        } else {
            GLES30.glUniform1i(uHasTextureLoc, 0)
        }

        if (mat.doubleSided) {
            GLES30.glDisable(GLES30.GL_CULL_FACE)
        } else {
            GLES30.glEnable(GLES30.GL_CULL_FACE)
        }
    }

    /**
     * Renders a single mesh with its material and the specified model transformation matrix.
     */
    fun renderMesh(mesh: Mesh, modelMat: FloatArray, overrideColor: Material? = null) {
        val mat = overrideColor ?: mesh.material
        setMaterialAndTransform(modelMat, mat)
        mesh.draw()
    }

    /**
     * Renders a complete 3D model (including all sub-meshes and node hierarchy).
     */
    fun renderModel(model: Model, worldTransform: Transform, overrideColor: Material? = null) {
        val baseModelMat = worldTransform.getMatrix()

        // Render direct meshes
        for (mesh in model.directMeshes) {
            renderMesh(mesh, baseModelMat, overrideColor)
        }

        // Render hierarchical nodes
        for (rootNode in model.rootNodes) {
            renderNode(rootNode, baseModelMat, overrideColor)
        }
    }

    /**
     * Renders a complete 3D model directly with a given 4x4 model matrix.
     */
    fun renderModel(model: Model, modelMat: FloatArray, overrideColor: Material? = null) {
        for (mesh in model.directMeshes) {
            renderMesh(mesh, modelMat, overrideColor)
        }
        for (rootNode in model.rootNodes) {
            renderNode(rootNode, modelMat, overrideColor)
        }
    }

    private fun renderNode(node: ModelNode, parentMat: FloatArray, overrideColor: Material?) {
        val currentMat = matrixStack[stackIndex]
        stackIndex++

        val localMat = node.localTransform.getMatrix()
        Matrix.multiplyMM(currentMat, 0, parentMat, 0, localMat, 0)

        if (node.mesh != null) {
            renderMesh(node.mesh, currentMat, overrideColor)
        }

        for (child in node.children) {
            renderNode(child, currentMat, overrideColor)
        }

        stackIndex--
    }

    fun end() {
        GLES30.glBindVertexArray(0)
        GLES30.glUseProgram(0)
    }

    fun release() {
        if (isInitialized) {
            GLES30.glDeleteProgram(programId)
            isInitialized = false
        }
    }
}
