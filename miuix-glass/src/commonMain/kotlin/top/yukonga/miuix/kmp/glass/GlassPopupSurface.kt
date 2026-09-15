// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.internal.drawGlassStroke
import kotlin.math.roundToInt

/**
 * How wide and tall a menu may be, and how close to the page's edge it may come.
 *
 * @property minWidth Narrowest the panel may be.
 * @property maxWidth Widest it may be.
 * @property maxHeight Tallest it may be, before the list inside scrolls.
 * @property safeMargin Gap the panel keeps from the edge of the page.
 */
@Immutable
data class GlassPopupSizing(
    val minWidth: Dp = GlassPopupDefaults.MinWidth,
    val maxWidth: Dp = GlassPopupDefaults.MaxWidth,
    val maxHeight: Dp = GlassPopupDefaults.MaxHeight,
    val safeMargin: Dp = GlassPopupDefaults.SafeMargin,
)

/**
 * What a menu's surface is made of.
 *
 * @property style The glass style. Only its blur and rim are consulted — a menu is a material and
 *   not glass, so its shading is left off.
 * @property alpha Opacity multiplier for the material.
 * @property stroke Optional bloom stroke along the rim.
 * @property shadow The shadow the panel casts. `null` removes it.
 * @property material The panel's own body.
 * @property containerColor Opaque fill used when there is no backdrop; stroke and shadow remain.
 */
@Immutable
data class GlassPopupVisuals(
    val style: GlassStyle,
    val alpha: Float,
    val stroke: GlassStroke?,
    val shadow: GlassShadow?,
    val material: GlassMaterial?,
    val containerColor: Color,
)

/** The rectangle a menu's panel occupies at one moment, and the corner radius it wears there. */
@Immutable
data class GlassPopupFrame(
    val rect: Rect,
    val cornerRadius: Dp,
)

/** Where a menu settles, and how far it has to move to stay on the page. */
@Immutable
class GlassPopupPlacement internal constructor(
    val rect: Rect,
    /** Whether the panel sits below the anchor. `false` means it had to open upward instead. */
    val alignTop: Boolean,
)

/**
 * Works out where a menu belongs: two of its edges on the anchor's, clamped to the page.
 *
 * An ordinary menu puts its end edge on the anchor's. The top edges meet unless there is no
 * room below and more above. Secondary menus use [placeGlassSecondaryPopup] instead: start-edge
 * alignment and upward shifting without flipping the opening direction.
 *
 * @param anchor The control or row the menu belongs to.
 * @param size The size the panel settles at.
 * @param margin Gap the panel keeps from the edge of the page.
 * @param page The page the panel sits on.
 * @param direction The ambient layout direction used to resolve the anchor's end edge.
 */
internal fun placeGlassPopup(
    anchor: Rect,
    size: Size,
    margin: Float,
    page: Size,
    direction: LayoutDirection,
): GlassPopupPlacement {
    val endAlignedLeft = if (direction == LayoutDirection.Ltr) {
        anchor.right - size.width
    } else {
        anchor.left
    }
    val left = endAlignedLeft
        .coerceIn(margin, (page.width - size.width - margin).coerceAtLeast(margin))
    val below = page.height - margin - anchor.top
    val above = anchor.bottom - margin
    val alignTop = size.height <= below || below >= above
    val wanted = if (alignTop) anchor.top else anchor.bottom - size.height
    val top = wanted.coerceIn(margin, (page.height - size.height - margin).coerceAtLeast(margin))
    return GlassPopupPlacement(Rect(left, top, left + size.width, top + size.height), alignTop)
}

/**
 * The panel every glass menu is built on.
 *
 * It owns everything the three openings share: the catcher that closes on a tap outside, the
 * placement, the shadow and the material, and the one measurement that freezes the rows at the size
 * the panel settles at so nothing reflows while it moves. What each opening supplies is only its
 * own geometry — [frame] for the panel and [contentLayer] for the rows inside it.
 *
 * @param onDismissRequest Called when a tap outside should close the menu.
 * @param backdrop The [Backdrop] behind the glass. `null` uses [GlassPopupVisuals.containerColor]
 *   while retaining the configured bloom stroke and shadow.
 * @param modifier The modifier applied to the panel.
 * @param sizing How wide and tall the panel may be.
 * @param visuals What its surface is made of.
 * @param underlayMaterial Parent colour treatment inherited from an action-bar button anchor.
 * @param contentPadding Padding around the rows.
 * @param panelLayer Applied to the panel as a whole: its opacity, its blur, its scale.
 * @param overlay Drawn over the panel and its rows, inside the silhouette.
 * @param frame The panel's rectangle and corner radius now, given the size it settles at and the
 *   page it sits on.
 * @param contentLayer Applied to the rows, given the size the panel settles at and where it is now.
 * @param reveal Clips the panel as it opens. A menu whose opening is the library's own passes
 *   [popupClipReveal] here; one that carries its own geometry leaves it empty.
 * @param onMeasured Called with the size the panel settles at, each time it is measured.
 * @param interactive Whether the panel's rows can receive input and accessibility actions.
 * @param scrollable Whether rows scroll within the safe-area-constrained secondary viewport.
 * @param content The rows.
 */
