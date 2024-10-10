package top.yukonga.miuix.kmp.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Box
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.BackHandler
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog
import top.yukonga.miuix.kmp.utils.getRoundedCorner
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.squircleshape.SquircleShape

/**
 * A dialog with a title, a summary, and a content.
 *
 * @param modifier The modifier to be applied to the [SuperDialog].
 * @param title The title of the [SuperDialog].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperDialog].
 * @param summaryColor The color of the summary.
 * @param backgroundColor The background color of the [SuperDialog].
 * @param show The state of the [SuperDialog].
 * @param onDismissRequest The callback when the [SuperDialog] is dismissed.
 * @param outsideMargin The margin outside the [SuperDialog].
 * @param insideMargin The margin inside the [SuperDialog].
 * @param defaultWindowInsetsPadding Whether to apply default window insets padding to the [SuperDialog].
 * @param content The [Composable] content of the [SuperDialog].
 */
@Composable
fun SuperDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = MiuixTheme.colorScheme.onSurface,
    summary: String? = null,
    summaryColor: Color = MiuixTheme.colorScheme.onSurfaceVariantDialog,
    backgroundColor: Color = MiuixTheme.colorScheme.surfaceVariant,
    show: MutableState<Boolean>,
    onDismissRequest: () -> Unit,
    outsideMargin: DpSize = DpSize(12.dp, 12.dp),
    insideMargin: Dp = 24.dp,
    defaultWindowInsetsPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val getWindowSize by rememberUpdatedState(getWindowSize())
    val windowWidth = getWindowSize.width.dp / density.density
    val windowHeight = getWindowSize.height.dp / density.density
    val paddingModifier = remember(outsideMargin) { Modifier.padding(horizontal = outsideMargin.width).padding(bottom = outsideMargin.height) }
    val roundedCorner by rememberUpdatedState(getRoundedCorner())
    val bottomCornerRadius by remember { derivedStateOf { if (roundedCorner != 0.dp) roundedCorner - outsideMargin.width else 32.dp } }
    val contentAlignment by rememberUpdatedState { derivedStateOf { if (windowHeight >= 480.dp && windowWidth >= 840.dp) Alignment.Center else Alignment.BottomCenter } }

    if (!dialogStates.contains(show)) dialogStates.add(show)
    LaunchedEffect(show.value) {
        if (show.value) {
            dialogStates.forEach { state -> if (state != show) state.value = false }
        }
    }

    BackHandler(enabled = show.value) {
        dismissDialog()
        onDismissRequest()
    }

    Box(
        modifier = if (defaultWindowInsetsPadding) {
            Modifier
                .imePadding()
                .navigationBarsPadding()
        } else {
            Modifier
        }
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        dismissDialog()
                        onDismissRequest()
                    }
                )
            }
            .then(paddingModifier)
    ) {
        Column(
            modifier = modifier
                .widthIn(max = 420.dp)
                .pointerInput(Unit) {
                    detectTapGestures { /* Do nothing to consume the click */ }
                }
                .align(contentAlignment.invoke().value)
                .graphicsLayer(
                    shape = SquircleShape(bottomCornerRadius),
                    clip = false
                )
                .background(
                    color = backgroundColor,
                    shape = SquircleShape(bottomCornerRadius)
                )
                .padding(insideMargin),
        ) {
            title?.let {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    text = it,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = titleColor
                )
            }
            summary?.let {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    text = it,
                    textAlign = TextAlign.Center,
                    color = summaryColor
                )
            }
            content()
        }
    }
}

/**
 * Only one dialog is allowed to be displayed at a time.
 */
val dialogStates = mutableStateListOf<MutableState<Boolean>>()