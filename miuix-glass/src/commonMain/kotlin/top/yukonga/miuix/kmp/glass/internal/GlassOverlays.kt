// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import top.yukonga.miuix.kmp.glass.GlassDefaults
import top.yukonga.miuix.kmp.glass.GlassShadow
import top.yukonga.miuix.kmp.glass.GlassShape
import top.yukonga.miuix.kmp.glass.GlassStroke
import top.yukonga.miuix.kmp.glass.GlassStrokeLight
import top.yukonga.miuix.kmp.glass.GlassStyle
import top.yukonga.miuix.kmp.shader.RuntimeShader
import top.yukonga.miuix.kmp.shader.asBrush
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

private const val LIGHT_REF_X = 0.5f
private const val LIGHT_REF_Y = 0.7f

/**
 * Both overlay shaders are stateless between draws — every uniform is written immediately before
 * the draw that reads it — so one instance of each serves every glass surface in the process.
 */
private val maskShader: RuntimeShader? by lazy {
    if (isRuntimeShaderSupported()) RuntimeShader(GLASS_MASK_SHADER) else null
}

private val strokeShader: RuntimeShader? by lazy {
    if (isRuntimeShaderSupported()) RuntimeShader(GLASS_STROKE_SHADER) else null
}

private val rimShader: RuntimeShader? by lazy {
    if (isRuntimeShaderSupported()) RuntimeShader(GLASS_RIM_SHADER) else null
}

/**
 * Draws the material's own rim light over the surface, at full resolution.
 *
 * @param shape The silhouette the rim follows.
 * @param layoutDirection Resolves the shape's start and end corners.
 * @param style The material whose rim geometry and lights this draws.
 * @param surface The material's own colour, standing in for what lies under the rim.
 * @param alpha The surface opacity, so the rim fades with the glass it belongs to.
 */
internal fun DrawScope.drawGlassRim(
    shape: GlassShape,
    layoutDirection: LayoutDirection,
    style: GlassStyle,
    surface: Color,
    alpha: Float,
) {
    val shader = rimShader ?: return
    if (alpha <= 0f || size.width <= 0f || size.height <= 0f) return
    if (style.background.unShade >= 0.999f) return

    val edge = (style.edge.width * density / GlassDefaults.SourceDensity)
        .coerceIn(1f, size.minDimension * 0.5f)

    shader.setSilhouetteUniforms(shape, layoutDirection, size, this)
    shader.setFloatUniform("in_rimEdge", edge, style.edge.pow.coerceAtLeast(0.01f), alpha, 0f)
    shader.setFloatUniform(
        "in_lightDir",
        style.light.directionX,
        style.light.directionY,
        style.light.directionZ,
        style.light.angleRange * PI.toFloat(),
    )
    shader.setFloatUniform("in_lightAmt", style.light.intensity, style.light.oppositeIntensity, 0f, 0f)
    shader.setFloatUniform("in_surface", surface.red, surface.green, surface.blue, 1f)

    drawRect(brush = shader.asBrush(), blendMode = BlendMode.Plus)
}

/**
 * Draws the bloom stroke over the surface, at full resolution.
 *
 * @param shape The silhouette the stroke traces.
 * @param layoutDirection Resolves the shape's start and end corners.
 * @param stroke The stroke to draw.
 * @param alpha The surface opacity, so the rim fades with the glass it belongs to.
 */
internal fun DrawScope.drawGlassStroke(
    shape: GlassShape,
    layoutDirection: LayoutDirection,
    stroke: GlassStroke,
    alpha: Float,
) {
    val shader = strokeShader ?: return
    if (alpha <= 0f || size.width <= 0f || size.height <= 0f) return

    // setBloomStrokeConfig uses the scalar setBloomStrokeWithDp overload, which retains
    // the half-pixel bias instead of rounding to an integer like the array overload.
    val width = (stroke.width * density + 0.5f).coerceIn(0.5f, size.minDimension * 0.5f)
    val bevel = (stroke.bevel * density + 0.5f).coerceAtLeast(0.5f)

    shader.setSilhouetteUniforms(shape, layoutDirection, size, this)
    shader.setFloatUniform("in_halfViewFloor", floor(size.width * 0.5f), floor(size.height * 0.5f))
    shader.setFloatUniform("in_strokeBand", width, bevel)
    val color = stroke.color
    shader.setFloatUniform("in_strokeColor", color.red, color.green, color.blue, color.alpha)
    shader.setFloatUniform("in_strokeAlpha", alpha)
    shader.setLightUniforms("in_light1", "in_light1Color", stroke.primary)
    shader.setLightUniforms("in_light2", "in_light2Color", stroke.secondary)

    drawRect(brush = shader.asBrush(), blendMode = BlendMode.Plus)
}

