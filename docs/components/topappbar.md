# TopAppBar

`TopAppBar` is a top application bar component in Miuix, used to provide navigation, title, and action buttons at the top of the interface. It supports both large title and regular modes, as well as dynamic effects during scrolling.

This component is typically used in conjunction with the `Scaffold` component to maintain consistent layout and behavior across different pages in the application.

<div style="position: relative; height: 300px; border-radius: 10px; overflow: hidden; border: 1px solid #777;">
    <iframe id="demoIframe" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" src="../compose/index.html?id=topAppBar" title="Demo" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin"></iframe>
</div>

## Import

```kotlin
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
```

## Basic Usage

### Glass top bar material visibility

`GlassTopAppBar` from the optional `miuix-glass` module accepts a required
`isContentScrolled` parameter in a separate overload. Pass `listState.canScrollBackward`
to start the material transition when content leaves the top and reverse it on return,
even if the large title stays collapsed. The overlay mask uses a 100ms linear transition;
navigation and action button surfaces share a 350ms linear transition. The existing
Compose shadow appearance and scroll ramp are retained.

`GlassTopAppBarDefaults.HorizontalPadding` is 12dp on both sides of the top controls.
Use the same value for page cards and tabs to align their outer edges.

```kotlin
GlassTopAppBar(
    title = "Glass",
    isContentScrolled = listState.canScrollBackward,
    backdrop = backdrop,
    scrollBehavior = scrollBehavior,
    navigationIcon = {
        GlassIconButton(onClick = { navigator.pop() }) {
            Icon(
                imageVector = MiuixIcons.Back,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
            )
        }
    },
)
```

Both `navigationIcon` and `actions` render their content directly. Use `GlassIconButton`
in either slot for matching press feedback; the bar does not add a second glass surface.
Buttons inherit the bar's material, size, shape, fill, stroke and shadow defaults.
Replace a bare clickable navigation icon with `GlassIconButton(onClick = { navigator.pop() }) { Icon(...) }`.

The overload without `isContentScrolled` remains available and derives visibility from
`scrollBehavior.state.contentOffset`. Use the explicit overload when the list's exact
top position is available. A transforming popup anchored to a `GlassIconButton` inherits
the button's material and current surface opacity automatically.

### Small TopAppBar

```kotlin
Scaffold(
    topBar = {
        SmallTopAppBar(
            title = "Title",
            navigationIcon = {
                IconButton(onClick = { /* Handle click event */ }) {
                    Icon(MiuixIcons.Back, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Handle click event */ }) {
                    Icon(MiuixIcons.More, contentDescription = "More")
                }
            }
        )
    }
)
```

### Large TopAppBar

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = "Title",
            largeTitle = "Large Title", // If not specified, title value will be used
            navigationIcon = {
                IconButton(onClick = { /* Handle click event */ }) {
                    Icon(MiuixIcons.Back, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Handle click event */ }) {
                    Icon(MiuixIcons.More, contentDescription = "More")
                }
            }
        )
    }
)
```

## Large TopAppBar Scroll Behavior (Using Scaffold)

TopAppBar supports changing its display state when content scrolls:

```kotlin
val scrollBehavior = MiuixScrollBehavior()

