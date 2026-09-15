// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.layout.CascadingPopupDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Default values for [GlassPopup]. */
object GlassPopupDefaults {

    /** Narrowest a popup is allowed to be: `miuix_popup_window_min_width`. */
    val MinWidth: Dp = 200.dp

    /** Widest: `miuix_popup_window_max_width`. */
    val MaxWidth: Dp = 288.dp

    /** Tallest, before the list inside starts to scroll: `miuix_popup_window_max_height`. */
    val MaxHeight: Dp = 416.dp

    /** Gap the popup keeps from the edge of the window: `miuix_popup_window_safe_margin`. */
    val SafeMargin: Dp = 12.dp

    /** Padding above the first item and below the last: `miuix_popup_window_vertical_padding`. */
    val ContentPaddingVertical: Dp = 8.dp

    /**
     * Corner radius of an anchored menu: `miuix_theme_radius_demi_big`.
     *
     * The wider of the two the source offers. Read off a menu's left edge at four depths, the
     * corner runs far further in than a 16dp one would — it is the demi-big token, not the common
     * one the popup window's own background uses.
     */
    val CornerRadius: Dp = 24.dp

    /** Corner radius of a list dropdown: `miuix_theme_radius_demi_big`. */
    val DropdownCornerRadius: Dp = 24.dp

    /** Shortest an item is allowed to be: `miuix_appcompat_popup_menu_item_min_height`. */
    val ItemMinHeight: Dp = 44.dp

    /** Padding inside a menu item: `miuix_appcompat_popup_menu_item_padding_left`. */
    val ItemPaddingHorizontal: Dp = 20.dp

    /** Padding above and below an item's label: `miuix_appcompat_popup_menu_item_padding_vertical`. */
    val ItemPaddingVertical: Dp = 10.dp

    /** Size of a row's leading icon: `miuix_appcompat_menu_icon_width`. */
    val ItemIconSize: Dp = 24.dp

    /** Gap between a row's label and anything beside it. */
    val ItemIconGap: Dp = 8.dp

    /**
     * Side inset of the line between two groups of rows: `popup_menu_divider_line`'s own inset.
     *
     * The source draws the line as a layer-list inside a 16dp block, inset 20dp on each side and
     * 7.75dp above and below. The block is what keeps the two groups apart; the line only marks
     * where the gap is.
     */
    val DividerPaddingHorizontal: Dp = 20.dp

    /** Space above and below that line, which is the rest of its 16dp block. */
    val DividerPaddingVertical: Dp = 7.75.dp

    /** Thickness of that line: the 0.5dp the block has left once its insets are taken. */
    val DividerThickness: Dp = 0.5.dp

    /** Size of the tick on the current row. */
    val ItemTickSize: Dp = 24.dp

    /** Size of the chevron on a row that leads to a further menu. */
    val ItemArrowSize: Dp = 16.dp

    /**
     * How far the block under a pressed row is held back from the panel's own edges.
     *
     * Measured off the source: the panel's inner surface starts 19px in from its rim on a 2.75
     * screen and the pressed block starts at 19px past that, which is 7dp. It is not the row's own
     * 20dp padding — the icon's left edge sits at that, well inside the block.
     */
    val ItemPressInset: Dp = 7.dp

    /**
     * Corner radius of that block.
     *
     * Measured off the source at 41px on a 2.75 screen, horizontally and vertically alike. Near
     * enough concentric with the panel's own 24dp corner across the 7dp inset, which is what makes
     * the two curves read as belonging to each other rather than as a box inside a box.
     */
    val ItemPressRadius: Dp = 15.dp

    /**
     * Fill of that block.
     *
     * Measured at 224 over a panel of 248, which is a tenth of the opposite tone. The dark side is
     * the same figure mirrored and has not been measured against a dark source page.
     */
    @Composable
    fun itemPressedColor(): Color = if (MiuixTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }

    /**
     * The wash laid over a menu while a second one stands in front of it.
     *
     * `hyperMenuMask`, which the source resolves to white at 0.4 on a light theme and black at 0.4
     * on a dark one. It lightens rather than dims on a light page: the menu behind has not gone
     * away, it has gone out of focus.
     */
    @Composable
    fun maskColor(): Color = if (MiuixTheme.colorScheme.background.luminance() < 0.5f) {
        Color.Black.copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }

    /** What a menu's surface is made of, with the source's own tokens as the defaults. */
    @Composable
    fun visuals(
        style: GlassStyle = GlassDefaults.Style,
        alpha: Float = 1f,
        stroke: GlassStroke? = stroke(),
        shadow: GlassShadow? = GlassShadows.Regular,
        material: GlassMaterial? = material(),
        containerColor: Color = containerColor(),
    ): GlassPopupVisuals = GlassPopupVisuals(style, alpha, stroke, shadow, material, containerColor)

