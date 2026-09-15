// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass.internal

import kotlin.math.sqrt

/**
 * Corner tile size as a multiple of the corner radius. The silhouette leaves the straight edge
 * this far from the box corner, which is what makes the corner continuous instead of circular.
 */
internal const val TILE_SCALE = 1.5286649465560913f

internal const val FIT_4 = -0.7391197269f
internal const val FIT_3 = 2.4034927648f
internal const val FIT_2 = -2.4907319173f
internal const val FIT_1 = 0.4768708960f
internal const val FIT_0 = 0.4747847594f

/** Below this radius the corner is a plain rounded box; the tile maths would divide by zero. */
internal const val MIN_SUPERCIRCLE_RADIUS = 0.5f

/**
 * The quintic smoothing kernel the rim profile is built from. It maps `[0, 1]` onto `[0, 1]` with
 * a zero first and second derivative at both ends, so the rim never shows a crease.
 */
internal fun smooth5Map(t: Float): Float {
    val x = (0.5f + 0.5f * t).coerceIn(0f, 1f)
    val s = x * x * x * (x * (x * 6f - 15f) + 10f)
    return (s - 0.5f) * 2f
}

/**
 * The rim-depth value the surface reaches [band] pixels inside the silhouette. The shader fades
 * coverage up to this value, which keeps the anti-aliased band a fixed width in screen pixels
 * whatever the rim width is.
 *
 * @param band Width of the anti-aliased band, in full-resolution pixels.
 * @param edgeFullPx Rim width, in full-resolution pixels.
 */
internal fun coverageThreshold(band: Float, edgeFullPx: Float): Float {
    val ratio = (band / if (edgeFullPx > 1f) edgeFullPx else 1f).coerceIn(0f, 1f)
    return smooth5Map(sqrt(ratio))
}
