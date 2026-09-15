// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch

/**
 * A separate return track leaves the popup's existing opening/closing springs intact.
 * Completion retains the last gesture fraction through exit, avoiding a jump back to open. A
 * caller may keep it until the next opening when another element needs a seamless visual handoff.
 */
@Composable
internal fun rememberGlassPopupBackProgress(
    show: Boolean,
    active: Boolean,
    enabled: Boolean,
    retainWhenInactive: Boolean,
    resetSpec: AnimationSpec<Float>,
    onDismissRequest: () -> Unit,
): State<Float> {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(show, active, retainWhenInactive) {
        if (show || (!active && !retainWhenInactive)) progress.snapTo(0f)
    }

    // Glass popups also work in standalone Box hosts without a navigation dispatcher.
    if (LocalNavigationEventDispatcherOwner.current != null) {
        val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = backState,
            isBackEnabled = enabled,
            onBackCancelled = { scope.launch { progress.animateTo(0f, resetSpec) } },
            onBackCompleted = onDismissRequest,
        )
        LaunchedEffect(backState) {
            snapshotFlow { backState.transitionState }.collect { state ->
                if (
                    state is NavigationEventTransitionState.InProgress &&
                    state.direction == NavigationEventTransitionState.TRANSITIONING_BACK
                ) {
                    progress.snapTo(state.latestEvent.progress.coerceIn(0f, 1f))
                }
            }
        }
    }
    return progress.asState()
}

/** Preserve normal spring overshoot; only the external gesture fraction is clamped. */
internal fun popupFractionWithBack(fraction: Float, backProgress: Float): Float = fraction * (1f - backProgress.coerceIn(0f, 1f))
