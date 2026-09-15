// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.IconButtonDefaults
import top.yukonga.miuix.kmp.overlay.OverlayCascadingListPopup

/**
 * An [IconButton] wrapper that opens a cascading popup for one [DropdownEntry], using the popup's
 * default solid fill and shadow.
 */
@Composable
fun OverlayIconCascadingDropdownMenu(
    entry: DropdownEntry,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxHeight: Dp? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    renderInRootScaffold: Boolean = true,
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    content: @Composable () -> Unit,
) {
    OverlayIconCascadingDropdownMenu(
        entry = entry,
        modifier = modifier,
        enabled = enabled,
        maxHeight = maxHeight,
        dropdownColors = dropdownColors,
        renderInRootScaffold = renderInRootScaffold,
        collapseOnSelection = collapseOnSelection,
        surface = null,
        onExpandedChange = onExpandedChange,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        minHeight = minHeight,
        minWidth = minWidth,
        content = content,
    )
}

/**
 * An [IconButton] wrapper that opens an [OverlayCascadingListPopup] for one [DropdownEntry], with a
 * caller-provided panel surface. Pass `glassSurface(...)` from `miuix-glass` to give this popup the
 * OS4 glass material.
 *
 * Items whose [top.yukonga.miuix.kmp.basic.DropdownItem.children] is non-empty become submenu
 * triggers; cascading depth is limited to 2. Keep the entry and item order stable while the menu is
 * shown; item state such as [top.yukonga.miuix.kmp.basic.DropdownItem.selected] may change.
 *
 * @param surface Paints the popup panel, given the silhouette it must fill. Pass `null` explicitly
 *   to use the default shadow and fill.
 */
@Composable
fun OverlayIconCascadingDropdownMenu(
    entry: DropdownEntry,
    surface: (@Composable (Shape) -> Modifier)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxHeight: Dp? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    renderInRootScaffold: Boolean = true,
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    content: @Composable () -> Unit,
) {
    val entries = remember(entry) { listOf(entry) }
    OverlayIconCascadingDropdownMenu(
        entries = entries,
        modifier = modifier,
        enabled = enabled,
        maxHeight = maxHeight,
        dropdownColors = dropdownColors,
        renderInRootScaffold = renderInRootScaffold,
        collapseOnSelection = collapseOnSelection,
        surface = surface,
        onExpandedChange = onExpandedChange,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        minHeight = minHeight,
        minWidth = minWidth,
        content = content,
    )
}

/**
 * An [IconButton] wrapper that opens a cascading popup for [DropdownEntry] groups, using the
 * popup's default solid fill and shadow.
 */
@Composable
fun OverlayIconCascadingDropdownMenu(
    entries: List<DropdownEntry>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxHeight: Dp? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    renderInRootScaffold: Boolean = true,
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    content: @Composable () -> Unit,
) {
    OverlayIconCascadingDropdownMenu(
        entries = entries,
        modifier = modifier,
        enabled = enabled,
        maxHeight = maxHeight,
        dropdownColors = dropdownColors,
        renderInRootScaffold = renderInRootScaffold,
        collapseOnSelection = collapseOnSelection,
        surface = null,
        onExpandedChange = onExpandedChange,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        minHeight = minHeight,
        minWidth = minWidth,
        content = content,
    )
}

/**
 * An [IconButton] wrapper that opens an [OverlayCascadingListPopup] for one or more
 * [DropdownEntry] groups, with a caller-provided panel surface. Pass `glassSurface(...)` from
 * `miuix-glass` to give this popup the OS4 glass material. Items whose
 * [top.yukonga.miuix.kmp.basic.DropdownItem.children] is non-empty become submenu triggers;
 * cascading depth is limited to 2. Keep the entry and item order stable while the menu is shown;
 * item state such as
 * [top.yukonga.miuix.kmp.basic.DropdownItem.selected] may change.
 *
 * @param surface Paints the popup panel, given the silhouette it must fill. Pass `null` explicitly
 *   to use the default shadow and fill.
 */
@Composable
fun OverlayIconCascadingDropdownMenu(
    entries: List<DropdownEntry>,
    surface: (@Composable (Shape) -> Modifier)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxHeight: Dp? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    renderInRootScaffold: Boolean = true,
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    content: @Composable () -> Unit,
) {
    val isDropdownExpanded = remember { mutableStateOf(false) }
    val isHoldDown = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
    val currentOnExpandedChange = rememberUpdatedState(onExpandedChange)
    val setExpanded: (Boolean) -> Unit = remember {
        { expanded ->
            if (isDropdownExpanded.value != expanded) {
                isDropdownExpanded.value = expanded
                currentOnExpandedChange.value?.invoke(expanded)
            }
        }
    }

    val nonEmptyEntries = entries.filter { it.items.isNotEmpty() }
    val actualEnabled = enabled && nonEmptyEntries.isNotEmpty()
    val handleClick = remember(actualEnabled) {
        {
            if (actualEnabled) {
                setExpanded(!isDropdownExpanded.value)
                if (isDropdownExpanded.value) {
                    isHoldDown.value = true
                    currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
        }
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = handleClick,
            enabled = actualEnabled,
            holdDownState = isHoldDown.value,
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            minHeight = minHeight,
            minWidth = minWidth,
            content = content,
        )
        if (nonEmptyEntries.isNotEmpty()) {
            OverlayCascadingListPopup(
                show = isDropdownExpanded.value,
                entries = nonEmptyEntries,
                onDismissRequest = { setExpanded(false) },
                onDismissFinished = { isHoldDown.value = false },
                maxHeight = maxHeight,
                dropdownColors = dropdownColors,
                renderInRootScaffold = renderInRootScaffold,
                collapseOnSelection = collapseOnSelection,
                surface = surface,
            )
        }
    }
}
