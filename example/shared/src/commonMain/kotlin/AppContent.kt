// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBarPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import component.liquid.IosLiquidGlassNavigationBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import navigation.CrossActivityTransition
import navigation.Navigator
import navigation.Route
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.FabPosition
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.FloatingToolbarDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.ToolbarPosition
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.isInDarkTheme
import utils.FPSMonitor
import utils.shouldExpandNavigationRail
import utils.shouldShowSplitPane
import kotlin.math.abs

private object UIConstants {
    const val MAIN_PAGE_INDEX = 0
    const val ICON_PAGE_INDEX = 1
    const val COLOR_PAGE_INDEX = 2
    const val TEXT_STYLE_PAGE_INDEX = 3
    const val PAGE_COUNT = 5
    const val GITHUB_URL = "https://github.com/compose-miuix-ui/miuix"

    val PAGE_TITLES = listOf("Home", "Icon", "Color", "TextStyle", "Settings")
}

enum class FloatingNavigationBarAlignment(val value: Int) {
    Center(0),
    Start(1),
    End(2),
    ;

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: Center
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator found!") }
val LocalIsWideScreen = staticCompositionLocalOf { false }
val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@Composable
fun AppContent(
    padding: PaddingValues,
) {
    val appState = LocalAppState.current

    val pagerState = rememberPagerState(pageCount = { UIConstants.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)
    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    // miuix-nav back stack. The explicit <Route> supertype is required: a bare
    // rememberNavBackStack(Route.Main) would infer T = the Route.Main singleton type and fail to
    // encode the whole sealed hierarchy for save/restore. miuix-nav is reflection-free, so no
    // SerializersModule / SavedStateConfiguration is needed for the @Serializable sealed Route.
    val backStack = rememberNavBackStack<Route>(Route.Main)
    val navigator = remember { Navigator(backStack) }

    val navigationItems = remember {
        listOf(
            NavigationItem(UIConstants.PAGE_TITLES[0], MiuixIcons.Home),
            NavigationItem(UIConstants.PAGE_TITLES[1], MiuixIcons.Create),
            NavigationItem(UIConstants.PAGE_TITLES[2], MiuixIcons.Image),
            NavigationItem(UIConstants.PAGE_TITLES[3], MiuixIcons.Edit),
            NavigationItem(UIConstants.PAGE_TITLES[4], MiuixIcons.Settings),
        )
    }

    MainScreenBackHandler(mainPagerState, navigator)

    val isWideScreen = shouldShowSplitPane()

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalMainPagerState provides mainPagerState,
        LocalIsWideScreen provides isWideScreen,
    ) {
        // Follow the device screen corner so the slide-in clip matches the platform (0 on
        // Desktop/Web). The two-pane layout animates inside a mid-screen pane, so no screen corner.
        val navCornerRadius = if (isWideScreen) 0.dp else rememberNavSystemCornerRadius()
        val isCrossActivityStyle = appState.navTransitionStyle == 1
        val backdropColor = MiuixTheme.colorScheme.surface
        val isDarkTheme = isInDarkTheme()
        val effects = remember(
            appState.enableCornerClip,
            navCornerRadius,
            appState.enableDim,
            appState.blockInputDuringTransition,
            isCrossActivityStyle,
            backdropColor,
            isDarkTheme,
        ) {
            NavDisplayEffects(
                enableCornerClip = appState.enableCornerClip,
                // The cross-activity card should stay rounded on Desktop/Web too, where the
                // platform reports no screen corner.
                cornerClipRadius = if (isCrossActivityStyle && navCornerRadius <= 0.dp) 32.dp else navCornerRadius,
                // The cross-activity card scales the whole page (round all four corners); the
                // slide style rounds only the leading edge.
                cornerClipMode = if (isCrossActivityStyle) NavCornerClipMode.All else NavCornerClipMode.Leading,
                // The cross-activity style uses the platform scrim constants (0.2 light / 0.8
                // dark); the slide style keeps the established 0.5.
                // The scrim curve itself (hold-during-gesture for the card style, depth-linear
                // for the slide style) is owned by each transition via NavTransition.scrimFraction.
                dimAmount = when {
                    !appState.enableDim -> 0f
                    isCrossActivityStyle -> if (isDarkTheme) 0.8f else 0.2f
                    else -> 0.5f
                },
                blockInputDuringTransition = appState.blockInputDuringTransition,
                // Fill the area revealed around scaled-down layers with the page background, like
                // the platform's back-animation background layer.
                backdropColor = backdropColor,
            )
        }

        // Swipe-to-dismiss is opt-in in miuix-nav and off by default; the example gates it behind a
        // setting. Directions are physical (not mirrored), so pick per layout direction: rightward
        // back-swipe under LTR, leftward under RTL.
        val swipeBackDirection = when {
            !appState.enableSwipeBack -> NavSwipeDirection.None
            LocalLayoutDirection.current == LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
            else -> NavSwipeDirection.LeftToRight
        }

        // Global transition style: the established default, or the cross-activity feel — the
        // latter is a fully custom transition defined in this app (navigation/CrossActivityTransition.kt)
        // on the public API, not a library preset. Both are top-level singletons, so the
        // expression itself is stable across recompositions.
        val navTransition = if (isCrossActivityStyle) CrossActivityTransition else NavTransitions.MiuixDefault

        // Keep NavDisplay at one composition location while adaptive chrome changes around it.
        // Otherwise switching between the compact and wide branches recreates the current route.
        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen && appState.showNavigationBar) {
                PersistentNavigationRail(
                    navigationItems = navigationItems,
                    mainPagerState = mainPagerState,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    // Wide-screen transition layers must not draw over the navigation rail.
                    .then(if (isWideScreen) Modifier.clipToBounds() else Modifier),
            ) {
                AppNavHost(
                    backStack = backStack,
                    navigator = navigator,
                    transition = navTransition,
                    effects = effects,
                    swipeBackDirection = swipeBackDirection,
                    padding = padding,
                    navigationItems = navigationItems,
                    mainPagerState = mainPagerState,
                )
            }
        }
    }

