// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** How one layer of a [GlassMaterial] combines with the backdrop beneath it. */
enum class GlassColorBlendMode {

    /** The layer replaces the backdrop, in proportion to its own alpha. */
    SrcOver,

    /** Subtractive darkening: `backdrop - layerAlpha * (1 - layer)`, clamped at black. */
    PlusDarker,

    /** Additive lightening: `backdrop + layerAlpha * layer`, clamped at white. */
    PlusLighter,

    /** A gentle dodge and burn. Never reaches pure black or pure white. */
    SoftLight,

    /** [Overlay] with the layer and the backdrop swapped. The layer decides the contrast. */
    HardLight,

    /** Multiplies dark backdrops and screens light ones. The backdrop decides the contrast. */
    Overlay,

    /** Keeps the backdrop's hue and saturation, and takes the layer's luminance. */
    Luminosity,

    /** Brightens the backdrop toward white, driven by the layer. */
    ColorDodge,

    /** Darkens the backdrop toward black, driven by the layer. */
    ColorBurn,
}

/**
 * One colour layer painted over the blurred backdrop.
 *
 * @property color The layer's colour. Its alpha is how far the blend carries.
 * @property mode How it combines with everything already under it.
 */
@Immutable
data class GlassColorLayer(
    val color: Color,
    val mode: GlassColorBlendMode,
)

/**
 * A blur radius and the colour treatment that goes over it — the panel's own body.
 *
 * @property blurRadius How far the backdrop blurs before the layers go on. It replaces the blur
 *   the [GlassStyle] would otherwise ask for.
 * @property first The layer nearest the backdrop. Always present.
 * @property second The layer over [first]. `null` ends the stack.
 * @property third The layer over [second]. `null` ends the stack.
 */
@Immutable
data class GlassMaterial(
    val blurRadius: Dp,
    val first: GlassColorLayer,
    val second: GlassColorLayer? = null,
    val third: GlassColorLayer? = null,
) {
    init {
        require(second != null || third == null) {
            "A GlassMaterial cannot carry a third colour layer without a second"
        }
    }
}
