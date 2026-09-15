// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/** The stock glass materials. */
object GlassStyles {

    /** Port of `GlassToken.Glass_Common_Small_Thin`. */
    @Stable
    val CommonSmallThin: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 2.0f,
            brightness = 0.24f,
            darker = 0.26f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.15f,
            colorWhite = 0.0f,
            colorMix = 0.4f,
            colorPow = 1.5f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 50.0f,
            pow = 4.0f,
            thickness = 60.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 2.0f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.4f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 1.5f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 30.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Small_Normal`. */
    @Stable
    val CommonSmallNormal: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.15f,
            saturation = 2.0f,
            brightness = 0.26f,
            darker = 0.26f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.0f,
            tint = Color(0.0f, 0.0f, 0.0f),
            tintStrength = 0.42f,
            colorWhite = 0.15f,
            colorMix = 0.4f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 16.0f,
            pow = 1.4f,
            thickness = 100.0f,
            reflectOffset = 1000.0f,
        ),
        reflect = GlassReflect(lighten = 0.75f, strength = 1.5f),
        light = GlassLight(
            directionX = 0.6f,
            directionY = -1.0f,
            directionZ = 0.0f,
            intensity = 1.0f,
            oppositeIntensity = 1.4f,
            angleRange = 0.8f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 150.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Small_Regular`. */
    @Stable
    val CommonSmallRegular: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.8f,
            curveD = 1.2f,
            amount = 0.24f,
            saturation = 1.4f,
            brightness = 0.14f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 0.9f,
        ),
        inner = GlassInner(
            bottom = 0.01f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.05f,
            colorWhite = 0.3f,
            colorMix = 0.5f,
            colorPow = 1.5f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 18.0f,
            pow = 1.6f,
            thickness = 100.0f,
            reflectOffset = 110.0f,
        ),
        reflect = GlassReflect(lighten = 0.8f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.5f,
            directionY = 0.5f,
            directionZ = 0.0f,
            intensity = 1.1f,
            oppositeIntensity = 0.5f,
            angleRange = 0.3f,
        ),
        refract = GlassRefract(ior = 1.4f),
        background = GlassBackground(
            saturation = 1.0f,
            brightness = 0.08f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 8.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Small_Semibold`. */
    @Stable
    val CommonSmallSemibold: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 2.6f,
            brightness = 0.22f,
            darker = 0.26f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.0f,
            colorWhite = 0.0f,
            colorMix = 0.4f,
            colorPow = 0.97f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 60.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 2.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.5f,
            directionY = 0.5f,
            directionZ = -0.8f,
            intensity = 1.2f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Small_Bold`. */
    @Stable
    val CommonSmallBold: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.24f,
            saturation = 1.4f,
            brightness = 0.28f,
            darker = 0.4f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.1f,
            colorWhite = 0.2f,
            colorMix = 0.3f,
            colorPow = 1.0f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.6f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 2.6f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Thin_Low`. */
    @Stable
    val CommonMediumThinLow: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 1.5f,
            brightness = 0.2f,
            darker = 0.1f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.05f,
            colorWhite = 0.0f,
            colorMix = 0.3f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 60.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.2f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 2.0f),
        background = GlassBackground(
            saturation = 1.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Thin_High`. */
    @Stable
    val CommonMediumThinHigh: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.6f,
            curveC = 0.0f,
            curveD = 0.3f,
            amount = 0.2f,
            saturation = 1.5f,
            brightness = 0.24f,
            darker = 0.1f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.1f,
            colorWhite = 0.0f,
            colorMix = 0.3f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 60.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 0.9f,
            oppositeIntensity = 0.3f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Regular_Low_Light`. */
    @Stable
    val CommonMediumRegularLowLight: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 2.0f,
            brightness = 0.2f,
            darker = 0.0f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.04f,
            tint = Color(0.9f, 0.9f, 0.9f),
            tintStrength = 1.0f,
            colorWhite = 0.0f,
            colorMix = 1.0f,
            colorPow = 1.0f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.4f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Regular_High_Light`. */
    @Stable
    val CommonMediumRegularHighLight: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 2.0f,
            brightness = 0.1f,
            darker = 0.0f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.02f,
            tint = Color(0.9f, 0.9f, 0.9f),
            tintStrength = 1.0f,
            colorWhite = 0.0f,
            colorMix = 1.0f,
            colorPow = 1.0f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.4f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Regular_Dark`. */
    @Stable
    val CommonMediumRegularDark: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 3.0f,
            brightness = 0.2f,
            darker = 0.0f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.04f,
            tint = Color(0.06f, 0.06f, 0.06f),
            tintStrength = 0.4f,
            colorWhite = 0.0f,
            colorMix = 0.8f,
            colorPow = 1.5f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 2.4f,
            oppositeIntensity = 1.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Bold_Light`. */
    @Stable
    val CommonMediumBoldLight: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.5f,
            saturation = 1.2f,
            brightness = 0.06f,
            darker = 0.1f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 2.0f,
            colorWhite = 0.0f,
            colorMix = 0.3f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 72.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.0f,
            oppositeIntensity = 0.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Bold_Dark`. */
    @Stable
    val CommonMediumBoldDark: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 2.0f,
            brightness = 0.14f,
            darker = 0.1f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.02f,
            tint = Color(0.27f, 0.27f, 0.27f),
            tintStrength = 0.6f,
            colorWhite = 0.0f,
            colorMix = 0.2f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 72.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 2.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Medium_Bold_Low`. */
    @Stable
    val CommonMediumBoldLow: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 2.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.2f,
            saturation = 1.2f,
            brightness = 0.0f,
            darker = 0.4f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.02f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.1f,
            colorWhite = 0.26f,
            colorMix = 0.3f,
            colorPow = 1.8f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 50.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1000.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 0.8f,
            oppositeIntensity = 0.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Large_Regular`. */
    @Stable
    val CommonLargeRegular: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.15f,
            saturation = 2.2f,
            brightness = 0.08f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.1f,
            colorWhite = 0.15f,
            colorMix = 0.4f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 50.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.6f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.6f,
            oppositeIntensity = 0.8f,
            angleRange = 1.0f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 80.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Common_Large_Demibold`. */
    @Stable
    val CommonLargeDemibold: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 1.55f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.56f,
            saturation = 2.0f,
            brightness = 0.18f,
            darker = 0.26f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 1.0f,
            colorWhite = 0.6f,
            colorMix = 0.9f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 50.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.6f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 2.0f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 50.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Bionic_Medium_Thin`. */
    @Stable
    val BionicMediumThin: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.15f,
            saturation = 1.4f,
            brightness = 0.08f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.15f,
            colorWhite = 0.15f,
            colorMix = 0.4f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 60.0f,
            reflectOffset = 1000.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.2f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 2.4f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Bionic_Medium_Normal`. */
    @Stable
    val BionicMediumNormal: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.24f,
            saturation = 1.4f,
            brightness = -0.02f,
            darker = 0.3f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.1f,
            colorWhite = 0.2f,
            colorMix = 0.3f,
            colorPow = 1.0f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.6f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 2.6f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Bionic_Medium_Normal_Motion`. */
    @Stable
    val BionicMediumNormalMotion: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.24f,
            saturation = 1.4f,
            brightness = -0.02f,
            darker = 0.3f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.1f,
            colorWhite = 0.2f,
            colorMix = 0.3f,
            colorPow = 1.0f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.6f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 2.6f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Bionic_Medium_Regular`. */
    @Stable
    val BionicMediumRegular: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.78f,
            curveB = 0.36f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.3f,
            saturation = 1.8f,
            brightness = -0.18f,
            darker = 0.1f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.1f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.15f,
            colorWhite = 0.16f,
            colorMix = 0.3f,
            colorPow = 1.1f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 2.6f,
            oppositeIntensity = 1.2f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 2.0f,
            brightness = -0.2f,
            burn = 1.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Bionic_Medium_Demibold_Motion`. */
    @Stable
    val BionicMediumDemiboldMotion: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.2f,
            saturation = 2.0f,
            brightness = -0.02f,
            darker = 0.3f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 0.2f,
            colorWhite = 0.0f,
            colorMix = 0.2f,
            colorPow = 0.4f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 2.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 2.6f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Bionic_Medium_Semibold_Motion`. */
    @Stable
    val BionicMediumSemiboldMotion: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 1.3f,
            curveB = 0.64f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.52f,
            saturation = 2.0f,
            brightness = -0.02f,
            darker = 0.3f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(1.0f, 1.0f, 1.0f),
            tintStrength = 1.4f,
            colorWhite = 0.15f,
            colorMix = 0.9f,
            colorPow = 0.4f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 2.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 2.6f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Purple`. */
    @Stable
    val TintPurple: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.2f,
            curveC = 0.06f,
            curveD = 1.0f,
            amount = 0.3f,
            saturation = 1.0f,
            brightness = 0.6f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.0f,
            tint = Color(0.44f, 0.37f, 1.0f),
            tintStrength = 0.6f,
            colorWhite = 0.5f,
            colorMix = 0.8f,
            colorPow = 1.6f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.4f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Orange`. */
    @Stable
    val TintOrange: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.2f,
            curveC = 0.06f,
            curveD = 1.0f,
            amount = 0.3f,
            saturation = 1.0f,
            brightness = 0.6f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.0f,
            tint = Color(0.99f, 0.41f, 0.15f),
            tintStrength = 0.6f,
            colorWhite = 0.5f,
            colorMix = 0.8f,
            colorPow = 1.6f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.0f,
            oppositeIntensity = 0.8f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Blue`. */
    @Stable
    val TintBlue: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.2f,
            curveC = 0.06f,
            curveD = 1.0f,
            amount = 0.3f,
            saturation = 1.0f,
            brightness = 0.6f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(0.35f, 0.45f, 0.65f),
            tintStrength = 0.8f,
            colorWhite = 0.5f,
            colorMix = 0.6f,
            colorPow = 1.45f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 0.75f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.4f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Yellow`. */
    @Stable
    val TintYellow: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.2f,
            curveC = 0.06f,
            curveD = 1.0f,
            amount = 0.3f,
            saturation = 1.0f,
            brightness = 0.6f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(0.99f, 0.78f, 0.15f),
            tintStrength = 0.6f,
            colorWhite = 0.5f,
            colorMix = 0.6f,
            colorPow = 1.45f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 0.75f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.0f,
            oppositeIntensity = 0.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Brown`. */
    @Stable
    val TintBrown: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.2f,
            curveC = 0.06f,
            curveD = 1.0f,
            amount = 0.3f,
            saturation = 2.0f,
            brightness = 0.6f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(0.38f, 0.26f, 0.22f),
            tintStrength = 0.8f,
            colorWhite = 0.5f,
            colorMix = 0.6f,
            colorPow = 1.45f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 0.75f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.8f,
            oppositeIntensity = 1.2f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 36.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Black_Thin`. */
    @Stable
    val TintBlackThin: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.15f,
            saturation = 2.0f,
            brightness = 0.18f,
            darker = 0.26f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.05f,
            tint = Color(0.0f, 0.0f, 0.0f),
            tintStrength = 0.3f,
            colorWhite = 0.15f,
            colorMix = 0.4f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 50.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.6f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 2.0f,
            oppositeIntensity = 1.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 50.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Black_Normal`. */
    @Stable
    val TintBlackNormal: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.15f,
            saturation = 2.0f,
            brightness = 0.3f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.02f,
            tint = Color(0.06f, 0.06f, 0.06f),
            tintStrength = 0.5f,
            colorWhite = 0.15f,
            colorMix = 0.4f,
            colorPow = 1.36f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 72.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1000.0f,
        ),
        reflect = GlassReflect(lighten = 1.4f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 2.6f,
            oppositeIntensity = 1.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 3.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Black_Regular`. */
    @Stable
    val TintBlackRegular: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.2f,
            saturation = 1.4f,
            brightness = 0.1f,
            darker = 0.3f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.02f,
            tint = Color(0.06f, 0.06f, 0.06f),
            tintStrength = 0.7f,
            colorWhite = 0.2f,
            colorMix = 0.3f,
            colorPow = 1.0f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.4f,
            oppositeIntensity = 2.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 4.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Black_Demibold`. */
    @Stable
    val TintBlackDemibold: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.15f,
            saturation = 3.0f,
            brightness = 0.3f,
            darker = 0.2f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.0f,
            tint = Color(0.06f, 0.06f, 0.06f),
            tintStrength = 0.6f,
            colorWhite = 0.15f,
            colorMix = 0.4f,
            colorPow = 1.36f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1000.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 2.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Black_Heavy`. */
    @Stable
    val TintBlackHeavy: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 2.0f,
            curveB = 2.0f,
            curveC = 0.5f,
            curveD = 0.8f,
            amount = 0.2f,
            saturation = 1.2f,
            brightness = 0.0f,
            darker = 0.4f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.02f,
            tint = Color(0.06f, 0.06f, 0.06f),
            tintStrength = 0.4f,
            colorWhite = 0.26f,
            colorMix = 0.3f,
            colorPow = 1.8f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 72.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1000.0f,
        ),
        reflect = GlassReflect(lighten = 1.0f, strength = 0.8f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.6f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 3.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 40.0f, big = 500.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Red_Motion`. */
    @Stable
    val TintRedMotion: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.8f,
            saturation = 2.0f,
            brightness = 0.2f,
            darker = 0.6f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.06f,
            tint = Color(1.0f, 0.31f, 0.27f),
            tintStrength = 1.0f,
            colorWhite = 0.3f,
            colorMix = 1.0f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Green_Motion`. */
    @Stable
    val TintGreenMotion: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.8f,
            saturation = 2.0f,
            brightness = 0.2f,
            darker = 0.6f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.06f,
            tint = Color(0.25f, 0.84f, 0.35f),
            tintStrength = 1.0f,
            colorWhite = 0.3f,
            colorMix = 1.0f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Blue_Motion`. */
    @Stable
    val TintBlueMotion: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.42f,
            curveB = 0.22f,
            curveC = 0.24f,
            curveD = 0.0f,
            amount = 0.8f,
            saturation = 2.0f,
            brightness = 0.2f,
            darker = 0.6f,
            darkerStart = 0.6f,
            darkerEnd = 1.0f,
        ),
        inner = GlassInner(
            bottom = 0.06f,
            tint = Color(0.2f, 0.51f, 1.0f),
            tintStrength = 1.0f,
            colorWhite = 0.3f,
            colorMix = 1.0f,
            colorPow = 1.2f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 80.0f,
            reflectOffset = 1200.0f,
        ),
        reflect = GlassReflect(lighten = 1.2f, strength = 1.0f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 3.0f,
            oppositeIntensity = 1.0f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 4.0f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 120.0f, big = 120.0f),
    )

    /** Port of `GlassToken.Glass_Tint_Blue_Normal`. */
    @Stable
    val TintBlueNormal: GlassStyle = GlassStyle(
        blend = GlassBlend(
            curveA = 0.5f,
            curveB = 1.0f,
            curveC = 0.0f,
            curveD = 1.0f,
            amount = 0.2f,
            saturation = 2.0f,
            brightness = 1.0f,
            darker = 0.0f,
            darkerStart = 0.6f,
            darkerEnd = 0.8f,
        ),
        inner = GlassInner(
            bottom = 0.0f,
            tint = Color(0.2f, 0.51f, 1.0f),
            tintStrength = 1.5f,
            colorWhite = 0.0f,
            colorMix = 0.4f,
            colorPow = 0.92f,
            alpha = 1.0f,
        ),
        edge = GlassEdge(
            width = 60.0f,
            pow = 4.0f,
            thickness = 60.0f,
            reflectOffset = 600.0f,
        ),
        reflect = GlassReflect(lighten = 2.0f, strength = 0.6f),
        light = GlassLight(
            directionX = -0.4f,
            directionY = 0.6f,
            directionZ = -0.8f,
            intensity = 1.0f,
            oppositeIntensity = 0.6f,
            angleRange = 1.4f,
        ),
        refract = GlassRefract(ior = 1.5f),
        background = GlassBackground(
            saturation = 0.0f,
            brightness = 0.0f,
            burn = 0.0f,
            unShade = 0.0f,
        ),
        blur = GlassBlur(small = 30.0f, big = 500.0f),
    )

    /**
     * Picks the light or the dark half of a theme pair.
     *
     * @param isDark True to return [dark].
     * @param light The style for a light theme.
     * @param dark The style for a dark theme.
     */
    @Stable
    fun forTheme(isDark: Boolean, light: GlassStyle, dark: GlassStyle): GlassStyle = if (isDark) dark else light
}
