// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BlurTopAppBar
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.glass.internal.drawGlassMask
import top.yukonga.miuix.kmp.glass.internal.drawGlassStroke
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Default values for [GlassTopAppBar] and [GlassIconButton]. */
object GlassTopAppBarDefaults {

    /** Start/end inset of the glass controls, aligned with the page's cards and tabs. */
    val HorizontalPadding: Dp = 12.dp

    /** Scroll threshold for the legacy shadow ramp. Material visibility is driven separately. */
    val RampStart: Dp = 0.dp

    /**
     * Scroll distance for the retained Compose shadow ramp. The mask and button material use
     * timed transitions independent of this distance.
     */
    val RampDistance: Dp = 32.dp

    /** How far the large title blurs out as it collapses. */
    val LargeTitleBlurRadius: Dp = 12.dp

    /** Diameter of the glass pill behind a bar button. */
    val ButtonSize: Dp = 44.dp

    /** Icon opacity while an action-bar button is held. */
    val PressedContentAlpha: Float = 0.6f

    private val NESTED_BLUR_LIGHT: Dp = 44.72.dp
    private val NESTED_BLUR_DARK: Dp = 63.25.dp

    /**
     * How far a bar has collapsed, in `[0, 1]`.
     *
     * @param scrollBehavior The behavior driving the bar. `null` reports a fully collapsed bar,
     *   which is what a bar over content that does not scroll wants.
     */
    @Composable
    fun collapseRamp(scrollBehavior: ScrollBehavior?): Float {
        val density = LocalDensity.current
        val startPx = with(density) { RampStart.toPx() }
        val rampPx = with(density) { RampDistance.toPx() }
        return scrollBehavior?.state?.let {
            ((-it.contentOffset - startPx) / rampPx).coerceIn(0f, 1f)
        } ?: 1f
    }

    /** The traditional fill used when backdrop material is unavailable. */
    @Composable
    fun buttonFill(): Color = if (isDarkTheme()) Color(0xFF2C2C2C) else Color.White

    /** The source system's `internal-pured-thin-glass` action-button material. */
    @Composable
    fun buttonMaterial(): GlassMaterial = GlassMaterials.puredThinGlass(isDarkTheme())

    /** Effective blur of the action bar's parent mask and a child's own 20dp mask blur. */
    @Composable
    internal fun nestedMaterialBlurRadius(): Dp = if (isDarkTheme()) NESTED_BLUR_DARK else NESTED_BLUR_LIGHT

    /** Parent `Mask.Pured_Regular` material applied before child button and tab materials. */
    @Composable
    internal fun actionBarUnderlayMaterial(): GlassMaterial = GlassMaterials.actionBarMask(isDarkTheme())

    /** The small bloom stroke paired with [buttonMaterial]. */
    @Composable
    fun buttonStroke(): GlassStroke = GlassStrokes.forTheme(
        isDark = isDarkTheme(),
        light = GlassStrokes.SmallLight,
        dark = GlassStrokes.SmallDark,
    )

    /** The state overlay painted behind a held action-bar icon. */
    @Composable
    fun buttonPressedOverlay(): Color = if (isDarkTheme()) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.10f)
    }

    /**
     * The band that dims the content as it passes under the bar: the page's own colour, laid back
     * over it, solid at the top edge and gone by the bottom.
     *
     * @param color The page's background colour.
     */
    @Stable
    fun bandBrush(color: Color): Brush = Brush.verticalGradient(
        0.00f to color,
        0.45f to color,
        0.58f to color.copy(alpha = 0.82f),
        0.72f to color.copy(alpha = 0.58f),
        0.86f to color.copy(alpha = 0.34f),
        1.00f to color.copy(alpha = 0f),
    )

    /** How far the band reaches past the bottom of the bar. */
    val BandOverhang: Dp = 28.dp

    @Composable
    private fun isDarkTheme(): Boolean = MiuixTheme.colorScheme.background.luminance() < 0.5f
}

