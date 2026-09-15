// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import top.yukonga.miuix.kmp.blur.Backdrop
import kotlin.math.roundToInt

/**
 * The control a [GlassTransformPopup] grows out of.
 *
 * The source system opens a menu two ways, and this is the second of them. A plain control gets
 * [GlassPopup]: the panel is revealed at the control's corner and the control itself never moves.
 * A control that declares itself transformable instead *becomes* the panel — the panel starts as
 * the control's own rectangle, at the control's own place, with the control's own corner radius,
 * and travels and grows out to where the menu belongs. The control's contents come along with it
 * and dissolve out as the panel's contents dissolve in.
 *
 * Hold one with [rememberGlassPopupAnchor], report the control with [glassPopupAnchor] and its
 * contents with [glassPopupAnchorContent], and hand it to [GlassTransformPopup].
 */
@Stable
class GlassPopupAnchor {

    internal var secondaryBackProgressState: State<Float>? by mutableStateOf(null)

    /** Predictive-back progress published by an attached [GlassDropdownPopup]. */
    internal var dropdownBackProgressState: State<Float>? by mutableStateOf(null)

    /**
     * Secondary predictive-back progress, including cancellation recovery and the retained exit
     * fraction. Read inside a draw/layer callback to make custom arrows follow the menu:
     * `arrowRotation = { rotation * (1f - anchor.secondaryBackProgress) }`.
     */
    val secondaryBackProgress: Float get() = secondaryBackProgressState?.value ?: 0f

    /** Material supplied by an attached glass button, including its resolved backdrop. */
    internal var surface: GlassAnchorSurface? by mutableStateOf(null)

    /** Current button surface opacity and target state, independent of the legacy shadow ramp. */
    internal var surfaceProgress: State<Float>? by mutableStateOf(null)
    internal var surfaceOpacity: Float by mutableFloatStateOf(1f)
    internal val surfaceAlpha: Float get() = surfaceOpacity * (surfaceProgress?.value ?: 1f)
    internal var surfaceFloating: Boolean by mutableStateOf(false)

    /** The control's outer bounds, in the root's coordinate space. */
    internal var containerBounds: Rect by mutableStateOf(Rect.Zero)

    /** The control's contents' bounds, in the root's coordinate space. */
    internal var contentBounds: Rect by mutableStateOf(Rect.Zero)

    /** The control's own corner radius. The panel's radius starts here and relaxes to its own. */
    internal var cornerRadius: Dp by mutableStateOf(0.dp)

    /**
     * Whether the control floats over its page rather than sitting in it.
     *
     * A floating control's panel is drawn at full strength for the whole journey, so the panel is
     * watched all the way home. A control that sits in the page instead has its panel fade with the
     * icon, and the panel is gone well before the spring has settled.
     */
    internal var floating: Boolean by mutableStateOf(false)

    /**
     * Whether the control is standing aside for the menu.
     *
     * Both openings that take a control need this, and both hand it back only when the menu has
     * finished leaving. [GlassTransformPopup] stands the whole control down, because from the
     * moment it opens the panel *is* the control and two of it would be drawn. [GlassDropdownPopup]
     * stands down only the value the row displays, because the list about to open is that value's
     * own choices. [glassPopupAnchor] and [glassPopupAnchorValue] read this; a control never has to.
     */
    internal var contentHidden: Boolean by mutableStateOf(false)

    /** How far the row's displayed value has faded. [GlassDropdownPopup] drives it. */
    internal var valueAlpha: Float by mutableFloatStateOf(1f)

    /** The displayed value reappears with the dropdown as it travels home. */
    internal val dropdownValueAlpha: Float
        get() = dropdownBackProgressState?.value?.coerceIn(0f, 1f) ?: 0f
}

/** The sampling and colour treatment shared by a glass button and its transforming menu. */
internal data class GlassAnchorSurface(
    val backdrop: Backdrop?,
    val style: GlassStyle,
    val material: GlassMaterial,
    val underlayMaterial: GlassMaterial?,
    val stroke: GlassStroke?,
    val fill: Color,
)

