// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.anim.folmeSpring
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.ListPopupLayoutInfo
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.rememberListPopupLayoutInfo
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val CascadingPopupCornerRadius = 16.dp

private const val MAIN_SPRING_DAMPING = 0.95f
private const val EXPAND_SPRING_DAMPING = 0.99f
private const val EXPAND_SPRING_RESPONSE = 0.45f
private const val COLLAPSE_SPRING_RESPONSE = 0.2f
private const val ARROW_EXPAND_SPRING_RESPONSE = 0.2f
private const val ARROW_COLLAPSE_SPRING_RESPONSE = 0.3f
private const val PRIMARY_SHRUNK_SCALE = 0.95f
internal const val ENTER_SCALE_FROM = 0.15f
internal const val ENTER_SCALE_RANGE = 1f - ENTER_SCALE_FROM

/**
 * The springs and the figures a cascading popup stacks on.
 *
 * A menu built outside this file stacks the same way by reading them from here. Restating any of
 * them elsewhere lets the two drift apart, and the stacking is one motion however the panel is
 * painted.
 */
object CascadingPopupDefaults {

    /** How far the first menu shrinks while a second one stands in front of it. */
    val PrimaryShrunkScale: Float = PRIMARY_SHRUNK_SCALE

    /** How far the trigger row's chevron turns once the second menu is open, in degrees. */
    fun arrowRotation(layoutDirection: LayoutDirection): Float = if (layoutDirection == LayoutDirection.Ltr) -90f else 90f

    /**
     * The spring the second menu opens and closes on.
     *
     * @param expanding Whether the menu is opening. It closes on a shorter and firmer spring.
     */
    fun <T> expandSpring(expanding: Boolean): SpringSpec<T> = if (expanding) {
        folmeSpring(EXPAND_SPRING_DAMPING, EXPAND_SPRING_RESPONSE)
    } else {
        folmeSpring(MAIN_SPRING_DAMPING, COLLAPSE_SPRING_RESPONSE)
    }

    /**
     * The spring the chevron turns on, which trails the panel on the way back.
     *
     * @param expanding Whether the menu is opening.
     */
    fun <T> arrowSpring(expanding: Boolean): SpringSpec<T> = folmeSpring(
        damping = MAIN_SPRING_DAMPING,
        response = if (expanding) ARROW_EXPAND_SPRING_RESPONSE else ARROW_COLLAPSE_SPRING_RESPONSE,
    )
}

private data class CascadingItemPosition(
    val entryIndex: Int,
    val itemIndex: Int,
)

private fun List<DropdownEntry>.positionOf(item: DropdownItem): CascadingItemPosition? {
    forEachIndexed { entryIndex, entry ->
        val itemIndex = entry.items.indexOfFirst { it === item }
        if (itemIndex >= 0) return CascadingItemPosition(entryIndex, itemIndex)
    }
    return null
}

private fun List<DropdownEntry>.itemAt(position: CascadingItemPosition): DropdownItem? = getOrNull(position.entryIndex)?.items?.getOrNull(position.itemIndex)