internal data class GlassTopAppBarContext(
    val backdrop: Backdrop?,
    val material: GlassMaterial,
    val underlayMaterial: GlassMaterial?,
    val floating: Boolean,
    val alpha: Float,
    val shadowAlpha: Float,
    val materialProgress: State<Float>,
    val keepMaterial: Boolean,
    val style: GlassStyle,
    val size: Dp,
    val shape: GlassShape,
    val fill: Color,
    val stroke: GlassStroke?,
    val shadow: GlassShadow?,
)

internal val LocalGlassTopAppBarContext = staticCompositionLocalOf<GlassTopAppBarContext?> { null }

/**
 * A top bar the content scrolls *under*, deriving material visibility from [ScrollBehavior].
 * For an exact page-top signal, use the overload with `isContentScrolled` and pass the list's
 * `canScrollBackward` value. Both overloads retain the same public visual parameters.
 *
 * @param title The compact title, centred once the bar has collapsed.
 * @param backdrop The page behind the action bar. Its details remain visible through the controls,
 *   but their effective blur combines the bar's parent mask blur with their own 20dp mask blur so
 *   text and hard edges do not pass through unchanged. `null` uses [fill].
 * @param modifier The modifier applied to the bar.
 * @param largeTitle The title shown large before the content scrolls. Defaults to [title].
 * @param subtitle Optional line under the title.
 * @param largeTitleBlurRadius How far the large title blurs out as it collapses.
 * @param scrollBehavior The behavior driving the collapse. Without one the bar stays at full
 *   strength, which is what a bar over non-scrolling content wants.
 * @param bandBrush The band that dims the content passing under the bar.
 * @param bandOverhang How far the band reaches past the bottom of the bar.
 * @param style Compatibility style forwarded to the renderer. The default source-style button
 *   uses [GlassTopAppBarDefaults.buttonMaterial] with bionic shading disabled, so the style does
 *   not recolour that material.
 * @param alpha Opacity multiplier for the button pills, on top of their floating transition.
 * @param buttonShape Silhouette of the button pills. Defaults to a circle of [buttonSize].
 * @param buttonSize Diameter of the button pills.
 * @param buttonShadow The shadow under the button pills. `null` removes it.
 * @param fill Traditional fill behind the button pills when runtime shaders are unavailable.
 * @param stroke Optional bloom stroke on the button pills. The default is the source system's
 *   small action-button stroke.
 * @param defaultWindowInsetsPadding Whether to inset the bar for the status bar.
 * @param contentModifier Applied to the foreground bar, leaving its background band stationary.
 * @param titleAlpha Draw-phase opacity of the compact and large title containers.
 * @param navigationIcon The leading control. Use [GlassIconButton] for the same glass surface
 *   and press feedback as trailing controls. The slot is rendered without an extra surface.
 * @param actions Trailing controls. Wrap them in [GlassIconButton] to give them the same pill.
 * @param bottomContent Content pinned under the title, inside the bar. A tab row goes here: the
 *   band reaches over it, so the list passing underneath is dimmed before it meets the tabs.
 */
@Composable
fun GlassTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    largeTitle: String = title,
    subtitle: String = "",
    scrollBehavior: ScrollBehavior? = null,
    bandBrush: Brush = GlassTopAppBarDefaults.bandBrush(MiuixTheme.colorScheme.surface),
    bandOverhang: Dp = GlassTopAppBarDefaults.BandOverhang,
    largeTitleBlurRadius: Dp = GlassTopAppBarDefaults.LargeTitleBlurRadius,
    style: GlassStyle = GlassDefaults.Style,
    alpha: Float = 1f,
    buttonSize: Dp = GlassTopAppBarDefaults.ButtonSize,
    buttonShape: GlassShape = GlassShape(buttonSize / 2),
    fill: Color = GlassTopAppBarDefaults.buttonFill(),
    stroke: GlassStroke? = GlassTopAppBarDefaults.buttonStroke(),
    buttonShadow: GlassShadow? = GlassShadows.Regular,
    defaultWindowInsetsPadding: Boolean = true,
    titleAlpha: () -> Float = { 1f },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) = GlassTopAppBar(
    title = title,
    isContentScrolled = scrollBehavior?.state?.let { it.contentOffset < 0f } ?: true,
    modifier = modifier,
    backdrop = backdrop,
    largeTitle = largeTitle,
    subtitle = subtitle,
    scrollBehavior = scrollBehavior,
    bandBrush = bandBrush,
    bandOverhang = bandOverhang,
    largeTitleBlurRadius = largeTitleBlurRadius,
    style = style,
    alpha = alpha,
    buttonSize = buttonSize,
    buttonShape = buttonShape,
    fill = fill,
    stroke = stroke,
    buttonShadow = buttonShadow,
    defaultWindowInsetsPadding = defaultWindowInsetsPadding,
    contentModifier = contentModifier,
    titleAlpha = titleAlpha,
    navigationIcon = navigationIcon,
    actions = actions,
    bottomContent = bottomContent,
)

