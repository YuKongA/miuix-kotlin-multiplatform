// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

/** Unscaled drag target bounds, in pixels, before the indicator's existing press transform. */
internal data class NavigationDragTarget(
    val left: Float,
    val right: Float,
    val following: Boolean,
    val movingRight: Boolean,
) {
    val leftSpring: SpringSpec<Float>
        get() = if (following) GlassMotion.navDragFollow() else GlassMotion.edgeSpring(!movingRight)

    val rightSpring: SpringSpec<Float>
        get() = if (following) GlassMotion.navDragFollow() else GlassMotion.edgeSpring(movingRight)
}

/**
 * Mirrors OverlayView's same-item drag trail and cross-item edge-spring targets.
 * The 60px trail cap is deliberately not density-scaled. Raw spring targets may extend past the
 * container; [navigationIndicatorBounds] limits the rendered edges, just like the native property
 * setters. Stretch is suppressed when the leading target reaches the container boundary.
 */
internal fun navigationDragTarget(
    left: Float,
    width: Float,
    containerWidth: Float,
    delta: Float,
    changedItem: Boolean,
): NavigationDragTarget {
    val start = left.coerceIn(0f, (containerWidth - width).coerceAtLeast(0f))
    val end = start + width
    val trail = if (changedItem) 0f else (abs(delta) * 4f).coerceAtMost(60f)
    return NavigationDragTarget(
        left = if (delta > 0f && end < containerWidth) start - trail else start,
        right = if (delta < 0f && start > 0f) end + trail else end,
        following = !changedItem,
        movingRight = delta > 0f,
    )
}

/** Limits each rendered edge to the outermost items without stopping the underlying spring. */
internal fun navigationIndicatorBounds(left: Float, right: Float, containerWidth: Float, inset: Float): Offset {
    val min = inset.coerceIn(0f, containerWidth.coerceAtLeast(0f) / 2f)
    val max = (containerWidth - min).coerceAtLeast(min)
    val start = left.coerceIn(min, max)
    return Offset(start, right.coerceIn(start, max))
}
