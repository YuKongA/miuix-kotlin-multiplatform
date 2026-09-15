// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupLayoutPosition
import top.yukonga.miuix.kmp.basic.popupClipReveal
import top.yukonga.miuix.kmp.squircle.isSquircleEnabled

private var deeperChildrenWarned = false

private val CascadeShadow: Shadow = Shadow(
    radius = 24.dp,
    color = Color.Black.copy(alpha = 0.18f),
    spread = 0.dp,
    offset = DpOffset(0.dp, 6.dp),
)

/** Primary side of a cascading popup. While a secondary is expanded, scales to
 *  [PRIMARY_SHRUNK_SCALE] under a translucent mask. */
@Composable
internal fun CascadingPrimaryContent(
    entries: List<DropdownEntry>,
    expandedItem: DropdownItem?,
    onExpand: (DropdownItem) -> Unit,
    onLeafSelected: (DropdownItem) -> Unit,
    onAnchorBounds: (DropdownItem, IntRect) -> Unit,
    enterFraction: () -> Float,
    enterAlpha: () -> Float,
    popupLayoutPosition: PopupLayoutPosition,
    transformOrigin: TransformOrigin,
    surfaceColor: Color,
    dropdownColors: DropdownColors,
    primaryScale: () -> Float,
    maskAlpha: () -> Float,
    maskColor: Color,
    onCollapseSecondary: () -> Unit,
    surface: (@Composable (Shape) -> Modifier)? = null,
) {
    val isExpanded = expandedItem != null
    val shape = remember { RoundedCornerShape(CascadingPopupCornerRadius) }
    Box(
        modifier = Modifier
            .graphicsLayer {
                val s = ENTER_SCALE_FROM + ENTER_SCALE_RANGE * enterFraction()
                scaleX = s
                scaleY = s
                alpha = enterAlpha()
                this.transformOrigin = transformOrigin
            }
            .graphicsLayer {
                val s = primaryScale()
                scaleX = s
                scaleY = s
                this.transformOrigin = transformOrigin
            }
            .popupClipReveal(enterFraction, popupLayoutPosition, CascadingPopupCornerRadius, isSquircleEnabled())
            .then(
                surface?.invoke(shape)
                    ?: Modifier.dropShadow(shape = shape, shadow = CascadeShadow).background(color = surfaceColor),
            ),
    ) {
        Box {
            ListPopupColumn {
                val lastEntryIdx = entries.lastIndex
                entries.forEachIndexed { entryIndex, entry ->
                    val lastItemIdx = entry.items.lastIndex
                    val isFirstEntry = entryIndex == 0
                    val isLastEntry = entryIndex == lastEntryIdx
                    entry.items.forEachIndexed { itemIndex, item ->
                        key(entry, item) {
                            CascadingPrimaryRow(
                                item = item,
                                optionSize = entry.items.size,
                                index = itemIndex,
                                entryEnabled = entry.enabled,
                                isFirst = isFirstEntry && itemIndex == 0,
                                isLast = isLastEntry && itemIndex == lastItemIdx,
                                dropdownColors = dropdownColors,
                                onClick = {
                                    val children = item.children?.takeIf { it.isNotEmpty() }
                                    if (children != null) {
                                        if (!deeperChildrenWarned &&
                                            children.any { !it.children.isNullOrEmpty() }
                                        ) {
                                            deeperChildrenWarned = true
                                            println(
                                                "[CascadingListPopup] Cascading depth is limited to 2;" +
                                                    " deeper children are ignored.",
                                            )
                                        }
                                        onExpand(item)
                                    } else {
                                        onLeafSelected(item)
                                    }
                                },
                                onAnchorBounds = onAnchorBounds,
                            )
                        }
                    }
                    if (entryIndex != lastEntryIdx) {
                        key("divider", entry) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                thickness = 1.5.dp,
                            )
                        }
                    }
                }
            }
        }
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = maskAlpha() }
                    .background(maskColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onCollapseSecondary() })
                    },
            )
        }
    }
}