Scaffold(
    topBar = {
        TopAppBar(
            title = "Title",
            largeTitle = "Large Title", // If not specified, title value will be used
            scrollBehavior = scrollBehavior
        )
    }
) { paddingValues ->
    // Content area needs to consider padding
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // If you want to add the overscroll effect, please add it before the scroll behavior
            .overScrollVertical()
            // Bind TopAppBar scroll behavior
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
    ) {
        // List content
    }
}
```

## Custom Styles

### Custom Colors

```kotlin
TopAppBar(
    title = "Title",
    color = MiuixTheme.colorScheme.primary,
    titleColor = MiuixTheme.colorScheme.onPrimary,
    largeTitleColor = MiuixTheme.colorScheme.onPrimary
)
```

### Custom Content Padding

```kotlin
TopAppBar(
    title = "Title",
    titlePadding = 32.dp
)
```

### Custom Icon Padding

```kotlin
TopAppBar(
    title = "Title",
    navigationIconPadding = 12.dp,
    actionIconPadding = 12.dp
)
```

## Properties

### TopAppBar Properties

| Property Name              | Type                            | Description                                    | Default Value                   | Required |
| -------------------------- | ------------------------------- | ---------------------------------------------- | ------------------------------- | -------- |
| title                      | String                          | Top bar title                                  | -                               | Yes      |
| modifier                   | Modifier                        | Modifier applied to the top bar                | Modifier                        | No       |
| color                      | Color                           | Top bar background color                       | MiuixTheme.colorScheme.surface  | No       |
| titleColor                 | Color                           | Color of the collapsed small title text        | MiuixTheme.colorScheme.onSurface | No       |
| largeTitle                 | String                          | Large title text                               | title                           | No       |
| largeTitleColor            | Color                           | Color of the expanded large title text         | MiuixTheme.colorScheme.onSurface | No       |
| subtitle                   | String                          | Subtitle text displayed below the title bar    | ""                              | No       |
| subtitleColor              | Color                           | Color of the subtitle text                     | MiuixTheme.colorScheme.onSurfaceVariantSummary | No       |
| navigationIcon             | @Composable () -> Unit          | Composable function for navigation icon area   | {}                              | No       |
| actions                    | @Composable RowScope.() -> Unit | Composable function for action buttons area    | {}                              | No       |
| scrollBehavior             | ScrollBehavior?                 | Controls top bar scroll behavior               | null                            | No       |
| defaultWindowInsetsPadding | Boolean                         | Whether to apply default window insets padding | true                            | No       |
| titlePadding          | Dp                              | Horizontal content padding                     | TopAppBarDefaults.TitlePadding | No       |
| navigationIconPadding      | Dp                              | Start padding of the navigation icon           | TopAppBarDefaults.NavigationIconPadding | No       |
| actionIconPadding          | Dp                              | End padding of the action icons                | TopAppBarDefaults.ActionIconPadding | No       |
| bottomContent              | @Composable () -> Unit          | Composable content displayed below the title bar area | {} | No |

### SmallTopAppBar Properties

| Property Name              | Type                            | Description                                    | Default Value                   | Required |
| -------------------------- | ------------------------------- | ---------------------------------------------- | ------------------------------- | -------- |
| title                      | String                          | Top bar title                                  | -                               | Yes      |
| modifier                   | Modifier                        | Modifier applied to the top bar                | Modifier                        | No       |
| color                      | Color                           | Top bar background color                       | MiuixTheme.colorScheme.surface  | No       |
| titleColor                 | Color                           | Color of the title text                        | MiuixTheme.colorScheme.onSurface | No       |
| subtitle                   | String                          | Subtitle text displayed below the title bar    | ""                              | No       |
| subtitleColor              | Color                           | Color of the subtitle text                     | MiuixTheme.colorScheme.onSurfaceVariantSummary | No       |
| navigationIcon             | @Composable () -> Unit          | Composable function for navigation icon area   | {}                              | No       |
| actions                    | @Composable RowScope.() -> Unit | Composable function for action buttons area    | {}                              | No       |
| scrollBehavior             | ScrollBehavior?                 | Controls top bar scroll behavior               | null                            | No       |
| defaultWindowInsetsPadding | Boolean                         | Whether to apply default window insets padding | true                            | No       |
| titlePadding          | Dp                              | Horizontal content padding                     | TopAppBarDefaults.TitlePadding | No       |
| navigationIconPadding      | Dp                              | Start padding of the navigation icon           | TopAppBarDefaults.NavigationIconPadding | No       |
| actionIconPadding          | Dp                              | End padding of the action icons                | TopAppBarDefaults.ActionIconPadding | No       |
| bottomContent              | @Composable () -> Unit          | Composable content displayed below the title bar area | {} | No |

### TopAppBarDefaults Object

The TopAppBarDefaults object provides default values for TopAppBar and SmallTopAppBar components.

#### Constants

| Constant Name             | Type | Description                                        | Default Value |
| ------------------------- | ---- | -------------------------------------------------- | ------------- |
| TitlePadding    | Dp   | Horizontal padding of the title and large title    | 26.dp         |
| NavigationIconPadding      | Dp   | Start padding of the navigation icon              | 16.dp         |
| ActionIconPadding          | Dp   | End padding of the action icons                   | 16.dp         |
| CollapsedHeight            | Dp   | Collapsed height of the TopAppBar                 | 52.dp         |
| SmallTopAppBarCenterHeight | Dp   | Vertical center height for SmallTopAppBar layout  | 50.dp         |
| LargeTitleBottomPadding    | Dp   | Bottom padding below the large title when no subtitle is present | 4.dp |
| SubtitleBottomPadding      | Dp   | Bottom padding below the subtitle (both large and small) | 8.dp  |

### ScrollBehavior

MiuixScrollBehavior is a configuration object used to control the scroll behavior of the top bar.

#### rememberTopAppBarState

Used to create and remember TopAppBarState:

```kotlin
val scrollBehavior = MiuixScrollBehavior(
    state = rememberTopAppBarState(),
    snapAnimationSpec = spring(stiffness = 2500f),
    canScroll = { true }
)
```

| Parameter Name     | Type                        | Default Value              | Description                                      |
| ------------------ | --------------------------- | -------------------------- | ------------------------------------------------ |
| state              | TopAppBarState              | rememberTopAppBarState()   | State object controlling scroll state            |
| canScroll          | () -> Boolean               | { true }                   | Callback to control whether scrolling is allowed |
| snapAnimationSpec  | AnimationSpec\<Float>?      | spring(stiffness = 2500f)  | Defines snap animation after scrolling           |
| flingAnimationSpec | DecayAnimationSpec\<Float>? | rememberSplineBasedDecay() | Defines decay animation for fling                |

## Advanced Usage

### Handling Window Insets

```kotlin
TopAppBar(
    title = "Title",
    largeTitle = "Large Title",
    defaultWindowInsetsPadding = false // Handle window insets manually
)
```

### Custom Scroll Behavior Animation

```kotlin
var isScrollingEnabled by remember { mutableStateOf(true) }
val scrollBehavior = MiuixScrollBehavior(
    snapAnimationSpec = tween(durationMillis = 100),
    flingAnimationSpec = rememberSplineBasedDecay(),
    canScroll = { isScrollingEnabled } // Can dynamically control whether scrolling is allowed
)

