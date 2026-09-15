// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop

/** Default values for [GlassDialog]. */
object GlassOverlayDefaults {

    /** Corner radius of a dialog. */
    val DialogCornerRadius: Dp = 28.dp

    /** Opacity of the scrim behind a dialog in a light theme. */
    val ScrimAlphaLight: Float = 0.3f

    /** Opacity of the scrim behind a dialog in a dark theme. */
    val ScrimAlphaDark: Float = 0.6f
}

/**
 * A dialog on glass, over a scrim.
 *
 * @param visible Whether the dialog is showing.
 * @param onDismissRequest Called when the scrim is tapped.
 * @param backdrop The [Backdrop] supplying the content behind the glass.
 * @param modifier The modifier applied to the box holding the scrim and the panel.
 * @param scrimColor Colour of the scrim behind the panel.
 * @param scrimAlpha Opacity of the scrim once the dialog has settled.
 * @param style The glass material.
 * @param shape The dialog's silhouette.
 * @param alpha Opacity multiplier for the material.
 * @param shadow The shadow under the dialog. `null` removes it.
 * @param stroke Optional bloom stroke along the rim.
 * @param content The dialog's content.
 */
@Composable
fun GlassDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black,
    scrimAlpha: Float = GlassOverlayDefaults.ScrimAlphaLight,
    style: GlassStyle = GlassDefaults.Style,
    shape: GlassShape = GlassShape(GlassOverlayDefaults.DialogCornerRadius),
    alpha: Float = 1f,
    stroke: GlassStroke? = null,
    shadow: GlassShadow? = GlassShadows.ExtraHigh,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrim by animateFloatAsState(
        targetValue = if (visible) scrimAlpha else 0f,
        animationSpec = if (visible) GlassMotion.dialogEnter() else GlassMotion.dialogExit(),
        label = "glassDialogScrim",
    )
    val active = visible || scrim > 0.001f
    rememberGlassPopupBackProgress(
        show = visible,
        active = active,
        enabled = visible,
        retainWhenInactive = false,
        resetSpec = GlassMotion.dialogExit(),
        onDismissRequest = onDismissRequest,
    )
    if (!active) return

    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = scrim))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDismissRequest,
                ),
        )
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
            enter = scaleIn(animationSpec = GlassMotion.dialogEnter(), initialScale = 0.86f) +
                fadeIn(animationSpec = GlassMotion.fadeIn()),
            exit = scaleOut(animationSpec = GlassMotion.dialogExit(), targetScale = 0.9f) +
                fadeOut(animationSpec = GlassMotion.fadeOut()),
        ) {
            Column(
                modifier = Modifier
                    .glassPanel(
                        backdrop = backdrop,
                        shape = shape,
                        style = style,
                        alpha = alpha,
                        stroke = stroke,
                        shadow = shadow,
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { /* Consume taps inside the dialog. */ }
                    }
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}
