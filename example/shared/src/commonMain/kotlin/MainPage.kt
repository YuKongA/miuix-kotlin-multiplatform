// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.arrowSection
import component.badgeSection
import component.basicComponentSection
import component.blurSection
import component.bottomSheetSection
import component.breadcrumbBarSection
import component.buttonSection
import component.cardSection
import component.checkboxSection
import component.colorPickerSection
import component.dialogSection
import component.dropdownSection
import component.glassEntrySection
import component.numberPickerSection
import component.otherPageSection
import component.progressIndicatorSection
import component.radioButtonSection
import component.sliderSection
import component.snackbarSection
import component.spinnerSection
import component.switchSection
import component.tabRowSection
import component.textFieldSection
import component.tooltipSection
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi
import top.yukonga.miuix.kmp.menu.OverlayIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme
import utils.AdaptiveTopAppBar
import utils.BlurredBar
import utils.pageContentPadding
import utils.pageScrollModifiers
import utils.rememberBlurBackdrop

@Composable
fun MainPage(
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
) {
    val appState = LocalAppState.current
    val isWideScreen = LocalIsWideScreen.current
    var searchValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val notExpanded by remember { derivedStateOf { !expanded } }
    val onCancelSearch = remember {
        {
            expanded = false
            searchValue = ""
        }
    }

    val backdrop = rememberBlurBackdrop()
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface

    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val lazyListState = rememberLazyListState()

    var selectedIndex1 by remember { mutableIntStateOf(0) }
    var selectedIndex2 by remember { mutableIntStateOf(1) }
    var selectedIndex3 by remember { mutableIntStateOf(2) }
    val optionItems = remember(
        selectedIndex1,
        selectedIndex2,
        selectedIndex3,
    ) {
        listOf(
            DropdownEntry(
                items = listOf("Selection A-1", "Selection A-2")
                    .mapIndexed { index, text ->
                        DropdownItem(
                            text = text,
                            selected = selectedIndex1 == index,
                            onClick = { selectedIndex1 = index },
                        )
                    },
            ),
            DropdownEntry(
                items = listOf("Selection B-1", "Selection B-2", "Selection B-3")
                    .mapIndexed { index, text ->
                        DropdownItem(
                            text = text,
                            selected = selectedIndex2 == index,
                            onClick = { selectedIndex2 = index },
                        )
                    },
            ),
            DropdownEntry(
                items = listOf("Selection C-1", "Selection C-2", "Selection C-3", "Selection C-4")
                    .mapIndexed { index, text ->
                        DropdownItem(
                            text = text,
                            selected = selectedIndex3 == index,
                            onClick = { selectedIndex3 = index },
                        )
                    },
            ),
        )
    }
    var cascadingSortIndex by remember { mutableIntStateOf(0) }
    var cascadingViewIndex by remember { mutableIntStateOf(0) }
    var cascadingFilterIndex by remember { mutableIntStateOf(0) }
    var cascadingCollapseOnSelection by remember { mutableStateOf(false) }
    val cascadingEntries = remember(
        cascadingSortIndex,
        cascadingViewIndex,
        cascadingFilterIndex,
        cascadingCollapseOnSelection,
    ) {
        val sortLabels = listOf("Sort by capture date", "Sort by date added")
        val viewLabels = listOf("Group by date", "Compact")
        val filterLabels = listOf("All items", "Camera album")
        listOf(
            DropdownEntry(
                items = sortLabels.mapIndexed { idx, label ->
                    DropdownItem(
                        text = label,
                        selected = cascadingSortIndex == idx,
                        onClick = { cascadingSortIndex = idx },
                    )
                },
            ),
            DropdownEntry(
                items = listOf(
                    DropdownItem(
                        text = "Collapse On Selection",
                        selected = cascadingCollapseOnSelection,
                        onClick = {
                            cascadingCollapseOnSelection = !cascadingCollapseOnSelection
                        },
                    ),
                    DropdownItem(
                        text = "View mode",
                        children = viewLabels.mapIndexed { idx, label ->
                            DropdownItem(
                                text = label,
                                selected = cascadingViewIndex == idx,
                                onClick = { cascadingViewIndex = idx },
                            )
                        },
                    ),
                    DropdownItem(
                        text = "Filter",
                        children = filterLabels.mapIndexed { idx, label ->
                            DropdownItem(
                                text = label,
                                selected = cascadingFilterIndex == idx,
                                onClick = { cascadingFilterIndex = idx },
                            )
                        },
                    ),
                ),
            ),
        )
    }
    var multiSelectedItems by remember {
        mutableStateOf(
            setOf(
                "Multi selection A-1",
                "Multi selection B-2",
                "Multi selection B-3",
            ),
        )
    }
    val multiSelectItems = remember(multiSelectedItems) {
        listOf(
            DropdownEntry(
                items = listOf(
                    "Multi selection A-1",
                    "Multi selection A-2",
                ).map { text ->
                    DropdownItem(
                        text = text,
                        selected = text in multiSelectedItems,
                        onClick = {
                            multiSelectedItems =
                                if (text in multiSelectedItems) {
                                    multiSelectedItems - text
                                } else {
                                    multiSelectedItems + text
                                }
                        },
                    )
                },
            ),
            DropdownEntry(
                items = listOf(
                    "Multi selection B-1",
                    "Multi selection B-2",
                    "Multi selection B-3",
                ).map { text ->
                    DropdownItem(
                        text = text,
                        selected = text in multiSelectedItems,
                        onClick = {
                            multiSelectedItems =
                                if (text in multiSelectedItems) {
                                    multiSelectedItems - text
                                } else {
                                    multiSelectedItems + text
                                }
                        },
                    )
                },
            ),
        )
    }

    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive, topAppBarScrollBehavior) {
                AdaptiveTopAppBar(
                    title = "Home",
                    showTopAppBar = appState.showTopAppBar,
                    isWideScreen = isWideScreen,
                    scrollBehavior = topAppBarScrollBehavior,
                    color = barColor,
                    actions = {
                        TooltipBox(text = "Options") {
                            OverlayIconCascadingDropdownMenu(
                                entries = cascadingEntries,
                                collapseOnSelection = cascadingCollapseOnSelection,
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Tune,
                                    contentDescription = "Tune",
                                )
                            }
                        }
                        TooltipBox(text = "Sort") {
                            OverlayIconDropdownMenu(
                                entries = optionItems,
                                collapseOnSelection = false,
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Sort,
                                    contentDescription = "Sort",
                                )
                            }
                        }
                        TooltipBox(text = "Select all") {
                            OverlayIconDropdownMenu(
                                entries = multiSelectItems,
                                collapseOnSelection = false,
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.SelectAll,
                                    contentDescription = "SelectAll",
                                )
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        val contentPadding = pageContentPadding(innerPadding, padding, isWideScreen)
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
                item(key = "searchbar") {
                    SmallTitle(text = "SearchBar")
                    SearchBar(
                        modifier = Modifier.padding(bottom = 12.dp),
                        inputField = {
                            InputField(
                                query = searchValue,
                                onQueryChange = { searchValue = it },
                                onSearch = { expanded = false },
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                label = "Search",
                            )
                        },
                        outsideEndAction = {
                            Text(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable(
                                        interactionSource = null,
                                        indication = null,
                                        onClick = onCancelSearch,
                                    ),
                                text = "Cancel",
                                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.primary,
                            )
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        Column {
                            repeat(4) { idx ->
                                val resultText = "Suggestion $idx"
                                BasicComponent(
                                    title = resultText,
                                    onClick = {
                                        searchValue = resultText
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                if (notExpanded) {
                    glassEntrySection()
                    basicComponentSection()
                    switchSection()
                    checkboxSection()
                    radioButtonSection()
                    buttonSection()
                    tabRowSection()
                    breadcrumbBarSection()
                    arrowSection()
                    dialogSection()
                    bottomSheetSection()
                    dropdownSection()
                    spinnerSection()
                    snackbarSection(snackbarHostState)
                    textFieldSection()
                    progressIndicatorSection()
                    sliderSection()
                    cardSection()
                    tooltipSection()
                    badgeSection()
                    numberPickerSection()
                    colorPickerSection()
                    blurSection()
                    otherPageSection()
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = contentPadding,
            )
        }
    }
}