TopAppBar(
    title = "Title",
    largeTitle = "Large Title",
    scrollBehavior = scrollBehavior
)
```

### Combining Large and Small Titles

```kotlin
var useSmallTopBar by remember { mutableStateOf(false) }

Box(modifier = Modifier.fillMaxSize()) {
    if (useSmallTopBar) {
        SmallTopAppBar(
            title = "Compact Mode",
            navigationIcon = {
                IconButton(onClick = { useSmallTopBar = false }) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "Switch to Large Title",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        )
    } else {
        TopAppBar(
            title = "Title",
            largeTitle = "Expanded Mode",
            navigationIcon = {
                IconButton(onClick = { useSmallTopBar = true }) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "Switch to Small Title",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }
}
```

### Search transitions with glass top bars

`GlassTopAppBar` provides `contentModifier: Modifier = Modifier` for transforming
its foreground independently of the background band. Keep search-mode scale,
alpha and blur on `contentModifier`; `modifier` transforms the entire bar,
including the band that masks scrolling content.

Both `GlassTopAppBar` and `BlurTopAppBar` accept `titleAlpha: () -> Float = { 1f }`.
This is read during drawing and multiplies the existing collapse opacity of the
compact and large title containers, including the subtitle.

```kotlin
GlassTopAppBar(
    title = "Glass",
    contentModifier = Modifier.graphicsLayer { alpha = foregroundAlpha.value },
    titleAlpha = { titleOpacity.value },
)
```
