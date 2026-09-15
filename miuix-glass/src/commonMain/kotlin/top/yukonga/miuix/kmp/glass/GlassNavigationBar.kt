// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * One destination in a [GlassNavigationBar].
 *
 * @property icon The icon drawn for this destination.
 * @property label Optional caption under the icon.
 * @property contentDescription What a screen reader announces for it.
 */
@Immutable
data class GlassNavigationItem(
    val icon: ImageVector,
    val label: String? = null,
    val contentDescription: String? = null,
)

/** Default values for [GlassNavigationBar]. */
object GlassNavigationBarDefaults {

    /** Minimum height; wrapped captions can make the bar taller. */
    val Height: Dp = 54.dp

    /** Size of an icon. */
    val IconSize: Dp = 28.dp

    /** Caption size in dp, matching the source's footnote3 dimension resource. */
    val LabelSize: Dp = 11.dp

    /** Caption size used when the system font scale reaches 1.6. */
    val LargeLabelSize: Dp = 16.dp

    /** Space above and below each icon-and-caption group. */
    val ContentPaddingVertical: Dp = 6.dp

    /** Gap between an icon and its caption. */
    val LabelSpacing: Dp = 0.dp

    /** Inset between the top and bottom of the bar and the indicator. */
    val IndicatorPaddingVertical: Dp = 3.dp

    /** The bar's own horizontal inset: where the destinations are laid out from. */
    val ContentPaddingHorizontal: Dp = 8.dp

    /** How far the capsule reaches past its destination's share of the bar, on each side. */
    val IndicatorOverhang: Dp = 5.dp

    /** Opacity of the destination under the finger. */
    val PressedAlpha: Float = 0.6f

    /** The rim traced around the bar. */
    @Composable
    fun stroke(): GlassStroke = GlassStrokes.forTheme(
        isDark = MiuixTheme.colorScheme.background.luminance() < 0.5f,
    )

    /** The bar's own body. */
    @Composable
    fun material(): GlassMaterial = GlassMaterials.puredThinGlass(
        isDark = MiuixTheme.colorScheme.background.luminance() < 0.5f,
    )

    /**
     * Resting fill of the capsule.
     *
     * The source builds all three of the capsule's states out of one colour at three alphas, not
     * out of three colours: white over a dark page, black over a light one. This is the first of
     * them.
     */
    @Composable
    fun indicatorColor(): Color = if (MiuixTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }

    /**
     * Fill of the capsule under a finger.
     *
     * The same colour, roughly twice as strong. The capsule does not change hue when it is held —
     * over a dark page that reads as the capsule lighting up, and what darkens is the destination
     * inside it, by [PressedAlpha].
     */
    @Composable
    fun indicatorPressedColor(): Color = if (MiuixTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.26f)
    } else {
        Color.Black.copy(alpha = 0.16f)
    }
}

/**
 * A floating bottom bar on glass.
 *
 * While dragging within a destination, the indicator follows a spring with damping 1 and
 * response 0.15s. Its trailing edge stretches by four times each pointer delta, capped at 60px.
 * Crossing destinations uses the directional edge springs; release retargets the same animated
 * edges without snapping them to the pointer. Rendered edges stay within the outermost items,
 * including during spring overshoot. Selection callbacks still run on press and drag.
 *
 * @param items The destinations, in order.
 * @param selectedIndex The index of the current destination.
 * @param onSelect Called with the index under the finger, on press and while dragging.
 * @param backdrop The [Backdrop] supplying the content behind the glass.
 * @param modifier The modifier applied to the bar.
 * @param style The glass material.
 * @param shape The bar's silhouette. Defaults to a capsule of [height].
 * @param alpha Opacity multiplier for the material.
 * @param visible Whether the bar is on screen. Turning it off does not simply hide it: the bar
 *   shrinks, fades and blurs itself away together, and comes back after a short delay.
 * @param stroke Optional bloom stroke along the rim.
 * @param shadow The shadow the floating capsule casts. `null` removes it.
 * @param height Minimum bar height. Content can grow for two-line or large-font captions.
 * @param material The bar's own body — the blur radius and the colour layers over it. `null`
 *   leaves the bar transparent, which over a dark page reads as a hole rather than as a panel.
 * @param indicatorPressedColor Fill of the capsule while a finger is on it.
 * @param indicatorColor Fill of the capsule behind the selected destination. Neutral rather than
 *   accented — the source bar tints the icon, not the indicator.
 * @param selectedColor Tint of the selected icon and label.
 * @param unselectedColor Tint of the others. The source bar does not dim them: every destination
 *   is drawn at full strength and the indicator alone says which one is current.
 */
