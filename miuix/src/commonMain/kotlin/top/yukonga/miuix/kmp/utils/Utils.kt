package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Window size data class.
 */
data class WindowSize(val width: Int, val height: Int)

/**
 * Returns the current window size.
 */
@Composable
expect fun getWindowSize(): WindowSize

/**
 * Platform enum class.
 */
enum class Platform {
    Android,
    IOS,
    Desktop,
    WasmJs,
    MacOS,
    Js
}

/**
 * Returns the current platform name.
 */
expect fun platform(): Platform

/**
 * Returns the rounded corner of the current device.
 */
@Composable
expect fun getRoundedCorner(): Dp

/**
 * Handles the back event.
 */
@Composable
expect fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
)