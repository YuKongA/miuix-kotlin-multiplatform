// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * The shadow a glass surface casts.
 *
 * @property color The shadow's colour. Its alpha is the strength at full opacity.
 * @property offsetX Horizontal displacement.
 * @property offsetY Vertical displacement. Small and positive: the light is above.
 * @property radius How far the shadow reaches past the silhouette.
 * @property dispersion Shape of the falloff. Below 0.5 the shadow hugs the edge; above it, the
 *   darkening spreads further out and thins.
 */
@Immutable
data class GlassShadow(
    val color: Color,
    val offsetX: Float,
    val offsetY: Float,
    val radius: Float,
    val dispersion: Float,
)

/** The stock shadows, ported from the source system's `ShadowToken`. */
object GlassShadows {

    private val Black = Color(0f, 0f, 0f, 0.05f)

    /** Port of `ShadowToken.Low`. A surface resting on the page. */
    @Stable
    val Low: GlassShadow = GlassShadow(Black, 0f, 0f, 44f, 0.5f)

    /** Port of `ShadowToken.Regular`. The everyday panel. */
    @Stable
    val Regular: GlassShadow = GlassShadow(Black, 0f, 2f, 64f, 0.5f)

    /** Port of `ShadowToken.High`. A surface lifted clear of the page. */
    @Stable
    val High: GlassShadow = GlassShadow(Black, 0f, 4f, 80f, 0.5f)

    /** Port of `ShadowToken.ExtraHigh`. A dialog over a dimmed page. */
    @Stable
    val ExtraHigh: GlassShadow = GlassShadow(Black, 0f, 70f, 96f, 0.5f)

    /** Port of `ShadowToken.Float`. A floating control. */
    @Stable
    val Float: GlassShadow = GlassShadow(Black, 0f, 0f, 24f, 0.5f)
}
