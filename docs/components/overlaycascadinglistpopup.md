---
title: OverlayCascadingListPopup
requiresScaffoldHost: true
prerequisites:
  - Must be used within `Scaffold` to provide `MiuixPopupHost`
  - Using outside `Scaffold` will cause popup content not to render
  - Multiple nested or side-by-side `Scaffold`s are supported without extra configuration
hostComponent: Scaffold
popupHost: MiuixPopupHost
---

# OverlayCascadingListPopup

`OverlayCascadingListPopup` is a popup list that supports a two-level cascading menu. Items whose `DropdownItem.children` is non-empty become submenu triggers; tapping such an item morphs the popup into a secondary list anchored at the trigger row, while the primary list scales down behind a translucent mask. Cascading depth is limited to **2**.

<div style="position: relative; height: 410px; border-radius: 10px; overflow: hidden; border: 1px solid #777;">
    <iframe id="demoIframe" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" src="../compose/index.html?id=overlayCascadingListPopup" title="Demo" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin"></iframe>
</div>

::: danger Prerequisite
This component depends on `Scaffold` providing `MiuixPopupHost` to render popup content. It must be used within `Scaffold`, otherwise popup content will not render correctly.
:::

## Import

### OS4 glass secondary menus

The optional `miuix-glass` module provides `GlassSecondaryPopup` for a
`GlassTransformPopup` in a shared root `Box`. This is a separate glass API, not a
change to `OverlayCascadingListPopup` or its `surface` hook.

It grows a panel from the trigger row plus vertical content padding without fading
the panel or blurring its rows; the glass material retains its transparency.
All four clip edges move; the rows stay full-size. Placement aligns the start edge
(mirrored in RTL), shifting upward when space below is insufficient. Opening uses
the native default spring (0.95 / 0.35s); collapse uses 0.95 / 0.2s. The primary
popup's `stacked` state shrinks it to 0.95 with a mask using the same timing.

```kotlin
GlassSecondaryPopup(
    show = submenuExpanded,
    onDismissRequest = { submenuExpanded = false },
    anchorBounds = frozenTriggerBounds,
    backdrop = secondaryBackdrop,
    materialAnchor = menuAnchor, // same anchor as GlassTransformPopup
    sizing = GlassPopupSizing(minWidth = primaryMenuWidth),
    onDismissFinished = { submenuPresent = false },
) {
    GlassPopupItem(text = "Sort by", onClick = { submenuExpanded = false })
    GlassPopupItem(text = "Name", onClick = { /* select */ })
}
```

Keep the call composed during collapse. Freeze the trigger bounds before opening,
and resume capturing them only after `onDismissFinished`, not as soon as `show`
becomes false. Pass the primary button's `materialAnchor` to share blur,
colour treatment and bloom stroke; `visuals` still supplies opacity and shadow.
An explicit `backdrop` takes precedence; null falls back to the anchor backdrop. Back and outside
taps request collapse; choice callbacks decide whether to keep or dismiss menus.
Rows accept clicks during opening, but become non-interactive as soon as dismissal
is requested, including the primary `GlassTransformPopup` while it shrinks back
into its button. Secondary visibility must depend on the primary being open,
never reopen the primary in reverse. The existing
`GlassPopup(secondary = true)` signature delegates to this geometry, but does not
provide anchor material inheritance. See `GlassPage` for the paired example.

Apply `Modifier.layerBackdrop(secondaryBackdrop)` to a `Box` containing the page
and primary menu, keep the secondary outside that `Box`, and pass
`backdrop = secondaryBackdrop`. This includes the primary contents, scale and mask
in the secondary blur without sampling the secondary itself. See `GlassPage` for
the complete structure. The primary mask has its own alpha spring: 0.95 / 0.35s
on expansion and 0.95 / 0.2s on collapse, settling at `(1 / 256) × 0.75` and hiding
at alpha `1 / 256`, matching the source's AUTO_ALPHA behavior.

Predictive Back previews the secondary collapse first; with no secondary open,
it previews the primary transform back into its button. Cancellation springs back,
and completion continues closing from the current gesture fraction. A shared
`materialAnchor` synchronizes the primary scale and mask. Custom submenu arrows can
follow the same progress using
`arrowRotation = { rotation * (1f - menuAnchor.secondaryBackProgress) }`.
The host must provide a navigation event dispatcher for Back gestures.

The popup layer reserves space for the full shadow while its opacity changes, including predictive return to a button with no visible material at the top of the page.

### Overlay imports

```kotlin
import top.yukonga.miuix.kmp.overlay.OverlayCascadingListPopup
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
```

## Basic Usage

Build `entries` so that some `DropdownItem`s carry a non-empty `children` list. The component renders a chevron next to such trigger rows; tapping the trigger expands the second level inline.

```kotlin
var showPopup by remember { mutableStateOf(false) }
var sortIndex by remember { mutableStateOf(0) }
var viewIndex by remember { mutableStateOf(0) }

val sortLabels = listOf("Sort by capture date", "Sort by date added")
val viewLabels = listOf("Group by date", "Compact")
val entries = listOf(
    DropdownEntry(
        items = sortLabels.mapIndexed { idx, label ->
            DropdownItem(
                text = label,
                selected = sortIndex == idx,
                onClick = { sortIndex = idx },
            )
        },
    ),
    DropdownEntry(
        items = listOf(
            DropdownItem(
                text = "View mode",
                children = viewLabels.mapIndexed { idx, label ->
                    DropdownItem(
                        text = label,
                        selected = viewIndex == idx,
                        onClick = { viewIndex = idx },
                    )
                },
            ),
        ),
    ),
)

Scaffold {
    Box {
        TextButton(
            text = "Click to show menu",
            onClick = { showPopup = true },
        )
        OverlayCascadingListPopup(
            show = showPopup,
            entries = entries,
            onDismissRequest = { showPopup = false },
        )
    }
}
```