    AnimatedVisibility(
        visible = appState.showFPSMonitor,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        FPSMonitor(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .captionBarPadding(),
        )
    }
}

@Composable
private fun AppNavHost(
    backStack: NavBackStack,
    navigator: Navigator,
    transition: NavTransition,
    effects: NavDisplayEffects,
    swipeBackDirection: NavSwipeDirection,
    padding: PaddingValues,
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
) {
    NavDisplay(
        backStack = backStack,
        onBack = { navigator.pop() },
        transition = transition,
        effects = effects,
    ) {
        entry<Route.Main>(swipeDismiss = swipeBackDirection) {
            Home(
                padding = padding,
                navigationItems = navigationItems,
                mainPagerState = mainPagerState,
            )
        }
        entry<Route.PullToRefresh>(swipeDismiss = swipeBackDirection) {
            PullToRefreshPage(padding = padding)
        }
        entry<Route.About>(swipeDismiss = swipeBackDirection) {
            AboutPage(padding = padding)
        }
        entry<Route.License>(swipeDismiss = swipeBackDirection) {
            LicensePage(padding = padding)
        }
        entry<Route.Navigation>(swipeDismiss = swipeBackDirection) { route ->
            val index = backStack.filterIsInstance<Route.Navigation>().indexOf(route) + 1
            NavTestPage(
                index = index,
                padding = padding,
            )
        }
        entry<Route.MultiScaffold>(swipeDismiss = swipeBackDirection) {
            MultiScaffoldTestPage(padding = padding)
        }
        entry<Route.NestedNav>(swipeDismiss = swipeBackDirection) {
            NestedNavTestPage(padding = padding)
        }
        entry<Route.OverscrollLoadMore>(swipeDismiss = swipeBackDirection) {
            OverscrollLoadMorePage(padding = padding)
        }
        entry<Route.Glass>(swipeDismiss = swipeBackDirection) {
            GlassPage(padding = padding)
        }
    }
}