    /** The popup's silhouette. */
    @Composable
    fun shape(cornerRadius: Dp = CornerRadius): GlassShape = GlassShape(cornerRadius)

    /** The popup's own body: `popupview-glass`, with the source token's 60dp mask blur. */
    @Composable
    fun material(): GlassMaterial = GlassMaterials.popupViewGlass(
        isDark = MiuixTheme.colorScheme.background.luminance() < 0.5f,
    )

    /** The small bloom stroke declared by `popupview-glass`. */
    @Composable
    fun stroke(): GlassStroke = GlassStrokes.forTheme(
        isDark = MiuixTheme.colorScheme.background.luminance() < 0.5f,
        light = GlassStrokes.SmallLight,
        dark = GlassStrokes.SmallDark,
    )

    /**
     * Fill behind the popup when it carries no material.
     *
     * `miuix_default_color_surface_highest`, which the source resolves to plain white on a light
     * theme and to `#2C2C2C` on a dark one. Highest, not container: a menu is the topmost surface
     * a page has, and a container grey under it reads as a card rather than as a panel over
     * everything.
     */
    @Composable
    fun containerColor(): Color = if (MiuixTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF2C2C2C)
    } else {
        Color.White
    }
}

/**
 * A menu that opens at the corner of the control it belongs to, on glass.
 *
 * The first of the source system's three openings, and the one a plain control gets. Two edges of
 * the panel never move — its right, and whichever of its top and bottom sits against the control.
 * The other two are interpolated, and not by one scale: the width runs to the panel's width while
 * the aspect ratio separately runs from a fifth up to the panel's own, so the panel is wide and
 * flat early on and lets its height out afterwards.
 *
 * Put it at the root of the page, not beside the control — a bar clips its own contents.
 *
 * @param show Whether the popup is open.
 * @param onDismissRequest Called when a tap outside should close it.
 * @param anchorBounds The control's bounds, in this composable's own coordinate space. A second
 *   menu takes the resting bounds of the row it belongs to. Bounds read out of the first menu
 *   while it is shrunk behind this one drag this one off it.
 * @param backdrop The [Backdrop] behind the glass. `null` uses an opaque fill while retaining the
 *   configured bloom stroke and Compose shadow.
 * @param modifier The modifier applied to the panel.
 * @param sizing How wide and tall the panel may be.
 * @param visuals What its surface is made of.
 * @param cornerRadius Corner radius the panel settles at.
 * @param secondary Whether this menu was opened from a row of another one. Delegates to
 *   [GlassSecondaryPopup]'s OS4 row-to-panel geometry. Use that component directly to inherit
 *   the primary popup's button material through its `materialAnchor` parameter.
 * @param contentPadding Padding around the items.
 * @param content The items.
 */
@Composable
fun BoxScope.GlassPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    cornerRadius: Dp = GlassPopupDefaults.CornerRadius,
    secondary: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(vertical = GlassPopupDefaults.ContentPaddingVertical),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (secondary) {
        GlassSecondaryPopup(
            show = show,
            onDismissRequest = onDismissRequest,
            anchorBounds = anchorBounds,
            backdrop = backdrop,
            modifier = modifier,
            sizing = sizing,
            visuals = visuals,
            cornerRadius = cornerRadius,
            contentPadding = contentPadding,
            content = content,
        )
        return
    }
    val progress by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.popupMorph(),
        label = "glassPopupBounds",
    )
    val fade by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.popupMorphFade(show),
        label = "glassPopupFade",
    )
    val sharpness by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = GlassMotion.popupMorphBlur(),
        label = "glassPopupBlur",
    )
    val active = progress > 0.001f || show
    rememberGlassPopupBackProgress(
        show = show,
        active = active,
        enabled = show,
        retainWhenInactive = false,
        resetSpec = GlassMotion.popupMorph(),
        onDismissRequest = onDismissRequest,
    )
    if (!active) return

    val startRadius = GlassMotion.POPUP_START_CORNER_DP.dp
    val layoutDirection = LocalLayoutDirection.current
    GlassPopupSurface(
        onDismissRequest = onDismissRequest,
        backdrop = backdrop,
        modifier = modifier,
        sizing = sizing,
        visuals = visuals,
        contentPadding = contentPadding,
        panelLayer = {},
        overlay = {},
        frame = { end, page ->
            val settled = placeGlassPopup(anchorBounds, end, sizing.safeMargin.toPx(), page, layoutDirection)
            directionFrame(settled, end, progress, startRadius, cornerRadius, layoutDirection)
        },
        contentLayer = { _, _ ->
            val t = progress.coerceIn(0f, 1f)
            val scale = GlassMotion.POPUP_START_WIDTH + (1f - GlassMotion.POPUP_START_WIDTH) * t
            scaleX = scale
            scaleY = scale
            val blur = (1f - sharpness) * GlassMotion.POPUP_MORPH_BLUR_PX
            renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
            alpha = fade
        },
        scrollable = true,
        content = content,
    )
}

