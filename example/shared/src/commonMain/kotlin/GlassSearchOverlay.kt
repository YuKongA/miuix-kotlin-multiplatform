// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.os4.Close
import top.yukonga.miuix.kmp.icon.os4.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

// EaseManager.SpringInterpolator (the nested legacy class) evaluates one second
// across the 400ms animator. The standalone SpringInterpolator is a different API.
private val SearchModeEasing: Easing = run {
    val omega = 2.0 * PI / 0.75f
    val stiffness = (omega * omega).toFloat()
    val damping = (0.98f * 2.0 * omega).toFloat()
    val decay = -damping / 2f
    val frequency = sqrt(-(damping.toDouble() * damping - stiffness.toDouble() * 4)).toFloat() / 2f
    val sineCoefficient = decay / frequency
    Easing { fraction ->
        (
            1.0 + exp((decay * fraction).toDouble()) *
                (-cos((frequency * fraction).toDouble()) + sineCoefficient * sin((frequency * fraction).toDouble()))
            ).toFloat()
    }
}

/** Example search mode, kept composed through its exit so Cancel can reverse the entrance. */
@Composable
internal fun GlassSearchOverlay(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val transition = updateTransition(expanded, label = "glassSearch")
    val progress = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchGeometry",
    ) { if (it) 1f else 0f }
    val cancelAlpha = transition.animateFloat(
        transitionSpec = { tween(150, delayMillis = if (targetState) 100 else 0, easing = LinearEasing) },
        label = "glassSearchCancelMaterial",
    ) { if (it) 1f else 0f }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val active = expanded || transition.currentState || transition.isRunning
    LaunchedEffect(expanded) {
        query = ""
        if (expanded) {
            focusRequester.requestFocus()
            keyboard?.show()
        } else if (active) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    if (!active) return
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = expanded,
        onBackCompleted = onDismissRequest,
    )
    val colors = MiuixTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    onDismissRequest()
                    true
                } else {
                    false
                }
            }
            .graphicsLayer { alpha = if (expanded) 1f else 0f }
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GlassTopAppBarDefaults.HorizontalPadding, vertical = 4.dp)
                // Without a content anchor, native search animates only its top inset.
                .graphicsLayer { translationY = -statusBarHeight * (1f - progress.value) },
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                enabled = expanded,
                singleLine = true,
                textStyle = MiuixTheme.textStyles.body1.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val endSpace = (54.dp.toPx() * progress.value).toInt()
                        val width = (constraints.maxWidth - endSpace).coerceAtLeast(0)
                        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                        layout(constraints.maxWidth, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .height(44.dp)
                    .focusRequester(focusRequester),
                decorationBox = { field ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surfaceContainerHigh, CircleShape)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(MiuixIcons.Os4.Search, contentDescription = null, modifier = Modifier.size(24.dp), tint = colors.disabledOnSurface)
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) Text("Search", color = colors.disabledOnSurface, style = MiuixTheme.textStyles.body1)
                            field()
                        }
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                focusRequester.requestFocus()
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(MiuixIcons.Os4.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = colors.disabledOnSurface)
                            }
                        }
                    }
                },
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .graphicsLayer {
                        val fraction = progress.value
                        translationX = 66.dp.toPx() * (1f - fraction) * if (direction == LayoutDirection.Ltr) 1f else -1f
                        scaleX = fraction
                        scaleY = fraction
                        alpha = cancelAlpha.value
                        val blur = 50f * (1f - cancelAlpha.value)
                        renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
                    },
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    enabled = expanded,
                    modifier = Modifier.size(44.dp).background(colors.surfaceContainerHigh, CircleShape),
                ) {
                    Icon(MiuixIcons.Os4.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp), tint = colors.onSurface)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = null, indication = null) { keyboard?.hide() },
            contentAlignment = Alignment.TopCenter,
        ) {
        }
    }
}

/** ActionBarView fades, shrinks to 0.9, and blurs while entering search mode. */
@Composable
internal fun Modifier.glassSearchBarTransition(expanded: Boolean): Modifier {
    val transition = updateTransition(expanded, label = "glassSearchTopBar")
    val scale = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchTopBarScale",
    ) { if (it) 0.9f else 1f }
    val opacity = transition.animateFloat(
        transitionSpec = {
            tween(150, easing = if (targetState) CubicBezierEasing(0f, 0f, 0.67f, 1f) else CubicBezierEasing(0.26f, 0f, 0.6f, 0.2f))
        },
        label = "glassSearchTopBarAlpha",
    ) { if (it) 0f else 1f }
    val blur = transition.animateFloat(
        transitionSpec = { tween(if (targetState) 150 else 120, easing = LinearEasing) },
        label = "glassSearchTopBarBlur",
    ) { if (it) 50f else 0f }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        alpha = opacity.value
        renderEffect = if (blur.value > 0.5f) BlurEffect(blur.value, blur.value, TileMode.Decal) else null
    }
}

/** ActionBarView reveals the title container after exit progress crosses 0.8. */
@Composable
internal fun rememberGlassSearchTitleAlpha(expanded: Boolean, collapsed: Boolean): () -> Float {
    val transition = updateTransition(expanded, label = "glassSearchTitlePhase")
    val progress = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchTitleExitProgress",
    ) { if (it) 0f else 1f }
    val reveal by remember(expanded, progress) { derivedStateOf { expanded || progress.value > 0.8f } }
    val response = if (collapsed) 0.15f else 0.6f
    val titleAlpha = animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = if (reveal && !expanded) {
            spring(dampingRatio = 1f, stiffness = (2 * PI / response).let { (it * it).toFloat() }, visibilityThreshold = 0.001f)
        } else {
            snap()
        },
        label = "glassSearchTitleAlpha",
    )
    return remember(titleAlpha) { { titleAlpha.value } }
}
