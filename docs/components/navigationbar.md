# NavigationBar

`NavigationBar` is a bottom navigation bar component in Miuix, used to create navigation menus fixed at the bottom of applications. It supports 2 to 5 navigation items, offering different display modes (icon only, icon and text, icon with selected label).

`FloatingNavigationBar` is a floating-style bottom navigation bar component, also supporting 2 to 5 navigation items, showing icons only.

These components are typically used in conjunction with the `Scaffold` component to maintain consistent layout and behavior across different pages in the application.

<div style="position: relative; height: 300px; border-radius: 10px; overflow: hidden; border: 1px solid #777;">
    <iframe id="demoIframe" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" src="../compose/index.html?id=navigationBar" title="Demo" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin"></iframe>
</div>

## Import

```kotlin
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
```

## Basic Usage

### NavigationBar

The NavigationBar component can be used to create bottom navigation menus fixed to the bottom:

```kotlin
var selectedIndex by remember { mutableStateOf(0) }
val items = listOf("Home", "Profile", "Settings")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)

Scaffold(
    bottomBar = {
        NavigationBar {
            items.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
)
```

### FloatingNavigationBar

The FloatingNavigationBar component can be used to create floating navigation menus at the bottom:

```kotlin
var selectedIndex by remember { mutableStateOf(0) }
val items = listOf("Home", "Profile", "Settings")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)

Scaffold(
    bottomBar = {
        FloatingNavigationBar {
            items.forEachIndexed { index, label ->
                FloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
)
```

### Custom Colors

Both item components accept `colors` from `NavigationBarDefaults.navigationBarItemColors()`. For example, use the theme's primary color for the selected item:

```kotlin
val itemColors = NavigationBarDefaults.navigationBarItemColors(
    unselectedContentColor = MiuixTheme.colorScheme.onSurfaceContainer,
    selectedContentColor = MiuixTheme.colorScheme.primary,
)

NavigationBar {
    items.forEachIndexed { index, label ->
        NavigationBarItem(
            selected = selectedIndex == index,
            onClick = { selectedIndex = index },
            icon = icons[index],
            label = label,
            colors = itemColors,
        )
    }
}
```

Pass the same `colors = itemColors` to `FloatingNavigationBarItem` inside a `FloatingNavigationBar` to customize its icons.

Colors are base colors: their alpha is multiplied by `0.4` when unselected, `0.6` when unselected and pressed, and `0.5` when selected and pressed. Selected, unpressed content uses the supplied color unchanged. `Color.Transparent` stays transparent in every state.

## Component States

### Selected State

`NavigationBarItem` automatically handles the visual style of the selected item, displaying it with bold text and highlighting the icon/text. `FloatingNavigationBarItem` highlights the icon when selected.

## Properties

### NavigationBar Properties

| Property Name              | Type                     | Description                         | Default Value                         | Required |
| -------------------------- | ------------------------ | ----------------------------------- | ------------------------------------- | -------- |
| modifier                   | Modifier                 | Modifier applied to the nav bar     | Modifier                              | No       |
| color                      | Color                    | Background color of the nav bar     | MiuixTheme.colorScheme.surface        | No       |
| showDivider                | Boolean                  | Show top divider line or not        | true                                  | No       |
| defaultWindowInsetsPadding | Boolean                  | Apply default window insets padding | true                                  | No       |
| mode                       | NavigationBarDisplayMode    | Display mode for items              | NavigationBarDisplayMode.IconAndText     | No       |
| content                    | @Composable RowScope.()  | The content of the nav bar          | -                                     | Yes      |

### NavigationBarItem Properties

| Property Name | Type          | Description                      | Default Value | Required |
| ------------- | ------------- | -------------------------------- | ------------- | -------- |
| selected      | Boolean       | Whether the item is selected     | -             | Yes      |
| onClick       | () -> Unit    | Callback when the item is clicked| -             | Yes      |
| icon          | ImageVector   | Icon of the item                 | -             | Yes      |
| label         | String        | Label of the item                | -             | Yes      |
| modifier      | Modifier      | Modifier applied to the item     | Modifier      | No       |
| enabled       | Boolean       | Whether the item is enabled      | true          | No       |
| colors | NavigationBarItemColors | Icon and label base colors | NavigationBarDefaults.navigationBarItemColors() | No |
| badge         | (@Composable () -> Unit)? | Optional badge shown on the item's icon, e.g. a `Badge` | null | No |

