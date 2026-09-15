// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.noiseDither
import top.yukonga.miuix.kmp.glass.internal.GLASS_COLOR_BLEND_SHADER_KEY
import top.yukonga.miuix.kmp.glass.internal.colorBlendEffect
import top.yukonga.miuix.kmp.glass.internal.drawGlassMask
import top.yukonga.miuix.kmp.glass.internal.drawGlassRim
import top.yukonga.miuix.kmp.glass.internal.drawGlassShadow
import top.yukonga.miuix.kmp.glass.internal.drawGlassStroke
import top.yukonga.miuix.kmp.glass.internal.glassEffect

/**
 * Renders this composable as a sheet of glass over [backdrop].
 *
 * @param backdrop The [Backdrop] supplying the content behind the glass. This composable must sit
 *   *outside* the subtree that backdrop records. Inside it the two form a cycle — the layer records
 *   a surface that draws the layer — and the render thread walks it until it runs out of stack.
 * @param shape The silhouette. It drives the layer clip, the [stroke] and the shader, so all
 *   three agree.
 * @param style The material. Pick one from [GlassStyles] or start from [GlassDefaults.style].
 * @param alpha Opacity multiplier folded into the material's colour layers, shading and stroke.
 *   The backdrop blur stays allocated while [enabled] is true so an animated alpha does not switch
 *   render paths halfway through its transition.
 * @param tint Overrides [GlassInner.tint]. Its alpha replaces the style's tint strength.
 *   [Color.Unspecified], the default, keeps the style's own tint.
 * @param material The panel's own body: a blur radius and the colour layers that go over it. It
 *   overrides the blur radius [style] asks for. `null` leaves the surface at whatever the blurred
 *   backdrop is, which reads as a hole over a dark page rather than as a panel.
 * @param noiseCoefficient Noise dithering coefficient for the backdrop blur, which prevents
 *   banding across large flat areas. 0 disables it.
 * @param stroke Optional bloom stroke traced along the rim. `null` skips it.
 * @param contentBlendMode How this composable's content composites over the glass.
 * @param enabled Whether the material is active. When false the content draws on its own.
 * @param shading Whether the surface is glass on top of its material. The source system declares a
 *   surface one way or the other and never both: a bar or a menu is a material token — a blur, the
 *   colour layers over it, a rim and a shadow — while a control is a glass token that refracts and
 *   shades what it stands on. `false` leaves the colour layers as the whole body, which is what a
 *   menu wants; the style's own tint is a shading value and would otherwise grey the panel.
 */
fun Modifier.glass(
    backdrop: Backdrop,
    shape: GlassShape,
    style: GlassStyle = GlassDefaults.Style,
    alpha: Float = 1f,
    tint: Color = Color.Unspecified,
    material: GlassMaterial? = null,
    noiseCoefficient: Float = GlassDefaults.NoiseCoefficient,
    stroke: GlassStroke? = null,
    contentBlendMode: BlendMode = BlendMode.SrcOver,
    enabled: Boolean = true,
    shading: Boolean = true,
): Modifier = glassImpl(
    backdrop = backdrop,
    shape = shape,
    style = style,
    alpha = alpha,
    tint = tint,
    material = material,
    underlayMaterial = null,
    noiseCoefficient = noiseCoefficient,
    stroke = stroke,
    contentBlendMode = contentBlendMode,
    enabled = enabled,
    shading = shading,
)

/** Applies a child material over the OS4 action bar's already blurred and colour-treated surface. */
internal fun Modifier.glassOnActionBar(
    backdrop: Backdrop,
    shape: GlassShape,
    style: GlassStyle,
    alpha: Float,
    material: GlassMaterial?,
    underlayMaterial: GlassMaterial,
    stroke: GlassStroke?,
    enabled: Boolean = true,
): Modifier = glassImpl(
    backdrop = backdrop,
    shape = shape,
    style = style,
    alpha = alpha,
    tint = Color.Unspecified,
    material = material,
    underlayMaterial = underlayMaterial,
    noiseCoefficient = GlassDefaults.NoiseCoefficient,
    stroke = stroke,
    contentBlendMode = BlendMode.SrcOver,
    enabled = enabled,
    shading = false,
)

private fun Modifier.glassImpl(
    backdrop: Backdrop,
    shape: GlassShape,
    style: GlassStyle,
    alpha: Float,
    tint: Color,
    material: GlassMaterial?,
    underlayMaterial: GlassMaterial?,
    noiseCoefficient: Float,
    stroke: GlassStroke?,
    contentBlendMode: BlendMode,
    enabled: Boolean,
    shading: Boolean,
): Modifier = this.drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = {
        noiseDither(noiseCoefficient)
        val radius = material?.blurRadius?.value ?: (style.blur.small / GlassDefaults.SourceDensity)
        blur(radius.coerceIn(0f, BlurDefaults.MaxBlurRadius) * density)
        if (underlayMaterial != null) {
            colorBlendEffect(underlayMaterial, 1f, "${GLASS_COLOR_BLEND_SHADER_KEY}ActionBar")
        }
        if (material != null) colorBlendEffect(material, alpha)
        if (shading) glassEffect(style, shape, alpha, tint)
    },
    onDrawFront = {
        if (stroke != null) drawGlassStroke(shape, layoutDirection, stroke, alpha)
        drawGlassMask(shape, layoutDirection)
        if (shading) {
            drawGlassRim(shape, layoutDirection, style, if (tint.isSpecified) tint else style.inner.tint, alpha)
        }
    },
    contentBlendMode = contentBlendMode,
    enabled = enabled,
)

/**
 * Casts [shadow] under this composable, in the shape of [shape].
 *
 * @param shape The silhouette casting the shadow. Use the same one [glass] gets.
 * @param shadow The shadow. Pick one from [GlassShadows]. `null` skips it.
 * @param alpha Opacity multiplier, to fade the shadow with the surface it belongs to.
 */
fun Modifier.glassShadow(
    shape: GlassShape,
    shadow: GlassShadow? = GlassShadows.Regular,
    alpha: Float = 1f,
): Modifier = if (shadow == null) {
    this
} else {
    this.drawBehind { drawGlassShadow(shape, layoutDirection, shadow, alpha) }
}
