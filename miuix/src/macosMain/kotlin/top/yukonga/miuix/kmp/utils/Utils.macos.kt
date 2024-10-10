package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.AppKit.NSEvent
import platform.AppKit.NSEventMaskKeyDown

@Composable
@OptIn(ExperimentalComposeUiApi::class)
actual fun getWindowSize(): WindowSize {
    val window = LocalWindowInfo.current
    return WindowSize(
        width = window.containerSize.width,
        height = window.containerSize.height
    )
}

actual fun platform(): Platform = Platform.MacOS

@Composable
actual fun getRoundedCorner(): Dp = 0.dp

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    if (!enabled) return
    DisposableEffect(Unit) {
        val monitor = NSEvent.addLocalMonitorForEventsMatchingMask(NSEventMaskKeyDown) { event ->
            if (event?.keyCode == 53.toUShort()) {
                onBack()
                null
            } else {
                event
            }
        }

        onDispose {
            if (monitor != null) {
                NSEvent.removeMonitor(monitor)
            }
        }
    }
}