@Composable
private fun CascadingPrimaryRow(
    item: DropdownItem,
    optionSize: Int,
    index: Int,
    entryEnabled: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    dropdownColors: DropdownColors,
    onClick: () -> Unit,
    onAnchorBounds: (DropdownItem, IntRect) -> Unit,
) {
    val rowEnabled = entryEnabled && item.enabled
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                onAnchorBounds(
                    item,
                    IntRect(
                        left = pos.x.toInt(),
                        top = pos.y.toInt(),
                        right = pos.x.toInt() + coords.size.width,
                        bottom = pos.y.toInt() + coords.size.height,
                    ),
                )
            },
        propagateMinConstraints = true,
    ) {
        DropdownImpl(
            item = item,
            optionSize = optionSize,
            isSelected = item.selected,
            index = index,
            dropdownColors = dropdownColors,
            enabled = rowEnabled,
            hasSubmenu = !item.children.isNullOrEmpty(),
            isFirst = isFirst,
            isLast = isLast,
            onSelectedIndexChange = { onClick() },
        )
    }
}

/** Secondary side of a cascading popup. Laid out at union size (primary ∪ secondary) with a
 *  single dynamic clip path that interpolates from the anchor row to the secondary rect. */
@Composable
internal fun CascadingSecondaryContent(
    triggerItem: DropdownItem,
    unionSize: IntSize,
    secondaryLocalInUnion: IntRect,
    anchorLocalInUnion: IntRect,
    anchorPaddingTopPx: Int,
    isAnchorPopupLast: Boolean,
    secondaryContentMaxHeight: Int,
    enterFraction: () -> Float,
    enterAlpha: () -> Float,
    popupLayoutPosition: PopupLayoutPosition,
    transformOrigin: TransformOrigin,
    expandFraction: () -> Float,
    arrowRotation: () -> Float,
    onCollapseSecondary: () -> Unit,
    onLeafSelected: (DropdownItem) -> Unit,
    dropdownColors: DropdownColors,
    surfaceColor: Color,
) {
    val cornerPx = with(LocalDensity.current) { CascadingPopupCornerRadius.toPx() }
    val anchorTopLeft = anchorLocalInUnion.topLeft
    val secondaryTopLeft = secondaryLocalInUnion.topLeft
    val secondaryWidth = secondaryLocalInUnion.width

    Box(
        modifier = Modifier
            .graphicsLayer {
                val s = ENTER_SCALE_FROM + ENTER_SCALE_RANGE * enterFraction()
                scaleX = s
                scaleY = s
                alpha = enterAlpha()
                this.transformOrigin = transformOrigin
            }
            .popupClipReveal(enterFraction, popupLayoutPosition, CascadingPopupCornerRadius, isSquircleEnabled())
            .drawWithCache {
                val innerPath = Path()
                onDrawWithContent {
                    val frac = expandFraction()
                    val rTop = cornerPx * frac
                    // Last-row anchors share primary's rounded bottom — keep both bottom corners
                    // full. Mid-list anchors must shrink uniformly into a flat internal row.
                    val rBottom = if (isAnchorPopupLast) cornerPx else rTop
                    val topRadius = CornerRadius(rTop, rTop)
                    val bottomRadius = CornerRadius(rBottom, rBottom)
                    innerPath.rewind()
                    innerPath.addRoundRect(
                        RoundRect(
                            left = lerp(
                                anchorLocalInUnion.left.toFloat(),
                                secondaryLocalInUnion.left.toFloat(),
                                frac,
                            ),
                            top = lerp(
                                anchorLocalInUnion.top.toFloat(),
                                secondaryLocalInUnion.top.toFloat(),
                                frac,
                            ),
                            right = lerp(
                                anchorLocalInUnion.right.toFloat(),
                                secondaryLocalInUnion.right.toFloat(),
                                frac,
                            ),
                            bottom = lerp(
                                anchorLocalInUnion.bottom.toFloat(),
                                secondaryLocalInUnion.bottom.toFloat(),
                                frac,
                            ),
                            topLeftCornerRadius = topRadius,
                            topRightCornerRadius = topRadius,
                            bottomRightCornerRadius = bottomRadius,
                            bottomLeftCornerRadius = bottomRadius,
                        ),
                    )
                    clipPath(innerPath) {
                        drawRect(
                            color = surfaceColor,
                            topLeft = androidx.compose.ui.geometry.Offset.Zero,
                            size = this.size,
                        )
                        this@onDrawWithContent.drawContent()
                    }
                }
            }
            // Occupy full union size so the inner content can slide between anchor and
            // secondary even when the anchor sits outside the secondary bounds.
            .layout { measurable, _ ->
                val placeable = measurable.measure(
                    Constraints.fixed(unionSize.width, unionSize.height),
                )
                layout(unionSize.width, unionSize.height) { placeable.place(0, 0) }
            },
    ) {
        // propagateMinConstraints stretches ListPopupColumn to secondaryWidth; without it the
        // column collapses to its intrinsic min and leaves an unclickable strip.
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val frac = expandFraction()
                    translationX = lerp(
                        anchorTopLeft.x.toFloat(),
                        secondaryTopLeft.x.toFloat(),
                        frac,
                    )
                    translationY = lerp(
                        anchorTopLeft.y.toFloat(),
                        secondaryTopLeft.y.toFloat(),
                        frac,
                    )
                }
                .layout { measurable, _ ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = secondaryWidth,
                            maxWidth = secondaryWidth,
                            minHeight = 0,
                            maxHeight = secondaryContentMaxHeight,
                        ),
                    )
                    // Place within union so the surface's drawWithCache sees a full-area drawContent.
                    layout(unionSize.width, unionSize.height) {
                        placeable.place(0, 0)
                    }
                },
            propagateMinConstraints = true,
        ) {
            CascadingSecondaryColumn(
                triggerItem = triggerItem,
                dropdownColors = dropdownColors,
                expandFraction = expandFraction,
                arrowRotation = arrowRotation,
                anchorHeightPx = anchorLocalInUnion.height,
                anchorPaddingTopPx = anchorPaddingTopPx,
                onHeaderClick = onCollapseSecondary,
                onLeafSelected = onLeafSelected,
            )
        }
    }
}

