package top.yukonga.miuix.kmp.basic

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A title component with Miuix style.
 *
 * @param text The text to be displayed in the title.
 * @param modifier The modifier to be applied to the [SmallTitle].
 * @param insideMargin The margin inside the [SmallTitle].
 */
@Composable
fun SmallTitle(
    text: String,
    modifier: Modifier = Modifier,
    insideMargin: DpSize = DpSize(28.dp, 8.dp),
) {
    val paddingModifier = remember(insideMargin) {
        Modifier.padding(horizontal = insideMargin.width, vertical = insideMargin.height)
    }
    Text(
        modifier = modifier.then(paddingModifier),
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MiuixTheme.colorScheme.onBackgroundVariant
    )
}