/**
 * A glass top bar with an explicit page-overlap state.
 *
 * @param title The compact title.
 * @param isContentScrolled Whether page content has left its resting position. For a lazy list,
 *   pass `listState.canScrollBackward`. This clears the material when the page returns to the
 *   top even if the large title stays collapsed. The mask animates over 100ms and both button
 *   surfaces share a 350ms transition. Other parameters behave as in the scroll-behaviour overload.
 */
@Composable
fun GlassTopAppBar(
    title: String,
    isContentScrolled: Boolean,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    largeTitle: String = title,
    subtitle: String = "",
    scrollBehavior: ScrollBehavior? = null,
    bandBrush: Brush = GlassTopAppBarDefaults.bandBrush(MiuixTheme.colorScheme.surface),
    bandOverhang: Dp = GlassTopAppBarDefaults.BandOverhang,
    largeTitleBlurRadius: Dp = GlassTopAppBarDefaults.LargeTitleBlurRadius,
    style: GlassStyle = GlassDefaults.Style,
    alpha: Float = 1f,
    buttonSize: Dp = GlassTopAppBarDefaults.ButtonSize,
    buttonShape: GlassShape = GlassShape(buttonSize / 2),
    fill: Color = GlassTopAppBarDefaults.buttonFill(),
    stroke: GlassStroke? = GlassTopAppBarDefaults.buttonStroke(),
    buttonShadow: GlassShadow? = GlassShadows.Regular,
    defaultWindowInsetsPadding: Boolean = true,
    titleAlpha: () -> Float = { 1f },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    val ramp = GlassTopAppBarDefaults.collapseRamp(scrollBehavior)
    val floating = isContentScrolled
    val transition = updateTransition(floating, label = "glassTopBarFloat")
    val maskProgress = transition.animateFloat(
        transitionSpec = { GlassMotion.topBarMask() },
        label = "glassTopBarMask",
    ) { if (it) 1f else 0f }
    val materialProgress = transition.animateFloat(
        transitionSpec = { GlassMotion.topBarButtonFloat() },
        label = "glassTopBarMaterial",
    ) { if (it) 1f else 0f }
    val keepMaterial = floating || materialProgress.value > 0f
    val baseMaterial = GlassTopAppBarDefaults.buttonMaterial()
    val material = if (keepMaterial && backdrop != null) {
        baseMaterial.copy(blurRadius = GlassTopAppBarDefaults.nestedMaterialBlurRadius())
    } else {
        baseMaterial
    }
    val underlayMaterial = GlassTopAppBarDefaults.actionBarUnderlayMaterial().takeIf { keepMaterial && backdrop != null }
    val buttonBackdrop = backdrop?.takeIf { isRuntimeShaderSupported() }

    Box(modifier = modifier) {
        val overhangPx = with(LocalDensity.current) { bandOverhang.toPx() }
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.alpha = maskProgress.value
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    scaleY = if (size.height > 0f) (size.height + overhangPx) / size.height else 1f
                }
                .background(bandBrush),
        )
        CompositionLocalProvider(
            LocalGlassTopAppBarContext provides GlassTopAppBarContext(
                backdrop = buttonBackdrop,
                material = material,
                underlayMaterial = underlayMaterial,
                floating = floating,
                alpha = alpha,
                shadowAlpha = ramp * alpha,
                materialProgress = materialProgress,
                keepMaterial = keepMaterial,
                style = style,
                size = buttonSize,
                shape = buttonShape,
                fill = fill,
                stroke = stroke,
                shadow = buttonShadow,
            ),
        ) {
            BlurTopAppBar(
                title = title,
                modifier = contentModifier,
                titleAlpha = titleAlpha,
                largeTitle = largeTitle,
                largeTitleBlurRadius = largeTitleBlurRadius,
                subtitle = subtitle,
                color = Color.Transparent,
                scrollBehavior = scrollBehavior,
                defaultWindowInsetsPadding = defaultWindowInsetsPadding,
                navigationIconPadding = GlassTopAppBarDefaults.HorizontalPadding,
                actionIconPadding = GlassTopAppBarDefaults.HorizontalPadding,
                navigationIcon = navigationIcon,
                actions = actions,
                bottomContent = bottomContent,
            )
        }
    }
}