/**
 * Wide-screen chrome: the always-visible navigation rail on the left of the two-pane layout.
 * A rail tab first pops the pane's stack back to the main route, then switches the pager.
 */
@Composable
private fun PersistentNavigationRail(
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
) {
    val appState = LocalAppState.current
    val navigator = LocalNavigator.current
    val page = mainPagerState.selectedPage
    val expandRail = shouldExpandNavigationRail()
    val railState = rememberNavigationRailState(
        initialValue = if (expandRail) NavigationRailValue.Expanded else NavigationRailValue.Collapsed,
    )
    LaunchedEffect(expandRail) {
        if (expandRail) railState.expand() else railState.collapse()
    }
    NavigationRail(
        state = railState,
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = page == index,
                onClick = {
                    navigator.popUntil { it is Route.Main }
                    mainPagerState.animateToPage(index)
                },
                icon = item.icon,
                label = item.label,
                badge = navigationItemBadge(index, appState.showNavigationBarBadge),
            )
        }
    }
}

@Composable
private fun Home(
    padding: PaddingValues,
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
) {
    val isWideScreen = LocalIsWideScreen.current
    val layoutDirection = LocalLayoutDirection.current
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerContent = remember(snackbarHostState, mainPagerState.pagerState) {
        movableContentOf<PaddingValues, Modifier> { pagerPadding, pagerModifier ->
            AppPager(
                snackbarHostState = snackbarHostState,
                padding = pagerPadding,
                pagerState = mainPagerState.pagerState,
                modifier = pagerModifier,
            )
        }
    }
    Scaffold(
        snackbarHost = {
            if (isWideScreen) {
                SnackbarHost(state = snackbarHostState)
            }
        },
    ) {
        if (isWideScreen) {
            WideScreenContent(
                layoutDirection = layoutDirection,
                pagerContent = pagerContent,
            )
        } else {
            CompactScreenLayout(
                navigationItems = navigationItems,
                snackbarHostState = snackbarHostState,
                padding = padding,
                mainPagerState = mainPagerState,
                pagerContent = pagerContent,
            )
        }
    }
}

@Composable
private fun WideScreenContent(
    layoutDirection: LayoutDirection,
    pagerContent: @Composable (PaddingValues, Modifier) -> Unit,
) {
    val appState = LocalAppState.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets =
        WindowInsets.systemBars.union(
            WindowInsets.displayCutout.exclude(
                WindowInsets.displayCutout.only(WindowInsetsSides.Start),
            ),
        ),
        floatingActionButton = {
            FloatingActionButton(show = appState.showFloatingActionButton)
        },
        floatingActionButtonPosition = appState.floatingActionButtonPosition.toFabPosition(),
        floatingToolbar = {
            FloatingToolbar(
                showFloatingToolbar = appState.showFloatingToolbar,
                floatingToolbarOrientation = appState.floatingToolbarOrientation,
            )
        },
        floatingToolbarPosition = appState.floatingToolbarPosition.toToolbarPosition(),
    ) { padding ->
        pagerContent(
            PaddingValues(top = padding.calculateTopPadding()),
            Modifier
                .imePadding()
                .padding(end = padding.calculateEndPadding(layoutDirection)),
        )
    }
}

