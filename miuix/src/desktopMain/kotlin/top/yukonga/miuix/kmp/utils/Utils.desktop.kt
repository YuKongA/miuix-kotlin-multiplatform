package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

@Composable
@OptIn(ExperimentalComposeUiApi::class)
actual fun getWindowSize(): WindowSize {
    val window = LocalWindowInfo.current
    return WindowSize(
        width = window.containerSize.width,
        height = window.containerSize.height
    )
}

actual fun platform(): Platform = Platform.Desktop

@Composable
actual fun getRoundedCorner(): Dp = 0.dp

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    if (!enabled) return
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { event ->
        if (event.keyCode == KeyEvent.VK_ESCAPE) {
            onBack()
        }
        false
    }
}