### FloatingNavigationBar Properties

| Property Name              | Type                     | Description                             | Default Value                           | Required |
| -------------------------- | ------------------------ | --------------------------------------- | --------------------------------------- | -------- |
| modifier                   | Modifier                 | Modifier applied to the nav bar         | Modifier                                | No       |
| color                      | Color                    | Background color of the nav bar         | MiuixTheme.colorScheme.surfaceContainer | No       |
| cornerRadius               | Dp                       | Corner radius of the nav bar            | FloatingToolbarDefaults.CornerRadius    | No       |
| horizontalAlignment        | Alignment.Horizontal     | Horizontal alignment within its parent  | CenterHorizontally                      | No       |
| horizontalOutSidePadding   | Dp                       | Horizontal padding outside the nav bar  | FloatingNavigationBarDefaults.HorizontalOutSidePadding | No       |
| shadowElevation            | Dp                       | The shadow elevation of the nav bar     | FloatingNavigationBarDefaults.ShadowElevation          | No       |
| showDivider                | Boolean                  | Show divider line around the nav bar    | false                                   | No       |
| defaultWindowInsetsPadding | Boolean                  | Apply default window insets padding     | true                                    | No       |
| content                    | @Composable () -> Unit   | The content of the nav bar              | -                                       | Yes      |

### FloatingNavigationBarItem Properties

| Property Name | Type          | Description                      | Default Value | Required |
| ------------- | ------------- | -------------------------------- | ------------- | -------- |
| selected      | Boolean       | Whether the item is selected     | -             | Yes      |
| onClick       | () -> Unit    | Callback when the item is clicked| -             | Yes      |
| icon          | ImageVector   | Icon of the item                 | -             | Yes      |
| label         | String        | Label of the item                | -             | Yes      |
| modifier      | Modifier      | Modifier applied to the item     | Modifier      | No       |
| enabled       | Boolean       | Whether the item is enabled      | true          | No       |
| colors | NavigationBarItemColors | Icon and label base colors | NavigationBarDefaults.navigationBarItemColors() | No |
| badge         | (@Composable () -> Unit)? | Optional badge shown on the item's icon, e.g. a `Badge` | null | No |

### NavigationBarDefaults Object

The NavigationBarDefaults object provides default values for NavigationBar and NavigationBarItem components.

#### Constants

| Constant Name          | Type     | Description                                  | Default Value |
| ---------------------- | -------- | -------------------------------------------- | ------------- |
| ItemHeight             | Dp       | Item height                                  | 64.dp         |
| IconSize               | Dp       | Icon size                                    | 26.dp         |
| LabelFontSize          | TextUnit | Label font size                              | 12.sp         |
| IconTopPadding         | Dp       | Top padding for the icon                     | 8.dp          |
| BottomPadding          | Dp       | Bottom padding for the label                 | 8.dp          |
| SelectedPressedAlpha   | Float    | Alpha value for selected item when pressed   | 0.5f          |
| UnselectedPressedAlpha | Float    | Alpha value for unselected item when pressed | 0.6f          |
| UnselectedAlpha        | Float    | Alpha value for unselected item              | 0.4f          |

#### navigationBarItemColors()

A composable factory returning a remembered `NavigationBarItemColors`, shared by both navigation item components. Omitting `colors` preserves the default theme appearance.

| Parameter | Type | Description | Default Value |
| --------- | ---- | ----------- | ------------- |
| unselectedContentColor | Color | Base color for unselected content | MiuixTheme.colorScheme.onSurfaceContainer |
| selectedContentColor | Color | Base color for selected content | MiuixTheme.colorScheme.onSurfaceContainer |

### FloatingNavigationBarDefaults Object

The FloatingNavigationBarDefaults object provides default values for FloatingNavigationBar and FloatingNavigationBarItem components.

#### Constants

