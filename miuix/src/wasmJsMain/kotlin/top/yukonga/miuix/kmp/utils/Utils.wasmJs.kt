package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import org.w3c.dom.events.KeyboardEvent

@Composable
@OptIn(ExperimentalComposeUiApi::class)
actual fun getWindowSize(): WindowSize {
    val window = LocalWindowInfo.current
    return WindowSize(
        width = window.containerSize.width,
        height = window.containerSize.height
    )
}

actual fun platform(): Platform = Platform.WasmJs

@Composable
actual fun getRoundedCorner(): Dp = 0.dp

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    if (!enabled) return
    LaunchedEffect(Unit) {
        window.addEventListener("keydown") { event ->
            if ((event as KeyboardEvent).key == "Escape") {
                onBack()
            }
        }
    }
}