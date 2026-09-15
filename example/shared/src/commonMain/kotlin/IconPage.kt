// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import component.SearchBarFake
import component.SearchPager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import utils.AdaptiveTopAppBar
import utils.All
import utils.BlurredBar
import utils.SearchStatus
import utils.pageContentPadding
import utils.pageScrollModifiers
import utils.rememberBlurBackdrop
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun IconsPage(
    padding: PaddingValues,
) {
    val appState = LocalAppState.current
    val isWideScreen = LocalIsWideScreen.current
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val density = LocalDensity.current

    // A lambda keeps the collapse fraction out of composition; always 0 on wide screens (pinned bar).
    val dynamicTopPadding: () -> Dp = remember(topAppBarScrollBehavior, isWideScreen) {
        {
            if (isWideScreen) 0.dp else 12.dp * (1f - topAppBarScrollBehavior.state.collapsedFraction)
        }
    }

    // Search state
    var searchStatus by remember { mutableStateOf(SearchStatus(label = "Search icons")) }
    val updateSearchStatus: (SearchStatus) -> Unit = { searchStatus = it }
    var searchOffsetY by remember { mutableStateOf(0.dp) }

    // Icon data
    var os4 by remember { mutableStateOf(false) }
    val allIcons = remember(os4) { if (os4) MiuixIcons.Os4.All else MiuixIcons.All }
    val regularIcons = remember(allIcons) { allIcons["Regular"] ?: emptyList() }
    val weightVariants: List<Pair<String, List<ImageVector>>> = remember(allIcons) {
        listOf("Light", "Normal", "Regular", "Medium", "Demibold").map { name ->
            name to (allIcons[name] ?: emptyList())
        }
    }
    val iconNames = remember(regularIcons) { regularIcons.map { it.name.substringBefore(".") } }

    // Accordion state — only one row may be expanded at a time. -1 = none.
    var expandedIndex by remember { mutableIntStateOf(-1) }

    // Search filtering
    val filteredIndices = remember(searchStatus.searchText, iconNames) {
        if (searchStatus.searchText.isBlank()) {
            emptyList()
        } else {
            iconNames.indices.filter {
                iconNames[it].contains(searchStatus.searchText, ignoreCase = true)
            }
        }
    }
    val searchResultStatus = remember(searchStatus.searchText, filteredIndices) {
        when {
            searchStatus.searchText.isBlank() -> SearchStatus.ResultStatus.DEFAULT
            filteredIndices.isEmpty() -> SearchStatus.ResultStatus.EMPTY
            else -> SearchStatus.ResultStatus.SHOW
        }
    }
    LaunchedEffect(searchResultStatus) {
        if (searchStatus.resultStatus != searchResultStatus) {
            searchStatus = searchStatus.copy(resultStatus = searchResultStatus)
        }
    }

    // Blur state
    val backdrop = rememberBlurBackdrop()
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface
    val searchCapsuleColor = if (blurActive) {
        colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
    } else {
        null
    }

    // Scroll state
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive, topAppBarScrollBehavior) {
                searchStatus.TopAppBarAnim(backgroundColor = barColor) {
                    AdaptiveTopAppBar(
                        title = "Icon",
                        showTopAppBar = appState.showTopAppBar,
                        isWideScreen = isWideScreen,
                        scrollBehavior = topAppBarScrollBehavior,
                        color = barColor,
                    ) {
                        Box(
                            modifier = Modifier
                                .alpha(if (searchStatus.isCollapsed()) 1f else 0f)
                                .onGloballyPositioned { coordinates ->
                                    with(density) {
                                        searchOffsetY = coordinates.positionInWindow().y.toDp()
                                    }
                                }
                                .then(
                                    if (searchStatus.isCollapsed()) {
                                        Modifier.pointerInput(Unit) {
                                            detectTapGestures {
                                                updateSearchStatus(searchStatus.copy(current = SearchStatus.Status.EXPANDING))
                                            }
                                        }
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {
                            SearchBarFake(searchStatus.label, dynamicTopPadding, searchCapsuleColor)
                        }
                    }
                }
            }
        },
        popupHost = {
            searchStatus.SearchPager(
                onSearchStatusChange = updateSearchStatus,
                offsetY = searchOffsetY,
                defaultResult = {},
                searchBarTopPadding = dynamicTopPadding,
                collapsedCapsuleColor = searchCapsuleColor,
            ) {
                items(
                    count = filteredIndices.size,
                    key = { filteredIndices[it] },
                ) { i ->
                    val index = filteredIndices[i]
                    Card(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicComponent(
                            title = iconNames[index],
                            startAction = {
                                Icon(
                                    imageVector = regularIcons[index],
                                    contentDescription = iconNames[index],
                                    tint = colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            onClick = {
                                searchStatus = searchStatus.copy(
                                    searchText = "",
                                    current = SearchStatus.Status.COLLAPSING,
                                )
                                coroutineScope.launch {
                                    delay(350.milliseconds)
                                    // item 0 = header, icon rows start at item 1
                                    lazyListState.animateScrollToItem(index + 1)
                                }
                            },
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(padding.calculateBottomPadding()))
                }
            }
        },
    ) { innerPadding ->
        val contentPadding = pageContentPadding(
            innerPadding,
            padding,
            isWideScreen,
        )
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.pageScrollModifiers(
                    appState.enableScrollEndHaptic,
                    appState.showTopAppBar,
                    topAppBarScrollBehavior,
                ),
                contentPadding = contentPadding,
            ) {
                item(key = "iconFamily") {
                    Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                        TextButton(text = if (os4) "OS3" else "OS3 ✓", onClick = {
                            os4 = false
                            expandedIndex = -1
                        })
                        TextButton(text = if (os4) "OS4 ✓" else "OS4", onClick = {
                            os4 = true
                            expandedIndex = -1
                        })
                    }
                }
                item(key = "iconsHeader") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .squircleClip(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp,
                            )
                            .background(colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Name",
                            modifier = Modifier.weight(1f),
                            style = MiuixTheme.textStyles.footnote1,
                            color = colorScheme.onSurfaceVariantActions,
                        )
                        Text(
                            text = "Tap to compare weights",
                            style = MiuixTheme.textStyles.footnote2,
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    }
                }
                items(
                    count = regularIcons.size,
                    key = { "icon_$it" },
                ) { index ->
                    val isLast = index == regularIcons.lastIndex
                    val bottomPadding = if (isLast) 6.dp else 0.dp
                    val expanded = expandedIndex == index
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .squircleClip(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomEnd = if (isLast) 16.dp else 0.dp,
                                bottomStart = if (isLast) 16.dp else 0.dp,
                            )
                            .background(colorScheme.surfaceContainer)
                            .clickable { expandedIndex = if (expanded) -1 else index }
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 6.dp)
                            .padding(bottom = bottomPadding),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = iconNames[index],
                                modifier = Modifier.weight(1f),
                                style = MiuixTheme.textStyles.body2,
                                color = colorScheme.onSurface,
                            )
                            Icon(
                                imageVector = regularIcons[index],
                                contentDescription = iconNames[index],
                                tint = colorScheme.onBackground,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(
                                imageVector = if (expanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                                tint = colorScheme.onSurfaceVariantActions,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                weightVariants.forEach { (label, icons) ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Icon(
                                            imageVector = icons[index],
                                            contentDescription = "${iconNames[index]} ($label)",
                                            tint = colorScheme.onBackground,
                                            modifier = Modifier.size(28.dp),
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            style = MiuixTheme.textStyles.footnote2,
                                            color = colorScheme.onSurfaceVariantActions,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = contentPadding,
            )
        }
    }
}