/** Internal shared layout for cascading list popups. Cascading depth is limited to 2. */
@Composable
internal fun CascadingListPopupLayout(
    show: Boolean,
    popupHost: @Composable (visible: Boolean, content: @Composable () -> Unit) -> Unit,
    entries: List<DropdownEntry>,
    onDismissRequest: () -> Unit,
    dropdownColors: DropdownColors,
    popupModifier: Modifier = Modifier,
    onDismissFinished: (() -> Unit)? = null,
    popupPositionProvider: PopupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.End,
    enableWindowDim: Boolean = true,
    maxHeight: Dp? = null,
    minWidth: Dp = 200.dp,
    collapseOnSelection: Boolean = true,
    surface: (@Composable (Shape) -> Modifier)? = null,
) {
    val internalVisible = remember { mutableStateOf(false) }
    val enterFraction = remember { Animatable(0f) }
    val enterAlpha = remember { Animatable(0f) }
    val dimProgress = remember { Animatable(0f) }
    // Two tracks: a depth-0 gesture may start while the depth-1 collapse is still settling.
    val backEnterProgress = remember { Animatable(0f) }
    val backExpandProgress = remember { Animatable(0f) }

    // Cascading menus assume entry/item structure stays stable while shown. Retaining positions
    // lets rebuilt item values, such as selected state, refresh without closing the submenu.
    var expandedItemPosition by remember { mutableStateOf<CascadingItemPosition?>(null) }
    // Outlives [expandedItemPosition] until the collapse spring settles so the secondary stays
    // subcomposed.
    var displayedItemPosition by remember { mutableStateOf<CascadingItemPosition?>(null) }
    val expandedItem = expandedItemPosition?.let(entries::itemAt)
        ?.takeIf { !it.children.isNullOrEmpty() }
    val displayedItem = displayedItemPosition?.let(entries::itemAt)
        ?.takeIf { !it.children.isNullOrEmpty() }
    SideEffect {
        if (expandedItemPosition != null && expandedItem == null) expandedItemPosition = null
        if (displayedItemPosition != null && displayedItem == null) displayedItemPosition = null
    }
    val expandFraction = remember { Animatable(0f) }
    val primaryScale = remember { Animatable(1f) }
    val maskAlpha = remember { Animatable(0f) }
    val arrowRotation = remember { Animatable(0f) }

    val layoutDirection = LocalLayoutDirection.current
    val arrowEndDeg = remember(layoutDirection) { CascadingPopupDefaults.arrowRotation(layoutDirection) }

    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val currentOnDismissFinished by rememberUpdatedState(onDismissFinished)
    val coroutineScope = rememberCoroutineScope()
    // Defer enter until both are measured — otherwise transformOrigin and offset jump on frame 1.
    var primarySize by remember { mutableStateOf(IntSize.Zero) }
    var hostPositionInWindow by remember { mutableStateOf(IntOffset.Zero) }
    var hostMeasured by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            internalVisible.value = true
            backEnterProgress.snapTo(0f)
            backExpandProgress.snapTo(0f)
            launch { dimProgress.animateTo(1f, ListPopupDefaults.DimEnterAnimationSpec) }
            snapshotFlow { primarySize to hostMeasured }
                .first { (size, ready) -> size != IntSize.Zero && ready }
            launch { enterFraction.animateTo(1f, ListPopupDefaults.FractionAnimationSpec) }
            launch { enterAlpha.animateTo(1f, ListPopupDefaults.AlphaEnterAnimationSpec) }
        } else {
            if (!internalVisible.value) return@LaunchedEffect
            expandedItemPosition = null
            launch { enterFraction.animateTo(0f, ListPopupDefaults.FractionAnimationSpec) }
            launch { dimProgress.animateTo(0f, ListPopupDefaults.DimExitAnimationSpec) }
            enterAlpha.animateTo(0f, ListPopupDefaults.AlphaExitAnimationSpec)
            enterFraction.stop()
            dimProgress.stop()
            backEnterProgress.snapTo(0f)
            backExpandProgress.snapTo(0f)
            internalVisible.value = false
            currentOnDismissFinished?.invoke()
        }
    }

    LaunchedEffect(expandedItemPosition) {
        val itemPosition = expandedItemPosition
        if (itemPosition != null) {
            displayedItemPosition = itemPosition
            backExpandProgress.snapTo(0f)
        }
        val target = if (itemPosition != null) 1f else 0f
        val mainSpec = CascadingPopupDefaults.expandSpring<Float>(target == 1f)
        launch {
            expandFraction.animateTo(target, mainSpec)
            // Settle on the main spring — the slower arrowRotation would otherwise hold the
            // secondary subcomposed past visibility.
            if (target == 0f) {
                displayedItemPosition = null
                // Only after the collapse settles — clearing earlier jumps the effective values.
                backExpandProgress.snapTo(0f)
            }
        }
        launch {
            primaryScale.animateTo(
                if (target == 1f) CascadingPopupDefaults.PrimaryShrunkScale else 1f,
                mainSpec,
            )
        }
        launch { maskAlpha.animateTo(target, mainSpec) }
        launch {
            arrowRotation.animateTo(target * arrowEndDeg, CascadingPopupDefaults.arrowSpring(target == 1f))
        }
    }

    if (!show && !internalVisible.value) return

    var parentBounds by remember { mutableStateOf(IntRect.Zero) }
    Spacer(
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            childCoordinates.parentLayoutCoordinates?.let { parent ->
                val pos = parent.positionInWindow()
                parentBounds = IntRect(
                    left = pos.x.toInt(),
                    top = pos.y.toInt(),
                    right = pos.x.toInt() + parent.size.width,
                    bottom = pos.y.toInt() + parent.size.height,
                )
            }
        },
    )
    if (parentBounds == IntRect.Zero) return

    val layoutInfo = rememberListPopupLayoutInfo(
        alignment = alignment,
        popupPositionProvider = popupPositionProvider,
        parentBounds = parentBounds,
        popupContentSize = primarySize,
    )

    val anchorBoundsByPosition = remember { mutableStateMapOf<CascadingItemPosition, IntRect>() }

    popupHost(internalVisible.value) {
        val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = backState,
            isBackEnabled = show,
            onBackCancelled = {
                // Reset whichever track the gesture drove; depth cannot change mid-gesture.
                coroutineScope.launch {
                    if (expandedItemPosition != null) {
                        backExpandProgress.animateTo(
                            0f,
                            folmeSpring(EXPAND_SPRING_DAMPING, EXPAND_SPRING_RESPONSE),
                        )
                    } else {
                        backEnterProgress.animateTo(0f, ListPopupDefaults.ResetAnimationSpec)
                    }
                }
            },
            // Mirror outside-tap: depth > 0 collapses the secondary, depth 0 dismisses the popup.
            onBackCompleted = {
                if (expandedItemPosition != null) {
                    expandedItemPosition = null
                } else {
                    currentOnDismiss()
                }
            },
        )

        LaunchedEffect(Unit) {
            // Single-coroutine collector so per-frame ticks do not relaunch this effect.
            snapshotFlow { backState.transitionState }
                .collect { transitionState ->
                    if (
                        transitionState is NavigationEventTransitionState.InProgress &&
                        transitionState.direction == NavigationEventTransitionState.TRANSITIONING_BACK
                    ) {
                        val progress = transitionState.latestEvent.progress
                        if (expandedItemPosition != null) {
                            // Preview secondary → primary collapse along the expand-tree.
                            backExpandProgress.snapTo(progress)
                        } else {
                            // Preview popup → spawn corner retreat along the entry tree.
                            backEnterProgress.snapTo(progress)
                        }
                    }
                }
        }

        CompositionLocalProvider(LocalDismissState provides { currentOnDismiss() }) {
            val dimColor = MiuixTheme.colorScheme.windowDimming
            val maskColor = remember(dimColor) {
                dimColor.copy(alpha = (dimColor.alpha * 0.5f).coerceIn(0f, 1f))
            }
            val surfaceColor = MiuixTheme.colorScheme.surfaceContainer

            Box(modifier = Modifier.fillMaxSize()) {
                if (enableWindowDim) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = dimProgress.value * (1f - backEnterProgress.value) }
                            .background(dimColor),
                    )
                }
                Box(
                    modifier = popupModifier
                        .fillMaxSize()
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInWindow()
                            hostPositionInWindow = IntOffset(pos.x.toInt(), pos.y.toInt())
                            hostMeasured = true
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                if (expandedItemPosition != null) {
                                    expandedItemPosition = null
                                } else {
                                    currentOnDismiss()
                                }
                            })
                        },
                ) {
                    CascadingMorphSubLayout(
                        entries = entries,
                        expandedItem = expandedItem,
                        displayedItem = displayedItem,
                        onExpand = { item -> expandedItemPosition = entries.positionOf(item) },
                        onCollapseSecondary = { expandedItemPosition = null },
                        onLeafSelected = { item ->
                            item.onClick?.invoke()
                            if (collapseOnSelection) {
                                expandedItemPosition = null
                                currentOnDismiss()
                            }
                        },
                        getAnchorBounds = { item ->
                            entries.positionOf(item)?.let(anchorBoundsByPosition::get)
                        },
                        setAnchorBounds = { item, bounds ->
                            // Freeze the anchor once secondary is shown so primary's scale
                            // animation can't drift it via onGloballyPositioned.
                            if (displayedItem == null) {
                                entries.positionOf(item)?.let { position ->
                                    anchorBoundsByPosition[position] = bounds
                                }
                            }
                        },
                        onPrimarySizeChange = { primarySize = it },
                        windowBounds = layoutInfo.windowBounds,
                        popupMargin = layoutInfo.popupMargin,
                        parentBounds = parentBounds,
                        popupPositionProvider = popupPositionProvider,
                        alignment = alignment,
                        hostOriginInWindow = { hostPositionInWindow },
                        maxHeight = maxHeight,
                        minWidth = minWidth,
                        dropdownColors = dropdownColors,
                        enterFraction = { enterFraction.value * (1f - backEnterProgress.value) },
                        enterAlpha = { enterAlpha.value * (1f - backEnterProgress.value) },
                        primaryScale = { 1f + (primaryScale.value - 1f) * (1f - backExpandProgress.value) },
                        maskAlpha = { maskAlpha.value * (1f - backExpandProgress.value) },
                        expandFraction = { expandFraction.value * (1f - backExpandProgress.value) },
                        arrowRotation = { arrowRotation.value * (1f - backExpandProgress.value) },
                        layoutInfo = layoutInfo,
                        layoutDirection = layoutDirection,
                        surfaceColor = surfaceColor,
                        maskColor = maskColor,
                        surface = surface,
                    )
                }
            }
        }
    }
}

