// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop

/** Default values for [GlassDropdownPopup]. */
object GlassDropdownDefaults {

    /** Corner radius the panel settles at: `miuix_appcompat_drop_down_menu_radius`. */
    val CornerRadius: Dp = 24.dp

    /** Corner radius it opens from. */
    val StartCornerRadius: Dp = 4.dp
}

/**
 * A list of choices dropped from a settings row, on glass.
 *
 * The third of the source system's three openings, and the only one that holds no edge still.
 * [GlassPopup] pins its far corner and lets the panel grow out of it; [GlassTransformPopup] hands
 * the control's own rectangle over to the panel. This one throws the panel along an arc: its size
 * and its centre run on two springs of different speeds, so opening, a flat capsule reaches where
 * the panel belongs before the panel has finished growing and its far edges pull in and rebound.
 * Closing, the two swap, and the panel blurs itself away rather than simply shrinking.
 *
 * @param show Whether the panel is open.
 * @param onDismissRequest Called when a tap outside should close it.
 * @param anchorBounds The row's bounds, in this composable's own coordinate space.
 * @param backdrop The [Backdrop] behind the glass. `null` uses an opaque fill while retaining the
 *   configured bloom stroke and Compose shadow.
 * @param modifier The modifier applied to the panel.
 * @param anchor Optional handle for the row. Given one, the panel stands the row's displayed value
 *   down while it is open — see [glassPopupAnchorValue].
 * @param sizing How wide and tall the panel may be.
 * @param visuals What its surface is made of.
 * @param cornerRadius Corner radius the panel settles at.
 * @param contentPadding Padding around the rows.
 * @param content The rows. [GlassPopupItem] with `selected` gives the source's own tick.
 */
@Composable
fun BoxScope.GlassDropdownPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    anchor: GlassPopupAnchor? = null,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    cornerRadius: Dp = GlassDropdownDefaults.CornerRadius,
    contentPadding: PaddingValues = PaddingValues(vertical = GlassPopupDefaults.ContentPaddingVertical),
    content: @Composable ColumnScope.() -> Unit,
) {
    val size by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.arcBounds(show),
        label = "glassDropdownSize",
    )
    val position by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.arcPosition(show),
        label = "glassDropdownPosition",
    )
    val fade by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.arcFade(show),
        label = "glassDropdownFade",
    )
    val sharpness by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.arcBlur(show),
        label = "glassDropdownBlur",
    )

    val active = show || size > 0.0001f
    val layoutDirection = LocalLayoutDirection.current
    val backProgress = rememberGlassPopupBackProgress(
        show = show,
        active = active,
        enabled = show,
        retainWhenInactive = true,
        resetSpec = GlassMotion.arcBounds(true),
        onDismissRequest = onDismissRequest,
    )
    DisposableEffect(anchor, backProgress) {
        anchor?.dropdownBackProgressState = backProgress
        onDispose {
            if (anchor?.dropdownBackProgressState === backProgress) {
                anchor.dropdownBackProgressState = null
            }
        }
    }
    fun geometryProgress() = popupFractionWithBack(size, backProgress.value)
    fun positionProgress() = popupFractionWithBack(position, backProgress.value)
    fun fadeProgress() = fade * (1f - backProgress.value.coerceIn(0f, 1f))
    fun sharpnessProgress() = sharpness * (1f - backProgress.value.coerceIn(0f, 1f))
    val standingDown = active
    DisposableEffect(anchor, standingDown) {
        anchor?.contentHidden = standingDown
        onDispose { anchor?.contentHidden = false }
    }
    if (!standingDown) return

    val startRadius = GlassDropdownDefaults.StartCornerRadius
    GlassPopupSurface(
        onDismissRequest = onDismissRequest,
        backdrop = backdrop,
        modifier = modifier,
        sizing = sizing,
        visuals = visuals,
        contentPadding = contentPadding,
        panelLayer = {
            alpha = if (show && geometryProgress() <= GlassMotion.ARC_VISIBLE_FRACTION) 0f else fadeProgress()
            val blur = (1f - sharpnessProgress()) * GlassMotion.ARC_EXIT_BLUR_DP * this.density
            renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
        },
        overlay = {},
        frame = { end, page ->
            arcFrame(
                anchorBounds,
                end,
                page,
                sizing.safeMargin.toPx(),
                geometryProgress(),
                positionProgress(),
                startRadius,
                cornerRadius,
                layoutDirection,
            )
        },
        contentLayer = { _, _ ->
            val fraction = GlassMotion.ARC_START_WIDTH + (1f - GlassMotion.ARC_START_WIDTH) * geometryProgress()
            val scale = fraction.coerceAtMost(1f)
            scaleX = scale
            scaleY = scale
        },
        scrollable = true,
        content = content,
    )
}

/**
 * The panel's rectangle partway along the arc.
 *
 * Two fractions drive it, not one: the width and the aspect ratio follow [sizeFraction], while the
 * centre follows the quicker [positionFraction]. The capsule it opens from is 69% of the panel's
 * width and a fifth of its own width tall, tucked against the panel's own far edges.
 */
private fun arcFrame(
    anchor: Rect,
    end: Size,
    page: Size,
    margin: Float,
    sizeFraction: Float,
    positionFraction: Float,
    startRadius: Dp,
    endRadius: Dp,
    layoutDirection: LayoutDirection,
): GlassPopupFrame {
    val placement = placeGlassPopup(anchor, end, margin, page, layoutDirection)
    val startWidth = end.width * GlassMotion.ARC_START_WIDTH
    val startHeight = startWidth * GlassMotion.ARC_START_RATIO
    val width = startWidth + (end.width - startWidth) * sizeFraction
    val endRatio = if (end.width > 0f) end.height / end.width else 1f
    val ratio = GlassMotion.ARC_START_RATIO + (endRatio - GlassMotion.ARC_START_RATIO) * sizeFraction
    val height = ratio * width

    val startCenterX = if (layoutDirection == LayoutDirection.Ltr) {
        placement.rect.right - startWidth / 2f
    } else {
        placement.rect.left + startWidth / 2f
    }
    val startCenterY = if (placement.alignTop) {
        placement.rect.top + startHeight / 2f
    } else {
        placement.rect.bottom - startHeight / 2f
    }
    val centerX = startCenterX + (placement.rect.center.x - startCenterX) * positionFraction
    val centerY = startCenterY + (placement.rect.center.y - startCenterY) * positionFraction
    val radius = startRadius + (endRadius - startRadius) * sizeFraction
    return GlassPopupFrame(
        rect = Rect(
            left = centerX - width / 2f,
            top = centerY - height / 2f,
            right = centerX + width / 2f,
            bottom = centerY + height / 2f,
        ),
        cornerRadius = radius,
    )
}