@Composable
private fun CompactScreenLayout(
    navigationItems: List<NavigationItem>,
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
    mainPagerState: MainPagerState,
    pagerContent: @Composable (PaddingValues, Modifier) -> Unit,
) {
    val surfaceColor = MiuixTheme.colorScheme.surface
    val appState = LocalAppState.current
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                navigationItems = navigationItems,
                mainPagerState = mainPagerState,
                backdrop = backdrop,
                modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            )
        },
        floatingActionButton = {
            FloatingActionButton(show = appState.showFloatingActionButton)
        },
        floatingActionButtonPosition = appState.floatingActionButtonPosition.toFabPosition(),
        floatingToolbar = {
            FloatingToolbar(
                showFloatingToolbar = appState.showFloatingToolbar,
                floatingToolbarOrientation = appState.floatingToolbarOrientation,
            )
        },
        floatingToolbarPosition = appState.floatingToolbarPosition.toToolbarPosition(),
        snackbarHost = {
            SnackbarHost(state = snackbarHostState)
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            pagerContent(
                innerPadding,
                Modifier
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = padding.calculateStartPadding(LocalLayoutDirection.current),
                        end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    )
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun NavigationBar(
    navigationItems: List<NavigationItem>,
    mainPagerState: MainPagerState,
    backdrop: LayerBackdrop?,
    modifier: Modifier = Modifier,
) {
    val appState = LocalAppState.current
    val blurActive = appState.enableBlur && backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
    val page = mainPagerState.selectedPage

    AnimatedVisibility(
        visible = appState.showNavigationBar,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        AnimatedVisibility(
            visible = !appState.useFloatingNavigationBar,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (blurActive) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = 25f,
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(0.8f)),
                                    ),
                                ),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .background(barColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .then(modifier),
            ) {
                NavigationBar(
                    modifier = Modifier,
                    color = barColor,
                    mode = NavigationBarDisplayMode.entries[appState.navigationBarMode],
                ) {
                    navigationItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = page == index,
                            onClick = { mainPagerState.animateToPage(index) },
                            icon = item.icon,
                            label = item.label,
                            badge = navigationItemBadge(index, appState.showNavigationBarBadge),
                        )
                    }
                }
            }
        }
        if (appState.useFloatingNavigationBar) {
            val floatingBarColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer
            val floatingBarShape = RoundedCornerShape(FloatingToolbarDefaults.CornerRadius)
            val isDark = isInDarkTheme()
            val floatingHighlight = remember(isDark) {
                if (isDark) Highlight.GlassStrokeMiddleDark else Highlight.GlassStrokeMiddleLight
            }
            Box(modifier = modifier) {
                if (appState.floatingNavigationBarStyle == 1) {
                    IosLiquidGlassNavigationBar(
                        items = navigationItems,
                        selectedIndex = page,
                        onItemClick = { mainPagerState.animateToPage(it) },
                        backdrop = backdrop,
                        isBlurActive = blurActive,
                        badge = { navigationItemBadge(it, appState.showNavigationBarBadge) },
                    )
                } else {
                    FloatingNavigationBar(
                        modifier = if (blurActive) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = floatingBarShape,
                                blurRadius = 25f,
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(color = MiuixTheme.colorScheme.surfaceContainer.copy(0.6f)),
                                    ),
                                ),
                                highlight = floatingHighlight,
                            )
                        } else {
                            Modifier
                        },
                        color = floatingBarColor,
                        horizontalAlignment = FloatingNavigationBarAlignment.fromInt(appState.floatingNavigationBarPosition)
                            .toAlignment(),
                    ) {
                        navigationItems.forEachIndexed { index, item ->
                            FloatingNavigationBarItem(
                                selected = page == index,
                                onClick = { mainPagerState.animateToPage(index) },
                                icon = item.icon,
                                label = item.label,
                                badge = navigationItemBadge(index, appState.showNavigationBarBadge),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingActionButton(
    show: Boolean,
) {
    if (show) {
        val uriHandler = LocalUriHandler.current
        FloatingActionButton(
            onClick = {
                uriHandler.openUri(UIConstants.GITHUB_URL)
            },
        ) {
            Icon(
                imageVector = MiuixIcons.Link,
                tint = MiuixTheme.colorScheme.onPrimary,
                contentDescription = "GitHub",
            )
        }
    }
}

@Composable
private fun FloatingToolbar(
    showFloatingToolbar: Boolean,
    floatingToolbarOrientation: Int,
) {
    AnimatedVisibility(
        visible = showFloatingToolbar,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        FloatingToolbar(
            color = MiuixTheme.colorScheme.primary,
            cornerRadius = 20.dp,
        ) {
            AnimatedContent(
                targetState = floatingToolbarOrientation,
            ) { orientation ->
                val iconTint = MiuixTheme.colorScheme.onPrimary
                val content = @Composable {
                    IconButton(onClick = { /* Action 1 */ }) {
                        Icon(
                            MiuixIcons.Edit,
                            contentDescription = "Edit",
                            tint = iconTint,
                        )
                    }
                    IconButton(onClick = { /* Action 2 */ }) {
                        Icon(
                            MiuixIcons.Delete,
                            contentDescription = "Delete",
                            tint = iconTint,
                        )
                    }
                    IconButton(onClick = { /* Action 3 */ }) {
                        Icon(
                            MiuixIcons.More,
                            contentDescription = "More",
                            tint = iconTint,
                        )
                    }
                }
                when (orientation) {
                    0 -> Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) { content() }

                    else -> Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) { content() }
                }
            }
        }
    }
}

private fun Int.toFabPosition(): FabPosition = when (this) {
    0 -> FabPosition.Start
    1 -> FabPosition.Center
    2 -> FabPosition.End
    else -> FabPosition.EndOverlay
}

private fun Int.toToolbarPosition(): ToolbarPosition = when (this) {
    0 -> ToolbarPosition.TopStart
    1 -> ToolbarPosition.CenterStart
    2 -> ToolbarPosition.BottomStart
    3 -> ToolbarPosition.TopEnd
    4 -> ToolbarPosition.CenterEnd
    5 -> ToolbarPosition.BottomEnd
    6 -> ToolbarPosition.TopCenter
    else -> ToolbarPosition.BottomCenter
}

private fun FloatingNavigationBarAlignment.toAlignment(): Alignment.Horizontal = when (this) {
    FloatingNavigationBarAlignment.Center -> CenterHorizontally
    FloatingNavigationBarAlignment.Start -> Alignment.Start
    FloatingNavigationBarAlignment.End -> Alignment.End
}

/**
 * The optional badge for navigation item [index], shown only when [show] is enabled: a numeric badge
 * on one item and a dot badge on another to showcase both styles. Returns null when disabled.
 */
private fun navigationItemBadge(index: Int, show: Boolean): (@Composable () -> Unit)? = when {
    !show -> null
    index == 1 -> ({ Badge { Text("8") } })
    index == 3 -> ({ Badge() })
    else -> null
}

@Composable
fun AppPager(
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val appState = LocalAppState.current
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = appState.enablePageUserScroll,
        verticalAlignment = Alignment.Top,
        pageContent = { page ->
            when (page) {
                UIConstants.MAIN_PAGE_INDEX -> MainPage(snackbarHostState = snackbarHostState, padding = padding)
                UIConstants.ICON_PAGE_INDEX -> IconsPage(padding = padding)
                UIConstants.COLOR_PAGE_INDEX -> ColorPage(padding = padding)
                UIConstants.TEXT_STYLE_PAGE_INDEX -> TextStylePage(padding = padding)
                else -> SettingsPage(padding = padding)
            }
        },
    )
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navigator: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navigator.current() is Route.Main && navigator.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        },
    )
}

@Stable
class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()

        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.scroll(MutatePriority.UserInput) {
                    val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(2)
                    val duration = 100 * distance + 100
                    val layoutInfo = pagerState.layoutInfo
                    val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
                    val currentDistanceInPages = targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
                    val scrollPixels = currentDistanceInPages * pageSize

                    var previousValue = 0f
                    animate(
                        initialValue = 0f,
                        targetValue = scrollPixels,
                        animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                    ) { currentValue, _ ->
                        previousValue += scrollBy(currentValue - previousValue)
                    }
                }

                if (pagerState.currentPage != targetIndex) {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MainPagerState = remember(pagerState, coroutineScope) {
    MainPagerState(pagerState, coroutineScope)
}