/**
 * A round glass button, the shape the source system gives a bar control once its bar has collapsed.
 * When [glassPopupAnchor] is attached, the button shares its resolved backdrop and material with
 * [GlassTransformPopup] so the panel retains the same blur and colour treatment as it grows.
 *
 * @param onClick Called when the button is tapped.
 * @param backdrop Optional [Backdrop] behind the glass. Inside [GlassTopAppBar] the button inherits
 *   its page backdrop and effective nested blur; elsewhere `null` gives the traditional [fill].
 * @param modifier The modifier applied to the button.
 * @param surfaceAlpha Opacity of the pill. Inside [GlassTopAppBar], leave this at its default so
 *   the bar can drive the source-style floating transition.
 * @param style Compatibility style forwarded to the renderer. The source-style button body uses
 *   [GlassTopAppBarDefaults.buttonMaterial] without bionic shading.
 * @param size Diameter of the button.
 * @param shape The button's silhouette. Defaults to a circle of [size].
 * @param shadow The shadow under the button. `null` removes it.
 * @param stroke Optional bloom stroke along the rim. The default is the small action-button token.
 * @param content The icon inside.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    surfaceAlpha: Float = 1f,
    style: GlassStyle = LocalGlassTopAppBarContext.current?.style ?: GlassDefaults.Style,
    size: Dp = LocalGlassTopAppBarContext.current?.size ?: GlassTopAppBarDefaults.ButtonSize,
    shape: GlassShape = LocalGlassTopAppBarContext.current?.shape ?: GlassShape(size / 2),
    fill: Color = LocalGlassTopAppBarContext.current?.fill ?: GlassTopAppBarDefaults.buttonFill(),
    stroke: GlassStroke? = LocalGlassTopAppBarContext.current.let { if (it != null) it.stroke else GlassTopAppBarDefaults.buttonStroke() },
    shadow: GlassShadow? = LocalGlassTopAppBarContext.current.let { if (it != null) it.shadow else GlassShadows.Regular },
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val topBarContext = LocalGlassTopAppBarContext.current
    GlassButtonSurface(
        backdrop = topBarContext?.backdrop ?: backdrop,
        floating = topBarContext?.floating ?: (surfaceAlpha > 0.01f),
        surfaceAlpha = surfaceAlpha * (topBarContext?.alpha ?: 1f),
        shadowAlpha = surfaceAlpha * (topBarContext?.shadowAlpha ?: 1f),
        style = style,
        material = topBarContext?.material ?: GlassTopAppBarDefaults.buttonMaterial(),
        underlayMaterial = topBarContext?.underlayMaterial,
        shape = shape,
        size = size,
        fill = fill,
        stroke = stroke,
        shadow = shadow,
        sharedProgress = topBarContext?.materialProgress,
        sharedKeepMaterial = topBarContext?.keepMaterial,
        pressed = pressed,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        content = content,
    )
}

/** The surface and press feedback used by [GlassIconButton] in either top-bar slot. */
@Composable
// foldIn only reads the anchor marker; the modifier is still applied to exactly one root Box.
@Suppress("ktlint:compose:modifier-reused-check", "ktlint:compose:state-param-check")
private fun GlassButtonSurface(
    backdrop: Backdrop?,
    floating: Boolean,
    surfaceAlpha: Float,
    shadowAlpha: Float,
    style: GlassStyle,
    material: GlassMaterial,
    underlayMaterial: GlassMaterial?,
    fill: Color,
    shape: GlassShape,
    size: Dp,
    stroke: GlassStroke?,
    shadow: GlassShadow?,
    modifier: Modifier = Modifier,
    pressed: Boolean = false,
    // Retain State for draw-phase reads; the bar owns the shared animation clock.
    sharedProgress: State<Float>? = null,
    sharedKeepMaterial: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val popupAnchor = modifier.foldIn<GlassPopupAnchor?>(null) { found, element ->
        if (element is GlassPopupAnchorElement) element.anchor else found
    }
    DisposableEffect(popupAnchor) {
        onDispose { popupAnchor?.surface = null }
    }
    val progress = sharedProgress ?: run {
        val floatingTransition = updateTransition(targetState = floating, label = "glassTopBarButtonFloat")
        floatingTransition.animateFloat(
            transitionSpec = { GlassMotion.topBarButtonFloat() },
            label = "glassTopBarButtonMaterial",
        ) { active ->
            if (active) 1f else 0f
        }
    }
    val keepMaterial = sharedKeepMaterial ?: (floating || progress.value > 0f)
    val opacity = surfaceAlpha.coerceIn(0f, 1f)
    SideEffect {
        popupAnchor?.surface = GlassAnchorSurface(backdrop, style, material, underlayMaterial, stroke, fill)
        popupAnchor?.surfaceProgress = progress
        popupAnchor?.surfaceOpacity = opacity
        popupAnchor?.surfaceFloating = floating
    }
    val pressTransition = updateTransition(targetState = pressed, label = "glassTopBarButtonPress")
    val contentAlpha by pressTransition.animateFloat(
        transitionSpec = { if (targetState) GlassMotion.pressDown() else GlassMotion.pressUp() },
        label = "glassTopBarButtonContentAlpha",
    ) { down ->
        if (down) GlassTopAppBarDefaults.PressedContentAlpha else 1f
    }
    val overlayAlpha by pressTransition.animateFloat(
        transitionSpec = { if (targetState) GlassMotion.pressDown() else GlassMotion.pressUp() },
        label = "glassTopBarButtonOverlayAlpha",
    ) { down ->
        if (down) 1f else 0f
    }
    val pressedOverlay = GlassTopAppBarDefaults.buttonPressedOverlay()

    Box(
        modifier = modifier
            .size(size)
            .glassShadow(shape, shadow, shadowAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = opacity * progress.value }
                .then(
                    if (backdrop != null) {
                        if (underlayMaterial != null) {
                            Modifier.glassOnActionBar(
                                backdrop = backdrop,
                                shape = shape,
                                style = style,
                                alpha = 1f,
                                material = material,
                                underlayMaterial = underlayMaterial,
                                stroke = stroke.takeIf { keepMaterial },
                                enabled = keepMaterial,
                            )
                        } else {
                            Modifier.glass(
                                backdrop = backdrop,
                                shape = shape,
                                style = style,
                                // Fade the resolved surface as one layer: colour-layer alpha alone
                                // cannot hide the opaque backdrop image when shading is disabled.
                                alpha = 1f,
                                material = material,
                                stroke = stroke.takeIf { keepMaterial },
                                enabled = keepMaterial,
                                shading = false,
                            )
                        }
                    } else {
                        Modifier
                            .then(
                                if (isRuntimeShaderSupported()) {
                                    Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                } else {
                                    Modifier.clip(shape)
                                },
                            )
                            .background(fill)
                            .drawWithContent {
                                drawContent()
                                if (stroke != null && keepMaterial) {
                                    drawGlassStroke(shape, layoutDirection, stroke, 1f)
                                }
                                drawGlassMask(shape, layoutDirection)
                            }
                    },
                ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(pressedOverlay.copy(alpha = pressedOverlay.alpha * overlayAlpha.coerceIn(0f, 1f))),
        )
        Box(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha.coerceIn(0f, 1f) },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