/** Marks the button's modifier so its renderer can publish the actual surface to this anchor. */
internal data class GlassPopupAnchorElement(val anchor: GlassPopupAnchor) : Modifier.Element

/**
 * Remembers a [GlassPopupAnchor].
 *
 * The fade [glassPopupAnchorValue] draws with is animated here rather than inside the menu. A menu
 * stops composing the moment it has finished leaving, which is exactly when the control is due to
 * come back — driven from there, the control would be left stranded halfway.
 */
@Composable
fun rememberGlassPopupAnchor(): GlassPopupAnchor {
    val anchor = remember { GlassPopupAnchor() }
    val alpha = animateFloatAsState(
        targetValue = if (anchor.contentHidden) 0f else 1f,
        animationSpec = GlassMotion.default(),
        label = "glassPopupAnchorValue",
    ).value
    SideEffect { anchor.valueAlpha = alpha }
    return anchor
}

/**
 * Reports a control to [anchor], and stands it down while its menu is open.
 *
 * Put this on the control's outermost node — the pill, not the icon inside it. The panel begins
 * life as exactly this rectangle, with this corner radius, so the control itself has to go: two of
 * it would be drawn otherwise. It comes back when the panel has shrunk into it again.
 * On [GlassIconButton], this also shares the button's backdrop, blur and colour treatment with
 * [GlassTransformPopup], including the action bar's parent material when present.
 *
 * @param anchor The anchor to report to.
 * @param cornerRadius The control's own corner radius. Half the control's height, for a pill.
 * @param floating Whether the control floats over its page. A floating control keeps its panel at
 *   full strength for the whole journey instead of fading it with the control's contents.
 */
@Stable
fun Modifier.glassPopupAnchor(
    anchor: GlassPopupAnchor,
    cornerRadius: Dp,
    floating: Boolean = false,
): Modifier = this
    .then(GlassPopupAnchorElement(anchor))
    .onGloballyPositioned {
        anchor.containerBounds = it.boundsInRoot()
        anchor.cornerRadius = cornerRadius
        anchor.floating = floating
    }
    .graphicsLayer { alpha = if (anchor.contentHidden) 0f else 1f }

/**
 * Narrows what the menu copies to one part of a control.
 *
 * Put this on the icon inside a control whose background the panel is already taking over. A glass
 * pill wants exactly that: the panel *is* the pill for the length of the animation, so copying the
 * pill as well would stack two of them and read as a doubled, too-bright glass. Left off, the menu
 * copies the control whole, which is what a control with nothing behind its icon wants.
 *
 * @param anchor The anchor to report to.
 */
@Stable
fun Modifier.glassPopupAnchorContent(anchor: GlassPopupAnchor): Modifier = this.onGloballyPositioned { anchor.contentBounds = it.boundsInRoot() }

/**
 * Reports a row to [anchor], for a [GlassDropdownPopup]. The row itself stays put.
 *
 * @param anchor The anchor to report to.
 */
@Stable
fun Modifier.glassPopupAnchorRow(anchor: GlassPopupAnchor): Modifier = this.onGloballyPositioned { anchor.containerBounds = it.boundsInRoot() }

/**
 * Fades a row's displayed value out while its list of choices is open.
 *
 * Put this on the value the row shows and the chevron beside it, not on the row. The list about to
 * open *is* that value's choices, so the source stands it down the moment the list starts to open
 * and hands it back only once the list has finished leaving. That asymmetry is deliberate: the
 * value going first is what makes the list read as coming out of it.
 *
 * @param anchor The anchor the row reported itself to.
 */
@Stable
fun Modifier.glassPopupAnchorValue(anchor: GlassPopupAnchor): Modifier = this.graphicsLayer {
    alpha = maxOf(anchor.valueAlpha, anchor.dropdownValueAlpha)
}

