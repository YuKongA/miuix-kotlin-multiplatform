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

/** The way into the glass page, kept at the top of the list while it is under active work. */
fun LazyListScope.glassEntrySection() {
    item(key = "glass-entry") {
        val navigator = LocalNavigator.current
        SmallTitle(text = "OS4")
        Card(modifier = Modifier.padding(horizontal = 12.dp)) {
            ArrowPreference(
                title = "Glass Components",
                summary = "Top bar, bottom bar, popup and dialog",
                onClick = { navigator.push(Route.Glass) },
            )
        }
    }
}
