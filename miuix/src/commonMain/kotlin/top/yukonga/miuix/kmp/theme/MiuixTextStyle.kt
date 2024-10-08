package top.yukonga.miuix.kmp.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp


/**
 * The default text styles for the Miuix components.
 *
 * @param main The main text style.
 * @param title The title text style.
 * @param paragraph The paragraph text style.
 */
@Immutable
class MiuixTextStyles(
    val main: TextStyle,
    val title: TextStyle,
    val paragraph: TextStyle
)

fun miuixTextStyles(
    main: TextStyle = DefaultTextStyle,
    title: TextStyle = TitleTextStyle,
    paragraph: TextStyle = ParagraphTextStyle
): MiuixTextStyles = MiuixTextStyles(
    main = main,
    title = title,
    paragraph = paragraph
)

private val DefaultTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 17.sp,
    )

private val TitleTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 12.sp
    )

private val ParagraphTextStyle: TextStyle
    get() = TextStyle(
        fontSize = 17.sp,
        lineHeight = 1.2f.em
    )