/**
 * A menu that grows out of the control it belongs to, on glass.
 *
 * The second of the source system's three openings, and the one a control that can take part gets.
 * Four things move at once, each on its own curve:
 *
 * - The **panel's rectangle** runs from the control's out to the menu's on one spring, carrying the
 *   corner radius with it.
 * - The **panel's centre** runs on a quicker spring, so it arrives where it belongs before the
 *   panel has finished growing. That difference is the whole reason the travel does not read as a
 *   slide across the screen.
 * - The **control's contents** are copied, and the copy travels by exactly the centre's
 *   displacement, grows with the panel, fades out and blurs *up*.
 * - The **panel's contents** scale with the panel's width, fade in and blur *down*, 50ms behind the
 *   control's. On the way out the two swap which of them waits.
 *
 * Rows accept input during opening and while open, unless a secondary menu is stacked above them.
 * During dismissal they remain drawn for the transform, but cannot activate a submenu.
 * Predictive Back follows the transform toward the anchor and springs back on cancellation.
 * While [stacked], the secondary menu handles Back instead.
 *
 * @param show Whether the menu is open.
 * @param onDismissRequest Called when a tap outside should close it.
 * @param anchor The control the menu grows out of.
 * @param backdrop The [Backdrop] behind the glass when the anchor has no shared button surface.
 *   A [GlassIconButton] anchor supplies its own resolved backdrop, including a null fallback.
 *   With no backdrop, the panel retains its configured bloom stroke and Compose shadow.
 * @param anchorContent A copy of the control — its background as well as its icon, unless
 *   [glassPopupAnchorContent] named a smaller part.
 * @param modifier The modifier applied to the panel.
 * @param simplified Whether the control's contents stay out of the opening transform. Overflow
 *   buttons use this because their compact glyph should hand directly to the panel instead of
 *   growing to the panel's width before it fades. The contents still join the reverse transform
 *   so the glyph travels back into the control when the panel closes.
 * @param stacked Whether a second menu stands in front of this one. It shrinks and takes a wash,
 *   which is what the source does when a submenu opens over a menu.
 * @param maskColor The wash laid over it while [stacked].
 * @param sizing How wide and tall the panel may be.
 * @param visuals The panel's appearance. A [GlassIconButton] anchor overrides the style, material,
 *   stroke and fallback colour with its own; popup opacity and shadow still come from [visuals].
 * @param anchorAlpha Background opacity for anchors without a shared button surface. Glass button
 *   anchors publish their actual animated opacity automatically, including during floating changes.
 * @param cornerRadius Corner radius the panel settles at.
 * @param gap Gap between the control and the panel.
 * @param contentPadding Padding around the items.
 * @param onMeasured Called with the size the panel settles at. A second menu opened from one of its
 *   rows is measured at least this wide, which is what puts the two panels' edges in line.
 * @param content The items.
 */
