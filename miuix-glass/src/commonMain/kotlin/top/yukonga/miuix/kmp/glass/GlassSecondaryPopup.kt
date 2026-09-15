// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.first
import top.yukonga.miuix.kmp.blur.Backdrop

/**
 * OS4 secondary menu grown from a primary menu row, not an edge-revealed ordinary popup.
 *
 * The row plus [contentPadding] is the initial clip rectangle. All four edges travel to the
 * measured menu bounds; rows remain full-size and move with the clip's top-left corner. The
 * panel does not fade or blur its rows during expansion and collapse; its glass material is
 * unchanged. Its start edge aligns to the row
 * (mirrored in RTL), and insufficient space below shifts it up rather than reversing the reveal.
 * Rows accept input during opening; requesting dismissal disables them immediately.
 * Predictive Back previews collapse, restores the panel on cancellation, and requests dismissal
 * on completion. A shared [materialAnchor] also drives the primary panel's scale and mask.
 *
 * @param show Whether the secondary menu is expanded. Keep this call composed during collapse.
 * @param onDismissRequest Called by a tap outside or Back to return to the primary menu.
 * @param anchorBounds Resting bounds of the trigger row, in the same root as this popup. Freeze
 *   these before setting the primary [GlassTransformPopup]'s `stacked` flag.
 * @param backdrop Background sampled behind this menu. Include the primary popup in this layer
 *   so its rendered content is blurred underneath the secondary. Null uses the anchor backdrop.
 * @param modifier Modifier applied to the panel.
 * @param materialAnchor The primary transform popup's button anchor. Passing the same anchor
 *   shares its blur, colour treatment and bloom stroke across both menu levels; an explicit
 *   [backdrop] takes precedence over the button backdrop.
 * @param sizing Panel limits. Set minWidth to the primary panel's measured width.
 * @param visuals Surface appearance, also used when no anchor surface is available.
 * @param cornerRadius Rounded clip radius throughout the transition.
 * @param contentPadding Insets around the menu rows, also included around the collapsed row.
 * @param onDismissFinished Called when the collapse animation has finished.
 * @param content Header row followed by secondary choices. The header should request collapse.
 */
@Composable
fun BoxScope.GlassSecondaryPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    materialAnchor: GlassPopupAnchor? = null,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    cornerRadius: Dp = GlassPopupDefaults.CornerRadius,
    contentPadding: PaddingValues = PaddingValues(vertical = GlassPopupDefaults.ContentPaddingVertical),
    onDismissFinished: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = show
    val transition = rememberTransition(state, label = "glassSecondaryPopup")
    val progress = transition.animateFloat(
        transitionSpec = { GlassMotion.secondaryPopup(targetState) },
        label = "glassSecondaryBounds",
    ) { if (it) 1f else 0f }
    val active = show || state.currentState || !state.isIdle
    val backProgress = rememberGlassPopupBackProgress(
        show = show,
        active = active,
        enabled = show,
        retainWhenInactive = false,
        resetSpec = GlassMotion.secondaryPopup(true),
        onDismissRequest = onDismissRequest,
    )
    DisposableEffect(materialAnchor, backProgress) {
        materialAnchor?.secondaryBackProgressState = backProgress
        onDispose {
            if (materialAnchor?.secondaryBackProgressState === backProgress) {
                materialAnchor.secondaryBackProgressState = null
            }
        }
    }
    val onFinished by rememberUpdatedState(onDismissFinished)
    var hasOpened by remember { mutableStateOf(false) }
    LaunchedEffect(show) {
        if (show) {
            hasOpened = true
        } else if (hasOpened) {
            snapshotFlow { state.isIdle }.first { it }
            hasOpened = false
            onFinished?.invoke()
        }
    }
    if (!active) return
    val direction = LocalLayoutDirection.current
    val source = materialAnchor?.surface
    val resolvedVisuals = if (source != null) {
        visuals.copy(style = source.style, material = source.material, stroke = source.stroke, containerColor = source.fill)
    } else {
        visuals
    }
    GlassPopupSurface(
        onDismissRequest = onDismissRequest,
        backdrop = backdrop ?: source?.backdrop,
        modifier = modifier,
        sizing = sizing,
        visuals = resolvedVisuals,
        interactive = show,
        scrollable = true,
        underlayMaterial = source?.underlayMaterial,
        contentPadding = contentPadding,
        panelLayer = {},
        overlay = {},
        frame = { end, page ->
            val settled = placeGlassSecondaryPopup(anchorBounds, end, sizing.safeMargin.toPx(), page, direction)
            GlassPopupFrame(
                secondaryPopupRect(
                    anchorBounds,
                    settled,
                    contentPadding.calculateTopPadding().toPx(),
                    contentPadding.calculateBottomPadding().toPx(),
                    popupFractionWithBack(progress.value, backProgress.value),
                ),
                cornerRadius,
            )
        },
        contentLayer = { _, _ -> },
        content = content,
    )
}

/** SecondaryPopupWindowStrategy: start-edge alignment, then shift upward to fit. */
internal fun placeGlassSecondaryPopup(anchor: Rect, size: Size, margin: Float, page: Size, direction: LayoutDirection): Rect {
    val width = size.width.coerceAtMost((page.width - margin * 2f).coerceAtLeast(0f))
    val height = size.height.coerceAtMost((page.height - margin * 2f).coerceAtLeast(0f))
    val x = if (direction == LayoutDirection.Ltr) anchor.left else anchor.right - width
    val left = x.coerceIn(margin, (page.width - margin - width).coerceAtLeast(margin))
    val top = anchor.top.coerceIn(margin, (page.height - margin - height).coerceAtLeast(margin))
    return Rect(left, top, left + width, top + height)
}

/** Full-size content moves with a rounded clip grown from the padded trigger row. */
internal fun secondaryPopupRect(anchor: Rect, end: Rect, topPadding: Float, bottomPadding: Float, fraction: Float): Rect {
    val t = fraction.coerceIn(0f, 1f)
    return Rect(
        lerp(anchor.left, end.left, t),
        lerp(anchor.top - topPadding, end.top, t),
        lerp(anchor.right, end.right, t),
        lerp(anchor.bottom + bottomPadding, end.bottom, t),
    )
}
