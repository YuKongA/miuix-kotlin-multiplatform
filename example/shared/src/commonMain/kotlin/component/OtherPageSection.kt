// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package component

import LocalNavigator
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navigation.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import kotlin.random.Random

fun LazyListScope.otherPageSection() {
    item(key = "other") {
        val navigator = LocalNavigator.current
        SmallTitle(text = "Other")
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp),
        ) {
            ArrowPreference(
                title = "OS4 Glass Components",
                summary = "Bars, tabs, popup and dialog on glass",
                onClick = { navigator.push(Route.Glass) },
            )
            ArrowPreference(
                title = "PullToRefresh Test",
                summary = "Navigate to a PullToRefresh Page",
                onClick = {
                    navigator.push(Route.PullToRefresh)
                },
            )
            ArrowPreference(
                title = "Navigation test",
                summary = "Navigate to a Navigation Page",
                onClick = { navigator.push(Route.Navigation(Random.nextLong().toString())) },
            )
            ArrowPreference(
                title = "MultiScaffold Test",
                summary = "Navigate to a MultiScaffold Page",
                onClick = { navigator.push(Route.MultiScaffold) },
            )
            ArrowPreference(
                title = "Nested Navigation Test",
                summary = "A NavDisplay nested inside an entry",
                onClick = { navigator.push(Route.NestedNav) },
            )
            ArrowPreference(
                title = "Overscroll + Load More Test",
                summary = "Fling to the bottom, then fling again",
                onClick = { navigator.push(Route.OverscrollLoadMore) },
            )
        }
    }
}