/**
 * Trims the surface to [shape] at full resolution.
 *
 * @param shape The silhouette to cut to.
 * @param layoutDirection Resolves the shape's start and end corners.
 */
internal fun DrawScope.drawGlassMask(shape: GlassShape, layoutDirection: LayoutDirection) {
    val shader = maskShader ?: return
    if (size.width <= 0f || size.height <= 0f) return
    shader.setSilhouetteUniforms(shape, layoutDirection, size, this)
    drawRect(brush = shader.asBrush(), blendMode = BlendMode.DstIn)
}

/** Uploads the size, corner radii and smoothing that `GLASS_SDF_SOURCE` reads. */
private fun RuntimeShader.setSilhouetteUniforms(
    shape: GlassShape,
    layoutDirection: LayoutDirection,
    size: Size,
    density: Density,
) {
    val halfMin = min(size.width, size.height) * 0.5f
    val isLtr = layoutDirection == LayoutDirection.Ltr
    val start = shape.topStart.toPx(size, density)
    val end = shape.topEnd.toPx(size, density)
    val bottomEnd = shape.bottomEnd.toPx(size, density)
    val bottomStart = shape.bottomStart.toPx(size, density)

    setFloatUniform("in_size", size.width, size.height)
    setFloatUniform(
        "in_radii",
        (if (isLtr) start else end).coerceIn(0f, halfMin),
        (if (isLtr) end else start).coerceIn(0f, halfMin),
        (if (isLtr) bottomEnd else bottomStart).coerceIn(0f, halfMin),
        (if (isLtr) bottomStart else bottomEnd).coerceIn(0f, halfMin),
    )
    setFloatUniform("in_smoothing", shape.smoothing.coerceIn(0f, 1f))
}

/** Turns a light's position into the unit direction and weight the stroke shader wants. */
private fun RuntimeShader.setLightUniforms(
    directionName: String,
    colorName: String,
    light: GlassStrokeLight,
) {
    val dx = light.x - LIGHT_REF_X
    val dy = light.y - LIGHT_REF_Y
    val dz = light.z
    val length = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(1e-6f)
    setFloatUniform(directionName, dx / length, dy / length, dz / length, light.color.alpha)
    setFloatUniform(colorName, light.color.red, light.color.green, light.color.blue, 0f)
}

private val shadowShader: RuntimeShader? by lazy {
    if (isRuntimeShaderSupported()) RuntimeShader(GLASS_SHADOW_SHADER) else null
}

/**
 * Draws the shadow a surface casts, reaching past its own bounds.
 *
 * @param shape The silhouette casting the shadow.
 * @param layoutDirection Resolves the shape's start and end corners.
 * @param shadow The shadow to cast.
 * @param alpha The surface opacity, so the shadow fades with the surface it belongs to.
 */
internal fun DrawScope.drawGlassShadow(
    shape: GlassShape,
    layoutDirection: LayoutDirection,
    shadow: GlassShadow,
    alpha: Float,
) {
    val shader = shadowShader ?: return
    if (alpha <= 0f || size.width <= 0f || size.height <= 0f) return

    val sourceScale = density / GlassDefaults.SourceDensity
    val reach = (shadow.radius * sourceScale).coerceAtLeast(1f)
    val offsetX = shadow.offsetX * sourceScale
    val offsetY = shadow.offsetY * sourceScale

    shader.setSilhouetteUniforms(shape, layoutDirection, size, this)
    shader.setFloatUniform("in_shadowOffset", offsetX, offsetY)
    shader.setFloatUniform("in_shadowShape", reach, shadow.dispersion)
    val color = shadow.color
    shader.setFloatUniform(
        "in_shadowColor",
        color.red,
        color.green,
        color.blue,
        color.alpha * alpha,
    )

    drawRect(
        brush = shader.asBrush(),
        topLeft = Offset(-reach + offsetX, -reach + offsetY),
        size = Size(size.width + reach * 2f, size.height + reach * 2f),
    )
}