/** Shared header + divider + children body for the secondary popup. Used by both the real
 *  animated layout and the measurement probe so their sizes can't diverge. */
@Composable
internal fun CascadingSecondaryColumn(
    triggerItem: DropdownItem,
    dropdownColors: DropdownColors,
    expandFraction: () -> Float,
    arrowRotation: () -> Float,
    anchorHeightPx: Int,
    anchorPaddingTopPx: Int,
    onHeaderClick: () -> Unit,
    onLeafSelected: (DropdownItem) -> Unit,
) {
    val children = triggerItem.children.orEmpty()
    ListPopupColumn {
        MorphHeaderRow(
            triggerItem = triggerItem,
            arrowRotation = arrowRotation,
            expandFraction = expandFraction,
            anchorHeightPx = anchorHeightPx,
            anchorPaddingTopPx = anchorPaddingTopPx,
            dropdownColors = dropdownColors,
            onClick = onHeaderClick,
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 4.dp),
            thickness = 1.5.dp,
        )
        val lastChildIdx = children.lastIndex
        children.forEachIndexed { index, child ->
            key(child) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    propagateMinConstraints = true,
                ) {
                    DropdownImpl(
                        item = child,
                        optionSize = children.size,
                        isSelected = child.selected,
                        index = index,
                        dropdownColors = dropdownColors,
                        enabled = child.enabled,
                        hasSubmenu = false,
                        // Header owns popup-global "first"; only the trailing child owns "last".
                        isFirst = false,
                        isLast = index == lastChildIdx,
                        onSelectedIndexChange = { onLeafSelected(child) },
                    )
                }
            }
        }
    }
}