@Composable
fun BoxScope.GlassTransformPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchor: GlassPopupAnchor,
    backdrop: Backdrop?,
    anchorContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    simplified: Boolean = false,
    stacked: Boolean = false,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    anchorAlpha: Float = 1f,
    maskColor: Color = GlassPopupDefaults.maskColor(),
    cornerRadius: Dp = GlassPopupDefaults.CornerRadius,
    gap: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(vertical = GlassPopupDefaults.ContentPaddingVertical),
    onMeasured: ((Size) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val transition = updateTransition(show, label = "glassTransformPopup")
    val bounds = transition.animateFloat(
        transitionSpec = { GlassMotion.transformBounds(targetState) },
        label = "glassTransformBounds",
    ) { if (it) 1f else 0f }
    val center = transition.animateFloat(
        transitionSpec = { GlassMotion.transformCenter(targetState) },
        label = "glassTransformCenter",
    ) { if (it) 1f else 0f }
    val iconMaterial = transition.animateFloat(
        transitionSpec = { GlassMotion.transformIconMaterial(targetState) },
        label = "glassTransformIcon",
    ) { if (it) 1f else 0f }
    val contentMaterial = transition.animateFloat(
        transitionSpec = { GlassMotion.transformContentMaterial(targetState) },
        label = "glassTransformContent",
    ) { if (it) 1f else 0f }

    val pushedBack = animateFloatAsState(
        targetValue = if (stacked) 1f else 0f,
        animationSpec = GlassMotion.secondaryPopup(stacked),
        label = "glassTransformStacked",
    )
    val maskAlpha = animateFloatAsState(
        targetValue = if (stacked) 1f else 0f,
        animationSpec = GlassMotion.secondaryPopupMask(stacked),
        label = "glassTransformMask",
    )
    val active = show || transition.currentState || transition.isRunning
    val backProgress = rememberGlassPopupBackProgress(
        show = show,
        active = active,
        enabled = show && !stacked,
        retainWhenInactive = false,
        resetSpec = GlassMotion.transformBounds(true),
        onDismissRequest = onDismissRequest,
    )
    DisposableEffect(active, anchor) {
        anchor.contentHidden = active
        onDispose { anchor.contentHidden = false }
    }
    if (!active) return

    val anchorSurface = anchor.surface
    val resolvedVisuals = if (anchorSurface != null) {
        visuals.copy(
            style = anchorSurface.style,
            material = anchorSurface.material,
            stroke = anchorSurface.stroke,
            containerColor = anchorSurface.fill,
        )
    } else {
        visuals
    }

    val startRect = anchor.containerBounds
    val layoutDirection = LocalLayoutDirection.current
    val iconRect = anchor.contentBounds.takeUnless { it.isEmpty } ?: startRect
    val startRadius = anchor.cornerRadius
    fun geometryProgress() = popupFractionWithBack(bounds.value, backProgress.value)
    fun centerProgress() = popupFractionWithBack(center.value, backProgress.value)
    fun iconProgress() = popupFractionWithBack(iconMaterial.value, backProgress.value)
    fun stackedProgress() = popupFractionWithBack(pushedBack.value, anchor.secondaryBackProgress)
    fun panelAlpha() = transformPanelAlpha(
        visualAlpha = visuals.alpha,
        floating = if (anchorSurface != null) anchor.surfaceFloating || anchor.surfaceAlpha > 0f else anchor.floating,
        // transformPanelAlpha multiplies by visualAlpha; the published surface alpha already
        // includes the button's opacity, so remove that outer factor at the anchor endpoint.
        anchorAlpha = if (anchorSurface != null) {
            if (visuals.alpha > 0f) anchor.surfaceAlpha / visuals.alpha else 0f
        } else {
            anchorAlpha
        },
        geometryProgress = geometryProgress(),
        iconMaterial = iconProgress(),
    )
    val travel = remember { TransformTravel() }

    GlassPopupSurface(
        onDismissRequest = onDismissRequest,
        backdrop = if (anchorSurface != null) anchorSurface.backdrop else backdrop,
        modifier = modifier,
        sizing = sizing,
        visuals = resolvedVisuals.copy(alpha = 1f),
        interactive = isTransformPopupInteractive(show, stacked),
        underlayMaterial = anchorSurface?.underlayMaterial,
        contentPadding = contentPadding,
        onMeasured = onMeasured,
        panelLayer = {
            alpha = panelAlpha()
            transformOrigin = TransformOrigin(
                if (startRect.center.x < travel.endCenterX) 0f else 1f,
                if (startRect.center.y < travel.endCenterY) 0f else 1f,
            )
            val s = 1f - 0.05f * stackedProgress()
            scaleX = s
            scaleY = s
        },
        overlay = {
            val fraction = popupFractionWithBack(maskAlpha.value, anchor.secondaryBackProgress).coerceIn(0f, 1f)
            if (fraction > GlassMotion.POPUP_MASK_MIN_VISIBLE_CHANGE) {
                drawRect(maskColor.copy(alpha = maskColor.alpha * fraction))
            }
        },
        frame = { end, page ->
            val frame = transformFrame(
                anchor = startRect,
                end = end,
                page = page,
                margin = sizing.safeMargin.toPx(),
                gap = gap.toPx(),
                sizeFraction = geometryProgress(),
                positionFraction = centerProgress(),
                startRadius = startRadius,
                endRadius = cornerRadius,
                layoutDirection = layoutDirection,
            )
            val settled = placeGlassPopup(
                startRect.translate(0f, gap.toPx()),
                end,
                sizing.safeMargin.toPx(),
                page,
                layoutDirection,
            )
            travel.endCenterX = settled.rect.center.x
            travel.endCenterY = settled.rect.center.y
            travel.endWidth = end.width
            frame
        },
        contentLayer = { end, _ ->
            val width = startRect.width + (end.width - startRect.width) * geometryProgress()
            val scale = if (end.width > 0f) (width / end.width).coerceAtMost(1f) else 1f
            scaleX = scale
            scaleY = scale
            val fraction = popupFractionWithBack(contentMaterial.value, backProgress.value)
            alpha = fraction
            val blur = GlassMotion.TRANSFORM_BLUR_PX * (1f - fraction)
            renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
        },
        scrollable = true,
        content = content,
    )

    val renderAnchorContent by remember(simplified, show, iconMaterial, backProgress) {
        derivedStateOf {
            shouldRenderAnchorContent(simplified, show && backProgress.value == 0f) &&
                popupFractionWithBack(iconMaterial.value, backProgress.value) < 0.999f
        }
    }
    if (renderAnchorContent) {
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        Constraints.fixed(
                            width = iconRect.width.roundToInt().coerceAtLeast(0),
                            height = iconRect.height.roundToInt().coerceAtLeast(0),
                        ),
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(iconRect.left.roundToInt(), iconRect.top.roundToInt())
                    }
                }
                .graphicsLayer {
                    translationX = (travel.endCenterX - startRect.center.x) * centerProgress()
                    translationY = (travel.endCenterY - startRect.center.y) * centerProgress()
                    val width = startRect.width + (travel.endWidth - startRect.width) * geometryProgress()
                    val growth = if (startRect.width > 0f) width / startRect.width else 1f
                    scaleX = growth
                    scaleY = growth
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    val fraction = iconProgress()
                    alpha = 1f - fraction
                    val blur = GlassMotion.TRANSFORM_BLUR_PX * fraction
                    renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
                },
            content = { anchorContent() },
        )
    }
}

