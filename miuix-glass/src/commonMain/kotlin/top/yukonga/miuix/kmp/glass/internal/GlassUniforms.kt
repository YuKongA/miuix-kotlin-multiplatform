// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.LayoutDirection
import top.yukonga.miuix.kmp.blur.BackdropEffectScope
import top.yukonga.miuix.kmp.blur.runtimeShaderEffect
import top.yukonga.miuix.kmp.glass.GlassColorBlendMode
import top.yukonga.miuix.kmp.glass.GlassDefaults
import top.yukonga.miuix.kmp.glass.GlassMaterial
import top.yukonga.miuix.kmp.glass.GlassShape
import top.yukonga.miuix.kmp.glass.GlassStyle
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

/**
 * Chains the glass material onto the effect pipeline.
 *
 * @param style The material to render.
 * @param shape The silhouette. Its corner sizes and smoothing drive the shader's distance field.
 * @param alpha Opacity multiplier folded into the material's own opacity.
 * @param tint Overrides [top.yukonga.miuix.kmp.glass.GlassInner.tint] when specified. Its alpha
 *   replaces the style's tint strength.
 */
internal fun BackdropEffectScope.glassEffect(
    style: GlassStyle,
    shape: GlassShape,
    alpha: Float,
    tint: Color,
) {
    val width = size.width
    val height = size.height
    if (width <= 0f || height <= 0f) return

    val scale = downscaleFactor.toFloat()
    val paddedWidth = width + padding * 2f
    val paddedHeight = height + padding * 2f
    val textureWidth = (paddedWidth.toInt() / downscaleFactor).coerceAtLeast(1).toFloat()
    val textureHeight = (paddedHeight.toInt() / downscaleFactor).coerceAtLeast(1).toFloat()

    val halfMin = min(width, height) * 0.5f
    val isLtr = layoutDirection == LayoutDirection.Ltr
    val start = shape.topStart.toPx(size, this)
    val end = shape.topEnd.toPx(size, this)
    val bottomEnd = shape.bottomEnd.toPx(size, this)
    val bottomStart = shape.bottomStart.toPx(size, this)
    val topLeft = (if (isLtr) start else end).coerceIn(0f, halfMin) / scale
    val topRight = (if (isLtr) end else start).coerceIn(0f, halfMin) / scale
    val bottomRight = (if (isLtr) bottomEnd else bottomStart).coerceIn(0f, halfMin) / scale
    val bottomLeft = (if (isLtr) bottomStart else bottomEnd).coerceIn(0f, halfMin) / scale

    val sourceScale = density / GlassDefaults.SourceDensity
    val edgeFullPx = (style.edge.width * sourceScale).coerceAtLeast(1f)
    val tintColor = if (tint.isSpecified) tint else style.inner.tint
    val tintStrength = if (tint.isSpecified) tint.alpha else style.inner.tintStrength

    val wideReach = style.blur.big * sourceScale / scale
    val wideRadiusX = min(wideReach, textureWidth * 0.5f)
    val wideRadiusY = min(wideReach, textureHeight * 0.5f)
    val wideCollapse = (wideReach / (max(width, height) * 0.5f / scale).coerceAtLeast(1f))
        .coerceIn(0f, 1f)

    runtimeShaderEffect(GLASS_SHADER_KEY, GLASS_SHADER, "child") {
        setFloatUniform("in_size", width / scale, height / scale)
        setFloatUniform("in_pad", padding / scale, padding / scale)
        setFloatUniform("in_maxCoord", textureWidth - 0.5f, textureHeight - 0.5f)
        setFloatUniform("in_radii", topLeft, topRight, bottomRight, bottomLeft)
        setFloatUniform("in_smoothing", shape.smoothing.coerceIn(0f, 1f))
        setFloatUniform(
            "in_alphaEdge",
            (style.inner.alpha * alpha).coerceIn(0f, 1f),
            edgeFullPx / scale,
            style.edge.thickness * sourceScale / scale,
            style.edge.reflectOffset * sourceScale / scale,
        )
        setFloatUniform(
            "in_iorRefl",
            style.refract.ior,
            style.reflect.strength,
            style.reflect.lighten,
            style.inner.colorPow,
        )
        setFloatUniform("in_tint", tintColor.red, tintColor.green, tintColor.blue, tintStrength)
        setFloatUniform(
            "in_whiteMixBg",
            style.inner.colorWhite,
            style.inner.colorMix,
            style.background.saturation,
            style.background.brightness,
        )
        setFloatUniform(
            "in_darker",
            style.blend.darkerStart,
            style.blend.darkerEnd,
            style.blend.darker,
            style.inner.bottom,
        )
        setFloatUniform(
            "in_lightDir",
            style.light.directionX,
            style.light.directionY,
            style.light.directionZ,
            style.light.angleRange * PI.toFloat(),
        )
        setFloatUniform(
            "in_lightAmt",
            style.light.intensity,
            style.light.oppositeIntensity,
            style.blend.amount,
            GlassDefaults.Overspill * density / scale,
        )
        setFloatUniform(
            "in_lumCurve",
            style.blend.curveA,
            style.blend.curveB,
            style.blend.curveC,
            style.blend.curveD,
        )
        setFloatUniform(
            "in_satBri",
            style.blend.saturation,
            style.blend.brightness,
            style.background.burn,
            style.background.unShade.coerceIn(0f, 1f),
        )
        setFloatUniform(
            "in_edgePow",
            style.edge.pow.coerceAtLeast(0.01f),
            wideRadiusX,
            wideRadiusY,
            wideCollapse,
        )
    }
}

/**
 * Chains a colour treatment onto the effect pipeline.
 *
 * @param material The material whose layers to apply, nearest the backdrop first.
 * @param alpha Opacity multiplier folded into every material layer.
 */
internal fun BackdropEffectScope.colorBlendEffect(
    material: GlassMaterial,
    alpha: Float,
    key: String = GLASS_COLOR_BLEND_SHADER_KEY,
) {
    val layers = listOfNotNull(material.first, material.second, material.third)
    val surfaceAlpha = alpha.coerceIn(0f, 1f)
    runtimeShaderEffect(key, GLASS_COLOR_BLEND_SHADER, "child") {
        layers.forEachIndexed { index, layer ->
            val color = layer.color
            setFloatUniform("in_blend$index", color.red, color.green, color.blue, color.alpha * surfaceAlpha)
        }
        for (index in layers.size until 3) {
            setFloatUniform("in_blend$index", 0f, 0f, 0f, 0f)
        }
        setFloatUniform(
            "in_blendMode",
            layers[0].mode.id,
            layers.getOrNull(1)?.mode?.id ?: 0f,
            layers.getOrNull(2)?.mode?.id ?: 0f,
            layers.size.toFloat(),
        )
    }
}

/** The id the shader's blend switch reads. Keep in step with `GLASS_COLOR_BLEND_SHADER`. */
private val GlassColorBlendMode.id: Float
    get() = when (this) {
        GlassColorBlendMode.SrcOver -> 0f
        GlassColorBlendMode.PlusDarker -> 1f
        GlassColorBlendMode.PlusLighter -> 2f
        GlassColorBlendMode.SoftLight -> 3f
        GlassColorBlendMode.HardLight -> 4f
        GlassColorBlendMode.Overlay -> 5f
        GlassColorBlendMode.Luminosity -> 6f
        GlassColorBlendMode.ColorDodge -> 7f
        GlassColorBlendMode.ColorBurn -> 8f
    }