/** The panel's rectangle partway through the opening, with two of its edges held still. */
private fun directionFrame(
    placement: GlassPopupPlacement,
    end: Size,
    progress: Float,
    startRadius: Dp,
    endRadius: Dp,
    layoutDirection: androidx.compose.ui.unit.LayoutDirection,
): GlassPopupFrame {
    val t = progress.coerceIn(0f, 1f)
    val width = end.width * (GlassMotion.POPUP_START_WIDTH + (1f - GlassMotion.POPUP_START_WIDTH) * t)
    val endRatio = if (end.width > 0f) end.height / end.width else 1f
    val ratio = GlassMotion.POPUP_START_RATIO + (endRatio - GlassMotion.POPUP_START_RATIO) * t
    val height = (width * ratio).coerceAtMost(end.height)
    val left = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Ltr) {
        placement.rect.right - width
    } else {
        placement.rect.left
    }
    val top = if (placement.alignTop) placement.rect.top else placement.rect.bottom - height
    return GlassPopupFrame(
        rect = Rect(left, top, left + width, top + height),
        cornerRadius = startRadius + (endRadius - startRadius) * t,
    )
}

/**
 * One row of a [GlassPopup].
 *
 * Everything but the label is optional, and the source's own menus use every combination: a bare
 * label for a plain menu, a label and a tick for a list of choices, and a label stacked over a
 * summary with an icon beside it and a chevron after it for a menu whose rows lead somewhere.
 *
 * @param text The label.
 * @param onClick Called when the row is tapped.
 * @param modifier The modifier applied to the row.
 * @param icon Optional icon before the label.
 * @param summary Optional second line under the label. Usually the row's current setting.
 * @param enabled Whether the row responds to a tap.
 * @param selected Whether this row is the current choice, which puts a tick at its end.
 * @param showArrow Whether the row leads to a further menu, which puts a chevron at its end.
 * @param arrowRotation How far that chevron is turned, in degrees. A row whose menu is open turns
 *   it by [CascadingPopupDefaults.arrowRotation].
 * @param contentColor Colour of the label and the icon.
 * @param summaryColor Colour of the second line.
 * @param selectedContentColor Colour of a selected row's label and its tick.
 * @param pressedColor Fill of the block behind the row under a finger.
 */
@Composable
fun GlassPopupItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    summary: String? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    showArrow: Boolean = false,
    arrowRotation: () -> Float = { 0f },
    contentColor: Color = MiuixTheme.colorScheme.onSurfaceContainer,
    summaryColor: Color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
    selectedContentColor: Color = MiuixTheme.colorScheme.primary,
    pressedColor: Color = GlassPopupDefaults.itemPressedColor(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressFill by animateColorAsState(
        targetValue = if (pressed) pressedColor else Color.Transparent,
        animationSpec = if (pressed) snap() else GlassMotion.popupPressExit(),
        label = "glassPopupItemPress",
    )
    val color = when {
        !enabled -> contentColor.copy(alpha = 0.4f)
        selected -> selectedContentColor
        else -> contentColor
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = GlassPopupDefaults.ItemMinHeight)
            .padding(horizontal = GlassPopupDefaults.ItemPressInset)
            .background(pressFill, GlassShape(GlassPopupDefaults.ItemPressRadius))
            .selectable(
                selected = selected,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() }
            .padding(
                horizontal = GlassPopupDefaults.ItemPaddingHorizontal - GlassPopupDefaults.ItemPressInset,
                vertical = GlassPopupDefaults.ItemPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = GlassPopupDefaults.ItemIconGap)
                    .size(GlassPopupDefaults.ItemIconSize),
                tint = color,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MiuixTheme.textStyles.headline2,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MiuixTheme.textStyles.body2,
                    color = summaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = MiuixIcons.Basic.Check,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = GlassPopupDefaults.ItemIconGap)
                    .size(GlassPopupDefaults.ItemTickSize),
                tint = selectedContentColor,
            )
        }
        if (showArrow) {
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = GlassPopupDefaults.ItemIconGap)
                    .size(GlassPopupDefaults.ItemArrowSize)
                    .graphicsLayer { rotationZ = arrowRotation() },
                tint = summaryColor,
            )
        }
    }
}
