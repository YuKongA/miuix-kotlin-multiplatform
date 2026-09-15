// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.blur.Backdrop

/**
 * A sheet of glass, in the form the library's own components ask for their surface in.
 *
 * The components already know how to lay a menu out, stack a second one over it, dim the one behind
 * and morph between them. What they do not know is what their panel is made of, so they ask for it.
 * Handing them this is the whole of what makes them glass — there is no second implementation.
 *
 * ```
 * OverlayIconCascadingDropdownMenu(
 *     entries = entries,
 *     surface = glassSurface(backdrop, GlassPopupDefaults.visuals()),
 * ) { Icon(…) }
 * ```
 *
 * @param backdrop The [Backdrop] supplying the content behind the glass. `null` falls back to the
 *   flat fill in [visuals], which is what a device without runtime shaders gets.
 * @param visuals What the surface is made of.
 * @param cornerRadius Corner radius used when the host supplies a [Shape] that cannot be safely
 *   converted to a [GlassShape]. Rounded corner shapes retain all four of their supplied corners.
 */
fun glassSurface(
    backdrop: Backdrop?,
    visuals: GlassPopupVisuals,
    cornerRadius: Dp = GlassPopupDefaults.CornerRadius,
): @Composable (Shape) -> Modifier = { providedShape ->
    Modifier.glassPanel(
        backdrop = backdrop,
        shape = resolveGlassSurfaceShape(providedShape, cornerRadius),
        style = visuals.style,
        alpha = visuals.alpha,
        material = visuals.material,
        stroke = visuals.stroke,
        shadow = visuals.shadow,
        shading = false,
        fallback = Modifier.clip(providedShape).background(visuals.containerColor),
    )
}

/** Keeps the host's rounded silhouette in the shader, stroke and shadow passes. */
internal fun resolveGlassSurfaceShape(providedShape: Shape, fallbackCornerRadius: Dp): GlassShape = when (providedShape) {
    is GlassShape -> providedShape

    is RoundedCornerShape -> GlassShape(
        topStart = providedShape.topStart,
        topEnd = providedShape.topEnd,
        bottomEnd = providedShape.bottomEnd,
        bottomStart = providedShape.bottomStart,
    )

    else -> GlassShape(fallbackCornerRadius)
}