::: tip Cascading Depth
Cascading depth is capped at 2. Items at the secondary level cannot have their own `children`; deeper trees are silently ignored.
:::

## Component States

### Different Alignments

```kotlin
OverlayCascadingListPopup(
    show = showPopup,
    entries = entries,
    onDismissRequest = { showPopup = false },
    alignment = PopupPositionProvider.Align.Start,
)
```

### Disable Window Dimming

```kotlin
OverlayCascadingListPopup(
    show = showPopup,
    entries = entries,
    onDismissRequest = { showPopup = false },
    enableWindowDim = false,
)
```

### Keep Popup Open After Selection

By default the popup closes when the user picks any leaf item. Set `collapseOnSelection = false` to keep it open (e.g., for multi-select within a submenu).

```kotlin
OverlayCascadingListPopup(
    show = showPopup,
    entries = entries,
    onDismissRequest = { showPopup = false },
    collapseOnSelection = false,
)
```

## Properties

### OverlayCascadingListPopup

| Property Name         | Type                        | Description                                                                                                                                                                  | Default Value                              |
| --------------------- | --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| show                  | Boolean                     | Whether to show the popup.                                                                                                                                                   | -                                          |
| entries               | List\<DropdownEntry>        | Grouped dropdown entries; top-level items with non-empty `children` become submenu triggers.                                                                                 | -                                          |
| onDismissRequest      | () -> Unit                  | Called when the user requests dismissal (e.g., clicking outside, tapping the back button).                                                                                   | -                                          |
| popupModifier         | Modifier                    | Modifier applied to the popup body.                                                                                                                                          | Modifier                                   |
| onDismissFinished     | (() -> Unit)?               | Called after the exit animation finishes.                                                                                                                                    | null                                       |
| popupPositionProvider | PopupPositionProvider       | Position strategy for the primary popup relative to its anchor.                                                                                                              | ListPopupDefaults.DropdownPositionProvider |
| alignment             | PopupPositionProvider.Align | Alignment of the primary popup.                                                                                                                                              | PopupPositionProvider.Align.End            |
| enableWindowDim       | Boolean                     | Whether to dim the rest of the window while the popup is shown.                                                                                                              | true                                       |
| maxHeight             | Dp?                         | Maximum height of either side. Null bounds it by the safe area.                                                                                                              | null                                       |
| minWidth              | Dp                          | Minimum width of the popup.                                                                                                                                                  | 200.dp                                     |
| renderInRootScaffold  | Boolean                     | Whether to render the popup in the root (outermost) Scaffold. When true, the popup covers the full screen. When false, it renders within the current Scaffold's bounds with position compensation. | true |
| dropdownColors        | DropdownColors              | Colors used by every row.                                                                                                                                                    | DropdownDefaults.dropdownColors()          |
| collapseOnSelection   | Boolean                     | When true, selecting any leaf dismisses the popup.                                                                                                                           | true                                       |

### DropdownEntry

| Property Name | Type                | Description                                                                                            | Default Value | Required |
| ------------- | ------------------- | ------------------------------------------------------------------------------------------------------ | ------------- | -------- |
| items         | List\<DropdownItem> | Items shown in this dropdown group                                                                     | -             | Yes      |
| enabled       | Boolean             | Whether this group is enabled. False disables all items; true still respects each item's enabled state | true          | No       |

### DropdownItem

| Property Name | Type                              | Description                                                                                                                       | Default Value | Required |
| ------------- | --------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ------------- | -------- |
| text          | String                            | Text shown for the item                                                                                                           | -             | Yes      |
| enabled       | Boolean                           | Whether the item can be clicked. Disabled items are gray                                                                          | true          | No       |
| selected      | Boolean                           | Whether the item is selected                                                                                                      | false         | No       |
| onClick       | (() -> Unit)?                     | Callback invoked when the item is clicked. Ignored when `children` is non-empty (the click expands the submenu instead)            | null          | No       |
| icon          | @Composable ((Modifier) -> Unit)? | Icon shown before the item text                                                                                                   | null          | No       |
| summary       | String?                           | Summary text shown below the item text                                                                                            | null          | No       |
| children      | List\<DropdownItem>?              | Optional submenu items; only the cascading variants render these as a submenu (depth limited to 2)                                | null          | No       |

### DropdownColors

| Property Name          | Type  | Description                             |
| ---------------------- | ----- | --------------------------------------- |
| contentColor           | Color | Color of the option title               |
| summaryColor           | Color | Color of the option summary             |
| containerColor         | Color | Background color of the option          |
| selectedContentColor   | Color | Title color of the selected option      |
| selectedSummaryColor   | Color | Summary color of the selected option    |
| selectedContainerColor | Color | Background color of the selected option |
| selectedIndicatorColor | Color | Color of the selected indicator icon    |

### PopupPositionProvider.Align

| Value       | Description                                         |
| ----------- | --------------------------------------------------- |
| Start       | Aligns the popup to the start of the anchor.        |
| End         | Aligns the popup to the end of the anchor.          |
| TopStart    | Aligns the popup to the top-start of the anchor.    |
| TopEnd      | Aligns the popup to the top-end of the anchor.      |
| BottomStart | Aligns the popup to the bottom-start of the anchor. |
| BottomEnd   | Aligns the popup to the bottom-end of the anchor.   |