@Composable
internal fun BoxScope.GlassPopupSurface(
    onDismissRequest: () -> Unit,
    backdrop: Backdrop?,
    sizing: GlassPopupSizing,
    visuals: GlassPopupVisuals,
    contentPadding: PaddingValues,
    panelLayer: GraphicsLayerScope.() -> Unit,
    overlay: DrawScope.() -> Unit,
    frame: Density.(end: Size, page: Size) -> GlassPopupFrame,
    contentLayer: GraphicsLayerScope.(end: Size, rect: Rect) -> Unit,
    modifier: Modifier = Modifier,
    reveal: Modifier = Modifier,
    underlayMaterial: GlassMaterial? = null,
    onMeasured: ((Size) -> Unit)? = null,
    interactive: Boolean = true,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = null, indication = null) { onDismissRequest() },
    )

    val density = LocalDensity.current
    val shadowOutsets = remember(visuals.shadow, density) {
        with(density) { popupShadowOutsets(visuals.shadow) }
    }
    val measured = remember { MeasuredPanel() }
    val shape = remember(measured) {
        val corner = MeasuredCorner(measured)
        GlassShape(corner, corner, corner, corner)
    }

    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(measured.rect.left.roundToInt(), measured.rect.top.roundToInt())
                }
            }
            .graphicsLayer {
                panelLayer()
                // Alpha below one creates an offscreen buffer. Keep the shader shadow's
                // external pixels inside it instead of clipping them to the panel rectangle.
                outsets = shadowOutsets
            }
            .then(reveal)
            .glassShadow(shape, visuals.shadow, visuals.alpha)
            .clip(shape)
            .then(
                if (backdrop == null) {
                    Modifier
                        .background(visuals.containerColor)
                        .drawWithContent {
                            drawContent()
                            visuals.stroke?.let { drawGlassStroke(shape, layoutDirection, it, visuals.alpha) }
                        }
                } else if (underlayMaterial != null) {
                    Modifier.glassOnActionBar(
                        backdrop = backdrop,
                        shape = shape,
                        style = visuals.style,
                        alpha = visuals.alpha,
                        material = visuals.material,
                        underlayMaterial = underlayMaterial,
                        stroke = visuals.stroke,
                    )
                } else {
                    Modifier.glass(
                        backdrop = backdrop,
                        shape = shape,
                        style = visuals.style,
                        alpha = visuals.alpha,
                        material = visuals.material,
                        stroke = visuals.stroke,
                        shading = false,
                    )
                },
            )
            .drawWithContent {
                drawContent()
                overlay()
            }
            .then(
                if (interactive) {
                    Modifier
                } else {
                    Modifier
                        .clearAndSetSemantics { }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                                }
                            }
                        }
                },
            )
            .layout { measurable, constraints ->
                val safeInset = if (scrollable) sizing.safeMargin.roundToPx() * 2 else 0
                val widest = sizing.maxWidth.roundToPx().coerceAtMost((constraints.maxWidth - safeInset).coerceAtLeast(0))
                val narrowest = sizing.minWidth.roundToPx().coerceAtMost(widest)
                val tallest = sizing.maxHeight.roundToPx().coerceAtMost((constraints.maxHeight - safeInset).coerceAtLeast(0))
                val width = measurable.maxIntrinsicWidth(tallest).coerceIn(narrowest, widest)
                val placeable = measurable.measure(
                    Constraints(minWidth = width, maxWidth = width, minHeight = 0, maxHeight = tallest),
                )
                measured.end = Size(placeable.width.toFloat(), placeable.height.toFloat())
                onMeasured?.invoke(measured.end)
                val page = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
                val current = frame(measured.end, page)
                measured.rect = current.rect
                measured.cornerRadius = current.cornerRadius
                layout(
                    current.rect.width.roundToInt().coerceAtLeast(0),
                    current.rect.height.roundToInt().coerceAtLeast(0),
                ) {
                    placeable.place(0, 0)
                }
            },
    ) {
        Column(
            modifier = Modifier
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    contentLayer(measured.end, measured.rect)
                }
                .padding(contentPadding),
            content = content,
        )
    }
}

/** Match drawGlassShadow's bounds, including its source-density conversion and offset. */
internal fun Density.popupShadowOutsets(shadow: GlassShadow?): LayerOutsets {
    if (shadow == null) return LayerOutsets.Zero
    val sourceScale = density / GlassDefaults.SourceDensity
    val reach = (shadow.radius * sourceScale).coerceAtLeast(1f)
    val x = shadow.offsetX * sourceScale
    val y = shadow.offsetY * sourceScale
    return LayerOutsets(
        left = (reach - x).coerceAtLeast(0f).toDp(),
        top = (reach - y).coerceAtLeast(0f).toDp(),
        right = (reach + x).coerceAtLeast(0f).toDp(),
        bottom = (reach + y).coerceAtLeast(0f).toDp(),
    )
}

/**
 * The panel's measured size and current rectangle.
 *
 * Written during measurement and read back inside the same pass, by the layout wrapped around the
 * one that wrote it. Deliberately not snapshot state: a state written mid-measure either lags a
 * frame or invalidates the pass that wrote it.
 */
private class MeasuredPanel {
    var end: Size = Size.Zero
    var rect: Rect = Rect.Zero
    var cornerRadius: Dp = 0.dp
}

/**
 * The panel's corner, resolved at the moment it is drawn.
 *
 * The radius travels with the panel, and a shape built during composition would be a frame behind
 * it.
 */
private class MeasuredCorner(private val measured: MeasuredPanel) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float = with(density) { measured.cornerRadius.toPx() }

    override fun toString(): String = "MeasuredCorner"
}