@Composable
fun GlassNavigationBar(
    items: List<GlassNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    style: GlassStyle = GlassDefaults.Style,
    shape: GlassShape = GlassShape(CornerSize(50), CornerSize(50), CornerSize(50), CornerSize(50)),
    alpha: Float = 1f,
    visible: Boolean = true,
    stroke: GlassStroke? = GlassNavigationBarDefaults.stroke(),
    shadow: GlassShadow? = GlassShadows.Float,
    height: Dp = GlassNavigationBarDefaults.Height,
    material: GlassMaterial? = GlassNavigationBarDefaults.material(),
    indicatorColor: Color = GlassNavigationBarDefaults.indicatorColor(),
    indicatorPressedColor: Color = GlassNavigationBarDefaults.indicatorPressedColor(),
    selectedColor: Color = MiuixTheme.colorScheme.onBackground,
    unselectedColor: Color = MiuixTheme.colorScheme.onBackground,
) {
    if (items.isEmpty()) return
    val index = selectedIndex.coerceIn(0, items.lastIndex)
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val labelSize = with(density) {
        if (fontScale >= 1.6f) GlassNavigationBarDefaults.LargeLabelSize.toSp() else GlassNavigationBarDefaults.LabelSize.toSp()
    }
    val contentInset = with(density) { GlassNavigationBarDefaults.ContentPaddingHorizontal.toPx() }
    val overhang = with(density) { GlassNavigationBarDefaults.IndicatorOverhang.toPx() }

    var barWidth by remember { mutableIntStateOf(0) }
    var pressedIndex by remember { mutableIntStateOf(-1) }
    val dragging = remember { mutableStateOf(false) }

    val showScale = remember { Animatable(if (visible) 1f else GlassMotion.NAV_HIDE_SCALE) }
    var interactionEnabled by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) {
            interactionEnabled = true
            delay(GlassMotion.NAV_SHOW_DELAY_MS)
            showScale.animateTo(1f, GlassMotion.navShowHide())
        } else {
            showScale.animateTo(GlassMotion.NAV_HIDE_SCALE, GlassMotion.navShowHide())
            interactionEnabled = false
        }
    }

    val scope = rememberCoroutineScope()
    val left = remember { Animatable(0f) }
    val right = remember { Animatable(0f) }
    fun animateIndicator(
        newLeft: Float,
        newRight: Float,
        leftSpring: SpringSpec<Float> = GlassMotion.edgeSpring(newLeft <= left.value),
        rightSpring: SpringSpec<Float> = GlassMotion.edgeSpring(newLeft > left.value),
    ) {
        // All motion owns the same two Animatables. Retarget through animateTo so their current
        // positions and velocities survive drag, reversal, selection and release interruptions.
        // These jobs belong to the composition, not to a restarting selection effect.
        scope.launch { left.animateTo(newLeft, leftSpring) }
        scope.launch { right.animateTo(newRight, rightSpring) }
    }
    val slot = if (barWidth <= 0) 0f else (barWidth - contentInset * 2f) / items.size
    val chipWidth = slot + overhang * 2f
    val targetLeft = contentInset - overhang + index * slot
    val targetRight = targetLeft + chipWidth

    LaunchedEffect(targetLeft, targetRight) {
        if (slot <= 0f) return@LaunchedEffect
        if (dragging.value) return@LaunchedEffect
        if (right.value <= left.value) {
            left.snapTo(targetLeft)
            right.snapTo(targetRight)
            return@LaunchedEffect
        }
        animateIndicator(targetLeft, targetRight)
    }

    val chipHeldPx = with(density) {
        (height - GlassNavigationBarDefaults.IndicatorPaddingVertical * 2).toPx()
    }
    val chipPressed = pressedIndex >= 0
    val chipScale by animateFloatAsState(
        targetValue = if (chipPressed) glassPressScale(chipHeldPx, density.density) else 1f,
        animationSpec = if (chipPressed) GlassMotion.pressDown() else GlassMotion.pressUp(),
        label = "glassNavigationIndicatorPress",
    )
    val chipFill by animateColorAsState(
        targetValue = if (chipPressed) indicatorPressedColor else indicatorColor,
        animationSpec = if (chipPressed) GlassMotion.navPressEnter() else GlassMotion.navPressExit(),
        label = "glassNavigationIndicatorFill",
    )

    val onSelectState by rememberUpdatedState(onSelect)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .graphicsLayer {
                val show = showScale.value
                scaleX = show
                scaleY = show
                this.alpha = ((show - GlassMotion.NAV_HIDE_SCALE) / (1f - GlassMotion.NAV_HIDE_SCALE))
                    .coerceIn(0f, 1f)
            }
            .blur(
                radius = (
                    (1f - ((showScale.value - GlassMotion.NAV_HIDE_SCALE) / (1f - GlassMotion.NAV_HIDE_SCALE)))
                        .coerceIn(0f, 1f) * GlassMotion.NAV_HIDE_BLUR_DP
                    ).dp,
                edgeTreatment = BlurredEdgeTreatment.Unbounded,
            )
            .glassPanel(
                backdrop = backdrop,
                shape = shape,
                style = style,
                alpha = alpha,
                material = material,
                stroke = stroke,
                shadow = shadow,
                shading = false,
            )
            .onSizeChanged { barWidth = it.width }
            .then(
                if (interactionEnabled) {
                    Modifier.pointerInput(items.size, contentInset, overhang, layoutDirection) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val width = size.width.toFloat()
                            if (width <= contentInset * 2f) return@awaitEachGesture
                            fun logicalX(x: Float) = if (layoutDirection == LayoutDirection.Rtl) width - x else x
                            val span = (width - contentInset * 2f) / items.size
                            fun indexAt(x: Float) = if (span <= 0f) {
                                0
                            } else {
                                ((x - contentInset) / span).toInt().coerceIn(0, items.lastIndex)
                            }

                            val chipW = span + overhang * 2f
                            val minLeft = contentInset - overhang
                            val maxLeft = (width - chipW).coerceAtLeast(0f)
                            val downX = logicalX(down.position.x)
                            var lastX = downX
                            var current = indexAt(downX)
                            pressedIndex = current
                            onSelectState(current)
                            var grabOffset = Float.NaN
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed || change.isConsumed) break
                                    val x = logicalX(change.position.x)
                                    if (grabOffset.isNaN()) {
                                        if (abs(x - downX) < viewConfiguration.touchSlop) {
                                            pressedIndex = indexAt(x)
                                            continue
                                        }
                                        val bounds = navigationIndicatorBounds(left.value, right.value, width, minLeft)
                                        grabOffset = x - bounds.x
                                        lastX = x
                                        dragging.value = true
                                    }
                                    val chipLeft = (x - grabOffset).coerceIn(0f, maxLeft)
                                    val next = indexAt(x)
                                    val target = navigationDragTarget(chipLeft, chipW, width, x - lastX, next != current)
                                    animateIndicator(target.left, target.right, target.leftSpring, target.rightSpring)
                                    if (next != current) {
                                        current = next
                                        onSelectState(next)
                                    }
                                    pressedIndex = next
                                    lastX = x
                                    change.consume()
                                }
                            } finally {
                                pressedIndex = -1
                                dragging.value = false
                                if (!grabOffset.isNaN()) {
                                    val homeLeft = minLeft + current * span
                                    animateIndicator(homeLeft, homeLeft + chipW)
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = GlassNavigationBarDefaults.IndicatorPaddingVertical)
                .layout { measurable, constraints ->
                    // OverlayView clamps in LEFT_PROPERTY/RIGHT_PROPERTY on every frame, not
                    // just at the target. Keep the spring alive so the other edge still rebounds.
                    val bounds = navigationIndicatorBounds(
                        left.value,
                        right.value,
                        constraints.maxWidth.toFloat(),
                        contentInset - overhang,
                    )
                    val start = bounds.x.roundToInt()
                    val width = (bounds.y.roundToInt() - start).coerceAtLeast(0)
                    val placeable = measurable.measure(
                        constraints.copy(minWidth = width, maxWidth = width),
                    )
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.placeRelative(IntOffset(start, 0))
                    }
                }
                .graphicsLayer {
                    scaleX = chipScale
                    scaleY = chipScale
                }
                .clip(RoundedCornerShape(percent = 50))
                .background(chipFill),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .padding(horizontal = GlassNavigationBarDefaults.ContentPaddingHorizontal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { position, item ->
                val selected = position == index
                val tint by animateColorAsState(
                    targetValue = if (selected) selectedColor else unselectedColor,
                    animationSpec = GlassMotion.navContent(),
                    label = "glassNavigationTint",
                )
                val pressAlpha by animateFloatAsState(
                    targetValue = if (position == pressedIndex) {
                        GlassNavigationBarDefaults.PressedAlpha
                    } else {
                        1f
                    },
                    animationSpec = GlassMotion.navContentFloat(),
                    label = "glassNavigationPress",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = GlassNavigationBarDefaults.IndicatorOverhang,
                            vertical = GlassNavigationBarDefaults.ContentPaddingVertical,
                        )
                        .then(
                            if (interactionEnabled) {
                                Modifier
                                    .semantics(mergeDescendants = true) {
                                        this.role = Role.Tab
                                        this.selected = selected
                                        (item.contentDescription ?: item.label)?.let {
                                            this.contentDescription = it
                                        }
                                        onClick {
                                            onSelectState(position)
                                            true
                                        }
                                    }
                                    .onKeyEvent { event ->
                                        val activationKey = event.key == Key.Enter ||
                                            event.key == Key.NumPadEnter ||
                                            event.key == Key.Spacebar
                                        if (activationKey) {
                                            if (event.type == KeyEventType.KeyUp) onSelectState(position)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    .focusable()
                            } else {
                                Modifier.clearAndSetSemantics { }
                            },
                        )
                        .graphicsLayer { this.alpha = pressAlpha },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(GlassNavigationBarDefaults.LabelSpacing),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(GlassNavigationBarDefaults.IconSize),
                            tint = tint,
                        )
                        if (item.label != null) {
                            Text(
                                text = item.label,
                                style = MiuixTheme.textStyles.footnote2.copy(fontSize = labelSize),
                                color = tint,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