private const val SLOT_PRIMARY = "primary"
private const val SLOT_SECONDARY = "secondary"
private const val SLOT_SECONDARY_PROBE = "secondary_probe"

// Hoisted so each measure pass reuses the same instances.
private val ProbeArrowRotation: () -> Float = { 0f }
private val ProbeExpandFraction: () -> Float = { 1f }
private val ProbeOnHeaderClick: () -> Unit = {}
private val ProbeOnLeafSelected: (DropdownItem) -> Unit = {}

@Suppress("LongParameterList")
@Composable
private fun CascadingMorphSubLayout(
    entries: List<DropdownEntry>,
    expandedItem: DropdownItem?,
    displayedItem: DropdownItem?,
    onExpand: (DropdownItem) -> Unit,
    onCollapseSecondary: () -> Unit,
    onLeafSelected: (DropdownItem) -> Unit,
    getAnchorBounds: (DropdownItem) -> IntRect?,
    setAnchorBounds: (DropdownItem, IntRect) -> Unit,
    onPrimarySizeChange: (IntSize) -> Unit,
    windowBounds: IntRect,
    popupMargin: IntRect,
    parentBounds: IntRect,
    popupPositionProvider: PopupPositionProvider,
    alignment: PopupPositionProvider.Align,
    hostOriginInWindow: () -> IntOffset,
    maxHeight: Dp?,
    minWidth: Dp,
    dropdownColors: DropdownColors,
    enterFraction: () -> Float,
    enterAlpha: () -> Float,
    primaryScale: () -> Float,
    maskAlpha: () -> Float,
    expandFraction: () -> Float,
    arrowRotation: () -> Float,
    layoutInfo: ListPopupLayoutInfo,
    layoutDirection: LayoutDirection,
    surfaceColor: Color,
    surface: (@Composable (Shape) -> Modifier)?,
    maskColor: Color,
) {
    SubcomposeLayout(modifier = Modifier.fillMaxSize()) { constraints ->
        val resolvedMaxHeightPx = maxHeight?.roundToPx()?.coerceAtLeast(50.dp.roundToPx())
            ?: (windowBounds.height - popupMargin.top - popupMargin.bottom)
                .coerceAtLeast(50.dp.roundToPx())
        val minWidthPx = minWidth.roundToPx().coerceAtMost(constraints.maxWidth)
        val primaryConstraints = Constraints(
            minWidth = minWidthPx,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = resolvedMaxHeightPx.coerceAtMost(constraints.maxHeight),
        )

        // Secondary subcomposition tracks [displayedItem] so the morphing surface survives the
        // collapse spring; primary's mask/chevron use [expandedItem] for instant input feedback.
        val activeExpanded = displayedItem
        val anchorRect = activeExpanded?.let { getAnchorBounds(it) }
        val (anchorPaddingTopPx, _) = if (activeExpanded != null) {
            computeAnchorPaddingPx(activeExpanded, entries)
        } else {
            0 to 0
        }
        // Last-row anchors share primary's rounded bottom — keep the morph's bottom corners
        // full. Mid-list anchors must shrink uniformly or a rounded tongue would protrude from
        // a flat internal row. === because DropdownItem is a data class.
        val isAnchorPopupLast = activeExpanded != null &&
            entries.lastOrNull { it.items.isNotEmpty() }?.items?.last() === activeExpanded

        val primaryPlaceables = subcompose(SLOT_PRIMARY) {
            CascadingPrimaryContent(
                entries = entries,
                expandedItem = expandedItem,
                onExpand = onExpand,
                onLeafSelected = onLeafSelected,
                onAnchorBounds = setAnchorBounds,
                enterFraction = enterFraction,
                enterAlpha = enterAlpha,
                popupLayoutPosition = layoutInfo.popupLayoutPosition,
                transformOrigin = layoutInfo.localTransformOrigin,
                surfaceColor = surfaceColor,
                dropdownColors = dropdownColors,
                primaryScale = primaryScale,
                maskAlpha = maskAlpha,
                maskColor = maskColor,
                onCollapseSecondary = onCollapseSecondary,
                surface = surface,
            )
        }.map { it.measure(primaryConstraints) }

        val primaryWidth = primaryPlaceables.maxOfOrZero { it.width }
        val primaryHeight = primaryPlaceables.sumOfOrZero { it.height }
        val primaryMeasuredSize = IntSize(primaryWidth, primaryHeight)
        onPrimarySizeChange(primaryMeasuredSize)

        val primaryOffsetWindow = if (primaryMeasuredSize == IntSize.Zero) {
            IntOffset.Zero
        } else {
            popupPositionProvider.calculatePosition(
                parentBounds,
                windowBounds,
                layoutDirection,
                primaryMeasuredSize,
                popupMargin,
                alignment,
            )
        }

        // Probe secondary's natural size first so the union rect (primary ∪ secondary) is
        // known before the real secondary is measured at union size for its anchor-to-final slide.
        val probeConstraints = primaryConstraints.copy(
            minWidth = primaryWidth.coerceAtMost(primaryConstraints.maxWidth),
            maxHeight = resolvedMaxHeightPx.coerceAtMost(constraints.maxHeight),
        )

        data class SecondaryLayout(
            val placeables: List<Placeable>,
            val unionRect: IntRect,
        )

        val secondaryLayout: SecondaryLayout? =
            if (activeExpanded != null && anchorRect != null) {
                val measured = subcompose(SLOT_SECONDARY_PROBE) {
                    Box(propagateMinConstraints = true) {
                        CascadingSecondaryColumn(
                            triggerItem = activeExpanded,
                            dropdownColors = dropdownColors,
                            expandFraction = ProbeExpandFraction,
                            arrowRotation = ProbeArrowRotation,
                            anchorHeightPx = 0,
                            anchorPaddingTopPx = 0,
                            onHeaderClick = ProbeOnHeaderClick,
                            onLeafSelected = ProbeOnLeafSelected,
                        )
                    }
                }.firstOrNull()?.measure(probeConstraints)
                val secondarySize = if (measured != null) {
                    IntSize(measured.width, measured.height)
                } else {
                    IntSize.Zero
                }
                if (secondarySize.width > 0 && secondarySize.height > 0 && primaryMeasuredSize != IntSize.Zero) {
                    val secondaryRect = computeSecondaryRect(
                        anchor = anchorRect,
                        secondarySize = secondarySize,
                        windowBounds = windowBounds,
                        layoutDirection = layoutDirection,
                    )
                    val primaryRect = IntRect(
                        left = primaryOffsetWindow.x,
                        top = primaryOffsetWindow.y,
                        right = primaryOffsetWindow.x + primaryMeasuredSize.width,
                        bottom = primaryOffsetWindow.y + primaryMeasuredSize.height,
                    )
                    val unionRect = unionOf(primaryRect, secondaryRect)
                    val secondaryLocalInUnion = IntRect(
                        left = secondaryRect.left - unionRect.left,
                        top = secondaryRect.top - unionRect.top,
                        right = secondaryRect.right - unionRect.left,
                        bottom = secondaryRect.bottom - unionRect.top,
                    )
                    val anchorLocalInUnion = IntRect(
                        left = anchorRect.left - unionRect.left,
                        top = anchorRect.top - unionRect.top,
                        right = anchorRect.right - unionRect.left,
                        bottom = anchorRect.bottom - unionRect.top,
                    )
                    val unionConstraints = Constraints.fixed(unionRect.width, unionRect.height)
                    val placeables = subcompose(SLOT_SECONDARY) {
                        CascadingSecondaryContent(
                            triggerItem = activeExpanded,
                            unionSize = IntSize(unionRect.width, unionRect.height),
                            secondaryLocalInUnion = secondaryLocalInUnion,
                            anchorLocalInUnion = anchorLocalInUnion,
                            anchorPaddingTopPx = anchorPaddingTopPx,
                            isAnchorPopupLast = isAnchorPopupLast,
                            secondaryContentMaxHeight = resolvedMaxHeightPx,
                            enterFraction = enterFraction,
                            enterAlpha = enterAlpha,
                            popupLayoutPosition = layoutInfo.popupLayoutPosition,
                            transformOrigin = layoutInfo.localTransformOrigin,
                            expandFraction = expandFraction,
                            arrowRotation = arrowRotation,
                            onCollapseSecondary = onCollapseSecondary,
                            onLeafSelected = onLeafSelected,
                            dropdownColors = dropdownColors,
                            surfaceColor = surfaceColor,
                        )
                    }.map { it.measure(unionConstraints) }
                    SecondaryLayout(placeables, unionRect)
                } else {
                    null
                }
            } else {
                null
            }

        val host = hostOriginInWindow()
        layout(constraints.maxWidth, constraints.maxHeight) {
            val primaryDxLocal = primaryOffsetWindow.x - host.x
            val primaryDyLocal = primaryOffsetWindow.y - host.y
            var py = primaryDyLocal
            primaryPlaceables.forEach { placeable ->
                placeable.place(primaryDxLocal, py)
                py += placeable.height
            }
            secondaryLayout?.let { (placeables, unionRect) ->
                val ux = unionRect.left - host.x
                val uy = unionRect.top - host.y
                placeables.forEach { it.place(ux, uy) }
            }
        }
    }
}

