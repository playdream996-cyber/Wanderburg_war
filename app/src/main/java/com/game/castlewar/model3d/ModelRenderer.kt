package com.game.castlewar.model3d

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log

/**
 * High-performance stylized 3D renderer tuned for bright fantasy readability.
 * The fortress and units receive stronger rim/brightness hierarchy while terrain and
 * buildings stay softer so gameplay silhouettes remain dominant on mobile screens.
 */
class ModelRenderer {

    companion object {
        private const val TAG = "ModelRenderer"

        val PROFILE_FORTRESS = Pair(0.96f, 1.24f)
        val PROFILE_CHARACTER = Pair(0.82f, 1.18f)
        val PROFILE_ENVIRONMENT = Pair(0.24f, 1.00f)
        val PROFILE_DEFAULT = Pair(0.30f, 1.04f)
        val PROFILE_BUILDING = Pair(0.42f, 1.08f)
        val PROFILE_PICKUP = Pair(1.05f, 1.34f)
        val PROFILE_VFX_PICKUP = Pair(1.10f, 1.38f)

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
        baseColor *= texture(uDiffuseTexture, vTexCoord);
    }

    vec3 N = normalize(vNormal);
    vec3 L = normalize(uSunDirection);
    vec3 V = normalize(uCameraPos - vWorldPos);

    // Soft hemisphere ambient keeps underside forms readable instead of muddy.
    float hemi = N.y * 0.5 + 0.5;
    vec3 ambientHemi = mix(uGroundColor, uSkyColor, hemi);
    float heightFactor = clamp((vWorldPos.y + 4.0) * 0.032, 0.0, 1.0);
    ambientHemi = mix(ambientHemi * 0.92, ambientHemi * 1.10, heightFactor);

    // Wrapped diffuse gives chunky stylized forms wide readable light bands.
    float NdotL = dot(N, L);
    float softDiffuse = smoothstep(-0.35, 0.88, NdotL);
    vec3 shadowTint = vec3(0.24, 0.29, 0.35);
    vec3 directLight = mix(shadowTint, uSunColor, softDiffuse);

    // Matte metallic/specular treatment.
    vec3 H = normalize(L + V);
    float NdotH = max(dot(N, H), 0.0);
    float shininess = mix(42.0, 7.0, uRoughness);
    float specFactor = pow(NdotH, shininess) * (1.0 - uRoughness) * (0.30 + uMetallic * 0.30);
    vec3 specular = uSunColor * specFactor;

    // Strong silhouette rim is the primary mobile readability tool.
    float rimFactor = 1.0 - max(dot(V, N), 0.0);
    rimFactor = pow(rimFactor, 2.7);
    float rimSunAlignment = max(dot(L, -V) * 0.5 + 0.5, 0.32);
    vec3 rim = uSunColor * (rimFactor * rimSunAlignment * uRimStrength);

    vec3 litRgb = (baseColor.rgb * (ambientHemi + directLight)) + specular + rim;
    litRgb *= uBrightnessBoost;

    // Softer, brighter distance atmosphere for fantasy overworld depth.
    float camDist = length(vWorldPos - uCameraPos);
    float fogFactor = clamp((camDist - 900.0) / 1700.0, 0.0, 0.16);
    vec3 fogColor = vec3(0.70, 0.78, 0.82);
    litRgb = mix(litRgb, fogColor, fogFactor);

    // Gentle premium contrast and saturation without plastic gloss.
    vec3 sCurve = litRgb * litRgb * (3.0 - 2.0 * litRgb);
    litRgb = mix(litRgb, sCurve, 0.20);
    float lum = dot(litRgb, vec3(0.299, 0.587, 0.114));
    litRgb = mix(vec3(lum), litRgb, 1.13);

