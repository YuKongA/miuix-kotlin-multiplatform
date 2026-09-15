// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * The silhouette of a glass surface.
 *
 * @param topStart Corner size at the top-start corner.
 * @param topEnd Corner size at the top-end corner.
 * @param bottomEnd Corner size at the bottom-end corner.
 * @param bottomStart Corner size at the bottom-start corner.
 * @property smoothing 0 draws a circular corner, 1 draws the continuous corner. Values between the
 *   two interpolate. Only the glass shaders read it — the mask, the stroke and the rim. [Modifier
 *   .clip] and anything else built on [createOutline] get a plain rounded rectangle, because the
 *   supercircle the shaders trace is a distance field with no exact path form. Draw a smoothed
 *   surface through [Modifier.glass], or through the same mask pass it runs.
 */
@Immutable
class GlassShape(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize,
    val smoothing: Float = 1f,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        val rect = Rect(0f, 0f, size.width, size.height)
        if (topStart + topEnd + bottomEnd + bottomStart == 0f) return Outline.Rectangle(rect)
        val isLtr = layoutDirection == LayoutDirection.Ltr
        return Outline.Rounded(
            RoundRect(
                rect = rect,
                topLeft = CornerRadius(if (isLtr) topStart else topEnd),
                topRight = CornerRadius(if (isLtr) topEnd else topStart),
                bottomRight = CornerRadius(if (isLtr) bottomEnd else bottomStart),
                bottomLeft = CornerRadius(if (isLtr) bottomStart else bottomEnd),
            ),
        )
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): GlassShape = GlassShape(topStart, topEnd, bottomEnd, bottomStart, smoothing)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GlassShape) return false
        return topStart == other.topStart &&
            topEnd == other.topEnd &&
            bottomEnd == other.bottomEnd &&
            bottomStart == other.bottomStart &&
            smoothing == other.smoothing
    }

    override fun hashCode(): Int {
        var result = topStart.hashCode()
        result = 31 * result + topEnd.hashCode()
        result = 31 * result + bottomEnd.hashCode()
        result = 31 * result + bottomStart.hashCode()
        result = 31 * result + smoothing.hashCode()
        return result
    }

    override fun toString(): String = "GlassShape(topStart=$topStart, topEnd=$topEnd, bottomEnd=$bottomEnd, " +
        "bottomStart=$bottomStart, smoothing=$smoothing)"
}

/**
 * A [GlassShape] with the same radius on all four corners.
 *
 * @param cornerRadius The radius applied to every corner.
 * @param smoothing 0 draws a circular corner, 1 draws the continuous corner.
 */
fun GlassShape(cornerRadius: Dp, smoothing: Float = GlassDefaults.Smoothing): GlassShape = GlassShape(
    CornerSize(cornerRadius),
    CornerSize(cornerRadius),
    CornerSize(cornerRadius),
    CornerSize(cornerRadius),
    smoothing,
)

/**
 * A [GlassShape] with a radius per corner. The order matches `RoundedCornerShape`.
 *
 * @param topStart The radius of the top-start corner.
 * @param topEnd The radius of the top-end corner.
 * @param bottomEnd The radius of the bottom-end corner.
 * @param bottomStart The radius of the bottom-start corner.
 * @param smoothing 0 draws a circular corner, 1 draws the continuous corner.
 */
fun GlassShape(
    topStart: Dp,
    topEnd: Dp,
    bottomEnd: Dp,
    bottomStart: Dp,
    smoothing: Float = GlassDefaults.Smoothing,
): GlassShape = GlassShape(
    CornerSize(topStart),
    CornerSize(topEnd),
    CornerSize(bottomEnd),
    CornerSize(bottomStart),
    smoothing,
)
