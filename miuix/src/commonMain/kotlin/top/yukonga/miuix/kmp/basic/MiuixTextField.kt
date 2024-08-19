package top.yukonga.miuix.kmp.basic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.squircleshape.SquircleShape

/**
 * A text field component with Miuix style.
 *
 * @param value The text to be displayed in the text field.
 * @param onValueChange The callback to be called when the value changes.
 * @param modifier The modifier to be applied to the [MiuixTextField].
 * @param label The label to be displayed when the [MiuixTextField] is empty.
 * @param enabled Whether the [MiuixTextField] is enabled.
 * @param readOnly Whether the [MiuixTextField] is read-only.
 * @param textStyle The text style to be applied to the [MiuixTextField].
 * @param keyboardOptions The keyboard options to be applied to the [MiuixTextField].
 * @param keyboardActions The keyboard actions to be applied to the [MiuixTextField].
 * @param singleLine Whether the text field is single line.
 * @param maxLines The maximum number of lines allowed to be displayed in [MiuixTextField].
 * @param minLines The minimum number of lines allowed to be displayed in [MiuixTextField]. It is required
 *   that 1 <= [minLines] <= [maxLines].
 * @param visualTransformation The visual transformation to be applied to the [MiuixTextField].
 * @param onTextLayout The callback to be called when the text layout changes.
 * @param interactionSource The interaction source to be applied to the [MiuixTextField].
 */
@Composable
fun MiuixTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isSecondary: Boolean = false,
    textStyle: TextStyle = MiuixTheme.textStyles.main,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderWidth by animateDpAsState(if (isFocused) 1.6.dp else 0.dp)
    val borderColor by animateColorAsState(
        if (isFocused) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.primaryContainer
    )
    val labelOffsetY by animateDpAsState(if (value.isNotEmpty()) (-10).dp else 0.dp)
    val innerTextOffsetY by animateDpAsState(if (value.isNotEmpty()) 7.dp else 0.dp)
    val labelFontSize by animateDpAsState(if (value.isNotEmpty()) 10.dp else 16.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MiuixTheme.colorScheme.cursor),
        decorationBox = { innerTextField ->
            MiuixBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isSecondary) MiuixTheme.colorScheme.secondaryContainer else MiuixTheme.colorScheme.primaryContainer,
                        shape = SquircleShape(18.dp)
                    )
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = SquircleShape(18.dp)
                    )
            ) {
                MiuixBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    MiuixBox(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MiuixText(
                            text = label,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Medium,
                            fontSize = labelFontSize.value.sp,
                            modifier = Modifier.offset(y = labelOffsetY),
                            color = MiuixTheme.colorScheme.subTextField
                        )
                        MiuixBox(
                            modifier = Modifier
                                .offset(y = innerTextOffsetY),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            innerTextField()
                        }
                    }
                }
            }
        }
    )
}