    fragColor = vec4(clamp(litRgb, 0.0, 1.0), baseColor.a);
}
"""
    }

    private var programId: Int = 0

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

    private val tempNormalMatrix = FloatArray(16)
    private val matrixStack = Array(16) { FloatArray(16) }
    private var stackIndex = 0

    // Brighter warm key light + cool sky fill.
    val sunDirection = floatArrayOf(0.48f, 0.84f, 0.28f)
    val sunColor = floatArrayOf(1.12f, 1.03f, 0.90f)
    val skyColor = floatArrayOf(0.52f, 0.61f, 0.73f)
    val groundColor = floatArrayOf(0.27f, 0.34f, 0.22f)
    val cameraPos = floatArrayOf(0f, 250f, 300f)

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
            Log.e(TAG, "Failed to link shader program: ${GLES30.glGetProgramInfoLog(programId)}")
        }

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
            Log.e(TAG, "Shader compilation failed ($type): ${GLES30.glGetShaderInfoLog(shader)}")
        }
        return shader
    }

    fun begin(viewMat: FloatArray, projMat: FloatArray, camX: Float, camY: Float, camZ: Float) {
        if (!isInitialized) initialize()
        GLES30.glUseProgram(programId)
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
        GLES30.glUniform3fv(uSunDirectionLoc, 1, sunDirection, 0)
        GLES30.glUniform3fv(uSunColorLoc, 1, sunColor, 0)
        GLES30.glUniform3fv(uSkyColorLoc, 1, skyColor, 0)
        GLES30.glUniform3fv(uGroundColorLoc, 1, groundColor, 0)
        setLightingProfile(0.40f, 1.04f)
        stackIndex = 0
    }

    fun setLightingProfile(rimStrength: Float, brightnessBoost: Float) {
        GLES30.glUniform1f(uRimStrengthLoc, rimStrength)
        GLES30.glUniform1f(uBrightnessBoostLoc, brightnessBoost)
    }

    fun setLightingProfile(profile: Pair<Float, Float>) {
        setLightingProfile(profile.first, profile.second)
    }

    fun setMaterialAndTransform(modelMat: FloatArray, mat: Material) {
        GLES30.glUniformMatrix4fv(uModelMatrixLoc, 1, false, modelMat, 0)
        MatrixUtils.computeNormalMatrix(tempNormalMatrix, modelMat)
        GLES30.glUniformMatrix4fv(uNormalMatrixLoc, 1, false, tempNormalMatrix, 0)

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

        if (mat.doubleSided) GLES30.glDisable(GLES30.GL_CULL_FACE) else GLES30.glEnable(GLES30.GL_CULL_FACE)
    }

    fun renderMesh(mesh: Mesh, modelMat: FloatArray, overrideColor: Material? = null) {
        val mat = overrideColor ?: mesh.material
        setMaterialAndTransform(modelMat, mat)
        mesh.draw()
    }

    fun renderModel(model: Model, worldTransform: Transform, overrideColor: Material? = null) {
        val baseModelMat = worldTransform.getMatrix()
        for (mesh in model.directMeshes) renderMesh(mesh, baseModelMat, overrideColor)
        for (rootNode in model.rootNodes) renderNode(rootNode, baseModelMat, overrideColor)
    }

    fun renderModel(model: Model, modelMat: FloatArray, overrideColor: Material? = null) {
        for (mesh in model.directMeshes) renderMesh(mesh, modelMat, overrideColor)
        for (rootNode in model.rootNodes) renderNode(rootNode, modelMat, overrideColor)
    }

    private fun renderNode(node: ModelNode, parentMat: FloatArray, overrideColor: Material?) {
        val currentMat = matrixStack[stackIndex]
        stackIndex++
        val localMat = node.localTransform.getMatrix()
        Matrix.multiplyMM(currentMat, 0, parentMat, 0, localMat, 0)
        if (node.mesh != null) renderMesh(node.mesh, currentMat, overrideColor)
        for (child in node.children) renderNode(child, currentMat, overrideColor)
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
