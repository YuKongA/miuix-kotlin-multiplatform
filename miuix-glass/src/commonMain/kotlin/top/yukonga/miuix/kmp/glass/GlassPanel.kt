// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.internal.drawGlassStroke

/**
 * Makes this composable a sheet of glass over [backdrop], with the shadow it casts.
 *
 * The pair belongs together — a panel without its shadow reads as painted on rather than as lying
 * over — and every glass surface in this module is built from it. A device without runtime shaders
 * has no backdrop to sample, so [fallback] says what the surface is instead.
 *
 * @param backdrop The [Backdrop] behind the glass. `null` uses [fallback].
 * @param shape The silhouette. It drives the shadow, the clip and the shader alike.
 * @param style The material.
 * @param alpha Opacity multiplier for the whole surface, shadow included.
 * @param material The panel's own body. `null` leaves the blurred backdrop as the whole of it.
 * @param stroke Optional bloom stroke along the rim.
 * @param shadow The shadow. `null` removes it.
 * @param shading Whether the surface refracts and shades what it stands on. A bar or a menu is a
 *   material and wants `false`; a control is glass and wants `true`.
 * @param fallback What the surface is when there is no backdrop. The default clips to [shape] and
 *   leaves whatever the caller paints next as the body.
 */
fun Modifier.glassPanel(
    backdrop: Backdrop?,
    shape: GlassShape,
    style: GlassStyle = GlassDefaults.Style,
    alpha: Float = 1f,
    material: GlassMaterial? = null,
    stroke: GlassStroke? = null,
    shadow: GlassShadow? = GlassShadows.Regular,
    shading: Boolean = true,
    fallback: Modifier = Modifier.clip(shape),
): Modifier = if (backdrop == null) {
    this
        .graphicsLayer { this.alpha = alpha }
        .glassShadow(shape, shadow)
        .then(fallback)
        .drawWithContent {
            drawContent()
            stroke?.let { drawGlassStroke(shape, layoutDirection, it, 1f) }
        }
} else {
    this
        .glassShadow(shape, shadow, alpha)
        .glass(
            backdrop = backdrop,
            shape = shape,
            style = style,
            alpha = alpha,
            material = material,
            stroke = stroke,
            shading = shading,
        )
}