private fun unionOf(a: IntRect, b: IntRect): IntRect = IntRect(
    left = minOf(a.left, b.left),
    top = minOf(a.top, b.top),
    right = maxOf(a.right, b.right),
    bottom = maxOf(a.bottom, b.bottom),
)

internal fun computeSecondaryRect(
    anchor: IntRect,
    secondarySize: IntSize,
    windowBounds: IntRect,
    layoutDirection: LayoutDirection,
): IntRect {
    val ltr = layoutDirection == LayoutDirection.Ltr
    // Leading-edge align; fall back to the opposite edge if the trailing side overflows.
    val left = if (ltr) {
        val leftAligned = anchor.left
        when {
            leftAligned + secondarySize.width <= windowBounds.right -> leftAligned

            anchor.right - secondarySize.width >= windowBounds.left ->
                windowBounds.right - secondarySize.width

            else -> windowBounds.left
        }
    } else {
        val rightAligned = anchor.right - secondarySize.width
        when {
            rightAligned >= windowBounds.left -> rightAligned
            anchor.left + secondarySize.width <= windowBounds.right -> anchor.left
            else -> (windowBounds.right - secondarySize.width).coerceAtLeast(windowBounds.left)
        }
    }
    val top = when {
        anchor.top + secondarySize.height <= windowBounds.bottom -> anchor.top
        anchor.bottom - secondarySize.height >= windowBounds.top -> anchor.bottom - secondarySize.height
        else -> (windowBounds.bottom - secondarySize.height).coerceAtLeast(windowBounds.top)
    }
    return IntRect(
        left = left,
        top = top,
        right = left + secondarySize.width,
        bottom = top + secondarySize.height,
    )
}

