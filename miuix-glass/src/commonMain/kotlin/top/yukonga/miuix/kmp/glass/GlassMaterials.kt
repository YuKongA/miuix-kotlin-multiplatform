// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** The stock materials, ported from the source system's material tokens. */
object GlassMaterials {

    /**
     * Port of `internal-pured-thin-glass`, light: what the source system's floating bars and
     * popups are made of over a light page.
     */
    @Stable
    val PuredThinGlassLight: GlassMaterial = GlassMaterial(
        blurRadius = 20.dp,
        first = GlassColorLayer(Color(0x05000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x99FFFFFF), GlassColorBlendMode.SoftLight),
        third = GlassColorLayer(Color(0x66FFFFFF), GlassColorBlendMode.HardLight),
    )

    /** Port of `internal-pured-thin-glass`, dark. */
    @Stable
    val PuredThinGlassDark: GlassMaterial = GlassMaterial(
        blurRadius = 20.dp,
        first = GlassColorLayer(Color(0x1A000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x66565656), GlassColorBlendMode.Luminosity),
        third = GlassColorLayer(Color(0x993F3F3F), GlassColorBlendMode.Overlay),
    )

    /** Port of `popupview-glass`, light: pured-thin colour layers with a 60dp mask blur. */
    @Stable
    val PopupViewGlassLight: GlassMaterial = PuredThinGlassLight.copy(blurRadius = 60.dp)

    /** Port of `popupview-glass`, dark: pured-thin colour layers with a 60dp mask blur. */
    @Stable
    val PopupViewGlassDark: GlassMaterial = PuredThinGlassDark.copy(blurRadius = 60.dp)

    /** Port of the OS4 action bar's `mask-pured-regular`, light. */
    @Stable
    val ActionBarMaskLight: GlassMaterial = GlassMaterial(
        blurRadius = 40.dp,
        first = GlassColorLayer(Color(0x33F9F9F9), GlassColorBlendMode.Overlay),
        second = GlassColorLayer(Color(0xB3FFFFFF), GlassColorBlendMode.HardLight),
    )

    /** Port of the OS4 action bar's `mask-pured-regular`, dark. */
    @Stable
    val ActionBarMaskDark: GlassMaterial = GlassMaterial(
        blurRadius = 60.dp,
        first = GlassColorLayer(Color(0x75000000), GlassColorBlendMode.ColorBurn),
        second = GlassColorLayer(Color(0x52000000), GlassColorBlendMode.SrcOver),
    )

    /** Picks the half of a theme pair that belongs to the current theme. */
    @Stable
    fun forTheme(isDark: Boolean, light: GlassMaterial, dark: GlassMaterial): GlassMaterial = if (isDark) dark else light

    /** [PuredThinGlassLight] or [PuredThinGlassDark], for the current theme. */
    @Stable
    fun puredThinGlass(isDark: Boolean): GlassMaterial = forTheme(isDark, PuredThinGlassLight, PuredThinGlassDark)

    /** [PopupViewGlassLight] or [PopupViewGlassDark], for the current theme. */
    @Stable
    fun popupViewGlass(isDark: Boolean): GlassMaterial = forTheme(isDark, PopupViewGlassLight, PopupViewGlassDark)

    /** [ActionBarMaskLight] or [ActionBarMaskDark], for the current theme. */
    @Stable
    fun actionBarMask(isDark: Boolean): GlassMaterial = forTheme(isDark, ActionBarMaskLight, ActionBarMaskDark)
}