/** A simplified anchor skips the outgoing copy, but still receives it on the way home. */
internal fun shouldRenderAnchorContent(simplified: Boolean, show: Boolean): Boolean = !simplified || !show

/** Retained exit content is visual only, even before the transition starts its first exit frame. */
internal fun isTransformPopupInteractive(show: Boolean, stacked: Boolean): Boolean = show && !stacked

/** Matches `TransformAnimation.updateFloatingAlpha()` on the popup's whole container view. */
internal fun transformPanelAlpha(
    visualAlpha: Float,
    floating: Boolean,
    anchorAlpha: Float,
    geometryProgress: Float,
    iconMaterial: Float,
): Float = visualAlpha * if (floating) {
    lerp(anchorAlpha, 1f, geometryProgress.coerceIn(0f, 1f))
} else {
    iconMaterial
}

/** The panel's rectangle partway from the control's own to the menu's. */
private fun transformFrame(
    anchor: Rect,
    end: Size,
    page: Size,
    margin: Float,
    gap: Float,
    sizeFraction: Float,
    positionFraction: Float,
    startRadius: Dp,
    endRadius: Dp,
    layoutDirection: androidx.compose.ui.unit.LayoutDirection,
): GlassPopupFrame {
    val placement = placeGlassPopup(anchor.translate(0f, gap), end, margin, page, layoutDirection)
    val width = anchor.width + (end.width - anchor.width) * sizeFraction
    val height = anchor.height + (end.height - anchor.height) * sizeFraction
    val centerX = anchor.center.x + (placement.rect.center.x - anchor.center.x) * positionFraction
    val centerY = anchor.center.y + (placement.rect.center.y - anchor.center.y) * positionFraction
    return GlassPopupFrame(
        rect = Rect(
            left = centerX - width / 2f,
            top = centerY - height / 2f,
            right = centerX + width / 2f,
            bottom = centerY + height / 2f,
        ),
        cornerRadius = startRadius + (endRadius - startRadius) * sizeFraction,
    )
}

/**
 * How far the panel has travelled from the control, and how much bigger it is.
 *
 * Written during measurement and read at draw time by the copy of the control's contents, so the
 * two move as one. A plain holder, not snapshot state: the write happens mid-measure.
 */
private class TransformTravel {
    var endCenterX: Float = 0f
    var endCenterY: Float = 0f
    var endWidth: Float = 0f
}
