// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.BlurDefaults

/** Default values for the glass material. */
object GlassDefaults {

    /** Default corner radius of a glass surface. */
    val CornerRadius: Dp = 24.dp

    /** Default corner smoothing. 1 draws the continuous corner the source effect uses. */
    val Smoothing: Float = 1f

    /**
     * Display density the source design tokens were authored at. [GlassEdge] and [GlassBlur] hold
     * pixels at this density; `Modifier.glass` rescales them by `density / SourceDensity` so a rim
     * keeps the same physical size on any screen.
     */
    val SourceDensity: Float = 3f

    /** Default noise dithering coefficient for the backdrop blur. 0 disables noise. */
    val NoiseCoefficient: Float = BlurDefaults.NoiseCoefficient

    /**
     * How far the material is drawn past its silhouette, in dp. A full-resolution mask pass trims
     * the edge afterwards, so the material only has to reach beyond it — far enough to cover the
     * widest downscale the backdrop blur picks.
     */
    val Overspill: Float = 3f

    /** Default material — the everyday light-theme card. */
    @Stable
    val Style: GlassStyle = GlassStyles.CommonMediumRegularLowLight

    /**
     * The everyday card material for the current theme.
     *
     * @param isDark True to return the dark-theme material.
     */
    @Stable
    fun style(isDark: Boolean): GlassStyle = GlassStyles.forTheme(
        isDark = isDark,
        light = GlassStyles.CommonMediumRegularLowLight,
        dark = GlassStyles.CommonMediumRegularDark,
    )
}
