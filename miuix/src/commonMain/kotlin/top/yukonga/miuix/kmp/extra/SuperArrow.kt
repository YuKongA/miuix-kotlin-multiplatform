package top.yukonga.miuix.kmp.extra

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A arrow with a title and a summary.
 *
 * @param modifier The modifier to be applied to the [SuperArrow].
 * @param title The title of the [SuperArrow].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperArrow].
 * @param leftAction The [Composable] content that on the left side of the [SuperArrow].
 * @param rightText The text on the right side of the [SuperArrow].
 * @param onClick The callback when the [SuperArrow] is clicked.
 * @param insideMargin The margin inside the [SuperArrow].
 */
@Composable
fun SuperArrow(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = MiuixTheme.colorScheme.onSurface,
    summary: String? = null,
    leftAction: @Composable (() -> Unit)? = null,
    rightText: String? = null,
    onClick: (() -> Unit)? = null,
    insideMargin: DpSize = DpSize(16.dp, 16.dp)
) {
    val updatedOnClick by rememberUpdatedState(onClick)
    BasicComponent(
        modifier = modifier,
        insideMargin = insideMargin,
        title = title,
        titleColor = titleColor,
        summary = summary,
        leftAction = leftAction,
        rightActions = { createRightActions(rightText) },
        onClick = updatedOnClick
    )
}

/**
 * Create the right actions of the [SuperArrow].
 *
 * @param rightText The text on the right side of the [SuperArrow].
 */
@Composable
private fun createRightActions(rightText: String?) {
    if (rightText != null) {
        Text(
            text = rightText,
            fontSize = 15.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
            textAlign = TextAlign.End,
        )
    }
    Image(
        modifier = Modifier
            .size(15.dp)
            .padding(start = 6.dp),
        imageVector = MiuixIcons.ArrowRight,
        contentDescription = null,
        colorFilter = BlendModeColorFilter(MiuixTheme.colorScheme.onSurfaceVariantActions, BlendMode.SrcIn),
    )
}