| Constant Name            | Type  | Description                         | Default Value |
| ------------------------ | ----- | ----------------------------------- | ------------- |
| HorizontalOutSidePadding | Dp    | Horizontal outside padding          | 36.dp         |
| ShadowElevation          | Dp    | Shadow elevation                    | 1.dp          |
| HorizontalPadding        | Dp    | Horizontal padding inside the bar   | 12.dp         |
| ItemSpacing              | Dp    | Spacing between items               | 12.dp         |
| IconSize                 | Dp    | Icon size                           | 28.dp         |
| IconPadding              | Dp    | Padding around the icon             | 10.dp         |
| SelectedPressedAlpha     | Float | Alpha for selected pressed item     | 0.5f          |
| UnselectedPressedAlpha   | Float | Alpha for unselected pressed item   | 0.6f          |
| UnselectedAlpha          | Float | Alpha for unselected item           | 0.4f          |

### NavigationBarDisplayMode Enum

| Value                 | Description                                  |
| --------------------- | -------------------------------------------- |
| IconAndText           | Show both icon and text                      |
| IconOnly              | Show icon only                               |
| IconWithSelectedLabel | Show icon always, show text only when selected|

## Advanced Usage

### NavigationBar

#### Custom Colors

```kotlin
NavigationBar(
    color = Color.Red.copy(alpha = 0.3f)
) {
    // ... items ...
}
```

#### Without Divider

```kotlin
NavigationBar(
    showDivider = false
) {
    // ... items ...
}
```

#### Handling Window Insets

```kotlin
NavigationBar(
    defaultWindowInsetsPadding = false // Handle window insets padding manually
) {
    // ... items ...
}
```

### FloatingNavigationBar

#### Custom Color and Corner Radius

```kotlin
FloatingNavigationBar(
    color = MiuixTheme.colorScheme.primaryContainer,
    cornerRadius = 28.dp
) {
    // ... items ...
}
```

#### Custom Alignment and Padding

```kotlin
FloatingNavigationBar(
    horizontalAlignment = Alignment.Start, // Align to start
    horizontalOutSidePadding = 16.dp // Set outside padding
) {
    // ... items ...
}
```

### Using with Page Navigation (Using Scaffold)

#### Using NavigationBar

```kotlin
val pages = listOf("Home", "Profile", "Settings")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)
var selectedIndex by remember { mutableStateOf(0) }

Scaffold(
    bottomBar = {
        NavigationBar {
            pages.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
) { paddingValues ->
    // Content area needs to consider padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Current Page: ${pages[selectedIndex]}",
            style = MiuixTheme.textStyles.title1
        )
    }
}
```

### GlassNavigationBar sizing

`GlassNavigationBar` in the optional `miuix-glass` module uses 28dp icons and 6dp
vertical content padding. Captions use the theme's `footnote2` style with an 11dp
font size (16dp at a system font scale of 1.6 or greater), converted to text units
without applying font scaling a second time. Captions support two lines and ellipsis.
`height` is a minimum (54dp by default), so longer captions can increase the height.
The default capsule shape follows the measured size. These defaults do not change
`NavigationBar` or `FloatingNavigationBar`.

```kotlin
GlassNavigationBar(
    items = items,
    selectedIndex = selectedIndex,
    onSelect = { selectedIndex = it },
    backdrop = backdrop,
    modifier = Modifier.padding(horizontal = 24.dp),
)
```

The GlassPage example additionally applies the four-item wide-screen width rule
(344dp when the overlap-adjusted available width exceeds 400dp). Its bottom margin
is 24dp when the navigation-bar inset is below 24dp, otherwise the inset plus 8dp.
Outer placement remains the caller's responsibility; the component does not add
system-bar insets itself.

During dragging, the indicator follows a spring (damping 1, response 0.15s).
Its trailing edge stretches by four times the pointer movement per event, capped
at 60px, and stops stretching when the leading edge reaches the bar boundary.
The rendered edges are limited to the first and last item bounds on every frame,
so spring overshoot cannot push the indicator beyond the outermost destinations.
Crossing destinations uses the directional edge springs. Release continues from
the animated bounds and velocities rather than snapping to the pointer position.
`onSelect` still fires on press and when dragging into another destination;
this does not introduce deferred selection on release.

#### Using FloatingNavigationBar

```kotlin
val pages = listOf("Home", "Profile", "Settings")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)
var selectedIndex by remember { mutableStateOf(0) }

Scaffold(
    bottomBar = {
        FloatingNavigationBar {
            pages.forEachIndexed { index, label ->
                FloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
) { paddingValues ->
    // Content area needs to consider padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Current Page: ${pages[selectedIndex]}",
            style = MiuixTheme.textStyles.title1
        )
    }
}
```
