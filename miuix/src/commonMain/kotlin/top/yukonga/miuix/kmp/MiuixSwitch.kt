package top.yukonga.miuix.kmp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.ui.MiuixTheme

@Composable
fun MiuixSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val backgroundColor by animateColorAsState(
        if (checked) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.primaryContainer
    )

    val toggleableModifier =
        if (onCheckedChange != null) {
            Modifier.minimumInteractiveComponentSize()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    enabled = enabled,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = null
                )
        } else {
            Modifier
        }

    Box(
        modifier = modifier
            .then(toggleableModifier)
            .wrapContentSize(Alignment.Center)
            .requiredSize(52.dp, 28.5.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .align(Alignment.CenterStart)
                .size(20.dp)
                .background(Color.White, shape = RoundedCornerShape(50.dp))
        )
    }
}