/** First/last/middle padding for [MorphHeaderRow]'s expansion-time padding interpolation.
 *  Must match the primary row's popup-global first/last logic or the morph's start frame jumps. */
private fun Density.computeAnchorPaddingPx(
    item: DropdownItem,
    entries: List<DropdownEntry>,
): Pair<Int, Int> {
    // Skip leading/trailing empty entries so the visually-first/last row keeps FirstLast padding.
    val firstNonEmptyIdx = entries.indexOfFirst { it.items.isNotEmpty() }
    val lastNonEmptyIdx = entries.indexOfLast { it.items.isNotEmpty() }
    entries.forEachIndexed { entryIdx, entry ->
        val idx = entry.items.indexOf(item)
        if (idx != -1) {
            val isPopupFirst = entryIdx == firstNonEmptyIdx && idx == 0
            val isPopupLast = entryIdx == lastNonEmptyIdx && idx == entry.items.lastIndex
            val top = if (isPopupFirst) {
                DropdownDefaults.FirstLastVerticalPadding.roundToPx()
            } else {
                DropdownDefaults.MiddleVerticalPadding.roundToPx()
            }
            val bottom = if (isPopupLast) {
                DropdownDefaults.FirstLastVerticalPadding.roundToPx()
            } else {
                DropdownDefaults.MiddleVerticalPadding.roundToPx()
            }
            return top to bottom
        }
    }
    return 0 to 0
}

private inline fun <T> List<T>.maxOfOrZero(selector: (T) -> Int): Int = if (isEmpty()) 0 else maxOf(selector)

private inline fun <T> List<T>.sumOfOrZero(selector: (T) -> Int): Int = if (isEmpty()) 0 else sumOf(selector)
